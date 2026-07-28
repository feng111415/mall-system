package com.ruoyi.mall.reconciliation.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.aftersale.domain.MallRefund;
import com.ruoyi.mall.aftersale.mapper.MallRefundMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;
import com.ruoyi.mall.reconciliation.domain.MallReconciliationDiff;
import com.ruoyi.mall.reconciliation.domain.MallReconciliationResult;
import com.ruoyi.mall.reconciliation.mapper.MallReconciliationMapper;
import com.ruoyi.mall.reconciliation.domain.MallReconciliationAlert;
import com.ruoyi.mall.reconciliation.mapper.MallReconciliationAlertMapper;
import com.ruoyi.mall.governance.service.MallCompensationService;

@Service
public class MallReconciliationService {
    private final MallReconciliationMapper mapper; private final MallPaymentMapper paymentMapper; private final MallRefundMapper refundMapper; private final MallCompensationService compensationService; private final MallReconciliationAlertMapper alertMapper;
    public MallReconciliationService(MallReconciliationMapper mapper, MallPaymentMapper paymentMapper, MallRefundMapper refundMapper, MallCompensationService compensationService, MallReconciliationAlertMapper alertMapper){this.mapper=mapper;this.paymentMapper=paymentMapper;this.refundMapper=refundMapper;this.compensationService=compensationService;this.alertMapper=alertMapper;}
    public List<MallReconciliationDiff> list(String type,String status,String businessNo,Integer limit,Integer offset){int l=limit==null?20:Math.min(Math.max(limit,1),100);int o=offset==null?0:Math.max(offset,0);return mapper.selectList(type,status,businessNo,l,o);}
    @Transactional(rollbackFor=Exception.class)
    public MallReconciliationDiff reconcile(MallReconciliationResult result){
        if(result==null||StringUtils.isBlank(result.getDiffType())||StringUtils.isBlank(result.getBusinessNo())||StringUtils.isBlank(result.getExternalStatus())) throw new ServiceException("对账参数无效");
        String type=result.getDiffType().trim().toUpperCase(); String localStatus; BigDecimal localAmount; String orderNo;
        if("PAYMENT".equals(type)){ MallPayment p=paymentMapper.selectByPaymentNo(result.getBusinessNo()); if(p==null) {localStatus="NOT_FOUND";localAmount=null;orderNo=null;} else {localStatus=p.getStatus();localAmount=p.getAmount();orderNo=p.getOrderNo();} }
        else if("REFUND".equals(type)){ MallRefund r=refundMapper.selectByRefundNoForUpdate(result.getBusinessNo()); if(r==null){localStatus="NOT_FOUND";localAmount=null;orderNo=null;} else {localStatus=r.getStatus();localAmount=r.getRefundAmount();orderNo=r.getOrderNo();} }
        else throw new ServiceException("不支持的对账类型");
        boolean statusMismatch=!result.getExternalStatus().equalsIgnoreCase(localStatus); boolean amountMismatch=result.getExternalAmount()!=null && (localAmount==null||result.getExternalAmount().compareTo(localAmount)!=0);
        if(!statusMismatch&&!amountMismatch) return null;
        String diffCode=statusMismatch&&amountMismatch?"STATUS_AMOUNT_MISMATCH":statusMismatch?"STATUS_MISMATCH":"AMOUNT_MISMATCH";
        MallReconciliationDiff existing=mapper.selectByBusinessCode(type,result.getBusinessNo(),diffCode); if(existing!=null) return existing;
        MallReconciliationDiff diff=new MallReconciliationDiff(); diff.setDiffNo("RD-"+UUID.randomUUID().toString().replace("-","")); diff.setDiffType(type);diff.setBusinessNo(result.getBusinessNo());diff.setOrderNo(orderNo);diff.setLocalStatus(localStatus);diff.setExternalStatus(result.getExternalStatus());diff.setLocalAmount(localAmount);diff.setExternalAmount(result.getExternalAmount());diff.setDiffCode(diffCode);diff.setDiffMessage("本地与外部渠道结果不一致");
        if(mapper.insertIgnore(diff)!=1) { existing=mapper.selectByBusinessCode(type,result.getBusinessNo(),diffCode); if(existing!=null) return existing; throw new ServiceException("对账差异保存失败"); } return diff;
    }
    @Transactional(rollbackFor=Exception.class)
    public MallReconciliationDiff handle(Long diffId,String status,String remark){if(diffId==null||diffId<=0||StringUtils.isBlank(status))throw new ServiceException("差异处理参数无效");String normalized=status.trim().toUpperCase();if(!List.of("RESOLVED","IGNORED","MANUAL").contains(normalized))throw new ServiceException("不允许的差异处理状态");if(mapper.updateStatus(diffId,normalized,remark)!=1)throw new ServiceException("差异不存在或已处理");return mapper.selectById(diffId);}

    public MallReconciliationDiff check(String type, String businessNo, String externalStatus, BigDecimal externalAmount, String providerNo, String externalMessage) {
        MallReconciliationResult result = new MallReconciliationResult(); result.setDiffType(type); result.setBusinessNo(businessNo); result.setExternalStatus(externalStatus); result.setExternalAmount(externalAmount); return reconcile(result);
    }
    public MallReconciliationDiff ignore(Long diffId, String remark, String operator) { return handle(diffId, "IGNORED", remark); }
    public MallReconciliationDiff createCompensation(Long diffId, String remark, String operator) {
        MallReconciliationDiff diff = mapper.selectById(diffId); if (diff == null) throw new ServiceException("对账差异不存在");
        String taskType = "PAYMENT".equals(diff.getDiffType()) ? "PAYMENT_CONFIRM" : "REFUND_CONFIRM";
        compensationService.create(taskType, diff.getBusinessNo(), diff.getOrderNo(), remark);
        return handle(diffId, "MANUAL", remark);
    }
    @Transactional(rollbackFor=Exception.class)
    public int generateAlerts() {
        int created=0; java.time.LocalDateTime cutoff=java.time.LocalDateTime.now().minusMinutes(30);
        for (MallReconciliationDiff diff : mapper.selectUnresolved(cutoff, 100)) {
            MallReconciliationAlert alert=new MallReconciliationAlert(); alert.setAlertNo("RA-"+UUID.randomUUID().toString().replace("-","")); alert.setDiffId(diff.getDiffId()); alert.setAlertLevel("HIGH"); alert.setAlertMessage("对账差异超过30分钟未处理："+diff.getDiffNo());
            if(alertMapper.insertIgnore(alert)==1) created++;
        } return created;
    }
    public List<MallReconciliationAlert> alerts(String status,Integer limit,Integer offset){int l=limit==null?20:Math.min(Math.max(limit,1),100);int o=offset==null?0:Math.max(offset,0);return alertMapper.selectList(status,l,o);}
    @Transactional(rollbackFor=Exception.class)
    public MallReconciliationAlert acknowledgeAlert(Long alertId,String operator){if(alertId==null||alertId<=0)throw new ServiceException("告警参数无效");if(alertMapper.acknowledge(alertId,operator)==0)throw new ServiceException("告警不存在或已确认");return alertMapper.selectById(alertId);}
}
