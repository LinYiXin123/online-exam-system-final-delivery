package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.AutoPaperRequest;
import com.rabbiter.oes.entity.PaperManage;

import java.util.List;
import java.util.Map;

public interface PaperService {

    List<PaperManage> findAll();

    List<PaperManage> findById(Integer paperId);

    int add(PaperManage paperManage);

    // 获取试卷总分
    Integer getMaxScore(Integer paperId);

    /**
     * 删除试卷中的某条试题
     *
     * @param paperId 试卷id
     * @param type 题目类型。1选择，2填空，3判断
     * @param questionId 题目id
     */
    void delete(String paperId, String type, String questionId);

    /**
     * 根据试卷id删除题目关联
     *
     * @param paperId 试卷id
     */
    void deleteByPaperId(Integer paperId);

    /**
     * 自动组卷
     *
     * @param request 自动组卷请求参数
     * @return 组卷结果
     */
    Map<String, Object> autoPaper(AutoPaperRequest request);

    /**
     * 获取题库统计信息
     *
     * @param subject 科目
     * @return 统计信息
     */
    Map<String, Object> getQuestionStats(String subject);

    /**
     * 获取题库中所有不重复的科目列表
     */
    List<String> getAllSubjects();
}
