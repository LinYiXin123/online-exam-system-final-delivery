package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.FillQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

//填空题
@Mapper
public interface FillQuestionMapper {

    @Select("select * from fill_question where questionId in (select questionId from paper_manage where questionType = 2 and paperId = #{paperId})")
    List<FillQuestion> findByIdAndType(Integer paperId);

    @Select("select * from fill_question")
    IPage<FillQuestion> findAll(Page page);

    /**
     * 查询最后一条questionId
     * @return FillQuestion
     */
    @Select("select questionId from fill_question order by questionId desc limit 1")
    FillQuestion findOnlyQuestionId();

    @Options(useGeneratedKeys = true,keyProperty ="questionId" )
    @Insert("insert into fill_question(subject,question,answer,analysis,level,section) values " +
            "(#{subject},#{question},#{answer},#{analysis},#{level},#{section})")
    int add(FillQuestion fillQuestion);

    @Select("select questionId from fill_question where subject = #{subject} order by rand() desc limit #{pageNo}")
    List<Integer> findBySubject(@Param("subject") String subject, @Param("pageNo") Integer pageNo);

    @Update("update fill_question set section = #{section}, question = #{question}, answer = #{answer}, level = #{level}, analysis = #{analysis} where questionId = #{questionId}")
    int edit(FillQuestion fillQuestion);

    @Select("select * from fill_question where subject = #{subject}")
    List<FillQuestion> findQuestionBySubject(@Param("subject") String subject);

    @Select("select * from fill_question where questionId = #{questionId}")
    FillQuestion findById(@Param("questionId") Integer questionId);

    /**
     * 根据科目、难度、知识点随机获取指定数量的填空题ID
     */
    @Select("<script>" +
            "SELECT questionId FROM fill_question WHERE subject = #{subject} " +
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
    @Select("SELECT level, COUNT(*) as count FROM fill_question WHERE subject = #{subject} GROUP BY level")
    List<java.util.Map<String, Object>> countBySubjectAndLevel(@Param("subject") String subject);

    /**
     * 获取所有不重复的科目
     */
    @Select("SELECT DISTINCT subject FROM fill_question WHERE subject IS NOT NULL AND subject != ''")
    List<String> findAllSubjects();

}
