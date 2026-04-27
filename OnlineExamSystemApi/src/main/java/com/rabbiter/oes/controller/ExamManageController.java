package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.ExamManage;
import com.rabbiter.oes.serviceimpl.ExamManageServiceImpl;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class ExamManageController {

    @Autowired
    private ExamManageServiceImpl examManageService;

    @Autowired
    private AccessGuard accessGuard;

    @GetMapping("/exams")
    public ApiResult findAll(){
        System.out.println("不分页查询所有试卷");
        ApiResult apiResult;
        apiResult = ApiResultHandler.buildApiResult(200, "请求成功！", examManageService.findAll());
        return apiResult;
    }

    @GetMapping("/exams/{page}/{size}")
    public ApiResult findAll(@PathVariable("page") Integer page, @PathVariable("size") Integer size){
        System.out.println("分页查询所有试卷");
        ApiResult apiResult;
        Page<ExamManage> examManage = new Page<>(page,size);
        IPage<ExamManage> all = examManageService.findAll(examManage);
        apiResult = ApiResultHandler.buildApiResult(200, "请求成功！", all);
        return apiResult;
    }
    
    // 带查询条件的分页查询
    @GetMapping("/exams/{page}/{size}/{source}/{institute}/{major}/{grade}")
    public ApiResult findAllWithSearch(
            @PathVariable("page") Integer page, 
            @PathVariable("size") Integer size,
            @PathVariable("source") String source,
            @PathVariable("institute") String institute,
            @PathVariable("major") String major,
            @PathVariable("grade") String grade) {
        System.out.println("带条件分页查询试卷");
        Page<ExamManage> examManage = new Page<>(page, size);
        // @表示空值
        source = "@".equals(source) ? "" : source;
        institute = "@".equals(institute) ? "" : institute;
        major = "@".equals(major) ? "" : major;
        grade = "@".equals(grade) ? "" : grade;
        IPage<ExamManage> all = examManageService.findAllWithSearch(examManage, source, institute, major, grade);
        return ApiResultHandler.buildApiResult(200, "请求成功！", all);
    }

    @GetMapping("/exam/{examCode}")
    public ApiResult findById(@PathVariable("examCode") Integer examCode){
        System.out.println("根据ID查找");
        ExamManage res = examManageService.findById(examCode);
        if(res == null) {
            return ApiResultHandler.buildApiResult(10000,"考试编号不存在",null);
        }
        return ApiResultHandler.buildApiResult(200,"请求成功！",res);
    }

    @DeleteMapping("/exam/{examCode}")
    public ApiResult deleteById(@PathVariable("examCode") Integer examCode, HttpServletRequest request){
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以删除考试");
        }
        int res = examManageService.delete(examCode);
        return ApiResultHandler.buildApiResult(200,"删除成功",res);
    }

    @PutMapping("/exam")
    public ApiResult update(@RequestBody ExamManage exammanage, HttpServletRequest request){
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以修改考试");
        }
        int res = examManageService.update(exammanage);
//        if (res == 0) {
//            return ApiResultHandler.buildApiResult(20000,"请求参数错误");
//        }
        System.out.print("更新操作执行---");
        return ApiResultHandler.buildApiResult(200,"更新成功",res);
    }

    @PostMapping("/exam")
    public ApiResult add(@RequestBody ExamManage exammanage, HttpServletRequest request){
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以新增考试");
        }
        int res = examManageService.add(exammanage);
        if (res ==1) {
            return ApiResultHandler.buildApiResult(200, "添加成功", res);
        } else {
            return  ApiResultHandler.buildApiResult(400,"添加失败",res);
        }
    }

    @GetMapping("/examManagePaperId")
    public ApiResult findOnlyPaperId(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以生成试卷编号");
        }
        ExamManage res = examManageService.findOnlyPaperId();
        if (res != null) {
            return ApiResultHandler.buildApiResult(200,"请求成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"请求失败",res);
    }

    /**
     * 获取所有不重复的学院、专业、年级列表
     */
    @GetMapping("/examOptions")
    public ApiResult getExamOptions(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看考试配置选项");
        }
        java.util.Map<String, Object> options = new java.util.HashMap<>();
        options.put("institutes", examManageService.findAllInstitutes());
        options.put("majors", examManageService.findAllMajors());
        options.put("grades", examManageService.findAllGrades());
        return ApiResultHandler.buildApiResult(200, "获取成功", options);
    }
}
