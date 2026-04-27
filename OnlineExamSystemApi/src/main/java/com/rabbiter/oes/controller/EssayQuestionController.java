package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.EssayAnswer;
import com.rabbiter.oes.entity.EssayQuestion;
import com.rabbiter.oes.mapper.EssayAnswerMapper;
import com.rabbiter.oes.service.EssayQuestionService;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 简答题Controller
 */
@RestController
@RequestMapping("/api/essay")
public class EssayQuestionController {
    
    @Autowired
    private EssayQuestionService essayQuestionService;
    
    @Autowired
    private EssayAnswerMapper essayAnswerMapper;

    @Autowired
    private AccessGuard accessGuard;
    
    /**
     * 保存学生主观题答案
     */
    @PostMapping("/answer/save")
    public Object saveAnswer(@RequestBody EssayAnswer essayAnswer, HttpServletRequest request) {
        try {
            if (essayAnswer.getExamCode() == null || essayAnswer.getStudentId() == null || essayAnswer.getQuestionId() == null) {
                return ApiResultHandler.buildApiResult(400, "考试编号、学生编号和题目编号不能为空", null);
            }
            if (!accessGuard.isCurrentUser(request, essayAnswer.getStudentId())) {
                return accessGuard.forbidden("只能提交自己的主观题答案");
            }
            // 设置默认值
            essayAnswer.setGradingStatus("pending");
            essayAnswer.setAiScore(null);
            essayAnswer.setAiComment(null);
            essayAnswer.setTeacherScore(null);
            essayAnswer.setTeacherComment(null);
            essayAnswer.setFinalScore(null);
            essayAnswerMapper.deleteByExamStudentQuestion(essayAnswer.getExamCode(), essayAnswer.getStudentId(), essayAnswer.getQuestionId());
            int result = essayAnswerMapper.insert(essayAnswer);
            if (result > 0) {
                return ApiResultHandler.buildApiResult(200, "保存成功", result);
            }
            return ApiResultHandler.buildApiResult(400, "保存失败", null);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "保存失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 获取所有简答题
     */
    @GetMapping("/all")
    public Object findAll(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看主观题题库");
        }
        List<EssayQuestion> questions = essayQuestionService.findAll();
        return ApiResultHandler.buildApiResult(200, "查询成功", questions);
    }
    
    /**
     * 根据ID获取简答题
     */
    @GetMapping("/{questionId}")
    public Object findById(@PathVariable Integer questionId, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看题目详情");
        }
        EssayQuestion question = essayQuestionService.findById(questionId);
        return ApiResultHandler.buildApiResult(200, "查询成功", question);
    }
    
    /**
     * 根据科目查询简答题
     */
    @GetMapping("/subject/{subject}")
    public Object findBySubject(@PathVariable String subject, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看主观题题库");
        }
        List<EssayQuestion> questions = essayQuestionService.findBySubject(subject);
        return ApiResultHandler.buildApiResult(200, "查询成功", questions);
    }
    
    /**
     * 根据科目和难度查询简答题
     */
    @GetMapping("/subject/{subject}/level/{level}")
    public Object findBySubjectAndLevel(@PathVariable String subject, @PathVariable String level, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看主观题题库");
        }
        List<EssayQuestion> questions = essayQuestionService.findBySubjectAndLevel(subject, level);
        return ApiResultHandler.buildApiResult(200, "查询成功", questions);
    }
    
    /**
     * 添加简答题
     */
    @PostMapping("/add")
    public Object add(@RequestBody EssayQuestion question, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以新增主观题");
        }
        int result = essayQuestionService.add(question);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "添加成功", result);
        }
        return ApiResultHandler.buildApiResult(400, "添加失败", null);
    }
    
    /**
     * 更新简答题
     */
    @PutMapping("/update")
    public Object update(@RequestBody EssayQuestion question, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以修改主观题");
        }
        int result = essayQuestionService.update(question);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "更新成功", result);
        }
        return ApiResultHandler.buildApiResult(400, "更新失败", null);
    }
    
    /**
     * 删除简答题
     */
    @DeleteMapping("/delete/{questionId}")
    public Object delete(@PathVariable Integer questionId, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以删除主观题");
        }
        int result = essayQuestionService.delete(questionId);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "删除成功", result);
        }
        return ApiResultHandler.buildApiResult(400, "删除失败", null);
    }
    
    /**
     * 获取所有科目列表
     */
    @GetMapping("/subjects")
    public Object findAllSubjects(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看科目列表");
        }
        List<String> subjects = essayQuestionService.findAllSubjects();
        return ApiResultHandler.buildApiResult(200, "查询成功", subjects);
    }
    
    /**
     * 获取最新添加的主观题ID
     */
    @GetMapping("/lastId")
    public Object getLastId(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看题目编号");
        }
        EssayQuestion question = essayQuestionService.findLastInserted();
        return ApiResultHandler.buildApiResult(200, "查询成功", question);
    }
}
