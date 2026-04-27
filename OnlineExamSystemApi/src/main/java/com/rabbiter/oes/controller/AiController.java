package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.serviceimpl.AiServiceImpl;
import com.rabbiter.oes.serviceimpl.EssayQuestionServiceImpl;
import com.rabbiter.oes.serviceimpl.FillQuestionServiceImpl;
import com.rabbiter.oes.serviceimpl.JudgeQuestionServiceImpl;
import com.rabbiter.oes.serviceimpl.MultiQuestionServiceImpl;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI聊天控制器
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiServiceImpl aiService;

    @Autowired
    private MultiQuestionServiceImpl multiQuestionService;

    @Autowired
    private FillQuestionServiceImpl fillQuestionService;

    @Autowired
    private JudgeQuestionServiceImpl judgeQuestionService;

    @Autowired
    private EssayQuestionServiceImpl essayQuestionService;

    @Autowired
    private AccessGuard accessGuard;

    /**
     * AI聊天接口
     * @param request 聊天请求
     * @return AI回复
     */
    @PostMapping("/chat")
    public ApiResult chat(@RequestBody AiChatRequest request) {
        try {
            // 验证请求参数
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "问题不能为空", null);
            }

            // 调用AI服务
            String answer = aiService.chat(request);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("answer", answer);
            result.put("question", request.getQuestion());
            
            return ApiResultHandler.buildApiResult(200, "成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "AI服务异常: " + e.getMessage(), null);
        }
    }

    /**
     * 简单测试接口
     */
    @GetMapping("/ping")
    public String ping() {
        System.out.println("===== PING接口被调用 =====");
        return "AI Controller is working!";
    }

    /**
     * 测试AI服务是否可用
     * @return 测试结果
     */
    @GetMapping("/test")
    public ApiResult test() {
        System.out.println("===== AI测试接口被调用 =====");
        try {
            System.out.println("开始创建测试请求...");
            AiChatRequest testRequest = new AiChatRequest();
            testRequest.setQuestion("你好，请简单介绍一下你自己。");
            
            System.out.println("开始调用AI服务...");
            String answer = aiService.chat(testRequest);
            System.out.println("AI服务调用成功，答案: " + answer);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("answer", answer);
            
            ApiResult apiResult = ApiResultHandler.buildApiResult(200, "AI服务正常", result);
            System.out.println("返回结果: " + apiResult);
            return apiResult;
        } catch (Exception e) {
            System.err.println("AI测试接口异常: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("error", e.getMessage());
            
            return ApiResultHandler.buildApiResult(500, "AI服务异常: " + e.getMessage(), result);
        }
    }

    /**
     * AI生成题目接口
     */
    @PostMapping("/generate")
    public ApiResult generateQuestions(@RequestBody AiGenerateQuestionRequest request, HttpServletRequest httpRequest) {
        if (!accessGuard.isTeacherOrAdmin(httpRequest)) {
            return accessGuard.forbidden("只有教师或管理员可以使用AI生成题目");
        }
        try {
            // 验证参数
            if (request.getSubject() == null || request.getSubject().trim().isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "科目不能为空", null);
            }
            if (request.getQuestionType() == null || request.getQuestionType().trim().isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "题型不能为空", null);
            }
            if (request.getCount() == null || request.getCount() <= 0) {
                return ApiResultHandler.buildApiResult(400, "生成数量必须大于0", null);
            }

            Map<String, Object> result = aiService.generateQuestions(request);
            return ApiResultHandler.buildApiResult(200, "生成成功", result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "AI生成题目失败: " + e.getMessage(), null);
        }
    }

    /**
     * 保存AI生成的题目到题库
     */
    @PostMapping("/saveQuestions")
    public ApiResult saveQuestions(@RequestBody Map<String, Object> requestData, HttpServletRequest httpRequest) {
        if (!accessGuard.isTeacherOrAdmin(httpRequest)) {
            return accessGuard.forbidden("只有教师或管理员可以保存AI生成题目");
        }
        try {
            System.out.println("=== 保存题目请求 ===");
            System.out.println("接收到的数据：" + requestData);
            String questionType = (String) requestData.get("questionType");
            System.out.println("题型：" + questionType);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) requestData.get("questions");
            System.out.println("题目数量：" + (questions != null ? questions.size() : 0));

            if (questions == null || questions.isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "没有要保存的题目", null);
            }

            int savedCount = 0;

            for (Map<String, Object> q : questions) {
                try {
                    switch (questionType) {
                        case "multi":
                            MultiQuestion multi = new MultiQuestion();
                            multi.setSubject((String) q.get("subject"));
                            multi.setQuestion((String) q.get("question"));
                            multi.setAnswerA((String) q.get("answerA"));
                            multi.setAnswerB((String) q.get("answerB"));
                            multi.setAnswerC((String) q.get("answerC"));
                            multi.setAnswerD((String) q.get("answerD"));
                            multi.setRightAnswer((String) q.get("rightAnswer"));
                            multi.setAnalysis((String) q.get("analysis"));
                            multi.setSection((String) q.get("section"));
                            multi.setLevel((String) q.get("level"));
                            multiQuestionService.add(multi);
                            savedCount++;
                            break;

                        case "fill":
                            FillQuestion fill = new FillQuestion();
                            fill.setSubject((String) q.get("subject"));
                            fill.setQuestion((String) q.get("question"));
                            fill.setAnswer((String) q.get("answer"));
                            fill.setAnalysis((String) q.get("analysis"));
                            fill.setSection((String) q.get("section"));
                            fill.setLevel((String) q.get("level"));
                            fillQuestionService.add(fill);
                            savedCount++;
                            break;

                        case "judge":
                            JudgeQuestion judge = new JudgeQuestion();
                            judge.setSubject((String) q.get("subject"));
                            judge.setQuestion((String) q.get("question"));
                            judge.setAnswer((String) q.get("answer"));
                            judge.setAnalysis((String) q.get("analysis"));
                            judge.setSection((String) q.get("section"));
                            judge.setLevel((String) q.get("level"));
                            judgeQuestionService.add(judge);
                            savedCount++;
                            break;

                        case "essay":
                            System.out.println("保存主观题，数据：" + q);
                            EssayQuestion essay = new EssayQuestion();
                            essay.setSubject((String) q.get("subject"));
                            essay.setQuestion((String) q.get("question"));
                            essay.setReferenceAnswer((String) q.get("referenceAnswer"));
                            essay.setScoringCriteria((String) q.get("scoringCriteria"));
                            essay.setSection((String) q.get("section"));
                            essay.setLevel((String) q.get("level"));
                            Object scoreObj = q.get("score");
                            if (scoreObj instanceof Number) {
                                essay.setScore(((Number) scoreObj).intValue());
                            } else {
                                essay.setScore(10);
                            }
                            try {
                                essayQuestionService.add(essay);
                                savedCount++;
                                System.out.println("主观题保存成功");
                            } catch (Exception ex) {
                                System.err.println("主观题保存失败：" + ex.getMessage());
                                ex.printStackTrace();
                            }
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("保存题目失败: " + e.getMessage());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("savedCount", savedCount);
            result.put("totalCount", questions.size());

            return ApiResultHandler.buildApiResult(200,
                    String.format("成功保存%d道题目到题库", savedCount), result);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "保存题目失败: " + e.getMessage(), null);
        }
    }
}
