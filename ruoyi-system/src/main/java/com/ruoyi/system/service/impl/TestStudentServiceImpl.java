package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.TestStudent;
import com.ruoyi.system.mapper.TestStudentMapper;
import com.ruoyi.system.service.ITestStudentService;

@Service
public class TestStudentServiceImpl implements ITestStudentService
{
    @Autowired
    private TestStudentMapper testStudentMapper;

    @Override
    public TestStudent selectTestStudentById(Long studentId)
    {
        return testStudentMapper.selectTestStudentById(studentId);
    }

    @Override
    public List<TestStudent> selectTestStudentList(TestStudent testStudent)
    {
        return testStudentMapper.selectTestStudentList(testStudent);
    }

    @Override
    public int insertTestStudent(TestStudent testStudent)
    {
        testStudent.setCreateTime(DateUtils.getNowDate());
        return testStudentMapper.insertTestStudent(testStudent);
    }

    @Override
    public int updateTestStudent(TestStudent testStudent)
    {
        testStudent.setUpdateTime(DateUtils.getNowDate());
        return testStudentMapper.updateTestStudent(testStudent);
    }

    @Override
    public int deleteTestStudentById(Long studentId)
    {
        return testStudentMapper.deleteTestStudentById(studentId);
    }

    @Override
    public int deleteTestStudentByIds(Long[] studentIds)
    {
        return testStudentMapper.deleteTestStudentByIds(studentIds);
    }
}