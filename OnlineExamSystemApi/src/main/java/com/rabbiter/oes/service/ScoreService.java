package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Score;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ScoreService {
    int add(Score score);

    List<Score> findAll();

    IPage<Score> findById(Page page, Long studentId);

    List<Score> findById(Long studentId);

    List<Map<String, Object>> findByExamCode(Integer examCode);

    Map<String, Object> getScoreDetail(Integer examCode, Long studentId);

    IPage<Score> findByIdWithFilter(Page page, Long studentId, String examType, 
                                     String subject, String startDate, String endDate, Integer ptScore);

    /**
     * 导出考试成绩Excel
     */
    void exportScoreExcel(Integer examCode, HttpServletResponse response) throws IOException;

    /**
     * 导出学生个人成绩Excel
     */
    void exportStudentScoreExcel(Long studentId, String examType, String subject, 
                                String startDate, String endDate, HttpServletResponse response) throws IOException;

    /**
     * 导出考试成绩CSV
     */
    void exportScoreCSV(Integer examCode, HttpServletResponse response) throws IOException;

    /**
     * 获取成绩统计分析数据
     */
    Map<String, Object> getScoreStatistics(Integer examCode);

    /**
     * 获取增强的成绩统计分析数据（包含多维度分析）
     */
    Map<String, Object> getEnhancedScoreStatistics(Integer examCode);

    /**
     * 获取学生个人成绩趋势分析
     */
    Map<String, Object> getStudentScoreTrendAnalysis(Long studentId);

    /**
     * 获取课程历史成绩对比分析
     */
    Map<String, Object> getSubjectHistoryAnalysis(String subject);
}
