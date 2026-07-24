package com.ruoyi.piano.service;

import java.util.List;
import com.ruoyi.piano.domain.PianoHomework;
import com.ruoyi.piano.domain.dto.PianoPortalSubmitRequest;
import com.ruoyi.piano.domain.vo.PianoDashboardVo;
import com.ruoyi.piano.domain.vo.PianoPortalHomeworkVo;

/**
 * 钢琴课后作业Service接口
 *
 * @author ruoyi
 */
public interface IPianoHomeworkService
{
    public PianoHomework selectPianoHomeworkById(Long homeworkId);

    public PianoHomework selectPianoHomeworkBySubmitCode(String submitCode);

    public PianoPortalHomeworkVo selectPianoPortalHomeworkBySubmitCode(String submitCode);

    public List<PianoHomework> selectPianoHomeworkList(PianoHomework pianoHomework);

    public int insertPianoHomework(PianoHomework pianoHomework);

    public int updatePianoHomework(PianoHomework pianoHomework);

    public int deletePianoHomeworkByIds(Long[] homeworkIds);

    public int deletePianoHomeworkById(Long homeworkId);

    public int submitPianoPortalHomework(PianoPortalSubmitRequest submitRequest);

    public PianoDashboardVo selectPianoDashboard();
}
