package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.Admin;
import com.rabbiter.oes.entity.Notification;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.Teacher;
import com.rabbiter.oes.mapper.AdminMapper;
import com.rabbiter.oes.mapper.NotificationMapper;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.mapper.TeacherMapper;
import com.rabbiter.oes.service.NotificationService;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AccessGuard accessGuard;

    // 获取用户的所有通知
    @GetMapping("/{receiverId}")
    public Object getNotifications(@PathVariable String receiverId, HttpServletRequest request) {
        if (!canAccessReceiver(request, receiverId)) {
            return ApiResultHandler.buildApiResult(403, "只能查看自己的通知", null);
        }
        List<Notification> notifications = notificationService.findByReceiverId(receiverId);
        return ApiResultHandler.buildApiResult(200, "查询成功", notifications);
    }

    // 获取未读通知数量
    @GetMapping("/unread/{receiverId}")
    public Object getUnreadCount(@PathVariable String receiverId, HttpServletRequest request) {
        if (!canAccessReceiver(request, receiverId)) {
            return ApiResultHandler.buildApiResult(403, "只能查看自己的通知", null);
        }
        int count = notificationService.countUnread(receiverId);
        Map<String, Integer> result = new HashMap<>();
        result.put("count", count);
        return ApiResultHandler.buildApiResult(200, "查询成功", result);
    }

    // 标记单条通知为已读
    @PutMapping("/read/{id}")
    public Object markAsRead(@PathVariable Integer id, HttpServletRequest request) {
        Notification notification = notificationMapper.findById(id);
        if (notification == null) {
            return ApiResultHandler.buildApiResult(404, "通知不存在", null);
        }
        if (!canAccessReceiver(request, notification.getReceiverId())) {
            return ApiResultHandler.buildApiResult(403, "只能修改自己的通知", null);
        }
        int result = notificationService.markAsRead(id);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "标记成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "标记失败", null);
    }

    // 标记所有通知为已读
    @PutMapping("/readAll/{receiverId}")
    public Object markAllAsRead(@PathVariable String receiverId, HttpServletRequest request) {
        if (!canAccessReceiver(request, receiverId)) {
            return ApiResultHandler.buildApiResult(403, "只能修改自己的通知", null);
        }
        int result = notificationService.markAllAsRead(receiverId);
        return ApiResultHandler.buildApiResult(200, "全部标记为已读", result);
    }

    // 添加通知
    @PostMapping
    public Object addNotification(@RequestBody Notification notification, HttpServletRequest request) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("不支持直接创建通知");
        }
        if (notification.getReceiverId() == null || notification.getReceiverId().trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "接收者不能为空", null);
        }
        // 在服务端设置创建时间，避免前端日期格式问题
        notification.setCreateTime(new java.util.Date());
        if (notification.getIsRead() == null) {
            notification.setIsRead(0);
        }
        String senderId = currentUserId(request);
        notification.setSenderId(senderId);
        notification.setSenderName(resolveCurrentUserName(request));
        int result = notificationService.add(notification);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "添加成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "添加失败", null);
    }

    // 删除通知
    @DeleteMapping("/{id}")
    public Object deleteNotification(@PathVariable Integer id, HttpServletRequest request) {
        Notification notification = notificationMapper.findById(id);
        if (notification == null) {
            return ApiResultHandler.buildApiResult(404, "通知不存在", null);
        }
        if (!canAccessReceiver(request, notification.getReceiverId())) {
            return ApiResultHandler.buildApiResult(403, "只能删除自己的通知", null);
        }
        int result = notificationService.delete(id);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "删除成功", null);
        }
        return ApiResultHandler.buildApiResult(400, "删除失败", null);
    }

    private boolean canAccessReceiver(HttpServletRequest request, String receiverId) {
        if (request == null || receiverId == null) {
            return false;
        }
        Object currentRole = request.getAttribute("currentRole");
        Object currentUserId = request.getAttribute("currentUserId");
        return "0".equals(currentRole) || (currentUserId != null && receiverId.equals(String.valueOf(currentUserId)));
    }

    private String currentUserId(HttpServletRequest request) {
        Object currentUserId = request.getAttribute("currentUserId");
        return currentUserId != null ? String.valueOf(currentUserId) : null;
    }

    private String resolveCurrentUserName(HttpServletRequest request) {
        Object currentRole = request.getAttribute("currentRole");
        String userId = currentUserId(request);
        if (userId == null) {
            return "未知用户";
        }
        try {
            if ("0".equals(currentRole)) {
                Admin admin = adminMapper.findById(Integer.parseInt(userId));
                return admin != null ? admin.getAdminName() : "未知用户";
            }
            if ("1".equals(currentRole)) {
                Teacher teacher = teacherMapper.findById(Integer.parseInt(userId));
                return teacher != null ? teacher.getTeacherName() : "未知用户";
            }
            Student student = studentMapper.findById(Long.parseLong(userId));
            return student != null ? student.getStudentName() : "未知用户";
        } catch (NumberFormatException e) {
            return "未知用户";
        }
    }
}
