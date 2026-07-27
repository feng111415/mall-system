package com.ruoyi.mall.audit.mapper;

import java.util.List;
import com.ruoyi.mall.audit.domain.MallOrderOperationLogQuery;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;

public interface MallAuditMapper
{
    List<MallOrderOperationLog> selectOrderOperationLogs(MallOrderOperationLogQuery query);
}
