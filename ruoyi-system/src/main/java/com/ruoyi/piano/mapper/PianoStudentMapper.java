package com.ruoyi.piano.mapper;

import java.util.List;
import com.ruoyi.piano.domain.PianoStudent;

/**
 * 钢琴学生档案Mapper接口
 *
 * @author ruoyi
 */
public interface PianoStudentMapper
{
    public PianoStudent selectPianoStudentById(Long studentId);

    public List<PianoStudent> selectPianoStudentList(PianoStudent pianoStudent);

    public List<PianoStudent> selectPianoStudentOptions();

    public Long selectActivePianoStudentCount();

    public int insertPianoStudent(PianoStudent pianoStudent);

    public int updatePianoStudent(PianoStudent pianoStudent);

    public int deletePianoStudentById(Long studentId);

    public int deletePianoStudentByIds(Long[] studentIds);
}
