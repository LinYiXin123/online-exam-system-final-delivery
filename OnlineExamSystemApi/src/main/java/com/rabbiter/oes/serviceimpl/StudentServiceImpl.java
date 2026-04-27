package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.service.StudentService;
import com.rabbiter.oes.util.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class StudentServiceImpl implements StudentService {
    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private PasswordService passwordService;


    @Override
    public IPage<Student> findAll(
            Page<Student> page, String name, String grade,
            String tel, String institute, String major, String clazz) {
        name = ("@".equals(name) ? "" : name);
        grade = ("@".equals(grade) ? "" : grade);
        tel = ("@".equals(tel) ? "" : tel);
        institute = ("@".equals(institute) ? "" : institute);
        major = ("@".equals(major) ? "" : major);
        clazz = ("@".equals(clazz) ? "" : clazz);
        return studentMapper.findAll(page, name, grade, tel, institute, major, clazz);
    }

    @Override
    public Student findById(Long studentId) {
        return studentMapper.findById(studentId);
    }

    @Override
    public int deleteById(Integer studentId) {
        return studentMapper.deleteById(studentId);
    }

    @Override
    public int update(Student student) {
        return studentMapper.update(student);
    }

    @Override
    public int updatePwd(Student student) {
        student.setPwd(passwordService.encodeIfNeeded(student.getPwd()));
        return studentMapper.updatePwd(student);
    }

    @Override
    public int add(Student student) {
        student.setPwd(passwordService.encodeIfNeeded(student.getPwd()));
        return studentMapper.add(student);
    }
    
    // 获取学生总数
    public long count() {
        return studentMapper.countAll();
    }
    
    // 检查电话号码是否已存在（用于添加）
    public boolean isTelExists(String tel) {
        return studentMapper.findByTel(tel) != null;
    }
    
    // 检查电话号码是否已被其他学生使用（用于更新）
    public boolean isTelExistsExcludeId(String tel, Long studentId) {
        return studentMapper.findByTelExcludeId(tel, studentId) != null;
    }
    
    // 检查身份证号是否已存在（用于添加）
    public boolean isCardIdExists(String cardId) {
        return studentMapper.findByCardId(cardId) != null;
    }
    
    // 检查身份证号是否已被其他学生使用（用于更新）
    public boolean isCardIdExistsExcludeId(String cardId, Long studentId) {
        return studentMapper.findByCardIdExcludeId(cardId, studentId) != null;
    }
    
    // 检查邮箱是否已存在（用于添加）
    public boolean isEmailExists(String email) {
        return studentMapper.findByEmail(email) != null;
    }
    
    // 检查邮箱是否已被其他学生使用（用于更新）
    public boolean isEmailExistsExcludeId(String email, Long studentId) {
        return studentMapper.findByEmailExcludeId(email, studentId) != null;
    }
}
