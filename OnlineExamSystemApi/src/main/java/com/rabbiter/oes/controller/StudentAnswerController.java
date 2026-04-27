package com.rabbiter.oes.controller;

import com.rabbiter.oes.entity.ApiResult;
import com.rabbiter.oes.entity.StudentAnswer;
import com.rabbiter.oes.mapper.StudentAnswerMapper;
import com.rabbiter.oes.util.AccessGuard;
import com.rabbiter.oes.util.ApiResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class StudentAnswerController {

    @Autowired
    private StudentAnswerMapper studentAnswerMapper;

    @Autowired
    private AccessGuard accessGuard;

    /**
     * 批量保存答题记录
     * @param answerList 答题记录列表
     * @return 保存结果
     */
    @PostMapping("/studentAnswer/batchSave")
    public ApiResult batchSave(@RequestBody List<StudentAnswer> answerList, HttpServletRequest request) {
        try {
            if (answerList == null || answerList.isEmpty()) {
                return ApiResultHandler.buildApiResult(400, "答题记录为空", null);
            }
            Integer examCode = answerList.get(0).getExamCode();
            Long studentId = answerList.get(0).getStudentId();
            if (examCode == null || studentId == null) {
                return ApiResultHandler.buildApiResult(400, "考试编号或学生编号不能为空", null);
            }
            if (!accessGuard.isCurrentUser(request, studentId)) {
                return accessGuard.forbidden("只能提交自己的答题记录");
            }
            boolean sameExamAndStudent = answerList.stream().allMatch(item ->
                    examCode.equals(item.getExamCode()) && studentId.equals(item.getStudentId()));
            if (!sameExamAndStudent) {
                return ApiResultHandler.buildApiResult(400, "答题记录必须属于同一场考试和同一学生", null);
            }

            studentAnswerMapper.deleteByExamAndStudent(examCode, studentId);
            int count = studentAnswerMapper.batchInsert(answerList);
            return ApiResultHandler.buildApiResult(200, "保存成功", count);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResultHandler.buildApiResult(500, "保存失败: " + e.getMessage(), null);
        }
    }

    /**
     * 查询答题记录
     * @param examCode 考试编号
     * @param studentId 学生ID
     * @return 答题记录列表
     */
    @GetMapping("/studentAnswer/{examCode}/{studentId}")
    public ApiResult getAnswerRecords(@PathVariable("examCode") Integer examCode,
                                      @PathVariable("studentId") Long studentId,
                                      HttpServletRequest request) {
        if (!accessGuard.canAccessStudent(request, studentId)) {
            return accessGuard.forbidden("只能查看自己的答题记录");
        }
        List<StudentAnswer> records = studentAnswerMapper.selectByExamAndStudent(examCode, studentId);
        return ApiResultHandler.buildApiResult(200, "查询成功", records);
    }
}
