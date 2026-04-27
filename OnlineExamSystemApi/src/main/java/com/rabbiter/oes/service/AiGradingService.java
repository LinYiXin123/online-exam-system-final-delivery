package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.EssayAnswer;
import com.rabbiter.oes.entity.EssayQuestion;
import java.util.List;
import java.util.Map;

/**
 * AI智能改卷Service接口
 */
public interface AiGradingService {
    
    /**
     * AI评分单个简答题
     * @param answer 学生答案
     * @param question 题目信息
     * @return 评分结果 {score, comment}
     */
    Map<String, Object> gradeEssayAnswer(EssayAnswer answer, EssayQuestion question);
    
    /**
     * 批量AI评分
     * @param examCode 考试编号
     * @return 评分数量
     */
    int batchGradeByExam(Integer examCode);
    
    /**
     * 获取考试的所有主观题答案（带题目信息）
     */
    List<Map<String, Object>> getAnswersWithQuestionByExam(Integer examCode);
    
    /**
     * 教师确认AI评分
     */
    int confirmAiGrading(Integer answerId);
    
    /**
     * 教师修改评分
     */
    int updateTeacherGrading(Integer answerId, Integer teacherScore, String teacherComment);
    
    /**
     * 获取考试评分统计
     */
    Map<String, Object> getGradingStats(Integer examCode);
    
    /**
     * 计算学生的主观题总分
     */
    Integer calculateEssayTotalScore(Integer examCode, Long studentId);
    
    /**
     * 获取参加考试的学生成绩列表
     */
    List<Map<String, Object>> getStudentScoresByExam(Integer examCode);
    
    /**
     * 获取学生客观题答案
     */
    List<Map<String, Object>> getObjectiveAnswers(Integer examCode, Long studentId);
    
    /**
     * 获取学生主观题答案
     */
    List<Map<String, Object>> getSubjectiveAnswers(Integer examCode, Long studentId);
    
    /**
     * 一键AI改卷（客观题+主观题）
     */
    Map<String, Object> autoGradeAll(Integer examCode);
    
    /**
     * AI评分单个学生的主观题
     */
    int gradeStudentEssay(Integer examCode, Long studentId);
    
    /**
     * 计算并更新学生总分
     */
    Map<String, Object> calculateAndUpdateTotalScore(Integer examCode, Long studentId);
    
    /**
     * 计算并更新学生总分（使用前端传递的分数）
     */
    Map<String, Object> calculateAndUpdateTotalScore(Integer examCode, Long studentId, Integer objectiveScore, Integer subjectiveScore);
}
