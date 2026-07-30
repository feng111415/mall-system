package com.ruoyi.mall.risk.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.risk.domain.MallRiskRecord;

public interface MallRiskMapper
{
    Long lockMemberForOrder(@Param("memberId") Long memberId);
    int countPendingOrders(@Param("memberId") Long memberId);
    int insertRecord(MallRiskRecord record);
    List<MallRiskRecord> selectRecords(@Param("decision") String decision,
            @Param("limit") int limit, @Param("offset") int offset);
}
