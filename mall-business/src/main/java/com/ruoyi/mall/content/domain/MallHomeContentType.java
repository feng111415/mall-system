package com.ruoyi.mall.content.domain;

import com.ruoyi.common.exception.ServiceException;

public enum MallHomeContentType
{
    HERO,
    TICKER,
    RECOMMENDATION;

    public static MallHomeContentType parse(String value)
    {
        for (MallHomeContentType type : values())
            if (type.name().equalsIgnoreCase(value)) return type;
        throw new ServiceException("首页内容类型不合法");
    }
}
