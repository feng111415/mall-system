package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketConfig;
import com.ruoyi.ticket.mapper.TicketConfigMapper;
import com.ruoyi.ticket.service.ITicketConfigService;

/**
 * 票务配置Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketConfigServiceImpl implements ITicketConfigService
{
    @Autowired
    private TicketConfigMapper ticketConfigMapper;

    @Override
    public TicketConfig selectTicketConfigById(Long configId)
    {
        return ticketConfigMapper.selectTicketConfigById(configId);
    }

    @Override
    public List<TicketConfig> selectTicketConfigList(TicketConfig ticketConfig)
    {
        return ticketConfigMapper.selectTicketConfigList(ticketConfig);
    }

    @Override
    public int insertTicketConfig(TicketConfig ticketConfig)
    {
        return ticketConfigMapper.insertTicketConfig(ticketConfig);
    }

    @Override
    public int updateTicketConfig(TicketConfig ticketConfig)
    {
        return ticketConfigMapper.updateTicketConfig(ticketConfig);
    }

    @Override
    public int deleteTicketConfigByIds(Long[] configIds)
    {
        return ticketConfigMapper.deleteTicketConfigByIds(configIds);
    }

    @Override
    public int deleteTicketConfigById(Long configId)
    {
        return ticketConfigMapper.deleteTicketConfigById(configId);
    }
}
