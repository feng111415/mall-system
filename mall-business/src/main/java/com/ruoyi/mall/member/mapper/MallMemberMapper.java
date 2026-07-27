package com.ruoyi.mall.member.mapper;

import java.util.List;
import com.ruoyi.mall.member.domain.MallMember;

public interface MallMemberMapper
{
    MallMember selectById(Long memberId);
    MallMember selectByPhone(String phone);
    List<MallMember> selectList(MallMember member);
    int insert(MallMember member);
    int update(MallMember member);
    int updateLoginInfo(MallMember member);
    int deleteByIds(Long[] memberIds);
}
