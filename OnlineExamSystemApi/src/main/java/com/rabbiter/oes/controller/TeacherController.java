package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.Teacher;
import com.rabbiter.oes.serviceimpl.TeacherServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class TeacherController {

    private TeacherServiceImpl teacherService;
    @Autowired
    public TeacherController(TeacherServiceImpl teacherService){
        this.teacherService = teacherService;
    }

    @GetMapping("/teachers/{page}/{size}")
    public ApiResult findAll(@PathVariable Integer page, @PathVariable Integer size, HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以查看教师列表");
        }
        Page<Teacher> teacherPage = new Page<>(page,size);
        IPage<Teacher> teacherIPage = teacherService.findAll(teacherPage);

        return ApiResultHandler.buildApiResult(200,"查询所有教师",teacherIPage);
    }

    @GetMapping("/teacher/{teacherId}")
    public ApiResult findById(@PathVariable("teacherId") Integer teacherId, HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以查看教师信息");
        }
        return ApiResultHandler.success(teacherService.findById(teacherId));
    }

    @DeleteMapping("/teacher/{teacherId}")
    public ApiResult deleteById(@PathVariable("teacherId") Integer teacherId, HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以删除教师");
        }
        return ApiResultHandler.success(teacherService.deleteById(teacherId));
    }

    @PutMapping("/teacher")
    public ApiResult update(@RequestBody Teacher teacher, HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以修改教师信息");
        }
        return ApiResultHandler.success(teacherService.update(teacher));
    }

    @PostMapping("/teacher")
    public ApiResult add(@RequestBody Teacher teacher, HttpServletRequest request){
        if (!isAdmin(request)) {
            return forbidden("只有管理员可以新增教师");
        }
        return ApiResultHandler.success(teacherService.add(teacher));
    }

    private boolean isAdmin(HttpServletRequest request) {
        return request != null && "0".equals(request.getAttribute("currentRole"));
    }

    private ApiResult forbidden(String message) {
        return ApiResultHandler.buildApiResult(403, message, null);
    }
}
