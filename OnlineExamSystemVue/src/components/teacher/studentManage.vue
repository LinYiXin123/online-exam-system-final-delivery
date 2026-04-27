// 学生管理页面
<template>
  <div class="student">
    <!-- 页面标题和操作区 - 与仪表盘风格一致 -->
    <el-card shadow="never" class="title-card" :body-style="{ padding: 0 }">
      <div class="custom-card-header">
        <span><i class="el-icon-user"></i> 学生管理</span>
        <el-button icon="el-icon-plus" @click="goToAddStudent" size="small" class="header-btn">添加学生</el-button>
      </div>
    </el-card>

    <!-- 搜索和筛选区 -->
    <div class="search-filter-bar">
      <div class="search-section">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索学生姓名、学号或联系方式"
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
          <el-button icon="el-icon-download" size="small" @click="exportStudents">导出</el-button>
          <el-dropdown @command="handleImportAction" size="small">
            <el-button icon="el-icon-upload2" size="small">
              导入<i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="import">导入数据</el-dropdown-item>
              <el-dropdown-item command="template">下载模板</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button icon="el-icon-refresh" size="small" @click="refreshData" :loading="isRefreshing">刷新</el-button>
        </div>
      </div>
      
      <transition name="fade-in">
      <el-table :data="pagination.records" border style="width: 100%" class="modern-table" :key="tableKey">
      <el-table-column type="index" label="序号" width="60" align="center" :index="getTableIndex"></el-table-column>
      <el-table-column prop="studentId" label="ID" min-width="100" show-overflow-tooltip></el-table-column>
      <el-table-column prop="studentName" label="姓名" min-width="80" show-overflow-tooltip></el-table-column>
      <el-table-column prop="institute" label="学院" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="major" label="专业" min-width="100" show-overflow-tooltip></el-table-column>
      <el-table-column prop="grade" label="年级" min-width="60" show-overflow-tooltip></el-table-column>
      <el-table-column prop="clazz" label="班级" min-width="60" show-overflow-tooltip></el-table-column>
      <el-table-column prop="sex" label="性别" min-width="50" show-overflow-tooltip></el-table-column>
      <el-table-column prop="tel" label="联系方式" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column label="操作" min-width="200" align="center">
        <template slot-scope="scope">
          <div class="action-btns">
            <el-button @click="checkGrade(scope.row.studentId)" type="primary" size="mini">编辑</el-button>
            <el-button @click="deleteById(scope.row.studentId)" type="danger" size="mini">删除</el-button>
          </div>
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
    <!-- 编辑对话框-->
    <el-dialog
      title="编辑学生信息"
      :visible.sync="dialogVisible"
      width="500px">
      <section class="update" style="padding: 10px 20px;">
        <el-form ref="form" :model="form" label-width="80px" style="width: 100%;">
          <el-form-item label="姓名" style="margin-bottom: 22px;">
            <el-input v-model="form.studentName"></el-input>
          </el-form-item>
          <el-form-item label="学院" style="margin-bottom: 22px;">
            <el-select v-model="form.institute" filterable allow-create placeholder="请选择或输入学院" style="width: 100%;">
              <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="专业" style="margin-bottom: 22px;">
            <el-select v-model="form.major" filterable allow-create placeholder="请选择或输入专业" style="width: 100%;">
              <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="年级" style="margin-bottom: 22px;">
            <el-select v-model="form.grade" filterable allow-create placeholder="请选择或输入年级" style="width: 100%;">
              <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="班级" style="margin-bottom: 22px;">
            <el-select v-model="form.clazz" filterable allow-create placeholder="请选择或输入班级" style="width: 100%;">
              <el-option v-for="item in clazzOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="性别" style="margin-bottom: 22px;">
            <el-select v-model="form.sex" placeholder="请选择性别" style="width: 100%;">
              <el-option v-for="item in sexOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="电话号码" style="margin-bottom: 22px;">
            <el-input v-model="form.tel"></el-input>
          </el-form-item>
          <el-form-item label="密码" style="margin-bottom: 22px;">
            <el-input v-model="form.pwd" type="password" placeholder="留空则不修改密码" show-password></el-input>
          </el-form-item>
        </el-form>
      </section>
      <span slot="footer" class="dialog-footer" style="display: flex; justify-content: center; gap: 20px; width: 100%;">
        <el-button type="primary" @click="submit()" size="medium" style="width: 100px; height: 40px;">确定</el-button>
        <el-button @click="dialogVisible = false" size="medium" icon="el-icon-delete" style="width: 100px; height: 40px;">取消</el-button>
      </span>
    </el-dialog>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      pagination: {
        //分页后的考试信息
        current: 1, //当前页
        total: null, //记录条数
        size: 6, //每页条数
      },
      dialogVisible: false, //对话框
      form: {}, //保存点击以后当前试卷的信息,
      condition: {
        name: "",
        tel: "",
        grade: "",
        clazz: "",
        major: "",
        institute: "",
      },
      // 新的搜索和筛选条件
      searchKeyword: '',
      filterCondition: {
        institute: '',
        major: '',
        grade: ''
      },
      // 下拉框选项数据
      instituteOptions: [],
      majorOptions: [],
      gradeOptions: [],
      clazzOptions: [],
      sexOptions: ['男', '女'],
      tableKey: 0,  // 用于触发表格重新渲染动画
      isRefreshing: false  // 刷新加载状态
    };
  },
  created() {
    this.getStudentGrade();
    this.loadSelectOptions();
  },
  methods: {
    getStudentGrade() {
      // 使用后端搜索，传递搜索关键词到后端
      let name = '@';
      let grade = '@';
      let tel = '@';
      let institute = '@';
      let major = '@';
      let clazz = '@';
      
      // 处理搜索关键词 - 判断是姓名还是联系方式
      if (this.searchKeyword && this.searchKeyword.trim()) {
        const keyword = this.searchKeyword.trim();
        // 如果是纯数字，按联系方式搜索；否则按姓名搜索
        if (/^\d+$/.test(keyword)) {
          tel = encodeURIComponent(keyword);
        } else {
          name = encodeURIComponent(keyword);
        }
      }
      
      // 处理筛选条件
      if (this.filterCondition.institute) {
        institute = encodeURIComponent(this.filterCondition.institute);
      }
      if (this.filterCondition.major) {
        major = encodeURIComponent(this.filterCondition.major);
      }
      if (this.filterCondition.grade) {
        grade = encodeURIComponent(this.filterCondition.grade);
      }
      
      const apiUrl = `/api/students/${this.pagination.current}/${this.pagination.size}/${name}/${grade}/${tel}/${institute}/${major}/${clazz}`;
      
      this.$axios(apiUrl).then(res => {
        const data = res.data.data;
        
        // 设置分页数据
        if (data && data.records) {
          this.pagination = {
            records: data.records,
            current: parseInt(data.current) || 1,
            total: parseInt(data.total) || 0,
            size: parseInt(data.size) || 6
          };
          // 从当前分页数据中提取选项
          if (data.records.length > 0) {
            this.extractOptionsFromStudents(data.records);
          }
        } else {
          // 数据结构不正确时，显示空列表
          console.warn('后端返回数据结构不正确:', res.data);
          this.pagination = {
            records: [],
            current: 1,
            total: 0,
            size: 6
          };
        }
      }).catch(error => {
        console.error('API调用错误:', error);
        this.$message.error('获取学生列表失败');
      });
    },
    // 加载下拉框选项数据（从数据库获取）
    loadSelectOptions() {
      // 从数据库获取所有学生数据，提取选项
      this.$axios.get('/api/students/1/1000/@/@/@/@/@/@').then(res => {
        if (res.data.code === 200 && res.data.data && res.data.data.records) {
          const students = res.data.data.records;
          // 清空现有选项
          this.instituteOptions = [];
          this.majorOptions = [];
          this.gradeOptions = [];
          this.clazzOptions = [];
          // 从数据库数据中提取选项
          this.extractOptionsFromStudents(students);
          console.log('从数据库加载下拉框选项成功，学生数:', students.length);
        } else {
          console.warn('获取学生数据失败，使用默认选项');
          this.setDefaultOptions();
        }
      }).catch(error => {
        console.error('加载选项数据失败:', error);
        this.setDefaultOptions();
      });
    },
    // 从学生数据中提取选项
    extractOptionsFromStudents(students) {
      if (!students || students.length === 0) return;
      
      // 提取并去重学院选项
      const institutes = [...new Set(students.map(s => s.institute).filter(item => item && item.trim()))];
      this.instituteOptions = [...new Set([...this.instituteOptions, ...institutes])].sort();
      
      // 提取并去重专业选项
      const majors = [...new Set(students.map(s => s.major).filter(item => item && item.trim()))];
      this.majorOptions = [...new Set([...this.majorOptions, ...majors])].sort();
      
      // 提取并去重年级选项
      const grades = [...new Set(students.map(s => s.grade).filter(item => item && item.trim()))];
      this.gradeOptions = [...new Set([...this.gradeOptions, ...grades])].sort();
      
      // 提取并去重班级选项
      const clazzes = [...new Set(students.map(s => s.clazz).filter(item => item && item.trim()))];
      this.clazzOptions = [...new Set([...this.clazzOptions, ...clazzes])].sort();
    },
    // 设置默认选项（从数据库获取失败时清空）
    setDefaultOptions() {
      console.log('数据库获取失败，清空选项');
      this.instituteOptions = [];
      this.majorOptions = [];
      this.gradeOptions = [];
      this.clazzOptions = [];
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = parseInt(val) || 6;
      this.pagination.current = 1; // 重置到第一页
      this.getStudentGrade();
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = parseInt(val) || 1;
      this.getStudentGrade();
    },
    checkGrade(studentId) { //修改学生信息
      this.dialogVisible = true
      this.$axios(`/api/student/${studentId}`).then(res => {
        this.form = res.data.data
      })
    },
    deleteById(studentId) { //删除当前学生
      var _this = this;
      this.$confirm("确定删除当前学生吗？删除后无法恢复","Warning",{
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'danger'
      }).then(()=> { //确认删除
        this.$axios({
          url: `/api/student/${studentId}`,
          method: 'delete',
        }).then(res => {
          this.getStudentGrade()
        })
      }).catch(() => {

      })
    },
    submit() { //提交更改
      this.$axios({
        url: '/api/student',
        method: 'put',
        data: {
          ...this.form
        }
      }).then(res => {
        if(res.data.code == 200) {
          this.dialogVisible = false
          this.$message({
            message: '更新成功',
            type: 'success'
          })
          this.getStudentGrade()
        } else {
          // 更新失败时不关闭对话框，显示具体错误原因
          this.$message({
            message: res.data.message || '更新失败',
            type: 'error',
            duration: 3000,
            showClose: true,
            customClass: 'error-message-red'
          })
        }
      }).catch(err => {
        this.$message({
          message: '更新失败，请检查网络连接',
          type: 'error',
          duration: 3000
        })
      })
    },
    // 工具栏按钮方法
    goToAddStudent() {
      // 根据当前路由判断是管理员端还是教师端
      const isAdmin = this.$route.path.startsWith('/admin');
      this.$router.push(isAdmin ? '/admin/addStudent' : '/addStudent');
    },
    // 导出学生数据为Excel文件
    exportStudents() {
      const loading = this.$loading({
        lock: true,
        text: '正在导出数据...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      
      try {
        // 获取当前的学生数据
        const students = this.pagination.records || [];
        
        if (students.length === 0) {
          this.$message({
            message: '没有数据可导出',
            type: 'warning'
          });
          loading.close();
          return;
        }
        
        // 准备导出Excel的数据
        const exportData = students.map(student => ({
          '学号': student.studentId,
          '姓名': student.studentName,
          '学院': student.institute,
          '专业': student.major,
          '年级': student.grade,
          '班级': student.clazz,
          '性别': student.sex,
          '联系方式': student.tel,
          '邮箱': student.email
        }));
        
        // 使用简单的CSV导出方式
        this.downloadCSV(exportData, '学生数据导出.csv');
        
        this.$message({
          message: `成功导出 ${students.length} 条学生数据`,
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
    
    // 导入学生数据
    importStudents() {
      // 创建隐藏的文件输入框
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = '.csv,.xlsx,.xls';
      input.style.display = 'none';
      
      input.onchange = (event) => {
        const file = event.target.files[0];
        if (!file) return;
        
        const loading = this.$loading({
          lock: true,
          text: '正在导入数据...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        
        // 检查文件类型
        const fileName = file.name.toLowerCase();
        if (fileName.endsWith('.csv')) {
          this.parseCSVFile(file, loading);
        } else {
          this.$message({
            message: '请上传CSV文件格式',
            type: 'warning'
          });
          loading.close();
        }
      };
      
      document.body.appendChild(input);
      input.click();
      document.body.removeChild(input);
    },
    
    // 下载CSV文件
    downloadCSV(data, filename) {
      if (!data || data.length === 0) return;
      
      // 获取表头
      const headers = Object.keys(data[0]);
      
      // 构建CSV内容
      let csvContent = '\uFEFF'; // BOM头，解决中文乱码问题
      csvContent += headers.join(',') + '\n';
      
      data.forEach(row => {
        const values = headers.map(header => {
          let value = row[header] || '';
          // 如果包含逗号或换行符，用双引号包裹
          if (value.toString().includes(',') || value.toString().includes('\n')) {
            value = `"${value.toString().replace(/"/g, '""')}"`;
          }
          return value;
        });
        csvContent += values.join(',') + '\n';
      });
      
      // 创建Blob对象
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      
      // 创建下载链接
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
    
    // 解析CSV文件
    parseCSVFile(file, loading) {
      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const text = e.target.result;
          const lines = text.split('\n').filter(line => line.trim());
          
          if (lines.length < 2) {
            this.$message({
              message: 'CSV文件格式不正确',
              type: 'error'
            });
            loading.close();
            return;
          }
          
          // 解析表头
          const headers = lines[0].split(',').map(h => h.trim().replace(/"/g, ''));
          console.log('表头:', headers);
          
          // 解析数据行
          const students = [];
          for (let i = 1; i < lines.length; i++) {
            const values = lines[i].split(',').map(v => v.trim().replace(/"/g, ''));
            if (values.length >= headers.length) {
              const student = {
                studentName: values[headers.indexOf('姓名')] || '',
                institute: values[headers.indexOf('学院')] || '',
                major: values[headers.indexOf('专业')] || '',
                grade: values[headers.indexOf('年级')] || '',
                clazz: values[headers.indexOf('班级')] || '',
                sex: values[headers.indexOf('性别')] || '',
                tel: values[headers.indexOf('联系方式')] || '',
                email: values[headers.indexOf('邮箱')] || '',
                pwd: '123456', // 默认密码
                cardId: '', // 身份证号留空
                role: '2' // 学生角色
              };
              
              // 验证必要字段
              if (student.studentName && student.institute && student.major) {
                students.push(student);
              }
            }
          }
          
          if (students.length === 0) {
            this.$message({
              message: '没有找到有效的学生数据',
              type: 'warning'
            });
            loading.close();
            return;
          }
          
          // 显示确认对话框
          this.$confirm(`将导入 ${students.length} 条学生数据，是否继续？`, '确认导入', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.batchImportStudents(students, loading);
          }).catch(() => {
            loading.close();
          });
          
        } catch (error) {
          console.error('解析CSV失败:', error);
          this.$message({
            message: 'CSV文件解析失败',
            type: 'error'
          });
          loading.close();
        }
      };
      
      reader.readAsText(file, 'UTF-8');
    },
    
    // 批量导入学生
    batchImportStudents(students, loading) {
      // 这里应该调用后端API进行批量导入
      // 暂时模拟导入过程
      setTimeout(() => {
        this.$message({
          message: `成功导入 ${students.length} 条学生数据`,
          type: 'success'
        });
        
        // 刷新学生列表
        this.getStudentGrade();
        loading.close();
      }, 1000);
      
      // 实际实现时应该调用后端API
      /*
      this.$axios.post('/api/students/batch', students).then(res => {
        if (res.data.code === 200) {
          this.$message({
            message: `成功导入 ${students.length} 条学生数据`,
            type: 'success'
          });
          this.getStudentGrade();
        } else {
          this.$message({
            message: '导入失败：' + res.data.message,
            type: 'error'
          });
        }
      }).catch(error => {
        this.$message({
          message: '导入失败，请重试',
          type: 'error'
        });
      }).finally(() => {
        loading.close();
      });
      */
    },
    
    // 处理导入操作
    handleImportAction(command) {
      if (command === 'import') {
        this.importStudents();
      } else if (command === 'template') {
        this.downloadTemplate();
      }
    },
    
    // 下载导入模板
    downloadTemplate() {
      const templateData = [
        {
          '姓名': '张三',
          '学院': '计算机学院',
          '专业': '计算机科学与技术',
          '年级': '2023',
          '班级': '1',
          '性别': '男',
          '联系方式': '13800000001',
          '邮箱': 'zhangsan@example.com'
        },
        {
          '姓名': '李四',
          '学院': '软件工程学院',
          '专业': '软件工程',
          '年级': '2023',
          '班级': '2',
          '性别': '女',
          '联系方式': '13800000002',
          '邮箱': 'lisi@example.com'
        }
      ];
      
      this.downloadCSV(templateData, '学生数据导入模板.csv');
      
      this.$message({
        message: '模板下载成功，请按照模板格式填写数据',
        type: 'success'
      });
    },
    
    async refreshData() {
      this.isRefreshing = true;
      this.tableKey++;  // 触发表格动画
      
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
    },
    showSettings() {
      this.$message({
        message: '设置功能开发中...',
        type: 'info'
      });
    },
    // 新的搜索和筛选方法
    handleSearch() {
      // 实时搜索，防抖处理
      console.log('搜索触发:', this.searchKeyword);
      clearTimeout(this.searchTimer);
      this.searchTimer = setTimeout(() => {
        console.log('执行搜索:', this.searchKeyword);
        this.applyFilters();
      }, 300);
    },
    handleFilter() {
      this.applyFilters();
    },
    resetFilters() {
      this.searchKeyword = '';
      this.filterCondition = {
        institute: '',
        major: '',
        grade: ''
      };
      this.applyFilters();
    },
    applyFilters() {
      this.tableKey++;  // 触发表格动画
      console.log('applyFilters被调用，搜索关键词:', this.searchKeyword);
      
      // 简化搜索逻辑，直接重新加载数据
      this.pagination.current = 1; // 重置到第一页
      this.getStudentGrade();
    },
    
    // 计算表格序号（支持分页）
    getTableIndex(index) {
      return (this.pagination.current - 1) * this.pagination.size + index + 1;
    },
  }
};
</script>
<style lang="less" scoped>
.student {
  width: 100%;
  padding: 20px;
  background-color: var(--bg-primary);
  
  // 标题卡片 - 与仪表盘风格统一
  .title-card {
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 20px;
  }
  
  // 通用卡片头部样式 - 与仪表盘一致
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
      
      .search-input {
        width: 100%;
        
        .el-input__inner {
          height: 40px;
          border-radius: 6px;
          border: 1px solid var(--border-color);
          
          &:focus {
            border-color: var(--primary-color);
          }
        }
      }
    }
    
    .filter-section {
      display: flex;
      gap: 12px;
      align-items: center;
      
      .el-select {
        width: 120px;
        
        .el-input__inner {
          height: 40px;
          border-radius: 6px;
        }
      }
      
      .el-button {
        height: 40px;
        padding: 0 16px;
        border-radius: 6px;
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
        align-items: center;
        
        .el-button {
          height: 36px;
          min-width: 80px;
          padding: 0 16px;
          border-radius: 6px;
          font-size: 13px;
          font-weight: 500;
          display: inline-flex;
          align-items: center;
          justify-content: center;
          
          i {
            margin-right: 4px;
          }
        }
        
        .el-dropdown {
          .el-button {
            height: 36px;
            min-width: 80px;
            padding: 0 16px;
            border-radius: 6px;
            font-size: 13px;
            font-weight: 500;
          }
        }
      }
    }
    
    .modern-table {
      .el-table__header-wrapper {
        .el-table__header {
          th {
            background-color: var(--table-header-bg);
            color: var(--text-primary);
            font-weight: 600;
            border-bottom: 1px solid var(--border-color);
          }
        }
      }
      
      .el-table__body-wrapper {
        .el-table__body {
          tr {
            &:hover {
              background-color: var(--table-row-hover);
            }
            
            td {
              border-bottom: 1px solid var(--border-light);
              
              .el-button {
                margin: 0 2px;
                padding: 4px 8px;
                font-size: 12px;
                border-radius: 4px;
                
                &.el-button--mini {
                  height: 28px;
                }
              }
            }
          }
        }
      }
    }
  }
  
  .demo-form-inline {
    background-color: var(--card-bg);
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 var(--shadow-color);
    padding: 20px;
    margin-bottom: 20px;
  }
  
  .el-table {
    width: 100%;
    background-color: var(--card-bg) !important;
    color: var(--text-primary) !important;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 var(--shadow-color);
  }
  
  .page {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .el-table tr {
    background-color: var(--card-bg) !important;
  }
  
  .el-table td, .el-table th {
    padding: 8px 0 !important;
    height: 50px !important;
  }
  
  .el-table--border td, .el-table--border th {
    border-right: 1px solid #ebeef5;
  }
  
  .el-table__fixed-right {
    height: auto !important;
  }
  
  // 操作按钮水平排列样式 - 与题目列表保持一致
  .action-btns {
    display: flex !important;
    flex-direction: row !important;
    justify-content: center !important;
    align-items: center !important;
    gap: 8px !important;
    flex-wrap: nowrap !important;
    white-space: nowrap !important;
    
    .el-button {
      margin: 0 !important;
      padding: 10px 12px !important;
      font-size: 12px !important;
      min-width: auto !important;
      height: 36px !important;
      border-radius: 4px !important;
      flex-shrink: 0 !important;
      white-space: nowrap !important;
      overflow: visible !important;
    }
  }
}

.el-table .success-row {
  background-color: var(--table-row-hover) !important;
}

/* 表单样式优化 */
.demo-form-inline {
  .el-row {
    margin-bottom: 10px;
    
    &:last-child {
      margin-bottom: 0;
    }
  }
  
  .el-form-item {
    margin-bottom: 10px;
    width: 100%;
  }
  
  .el-form-item__label {
    color: var(--text-primary) !important;
    font-size: 14px;
    width: 80px !important;
  }
  
  .el-input {
    width: 100%;
  }
  
  .el-button--primary {
    background-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
    border-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
    border-radius: 6px !important;
    font-weight: 500 !important;
    transition: all 0.3s ease !important;
    
    &:hover {
      background-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
      border-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
      opacity: 0.9;
      transform: translateY(-2px) !important;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
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

// 操作按钮使用主题色 - 统一风格：主题色背景+白色图标文字
/deep/ .action-btns {
  .el-button--primary,
  .el-button--danger {
    background-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
    border-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
    color: #fff !important;
    
    // 确保图标和文字都是白色
    i, span {
      color: #fff !important;
    }
  }
}

// 导出/导入/刷新按钮 - 统一风格：主题色背景+白色图标文字
/deep/ .table-actions .el-button {
  background-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
  border-color: var(--admin-primary, var(--primary-color, #6366f1)) !important;
  color: #fff !important;
  
  i, span {
    color: #fff !important;
  }
  
  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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

<style>
/* 错误消息浅红色背景样式 */
.error-message-red {
  background-color: #fff2f0 !important;
  border-color: #ffccc7 !important;
}
.error-message-red .el-message__content {
  color: #ff4d4f !important;
}
.el-message--error {
  background-color: #fff2f0 !important;
  border-color: #ffccc7 !important;
}
</style>
