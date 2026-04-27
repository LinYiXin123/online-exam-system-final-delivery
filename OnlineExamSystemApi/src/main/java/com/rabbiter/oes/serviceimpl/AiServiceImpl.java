package com.rabbiter.oes.serviceimpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbiter.oes.entity.AiChatRequest;
import com.rabbiter.oes.entity.AiGenerateQuestionRequest;
import com.rabbiter.oes.service.AiService;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI服务实现类
 * 支持OpenAI兼容接口（OpenAI、通义千问、Moonshot等）
 */
@Service
public class AiServiceImpl implements AiService {

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.max.tokens:1000}")
    private Integer maxTokens;

    @Value("${ai.temperature:0.7}")
    private Double temperature;


    @Override
    public String chat(AiChatRequest request) throws Exception {
        // 配置超时时间（DeepSeek-R1思考时间较长，需要更长超时）
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)      // 连接超时：30秒
                .setSocketTimeout(180000)      // 读取超时：180秒（3分钟）
                .setConnectionRequestTimeout(30000)
                .build();

        // 构建系统提示词（根据科目动态调整）
        StringBuilder systemPrompt = new StringBuilder();
        systemPrompt.append("你是一个专业的学习助手，帮助学生理解和解答学习中遇到的问题。");
        if (request.getSubject() != null && !request.getSubject().isEmpty()) {
            systemPrompt.append("当前科目是：").append(request.getSubject()).append("。");
            systemPrompt.append("请基于该科目的知识体系回答问题。");
        }
        systemPrompt.append("请用简洁清晰的语言回答问题，并提供解题思路和相关知识点。");

        // 构建用户消息
        StringBuilder userMessage = new StringBuilder();
        if (request.getSubject() != null && !request.getSubject().isEmpty()) {
            userMessage.append("【科目：").append(request.getSubject()).append("】\n\n");
        }
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            userMessage.append("题目：").append(request.getContext()).append("\n\n");
        }
        userMessage.append("问题：").append(request.getQuestion());

        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);
        
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        
        // 如果有图片，使用数组格式的content（支持Vision API）
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            JSONArray contentArray = new JSONArray();
            
            // 添加文本部分
            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            textPart.put("text", userMessage.toString());
            contentArray.add(textPart);
            
            // 添加图片部分
            for (Object imgObj : request.getImages()) {
                if (imgObj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, String> img = (java.util.Map<String, String>) imgObj;
                    JSONObject imagePart = new JSONObject();
                    imagePart.put("type", "image_url");
                    
                    JSONObject imageUrl = new JSONObject();
                    imageUrl.put("url", img.get("url"));  // base64 data URL
                    imagePart.put("image_url", imageUrl);
                    
                    contentArray.add(imagePart);
                }
            }
            
            userMsg.put("content", contentArray);
        } else {
            // 没有图片，使用简单的字符串格式
            userMsg.put("content", userMessage.toString());
        }
        
        messages.add(userMsg);
        
        requestBody.put("messages", messages);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Authorization", "Bearer " + apiKey);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(requestBody.toJSONString(), StandardCharsets.UTF_8));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            
            System.out.println("AI API响应状态码: " + statusCode);
            System.out.println("AI API响应内容: " + responseBody);
            
            if (statusCode != 200) {
                throw new IOException("AI API请求失败: " + statusCode + " " + response.getStatusLine().getReasonPhrase() + " 响应: " + responseBody);
            }

            JSONObject jsonResponse = JSON.parseObject(responseBody);
            
            // 解析OpenAI格式的响应
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                String content = message.getString("content");
                if (content != null && !content.isEmpty()) {
                    return content;
                }
            }
            
            throw new Exception("AI响应格式错误或内容为空: " + responseBody);
        } catch (Exception e) {
            System.err.println("AI服务调用异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * AI生成题目
     */
    @Override
    public Map<String, Object> generateQuestions(AiGenerateQuestionRequest request) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> questions = new ArrayList<>();

        // 构建提示词
        String prompt = buildGeneratePrompt(request);

        // 调用AI
        AiChatRequest chatRequest = new AiChatRequest();
        chatRequest.setQuestion(prompt);
        chatRequest.setSubject(request.getSubject());

        String aiResponse = chat(chatRequest);

        // 解析AI返回的题目
        questions = parseQuestions(aiResponse, request.getQuestionType(), request.getSubject(),
                                   request.getSection(), request.getDifficulty());

        result.put("success", true);
        result.put("questions", questions);
        result.put("count", questions.size());
        result.put("rawResponse", aiResponse);
        
        // 调试日志
        System.out.println("=== AI生成题目调试 ===");
        System.out.println("题型: " + request.getQuestionType());
        System.out.println("AI响应长度: " + (aiResponse != null ? aiResponse.length() : 0));
        System.out.println("解析出题目数: " + questions.size());
        if (questions.isEmpty() && aiResponse != null) {
            System.out.println("AI原始响应(前500字符): " + aiResponse.substring(0, Math.min(500, aiResponse.length())));
        }

        return result;
    }

    /**
     * 构建生成题目的提示词
     */
    private String buildGeneratePrompt(AiGenerateQuestionRequest request) {
        StringBuilder prompt = new StringBuilder();

        String typeName = "";
        String formatExample = "";

        switch (request.getQuestionType()) {
            case "multi":
                typeName = "选择题";
                formatExample = "格式要求（每道题严格按此格式，用---分隔每道题）：\n" +
                        "题目：[题目内容]\n" +
                        "A. [选项A]\n" +
                        "B. [选项B]\n" +
                        "C. [选项C]\n" +
                        "D. [选项D]\n" +
                        "答案：[A/B/C/D]\n" +
                        "解析：[解析内容]\n" +
                        "---";
                break;
            case "fill":
                typeName = "填空题";
                formatExample = "格式要求（每道题严格按此格式，用---分隔每道题）：\n" +
                        "题目：[题目内容，用____表示填空位置]\n" +
                        "答案：[正确答案]\n" +
                        "解析：[解析内容]\n" +
                        "---";
                break;
            case "judge":
                typeName = "判断题";
                formatExample = "格式要求（每道题严格按此格式，用---分隔每道题）：\n" +
                        "题目：[题目内容]\n" +
                        "答案：[T/F]（T表示正确，F表示错误）\n" +
                        "解析：[解析内容]\n" +
                        "---";
                break;
            case "essay":
                typeName = "主观题/简答题";
                formatExample = "格式要求（每道题严格按此格式，用---分隔每道题）：\n" +
                        "题目：[题目内容]\n" +
                        "参考答案：[详细的参考答案]\n" +
                        "评分标准：[评分要点和标准]\n" +
                        "---";
                break;
        }

        prompt.append("请为【").append(request.getSubject()).append("】科目生成").append(request.getCount()).append("道").append(typeName).append("。\n\n");

        if (request.getSection() != null && !request.getSection().isEmpty()) {
            prompt.append("知识点/章节：").append(request.getSection()).append("\n");
        }

        if (request.getDifficulty() != null && !request.getDifficulty().isEmpty()) {
            prompt.append("难度要求：").append(request.getDifficulty()).append("\n");
        }

        if (request.getKeywords() != null && !request.getKeywords().isEmpty()) {
            prompt.append("关键词：").append(request.getKeywords()).append("\n");
        }

        prompt.append("\n").append(formatExample).append("\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 题目内容要准确、专业，符合该科目的知识体系\n");
        prompt.append("2. 答案必须正确\n");
        prompt.append("3. 解析要清晰易懂\n");
        prompt.append("4. 严格按照上述格式输出，便于系统解析\n");
        prompt.append("5. 每道题之间用---分隔\n");

        return prompt.toString();
    }

    /**
     * 解析AI返回的题目
     */
    private List<Map<String, Object>> parseQuestions(String response, String questionType,
                                                      String subject, String section, String difficulty) {
        List<Map<String, Object>> questions = new ArrayList<>();

        // 移除DeepSeek R1模型的思考过程（<think>...</think>或</think>之前的内容）
        if (response.contains("</think>")) {
            response = response.substring(response.indexOf("</think>") + 8);
        }
        // 移除markdown格式标记，支持多种变体
        response = response.replace("**题目：**", "题目：")
                          .replace("**题目**：", "题目：")
                          .replace("**题目**", "题目")
                          .replace("**答案：**", "答案：")
                          .replace("**答案**：", "答案：")
                          .replace("**答案**", "答案")
                          .replace("**解析：**", "解析：")
                          .replace("**解析**：", "解析：")
                          .replace("**解析**", "解析")
                          .replace("### 数据结构选择题", "")
                          .replace("### 问题", "---\n题目");

        // 按---分隔题目
        String[] parts = response.split("---");

        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;

            Map<String, Object> question = new HashMap<>();
            question.put("subject", subject);
            question.put("section", section != null ? section : "");
            question.put("level", difficulty != null ? difficulty : "中等");

            try {
                switch (questionType) {
                    case "multi":
                        parseMultiQuestion(part, question);
                        break;
                    case "fill":
                        parseFillQuestion(part, question);
                        break;
                    case "judge":
                        parseJudgeQuestion(part, question);
                        break;
                    case "essay":
                        parseEssayQuestion(part, question);
                        break;
                }

                // 只有成功解析的题目才添加
                if (question.containsKey("question") && question.get("question") != null) {
                    questions.add(question);
                }
            } catch (Exception e) {
                System.err.println("解析题目失败: " + e.getMessage());
            }
        }

        return questions;
    }

    /**
     * 解析选择题
     */
    private void parseMultiQuestion(String text, Map<String, Object> question) {
        String[] lines = text.split("\n");

        for (String line : lines) {
            line = line.trim();
            // 支持多种题目格式
            if (line.startsWith("题目：") || line.startsWith("题目:")) {
                question.put("question", line.substring(3).trim());
            } else if (line.matches("^\\d+[.、．:].*") && !question.containsKey("question")) {
                // 支持"1. 题目内容"格式
                question.put("question", line.replaceFirst("^\\d+[.、．:]\\s*", ""));
            } else if (line.startsWith("A.") || line.startsWith("A、") || line.startsWith("A．") || line.startsWith("A ")) {
                question.put("answerA", line.substring(2).trim());
            } else if (line.startsWith("B.") || line.startsWith("B、") || line.startsWith("B．") || line.startsWith("B ")) {
                question.put("answerB", line.substring(2).trim());
            } else if (line.startsWith("C.") || line.startsWith("C、") || line.startsWith("C．") || line.startsWith("C ")) {
                question.put("answerC", line.substring(2).trim());
            } else if (line.startsWith("D.") || line.startsWith("D、") || line.startsWith("D．") || line.startsWith("D ")) {
                question.put("answerD", line.substring(2).trim());
            } else if (line.startsWith("答案：") || line.startsWith("答案:") || 
                       line.startsWith("正确答案：") || line.startsWith("正确答案:") ||
                       line.startsWith("**答案**：") || line.startsWith("**答案：**")) {
                // 提取答案内容
                String answer = line;
                if (line.contains("：")) {
                    answer = line.substring(line.indexOf("：") + 1).trim();
                } else if (line.contains(":")) {
                    answer = line.substring(line.indexOf(":") + 1).trim();
                }
                answer = answer.toUpperCase().replaceAll("[^ABCD]", "");
                if (!answer.isEmpty()) {
                    question.put("rightAnswer", answer.substring(0, 1));
                }
            } else if (line.startsWith("解析：") || line.startsWith("解析:") ||
                       line.startsWith("**解析**：") || line.startsWith("**解析：**")) {
                String analysis = line;
                if (line.contains("：")) {
                    analysis = line.substring(line.indexOf("：") + 1).trim();
                } else if (line.contains(":")) {
                    analysis = line.substring(line.indexOf(":") + 1).trim();
                }
                question.put("analysis", analysis);
            }
        }
    }

    /**
     * 解析填空题
     */
    private void parseFillQuestion(String text, Map<String, Object> question) {
        String[] lines = text.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("题目：") || line.startsWith("题目:")) {
                question.put("question", line.substring(3).trim());
            } else if (line.startsWith("答案：") || line.startsWith("答案:")) {
                question.put("answer", line.substring(3).trim());
            } else if (line.startsWith("解析：") || line.startsWith("解析:")) {
                question.put("analysis", line.substring(3).trim());
            }
        }
    }

    /**
     * 解析判断题
     */
    private void parseJudgeQuestion(String text, Map<String, Object> question) {
        String[] lines = text.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("题目：") || line.startsWith("题目:")) {
                question.put("question", line.substring(3).trim());
            } else if (line.startsWith("答案：") || line.startsWith("答案:")) {
                String answer = line.substring(3).trim().toUpperCase();
                // 统一转换为T/F格式
                if (answer.contains("正确") || answer.equals("T") || answer.equals("TRUE") || answer.equals("对")) {
                    question.put("answer", "T");
                } else {
                    question.put("answer", "F");
                }
            } else if (line.startsWith("解析：") || line.startsWith("解析:")) {
                question.put("analysis", line.substring(3).trim());
            }
        }
    }

    /**
     * 解析主观题/简答题
     */
    private void parseEssayQuestion(String text, Map<String, Object> question) {
        String[] lines = text.split("\n");
        StringBuilder referenceAnswer = new StringBuilder();
        StringBuilder scoringCriteria = new StringBuilder();
        boolean inAnswer = false;
        boolean inCriteria = false;

        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("题目：") || line.startsWith("题目:") ||
                line.startsWith("**题目**：") || line.startsWith("**题目：**")) {
                String q = line;
                if (line.contains("：")) {
                    q = line.substring(line.indexOf("：") + 1).trim();
                } else if (line.contains(":")) {
                    q = line.substring(line.indexOf(":") + 1).trim();
                }
                question.put("question", q);
                inAnswer = false;
                inCriteria = false;
            } else if (line.startsWith("参考答案：") || line.startsWith("参考答案:") ||
                       line.startsWith("**参考答案**：") || line.startsWith("**参考答案：**") ||
                       line.startsWith("答案：") || line.startsWith("答案:")) {
                String answer = line;
                if (line.contains("：")) {
                    answer = line.substring(line.indexOf("：") + 1).trim();
                } else if (line.contains(":")) {
                    answer = line.substring(line.indexOf(":") + 1).trim();
                }
                referenceAnswer.append(answer);
                inAnswer = true;
                inCriteria = false;
            } else if (line.startsWith("评分标准：") || line.startsWith("评分标准:") ||
                       line.startsWith("**评分标准**：") || line.startsWith("**评分标准：**") ||
                       line.startsWith("评分要点：") || line.startsWith("评分要点:")) {
                String criteria = line;
                if (line.contains("：")) {
                    criteria = line.substring(line.indexOf("：") + 1).trim();
                } else if (line.contains(":")) {
                    criteria = line.substring(line.indexOf(":") + 1).trim();
                }
                scoringCriteria.append(criteria);
                inAnswer = false;
                inCriteria = true;
            } else if (!line.isEmpty() && !line.startsWith("---")) {
                // 继续追加多行内容
                if (inAnswer) {
                    referenceAnswer.append("\n").append(line);
                } else if (inCriteria) {
                    scoringCriteria.append("\n").append(line);
                }
            }
        }

        if (referenceAnswer.length() > 0) {
            question.put("referenceAnswer", referenceAnswer.toString().trim());
        }
        if (scoringCriteria.length() > 0) {
            question.put("scoringCriteria", scoringCriteria.toString().trim());
        }
        // 默认分值
        question.put("score", 10);
    }
}
