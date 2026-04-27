package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.Admin;
import com.rabbiter.oes.entity.Teacher;
import com.rabbiter.oes.mapper.AdminMapper;
import com.rabbiter.oes.mapper.TeacherMapper;
import com.rabbiter.oes.service.AdminService;
import com.rabbiter.oes.util.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private TeacherMapper teacherMapper;
    @Autowired
    private PasswordService passwordService;

    @Override
    public List<Admin> findAll() {
        return adminMapper.findAll();
    }

    @Override
    public Admin findById(Integer adminId) {
        return adminMapper.findById(adminId);
    }

    @Override
    public int deleteById(int adminId) {
        return adminMapper.deleteById(adminId);
    }

    @Override
    public int update(Admin admin) {
        Admin existing = adminMapper.findById(admin.getAdminId());
        if (existing == null) {
            return 0;
        }
        if (admin.getPwd() == null || admin.getPwd().trim().isEmpty()) {
            admin.setPwd(existing.getPwd());
        } else {
            admin.setPwd(passwordService.encodeIfNeeded(admin.getPwd()));
        }
        return adminMapper.update(admin);
    }

    @Override
    public int add(Admin admin) {
        admin.setPwd(passwordService.encodeIfNeeded(admin.getPwd()));
        return adminMapper.add(admin);
    }

    @Override
    public Object resetPsw(Integer adminId, String newPsw, String oldPsw) {
        Admin admin = findById(adminId);
        if(admin != null && passwordService.matches(oldPsw, admin.getPwd())) {
            admin.setPwd(passwordService.encode(newPsw));
            update(admin);
            return true;

        }else if(admin == null){
            Teacher teacher = teacherMapper.findById(adminId);
            if(teacher != null && passwordService.matches(oldPsw, teacher.getPwd())) {
                teacher.setPwd(passwordService.encode(newPsw));
                teacherMapper.update(teacher);
                return true;

            }
        }
        return "原密码错误";
    }


}
