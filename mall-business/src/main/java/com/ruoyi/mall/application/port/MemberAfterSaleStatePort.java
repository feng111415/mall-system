package com.ruoyi.mall.application.port;

/** 会员账号注销时读取售后模块处理中状态的应用接口。 */
public interface MemberAfterSaleStatePort
{
    int countInProgressAfterSales(Long memberId);
}
