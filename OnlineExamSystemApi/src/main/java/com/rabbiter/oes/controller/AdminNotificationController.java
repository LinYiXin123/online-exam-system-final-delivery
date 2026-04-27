package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 管理员通知管理控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminNotificationController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccessGuard accessGuard;

    /**
     * 获取所有通知历史
     */
    @GetMapping("/notifications")
    public ApiResult getNotifications(HttpServletRequest request) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以查看通知历史");
        }
        String sql = "SELECT DISTINCT n.id, n.type, n.title, n.content, n.targetDesc, n.createTime " +
                "FROM admin_notification n ORDER BY n.createTime DESC LIMIT 100";
        try {
            List<Map<String, Object>> notifications = jdbcTemplate.queryForList(sql);
            return ApiResultHandler.buildApiResult(200, "success", notifications);
        } catch (Exception e) {
            // 表可能不存在，返回空列表
            return ApiResultHandler.buildApiResult(200, "success", new ArrayList<>());
        }
    }

    /**
     * 发送通知
     */
    @PostMapping("/notification/send")
    public ApiResult sendNotification(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以发送系统通知");
        }
        String type = payload.get("type");
        String target = payload.get("target");
        String institute = payload.get("institute");
        String title = payload.get("title");
        String content = payload.get("content");
        
        // 确保通知表存在
        ensureNotificationTableExists();
        
        // 根据目标获取接收者列表
        List<String> receivers = getReceivers(target, institute);
        String targetDesc = getTargetDesc(target, institute);
        
        // 记录通知到管理员通知表
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String insertAdminNotification = "INSERT INTO admin_notification (type, title, content, targetDesc, createTime) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertAdminNotification, type, title, content, targetDesc, now);
        
        // 发送通知到每个接收者
        int sentCount = 0;
        String insertNotification = "INSERT INTO notification (receiverId, senderId, type, title, content, isRead, createTime) VALUES (?, ?, ?, ?, ?, 0, ?)";
        for (String receiverId : receivers) {
            try {
                jdbcTemplate.update(insertNotification, receiverId, "system", type, title, content, now);
                sentCount++;
            } catch (Exception e) {
                // 忽略单个发送失败
            }
        }
        
        return ApiResultHandler.buildApiResult(200, "发送成功", sentCount);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/notification/{id}")
    public ApiResult deleteNotification(@PathVariable Integer id, HttpServletRequest request) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以删除通知");
        }
        try {
            jdbcTemplate.update("DELETE FROM admin_notification WHERE id = ?", id);
            return ApiResultHandler.buildApiResult(200, "删除成功", null);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "删除失败", null);
        }
    }

    private void ensureNotificationTableExists() {
        try {
            String createTable = "CREATE TABLE IF NOT EXISTS admin_notification (" +
                    "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "type VARCHAR(20), " +
                    "title VARCHAR(100), " +
                    "content TEXT, " +
                    "targetDesc VARCHAR(50), " +
                    "createTime VARCHAR(20)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
            jdbcTemplate.execute(createTable);
        } catch (Exception e) {
            // 表已存在或其他错误
        }
    }

    private List<String> getReceivers(String target, String institute) {
        String sql;
        switch (target) {
            case "all_students":
                sql = "SELECT CAST(studentId AS CHAR) FROM student";
                break;
            case "all_teachers":
                sql = "SELECT CAST(teacherId AS CHAR) FROM teacher";
                break;
            case "all":
                // 合并学生和教师
                List<String> students = jdbcTemplate.queryForList("SELECT CAST(studentId AS CHAR) FROM student", String.class);
                List<String> teachers = jdbcTemplate.queryForList("SELECT CAST(teacherId AS CHAR) FROM teacher", String.class);
                students.addAll(teachers);
                return students;
            case "institute":
                sql = "SELECT CAST(studentId AS CHAR) FROM student WHERE institute = ?";
                return jdbcTemplate.queryForList(sql, String.class, institute);
            default:
                return new ArrayList<>();
        }
        return jdbcTemplate.queryForList(sql, String.class);
    }

    private String getTargetDesc(String target, String institute) {
        switch (target) {
            case "all_students":
                return "全体学生";
            case "all_teachers":
                return "全体教师";
            case "all":
                return "全体用户";
            case "institute":
                return institute;
            default:
                return target;
        }
    }
}
