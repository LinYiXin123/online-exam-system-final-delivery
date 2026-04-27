package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.AiChatRequest;
import com.rabbiter.oes.entity.AiGenerateQuestionRequest;

import java.util.Map;

/**
 * AI服务接口
 */
public interface AiService {
    /**
     * 发送聊天消息到AI
     * @param request 聊天请求
     * @return AI回复
     */
    String chat(AiChatRequest request) throws Exception;

    /**
     * AI生成题目
     * @param request 生成题目请求
     * @return 生成结果
     */
    Map<String, Object> generateQuestions(AiGenerateQuestionRequest request) throws Exception;
}
