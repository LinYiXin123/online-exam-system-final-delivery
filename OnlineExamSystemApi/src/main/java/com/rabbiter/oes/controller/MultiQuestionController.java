package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.MultiQuestion;
import com.rabbiter.oes.serviceimpl.MultiQuestionServiceImpl;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class MultiQuestionController {

    @Autowired
    private MultiQuestionServiceImpl multiQuestionService;

    @Autowired
    private AccessGuard accessGuard;

    @GetMapping("/multiQuestionId")
    public ApiResult findOnlyQuestion(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看题目编号");
        }
        MultiQuestion res = multiQuestionService.findOnlyQuestionId();
        return ApiResultHandler.buildApiResult(200,"查询成功",res);
    }

    @GetMapping("/MultiQuestion")
    public ApiResult findAll(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看题库");
        }
        // 根据您的实际业务需求,查询所有多选题或分页查询
        // 例如: List<MultiQuestion> res = multiQuestionService.findAll();
        // return ApiResultHandler.buildApiResult(200,"查询成功",res);
        return ApiResultHandler.buildApiResult(200,"请实现查询逻辑",null);
    }

    @PostMapping("/MultiQuestion")
    public ApiResult add(@RequestBody MultiQuestion multiQuestion, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以新增题目");
        }
        int res = multiQuestionService.add(multiQuestion);
        if (res != 0) {

            return ApiResultHandler.buildApiResult(200,"添加成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"添加失败",res);
    }

    @PostMapping("/editMultiQuestion")
    public ApiResult edit(@RequestBody MultiQuestion multiQuestion, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以修改题目");
        }
        int res = multiQuestionService.edit(multiQuestion);
        if (res != 0) {

            return ApiResultHandler.buildApiResult(200,"修改成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"修改失败",res);
    }
}
