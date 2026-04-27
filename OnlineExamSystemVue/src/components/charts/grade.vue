<!--
 * @Description: 
 * @Author: 
 * @Date: 2024-03-08 20:38:49
-->
// 成绩统计页面
<template>
  <div id="grade">
    <!-- 图表类型切换按钮 -->
    <div class="chart-toolbar">
      <el-radio-group v-model="chartType" size="small" @change="changeChartType">
        <el-radio-button label="line">折线图</el-radio-button>
        <el-radio-button label="bar">柱状图</el-radio-button>
        <el-radio-button label="scatter">散点图</el-radio-button>
      </el-radio-group>
    </div>
    <div ref="box" class="box"></div>
    <div class="notFound" v-if="isNull">
      <i class="iconfont icon-LC_icon_tips_fill"></i
      ><span>该考生未参加考试</span>
    </div>
  </div>
</template>

<script>
export default {
  name: "grade",
  data() {
    return {
      isNull: false, //原始数据
      tableDataX: [], //x轴数据 保存次数
      tableDataY: [], //y轴数据 保存分数
      scoreCharts: null, // 保存图表实例
      chartType: 'line', // 图表类型
    };
  },
  mounted() {
    this.score();
    // 添加窗口大小变化监听
    window.addEventListener('resize', this.handleResize);
    // 监听主题切换
    this.observeThemeChange();
  },
  beforeDestroy() {
    // 移除监听器
    window.removeEventListener('resize', this.handleResize);
    if (this.scoreCharts) {
      this.scoreCharts.dispose();
    }
  },
  methods: {
    score() {
      let studentId = this.$route.query.studentId;
      this.$axios(`/api/score/${studentId}`).then((res) => {
        //根据学生Id查询成绩
        if (res.data.code == 200) {
          let rootData = res.data.data;
          rootData.forEach((element, index) => {

            // 科目名称+介绍转列显示
            var insertIntervalString = (
              originStr,
              disNum = 10,
              insertStr = "\n"
            ) =>
              originStr.replace(
                new RegExp("(.{" + disNum + "})", "g"),
                "$1" + insertStr
              );
            // 组合科目名称和介绍（如：计算机网络（2023秋季期末））
            let subjectWithDesc = element.subject;
            if (element.description) {
              subjectWithDesc = element.subject + '\n(' + element.description + ')';
            }
            // 每隔几个字符插入换行
            var subject = insertIntervalString(subjectWithDesc, 5, "\n");

            this.tableDataX.push(subject);
            // 使用总分（客观题+主观题）
            this.tableDataY.push((element.etScore || 0) + (element.ptScore || 0));
          });
          let boxDom = this.$refs["box"];
          this.scoreCharts = this.$echarts.init(boxDom);
          
          // 检查当前主题模式
          const isDarkMode = document.body.classList.contains('dark-theme');
          const textColor = isDarkMode ? '#ffffff' : '#333333';
          const gridColor = isDarkMode ? '#6b7280' : '#999999';
          
          let option = {
            backgroundColor: 'transparent',
            textStyle: {
              color: textColor
            },
            xAxis: {
              type: "category",
              name: '科目',
              nameTextStyle: { color: textColor, fontSize: 14 },
              data: this.tableDataX,
              axisLabel: {
                color: textColor,
                fontSize: 12
              },
              axisLine: {
                lineStyle: {
                  color: gridColor
                }
              },
              axisTick: {
                lineStyle: {
                  color: gridColor
                }
              }
            },
            yAxis: {
              type: "value",
              name: '分数',
              nameTextStyle: { color: textColor, fontSize: 14 },
              axisLabel: {
                color: textColor,
                fontSize: 12
              },
              axisLine: {
                lineStyle: {
                  color: gridColor
                }
              },
              axisTick: {
                lineStyle: {
                  color: gridColor
                }
              },
              splitLine: {
                lineStyle: {
                  color: gridColor,
                  type: 'dashed'
                }
              }
            },
            grid: {
              left: '5%',
              right: '5%',
              bottom: '15%',
              containLabel: true
            },
            series: [
              {
                data: this.tableDataY,
                type: this.chartType,
                itemStyle: { 
                  normal: { 
                    label: { 
                      show: true,
                      color: textColor,
                      position: 'top'
                    },
                    color: isDarkMode ? '#3b82f6' : '#409eff'
                  } 
                },
                lineStyle: {
                  color: isDarkMode ? '#3b82f6' : '#409eff'
                },
                symbolSize: this.chartType === 'scatter' ? 15 : 8
              },
            ],
          };
          this.scoreCharts.setOption(option);
          this.scoreCharts.on("mouseover", (params) => {
            console.log(params.value);
          });
        } else {
          this.isNull = true;
        }
      });
    },
    // 响应式调整方法
    handleResize() {
      if (this.scoreCharts) {
        setTimeout(() => {
          this.scoreCharts.resize();
        }, 100);
      }
    },
    
    // 监听主题切换
    observeThemeChange() {
      const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
          if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
            console.log('成绩图表检测到主题切换，重新渲染');
            setTimeout(() => {
              this.forceUpdateChart();
            }, 100);
          }
        });
      });
      
      observer.observe(document.body, {
        attributes: true,
        attributeFilter: ['class']
      });
      
      this.$once('hook:beforeDestroy', () => {
        observer.disconnect();
      });
    },
    
    // 切换图表类型
    changeChartType() {
      this.forceUpdateChart();
    },

    // 强制重新渲染图表
    forceUpdateChart() {
      if (!this.$refs.box || this.tableDataX.length === 0) return;
      
      // 销毁旧实例
      if (this.scoreCharts) {
        this.scoreCharts.dispose();
        this.scoreCharts = null;
      }
      
      setTimeout(() => {
        let boxDom = this.$refs['box'];
        if (!boxDom) return;
        
        this.scoreCharts = this.$echarts.init(boxDom);
        
        const isDarkMode = document.body.classList.contains('dark-theme');
        const textColor = isDarkMode ? '#ffffff' : '#333333';
        const gridColor = isDarkMode ? '#6b7280' : '#999999';
        
        let option = {
          backgroundColor: 'transparent',
          textStyle: { color: textColor },
          xAxis: {
            type: 'category',
            name: '科目',
            nameTextStyle: { color: textColor, fontSize: 14 },
            data: this.tableDataX,
            axisLabel: { color: textColor, fontSize: 12 },
            axisLine: { lineStyle: { color: gridColor } },
            axisTick: { lineStyle: { color: gridColor } }
          },
          yAxis: {
            type: 'value',
            name: '分数',
            nameTextStyle: { color: textColor, fontSize: 14 },
            axisLabel: { color: textColor, fontSize: 12 },
            axisLine: { lineStyle: { color: gridColor } },
            axisTick: { lineStyle: { color: gridColor } },
            splitLine: { lineStyle: { color: gridColor, type: 'dashed' } }
          },
          grid: {
            left: '5%',
            right: '5%',
            bottom: '15%',
            containLabel: true
          },
          series: [{
            data: this.tableDataY,
            type: this.chartType,
            itemStyle: { 
              normal: { 
                label: { show: true, color: textColor, position: 'top' },
                color: isDarkMode ? '#3b82f6' : '#409eff'
              } 
            },
            lineStyle: { color: isDarkMode ? '#3b82f6' : '#409eff' },
            symbolSize: this.chartType === 'scatter' ? 15 : 8
          }]
        };
        this.scoreCharts.setOption(option);
      }, 50);
    },
  },
};
</script>

<style lang="less" scoped>
#grade {
  position: relative;
  padding: 20px;
  background-color: var(--bg-color);
  color: var(--text-primary);
  min-height: calc(100vh - 200px);
  width: 100%;
  
  .chart-toolbar {
    margin-bottom: 15px;
    text-align: center;
  }
  
  .box {
    width: 100%;
    height: 70vh;
    max-height: 600px;
    min-height: 400px;
    background-color: var(--card-bg);
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 var(--shadow-color);
    padding: 20px;
    box-sizing: border-box;
  }
  
  .notFound {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    text-align: center;
    color: var(--text-secondary);
    font-size: 18px;
    
    i {
      font-size: 48px;
      color: var(--text-muted);
      display: block;
      margin-bottom: 10px;
    }
    
    span {
      color: var(--text-primary);
    }
  }
}
</style>
