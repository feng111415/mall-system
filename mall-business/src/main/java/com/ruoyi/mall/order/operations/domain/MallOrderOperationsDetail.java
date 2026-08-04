package com.ruoyi.mall.order.operations.domain;

import java.util.List;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSale;
import com.ruoyi.mall.inventory.domain.MallStockReservation;
import com.ruoyi.mall.logistics.domain.MallLogisticsNode;
import com.ruoyi.mall.logistics.domain.MallLogisticsShipment;
import com.ruoyi.mall.order.domain.MallOrderItem;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.risk.domain.MallRiskRecord;

public class MallOrderOperationsDetail extends MallOrderOperationsSummary
{
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverDetailAddress;
    private String remark;
    private String cancelReason;
    private List<MallOrderItem> items;
    private List<MallPayment> payments;
    private List<MallStockReservation> reservations;
    private MallLogisticsShipment shipment;
    private List<MallItemAfterSale> afterSales;
    private List<MallRiskRecord> riskRecords;
    private List<MallOrderOperationLog> operations;

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String value) { receiverName = value; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String value) { receiverPhone = value; }
    public String getReceiverProvince() { return receiverProvince; }
    public void setReceiverProvince(String value) { receiverProvince = value; }
    public String getReceiverCity() { return receiverCity; }
    public void setReceiverCity(String value) { receiverCity = value; }
    public String getReceiverDistrict() { return receiverDistrict; }
    public void setReceiverDistrict(String value) { receiverDistrict = value; }
    public String getReceiverDetailAddress() { return receiverDetailAddress; }
    public void setReceiverDetailAddress(String value) { receiverDetailAddress = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String value) { cancelReason = value; }
    public List<MallOrderItem> getItems() { return items; }
    public void setItems(List<MallOrderItem> value) { items = value; }
    public List<MallPayment> getPayments() { return payments; }
    public void setPayments(List<MallPayment> value) { payments = value; }
    public List<MallStockReservation> getReservations() { return reservations; }
    public void setReservations(List<MallStockReservation> value) { reservations = value; }
    public MallLogisticsShipment getShipment() { return shipment; }
    public void setShipment(MallLogisticsShipment value) { shipment = value; }
    public List<MallItemAfterSale> getAfterSales() { return afterSales; }
    public void setAfterSales(List<MallItemAfterSale> value) { afterSales = value; }
    public List<MallRiskRecord> getRiskRecords() { return riskRecords; }
    public void setRiskRecords(List<MallRiskRecord> value) { riskRecords = value; }
    public List<MallOrderOperationLog> getOperations() { return operations; }
    public void setOperations(List<MallOrderOperationLog> value) { operations = value; }
}
