package com.ruoyi.mall.reconciliation.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.reconciliation.domain.MallReconciliationAlert;

public interface MallReconciliationAlertMapper {
    int insertIgnore(MallReconciliationAlert alert);
    MallReconciliationAlert selectById(@Param("alertId") Long alertId);
    List<MallReconciliationAlert> selectList(@Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);
    int acknowledge(@Param("alertId") Long alertId, @Param("operator") String operator);
}
