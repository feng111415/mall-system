package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketConfig;

/**
 * 票务配置Mapper接口
 *
 * @author ruoyi
 */
public interface TicketConfigMapper
{
    public TicketConfig selectTicketConfigById(Long configId);

    public List<TicketConfig> selectTicketConfigList(TicketConfig ticketConfig);

    public int insertTicketConfig(TicketConfig ticketConfig);

    public int updateTicketConfig(TicketConfig ticketConfig);

    public int deleteTicketConfigById(Long configId);

    public int deleteTicketConfigByIds(Long[] configIds);
}
