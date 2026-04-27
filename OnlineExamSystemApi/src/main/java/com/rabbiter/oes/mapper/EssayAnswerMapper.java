package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.oes.entity.EssayAnswer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Delete;

import java.util.List;
import java.util.Map;

/**
 * 学生主观题答案Mapper接口
 */
@Mapper
public interface EssayAnswerMapper extends BaseMapper<EssayAnswer> {
    @Delete("DELETE FROM essay_answer WHERE examCode = #{examCode} AND studentId = #{studentId} AND questionId = #{questionId}")
    int deleteByExamStudentQuestion(Integer examCode, Long studentId, Integer questionId);
    
    /**
     * 根据考试编号和学生ID查询答案
     */
    @Select("SELECT ea.* FROM essay_answer ea " +
            "INNER JOIN (SELECT questionId, MAX(answerId) as maxId FROM essay_answer WHERE examCode = #{examCode} AND studentId = #{studentId} GROUP BY questionId) latest " +
            "ON ea.answerId = latest.maxId")
    List<EssayAnswer> findByExamAndStudent(Integer examCode, Long studentId);
    
    /**
     * 根据考试编号查询所有待评分的答案
     */
    @Select("SELECT * FROM essay_answer WHERE examCode = #{examCode} AND gradingStatus = 'pending'")
    List<EssayAnswer> findPendingByExam(Integer examCode);
    
    /**
     * 根据考试编号查询所有答案（带学生信息）
     */
    @Select("SELECT ea.*, s.studentName FROM essay_answer ea " +
            "LEFT JOIN student s ON ea.studentId = s.studentId " +
            "WHERE ea.examCode = #{examCode}")
    List<Map<String, Object>> findAllByExamWithStudent(Integer examCode);
    
    /**
     * 更新AI评分
     */
    @Update("UPDATE essay_answer SET aiScore = #{aiScore}, aiComment = #{aiComment}, " +
            "gradingStatus = 'ai_graded', gradingTime = NOW() WHERE answerId = #{answerId}")
    int updateAiGrading(Integer answerId, Integer aiScore, String aiComment);
    
    /**
     * 更新教师评分
     */
    @Update("UPDATE essay_answer SET teacherScore = #{teacherScore}, teacherComment = #{teacherComment}, " +
            "finalScore = #{finalScore}, gradingStatus = 'teacher_reviewed', gradingTime = NOW() " +
            "WHERE answerId = #{answerId}")
    int updateTeacherGrading(Integer answerId, Integer teacherScore, String teacherComment, Integer finalScore);
    
    /**
     * 确认AI评分为最终分数
     */
    @Update("UPDATE essay_answer SET finalScore = aiScore, gradingStatus = 'teacher_reviewed', " +
            "gradingTime = NOW() WHERE answerId = #{answerId}")
    int confirmAiGrading(Integer answerId);
    
    /**
     * 统计考试的评分状态
     */
    @Select("SELECT gradingStatus, COUNT(*) as count FROM essay_answer " +
            "WHERE examCode = #{examCode} GROUP BY gradingStatus")
    List<Map<String, Object>> countGradingStatus(Integer examCode);
    
    /**
     * 获取参加考试的学生成绩列表
     */
    @Select("SELECT sc.studentId, s.studentName, s.major, s.grade, " +
            "sc.etScore as objectiveScore, sc.ptScore as subjectiveScore, " +
            "(IFNULL(sc.etScore, 0) + IFNULL(sc.ptScore, 0)) as totalScore, " +
            "CASE WHEN EXISTS(SELECT 1 FROM essay_answer ea WHERE ea.examCode = sc.examCode AND ea.studentId = sc.studentId AND ea.gradingStatus != 'teacher_reviewed') " +
            "THEN 'pending' ELSE 'completed' END as gradingStatus " +
            "FROM score sc " +
            "INNER JOIN (SELECT studentId, MAX(scoreId) as maxId FROM score WHERE examCode = #{examCode} GROUP BY studentId) latest ON sc.scoreId = latest.maxId " +
            "LEFT JOIN student s ON sc.studentId = s.studentId " +
            "WHERE sc.examCode = #{examCode}")
    List<Map<String, Object>> getStudentScoresByExam(Integer examCode);
    
