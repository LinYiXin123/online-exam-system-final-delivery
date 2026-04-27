//显示学生成绩
<template>
  <div class="table">
    <p class="top-title"> 我的分数</p>
    <section class="content-el">
      <!-- 筛选条件区域 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="4">
          <el-select v-model="filterParams.examType" placeholder="考试类型" clearable @change="handleFilter" style="width: 100%;">
            <el-option label="全部考试" value=""></el-option>
            <el-option label="考试" value="exam"></el-option>
            <el-option label="练习" value="practice"></el-option>
          </el-select>
        </el-col>
        <el-col :span="5">
          <el-input v-model="filterParams.subject" placeholder="搜索课程名称" clearable @input="handleFuzzySearch" @clear="handleFilter">
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </el-col>
        <el-col :span="10">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            @change="handleDateChange">
          </el-date-picker>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterParams.ptScore" placeholder="是否及格" clearable @change="handleFilter">
            <el-option label="全部" value=""></el-option>
            <el-option label="及格" :value="1"></el-option>
            <el-option label="不及格" :value="0"></el-option>
          </el-select>
        </el-col>
      </el-row>
      
      <!-- 导出功能区域 -->
      <el-row style="margin-bottom: 20px;">
        <el-col :span="24" style="text-align: right; display: flex; justify-content: flex-end; align-items: center; gap: 10px;">
          <el-button 
            size="small"
            icon="el-icon-download" 
            @click="exportScores"
            :loading="exportLoading"
            class="action-btn">
            导出成绩
          </el-button>
          <el-button 
            size="small"
            icon="el-icon-data-analysis" 
            @click="goToTrendAnalysis"
            class="action-btn">
            成绩趋势分析
          </el-button>
        </el-col>
      </el-row>
      <el-table ref="filterTable" :data="score" v-loading="loading" style="border-radius: 8px;">
        <el-table-column
          type="index"
          label="序号"
          width="70"
          align="center">
          <template slot-scope="scope">
            <el-tag size="mini" type="info">{{ (pagination.current - 1) * pagination.size + scope.$index + 1 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="answerDate"
          label="考试日期"
          width="130"
          sortable
          column-key="answerDate"
          :filters="filter"
          :filter-method="filterHandler">
        </el-table-column>
        <el-table-column
          prop="subject"
          label="课程名称"
          width="180"
          filter-placement="bottom-end">
          <template slot-scope="scope">
            <el-tag type="primary" size="small">{{scope.row.subject}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="试卷介绍"
          min-width="180">
          <template slot-scope="scope">
            <span class="description-text">{{ scope.row.description || '暂无介绍' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="考试类型" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.scoreType === 'practice' ? 'info' : 'success'" size="small">
              {{ scope.row.scoreType === 'practice' ? '练习' : '考试' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="etScore" label="客观题得分" width="110" align="center">
          <template slot-scope="scope">
            <span style="font-weight: 600; color: #909399;">{{ scope.row.etScore }}</span>
          </template>
        </el-table-column>
        <el-table-column label="最终得分" width="110" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.gradingCompleted === 0" type="warning" size="small">
              <i class="el-icon-time" style="margin-right: 4px;"></i>待批改
            </el-tag>
            <span v-else :style="{ fontWeight: 600, color: 'var(--primary-color, #409eff)' }">{{ (scope.row.etScore || 0) + (scope.row.subjectiveScore || 0) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="是否及格" width="100" align="center">
          <template slot-scope="scope">
            <template v-if="scope.row.gradingCompleted === 0">
              <el-tag type="info" size="small">待定</el-tag>
            </template>
            <template v-else>
              <el-tag :type="isPass(scope.row) ? 'success' : 'danger'" size="small">
                <i :class="isPass(scope.row) ? 'el-icon-circle-check' : 'el-icon-circle-close'" style="margin-right: 4px;"></i>
                {{ isPass(scope.row) ? "及格" : "不及格" }}
              </el-tag>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template slot-scope="scope">
            <el-button 
              type="primary" 
              size="mini" 
              icon="el-icon-view" 
              @click="viewDetail(scope.row)">
              查看答卷
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-row type="flex" justify="center" align="middle" class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.current"
          :page-sizes="[4,6,8,10]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </el-row>
    </section>
  </div>
</template>

<script>
export default {
  data() {
    return {
      pagination: { //分页后的留言列表
        current: 1, //当前页
        total: null, //记录条数
        size: 10 //每页条数
      },
      loading: false, //加载标识符
      score: [], //学生成绩
      filter: null, //过滤参数
      filterParams: {
        examType: '', // 考试类型
        subject: '', // 课程名称
        startDate: '', // 开始日期
        endDate: '', // 结束日期
        ptScore: '' // 是否及格
      },
      dateRange: [], // 日期范围
      exportLoading: false, // 导出加载状态
      searchTimer: null // 模糊搜索防抖计时器
    }
  },
  created() {
    this.getScore()
    this.loading = true //数据加载则遮罩表格
  },
  methods: {
    getScore() {
      let studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid")
      // 构建查询参数
      let params = {
        examType: this.filterParams.examType,
        subject: this.filterParams.subject,
        startDate: this.filterParams.startDate,
        endDate: this.filterParams.endDate
      }
      if (this.filterParams.ptScore !== '') {
        params.ptScore = this.filterParams.ptScore
      }
      
      this.$axios.get(`/api/score/filter/${this.pagination.current}/${this.pagination.size}/${studentId}`, { params }).then(res => {
        if(res.data.code == 200) {
          this.loading = false //数据加载完成去掉遮罩
          this.score = res.data.data.records
          const data = res.data.data;
          this.pagination = {
            ...data,
            current: parseInt(data.current) || 1,
            total: parseInt(data.total) || 0,
            size: parseInt(data.size) || 6
          };
          let mapVal = this.score.map((element,index) => { //通过map得到 filter:[{text,value}]形式的数组对象
            let newVal = {}
            newVal.text = element.answerDate
            newVal.value = element.answerDate
            return newVal
          })
          let hash = []
          const newArr = mapVal.reduce((item, next) => { //对新对象进行去重操作
            hash[next.text] ? '' : hash[next.text] = true && item.push(next);
            return item
          }, []);
          this.filter = newArr
        }
      })
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = parseInt(val) || 6
      this.pagination.current = 1 // 重置到第一页
      this.getScore()
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = parseInt(val) || 1
      this.getScore()
    },
    formatter(row, column) {
      return row.address;
    },
    filterTag(value, row) {
      return row.tag === value;
    },
    filterHandler(value, row, column) {
      const property = column["property"];
      return row[property] === value;
    },
    viewDetail(row) {
      // 跳转到答卷详情页面，传递考试记录ID
      this.$router.push({
        path: '/examDetail',
        query: {
          examCode: row.examCode,
          studentId: row.studentId
        }
      });
    },
    // 判断是否及格：最终分数 >= 试卷总分 × 60%
    isPass(row) {
      const finalScore = (row.etScore || 0) + (row.subjectiveScore || 0)
      const totalScore = row.totalScore || 100
      const passLine = totalScore * 0.6
      return finalScore >= passLine
    },
    handleFilter() {
      // 触发筛选时重置到第一页
      this.pagination.current = 1
      this.getScore()
    },
    handleDateChange(val) {
      if (val && val.length === 2) {
        this.filterParams.startDate = val[0]
        this.filterParams.endDate = val[1]
      } else {
        this.filterParams.startDate = ''
        this.filterParams.endDate = ''
      }
      this.handleFilter()
    },
    // 模糊搜索功能 - 防抖处理
    handleFuzzySearch() {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      this.searchTimer = setTimeout(() => {
        this.pagination.current = 1
        this.getScore()
      }, 300)
    },
    // 导出成绩功能
    exportScores() {
      this.exportLoading = true
      let studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid")
      
      // 构建导出参数
      let params = new URLSearchParams()
      if (this.filterParams.examType) {
        params.append('examType', this.filterParams.examType)
      }
      if (this.filterParams.subject) {
        params.append('subject', this.filterParams.subject)
      }
      if (this.filterParams.startDate) {
        params.append('startDate', this.filterParams.startDate)
      }
      if (this.filterParams.endDate) {
        params.append('endDate', this.filterParams.endDate)
      }
      
      // 构建完整的导出URL
      let exportUrl = `/api/export/score/student/${studentId}`
      if (params.toString()) {
        exportUrl += '?' + params.toString()
      }
      
      // 创建隐藏的下载链接
      const link = document.createElement('a')
      link.href = exportUrl
      link.download = `我的成绩_${new Date().toISOString().slice(0, 10)}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      this.exportLoading = false
      this.$message.success('成绩导出成功！')
    },
    // 跳转到成绩趋势分析页面
    goToTrendAnalysis() {
      this.$router.push({
        path: '/studentTrend'
      })
    }
  }
};
</script>

<style lang="less" scoped>
.pagination {
  padding-top: 20px;
}
.table {
  padding: 20px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  .top-title {
    margin: 0 0 20px 0;
    padding: 15px 20px;
    font-size: 20px;
    font-weight: 600;
    color: #333;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }
  .content-el {
    background-color: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    overflow-x: auto;
  }
}

.description-text {
  color: #666;
  font-size: 13px;
}

/deep/ .el-table th {
  background-color: var(--primary-color, #f5f7fa) !important;
  color: #fff !important;
  font-weight: 600;
}

/deep/ .el-table th .cell {
  color: #fff !important;
}

/deep/ .el-table__header th {
  background-color: var(--primary-color, #f5f7fa) !important;
}

/deep/ .el-table__header th .cell {
  color: #fff !important;
}

/deep/ .el-table td {
  color: #333 !important;
}

/deep/ .el-table tr:hover > td {
  background-color: #ecf5ff !important;
}

/* 统一按钮图标大小 */
.el-button i {
  font-size: 14px !important;
}

/deep/ .el-table {
  border-radius: 8px;
  overflow: hidden;
}

/* 操作按钮美化样式 */
.action-btn {
  background-color: var(--primary-color, #409EFF) !important;
  border-color: var(--primary-color, #409EFF) !important;
  color: #fff !important;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.3s;
  height: 32px !important;
  padding: 0 15px !important;
  line-height: 30px !important;
}

.action-btn:hover {
  background-color: var(--primary-dark, #1976d2) !important;
  border-color: var(--primary-dark, #1976d2) !important;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.action-btn i {
  color: #fff !important;
}

.action-btn span {
  color: #fff !important;
}
</style>

<style>
/* 非 scoped 全局样式 - 确保按钮样式生效 */
.table .action-btn.el-button,
.table .el-button.action-btn {
  background-color: var(--primary-color, #409EFF) !important;
  border-color: var(--primary-color, #409EFF) !important;
  color: #fff !important;
  height: 32px !important;
  min-height: 32px !important;
  max-height: 32px !important;
  padding: 0 15px !important;
  line-height: 30px !important;
  font-size: 12px !important;
  box-sizing: border-box !important;
}

.table .action-btn.el-button span,
.table .el-button.action-btn span {
  color: #fff !important;
}

.table .action-btn.el-button i,
.table .el-button.action-btn i {
  color: #fff !important;
}
</style>
