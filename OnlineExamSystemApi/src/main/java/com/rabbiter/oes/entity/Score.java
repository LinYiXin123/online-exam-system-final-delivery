package com.rabbiter.oes.entity;

public class Score {
    private Integer examCode;

    private Long studentId;

    private String subject;

    private Integer ptScore;

    private Integer etScore;

    private Integer score;

    private Integer scoreId;

    private String answerDate;

    private String scoreType; // exam-考试, practice-练习

    private String description; // 试卷介绍
    
    private Integer gradingCompleted; // 批改完成状态：0-待批改，1-已完成
    
    private Integer subjectiveScore; // 主观题得分
    
    private Integer totalScore; // 试卷总分

    public Integer getSubjectiveScore() {
        return subjectiveScore;
    }

    public void setSubjectiveScore(Integer subjectiveScore) {
        this.subjectiveScore = subjectiveScore;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getGradingCompleted() {
        return gradingCompleted;
    }

    public void setGradingCompleted(Integer gradingCompleted) {
        this.gradingCompleted = gradingCompleted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Integer getPtScore() {
        return ptScore;
    }

    public void setPtScore(Integer ptScore) {
        this.ptScore = ptScore;
    }

    public Integer getEtScore() {
        return etScore;
    }

    public void setEtScore(Integer etScore) {
        this.etScore = etScore;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getScoreId() {
        return scoreId;
    }

    public void setScoreId(Integer scoreId) {
        this.scoreId = scoreId;
    }

    public String getAnswerDate() {
        return answerDate;
    }

    public void setAnswerDate(String answerDate) {
        this.answerDate = answerDate;
    }

    public String getScoreType() {
        return scoreType;
    }

    public void setScoreType(String scoreType) {
        this.scoreType = scoreType;
    }

    @Override
    public String toString() {
        return "Score{" +
                "examCode=" + examCode +
                ", studentId=" + studentId +
                ", subject='" + subject + '\'' +
                ", ptScore=" + ptScore +
                ", etScore=" + etScore +
                ", score=" + score +
                ", scoreId=" + scoreId +
                ", answerDate='" + answerDate + '\'' +
                ", scoreType='" + scoreType + '\'' +
                '}';
    }
}

