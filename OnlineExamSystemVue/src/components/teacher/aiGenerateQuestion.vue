<!-- AI智能生成题目页面 -->
<template>
  <div class="ai-generate">
    <el-card class="box-card ai-card-custom">
      <div slot="header" class="clearfix form-header" style="background: linear-gradient(135deg, #e8f0ff 0%, #dbeafe 100%); padding: 16px 20px; border-bottom: 1px solid #bfdbfe;">
        <span style="color: #1e40af; font-weight: 600; font-size: 18px;">AI智能生成题目</span>
      </div>

        <el-form ref="form" :model="form" :rules="rules" label-width="120px" class="generate-form">
        <!-- 基本信息 -->
        <el-form-item label="科目名称" prop="subject">
          <el-select v-model="form.subject" filterable allow-create placeholder="请选择或输入科目名称" style="width: 300px;">
            <el-option v-for="item in subjectOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>

        <el-divider content-position="left">题目设置</el-divider>

        <el-form-item label="题目类型" prop="questionType">
          <el-radio-group v-model="form.questionType">
            <el-radio label="multi">选择题</el-radio>
            <el-radio label="fill">填空题</el-radio>
            <el-radio label="judge">判断题</el-radio>
            <el-radio label="essay">主观题</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="生成数量" prop="count">
          <el-input-number v-model="form.count" :min="1" :max="10" controls-position="right"></el-input-number>
          <span style="margin-left: 10px; color: #909399;">（建议每次生成1-5道，避免超时）</span>
        </el-form-item>

        <el-divider content-position="left">难度设置</el-divider>

        <el-form-item label="难度等级">
          <el-select v-model="form.difficulty" placeholder="请选择难度" style="width: 200px;">
            <el-option label="简单" value="简单"></el-option>
            <el-option label="中等" value="中等"></el-option>
            <el-option label="困难" value="困难"></el-option>
          </el-select>
        </el-form-item>

        <el-divider content-position="left">内容设置</el-divider>

        <el-form-item label="知识点/章节">
          <el-input v-model="form.section" placeholder="可选，如：TCP/IP协议、数据结构" style="width: 300px;"></el-input>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input v-model="form.keywords" placeholder="可选，多个关键词用逗号分隔" style="width: 300px;"></el-input>
        </el-form-item>

        <el-form-item class="button-group-center">
          <div style="display: flex; justify-content: center; gap: 20px; width: 100%;">
            <el-button type="primary" @click="generateQuestions" :loading="generating" size="medium" style="min-width: 120px; height: 40px;">
              <i class="el-icon-magic-stick" style="font-size: 14px;"></i>
              {{ generating ? '生成中...' : '开始生成' }}
            </el-button>
            <el-button @click="resetForm" size="medium" style="min-width: 100px; height: 40px;">
              <i class="el-icon-refresh" style="font-size: 14px;"></i> 重置
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 生成结果 -->
    <el-card class="box-card result-card" v-if="questions.length > 0">
      <div slot="header" class="clearfix">
        <span>生成结果（共{{ questions.length }}道题）</span>
        <el-button type="success" size="small" style="float: right;" @click="saveAllQuestions" :loading="saving">
          {{ saving ? '保存中...' : '全部保存到题库' }}
        </el-button>
      </div>

      <div v-for="(q, index) in questions" :key="index" class="question-item">
        <div class="question-header">
          <el-tag size="small" :type="getTagType(form.questionType)">{{ getTypeName(form.questionType) }}</el-tag>
          <el-tag size="small" type="info" style="margin-left: 10px;">{{ q.level || '中等' }}</el-tag>
          <el-checkbox v-model="q.selected" style="float: right;">选中</el-checkbox>
        </div>

        <div class="question-content">
          <p><strong>题目：</strong>{{ q.question }}</p>

          <!-- 选择题选项 -->
          <div v-if="form.questionType === 'multi'" class="options">
            <p>A. {{ q.answerA }}</p>
            <p>B. {{ q.answerB }}</p>
            <p>C. {{ q.answerC }}</p>
            <p>D. {{ q.answerD }}</p>
          </div>

          <!-- 主观题参考答案和评分标准 -->
          <div v-if="form.questionType === 'essay'" class="essay-content">
            <p><strong>参考答案：</strong></p>
            <div class="reference-answer">{{ q.referenceAnswer }}</div>
            <p><strong>评分标准：</strong></p>
            <div class="scoring-criteria">{{ q.scoringCriteria }}</div>
          </div>

          <p class="answer" v-if="form.questionType !== 'essay'"><strong>答案：</strong>
            <el-tag type="success" size="small">{{ q.rightAnswer || q.answer }}</el-tag>
          </p>

          <p v-if="q.analysis" class="analysis"><strong>解析：</strong>{{ q.analysis }}</p>
        </div>

        <div class="question-actions">
          <el-button type="text" size="small" @click="editQuestion(index)">编辑</el-button>
          <el-button type="text" size="small" @click="removeQuestion(index)" style="color: #F56C6C;">删除</el-button>
        </div>
      </div>
    </el-card>

    <!-- 编辑题目对话框 -->
    <el-dialog title="编辑题目" :visible.sync="editDialogVisible" width="600px">
      <el-form :model="editingQuestion" label-width="80px" v-if="editingQuestion">
        <el-form-item label="题目">
          <el-input type="textarea" v-model="editingQuestion.question" :rows="3"></el-input>
        </el-form-item>

        <template v-if="form.questionType === 'multi'">
          <el-form-item label="选项A">
            <el-input v-model="editingQuestion.answerA"></el-input>
          </el-form-item>
          <el-form-item label="选项B">
            <el-input v-model="editingQuestion.answerB"></el-input>
          </el-form-item>
          <el-form-item label="选项C">
            <el-input v-model="editingQuestion.answerC"></el-input>
          </el-form-item>
          <el-form-item label="选项D">
            <el-input v-model="editingQuestion.answerD"></el-input>
          </el-form-item>
          <el-form-item label="正确答案">
            <el-select v-model="editingQuestion.rightAnswer">
              <el-option label="A" value="A"></el-option>
              <el-option label="B" value="B"></el-option>
              <el-option label="C" value="C"></el-option>
              <el-option label="D" value="D"></el-option>
            </el-select>
          </el-form-item>
        </template>

        <template v-else-if="form.questionType === 'essay'">
          <el-form-item label="参考答案">
            <el-input type="textarea" v-model="editingQuestion.referenceAnswer" :rows="4"></el-input>
          </el-form-item>
          <el-form-item label="评分标准">
            <el-input type="textarea" v-model="editingQuestion.scoringCriteria" :rows="3"></el-input>
          </el-form-item>
          <el-form-item label="分值">
            <el-input-number v-model="editingQuestion.score" :min="1" :max="20"></el-input-number>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="答案">
            <el-input v-model="editingQuestion.answer"></el-input>
          </el-form-item>
        </template>

        <el-form-item label="解析">
          <el-input type="textarea" v-model="editingQuestion.analysis" :rows="2"></el-input>
        </el-form-item>

        <el-form-item label="难度">
          <el-select v-model="editingQuestion.level">
            <el-option label="简单" value="简单"></el-option>
            <el-option label="中等" value="中等"></el-option>
            <el-option label="困难" value="困难"></el-option>
          </el-select>
        </el-form-item>
      </el-form>

      <span slot="footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEditedQuestion">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AiGenerateQuestion',
  data() {
    return {
      form: {
        subject: '',
        questionType: 'multi',
        count: 3,
        difficulty: '中等',
        section: '',
        keywords: ''
      },
      rules: {
        subject: [{ required: true, message: '请输入科目名称', trigger: 'blur' }],
        questionType: [{ required: true, message: '请选择题目类型', trigger: 'change' }],
        count: [{ required: true, message: '请输入生成数量', trigger: 'blur' }]
      },
      subjectOptions: [],
      questions: [],
      generating: false,
      saving: false,
      editDialogVisible: false,
      editingQuestion: null,
      editingIndex: -1
    }
  },
  created() {
    this.loadSubjects();
  },
  methods: {
    // 从数据库加载科目数据
    loadSubjects() {
      // 优先从考试表获取科目
      this.$axios.get('/api/exams').then(res => {
        if (res.data.code === 200 && res.data.data && res.data.data.length > 0) {
          const subjects = res.data.data
            .map(item => item.source)
            .filter(subject => subject && subject.trim())
            .filter((subject, index, arr) => arr.indexOf(subject) === index)
            .sort();
          
          this.subjectOptions = subjects;
          console.log('从考试数据获取的科目:', subjects);
        } else {
          // 备用：从选择题表获取
          this.loadSubjectsFromMulti();
        }
      }).catch(error => {
        console.error('获取考试科目失败:', error);
        this.loadSubjectsFromMulti();
      });
    },
    
    // 备用方法：从选择题表获取科目
    loadSubjectsFromMulti() {
      this.$axios.get('/api/multiQuestion/1/100').then(res => {
        if (res.data.code === 200 && res.data.data && res.data.data.records) {
          const subjects = res.data.data.records
            .map(item => item.subject)
            .filter(subject => subject && subject.trim())
            .filter((subject, index, arr) => arr.indexOf(subject) === index)
            .sort();
          
          this.subjectOptions = subjects;
          console.log('从选择题获取的科目:', subjects);
        } else {
          this.subjectOptions = [];
        }
      }).catch(error => {
        console.error('获取选择题科目失败:', error);
        this.subjectOptions = [];
      });
    },
    
    // 备用方法：从考试数据中获取科目
    loadSubjectsFromExams() {
      this.$axios.get('/api/exams/1/1000/@/@/@/@/@/@').then(res => {
        if (res.data.code === 200 && res.data.data && res.data.data.records) {
          const subjects = res.data.data.records
            .map(item => item.source) // 考试表中的科目字段是source
            .filter(subject => subject && subject.trim())
            .filter((subject, index, arr) => arr.indexOf(subject) === index)
            .sort();
          
          this.subjectOptions = subjects;
          console.log('从考试数据获取的科目:', subjects);
        } else {
          this.subjectOptions = [];
          console.log('数据库中没有考试数据');
        }
      }).catch(error => {
        console.error('获取考试科目数据也失败:', error);
        this.subjectOptions = [];
      });
    },
    generateQuestions() {
      this.$refs.form.validate(valid => {
        if (!valid) return

        this.generating = true
        this.questions = []

        this.$axios({
          url: '/api/ai/generate',
          method: 'post',
          data: this.form,
          timeout: 180000 // 3分钟超时
        }).then(res => {
          this.generating = false
          if (res.data.code === 200) {
            const data = res.data.data
            this.questions = (data.questions || []).map(q => ({
              ...q,
              selected: true
            }))
            if (this.questions.length > 0) {
              this.$message.success(`成功生成${this.questions.length}道题目`)
            } else {
              this.$message.warning('AI返回的内容无法解析为题目，请重试')
            }
          } else {
            this.$message.error(res.data.message || '生成失败')
          }
        }).catch(err => {
          this.generating = false
          if (err.code === 'ECONNABORTED') {
            this.$message.error('请求超时，请减少生成数量后重试')
          } else {
            this.$message.error('生成失败：' + (err.message || '未知错误'))
          }
        })
      })
    },
    saveAllQuestions() {
      const selectedQuestions = this.questions.filter(q => q.selected)
      if (selectedQuestions.length === 0) {
        this.$message.warning('请至少选择一道题目')
        return
      }

      this.saving = true
      this.$axios({
        url: '/api/ai/saveQuestions',
        method: 'post',
        data: {
          questionType: this.form.questionType,
          questions: selectedQuestions
        }
      }).then(res => {
        this.saving = false
        if (res.data.code === 200) {
          this.$message.success(res.data.message)
          // 移除已保存的题目
          this.questions = this.questions.filter(q => !q.selected)
        } else {
          this.$message.error(res.data.message || '保存失败')
        }
      }).catch(err => {
        this.saving = false
        this.$message.error('保存失败：' + (err.message || '未知错误'))
      })
    },
    editQuestion(index) {
      this.editingIndex = index
      this.editingQuestion = { ...this.questions[index] }
      this.editDialogVisible = true
    },
    saveEditedQuestion() {
      if (this.editingIndex >= 0) {
        this.questions[this.editingIndex] = { ...this.editingQuestion }
      }
      this.editDialogVisible = false
    },
    removeQuestion(index) {
      this.questions.splice(index, 1)
    },
    resetForm() {
      this.$refs.form.resetFields()
      this.questions = []
    },
    getTypeName(type) {
      const map = { multi: '选择题', fill: '填空题', judge: '判断题', essay: '主观题' }
      return map[type] || type
    },
    getTagType(type) {
      const map = { multi: 'primary', fill: 'success', judge: 'warning', essay: 'danger' }
      return map[type] || 'info'
    }
  }
}
</script>

