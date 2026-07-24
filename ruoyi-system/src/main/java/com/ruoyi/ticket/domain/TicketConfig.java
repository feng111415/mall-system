package com.ruoyi.ticket.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票务配置对象 ticket_config
 *
 * @author ruoyi
 */
public class TicketConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long configId;

    /** 配置键 */
    @Excel(name = "配置键")
    private String configKey;

    /** 配置值 */
    @Excel(name = "配置值")
    private String configValue;

    /** 配置名称 */
    @Excel(name = "配置名称")
    private String configName;

    /** 配置类型 */
    @Excel(name = "配置类型")
    private String configType;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    public Long getConfigId()
    {
        return configId;
    }

    public void setConfigId(Long configId)
    {
        this.configId = configId;
    }
    public String getConfigKey()
    {
        return configKey;
    }

    public void setConfigKey(String configKey)
    {
        this.configKey = configKey;
    }
    public String getConfigValue()
    {
        return configValue;
    }

    public void setConfigValue(String configValue)
    {
        this.configValue = configValue;
    }
    public String getConfigName()
    {
        return configName;
    }

    public void setConfigName(String configName)
    {
        this.configName = configName;
    }
    public String getConfigType()
    {
        return configType;
    }

    public void setConfigType(String configType)
    {
        this.configType = configType;
    }
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("configId", getConfigId())
            .append("configKey", getConfigKey())
            .append("configValue", getConfigValue())
            .append("configName", getConfigName())
            .append("configType", getConfigType())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
