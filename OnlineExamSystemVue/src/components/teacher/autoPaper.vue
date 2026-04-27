<!-- 自动组卷页面 -->
<template>
  <div class="auto-paper">
    <el-card class="box-card">
      <div slot="header" class="clearfix form-header" style="background: linear-gradient(135deg, #e8f0ff 0%, #dbeafe 100%); padding: 16px 20px; border-bottom: 1px solid #bfdbfe;">
        <span style="color: #1e40af; font-weight: 600; font-size: 18px;">智能自动组卷</span>
      </div>

      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <!-- 选择试卷 -->
        <el-form-item label="选择试卷" prop="examCode">
          <el-select v-model="form.examCode" filterable placeholder="请选择需要组卷的试卷" @change="onExamChange" style="width: 300px;">
            <el-option v-for="item in availableExams" :key="item.examCode" :label="item.source + ' - ' + item.description" :value="item.examCode"></el-option>
          </el-select>
          <el-button type="primary" size="small" @click="refreshExams" style="margin-left: 10px;">刷新列表</el-button>
        </el-form-item>

        <!-- 科目名称（只读） -->
        <el-form-item label="科目名称">
          <el-input v-model="form.subject" disabled placeholder="选择试卷后自动填充" style="width: 300px;"></el-input>
          <el-button type="primary" size="small" @click="refreshStats" style="margin-left: 10px;">刷新题库</el-button>
        </el-form-item>

        <!-- 介绍（只读） -->
        <el-form-item label="介绍">
          <el-input v-model="form.description" disabled placeholder="选择试卷后自动填充"></el-input>
        </el-form-item>

        <!-- 考试信息详情 -->
        <el-divider content-position="left">考试信息详情</el-divider>
        <div class="exam-info-detail" v-if="selectedExam">
          <el-descriptions :column="4" border size="small">
            <el-descriptions-item label="所属学院">{{ selectedExam.institute || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属专业">{{ selectedExam.major || '-' }}</el-descriptions-item>
            <el-descriptions-item label="年级">{{ selectedExam.grade || '-' }}</el-descriptions-item>
            <el-descriptions-item label="考试日期">{{ selectedExam.examDate || '-' }}</el-descriptions-item>
            <el-descriptions-item label="持续时间">{{ selectedExam.totalTime ? selectedExam.totalTime + '分钟' : '-' }}</el-descriptions-item>
            <el-descriptions-item label="总分">{{ selectedExam.totalScore || '-' }}分</el-descriptions-item>
            <el-descriptions-item label="试卷类型">{{ selectedExam.type || '-' }}</el-descriptions-item>
            <el-descriptions-item label="考生提示">{{ selectedExam.tips || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div v-else class="exam-info-placeholder">
          <span>请先选择试卷查看详情</span>
        </div>

        <!-- 题库统计信息 -->
        <el-form-item label="题库统计" v-if="stats.total > 0" style="margin-top: 20px;">
          <el-tag type="info" style="margin-right: 10px;">
            选择题: {{ stats.multiTotal }}道
          </el-tag>
          <el-tag type="info" style="margin-right: 10px;">
            填空题: {{ stats.fillTotal }}道
          </el-tag>
          <el-tag type="info" style="margin-right: 10px;">
            判断题: {{ stats.judgeTotal }}道
          </el-tag>
          <el-tag type="info">
            主观题: {{ stats.essayTotal || 0 }}道
          </el-tag>
        </el-form-item>

        <el-divider content-position="left">题型设置</el-divider>

        <!-- 选择题设置 -->
        <el-form-item label="选择题数量">
          <el-input-number v-model="form.multiCount" :min="0" :max="stats.multiTotal || 100" controls-position="right"></el-input-number>
          <span class="score-label">每题分数：</span>
          <el-input-number v-model="form.multiScore" :min="1" :max="20" controls-position="right"></el-input-number>
          <span class="score-unit">分</span>
        </el-form-item>

        <!-- 填空题设置 -->
        <el-form-item label="填空题数量">
          <el-input-number v-model="form.fillCount" :min="0" :max="stats.fillTotal || 100" controls-position="right"></el-input-number>
          <span class="score-label">每题分数：</span>
          <el-input-number v-model="form.fillScore" :min="1" :max="20" controls-position="right"></el-input-number>
          <span class="score-unit">分</span>
        </el-form-item>

        <!-- 判断题设置 -->
        <el-form-item label="判断题数量">
          <el-input-number v-model="form.judgeCount" :min="0" :max="stats.judgeTotal || 100" controls-position="right"></el-input-number>
          <span class="score-label">每题分数：</span>
          <el-input-number v-model="form.judgeScore" :min="1" :max="20" controls-position="right"></el-input-number>
          <span class="score-unit">分</span>
        </el-form-item>

        <!-- 主观题设置 -->
        <el-form-item label="主观题数量">
          <el-input-number v-model="form.essayCount" :min="0" :max="stats.essayTotal || 100" controls-position="right"></el-input-number>
          <span class="score-label">每题分数：</span>
          <el-input-number v-model="form.essayScore" :min="1" :max="50" controls-position="right"></el-input-number>
          <span class="score-unit">分</span>
        </el-form-item>

        <el-divider content-position="left">难度设置</el-divider>

        <el-form-item label="难度要求">
          <el-radio-group v-model="form.difficulty">
            <el-radio label="mixed">混合难度（推荐）</el-radio>
            <el-radio label="简单">简单</el-radio>
            <el-radio label="中等">中等</el-radio>
            <el-radio label="困难">困难</el-radio>
          </el-radio-group>
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            混合难度将按照 简单30% + 中等50% + 困难20% 的比例抽取题目
          </div>
        </el-form-item>

        <el-form-item label="知识点/章节">
          <el-input v-model="form.section" placeholder="可选，留空则从所有章节抽取" style="width: 300px;"></el-input>
        </el-form-item>

        <!-- 预计总分 -->
        <el-form-item label="预计总分">
          <span class="estimated-score">{{ calculateTotalScore }} 分</span>
          <span style="margin-left: 20px; color: #909399;">
            （选择题 {{ form.multiCount * form.multiScore }} + 填空题 {{ form.fillCount * form.fillScore }} + 判断题 {{ form.judgeCount * form.judgeScore }} + 主观题 {{ form.essayCount * form.essayScore }}）
          </span>
        </el-form-item>

        <el-form-item class="button-group-center">
          <div style="display: flex; justify-content: center; gap: 20px; width: 100%;">
            <el-button type="primary" @click="submitForm" :loading="loading" size="medium" style="min-width: 140px; height: 40px;">
              开始自动组卷
            </el-button>
            <el-button @click="resetForm" size="medium" style="min-width: 100px; height: 40px;">
              <i class="el-icon-refresh" style="font-size: 14px;"></i> 重置
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 组卷结果对话框 -->
    <el-dialog title="组卷结果" :visible.sync="resultDialogVisible" width="500px">
      <div v-if="result">
        <el-result icon="success" :title="result.message">
          <template slot="extra">
            <div style="text-align: left; padding: 20px;">
              <p><strong>选择题：</strong>{{ result.multiCount }} 道</p>
              <p><strong>填空题：</strong>{{ result.fillCount }} 道</p>
              <p><strong>判断题：</strong>{{ result.judgeCount }} 道</p>
              <p v-if="result.essayCount > 0"><strong>主观题：</strong>{{ result.essayCount }} 道</p>
              <p><strong>总题数：</strong>{{ result.totalCount }} 道</p>
              <p><strong>总分：</strong>{{ result.totalScore }} 分</p>
            </div>
            <el-button type="primary" @click="goToExamList">查看试卷列表</el-button>
          </template>
        </el-result>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AutoPaper',
  data() {
    return {
      form: {
        examCode: '',
        subject: '',
        description: '',
        multiCount: 10,
        fillCount: 5,
        judgeCount: 5,
        essayCount: 2,
        multiScore: 2,
        fillScore: 2,
        judgeScore: 2,
        essayScore: 10,
        difficulty: 'mixed',
        section: ''
      },
      rules: {
        examCode: [
          { required: true, message: '请选择试卷', trigger: 'change' }
        ]
      },
      availableExams: [],  // 可用于组卷的试卷列表（判断题和填空题都为空的）
      selectedExam: null,  // 当前选中的试卷详情
      stats: {
        total: 0,
        multiTotal: 0,
        fillTotal: 0,
        judgeTotal: 0,
        essayTotal: 0
      },
      loading: false,
      resultDialogVisible: false,
      result: null
    }
  },
  created() {
    this.loadAvailableExams();
  },
  computed: {
    calculateTotalScore() {
      return this.form.multiCount * this.form.multiScore +
             this.form.fillCount * this.form.fillScore +
             this.form.judgeCount * this.form.judgeScore +
             this.form.essayCount * this.form.essayScore
    }
  },
  methods: {
    // 加载可用于组卷的试卷列表（判断题和填空题都为空的试卷）
    loadAvailableExams() {
      this.$axios.get('/api/exams').then(res => {
        if (res.data.code === 200 && res.data.data) {
          // 过滤出判断题和填空题都为空的试卷
          this.checkAndFilterExams(res.data.data);
        } else {
          this.availableExams = [];
        }
      }).catch(error => {
        console.error('获取试卷列表失败:', error);
        this.availableExams = [];
      });
    },
    
    // 检查并过滤试卷（只保留填空题和判断题都为空的）
    async checkAndFilterExams(exams) {
      const available = [];
      for (const exam of exams) {
        try {
          // 获取该试卷的题目，API返回格式：{1: [选择题], 2: [填空题], 3: [判断题]}
          const res = await this.$axios.get(`/api/paper/${exam.paperId}`);
          const data = res.data;
          // 检查是否有填空题(key=2)和判断题(key=3)
          const fillQuestions = data['2'] || [];
          const judgeQuestions = data['3'] || [];
          const hasFill = fillQuestions.length > 0;
          const hasJudge = judgeQuestions.length > 0;
          console.log(`试卷${exam.source}: 填空题${fillQuestions.length}道, 判断题${judgeQuestions.length}道`);
          // 填空题和判断题都为空的试卷才可以组卷
          if (!hasFill && !hasJudge) {
            available.push(exam);
          }
        } catch (e) {
          console.error(`获取试卷${exam.paperId}题目失败:`, e);
          // 获取失败的试卷也加入（可能是新试卷）
          available.push(exam);
        }
      }
      this.availableExams = available;
      console.log('可组卷试卷数量:', available.length);
      if (available.length === 0) {
        this.$message.info('暂无可组卷的试卷（所有试卷都已有填空题和判断题）');
      }
    },
    
    // 刷新试卷列表
    refreshExams() {
      this.loadAvailableExams();
      this.$message.success('已刷新试卷列表');
    },
    
    // 选择试卷时触发
    onExamChange(examCode) {
      const exam = this.availableExams.find(e => e.examCode === examCode);
      if (exam) {
        this.selectedExam = exam;
        this.form.subject = exam.source;
        this.form.description = exam.description;
        // 加载题库统计
        this.loadStats();
      } else {
        this.selectedExam = null;
        this.form.subject = '';
        this.form.description = '';
      }
    },
    
    // 加载题库统计信息
    loadStats() {
      if (!this.form.subject) {
        this.$message.warning('请先选择试卷');
        return;
      }
      this.$axios(`/api/paperStats/${encodeURIComponent(this.form.subject)}`).then(res => {
        if (res.data.code === 200) {
          this.stats = res.data.data;
          if (this.stats.total === 0) {
            this.$message.warning('该科目暂无题目，请先添加题目到题库');
          } else {
            this.$message.success(`已加载题库：共${this.stats.total}道题目（选择题${this.stats.multiTotal}道，填空题${this.stats.fillTotal}道，判断题${this.stats.judgeTotal}道，主观题${this.stats.essayTotal || 0}道）`);
          }
        }
      }).catch(() => {
        this.$message.error('获取题库统计失败');
      });
    },
    
    // 刷新题库统计
    refreshStats() {
      this.loadStats();
    },
    // 提交表单
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return false
        }

        // 验证至少选择一种题型
        if (this.form.multiCount === 0 && this.form.fillCount === 0 && this.form.judgeCount === 0 && this.form.essayCount === 0) {
          this.$message.warning('请至少设置一种题型的数量')
          return
        }

        // 检查题库是否有足够的题目
        if (this.stats.total === 0) {
          this.$message.warning('请先选择科目并加载题库统计')
          return
        }

        // 检查各题型数量是否超过题库数量
        if (this.form.multiCount > this.stats.multiTotal) {
          this.$message.warning(`选择题数量(${this.form.multiCount})超过题库数量(${this.stats.multiTotal})`)
          return
        }
        if (this.form.fillCount > this.stats.fillTotal) {
          this.$message.warning(`填空题数量(${this.form.fillCount})超过题库数量(${this.stats.fillTotal})`)
          return
        }
        if (this.form.judgeCount > this.stats.judgeTotal) {
          this.$message.warning(`判断题数量(${this.form.judgeCount})超过题库数量(${this.stats.judgeTotal})`)
          return
        }
        if (this.form.essayCount > (this.stats.essayTotal || 0)) {
          this.$message.warning(`主观题数量(${this.form.essayCount})超过题库数量(${this.stats.essayTotal || 0})`)
          return
        }

        this.$confirm('确定要进行自动组卷吗？这将替换该试卷原有的所有题目！', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.doAutoPaper()
        }).catch(() => {})
      })
    },
    // 执行自动组卷
    doAutoPaper() {
      this.loading = true;
      // 构建提交数据，包含选中试卷的paperId
      console.log('选中的试卷:', this.selectedExam);
      console.log('paperId:', this.selectedExam ? this.selectedExam.paperId : null);
      const submitData = {
        ...this.form,
        paperId: this.selectedExam ? this.selectedExam.paperId : null,
        examType: this.selectedExam ? this.selectedExam.type : '',
        examDate: this.selectedExam ? this.selectedExam.examDate : '',
        totalTime: this.selectedExam ? this.selectedExam.totalTime : 90,
        grade: this.selectedExam ? this.selectedExam.grade : '',
        term: '1',
        major: this.selectedExam ? this.selectedExam.major : '',
        institute: this.selectedExam ? this.selectedExam.institute : '',
        tips: this.selectedExam ? this.selectedExam.tips : ''
      };
      this.$axios({
        url: '/api/paper/auto',
        method: 'post',
        data: submitData
      }).then(res => {
        this.loading = false
        if (res.data.code === 200) {
          this.result = res.data.data
          this.resultDialogVisible = true
          this.$message.success('自动组卷成功！')
        } else {
          this.$message.error(res.data.message || '组卷失败')
        }
      }).catch(err => {
        this.loading = false
        this.$message.error('组卷请求失败：' + (err.message || '未知错误'))
      })
    },
    // 重置表单
    resetForm() {
      this.$refs.form.resetFields();
      this.form = {
        examCode: '',
        subject: '',
        description: '',
        multiCount: 10,
        fillCount: 5,
        judgeCount: 5,
        multiScore: 2,
        fillScore: 2,
        judgeScore: 2,
        difficulty: 'mixed',
        section: ''
      };
      this.selectedExam = null;
      this.stats = {
        total: 0,
        multiTotal: 0,
        fillTotal: 0,
        judgeTotal: 0
      };
    },
    // 跳转到试卷列表
    goToExamList() {
      this.resultDialogVisible = false
      // 刷新试卷列表数据
      this.$router.push('/selectExam').then(() => {
        // 强制刷新页面以确保显示最新数据
        this.$nextTick(() => {
          window.location.reload()
        })
      })
    }
  }
}
</script>

