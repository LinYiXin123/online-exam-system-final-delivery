package com.rabbiter.oes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 学生主观题答案实体类
 */
@TableName("essay_answer")
public class EssayAnswer {
    
    @TableId(value = "answerId", type = IdType.AUTO)
    private Integer answerId;
    
    @TableField("examCode")
    private Integer examCode;       // 考试编号
    
    @TableField("studentId")
    private Long studentId;         // 学生ID
    
    @TableField("questionId")
    private Integer questionId;     // 题目ID
    
    @TableField("studentAnswer")
    private String studentAnswer;   // 学生作答内容
    
    @TableField("aiScore")
    private Integer aiScore;        // AI评分
    
    @TableField("aiComment")
    private String aiComment;       // AI评语
    
    @TableField("teacherScore")
    private Integer teacherScore;   // 教师评分(可覆盖AI)
    
    @TableField("teacherComment")
    private String teacherComment;  // 教师评语
    
    @TableField("finalScore")
    private Integer finalScore;     // 最终得分
    
    @TableField("gradingStatus")
    private String gradingStatus;   // 评分状态：pending/ai_graded/teacher_reviewed
    
    @TableField("gradingTime")
    private Date gradingTime;       // 评分时间

    public Integer getAnswerId() {
        return answerId;
    }

    public void setAnswerId(Integer answerId) {
        this.answerId = answerId;
    }

    public Integer getExamCode() {
        return examCode;
    }

    public void setExamCode(Integer examCode) {
        this.examCode = examCode;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public Integer getAiScore() {
        return aiScore;
    }

    public void setAiScore(Integer aiScore) {
        this.aiScore = aiScore;
    }

    public String getAiComment() {
        return aiComment;
    }

    public void setAiComment(String aiComment) {
        this.aiComment = aiComment;
    }

    public Integer getTeacherScore() {
        return teacherScore;
    }

    public void setTeacherScore(Integer teacherScore) {
        this.teacherScore = teacherScore;
    }

    public String getTeacherComment() {
        return teacherComment;
    }

    public void setTeacherComment(String teacherComment) {
        this.teacherComment = teacherComment;
    }

    public Integer getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    public String getGradingStatus() {
        return gradingStatus;
    }

    public void setGradingStatus(String gradingStatus) {
        this.gradingStatus = gradingStatus;
    }

    public Date getGradingTime() {
        return gradingTime;
    }

    public void setGradingTime(Date gradingTime) {
        this.gradingTime = gradingTime;
    }
}
