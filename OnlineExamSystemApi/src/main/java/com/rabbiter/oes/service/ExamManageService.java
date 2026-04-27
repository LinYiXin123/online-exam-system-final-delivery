package com.rabbiter.oes.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbiter.oes.entity.ExamManage;

import java.util.List;

public interface ExamManageService {

    /**
     * 不分页查询所有考试信息
     */
    List<ExamManage> findAll();
    IPage<ExamManage> findAll(Page<ExamManage> page);

    ExamManage findById(Integer examCode);

    int delete(Integer examCode);

    int update(ExamManage exammanage);

    int add(ExamManage exammanage);

    ExamManage findOnlyPaperId();

    /**
     * 获取所有不重复的学院列表
     */
    List<String> findAllInstitutes();

    /**
     * 获取所有不重复的专业列表
     */
    List<String> findAllMajors();

    /**
     * 获取所有不重复的年级列表
     */
    List<String> findAllGrades();

}
