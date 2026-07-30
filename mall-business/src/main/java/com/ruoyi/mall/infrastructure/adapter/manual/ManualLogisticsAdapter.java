package com.ruoyi.mall.infrastructure.adapter.manual;

import org.springframework.stereotype.Component;
import com.ruoyi.mall.application.port.LogisticsPort;

/** 手工物流适配器：后台录入的运单号直接作为渠道结果持久化。 */
@Component
public class ManualLogisticsAdapter implements LogisticsPort
{
    @Override
    public LogisticsCreateResult createShipment(String orderNo, String companyCode,
            String trackingNo, String receiverName, String receiverAddress)
    {
        return new LogisticsCreateResult(true, trackingNo, "手工物流运单已登记");
    }
}
