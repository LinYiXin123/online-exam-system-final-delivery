package com.rabbiter.oes.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 简答题/主观题实体类
 */
@TableName("essay_question")
public class EssayQuestion {
    
    @TableId(value = "questionId", type = IdType.AUTO)
    private Integer questionId;
    
    @TableField("subject")
    private String subject;      // 科目
    
    @TableField("question")
    private String question;     // 题目内容
    
    @TableField("referenceAnswer")
    private String referenceAnswer;  // 参考答案
    
    @TableField("scoringCriteria")
    private String scoringCriteria;  // 评分标准/要点
    
    @TableField("score")
    private Integer score;       // 分值
    
    @TableField("section")
    private String section;      // 章节/知识点
    
    @TableField("level")
    private String level;        // 难度：简单/中等/困难

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReferenceAnswer() {
        return referenceAnswer;
    }

    public void setReferenceAnswer(String referenceAnswer) {
        this.referenceAnswer = referenceAnswer;
    }

    public String getScoringCriteria() {
        return scoringCriteria;
    }

    public void setScoringCriteria(String scoringCriteria) {
        this.scoringCriteria = scoringCriteria;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
