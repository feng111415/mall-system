package com.ruoyi.mall.application.port;

/** 会员账号注销时读取订单模块未完结状态的应用接口。 */
public interface MemberOrderStatePort
{
    int countUnfinishedOrders(Long memberId);
}
