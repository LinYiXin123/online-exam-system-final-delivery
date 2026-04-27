<!--
 * @Description: 
 * @Author: 
 * @Date: 2024-03-08 20:38:49
-->
//获取试卷并跳转到添加题库
<template>
  <div class="exam">
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-input 
        v-model="searchForm.source" 
        placeholder="搜索科目名称" 
        clearable
        class="search-input"
        @keyup.enter.native="handleSearch"
      >
        <i slot="prefix" class="el-input__icon el-icon-search"></i>
      </el-input>
      <el-select v-model="searchForm.institute" placeholder="全部学院" clearable class="filter-select">
        <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
      </el-select>
      <el-select v-model="searchForm.major" placeholder="全部专业" clearable class="filter-select">
        <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item"></el-option>
      </el-select>
      <el-select v-model="searchForm.grade" placeholder="全部年级" clearable class="filter-select">
        <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item"></el-option>
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
      <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
    </div>
    
    <!-- 记录数显示 -->
    <div class="record-info">
      <span>共 {{ pagination.total || 0 }} 条记录</span>
    </div>
    
    <el-table :data="pagination.records" border>
      <el-table-column type="index" label="序号" width="60" align="center" fixed="left" :index="getTableIndex"></el-table-column>
      <el-table-column prop="source" label="科目名称" width="180"></el-table-column>
      <el-table-column prop="description" label="介绍" width="200"></el-table-column>
      <el-table-column prop="institute" label="所属学院" width="120"></el-table-column>
      <el-table-column prop="major" label="所属专业" width="200"></el-table-column>
      <el-table-column prop="grade" label="年级" width="100"></el-table-column>
      <el-table-column prop="examDate" label="考试日期" width="120"></el-table-column>
      <el-table-column prop="totalTime" label="持续时间" width="120"></el-table-column>
      <!-- <el-table-column prop="totalScore" label="总分" width="120"></el-table-column> -->
      <el-table-column prop="type" label="试卷类型" width="120"></el-table-column>
      <el-table-column prop="tips" label="考生提示" width="400"></el-table-column>
      <el-table-column fixed="right" label="操作" width="180">
        <template slot-scope="scope">
          <el-button @click="add(scope.row.paperId,scope.row.source,scope.row.description)" type="primary"> 录入题目</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.current"
      :page-sizes="[4, 8, 10]"
      :page-size="pagination.size"
      layout="total, sizes, prev, pager, next, jumper"
      :total="pagination.total" class="page">
    </el-pagination>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {}, //保存点击以后当前试卷的信息
      pagination: { //分页后的考试信息
        current: 1, //当前页
        total: null, //记录条数
        size: 4 //每页条数
      },
      searchForm: {
        source: '',
        institute: '',
        major: '',
        grade: ''
      },
      instituteOptions: [],
      majorOptions: [],
      gradeOptions: []
    }
  },
  created() {
    this.getExamInfo();
    this.loadOptions();
  },
  methods: {
    getExamInfo() { //分页查询所有试卷信息
      this.$axios(`/api/exams/${this.pagination.current}/${this.pagination.size}`).then(res => {
        const data = res.data.data;
        this.pagination = {
          ...data,
          current: parseInt(data.current) || 1,
          total: parseInt(data.total) || 0,
          size: parseInt(data.size) || 4
        };
      }).catch(error => {
      })
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = parseInt(val) || 4
      this.pagination.current = 1 // 重置到第一页
      this.getExamInfo()
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = parseInt(val) || 1
      this.getExamInfo()
    },
    add(paperId,source,description) { //增加题库
      this.$router.push({path:'/addAnswerChildren',query: {paperId: paperId, subject: source, description: description || ''}})
    },
    // 计算表格序号（支持分页）
    getTableIndex(index) {
      return (this.pagination.current - 1) * this.pagination.size + index + 1;
    },
    // 加载筛选选项
    loadOptions() {
      this.$axios.get('/api/examOptions').then(res => {
        if (res.data.code === 200 && res.data.data) {
          this.instituteOptions = res.data.data.institutes || [];
          this.majorOptions = res.data.data.majors || [];
          this.gradeOptions = res.data.data.grades || [];
        }
      }).catch(() => {});
    },
    // 搜索
    handleSearch() {
      this.pagination.current = 1;
      this.getExamInfoWithSearch();
    },
    // 重置搜索
    handleReset() {
      this.searchForm = {
        source: '',
        institute: '',
        major: '',
        grade: ''
      };
      this.pagination.current = 1;
      this.getExamInfo();
    },
    // 带搜索条件的查询
    getExamInfoWithSearch() {
      const source = this.searchForm.source || '@';
      const institute = this.searchForm.institute || '@';
      const major = this.searchForm.major || '@';
      const grade = this.searchForm.grade || '@';
      
      this.$axios.get(`/api/exams/${this.pagination.current}/${this.pagination.size}/${source}/${institute}/${major}/${grade}`).then(res => {
        const data = res.data.data;
        this.pagination = {
          ...data,
          current: parseInt(data.current) || 1,
          total: parseInt(data.total) || 0,
          size: parseInt(data.size) || 4
        };
      }).catch(() => {});
    }
  },
};
</script>
<style lang="less" scoped>
.exam {
  padding: 0px 40px;
  
  // 搜索区域样式
  .search-area {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px 20px;
    background: var(--card-bg, #fff);
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    
    .search-input {
      width: 180px;
      
      /deep/ .el-input__inner {
        border-radius: 6px;
        background: var(--input-bg, #f5f7fa);
        border-color: var(--border-color, #dcdfe6);
        color: var(--text-primary, #303133);
        
        &::placeholder {
          color: var(--text-secondary, #909399);
        }
        
        &:focus {
          border-color: var(--primary-color, #409eff);
        }
      }
    }
    
    .filter-select {
      width: 140px;
      
      /deep/ .el-input__inner {
        border-radius: 6px;
        background: var(--input-bg, #f5f7fa);
        border-color: var(--border-color, #dcdfe6);
        color: var(--text-primary, #303133);
      }
    }
    
    .el-button {
      border-radius: 6px;
    }
  }
  
  // 记录数显示样式
  .record-info {
    margin-bottom: 12px;
    padding: 8px 0;
    color: var(--text-secondary, #606266);
    font-size: 14px;
  }
  
  .page {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .edit {
    margin-left: 20px;
  }
}

// 深色模式样式
.dark-theme {
  .exam {
    .search-area {
      background: var(--card-bg, #1e1e2d);
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
      
      .search-input,
      .filter-select {
        /deep/ .el-input__inner {
          background: var(--input-bg, #2d2d3a);
          border-color: var(--border-color, #3d3d4d);
          color: var(--text-primary, #e0e0e0);
          
          &::placeholder {
            color: var(--text-secondary, #888);
          }
        }
      }
    }
    
    .record-info {
      color: var(--text-secondary, #aaa);
    }
  }
}
</style>
