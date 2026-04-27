package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.EssayQuestion;
import java.util.List;

/**
 * 简答题Service接口
 */
public interface EssayQuestionService {
    
    /**
     * 获取所有简答题
     */
    List<EssayQuestion> findAll();
    
    /**
     * 根据ID获取简答题
     */
    EssayQuestion findById(Integer questionId);
    
    /**
     * 根据科目查询简答题
     */
    List<EssayQuestion> findBySubject(String subject);
    
    /**
     * 根据科目和难度查询简答题
     */
    List<EssayQuestion> findBySubjectAndLevel(String subject, String level);
    
    /**
     * 添加简答题
     */
    int add(EssayQuestion question);
    
    /**
     * 更新简答题
     */
    int update(EssayQuestion question);
    
    /**
     * 删除简答题
     */
    int delete(Integer questionId);
    
    /**
     * 获取所有科目列表
     */
    List<String> findAllSubjects();
    
    /**
     * 获取最新插入的简答题
     */
    EssayQuestion findLastInserted();
}
