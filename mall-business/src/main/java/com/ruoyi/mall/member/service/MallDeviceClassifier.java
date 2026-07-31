package com.ruoyi.mall.member.service;

import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class MallDeviceClassifier
{
    public DeviceInfo classify(String userAgent)
    {
        String ua = userAgent == null ? "" : userAgent.toLowerCase(Locale.ROOT);
        boolean mobile = ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")
                || ua.contains("ipad") || ua.contains("ipod") || ua.contains("tablet");
        if (ua.contains("iphone")) return new DeviceInfo("MOBILE", "iPhone 浏览器");
        if (ua.contains("ipad")) return new DeviceInfo("MOBILE", "iPad 浏览器");
        if (ua.contains("android")) return new DeviceInfo("MOBILE", "Android 浏览器");
        if (mobile) return new DeviceInfo("MOBILE", "移动端浏览器");
        if (ua.contains("windows")) return new DeviceInfo("DESKTOP", "Windows 电脑");
        if (ua.contains("macintosh") || ua.contains("mac os")) return new DeviceInfo("DESKTOP", "Mac 电脑");
        if (ua.contains("linux")) return new DeviceInfo("DESKTOP", "Linux 电脑");
        return new DeviceInfo("DESKTOP", "电脑浏览器");
    }

    public record DeviceInfo(String type, String name)
    {
    }
}
