package com.ruoyi.mall.member.service;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.domain.MallCaptchaChallenge;
import com.ruoyi.mall.member.mapper.MallMemberAuthMapper;

/** Risk-triggered challenge and one-time ticket boundary for member SMS login. */
@Service
public class MallMemberCaptchaService
{
    private static final long RISK_WINDOW_MILLIS = 10 * 60 * 1000L;
    private static final long CHALLENGE_VALID_MILLIS = 2 * 60 * 1000L;
    private static final long TICKET_VALID_MILLIS = 2 * 60 * 1000L;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int PHONE_WINDOW_LIMIT = 3;
    private static final int IP_WINDOW_LIMIT = 8;
    private static final int DEVICE_WINDOW_LIMIT = 5;
    private static final int DAILY_PHONE_RISK_LIMIT = 3;
    private static final int DAILY_IP_RISK_LIMIT = 15;
    private static final int POSITION_SCALE = 10;
    private static final int PIECE_START_POSITION = 120;
    private static final int MIN_TARGET_POSITION = 180;
    private static final int MAX_TARGET_POSITION = 820;
    private static final int VERIFY_TOLERANCE = 10;
    private static final int SCENE_WIDTH = 320;
    private static final int SCENE_HEIGHT = 188;
    private static final int GAP_SIZE = 50;
    private static final String WINDOW_PREFIX = "mall_member:sms:risk:";
    private final SecureRandom secureRandom = new SecureRandom();
    private final MallMemberAuthMapper authMapper;
    private final RedisTemplate<Object, Object> redisTemplate;

    public MallMemberCaptchaService(MallMemberAuthMapper authMapper,
            RedisTemplate<Object, Object> redisTemplate)
    {
        this.authMapper = authMapper;
        this.redisTemplate = redisTemplate;
    }

    public boolean requiresChallenge(String phone, String requestIp, String deviceIdentifier)
    {
        long phoneCount = incrementWindow("phone", phone);
        long ipCount = incrementWindow("ip", safe(requestIp));
        long deviceCount = incrementWindow("device", safe(deviceIdentifier));
        Date startOfDay = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        return phoneCount >= PHONE_WINDOW_LIMIT || ipCount >= IP_WINDOW_LIMIT || deviceCount >= DEVICE_WINDOW_LIMIT
                || authMapper.countSmsByPhoneSince(phone, startOfDay) >= DAILY_PHONE_RISK_LIMIT
                || authMapper.countSmsByIpSince(requestIp, startOfDay) >= DAILY_IP_RISK_LIMIT;
    }

    public Map<String, Object> createChallenge(String phone, String requestIp, String deviceIdentifier)
    {
        String challengeKey = randomHex(32);
        int targetPosition = MIN_TARGET_POSITION
                + secureRandom.nextInt(MAX_TARGET_POSITION - MIN_TARGET_POSITION + 1);
        MallCaptchaChallenge challenge = new MallCaptchaChallenge();
        challenge.setChallengeKey(challengeKey);
        challenge.setPhone(phone);
        challenge.setRequestIp(safe(requestIp));
        challenge.setDeviceIdentifier(safe(deviceIdentifier));
        challenge.setAnswerHash(hash(challengeKey + ":" + targetPosition));
        challenge.setStatus("ISSUED");
        challenge.setVerifyAttempts(0);
        challenge.setExpireTime(new Date(System.currentTimeMillis() + CHALLENGE_VALID_MILLIS));
        authMapper.insertCaptchaChallenge(challenge);

        ChallengeScene scene = createChallengeScene(targetPosition);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("challengeId", challengeKey);
        response.put("pieceX", PIECE_START_POSITION);
        response.put("positionScale", POSITION_SCALE);
        response.put("sceneImage", scene.backgroundImage());
        response.put("pieceImage", scene.pieceImage());
        response.put("expiresIn", CHALLENGE_VALID_MILLIS / 1000);
        return response;
    }

    public Map<String, Object> verifyChallenge(String challengeKey, int position,
            String requestIp, String deviceIdentifier)
    {
        MallCaptchaChallenge challenge = authMapper.selectCaptchaChallenge(challengeKey);
        if (challenge == null || !"ISSUED".equals(challenge.getStatus())
                || challenge.getExpireTime() == null || challenge.getExpireTime().before(new Date()))
        {
            throw new ServiceException("验证已失效，请重新获取");
        }
        if (!safe(challenge.getRequestIp()).equals(safe(requestIp))
                || !safe(challenge.getDeviceIdentifier()).equals(safe(deviceIdentifier)))
        {
            throw new ServiceException("验证环境已变化，请重新获取");
        }
        if (challenge.getVerifyAttempts() != null && challenge.getVerifyAttempts() >= MAX_VERIFY_ATTEMPTS)
        {
            throw new ServiceException("验证失败次数过多，请重新获取");
        }
        if (!matchesAnswer(challengeKey, position, challenge.getAnswerHash()))
        {
            int attempt = challenge.getVerifyAttempts() == null ? 1 : challenge.getVerifyAttempts() + 1;
            authMapper.incrementCaptchaAttempts(challenge.getChallengeId());
            if (attempt >= MAX_VERIFY_ATTEMPTS) authMapper.failCaptchaChallenge(challenge.getChallengeId());
            throw new ServiceException("拼图位置不正确，请重试");
        }

        String ticket = randomHex(32);
        Date ticketExpireTime = new Date(System.currentTimeMillis() + TICKET_VALID_MILLIS);
        if (authMapper.verifyCaptchaChallenge(challenge.getChallengeId(), hash(ticket), ticketExpireTime) != 1)
        {
            throw new ServiceException("验证状态已变化，请重新获取");
        }
        return Map.of("ticket", ticket, "expiresIn", TICKET_VALID_MILLIS / 1000);
    }

