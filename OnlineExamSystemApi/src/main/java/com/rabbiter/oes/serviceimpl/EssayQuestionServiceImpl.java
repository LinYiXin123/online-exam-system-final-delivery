package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.EssayQuestion;
import com.rabbiter.oes.mapper.EssayQuestionMapper;
import com.rabbiter.oes.service.EssayQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 简答题Service实现类
 */
@Service
public class EssayQuestionServiceImpl implements EssayQuestionService {
    
    @Autowired
    private EssayQuestionMapper essayQuestionMapper;
    
    @Override
    public List<EssayQuestion> findAll() {
        return essayQuestionMapper.selectList(null);
    }
    
    @Override
    public EssayQuestion findById(Integer questionId) {
        return essayQuestionMapper.selectById(questionId);
    }
    
    @Override
    public List<EssayQuestion> findBySubject(String subject) {
        return essayQuestionMapper.findBySubject(subject);
    }
    
    @Override
    public List<EssayQuestion> findBySubjectAndLevel(String subject, String level) {
        return essayQuestionMapper.findBySubjectAndLevel(subject, level);
    }
    
    @Override
    public int add(EssayQuestion question) {
        return essayQuestionMapper.insert(question);
    }
    
    @Override
    public int update(EssayQuestion question) {
        return essayQuestionMapper.updateById(question);
    }
    
    @Override
    public int delete(Integer questionId) {
        return essayQuestionMapper.deleteById(questionId);
    }
    
    @Override
    public List<String> findAllSubjects() {
        return essayQuestionMapper.findAllSubjects();
    }
    
    @Override
    public EssayQuestion findLastInserted() {
        return essayQuestionMapper.findLastInserted();
    }
}
