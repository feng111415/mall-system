package com.ruoyi.mall.member.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public record MallPhoneChangeRequestVo(Long requestId, String status, String oldPhone, String newPhone,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date expiresTime, String ticket)
{
    public static String maskPhone(String phone)
    {
        return phone != null && phone.matches("^1[3-9]\\d{9}$")
                ? phone.substring(0, 3) + "****" + phone.substring(7) : "";
    }
}
