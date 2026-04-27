package com.rabbiter.oes.mapper;

import com.rabbiter.oes.entity.PaperManage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PaperMapper {
    @Select("select paperId, questionType,questionId from paper_manage")
    List<PaperManage> findAll();

    @Select("select paperId, questionType,questionId from paper_manage where paperId = #{paperId}")
    List<PaperManage> findById(Integer paperId);

    @Insert("insert into paper_manage(paperId,questionType,questionId) values " +
            "(#{paperId},#{questionType},#{questionId})")
    int add(PaperManage paperManage);

    @Delete("delete from paper_manage where paperId = #{paperId} and questionType = #{type} and questionId = #{questionId}")
    void delete(@Param("paperId") String paperId, @Param("type") String type, @Param("questionId") String questionId);

    /**
     * 根据试卷id删除题目关联
     *
     * @param paperId 试卷id
     */
    @Delete("DELETE FROM paper_manage WHERE paperId = #{paperId}")
    void deleteByPaperId(@Param("paperId") Integer paperId);

    /**
     * 批量插入试卷题目关联
     */
    @Insert("<script>" +
            "INSERT INTO paper_manage(paperId, questionType, questionId) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.paperId}, #{item.questionType}, #{item.questionId})" +
            "</foreach>" +
            "</script>")
    int batchAdd(@Param("list") List<PaperManage> paperManageList);

    /**
     * 统计指定试卷中指定题型的数量
     * @param paperId 试卷ID
     * @param questionType 题型（1=单选，2=填空，3=判断，4=主观题）
     */
    @Select("SELECT COUNT(*) FROM paper_manage WHERE paperId = #{paperId} AND questionType = #{questionType}")
    int countByPaperIdAndType(@Param("paperId") Integer paperId, @Param("questionType") Integer questionType);
}
