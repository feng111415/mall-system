package com.ruoyi.mall.member.mapper;

import java.util.Date;
import com.ruoyi.mall.member.domain.MallMemberProfileAudit;

public interface MallMemberProfileAuditMapper
{
    int countNicknameChangesSince(Long memberId, Date sinceTime);
    int insert(MallMemberProfileAudit audit);
}
