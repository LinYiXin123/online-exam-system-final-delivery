package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.MultiQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

//选择题
@Mapper
public interface MultiQuestionMapper {
    /**
     * select * from multiquestions where questionId in (
     * 	select questionId from papermanage where questionType = 1 and paperId = 1001
     * )
     */
    @Select("select * from multi_question where questionId in (select questionId from paper_manage where questionType = 1 and paperId = #{paperId})")
    List<MultiQuestion> findByIdAndType(Integer PaperId);

    @Select("select * from multi_question")
    IPage<MultiQuestion> findAll(Page page);

    /**
     * 查询最后一条记录的questionId
     * @return MultiQuestion
     */
    @Select("select questionId from multi_question order by questionId desc limit 1")
    MultiQuestion findOnlyQuestionId();

    @Options(useGeneratedKeys = true,keyProperty = "questionId")
    @Insert("insert into multi_question(subject,question,answerA,answerB,answerC,answerD,rightAnswer,analysis,section,level) " +
            "values(#{subject},#{question},#{answerA},#{answerB},#{answerC},#{answerD},#{rightAnswer},#{analysis},#{section},#{level})")
    int add(MultiQuestion multiQuestion);

    @Select("select questionId from multi_question  where subject =#{subject} order by rand() desc limit #{pageNo}")
    List<Integer> findBySubject(@Param("subject") String subject, @Param("pageNo") Integer pageNo);

    @Update("update multi_question set subject = #{subject}, question = #{question}, answerA = #{answerA}, answerB = #{answerB}, answerC = #{answerC}, answerD = #{answerD}, rightAnswer = #{rightAnswer}, analysis = #{analysis}, section = #{section}, level = #{level} where questionId = #{questionId}")
    int edit(MultiQuestion multiQuestion);

    @Select("select * from multi_question where subject =#{subject}")
    List<MultiQuestion> findQuestionBySubject(@Param("subject") String subject);

    @Select("select * from multi_question where questionId = #{questionId}")
    MultiQuestion findById(@Param("questionId") Integer questionId);

    /**
     * 根据科目、难度、知识点随机获取指定数量的选择题ID
     */
    @Select("<script>" +
            "SELECT questionId FROM multi_question WHERE subject = #{subject} " +
            "<if test='level != null and level != \"\"'> AND level = #{level} </if>" +
            "<if test='section != null and section != \"\"'> AND section = #{section} </if>" +
            "ORDER BY RAND() LIMIT #{count}" +
            "</script>")
    List<Integer> findRandomByCondition(@Param("subject") String subject,
                                        @Param("level") String level,
                                        @Param("section") String section,
                                        @Param("count") Integer count);

    /**
     * 根据科目统计各难度题目数量
     */
    @Select("SELECT level, COUNT(*) as count FROM multi_question WHERE subject = #{subject} GROUP BY level")
    List<java.util.Map<String, Object>> countBySubjectAndLevel(@Param("subject") String subject);

    /**
     * 获取所有不重复的科目
     */
    @Select("SELECT DISTINCT subject FROM multi_question WHERE subject IS NOT NULL AND subject != ''")
    List<String> findAllSubjects();
}
