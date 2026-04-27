package com.rabbiter.oes.controller;

import com.rabbiter.oes.service.AiGradingService;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * AI智能改卷Controller
 */
@RestController
@RequestMapping("/api/ai-grading")
public class AiGradingController {
    
    @Autowired
    private AiGradingService aiGradingService;

    @Autowired
    private AccessGuard accessGuard;
    
    /**
     * 获取参加考试的学生成绩列表（含客观题、主观题、总分）
     */
    @GetMapping("/students/{examCode}")
    public Object getStudentScores(@PathVariable Integer examCode, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看阅卷列表");
        }
        List<Map<String, Object>> students = aiGradingService.getStudentScoresByExam(examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", students);
    }
    
    /**
     * 获取学生的客观题答题详情
     */
    @GetMapping("/objective-answers/{examCode}/{studentId}")
    public Object getObjectiveAnswers(@PathVariable Integer examCode, @PathVariable Long studentId, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看答题详情");
        }
        List<Map<String, Object>> answers = aiGradingService.getObjectiveAnswers(examCode, studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", answers);
    }
    
    /**
     * 获取学生的主观题答题详情
     */
    @GetMapping("/subjective-answers/{examCode}/{studentId}")
    public Object getSubjectiveAnswers(@PathVariable Integer examCode, @PathVariable Long studentId, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看答题详情");
        }
        List<Map<String, Object>> answers = aiGradingService.getSubjectiveAnswers(examCode, studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", answers);
    }
    
    /**
     * 一键AI改卷（客观题自动判分 + 主观题AI评分）
     */
    @PostMapping("/auto-grade-all/{examCode}")
    public Object autoGradeAll(@PathVariable Integer examCode, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以执行智能阅卷");
        }
        try {
            Map<String, Object> result = aiGradingService.autoGradeAll(examCode);
            return ApiResultHandler.buildApiResult(200, "改卷完成", result);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "改卷失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * AI评分单个学生的主观题
     */
    @PostMapping("/grade-student-essay/{examCode}/{studentId}")
    public Object gradeStudentEssay(@PathVariable Integer examCode, @PathVariable Long studentId, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以执行主观题评分");
        }
        try {
            int count = aiGradingService.gradeStudentEssay(examCode, studentId);
            return ApiResultHandler.buildApiResult(200, "AI评分完成", count);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "AI评分失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 计算并更新学生总分（接收前端传递的动态计算分数）
     */
    @PostMapping("/calculate-total/{examCode}/{studentId}")
    public Object calculateAndUpdateTotal(@PathVariable Integer examCode, @PathVariable Long studentId,
                                          @RequestBody(required = false) Map<String, Object> scores,
                                          HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以计算总分");
        }
        try {
            Integer objectiveScore = null;
            Integer subjectiveScore = null;
            if (scores != null) {
                objectiveScore = scores.get("objectiveScore") != null ? ((Number) scores.get("objectiveScore")).intValue() : null;
                subjectiveScore = scores.get("subjectiveScore") != null ? ((Number) scores.get("subjectiveScore")).intValue() : null;
            }
            Map<String, Object> result = aiGradingService.calculateAndUpdateTotalScore(examCode, studentId, objectiveScore, subjectiveScore);
            return ApiResultHandler.buildApiResult(200, "总分计算成功", result);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "总分计算失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 获取考试的所有主观题答案（教师端查看）
     */
    @GetMapping("/answers/{examCode}")
    public Object getAnswersByExam(@PathVariable Integer examCode, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看主观题答案");
        }
        List<Map<String, Object>> answers = aiGradingService.getAnswersWithQuestionByExam(examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", answers);
    }
    
    /**
     * 批量AI评分
     */
    @PostMapping("/batch-grade/{examCode}")
    public Object batchGrade(@PathVariable Integer examCode, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以批量评分");
        }
        try {
            int gradedCount = aiGradingService.batchGradeByExam(examCode);
            return ApiResultHandler.buildApiResult(200, "AI评分完成", gradedCount);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "AI评分失败: " + e.getMessage(), null);
        }
    }
    
    /**
     * 确认AI评分
     */
    @PostMapping("/confirm/{answerId}")
    public Object confirmAiGrading(@PathVariable Integer answerId, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以确认评分");
        }
        int result = aiGradingService.confirmAiGrading(answerId);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "确认成功", result);
        }
        return ApiResultHandler.buildApiResult(400, "确认失败", null);
    }
    
    /**
     * 教师修改评分
     */
    @PostMapping("/teacher-grade")
    public Object updateTeacherGrading(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以教师评分");
        }
        Integer answerId = (Integer) params.get("answerId");
        Integer teacherScore = (Integer) params.get("teacherScore");
        String teacherComment = (String) params.get("teacherComment");
        
        int result = aiGradingService.updateTeacherGrading(answerId, teacherScore, teacherComment);
        if (result > 0) {
            return ApiResultHandler.buildApiResult(200, "评分成功", result);
        }
        return ApiResultHandler.buildApiResult(400, "评分失败", null);
    }
    
    /**
     * 获取考试评分统计
     */
    @GetMapping("/stats/{examCode}")
    public Object getGradingStats(@PathVariable Integer examCode, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看阅卷统计");
        }
        Map<String, Object> stats = aiGradingService.getGradingStats(examCode);
        return ApiResultHandler.buildApiResult(200, "查询成功", stats);
    }
    
    /**
     * 计算学生的主观题总分
     */
    @GetMapping("/essay-score/{examCode}/{studentId}")
    public Object calculateEssayScore(@PathVariable Integer examCode, @PathVariable Long studentId, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看主观题得分");
        }
        Integer totalScore = aiGradingService.calculateEssayTotalScore(examCode, studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", totalScore);
    }
}
