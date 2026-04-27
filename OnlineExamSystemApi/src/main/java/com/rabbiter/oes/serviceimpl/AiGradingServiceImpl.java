package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.AiChatRequest;
import com.rabbiter.oes.entity.EssayAnswer;
import com.rabbiter.oes.entity.EssayQuestion;
import com.rabbiter.oes.mapper.EssayAnswerMapper;
import com.rabbiter.oes.mapper.EssayQuestionMapper;
import com.rabbiter.oes.service.AiGradingService;
import com.rabbiter.oes.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI智能改卷Service实现类
 */
@Service
public class AiGradingServiceImpl implements AiGradingService {
    
    @Autowired
    private EssayAnswerMapper essayAnswerMapper;
    
    @Autowired
    private EssayQuestionMapper essayQuestionMapper;
    
    @Autowired
    private AiService aiService;
    
    @Override
    public Map<String, Object> gradeEssayAnswer(EssayAnswer answer, EssayQuestion question) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 构建AI评分提示词
            String prompt = buildGradingPrompt(question, answer.getStudentAnswer());
            
            // 构建AI请求对象
            AiChatRequest chatRequest = new AiChatRequest();
            chatRequest.setQuestion(prompt);
            
            // 调用AI服务获取评分
            String aiResponse = aiService.chat(chatRequest);
            