    /**
     * 获取学生客观题答案（从answer_record表获取，去重并按题目顺序排列）
     */
    @Select("SELECT ar.questionId, ar.questionType, ar.studentAnswer, ar.isCorrect, " +
            "CASE WHEN ar.isCorrect = 1 THEN " +
            "CASE ar.questionType WHEN 1 THEN 2 WHEN 2 THEN 2 WHEN 3 THEN 2 ELSE 2 END " +
            "ELSE 0 END as score " +
            "FROM answer_record ar " +
            "INNER JOIN (" +
            "  SELECT questionType, questionId, MAX(recordId) as maxId " +
            "  FROM answer_record " +
            "  WHERE examCode = #{examCode} AND studentId = #{studentId} " +
            "  GROUP BY questionType, questionId" +
            ") latest ON ar.recordId = latest.maxId " +
            "WHERE ar.examCode = #{examCode} AND ar.studentId = #{studentId} " +
            "ORDER BY ar.questionType, ar.questionId")
    List<Map<String, Object>> getObjectiveAnswers(Integer examCode, Long studentId);
    
    /**
     * 获取试卷的题目ID映射（按题型和顺序）
     */
    @Select("SELECT pm.questionType, pm.questionId as realQuestionId " +
            "FROM paper_manage pm " +
            "INNER JOIN exam_manage em ON em.paperId = pm.paperId " +
            "WHERE em.examCode = #{examCode} AND pm.questionType IN (1, 2, 3) " +
            "ORDER BY pm.questionType, pm.id")
    List<Map<String, Object>> getPaperQuestionMapping(Integer examCode);
    
    /**
     * 获取试卷所有题目（包括主观题），按试卷顺序排列
     */
    @Select("SELECT pm.id as orderNum, pm.questionType, pm.questionId " +
            "FROM paper_manage pm " +
            "INNER JOIN exam_manage em ON em.paperId = pm.paperId " +
            "WHERE em.examCode = #{examCode} " +
            "ORDER BY pm.id")
    List<Map<String, Object>> getAllPaperQuestions(Integer examCode);
    
    /**
     * 根据考试和学生查询待评分的主观题答案
     */
    @Select("SELECT * FROM essay_answer WHERE examCode = #{examCode} AND studentId = #{studentId} AND gradingStatus = 'pending'")
    List<EssayAnswer> findPendingByExamAndStudent(Integer examCode, Long studentId);
    
    /**
     * 获取选择题正确答案
     */
    @Select("SELECT rightAnswer FROM multi_question WHERE questionId = #{questionId}")
    String getMultiQuestionAnswer(Integer questionId);
    
    /**
     * 获取填空题正确答案
     */
    @Select("SELECT answer FROM fill_question WHERE questionId = #{questionId}")
    String getFillQuestionAnswer(Integer questionId);
    
    /**
     * 获取判断题正确答案
     */
    @Select("SELECT answer FROM judge_question WHERE questionId = #{questionId}")
    String getJudgeQuestionAnswer(Integer questionId);
    
    /**
     * 客观题自动判分（返回影响的学生数）
     */
    @Update("UPDATE score sc SET sc.etScore = (" +
            "SELECT IFNULL(SUM(CASE latestAnswers.isCorrect WHEN 1 THEN " +
            "CASE latestAnswers.questionType WHEN 1 THEN 2 WHEN 2 THEN 2 WHEN 3 THEN 2 ELSE 2 END ELSE 0 END), 0) " +
            "FROM (" +
            "SELECT ar.questionType, ar.questionId, ar.isCorrect FROM answer_record ar " +
            "INNER JOIN (" +
            "SELECT questionType, questionId, MAX(recordId) as maxId FROM answer_record " +
            "WHERE examCode = sc.examCode AND studentId = sc.studentId GROUP BY questionType, questionId" +
            ") latest ON ar.recordId = latest.maxId" +
            ") latestAnswers) " +
            "WHERE sc.examCode = #{examCode}")
    int autoGradeObjective(Integer examCode);
    
    /**
     * 更新学生的主观题分数和总分
     */
    @Update("UPDATE score SET ptScore = #{subjectiveScore}, " +
            "etScore = IFNULL(etScore, 0) " +
            "WHERE examCode = #{examCode} AND studentId = #{studentId}")
    int updateStudentScore(Integer examCode, Long studentId, Integer subjectiveScore);
    
    /**
     * 更新学生的客观题和主观题分数
     */
    @Update("UPDATE score SET etScore = #{objectiveScore}, ptScore = #{subjectiveScore} " +
            "WHERE examCode = #{examCode} AND studentId = #{studentId}")
    int updateStudentScoreWithObjective(Integer examCode, Long studentId, Integer objectiveScore, Integer subjectiveScore);
}
