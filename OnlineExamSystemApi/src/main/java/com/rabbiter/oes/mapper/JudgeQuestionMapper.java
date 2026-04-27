package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.JudgeQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

//判断题

@Mapper
public interface JudgeQuestionMapper {

    @Select("select * from judge_question where questionId in (select questionId from paper_manage where questionType = 3 and paperId = #{paperId})")
    List<JudgeQuestion> findByIdAndType(Integer paperId);

    @Select("select * from judge_question")
    IPage<JudgeQuestion> findAll(Page page);

    /**
     * 查询最后一条记录的questionId
     * @return JudgeQuestion
     */
    @Select("select questionId from judge_question order by questionId desc limit 1")
    JudgeQuestion findOnlyQuestionId();

    @Insert("insert into judge_question(subject,question,answer,analysis,level,section) values " +
            "(#{subject},#{question},#{answer},#{analysis},#{level},#{section})")
    int add(JudgeQuestion judgeQuestion);

    @Select("select questionId from judge_question  where subject=#{subject}  order by rand() desc limit #{pageNo}")
    List<Integer> findBySubject(@Param("subject") String subject, @Param("pageNo") Integer pageNo);

    @Update("update judge_question set subject = #{subject}, question = #{question}, answer = #{answer}, section = #{section}, analysis = #{analysis}, level = #{level} where questionId = #{questionId}")
    int edit(JudgeQuestion judgeQuestion);

    @Select("select * from judge_question  where subject=#{subject}")
    List<JudgeQuestion> findQuestionBySubject(@Param("subject") String subject);

    @Select("select * from judge_question where questionId = #{questionId}")
    JudgeQuestion findById(@Param("questionId") Integer questionId);

    /**
     * 根据科目、难度、知识点随机获取指定数量的判断题ID
     */
    @Select("<script>" +
            "SELECT questionId FROM judge_question WHERE subject = #{subject} " +
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
    @Select("SELECT level, COUNT(*) as count FROM judge_question WHERE subject = #{subject} GROUP BY level")
    List<java.util.Map<String, Object>> countBySubjectAndLevel(@Param("subject") String subject);

    /**
     * 获取所有不重复的科目
     */
    @Select("SELECT DISTINCT subject FROM judge_question WHERE subject IS NOT NULL AND subject != ''")
    List<String> findAllSubjects();

}
