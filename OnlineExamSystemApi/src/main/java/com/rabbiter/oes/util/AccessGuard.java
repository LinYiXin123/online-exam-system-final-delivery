package com.rabbiter.oes.util;

import com.rabbiter.oes.entity.ApiResult;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
public class AccessGuard {

    public String currentRole(HttpServletRequest request) {
        Object role = request != null ? request.getAttribute("currentRole") : null;
        return role != null ? String.valueOf(role) : null;
    }

    public String currentUserId(HttpServletRequest request) {
        Object userId = request != null ? request.getAttribute("currentUserId") : null;
        return userId != null ? String.valueOf(userId) : null;
    }

    public boolean isAdmin(HttpServletRequest request) {
        return "0".equals(currentRole(request));
    }

    public boolean isTeacher(HttpServletRequest request) {
        return "1".equals(currentRole(request));
    }

    public boolean isStudent(HttpServletRequest request) {
        return "2".equals(currentRole(request));
    }

    public boolean isTeacherOrAdmin(HttpServletRequest request) {
        return isAdmin(request) || isTeacher(request);
    }

    public boolean isCurrentUser(HttpServletRequest request, Object userId) {
        String currentUserId = currentUserId(request);
        return currentUserId != null && userId != null && currentUserId.equals(String.valueOf(userId));
    }

    public boolean canAccessStudent(HttpServletRequest request, Object studentId) {
        return isTeacherOrAdmin(request) || (isStudent(request) && isCurrentUser(request, studentId));
    }

    public ApiResult forbidden(String message) {
        return ApiResultHandler.buildApiResult(403, message, null);
    }
}
