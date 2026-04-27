// 所有学生成绩查询页面
<template>
  <div class="student-grade">
    <!-- 标题卡片 - 跟随主题色 -->
    <el-card shadow="never" class="title-card" :body-style="{ padding: 0 }">
      <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
        <span><i class="el-icon-s-data"></i> 学生成绩查询</span>
      </div>
    </el-card>

    <!-- 搜索筛选区 -->
    <div class="search-filter-section">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学生姓名或联系方式..."
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
          <el-button icon="el-icon-download" size="small" @click="exportGrades">导出</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="refreshData" :loading="isRefreshing">刷新</el-button>
        </div>
      </div>

      <transition name="fade-in">
      <el-table :data="pagination.records" style="width: 100%" class="modern-table" :key="tableKey">
        <el-table-column type="index" label="序号" width="60" align="center" :index="getTableIndex"></el-table-column>
        <el-table-column prop="studentName" label="姓名" min-width="100" show-overflow-tooltip></el-table-column>
        <el-table-column prop="institute" label="学院" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="major" label="专业" min-width="140" show-overflow-tooltip></el-table-column>
        <el-table-column prop="grade" label="年级" min-width="80" show-overflow-tooltip></el-table-column>
        <el-table-column prop="clazz" label="班级" min-width="80" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sex" label="性别" min-width="60" show-overflow-tooltip></el-table-column>
        <el-table-column prop="tel" label="联系方式" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" min-width="120" align="center">
          <template slot-scope="scope">
            <el-button @click="checkGrade(scope.row.studentId)" type="primary" size="small" icon="el-icon-view">查看成绩</el-button>
          </template>
        </el-table-column>
      </el-table>
      </transition>
      
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.current"
        :page-sizes="[6, 10]"
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
        size: 6
      },
      condition: {
        name: "",
        tel: "",
        grade: "",
        clazz: "",
        major: "",
        institute: "",
      },
      searchKeyword: '',
      filterCondition: {
        institute: '',
        major: '',
        grade: ''
      },
      instituteOptions: [],
      majorOptions: [],
      gradeOptions: [],
      isRefreshing: false,
      tableKey: 0,
      currentThemeColor: '#6366f1'
    };
  },
  created() {
    this.getStudentGrade();
    this.loadOptions();
    this.updateThemeColor();
  },
  mounted() {
    // 监听主题变化
    this.themeObserver = new MutationObserver(() => {
      this.updateThemeColor();
    });
    this.themeObserver.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['style']
    });
  },
  beforeDestroy() {
    if (this.themeObserver) {
      this.themeObserver.disconnect();
    }
  },
  methods: {
    // 更新主题色
    updateThemeColor() {
      const themeColor = getComputedStyle(document.documentElement).getPropertyValue('--admin-primary').trim();
      if (themeColor) {
        this.currentThemeColor = themeColor;
      }
    },
    
    getStudentGrade() {
      var name = (this.condition.name.trim() == "" ? "@" : this.condition.name);
      var grade = (this.condition.grade.trim() == "" ? "@" : this.condition.grade);
      var tel = (this.condition.tel.trim() == "" ? "@" : this.condition.tel);
      var institute = (this.condition.institute.trim() == "" ? "@" : this.condition.institute);
      var major = (this.condition.major.trim() == "" ? "@" : this.condition.major);
      var clazz = (this.condition.clazz.trim() == "" ? "@" : this.condition.clazz);
      this.$axios(`/api/students/${this.pagination.current}/${this.pagination.size}/${name}/${grade}/${tel}/${institute}/${major}/${clazz}`).then(res => {
        const data = res.data.data;
        this.pagination = {
          ...data,
          current: parseInt(data.current) || 1,
          total: parseInt(data.total) || 0,
          size: parseInt(data.size) || 6
        };
      }).catch(error => {});
    },
    
    // 加载选项数据（从数据库获取）
    loadOptions() {
      this.$axios.get('/api/students/1/1000/@/@/@/@/@/@').then(res => {
        if (res.data.code === 200 && res.data.data && res.data.data.records) {
          this.extractOptionsFromStudents(res.data.data.records);
        } else {
          this.setDefaultOptions();
        }
      }).catch(error => {
        this.setDefaultOptions();
      });
    },
    
    // 从学生数据中提取选项
    extractOptionsFromStudents(students) {
      if (!students || students.length === 0) {
        this.setDefaultOptions();
        return;
      }
      
      const institutes = [...new Set(students.map(s => s.institute).filter(item => item && item.trim()))];
      this.instituteOptions = institutes.sort();
      
      const majors = [...new Set(students.map(s => s.major).filter(item => item && item.trim()))];
      this.majorOptions = majors.sort();
      
      const grades = [...new Set(students.map(s => s.grade).filter(item => item && item.trim()))];
      this.gradeOptions = grades.sort();
    },
    
    // 设置默认选项（从数据库获取失败时清空）
    setDefaultOptions() {
      this.instituteOptions = [];
      this.majorOptions = [];
      this.gradeOptions = [];
    },
    
    // 搜索处理 - 智能判断搜索类型（姓名或联系方式）
    handleSearch() {
      clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => {
        const keyword = this.searchKeyword.trim();
        // 判断是否为电话号码（纯数字且长度大于5）
        const isPhoneNumber = /^\d{5,}$/.test(keyword);
        
        if (isPhoneNumber) {
          // 按联系方式搜索
          this.condition.name = "";
          this.condition.tel = keyword;
        } else {
          // 按姓名搜索
          this.condition.name = keyword;
          this.condition.tel = "";
        }
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
      
      this.pagination.current = 1;
      this.getStudentGrade();
    },
    
    // 重置筛选
    resetFilters() {
      this.searchKeyword = '';
      this.filterCondition = {
        institute: '',
        major: '',
        grade: ''
      };
      this.condition = {
        name: "",
        tel: "",
        grade: "",
        clazz: "",
        major: "",
        institute: "",
      };
      this.pagination.current = 1;
      this.getStudentGrade();
    },
    
    // 计算表格序号
    getTableIndex(index) {
      return (this.pagination.current - 1) * this.pagination.size + index + 1;
    },
    
    // 改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = parseInt(val) || 6;
      this.pagination.current = 1;
      this.getStudentGrade();
    },
    
    // 改变当前页码
    handleCurrentChange(val) {
      this.pagination.current = parseInt(val) || 1;
      this.getStudentGrade();
    },
    
    // 查看成绩
    checkGrade(studentId) {
      this.$router.push({ path: "/grade", query: { studentId: studentId } });
    },
    
    // 导出成绩（一个xlsx文件，多个工作表）
    async exportGrades() {
      try {
        this.$message({
          message: '正在导出成绩，请稍候...',
          type: 'info'
        });
        
        // 获取总页数
        const totalPages = Math.ceil(this.pagination.total / this.pagination.size) || 1;
        const pageSize = this.pagination.size;
        
        // 生成时间戳
        const now = new Date();
        const dateStr = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}`;
        const timeStr = `${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`;
        
        // 构建包含多个工作表的Excel XML
        let excelContent = `<?xml version="1.0" encoding="UTF-8"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">`;
        
        // 遍历每一页，创建对应的工作表
        for (let page = 1; page <= totalPages; page++) {
          // 获取当前页数据
          const apiUrl = `/api/students/${page}/${pageSize}/@/@/@/@/@/@`;
          const res = await this.$axios(apiUrl);
          const pageData = res.data.data.records || [];
          
          // 工作表名称
          const sheetName = page === 1 ? '学生成绩' : `第${page}页`;
          
          excelContent += `
<Worksheet ss:Name="${sheetName}">
<Table>
<Row>
  <Cell><Data ss:Type="String">序号</Data></Cell>
  <Cell><Data ss:Type="String">姓名</Data></Cell>
  <Cell><Data ss:Type="String">学院</Data></Cell>
  <Cell><Data ss:Type="String">专业</Data></Cell>
  <Cell><Data ss:Type="String">年级</Data></Cell>
  <Cell><Data ss:Type="String">班级</Data></Cell>
  <Cell><Data ss:Type="String">性别</Data></Cell>
  <Cell><Data ss:Type="String">联系方式</Data></Cell>
</Row>`;
          
          // 添加数据行
          pageData.forEach((item, index) => {
            excelContent += `
<Row>
  <Cell><Data ss:Type="Number">${(page - 1) * pageSize + index + 1}</Data></Cell>
  <Cell><Data ss:Type="String">${item.studentName || ''}</Data></Cell>
  <Cell><Data ss:Type="String">${item.institute || ''}</Data></Cell>
  <Cell><Data ss:Type="String">${item.major || ''}</Data></Cell>
  <Cell><Data ss:Type="String">${item.grade || ''}</Data></Cell>
  <Cell><Data ss:Type="String">${item.clazz || ''}</Data></Cell>
  <Cell><Data ss:Type="String">${item.sex || ''}</Data></Cell>
  <Cell><Data ss:Type="String">${item.tel || ''}</Data></Cell>
</Row>`;
          });
          
          excelContent += `
</Table>
</Worksheet>`;
        }
        
        excelContent += `
</Workbook>`;
        
        // 创建Blob并下载（单个文件包含多个工作表）
        const blob = new Blob([excelContent], { type: 'application/vnd.ms-excel;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `学生成绩列表_${dateStr}_${timeStr}.xls`;
        link.click();
        URL.revokeObjectURL(link.href);
        
        this.$message({
          message: `成绩导出成功！共${totalPages}个工作表`,
          type: 'success'
        });
      } catch (error) {
        console.error('导出失败:', error);
        this.$message({
          message: '导出失败，请稍后重试',
          type: 'error'
        });
      }
    },
    
    // 刷新数据（带闪入动画）
    async refreshData() {
      this.isRefreshing = true;
      this.tableKey++; // 触发表格重新渲染
      
      try {
        await new Promise(resolve => {
          var name = (this.condition.name.trim() == "" ? "@" : this.condition.name);
          var grade = (this.condition.grade.trim() == "" ? "@" : this.condition.grade);
          var tel = (this.condition.tel.trim() == "" ? "@" : this.condition.tel);
          var institute = (this.condition.institute.trim() == "" ? "@" : this.condition.institute);
          var major = (this.condition.major.trim() == "" ? "@" : this.condition.major);
          var clazz = (this.condition.clazz.trim() == "" ? "@" : this.condition.clazz);
          this.$axios(`/api/students/${this.pagination.current}/${this.pagination.size}/${name}/${grade}/${tel}/${institute}/${major}/${clazz}`).then(res => {
            const data = res.data.data;
            this.pagination = {
              ...data,
              current: parseInt(data.current) || 1,
              total: parseInt(data.total) || 0,
              size: parseInt(data.size) || 6
            };
            resolve();
          }).catch(() => resolve());
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
.student-grade {
  width: 100%;
  padding: 20px;
  background-color: var(--bg-primary);
  
  .title-card {
    margin-bottom: 20px;
    border-radius: 8px;
    overflow: hidden;
    border: none;
    
    .custom-card-header {
      padding: 15px 20px;
      color: #fff !important;
      font-size: 18px;
      font-weight: 600;
      display: flex;
      align-items: center;
      
      span {
        color: #fff !important;
      }
      
      i {
        margin-right: 10px;
        font-size: 20px;
        color: #fff !important;
      }
    }
  }
  
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
    }
    
    .filter-options {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      align-items: center;
      
      .el-select {
        width: 150px;
      }
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

// 响应式设计
@media (max-width: 768px) {
  .student-grade {
    padding: 16px;
    
    .search-filter-section {
      padding: 16px;
      
      .filter-options {
        .el-select {
          width: 120px;
        }
      }
    }
    
    .table-container {
      padding: 16px;
      
      .table-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
        
        .table-actions {
          width: 100%;
          justify-content: flex-end;
        }
      }
    }
  }
}
</style>
