package com.ruoyi.mall.reconciliation.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.reconciliation.domain.MallReconciliationDiff;

public interface MallReconciliationMapper {
    int insertIgnore(MallReconciliationDiff diff);
    MallReconciliationDiff selectById(@Param("diffId") Long diffId);
    MallReconciliationDiff selectByBusinessCode(@Param("diffType") String diffType,
            @Param("businessNo") String businessNo, @Param("diffCode") String diffCode);
    List<MallReconciliationDiff> selectList(@Param("diffType") String diffType, @Param("status") String status,
            @Param("businessNo") String businessNo, @Param("limit") int limit, @Param("offset") int offset);
    List<MallReconciliationDiff> selectUnresolved(@Param("cutoffTime") java.time.LocalDateTime cutoffTime,
            @Param("limit") int limit);
    int updateStatus(@Param("diffId") Long diffId, @Param("status") String status, @Param("remark") String remark);
}
