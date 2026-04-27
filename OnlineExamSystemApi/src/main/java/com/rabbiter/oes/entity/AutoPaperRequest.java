package com.rabbiter.oes.entity;

/**
 * 自动组卷请求参数实体类
 */
public class AutoPaperRequest {

    private Integer paperId;           // 试卷ID（用于更新已有试卷）
    private String subject;            // 科目名称
    private String description;        // 考试介绍
    private String examType;           // 试卷类型
    private String examDate;           // 考试日期
    private Integer totalTime;         // 持续时长
    private String grade;              // 年级
    private String term;               // 学期
    private String major;              // 专业
    private String institute;          // 学院
    private String tips;               // 考生须知
    private Integer multiCount;        // 选择题数量
    private Integer fillCount;         // 填空题数量
    private Integer judgeCount;        // 判断题数量
    private Integer multiScore;        // 每道选择题分数
    private Integer fillScore;         // 每道填空题分数
    private Integer judgeScore;        // 每道判断题分数
    private Integer essayCount;        // 主观题数量
    private Integer essayScore;        // 每道主观题分数
    private String difficulty;         // 难度要求：easy(简单), medium(中等), hard(困难), mixed(混合)
    private String section;            // 知识点/章节（可选）

    public Integer getPaperId() {
        return paperId;
    }

    public void setPaperId(Integer paperId) {
        this.paperId = paperId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public Integer getMultiCount() {
        return multiCount;
    }

    public void setMultiCount(Integer multiCount) {
        this.multiCount = multiCount;
    }

    public Integer getFillCount() {
        return fillCount;
    }

    public void setFillCount(Integer fillCount) {
        this.fillCount = fillCount;
    }

    public Integer getJudgeCount() {
        return judgeCount;
    }

    public void setJudgeCount(Integer judgeCount) {
        this.judgeCount = judgeCount;
    }

    public Integer getMultiScore() {
        return multiScore;
    }

    public void setMultiScore(Integer multiScore) {
        this.multiScore = multiScore;
    }

    public Integer getFillScore() {
        return fillScore;
    }

    public void setFillScore(Integer fillScore) {
        this.fillScore = fillScore;
    }

    public Integer getJudgeScore() {
        return judgeScore;
    }

    public void setJudgeScore(Integer judgeScore) {
        this.judgeScore = judgeScore;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public Integer getEssayCount() {
        return essayCount;
    }

    public void setEssayCount(Integer essayCount) {
        this.essayCount = essayCount;
    }

    public Integer getEssayScore() {
        return essayScore;
    }

    public void setEssayScore(Integer essayScore) {
        this.essayScore = essayScore;
    }

    @Override
    public String toString() {
        return "AutoPaperRequest{" +
                "subject='" + subject + '\'' +
                ", description='" + description + '\'' +
                ", examType='" + examType + '\'' +
                ", examDate='" + examDate + '\'' +
                ", totalTime=" + totalTime +
                ", grade='" + grade + '\'' +
                ", term='" + term + '\'' +
                ", major='" + major + '\'' +
                ", institute='" + institute + '\'' +
                ", tips='" + tips + '\'' +
                ", multiCount=" + multiCount +
                ", fillCount=" + fillCount +
                ", judgeCount=" + judgeCount +
                ", essayCount=" + essayCount +
                ", multiScore=" + multiScore +
                ", fillScore=" + fillScore +
                ", judgeScore=" + judgeScore +
                ", essayScore=" + essayScore +
                ", difficulty='" + difficulty + '\'' +
                ", section='" + section + '\'' +
                '}';
    }
}
