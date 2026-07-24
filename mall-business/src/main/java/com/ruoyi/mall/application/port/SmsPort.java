package com.ruoyi.mall.application.port;

/**
 * 短信服务出站端口。
 *
 * <p>业务模块只依赖此接口，实际短信供应商通过适配器接入。</p>
 */
public interface SmsPort
{
    /**
     * 发送短信验证码。
     *
     * @param phone 手机号
     * @param templateCode 短信模板编码
     * @param code 验证码
     * @return 发送结果
     */
    SmsSendResult sendVerificationCode(String phone, String templateCode, String code);

    /** 短信发送结果。 */
    record SmsSendResult(boolean success, String providerRequestId, String message)
    {
    }
}
