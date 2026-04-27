<template>
  <div class="student-trend">
    <!-- 页面标题 -->
    <div class="page-header" :style="{ backgroundColor: currentThemeColor }">
      <h2 style="color: #fff;">个人成绩趋势分析</h2>
      <div class="header-actions">
        <el-button icon="el-icon-refresh" @click="refreshData" size="medium" class="action-btn header-btn">刷新数据</el-button>
        <el-dropdown @command="handleExportCommand" style="margin-left: 10px;">
          <el-button icon="el-icon-download" size="medium" class="action-btn header-btn">
            导出报告<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="csv">导出CSV数据报告</el-dropdown-item>
            <el-dropdown-item command="trendChart">导出成绩趋势图</el-dropdown-item>
            <el-dropdown-item command="subjectChart">导出科目分布图</el-dropdown-item>
            <el-dropdown-item command="all">导出全部（数据+图表）</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="filter-section" v-if="trendData && trendData.hasTrend">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="filter-item">
            <span class="filter-label">时间范围：</span>
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              value-format="yyyy-MM-dd"
              :unlink-panels="true"
              @change="handleFilterChange">
            </el-date-picker>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="filter-item">
            <span class="filter-label">科目：</span>
            <el-select v-model="selectedSubject" placeholder="全部科目" size="small" clearable @change="handleFilterChange">
              <el-option label="全部科目" value=""></el-option>
              <el-option v-for="subject in subjectList" :key="subject" :label="subject" :value="subject"></el-option>
            </el-select>
          </div>
        </el-col>
        <el-col :span="4">
          <el-button size="small" @click="resetFilters" icon="el-icon-refresh-right" class="reset-filter-btn">重置筛选</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 趋势概览卡片 -->
    <div class="trend-overview" v-if="trendData && trendData.hasTrend">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ trendData.averageScore }}</div>
              <div class="stats-label">平均分</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ trendData.maxScore }}</div>
              <div class="stats-label">最高分</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value">{{ trendData.minScore }}</div>
              <div class="stats-label">最低分</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-item">
              <div class="stats-value" :class="getTrendClass()">
                {{ trendData.overallTrend || '稳定' }}
              </div>
              <div class="stats-label">总体趋势</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 成绩趋势图表 -->
    <div class="trend-charts" v-if="trendData && trendData.hasTrend">
      <el-row :gutter="20">
        <!-- 成绩趋势折线图 -->
        <el-col :span="16">
          <el-card class="chart-card" shadow="never" :body-style="{ padding: 0 }">
            <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
              <span>成绩变化趋势</span>
              <el-switch v-model="showTrendLine" active-text="显示趋势线"></el-switch>
            </div>
            <div ref="trendChart" class="chart-container" style="padding: 20px;"></div>
          </el-card>
        </el-col>

        <!-- 科目分布饼图 -->
        <el-col :span="8">
          <el-card class="chart-card" shadow="never" :body-style="{ padding: 0 }">
            <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
              <span>科目分布</span>
            </div>
            <div ref="subjectChart" class="chart-container" style="padding: 20px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细成绩记录表格 -->
    <div class="detailed-records" v-if="trendData && trendData.hasTrend" style="margin-top: 20px;">
      <el-card shadow="never" :body-style="{ padding: 0 }">
        <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
          <span>详细成绩记录</span>
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程名称或考试名称"
            prefix-icon="el-icon-search"
            style="width: 300px;">
          </el-input>
        </div>
        
        <el-table :data="paginatedRecords" style="width: 100%" stripe>
          <el-table-column label="序号" width="60" align="center">
            <template slot-scope="scope">
              {{ (pagination.currentPage - 1) * pagination.pageSize + scope.$index + 1 }}
            </template>
          </el-table-column>
          <el-table-column prop="answerDate" label="考试日期" min-width="120" sortable></el-table-column>
          <el-table-column prop="subject" label="课程名称" min-width="120"></el-table-column>
          <el-table-column prop="examDescription" label="考试名称" min-width="180"></el-table-column>
          <el-table-column prop="examType" label="考试类型" min-width="100">
            <template slot-scope="scope">
              <span>{{ scope.row.examType || '正式考试' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="考试分数" min-width="100" sortable>
            <template slot-scope="scope">
              <span v-if="scope.row.isGraded" :class="getScoreClass(scope.row.etScore)">{{ scope.row.etScore }}</span>
              <el-tag v-else type="warning" size="mini">待批改</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="成绩等级" min-width="90">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.isGraded" :type="getGradeType(scope.row.etScore)">
                {{ getGradeText(scope.row.etScore) }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="100">
            <template slot-scope="scope">
              <el-button size="mini" type="text" @click="viewExamDetail(scope.row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页组件 -->
        <el-pagination
          style="margin-top: 15px; text-align: center;"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredRecords.length">
        </el-pagination>
      </el-card>
    </div>

    <!-- 无数据提示 -->
    <div v-if="trendData && !trendData.hasTrend" class="no-data">
      <el-empty description="暂无成绩数据">
        <el-button type="primary" @click="$router.push('/student')">去参加考试</el-button>
      </el-empty>
    </div>

    <!-- 加载状态 -->
    <div v-loading="loading" element-loading-text="正在加载趋势分析..." element-loading-spinner="el-icon-loading" element-loading-background="rgba(0, 0, 0, 0.8)" class="loading-container">
    </div>
  </div>
</template>

<script>
export default {
  name: 'StudentTrend',
  data() {
    return {
      loading: false,
      studentId: null,
      showTrendLine: true,
      searchKeyword: '',
      
      // 分页配置
      pagination: {
        currentPage: 1,
        pageSize: 10
      },
      
      // 趋势数据
      trendData: null,
      
      // 图表实例
      trendChart: null,
      subjectChart: null,
      
      // 主题相关
      isDarkTheme: false,
      // 当前主题色
      currentThemeColor: '#409EFF',
      
      // 筛选相关
      dateRange: null,
      selectedSubject: '',
      subjectList: [],
      originalTrendData: null
    }
  },
  computed: {
    filteredRecords() {
      if (!this.trendData || !this.trendData.trendData) return []
      
      if (!this.searchKeyword) return this.trendData.trendData
      
      return this.trendData.trendData.filter(record => 
        record.subject.includes(this.searchKeyword) ||
        record.examDescription.includes(this.searchKeyword)
      )
    },
    // 分页后的数据
    paginatedRecords() {
      const start = (this.pagination.currentPage - 1) * this.pagination.pageSize
      const end = start + this.pagination.pageSize
      return this.filteredRecords.slice(start, end)
    }
  },
  created() {
    this.studentId = this.$cookies.get("cid") || this.$route.query.studentId
    this.loadTrendAnalysis()
    this.checkTheme()
    // 初始化主题色
    this.currentThemeColor = this.getThemeColor()
  },
  mounted() {
    this.$nextTick(() => {
      this.initCharts()
      this.observeThemeChange()
      // 强制设置页面标题背景色
      this.applyThemeToHeader()
      // 监听窗口大小变化
      this.handleResize = () => {
        if (this.trendChart) this.trendChart.resize()
        if (this.subjectChart) this.subjectChart.resize()
      }
      window.addEventListener('resize', this.handleResize)
    })
  },
  beforeDestroy() {
    // 移除窗口大小变化监听
    if (this.handleResize) {
      window.removeEventListener('resize', this.handleResize)
    }
  },
  watch: {
    showTrendLine() {
      this.updateTrendChart()
    }
  },
  methods: {
    // 加载趋势分析数据
    loadTrendAnalysis() {
      this.loading = true
      this.$axios.get(`/api/analysis/trend/student/${this.studentId}`)
        .then(res => {
          if (res.data.code === 200) {
            this.trendData = res.data.data
            // 保存原始数据用于筛选
            this.originalTrendData = JSON.parse(JSON.stringify(res.data.data))
            // 提取科目列表
            this.extractSubjectList()
            
            // 延迟更新图表，确保DOM已渲染
            this.$nextTick(() => {
              if (this.trendData && this.trendData.hasTrend) {
                setTimeout(() => {
                  this.updateAllCharts()
                }, 100)
              }
            })
          } else {
            this.$message.error('获取趋势分析失败：' + res.data.message)
          }
        })
        .catch(error => {
          this.$message.error('获取趋势分析失败：' + error.message)
        })
        .finally(() => {
          this.loading = false
        })
    },

    // 提取科目列表
    extractSubjectList() {
      if (!this.originalTrendData || !this.originalTrendData.trendData) return
      const subjects = new Set()
      this.originalTrendData.trendData.forEach(item => {
        if (item.subject) subjects.add(item.subject)
      })
      this.subjectList = Array.from(subjects)
    },
    
    // 处理筛选变化
    handleFilterChange(val) {
      // 对于日期范围选择器，只有两个日期都选择后才筛选
      if (Array.isArray(val)) {
        if (val && val.length === 2 && val[0] && val[1]) {
          this.applyFilters()
        }
        return
      }
      // 科目选择器直接筛选
      this.applyFilters()
    },
    
    // 应用筛选
    applyFilters() {
      if (!this.originalTrendData) return
      
      let filteredData = [...this.originalTrendData.trendData]
      
      // 时间范围筛选
      if (this.dateRange && this.dateRange.length === 2) {
        const [startDate, endDate] = this.dateRange
        filteredData = filteredData.filter(item => {
          const itemDate = item.answerDate || item.examDate || item.date
          if (!itemDate) return true // 没有日期的记录不过滤
          return itemDate >= startDate && itemDate <= endDate
        })
      }
      
      // 科目筛选
      if (this.selectedSubject) {
        filteredData = filteredData.filter(item => item.subject === this.selectedSubject)
      }
      
      // 检查筛选结果
      if (filteredData.length === 0) {
        this.$message.warning('该筛选条件下暂无成绩数据')
        return // 不更新数据，保持原有显示
      }
      
      // 更新趋势数据
      this.trendData = {
        ...this.originalTrendData,
        trendData: filteredData,
        hasTrend: filteredData.length > 0
      }
      
      // 重新计算统计数据
      if (filteredData.length > 0) {
        const scores = filteredData.map(item => item.etScore)
        this.trendData.averageScore = (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(2)
        this.trendData.maxScore = Math.max(...scores)
        this.trendData.minScore = Math.min(...scores)
        
        // 重新计算总体趋势
        if (scores.length >= 2) {
          const firstHalf = scores.slice(0, Math.floor(scores.length / 2))
          const secondHalf = scores.slice(Math.floor(scores.length / 2))
          const firstAvg = firstHalf.reduce((a, b) => a + b, 0) / firstHalf.length
          const secondAvg = secondHalf.reduce((a, b) => a + b, 0) / secondHalf.length
          if (secondAvg > firstAvg + 5) {
            this.trendData.overallTrend = '上升'
          } else if (secondAvg < firstAvg - 5) {
            this.trendData.overallTrend = '下降'
          } else {
            this.trendData.overallTrend = '稳定'
          }
        }
        
        // 重新计算科目分布
        const subjectCount = {}
        filteredData.forEach(item => {
          subjectCount[item.subject] = (subjectCount[item.subject] || 0) + 1
        })
        this.trendData.subjectDistribution = subjectCount
      }
      
      // 更新图表
      this.$nextTick(() => {
        this.updateAllCharts()
      })
    },
    
    // 重置筛选
    resetFilters() {
      this.dateRange = null
      this.selectedSubject = ''
      this.trendData = JSON.parse(JSON.stringify(this.originalTrendData))
      this.$nextTick(() => {
        this.updateAllCharts()
      })
    },
    
    // 检查当前主题
    checkTheme() {
      this.isDarkTheme = document.body.classList.contains('dark-theme')
    },
    
    // 获取图表文字颜色
    getTextColor() {
      return this.isDarkTheme ? '#fff' : '#333'
    },
    
    // 强制应用主题色到页面标题
    applyThemeToHeader() {
      const header = this.$el.querySelector('.page-header')
      if (header) {
        header.style.backgroundColor = this.currentThemeColor
        header.style.setProperty('background-color', this.currentThemeColor, 'important')
      }
      // 同时应用到卡片标题
      const cardHeaders = this.$el.querySelectorAll('.custom-card-header')
      cardHeaders.forEach(h => {
        h.style.backgroundColor = this.currentThemeColor
        h.style.setProperty('background-color', this.currentThemeColor, 'important')
      })
    },
    
    // 获取主题色
    getThemeColor() {
      // 先尝试从 CSS 变量获取
      let color = getComputedStyle(document.documentElement).getPropertyValue('--primary-color').trim()
      if (color) return color
      
      // 如果 CSS 变量为空，尝试从 localStorage 获取
      const savedTheme = localStorage.getItem('studentTheme')
      const themes = {
        '默认蓝': '#2384d6',
        '科技紫': '#7c3aed',
        '活力橙': '#f97316',
        '森林绿': '#059669',
        '玫瑰红': '#e11d48',
        '天空蓝': '#0ea5e9',
        '琥珀黄': '#d97706',
        '石墨灰': '#475569'
      }
      return themes[savedTheme] || '#409EFF'
    },
    
    // 监听主题切换
    observeThemeChange() {
      const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
          if (mutation.type === 'attributes' && (mutation.attributeName === 'class' || mutation.attributeName === 'style')) {
            console.log('成绩趋势图表检测到主题切换，重新渲染图表')
            this.checkTheme()
            // 更新主题色
            this.currentThemeColor = this.getThemeColor()
            // 重新应用主题色到页面元素
            this.applyThemeToHeader()
            setTimeout(() => {
              this.forceUpdateCharts()
            }, 100)
          }
        })
      })
      
      observer.observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['class', 'style']
      })
      
      this.$once('hook:beforeDestroy', () => {
        observer.disconnect()
      })
    },
    
    // 强制重新渲染所有图表
    forceUpdateCharts() {
      if (!this.trendData || !this.trendData.hasTrend) return
      
      // 销毁旧实例
      if (this.trendChart) {
        this.trendChart.dispose()
        this.trendChart = null
      }
      if (this.subjectChart) {
        this.subjectChart.dispose()
        this.subjectChart = null
      }
      
      // 重新初始化
      setTimeout(() => {
        this.initCharts()
        this.updateAllCharts()
      }, 50)
    },
    
    // 初始化图表
    initCharts() {
      if (this.$refs.trendChart) {
        this.trendChart = this.$echarts.init(this.$refs.trendChart)
      }
      if (this.$refs.subjectChart) {
        this.subjectChart = this.$echarts.init(this.$refs.subjectChart)
      }
    },

    // 更新所有图表
    updateAllCharts() {
      // 确保图表实例已初始化
      if (!this.trendChart && this.$refs.trendChart) {
        this.trendChart = this.$echarts.init(this.$refs.trendChart)
      }
      if (!this.subjectChart && this.$refs.subjectChart) {
        this.subjectChart = this.$echarts.init(this.$refs.subjectChart)
      }
      this.updateTrendChart()
      this.updateSubjectChart()
    },

    // 更新趋势图表
    updateTrendChart() {
      if (!this.trendChart || !this.trendData || !this.trendData.trendData) return
      
      const dates = this.trendData.trendData.map(item => item.answerDate)
      const scores = this.trendData.trendData.map(item => item.etScore)
      const subjects = this.trendData.trendData.map(item => item.subject)
      const examDescriptions = this.trendData.trendData.map(item => item.examDescription || '')
      
      const themeColor = this.getThemeColor()
      const series = [{
        name: '考试分数',
        type: 'line',
        data: scores,
        itemStyle: {
          color: themeColor
        },
        lineStyle: {
          width: 3
        },
        symbol: 'circle',
        symbolSize: 8
      }]
      
      // 添加趋势线
      if (this.showTrendLine && scores.length > 1) {
        // 简单线性回归计算趋势线
        const n = scores.length
        const sumX = dates.reduce((sum, _, index) => sum + index, 0)
        const sumY = scores.reduce((sum, score) => sum + score, 0)
        const sumXY = scores.reduce((sum, score, index) => sum + index * score, 0)
        const sumX2 = dates.reduce((sum, _, index) => sum + index * index, 0)
        
        const slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        const intercept = (sumY - slope * sumX) / n
        
        const trendData = dates.map((_, index) => slope * index + intercept)
        
        series.push({
          name: '趋势线',
          type: 'line',
          data: trendData,
          itemStyle: {
            color: '#F56C6C'
          },
          lineStyle: {
            type: 'dashed',
            width: 2
          },
          symbol: 'none'
        })
      }
      
      const textColor = this.getTextColor()
      const option = {
        title: {
          text: '个人成绩变化趋势',
          left: 'center',
          textStyle: { color: textColor }
        },
        tooltip: {
          trigger: 'axis',
          backgroundColor: themeColor + 'E6',
          borderColor: themeColor,
          borderWidth: 1,
          textStyle: {
            color: '#fff'
          },
          formatter: function(params) {
            const dataIndex = params[0].dataIndex
            const examDesc = examDescriptions[dataIndex] ? `<div>📝 ${examDescriptions[dataIndex]}</div>` : ''
            return `<div style="padding: 4px 8px;">
                    <div style="font-weight: bold; margin-bottom: 4px;">📅 ${dates[dataIndex]}</div>
                    <div>📚 ${subjects[dataIndex]}</div>
                    ${examDesc}
                    <div>📊 ${params[0].value}分</div>
                    </div>`
          }
        },
        legend: {
          data: series.map(s => s.name),
          top: 30,
          textStyle: { color: textColor }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            rotate: 45,
            color: textColor,
            fontSize: 11,
            interval: 'auto',
            formatter: function(value) {
              return value.substring(5); // 只显示 MM-DD
            }
          },
          axisLine: { lineStyle: { color: textColor } }
        },
        yAxis: {
          type: 'value',
          name: '分数',
          min: 0,
          max: 100,
          axisLabel: { color: textColor },
          axisLine: { lineStyle: { color: textColor } },
          splitLine: { lineStyle: { color: this.isDarkTheme ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)' } }
        },
        series: series
      }
      
      this.trendChart.setOption(option, true)
    },

    // 更新科目分布图表
    updateSubjectChart() {
      if (!this.subjectChart || !this.trendData || !this.trendData.subjectDistribution) return
      
      const data = Object.entries(this.trendData.subjectDistribution).map(([subject, count]) => ({
        name: subject,
        value: count
      }))
      
      const textColor = this.getTextColor()
      const themeColor = this.getThemeColor()
      const option = {
        title: {
          text: '科目考试次数分布',
          left: 'center',
          textStyle: { color: textColor }
        },
        tooltip: {
          trigger: 'item',
          backgroundColor: themeColor + 'E6',
          borderColor: themeColor,
          borderWidth: 1,
          textStyle: {
            color: '#fff'
          },
          formatter: function(params) {
            return `<div style="padding: 4px 8px;">
                    <div style="font-weight: bold; margin-bottom: 4px;">📊 ${params.seriesName}</div>
                    <div>📚 ${params.name}: ${params.value}次 (${params.percent}%)</div>
                    </div>`
          }
        },
        series: [{
          name: '考试次数',
          type: 'pie',
          radius: '60%',
          data: data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      
      this.subjectChart.setOption(option, true)
    },

    // 获取趋势样式类
    getTrendClass() {
      if (!this.trendData.overallTrend) return ''
      
      switch (this.trendData.overallTrend) {
        case '上升': return 'trend-up'
        case '下降': return 'trend-down'
        default: return 'trend-stable'
      }
    },

    // 获取分数样式类
    getScoreClass(score) {
      if (score >= 90) return 'score-excellent'
      if (score >= 80) return 'score-good'
      if (score >= 70) return 'score-fair'
      if (score >= 60) return 'score-pass'
      return 'score-fail'
    },

    // 获取成绩等级类型
    getGradeType(score) {
      if (score >= 90) return 'success'
      if (score >= 80) return 'primary'
      if (score >= 70) return 'warning'
      if (score >= 60) return 'info'
      return 'danger'
    },

    // 获取成绩等级文本
    getGradeText(score) {
      if (score >= 90) return '优秀'
      if (score >= 80) return '良好'
      if (score >= 70) return '中等'
      if (score >= 60) return '及格'
      return '不及格'
    },

    // 分页大小改变
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.pagination.currentPage = 1
    },
    
    // 当前页改变
    handleCurrentChange(val) {
      this.pagination.currentPage = val
    },
    
    // 查看考试详情
    viewExamDetail(record) {
      this.$router.push({
        path: '/examDetail',
        query: {
          examCode: record.examCode,
          studentId: this.studentId
        }
      })
    },

    // 刷新数据
    refreshData() {
      this.checkTheme()
      this.forceUpdateCharts()
      this.loadTrendAnalysis()
    },

    // 处理导出命令
    handleExportCommand(command) {
      switch (command) {
        case 'csv':
          this.exportTrendReport()
          break
        case 'trendChart':
          this.exportChart(this.trendChart, '成绩趋势图')
          break
        case 'subjectChart':
          this.exportChart(this.subjectChart, '科目分布图')
          break
        case 'all':
          this.exportAll()
          break
      }
    },
    
    // 导出图表为图片
    exportChart(chart, name) {
      if (!chart) {
        this.$message.warning('图表未加载，无法导出')
        return
      }
      const url = chart.getDataURL({
        type: 'png',
        pixelRatio: 2,
        backgroundColor: '#fff'
      })
      const link = document.createElement('a')
      link.download = `${name}_${this.studentId}_${new Date().toISOString().slice(0, 10)}.png`
      link.href = url
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      this.$message.success(`${name}导出成功`)
    },
    
    // 导出全部（数据+图表）
    exportAll() {
      this.exportTrendReport()
      setTimeout(() => {
        this.exportChart(this.trendChart, '成绩趋势图')
      }, 500)
      setTimeout(() => {
        this.exportChart(this.subjectChart, '科目分布图')
      }, 1000)
    },
    
    // 导出CSV数据报告
    exportTrendReport() {
      if (!this.trendData || !this.trendData.trendData || this.trendData.trendData.length === 0) {
        this.$message.warning('暂无成绩数据可导出')
        return
      }
      
      // 构建CSV内容
      let csvContent = '\uFEFF' // BOM头，解决中文乱码
      
      // 添加报告标题
      csvContent += '个人成绩趋势分析报告\n'
      csvContent += `导出时间：${new Date().toLocaleString()}\n`
      csvContent += `学生ID：${this.studentId}\n\n`
      
      // 添加统计概览
      csvContent += '【统计概览】\n'
      csvContent += `平均分,${this.trendData.averageScore}\n`
      csvContent += `最高分,${this.trendData.maxScore}\n`
      csvContent += `最低分,${this.trendData.minScore}\n`
      csvContent += `总体趋势,${this.trendData.overallTrend || '稳定'}\n\n`
      
      // 添加科目分布
      csvContent += '【科目考试次数分布】\n'
      csvContent += '科目名称,考试次数\n'
      if (this.trendData.subjectDistribution) {
        Object.entries(this.trendData.subjectDistribution).forEach(([subject, count]) => {
          csvContent += `${subject},${count}\n`
        })
      }
      csvContent += '\n'
      
      // 添加详细成绩记录
      csvContent += '【详细成绩记录】\n'
      csvContent += '序号,考试日期,课程名称,考试名称,考试类型,考试分数,批改状态\n'
      this.trendData.trendData.forEach((record, index) => {
        const gradingStatus = record.isGraded ? '已批改' : '待批改'
        const score = record.isGraded ? record.etScore : '-'
        csvContent += `${index + 1},${record.answerDate},${record.subject},${record.examDescription || '-'},${record.examType || '正式考试'},${score},${gradingStatus}\n`
      })
      
      // 创建下载
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      link.setAttribute('download', `成绩趋势报告_${this.studentId}_${new Date().toISOString().slice(0, 10)}.csv`)
      link.style.visibility = 'hidden'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      this.$message.success('趋势报告导出成功')
    }
  }
}
</script>

