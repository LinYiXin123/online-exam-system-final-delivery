package com.rabbiter.oes.mapper;

import com.rabbiter.oes.entity.StudentAnswer;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentAnswerMapper {
    @Delete("DELETE FROM answer_record WHERE examCode = #{examCode} AND studentId = #{studentId}")
    int deleteByExamAndStudent(@Param("examCode") Integer examCode, @Param("studentId") Long studentId);
    
    /**
     * 批量插入答题记录
     * @param answerList 答题记录列表
     * @return 插入条数
     */
    @Insert("<script>" +
            "INSERT INTO answer_record (studentId, examCode, questionId, questionType, studentAnswer, isCorrect) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.studentId}, #{item.examCode}, #{item.questionId}, #{item.questionType}, #{item.studentAnswer}, #{item.isCorrect})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<StudentAnswer> answerList);
    
    /**
     * 根据考试编号和学生ID查询答题记录
     * @param examCode 考试编号
     * @param studentId 学生ID
     * @return 答题记录列表
     */
    @Select("SELECT ar.recordId, ar.studentId, ar.examCode, ar.questionId, ar.questionType, ar.studentAnswer, ar.isCorrect " +
           "FROM answer_record ar " +
           "INNER JOIN (SELECT questionType, questionId, MAX(recordId) as maxId FROM answer_record WHERE examCode = #{examCode} AND studentId = #{studentId} GROUP BY questionType, questionId) latest " +
           "ON ar.recordId = latest.maxId")
    @Results({
        @Result(property = "recordId", column = "recordId"),
        @Result(property = "studentId", column = "studentId"),
        @Result(property = "examCode", column = "examCode"),
        @Result(property = "questionId", column = "questionId"),
        @Result(property = "questionType", column = "questionType"),
        @Result(property = "studentAnswer", column = "studentAnswer"),
        @Result(property = "isCorrect", column = "isCorrect")
    })
    List<StudentAnswer> selectByExamAndStudent(@Param("examCode") Integer examCode, @Param("studentId") Long studentId);
}
