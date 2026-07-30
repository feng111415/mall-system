package com.ruoyi.mall.logistics.domain;

import com.ruoyi.common.exception.ServiceException;

/** 后台手工发货允许选择的物流公司白名单。 */
public enum MallLogisticsCompany
{
    SF("SF", "顺丰速运"),
    ZTO("ZTO", "中通快递"),
    YTO("YTO", "圆通速递"),
    STO("STO", "申通快递"),
    YD("YD", "韵达速递"),
    JD("JD", "京东物流"),
    EMS("EMS", "中国邮政 EMS"),
    JT("JT", "极兔速递"),
    DEPPON("DEPPON", "德邦快递"),
    MOCK("MOCK", "模拟物流");

    private final String code;
    private final String name;

    MallLogisticsCompany(String code, String name)
    {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    public static MallLogisticsCompany parse(String value)
    {
        if (value == null || value.isBlank()) throw new ServiceException("物流公司不能为空");
        String normalized = value.trim().toUpperCase();
        for (MallLogisticsCompany company : values())
        {
            if (company.code.equals(normalized)) return company;
        }
        throw new ServiceException("不支持的物流公司");
    }
}
