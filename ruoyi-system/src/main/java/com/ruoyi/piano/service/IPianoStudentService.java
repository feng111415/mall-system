package com.ruoyi.piano.service;

import java.util.List;
import com.ruoyi.piano.domain.PianoStudent;

/**
 * 钢琴学生档案Service接口
 *
 * @author ruoyi
 */
public interface IPianoStudentService
{
    public PianoStudent selectPianoStudentById(Long studentId);

    public List<PianoStudent> selectPianoStudentList(PianoStudent pianoStudent);

    public List<PianoStudent> selectPianoStudentOptions();

    public int insertPianoStudent(PianoStudent pianoStudent);

    public int updatePianoStudent(PianoStudent pianoStudent);

    public int deletePianoStudentByIds(Long[] studentIds);

    public int deletePianoStudentById(Long studentId);
}