<style lang="less" scoped>
.student-trend {
  padding: 20px;
  
  // 筛选区域样式
  .filter-section {
    background: #fff;
    padding: 15px 20px;
    border-radius: 8px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    
    .filter-item {
      display: flex;
      align-items: center;
      
      .filter-label {
        font-size: 14px;
        color: #606266;
        white-space: nowrap;
        margin-right: 8px;
      }
      
      .el-date-editor, .el-select {
        flex: 1;
      }
    }
    
    .reset-filter-btn {
      background-color: var(--primary-color, #409EFF) !important;
      border-color: var(--primary-color, #409EFF) !important;
      color: #fff !important;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }
      
      i {
        color: #fff !important;
      }
    }
  }

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px 20px;
    border-radius: 8px;
    
    h2 {
      margin: 0;
      color: #fff !important;
    }
    
    .header-actions {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .el-button {
        margin: 0;
      }
      
      .action-btn {
        min-width: 140px;
        height: 42px;
        padding: 10px 20px;
        font-size: 14px;
        font-weight: 500;
        border-radius: 8px;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }
    }
  }
  
  .trend-overview {
    margin-bottom: 20px;
    
    .stats-card {
      text-align: center;
      
      .stats-item {
        .stats-value {
          font-size: 28px;
          font-weight: bold;
          margin-bottom: 8px;
          
          &.trend-up {
            color: #67C23A;
          }
          
          &.trend-down {
            color: #F56C6C;
          }
          
          &.trend-stable {
            color: var(--primary-color, #409EFF);
          }
        }
        
        .stats-label {
          font-size: 14px;
          color: #666;
        }
      }
    }
  }
  
  .chart-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      span {
        color: #fff !important;
        font-weight: 600;
      }
    }
    
    .chart-container {
      height: 350px;
      width: 100%;
    }
  }
  
  // 所有卡片标题使用主题色背景
  /deep/ .el-card__header {
    background-color: var(--primary-color, #409EFF) !important;
    color: #fff !important;
    border-bottom: none;
    padding: 15px 20px;
    
    span {
      color: #fff !important;
      font-weight: 600;
    }
    
    .card-header {
      span {
        color: #fff !important;
      }
    }
    
    .el-switch__label {
      color: #fff !important;
    }
  }
  
  // 详细记录卡片标题样式
  .detailed-records {
    /deep/ .el-card__header {
      background-color: var(--primary-color, #409EFF);
      color: #fff;
      border-bottom: none;
      padding: 15px 20px;
      
      span {
        color: #fff;
        font-weight: 600;
      }
      
      .card-header {
        span {
          color: #fff;
        }
      }
    }
    
    /deep/ .el-table th {
      background-color: var(--primary-color, #409EFF) !important;
      color: #fff !important;
    }
    
    /deep/ .el-table th .cell {
      color: #fff !important;
    }
    
    // 排序箭头白色
    /deep/ .el-table .caret-wrapper .sort-caret.ascending {
      border-bottom-color: rgba(255, 255, 255, 0.6) !important;
    }
    
    /deep/ .el-table .caret-wrapper .sort-caret.descending {
      border-top-color: rgba(255, 255, 255, 0.6) !important;
    }
    
    /deep/ .el-table .ascending .caret-wrapper .sort-caret.ascending {
      border-bottom-color: #fff !important;
    }
    
    /deep/ .el-table .descending .caret-wrapper .sort-caret.descending {
      border-top-color: #fff !important;
    }
  }
  
  .score-excellent {
    color: #67C23A;
    font-weight: bold;
  }
  
  .score-good {
    color: var(--primary-color, #409EFF);
    font-weight: bold;
  }
  
  .score-fair {
    color: #E6A23C;
  }
  
  .score-pass {
    color: #909399;
  }
  
  .score-fail {
    color: #F56C6C;
    font-weight: bold;
  }
  
  .no-data {
    text-align: center;
    padding: 50px 0;
  }
  
  .loading-container {
    position: relative;
    height: 200px;
  }
}
</style>

<style>
/* 全局样式 - 确保成绩趋势页面卡片标题使用主题色 */
.student-trend .el-card__header {
    background-color: var(--primary-color, #409EFF) !important;
    color: #fff !important;
    border-bottom: none !important;
    padding: 15px 20px !important;
}

.student-trend .el-card__header span {
    color: #fff !important;
}

.student-trend .el-card__header .el-switch__label {
    color: #fff !important;
}

.student-trend .page-header h2 {
    color: #fff !important;
}

/* 覆盖 el-card header 的默认浅蓝色背景 */
.student-trend .el-card__header {
    background-color: transparent !important;
    padding: 0 !important;
    border-bottom: none !important;
}

.student-trend .card-header {
    padding: 15px 20px !important;
}

/* 自定义卡片标题样式 */
.student-trend .custom-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    color: #fff;
    border-radius: 4px 4px 0 0;
}

.student-trend .custom-card-header span {
    color: #fff;
    font-weight: 600;
    font-size: 16px;
}

.student-trend .custom-card-header .el-switch__label {
    color: #fff !important;
}

/* 刷新数据按钮白色文字和图标 */
.student-trend .page-header .el-button--primary {
    background-color: rgba(255, 255, 255, 0.2) !important;
    border-color: #fff !important;
    color: #fff !important;
}

.student-trend .page-header .el-button--primary i {
    color: #fff !important;
}

.student-trend .page-header .el-button--primary:hover {
    background-color: rgba(255, 255, 255, 0.3) !important;
}

/* 重置筛选按钮样式 */
.student-trend .reset-filter-btn.el-button {
    background-color: var(--primary-color, #409EFF) !important;
    border-color: var(--primary-color, #409EFF) !important;
    color: #fff !important;
}

.student-trend .reset-filter-btn.el-button span {
    color: #fff !important;
}

.student-trend .reset-filter-btn.el-button i {
    color: #fff !important;
}

.student-trend .reset-filter-btn.el-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
