package com.ruoyi.mall.content.domain;

import com.ruoyi.common.exception.ServiceException;

public enum MallHomeContentStatus
{
    DRAFT,
    PUBLISHED,
    DISABLED;

    public static MallHomeContentStatus parse(String value)
    {
        for (MallHomeContentStatus status : values())
            if (status.name().equalsIgnoreCase(value)) return status;
        throw new ServiceException("首页内容状态不合法");
    }
}
