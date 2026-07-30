package com.ruoyi.mall.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mall.member.mapper.MallMemberAuthMapper;

/** Commits failed verification attempts independently from the login transaction. */
@Service
public class MallSmsVerificationAttemptService
{
    private final MallMemberAuthMapper authMapper;

    public MallSmsVerificationAttemptService(MallMemberAuthMapper authMapper)
    {
        this.authMapper = authMapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(Long smsId)
    {
        if (smsId != null) authMapper.incrementSmsAttempts(smsId);
    }
}
