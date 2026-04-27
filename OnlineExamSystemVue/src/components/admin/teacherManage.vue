// 教师管理页面
<template>
  <div class="teacher">
    <!-- 页面标题和操作区 - 与学生管理风格一致 -->
    <el-card shadow="never" class="title-card" :body-style="{ padding: 0 }">
      <div class="custom-card-header">
        <span><i class="el-icon-s-custom"></i> 教师管理</span>
        <el-button icon="el-icon-plus" @click="goToAddTeacher" size="small" class="header-btn">添加教师</el-button>
      </div>
    </el-card>

    <!-- 搜索和筛选区 -->
    <div class="search-filter-bar">
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索教师姓名、工号或联系方式"
          prefix-icon="el-icon-search"
          clearable
          @input="handleSearch"
          class="search-input">
        </el-input>
      </div>
      <div class="filter-section">
        <el-select v-model="filterCondition.institute" placeholder="学院" clearable @change="handleFilter">
          <el-option label="全部学院" value=""></el-option>
          <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
        </el-select>
        <el-select v-model="filterCondition.type" placeholder="职称" clearable @change="handleFilter">
          <el-option label="全部职称" value=""></el-option>
          <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item"></el-option>
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
          <el-button icon="el-icon-refresh" size="small" @click="refreshData" :loading="isRefreshing">刷新</el-button>
        </div>
      </div>
      
      <el-table :data="pagination.records" border style="width: 100%" class="modern-table">
        <el-table-column type="index" label="序号" width="60" align="center" :index="getTableIndex"></el-table-column>
        <el-table-column prop="teacherId" label="ID" min-width="100" show-overflow-tooltip></el-table-column>
        <el-table-column prop="teacherName" label="姓名" min-width="80" show-overflow-tooltip></el-table-column>
        <el-table-column prop="institute" label="学院" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="sex" label="性别" min-width="50" show-overflow-tooltip></el-table-column>
        <el-table-column prop="tel" label="联系方式" min-width="120" show-overflow-tooltip></el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip></el-table-column>
        <el-table-column prop="type" label="职称" min-width="80" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" min-width="180" align="center">
          <template slot-scope="scope">
            <div class="action-btns">
              <el-button @click="checkGrade(scope.row.teacherId)" type="primary" size="mini">编辑</el-button>
              <el-button @click="deleteById(scope.row.teacherId)" type="danger" size="mini">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
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
    <!-- 编辑对话框-->
    <el-dialog title="编辑教师信息" :visible.sync="dialogVisible" width="30%">
      <section class="update">
        <el-form ref="form" :model="form" label-width="80px">
          <el-form-item label="姓名">
            <el-input v-model="form.teacherName"></el-input>
          </el-form-item>
          <el-form-item label="学院">
            <el-input v-model="form.institute"></el-input>
          </el-form-item>
          <el-form-item label="性别">
            <el-input v-model="form.sex"></el-input>
          </el-form-item>
          <el-form-item label="电话号码">
            <el-input v-model="form.tel"></el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.pwd" type="password" placeholder="留空则不修改密码" show-password></el-input>
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="form.cardId"></el-input>
          </el-form-item>
          <el-form-item label="职称">
            <el-input v-model="form.type"></el-input>
          </el-form-item>
        </el-form>
      </section>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false" style="font-size: 20px;"> 取 消</el-button>
        <el-button type="primary" @click="submit()"> 确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      pagination: {
        current: 1,
        total: null,
        size: 6,
      },
      dialogVisible: false,
      form: {},
      // 搜索和筛选
      searchKeyword: '',
      filterCondition: {
        institute: '',
        type: ''
      },
      instituteOptions: [],
      typeOptions: [],
      isRefreshing: false,
      allTeachers: [] // 保存所有教师数据用于前端筛选
    };
  },
  created() {
    // 先加载所有数据，再显示
    this.initData();
  },
  methods: {
    // 初始化数据 - 确保数据加载顺序正确
    async initData() {
      try {
        // 先加载所有教师数据
        const res = await this.$axios.get('/api/teachers/1/1000');
        if (res.data.code === 200 && res.data.data && res.data.data.records) {
          this.allTeachers = res.data.data.records;
          // 提取学院选项
          const institutes = [...new Set(this.allTeachers.map(t => t.institute).filter(Boolean))];
          this.instituteOptions = institutes.sort();
          // 提取职称选项
          const types = [...new Set(this.allTeachers.map(t => t.type).filter(Boolean))];
          this.typeOptions = types.sort();
          // 更新分页信息并应用筛选
          this.pagination.total = this.allTeachers.length;
          this.applyClientFilter();
        }
      } catch (error) {
        console.error('加载数据失败:', error);
        // 回退到分页加载
        this.getTeacherInfo();
      }
    },
    
    getTeacherInfo() {
      this.$axios(`/api/teachers/${this.pagination.current}/${this.pagination.size}`).then(res => {
        const data = res.data.data;
        this.pagination = {
          ...data,
          current: parseInt(data.current) || 1,
          total: parseInt(data.total) || 0,
          size: parseInt(data.size) || 6
        };
      }).catch(error => { });
    },
    
    // 加载下拉框选项（刷新时使用）
    async loadSelectOptions() {
      try {
        const res = await this.$axios.get('/api/teachers/1/1000');
        if (res.data.code === 200 && res.data.data && res.data.data.records) {
          this.allTeachers = res.data.data.records;
          // 提取学院选项
          const institutes = [...new Set(this.allTeachers.map(t => t.institute).filter(Boolean))];
          this.instituteOptions = institutes.sort();
          // 提取职称选项
          const types = [...new Set(this.allTeachers.map(t => t.type).filter(Boolean))];
          this.typeOptions = types.sort();
        }
      } catch (error) {
        console.error('加载选项失败:', error);
      }
    },
    
    // 搜索处理
    handleSearch() {
      clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => {
        this.pagination.current = 1;
        this.applyClientFilter();
      }, 300);
    },
    
    // 筛选处理
    handleFilter() {
      this.pagination.current = 1;
      this.applyClientFilter();
    },
    
    // 重置筛选
    resetFilters() {
      this.searchKeyword = '';
      this.filterCondition = { institute: '', type: '' };
      this.pagination.current = 1;
      this.getTeacherInfo();
    },
    
    // 前端筛选逻辑
    applyClientFilter() {
      let filtered = [...this.allTeachers];
      
      // 搜索关键词筛选
      if (this.searchKeyword && this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.trim().toLowerCase();
        filtered = filtered.filter(t => 
          (t.teacherName && t.teacherName.toLowerCase().includes(keyword)) ||
          (t.teacherId && t.teacherId.toString().includes(keyword)) ||
          (t.tel && t.tel.includes(keyword))
        );
      }
      
      // 学院筛选
      if (this.filterCondition.institute) {
        filtered = filtered.filter(t => t.institute === this.filterCondition.institute);
      }
      
      // 职称筛选
      if (this.filterCondition.type) {
        filtered = filtered.filter(t => t.type === this.filterCondition.type);
      }
      
      // 更新分页数据
      this.pagination.total = filtered.length;
      const start = (this.pagination.current - 1) * this.pagination.size;
      const end = start + this.pagination.size;
      this.pagination.records = filtered.slice(start, end);
    },
    
    // 刷新数据
    async refreshData() {
      this.isRefreshing = true;
      try {
        await this.loadSelectOptions();
        this.getTeacherInfo();
        this.$message.success('数据刷新成功');
      } finally {
        this.isRefreshing = false;
      }
    },
    
    // 跳转添加教师
    goToAddTeacher() {
      // 根据当前路由判断是管理员端还是教师端
      const isAdmin = this.$route.path.startsWith('/admin');
      this.$router.push(isAdmin ? '/admin/addTeacher' : '/addTeacher');
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = parseInt(val) || 6;
      this.pagination.current = 1; // 重置到第一页
      this.getTeacherInfo();
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = parseInt(val) || 1;
      this.getTeacherInfo();
    },
    checkGrade(teacherId) { //修改教师信息
      this.dialogVisible = true
      this.$axios(`/api/teacher/${teacherId}`).then(res => {
        this.form = res.data.data
      })
    },
    deleteById(teacherId) { //删除当前教师
      this.$confirm("确定删除当前教师吗？删除后无法恢复", "Warning", {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      }).then(() => { //确认删除
        this.$axios({
          url: `/api/teacher/${teacherId}`,
          method: 'delete',
        }).then(res => {
          this.getTeacherInfo()
        })
      }).catch(() => {

      })
    },
    submit() { //提交更改
      // 校验
      if (this.form.teacherName == "") {
        this.$message({
          message: '请输入教师姓名',
          type: 'error'
        })
        return
      }
      if (this.form.institute == "") {
        this.$message({
          message: '请输入学院',
          type: 'error'
        })
        return
      }
      if (this.form.sex == "") {
        this.$message({
          message: '请输入性别',
          type: 'error'
        })
        return
      }
      if (this.form.tel == "") {
        this.$message({
          message: '请输入电话号码',
          type: 'error'
        })
        return
      }
      if(this.form.tel.length > 11) {
        this.$message({
          message: '请输入正确的电话号码',
          type: 'error'
        })
        return
      }
      if (this.form.pwd == "") {
        this.$message({
          message: '请输入密码',
          type: 'error'
        })
        return
      }
      if (this.form.cardId == "") {
        this.$message({
          message: '请输入身份证号码',
          type: 'error'
        })
        return
      }
      if(this.form.cardId.length > 18) {
        this.$message({
          message: '请输入正确的身份证号码',
          type: 'error'
        })
        return
      }
      if (this.form.type == "") {
        this.$message({
          message: '请输入职称',
          type: 'error'
        })
        return
      }
      this.dialogVisible = false
      this.$axios({
        url: '/api/teacher',
        method: 'put',
        data: {
          ...this.form
        }
      }).then(res => {
        console.log(res)
        if (res.data.code == 200) {
          this.$message({
            message: '更新成功',
            type: 'success'
          });
        }
        this.getTeacherInfo();
      });
    },
    
    // 计算表格序号（支持分页）
    getTableIndex(index) {
      return (this.pagination.current - 1) * this.pagination.size + index + 1;
    }
  }
};
</script>
<style lang="less" scoped>
.teacher {
  width: 100%;
  padding: 20px;
  background-color: var(--bg-primary);
  
  // 标题卡片 - 与仪表盘风格统一
  .title-card {
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 20px;
  }
  
  // 通用卡片头部样式
  .custom-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background-color: var(--admin-primary, var(--primary-color, #6366f1));
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
    
    /deep/ .header-btn.el-button {
      background-color: #fff !important;
      border-color: #fff !important;
      color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
      border-radius: 6px;
      font-weight: 500;
      
      i {
        color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
      }
      
      &:hover, &:focus {
        background-color: #fff !important;
        border-color: #fff !important;
        opacity: 0.9;
      }
    }
  }
  
  .search-filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    background-color: var(--card-bg);
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
    
    .search-section {
      flex: 1;
      max-width: 400px;
    }
    
    .filter-section {
      display: flex;
      gap: 12px;
      align-items: center;
      
      .el-select {
        width: 120px;
      }
    }
  }
  
  .table-container {
    background-color: var(--card-bg);
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      border-bottom: 1px solid var(--border-color);
      
      .table-info {
        font-size: 14px;
        color: var(--text-secondary);
      }
      
      .table-actions {
        display: flex;
        gap: 10px;
      }
    }
  }
  
  .page {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  // 操作按钮样式 - 统一风格：主题色背景+白色图标文字
  .action-btns {
    display: flex !important;
    justify-content: center !important;
    gap: 8px !important;
    
    .el-button--primary,
    .el-button--danger {
      background-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
      border-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
      color: #fff !important;
      
      i, span {
        color: #fff !important;
      }
    }
  }
}

// 表格表头使用主题色
/deep/ .el-table__header-wrapper th {
  background-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
  color: #fff !important;
  
  .cell {
    color: #fff !important;
  }
}

// 分页器使用主题色
/deep/ .el-pagination {
  .el-pager li.active {
    background-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
    color: #fff !important;
  }
  
  .el-pager li:hover {
    color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
  }
}

// 刷新按钮样式
/deep/ .table-actions .el-button {
  background-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
  border-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
  color: #fff !important;
  
  i, span {
    color: #fff !important;
  }
  
  &:hover {
    opacity: 0.9;
  }
}
</style>
