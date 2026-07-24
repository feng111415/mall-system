package com.ruoyi.ticket.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 小程序用户对象 ticket_user
 *
 * @author ruoyi
 */
public class TicketUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 小程序用户ID */
    private Long userId;

    /** 微信openid */
    @Excel(name = "微信openid")
    private String openid;

    /** 微信unionid */
    @Excel(name = "微信unionid")
    private String unionid;

    /** 微信session_key */
    @Excel(name = "微信session_key")
    private String sessionKey;

    /** 昵称 */
    @Excel(name = "昵称")
    private String nickName;

    /** 头像地址 */
    @Excel(name = "头像地址")
    private String avatar;

    /** 手机号 */
    @Excel(name = "手机号")
    private String mobile;

    /** 实名状态 */
    @Excel(name = "实名状态")
    private String realNameStatus;

    /** 账号状态 */
    @Excel(name = "账号状态")
    private String status;

    /** 删除标志 */
    @Excel(name = "删除标志")
    private String delFlag;

    /** 最后登录IP */
    @Excel(name = "最后登录IP")
    private String lastLoginIp;

    /** 最后登录时间 */
    @Excel(name = "最后登录时间")
    private Date lastLoginTime;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }
    public String getUnionid()
    {
        return unionid;
    }

    public void setUnionid(String unionid)
    {
        this.unionid = unionid;
    }
    public String getSessionKey()
    {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey)
    {
        this.sessionKey = sessionKey;
    }
    public String getNickName()
    {
        return nickName;
    }

    public void setNickName(String nickName)
    {
        this.nickName = nickName;
    }
    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }
    public String getMobile()
    {
        return mobile;
    }

    public void setMobile(String mobile)
    {
        this.mobile = mobile;
    }
    public String getRealNameStatus()
    {
        return realNameStatus;
    }

    public void setRealNameStatus(String realNameStatus)
    {
        this.realNameStatus = realNameStatus;
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
    public String getLastLoginIp()
    {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp)
    {
        this.lastLoginIp = lastLoginIp;
    }
    public Date getLastLoginTime()
    {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime)
    {
        this.lastLoginTime = lastLoginTime;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("userId", getUserId())
            .append("openid", getOpenid())
            .append("unionid", getUnionid())
            .append("sessionKey", getSessionKey())
            .append("nickName", getNickName())
            .append("avatar", getAvatar())
            .append("mobile", getMobile())
            .append("realNameStatus", getRealNameStatus())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("lastLoginIp", getLastLoginIp())
            .append("lastLoginTime", getLastLoginTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
