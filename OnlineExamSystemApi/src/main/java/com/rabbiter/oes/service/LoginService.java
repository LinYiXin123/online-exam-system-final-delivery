package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.Admin;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.Teacher;

public interface LoginService {

    public Admin adminLogin(Long username, String password);

    public Teacher teacherLogin(Long username, String password);

    public Student studentLogin(Long username, String password);

    // 检查学号是否已存在
    public boolean checkStudentExists(Long studentId);

    // 学生注册
    public int registerStudent(Student student);

    // 获取所有年级
    public java.util.List<String> getAllGrades();

    // 获取所有专业
    public java.util.List<String> getAllMajors();

    // 获取所有班级
    public java.util.List<String> getAllClasses();

    // 获取所有学院
    public java.util.List<String> getAllInstitutes();

    // 验证学生信息用于密码重置
    public boolean verifyStudentForPasswordReset(Long studentId, String email, String tel);

    // 通过邮箱验证学生
    public boolean verifyStudentByEmail(Long studentId, String email);

    // 通过手机号验证学生
    public boolean verifyStudentByTel(Long studentId, String tel);

    // 重置学生密码
    public int resetStudentPassword(Long studentId, String newPwd);
}
