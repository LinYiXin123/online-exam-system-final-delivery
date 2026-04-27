package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Score;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScoreMapper {
    /**
     * @param score 添加一条成绩记录
     * @return
     */
    @Options(useGeneratedKeys = true,keyProperty = "scoreId")
    @Insert("insert into score(examCode,studentId,subject,ptScore,etScore,score,answerDate,scoreType) values(#{examCode},#{studentId},#{subject},#{ptScore},#{etScore},#{score},#{answerDate},#{scoreType})")
    int add(Score score);

    @Select("<script>" +
            "SELECT * FROM score WHERE examCode = #{examCode} AND studentId = #{studentId} " +
            "<choose>" +
            "<when test='scoreType == \"exam\"'> AND (scoreType = 'exam' OR scoreType IS NULL OR scoreType = '') </when>" +
            "<otherwise> AND scoreType = #{scoreType} </otherwise>" +
            "</choose>" +
            "ORDER BY scoreId DESC LIMIT 1" +
            "</script>")
    Score findLatestByExamStudentAndType(@Param("examCode") Integer examCode,
                                         @Param("studentId") Long studentId,
                                         @Param("scoreType") String scoreType);

    @Update("update score set subject = #{subject}, ptScore = #{ptScore}, etScore = #{etScore}, score = #{score}, answerDate = #{answerDate}, scoreType = #{scoreType} where scoreId = #{scoreId}")
    int updateById(Score score);

    @Select("select scoreId,examCode,studentId,subject,ptScore,etScore,score,answerDate from score order by scoreId desc")
    List<Score> findAll();

    // 分页
    @Select("select scoreId,examCode,studentId,subject,ptScore,etScore,score,answerDate from score where studentId = #{studentId} order by scoreId asc")
    IPage<Score> findById(Page<?> page, @Param("studentId") Long studentId);
    
    // 获取学生成绩（包含考试介绍，用于图表显示）
    @Select("SELECT s.scoreId, s.examCode, s.studentId, s.subject, s.ptScore, s.etScore, s.score, s.answerDate, " +
            "em.description as description " +
            "FROM score s " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.studentId = #{studentId} ORDER BY s.scoreId ASC")
    List<Map<String, Object>> findByIdWithDescription(@Param("studentId") Long studentId);

    /**
     *
     * @return 查询学生的学科分数（包含学生信息）。
     */
    @Select("SELECT s.scoreId, s.examCode, s.studentId, s.subject, s.ptScore, s.etScore, s.score, s.answerDate, " +
            "st.studentName, st.major, st.grade, st.clazz, st.institute " +
            "FROM score s " +
            "LEFT JOIN student st ON s.studentId = st.studentId " +
            "INNER JOIN (SELECT studentId, MAX(scoreId) as maxId FROM score WHERE examCode = #{examCode} AND (scoreType = 'exam' OR scoreType IS NULL OR scoreType = '') GROUP BY studentId) latest ON s.scoreId = latest.maxId " +
            "WHERE s.examCode = #{examCode}")
    List<Map<String, Object>> findByExamCode(Integer examCode);

    /**
     * 获取考试详情（用于答卷回顾，取最新一条记录，包含考试介绍）
     * @param examCode 考试编号
     * @param studentId 学生ID
     * @return 成绩详情（包含考试介绍）
     */
    @Select("SELECT s.scoreId, s.examCode, s.studentId, s.subject, s.ptScore, s.etScore, s.score, s.answerDate, s.scoreType, " +
            "em.description as examDescription, em.totalScore as totalScore, em.totalTime as totalTime, " +
            "CASE WHEN EXISTS(SELECT 1 FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId) " +
            "THEN IFNULL((SELECT SUM(IFNULL(ea.finalScore, 0)) FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId), 0) " +
            "ELSE IFNULL(s.ptScore, 0) END as subjectiveScore, " +
            "(s.etScore + CASE WHEN EXISTS(SELECT 1 FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId) " +
            "THEN IFNULL((SELECT SUM(IFNULL(ea.finalScore, 0)) FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId), 0) " +
            "ELSE IFNULL(s.ptScore, 0) END) as totalScoreActual " +
            "FROM score s " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.examCode = #{examCode} AND s.studentId = #{studentId} " +
            "ORDER BY s.scoreId DESC LIMIT 1")
    Map<String, Object> getScoreDetail(@Param("examCode") Integer examCode, @Param("studentId") Long studentId);

    /**
     * 分页查询成绩（支持多条件筛选）
     * @param page 分页对象
     * @param studentId 学生ID
     * @param examType 考试类型
     * @param subject 课程名称（模糊搜索）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param ptScore 是否及格
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT s.scoreId,s.examCode,s.studentId,s.subject,s.ptScore,s.etScore,s.score,s.answerDate,s.scoreType,em.description,em.totalScore, " +
            "CASE WHEN EXISTS(SELECT 1 FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId) " +
            "THEN IFNULL((SELECT SUM(IFNULL(ea.finalScore, 0)) FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId), 0) " +
            "ELSE IFNULL(s.ptScore, 0) END as subjectiveScore, " +
            "CASE WHEN EXISTS(SELECT 1 FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId AND ea.gradingStatus != 'teacher_reviewed') " +
            "THEN 0 ELSE 1 END as gradingCompleted " +
            "FROM score s " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.studentId = #{studentId} " +
            "<if test='examType != null and examType != \"\"'>" +
            "<choose>" +
            "<when test='examType == \"exam\"'> AND (s.scoreType = 'exam' OR s.scoreType IS NULL OR s.scoreType = '') </when>" +
            "<otherwise> AND s.scoreType = #{examType} </otherwise>" +
            "</choose>" +
            "</if>" +
            "<if test='subject != null and subject != \"\"'> AND s.subject LIKE CONCAT('%', #{subject}, '%') </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND s.answerDate &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND s.answerDate &lt;= #{endDate} </if>" +
            "<if test='ptScore != null'> " +
            "AND (CASE WHEN (IFNULL(s.etScore, 0) + IFNULL(s.ptScore, 0)) &gt;= (COALESCE(em.totalScore, s.score, 100) * 0.6) THEN 1 ELSE 0 END) = #{ptScore} " +
            "</if>" +
            "ORDER BY s.scoreId DESC" +
            "</script>")
    IPage<Score> findByIdWithFilter(
            Page<?> page,
            @Param("studentId") Long studentId,
            @Param("examType") String examType,
            @Param("subject") String subject,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("ptScore") Integer ptScore
    );

    /**
     * 获取考试成绩详细信息（包含学生信息）用于导出
     * @param examCode 考试编号
     * @return 详细成绩信息列表
     */
    @Select("SELECT " +
            "s.scoreId, s.examCode, s.studentId, s.subject, s.ptScore, s.etScore, s.score, s.answerDate, s.scoreType, " +
            "st.studentName, st.grade, st.major, st.clazz, st.institute, st.tel, st.email, st.sex, " +
            "em.description as examDescription, em.examDate, em.totalTime, em.totalScore as examTotalScore " +
            "FROM score s " +
            "INNER JOIN (SELECT studentId, MAX(scoreId) as maxId FROM score WHERE examCode = #{examCode} AND (scoreType = 'exam' OR scoreType IS NULL OR scoreType = '') GROUP BY studentId) latest ON s.scoreId = latest.maxId " +
            "LEFT JOIN student st ON s.studentId = st.studentId " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.examCode = #{examCode} " +
            "ORDER BY st.studentName ASC")
    List<Map<String, Object>> findDetailedScoresByExamCode(@Param("examCode") Integer examCode);

    /**
     * 获取学生个人成绩详细信息（包含考试信息）用于导出
     * @param studentId 学生ID
     * @param examType 考试类型
     * @param subject 课程名称
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 详细成绩信息列表
     */
    @Select("<script>" +
            "SELECT " +
            "s.scoreId, s.examCode, s.studentId, s.subject, s.ptScore, s.etScore, s.score, s.answerDate, s.scoreType, " +
            "st.studentName, st.grade, st.major, st.clazz, st.institute, st.tel, st.email, st.sex, " +
            "em.description as examDescription, em.examDate, em.totalTime, em.totalScore as examTotalScore " +
            "FROM score s " +
            "LEFT JOIN student st ON s.studentId = st.studentId " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.studentId = #{studentId} " +
            "<if test='examType != null and examType != \"\"'>" +
            "<choose>" +
            "<when test='examType == \"exam\"'> AND (s.scoreType = 'exam' OR s.scoreType IS NULL OR s.scoreType = '') </when>" +
            "<otherwise> AND s.scoreType = #{examType} </otherwise>" +
            "</choose>" +
            "</if>" +
            "<if test='subject != null and subject != \"\"'> AND s.subject LIKE CONCAT('%', #{subject}, '%') </if>" +
            "<if test='startDate != null and startDate != \"\"'> AND s.answerDate &gt;= #{startDate} </if>" +
            "<if test='endDate != null and endDate != \"\"'> AND s.answerDate &lt;= #{endDate} </if>" +
            "ORDER BY s.answerDate DESC" +
            "</script>")
    List<Map<String, Object>> findDetailedScoresByStudentId(
            @Param("studentId") Long studentId,
            @Param("examType") String examType,
            @Param("subject") String subject,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    /**
     * 获取班级成绩统计
     * @param examCode 考试编号
     * @return 班级统计数据
     */
    @Select("SELECT " +
            "st.clazz as className, " +
            "COUNT(*) as totalCount, " +
            "AVG(s.etScore + IFNULL(s.ptScore, 0)) as averageScore, " +
            "MAX(s.etScore + IFNULL(s.ptScore, 0)) as maxScore, " +
            "MIN(s.etScore + IFNULL(s.ptScore, 0)) as minScore, " +
            "SUM(CASE WHEN (s.etScore + IFNULL(s.ptScore, 0)) >= (em.totalScore * 0.6) THEN 1 ELSE 0 END) as passCount " +
            "FROM score s " +
            "LEFT JOIN student st ON s.studentId = st.studentId " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.examCode = #{examCode} AND (s.scoreType = 'exam' OR s.scoreType IS NULL OR s.scoreType = '') " +
            "GROUP BY st.clazz " +
            "ORDER BY averageScore DESC")
    List<Map<String, Object>> getClassStatistics(@Param("examCode") Integer examCode);

    /**
     * 获取专业成绩统计
     * @param examCode 考试编号
     * @return 专业统计数据
     */
    @Select("SELECT " +
            "st.major as majorName, " +
            "COUNT(*) as totalCount, " +
            "AVG(s.etScore + IFNULL(s.ptScore, 0)) as averageScore, " +
            "MAX(s.etScore + IFNULL(s.ptScore, 0)) as maxScore, " +
            "MIN(s.etScore + IFNULL(s.ptScore, 0)) as minScore, " +
            "SUM(CASE WHEN (s.etScore + IFNULL(s.ptScore, 0)) >= (em.totalScore * 0.6) THEN 1 ELSE 0 END) as passCount " +
            "FROM score s " +
            "LEFT JOIN student st ON s.studentId = st.studentId " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.examCode = #{examCode} AND (s.scoreType = 'exam' OR s.scoreType IS NULL OR s.scoreType = '') " +
            "GROUP BY st.major " +
            "ORDER BY averageScore DESC")
    List<Map<String, Object>> getMajorStatistics(@Param("examCode") Integer examCode);

    /**
     * 获取学院成绩统计
     * @param examCode 考试编号
     * @return 学院统计数据
     */
    @Select("SELECT " +
            "st.institute as instituteName, " +
            "COUNT(*) as totalCount, " +
            "AVG(s.etScore + IFNULL(s.ptScore, 0)) as averageScore, " +
            "MAX(s.etScore + IFNULL(s.ptScore, 0)) as maxScore, " +
            "MIN(s.etScore + IFNULL(s.ptScore, 0)) as minScore, " +
            "SUM(CASE WHEN (s.etScore + IFNULL(s.ptScore, 0)) >= (em.totalScore * 0.6) THEN 1 ELSE 0 END) as passCount " +
            "FROM score s " +
            "LEFT JOIN student st ON s.studentId = st.studentId " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.examCode = #{examCode} AND (s.scoreType = 'exam' OR s.scoreType IS NULL OR s.scoreType = '') " +
            "GROUP BY st.institute " +
            "ORDER BY averageScore DESC")
    List<Map<String, Object>> getInstituteStatistics(@Param("examCode") Integer examCode);

    /**
     * 获取学生个人成绩趋势（按时间排序）
     * @param studentId 学生ID
     * @return 成绩趋势数据
     */
    @Select("SELECT " +
            "s.examCode, s.studentId, s.subject, s.etScore, s.ptScore, s.answerDate, " +
            "s.scoreType as examType, " +
            "em.description as examDescription, " +
            "CASE WHEN EXISTS(SELECT 1 FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId) " +
            "THEN IFNULL((SELECT SUM(IFNULL(ea.finalScore, 0)) FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId), 0) " +
            "ELSE IFNULL(s.ptScore, 0) END as subjectiveScore, " +
            "CASE WHEN NOT EXISTS(SELECT 1 FROM paper_manage pm WHERE pm.paperId = em.paperId AND pm.questionType = 4) THEN 1 " +
            "WHEN NOT EXISTS(SELECT 1 FROM essay_answer ea WHERE ea.examCode = s.examCode AND ea.studentId = s.studentId AND (ea.gradingStatus IS NULL OR ea.gradingStatus != 'teacher_reviewed')) THEN 1 " +
            "ELSE 0 END as isGraded " +
            "FROM score s " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.studentId = #{studentId} " +
            "ORDER BY s.answerDate ASC")
    List<Map<String, Object>> getStudentScoreTrend(@Param("studentId") Long studentId);

    /**
     * 获取课程历史成绩统计（同一课程的历次考试对比）
     * @param subject 课程名称
     * @return 课程历史统计数据
     */
    @Select("SELECT " +
            "s.examCode, em.description as examDescription, em.examDate, " +
            "COUNT(*) as totalCount, " +
            "AVG(s.etScore + IFNULL(s.ptScore, 0)) as averageScore, " +
            "MAX(s.etScore + IFNULL(s.ptScore, 0)) as maxScore, " +
            "MIN(s.etScore + IFNULL(s.ptScore, 0)) as minScore, " +
            "SUM(CASE WHEN (s.etScore + IFNULL(s.ptScore, 0)) >= (em.totalScore * 0.6) THEN 1 ELSE 0 END) as passCount " +
            "FROM score s " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.subject = #{subject} AND (s.scoreType = 'exam' OR s.scoreType IS NULL OR s.scoreType = '') " +
            "GROUP BY s.examCode, em.description, em.examDate " +
            "ORDER BY em.examDate DESC")
    List<Map<String, Object>> getSubjectHistoryStatistics(@Param("subject") String subject);

    /**
     * 获取性别成绩统计
     * @param examCode 考试编号
     * @return 性别统计数据
     */
    @Select("SELECT " +
            "st.sex as gender, " +
            "COUNT(*) as totalCount, " +
            "AVG(s.etScore + IFNULL(s.ptScore, 0)) as averageScore, " +
            "MAX(s.etScore + IFNULL(s.ptScore, 0)) as maxScore, " +
            "MIN(s.etScore + IFNULL(s.ptScore, 0)) as minScore, " +
            "SUM(CASE WHEN (s.etScore + IFNULL(s.ptScore, 0)) >= (em.totalScore * 0.6) THEN 1 ELSE 0 END) as passCount " +
            "FROM score s " +
            "LEFT JOIN student st ON s.studentId = st.studentId " +
            "LEFT JOIN exam_manage em ON s.examCode = em.examCode " +
            "WHERE s.examCode = #{examCode} AND (s.scoreType = 'exam' OR s.scoreType IS NULL OR s.scoreType = '') " +
            "GROUP BY st.sex " +
            "ORDER BY averageScore DESC")
    List<Map<String, Object>> getGenderStatistics(@Param("examCode") Integer examCode);
}
