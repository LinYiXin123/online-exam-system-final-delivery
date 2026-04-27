<template>
  <div class="dashboard">
    <!-- 页面标题 - 与下方卡片头部风格统一 -->
    <el-card shadow="never" class="title-card" :body-style="{ padding: 0 }">
      <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
        <span><i class="el-icon-data-analysis"></i> 系统仪表盘</span>
        <el-button icon="el-icon-refresh" @click="refreshData" size="small" class="header-btn" :style="{ backgroundColor: '#fff', borderColor: '#fff', color: currentThemeColor }">刷新数据</el-button>
      </div>
    </el-card>

    <!-- 筛选条件区域 -->
    <el-card shadow="never" class="filter-card">
      <el-row :gutter="12" type="flex" align="middle">
        <el-col :span="4">
          <el-select v-model="filterForm.institute" placeholder="学院" clearable size="small" style="width: 100%" @change="onInstituteChange">
            <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterForm.major" placeholder="专业" clearable size="small" style="width: 100%" @change="onMajorChange" :disabled="!filterForm.institute">
            <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-select v-model="filterForm.clazz" placeholder="班级" clearable size="small" style="width: 100%" :disabled="!filterForm.major">
            <el-option v-for="item in clazzOptions" :key="item" :label="item + '班'" :value="item"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterForm.subject" placeholder="科目" clearable size="small" style="width: 100%">
            <el-option v-for="item in subjectOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-select v-model="filterForm.examCode" placeholder="考试/试卷" clearable size="small" style="width: 100%">
            <el-option v-for="item in examOptions" :key="item.examCode" :label="item.source + ' - ' + item.description" :value="item.examCode"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4" style="display: flex; gap: 8px;">
          <el-button size="small" @click="applyFilter" icon="el-icon-search" :style="{ backgroundColor: '#fff', borderColor: currentThemeColor, color: currentThemeColor }">筛选</el-button>
          <el-button size="small" @click="resetFilter" icon="el-icon-refresh-left" :style="{ backgroundColor: '#fff', borderColor: currentThemeColor, color: currentThemeColor }">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2);">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.studentCount || 0 }}</div>
            <div class="stat-label">学生总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c);">
            <i class="el-icon-s-custom"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.teacherCount || 0 }}</div>
            <div class="stat-label">教师总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe);">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.examCount || 0 }}</div>
            <div class="stat-label">考试总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7);">
            <i class="el-icon-edit-outline"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.questionCount || 0 }}</div>
            <div class="stat-label">题目总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a, #fee140);">
            <i class="el-icon-trophy"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.scoreCount || 0 }}</div>
            <div class="stat-label">成绩记录</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #a8edea, #fed6e3);">
            <i class="el-icon-chat-dot-round"></i>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.messageCount || 0 }}</div>
            <div class="stat-label">留言总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 学院分布图表 -->
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" :body-style="{ padding: 0 }">
          <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
            <span><i class="el-icon-office-building"></i> 各学院学生分布</span>
            <div class="chart-type-switch">
              <el-button-group size="mini">
                <el-button :class="{ active: instituteChartType === 'pie' }" @click="switchInstituteChart('pie')" icon="el-icon-pie-chart"></el-button>
                <el-button :class="{ active: instituteChartType === 'bar' }" @click="switchInstituteChart('bar')" icon="el-icon-s-data"></el-button>
              </el-button-group>
            </div>
          </div>
          <div ref="instituteChart" class="chart-container" style="padding: 20px;"></div>
        </el-card>
      </el-col>
      <!-- 成绩分布图表 -->
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" :body-style="{ padding: 0 }">
          <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
            <span><i class="el-icon-data-line"></i> 成绩分布统计</span>
            <div class="chart-type-switch">
              <el-button-group size="mini">
                <el-button :class="{ active: scoreChartType === 'pie' }" @click="switchScoreChart('pie')" icon="el-icon-pie-chart"></el-button>
                <el-button :class="{ active: scoreChartType === 'bar' }" @click="switchScoreChart('bar')" icon="el-icon-s-data"></el-button>
              </el-button-group>
            </div>
          </div>
          <div ref="scoreChart" class="chart-container" style="padding: 20px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 题库统计柱状图 -->
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" :body-style="{ padding: 0 }">
          <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
            <span><i class="el-icon-tickets"></i> 题库统计</span>
            <div class="chart-type-switch">
              <el-button-group size="mini">
                <el-button :class="{ active: questionChartType === 'bar' }" @click="switchQuestionChart('bar')" icon="el-icon-s-data"></el-button>
                <el-button :class="{ active: questionChartType === 'pie' }" @click="switchQuestionChart('pie')" icon="el-icon-pie-chart"></el-button>
                <el-button :class="{ active: questionChartType === 'line' }" @click="switchQuestionChart('line')" icon="el-icon-data-line"></el-button>
              </el-button-group>
            </div>
          </div>
          <div ref="questionChart" class="chart-container" style="padding: 20px;"></div>
        </el-card>
      </el-col>
      <!-- 最近考试列表 -->
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" :body-style="{ padding: 0 }">
          <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
            <span><i class="el-icon-time"></i> 最近考试安排</span>
          </div>
          <div style="padding: 15px;">
            <el-table :data="recentExams" style="width: 100%" size="small" :row-style="{height: '45px'}" class="themed-table">
              <el-table-column prop="source" label="科目" width="120"></el-table-column>
              <el-table-column prop="description" label="考试描述" min-width="180" show-overflow-tooltip></el-table-column>
              <el-table-column prop="examDate" label="考试时间" width="110">
                <template slot-scope="scope">
                  {{ scope.row.examDate ? scope.row.examDate.substring(0, 10) : '' }}
                </template>
              </el-table-column>
              <el-table-column prop="institute" label="学院" width="120" show-overflow-tooltip></el-table-column>
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'AdminDashboard',
  data() {
    return {
      loading: false,
      overview: {},
      instituteDistribution: [],
      scoreDistribution: {},
      questionStats: {},
      recentExams: [],
      // 图表实例
      instituteChart: null,
      scoreChart: null,
      questionChart: null,
      // 图表类型
      instituteChartType: 'pie',
      scoreChartType: 'pie',
      questionChartType: 'bar',
      // 筛选条件
      filterForm: {
        institute: '',
        major: '',
        clazz: '',
        subject: '',
        examCode: ''
      },
      // 筛选选项
      instituteOptions: [],
      majorOptions: [],
      clazzOptions: [],
      subjectOptions: [],
      examOptions: [],
      // 所有学生数据（用于筛选）
      allStudents: [],
      // 专业-学院映射
      majorInstituteMap: {},
      // 当前主题色
      currentThemeColor: '#6366f1'
    }
  },
  computed: {},
  mounted() {
    this.updateThemeColor()
    this.loadFilterOptions()
    this.loadAllData()
    window.addEventListener('resize', this.handleResize)
    // 监听主题色变化
    this.themeObserver = new MutationObserver(() => {
      this.updateThemeColor()
    })
    this.themeObserver.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['style']
    })
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.themeObserver) this.themeObserver.disconnect()
    if (this.instituteChart) this.instituteChart.dispose()
    if (this.scoreChart) this.scoreChart.dispose()
    if (this.questionChart) this.questionChart.dispose()
  },
  methods: {
    // 加载所有数据
    async loadAllData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadOverview(),
          this.loadInstituteDistribution(),
          this.loadScoreDistribution(),
          this.loadQuestionStats(),
          this.loadRecentExams()
        ])
        this.$nextTick(() => {
          this.initCharts()
        })
      } catch (error) {
        console.error('加载数据失败:', error)
      } finally {
        this.loading = false
      }
    },

    // 构建筛选参数
    buildFilterParams() {
      const params = {}
      if (this.filterForm.institute) params.institute = this.filterForm.institute
      if (this.filterForm.major) params.major = this.filterForm.major
      if (this.filterForm.clazz) params.clazz = this.filterForm.clazz
      if (this.filterForm.subject) params.subject = this.filterForm.subject
      if (this.filterForm.examCode) params.examCode = this.filterForm.examCode
      return params
    },

    // 加载系统概览
    async loadOverview() {
      const params = this.buildFilterParams()
      const res = await this.$axios.get('/api/dashboard/overview', { params })
      if (res.data.code === 200) {
        this.overview = res.data.data
      }
    },

    // 加载学院分布
    async loadInstituteDistribution() {
      const params = this.buildFilterParams()
      const res = await this.$axios.get('/api/dashboard/institute-distribution', { params })
      if (res.data.code === 200) {
        this.instituteDistribution = res.data.data
      }
    },

    // 加载成绩分布
    async loadScoreDistribution() {
      const params = this.buildFilterParams()
      const res = await this.$axios.get('/api/dashboard/score-distribution', { params })
      if (res.data.code === 200) {
        this.scoreDistribution = res.data.data
      }
    },

    // 加载题库统计（包含主观题）
    async loadQuestionStats() {
      const params = this.buildFilterParams()
      const res = await this.$axios.get('/api/dashboard/question-stats', { params })
      if (res.data.code === 200) {
        this.questionStats = res.data.data
      }
    },

    // 加载最近考试
    async loadRecentExams() {
      const params = this.buildFilterParams()
      const res = await this.$axios.get('/api/dashboard/recent-exams', { params })
      if (res.data.code === 200) {
        this.recentExams = res.data.data
      }
    },

    // 初始化图表
    initCharts() {
      this.initInstituteChart()
      this.initScoreChart()
      this.initQuestionChart()
    },

    // 切换学院分布图表类型
    switchInstituteChart(type) {
      this.instituteChartType = type
      this.initInstituteChart()
    },

    // 学院分布图表 - 支持饼图和柱状图
    initInstituteChart() {
      if (!this.$refs.instituteChart) return
      if (this.instituteChart) this.instituteChart.dispose()
      this.instituteChart = this.$echarts.init(this.$refs.instituteChart)
      
      // 通用tooltip样式 - 白色背景、黑色文字
      const tooltipStyle = {
        backgroundColor: '#fff',
        borderColor: '#e0e0e0',
        borderWidth: 1,
        textStyle: { color: '#333' },
        extraCssText: 'box-shadow: 0 2px 8px rgba(0,0,0,0.15);'
      }
      
      let option = {}
      
      if (this.instituteChartType === 'pie') {
        option = {
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c}人 ({d}%)',
            ...tooltipStyle
          },
          legend: {
            orient: 'horizontal',
            bottom: 10,
            textStyle: { fontSize: 11 }
          },
          series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
            label: { show: false },
            emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
            data: this.instituteDistribution
          }]
        }
      } else {
        // 柱状图
        const names = this.instituteDistribution.map(item => item.name)
        const values = this.instituteDistribution.map(item => item.value)
        option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            formatter: '{b}: {c}人',
            ...tooltipStyle
          },
          grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
          xAxis: {
            type: 'category',
            data: names,
            axisLabel: { fontSize: 10, rotate: 30 }
          },
          yAxis: { type: 'value', axisLabel: { fontSize: 12 } },
          series: [{
            type: 'bar',
            barWidth: '60%',
            data: values,
            itemStyle: { borderRadius: [4, 4, 0, 0], color: '#409EFF' },
            label: { show: true, position: 'top', fontSize: 12, color: '#333' }
          }]
        }
      }
      this.instituteChart.setOption(option)
    },

    // 切换成绩分布图表类型
    switchScoreChart(type) {
      this.scoreChartType = type
      this.initScoreChart()
    },

    // 成绩分布图表 - 支持饼图和柱状图
    initScoreChart() {
      if (!this.$refs.scoreChart) return
      if (this.scoreChart) this.scoreChart.dispose()
      this.scoreChart = this.$echarts.init(this.$refs.scoreChart)
      
      const data = [
        { value: this.scoreDistribution.excellent || 0, name: '优秀(90-100)', itemStyle: { color: '#67C23A' } },
        { value: this.scoreDistribution.good || 0, name: '良好(80-89)', itemStyle: { color: '#409EFF' } },
        { value: this.scoreDistribution.medium || 0, name: '中等(70-79)', itemStyle: { color: '#E6A23C' } },
        { value: this.scoreDistribution.pass || 0, name: '及格(60-69)', itemStyle: { color: '#909399' } },
        { value: this.scoreDistribution.fail || 0, name: '不及格(<60)', itemStyle: { color: '#F56C6C' } }
      ]
      
      // 通用tooltip样式 - 白色背景、黑色文字
      const tooltipStyle = {
        backgroundColor: '#fff',
        borderColor: '#e0e0e0',
        borderWidth: 1,
        textStyle: { color: '#333' },
        extraCssText: 'box-shadow: 0 2px 8px rgba(0,0,0,0.15);'
      }
      
      let option = {}
      
      if (this.scoreChartType === 'pie') {
        option = {
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c}人 ({d}%)',
            ...tooltipStyle
          },
          legend: {
            orient: 'horizontal',
            bottom: 10,
            textStyle: { fontSize: 11 }
          },
          color: ['#67C23A', '#409EFF', '#E6A23C', '#909399', '#F56C6C'],
          series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
            label: { show: true, formatter: '{b}: {c}' },
            data: data
          }]
        }
      } else {
        // 柱状图
        option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            ...tooltipStyle
          },
          grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
          xAxis: {
            type: 'category',
            data: data.map(item => item.name),
            axisLabel: { fontSize: 10, rotate: 20 }
          },
          yAxis: { type: 'value', axisLabel: { fontSize: 12 } },
          series: [{
            type: 'bar',
            barWidth: '50%',
            data: data,
            itemStyle: { borderRadius: [4, 4, 0, 0] },
            label: { show: true, position: 'top', fontSize: 12, color: '#333' }
          }]
        }
      }
      this.scoreChart.setOption(option)
    },

    // 切换题库统计图表类型
    switchQuestionChart(type) {
      this.questionChartType = type
      this.initQuestionChart()
    },

    // 题库统计图表 - 支持多种类型
    initQuestionChart() {
      if (!this.$refs.questionChart) return
      if (this.questionChart) this.questionChart.dispose()
      this.questionChart = this.$echarts.init(this.$refs.questionChart)
      
      // 包含主观题的题库数据
      const chartData = [
        { value: this.questionStats.multiCount || 0, name: '选择题', itemStyle: { color: '#409EFF' } },
        { value: this.questionStats.fillCount || 0, name: '填空题', itemStyle: { color: '#67C23A' } },
        { value: this.questionStats.judgeCount || 0, name: '判断题', itemStyle: { color: '#E6A23C' } },
        { value: this.questionStats.essayCount || 0, name: '主观题', itemStyle: { color: '#F56C6C' } }
      ]
      
      // 通用tooltip样式 - 白色背景、黑色文字
      const tooltipStyle = {
        backgroundColor: '#fff',
        borderColor: '#e0e0e0',
        borderWidth: 1,
        textStyle: {
          color: '#333'
        },
        extraCssText: 'box-shadow: 0 2px 8px rgba(0,0,0,0.15);'
      }
      
      let option = {}
      
      if (this.questionChartType === 'bar') {
        // 柱状图
        option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' },
            ...tooltipStyle
          },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: {
            type: 'category',
            data: ['选择题', '填空题', '判断题', '主观题'],
            axisLabel: { fontSize: 11 }
          },
          yAxis: { type: 'value', axisLabel: { fontSize: 12 } },
          series: [{
            type: 'bar',
            barWidth: '50%',
            data: chartData,
            itemStyle: { borderRadius: [6, 6, 0, 0] },
            label: { show: true, position: 'top', fontSize: 12, color: '#333' }
          }]
        }
      } else if (this.questionChartType === 'pie') {
        // 饼图
        option = {
          tooltip: {
            trigger: 'item',
            formatter: '{b}: {c}道 ({d}%)',
            ...tooltipStyle
          },
          legend: {
            orient: 'horizontal',
            bottom: 10,
            data: ['选择题', '填空题', '判断题', '主观题']
          },
          series: [{
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            label: {
              show: true,
              formatter: '{b}: {c}'
            },
            data: chartData
          }]
        }
      } else if (this.questionChartType === 'line') {
        // 折线图 - 基础丝滑折线，无渐变区域
        option = {
          tooltip: {
            trigger: 'axis',
            ...tooltipStyle
          },
          grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
          xAxis: {
            type: 'category',
            data: ['选择题', '填空题', '判断题', '主观题'],
            axisLabel: { fontSize: 11 }
          },
          yAxis: { type: 'value', axisLabel: { fontSize: 12 } },
          series: [{
            type: 'line',
            smooth: 0.3,
            symbolSize: 8,
            data: chartData,
            lineStyle: { color: '#409EFF', width: 2 },
            itemStyle: { color: '#409EFF' },
            label: { show: true, position: 'top', fontSize: 12, color: '#333' }
          }]
        }
      }
      
      this.questionChart.setOption(option)
    },

    // 刷新数据
    refreshData() {
      this.loadAllData()
      this.$message.success('数据已刷新')
    },

    // 窗口大小变化
    handleResize() {
      if (this.instituteChart) this.instituteChart.resize()
      if (this.scoreChart) this.scoreChart.resize()
      if (this.questionChart) this.questionChart.resize()
    },

    // 更新主题色
    updateThemeColor() {
      const adminColor = getComputedStyle(document.documentElement).getPropertyValue('--admin-primary').trim()
      const primaryColor = getComputedStyle(document.documentElement).getPropertyValue('--primary-color').trim()
      this.currentThemeColor = adminColor || primaryColor || '#6366f1'
    },

    // 加载筛选选项
    async loadFilterOptions() {
      try {
        const res = await this.$axios.get('/api/dashboard/filter-options')
        if (res.data.code === 200) {
          const data = res.data.data
          // 学院选项
          this.instituteOptions = data.institutes || []
          // 学生数据（用于联动筛选）
          this.allStudents = data.students || []
          // 构建专业-学院映射
          this.allStudents.forEach(s => {
            if (s.major && s.institute) {
              this.majorInstituteMap[s.major] = s.institute
            }
          })
          // 考试列表
          this.examOptions = data.exams || []
          // 科目列表
          this.subjectOptions = data.subjects || []
        }
      } catch (error) {
        console.error('加载筛选选项失败:', error)
      }
    },

    // 学院变化时更新专业选项
    onInstituteChange(val) {
      this.filterForm.major = ''
      this.filterForm.clazz = ''
      if (val) {
        const majors = this.allStudents
          .filter(s => s.institute === val)
          .map(s => s.major)
          .filter(Boolean)
        this.majorOptions = [...new Set(majors)]
      } else {
        this.majorOptions = []
      }
      this.clazzOptions = []
    },

    // 专业变化时更新班级选项
    onMajorChange(val) {
      this.filterForm.clazz = ''
      if (val) {
        const clazzes = this.allStudents
          .filter(s => s.major === val && s.institute === this.filterForm.institute)
          .map(s => s.clazz)
          .filter(Boolean)
        this.clazzOptions = [...new Set(clazzes)].sort()
      } else {
        this.clazzOptions = []
      }
    },

    // 应用筛选
    applyFilter() {
      this.loadAllData()
      this.$message.success('筛选条件已应用')
    },

    // 重置筛选
    resetFilter() {
      this.filterForm = {
        institute: '',
        major: '',
        clazz: '',
        subject: '',
        examCode: ''
      }
      this.majorOptions = []
      this.clazzOptions = []
      this.loadAllData()
      this.$message.success('筛选条件已重置')
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard {
  padding: 0;
}

// 标题卡片 - 与下方图表卡片风格统一
.title-card {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
}

// 筛选条件卡片
.filter-card {
  border-radius: 12px;
  margin-bottom: 20px;
  
  /deep/ .el-card__body {
    padding: 15px 20px;
  }
  
  /deep/ .el-select {
    .el-input__inner {
      border-radius: 6px;
    }
  }
  
  /deep/ .el-button {
    border-radius: 6px;
  }
  
  // 筛选和重置按钮 - 白色背景、主题色边框和文字
  /deep/ .el-button {
    background-color: #fff !important;
    
    &:hover, &:focus {
      background-color: #fff !important;
      opacity: 0.9;
    }
  }
}

.stats-cards {
  margin-bottom: 20px;
  
  .stat-card {
    border-radius: 12px;
    overflow: hidden;
    
    /deep/ .el-card__body {
      display: flex;
      align-items: center;
      padding: 20px;
    }
    
    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      
      i {
        font-size: 28px;
        color: #fff !important;
      }
    }
    
    .stat-info {
      flex: 1;
      
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: var(--text-primary);
        line-height: 1.2;
      }
      
      .stat-label {
        font-size: 13px;
        color: var(--text-secondary);
        margin-top: 4px;
      }
    }
  }
}

