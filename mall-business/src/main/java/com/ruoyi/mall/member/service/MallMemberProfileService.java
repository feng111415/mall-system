package com.ruoyi.mall.member.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallMemberProfileAudit;
import com.ruoyi.mall.member.domain.vo.MallAvatarPresetVo;
import com.ruoyi.mall.member.domain.vo.MallMemberProfileVo;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.mapper.MallMemberProfileAuditMapper;
import com.ruoyi.mall.member.port.MallAvatarStoragePort;

@Service
public class MallMemberProfileService
{
    private static final int MAX_NICKNAME_CHANGES = 3;
    private static final List<MallAvatarPresetVo> AVATAR_PRESETS = List.of(
            new MallAvatarPresetVo("coral", "珊瑚", "/assets/avatars/avatar-coral.svg"),
            new MallAvatarPresetVo("mint", "薄荷", "/assets/avatars/avatar-mint.svg"),
            new MallAvatarPresetVo("sunny", "晴日", "/assets/avatars/avatar-sunny.svg"),
            new MallAvatarPresetVo("sky", "晴空", "/assets/avatars/avatar-sky.svg"),
            new MallAvatarPresetVo("berry", "莓果", "/assets/avatars/avatar-berry.svg"),
            new MallAvatarPresetVo("graphite", "石墨", "/assets/avatars/avatar-graphite.svg"));
    private final MallMemberMapper memberMapper;
    private final MallMemberProfileAuditMapper auditMapper;
    private final MallNicknamePolicy nicknamePolicy;
    private final MallAvatarStoragePort avatarStorage;

    public MallMemberProfileService(MallMemberMapper memberMapper, MallMemberProfileAuditMapper auditMapper,
            MallNicknamePolicy nicknamePolicy, MallAvatarStoragePort avatarStorage)
    {
        this.memberMapper = memberMapper;
        this.auditMapper = auditMapper;
        this.nicknamePolicy = nicknamePolicy;
        this.avatarStorage = avatarStorage;
    }

    @Transactional
    public MallMember updateNickname(Long memberId, String nickname, String requestIp)
    {
        MallMember member = requireActiveMemberForUpdate(memberId);
        String normalized = nicknamePolicy.validateAndNormalize(nickname);
        if (normalized.equals(member.getNickname()))
        {
            return member;
        }
        Date sinceTime = nicknameWindowStart();
        if (auditMapper.countNicknameChangesSince(memberId, sinceTime) >= MAX_NICKNAME_CHANGES)
        {
            throw new ServiceException("昵称 30 天内最多修改 3 次");
        }
        if (memberMapper.updateNickname(memberId, normalized, "member:" + memberId) != 1)
        {
            throw new ServiceException("昵称修改失败，请稍后重试");
        }
        MallMemberProfileAudit audit = profileAudit(memberId, "NICKNAME",
                member.getNickname(), normalized, requestIp);
        appendAudit(audit);
        member.setNickname(normalized);
        return member;
    }

    public List<MallAvatarPresetVo> avatarPresets()
    {
        return AVATAR_PRESETS;
    }

    public MallMemberProfileVo profile(Long memberId)
    {
        MallMember member = requireActiveMember(memberId);
        int usedChanges = auditMapper.countNicknameChangesSince(memberId, nicknameWindowStart());
        return MallMemberProfileVo.from(member, Math.max(0, MAX_NICKNAME_CHANGES - usedChanges));
    }

    @Transactional
    public MallMember selectPresetAvatar(Long memberId, String presetCode, String requestIp)
    {
        MallAvatarPresetVo preset = AVATAR_PRESETS.stream()
                .filter(item -> item.code().equals(presetCode))
                .findFirst()
                .orElseThrow(() -> new ServiceException("请选择有效的预设头像"));
        MallMember member = requireActiveMemberForUpdate(memberId);
        return changeAvatar(member, preset.url(), requestIp);
    }

    @Transactional
    public MallMember uploadAvatar(Long memberId, MultipartFile file, String requestIp)
    {
        String storedUrl = avatarStorage.store(memberId, file);
        afterRollback(() -> avatarStorage.deleteOwned(memberId, storedUrl));
        try
        {
            MallMember member = requireActiveMemberForUpdate(memberId);
            return changeAvatar(member, storedUrl, requestIp);
        }
        catch (RuntimeException exception)
        {
            avatarStorage.deleteOwned(memberId, storedUrl);
            throw exception;
        }
    }

    private MallMember changeAvatar(MallMember member, String avatarUrl, String requestIp)
    {
        String oldAvatar = member.getAvatar();
        if (avatarUrl.equals(oldAvatar))
        {
            return member;
        }
        Long memberId = member.getMemberId();
        if (memberMapper.updateAvatar(memberId, avatarUrl, "member:" + memberId) != 1)
        {
            throw new ServiceException("头像修改失败，请稍后重试");
        }
        appendAudit(profileAudit(memberId, "AVATAR", oldAvatar, avatarUrl, requestIp));
        member.setAvatar(avatarUrl);
        afterCommit(() -> avatarStorage.deleteOwned(memberId, oldAvatar));
        return member;
    }

    private void afterCommit(Runnable action)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                action.run();
            }
        });
    }

    private void afterRollback(Runnable action)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCompletion(int status)
            {
                if (status != TransactionSynchronization.STATUS_COMMITTED)
                {
                    action.run();
                }
            }
        });
    }

    private MallMemberProfileAudit profileAudit(Long memberId, String changeType,
            String oldValue, String newValue, String requestIp)
    {
        MallMemberProfileAudit audit = new MallMemberProfileAudit();
        audit.setMemberId(memberId);
        audit.setChangeType(changeType);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        audit.setChangeSource("MEMBER_PORTAL");
        audit.setRequestIp(requestIp);
        return audit;
    }

    private void appendAudit(MallMemberProfileAudit audit)
    {
        if (auditMapper.insert(audit) != 1)
        {
            throw new ServiceException("资料审计记录失败，请稍后重试");
        }
    }

    private MallMember requireActiveMemberForUpdate(Long memberId)
    {
        MallMember member = memberMapper.selectByIdForUpdate(memberId);
        if (member == null || !"0".equals(member.getStatus()))
        {
            throw new ServiceException("会员不存在或已停用");
        }
        return member;
    }

    private MallMember requireActiveMember(Long memberId)
    {
        MallMember member = memberMapper.selectById(memberId);
        if (member == null || !"0".equals(member.getStatus()))
        {
            throw new ServiceException("会员不存在或已停用");
        }
        return member;
    }

    private Date nicknameWindowStart()
    {
        return Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
    }
}
