// 点击试卷后的缩略信息
<template>
  <div id="msg">
    <!-- 面包屑导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/startExam' }">
          <i class="el-icon-document-copy"></i> 试卷列表
        </el-breadcrumb-item>
        <el-breadcrumb-item>{{ examData.source }}</el-breadcrumb-item>
      </el-breadcrumb>
    </div>
    
    <!-- 试卷信息卡片 -->
    <el-card class="exam-card" shadow="hover">
      <div class="card-header">
        <div class="header-left">
          <div class="exam-icon">
            <i class="el-icon-notebook-2"></i>
          </div>
          <div class="exam-title-info">
            <h2 class="exam-name">{{ examData.source }} <span class="exam-desc" v-if="examData.description">- {{ examData.description }}</span></h2>
            <div class="exam-meta">
              <el-tag size="small" type="info" effect="plain">
                <i class="el-icon-office-building"></i> {{ examData.institute }}
              </el-tag>
              <el-tag size="small" :type="examData.type === '期末考试' ? 'danger' : 'success'" effect="plain">
                {{ examData.type }}
              </el-tag>
              <span class="update-time">
                <i class="el-icon-time"></i> 更新于 {{ examData.examDate }}
              </span>
            </div>
          </div>
        </div>
        <div class="header-right">
          <div class="score-display">
            <div class="score-label">总分</div>
            <div class="score-value">{{ (score[0] || 0) + (score[1] || 0) + (score[2] || 0) + (score[3] || 0) }}</div>
          </div>
          <el-button type="primary" size="large" class="start-btn" @click="toAnswer(examData.examCode)">
            <i :class="$store.state.isPractice ? 'el-icon-edit-outline' : 'el-icon-s-claim'"></i>
            {{ $store.state.isPractice == true ? '开始练习' : '开始考试' }}
          </el-button>
        </div>
      </div>
      
      <!-- 试卷统计信息 -->
      <div class="exam-stats">
        <div class="stat-item">
          <div class="stat-icon choice">
            <i class="el-icon-finished"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ topicCount[0] || 0 }}</div>
            <div class="stat-label">选择题</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon fill">
            <i class="el-icon-edit"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ topicCount[1] || 0 }}</div>
            <div class="stat-label">填空题</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon judge">
            <i class="el-icon-check"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ topicCount[2] || 0 }}</div>
            <div class="stat-label">判断题</div>
          </div>
        </div>
        <div class="stat-item" v-if="topicCount[3] > 0">
          <div class="stat-icon essay">
            <i class="el-icon-edit-outline"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ topicCount[3] || 0 }}</div>
            <div class="stat-label">主观题</div>
          </div>
        </div>
        <div class="stat-item">
          <div class="stat-icon time">
            <i class="el-icon-timer"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ examData.totalTime || '不限' }}</div>
            <div class="stat-label">考试时长(分钟)</div>
          </div>
        </div>
      </div>
      
      <!-- 考生须知入口 -->
      <div class="exam-notice" @click="dialogVisible = true">
        <i class="el-icon-warning-outline"></i>
        <span>查看考生须知</span>
        <i class="el-icon-arrow-right"></i>
      </div>
    </el-card>
    <div class="content">
      <el-collapse v-model="activeName">
        <el-collapse-item class="header" name="0">
          <template slot="title">
            <div class="title">
              <span>{{ examData.source }}</span><i class="header-icon el-icon-a-041"></i>
              <span class="time">{{ (score[0] || 0) + (score[1] || 0) + (score[2] || 0) + (score[3] || 0) }}分 / {{ examData.totalTime }}分钟</span>
              <el-button type="primary" size="small">试题</el-button>
            </div>
          </template>
          <el-collapse class="inner">
            <el-collapse-item>
              <template slot="title">
                <div class="titlei">
                  <el-tag type="primary" size="small">选择题</el-tag>
                  <span class="topic-summary">共{{ topicCount[0] }}题 共计{{ score[0] }}分</span>
                </div>
              </template>
              <div class="contenti">
                <el-table :data="topic[1]" style="width: 100%" size="small" :show-header="true">
                  <el-table-column label="序号" width="70" align="center">
                    <template slot-scope="scope">
                      <el-tag size="mini" type="info">{{ scope.$index + 1 }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="question" label="题目内容" min-width="400"></el-table-column>
                  <el-table-column label="分值" width="80" align="center">
                    <template slot-scope="scope">
                      <el-tag type="warning" size="mini">{{ scope.row.score }}分</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-collapse-item>
            <el-collapse-item>
              <template slot="title">
                <div class="titlei">
                  <el-tag type="success" size="small">填空题</el-tag>
                  <span class="topic-summary">共{{ topicCount[1] }}题 共计{{ score[1] }}分</span>
                </div>
              </template>
              <div class="contenti">
                <el-table :data="topic[2]" style="width: 100%" size="small" :show-header="true">
                  <el-table-column label="序号" width="70" align="center">
                    <template slot-scope="scope">
                      <el-tag size="mini" type="info">{{ topicCount[0] + scope.$index + 1 }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="question" label="题目内容" min-width="400"></el-table-column>
                  <el-table-column label="分值" width="80" align="center">
                    <template slot-scope="scope">
                      <el-tag type="warning" size="mini">{{ scope.row.score }}分</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-collapse-item>
            <el-collapse-item>
              <template slot="title">
                <div class="titlei">
                  <el-tag type="danger" size="small">判断题</el-tag>
                  <span class="topic-summary">共{{ topicCount[2] }}题 共计{{ score[2] }}分</span>
                </div>
              </template>
              <div class="contenti">
                <el-table :data="topic[3]" style="width: 100%" size="small" :show-header="true">
                  <el-table-column label="序号" width="70" align="center">
                    <template slot-scope="scope">
                      <el-tag size="mini" type="info">{{ topicCount[0] + topicCount[1] + scope.$index + 1 }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="question" label="题目内容" min-width="400"></el-table-column>
                  <el-table-column label="分值" width="80" align="center">
                    <template slot-scope="scope">
                      <el-tag type="warning" size="mini">{{ scope.row.score }}分</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-collapse-item>
            <!-- 主观题 -->
            <el-collapse-item v-if="topic[4] && topic[4].length > 0">
              <template slot="title">
                <div class="titlei">
                  <el-tag type="warning" size="small">主观题</el-tag>
                  <span class="topic-summary">共{{ topicCount[3] }}题 共计{{ score[3] }}分</span>
                </div>
              </template>
              <div class="contenti">
                <el-table :data="topic[4]" style="width: 100%" size="small" :show-header="true">
                  <el-table-column label="序号" width="70" align="center">
                    <template slot-scope="scope">
                      <el-tag size="mini" type="info">{{ topicCount[0] + topicCount[1] + topicCount[2] + scope.$index + 1 }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="question" label="题目内容" min-width="400"></el-table-column>
                  <el-table-column label="分值" width="80" align="center">
                    <template slot-scope="scope">
                      <el-tag type="warning" size="mini">{{ scope.row.score }}分</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-collapse-item>

      </el-collapse>
    </div>
    <!--考生须知对话框-->
    <el-dialog title="考生须知" :visible.sync="dialogVisible" width="30%">
      <span>{{ examData.tips }}</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false"> 确认</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      dialogVisible: false, //对话框属性
      activeName: '0',  //默认打开序号
      topicCount: [],//每种类型题目的总数
      score: [],  //每种类型分数的总数
      examData: { //考试信息
        // source: null,
        // totalScore: null,
      },
      topic: {  //试卷信息

      },
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    //初始化页面数据
    init() {
      let examCode = this.$route.query.examCode //获取路由传递过来的试卷编号
      this.$axios(`/api/exam/${examCode}`).then(res => {  //通过examCode请求试卷详细信息
        res.data.data.examDate = res.data.data.examDate.substr(0, 10)
        this.examData = { ...res.data.data }
        let paperId = this.examData.paperId
        this.$axios(`/api/paper/${paperId}`).then(res => {  //通过paperId获取试题题目信息
          this.topic = { ...res.data }
          let keys = Object.keys(this.topic) //对象转数组
          keys.forEach(e => {
            let data = this.topic[e]
            this.topicCount.push(data.length)
            let currentScore = 0
            for (let i = 0; i < data.length; i++) { //循环每种题型,计算出总分
              currentScore += data[i].score
            }
            this.score.push(currentScore) //把每种题型总分存入score
          })
        })
      })
    },
    toAnswer(id) {
      const status = this.getExamStatus()
      if (status === 'not_started') {
        this.$message.error(this.$store.state.isPractice ? '练习尚未开放' : '考试尚未开始')
        return
      }
      if (status === 'ended') {
        this.$message.error(this.$store.state.isPractice ? '练习已结束' : '考试已结束')
        return
      }
      if (this.$store.state.isPractice) {
        // 练习，根据科目名称从题库里面搜索所有题目
        this.$confirm(`将开始练习题库中所有科目为【${this.examData.source}】的题目`, "友情提示", {
          confirmButtonText: " 确认开始",
          cancelButtonText: " 取消",
          type: "warning",
        })
          .then(() => {
            this.$router.push({ path: "/answer", query: { examCode: id } })
          })
          .catch(() => { });
      } else {

        this.$router.push({ path: "/answer", query: { examCode: id } })
      }
    },
    buildExamDateTime(dateStr, timeStr) {
      if (!dateStr || !timeStr) {
        return null
      }
      return new Date(`${dateStr.substr(0, 10)}T${timeStr}`)
    },
    getExamStatus() {
      const now = new Date()
      const examDay = this.examData.examDate ? this.examData.examDate.substr(0, 10) : null
      const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
      const start = this.examData.startTime ? this.buildExamDateTime(this.examData.examDate, this.examData.startTime) : this.buildExamDateTime(this.examData.examDate, '00:00:00')
      const end = this.examData.endTime ? this.buildExamDateTime(this.examData.examDate, this.examData.endTime) : null
      if (examDay && examDay > today) {
        return 'not_started'
      }
      if (examDay === today && start && now < start) {
        return 'not_started'
      }
      if (examDay === today && end && now > end) {
        return 'ended'
      }
      return 'available'
    }
  }
}
</script>

<style lang="less" scoped>
#msg {
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
  padding: 20px;
  flex: 1;
}

/* 面包屑导航 */
.breadcrumb {
  margin-bottom: 20px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 试卷信息卡片 */
.exam-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 20px;
  
  /deep/ .el-card__body {
    padding: 0;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #2384d6 0%, #1a6cb3 100%);
  border-radius: 12px 12px 0 0;
  color: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.exam-icon {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  i {
    font-size: 32px;
    color: #fff;
  }
}

.exam-title-info {
  .exam-name {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 10px 0;
    color: #fff;
  }
  
  .exam-meta {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    
    .el-tag {
      background: rgba(255, 255, 255, 0.2);
      border: none;
      color: #fff;
    }
    
    .update-time {
      font-size: 13px;
      opacity: 0.9;
      
      i {
        margin-right: 4px;
      }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.score-display {
  text-align: center;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  
  .score-label {
    font-size: 13px;
    opacity: 0.9;
    margin-bottom: 4px;
  }
  
  .score-value {
    font-size: 36px;
    font-weight: 700;
    line-height: 1;
  }
}

.start-btn {
  padding: 14px 28px;
  font-size: 16px;
  border-radius: 8px;
  background: #fff;
  color: #2384d6;
  border: none;
  font-weight: 600;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
    background: #fff;
    color: #1a6cb3;
  }
  
  i {
    margin-right: 6px;
  }
}

/* 试卷统计信息 */
.exam-stats {
  display: flex;
  padding: 20px;
  gap: 16px;
  background: #fff;
}

.stat-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 10px;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  i {
    font-size: 24px;
    color: #fff !important;
  }
  
  .el-icon-finished,
  .el-icon-edit,
  .el-icon-check,
  .el-icon-edit-outline,
  .el-icon-timer {
    color: #fff !important;
  }
  
  &.choice {
    background: linear-gradient(135deg, #36d1dc 0%, #5b86e5 100%);
  }
  
  &.fill {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  }
  
  &.judge {
    background: linear-gradient(135deg, #ee0979 0%, #ff6a00 100%);
  }
  
  &.time {
    background: linear-gradient(135deg, #2384d6 0%, #1a6cb3 100%);
  }
  
  &.essay {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
}

.stat-content {
  .stat-value {
    font-size: 24px;
    font-weight: 700;
    color: #333;
    line-height: 1.2;
  }
  
  .stat-label {
    font-size: 13px;
    color: #888;
    margin-top: 4px;
  }
}

/* 考生须知入口 */
.exam-notice {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  background: #fffbeb;
  border-top: 1px solid #fef3c7;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 0 0 12px 12px;
  
  &:hover {
    background: #fef3c7;
  }
  
  i:first-child {
    font-size: 18px;
    color: #f59e0b;
    margin-right: 8px;
  }
  
  span {
    flex: 1;
    color: #92400e;
    font-size: 14px;
  }
  
  i:last-child {
    color: #d97706;
  }
}

/* 试题内容区域 */
.content {
  margin-top: 20px;
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.content .header {
  padding: 10px 30px;
}

#msg .content .title {
  font-size: 20px;
  margin: 0px;
  display: flex;
  align-items: center;
}

.content .title span {
  margin-right: 10px;
}

.content .title .time {
  font-size: 16px;
  margin-left: 420px;
  color: #999;
}

.content .inner .titlei {
  margin-left: 20px;
  font-size: 16px;
  color: #88949b;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 10px;
}

.content .inner .titlei .topic-summary {
  color: #999;
  font-size: 14px;
  font-weight: normal;
}

.contenti {
  padding: 10px 20px;
}

.contenti .el-table {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 10px;
}

.contenti .el-table th {
  background-color: #f5f7fa !important;
  color: #333 !important;
  font-weight: 600;
}

.contenti .el-table td {
  color: #333 !important;
}

.contenti .el-table tr:hover > td {
  background-color: #ecf5ff !important;
}

.inner .contenti .question {
  margin-left: 40px;
  color: #9a9a9a;
  font-size: 14px;
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .header-left {
    flex-direction: column;
  }
  
  .exam-meta {
    justify-content: center;
  }
  
  .exam-stats {
    flex-wrap: wrap;
  }
  
  .stat-item {
    min-width: calc(50% - 8px);
  }
}
</style>
