package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallMemberProfileAudit;
import com.ruoyi.mall.member.domain.vo.MallMemberProfileVo;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.mapper.MallMemberProfileAuditMapper;
import com.ruoyi.mall.member.port.MallAvatarStoragePort;
import com.ruoyi.mall.member.service.MallMemberProfileService;
import com.ruoyi.mall.member.service.MallNicknamePolicy;

class MallMemberProfileServiceTest
{
    @Mock private MallMemberMapper memberMapper;
    @Mock private MallMemberProfileAuditMapper auditMapper;
    @Mock private MallAvatarStoragePort avatarStorage;
    private MallMemberProfileService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallMemberProfileService(memberMapper, auditMapper,
                new MallNicknamePolicy("管理员,官方客服,加微信"), avatarStorage);
    }

    @Test
    void rejectsFourthNicknameChangeWithinThirtyDays()
    {
        MallMember member = activeMember("原昵称");
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(member);
        when(auditMapper.countNicknameChangesSince(eq(7L), any(Date.class))).thenReturn(3);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateNickname(7L, "新昵称", "127.0.0.1"));

        assertEquals("昵称 30 天内最多修改 3 次", exception.getMessage());
        verify(memberMapper, never()).updateNickname(any(), anyString(), anyString());
        verify(auditMapper, never()).insert(any());
    }

    @Test
    void unchangedNicknameDoesNotConsumeModificationQuota()
    {
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(activeMember("原昵称"));
        when(auditMapper.countNicknameChangesSince(eq(7L), any(Date.class))).thenReturn(3);

        MallMember result = service.updateNickname(7L, " 原昵称 ", "127.0.0.1");

        assertEquals("原昵称", result.getNickname());
        verify(memberMapper, never()).updateNickname(any(), anyString(), anyString());
        verify(auditMapper, never()).insert(any());
    }

    @Test
    void profileReportsRemainingNicknameChangesInCurrentWindow()
    {
        when(memberMapper.selectById(7L)).thenReturn(activeMember("原昵称"));
        when(auditMapper.countNicknameChangesSince(eq(7L), any(Date.class))).thenReturn(2);

        MallMemberProfileVo profile = service.profile(7L);

        assertEquals(1, profile.getNicknameChangesRemaining());
        assertEquals(30, profile.getNicknameChangeWindowDays());
    }

    @Test
    void updatesNicknameAndAppendsAuditInOneUseCase()
    {
        MallMember member = activeMember("原昵称");
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(member);
        when(auditMapper.countNicknameChangesSince(eq(7L), any(Date.class))).thenReturn(2);
        when(memberMapper.updateNickname(7L, "新昵称", "member:7")).thenReturn(1);
        when(auditMapper.insert(any())).thenReturn(1);

        MallMember result = service.updateNickname(7L, "  新昵称  ", "127.0.0.1");

        assertEquals("新昵称", result.getNickname());
        ArgumentCaptor<MallMemberProfileAudit> auditCaptor = ArgumentCaptor.forClass(MallMemberProfileAudit.class);
        verify(auditMapper).insert(auditCaptor.capture());
        MallMemberProfileAudit audit = auditCaptor.getValue();
        assertEquals("NICKNAME", audit.getChangeType());
        assertEquals("原昵称", audit.getOldValue());
        assertEquals("新昵称", audit.getNewValue());
        assertEquals("MEMBER_PORTAL", audit.getChangeSource());
        assertEquals("127.0.0.1", audit.getRequestIp());
    }

    @Test
    void failsProfileChangeWhenAuditCannotBeAppended()
    {
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(activeMember("原昵称"));
        when(auditMapper.countNicknameChangesSince(eq(7L), any(Date.class))).thenReturn(0);
        when(memberMapper.updateNickname(7L, "新昵称", "member:7")).thenReturn(1);
        when(auditMapper.insert(any())).thenReturn(0);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateNickname(7L, "新昵称", "127.0.0.1"));

        assertEquals("资料审计记录失败，请稍后重试", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = { "联系我13800138000", "官方客服小王", "www.example.com", "foo@example.com", "QQ123456", "vx_abcd" })
    void rejectsSensitiveOrContactInformationInNickname(String nickname)
    {
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(activeMember("原昵称"));

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.updateNickname(7L, nickname, "127.0.0.1"));

        assertEquals("昵称包含联系方式或不适合公开展示的内容", exception.getMessage());
        verify(memberMapper, never()).updateNickname(any(), anyString(), anyString());
        verify(auditMapper, never()).insert(any());
    }

    @Test
    void rejectsAvatarPresetOutsideServerWhitelist()
    {
        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.selectPresetAvatar(7L, "../../private", "127.0.0.1"));

        assertEquals("请选择有效的预设头像", exception.getMessage());
        verify(memberMapper, never()).updateAvatar(any(), anyString(), anyString());
        verify(auditMapper, never()).insert(any());
    }

    @Test
    void selectsWhitelistedPresetAndAppendsAvatarAudit()
    {
        MallMember member = activeMember("原昵称");
        member.setAvatar("/profile/mall/avatar/7/0123456789abcdef0123456789abcdef.png");
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(member);
        when(memberMapper.updateAvatar(7L, "/assets/avatars/avatar-mint.svg", "member:7")).thenReturn(1);
        when(auditMapper.insert(any())).thenReturn(1);

        MallMember result = service.selectPresetAvatar(7L, "mint", "127.0.0.1");

        assertEquals("/assets/avatars/avatar-mint.svg", result.getAvatar());
        ArgumentCaptor<MallMemberProfileAudit> auditCaptor = ArgumentCaptor.forClass(MallMemberProfileAudit.class);
        verify(auditMapper).insert(auditCaptor.capture());
        assertEquals("AVATAR", auditCaptor.getValue().getChangeType());
        assertEquals("/assets/avatars/avatar-mint.svg", auditCaptor.getValue().getNewValue());
        verify(avatarStorage).deleteOwned(7L,
                "/profile/mall/avatar/7/0123456789abcdef0123456789abcdef.png");
    }

    @Test
    void storesUploadedAvatarAndAppendsAudit()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[] { 1, 2, 3 });
        MallMember member = activeMember("原昵称");
        member.setAvatar("/assets/avatars/avatar-coral.svg");
        String storedUrl = "/profile/mall/avatar/7/fedcba9876543210fedcba9876543210.png";
        when(avatarStorage.store(7L, file)).thenReturn(storedUrl);
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(member);
        when(memberMapper.updateAvatar(7L, storedUrl, "member:7")).thenReturn(1);
        when(auditMapper.insert(any())).thenReturn(1);

        MallMember result = service.uploadAvatar(7L, file, "127.0.0.1");

        assertEquals(storedUrl, result.getAvatar());
        ArgumentCaptor<MallMemberProfileAudit> auditCaptor = ArgumentCaptor.forClass(MallMemberProfileAudit.class);
        verify(auditMapper).insert(auditCaptor.capture());
        assertEquals("AVATAR", auditCaptor.getValue().getChangeType());
        assertEquals(storedUrl, auditCaptor.getValue().getNewValue());
    }

    @Test
    void deletesNewUploadWhenProfileUpdateFails()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", new byte[] { 1, 2, 3 });
        String storedUrl = "/profile/mall/avatar/7/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.png";
        when(avatarStorage.store(7L, file)).thenReturn(storedUrl);
        when(memberMapper.selectByIdForUpdate(7L)).thenReturn(activeMember("原昵称"));
        when(memberMapper.updateAvatar(7L, storedUrl, "member:7")).thenReturn(0);

        assertThrows(ServiceException.class,
                () -> service.uploadAvatar(7L, file, "127.0.0.1"));

        verify(avatarStorage).deleteOwned(7L, storedUrl);
        verify(auditMapper, never()).insert(any());
    }

    private MallMember activeMember(String nickname)
    {
        MallMember member = new MallMember();
        member.setMemberId(7L);
        member.setNickname(nickname);
        member.setStatus("0");
        return member;
    }
}
