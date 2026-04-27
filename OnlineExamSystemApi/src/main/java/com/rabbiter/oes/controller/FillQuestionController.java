package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.FillQuestion;
import com.rabbiter.oes.serviceimpl.FillQuestionServiceImpl;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class FillQuestionController {

    @Autowired
    private FillQuestionServiceImpl fillQuestionService;

    @Autowired
    private AccessGuard accessGuard;

    @PostMapping("/fillQuestion")
    public ApiResult add(@RequestBody FillQuestion fillQuestion, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以新增题目");
        }
        int res = fillQuestionService.add(fillQuestion);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200,"添加成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"添加失败",res);
    }

    @GetMapping("/fillQuestionId")
    public ApiResult findOnlyQuestionId(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看题目编号");
        }
        FillQuestion res = fillQuestionService.findOnlyQuestionId();
        return ApiResultHandler.buildApiResult(200,"查询成功",res);
    }

    @PostMapping("/editFillQuestion")
    public ApiResult edit(@RequestBody FillQuestion fillQuestion, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以修改题目");
        }
        int res = fillQuestionService.edit(fillQuestion);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200,"修改成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"修改失败",res);
    }
}
