package com.rabbiter.oes.mapper;

import com.rabbiter.oes.entity.Admin;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.entity.Teacher;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoginMapper {

    @Select("select adminId,adminName,sex,tel,email,cardId,role from `admin` where adminId = #{username} and pwd = #{password}")
    public Admin adminLogin(@Param("username") Long username, @Param("password") String password);

    @Select("select teacherId,teacherName,institute,sex,tel,email,cardId," +
            "type,role from teacher where teacherId = #{username} and pwd = #{password}")
    public Teacher teacherLogin(@Param("username") Long username, @Param("password") String password);

    @Select("select studentId,studentName,grade,major,clazz,institute,tel," +
            "email,cardId,sex,role from student where studentId = #{username} and pwd = #{password}")
    public Student studentLogin(@Param("username") Long username, @Param("password") String password);

    // 检查学号是否已存在
    @Select("select count(*) from student where studentId = #{studentId}")
    public int checkStudentExists(@Param("studentId") Long studentId);

    // 学生注册
    @Insert("insert into student(studentId, studentName, pwd, sex, tel, email, grade, major, clazz, institute, role) " +
            "values(#{studentId}, #{studentName}, #{pwd}, #{sex}, #{tel}, #{email}, #{grade}, #{major}, #{clazz}, #{institute}, '2')")
    public int registerStudent(Student student);

    // 获取所有年级（去重）
    @Select("select distinct grade from student where grade is not null and grade != '' order by grade")
    public java.util.List<String> getAllGrades();

    // 获取所有专业（去重）
    @Select("select distinct major from student where major is not null and major != '' order by major")
    public java.util.List<String> getAllMajors();

    // 获取所有班级（去重）
    @Select("select distinct clazz from student where clazz is not null and clazz != '' order by clazz")
    public java.util.List<String> getAllClasses();

    // 获取所有学院（去重）
    @Select("select distinct institute from student where institute is not null and institute != '' order by institute")
    public java.util.List<String> getAllInstitutes();

    // 通过邮箱验证学生信息
    @Select("select count(*) from student where studentId = #{studentId} and email = #{email}")
    public int verifyStudentByEmail(@Param("studentId") Long studentId, @Param("email") String email);

    // 通过手机号验证学生信息
    @Select("select count(*) from student where studentId = #{studentId} and tel = #{tel}")
    public int verifyStudentByTel(@Param("studentId") Long studentId, @Param("tel") String tel);

    // 重置学生密码
    @Update("update student set pwd = #{newPwd} where studentId = #{studentId}")
    public int resetStudentPassword(@Param("studentId") Long studentId, @Param("newPwd") String newPwd);
}
