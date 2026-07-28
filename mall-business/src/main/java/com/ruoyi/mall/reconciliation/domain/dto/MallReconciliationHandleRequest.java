package com.ruoyi.mall.reconciliation.domain.dto;

import jakarta.validation.constraints.Size;

public class MallReconciliationHandleRequest
{
    @Size(max = 500, message = "处理备注长度不能超过500个字符")
    private String remark;

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
