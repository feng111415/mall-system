package com.ruoyi.piano.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.piano.domain.PianoHomework;

/**
 * 钢琴课后作业Mapper接口
 *
 * @author ruoyi
 */
public interface PianoHomeworkMapper
{
    public PianoHomework selectPianoHomeworkById(Long homeworkId);

    public PianoHomework selectPianoHomeworkBySubmitCode(String submitCode);

    public List<PianoHomework> selectPianoHomeworkList(PianoHomework pianoHomework);

    public int insertPianoHomework(PianoHomework pianoHomework);

    public int updatePianoHomework(PianoHomework pianoHomework);

    public int deletePianoHomeworkById(Long homeworkId);

    public int deletePianoHomeworkByIds(Long[] homeworkIds);

    public int updatePianoHomeworkStatus(@Param("homeworkId") Long homeworkId, @Param("homeworkStatus") String homeworkStatus);

    public Long selectPianoHomeworkCountBySubmitCode(String submitCode);

    public Long selectWeekHomeworkCount();

    public Long selectOverdueUnfinishedHomeworkCount();

    public Long selectPianoHomeworkCountByStatus(String homeworkStatus);

    public List<PianoHomework> selectDashboardTodoHomeworks();
}
