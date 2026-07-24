package com.ruoyi.mall.application.port;

/**
 * 物流服务出站端口。
 *
 * <p>物流公司由适配器隔离，后续可替换为真实物流平台。</p>
 */
public interface LogisticsPort
{
    /**
     * 创建物流运单。
     *
     * @param orderNo 商城订单号
     * @param companyCode 物流公司编码
     * @param receiverName 收件人姓名
     * @param receiverAddress 收件地址
     * @return 运单创建结果
     */
    LogisticsCreateResult createShipment(String orderNo, String companyCode,
            String receiverName, String receiverAddress);

    /** 物流运单创建结果。 */
    record LogisticsCreateResult(boolean success, String trackingNo, String message)
    {
    }
}
