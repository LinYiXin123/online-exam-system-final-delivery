package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.Admin;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.Teacher;
import com.rabbiter.oes.mapper.AdminMapper;
import com.rabbiter.oes.mapper.LoginMapper;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.mapper.TeacherMapper;
import com.rabbiter.oes.service.LoginService;
import com.rabbiter.oes.util.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginMapper loginMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private PasswordService passwordService;

    @Override
    public Admin adminLogin(Long username, String password) {
        Admin admin = adminMapper.findById(username != null ? username.intValue() : null);
        if (admin == null || !passwordService.matches(password, admin.getPwd())) {
            return null;
        }
        upgradeLegacyPasswordIfNeeded(admin.getPwd(), password, encoded -> {
            admin.setPwd(encoded);
            adminMapper.update(admin);
        });
        admin.setPwd(null);
        return admin;
    }

    @Override
    public Teacher teacherLogin(Long username, String password) {
        Teacher teacher = teacherMapper.findById(username != null ? username.intValue() : null);
        if (teacher == null || !passwordService.matches(password, teacher.getPwd())) {
            return null;
        }
        upgradeLegacyPasswordIfNeeded(teacher.getPwd(), password, encoded -> {
            teacher.setPwd(encoded);
            teacherMapper.update(teacher);
        });
        teacher.setPwd(null);
        return teacher;
    }

    @Override
    public Student studentLogin(Long username, String password) {
        Student student = studentMapper.findById(username);
        if (student == null || !passwordService.matches(password, student.getPwd())) {
            return null;
        }
        upgradeLegacyPasswordIfNeeded(student.getPwd(), password, encoded -> {
            student.setPwd(encoded);
            studentMapper.updatePwd(student);
        });
        student.setPwd(null);
        return student;
    }

    @Override
    public boolean checkStudentExists(Long studentId) {
        return loginMapper.checkStudentExists(studentId) > 0;
    }

    @Override
    public int registerStudent(Student student) {
        student.setPwd(passwordService.encodeIfNeeded(student.getPwd()));
        return loginMapper.registerStudent(student);
    }

    @Override
    public java.util.List<String> getAllGrades() {
        return loginMapper.getAllGrades();
    }

    @Override
    public java.util.List<String> getAllMajors() {
        return loginMapper.getAllMajors();
    }

    @Override
    public java.util.List<String> getAllClasses() {
        return loginMapper.getAllClasses();
    }

    @Override
    public java.util.List<String> getAllInstitutes() {
        return loginMapper.getAllInstitutes();
    }

    @Override
    public boolean verifyStudentForPasswordReset(Long studentId, String email, String tel) {
        // 如果提供了邮箱，验证邮箱
        if (email != null && !email.trim().isEmpty()) {
            if (loginMapper.verifyStudentByEmail(studentId, email) > 0) {
                return true;
            }
        }
        // 如果提供了手机号，验证手机号
        if (tel != null && !tel.trim().isEmpty()) {
            if (loginMapper.verifyStudentByTel(studentId, tel) > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean verifyStudentByEmail(Long studentId, String email) {
        return loginMapper.verifyStudentByEmail(studentId, email) > 0;
    }

    @Override
    public boolean verifyStudentByTel(Long studentId, String tel) {
        return loginMapper.verifyStudentByTel(studentId, tel) > 0;
    }

    @Override
    public int resetStudentPassword(Long studentId, String newPwd) {
        return loginMapper.resetStudentPassword(studentId, passwordService.encode(newPwd));
    }

    private void upgradeLegacyPasswordIfNeeded(String storedPassword, String rawPassword, java.util.function.Consumer<String> upgrader) {
        if (!passwordService.isEncoded(storedPassword) && storedPassword != null && storedPassword.equals(rawPassword)) {
            upgrader.accept(passwordService.encode(rawPassword));
        }
    }
}