    public void consumeTicket(String ticket, String phone, String requestIp, String deviceIdentifier)
    {
        if (ticket == null || ticket.isBlank() || authMapper.consumeCaptchaTicket(hash(ticket), phone,
                safe(deviceIdentifier), safe(requestIp)) != 1)
        {
            throw new ServiceException("安全验证已失效，请重新完成验证");
        }
    }

    private long incrementWindow(String type, String value)
    {
        String key = WINDOW_PREFIX + type + ":" + value;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L)
        {
            redisTemplate.expire(key, RISK_WINDOW_MILLIS, TimeUnit.MILLISECONDS);
        }
        return count == null ? 1L : count;
    }

    private String randomHex(int bytes)
    {
        byte[] value = new byte[bytes];
        secureRandom.nextBytes(value);
        return HexFormat.of().formatHex(value);
    }

    private boolean matchesAnswer(String challengeKey, int position, String answerHash)
    {
        for (int offset = -VERIFY_TOLERANCE; offset <= VERIFY_TOLERANCE; offset++)
        {
            int candidate = position + offset;
            if (candidate < 0 || candidate > 100 * POSITION_SCALE) continue;
            String expected = hash(challengeKey + ":" + candidate);
            if (MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                    answerHash.getBytes(StandardCharsets.US_ASCII))) return true;
        }
        return false;
    }

    private ChallengeScene createChallengeScene(int targetPosition)
    {
        BufferedImage image = new BufferedImage(SCENE_WIDTH, SCENE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try
        {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(247, 164, 140));
            graphics.fillRect(0, 0, SCENE_WIDTH, SCENE_HEIGHT);

            graphics.setColor(new Color(255, 255, 255, 92));
            for (int x = 0; x < SCENE_WIDTH; x += 42) graphics.drawLine(x, 0, x, SCENE_HEIGHT);
            for (int y = 0; y < SCENE_HEIGHT; y += 42) graphics.drawLine(0, y, SCENE_WIDTH, y);

            graphics.setColor(new Color(255, 209, 81));
            graphics.fillOval(SCENE_WIDTH - 72, 22, 44, 44);
            graphics.setColor(new Color(245, 93, 72));
            graphics.fillPolygon(new Polygon(
                    new int[] { 26, 138, 186, 221, 276 },
                    new int[] { SCENE_HEIGHT, 54, 118, 82, SCENE_HEIGHT }, 5));

            graphics.setColor(new Color(32, 32, 42, 36));
            for (int i = 0; i < 42; i++)
            {
                int size = 3 + secureRandom.nextInt(12);
                graphics.fillOval(secureRandom.nextInt(SCENE_WIDTH - size),
                        secureRandom.nextInt(SCENE_HEIGHT - size), size, size);
            }

            int centerX = (int) Math.round((targetPosition / (double) POSITION_SCALE / 100D) * SCENE_WIDTH);
            int gapX = Math.max(0, Math.min(SCENE_WIDTH - GAP_SIZE, centerX - GAP_SIZE / 2));
            int gapY = SCENE_HEIGHT - GAP_SIZE;
            BufferedImage piece = new BufferedImage(GAP_SIZE, GAP_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D pieceGraphics = piece.createGraphics();
            try
            {
                pieceGraphics.drawImage(image, 0, 0, GAP_SIZE, GAP_SIZE,
                        gapX, gapY, gapX + GAP_SIZE, gapY + GAP_SIZE, null);
            }
            finally
            {
                pieceGraphics.dispose();
            }

            graphics.setColor(new Color(32, 32, 42, 148));
            graphics.fillRect(gapX, gapY, GAP_SIZE, GAP_SIZE);
            graphics.setColor(new Color(255, 248, 234, 210));
            graphics.setStroke(new BasicStroke(2F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10F, new float[] { 6F, 4F }, 0F));
            graphics.drawRect(gapX + 1, gapY + 1, GAP_SIZE - 2, GAP_SIZE - 2);

            return new ChallengeScene(encodePng(image), encodePng(piece));
        }
        finally
        {
            graphics.dispose();
        }
    }

    private String encodePng(BufferedImage image)
    {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            if (!ImageIO.write(image, "png", output)) throw new IOException("PNG writer unavailable");
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(output.toByteArray());
        }
        catch (IOException exception)
        {
            throw new ServiceException("安全验证图片生成失败，请稍后重试");
        }
    }

    private record ChallengeScene(String backgroundImage, String pieceImage)
    {
    }

    private String hash(String value)
    {
        try
        {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private String safe(String value)
    {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