// 通用卡片头部样式 - 标题卡片和图表卡片共用
.custom-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  color: #fff !important;
  
  span {
    color: #fff !important;
    font-weight: 600;
    font-size: 15px;
    
    i {
      margin-right: 8px;
      color: #fff !important;
    }
  }
  
  // 标题文字图标为白色（不影响按钮内图标）
  > span i {
    color: #fff !important;
  }
  
  /deep/ .header-btn.el-button {
    // 白色背景、主题色文字 - 使用!important确保生效
    background-color: #fff !important;
    border-color: #fff !important;
    border-radius: 6px;
    font-weight: 500;
    
    &:hover, &:focus {
      background-color: #fff !important;
      border-color: #fff !important;
      opacity: 0.9;
    }
  }
  
  // 图表类型切换按钮组 - 白色背景，选中时图标变主题色
  .chart-type-switch {
    /deep/ .el-button-group {
      .el-button {
        background: #fff !important;
        background-color: #fff !important;
        border-color: #fff !important;
        color: #ccc !important;
        padding: 5px 10px !important;
        transition: all 0.3s;
        border-radius: 4px !important;
        margin: 0 2px;
        
        i {
          color: #ccc !important;
          font-size: 14px;
        }
        
        &:hover {
          background: #fff !important;
          background-color: #fff !important;
          i {
            color: #999 !important;
          }
        }
        
        // 选中状态 - 图标变主题色
        &.active {
          background: #fff !important;
          background-color: #fff !important;
          border-color: #fff !important;
          box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
          
          i {
            color: var(--admin-primary, #6366f1) !important;
          }
        }
      }
    }
  }
}

.charts-row {
  margin-bottom: 20px;
  
  .chart-card {
    border-radius: 12px;
    overflow: hidden;
    
    .chart-container {
      height: 280px;
    }
    
    // 表格标题栏主题色
    /deep/ .themed-table th {
      background-color: var(--admin-primary, #6366f1) !important;
      color: #fff !important;
    }
    
    /deep/ .themed-table th .cell {
      color: #fff !important;
    }
  }
}
</style>
