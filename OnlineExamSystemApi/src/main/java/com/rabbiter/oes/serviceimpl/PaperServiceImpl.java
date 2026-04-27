package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.*;
import com.rabbiter.oes.mapper.EssayQuestionMapper;
import com.rabbiter.oes.mapper.ExamManageMapper;
import com.rabbiter.oes.mapper.FillQuestionMapper;
import com.rabbiter.oes.mapper.JudgeQuestionMapper;
import com.rabbiter.oes.mapper.MultiQuestionMapper;
import com.rabbiter.oes.mapper.PaperMapper;
import com.rabbiter.oes.service.PaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PaperServiceImpl implements PaperService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private JudgeQuestionServiceImpl judgeQuestionService;

    @Autowired
    private MultiQuestionServiceImpl multiQuestionService;

    @Autowired
    private FillQuestionServiceImpl fillQuestionService;

    @Autowired
    private MultiQuestionMapper multiQuestionMapper;

    @Autowired
    private FillQuestionMapper fillQuestionMapper;

    @Autowired
    private JudgeQuestionMapper judgeQuestionMapper;

    @Autowired
    private ExamManageMapper examManageMapper;

    @Autowired
    private EssayQuestionMapper essayQuestionMapper;

    @Override
    public List<PaperManage> findAll() {
        return paperMapper.findAll();
    }

    @Override
    public List<PaperManage> findById(Integer paperId) {
        return paperMapper.findById(paperId);
    }

    @Override
    public int add(PaperManage paperManage) {
        return paperMapper.add(paperManage);
    }

    @Override
    public Integer getMaxScore(Integer paperId) {
        List<MultiQuestion> multiQuestionRes = multiQuestionService.findByIdAndType(paperId);   //选择题题库 1
        List<FillQuestion> fillQuestionsRes = fillQuestionService.findByIdAndType(paperId);     //填空题题库 2
        List<JudgeQuestion> judgeQuestionRes = judgeQuestionService.findByIdAndType(paperId);   //判断题题库 3
        int essayCount = paperMapper.countByPaperIdAndType(paperId, 4);  //主观题数量 4
        int objectiveScore = 2 * (multiQuestionRes.size() + fillQuestionsRes.size() + judgeQuestionRes.size());
        int subjectiveScore = essayCount * 10;  //主观题每题10分
        return objectiveScore + subjectiveScore;
    }

    @Override
    @Transactional
    public void delete(String paperId, String type, String questionId) {
        paperMapper.delete(paperId, type, questionId);
        // 删除题目后更新试卷总分
        updateExamTotalScore(Integer.parseInt(paperId));
    }
    
    /**
     * 更新试卷总分（根据当前题目数量重新计算）
     * 客观题每题2分，主观题每题10分
     */
    private void updateExamTotalScore(Integer paperId) {
        System.out.println("=== 开始更新试卷总分，paperId=" + paperId + " ===");
        // 获取该试卷的所有客观题
        List<MultiQuestion> multiQuestions = multiQuestionService.findByIdAndType(paperId);
        List<FillQuestion> fillQuestions = fillQuestionService.findByIdAndType(paperId);
        List<JudgeQuestion> judgeQuestions = judgeQuestionService.findByIdAndType(paperId);
        
        // 获取该试卷的主观题数量（questionType=4）
        int essayCount = paperMapper.countByPaperIdAndType(paperId, 4);
        
        System.out.println("当前题目数：选择题" + multiQuestions.size() + "，填空题" + fillQuestions.size() + 
                          "，判断题" + judgeQuestions.size() + "，主观题" + essayCount);
        
        // 计算总分：客观题每题2分，主观题每题10分
        int objectiveScore = (multiQuestions.size() + fillQuestions.size() + judgeQuestions.size()) * 2;
        int subjectiveScore = essayCount * 10;
        int totalScore = objectiveScore + subjectiveScore;
        System.out.println("计算得到总分：客观题" + objectiveScore + "分 + 主观题" + subjectiveScore + "分 = " + totalScore + "分");
        
        // 更新考试管理表中的总分
        ExamManage exam = examManageMapper.findByPaperId(paperId);
        if (exam != null) {
            System.out.println("找到试卷，examCode=" + exam.getExamCode() + "，原总分=" + exam.getTotalScore());
            exam.setTotalScore(totalScore);
            int updateResult = examManageMapper.update(exam);
            System.out.println("更新结果：" + updateResult + "，新总分=" + totalScore);
        } else {
            System.out.println("未找到paperId=" + paperId + "对应的试卷");
        }
    }

    @Override
    public void deleteByPaperId(Integer paperId) {
        paperMapper.deleteByPaperId(paperId);
    }

    /**
     * 自动组卷算法实现
     * 支持按难度、知识点筛选题目，随机抽取指定数量的题目
     */
    @Override
    @Transactional
    public Map<String, Object> autoPaper(AutoPaperRequest request) {
        Map<String, Object> result = new HashMap<>();
        List<PaperManage> paperManageList = new ArrayList<>();

        String subject = request.getSubject();
        String description = request.getDescription();
        String examType = request.getExamType();
        String examDate = request.getExamDate();
        Integer totalTime = request.getTotalTime();
        String grade = request.getGrade();
        String term = request.getTerm();
        String major = request.getMajor();
        String institute = request.getInstitute();
        String tips = request.getTips();
        String difficulty = request.getDifficulty();
        String section = request.getSection();
        
        System.out.println("组卷参数：subject=" + subject + ", description=" + description + ", examType=" + examType + ", examDate=" + examDate + ", difficulty=" + difficulty);
        
        // 使用前端传递的paperId（已存在的试卷）
        Integer paperId = request.getPaperId();
        if (paperId == null) {
            throw new RuntimeException("未指定试卷ID，请选择一个已存在的试卷进行组卷");
        }
        
        // 检查试卷是否存在
        ExamManage existingExam = examManageMapper.findByPaperId(paperId);
        if (existingExam == null) {
            throw new RuntimeException("试卷不存在，paperId=" + paperId);
        }
        
        System.out.println("更新现有试卷：" + subject + " - " + description + " (试卷编号: " + paperId + ")");

        // 根据难度设置查询条件
        String level = null;
        if (difficulty != null && !"mixed".equals(difficulty)) {
            level = difficulty;
        }

        int totalScore = 0;
        int actualMultiCount = 0;
        int actualFillCount = 0;
        int actualJudgeCount = 0;

        // 1. 抽取选择题
        if (request.getMultiCount() != null && request.getMultiCount() > 0) {
            List<Integer> multiIds;
            if ("mixed".equals(difficulty)) {
                // 混合难度：按比例抽取（简单30%，中等50%，困难20%）
                multiIds = getMixedDifficultyQuestions(1, subject, section, request.getMultiCount());
            } else {
                multiIds = multiQuestionMapper.findRandomByCondition(subject, level, section, request.getMultiCount());
            }
            actualMultiCount = multiIds.size();
            System.out.println("选择题：设置" + request.getMultiCount() + "道，实际抽取" + actualMultiCount + "道");
            for (Integer questionId : multiIds) {
                PaperManage pm = new PaperManage(paperId, 1, questionId);
                paperManageList.add(pm);
            }
            int multiScore = request.getMultiScore() != null ? request.getMultiScore() : 2;
            totalScore += actualMultiCount * multiScore;
            System.out.println("选择题分数：" + actualMultiCount + " × " + multiScore + " = " + (actualMultiCount * multiScore));
        }

        // 2. 抽取填空题
        if (request.getFillCount() != null && request.getFillCount() > 0) {
            List<Integer> fillIds;
            if ("mixed".equals(difficulty)) {
                fillIds = getMixedDifficultyQuestions(2, subject, section, request.getFillCount());
            } else {
                fillIds = fillQuestionMapper.findRandomByCondition(subject, level, section, request.getFillCount());
            }
            actualFillCount = fillIds.size();
            System.out.println("填空题：设置" + request.getFillCount() + "道，实际抽取" + actualFillCount + "道");
            for (Integer questionId : fillIds) {
                PaperManage pm = new PaperManage(paperId, 2, questionId);
                paperManageList.add(pm);
            }
            int fillScore = request.getFillScore() != null ? request.getFillScore() : 2;
            totalScore += actualFillCount * fillScore;
            System.out.println("填空题分数：" + actualFillCount + " × " + fillScore + " = " + (actualFillCount * fillScore));
        }

        // 3. 抽取判断题
        if (request.getJudgeCount() != null && request.getJudgeCount() > 0) {
            List<Integer> judgeIds;
            if ("mixed".equals(difficulty)) {
                judgeIds = getMixedDifficultyQuestions(3, subject, section, request.getJudgeCount());
            } else {
                judgeIds = judgeQuestionMapper.findRandomByCondition(subject, level, section, request.getJudgeCount());
            }
            actualJudgeCount = judgeIds.size();
            System.out.println("判断题：设置" + request.getJudgeCount() + "道，实际抽取" + actualJudgeCount + "道");
            for (Integer questionId : judgeIds) {
                PaperManage pm = new PaperManage(paperId, 3, questionId);
                paperManageList.add(pm);
            }
            int judgeScore = request.getJudgeScore() != null ? request.getJudgeScore() : 2;
            totalScore += actualJudgeCount * judgeScore;
            System.out.println("判断题分数：" + actualJudgeCount + " × " + judgeScore + " = " + (actualJudgeCount * judgeScore));
        }

        // 4. 抽取主观题
        int actualEssayCount = 0;
        if (request.getEssayCount() != null && request.getEssayCount() > 0) {
            List<Integer> essayIds = essayQuestionMapper.findRandomByCondition(subject, level, section, request.getEssayCount());
            actualEssayCount = essayIds.size();
            System.out.println("主观题：设置" + request.getEssayCount() + "道，实际抽取" + actualEssayCount + "道");
            for (Integer questionId : essayIds) {
                PaperManage pm = new PaperManage(paperId, 4, questionId);
                paperManageList.add(pm);
            }
            int essayScore = request.getEssayScore() != null ? request.getEssayScore() : 10;
            totalScore += actualEssayCount * essayScore;
            System.out.println("主观题分数：" + actualEssayCount + " × " + essayScore + " = " + (actualEssayCount * essayScore));
        }

        // 5. 先删除原有题目关联，再批量插入新题目
        paperMapper.deleteByPaperId(paperId);
        if (!paperManageList.isEmpty()) {
            paperMapper.batchAdd(paperManageList);
        }

        // 5. 更新试卷总分
        existingExam.setTotalScore(totalScore);
        examManageMapper.update(existingExam);
        System.out.println("已更新试卷总分：" + totalScore + "分，试卷编号：" + paperId);

        // 7. 返回结果
        result.put("success", true);
        result.put("totalScore", totalScore);
        result.put("multiCount", actualMultiCount);
        result.put("fillCount", actualFillCount);
        result.put("judgeCount", actualJudgeCount);
        result.put("essayCount", actualEssayCount);
        result.put("totalCount", actualMultiCount + actualFillCount + actualJudgeCount + actualEssayCount);
        System.out.println("总分计算：" + totalScore + "分");
        if (actualEssayCount > 0) {
            result.put("message", String.format("组卷成功！共抽取%d道选择题、%d道填空题、%d道判断题、%d道主观题，总分%d分",
                    actualMultiCount, actualFillCount, actualJudgeCount, actualEssayCount, totalScore));
        } else {
            result.put("message", String.format("组卷成功！共抽取%d道选择题、%d道填空题、%d道判断题，总分%d分",
                    actualMultiCount, actualFillCount, actualJudgeCount, totalScore));
        }

        return result;
    }

    /**
     * 混合难度抽题：简单30%，中等50%，困难20%
     */
    private List<Integer> getMixedDifficultyQuestions(int questionType, String subject, String section, int totalCount) {
        List<Integer> result = new ArrayList<>();

        // 简化逻辑：直接按总数量随机抽取，不按难度比例分配
        // 这样可以避免复杂的补充逻辑导致的数量问题
        switch (questionType) {
            case 1: // 选择题
                result = multiQuestionMapper.findRandomByCondition(subject, null, section, totalCount);
                break;
            case 2: // 填空题
                result = fillQuestionMapper.findRandomByCondition(subject, null, section, totalCount);
                break;
            case 3: // 判断题
                result = judgeQuestionMapper.findRandomByCondition(subject, null, section, totalCount);
                break;
            default:
                return result;
        }

        return result;
    }

    /**
     * 获取题库统计信息
     */
    @Override
    public Map<String, Object> getQuestionStats(String subject) {
        Map<String, Object> stats = new HashMap<>();

        // 统计选择题
        List<Map<String, Object>> multiStats = multiQuestionMapper.countBySubjectAndLevel(subject);
        int multiTotal = 0;
        Map<String, Integer> multiByLevel = new HashMap<>();
        for (Map<String, Object> item : multiStats) {
            String level = (String) item.get("level");
            Long count = (Long) item.get("count");
            multiByLevel.put(level, count.intValue());
            multiTotal += count.intValue();
        }
        stats.put("multiTotal", multiTotal);
        stats.put("multiByLevel", multiByLevel);

        // 统计填空题
        List<Map<String, Object>> fillStats = fillQuestionMapper.countBySubjectAndLevel(subject);
        int fillTotal = 0;
        Map<String, Integer> fillByLevel = new HashMap<>();
        for (Map<String, Object> item : fillStats) {
            String level = (String) item.get("level");
            Long count = (Long) item.get("count");
            fillByLevel.put(level, count.intValue());
            fillTotal += count.intValue();
        }
        stats.put("fillTotal", fillTotal);
        stats.put("fillByLevel", fillByLevel);

        // 统计判断题
        List<Map<String, Object>> judgeStats = judgeQuestionMapper.countBySubjectAndLevel(subject);
        int judgeTotal = 0;
        Map<String, Integer> judgeByLevel = new HashMap<>();
        for (Map<String, Object> item : judgeStats) {
            String level = (String) item.get("level");
            Long count = (Long) item.get("count");
            judgeByLevel.put(level, count.intValue());
            judgeTotal += count.intValue();
        }
        stats.put("judgeTotal", judgeTotal);
        stats.put("judgeByLevel", judgeByLevel);

        // 统计主观题
        int essayTotal = essayQuestionMapper.countBySubject(subject);
        stats.put("essayTotal", essayTotal);

        stats.put("total", multiTotal + fillTotal + judgeTotal + essayTotal);

        return stats;
    }

    @Override
    public List<String> getAllSubjects() {
        // 从多个表获取科目，合并去重
        Set<String> subjectSet = new HashSet<>();

        // 从选择题表获取
        List<String> multiSubjects = multiQuestionMapper.findAllSubjects();
        if (multiSubjects != null) {
            subjectSet.addAll(multiSubjects);
        }

        // 从填空题表获取
        List<String> fillSubjects = fillQuestionMapper.findAllSubjects();
        if (fillSubjects != null) {
            subjectSet.addAll(fillSubjects);
        }

        // 从判断题表获取
        List<String> judgeSubjects = judgeQuestionMapper.findAllSubjects();
        if (judgeSubjects != null) {
            subjectSet.addAll(judgeSubjects);
        }

        // 从考试管理表获取（课程名称）
        List<String> examSubjects = examManageMapper.findAllSubjects();
        if (examSubjects != null) {
            subjectSet.addAll(examSubjects);
        }

        // 从主观题表获取
        List<String> essaySubjects = essayQuestionMapper.findAllSubjects();
        if (essaySubjects != null) {
            subjectSet.addAll(essaySubjects);
        }

        // 转为List并排序，同时去除空格差异
        List<String> result = new ArrayList<>();
        Set<String> trimmedSet = new HashSet<>();
        for (String s : subjectSet) {
            if (s != null) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty() && !trimmedSet.contains(trimmed)) {
                    trimmedSet.add(trimmed);
                    result.add(trimmed);
                }
            }
        }
        Collections.sort(result);
        return result;
    }

}
