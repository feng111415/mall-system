package com.ruoyi.mall.member.mapper;

import java.util.List;
import com.ruoyi.mall.member.domain.MallMemberConsent;
import com.ruoyi.mall.member.domain.MallMemberPhoneChangeRequest;
import com.ruoyi.mall.member.domain.MallMemberAccountActionTicket;
import com.ruoyi.mall.member.domain.MallMemberLifecycleAudit;
import com.ruoyi.mall.member.domain.vo.MallMemberLifecycleEventVo;

public interface MallMemberAccountMapper
{
    List<MallMemberConsent> selectConsents(Long memberId);
    List<MallMemberLifecycleEventVo> selectLifecycleEvents(Long memberId);
    int countActiveSessions(Long memberId);
    int insertPhoneChangeRequest(MallMemberPhoneChangeRequest request);
    MallMemberPhoneChangeRequest selectPhoneChangeRequest(@org.apache.ibatis.annotations.Param("requestId") Long requestId,
            @org.apache.ibatis.annotations.Param("memberId") Long memberId);
    int markOldPhoneVerified(@org.apache.ibatis.annotations.Param("requestId") Long requestId,
            @org.apache.ibatis.annotations.Param("memberId") Long memberId);
    int markNewPhoneVerified(@org.apache.ibatis.annotations.Param("requestId") Long requestId,
            @org.apache.ibatis.annotations.Param("memberId") Long memberId);
    int insertActionTicket(MallMemberAccountActionTicket ticket);
    MallMemberAccountActionTicket selectValidActionTicket(@org.apache.ibatis.annotations.Param("ticketHash") String ticketHash,
            @org.apache.ibatis.annotations.Param("requestId") Long requestId, @org.apache.ibatis.annotations.Param("memberId") Long memberId);
    int consumeActionTicket(@org.apache.ibatis.annotations.Param("ticketId") Long ticketId);
    int insertLifecycleAudit(MallMemberLifecycleAudit audit);
}
