package com.ruoyi.mall.member.message.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.member.message.domain.MallMemberMessage;
import com.ruoyi.mall.member.message.domain.MallMemberMessageSummary;

public interface MallMemberMessageMapper
{
    List<MallMemberMessageSummary> selectSummary(@Param("memberId") Long memberId);
    List<MallMemberMessage> selectList(@Param("memberId") Long memberId, @Param("category") String category);
    MallMemberMessage selectById(@Param("memberId") Long memberId, @Param("messageId") Long messageId);
    int insert(MallMemberMessage message);
    int markRead(@Param("memberId") Long memberId, @Param("messageId") Long messageId);
    int markAllRead(@Param("memberId") Long memberId, @Param("category") String category);
}
