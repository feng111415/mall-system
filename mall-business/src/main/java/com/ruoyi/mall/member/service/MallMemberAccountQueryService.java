package com.ruoyi.mall.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.MemberAfterSaleStatePort;
import com.ruoyi.mall.application.port.MemberOrderStatePort;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallMemberConsent;
import com.ruoyi.mall.member.domain.vo.MallCancellationEligibilityVo;
import com.ruoyi.mall.member.domain.vo.MallMemberAccountOverviewVo;
import com.ruoyi.mall.member.domain.vo.MallMemberLifecycleEventVo;
import com.ruoyi.mall.member.mapper.MallMemberAccountMapper;
import com.ruoyi.mall.member.mapper.MallMemberMapper;

@Service
public class MallMemberAccountQueryService
{
    private final MallMemberMapper memberMapper;
    private final MallMemberAccountMapper accountMapper;
    private final MemberOrderStatePort orderStatePort;
    private final MemberAfterSaleStatePort afterSaleStatePort;

    public MallMemberAccountQueryService(MallMemberMapper memberMapper, MallMemberAccountMapper accountMapper,
            MemberOrderStatePort orderStatePort, MemberAfterSaleStatePort afterSaleStatePort)
    {
        this.memberMapper = memberMapper;
        this.accountMapper = accountMapper;
        this.orderStatePort = orderStatePort;
        this.afterSaleStatePort = afterSaleStatePort;
    }

    public MallMemberAccountOverviewVo overview(Long memberId)
    {
        MallMember member = memberMapper.selectById(memberId);
        if (member == null || !"0".equals(member.getStatus()))
            throw new ServiceException("会员不存在或已停用");
        return buildOverview(member);
    }

    public MallMemberAccountOverviewVo overviewForAdmin(Long memberId)
    {
        MallMember member = memberMapper.selectById(memberId);
        if (member == null) throw new ServiceException("会员不存在");
        return buildOverview(member);
    }

    private MallMemberAccountOverviewVo buildOverview(MallMember member)
    {
        List<MallMemberConsent> consents = accountMapper.selectConsents(member.getMemberId());
        List<MallMemberLifecycleEventVo> lifecycle = accountMapper.selectLifecycleEvents(member.getMemberId());
        MallCancellationEligibilityVo eligibility = new MallCancellationEligibilityVo(
                orderStatePort.countUnfinishedOrders(member.getMemberId()),
                afterSaleStatePort.countInProgressAfterSales(member.getMemberId()));
        return new MallMemberAccountOverviewVo(member.getMemberId(), member.getNickname(), member.getAvatar(),
                maskPhone(member.getPhone()), member.getStatus(), member.getCreateTime(), member.getLastLoginTime(),
                accountMapper.countActiveSessions(member.getMemberId()), consents == null ? List.of() : consents,
                lifecycle == null ? List.of() : lifecycle, eligibility);
    }

    private String maskPhone(String phone)
    {
        return phone != null && phone.matches("^1[3-9]\\d{9}$")
                ? phone.substring(0, 3) + "****" + phone.substring(7) : "";
    }
}
