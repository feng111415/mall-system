package com.ruoyi.mall.member.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.service.IMallMemberService;

@Service
public class MallMemberServiceImpl implements IMallMemberService
{
    private final MallMemberMapper memberMapper;

    public MallMemberServiceImpl(MallMemberMapper memberMapper)
    {
        this.memberMapper = memberMapper;
    }

    @Override
    public MallMember selectById(Long memberId) { return memberMapper.selectById(memberId); }

    @Override
    public List<MallMember> selectList(MallMember member) { return memberMapper.selectList(member); }

    @Override
    public int update(MallMember member) { return memberMapper.update(member); }

    @Override
    public int deleteByIds(Long[] memberIds) { return memberMapper.deleteByIds(memberIds); }
}
