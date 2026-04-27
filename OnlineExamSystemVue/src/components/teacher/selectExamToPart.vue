//成绩分段查询页面
<template>
  <div class="exam-grade-query">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">成绩分段查询</h2>
      <p class="page-description">按照不同分数段查询和统计学生考试成绩</p>
    </div>

    <!-- 搜索筛选区 -->
    <div class="search-filter-section">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索考试名称或介绍..."
          prefix-icon="el-icon-search"
          clearable
          @input="handleSearch"
          class="search-input">
        </el-input>
      </div>
      <div class="filter-options">
        <el-select v-model="filterCondition.institute" placeholder="学院" clearable @change="handleFilter">
          <el-option label="全部学院" value=""></el-option>
          <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
        </el-select>
        <el-select v-model="filterCondition.major" placeholder="专业" clearable @change="handleFilter">
          <el-option label="全部专业" value=""></el-option>
          <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item"></el-option>
        </el-select>
        <el-select v-model="filterCondition.grade" placeholder="年级" clearable @change="handleFilter">
          <el-option label="全部年级" value=""></el-option>
          <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item"></el-option>
        </el-select>
        <el-date-picker
          v-model="filterCondition.examDate"
          type="date"
          placeholder="考试日期"
          size="small"
          clearable
          @change="handleFilter"
          value-format="yyyy-MM-dd">
        </el-date-picker>
        <el-button type="default" icon="el-icon-refresh" @click="resetFilters">重置</el-button>
      </div>
    </div>

    <!-- 表格区 -->
    <div class="table-container">
      <div class="table-header">
        <div class="table-info">
          <span>共 {{ pagination.total }} 条记录</span>
        </div>
        <div class="table-actions">
          <el-button icon="el-icon-download" size="small" @click="exportExamGrades">导出</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="refreshData" :loading="isRefreshing">刷新</el-button>
        </div>
      </div>
      
      <transition name="fade-in">
      <el-table :data="pagination.records" style="width: 100%" class="modern-table" :key="tableKey">
        <el-table-column type="index" label="序号" width="60" align="center" :index="getTableIndex"></el-table-column>
        <el-table-column prop="examCode" label="试卷ID" width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="source" label="科目名称" min-width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="description" label="介绍" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="institute" label="所属学院" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="major" label="所属专业" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="grade" label="年级" min-width="100" show-overflow-tooltip></el-table-column>
        <el-table-column prop="examDate" label="考试日期" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="totalTime" label="持续时间" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="totalScore" label="总分" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="type" label="试卷类型" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" min-width="140" align="center">
          <template slot-scope="scope">
            <el-button @click="toPart(scope.row.examCode, scope.row.source, scope.row.description)" type="primary" size="small" icon="el-icon-data-analysis">查看分段</el-button>
          </template>
        </el-table-column>
      </el-table>
      </transition>
      
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[4, 8, 10]"
        :page-size="pagination.size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        class="page">
      </el-pagination>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      pagination: {
        current: 1,
        total: null,
        size: 4
      },
      condition: {
        source: '',
        institute: '',
        major: '',
        grade: '',
        type: '',
        examDate: ''
      },
      searchKeyword: '',
      filterCondition: {
        institute: '',
        major: '',
        grade: '',
        examDate: ''
      },
      instituteOptions: [],
      majorOptions: [],
      gradeOptions: [],
      isRefreshing: false,
      tableKey: 0
    }
  },
  created() {
    this.loadOptions();
    this.getExamInfo();
  },
  methods: {
    // 加载下拉框选项数据
    loadOptions() {
      // 直接使用默认选项，避免使用不存在的API
      this.setDefaultOptions();
      /*
      this.$axios.get('/api/exams/all').then(res => {
        if (res.data.code === 200 && res.data.data) {
          this.extractOptionsFromExams(res.data.data);
        } else {
          this.setDefaultOptions();
        }
      }).catch(() => {
        this.setDefaultOptions();
      });
      */
    },
    // 从考试数据中提取选项
    extractOptionsFromExams(exams) {
      if (!exams || exams.length === 0) {
        this.setDefaultOptions();
        return;
      }
      
      const institutes = [...new Set(exams.map(e => e.institute).filter(item => item && item.trim()))];
      this.instituteOptions = institutes.sort();
      
      const majors = [...new Set(exams.map(e => e.major).filter(item => item && item.trim()))];
      this.majorOptions = majors.sort();
      
      const grades = [...new Set(exams.map(e => e.grade).filter(item => item && item.trim()))];
      this.gradeOptions = grades.sort();
    },
    
    // 设置默认选项（从数据库获取失败时清空）
    setDefaultOptions() {
      this.instituteOptions = [];
      this.majorOptions = [];
      this.gradeOptions = [];
    },
    
    // 搜索处理（带防抖）
    handleSearch() {
      clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => {
        this.condition.source = this.searchKeyword;
        this.applyFilters();
      }, 300);
    },
    
    // 筛选处理
    handleFilter() {
      this.applyFilters();
    },
    
    // 应用筛选
    applyFilters() {
      this.condition.institute = this.filterCondition.institute;
      this.condition.major = this.filterCondition.major;
      this.condition.grade = this.filterCondition.grade;
      this.condition.examDate = this.filterCondition.examDate;
      
      this.pagination.current = 1;
      this.getExamInfo();
    },
    
    // 重置筛选
    resetFilters() {
      this.searchKeyword = '';
      this.filterCondition = {
        institute: '',
        major: '',
        grade: '',
        examDate: ''
      };
      this.condition = {
        source: '',
        institute: '',
        major: '',
        grade: '',
        type: '',
        examDate: ''
      };
      this.pagination.current = 1;
      this.getExamInfo();
    },
    
    // 计算表格序号
    getTableIndex(index) {
      return (this.pagination.current - 1) * this.pagination.size + index + 1;
    },
    getExamInfo() { //分页查询所有试卷信息（支持前端筛选）
      // 使用正确的API路径
      const apiUrl = `/api/exams/${this.pagination.current}/${this.pagination.size}`;
      
      this.$axios(apiUrl).then(res => {
        const data = res.data.data;
        let records = data.records || [];
        
        // 前端筛选（考试名称、学院、专业、年级、日期）
        if (this.condition.source) {
          const keyword = this.condition.source.toLowerCase();
          records = records.filter(item => 
            (item.source && item.source.toLowerCase().includes(keyword)) ||
            (item.description && item.description.toLowerCase().includes(keyword))
          );
        }
        if (this.condition.institute) {
          records = records.filter(item => item.institute && item.institute.includes(this.condition.institute));
        }
        if (this.condition.major) {
          records = records.filter(item => item.major && item.major.includes(this.condition.major));
        }
        if (this.condition.grade) {
          records = records.filter(item => item.grade && item.grade.includes(this.condition.grade));
        }
        if (this.condition.examDate) {
          records = records.filter(item => item.examDate && item.examDate.includes(this.condition.examDate));
        }
        
        this.pagination = {
          records: records,
          current: parseInt(data.current) || 1,
          total: records.length,
          size: parseInt(data.size) || 4
        };
        
        // 从返回数据中提取选项
        if (data.records && data.records.length > 0) {
          this.extractOptionsFromExams(data.records);
        }
      }).catch(error => {
        console.error('获取考试列表失败:', error);
      });
    },
    
    // 改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = parseInt(val) || 4;
      this.pagination.current = 1;
      this.getExamInfo();
    },
    
    // 改变当前页码
    handleCurrentChange(val) {
      this.pagination.current = parseInt(val) || 1;
      this.getExamInfo();
    },
    
    // 跳转到分段页面
    toPart(examCode, source, description) {
      this.$router.push({ path: '/scorePart', query: { examCode: examCode, source: source, description: description || '' } });
    },
    
    // 导出考试成绩
    exportExamGrades() {
      const loading = this.$loading({
        lock: true,
        text: '正在导出数据...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      
      try {
        const exams = this.pagination.records || [];
        
        if (exams.length === 0) {
          this.$message({
            message: '没有数据可导出',
            type: 'warning'
          });
          loading.close();
          return;
        }
        
        // 准备导出数据
        const exportData = exams.map(exam => ({
          '试卷ID': exam.examCode,
          '科目名称': exam.source,
          '介绍': exam.description,
          '所属学院': exam.institute,
          '所属专业': exam.major,
          '年级': exam.grade,
          '考试日期': exam.examDate,
          '持续时间': exam.totalTime,
          '总分': exam.totalScore,
          '试卷类型': exam.type
        }));
        
        this.downloadCSV(exportData, '成绩分段查询数据导出.csv');
        
        this.$message({
          message: `成功导出 ${exams.length} 条考试数据`,
          type: 'success'
        });
        
      } catch (error) {
        console.error('导出失败:', error);
        this.$message({
          message: '导出失败，请重试',
          type: 'error'
        });
      } finally {
        loading.close();
      }
    },
    
    // 下载CSV文件
    downloadCSV(data, filename) {
      if (!data || data.length === 0) return;
      
      const headers = Object.keys(data[0]);
      let csvContent = '\uFEFF'; // BOM头，解决中文乱码
      csvContent += headers.join(',') + '\n';
      
      data.forEach(row => {
        const values = headers.map(header => {
          let value = row[header] || '';
          if (value.toString().includes(',') || value.toString().includes('\n')) {
            value = `"${value.toString().replace(/"/g, '""')}"`;
          }
          return value;
        });
        csvContent += values.join(',') + '\n';
      });
      
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      if (link.download !== undefined) {
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', filename);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
      }
    },
    
    // 刷新数据（带动画效果）
    async refreshData() {
      this.isRefreshing = true;
      this.tableKey++;
      
      try {
        await new Promise(resolve => {
          this.getExamInfo();
          setTimeout(resolve, 300);
        });
        
        this.$message({
          message: '数据刷新成功',
          type: 'success'
        });
      } finally {
        this.isRefreshing = false;
      }
    }
  }
};
</script>
<style lang="less" scoped>
.exam-grade-query {
  width: 100%;
  padding: 20px;
  background-color: var(--bg-primary);
  
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 8px 0;
    }
    
    .page-description {
      font-size: 14px;
      color: var(--text-secondary);
      margin: 0;
    }
  }
  
  .search-filter-section {
    background: var(--card-bg);
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border: 1px solid var(--border-color);
    
    .search-box {
      margin-bottom: 16px;
      
      .search-input {
        max-width: 400px;
      }
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
    }
  }
  
  .table-container {
    background: var(--card-bg);
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    border: 1px solid var(--border-color);
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .table-info {
        color: var(--text-secondary);
        font-size: 14px;
      }
      
      .table-actions {
        display: flex;
        gap: 8px;
        
        .el-button {
          font-size: 14px;
        }
      }
    }
    
    .modern-table {
      border-radius: 8px;
      overflow: hidden;
      border: 1px solid var(--border-color);
    }
    
    .page {
      margin-top: 20px;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
}

// 闪入动画
.fade-in-enter-active {
  animation: fadeIn 0.4s ease-out;
}

.fade-in-leave-active {
  animation: fadeOut 0.2s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeOut {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
</style>
