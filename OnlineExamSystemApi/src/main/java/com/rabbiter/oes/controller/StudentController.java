package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.Student;
import com.rabbiter.oes.serviceimpl.StudentServiceImpl;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private StudentServiceImpl studentService;

    @GetMapping("/students/{page}/{size}/{name}/{grade}/{tel}/{institute}/{major}/{clazz}")
    public ApiResult findAll(@PathVariable Integer page, @PathVariable Integer size,
                             @PathVariable  String name, @PathVariable String grade,
                             @PathVariable String tel, @PathVariable String institute,
                             @PathVariable String major, @PathVariable String clazz,
                             HttpServletRequest request) {
        if (!isManager(request)) {
            return forbidden("只有教师或管理员可以查看学生列表");
        }
        Page<Student> studentPage = new Page<>(page,size);
        IPage<Student> res = studentService.findAll(
                studentPage, name, grade, tel, institute, major, clazz
        );
        return  ApiResultHandler.buildApiResult(200,"分页查询所有学生",res);
    }

    @GetMapping("/student/{studentId}")
    public ApiResult findById(@PathVariable("studentId") Long studentId, HttpServletRequest request) {
        logger.info("========== 进入 findById 方法 ==========");
        logger.info("查询学生信息，studentId: {}, 类型: {}", studentId, studentId.getClass().getName());
        if (!isManager(request) && !isCurrentStudent(request, studentId)) {
            return forbidden("只能查看自己的学生信息");
        }
        Student res = studentService.findById(studentId);
        logger.info("查询结果: {}", res);
        if (res != null) {
            res.setPwd(null);
            return ApiResultHandler.buildApiResult(200,"请求成功",res);
        } else {
            return ApiResultHandler.buildApiResult(404,"查询的用户不存在",null);
        }
    }

    @DeleteMapping("/student/{studentId}")
    public ApiResult deleteById(@PathVariable("studentId") Integer studentId, HttpServletRequest request) {
        if (!isManager(request)) {
            return forbidden("只有教师或管理员可以删除学生");
        }
        return ApiResultHandler.buildApiResult(200,"删除成功",studentService.deleteById(studentId));
    }

    @PutMapping("/studentPWD")
    public ApiResult updatePwd(@RequestBody Student student, HttpServletRequest request) {
        logger.info("修改密码请求 - studentId: {}", student.getStudentId());
        if (student.getStudentId() == null) {
            return ApiResultHandler.buildApiResult(400, "学生ID不能为空", null);
        }
        if (!isCurrentStudent(request, student.getStudentId())) {
            return forbidden("只能修改自己的密码");
        }
        if (student.getPwd() == null || student.getPwd().trim().length() < 6) {
            return ApiResultHandler.buildApiResult(400, "密码长度不能少于6位", null);
        }
        try {
            studentService.updatePwd(student);
            logger.info("密码修改成功 - studentId: {}", student.getStudentId());
            return ApiResultHandler.buildApiResult(200,"密码更新成功",null);
        } catch (Exception e) {
            logger.error("密码修改失败 - studentId: {}, 错误: {}", student.getStudentId(), e.getMessage());
            return ApiResultHandler.buildApiResult(500,"密码更新失败",null);
        }
    }
    @PutMapping("/student/{studentId}")
    public ApiResult updateById(@PathVariable("studentId") Long studentId,
                                @RequestBody Student student,
                                HttpServletRequest request) {
        student.setStudentId(studentId);
        return doUpdate(student, request);
    }

    @PutMapping("/student")
    public ApiResult update(@RequestBody Student student, HttpServletRequest request) {
        return doUpdate(student, request);
    }

    private ApiResult doUpdate(Student student, HttpServletRequest request) {
        if (student.getStudentId() == null) {
            return ApiResultHandler.buildApiResult(400, "学生ID不能为空", null);
        }
        Student existing = studentService.findById(student.getStudentId());
        if (existing == null) {
            return ApiResultHandler.buildApiResult(404, "学生不存在", null);
        }
        boolean manager = isManager(request);
        boolean self = isCurrentStudent(request, student.getStudentId());
        if (!manager && !self) {
            return forbidden("只能修改自己的信息");
        }

        if (self && !manager) {
            existing.setTel(student.getTel());
            existing.setEmail(student.getEmail());
        } else {
            existing.setStudentName(defaultIfNull(student.getStudentName(), existing.getStudentName()));
            existing.setGrade(defaultIfNull(student.getGrade(), existing.getGrade()));
            existing.setMajor(defaultIfNull(student.getMajor(), existing.getMajor()));
            existing.setClazz(defaultIfNull(student.getClazz(), existing.getClazz()));
            existing.setInstitute(defaultIfNull(student.getInstitute(), existing.getInstitute()));
            existing.setTel(defaultIfNull(student.getTel(), existing.getTel()));
            existing.setEmail(defaultIfNull(student.getEmail(), existing.getEmail()));
            existing.setCardId(defaultIfNull(student.getCardId(), existing.getCardId()));
            existing.setSex(defaultIfNull(student.getSex(), existing.getSex()));
        }
        existing.setRole("2");

        // 检查电话号码格式（必须是11位数字）
        if (existing.getTel() != null && !existing.getTel().isEmpty()) {
            if (!existing.getTel().matches("^\\d{11}$")) {
                return ApiResultHandler.buildApiResult(400, "电话号码必须是11位数字", null);
            }
            if (studentService.isTelExistsExcludeId(existing.getTel(), existing.getStudentId())) {
                return ApiResultHandler.buildApiResult(400, "该电话号码已被其他学生使用", null);
            }
        }
        // 检查身份证号格式
        if (existing.getCardId() != null && !existing.getCardId().isEmpty()) {
            if (!existing.getCardId().matches("^\\d{17}[\\dXx]$")) {
                return ApiResultHandler.buildApiResult(400, "身份证号格式不正确，应为18位", null);
            }
            if (studentService.isCardIdExistsExcludeId(existing.getCardId(), existing.getStudentId())) {
                return ApiResultHandler.buildApiResult(400, "该身份证号已被其他学生使用", null);
            }
        }
        // 检查邮箱格式
        if (existing.getEmail() != null && !existing.getEmail().isEmpty()) {
            if (!existing.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return ApiResultHandler.buildApiResult(400, "邮箱格式不正确", null);
            }
            if (studentService.isEmailExistsExcludeId(existing.getEmail(), existing.getStudentId())) {
                return ApiResultHandler.buildApiResult(400, "该邮箱已被其他学生使用", null);
            }
        }
        int res = studentService.update(existing);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200,"更新成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"更新失败",res);
    }

    @PostMapping("/student")
    public ApiResult add(@RequestBody Student student, HttpServletRequest request) {
        if (!isManager(request)) {
            return forbidden("只有教师或管理员可以新增学生");
        }
        // 检查电话号码格式（必须是11位数字）
        if (student.getTel() != null && !student.getTel().isEmpty()) {
            if (!student.getTel().matches("^\\d{11}$")) {
                return ApiResultHandler.buildApiResult(400, "电话号码必须是11位数字", null);
            }
            if (studentService.isTelExists(student.getTel())) {
                return ApiResultHandler.buildApiResult(400, "该电话号码已被使用", null);
            }
        }
        // 检查身份证号格式（18位，最后一位可以是X）
        if (student.getCardId() != null && !student.getCardId().isEmpty()) {
            if (!student.getCardId().matches("^\\d{17}[\\dXx]$")) {
                return ApiResultHandler.buildApiResult(400, "身份证号格式不正确，应为18位", null);
            }
            if (studentService.isCardIdExists(student.getCardId())) {
                return ApiResultHandler.buildApiResult(400, "该身份证号已被使用", null);
            }
        }
        // 检查邮箱格式
        if (student.getEmail() != null && !student.getEmail().isEmpty()) {
            if (!student.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                return ApiResultHandler.buildApiResult(400, "邮箱格式不正确", null);
            }
            if (studentService.isEmailExists(student.getEmail())) {
                return ApiResultHandler.buildApiResult(400, "该邮箱已被使用", null);
            }
        }
        student.setRole("2");
        int res = studentService.add(student);
        if (res == 1) {
            return ApiResultHandler.buildApiResult(200,"添加成功",null);
        }else {
            return ApiResultHandler.buildApiResult(400,"添加失败",null);
        }
    }
    
    // 获取学生总数
    @GetMapping("/students/count")
    public ApiResult getStudentCount(HttpServletRequest request) {
        if (!isManager(request)) {
            return forbidden("只有教师或管理员可以查看学生总数");
        }
        long count = studentService.count();
        return ApiResultHandler.buildApiResult(200, "获取学生总数成功", count);
    }

    private boolean isManager(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        Object role = request.getAttribute("currentRole");
        return "0".equals(role) || "1".equals(role);
    }

    private boolean isCurrentStudent(HttpServletRequest request, Long studentId) {
        if (request == null || studentId == null) {
            return false;
        }
        Object role = request.getAttribute("currentRole");
        Object userId = request.getAttribute("currentUserId");
        return "2".equals(role) && userId != null && String.valueOf(studentId).equals(String.valueOf(userId));
    }

    private ApiResult forbidden(String message) {
        return ApiResultHandler.buildApiResult(403, message, null);
    }

    private String defaultIfNull(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }
}
