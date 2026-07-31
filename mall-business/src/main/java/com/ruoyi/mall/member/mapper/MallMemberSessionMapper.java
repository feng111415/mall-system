package com.ruoyi.mall.member.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.member.domain.MallMemberDevice;
import com.ruoyi.mall.member.domain.MallMemberDeviceAudit;
import com.ruoyi.mall.member.domain.MallMemberSession;

public interface MallMemberSessionMapper
{
    MallMemberDevice selectDevice(@Param("memberId") Long memberId, @Param("deviceIdentifier") String deviceIdentifier);
    MallMemberDevice selectDeviceById(@Param("memberId") Long memberId, @Param("memberDeviceId") Long memberDeviceId);
    MallMemberDevice selectPrimaryMobileDevice(@Param("memberId") Long memberId);
    int insertDevice(MallMemberDevice device);
    int updateDeviceLogin(MallMemberDevice device);
    int clearPrimaryMobile(@Param("memberId") Long memberId);
    int markPrimaryMobile(@Param("memberId") Long memberId, @Param("memberDeviceId") Long memberDeviceId);
    int expireSessions(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);
    List<MallMemberSession> selectActiveSessionsForUpdate(@Param("memberId") Long memberId,
            @Param("now") LocalDateTime now);
    List<MallMemberSession> selectActiveSessions(@Param("memberId") Long memberId,
            @Param("now") LocalDateTime now);
    MallMemberSession selectActiveSession(@Param("memberId") Long memberId,
            @Param("sessionId") Long sessionId, @Param("now") LocalDateTime now);
    int insertSession(MallMemberSession session);
    int markSessionOffline(@Param("sessionId") Long sessionId, @Param("status") String status,
            @Param("reason") String reason);
    int countPrimaryChangesSince(@Param("memberId") Long memberId, @Param("sinceTime") LocalDateTime sinceTime);
    int insertDeviceAudit(MallMemberDeviceAudit audit);
}
