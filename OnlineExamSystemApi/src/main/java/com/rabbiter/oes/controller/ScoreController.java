package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.Score;
import com.rabbiter.oes.serviceimpl.ScoreServiceImpl;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ScoreController {
    @Autowired
    private ScoreServiceImpl scoreService;

    @Autowired
    private AccessGuard accessGuard;

    @GetMapping("/scores")
    public ApiResult findAll(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看全部成绩");
        }
        List<Score> res = scoreService.findAll();
        return ApiResultHandler.buildApiResult(200,"查询所有学生成绩",res);
    }
//    分页
    @GetMapping("/score/{page}/{size}/{studentId}")
    public ApiResult findById(@PathVariable("page") Integer page, @PathVariable("size") Integer size,
                              @PathVariable("studentId") Long studentId, HttpServletRequest request) {
        if (!accessGuard.canAccessStudent(request, studentId)) {
            return accessGuard.forbidden("只能查看自己的成绩");
        }
        Page<Score> scorePage = new Page<>(page, size);
        IPage<Score> res = scoreService.findById(scorePage, studentId);
        return ApiResultHandler.buildApiResult(200, "根据ID查询成绩", res);
    }

    //    不分页（包含考试介绍，用于图表显示）
    @GetMapping("/score/{studentId}")
    public ApiResult findById(@PathVariable("studentId") Long studentId, HttpServletRequest request) {
        if (!accessGuard.canAccessStudent(request, studentId)) {
            return accessGuard.forbidden("只能查看自己的成绩");
        }
        List<Map<String, Object>> res = scoreService.findByIdWithDescription(studentId);
        if (!res.isEmpty()) {
            return ApiResultHandler.buildApiResult(200, "根据ID查询成绩", res);
        } else {
            return ApiResultHandler.buildApiResult(400, "ID不存在", res);
        }
    }

    @PostMapping("/score")
    public ApiResult add(@RequestBody Score score, HttpServletRequest request) {
        if (accessGuard.isStudent(request)) {
            if (score.getStudentId() == null || !accessGuard.isCurrentUser(request, score.getStudentId())) {
                return accessGuard.forbidden("只能提交自己的成绩记录");
            }
        } else if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("无权提交成绩");
        }
        int res = scoreService.add(score);
        if (res == 0) {
            return ApiResultHandler.buildApiResult(400,"成绩添加失败",res);
        }else {
            return ApiResultHandler.buildApiResult(200,"成绩添加成功",res);
        }
    }

    @GetMapping("/scores/{examCode}")
    public ApiResult findByExamCode(@PathVariable("examCode") Integer examCode, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看考试成绩");
        }
        List<Map<String, Object>> scores = scoreService.findByExamCode(examCode);
        return ApiResultHandler.buildApiResult(200,"查询成功",scores);
    }

    @GetMapping("/scoreDetail/{examCode}/{studentId}")
    public ApiResult getScoreDetail(@PathVariable("examCode") Integer examCode, 
                                     @PathVariable("studentId") Long studentId,
                                     HttpServletRequest request) {
        if (!accessGuard.canAccessStudent(request, studentId)) {
            return accessGuard.forbidden("只能查看自己的考试详情");
        }
        Map<String, Object> scoreDetail = scoreService.getScoreDetail(examCode, studentId);
        if (scoreDetail != null) {
            return ApiResultHandler.buildApiResult(200, "查询成功", scoreDetail);
        } else {
            return ApiResultHandler.buildApiResult(400, "未找到该考试记录", null);
        }
    }

    /**
     * 分页查询成绩（支持多条件筛选）
     * @param page 当前页
     * @param size 每页大小
     * @param studentId 学生ID
     * @param examType 考试类型
     * @param subject 课程名称
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param ptScore 是否及格
     * @return 分页结果
     */
    @GetMapping("/score/filter/{page}/{size}/{studentId}")
    public ApiResult findByIdWithFilter(
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size,
            @PathVariable("studentId") Long studentId,
            HttpServletRequest request,
            @RequestParam(value = "examType", required = false) String examType,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "ptScore", required = false) Integer ptScore) {
        if (!accessGuard.canAccessStudent(request, studentId)) {
            return accessGuard.forbidden("只能查看自己的成绩");
        }
        Page<Score> scorePage = new Page<>(page, size);
        IPage<Score> res = scoreService.findByIdWithFilter(
                scorePage, studentId, examType, subject, startDate, endDate, ptScore);
        return ApiResultHandler.buildApiResult(200, "查询成功", res);
    }

    /**
     * 导出考试成绩Excel
     * @param examCode 考试编号
     * @param response HTTP响应
     */
    @GetMapping("/export/score/excel/{examCode}")
    public void exportScoreExcel(@PathVariable("examCode") Integer examCode, 
                                HttpServletResponse response,
                                HttpServletRequest request) throws IOException {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("无权导出考试成绩");
            return;
        }
        try {
            scoreService.exportScoreExcel(examCode, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出学生个人成绩Excel
     * @param studentId 学生ID
     * @param response HTTP响应
     */
    @GetMapping("/export/score/student/{studentId}")
    public void exportStudentScoreExcel(@PathVariable("studentId") Long studentId,
                                       @RequestParam(value = "examType", required = false) String examType,
                                       @RequestParam(value = "subject", required = false) String subject,
                                       @RequestParam(value = "startDate", required = false) String startDate,
                                       @RequestParam(value = "endDate", required = false) String endDate,
                                       HttpServletResponse response,
                                       HttpServletRequest request) throws IOException {
        if (!accessGuard.canAccessStudent(request, studentId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("无权导出该学生成绩");
            return;
        }
        try {
            scoreService.exportStudentScoreExcel(studentId, examType, subject, startDate, endDate, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出考试成绩CSV
     * @param examCode 考试编号
     * @param response HTTP响应
     */
    @GetMapping("/export/score/csv/{examCode}")
    public void exportScoreCSV(@PathVariable("examCode") Integer examCode,
                              HttpServletResponse response,
                              HttpServletRequest request) throws IOException {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("无权导出考试成绩");
            return;
        }
        try {
            scoreService.exportScoreCSV(examCode, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("导出失败：" + e.getMessage());
        }
    }

    /**
     * 获取成绩统计分析数据
     * @param examCode 考试编号
     * @return 统计分析结果
     */
    @GetMapping("/statistics/score/{examCode}")
    public ApiResult getScoreStatistics(@PathVariable("examCode") Integer examCode, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看成绩统计");
        }
        try {
            Object statistics = scoreService.getScoreStatistics(examCode);
            return ApiResultHandler.buildApiResult(200, "获取统计数据成功", statistics);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取统计数据失败：" + e.getMessage(), null);
        }
    }

    /**
     * 获取增强的成绩统计分析数据（包含多维度分析）
     * @param examCode 考试编号
     * @return 增强统计分析结果
     */
    @GetMapping("/statistics/enhanced/{examCode}")
    public ApiResult getEnhancedScoreStatistics(@PathVariable("examCode") Integer examCode, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看增强统计");
        }
        try {
            Object enhancedStats = scoreService.getEnhancedScoreStatistics(examCode);
            return ApiResultHandler.buildApiResult(200, "获取增强统计数据成功", enhancedStats);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取增强统计数据失败：" + e.getMessage(), null);
        }
    }

    /**
     * 获取学生个人成绩趋势分析
     * @param studentId 学生ID
     * @return 学生成绩趋势分析结果
     */
    @GetMapping("/analysis/trend/student/{studentId}")
    public ApiResult getStudentScoreTrendAnalysis(@PathVariable("studentId") Long studentId, HttpServletRequest request) {
        if (!accessGuard.canAccessStudent(request, studentId)) {
            return accessGuard.forbidden("只能查看自己的成绩趋势");
        }
        try {
            Object trendAnalysis = scoreService.getStudentScoreTrendAnalysis(studentId);
            return ApiResultHandler.buildApiResult(200, "获取学生成绩趋势分析成功", trendAnalysis);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取学生成绩趋势分析失败：" + e.getMessage(), null);
        }
    }

    /**
     * 获取课程历史成绩对比分析
     * @param subject 课程名称
     * @return 课程历史成绩分析结果
     */
    @GetMapping("/analysis/history/subject/{subject}")
    public ApiResult getSubjectHistoryAnalysis(@PathVariable("subject") String subject, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看课程历史分析");
        }
        try {
            Object historyAnalysis = scoreService.getSubjectHistoryAnalysis(subject);
            return ApiResultHandler.buildApiResult(200, "获取课程历史分析成功", historyAnalysis);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取课程历史分析失败：" + e.getMessage(), null);
        }
    }
}
