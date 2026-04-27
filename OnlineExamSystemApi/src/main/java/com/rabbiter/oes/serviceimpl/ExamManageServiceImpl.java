package com.rabbiter.oes.serviceimpl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ExamManage;
import com.rabbiter.oes.mapper.ExamManageMapper;
import com.rabbiter.oes.service.ExamManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamManageServiceImpl implements ExamManageService {
    @Autowired
    private ExamManageMapper examManageMapper;
    @Autowired
    private PaperServiceImpl paperService;

    private void setMaxScore(List<ExamManage> examManageList) {
        for (ExamManage examManage : examManageList) {
            try {
                // 始终根据试卷内容计算实际总分
                if (examManage.getPaperId() != null) {
                    Integer calculatedScore = paperService.getMaxScore(examManage.getPaperId());
                    if (calculatedScore != null && calculatedScore > 0) {
                        examManage.setTotalScore(calculatedScore);
                    } else {
                        examManage.setTotalScore(100);
                    }
                } else {
                    examManage.setTotalScore(100);
                }
            } catch (Exception e) {
                System.err.println("计算试卷总分异常，examCode: " + examManage.getExamCode() + ", 错误: " + e.getMessage());
                examManage.setTotalScore(100);
            }
        }
    }

    @Override
    public List<ExamManage> findAll() {
        Page<ExamManage> examManage = new Page<>(0,9999);
        List<ExamManage> examManageList = examManageMapper.findAll(examManage).getRecords();
        setMaxScore(examManageList);
        return examManageList;
    }

    @Override
    public IPage<ExamManage> findAll(Page<ExamManage> page) {
        IPage<ExamManage> iPage = examManageMapper.findAll(page);
        setMaxScore(iPage.getRecords());
        return iPage;
    }

    @Override
    public ExamManage findById(Integer examCode) {
        ExamManage examManage = examManageMapper.findById(examCode);
        // 始终根据试卷内容计算实际总分
        if (examManage.getPaperId() != null) {
            Integer calculatedScore = paperService.getMaxScore(examManage.getPaperId());
            if (calculatedScore != null && calculatedScore > 0) {
                examManage.setTotalScore(calculatedScore);
            }
        }
        return examManage;
    }

    @Override
    public int delete(Integer examCode) {
        // 移除题目关联
        ExamManage examManage = examManageMapper.findById(examCode);
        if(examManage == null) {
            return 0;
        }
        paperService.deleteByPaperId(examManage.getPaperId());
        return examManageMapper.delete(examCode);
    }

    @Override
    public int update(ExamManage exammanage) {
        return examManageMapper.update(exammanage);
    }

    @Override
    public int add(ExamManage exammanage) {
        return examManageMapper.add(exammanage);
    }

    @Override
    public ExamManage findOnlyPaperId() {
//        int max=999999;
//        int min=1;
//        Random random = new Random();
//        ExamManage examManage = new ExamManage();
//        examManage.setPaperId(random.nextInt(max)%(max-min+1) + min);
//        return examManage;
        Integer maxPaperId = examManageMapper.findMaxPaperId();
        ExamManage examManage = new ExamManage();
        examManage.setPaperId((maxPaperId != null) ? maxPaperId + 1 : 1001);
        return examManage;
    }

    @Override
    public List<String> findAllInstitutes() {
        return examManageMapper.findAllInstitutes();
    }

    @Override
    public List<String> findAllMajors() {
        return examManageMapper.findAllMajors();
    }

    @Override
    public List<String> findAllGrades() {
        return examManageMapper.findAllGrades();
    }
    
    // 带查询条件的分页查询
    public IPage<ExamManage> findAllWithSearch(Page<ExamManage> page, String source, 
            String institute, String major, String grade) {
        IPage<ExamManage> iPage = examManageMapper.findAllWithSearch(page, source, institute, major, grade);
        setMaxScore(iPage.getRecords());
        return iPage;
    }
}
