package com.ruoyi.ticket.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.ticket.domain.TicketStock;

/**
 * 票务库存Mapper接口
 *
 * @author ruoyi
 */
public interface TicketStockMapper
{
    public TicketStock selectTicketStockById(Long stockId);

    public List<TicketStock> selectTicketStockList(TicketStock ticketStock);

    public int insertTicketStock(TicketStock ticketStock);

    public int updateTicketStock(TicketStock ticketStock);

    public int deleteTicketStockById(Long stockId);

    public int deleteTicketStockByIds(Long[] stockIds);

    public TicketStock selectTicketStockByTypeId(Long typeId);

    public int lockStockByTypeId(@Param("typeId") Long typeId, @Param("quantity") Integer quantity);
}
