package com.rabbiter.oes.entity;

/**
 * 学生答题记录实体类
 */
public class StudentAnswer {
    private Integer recordId;
    private Integer examCode;
    private Long studentId;
    private Integer questionId;
    private Integer questionType;
    private String studentAnswer;
    private Integer isCorrect;  // 1-正确, 0-错误

    public Integer getId() {
        return recordId;
    }

    public void setId(Integer id) {
        this.recordId = id;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
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

    public Integer getQuestionType() {
        return questionType;
    }

    public void setQuestionType(Integer questionType) {
        this.questionType = questionType;
    }

    public String getStudentAnswer() {
        return studentAnswer;
    }

    public void setStudentAnswer(String studentAnswer) {
        this.studentAnswer = studentAnswer;
    }

    public Integer getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Integer isCorrect) {
        this.isCorrect = isCorrect;
    }

    @Override
    public String toString() {
        return "StudentAnswer{" +
                "recordId=" + recordId +
                ", examCode=" + examCode +
                ", studentId=" + studentId +
                ", questionId=" + questionId +
                ", questionType='" + questionType + '\'' +
                ", studentAnswer='" + studentAnswer + '\'' +
                ", isCorrect=" + isCorrect +
                '}';
    }
}
