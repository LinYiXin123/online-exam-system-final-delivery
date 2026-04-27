package com.rabbiter.oes.vo;

import java.util.List;

/**
 * 考试详情VO - 用于答卷回顾
 */
public class ExamDetailVO {
    private QuestionListVO questions;

    public QuestionListVO getQuestions() {
        return questions;
    }

    public void setQuestions(QuestionListVO questions) {
        this.questions = questions;
    }

    public static class QuestionListVO {
        private List<ChoiceQuestionDetailVO> choice;
        private List<FillQuestionDetailVO> fill;
        private List<JudgeQuestionDetailVO> judge;
        private List<EssayQuestionDetailVO> essay;

        public List<ChoiceQuestionDetailVO> getChoice() {
            return choice;
        }

        public void setChoice(List<ChoiceQuestionDetailVO> choice) {
            this.choice = choice;
        }

        public List<FillQuestionDetailVO> getFill() {
            return fill;
        }

        public void setFill(List<FillQuestionDetailVO> fill) {
            this.fill = fill;
        }

        public List<JudgeQuestionDetailVO> getJudge() {
            return judge;
        }

        public void setJudge(List<JudgeQuestionDetailVO> judge) {
            this.judge = judge;
        }

        public List<EssayQuestionDetailVO> getEssay() {
            return essay;
        }

        public void setEssay(List<EssayQuestionDetailVO> essay) {
            this.essay = essay;
        }
    }

    public static class ChoiceQuestionDetailVO {
        private Integer questionNumber;
        private String question;
        private String answerA;
        private String answerB;
        private String answerC;
        private String answerD;
        private String rightAnswer;
        private String yourAnswer;
        private Boolean isCorrect;

        public Integer getQuestionNumber() {
            return questionNumber;
        }

        public void setQuestionNumber(Integer questionNumber) {
            this.questionNumber = questionNumber;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswerA() {
            return answerA;
        }

        public void setAnswerA(String answerA) {
            this.answerA = answerA;
        }

        public String getAnswerB() {
            return answerB;
        }

        public void setAnswerB(String answerB) {
            this.answerB = answerB;
        }

        public String getAnswerC() {
            return answerC;
        }

        public void setAnswerC(String answerC) {
            this.answerC = answerC;
        }

        public String getAnswerD() {
            return answerD;
        }

        public void setAnswerD(String answerD) {
            this.answerD = answerD;
        }

        public String getRightAnswer() {
            return rightAnswer;
        }

        public void setRightAnswer(String rightAnswer) {
            this.rightAnswer = rightAnswer;
        }

        public String getYourAnswer() {
            return yourAnswer;
        }

        public void setYourAnswer(String yourAnswer) {
            this.yourAnswer = yourAnswer;
        }

        public Boolean getIsCorrect() {
            return isCorrect;
        }

        public void setIsCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
        }
    }

    public static class FillQuestionDetailVO {
        private Integer questionNumber;
        private String question;
        private List<String> answer;
        private List<String> yourAnswer;
        private Boolean isCorrect;

        public Integer getQuestionNumber() {
            return questionNumber;
        }

        public void setQuestionNumber(Integer questionNumber) {
            this.questionNumber = questionNumber;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getAnswer() {
            return answer;
        }

        public void setAnswer(List<String> answer) {
            this.answer = answer;
        }

        public List<String> getYourAnswer() {
            return yourAnswer;
        }

        public void setYourAnswer(List<String> yourAnswer) {
            this.yourAnswer = yourAnswer;
        }

        public Boolean getIsCorrect() {
            return isCorrect;
        }

        public void setIsCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
        }
    }

    public static class JudgeQuestionDetailVO {
        private Integer questionNumber;
        private String question;
        private String answer;
        private Integer yourAnswer;
        private Boolean isCorrect;

        public Integer getQuestionNumber() {
            return questionNumber;
        }

        public void setQuestionNumber(Integer questionNumber) {
            this.questionNumber = questionNumber;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public Integer getYourAnswer() {
            return yourAnswer;
        }

        public void setYourAnswer(Integer yourAnswer) {
            this.yourAnswer = yourAnswer;
        }

        public Boolean getIsCorrect() {
            return isCorrect;
        }

        public void setIsCorrect(Boolean isCorrect) {
            this.isCorrect = isCorrect;
        }
    }

    public static class EssayQuestionDetailVO {
        private Integer questionNumber;
        private String question;
        private String referenceAnswer;
        private String yourAnswer;

        public Integer getQuestionNumber() {
            return questionNumber;
        }

        public void setQuestionNumber(Integer questionNumber) {
            this.questionNumber = questionNumber;
        }
        private Integer aiScore;
        private String aiComment;
        private Integer teacherScore;
        private String teacherComment;
        private Integer finalScore;
        private String gradingStatus;

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

        public String getYourAnswer() {
            return yourAnswer;
        }

        public void setYourAnswer(String yourAnswer) {
            this.yourAnswer = yourAnswer;
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
    }
}
