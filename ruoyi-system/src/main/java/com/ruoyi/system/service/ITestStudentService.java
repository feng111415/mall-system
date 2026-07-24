package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.TestStudent;

public interface ITestStudentService
{
    public TestStudent selectTestStudentById(Long studentId);

    public List<TestStudent> selectTestStudentList(TestStudent testStudent);

    public int insertTestStudent(TestStudent testStudent);

    public int updateTestStudent(TestStudent testStudent);

    public int deleteTestStudentById(Long studentId);

    public int deleteTestStudentByIds(Long[] studentIds);
}