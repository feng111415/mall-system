package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketCheckinCode;

/**
 * 票务核销码Mapper接口
 *
 * @author ruoyi
 */
public interface TicketCheckinCodeMapper
{
    public TicketCheckinCode selectTicketCheckinCodeById(Long codeId);

    public List<TicketCheckinCode> selectTicketCheckinCodeList(TicketCheckinCode ticketCheckinCode);

    public int insertTicketCheckinCode(TicketCheckinCode ticketCheckinCode);

    public int updateTicketCheckinCode(TicketCheckinCode ticketCheckinCode);

    public int deleteTicketCheckinCodeById(Long codeId);

    public int deleteTicketCheckinCodeByIds(Long[] codeIds);
}
