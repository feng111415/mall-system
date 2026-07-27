package com.ruoyi.mall.member.domain;

import java.io.Serializable;
import java.util.Date;

/** 会员协议同意记录，对应 mall_member_consent。 */
public class MallMemberConsent implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long consentId;
    private Long memberId;
    private String consentType;
    private String consentVersion;
    private String consentIp;
    private Date consentTime;

    public Long getConsentId() { return consentId; }
    public void setConsentId(Long consentId) { this.consentId = consentId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getConsentType() { return consentType; }
    public void setConsentType(String consentType) { this.consentType = consentType; }
    public String getConsentVersion() { return consentVersion; }
    public void setConsentVersion(String consentVersion) { this.consentVersion = consentVersion; }
    public String getConsentIp() { return consentIp; }
    public void setConsentIp(String consentIp) { this.consentIp = consentIp; }
    public Date getConsentTime() { return consentTime; }
    public void setConsentTime(Date consentTime) { this.consentTime = consentTime; }
}
