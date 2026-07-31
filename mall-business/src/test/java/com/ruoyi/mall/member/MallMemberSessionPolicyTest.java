package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.Test;
import com.ruoyi.mall.member.service.MallMemberSessionPolicy;
import com.ruoyi.mall.member.service.MallMemberSessionPolicy.LoginDecision;
import com.ruoyi.mall.member.service.MallMemberSessionPolicy.OnlineSession;

class MallMemberSessionPolicyTest
{
    private final MallMemberSessionPolicy policy = new MallMemberSessionPolicy();

    @Test
    void thirdMobileKeepsPrimaryAndReplacesOrdinaryMobile()
    {
        List<OnlineSession> online = List.of(
                new OnlineSession(11L, "primary", "MOBILE", true),
                new OnlineSession(12L, "ordinary", "MOBILE", false));

        LoginDecision decision = policy.decideLogin("new-mobile", "MOBILE", false, online);

        assertFalse(decision.primaryMobile());
        assertEquals(List.of(12L), decision.replacedSessionIds());
    }

    @Test
    void returningPrimaryMobileReplacesSameSessionAndKeepsPrimaryRole()
    {
        List<OnlineSession> online = List.of(
                new OnlineSession(11L, "primary", "MOBILE", true),
                new OnlineSession(12L, "ordinary", "MOBILE", false));

        LoginDecision decision = policy.decideLogin("primary", "MOBILE", true, online);

        assertTrue(decision.primaryMobile());
        assertEquals(List.of(11L), decision.replacedSessionIds());
    }

    @Test
    void returningOfflinePrimaryMakesRoomWhenTwoOrdinaryMobilesAreOnline()
    {
        List<OnlineSession> online = List.of(
                new OnlineSession(21L, "ordinary-a", "MOBILE", false),
                new OnlineSession(22L, "ordinary-b", "MOBILE", false));

        LoginDecision decision = policy.decideLogin("primary", "MOBILE", true, online);

        assertTrue(decision.primaryMobile());
        assertEquals(List.of(21L), decision.replacedSessionIds());
    }

    @Test
    void newDesktopReplacesExistingDesktopButDoesNotTouchMobileSessions()
    {
        List<OnlineSession> online = List.of(
                new OnlineSession(31L, "desktop-old", "DESKTOP", false),
                new OnlineSession(32L, "mobile", "MOBILE", true));

        LoginDecision decision = policy.decideLogin("desktop-new", "DESKTOP", false, online);

        assertFalse(decision.primaryMobile());
        assertEquals(List.of(31L), decision.replacedSessionIds());
    }

    @Test
    void sameOrdinaryDeviceLoginRotatesOnlyItsOwnSession()
    {
        List<OnlineSession> online = List.of(
                new OnlineSession(41L, "primary", "MOBILE", true),
                new OnlineSession(42L, "ordinary", "MOBILE", false));

        LoginDecision decision = policy.decideLogin("ordinary", "MOBILE", false, online);

        assertFalse(decision.primaryMobile());
        assertEquals(List.of(42L), decision.replacedSessionIds());
    }
}
