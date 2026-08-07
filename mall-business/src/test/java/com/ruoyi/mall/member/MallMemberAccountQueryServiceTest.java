package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.application.port.MemberAfterSaleStatePort;
import com.ruoyi.mall.application.port.MemberOrderStatePort;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallMemberConsent;
import com.ruoyi.mall.member.domain.vo.MallMemberLifecycleEventVo;
import com.ruoyi.mall.member.mapper.MallMemberAccountMapper;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.service.MallMemberAccountQueryService;

class MallMemberAccountQueryServiceTest
{
    @Mock private MallMemberMapper memberMapper;
    @Mock private MallMemberAccountMapper accountMapper;
    @Mock private MemberOrderStatePort orderStatePort;
    @Mock private MemberAfterSaleStatePort afterSaleStatePort;
    private MallMemberAccountQueryService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallMemberAccountQueryService(memberMapper, accountMapper,
                orderStatePort, afterSaleStatePort);
    }

    @Test
    void memberCanReadMaskedAccountOverviewAndRealCancellationBlockers()
    {
        MallMember member = new MallMember();
        member.setMemberId(9L);
        member.setPhone("13800138000");
        member.setNickname("测试会员");
        member.setStatus("0");
        member.setLastLoginTime(new Date(1_700_000_000_000L));
        member.setCreateTime(new Date(1_600_000_000_000L));
        MallMemberConsent consent = new MallMemberConsent();
        consent.setConsentType("PRIVACY_POLICY");
        consent.setConsentVersion("1.0");
        consent.setConsentTime(new Date(1_650_000_000_000L));
        when(memberMapper.selectById(9L)).thenReturn(member);
        when(accountMapper.selectConsents(9L)).thenReturn(List.of(consent));
        when(accountMapper.countActiveSessions(9L)).thenReturn(2);
        when(orderStatePort.countUnfinishedOrders(9L)).thenReturn(1);
        when(afterSaleStatePort.countInProgressAfterSales(9L)).thenReturn(3);

        var overview = service.overview(9L);

        assertEquals("138****8000", overview.getMaskedPhone());
        assertEquals(2, overview.getOnlineDeviceCount());
        assertEquals(1, overview.getConsents().size());
        assertEquals(1, overview.getCancellationEligibility().getUnfinishedOrderCount());
        assertEquals(3, overview.getCancellationEligibility().getInProgressAfterSaleCount());
        assertEquals(false, overview.getCancellationEligibility().isEligible());
    }

    @Test
    void overviewReturnsRealLifecycleEventsInMapperOrder()
    {
        MallMember member = new MallMember();
        member.setMemberId(9L);
        member.setPhone("13800138000");
        member.setStatus("0");
        when(memberMapper.selectById(9L)).thenReturn(member);
        when(accountMapper.selectConsents(9L)).thenReturn(List.of());
        when(accountMapper.selectLifecycleEvents(9L)).thenReturn(List.of(
                new MallMemberLifecycleEventVo("SESSION_LOGIN", "当前设备登录", "Windows 电脑", new Date(3000)),
                new MallMemberLifecycleEventVo("ACCOUNT_CREATED", "账号创建", "手机号验证注册", new Date(1000))));

        var overview = service.overview(9L);

        assertEquals(2, overview.getLifecycle().size());
        assertEquals("SESSION_LOGIN", overview.getLifecycle().get(0).getEventType());
        assertEquals("ACCOUNT_CREATED", overview.getLifecycle().get(1).getEventType());
    }
}
