package com.ruoyi.mall.audit.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.mall.audit.domain.MallOrderOperationLogQuery;
import com.ruoyi.mall.audit.mapper.MallAuditMapper;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;

@Service
public class MallAuditService
{
    private final MallAuditMapper mapper;

    public MallAuditService(MallAuditMapper mapper) { this.mapper = mapper; }

    public List<MallOrderOperationLog> orderOperationLogs(MallOrderOperationLogQuery query)
    {
        return mapper.selectOrderOperationLogs(query == null ? new MallOrderOperationLogQuery() : query);
    }
}
