package com.ruoyi.mall.application.port;

/** Cross-module write boundary for member-facing business messages. */
public interface MemberMessagePort
{
    void publish(Long memberId, String category, String title, String summary,
            String content, String businessType, Long businessId, String businessNo,
            String actionPath);
}
