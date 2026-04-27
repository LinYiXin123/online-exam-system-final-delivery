<template>
  <div class="ai-grading-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2><i class="el-icon-s-check"></i> AI智能改卷</h2>
      <p class="sub-title">AI自动批改客观题和主观题，计算学生总分</p>
    </div>

    <!-- 第一步：筛选试卷 -->
    <el-card class="filter-card">
      <el-row :gutter="20" type="flex" align="middle">
        <el-col :span="6">
          <el-select v-model="selectedExamCode" placeholder="选择考试" style="width: 100%" @change="handleExamChange" clearable>
            <el-option v-for="exam in examList" :key="exam.examCode" :label="exam.source + ' - ' + exam.description" :value="exam.examCode">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="selectedMajor" placeholder="选择专业" style="width: 100%" @change="filterStudents" clearable>
            <el-option v-for="major in majorList" :key="major" :label="major" :value="major"></el-option>
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="selectedGrade" placeholder="选择年级" style="width: 100%" @change="filterStudents" clearable>
            <el-option v-for="grade in gradeList" :key="grade" :label="grade + '级'" :value="grade"></el-option>
          </el-select>
        </el-col>
        <el-col :span="8" style="text-align: right">
          <el-button icon="el-icon-refresh" @click="refreshData">刷新</el-button>
          <el-button type="warning" icon="el-icon-cpu" @click="batchAutoGrade" :loading="autoGradeLoading" :disabled="!selectedExamCode">
            一键AI改卷
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 考试信息卡片 -->
    <el-card class="exam-info-card" v-if="currentExam">
      <div slot="header">
        <span><i class="el-icon-document"></i> 当前考试：{{ currentExam.source }}</span>
        <span style="float: right; color: #909399">{{ currentExam.description }}</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="4">
          <div class="info-item">
            <div class="info-label">考试时间</div>
            <div class="info-value">{{ currentExam.examDate }}</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="info-item">
            <div class="info-label">总分</div>
            <div class="info-value">{{ currentExam.totalScore }}分</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="info-item">
            <div class="info-label">参考人数</div>
            <div class="info-value">{{ studentList.length }}人</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="info-item">
            <div class="info-label">已批改</div>
            <div class="info-value" style="color: #67C23A">{{ gradedCount }}人</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="info-item">
            <div class="info-label">待批改</div>
            <div class="info-value" style="color: #E6A23C">{{ pendingCount }}人</div>
          </div>
        </el-col>
        <el-col :span="4">
          <div class="info-item">
            <div class="info-label">完成率</div>
            <div class="info-value" style="color: #409EFF">{{ completionRate }}%</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 第二步：学生列表 -->
    <el-card class="table-card" v-if="selectedExamCode">
      <div slot="header">
        <span><i class="el-icon-user"></i> 学生成绩列表</span>
        <div style="float: right; display: flex; align-items: center; gap: 10px;">
          <el-select v-model="sortField" placeholder="排序方式" size="small" style="width: 130px" @change="sortStudents">
            <el-option label="按学号排序" value="studentId"></el-option>
            <el-option label="按总分排序" value="totalScore"></el-option>
          </el-select>
          <el-select v-model="sortOrder" placeholder="排序顺序" size="small" style="width: 100px" @change="sortStudents">
            <el-option label="升序" value="asc"></el-option>
            <el-option label="降序" value="desc"></el-option>
          </el-select>
          <el-input v-model="searchKeyword" placeholder="搜索学号/姓名" style="width: 180px" size="small" clearable @input="filterStudents">
            <i slot="prefix" class="el-icon-search"></i>
          </el-input>
        </div>
      </div>

      <el-table :data="filteredStudents" stripe style="width: 100%" v-loading="tableLoading" empty-text="暂无学生数据" @row-click="showStudentDetail">
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="studentId" label="学号" min-width="100"></el-table-column>
        <el-table-column prop="studentName" label="姓名" min-width="80"></el-table-column>
        <el-table-column prop="major" label="专业" min-width="100"></el-table-column>
        <el-table-column prop="grade" label="年级" min-width="60"></el-table-column>
        <el-table-column label="客观题分" min-width="80" align="center">
          <template slot-scope="scope">
            <span :class="getScoreClass(scope.row.objectiveScore, 70)">{{ scope.row.objectiveScore || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="主观题分" min-width="80" align="center">
          <template slot-scope="scope">
            <span :class="getScoreClass(scope.row.subjectiveScore, 30)">{{ scope.row.subjectiveScore || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="总分" min-width="70" align="center">
          <template slot-scope="scope">
            <el-tag :type="getTotalScoreType(scope.row.totalScore)" size="medium">
              {{ scope.row.totalScore || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="批改状态" min-width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.gradingStatus === 'completed' ? 'success' : 'warning'" size="small">
              {{ scope.row.gradingStatus === 'completed' ? '已完成' : '待批改' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" icon="el-icon-edit" @click.stop="showStudentDetail(scope.row)">
              批改
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 空状态提示 -->
    <el-empty v-if="!selectedExamCode" description="请先选择要批改的考试"></el-empty>

    <!-- 学生答卷详情弹窗 -->
    <el-dialog :title="'批改答卷 - ' + (currentStudent ? currentStudent.studentName : '')" :visible.sync="detailDialogVisible" width="900px" top="3vh" custom-class="detail-dialog">
      <div v-if="currentStudent" class="detail-content">
        <!-- 学生信息 -->
        <div class="student-header">
          <el-descriptions :column="5" border size="small">
            <el-descriptions-item label="学号">{{ currentStudent.studentId }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ currentStudent.studentName }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
            <el-descriptions-item label="客观题">{{ currentStudent.objectiveScore || 0 }}分</el-descriptions-item>
            <el-descriptions-item label="主观题">{{ currentStudent.subjectiveScore || 0 }}分</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 答题Tab切换 -->
        <el-tabs v-model="activeTab" type="border-card" class="answer-tabs">
          <!-- 客观题Tab -->
          <el-tab-pane label="客观题答案" name="objective">
            <el-table :data="objectiveAnswers" stripe size="small" max-height="400" style="width: 100%">
              <el-table-column prop="questionType" label="题型" min-width="80">
                <template slot-scope="scope">
                  {{ getQuestionTypeName(scope.row.questionType) }}
                </template>
              </el-table-column>
              <el-table-column prop="questionId" label="题号" min-width="80"></el-table-column>
              <el-table-column prop="studentAnswer" label="学生答案" min-width="120"></el-table-column>
              <el-table-column prop="correctAnswer" label="正确答案" min-width="120"></el-table-column>
              <el-table-column label="结果" min-width="80" align="center">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.isCorrect ? 'success' : 'danger'" size="mini">
                    {{ scope.row.isCorrect ? '正确' : '错误' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="score" label="得分" min-width="80" align="center"></el-table-column>
            </el-table>
            <div class="objective-summary">
              <span>客观题总分：<strong style="color: #409EFF">{{ calculatedObjectiveScore }}</strong> 分</span>
            </div>
          </el-tab-pane>

          <!-- 主观题Tab -->
          <el-tab-pane label="主观题答案" name="subjective">
            <div v-for="(answer, index) in subjectiveAnswers" :key="answer.answerId" class="essay-item">
              <div class="essay-header">
                <span class="essay-title">第{{ index + 1 }}题 ({{ answer.maxScore }}分)</span>
                <el-tag :type="getStatusTagType(answer.gradingStatus)" size="small">
                  {{ getStatusText(answer.gradingStatus) }}
                </el-tag>
              </div>
              <div class="essay-question">{{ answer.questionContent }}</div>
              <div class="essay-answer">
                <div class="answer-label">学生答案：</div>
                <div class="answer-content">{{ answer.studentAnswer || '（未作答）' }}</div>
              </div>
              <div class="essay-reference" v-if="showReference">
                <div class="reference-label">参考答案：</div>
                <div class="reference-content">{{ answer.referenceAnswer }}</div>
              </div>
              <div class="essay-grading">
                <el-row :gutter="20">
                  <el-col :span="8">
                    <span class="grading-label">AI评分：</span>
                    <el-tag v-if="answer.aiScore !== null" type="primary">{{ answer.aiScore }}分</el-tag>
                    <span v-else class="no-score">未评分</span>
                  </el-col>
                  <el-col :span="10">
                    <span class="grading-label">教师评分：</span>
                    <el-input-number v-model="answer.teacherScore" :min="0" :max="answer.maxScore" size="small" style="width: 120px"></el-input-number>
                    <span class="max-score">/{{ answer.maxScore }}分</span>
                  </el-col>
                  <el-col :span="6">
                    <el-button size="mini" type="text" @click="answer.teacherScore = answer.aiScore" v-if="answer.aiScore !== null">
                      采用AI评分
                    </el-button>
                  </el-col>
                </el-row>
                <el-input v-model="answer.teacherComment" type="textarea" :rows="2" placeholder="教师评语（可选）" style="margin-top: 10px"></el-input>
              </div>
            </div>
            <el-empty v-if="subjectiveAnswers.length === 0" description="该学生没有主观题答案"></el-empty>
          </el-tab-pane>
        </el-tabs>

        <!-- 总分汇总 -->
        <div class="score-summary">
          <el-row :gutter="20" type="flex" align="middle">
            <el-col :span="6">
              <div class="summary-item">
                <span class="summary-label">客观题：</span>
                <span class="summary-value">{{ calculatedObjectiveScore }}分</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item">
                <span class="summary-label">主观题：</span>
                <span class="summary-value">{{ calculatedSubjectiveScore }}分</span>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="summary-item total">
                <span class="summary-label">总分：</span>
                <span class="summary-value">{{ calculatedObjectiveScore + calculatedSubjectiveScore }}分</span>
              </div>
            </el-col>
            <el-col :span="6">
              <el-checkbox v-model="showReference">显示参考答案</el-checkbox>
            </el-col>
          </el-row>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false" icon="el-icon-close">取消</el-button>
        <el-button type="warning" @click="aiGradeCurrentStudent" :loading="aiGradingLoading" icon="el-icon-cpu">
          AI评分主观题
        </el-button>
        <el-button type="success" @click="submitStudentGrading" :loading="submitLoading" icon="el-icon-check">
          保存并计算总分
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AiGrading',
  data() {
    return {
      // 考试相关
      examList: [],
      selectedExamCode: null,
      currentExam: null,
      // 筛选条件
      selectedMajor: '',
      selectedGrade: '',
      searchKeyword: '',
      majorList: [],
      gradeList: [],
      // 排序
      sortField: 'studentId',
      sortOrder: 'asc',
      // 学生列表
      studentList: [],
      filteredStudents: [],
      // 当前学生详情
      currentStudent: null,
      objectiveAnswers: [],
      subjectiveAnswers: [],
      activeTab: 'subjective',
      showReference: false,
      // 加载状态
      tableLoading: false,
      autoGradeLoading: false,
      aiGradingLoading: false,
      submitLoading: false,
      // 弹窗
      detailDialogVisible: false
    }
  },
  computed: {
    // 已批改人数
    gradedCount() {
      return this.studentList.filter(s => s.gradingStatus === 'completed').length
    },
    // 待批改人数
    pendingCount() {
      return this.studentList.filter(s => s.gradingStatus !== 'completed').length
    },
    // 完成率
    completionRate() {
      if (this.studentList.length === 0) return 0
      return Math.round((this.gradedCount / this.studentList.length) * 100)
    },
    // 计算当前学生主观题总分
    calculatedSubjectiveScore() {
      let total = 0
      for (const answer of this.subjectiveAnswers) {
        const score = answer.teacherScore !== null && answer.teacherScore !== undefined 
          ? answer.teacherScore 
          : (answer.aiScore || 0)
        total += score
      }
      return total
    },
    // 计算当前学生客观题总分（根据表格数据动态计算）
    calculatedObjectiveScore() {
      let total = 0
      for (const answer of this.objectiveAnswers) {
        total += (answer.score || 0)
      }
      return total
    }
  },
  created() {
    this.fetchExamList()
  },
  methods: {
    // 获取考试列表
    async fetchExamList() {
      try {
        const res = await this.$axios.get('/api/exams')
        if (res.data.code === 200) {
          this.examList = res.data.data || []
        }
      } catch (error) {
        console.error('获取考试列表失败:', error)
      }
    },

    // 考试选择变化
    handleExamChange() {
      if (!this.selectedExamCode) {
        this.currentExam = null
        this.studentList = []
        this.filteredStudents = []
        return
      }
      this.currentExam = this.examList.find(e => e.examCode === this.selectedExamCode)
      this.fetchStudentScores()
    },

    // 获取学生成绩列表
    async fetchStudentScores() {
      if (!this.selectedExamCode) return
      this.tableLoading = true
      try {
        const res = await this.$axios.get(`/api/ai-grading/students/${this.selectedExamCode}`)
        if (res.data.code === 200) {
          this.studentList = res.data.data || []
          this.extractFilters()
          this.filterStudents()
        }
      } catch (error) {
        console.error('获取学生成绩失败:', error)
        this.$message.error('获取学生成绩失败')
      } finally {
        this.tableLoading = false
      }
    },

    // 提取筛选选项
    extractFilters() {
      const majors = new Set()
      const grades = new Set()
      this.studentList.forEach(s => {
        if (s.major) majors.add(s.major)
        if (s.grade) grades.add(s.grade)
      })
      this.majorList = Array.from(majors)
      this.gradeList = Array.from(grades).sort()
    },

    // 筛选学生
    filterStudents() {
      let result = this.studentList
      if (this.selectedMajor) {
        result = result.filter(s => s.major === this.selectedMajor)
      }
      if (this.selectedGrade) {
        result = result.filter(s => s.grade === this.selectedGrade)
      }
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(s => 
          String(s.studentId).includes(keyword) || 
          (s.studentName && s.studentName.toLowerCase().includes(keyword))
        )
      }
      // 应用排序
      this.filteredStudents = this.applySorting(result)
    },

    // 排序学生列表
    sortStudents() {
      this.filterStudents()
    },

    // 应用排序
    applySorting(list) {
      const sorted = [...list]
      sorted.sort((a, b) => {
        let valueA, valueB
        if (this.sortField === 'studentId') {
          valueA = a.studentId || 0
          valueB = b.studentId || 0
        } else if (this.sortField === 'totalScore') {
          // 总分 = 客观题分 + 主观题分
          valueA = (a.objectiveScore || 0) + (a.subjectiveScore || 0)
          valueB = (b.objectiveScore || 0) + (b.subjectiveScore || 0)
        }
        if (this.sortOrder === 'asc') {
          return valueA - valueB
        } else {
          return valueB - valueA
        }
      })
      return sorted
    },

    // 刷新数据
    refreshData() {
      this.fetchStudentScores()
    },

    // 显示学生详情
    async showStudentDetail(row) {
      this.currentStudent = row
      this.activeTab = 'subjective'
      this.detailDialogVisible = true
      await this.fetchStudentAnswers(row.studentId)
    },

    // 获取学生答题详情
    async fetchStudentAnswers(studentId) {
      try {
        // 获取客观题答案
        const objRes = await this.$axios.get(`/api/ai-grading/objective-answers/${this.selectedExamCode}/${studentId}`)
        if (objRes.data.code === 200) {
          this.objectiveAnswers = objRes.data.data || []
        }
        // 获取主观题答案
        const subRes = await this.$axios.get(`/api/ai-grading/subjective-answers/${this.selectedExamCode}/${studentId}`)
        if (subRes.data.code === 200) {
          this.subjectiveAnswers = (subRes.data.data || []).map(a => ({
            ...a,
            teacherScore: a.finalScore !== null ? a.finalScore : a.aiScore
          }))
        }
      } catch (error) {
        console.error('获取答题详情失败:', error)
      }
    },

    // 一键AI改卷（批量）
    async batchAutoGrade() {
      this.$confirm('将对所有学生进行AI自动批改（客观题自动判分，主观题AI评分），是否继续？', '一键AI改卷', {
        confirmButtonText: '开始改卷',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.autoGradeLoading = true
        try {
          const res = await this.$axios.post(`/api/ai-grading/auto-grade-all/${this.selectedExamCode}`)
          if (res.data.code === 200) {
            const result = res.data.data || {}
            this.$message.success(`改卷完成！客观题批改${result.objectiveCount || 0}人，主观题AI评分${result.subjectiveCount || 0}题`)
            this.refreshData()
          } else {
            this.$message.error(res.data.msg || '改卷失败')
          }
        } catch (error) {
          console.error('一键改卷失败:', error)
          this.$message.error('一键改卷失败，请稍后重试')
        } finally {
          this.autoGradeLoading = false
        }
      }).catch(() => {})
    },

    // AI评分当前学生主观题
    async aiGradeCurrentStudent() {
      if (!this.currentStudent) return
      this.aiGradingLoading = true
      try {
        const res = await this.$axios.post(`/api/ai-grading/grade-student-essay/${this.selectedExamCode}/${this.currentStudent.studentId}`)
        if (res.data.code === 200) {
          this.$message.success('AI评分完成')
          await this.fetchStudentAnswers(this.currentStudent.studentId)
        } else {
          this.$message.error(res.data.msg || 'AI评分失败')
        }
      } catch (error) {
        console.error('AI评分失败:', error)
        this.$message.error('AI评分失败')
      } finally {
        this.aiGradingLoading = false
      }
    },

    // 保存学生评分并计算总分
    async submitStudentGrading() {
      this.submitLoading = true
      try {
        // 提交所有主观题评分
        for (const answer of this.subjectiveAnswers) {
          if (answer.teacherScore !== null && answer.teacherScore !== undefined) {
            await this.$axios.post('/api/ai-grading/teacher-grade', {
              answerId: answer.answerId,
              teacherScore: answer.teacherScore,
              teacherComment: answer.teacherComment || ''
            })
          }
        }
        // 计算并更新总分（包含动态计算的客观题分数）
        const totalRes = await this.$axios.post(`/api/ai-grading/calculate-total/${this.selectedExamCode}/${this.currentStudent.studentId}`, {
          objectiveScore: this.calculatedObjectiveScore,
          subjectiveScore: this.calculatedSubjectiveScore
        })
        if (totalRes.data.code === 200) {
          this.$message.success('评分保存成功，总分已更新')
          this.detailDialogVisible = false
          this.refreshData()
        } else {
          this.$message.error(totalRes.data.msg || '总分计算失败')
        }
      } catch (error) {
        console.error('保存评分失败:', error)
        this.$message.error('保存评分失败')
      } finally {
        this.submitLoading = false
      }
    },

    // 获取题型名称
    getQuestionTypeName(type) {
      const map = { 1: '单选', 2: '填空', 3: '判断' }
      return map[type] || '未知'
    },

    // 获取分数样式类
    getScoreClass(score, maxScore) {
      if (!score) return 'score-zero'
      const rate = score / maxScore
      if (rate >= 0.8) return 'score-high'
      if (rate >= 0.6) return 'score-medium'
      return 'score-low'
    },

    // 获取总分标签类型
    getTotalScoreType(score) {
      if (!score) return 'info'
      if (score >= 90) return 'success'
      if (score >= 60) return ''
      return 'danger'
    },

    // 获取状态标签类型
    getStatusTagType(status) {
      const map = {
        'pending': 'info',
        'ai_graded': 'warning',
        'teacher_reviewed': 'success'
      }
      return map[status] || 'info'
    },

    // 获取状态文本
    getStatusText(status) {
      const map = {
        'pending': '待评分',
        'ai_graded': 'AI已评',
        'teacher_reviewed': '已确认'
      }
      return map[status] || status
    }
  }
}
</script>

<style scoped>
.ai-grading-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 5px 0;
  color: #303133;
}

.page-header .sub-title {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.filter-card {
  margin-bottom: 20px;
}

.exam-info-card {
  margin-bottom: 20px;
}

.info-item {
  text-align: center;
  padding: 10px 0;
}

.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.info-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.table-card {
  margin-bottom: 20px;
}

.score-zero { color: #C0C4CC; }
.score-low { color: #F56C6C; font-weight: bold; }
.score-medium { color: #E6A23C; font-weight: bold; }
.score-high { color: #67C23A; font-weight: bold; }

/* 学生详情弹窗 */
.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.student-header {
  margin-bottom: 15px;
}

.answer-tabs {
  margin-top: 15px;
}

.objective-summary {
  margin-top: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  text-align: right;
}

/* 主观题样式 */
.essay-item {
  margin-bottom: 20px;
  padding: 15px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.essay-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.essay-title {
  font-weight: bold;
  color: #303133;
  font-size: 15px;
}

.essay-question {
  color: #606266;
  margin-bottom: 12px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  border-left: 3px solid #409EFF;
}

.essay-answer {
  margin-bottom: 12px;
}

.answer-label, .reference-label, .grading-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 5px;
}

.answer-content {
  padding: 10px;
  background: #ecf5ff;
  border-radius: 4px;
  color: #409EFF;
  line-height: 1.6;
  white-space: pre-wrap;
}

.essay-reference {
  margin-bottom: 12px;
}

.reference-content {
  padding: 10px;
  background: #f0f9eb;
  border-radius: 4px;
  color: #67C23A;
  line-height: 1.6;
  white-space: pre-wrap;
}

.essay-grading {
  padding: 12px;
  background: #fff;
  border-radius: 4px;
  border: 1px dashed #dcdfe6;
}

.grading-label {
  display: inline-block;
  margin-right: 10px;
}

.max-score {
  margin-left: 5px;
  color: #909399;
  font-size: 13px;
}

.no-score {
  color: #C0C4CC;
  font-style: italic;
}

/* 总分汇总 */
.score-summary {
  margin-top: 20px;
  padding: 15px;
  background: linear-gradient(135deg, #e8f4fc 0%, #d6eaf8 100%);
  border-radius: 8px;
  color: #303133;
}

.summary-item {
  text-align: center;
}

.summary-label {
  font-size: 14px;
  opacity: 0.9;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
}

.summary-item.total .summary-value {
  font-size: 32px;
  color: #409EFF;
}

.score-summary .el-checkbox {
  color: #303133;
}

.score-summary /deep/ .el-checkbox__label {
  color: #303133;
}
</style>

<style>
/* 深色模式专用样式 */
.dark-theme .objective-summary {
  background: #1a3a4a !important;
  color: #e0f0ff !important;
}
.dark-theme .essay-item {
  background: #1e2d3a !important;
  border: 1px solid #3a4a5a !important;
}
.dark-theme .essay-title {
  color: #e0f0ff !important;
}
.dark-theme .essay-question {
  background: #243442 !important;
  color: #b0c4d8 !important;
}
.dark-theme .answer-label,
.dark-theme .reference-label,
.dark-theme .grading-label {
  color: #a0b0c0 !important;
}
.dark-theme .answer-content {
  background: #1a4a6e !important;
  color: #a3d4ff !important;
}
.dark-theme .reference-content {
  background: #1e4620 !important;
  color: #8fd694 !important;
}
.dark-theme .essay-grading {
  background: #243442 !important;
  border: 1px dashed #4a5a6a !important;
}
.dark-theme .max-score {
  color: #a0b0c0 !important;
}
.dark-theme .score-summary {
  background: linear-gradient(135deg, #1a4a6e 0%, #1e5a7a 100%) !important;
  color: #e0f0ff !important;
}
.dark-theme .summary-item.total .summary-value {
  color: #5ddfff !important;
}
.dark-theme .score-summary .el-checkbox,
.dark-theme .score-summary .el-checkbox__label {
  color: #e0f0ff !important;
}

/* 学生信息表单深色模式 */
.dark-theme .student-header .el-descriptions {
  background: #1e2d3a !important;
}
.dark-theme .student-header .el-descriptions-item__label {
  background: #1a3a5c !important;
  color: #ffffff !important;
}
.dark-theme .student-header .el-descriptions-item__content {
  background: #243442 !important;
  color: #ffffff !important;
}

/* AI评分标签深色模式 - 红框部分 */
.dark-theme .essay-grading .el-tag--primary {
  background: #1a3a5c !important;
  color: #ffffff !important;
  border-color: #3a6a8e !important;
}

/* 教师评分输入框深色模式 */
.dark-theme .essay-grading .el-input-number {
  background: #1e2d3a !important;
}
.dark-theme .essay-grading .el-input-number .el-input__inner {
  background: #1a3a5c !important;
  color: #ffffff !important;
  border-color: #3a4a5a !important;
}
/* 教师评分+/-按钮浅色 - 绿框部分 */
.dark-theme .essay-grading .el-input-number .el-input-number__decrease,
.dark-theme .essay-grading .el-input-number .el-input-number__increase {
  background: #e8f4fc !important;
  color: #1a3a5c !important;
  border-color: #b0c4d8 !important;
}

/* 一键AI改卷对话框深色模式 */
.dark-theme .el-message-box {
  background: #1e2d3a !important;
  border: 1px solid #3a4a5a !important;
}
.dark-theme .el-message-box__header {
  background: #1a3a5c !important;
}
.dark-theme .el-message-box__title {
  color: #ffffff !important;
}
.dark-theme .el-message-box__content {
  color: #e0f0ff !important;
}
.dark-theme .el-message-box__message p {
  color: #e0f0ff !important;
}

/* 总分列深色模式 - 修复灰色问题 */
.dark-theme .el-table .el-tag--info {
  background: #1a4a6e !important;
  color: #ffffff !important;
  border-color: #3a6a8e !important;
}
</style>
