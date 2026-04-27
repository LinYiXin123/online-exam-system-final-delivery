package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.Admin;
import com.rabbiter.oes.entity.Message;
import com.rabbiter.oes.entity.Notification;
import com.rabbiter.oes.entity.Replay;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.Teacher;
import com.rabbiter.oes.mapper.AdminMapper;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.mapper.TeacherMapper;
import com.rabbiter.oes.serviceimpl.MessageServiceImpl;
import com.rabbiter.oes.serviceimpl.ReplayServiceImpl;
import com.rabbiter.oes.serviceimpl.NotificationServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReplayController {

    @Autowired
    private ReplayServiceImpl replayService;
    
    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private MessageServiceImpl messageService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private AdminMapper adminMapper;

    @PostMapping("/replay")
    public ApiResult add(@RequestBody Replay replay, HttpServletRequest request) {
        if (replay.getMessageId() == null) {
            return ApiResultHandler.buildApiResult(400, "留言ID不能为空", null);
        }
        if (replay.getReplay() == null || replay.getReplay().trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "回复内容不能为空", null);
        }
        String currentUserId = currentUserId(request);
        if (currentUserId == null) {
            return ApiResultHandler.buildApiResult(401, "未登录", null);
        }
        replay.setReplayerId(currentUserId);
        replay.setReplayerName(resolveCurrentUserName(request));
        replay.setReplayTime(new Date());
        int data = replayService.add(replay);
        if (data != 0) {
            createReplyNotification(replay, currentUserId);
            // 返回包含ID的完整回复对象
            return ApiResultHandler.buildApiResult(200,"添加成功！",replay);
        } else {
            return ApiResultHandler.buildApiResult(400,"添加失败！",null);
        }
    }

    @GetMapping("/replay/{messageId}")
    public ApiResult findAllById(@PathVariable("messageId") Integer messageId) {
        List<Replay> res = replayService.findAllById(messageId);
        return ApiResultHandler.buildApiResult(200,"根据messageId查询",res);
    }

    @GetMapping("/replay/findByMessageId/{messageId}")
    public ApiResult findAllByIdCompat(@PathVariable("messageId") Integer messageId) {
        return findAllById(messageId);
    }
    
    @DeleteMapping("/replay/{replayId}")
    public ApiResult delete(@PathVariable("replayId") Integer replayId, HttpServletRequest request) {
        Replay replay = replayService.findById(replayId);
        if (replay == null) {
            return ApiResultHandler.buildApiResult(404,"回复不存在",null);
        }
        if (!canOperateReplay(request, replay.getReplayerId())) {
            return ApiResultHandler.buildApiResult(403,"只能删除自己的回复",null);
        }
        int data = replayService.delete(replayId);
        if (data != 0) {
            // 同时删除对应的通知
            notificationService.deleteByReplayId(replayId);
            return ApiResultHandler.buildApiResult(200,"删除成功！",null);
        } else {
            return ApiResultHandler.buildApiResult(400,"删除失败！",null);
        }
    }

    @PostMapping("/replay/add")
    public ApiResult addCompat(@RequestBody Replay replay, HttpServletRequest request) {
        return add(replay, request);
    }

    private boolean canOperateReplay(HttpServletRequest request, String ownerId) {
        if (request == null || ownerId == null) {
            return false;
        }
        Object role = request.getAttribute("currentRole");
        Object userId = request.getAttribute("currentUserId");
        return "0".equals(role) || (userId != null && ownerId.equals(String.valueOf(userId)));
    }

    private String currentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("currentUserId");
        return userId != null ? String.valueOf(userId) : null;
    }

    private String resolveCurrentUserName(HttpServletRequest request) {
        Object role = request.getAttribute("currentRole");
        String userId = currentUserId(request);
        if (userId == null) {
            return "未知用户";
        }
        try {
            if ("0".equals(role)) {
                Admin admin = adminMapper.findById(Integer.parseInt(userId));
                return admin != null ? admin.getAdminName() : "未知用户";
            }
            if ("1".equals(role)) {
                Teacher teacher = teacherMapper.findById(Integer.parseInt(userId));
                return teacher != null ? teacher.getTeacherName() : "未知用户";
            }
            Student student = studentMapper.findById(Long.parseLong(userId));
            return student != null ? student.getStudentName() : "未知用户";
        } catch (NumberFormatException e) {
            return "未知用户";
        }
    }

    private void createReplyNotification(Replay replay, String currentUserId) {
        String receiverId = null;
        if (replay.getReplyToId() != null && !replay.getReplyToId().trim().isEmpty()) {
            receiverId = replay.getReplyToId().trim();
        } else {
            Message message = messageService.findById(replay.getMessageId());
            if (message != null) {
                receiverId = message.getStudentId();
            }
        }

        if (receiverId == null || receiverId.equals(currentUserId)) {
            return;
        }

        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setSenderId(currentUserId);
        notification.setSenderName(replay.getReplayerName());
        notification.setType("reply");
        notification.setTitle("回复通知");
        String content = replay.getReplay();
        if (content != null && content.length() > 50) {
            content = content.substring(0, 50) + "...";
        }
        notification.setContent(content);
        notification.setMessageId(replay.getMessageId());
        notification.setReplayId(replay.getReplayId());
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        notificationService.add(notification);
    }
}
