package com.ruoyi.ticket.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 实名购票人对象 ticket_passenger
 *
 * @author ruoyi
 */
public class TicketPassenger extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 购票人ID */
    private Long passengerId;

    /** 小程序用户ID */
    @Excel(name = "小程序用户ID")
    private Long userId;

    /** 真实姓名 */
    @Excel(name = "真实姓名")
    private String realName;

    /** 证件类型 */
    @Excel(name = "证件类型")
    private String idType;

    /** 证件号码 */
    @Excel(name = "证件号码")
    private String idNo;

    /** 手机号 */
    @Excel(name = "手机号")
    private String mobile;

    /** 购票人类型 */
    @Excel(name = "购票人类型")
    private String passengerType;

    /** 是否默认 */
    @Excel(name = "是否默认")
    private String isDefault;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 删除标志 */
    @Excel(name = "删除标志")
    private String delFlag;

    public Long getPassengerId()
    {
        return passengerId;
    }

    public void setPassengerId(Long passengerId)
    {
        this.passengerId = passengerId;
    }
    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }
    public String getIdType()
    {
        return idType;
    }

    public void setIdType(String idType)
    {
        this.idType = idType;
    }
    public String getIdNo()
    {
        return idNo;
    }

    public void setIdNo(String idNo)
    {
        this.idNo = idNo;
    }
    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }
    public String getPassengerType()
    {
        return passengerType;
    }

    public void setPassengerType(String passengerType)
    {
        this.passengerType = passengerType;
    }
    public String getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(String isDefault)
    {
        this.isDefault = isDefault;
    }
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("passengerId", getPassengerId())
            .append("userId", getUserId())
            .append("realName", getRealName())
            .append("idType", getIdType())
            .append("idNo", getIdNo())
            .append("mobile", getMobile())
            .append("passengerType", getPassengerType())
            .append("isDefault", getIsDefault())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
