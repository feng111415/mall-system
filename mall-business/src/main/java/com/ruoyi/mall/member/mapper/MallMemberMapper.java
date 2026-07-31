package com.ruoyi.mall.member.mapper;

import java.util.List;
import com.ruoyi.mall.member.domain.MallMember;

public interface MallMemberMapper
{
    MallMember selectById(Long memberId);
    MallMember selectByIdForUpdate(Long memberId);
    MallMember selectByPhone(String phone);
    List<MallMember> selectList(MallMember member);
    int insert(MallMember member);
    int update(MallMember member);
    int updateNickname(Long memberId, String nickname, String updateBy);
    int updateAvatar(Long memberId, String avatar, String updateBy);
    int updateLoginInfo(MallMember member);
    int deleteByIds(Long[] memberIds);
}
