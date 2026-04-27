package com.rabbiter.oes.entity;

import java.util.List;
import java.util.Map;

/**
 * AI聊天请求实体类
 */
public class AiChatRequest {
    private String question;      // 用户问题
    private String context;        // 当前题目上下文
    private String subject;        // 科目
    private List<Map<String, String>> images;  // 图片列表

    public AiChatRequest() {
    }

    public AiChatRequest(String question, String context, String subject) {
        this.question = question;
        this.context = context;
        this.subject = subject;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<Map<String, String>> getImages() {
        return images;
    }

    public void setImages(List<Map<String, String>> images) {
        this.images = images;
    }

    @Override
    public String toString() {
        return "AiChatRequest{" +
                "question='" + question + '\'' +
                ", context='" + context + '\'' +
                ", subject='" + subject + '\'' +
                ", images=" + (images != null ? images.size() + " image(s)" : "null") +
                '}';
    }
}
