package com.ruoyi.mall.member.domain;

import java.io.Serializable;
import java.util.Date;

/** Server-side record for a short-lived member login challenge. */
public class MallCaptchaChallenge implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long challengeId;
    private String challengeKey;
    private String phone;
    private String deviceIdentifier;
    private String requestIp;
    private String answerHash;
    private String ticketHash;
    private String status;
    private Integer verifyAttempts;
    private Date expireTime;
    private Date ticketExpireTime;
    private Date verifiedTime;
    private Date consumedTime;
    private Date createTime;

    public Long getChallengeId() { return challengeId; }
    public void setChallengeId(Long value) { challengeId = value; }
    public String getChallengeKey() { return challengeKey; }
    public void setChallengeKey(String value) { challengeKey = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String value) { deviceIdentifier = value; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String value) { requestIp = value; }
    public String getAnswerHash() { return answerHash; }
    public void setAnswerHash(String value) { answerHash = value; }
    public String getTicketHash() { return ticketHash; }
    public void setTicketHash(String value) { ticketHash = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Integer getVerifyAttempts() { return verifyAttempts; }
    public void setVerifyAttempts(Integer value) { verifyAttempts = value; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date value) { expireTime = value; }
    public Date getTicketExpireTime() { return ticketExpireTime; }
    public void setTicketExpireTime(Date value) { ticketExpireTime = value; }
    public Date getVerifiedTime() { return verifiedTime; }
    public void setVerifiedTime(Date value) { verifiedTime = value; }
    public Date getConsumedTime() { return consumedTime; }
    public void setConsumedTime(Date value) { consumedTime = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
}
