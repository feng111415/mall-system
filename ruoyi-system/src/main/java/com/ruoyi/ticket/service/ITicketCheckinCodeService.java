package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketCheckinCode;

/**
 * 票务核销码Service接口
 *
 * @author ruoyi
 */
public interface ITicketCheckinCodeService
{
    public TicketCheckinCode selectTicketCheckinCodeById(Long codeId);

    public List<TicketCheckinCode> selectTicketCheckinCodeList(TicketCheckinCode ticketCheckinCode);

    public int insertTicketCheckinCode(TicketCheckinCode ticketCheckinCode);

    public int updateTicketCheckinCode(TicketCheckinCode ticketCheckinCode);

    public int deleteTicketCheckinCodeByIds(Long[] codeIds);

    public int deleteTicketCheckinCodeById(Long codeId);
}
