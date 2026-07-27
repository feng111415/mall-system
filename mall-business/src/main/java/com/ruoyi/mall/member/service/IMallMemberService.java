package com.ruoyi.mall.member.service;

import java.util.List;
import com.ruoyi.mall.member.domain.MallMember;

public interface IMallMemberService
{
    MallMember selectById(Long memberId);
    List<MallMember> selectList(MallMember member);
    int update(MallMember member);
    int deleteByIds(Long[] memberIds);
}
