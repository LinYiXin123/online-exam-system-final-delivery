package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ExamManage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ExamManageMapper {
//    @Select("select * from exam_manage")
//    List<ExamManage> findAll();

    @Select("select * from exam_manage")
    IPage<ExamManage> findAll(Page page);

    @Select("select * from exam_manage where examCode = #{examCode}")
    ExamManage findById(Integer examCode);

    @Select("select * from exam_manage where paperId = #{paperId}")
    ExamManage findByPaperId(Integer paperId);

    @Delete("delete from exam_manage where examCode = #{examCode}")
    int delete(Integer examCode);

    @Update("update exam_manage set description = #{description},source = #{source},paperId = #{paperId}," +
            "examDate = #{examDate},startTime = COALESCE(#{startTime}, startTime),endTime = COALESCE(#{endTime}, endTime),totalTime = #{totalTime},grade = #{grade},term = #{term}," +
            "major = #{major},institute = #{institute},totalScore = #{totalScore}," +
            "type = #{type},tips = #{tips} where examCode = #{examCode}")
    int update(ExamManage exammanage);

    @Options(useGeneratedKeys = true,keyProperty = "examCode")
    @Insert("insert into exam_manage(description,source,paperId,examDate,startTime,endTime,totalTime,grade,term,major,institute,totalScore,type,tips)" +
            " values(#{description},#{source},#{paperId},#{examDate},#{startTime},#{endTime},#{totalTime},#{grade},#{term},#{major},#{institute},#{totalScore},#{type},#{tips})")
    int add(ExamManage exammanage);

    /**
     * 查询最后一条记录的paperId,返回给前端达到自增效果
     * @return paperId
     */
    @Select("select paperId from exam_manage order by paperId desc limit 1")
    Integer findMaxPaperId();

    /**
     * 获取所有不重复的学院
     */
    @Select("SELECT DISTINCT institute FROM exam_manage WHERE institute IS NOT NULL AND institute != ''")
    List<String> findAllInstitutes();

    /**
     * 获取所有不重复的专业
     */
    @Select("SELECT DISTINCT major FROM exam_manage WHERE major IS NOT NULL AND major != ''")
    List<String> findAllMajors();

    /**
     * 获取所有不重复的年级
     */
    @Select("SELECT DISTINCT grade FROM exam_manage WHERE grade IS NOT NULL AND grade != '' AND grade REGEXP '^[0-9]{4}$' ORDER BY grade DESC")
    List<String> findAllGrades();

    /**
     * 获取所有不重复的科目/课程名称
     */
    @Select("SELECT DISTINCT source FROM exam_manage WHERE source IS NOT NULL AND source != ''")
    List<String> findAllSubjects();
    
    /**
     * 带查询条件的分页查询
     */
    @Select("SELECT * FROM exam_manage WHERE " +
            "source LIKE CONCAT('%',#{source},'%') " +
            "AND institute LIKE CONCAT('%',#{institute},'%') " +
            "AND major LIKE CONCAT('%',#{major},'%') " +
            "AND grade LIKE CONCAT('%',#{grade},'%')")
    IPage<ExamManage> findAllWithSearch(Page page, @Param("source") String source, 
            @Param("institute") String institute, @Param("major") String major, @Param("grade") String grade);
}
