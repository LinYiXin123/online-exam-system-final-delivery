package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ExamManage;
import com.rabbiter.oes.entity.Score;
import com.rabbiter.oes.mapper.ExamManageMapper;
import com.rabbiter.oes.mapper.ScoreMapper;
import com.rabbiter.oes.service.ScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Autowired
    private ScoreMapper scoreMapper;

    @Autowired
    private ExamManageMapper examManageMapper;
    @Override
    public int add(Score score) {
        if (score == null || score.getExamCode() == null || score.getStudentId() == null) {
            return 0;
        }

        if ("exam".equals(score.getScoreType())) {
            ExamManage examManage = examManageMapper.findById(score.getExamCode());
            if (examManage != null && !isWithinExamWindow(examManage)) {
                return 0;
            }
            Score existing = scoreMapper.findLatestByExamStudentAndType(score.getExamCode(), score.getStudentId(), "exam");
            if (existing != null) {
                score.setScoreId(existing.getScoreId());
                return scoreMapper.updateById(score);
            }
        }
        return scoreMapper.add(score);
    }

    @Override
    public List<Score> findAll() {
        return scoreMapper.findAll();
    }

    @Override
    public IPage<Score> findById(Page page, Long studentId) {
        return scoreMapper.findById(page, studentId);
    }

    @Override
    public List<Score> findById(Long studentId) {
        Page<Score> scorePage = new Page<>(0, 9999);
        return scoreMapper.findById(scorePage, studentId).getRecords();
    }
    
    /**
     * 获取学生成绩（包含考试介绍，用于图表显示）
     */
    public List<Map<String, Object>> findByIdWithDescription(Long studentId) {
        return scoreMapper.findByIdWithDescription(studentId);
    }

    @Override
    public List<Map<String, Object>> findByExamCode(Integer examCode) {
        return scoreMapper.findByExamCode(examCode);
    }

    @Override
    public Map<String, Object> getScoreDetail(Integer examCode, Long studentId) {
        return scoreMapper.getScoreDetail(examCode, studentId);
    }

    @Override
    public IPage<Score> findByIdWithFilter(Page page, Long studentId, String examType,
                                            String subject, String startDate, String endDate, Integer ptScore) {
        return scoreMapper.findByIdWithFilter(page, studentId, examType, subject, startDate, endDate, ptScore);
    }

    @Override
    public void exportScoreExcel(Integer examCode, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> detailedScores = scoreMapper.findDetailedScoresByExamCode(examCode);
        
        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("考试成绩详细表_" + examCode + "_" + 
            new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv", "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        PrintWriter writer = response.getWriter();
        
        // 写入CSV头部 - 包含更全面的字段
        writer.println("学号,姓名,性别,年级,专业,班级,学院,联系电话,邮箱,考试编号,考试名称,课程名称,考试日期,考试时长(分钟),试卷总分,考试分数,是否及格,答题日期,考试类型");
        
        // 写入数据
        for (Map<String, Object> record : detailedScores) {
            int finalScore = getFinalScore(record);
            boolean pass = isPass(record, finalScore);
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                getValue(record, "studentId"),
                getValue(record, "studentName"),
                getValue(record, "sex"),
                getValue(record, "grade"),
                getValue(record, "major"),
                getValue(record, "clazz"),
                getValue(record, "institute"),
                getValue(record, "tel"),
                getValue(record, "email"),
                getValue(record, "examCode"),
                getValue(record, "examDescription"),
                getValue(record, "subject"),
                getValue(record, "examDate"),
                getValue(record, "totalTime"),
                getValue(record, "examTotalScore"),
                finalScore,
                pass ? "及格" : "不及格",
                getValue(record, "answerDate"),
                getValue(record, "scoreType")
            );
        }
        
        writer.flush();
        writer.close();
    }

    /**
     * 安全获取Map中的值，避免空指针异常
     */
    private String getValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    @Override
    public void exportStudentScoreExcel(Long studentId, String examType, String subject, 
                                       String startDate, String endDate, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> detailedScores = scoreMapper.findDetailedScoresByStudentId(studentId, examType, subject, startDate, endDate);
        
        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("UTF-8");
        String fileName = URLEncoder.encode("个人成绩详细表_" + studentId + "_" + 
            new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv", "UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        
        PrintWriter writer = response.getWriter();
        
        // 写入CSV头部 - 包含更全面的字段
        writer.println("学号,姓名,性别,年级,专业,班级,学院,联系电话,邮箱,考试编号,考试名称,课程名称,考试日期,考试时长(分钟),试卷总分,考试分数,是否及格,答题日期,考试类型");
        
        // 写入数据
        for (Map<String, Object> record : detailedScores) {
            int finalScore = getFinalScore(record);
            boolean pass = isPass(record, finalScore);
            writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                getValue(record, "studentId"),
                getValue(record, "studentName"),
                getValue(record, "sex"),
                getValue(record, "grade"),
                getValue(record, "major"),
                getValue(record, "clazz"),
                getValue(record, "institute"),
                getValue(record, "tel"),
                getValue(record, "email"),
                getValue(record, "examCode"),
                getValue(record, "examDescription"),
                getValue(record, "subject"),
                getValue(record, "examDate"),
                getValue(record, "totalTime"),
                getValue(record, "examTotalScore"),
                finalScore,
                pass ? "及格" : "不及格",
                getValue(record, "answerDate"),
                getValue(record, "scoreType")
            );
        }
        
        writer.flush();
        writer.close();
    }

    @Override
    public void exportScoreCSV(Integer examCode, HttpServletResponse response) throws IOException {
        // CSV导出与Excel导出逻辑相同，因为我们使用的是CSV格式
        exportScoreExcel(examCode, response);
    }

    @Override
    public Map<String, Object> getScoreStatistics(Integer examCode) {
        List<Map<String, Object>> scores = scoreMapper.findByExamCode(examCode);
        Map<String, Object> statistics = new HashMap<>();
        
        if (scores.isEmpty()) {
            statistics.put("totalCount", 0);
            statistics.put("passCount", 0);
            statistics.put("failCount", 0);
            statistics.put("passRate", 0.0);
            statistics.put("averageScore", 0.0);
            statistics.put("maxScore", 0);
            statistics.put("minScore", 0);
            statistics.put("scoreDistribution", new HashMap<>());
            return statistics;
        }
        
        int totalCount = scores.size();
        // 及格率基于总分（客观题etScore + 主观题ptScore）计算，总分>=60为及格
        int passCount = (int) scores.stream().filter(s -> {
            Object et = s.get("etScore");
            Object pt = s.get("ptScore");
            int etScore = et != null ? ((Number) et).intValue() : 0;
            int ptScore = pt != null ? ((Number) pt).intValue() : 0;
            int totalScore = etScore + ptScore;
            return totalScore >= 60;
        }).count();
        int failCount = totalCount - passCount;
        double passRate = (double) passCount / totalCount * 100;
        
        // 平均分、最高分、最低分基于总分计算
        double averageScore = scores.stream().mapToInt(s -> {
            Object et = s.get("etScore");
            Object pt = s.get("ptScore");
            int etScore = et != null ? ((Number) et).intValue() : 0;
            int ptScore = pt != null ? ((Number) pt).intValue() : 0;
            return etScore + ptScore;
        }).average().orElse(0.0);
        int maxScore = scores.stream().mapToInt(s -> {
            Object et = s.get("etScore");
            Object pt = s.get("ptScore");
            int etScore = et != null ? ((Number) et).intValue() : 0;
            int ptScore = pt != null ? ((Number) pt).intValue() : 0;
            return etScore + ptScore;
        }).max().orElse(0);
        int minScore = scores.stream().mapToInt(s -> {
            Object et = s.get("etScore");
            Object pt = s.get("ptScore");
            int etScore = et != null ? ((Number) et).intValue() : 0;
            int ptScore = pt != null ? ((Number) pt).intValue() : 0;
            return etScore + ptScore;
        }).min().orElse(0);
        
        // 分数段分布统计（基于总分）
        Map<String, Integer> scoreDistribution = new HashMap<>();
        scoreDistribution.put("90分及以上", 0);
        scoreDistribution.put("80-89分", 0);
        scoreDistribution.put("70-79分", 0);
        scoreDistribution.put("60-69分", 0);
        scoreDistribution.put("60分以下", 0);
        
        for (Map<String, Object> score : scores) {
            Object etObj = score.get("etScore");
            Object ptObj = score.get("ptScore");
            int etScore = etObj != null ? ((Number) etObj).intValue() : 0;
            int ptScore = ptObj != null ? ((Number) ptObj).intValue() : 0;
            int totalScore = etScore + ptScore;
            if (totalScore >= 90) {
                scoreDistribution.put("90分及以上", scoreDistribution.get("90分及以上") + 1);
            } else if (totalScore >= 80) {
                scoreDistribution.put("80-89分", scoreDistribution.get("80-89分") + 1);
            } else if (totalScore >= 70) {
                scoreDistribution.put("70-79分", scoreDistribution.get("70-79分") + 1);
            } else if (totalScore >= 60) {
                scoreDistribution.put("60-69分", scoreDistribution.get("60-69分") + 1);
            } else {
                scoreDistribution.put("60分以下", scoreDistribution.get("60分以下") + 1);
            }
        }
        
        statistics.put("totalCount", totalCount);
        statistics.put("passCount", passCount);
        statistics.put("failCount", failCount);
        statistics.put("passRate", Math.round(passRate * 100.0) / 100.0);
        statistics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        statistics.put("maxScore", maxScore);
        statistics.put("minScore", minScore);
        statistics.put("scoreDistribution", scoreDistribution);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getEnhancedScoreStatistics(Integer examCode) {
        Map<String, Object> enhancedStats = new HashMap<>();
        
        // 基础统计数据
        Map<String, Object> basicStats = getScoreStatistics(examCode);
        enhancedStats.put("basicStatistics", basicStats);
        
        // 班级统计
        List<Map<String, Object>> classStats = scoreMapper.getClassStatistics(examCode);
        enhancedStats.put("classStatistics", processStatisticsList(classStats));
        
        // 专业统计
        List<Map<String, Object>> majorStats = scoreMapper.getMajorStatistics(examCode);
        enhancedStats.put("majorStatistics", processStatisticsList(majorStats));
        
        // 学院统计
        List<Map<String, Object>> instituteStats = scoreMapper.getInstituteStatistics(examCode);
        enhancedStats.put("instituteStatistics", processStatisticsList(instituteStats));
        
        // 性别统计
        List<Map<String, Object>> genderStats = scoreMapper.getGenderStatistics(examCode);
        enhancedStats.put("genderStatistics", processStatisticsList(genderStats));
        
        return enhancedStats;
    }

    @Override
    public Map<String, Object> getStudentScoreTrendAnalysis(Long studentId) {
        Map<String, Object> trendAnalysis = new HashMap<>();
        
        // 获取学生成绩趋势数据
        List<Map<String, Object>> trendData = scoreMapper.getStudentScoreTrend(studentId);
        
        if (trendData.isEmpty()) {
            trendAnalysis.put("hasTrend", false);
            trendAnalysis.put("message", "暂无成绩数据");
            return trendAnalysis;
        }
        
        // 处理每条记录，计算总分（已批改的才计算主观题分数）
        for (Map<String, Object> data : trendData) {
            int etScore = Integer.valueOf(getValue(data, "etScore"));
            int isGraded = Integer.valueOf(getValue(data, "isGraded"));
            int subjectiveScore = Integer.valueOf(getValue(data, "subjectiveScore"));
            
            // 已批改：总分 = 客观题分数 + 主观题分数
            // 未批改：只显示客观题分数
            if (isGraded == 1) {
                data.put("etScore", etScore + subjectiveScore);
            }
            // 转换isGraded为布尔值便于前端使用
            data.put("isGraded", isGraded == 1);
        }
        
        trendAnalysis.put("hasTrend", true);
        trendAnalysis.put("trendData", trendData);
        
        // 只计算已批改的成绩
        List<Integer> gradedScores = trendData.stream()
            .filter(data -> Boolean.TRUE.equals(data.get("isGraded")))
            .map(data -> Integer.valueOf(getValue(data, "etScore")))
            .collect(Collectors.toList());
        
        if (!gradedScores.isEmpty()) {
            // 平均分
            double avgScore = gradedScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            trendAnalysis.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
            
            // 最高分和最低分
            int maxScore = gradedScores.stream().mapToInt(Integer::intValue).max().orElse(0);
            int minScore = gradedScores.stream().mapToInt(Integer::intValue).min().orElse(0);
            trendAnalysis.put("maxScore", maxScore);
            trendAnalysis.put("minScore", minScore);
            
            // 成绩趋势（上升/下降/稳定）
            if (gradedScores.size() >= 2) {
                int firstScore = gradedScores.get(0);
                int lastScore = gradedScores.get(gradedScores.size() - 1);
                String trend = lastScore > firstScore ? "上升" : 
                              lastScore < firstScore ? "下降" : "稳定";
                trendAnalysis.put("overallTrend", trend);
                trendAnalysis.put("trendValue", lastScore - firstScore);
            }
        } else {
            trendAnalysis.put("averageScore", 0);
            trendAnalysis.put("maxScore", 0);
            trendAnalysis.put("minScore", 0);
        }
        
        // 科目分布
        Map<String, Integer> subjectCount = new HashMap<>();
        for (Map<String, Object> data : trendData) {
            String subject = getValue(data, "subject");
            subjectCount.put(subject, subjectCount.getOrDefault(subject, 0) + 1);
        }
        trendAnalysis.put("subjectDistribution", subjectCount);
        
        return trendAnalysis;
    }

    @Override
    public Map<String, Object> getSubjectHistoryAnalysis(String subject) {
        Map<String, Object> historyAnalysis = new HashMap<>();
        
        // 获取课程历史统计数据
        List<Map<String, Object>> historyData = scoreMapper.getSubjectHistoryStatistics(subject);
        
        if (historyData.isEmpty()) {
            historyAnalysis.put("hasHistory", false);
            historyAnalysis.put("message", "暂无该课程的历史数据");
            return historyAnalysis;
        }
        
        historyAnalysis.put("hasHistory", true);
        historyAnalysis.put("historyData", processStatisticsList(historyData));
        
        // 计算历史趋势
        List<Double> avgScores = historyData.stream()
            .map(data -> Double.valueOf(getValue(data, "averageScore")))
            .collect(Collectors.toList());
        
        // 整体平均分
        double overallAvg = avgScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        historyAnalysis.put("overallAverageScore", Math.round(overallAvg * 100.0) / 100.0);
        
        // 最好和最差的考试
        if (historyData.size() > 1) {
            Map<String, Object> bestExam = historyData.stream()
                .max((a, b) -> Double.compare(
                    Double.valueOf(getValue(a, "averageScore")),
                    Double.valueOf(getValue(b, "averageScore"))
                )).orElse(null);
            
            Map<String, Object> worstExam = historyData.stream()
                .min((a, b) -> Double.compare(
                    Double.valueOf(getValue(a, "averageScore")),
                    Double.valueOf(getValue(b, "averageScore"))
                )).orElse(null);
            
            historyAnalysis.put("bestExam", bestExam);
            historyAnalysis.put("worstExam", worstExam);
        }
        
        return historyAnalysis;
    }

    /**
     * 处理统计列表数据，计算及格率等
     */
    private List<Map<String, Object>> processStatisticsList(List<Map<String, Object>> statsList) {
        return statsList.stream().map(stats -> {
            Map<String, Object> processedStats = new HashMap<>(stats);
            
            // 计算及格率
            int totalCount = Integer.valueOf(getValue(stats, "totalCount"));
            int passCount = Integer.valueOf(getValue(stats, "passCount"));
            
            if (totalCount > 0) {
                double passRate = (double) passCount / totalCount * 100;
                processedStats.put("passRate", Math.round(passRate * 100.0) / 100.0);
                processedStats.put("failCount", totalCount - passCount);
            } else {
                processedStats.put("passRate", 0.0);
                processedStats.put("failCount", 0);
            }
            
            // 格式化平均分
            if (stats.containsKey("averageScore")) {
                double avgScore = Double.valueOf(getValue(stats, "averageScore"));
                processedStats.put("averageScore", Math.round(avgScore * 100.0) / 100.0);
            }
            
            return processedStats;
        }).collect(Collectors.toList());
    }

    private boolean isWithinExamWindow(ExamManage examManage) {
        if (examManage.getExamDate() == null || examManage.getExamDate().trim().isEmpty()) {
            return true;
        }
        try {
            LocalDate examDate = LocalDate.parse(examManage.getExamDate().trim());
            LocalDateTime now = LocalDateTime.now();
            if (now.toLocalDate().isBefore(examDate)) {
                return false;
            }
            if (now.toLocalDate().isAfter(examDate)) {
                return true;
            }

            if (examManage.getStartTime() != null && examManage.getEndTime() != null
                    && !examManage.getStartTime().trim().isEmpty()
                    && !examManage.getEndTime().trim().isEmpty()) {
                LocalDateTime start = LocalDateTime.of(examDate, LocalTime.parse(examManage.getStartTime().trim()));
                LocalDateTime end = LocalDateTime.of(examDate, LocalTime.parse(examManage.getEndTime().trim()));
                return !now.isBefore(start) && !now.isAfter(end);
            }

            return now.toLocalDate().isEqual(examDate);
        } catch (Exception e) {
            return true;
        }
    }

    private int getFinalScore(Map<String, Object> record) {
        int objective = parseInt(record.get("etScore"));
        int subjective = parseInt(record.get("ptScore"));
        return objective + subjective;
    }

    private boolean isPass(Map<String, Object> record, int finalScore) {
        int total = parseInt(record.get("examTotalScore"));
        if (total <= 0) {
            total = 100;
        }
        return finalScore >= Math.round(total * 0.6);
    }

    private int parseInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }
}
