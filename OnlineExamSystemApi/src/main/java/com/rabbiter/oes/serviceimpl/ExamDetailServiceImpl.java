package com.rabbiter.oes.serviceimpl;

import com.alibaba.fastjson.JSON;
import com.rabbiter.oes.entity.StudentAnswer;
import com.rabbiter.oes.entity.EssayAnswer;
import com.rabbiter.oes.entity.EssayQuestion;
import com.rabbiter.oes.mapper.MultiQuestionMapper;
import com.rabbiter.oes.mapper.FillQuestionMapper;
import com.rabbiter.oes.mapper.JudgeQuestionMapper;
import com.rabbiter.oes.mapper.StudentAnswerMapper;
import com.rabbiter.oes.mapper.EssayAnswerMapper;
import com.rabbiter.oes.mapper.EssayQuestionMapper;
import com.rabbiter.oes.entity.MultiQuestion;
import com.rabbiter.oes.entity.FillQuestion;
import com.rabbiter.oes.entity.JudgeQuestion;
import com.rabbiter.oes.vo.ExamDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExamDetailServiceImpl {

    @Autowired
    private MultiQuestionMapper multiQuestionMapper;

    @Autowired
    private FillQuestionMapper fillQuestionMapper;

    @Autowired
    private JudgeQuestionMapper judgeQuestionMapper;
    
    @Autowired
    private StudentAnswerMapper studentAnswerMapper;
    
    @Autowired
    private EssayAnswerMapper essayAnswerMapper;
    
    @Autowired
    private EssayQuestionMapper essayQuestionMapper;

    /**
     * 获取答题详情 - 返回试卷所有题目（包括未作答的）
     * @param examCode 考试编号
     * @param studentId 学生ID
     * @return 答题详情
     */
    public ExamDetailVO getExamDetail(Integer examCode, Integer studentId) {
        ExamDetailVO examDetailVO = new ExamDetailVO();
        ExamDetailVO.QuestionListVO questionListVO = new ExamDetailVO.QuestionListVO();

        // 获取试卷所有题目（按试卷顺序）
        List<Map<String, Object>> allQuestions = essayAnswerMapper.getAllPaperQuestions(examCode);
        System.out.println("========== ExamDetailServiceImpl Debug ==========");
        System.out.println("examCode: " + examCode + ", studentId: " + studentId);
        System.out.println("试卷总题数: " + (allQuestions != null ? allQuestions.size() : 0));

        // 查询学生答题记录，构建Map方便查找
        List<StudentAnswer> answerRecords = studentAnswerMapper.selectByExamAndStudent(examCode, Long.valueOf(studentId));
        Map<String, StudentAnswer> answerMap = new HashMap<>();
        if (answerRecords != null) {
            for (StudentAnswer record : answerRecords) {
                String key = record.getQuestionType() + "_" + record.getQuestionId();
                answerMap.put(key, record);
            }
        }

        // 查询主观题答案，构建Map
        List<EssayAnswer> essayAnswers = essayAnswerMapper.findByExamAndStudent(examCode, Long.valueOf(studentId));
        Map<Integer, EssayAnswer> essayAnswerMap = new HashMap<>();
        if (essayAnswers != null) {
            for (EssayAnswer ea : essayAnswers) {
                essayAnswerMap.put(ea.getQuestionId(), ea);
            }
        }

        List<ExamDetailVO.ChoiceQuestionDetailVO> choiceList = new ArrayList<>();
        List<ExamDetailVO.FillQuestionDetailVO> fillList = new ArrayList<>();
        List<ExamDetailVO.JudgeQuestionDetailVO> judgeList = new ArrayList<>();
        List<ExamDetailVO.EssayQuestionDetailVO> essayList = new ArrayList<>();

        int questionNumber = 0;
        for (Map<String, Object> q : allQuestions) {
            questionNumber++;
            int questionType = ((Number) q.get("questionType")).intValue();
            int questionId = ((Number) q.get("questionId")).intValue();
            String answerKey = questionType + "_" + questionId;
            StudentAnswer studentAnswer = answerMap.get(answerKey);

            if (questionType == 1) {
                // 选择题
                MultiQuestion question = multiQuestionMapper.findById(questionId);
                if (question != null) {
                    ExamDetailVO.ChoiceQuestionDetailVO vo = new ExamDetailVO.ChoiceQuestionDetailVO();
                    vo.setQuestionNumber(questionNumber);
                    vo.setQuestion(question.getQuestion());
                    vo.setAnswerA(question.getAnswerA());
                    vo.setAnswerB(question.getAnswerB());
                    vo.setAnswerC(question.getAnswerC());
                    vo.setAnswerD(question.getAnswerD());
                    vo.setRightAnswer(question.getRightAnswer());
                    if (studentAnswer != null) {
                        vo.setYourAnswer(studentAnswer.getStudentAnswer());
                        vo.setIsCorrect(studentAnswer.getIsCorrect() != null && studentAnswer.getIsCorrect() == 1);
                    } else {
                        vo.setYourAnswer(null);
                        vo.setIsCorrect(false);
                    }
                    choiceList.add(vo);
                }
            } else if (questionType == 2) {
                // 填空题
                FillQuestion question = fillQuestionMapper.findById(questionId);
                if (question != null) {
                    ExamDetailVO.FillQuestionDetailVO vo = new ExamDetailVO.FillQuestionDetailVO();
                    vo.setQuestionNumber(questionNumber);
                    vo.setQuestion(question.getQuestion());
                    List<String> answers = new ArrayList<>();
                    String[] answerArr = question.getAnswer().split(" ");
                    for (String ans : answerArr) {
                        answers.add(ans.trim());
                    }
                    vo.setAnswer(answers);
                    if (studentAnswer != null && studentAnswer.getStudentAnswer() != null) {
                        // 填空题答案可能是逗号分隔的字符串
                        String[] yourAnswerArr = studentAnswer.getStudentAnswer().split(",");
                        List<String> yourAnswers = new ArrayList<>();
                        for (String ans : yourAnswerArr) {
                            yourAnswers.add(ans.trim());
                        }
                        vo.setYourAnswer(yourAnswers);
                        vo.setIsCorrect(studentAnswer.getIsCorrect() != null && studentAnswer.getIsCorrect() == 1);
                    } else {
                        vo.setYourAnswer(new ArrayList<>());
                        vo.setIsCorrect(false);
                    }
                    fillList.add(vo);
                }
            } else if (questionType == 3) {
                // 判断题
                JudgeQuestion question = judgeQuestionMapper.findById(questionId);
                if (question != null) {
                    ExamDetailVO.JudgeQuestionDetailVO vo = new ExamDetailVO.JudgeQuestionDetailVO();
                    vo.setQuestionNumber(questionNumber);
                    vo.setQuestion(question.getQuestion());
                    vo.setAnswer(question.getAnswer());
                    if (studentAnswer != null) {
                        try {
                            vo.setYourAnswer(Integer.parseInt(studentAnswer.getStudentAnswer()));
                        } catch (Exception e) {
                            vo.setYourAnswer(0);
                        }
                        vo.setIsCorrect(studentAnswer.getIsCorrect() != null && studentAnswer.getIsCorrect() == 1);
                    } else {
                        vo.setYourAnswer(0);
                        vo.setIsCorrect(false);
                    }
                    judgeList.add(vo);
                }
            } else if (questionType == 4) {
                // 主观题
                EssayQuestion question = essayQuestionMapper.selectById(questionId);
                if (question != null) {
                    ExamDetailVO.EssayQuestionDetailVO vo = new ExamDetailVO.EssayQuestionDetailVO();
                    vo.setQuestionNumber(questionNumber);
                    vo.setQuestion(question.getQuestion());
                    vo.setReferenceAnswer(question.getReferenceAnswer());
                    EssayAnswer ea = essayAnswerMap.get(questionId);
                    if (ea != null) {
                        vo.setYourAnswer(ea.getStudentAnswer());
                        vo.setAiScore(ea.getAiScore());
                        vo.setAiComment(ea.getAiComment());
                        vo.setTeacherScore(ea.getTeacherScore());
                        vo.setTeacherComment(ea.getTeacherComment());
                        vo.setFinalScore(ea.getFinalScore());
                        vo.setGradingStatus(ea.getGradingStatus());
                    } else {
                        vo.setYourAnswer(null);
                        vo.setGradingStatus("pending");
                    }
                    essayList.add(vo);
                }
            }
        }

        questionListVO.setChoice(choiceList);
        questionListVO.setFill(fillList);
        questionListVO.setJudge(judgeList);
        questionListVO.setEssay(essayList);
        examDetailVO.setQuestions(questionListVO);

        return examDetailVO;
    }
}
