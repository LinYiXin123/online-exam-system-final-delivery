package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员仪表盘控制器 - 提供系统统计数据（支持筛选）
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AccessGuard accessGuard;

    // 兼容旧版Spring的计数查询方法
    private int getCount(String sql) {
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        if (result != null && !result.isEmpty()) {
            Object val = result.get(0).values().iterator().next();
            return val != null ? ((Number) val).intValue() : 0;
        }
        return 0;
    }
    
    // 带参数的计数查询方法
    private int getCountWithParams(String sql, Object... params) {
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params);
        if (result != null && !result.isEmpty()) {
            Object val = result.get(0).values().iterator().next();
            return val != null ? ((Number) val).intValue() : 0;
        }
        return 0;
    }
    
    // 构建学生筛选SQL条件
    private String buildStudentFilter(String institute, String major, String clazz, List<Object> params) {
        StringBuilder filter = new StringBuilder();
        if (institute != null && !institute.isEmpty()) {
            filter.append(" AND institute = ?");
            params.add(institute);
        }
        if (major != null && !major.isEmpty()) {
            filter.append(" AND major = ?");
            params.add(major);
        }
        if (clazz != null && !clazz.isEmpty()) {
            filter.append(" AND clazz = ?");
            params.add(clazz);
        }
        return filter.toString();
    }

    /**
     * 获取系统概览统计数据（支持筛选）
     */
    @GetMapping("/overview")
    public ApiResult getOverview(
            HttpServletRequest request,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String clazz,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Integer examCode) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以查看仪表盘数据");
        }
        Map<String, Object> stats = new HashMap<>();
        
        // 构建学生筛选条件
        List<Object> studentParams = new ArrayList<>();
        String studentFilter = buildStudentFilter(institute, major, clazz, studentParams);
        
        // 学生总数（支持筛选，包括按考试筛选参加该考试的学生）
        if (examCode != null) {
            String studentSql = "SELECT COUNT(DISTINCT st.studentId) FROM student st JOIN score s ON st.studentId = s.studentId WHERE s.examCode = ?" + studentFilter.replace("AND ", "AND st.");
            List<Object> examStudentParams = new ArrayList<>();
            examStudentParams.add(examCode);
            examStudentParams.addAll(studentParams);
            stats.put("studentCount", getCountWithParams(studentSql, examStudentParams.toArray()));
        } else {
            String studentSql = "SELECT COUNT(*) FROM student WHERE 1=1" + studentFilter;
            stats.put("studentCount", getCountWithParams(studentSql, studentParams.toArray()));
        }
        
        // 教师总数
        stats.put("teacherCount", getCount("SELECT COUNT(*) FROM teacher"));
        
        // 考试总数（支持科目和考试筛选）
        if (examCode != null) {
            stats.put("examCount", 1); // 选择了特定考试
        } else if (subject != null && !subject.isEmpty()) {
            stats.put("examCount", getCountWithParams("SELECT COUNT(*) FROM exam_manage WHERE source = ?", subject));
        } else if (institute != null && !institute.isEmpty()) {
            stats.put("examCount", getCountWithParams("SELECT COUNT(*) FROM exam_manage WHERE institute = ?", institute));
        } else {
            stats.put("examCount", getCount("SELECT COUNT(*) FROM exam_manage"));
        }
        
        // 题目总数（支持科目筛选）
        String subjectFilter = "";
        if (subject != null && !subject.isEmpty()) {
            subjectFilter = " WHERE subject = '" + subject.replace("'", "''") + "'";
        }
        int multiCount = getCount("SELECT COUNT(*) FROM multi_question" + subjectFilter);
        int fillCount = getCount("SELECT COUNT(*) FROM fill_question" + subjectFilter);
        int judgeCount = getCount("SELECT COUNT(*) FROM judge_question" + subjectFilter);
        int essayCount = getEssayCount(subject);
        stats.put("questionCount", multiCount + fillCount + judgeCount + essayCount);
        
        // 成绩记录总数（支持科目筛选）
        if (examCode != null) {
            stats.put("scoreCount", getCountWithParams("SELECT COUNT(*) FROM score WHERE examCode = ?", examCode));
        } else if (subject != null && !subject.isEmpty()) {
            // 通过考试的source字段筛选科目
            stats.put("scoreCount", getCountWithParams(
                "SELECT COUNT(*) FROM score s JOIN exam_manage e ON s.examCode = e.examCode WHERE e.source = ?", subject));
        } else if (!studentParams.isEmpty()) {
            String scoreSql = "SELECT COUNT(*) FROM score s JOIN student st ON s.studentId = st.studentId WHERE 1=1" + studentFilter.replace("AND ", "AND st.");
            stats.put("scoreCount", getCountWithParams(scoreSql, studentParams.toArray()));
        } else {
            stats.put("scoreCount", getCount("SELECT COUNT(*) FROM score"));
        }
        
        // 留言总数
        stats.put("messageCount", getCount("SELECT COUNT(*) FROM message"));
        
        return ApiResultHandler.buildApiResult(200, "获取成功", stats);
    }
    
    // 安全获取主观题数量（表可能不存在，支持科目筛选）
    private int getEssayCount(String subject) {
        try {
            if (subject != null && !subject.isEmpty()) {
                return getCountWithParams("SELECT COUNT(*) FROM essay_question WHERE subject = ?", subject);
            }
            return getCount("SELECT COUNT(*) FROM essay_question");
        } catch (Exception e) {
            return 0;
        }
    }
    
    // 无参数版本（保持兼容）
    private int getEssayCount() {
        return getEssayCount(null);
    }

    /**
     * 获取各学院学生分布（支持筛选，包括按考试筛选参加该考试的学生）
     */
    @GetMapping("/institute-distribution")
    public ApiResult getInstituteDistribution(
            HttpServletRequest request,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String clazz,
            @RequestParam(required = false) Integer examCode) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以查看仪表盘数据");
        }
        List<Object> params = new ArrayList<>();
        String filter = buildStudentFilter(institute, major, clazz, params);
        
        String sql;
        if (examCode != null) {
            // 按考试筛选：只统计参加该考试的学生
            sql = "SELECT st.institute as name, COUNT(DISTINCT st.studentId) as value " +
                  "FROM student st JOIN score s ON st.studentId = s.studentId " +
                  "WHERE s.examCode = ?" + filter.replace("AND ", "AND st.") +
                  " GROUP BY st.institute ORDER BY value DESC";
            params.add(0, examCode);
        } else {
            sql = "SELECT institute as name, COUNT(*) as value FROM student WHERE 1=1" + filter + " GROUP BY institute ORDER BY value DESC";
        }
        List<Map<String, Object>> distribution = jdbcTemplate.queryForList(sql, params.toArray());
        return ApiResultHandler.buildApiResult(200, "获取成功", distribution);
    }

    /**
     * 获取各科目考试次数
     */
    @GetMapping("/subject-exams")
    public ApiResult getSubjectExams(HttpServletRequest request) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以查看仪表盘数据");
        }
        List<Map<String, Object>> subjectExams = jdbcTemplate.queryForList(
            "SELECT source as name, COUNT(*) as value FROM exam_manage GROUP BY source ORDER BY value DESC"
        );
        return ApiResultHandler.buildApiResult(200, "获取成功", subjectExams);
    }

    /**
     * 获取成绩分布（按分数段，支持筛选）
     */
    @GetMapping("/score-distribution")
    public ApiResult getScoreDistribution(
            HttpServletRequest request,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String clazz,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Integer examCode) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以查看仪表盘数据");
        }
        Map<String, Object> distribution = new HashMap<>();
        
        // 构建基础SQL和条件
        String baseJoin = "";
        String baseFilter = "";
        List<Object> baseParams = new ArrayList<>();
        
        if (examCode != null) {
            baseFilter = " AND s.examCode = ?";
            baseParams.add(examCode);
        }
        
        // 科目筛选 - 通过考试的source字段
        if (subject != null && !subject.isEmpty()) {
            baseJoin = " JOIN exam_manage e ON s.examCode = e.examCode";
            baseFilter += " AND e.source = ?";
            baseParams.add(subject);
        }
        
        if (institute != null && !institute.isEmpty() || major != null && !major.isEmpty() || clazz != null && !clazz.isEmpty()) {
            if (!baseJoin.contains("student")) {
                baseJoin += " JOIN student st ON s.studentId = st.studentId";
            }
            if (institute != null && !institute.isEmpty()) {
                baseFilter += " AND st.institute = ?";
                baseParams.add(institute);
            }
            if (major != null && !major.isEmpty()) {
                baseFilter += " AND st.major = ?";
                baseParams.add(major);
            }
            if (clazz != null && !clazz.isEmpty()) {
                baseFilter += " AND st.clazz = ?";
                baseParams.add(clazz);
            }
        }
        
        // 各分数段人数
        String baseSql = "SELECT COUNT(*) FROM score s" + baseJoin + " WHERE 1=1" + baseFilter;
        distribution.put("excellent", getCountWithParams(baseSql + " AND s.etScore >= 90", baseParams.toArray()));
        distribution.put("good", getCountWithParams(baseSql + " AND s.etScore >= 80 AND s.etScore < 90", baseParams.toArray()));
        distribution.put("medium", getCountWithParams(baseSql + " AND s.etScore >= 70 AND s.etScore < 80", baseParams.toArray()));
        distribution.put("pass", getCountWithParams(baseSql + " AND s.etScore >= 60 AND s.etScore < 70", baseParams.toArray()));
        distribution.put("fail", getCountWithParams(baseSql + " AND s.etScore < 60", baseParams.toArray()));
        
        return ApiResultHandler.buildApiResult(200, "获取成功", distribution);
    }

    /**
     * 获取最近考试列表（支持筛选）
     */
    @GetMapping("/recent-exams")
    public ApiResult getRecentExams(
            HttpServletRequest request,
            @RequestParam(required = false) String institute,
            @RequestParam(required = false) Integer examCode) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以查看仪表盘数据");
        }
        String sql;
        Object[] params;
        
        if (examCode != null) {
            sql = "SELECT examCode, description, source, examDate, institute, major FROM exam_manage WHERE examCode = ?";
            params = new Object[]{examCode};
        } else if (institute != null && !institute.isEmpty()) {
            sql = "SELECT examCode, description, source, examDate, institute, major FROM exam_manage WHERE institute = ? ORDER BY examDate DESC LIMIT 5";
            params = new Object[]{institute};
        } else {
            sql = "SELECT examCode, description, source, examDate, institute, major FROM exam_manage ORDER BY examDate DESC LIMIT 5";
            params = new Object[]{};
        }
        
        List<Map<String, Object>> recentExams = jdbcTemplate.queryForList(sql, params);
        return ApiResultHandler.buildApiResult(200, "获取成功", recentExams);
    }

    /**
     * 获取筛选选项（学院、专业、班级、考试列表）
     */
    @GetMapping("/filter-options")
    public ApiResult getFilterOptions(HttpServletRequest request) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以查看仪表盘数据");
        }
        Map<String, Object> options = new HashMap<>();
        
        // 获取所有学院
        List<Map<String, Object>> institutes = jdbcTemplate.queryForList(
            "SELECT DISTINCT institute FROM student WHERE institute IS NOT NULL AND institute != '' ORDER BY institute"
        );
        options.put("institutes", institutes.stream().map(m -> m.get("institute")).collect(Collectors.toList()));
        
        // 获取所有学生（用于联动筛选）
        List<Map<String, Object>> students = jdbcTemplate.queryForList(
            "SELECT studentId, institute, major, clazz FROM student WHERE institute IS NOT NULL"
        );
        options.put("students", students);
        
        // 获取所有考试
        List<Map<String, Object>> exams = jdbcTemplate.queryForList(
            "SELECT examCode, source, description FROM exam_manage ORDER BY examDate DESC"
        );
        options.put("exams", exams);
        
        // 获取所有科目（从题库和考试中提取）
        List<Map<String, Object>> subjects = jdbcTemplate.queryForList(
            "SELECT DISTINCT subject FROM (" +
            "SELECT subject FROM multi_question UNION " +
            "SELECT subject FROM fill_question UNION " +
            "SELECT subject FROM judge_question UNION " +
            "SELECT source as subject FROM exam_manage" +
            ") t WHERE subject IS NOT NULL AND subject != '' ORDER BY subject"
        );
        options.put("subjects", subjects.stream().map(m -> m.get("subject")).collect(Collectors.toList()));
        
        return ApiResultHandler.buildApiResult(200, "获取成功", options);
    }

    /**
     * 获取题库统计（包含主观题，支持科目筛选）
     */
    @GetMapping("/question-stats")
    public ApiResult getQuestionStats(@RequestParam(required = false) String subject, HttpServletRequest request) {
        if (!accessGuard.isAdmin(request)) {
            return accessGuard.forbidden("只有管理员可以查看仪表盘数据");
        }
        Map<String, Object> stats = new HashMap<>();
        
        String subjectFilter = "";
        Object[] params = new Object[]{};
        if (subject != null && !subject.isEmpty()) {
            subjectFilter = " WHERE subject = ?";
            params = new Object[]{subject};
        }
        
        // 选择题数量
        stats.put("multiCount", getCountWithParams("SELECT COUNT(*) FROM multi_question" + subjectFilter, params));
        // 填空题数量
        stats.put("fillCount", getCountWithParams("SELECT COUNT(*) FROM fill_question" + subjectFilter, params));
        // 判断题数量
        stats.put("judgeCount", getCountWithParams("SELECT COUNT(*) FROM judge_question" + subjectFilter, params));
        // 主观题数量
        stats.put("essayCount", getEssayCount(subject));
        
        // 按科目统计（包含主观题）
        String unionSql = "SELECT subject FROM multi_question UNION ALL " +
            "SELECT subject FROM fill_question UNION ALL " +
            "SELECT subject FROM judge_question";
        try {
            // 尝试添加主观题表
            unionSql += " UNION ALL SELECT subject FROM essay_question";
        } catch (Exception ignored) {}
        
        List<Map<String, Object>> bySubject = jdbcTemplate.queryForList(
            "SELECT subject, COUNT(*) as count FROM (" + unionSql + ") t GROUP BY subject ORDER BY count DESC"
        );
        stats.put("bySubject", bySubject);
        
        return ApiResultHandler.buildApiResult(200, "获取成功", stats);
    }
}
