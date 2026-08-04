package com.ruoyi.mall.logistics.domain;

import java.io.Serializable;

public class MallLogisticsCompanyOption implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String code;
    private final String name;

    public MallLogisticsCompanyOption(String code, String name)
    {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
}
