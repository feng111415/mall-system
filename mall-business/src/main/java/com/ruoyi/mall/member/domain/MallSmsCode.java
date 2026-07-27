package com.ruoyi.mall.member.domain;

import java.io.Serializable;
import java.util.Date;

/** 短信验证码审计记录，对应 mall_sms_code；仅保存验证码摘要。 */
public class MallSmsCode implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long smsId;
    private String phone;
    private String purpose;
    private String codeHash;
    private String codeSalt;
    private String providerRequestId;
    private String sendStatus;
    private Integer verifyAttempts;
    private String usedFlag;
    private Date expireTime;
    private Date usedTime;
    private String requestIp;
    private Date createTime;

    public Long getSmsId() { return smsId; }
    public void setSmsId(Long smsId) { this.smsId = smsId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public String getCodeSalt() { return codeSalt; }
    public void setCodeSalt(String codeSalt) { this.codeSalt = codeSalt; }
    public String getProviderRequestId() { return providerRequestId; }
    public void setProviderRequestId(String providerRequestId) { this.providerRequestId = providerRequestId; }
    public String getSendStatus() { return sendStatus; }
    public void setSendStatus(String sendStatus) { this.sendStatus = sendStatus; }
    public Integer getVerifyAttempts() { return verifyAttempts; }
    public void setVerifyAttempts(Integer verifyAttempts) { this.verifyAttempts = verifyAttempts; }
    public String getUsedFlag() { return usedFlag; }
    public void setUsedFlag(String usedFlag) { this.usedFlag = usedFlag; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }
    public Date getUsedTime() { return usedTime; }
    public void setUsedTime(Date usedTime) { this.usedTime = usedTime; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
