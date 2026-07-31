package com.ruoyi.mall.member.adapter;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.port.MallAvatarStoragePort;

@Component
public class LocalMallAvatarStorageAdapter implements MallAvatarStoragePort
{
    static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;
    static final long MAX_PIXELS = 25_000_000L;
    static final int OUTPUT_SIZE = 512;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private final Path profileRoot;

    public LocalMallAvatarStorageAdapter(@Value("${ruoyi.profile}") String profileRoot)
    {
        this.profileRoot = Path.of(profileRoot).toAbsolutePath().normalize();
    }

    @Override
    public String store(Long memberId, MultipartFile file)
    {
        validateDeclaration(file);
        try
        {
            BufferedImage source = decodeVerifiedImage(file);
            BufferedImage avatar = cropAndResize(source);
            ByteArrayOutputStream encoded = new ByteArrayOutputStream();
            if (!ImageIO.write(avatar, "png", encoded))
            {
                throw new ServiceException("头像重新编码失败");
            }

            Path memberDirectory = ownedDirectory(memberId);
            Files.createDirectories(memberDirectory);
            String randomName = UUID.randomUUID().toString().replace("-", "") + ".png";
            Files.write(memberDirectory.resolve(randomName), encoded.toByteArray(), StandardOpenOption.CREATE_NEW);
            return "/profile/mall/avatar/" + memberId + "/" + randomName;
        }
        catch (ServiceException exception)
        {
            throw exception;
        }
        catch (IOException exception)
        {
            throw new ServiceException("头像处理失败，请稍后重试");
        }
    }

    @Override
    public void deleteOwned(Long memberId, String avatarUrl)
    {
        if (avatarUrl == null || !avatarUrl.startsWith("/profile/mall/avatar/" + memberId + "/"))
        {
            return;
        }
        String fileName = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
        if (!fileName.matches("[0-9a-f]{32}\\.png"))
        {
            return;
        }
        try
        {
            Files.deleteIfExists(ownedDirectory(memberId).resolve(fileName));
        }
        catch (IOException ignored)
        {
            // Old avatar cleanup is best-effort and must not roll back the profile change.
        }
    }

    private void validateDeclaration(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("请选择头像图片");
        }
        if (file.getSize() > MAX_FILE_SIZE)
        {
            throw new ServiceException("头像图片不能超过 5MB");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename == null ? ""
                : FilenameUtils.getExtension(originalFilename).toLowerCase(Locale.ROOT);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension) || !ALLOWED_CONTENT_TYPES.contains(contentType))
        {
            throw new ServiceException("头像仅支持 JPG、JPEG、PNG 格式");
        }
    }

    private BufferedImage decodeVerifiedImage(MultipartFile file) throws IOException
    {
        byte[] bytes = file.getBytes();
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes)))
        {
            if (input == null)
            {
                throw new ServiceException("头像文件不是有效图片");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext())
            {
                throw new ServiceException("头像文件不是有效图片");
            }
            ImageReader reader = readers.next();
            try
            {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!"png".equals(format) && !"jpeg".equals(format) && !"jpg".equals(format))
                {
                    throw new ServiceException("头像仅支持 JPG、JPEG、PNG 格式");
                }
                String detectedFormat = "jpg".equals(format) ? "jpeg" : format;
                String declaredExtension = FilenameUtils.getExtension(file.getOriginalFilename())
                        .toLowerCase(Locale.ROOT);
                String extensionFormat = "png".equals(declaredExtension) ? "png" : "jpeg";
                String contentTypeFormat = "image/png".equalsIgnoreCase(file.getContentType()) ? "png" : "jpeg";
                if (!detectedFormat.equals(extensionFormat) || !detectedFormat.equals(contentTypeFormat))
                {
                    throw new ServiceException("头像文件格式与声明不一致");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width < 64 || height < 64 || width > 8192 || height > 8192
                        || (long) width * height > MAX_PIXELS)
                {
                    throw new ServiceException("头像尺寸需在 64 至 8192 像素内");
                }
                BufferedImage image = reader.read(0);
                if (image == null)
                {
                    throw new ServiceException("头像文件不是有效图片");
                }
                return image;
            }
            finally
            {
                reader.dispose();
            }
        }
    }

    private BufferedImage cropAndResize(BufferedImage source)
    {
        int cropSize = Math.min(source.getWidth(), source.getHeight());
        int sourceX = (source.getWidth() - cropSize) / 2;
        int sourceY = (source.getHeight() - cropSize) / 2;
        BufferedImage output = new BufferedImage(OUTPUT_SIZE, OUTPUT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, OUTPUT_SIZE, OUTPUT_SIZE,
                sourceX, sourceY, sourceX + cropSize, sourceY + cropSize, null);
        graphics.dispose();
        return output;
    }

    private Path ownedDirectory(Long memberId)
    {
        Path directory = profileRoot.resolve("mall").resolve("avatar")
                .resolve(String.valueOf(memberId)).normalize();
        if (!directory.startsWith(profileRoot))
        {
            throw new ServiceException("头像存储路径无效");
        }
        return directory;
    }
}
