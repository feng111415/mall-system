package com.ruoyi.mall.member.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.MemberAfterSaleStatePort;
import com.ruoyi.mall.application.port.MemberMessagePort;
import com.ruoyi.mall.application.port.MemberOrderStatePort;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallMemberAccountActionTicket;
import com.ruoyi.mall.member.domain.MallMemberLifecycleAudit;
import com.ruoyi.mall.member.domain.MallMemberPhoneChangeRequest;
import com.ruoyi.mall.member.domain.dto.MallPhoneChangeStartRequest;
import com.ruoyi.mall.member.domain.vo.MallCancellationEligibilityVo;
import com.ruoyi.mall.member.domain.vo.MallPhoneChangeRequestVo;
import com.ruoyi.mall.member.mapper.MallMemberAccountMapper;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.mapper.MallMemberSessionMapper;
import com.ruoyi.mall.member.service.MallMemberTokenService.MemberSession;

@Service
public class MallMemberAccountLifecycleService
{
    private static final long ACTION_WINDOW_MILLIS = 10 * 60 * 1000L;
    private static final String OLD_TICKET_ACTION = "PHONE_CHANGE_OLD_VERIFIED";
    private final MallMemberMapper memberMapper;
    private final MallMemberAccountMapper accountMapper;
    private final MallMemberSessionMapper sessionMapper;
    private final MallMemberAuthService authService;
    private final MallMemberTokenService tokenService;
    private final MemberOrderStatePort orderStatePort;
    private final MemberAfterSaleStatePort afterSaleStatePort;
    private final MemberMessagePort memberMessagePort;

    public MallMemberAccountLifecycleService(MallMemberMapper memberMapper, MallMemberAccountMapper accountMapper,
            MallMemberSessionMapper sessionMapper, MallMemberAuthService authService,
            MallMemberTokenService tokenService, MemberOrderStatePort orderStatePort,
            MemberAfterSaleStatePort afterSaleStatePort, MemberMessagePort memberMessagePort)
    {
        this.memberMapper = memberMapper;
        this.accountMapper = accountMapper;
        this.sessionMapper = sessionMapper;
        this.authService = authService;
        this.tokenService = tokenService;
        this.orderStatePort = orderStatePort;
        this.afterSaleStatePort = afterSaleStatePort;
        this.memberMessagePort = memberMessagePort;
    }

    @Transactional
    public MallPhoneChangeRequestVo startPhoneChange(MemberSession current, MallPhoneChangeStartRequest request,
            String requestIp)
    {
        MallMember member = requireActiveMemberForUpdate(current.getMemberId());
        String newPhone = request.getNewPhone();
        if (newPhone.equals(member.getPhone())) throw new ServiceException("新手机号不能与当前手机号相同");
        if (memberMapper.selectByPhone(newPhone) != null) throw new ServiceException("新手机号已绑定其他账号");
        MallMemberPhoneChangeRequest value = new MallMemberPhoneChangeRequest();
        value.setMemberId(member.getMemberId());
        value.setOldPhone(member.getPhone());
        value.setNewPhone(newPhone);
        value.setStatus("OLD_PHONE_PENDING");
        value.setExpiresTime(new Date(System.currentTimeMillis() + ACTION_WINDOW_MILLIS));
        value.setRequestIp(requestIp);
        accountMapper.insertPhoneChangeRequest(value);
        audit(member.getMemberId(), "PHONE_CHANGE_STARTED", "已发起手机号更换",
                "等待验证当前手机号", member.getPhone(), newPhone, requestIp);
        return toVo(value, null);
    }

    public java.util.Map<String, Object> sendOldPhoneCode(MemberSession current, Long requestId, String requestIp)
    {
        MallMemberPhoneChangeRequest request = requireRequest(current, requestId, "OLD_PHONE_PENDING");
        return authService.sendAccountLifecycleCode(request.getOldPhone(), MallMemberAuthService.PHONE_CHANGE_OLD_PURPOSE,
                "MALL_PHONE_CHANGE_OLD_CODE", requestIp);
    }

