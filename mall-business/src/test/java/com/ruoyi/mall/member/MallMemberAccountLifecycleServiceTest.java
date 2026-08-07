package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.MemberAfterSaleStatePort;
import com.ruoyi.mall.application.port.MemberMessagePort;
import com.ruoyi.mall.application.port.MemberOrderStatePort;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallMemberAccountActionTicket;
import com.ruoyi.mall.member.domain.MallMemberPhoneChangeRequest;
import com.ruoyi.mall.member.domain.MallMemberSession;
import com.ruoyi.mall.member.domain.dto.MallPhoneChangeStartRequest;
import com.ruoyi.mall.member.mapper.MallMemberAccountMapper;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.mapper.MallMemberSessionMapper;
import com.ruoyi.mall.member.service.MallMemberAccountLifecycleService;
import com.ruoyi.mall.member.service.MallMemberAuthService;
import com.ruoyi.mall.member.service.MallMemberTokenService;

@ExtendWith(MockitoExtension.class)
class MallMemberAccountLifecycleServiceTest
{
    @Mock MallMemberMapper memberMapper;
    @Mock MallMemberAccountMapper accountMapper;
    @Mock MallMemberSessionMapper sessionMapper;
    @Mock MallMemberAuthService authService;
    @Mock MallMemberTokenService tokenService;
    @Mock MemberOrderStatePort orderStatePort;
    @Mock MemberAfterSaleStatePort afterSaleStatePort;
    @Mock MemberMessagePort memberMessagePort;
    private MallMemberAccountLifecycleService service;
    private MallMember member;
    private MallMemberTokenService.MemberSession current;

    @BeforeEach
    void setUp()
    {
        service = new MallMemberAccountLifecycleService(memberMapper, accountMapper, sessionMapper, authService,
                tokenService, orderStatePort, afterSaleStatePort, memberMessagePort);
        member = new MallMember();
        member.setMemberId(7L); member.setPhone("13800138000"); member.setStatus("0");
        current = new MallMemberTokenService.MemberSession(); current.setMemberId(7L); current.setSessionId(11L);
        current.setTokenHash("a".repeat(64));
    }

    @Test
    void rejectsPhoneAlreadyBound()
    {
        MallPhoneChangeStartRequest request = new MallPhoneChangeStartRequest(); request.setNewPhone("13900139000");
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(member);
        when(memberMapper.selectByPhone("13900139000")).thenReturn(member);

        assertThrows(ServiceException.class, () -> service.startPhoneChange(current, request, "127.0.0.1"));
        verify(accountMapper, never()).insertPhoneChangeRequest(any());
    }

    @Test
    void cancellationIsBlockedWhenOrderOrAfterSaleIsInProgress()
    {
        when(orderStatePort.countUnfinishedOrders(7L)).thenReturn(1);
        when(afterSaleStatePort.countInProgressAfterSales(7L)).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.sendCancellationCode(current, "127.0.0.1"));
        verify(authService, never()).sendAccountLifecycleCode(anyString(), anyString(), anyString(), anyString());
        verify(accountMapper).insertLifecycleAudit(any());
    }

    @Test
    void oldPhoneVerificationIssuesShortLivedTicket()
    {
        MallMemberPhoneChangeRequest request = pendingRequest();
        when(accountMapper.selectPhoneChangeRequest(12L, 7L)).thenReturn(request);
        when(accountMapper.markOldPhoneVerified(12L, 7L)).thenReturn(1);
        when(accountMapper.insertActionTicket(any(MallMemberAccountActionTicket.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, MallMemberAccountActionTicket.class).setTicketId(3L); return 1;
        });

        var result = service.verifyOldPhone(current, 12L, "123456", "127.0.0.1");

        assertEquals("NEW_PHONE_PENDING", result.status());
        org.junit.jupiter.api.Assertions.assertNotNull(result.ticket());
        verify(authService).verifySmsCode("13800138000", MallMemberAuthService.PHONE_CHANGE_OLD_PURPOSE, "123456");
    }

    @Test
    void cancellationDeactivatesAccountAndRevokesEveryActiveSession()
    {
        when(orderStatePort.countUnfinishedOrders(7L)).thenReturn(0);
        when(afterSaleStatePort.countInProgressAfterSales(7L)).thenReturn(0);
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(member);
        when(memberMapper.deactivate(7L)).thenReturn(1);
        when(accountMapper.insertLifecycleAudit(any())).thenReturn(1);
        MallMemberSession active = new MallMemberSession(); active.setSessionId(11L); active.setTokenHash("b".repeat(64));
        when(sessionMapper.selectActiveSessionsForUpdate(any(Long.class), any(LocalDateTime.class))).thenReturn(List.of(active));
        when(sessionMapper.markSessionOffline(11L, "REVOKED", "ACCOUNT_DEACTIVATED")).thenReturn(1);

        service.cancelAccount(current, "123456", "127.0.0.1");

        verify(authService).verifySmsCode("13800138000", MallMemberAuthService.ACCOUNT_CANCELLATION_PURPOSE, "123456");
        verify(memberMapper).deactivate(7L);
        verify(sessionMapper).markSessionOffline(11L, "REVOKED", "ACCOUNT_DEACTIVATED");
    }

    @Test
    void completedPhoneChangePublishesSecurityMessageAndRevokesEverySession()
    {
        MallMemberPhoneChangeRequest request = pendingRequest();
        request.setStatus("NEW_PHONE_PENDING");
        MallMemberAccountActionTicket ticket = new MallMemberAccountActionTicket();
        ticket.setTicketId(3L); ticket.setActionType("PHONE_CHANGE_OLD_VERIFIED");
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(member);
        when(accountMapper.selectPhoneChangeRequest(12L, 7L)).thenReturn(request);
        when(accountMapper.selectValidActionTicket(anyString(), eq(12L), eq(7L))).thenReturn(ticket);
        when(accountMapper.markNewPhoneVerified(12L, 7L)).thenReturn(1);
        when(memberMapper.updatePhone(7L, "13900139000")).thenReturn(1);
        when(accountMapper.consumeActionTicket(3L)).thenReturn(1);
        when(accountMapper.insertLifecycleAudit(any())).thenReturn(1);
        MallMemberSession active = new MallMemberSession(); active.setSessionId(11L); active.setTokenHash("b".repeat(64));
        when(sessionMapper.selectActiveSessionsForUpdate(any(Long.class), any(LocalDateTime.class))).thenReturn(List.of(active));
        when(sessionMapper.markSessionOffline(11L, "REVOKED", "PHONE_CHANGED")).thenReturn(1);

        service.completePhoneChange(current, 12L, "ticket", "123456", "127.0.0.1");

        verify(memberMessagePort).publish(7L, "ACCOUNT", "登录手机号已更换",
                "手机号更换成功，所有设备已退出登录", "如果这不是你的操作，请及时联系商城客服核查账号安全",
                "ACCOUNT", 7L, null, "/account/privacy");
        verify(sessionMapper).markSessionOffline(11L, "REVOKED", "PHONE_CHANGED");
    }

    private MallMemberPhoneChangeRequest pendingRequest()
    {
        MallMemberPhoneChangeRequest request = new MallMemberPhoneChangeRequest();
        request.setRequestId(12L); request.setMemberId(7L); request.setOldPhone("13800138000");
        request.setNewPhone("13900139000"); request.setStatus("OLD_PHONE_PENDING");
        request.setExpiresTime(new Date(System.currentTimeMillis() + 60_000));
        return request;
    }
}
