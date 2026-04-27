package com.rabbiter.oes.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.serviceimpl.AnswerServiceImpl;
import com.rabbiter.oes.serviceimpl.ExamDetailServiceImpl;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import com.rabbiter.oes.vo.AnswerVO;
import com.rabbiter.oes.vo.ExamDetailVO;
import com.rabbiter.oes.vo.QuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class AnswerController {

    @Autowired
    private AnswerServiceImpl answerService;

    @Autowired
    private ExamDetailServiceImpl examDetailService;

    @Autowired
    private AccessGuard accessGuard;

    @GetMapping("/answers/{page}/{size}/{subject}/{section}/{question}/{questionType}")
    public ApiResult findAllQuestion(
            @PathVariable("page") Integer page, @PathVariable("size") Integer size,
            @PathVariable("subject") String subject, @PathVariable("section") String section,
            @PathVariable("question") String question, @PathVariable("questionType") String questionType,
            HttpServletRequest request){
       if (!accessGuard.isTeacherOrAdmin(request)) {
           return accessGuard.forbidden("只有教师或管理员可以查看题库");
       }
       Page<AnswerVO> answerVOPage = new Page<>(page,size);
       IPage<AnswerVO> answerVOIPage = answerService.findAll(answerVOPage, subject, section, question, questionType);
       return ApiResultHandler.buildApiResult(200,"查询所有题库", answerVOIPage);
    }

    /**
     * 根据类型和id获取题目
     *
     * @param type 类型
     * @param questionId 题目id
     * @return 题目信息
     */
    @GetMapping("/answers/{type}/{questionId}")
    public ApiResult findByIdAndType(
            @PathVariable("type") String type, @PathVariable("questionId") Long questionId,
            HttpServletRequest request
    ) {
        if (!accessGuard.isTeacherOrAdmin(request)) {
            return accessGuard.forbidden("只有教师或管理员可以查看题目详情");
        }
        QuestionVO questionVO = answerService.findByIdAndType(type, questionId);
        return ApiResultHandler.buildApiResult(200, "查询题目", questionVO);
    }

    /**
     * 获取答题详情（用于查看答卷）
     * @param examCode 考试编号
     * @param studentId 学生ID
     * @return 答题详情
     */
    @GetMapping("/answer/detail/{examCode}/{studentId}")
    public ApiResult getExamDetail(
            @PathVariable("examCode") Integer examCode,
            @PathVariable("studentId") Integer studentId,
            HttpServletRequest request
    ) {
        if (!accessGuard.canAccessStudent(request, studentId)) {
            return accessGuard.forbidden("只能查看自己的答卷详情");
        }
        ExamDetailVO examDetailVO = examDetailService.getExamDetail(examCode, studentId);
        return ApiResultHandler.buildApiResult(200, "查询答题详情成功", examDetailVO);
    }

}
