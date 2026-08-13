package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import com.ruoyi.mall.member.service.MallDeviceClassifier;

class MallDeviceClassifierTest
{
    private final MallDeviceClassifier classifier = new MallDeviceClassifier();

    @Test
    void classifiesPhonesAndTabletsAsMobile()
    {
        assertEquals("MOBILE", classifier.classify("Mozilla/5.0 (iPhone) Mobile").type());
        assertEquals("MOBILE", classifier.classify("Mozilla/5.0 (iPad) Safari").type());
        assertEquals("MOBILE", classifier.classify("Mozilla/5.0 (Linux; Android 15) Chrome").type());
        assertEquals("MOBILE", classifier.classify("Mozilla/5.0 MicroMessenger MiniProgram").type());
        assertEquals("微信小程序", classifier.classify("Mozilla/5.0 MicroMessenger MiniProgram").name());
    }

    @Test
    void classifiesDesktopOperatingSystemsAndUnknownAgentsAsDesktop()
    {
        assertEquals("DESKTOP", classifier.classify("Mozilla/5.0 (Windows NT 10.0) Chrome").type());
        assertEquals("DESKTOP", classifier.classify("Mozilla/5.0 (Macintosh; Intel Mac OS X) Safari").type());
        assertEquals("DESKTOP", classifier.classify(null).type());
    }
}
