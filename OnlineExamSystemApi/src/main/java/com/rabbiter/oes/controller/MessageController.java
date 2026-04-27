package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Admin;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.Message;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.Teacher;
import com.rabbiter.oes.mapper.AdminMapper;
import com.rabbiter.oes.mapper.StudentMapper;
import com.rabbiter.oes.mapper.TeacherMapper;
import com.rabbiter.oes.serviceimpl.MessageServiceImpl;
import com.rabbiter.oes.serviceimpl.NotificationServiceImpl;
import com.rabbiter.oes.serviceimpl.ReplayServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageServiceImpl messageService;

    @Autowired
    private ReplayServiceImpl replayService;

    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TeacherMapper teacherMapper;

    @Autowired
    private AdminMapper adminMapper;

    @GetMapping("/messages/{page}/{size}")
    public ApiResult<Message> findAll(@PathVariable("page") Integer page, @PathVariable("size") Integer size) {
        Page<Message> messagePage = new Page<>(page,size);
        IPage<Message> all = messageService.findAll(messagePage);
        return ApiResultHandler.buildApiResult(200,"查询所有留言",all);
    }

    @GetMapping("/message/findAll")
    public ApiResult<Message> findAllCompat() {
        Page<Message> messagePage = new Page<>(1, 1000);
        IPage<Message> all = messageService.findAll(messagePage);
        return ApiResultHandler.buildApiResult(200, "查询所有留言", all.getRecords());
    }

    @GetMapping("/message/{id}")
    public ApiResult findById(@PathVariable("id") Integer id) {
        Message res = messageService.findById(id);
        return ApiResultHandler.buildApiResult(200,"根据Id查询",res);
    }

    @DeleteMapping("/message/{id}")
    public ApiResult delete(@PathVariable("id") Integer id, HttpServletRequest request) {
        Message message = messageService.findById(id);
        if (message == null) {
            return ApiResultHandler.buildApiResult(404, "留言不存在", null);
        }
        if (!canOperateMessage(request, message.getStudentId())) {
            return ApiResultHandler.buildApiResult(403, "只能删除自己的留言", null);
        }
        notificationService.deleteByMessageId(id);
        replayService.deleteByMessageId(id);
        int res = messageService.delete(id);
        if (res > 0) {
            return ApiResultHandler.buildApiResult(200, "删除成功", res);
        }
        return ApiResultHandler.buildApiResult(400, "删除失败", res);
    }

    @DeleteMapping("/message/delete/{id}")
    public ApiResult deleteCompat(@PathVariable("id") Integer id, HttpServletRequest request) {
        return delete(id, request);
    }

    @PostMapping("/message")
    public ApiResult add(@RequestBody Message message, HttpServletRequest request) {
        String currentUserId = currentUserId(request);
        if (currentUserId == null) {
            return ApiResultHandler.buildApiResult(401, "未登录", null);
        }
        if (message.getTitle() == null || message.getTitle().trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "留言标题不能为空", null);
        }
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return ApiResultHandler.buildApiResult(400, "留言内容不能为空", null);
        }
        message.setStudentId(currentUserId);
        message.setPublisherName(resolveCurrentUserName(request));
        message.setPublisherRole(resolveCurrentUserRoleText(request));
        message.setTime(new Date());
        int res = messageService.add(message);
        if (res == 0) {
            return ApiResultHandler.buildApiResult(400,"添加失败",res);
        } else {
            return ApiResultHandler.buildApiResult(200,"添加成功",message);
        }
    }

    @PostMapping("/message/add")
    public ApiResult addCompat(@RequestBody Message message, HttpServletRequest request) {
        return add(message, request);
    }

    private boolean canOperateMessage(HttpServletRequest request, String ownerId) {
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

    private String resolveCurrentUserRoleText(HttpServletRequest request) {
        Object role = request.getAttribute("currentRole");
        if ("0".equals(role)) {
            return "管理员";
        }
        if ("1".equals(role)) {
            return "教师";
        }
        return "学生";
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
}
