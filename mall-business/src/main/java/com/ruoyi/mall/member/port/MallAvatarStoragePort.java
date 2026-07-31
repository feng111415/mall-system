package com.ruoyi.mall.member.port;

import org.springframework.web.multipart.MultipartFile;

public interface MallAvatarStoragePort
{
    String store(Long memberId, MultipartFile file);
    void deleteOwned(Long memberId, String avatarUrl);
}