<style lang="less" scoped>
.auto-paper {
  padding: 20px;
  width: 100%;
  display: flex;
  justify-content: center;

  .box-card {
    max-width: 900px;
    width: 100%;
    background-color: var(--card-bg);
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 var(--shadow-color);
    
    .el-card__header {
      background: linear-gradient(135deg, #e8f0ff 0%, #dbeafe 100%) !important;
      border-bottom: 1px solid #bfdbfe !important;
      padding: 16px 20px !important;
    }
    
    .el-card__header span {
      color: #1e40af !important;
      font-weight: 600;
      font-size: 18px;
    }
    
    .el-card__body {
      background-color: var(--card-bg);
    }
  }

  .clearfix {
    font-size: 18px;
    font-weight: bold;
  }

  .el-divider {
    margin: 30px 0 20px 0;
  }

  .el-input-number {
    width: 130px;
  }

  .score-label {
    margin-left: 20px;
    margin-right: 10px;
  }

  .score-unit {
    margin-left: 10px;
    color: #909399;
  }
  
  // 预估总分绿色半透明样式（参考蓝色按钮效果）
  .estimated-score {
    display: inline-block;
    padding: 8px 16px;
    background-color: rgba(103, 194, 58, 0.2);
    color: #67c23a;
    border: 1px solid rgba(103, 194, 58, 0.4);
    border-radius: 4px;
    font-weight: bold;
    font-size: 14px;
  }
  
  // 按钮居中样式
  .button-group-center {
    .el-form-item__content {
      display: flex;
      justify-content: center;
      margin-left: 0 !important;
    }
  }
  
  // 考试信息详情样式
  .exam-info-detail {
    margin: 15px 0 20px 120px;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 6px;
    border: 1px solid #e4e7ed;
    
    // 字段标签样式 - 蓝色
    /deep/ .el-descriptions-item__label {
      background: #e8f0ff !important;
      color: #1e40af !important;
      font-weight: 600 !important;
    }
    
    // 数据值样式 - 深灰色/黑色
    /deep/ .el-descriptions-item__content {
      background: #ffffff !important;
      color: #303133 !important;
      font-weight: 500 !important;
    }
  }
  
  .exam-info-placeholder {
    margin: 15px 0 20px 120px;
    padding: 30px;
    background: #fafafa;
    border-radius: 6px;
    border: 1px dashed #dcdfe6;
    text-align: center;
    color: #909399;
  }
}
</style>

<style>
/* 深色模式专用样式 */
.dark-theme .auto-paper .box-card .el-card__header {
  background: linear-gradient(135deg, #1a3a5c 0%, #1e4a6a 100%) !important;
  border-bottom: 1px solid #2a5a7a !important;
}
.dark-theme .auto-paper .box-card .el-card__header span {
  color: #7dd3fc !important;
}
.dark-theme .auto-paper .exam-info-detail {
  background: #1e2d3a !important;
  border: 1px solid #3a4a5a !important;
}
.dark-theme .auto-paper .exam-info-detail .el-descriptions-item__label {
  background: #1a3a5c !important;
  color: #7dd3fc !important;
}
.dark-theme .auto-paper .exam-info-detail .el-descriptions-item__content {
  background: #243442 !important;
  color: #e0f0ff !important;
}
.dark-theme .auto-paper .exam-info-placeholder {
  background: #1e2d3a !important;
  border: 1px dashed #4a5a6a !important;
  color: #7dd3fc !important;
}
</style>
