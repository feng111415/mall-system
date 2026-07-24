package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class TestStudent extends BaseEntity{
    private static final long serialVersionUID = 1L;
    private Long studentId;
    private String studentName;
    private Integer age;
    private String status;
    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    public String getStudentName() {
        return studentName;
    }
    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
  
    
}
