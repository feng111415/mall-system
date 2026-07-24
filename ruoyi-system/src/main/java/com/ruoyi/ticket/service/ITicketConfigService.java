package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketConfig;

/**
 * 票务配置Service接口
 *
 * @author ruoyi
 */
public interface ITicketConfigService
{
    public TicketConfig selectTicketConfigById(Long configId);

    public List<TicketConfig> selectTicketConfigList(TicketConfig ticketConfig);

    public int insertTicketConfig(TicketConfig ticketConfig);

    public int updateTicketConfig(TicketConfig ticketConfig);

    public int deleteTicketConfigByIds(Long[] configIds);

    public int deleteTicketConfigById(Long configId);
}
