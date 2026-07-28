package com.ruoyi.mall.governance.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.governance.domain.MallCompensationTask;

public interface MallCompensationMapper
{
    int insertIgnore(MallCompensationTask task);
    MallCompensationTask selectByBusinessKey(@Param("taskType") String taskType,
            @Param("businessKey") String businessKey);
    MallCompensationTask selectById(@Param("taskId") Long taskId);
    List<MallCompensationTask> selectDue(@Param("limit") int limit);
    List<MallCompensationTask> selectList(@Param("status") String status,
            @Param("taskType") String taskType, @Param("limit") int limit, @Param("offset") int offset);
    int markProcessing(@Param("taskId") Long taskId);
    int markSuccess(@Param("taskId") Long taskId);
    int markFailure(@Param("taskId") Long taskId, @Param("status") String status,
            @Param("nextRetryTime") LocalDateTime nextRetryTime, @Param("lastError") String lastError);
    int resetForRetry(@Param("taskId") Long taskId);
    int recoverStaleProcessing(@Param("cutoffTime") LocalDateTime cutoffTime);
}