    @Transactional
    public MallPhoneChangeRequestVo verifyOldPhone(MemberSession current, Long requestId, String code,
            String requestIp)
    {
        MallMemberPhoneChangeRequest request = requireRequest(current, requestId, "OLD_PHONE_PENDING");
        authService.verifySmsCode(request.getOldPhone(), MallMemberAuthService.PHONE_CHANGE_OLD_PURPOSE, code);
        if (accountMapper.markOldPhoneVerified(requestId, current.getMemberId()) != 1)
            throw new ServiceException("手机号更换状态已失效，请重新开始");
        String rawTicket = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        MallMemberAccountActionTicket ticket = new MallMemberAccountActionTicket();
        ticket.setRequestId(requestId);
        ticket.setMemberId(current.getMemberId());
        ticket.setActionType(OLD_TICKET_ACTION);
        ticket.setTicketHash(hash(rawTicket));
        ticket.setExpireTime(new Date(System.currentTimeMillis() + ACTION_WINDOW_MILLIS));
        accountMapper.insertActionTicket(ticket);
        audit(current.getMemberId(), "PHONE_CHANGE_OLD_VERIFIED", "当前手机号验证通过",
                "等待验证新手机号", request.getOldPhone(), request.getNewPhone(), requestIp);
        request.setStatus("NEW_PHONE_PENDING");
        request.setOldVerifiedTime(new Date());
        return toVo(request, rawTicket);
    }

    public java.util.Map<String, Object> sendNewPhoneCode(MemberSession current, Long requestId, String ticket,
            String requestIp)
    {
        MallMemberPhoneChangeRequest request = requireRequest(current, requestId, "NEW_PHONE_PENDING");
        requireTicket(current, requestId, ticket);
        if (memberMapper.selectByPhone(request.getNewPhone()) != null)
            throw new ServiceException("新手机号已绑定其他账号");
        return authService.sendAccountLifecycleCode(request.getNewPhone(), MallMemberAuthService.PHONE_CHANGE_NEW_PURPOSE,
                "MALL_PHONE_CHANGE_NEW_CODE", requestIp);
    }

    @Transactional
    public void completePhoneChange(MemberSession current, Long requestId, String ticket, String code,
            String requestIp)
    {
        MallMember member = requireActiveMemberForUpdate(current.getMemberId());
        MallMemberPhoneChangeRequest request = requireRequest(current, requestId, "NEW_PHONE_PENDING");
        MallMemberAccountActionTicket actionTicket = requireTicket(current, requestId, ticket);
        if (memberMapper.selectByPhone(request.getNewPhone()) != null)
            throw new ServiceException("新手机号已绑定其他账号");
        authService.verifySmsCode(request.getNewPhone(), MallMemberAuthService.PHONE_CHANGE_NEW_PURPOSE, code);
        if (accountMapper.markNewPhoneVerified(requestId, current.getMemberId()) != 1)
            throw new ServiceException("手机号更换状态已失效，请重新开始");
        if (memberMapper.updatePhone(member.getMemberId(), request.getNewPhone()) != 1)
            throw new ServiceException("手机号更新失败，请稍后重试");
        if (accountMapper.consumeActionTicket(actionTicket.getTicketId()) != 1)
            throw new ServiceException("手机号更换凭证已失效，请重新开始");
        audit(member.getMemberId(), "PHONE_CHANGED", "手机号更换成功",
                "全部设备已下线，请使用新手机号重新登录", request.getOldPhone(), request.getNewPhone(), requestIp);
        memberMessagePort.publish(member.getMemberId(), "ACCOUNT", "登录手机号已更换",
                "手机号更换成功，所有设备已退出登录", "如果这不是你的操作，请及时联系商城客服核查账号安全",
                "ACCOUNT", member.getMemberId(), null, "/account/privacy");
        revokeAllSessions(member.getMemberId(), "PHONE_CHANGED");
    }

    public java.util.Map<String, Object> sendCancellationCode(MemberSession current, String requestIp)
    {
        assertCancellationEligible(current.getMemberId(), requestIp);
        MallMember member = requireActiveMember(current.getMemberId());
        return authService.sendAccountLifecycleCode(member.getPhone(), MallMemberAuthService.ACCOUNT_CANCELLATION_PURPOSE,
                "MALL_ACCOUNT_CANCELLATION_CODE", requestIp);
    }

