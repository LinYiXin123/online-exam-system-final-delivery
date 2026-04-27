package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rabbiter.oes.entity.EssayQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 简答题Mapper接口
 */
@Mapper
public interface EssayQuestionMapper extends BaseMapper<EssayQuestion> {
    
    /**
     * 根据科目查询简答题
     */
    @Select("SELECT * FROM essay_question WHERE subject = #{subject}")
    List<EssayQuestion> findBySubject(String subject);
    
    /**
     * 根据科目和难度查询简答题
     */
    @Select("SELECT * FROM essay_question WHERE subject = #{subject} AND level = #{level}")
    List<EssayQuestion> findBySubjectAndLevel(String subject, String level);
    
    /**
     * 获取所有科目列表
     */
    @Select("SELECT DISTINCT subject FROM essay_question")
    List<String> findAllSubjects();
    
    /**
     * 统计各科目简答题数量
     */
    @Select("SELECT subject, COUNT(*) as count FROM essay_question GROUP BY subject")
    List<Object> countAllBySubject();
    
    /**
     * 统计指定科目的简答题数量
     */
    @Select("SELECT COUNT(*) FROM essay_question WHERE subject = #{subject}")
    int countBySubject(String subject);
    
    /**
     * 根据试卷ID查询主观题（通过paper_manage关联）
     */
    @Select("SELECT eq.* FROM essay_question eq " +
            "INNER JOIN paper_manage pm ON eq.questionId = pm.questionId " +
            "WHERE pm.paperId = #{paperId} AND pm.questionType = 4")
    List<EssayQuestion> findByPaperId(Integer paperId);
    
    /**
     * 获取最新插入的简答题
     */
    @Select("SELECT * FROM essay_question ORDER BY questionId DESC LIMIT 1")
    EssayQuestion findLastInserted();
    
    /**
     * 随机抽取指定数量的主观题
     */
    @Select("<script>" +
            "SELECT questionId FROM essay_question WHERE subject = #{subject} " +
            "<if test='level != null'> AND level = #{level} </if>" +
            "<if test='section != null'> AND section LIKE CONCAT('%', #{section}, '%') </if>" +
            "ORDER BY RAND() LIMIT #{count}" +
            "</script>")
    List<Integer> findRandomByCondition(String subject, String level, String section, Integer count);
}
