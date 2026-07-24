package com.ruoyi.piano.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.piano.domain.PianoStudent;
import com.ruoyi.piano.mapper.PianoStudentMapper;
import com.ruoyi.piano.service.IPianoStudentService;

/**
 * 钢琴学生档案Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class PianoStudentServiceImpl implements IPianoStudentService
{
    @Autowired
    private PianoStudentMapper pianoStudentMapper;

    @Override
    public PianoStudent selectPianoStudentById(Long studentId)
    {
        return pianoStudentMapper.selectPianoStudentById(studentId);
    }

    @Override
    public List<PianoStudent> selectPianoStudentList(PianoStudent pianoStudent)
    {
        return pianoStudentMapper.selectPianoStudentList(pianoStudent);
    }

    @Override
    public List<PianoStudent> selectPianoStudentOptions()
    {
        return pianoStudentMapper.selectPianoStudentOptions();
    }

    @Override
    public int insertPianoStudent(PianoStudent pianoStudent)
    {
        return pianoStudentMapper.insertPianoStudent(pianoStudent);
    }

    @Override
    public int updatePianoStudent(PianoStudent pianoStudent)
    {
        return pianoStudentMapper.updatePianoStudent(pianoStudent);
    }

    @Override
    public int deletePianoStudentByIds(Long[] studentIds)
    {
        return pianoStudentMapper.deletePianoStudentByIds(studentIds);
    }

    @Override
    public int deletePianoStudentById(Long studentId)
    {
        return pianoStudentMapper.deletePianoStudentById(studentId);
    }
}
