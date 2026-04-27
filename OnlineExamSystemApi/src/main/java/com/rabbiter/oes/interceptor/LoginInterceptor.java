package com.rabbiter.oes.interceptor;

import com.rabbiter.oes.entity.Admin;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.Teacher;
import com.rabbiter.oes.mapper.AdminMapper;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.mapper.TeacherMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录检查
 * 1.配置到拦截器要拦截哪些请求
 * 2.把这些配置放在容器中
 *
 * 实现HandlerInterceptor接口
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final AdminMapper adminMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;

    public LoginInterceptor(AdminMapper adminMapper, TeacherMapper teacherMapper, StudentMapper studentMapper) {
        this.adminMapper = adminMapper;
        this.teacherMapper = teacherMapper;
        this.studentMapper = studentMapper;
    }

    /**
     * 目标方法执行之前
     * 登录检查写在这里，如果没有登录，就不执行目标方法
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String token = null;
        String role = null;
        String userId = null;
        for (Cookie cookie : cookies) {
            if ("rb_token".equals(cookie.getName())) {
                token = cookie.getValue();
            } else if ("rb_role".equals(cookie.getName())) {
                role = cookie.getValue();
            } else if ("rb_id".equals(cookie.getName())) {
                userId = cookie.getValue();
            }
        }

        if (isBlank(token) || isBlank(role) || isBlank(userId)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        try {
            if (!isValidUser(role, userId, token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            request.setAttribute("currentRole", role);
            request.setAttribute("currentUserId", userId);
            return true;
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    private boolean isValidUser(String role, String userId, String token) {
        switch (role) {
            case "0":
                Admin admin = adminMapper.findById(Integer.parseInt(userId));
                return admin != null && token.equals(admin.getCardId());
            case "1":
                Teacher teacher = teacherMapper.findById(Integer.parseInt(userId));
                return teacher != null && token.equals(teacher.getCardId());
            case "2":
                Student student = studentMapper.findById(Long.parseLong(userId));
                return student != null && token.equals(student.getCardId());
            default:
                return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
