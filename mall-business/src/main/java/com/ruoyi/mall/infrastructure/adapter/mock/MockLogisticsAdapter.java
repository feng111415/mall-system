package com.ruoyi.mall.infrastructure.adapter.mock;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.ruoyi.mall.application.port.LogisticsPort;

/**
 * 首期物流 Mock 适配器。
 *
 * <p>用于创建模拟运单，后续可替换为真实物流渠道适配器。</p>
 */
@Component
public class MockLogisticsAdapter implements LogisticsPort
{
    @Override
    public LogisticsCreateResult createShipment(String orderNo, String companyCode,
            String receiverName, String receiverAddress)
    {
        String trackingNo = "MOCK-" + UUID.randomUUID().toString().replace("-", "");
        return new LogisticsCreateResult(true, trackingNo, "Mock 物流运单创建成功");
    }
}
