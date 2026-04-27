<template>
  <div class="part">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">{{ name }} - 成绩分段分析</h2>
      <p class="page-subtitle" v-if="examDescription">{{ examDescription }}</p>
      <p class="page-subtitle" v-else>考试成绩统计与可视化展示</p>
    </div>

    <!-- 功能按钮区域 -->
    <div class="toolbar">
      <el-button 
        type="success" 
        icon="el-icon-download" 
        @click="exportExamScores"
        :loading="exportLoading"
        size="medium"
        class="action-btn">
        导出考试成绩
      </el-button>
      <el-button 
        type="info" 
        icon="el-icon-data-analysis" 
        @click="showStatistics"
        :loading="statsLoading"
        size="medium"
        class="action-btn">
        查看统计分析
      </el-button>
      <el-button 
        type="warning" 
        icon="el-icon-s-data" 
        @click="goToEnhancedAnalysis"
        size="medium"
        class="action-btn">
        增强数据分析
      </el-button>
    </div>

    <!-- 统计信息卡片 -->
    <div v-if="statistics" class="stats-cards">
      <el-row :gutter="24">
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-item">
              <div class="stats-icon">
                <i class="el-icon-user"></i>
              </div>
              <div class="stats-content">
                <div class="stats-value">{{ statistics.totalCount }}</div>
                <div class="stats-label">总人数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-item">
              <div class="stats-icon success">
                <i class="el-icon-check"></i>
              </div>
              <div class="stats-content">
                <div class="stats-value">{{ statistics.passCount }}</div>
                <div class="stats-label">及格人数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-item">
              <div class="stats-icon warning">
                <i class="el-icon-pie-chart"></i>
              </div>
              <div class="stats-content">
                <div class="stats-value">{{ statistics.passRate }}%</div>
                <div class="stats-label">及格率</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6" :md="6" :lg="6">
          <el-card class="stats-card" shadow="hover">
            <div class="stats-item">
              <div class="stats-icon info">
                <i class="el-icon-trophy"></i>
              </div>
              <div class="stats-content">
                <div class="stats-value">{{ statistics.averageScore }}</div>
                <div class="stats-label">平均分</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表容器 -->
    <div class="chart-wrapper">
      <el-card class="chart-card" shadow="hover">
        <div slot="header" class="chart-header">
          <span class="chart-title">{{ name }}分数段分布图</span>
        </div>
        <div class="box" ref="box"></div>
        <div v-if="isNull" class="no-data">
          <div class="no-data-icon">
            <i class="el-icon-warning-outline"></i>
          </div>
          <div class="no-data-text">
            <h3>暂无考试数据</h3>
            <p>该门考试还没人参考哦，请提醒学生参加考试。</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 学生列表弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="studentDialogVisible" width="70%" top="5vh">
      <el-table :data="segmentStudents" stripe border style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
        <el-table-column prop="studentId" label="学号" width="120" align="center"></el-table-column>
        <el-table-column prop="studentName" label="姓名" width="100" align="center"></el-table-column>
        <el-table-column prop="major" label="专业" min-width="120"></el-table-column>
        <el-table-column prop="grade" label="年级" width="80" align="center"></el-table-column>
        <el-table-column label="客观题分" width="100" align="center">
          <template slot-scope="scope">{{ scope.row.etScore || 0 }}</template>
        </el-table-column>
        <el-table-column label="主观题分" width="100" align="center">
          <template slot-scope="scope">{{ scope.row.ptScore || 0 }}</template>
        </el-table-column>
        <el-table-column label="总分" width="100" align="center">
          <template slot-scope="scope">
            <span style="font-weight: bold; color: #409EFF">{{ (scope.row.etScore || 0) + (scope.row.ptScore || 0) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="studentDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      isNull: false, //是否有成绩标志位
      name: null,
      examDescription: '', // 试卷介绍
      allScoreData: [], // 保存所有成绩数据
      category: { //保存分数段
        '90分及以上': 0,
        '80-89分': 0,
        '70-79分': 0,
        '60-69分': 0,
        '60分以下': 0,
      },
      exportLoading: false, // 导出加载状态
      statsLoading: false, // 统计加载状态
      statistics: null, // 统计数据
      chartInstance: null, // 保存图表实例
      // 学生列表弹窗
      studentDialogVisible: false,
      dialogTitle: '',
      segmentStudents: []
    }
  },
  created() {
    this.getScoreInfo()
  },
  mounted() {
    this.$nextTick(() => {
      // 监听主题切换
      this.observeThemeChange()
    })
  },
  methods: {
    getScoreInfo() {
      let examCode = this.$route.query.examCode
      this.name = this.$route.query.source
      this.examDescription = this.$route.query.description || ''
      this.$axios(`/api/scores/${examCode}`).then(res => {
        let data = res.data.data
        if(data.length > 0) {
          // 保存所有成绩数据用于后续筛选
          this.allScoreData = data
          let box = this.$refs['box']
          // 销毁旧实例
          if (this.chartInstance) {
            this.chartInstance.dispose()
          }
          this.chartInstance = this.$echarts.init(box)
          
          // 检查当前主题模式
          const isDarkMode = document.body.classList.contains('dark-theme');
          const textColor = isDarkMode ? '#ffffff' : '#1a1a1a';
          const legendColor = isDarkMode ? '#ffffff' : '#666';
          const tooltipBg = isDarkMode ? 'rgba(45, 45, 45, 0.95)' : 'rgba(255, 255, 255, 0.95)';
          const tooltipTextColor = isDarkMode ? '#ffffff' : '#000000';
          const chartBg = isDarkMode ? '#1a1a1a' : '#ffffff'; // 根据主题设置背景色
          
          data.forEach(element => {
            // 使用总分（客观题etScore + 主观题ptScore）来计算分段
            const totalScore = (element.etScore || 0) + (element.ptScore || 0);
            switch(Math.floor(totalScore / 10)) {
              case 10:
              case 9:
                this.category["90分及以上"]++
                break
              case 8:
                this.category['80-89分']++
                break
              case 7:
                this.category["70-79分"]++
                break
              case 6:
                this.category['60-69分']++
                break
              default:
                this.category['60分以下']++
            }
          });
          let option = {
              backgroundColor: chartBg,
              title : {
                  text: `${this.name}分数段分布`,
                  left: 'center',
                  top: '5%',
                  textStyle: {
                    fontSize: 16,
                    fontWeight: 600,
                    color: textColor
                  }
              },
              tooltip : {
                  trigger: 'item',
                  formatter: "{a}：{b} <br/> {c}人 ({d}%)",
                  backgroundColor: tooltipBg,
                  borderColor: '#667eea',
                  borderWidth: 1,
                  textStyle: {
                    color: tooltipTextColor
                  }
              },
              legend: {
                  orient: 'horizontal',
                  bottom: '5%',
                  left: 'center',
                  data: ['90分及以上','80-89分','70-79分','60-69分','60分以下'],
                  textStyle: {
                    fontSize: 12,
                    color: legendColor
                  }
              },
              series : [
                  {
                      name: '分数段',
                      type: 'pie',
                      radius : '60%',
                      center: ['50%', '50%'],
                      data:[
                          {value:this.category['90分及以上'], name:'90分及以上'},
                          {value:this.category['80-89分'], name:'80-89分'},
                          {value:this.category['70-79分'], name:'70-79分'},
                          {value:this.category['60-69分'], name:'60-69分'},
                          {value:this.category['60分以下'], name:'60分以下'}
                      ],
                      itemStyle: {
                          emphasis: {
                              shadowBlur: 10,
                              shadowOffsetX: 0,
                              shadowColor: 'rgba(0, 0, 0, 0.5)'
                          }
                      },
                      label: {
                          show: true,
                          position: 'outside',
                          formatter: '{b}\n{c}人 ({d}%)',
                          fontSize: 12,
                          color: textColor
                      },
                      labelLine: {
                          show: true,
                          length: 15,
                          length2: 10
                      },
                      emphasis: {
                          label: {
                              fontSize: 16,
                              fontWeight: 'bold',
                              color: textColor
                          },
                          itemStyle: {
                              shadowBlur: 15,
                              shadowOffsetX: 0,
                              shadowColor: 'rgba(0, 0, 0, 0.3)'
                          }
                      }
                  }
              ]
          };
          this.chartInstance.setOption(option)
          
          // 添加饼图点击事件
          this.chartInstance.on('click', (params) => {
            if (params.seriesType === 'pie') {
              this.showSegmentStudents(params.name)
            }
          })
        }else {
          this.isNull = true
        }
      })
    },
    
    // 显示分数段学生列表
    showSegmentStudents(segmentName) {
      this.dialogTitle = `${segmentName} - 学生列表`
      // 根据分数段筛选学生
      this.segmentStudents = this.allScoreData.filter(student => {
        const totalScore = (student.etScore || 0) + (student.ptScore || 0)
        const segment = Math.floor(totalScore / 10)
        switch(segmentName) {
          case '90分及以上': return segment >= 9
          case '80-89分': return segment === 8
          case '70-79分': return segment === 7
          case '60-69分': return segment === 6
          case '60分以下': return segment < 6
          default: return false
        }
      })
      this.studentDialogVisible = true
    },
    
    // 监听主题切换
    observeThemeChange() {
      // 监听body类名变化
      const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
          if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
            console.log('分段查询图表检测到主题切换，重新渲染图表');
            // 主题切换时强制重新渲染图表
            this.$nextTick(() => {
              this.forceUpdateChart()
            })
          }
        })
      })
      
      observer.observe(document.body, {
        attributes: true,
        attributeFilter: ['class']
      })
      
      // 组件销毁时断开监听
      this.$once('hook:beforeDestroy', () => {
        observer.disconnect()
      })
    },
    
    // 更新图表（已废弃，使用 forceUpdateChart 代替）
    updateChart() {
      this.forceUpdateChart()
    },
    
    // 强制重新渲染图表
    forceUpdateChart() {
      if (this.$refs.box && this.category) {
        // 正确销毁旧的 ECharts 实例
        if (this.chartInstance) {
          this.chartInstance.dispose()
          this.chartInstance = null
        }
        
        // 使用 setTimeout 确保 DOM 更新完成
        setTimeout(() => {
          let box = this.$refs['box']
          if (!box) return
          
          // 重新初始化图表
          this.chartInstance = this.$echarts.init(box)
          
          // 检查当前主题模式
          const isDarkMode = document.body.classList.contains('dark-theme');
          const textColor = isDarkMode ? '#ffffff' : '#1a1a1a';
          const legendColor = isDarkMode ? '#ffffff' : '#666';
          const tooltipBg = isDarkMode ? 'rgba(45, 45, 45, 0.95)' : 'rgba(255, 255, 255, 0.95)';
          const tooltipTextColor = isDarkMode ? '#ffffff' : '#000000';
          const chartBg = isDarkMode ? '#1a1a1a' : '#ffffff';
          
          let option = {
              backgroundColor: chartBg,
              title : {
                  text: `${this.name}分数段分布`,
                  left: 'center',
                  top: '5%',
                  textStyle: {
                    fontSize: 16,
                    fontWeight: 600,
                    color: textColor
                  }
              },
              tooltip : {
                  trigger: 'item',
                  formatter: "{a}：{b} <br/> {c}人 ({d}%)",
                  backgroundColor: tooltipBg,
                  borderColor: '#667eea',
                  borderWidth: 1,
                  textStyle: {
                    color: tooltipTextColor
                  }
              },
              legend: {
                  orient: 'horizontal',
                  bottom: '5%',
                  left: 'center',
                  data: ['90分及以上','80-89分','70-79分','60-69分','60分以下'],
                  textStyle: {
                    fontSize: 12,
                    color: legendColor
                  }
              },
              series : [
                  {
                      name: '分数段',
                      type: 'pie',
                      radius : '60%',
                      center: ['50%', '50%'],
                      data:[
                          {value:this.category['90分及以上'], name:'90分及以上'},
                          {value:this.category['80-89分'], name:'80-89分'},
                          {value:this.category['70-79分'], name:'70-79分'},
                          {value:this.category['60-69分'], name:'60-69分'},
                          {value:this.category['60分以下'], name:'60分以下'}
                      ],
                      itemStyle: {
                          emphasis: {
                              shadowBlur: 10,
                              shadowOffsetX: 0,
                              shadowColor: 'rgba(0, 0, 0, 0.5)'
                          }
                      },
                      label: {
                          show: true,
                          position: 'outside',
                          formatter: '{b}\n{c}人 ({d}%)',
                          fontSize: 12,
                          color: textColor
                      },
                      labelLine: {
                          show: true,
                          length: 15,
                          length2: 10
                      },
                      emphasis: {
                          label: {
                              fontSize: 16,
                              fontWeight: 'bold',
                              color: textColor
                          },
                          itemStyle: {
                              shadowBlur: 15,
                              shadowOffsetX: 0,
                              shadowColor: 'rgba(0, 0, 0, 0.3)'
                          }
                      }
                  }
              ]
          };
          this.chartInstance.setOption(option)
          // 强制调整图表大小
          this.chartInstance.resize()
          
          // 重新绑定饼图点击事件
          this.chartInstance.on('click', (params) => {
            if (params.seriesType === 'pie') {
              this.showSegmentStudents(params.name)
            }
          })
        }, 100)
      }
    },
    
    // 导出考试成绩
    exportExamScores() {
      this.exportLoading = true
      let examCode = this.$route.query.examCode
      
      // 创建隐藏的下载链接
      const link = document.createElement('a')
      link.href = `/api/export/score/excel/${examCode}`
      link.download = `考试成绩_${examCode}_${new Date().toISOString().slice(0, 10)}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      this.exportLoading = false
      this.$message.success('成绩导出成功！')
    },
    // 显示统计分析
    showStatistics() {
      this.statsLoading = true
      let examCode = this.$route.query.examCode
      
      this.$axios.get(`/api/statistics/score/${examCode}`).then(res => {
        if (res.data.code === 200) {
          this.statistics = res.data.data
          this.$message.success('统计数据加载成功！')
        } else {
          this.$message.error('获取统计数据失败：' + res.data.message)
        }
      }).catch(error => {
        this.$message.error('获取统计数据失败：' + error.message)
      }).finally(() => {
        this.statsLoading = false
      })
    },
    // 跳转到增强数据分析页面
    goToEnhancedAnalysis() {
      let examCode = this.$route.query.examCode
      let source = this.$route.query.source
      
      this.$router.push({
        path: '/enhancedAnalysis',
        query: {
          examCode: examCode,
          source: source
        }
      })
    }
  },

}
</script>

<style lang="less" scoped>
.part {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  
  .page-header {
    text-align: center;
    margin-bottom: 32px;
    padding: 24px;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    backdrop-filter: blur(10px);
    
    .page-title {
      margin: 0 0 8px 0;
      color: #1a1a1a !important;
      font-size: 28px;
      font-weight: 600;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .page-subtitle {
      margin: 0;
      color: #1a1a1a;
      font-size: 14px;
      opacity: 0.8;
    }
  }
  
  // 深色模式下页面标题区域
  .dark-theme & .page-header {
    background: var(--bg-secondary);
    
    .page-subtitle {
      color: #c0c0c0 !important;
    }
  }
  
  .toolbar {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
    padding: 20px;
    background: rgba(255, 255, 255, 0.9);
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    
    .el-button {
      margin: 0 8px;
      border-radius: 8px;
      font-weight: 500;
      padding: 12px 24px;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
      }
    }
    
    .action-btn {
      min-width: 140px;
      height: 42px;
      padding: 10px 20px;
      font-size: 14px;
      font-weight: 500;
      border-radius: 8px;
      margin: 0;
      transition: all 0.3s ease;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
      }
    }
  }
  
  .stats-cards {
    margin-bottom: 32px;
    
    .stats-card {
      border-radius: 16px;
      border: none;
      background: rgba(255, 255, 255, 0.9);
      backdrop-filter: blur(10px);
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
      }
      
      .stats-item {
        display: flex;
        align-items: center;
        padding: 16px;
        
        .stats-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          font-size: 20px;
          
          &.success {
            background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
          }
          
          &.warning {
            background: linear-gradient(135deg, #E6A23C 0%, #f0c78a 100%);
          }
          
          &.info {
            background: linear-gradient(135deg, #909399 0%, #b1b3b8 100%);
          }
        }
        
        .stats-content {
          flex: 1;
          
          .stats-value {
            font-size: 28px;
            font-weight: 700;
            color: #1a1a1a;
            margin-bottom: 4px;
            line-height: 1;
          }
          
          .stats-label {
            font-size: 14px;
            color: #666;
            font-weight: 500;
          }
        }
      }
    }
  }
  
  .chart-wrapper {
    .chart-card {
      border-radius: 16px;
      border: none;
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(10px);
      box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
      overflow: hidden;
      
      .chart-header {
        text-align: center;
        padding: 16px 20px;
        background: linear-gradient(135deg, #f8f9ff 0%, #e8f0ff 100%);
        border-bottom: 1px solid rgba(0, 0, 0, 0.05);
        
        .chart-title {
          font-size: 18px;
          font-weight: 600;
          color: #1a1a1a;
        }
      }
      
      // 深色模式下图表标题
      .dark-theme & .chart-header {
        background: rgba(59, 130, 246, 0.15);
        
        .chart-title {
          color: #fff !important;
        }
      }
      
      .box {
        width: 100%;
        height: 600px;
        margin: 0;
        padding: 20px;
      }
      
      .no-data {
        text-align: center;
        padding: 80px 20px;
        
        .no-data-icon {
          font-size: 64px;
          color: #E6A23C;
          margin-bottom: 16px;
        }
        
        .no-data-text {
          h3 {
            margin: 0 0 8px 0;
            color: #1a1a1a;
            font-size: 20px;
            font-weight: 600;
          }
          
          p {
            margin: 0;
            color: #666;
            font-size: 14px;
          }
        }
      }
    }
  }
  
  // 响应式设计
  @media (max-width: 768px) {
    padding: 16px;
    
    .page-header {
      padding: 16px;
      
      .page-title {
        font-size: 24px;
      }
    }
    
    .toolbar {
      padding: 16px;
      
      .el-button {
        margin: 4px;
        padding: 10px 16px;
        font-size: 14px;
      }
    }
    
    .stats-cards {
      .stats-card {
        margin-bottom: 16px;
        
        .stats-item {
          padding: 12px;
          
          .stats-icon {
            width: 40px;
            height: 40px;
            font-size: 18px;
            margin-right: 12px;
          }
          
          .stats-content {
            .stats-value {
              font-size: 24px;
            }
          }
        }
      }
    }
    
    .chart-wrapper {
      .chart-card {
        .box {
          height: 400px;
          padding: 16px;
        }
      }
    }
  }
}
</style>

<style>
/* 深色模式专用样式 */
.dark-theme .part .page-header .page-title {
  color: #ffffff !important;
}
.dark-theme .part .page-header .page-subtitle {
  color: #a0aec0 !important;
}
</style>