            // 解析AI返回的评分结果
            result = parseAiGradingResponse(aiResponse, question.getScore());
            
        } catch (Exception e) {
            // 如果AI评分失败，给出默认评分
            result.put("score", 0);
            result.put("comment", "AI评分异常，请教师手动评分");
        }
        
        return result;
    }
    
    /**
     * 统一判断题答案格式
     * 将各种格式的判断题答案统一为 "T" 或 "F"
     */
    private String normalizeJudgeAnswer(String answer) {
        if (answer == null) return "";
        String normalized = answer.trim().toUpperCase();
        // 统一为 T（正确）或 F（错误）
        if ("1".equals(normalized) || "T".equals(normalized) || "TRUE".equals(normalized) || "正确".equals(answer.trim())) {
            return "T";
        } else if ("0".equals(normalized) || "F".equals(normalized) || "FALSE".equals(normalized) || "错误".equals(answer.trim())) {
            return "F";
        }
        return normalized;
    }
    
    /**
     * 构建AI评分提示词
     */
    private String buildGradingPrompt(EssayQuestion question, String studentAnswer) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一位专业的考试评分老师，请根据以下信息对学生的答案进行评分：\n\n");
        prompt.append("【题目】\n").append(question.getQuestion()).append("\n\n");
        prompt.append("【参考答案】\n").append(question.getReferenceAnswer()).append("\n\n");
        prompt.append("【评分标准】\n").append(question.getScoringCriteria()).append("\n\n");
        prompt.append("【满分分值】").append(question.getScore()).append("分\n\n");
        prompt.append("【学生答案】\n").append(studentAnswer != null ? studentAnswer : "（未作答）").append("\n\n");
        prompt.append("请严格按照以下JSON格式返回评分结果，不要包含其他内容：\n");
        prompt.append("{\"score\": 分数(0-").append(question.getScore()).append("的整数), \"comment\": \"评语(说明得分理由和改进建议)\"}");
        return prompt.toString();
    }
    
    /**
     * 解析AI评分响应
     */
    private Map<String, Object> parseAiGradingResponse(String response, Integer maxScore) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 尝试提取JSON部分
            int startIndex = response.indexOf("{");
            int endIndex = response.lastIndexOf("}") + 1;
            
            if (startIndex >= 0 && endIndex > startIndex) {
                String jsonStr = response.substring(startIndex, endIndex);
                // 简单解析JSON
                int scoreStart = jsonStr.indexOf("\"score\"");
                int commentStart = jsonStr.indexOf("\"comment\"");
                
                if (scoreStart >= 0) {
                    // 提取分数
                    String scoreSection = jsonStr.substring(scoreStart);
                    int colonIndex = scoreSection.indexOf(":");
                    int commaIndex = scoreSection.indexOf(",");
                    if (commaIndex < 0) commaIndex = scoreSection.indexOf("}");
                    String scoreStr = scoreSection.substring(colonIndex + 1, commaIndex).trim();
                    int score = Integer.parseInt(scoreStr.replaceAll("[^0-9]", ""));
                    // 确保分数在有效范围内
                    score = Math.min(Math.max(score, 0), maxScore);
                    result.put("score", score);
                }
                
                if (commentStart >= 0) {
                    // 提取评语
                    String commentSection = jsonStr.substring(commentStart);
                    int firstQuote = commentSection.indexOf("\"", commentSection.indexOf(":"));
                    int lastQuote = commentSection.lastIndexOf("\"");
                    if (firstQuote >= 0 && lastQuote > firstQuote) {
                        String comment = commentSection.substring(firstQuote + 1, lastQuote);
                        result.put("comment", comment);
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败时使用默认值
        }
        
        // 确保有默认值
        if (!result.containsKey("score")) {
            result.put("score", 0);
        }
        if (!result.containsKey("comment")) {
            result.put("comment", "评分完成");
        }
        
        return result;
    }
    
    @Override
    public int batchGradeByExam(Integer examCode) {
        // 获取所有待评分的答案
        List<EssayAnswer> pendingAnswers = essayAnswerMapper.findPendingByExam(examCode);
        int gradedCount = 0;
        
        for (EssayAnswer answer : pendingAnswers) {
            // 获取题目信息
            EssayQuestion question = essayQuestionMapper.selectById(answer.getQuestionId());
            if (question == null) continue;
            
            // AI评分
            Map<String, Object> gradingResult = gradeEssayAnswer(answer, question);
            
            // 更新评分结果
            Integer aiScore = (Integer) gradingResult.get("score");
            String aiComment = (String) gradingResult.get("comment");
            
            essayAnswerMapper.updateAiGrading(answer.getAnswerId(), aiScore, aiComment);
            gradedCount++;
        }
        
        return gradedCount;
    }
    
    @Override
    public List<Map<String, Object>> getAnswersWithQuestionByExam(Integer examCode) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> answers = essayAnswerMapper.findAllByExamWithStudent(examCode);
        
        for (Map<String, Object> answer : answers) {
            Integer questionId = (Integer) answer.get("questionId");
            EssayQuestion question = essayQuestionMapper.selectById(questionId);
            
            Map<String, Object> item = new HashMap<>(answer);
            if (question != null) {
                item.put("questionContent", question.getQuestion());
                item.put("referenceAnswer", question.getReferenceAnswer());
                item.put("scoringCriteria", question.getScoringCriteria());
                item.put("maxScore", question.getScore());
            }
            results.add(item);
        }
        
        return results;
    }
    
    @Override
    public int confirmAiGrading(Integer answerId) {
        return essayAnswerMapper.confirmAiGrading(answerId);
    }
    
    @Override
    public int updateTeacherGrading(Integer answerId, Integer teacherScore, String teacherComment) {
        return essayAnswerMapper.updateTeacherGrading(answerId, teacherScore, teacherComment, teacherScore);
    }
    
    @Override
    public Map<String, Object> getGradingStats(Integer examCode) {
        Map<String, Object> stats = new HashMap<>();
        List<Map<String, Object>> statusCounts = essayAnswerMapper.countGradingStatus(examCode);
        
        int total = 0;
        int pending = 0;
        int aiGraded = 0;
        int teacherReviewed = 0;
        
        for (Map<String, Object> item : statusCounts) {
            String status = (String) item.get("gradingStatus");
            Long count = (Long) item.get("count");
            total += count;
            
            if ("pending".equals(status)) {
                pending = count.intValue();
            } else if ("ai_graded".equals(status)) {
                aiGraded = count.intValue();
            } else if ("teacher_reviewed".equals(status)) {
                teacherReviewed = count.intValue();
            }
        }
        
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("aiGraded", aiGraded);
        stats.put("teacherReviewed", teacherReviewed);
        stats.put("completionRate", total > 0 ? (teacherReviewed * 100 / total) : 0);
        
        return stats;
    }
    
    @Override
    public Integer calculateEssayTotalScore(Integer examCode, Long studentId) {
        List<EssayAnswer> answers = essayAnswerMapper.findByExamAndStudent(examCode, studentId);
        int totalScore = 0;
        
        for (EssayAnswer answer : answers) {
            Integer finalScore = answer.getFinalScore();
            if (finalScore != null) {
                totalScore += finalScore;
            } else if (answer.getAiScore() != null) {
                totalScore += answer.getAiScore();
            }
        }
        
        return totalScore;
    }
    
    @Override
    public List<Map<String, Object>> getStudentScoresByExam(Integer examCode) {
        // 从score表获取学生成绩，关联student表获取学生信息
        List<Map<String, Object>> students = essayAnswerMapper.getStudentScoresByExam(examCode);
        System.out.println("========== 获取学生成绩列表 ==========");
        System.out.println("examCode: " + examCode + ", 学生数量: " + students.size());
        for (Map<String, Object> s : students) {
            System.out.println("学生: studentId=" + s.get("studentId") + ", studentName=" + s.get("studentName") + ", major=" + s.get("major"));
        }
        
        // 为每个学生动态计算客观题分数
        for (Map<String, Object> student : students) {
            Long studentId = ((Number) student.get("studentId")).longValue();
            // 获取该学生的客观题答案并计算分数
            List<Map<String, Object>> answers = getObjectiveAnswers(examCode, studentId);
            int objectiveScore = 0;
            for (Map<String, Object> answer : answers) {
                objectiveScore += ((Number) answer.getOrDefault("score", 0)).intValue();
            }
            // 更新客观题分数和总分
            student.put("objectiveScore", objectiveScore);
            Integer subjectiveScore = student.get("subjectiveScore") != null ? 
                ((Number) student.get("subjectiveScore")).intValue() : 0;
            student.put("totalScore", objectiveScore + subjectiveScore);
        }
        
        return students;
    }
    
    @Override
    public List<Map<String, Object>> getObjectiveAnswers(Integer examCode, Long studentId) {
        // 获取学生答题记录
        List<Map<String, Object>> answers = essayAnswerMapper.getObjectiveAnswers(examCode, studentId);
        // 获取试卷所有题目（按试卷顺序排列）
        List<Map<String, Object>> allQuestions = essayAnswerMapper.getAllPaperQuestions(examCode);
        
        // 构建学生答案映射：(questionType, questionId) -> studentAnswer
        Map<String, String> studentAnswerMap = new HashMap<>();
        for (Map<String, Object> answer : answers) {
            Integer qType = ((Number) answer.get("questionType")).intValue();
            Integer qId = ((Number) answer.get("questionId")).intValue();
            String studentAnswer = (String) answer.get("studentAnswer");
            studentAnswerMap.put(qType + "_" + qId, studentAnswer);
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        int paperQuestionNumber = 0;
        
        // 遍历试卷中的所有题目，包括未作答的
        for (Map<String, Object> q : allQuestions) {
            paperQuestionNumber++;
            int qType = ((Number) q.get("questionType")).intValue();
            int qId = ((Number) q.get("questionId")).intValue();
            
            // 只处理客观题（1=选择, 2=填空, 3=判断）
            if (qType >= 1 && qType <= 3) {
                Map<String, Object> item = new HashMap<>();
                item.put("questionType", qType);
                item.put("questionId", paperQuestionNumber); // 试卷中的连续题号
                
                // 获取学生答案（可能为null表示未作答）
                String key = qType + "_" + qId;
                String studentAnswer = studentAnswerMap.get(key);
                item.put("studentAnswer", studentAnswer != null ? studentAnswer : "未作答");
                
                // 根据题型查询正确答案
                String correctAnswer = null;
                if (qType == 1) {
                    correctAnswer = essayAnswerMapper.getMultiQuestionAnswer(qId);
                } else if (qType == 2) {
                    correctAnswer = essayAnswerMapper.getFillQuestionAnswer(qId);
                } else if (qType == 3) {
                    correctAnswer = essayAnswerMapper.getJudgeQuestionAnswer(qId);
                }
                item.put("correctAnswer", correctAnswer);
                
                // 动态计算isCorrect（未作答视为错误）
                boolean isCorrect = false;
                if (studentAnswer != null && correctAnswer != null) {
                    if (qType == 3) {
                        // 判断题特殊处理：统一格式比较
                        // 学生答案可能是 "1"/"0"/"T"/"F"/"true"/"false"/"正确"/"错误"
                        // 正确答案可能是 "T"/"F"
                        String normalizedStudent = normalizeJudgeAnswer(studentAnswer);
                        String normalizedCorrect = normalizeJudgeAnswer(correctAnswer);
                        isCorrect = normalizedStudent.equals(normalizedCorrect);
                    } else {
                        isCorrect = correctAnswer.equals(studentAnswer);
                    }
                }
                item.put("isCorrect", isCorrect ? 1 : 0);
                // 重新计算得分
                int score = isCorrect ? 2 : 0;
                item.put("score", score);
                
                result.add(item);
            }
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getSubjectiveAnswers(Integer examCode, Long studentId) {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // 获取试卷中主观题的顺序（questionType=4）
        List<Map<String, Object>> allQuestions = essayAnswerMapper.getAllPaperQuestions(examCode);
        List<EssayAnswer> answers = essayAnswerMapper.findByExamAndStudent(examCode, studentId);
        
        // 构建答案映射
        Map<Integer, EssayAnswer> answerMap = new HashMap<>();
        for (EssayAnswer a : answers) {
            answerMap.put(a.getQuestionId(), a);
        }
        
        // 按试卷顺序遍历主观题
        int questionNumber = 0;
        int essayIndex = 0;
        for (Map<String, Object> q : allQuestions) {
            questionNumber++;
            int questionType = ((Number) q.get("questionType")).intValue();
            int questionId = ((Number) q.get("questionId")).intValue();
            
            if (questionType == 4) {
                essayIndex++;
                EssayQuestion question = essayQuestionMapper.selectById(questionId);
                EssayAnswer answer = answerMap.get(questionId);
                
                Map<String, Object> item = new HashMap<>();
                item.put("paperQuestionNumber", questionNumber); // 试卷中的题号
                item.put("essayIndex", essayIndex); // 主观题内的序号
                item.put("questionId", questionId);
                
                if (answer != null) {
                    item.put("answerId", answer.getAnswerId());
                    item.put("studentAnswer", answer.getStudentAnswer());
                    item.put("aiScore", answer.getAiScore());
                    item.put("aiComment", answer.getAiComment());
                    item.put("teacherScore", answer.getTeacherScore());
                    item.put("teacherComment", answer.getTeacherComment());
                    item.put("finalScore", answer.getFinalScore());
                    item.put("gradingStatus", answer.getGradingStatus());
                } else {
                    item.put("studentAnswer", null);
                    item.put("gradingStatus", "pending");
                }
                
                if (question != null) {
                    item.put("questionContent", question.getQuestion());
                    item.put("referenceAnswer", question.getReferenceAnswer());
                    item.put("scoringCriteria", question.getScoringCriteria());
                    item.put("maxScore", question.getScore());
                }
                results.add(item);
            }
        }
        return results;
    }
    
    @Override
    public Map<String, Object> autoGradeAll(Integer examCode) {
        Map<String, Object> result = new HashMap<>();
        int objectiveCount = 0;
        int subjectiveCount = 0;
        
        // 1. 批量评分主观题
        subjectiveCount = batchGradeByExam(examCode);
        
        // 2. 客观题自动判分（更新score表）
        objectiveCount = essayAnswerMapper.autoGradeObjective(examCode);
        
        result.put("objectiveCount", objectiveCount);
        result.put("subjectiveCount", subjectiveCount);
        return result;
    }
    
    @Override
    public int gradeStudentEssay(Integer examCode, Long studentId) {
        List<EssayAnswer> pendingAnswers = essayAnswerMapper.findPendingByExamAndStudent(examCode, studentId);
        int gradedCount = 0;
        
        for (EssayAnswer answer : pendingAnswers) {
            EssayQuestion question = essayQuestionMapper.selectById(answer.getQuestionId());
            if (question == null) continue;
            
            Map<String, Object> gradingResult = gradeEssayAnswer(answer, question);
            Integer aiScore = (Integer) gradingResult.get("score");
            String aiComment = (String) gradingResult.get("comment");
            
            essayAnswerMapper.updateAiGrading(answer.getAnswerId(), aiScore, aiComment);
            gradedCount++;
        }
        
        return gradedCount;
    }
    
    @Override
    public Map<String, Object> calculateAndUpdateTotalScore(Integer examCode, Long studentId) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算主观题总分
        Integer subjectiveScore = calculateEssayTotalScore(examCode, studentId);
        
        // 更新score表中的ptScore和总分
        int updated = essayAnswerMapper.updateStudentScore(examCode, studentId, subjectiveScore);
        
        result.put("subjectiveScore", subjectiveScore);
        result.put("updated", updated > 0);
        return result;
    }
    
    @Override
    public Map<String, Object> calculateAndUpdateTotalScore(Integer examCode, Long studentId, Integer objectiveScore, Integer subjectiveScore) {
        Map<String, Object> result = new HashMap<>();
        
        // 如果没有传递分数，使用计算的值
        if (subjectiveScore == null) {
            subjectiveScore = calculateEssayTotalScore(examCode, studentId);
        }
        
        // 更新score表中的etScore、ptScore
        int updated = essayAnswerMapper.updateStudentScoreWithObjective(examCode, studentId, objectiveScore, subjectiveScore);
        
        result.put("objectiveScore", objectiveScore);
        result.put("subjectiveScore", subjectiveScore);
        result.put("updated", updated > 0);
        return result;
    }
}
