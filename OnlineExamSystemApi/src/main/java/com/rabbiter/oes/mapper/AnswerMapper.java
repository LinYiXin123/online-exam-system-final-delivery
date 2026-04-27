package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.EssayQuestion;
import com.rabbiter.oes.entity.FillQuestion;
import com.rabbiter.oes.entity.JudgeQuestion;
import com.rabbiter.oes.entity.MultiQuestion;
import com.rabbiter.oes.vo.AnswerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface AnswerMapper {
    @Select("<script>" +
            "SELECT * FROM (" +
            "SELECT questionId, question, subject, score, section, level, '选择题' as type FROM multi_question " +
            "WHERE question LIKE CONCAT('%',#{question},'%') AND subject LIKE CONCAT('%',#{subject},'%') AND section LIKE CONCAT('%',#{section},'%') " +
            "UNION ALL " +
            "SELECT questionId, question, subject, score, section, level, '判断题' as type FROM judge_question " +
            "WHERE question LIKE CONCAT('%',#{question},'%') AND subject LIKE CONCAT('%',#{subject},'%') AND section LIKE CONCAT('%',#{section},'%') " +
            "UNION ALL " +
            "SELECT questionId, question, subject, score, section, level, '填空题' as type FROM fill_question " +
            "WHERE question LIKE CONCAT('%',#{question},'%') AND subject LIKE CONCAT('%',#{subject},'%') AND section LIKE CONCAT('%',#{section},'%') " +
            "UNION ALL " +
            "SELECT questionId, question, subject, score, section, level, '主观题' as type FROM essay_question " +
            "WHERE question LIKE CONCAT('%',#{question},'%') AND subject LIKE CONCAT('%',#{subject},'%') AND section LIKE CONCAT('%',#{section},'%')" +
            ") AS combined " +
            "<where>" +
            "<if test='questionType != null and questionType != \"\"'> type = #{questionType} </if>" +
            "</where>" +
            "</script>")
    IPage<AnswerVO> findAll(Page<AnswerVO> page, @Param("subject") String subject, @Param("section") String section, @Param("question") String question, @Param("questionType") String questionType);


    /**
     * 查选择题
     * @param questionId 选择题id
     * @return 选择题
     */
    @Select("select questionId, subject, question, answerA, answerB, answerC, answerD, rightAnswer, section, level, analysis from multi_question where questionId = #{questionId}")
    MultiQuestion findMultiQuestionById(Long questionId);

    /**
     * 查填空题
     *
     * @param questionId 题目id
     * @return 填空题
     */
    @Select("select questionId, subject, question, answer, analysis, level, section from fill_question where questionId = #{questionId}")
    FillQuestion findFillQuestionById(Long questionId);

    /**
     * 查判断题
     *
     * @param questionId 题目id
     * @return 判断题
     */
    @Select("select questionId, subject, question, answer, analysis, level, section from judge_question where questionId = #{questionId}")
    JudgeQuestion findJudgeQuestionById(Long questionId);

    /**
     * 查主观题
     *
     * @param questionId 题目id
     * @return 主观题
     */
    @Select("select questionId, subject, question, answer, scoringCriteria, score, level, section from essay_question where questionId = #{questionId}")
    EssayQuestion findEssayQuestionById(Long questionId);
}
