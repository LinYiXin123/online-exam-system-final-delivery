package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.mapper.EssayQuestionMapper;
import com.rabbiter.oes.serviceimpl.FillQuestionServiceImpl;
import com.rabbiter.oes.serviceimpl.JudgeQuestionServiceImpl;
import com.rabbiter.oes.serviceimpl.MultiQuestionServiceImpl;
import com.rabbiter.oes.serviceimpl.PaperServiceImpl;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaperController {

    @Autowired
    private PaperServiceImpl paperService;

    @Autowired
    private JudgeQuestionServiceImpl judgeQuestionService;

    @Autowired
    private MultiQuestionServiceImpl multiQuestionService;

    @Autowired
    private FillQuestionServiceImpl fillQuestionService;
    
    @Autowired
    private EssayQuestionMapper essayQuestionMapper;

    @Autowired
    private AccessGuard accessGuard;
    
    @GetMapping("/papers")
    public ApiResult<PaperManage> findAll(HttpServletRequest request) {
       if (!accessGuard.isTeacherOrAdmin(request)) {
           return accessGuard.forbidden("只有教师或管理员可以查看试卷管理数据");
       }
       ApiResult res =  ApiResultHandler.buildApiResult(200,"请求成功",paperService.findAll());
       return  res;
    }

    @GetMapping("/paper/{paperId}")
    public Map<Integer, List<?>> findById(@PathVariable("paperId") Integer paperId) {
        List<MultiQuestion> multiQuestionRes = multiQuestionService.findByIdAndType(paperId);   //选择题题库 1
        List<FillQuestion> fillQuestionsRes = fillQuestionService.findByIdAndType(paperId);     //填空题题库 2
        List<JudgeQuestion> judgeQuestionRes = judgeQuestionService.findByIdAndType(paperId);   //判断题题库 3
        List<EssayQuestion> essayQuestionRes = essayQuestionMapper.findByPaperId(paperId);      //主观题题库 4
        Map<Integer, List<?>> map = new HashMap<>();
        map.put(1,multiQuestionRes);
        map.put(2,fillQuestionsRes);
        map.put(3,judgeQuestionRes);
        map.put(4,essayQuestionRes);
        return  map;
    }

    @GetMapping("/practice/{source}")
    public Map<Integer, List<?>> findBySubject(@PathVariable("source") String subject) {
        List<MultiQuestion> multiQuestionRes = multiQuestionService.findBySubject(subject);   //选择题题库 1
        List<FillQuestion> fillQuestionsRes = fillQuestionService.findBySubject(subject);     //填空题题库 2
        List<JudgeQuestion> judgeQuestionRes = judgeQuestionService.findBySubject(subject);   //判断题题库 3
        Map<Integer, List<?>> map = new HashMap<>();
        map.put(1,multiQuestionRes);
        map.put(2,fillQuestionsRes);
        map.put(3,judgeQuestionRes);
        return  map;
    }

    @PostMapping("/paperManage")
    public ApiResult add(@RequestBody PaperManage paperManage, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以修改试卷");
        }
        int res = paperService.add(paperManage);
        if (res != 0) {
            return ApiResultHandler.buildApiResult(200,"添加成功",res);
        }
        return ApiResultHandler.buildApiResult(400,"添加失败",res);
    }

    /**
     * 删除试卷中的某条试题
     *
     * @param paperId 试卷id
     * @param type 题目类型。1选择，2填空，3判断
     * @param questionId 题目id
     */
    @DeleteMapping("/paper/{paperId}/{type}/{questionId}")
    public ApiResult delete(
            @PathVariable("paperId") String paperId,
            @PathVariable("type") String type,
            @PathVariable("questionId") String questionId,
            HttpServletRequest request
    ) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以删除试卷题目");
        }
        paperService.delete(paperId, type, questionId);
        return ApiResultHandler.buildApiResult(200,"删除成功", null);
    }

    /**
     * 自动组卷接口
     * 根据科目、题型数量、难度等参数自动生成试卷
     */
    @PostMapping("/paper/auto")
    public ApiResult autoPaper(@RequestBody AutoPaperRequest request, HttpServletRequest httpRequest) {
        if (!accessGuard.isTeacherOrAdmin(httpRequest)) {
            return accessGuard.forbidden("只有教师或管理员可以自动组卷");
        }
        System.out.println("收到自动组卷请求：" + request);
        try {
            Map<String, Object> result = paperService.autoPaper(request);
            System.out.println("组卷结果：" + result);
            return ApiResultHandler.buildApiResult(200, (String) result.get("message"), result);
        } catch (Exception e) {
            System.out.println("组卷异常：" + e.getMessage());
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "自动组卷失败：" + e.getMessage(), null);
        }
    }

    /**
     * 获取题库统计信息
     * 用于自动组卷时显示可用题目数量
     */
    @GetMapping("/paperStats/{subject}")
    public ApiResult getQuestionStats(@PathVariable("subject") String subject, HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看题库统计");
        }
        try {
            Map<String, Object> stats = paperService.getQuestionStats(subject);
            return ApiResultHandler.buildApiResult(200, "获取成功", stats);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取统计信息失败：" + e.getMessage(), null);
        }
    }

    /**
     * 获取题库中所有科目列表
     */
    @GetMapping("/subjects")
    public ApiResult getAllSubjects(HttpServletRequest request) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看科目列表");
        }
        try {
            List<String> subjects = paperService.getAllSubjects();
            return ApiResultHandler.buildApiResult(200, "获取成功", subjects);
        } catch (Exception e) {
            return ApiResultHandler.buildApiResult(500, "获取科目列表失败：" + e.getMessage(), null);
        }
    }
}
