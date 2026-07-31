package com.ruoyi.mall.member.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;

@Component
public class MallNicknamePolicy
{
    private static final Pattern PHONE = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern EMAIL = Pattern.compile("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern URL = Pattern.compile("(?:https?://|www\\.)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTACT_HANDLE = Pattern.compile(
            "(?:qq|vx|v信|扣扣|微信号|手机号|联系电话|电话)[\\s:_-]*[a-z0-9]{3,}",
            Pattern.CASE_INSENSITIVE);
    private final List<String> sensitiveWords;

    public MallNicknamePolicy(@Value("${mall.member.nickname-sensitive-words:管理员,官方客服,客服,官方,加微信,加微,微信,微信号,微商,QQ,VX,V信,扣扣,联系电话,兼职刷单}") String sensitiveWords)
    {
        this.sensitiveWords = Arrays.stream(sensitiveWords.split(","))
                .map(String::strip)
                .filter(word -> !word.isEmpty())
                .map(word -> word.toLowerCase(Locale.ROOT))
                .toList();
    }

    public String validateAndNormalize(String nickname)
    {
        String normalized = nickname == null ? "" : nickname.strip().replaceAll("\\s+", " ");
        int length = normalized.codePointCount(0, normalized.length());
        if (length < 2 || length > 20)
        {
            throw new ServiceException("昵称长度需为 2 至 20 个字符");
        }
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        boolean hasControlCharacter = normalized.codePoints().anyMatch(Character::isISOControl);
        boolean hasSensitiveWord = sensitiveWords.stream().anyMatch(lowerCase::contains);
        if (hasControlCharacter || normalized.indexOf('<') >= 0 || normalized.indexOf('>') >= 0
                || PHONE.matcher(normalized).find() || EMAIL.matcher(normalized).find()
                || URL.matcher(normalized).find() || CONTACT_HANDLE.matcher(normalized).find() || hasSensitiveWord)
        {
            throw new ServiceException("昵称包含联系方式或不适合公开展示的内容");
        }
        return normalized;
    }
}