    @Transactional
    public void cancelAccount(MemberSession current, String code, String requestIp)
    {
        assertCancellationEligible(current.getMemberId(), requestIp);
        MallMember member = requireActiveMemberForUpdate(current.getMemberId());
        authService.verifySmsCode(member.getPhone(), MallMemberAuthService.ACCOUNT_CANCELLATION_PURPOSE, code);
        if (memberMapper.deactivate(member.getMemberId()) != 1)
            throw new ServiceException("账号状态更新失败，请稍后重试");
        audit(member.getMemberId(), "ACCOUNT_DEACTIVATED", "账号已注销",
                "账号立即停用，交易与售后凭证按法规保留", member.getPhone(), null, requestIp);
        revokeAllSessions(member.getMemberId(), "ACCOUNT_DEACTIVATED");
    }

    private void assertCancellationEligible(Long memberId, String requestIp)
    {
        int unfinishedOrders = orderStatePort.countUnfinishedOrders(memberId);
        int inProgressAfterSales = afterSaleStatePort.countInProgressAfterSales(memberId);
        if (unfinishedOrders > 0 || inProgressAfterSales > 0)
        {
            audit(memberId, "ACCOUNT_CANCELLATION_BLOCKED", "账号注销暂不可用",
                    "请先完成未完成订单或处理中售后", null, null, requestIp);
            throw new ServiceException("当前存在未完成订单或处理中售后，暂不能注销账号");
        }
    }

    private MallMemberPhoneChangeRequest requireRequest(MemberSession current, Long requestId, String status)
    {
        MallMemberPhoneChangeRequest request = accountMapper.selectPhoneChangeRequest(requestId, current.getMemberId());
        if (request == null || !status.equals(request.getStatus()) || request.getExpiresTime() == null
                || request.getExpiresTime().before(new Date()))
            throw new ServiceException("手机号更换请求已失效，请重新开始");
        return request;
    }

    private MallMemberAccountActionTicket requireTicket(MemberSession current, Long requestId, String rawTicket)
    {
        if (rawTicket == null || rawTicket.isBlank()) throw new ServiceException("请先完成当前手机号验证");
        MallMemberAccountActionTicket ticket = accountMapper.selectValidActionTicket(hash(rawTicket), requestId,
                current.getMemberId());
        if (ticket == null || !OLD_TICKET_ACTION.equals(ticket.getActionType()))
            throw new ServiceException("手机号更换凭证已失效，请重新开始");
        return ticket;
    }

    private MallMember requireActiveMember(Long memberId)
    {
        MallMember member = memberMapper.selectById(memberId);
        if (member == null || !"0".equals(member.getStatus())) throw new ServiceException("会员不存在或已停用");
        return member;
    }

    private MallMember requireActiveMemberForUpdate(Long memberId)
    {
        MallMember member = memberMapper.selectByIdForUpdate(memberId);
        if (member == null || !"0".equals(member.getStatus())) throw new ServiceException("会员不存在或已停用");
        return member;
    }

    private void revokeAllSessions(Long memberId, String reason)
    {
        List<com.ruoyi.mall.member.domain.MallMemberSession> active = sessionMapper.selectActiveSessionsForUpdate(memberId,
                LocalDateTime.now());
        for (com.ruoyi.mall.member.domain.MallMemberSession session : active)
        {
            if (sessionMapper.markSessionOffline(session.getSessionId(), "REVOKED", reason) != 1)
                throw new ServiceException("设备下线失败，请稍后重试");
            afterCommit(() -> tokenService.deleteByHash(session.getTokenHash()));
        }
    }

    private void audit(Long memberId, String type, String title, String summary, String oldPhone, String newPhone,
            String requestIp)
    {
        MallMemberLifecycleAudit audit = new MallMemberLifecycleAudit();
        audit.setMemberId(memberId); audit.setEventType(type); audit.setTitle(title); audit.setSummary(summary);
        audit.setOldPhone(oldPhone); audit.setNewPhone(newPhone); audit.setRequestIp(requestIp);
        accountMapper.insertLifecycleAudit(audit);
    }

    private MallPhoneChangeRequestVo toVo(MallMemberPhoneChangeRequest request, String ticket)
    {
        return new MallPhoneChangeRequestVo(request.getRequestId(), request.getStatus(),
                MallPhoneChangeRequestVo.maskPhone(request.getOldPhone()),
                MallPhoneChangeRequestVo.maskPhone(request.getNewPhone()), request.getExpiresTime(), ticket);
    }

    private String hash(String value)
    {
        try
        {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.US_ASCII)));
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private void afterCommit(Runnable action)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) { action.run(); return; }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override public void afterCommit() { action.run(); }
        });
    }
}
