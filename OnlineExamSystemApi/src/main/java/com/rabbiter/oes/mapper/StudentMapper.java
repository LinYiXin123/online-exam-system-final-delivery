package com.rabbiter.oes.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.Student;
import org.apache.ibatis.annotations.*;

@Mapper
public interface StudentMapper {

    /**
     * 分页查询所有学生
     * @param page
     * @return List<Student>
     */
    @Select("select * from student where " +
            "studentName like concat('%',#{name},'%') " +
            "and grade like concat('%',#{grade},'%') " +
            "and tel like concat('%',#{tel},'%') " +
            "and major like concat('%',#{major},'%') " +
            "and institute like concat('%',#{institute},'%') " +
            "and clazz like concat('%',#{clazz},'%')")
    IPage<Student> findAll(Page page, @Param("name") String name, @Param("grade") String grade,
                           @Param("tel") String tel,  @Param("institute") String institute,
                           @Param("major")String major, @Param("clazz") String clazz);

    @Select("select * from student where studentId = #{studentId}")
    Student findById(Long studentId);

    @Delete("delete from student where studentId = #{studentId}")
    int deleteById(Integer studentId);

    /**
     *更新所有学生信息
     * @param student 传递一个对象
     * @return 受影响的记录条数
     */
    @Update("update student set studentName = #{studentName},grade = #{grade},major = #{major},clazz = #{clazz}," +
            "institute = #{institute},tel = #{tel},email = #{email},cardId = #{cardId},sex = #{sex} " +
            "where studentId = #{studentId}")
    int update(Student student);

    /**
     * 更新密码
     * @param student
     * @return 受影响的记录条数
     */
    @Update("update student set pwd = #{pwd} where studentId = #{studentId}")
    int updatePwd(Student student);


    @Options(useGeneratedKeys = true,keyProperty = "studentId")
    @Insert("insert into student(studentName,grade,major,clazz,institute,tel,email,pwd,cardId,sex,role) values " +
            "(#{studentName},#{grade},#{major},#{clazz},#{institute},#{tel},#{email},#{pwd},#{cardId},#{sex},'2')")
    int add(Student student);
    
    // 获取学生总数
    @Select("select count(*) from student")
    long countAll();
    
    // 根据电话号码查询学生
    @Select("select * from student where tel = #{tel}")
    Student findByTel(@Param("tel") String tel);
    
    // 根据电话号码查询学生（排除指定ID）
    @Select("select * from student where tel = #{tel} and studentId != #{studentId}")
    Student findByTelExcludeId(@Param("tel") String tel, @Param("studentId") Long studentId);
    
    // 根据身份证号查询学生
    @Select("select * from student where cardId = #{cardId}")
    Student findByCardId(@Param("cardId") String cardId);
    
    // 根据身份证号查询学生（排除指定ID）
    @Select("select * from student where cardId = #{cardId} and studentId != #{studentId}")
    Student findByCardIdExcludeId(@Param("cardId") String cardId, @Param("studentId") Long studentId);
    
    // 根据邮箱查询学生
    @Select("select * from student where email = #{email}")
    Student findByEmail(@Param("email") String email);
    
    // 根据邮箱查询学生（排除指定ID）
    @Select("select * from student where email = #{email} and studentId != #{studentId}")
    Student findByEmailExcludeId(@Param("email") String email, @Param("studentId") Long studentId);
}
