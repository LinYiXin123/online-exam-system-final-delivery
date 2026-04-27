package com.rabbiter.oes.entity;

/**
 * AI生成题目请求实体类
 */
public class AiGenerateQuestionRequest {

    private String subject;           // 科目名称
    private String section;           // 章节/知识点
    private String questionType;      // 题型：multi(选择题), fill(填空题), judge(判断题)
    private Integer count;            // 生成数量
    private String difficulty;        // 难度：简单, 中等, 困难
    private String keywords;          // 关键词（可选）

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "AiGenerateQuestionRequest{" +
                "subject='" + subject + '\'' +
                ", section='" + section + '\'' +
                ", questionType='" + questionType + '\'' +
                ", count=" + count +
                ", difficulty='" + difficulty + '\'' +
                ", keywords='" + keywords + '\'' +
                '}';
    }
}
