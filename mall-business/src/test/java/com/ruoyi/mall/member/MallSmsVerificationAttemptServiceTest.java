package com.ruoyi.mall.member;

import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.member.mapper.MallMemberAuthMapper;
import com.ruoyi.mall.member.service.MallSmsVerificationAttemptService;

class MallSmsVerificationAttemptServiceTest
{
    @Mock private MallMemberAuthMapper authMapper;
    private MallSmsVerificationAttemptService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallSmsVerificationAttemptService(authMapper);
    }

    @Test
    void persistsFailedAttemptThroughDedicatedTransactionModule()
    {
        service.recordFailedAttempt(7L);

        verify(authMapper).incrementSmsAttempts(7L);
    }
}
