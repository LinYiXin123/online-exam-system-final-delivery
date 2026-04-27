package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.serviceimpl.LoginServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@org.springframework.web.bind.annotation.RequestMapping("/api")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private LoginServiceImpl loginService;

    @PostMapping("/login")
    public ApiResult login(@RequestBody Login login, HttpServletRequest request, HttpServletResponse response) {

        Long username = login.getUsername();
        String password = login.getPassword();
        logger.info("登录请求 - 用户名: {}", username);
        
        Admin adminRes = loginService.adminLogin(username, password);
        logger.info("管理员登录查询结果: {}", adminRes);
        if (adminRes != null) {
            Cookie token = new Cookie("rb_token", adminRes.getCardId());
            token.setPath("/");
            Cookie role = new Cookie("rb_role", "0");
            role.setPath("/");
            Cookie userId = new Cookie("rb_id", String.valueOf(adminRes.getAdminId()));
            userId.setPath("/");

            //将cookie对象加入response响应
            response.addCookie(token);
            response.addCookie(role);
            response.addCookie(userId);

            return ApiResultHandler.buildApiResult(200, "请求成功", adminRes);
        }

        Teacher teacherRes = loginService.teacherLogin(username,password);
        logger.info("教师登录查询结果: {}", teacherRes);
        if (teacherRes != null) {
            Cookie token = new Cookie("rb_token", teacherRes.getCardId());
            token.setPath("/");
            Cookie role = new Cookie("rb_role", "1");
            role.setPath("/");
            Cookie userId = new Cookie("rb_id", String.valueOf(teacherRes.getTeacherId()));
            userId.setPath("/");
            response.addCookie(token);
            response.addCookie(role);
            response.addCookie(userId);
            return ApiResultHandler.buildApiResult(200, "请求成功", teacherRes);
        }

        Student studentRes = loginService.studentLogin(username,password);
        logger.info("学生登录查询结果: {}", studentRes);
        if (studentRes != null) {
            Cookie token = new Cookie("rb_token", studentRes.getCardId());
            token.setPath("/");
            Cookie role = new Cookie("rb_role", "2");
            role.setPath("/");
            Cookie userId = new Cookie("rb_id", String.valueOf(studentRes.getStudentId()));
            userId.setPath("/");
            response.addCookie(token);
            response.addCookie(role);
            response.addCookie(userId);
            return ApiResultHandler.buildApiResult(200, "请求成功", studentRes);
        }

        logger.warn("登录失败 - 用户名: {}", username);
        return ApiResultHandler.buildApiResult(400, "请求失败", null);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie token = new Cookie("rb_token", null);
        token.setPath("/");
        token.setMaxAge(0);
        Cookie role = new Cookie("rb_role", null);
        role.setPath("/");
        role.setMaxAge(0);
        Cookie userId = new Cookie("rb_id", null);
        userId.setPath("/");
        userId.setMaxAge(0);
        response.addCookie(token);
        response.addCookie(role);
        response.addCookie(userId);
    }

    // 获取所有年级选项
    @GetMapping("/options/grades")
    public ApiResult getGrades() {
        return ApiResultHandler.buildApiResult(200, "请求成功", loginService.getAllGrades());
    }

    // 获取所有专业选项
    @GetMapping("/options/majors")
    public ApiResult getMajors() {
        return ApiResultHandler.buildApiResult(200, "请求成功", loginService.getAllMajors());
    }

    // 获取所有班级选项
    @GetMapping("/options/classes")
    public ApiResult getClasses() {
        return ApiResultHandler.buildApiResult(200, "请求成功", loginService.getAllClasses());
    }

    // 获取所有学院选项
    @GetMapping("/options/institutes")
    public ApiResult getInstitutes() {
        return ApiResultHandler.buildApiResult(200, "请求成功", loginService.getAllInstitutes());
    }

    @PostMapping("/register")
    public ApiResult register(@RequestBody Student student) {
        logger.info("注册请求 - 学号: {}, 姓名: {}", student.getStudentId(), student.getStudentName());
        
        // 参数校验
        if (student.getStudentId() == null) {
            return ApiResultHandler.buildApiResult(400, "学号不能为空", null);
        }
        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "姓名不能为空", null);
        }
        if (student.getPwd() == null || student.getPwd().trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "密码不能为空", null);
        }
        if (student.getPwd().length() < 6) {
            return ApiResultHandler.buildApiResult(400, "密码长度不能少于6位", null);
        }
        // 手机号格式验证（选填，但填了就要验证）
        if (student.getTel() != null && !student.getTel().isEmpty()) {
            if (!student.getTel().matches("^1[3-9]\\d{9}$")) {
                return ApiResultHandler.buildApiResult(400, "手机号格式不正确，应为11位数字", null);
            }
        }
        // 邮箱格式验证（选填，但填了就要验证）
        if (student.getEmail() != null && !student.getEmail().isEmpty()) {
            if (!student.getEmail().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return ApiResultHandler.buildApiResult(400, "邮箱格式不正确", null);
            }
        }
        
        // 检查学号是否已存在
        if (loginService.checkStudentExists(student.getStudentId())) {
            logger.warn("注册失败 - 学号已存在: {}", student.getStudentId());
            return ApiResultHandler.buildApiResult(400, "该学号已被注册", null);
        }
        
        // 执行注册
        try {
            int result = loginService.registerStudent(student);
            if (result > 0) {
                logger.info("注册成功 - 学号: {}", student.getStudentId());
                return ApiResultHandler.buildApiResult(200, "注册成功", null);
            } else {
                logger.error("注册失败 - 学号: {}", student.getStudentId());
                return ApiResultHandler.buildApiResult(500, "注册失败，请稍后重试", null);
            }
        } catch (Exception e) {
            logger.error("注册异常 - 学号: {}, 错误: {}", student.getStudentId(), e.getMessage());
            return ApiResultHandler.buildApiResult(500, "注册失败：" + e.getMessage(), null);
        }
    }

    @PostMapping("/forgot-password")
    public ApiResult forgotPassword(@RequestBody java.util.Map<String, Object> params) {
        Long studentId = null;
        if (params.get("studentId") != null) {
            if (params.get("studentId") instanceof Number) {
                studentId = ((Number) params.get("studentId")).longValue();
            } else {
                try {
                    studentId = Long.parseLong(params.get("studentId").toString());
                } catch (NumberFormatException e) {
                    return ApiResultHandler.buildApiResult(400, "学号格式不正确", null);
                }
            }
        }
        String email = (String) params.get("email");
        String tel = (String) params.get("tel");
        String newPwd = (String) params.get("newPwd");

        logger.info("找回密码请求 - 学号: {}, 邮箱: {}, 手机号: {}", studentId, email, tel);

        // 参数校验
        if (studentId == null) {
            return ApiResultHandler.buildApiResult(400, "学号不能为空", null);
        }
        if (newPwd == null || newPwd.trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "新密码不能为空", null);
        }
        if (newPwd.length() < 6) {
            return ApiResultHandler.buildApiResult(400, "密码长度不能少于6位", null);
        }
        if ((email == null || email.trim().isEmpty()) && (tel == null || tel.trim().isEmpty())) {
            return ApiResultHandler.buildApiResult(400, "请至少填写邮箱或手机号", null);
        }

        // 验证学生信息
        try {
            boolean verified = loginService.verifyStudentForPasswordReset(studentId, email, tel);
            if (!verified) {
                logger.warn("找回密码失败 - 学号: {}, 信息验证失败", studentId);
                return ApiResultHandler.buildApiResult(400, "学号与邮箱/手机号不匹配，请检查信息", null);
            }

            // 更新密码
            int result = loginService.resetStudentPassword(studentId, newPwd);
            if (result > 0) {
                logger.info("密码重置成功 - 学号: {}", studentId);
                return ApiResultHandler.buildApiResult(200, "密码重置成功", null);
            } else {
                logger.error("密码重置失败 - 学号: {}", studentId);
                return ApiResultHandler.buildApiResult(500, "密码重置失败，请稍后重试", null);
            }
        } catch (Exception e) {
            logger.error("找回密码异常 - 学号: {}, 错误: {}", studentId, e.getMessage());
            return ApiResultHandler.buildApiResult(500, "密码重置失败：" + e.getMessage(), null);
        }
    }
}
