package com.ruoyi.mall.member.mapper;

import java.util.List;
import java.util.Date;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.member.domain.MallMemberAddress;
import com.ruoyi.mall.member.domain.MallMemberConsent;
import com.ruoyi.mall.member.domain.MallSmsCode;

public interface MallMemberAuthMapper
{
    MallSmsCode selectLatestSmsCode(@Param("phone") String phone, @Param("purpose") String purpose);
    int countSmsByPhoneSince(@Param("phone") String phone, @Param("since") Date since);
    int countSmsByIpSince(@Param("requestIp") String requestIp, @Param("since") Date since);
    int insertSmsCode(MallSmsCode smsCode);
    int updateSmsSendResult(MallSmsCode smsCode);
    int incrementSmsAttempts(Long smsId);
    int consumeSmsCode(Long smsId);
    int insertConsent(MallMemberConsent consent);
    List<MallMemberAddress> selectAddressList(Long memberId);
    MallMemberAddress selectAddress(@Param("addressId") Long addressId, @Param("memberId") Long memberId);
    int clearDefaultAddress(Long memberId);
    int insertAddress(MallMemberAddress address);
    int updateAddress(MallMemberAddress address);
    int deleteAddress(@Param("addressId") Long addressId, @Param("memberId") Long memberId);
}
