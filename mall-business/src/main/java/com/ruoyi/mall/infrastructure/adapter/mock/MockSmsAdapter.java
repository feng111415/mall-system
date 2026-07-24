package com.ruoyi.mall.infrastructure.adapter.mock;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.ruoyi.mall.application.port.SmsPort;

/**
 * 首期短信 Mock 适配器。
 *
 * <p>不调用真实短信平台，仅返回可追踪的请求编号，供开发和联调使用。</p>
 */
@Component
public class MockSmsAdapter implements SmsPort
{
    @Override
    public SmsSendResult sendVerificationCode(String phone, String templateCode, String code)
    {
        String requestId = "mock-sms-" + UUID.randomUUID();
        return new SmsSendResult(true, requestId, "Mock 短信发送成功");
    }
}