<style lang="less" scoped>
.ai-generate {
  width: 100%;
  padding: 20px;
  background-color: var(--bg-primary);
  
  // AI智能生成题目卡片头部样式 - 统一为浅蓝色渐变
  .box-card .el-card__header {
    background: linear-gradient(135deg, #e8f0ff 0%, #dbeafe 100%) !important;
    border-bottom: 1px solid #bfdbfe !important;
    padding: 16px 20px !important;
  }
  
  .box-card .el-card__header .clearfix {
    color: #1e40af !important;
  }
  
  .box-card .el-card__header .clearfix span {
    color: #1e40af !important;
    font-weight: 600 !important;
    font-size: 18px !important;
  }
  
  .page-header {
    margin-bottom: 24px;
    text-align: center;
    
    .page-title {
      font-size: 28px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 8px 0;
    }
    
    .page-description {
      font-size: 16px;
      color: var(--text-secondary);
      margin: 0;
    }
  }
  
  .form-container {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
    
    .form-card {
      width: 100%;
      max-width: 800px;
      background: var(--card-bg);
      border-radius: 12px;
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
      border: 1px solid var(--border-color);
      
      // 暗色模式适配
      .dark-theme & {
        background: var(--bg-secondary);
        border: 1px solid var(--border-color);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
      }
      
      .card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
        
        i {
          color: var(--primary-color);
          font-size: 20px;
        }
      }
      
      .generate-form {
        padding: 20px 0;
        
        .el-form-item {
          margin-bottom: 24px;
          
          .el-form-item__label {
            color: var(--text-primary);
            font-weight: 500;
            font-size: 14px;
          }
        }
        
        .el-button {
          margin-right: 12px;
          padding: 12px 24px;
          font-size: 14px;
          border-radius: 8px;
          
          &:last-child {
            margin-right: 0;
          }
        }
        
        .action-button {
          min-width: 120px;
          height: 40px;
          font-size: 14px;
          font-weight: 500;
          border-radius: 8px;
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
          }
        }
        
        // 按钮居中样式
        .button-group-center {
          .el-form-item__content {
            display: flex;
            justify-content: center;
            margin-left: 0 !important;
          }
        }
      }
    }
  }
  
  .result-card {
    margin-top: 20px;
    background: var(--card-bg);
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    border: 1px solid var(--border-color);
    
    .clearfix {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      span {
        font-size: 16px;
        font-weight: 600;
        color: var(--text-primary);
      }
    }
  }
  
  .question-item {
    border: 1px solid var(--border-color);
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 16px;
    background-color: var(--bg-secondary);
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .question-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
    }
    
    .question-content {
      color: var(--text-primary);
      
      p {
        margin: 8px 0;
        line-height: 1.6;
      }
      
      .options {
        padding-left: 20px;
        
        p {
          margin: 6px 0;
        }
      }
      
      .answer {
        color: var(--success-color);
        font-weight: 500;
      }
      
      .analysis {
        color: var(--text-secondary);
        font-size: 13px;
        font-style: italic;
      }
      
      .essay-content {
        background: #f5f7fa;
        border-radius: 6px;
        padding: 12px;
        margin: 10px 0;
        
        .reference-answer {
          background: #e8f5e9;
          padding: 10px;
          border-radius: 4px;
          margin-bottom: 10px;
          color: #2e7d32;
          white-space: pre-wrap;
        }
        
        .scoring-criteria {
          background: #fff3e0;
          padding: 10px;
          border-radius: 4px;
          color: #e65100;
          white-space: pre-wrap;
        }
      }
    }
    
    .question-actions {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px dashed var(--border-color);
      text-align: right;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .ai-generate {
    padding: 16px;
    
    .form-container {
      .form-card {
        margin: 0 -16px;
        border-radius: 0;
        
        .generate-form {
          padding: 16px;
        }
      }
    }
    
    .question-item {
      padding: 16px;
      margin: 0 -16px 16px -16px;
      border-radius: 0;
    }
  }
}
</style>
