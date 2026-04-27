package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Message;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MessageMapper {
    @Select("select m.id, m.id as temp_id, m.studentId, m.title, m.content, m.time, " +
            "COALESCE(s.studentName, t.teacherName, a.adminName, '未知用户') as publisherName, " +
            "CASE " +
            "WHEN s.studentId is not null THEN '学生' " +
            "WHEN t.teacherId is not null THEN '教师' " +
            "WHEN a.adminId is not null THEN '管理员' " +
            "ELSE '未知' END as publisherRole " +
            "from message m " +
            "left join student s on CAST(s.studentId AS CHAR) = m.studentId " +
            "left join teacher t on CAST(t.teacherId AS CHAR) = m.studentId " +
            "left join admin a on CAST(a.adminId AS CHAR) = m.studentId " +
            "order by m.id desc")
    @Results({
            @Result(property = "replays", column = "temp_id",many = @Many(select = "com.rabbiter.oes.mapper.ReplayMapper.findAllById"))
    })
    IPage<Message> findAll(Page page);

    @Select("select m.id,m.studentId,m.title,m.content,m.time, " +
            "COALESCE(s.studentName, t.teacherName, a.adminName, '未知用户') as publisherName, " +
            "CASE " +
            "WHEN s.studentId is not null THEN '学生' " +
            "WHEN t.teacherId is not null THEN '教师' " +
            "WHEN a.adminId is not null THEN '管理员' " +
            "ELSE '未知' END as publisherRole " +
            "from message m " +
            "left join student s on CAST(s.studentId AS CHAR) = m.studentId " +
            "left join teacher t on CAST(t.teacherId AS CHAR) = m.studentId " +
            "left join admin a on CAST(a.adminId AS CHAR) = m.studentId " +
            "where m.id = #{id}")
    @Results({
            @Result(property = "replays", column = "id",many = @Many(select = "com.rabbiter.oes.mapper.ReplayMapper.findAllById"))
    })
    Message findById(Integer id);

    @Delete("delete from message where id = #{id}")
    int delete(Integer id);

    @Update("update message set title = #{title}, content = #{content}, time = #{time} where " +
            "id = #{id}")
    int update(Message message);

    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("insert into message(studentId, title, content, time) values(#{studentId},#{title},#{content},#{time})")
    int add(Message message);
}
