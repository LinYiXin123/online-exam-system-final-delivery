<!--
 * @Description: 
 * @Author: 
 * @Date: 2024-03-08 20:38:49
-->
<!-- 添加学生 -->
<template>
  <section class="add">
    <!-- 标题卡片 - 跟随主题色 -->
    <el-card shadow="never" class="title-card" :body-style="{ padding: 0 }">
      <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
        <span><i class="el-icon-user"></i> 添加学生</span>
      </div>
    </el-card>
    
    <el-card class="form-card">
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="姓名">
        <el-input v-model="form.studentName"></el-input>
      </el-form-item>
      <el-form-item label="性别">
        <el-select v-model="form.sex" placeholder="请选择性别" style="width: 100%;">
          <el-option label="男" value="男"></el-option>
          <el-option label="女" value="女"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="学院">
        <el-select v-model="form.institute" filterable allow-create placeholder="请选择或输入学院" style="width: 100%;">
          <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="所属专业">
        <el-select v-model="form.major" filterable allow-create placeholder="请选择或输入专业" style="width: 100%;">
          <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="年级">
        <el-select v-model="form.grade" filterable allow-create placeholder="请选择或输入年级" style="width: 100%;">
          <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="班级">
        <el-select v-model="form.clazz" filterable allow-create placeholder="请选择或输入班级" style="width: 100%;">
          <el-option v-for="item in clazzOptions" :key="item" :label="item" :value="item"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="电话号码" prop="tel">
        <el-input v-model="form.tel" placeholder="请输入11位手机号码"></el-input>
      </el-form-item>
      <el-form-item label="身份证号" prop="cardId">
        <el-input v-model="form.cardId" placeholder="请输入15或18位身份证号"></el-input>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" autocomplete="off" placeholder="请输入邮箱地址"></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="pwd">
        <el-input v-model="form.pwd" type="password" show-password autocomplete="new-password" placeholder="请输入密码"></el-input>
      </el-form-item>
      <el-form-item class="button-group">
        <el-button type="primary" @click="onSubmit()" size="medium">
          <i class="el-icon-circle-plus-outline"></i> 立即创建
        </el-button>
        <el-button type="danger" @click="cancel()" size="medium">
          <i class="el-icon-delete"></i> 取消
        </el-button>
      </el-form-item>
    </el-form>
    </el-card>
  </section>
</template>

<script>
export default {
  data() {
    return {
      form: { //表单数据初始化
        studentId: null,
        studentName: null,
        grade: null,
        major: null,
        clazz: null,
        institute: null,
        tel: null,
        email: null,
        pwd: null,
        cardId: null,
        sex: null,
        role: 2
      },
      // 下拉框选项数据
      instituteOptions: [],
      majorOptions: [],
      gradeOptions: [],
      clazzOptions: [],
      // 表单验证规则
      rules: {
        tel: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
        ],
        cardId: [
          { pattern: /(^\d{15}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号码（15位或18位）', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱地址', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式（如：xxx@xxx.com）', trigger: 'blur' }
        ],
        pwd: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
        ]
      },
      currentThemeColor: '#6366f1'
    };
  },
  created() {
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
    
    // 加载下拉框选项数据
    loadOptions() {
      console.log('开始加载下拉框选项数据...');
      // 直接使用分页接口获取数据
      this.loadOptionsFromPagination();
    },
    // 从分页接口获取数据
    loadOptionsFromPagination() {
      console.log('尝试从分页接口获取数据...');
      this.$axios.get('/api/students/1/1000/@/@/@/@/@/@').then(res => {
        console.log('分页接口响应:', res.data);
        if (res.data.code === 200 && res.data.data && res.data.data.records) {
          console.log('从分页接口获取的学生数据:', res.data.data.records);
          this.extractOptionsFromStudents(res.data.data.records);
        } else {
          console.log('分页接口也失败，使用默认选项');
          this.setDefaultOptions();
        }
      }).catch(error => {
        console.error('分页接口调用失败:', error);
        this.setDefaultOptions();
      });
    },
    // 从学生数据中提取选项
    extractOptionsFromStudents(students) {
      console.log('开始提取学生数据选项, 学生数量:', students ? students.length : 0);
      
      if (!students || students.length === 0) {
        console.log('没有学生数据，使用默认选项');
        this.setDefaultOptions();
        return;
      }
      
      // 提取并去重学院选项
      const institutes = [...new Set(students.map(s => s.institute).filter(item => item && item.trim()))];
      console.log('提取到的学院选项:', institutes);
      this.instituteOptions = institutes.sort();
      
      // 提取并去重专业选项
      const majors = [...new Set(students.map(s => s.major).filter(item => item && item.trim()))];
      console.log('提取到的专业选项:', majors);
      this.majorOptions = majors.sort();
      
      // 提取并去重年级选项
      const grades = [...new Set(students.map(s => s.grade).filter(item => item && item.trim()))];
      console.log('提取到的年级选项:', grades);
      this.gradeOptions = grades.sort();
      
      // 提取并去重班级选项
      const clazzes = [...new Set(students.map(s => s.clazz).filter(item => item && item.trim()))];
      console.log('提取到的班级选项:', clazzes);
      this.clazzOptions = clazzes.sort();
      
      // 输出最终的选项结果
      console.log('最终的专业选项:', this.majorOptions);
      console.log('最终的学院选项:', this.instituteOptions);
      console.log('最终的年级选项:', this.gradeOptions);
      console.log('最终的班级选项:', this.clazzOptions);
    },
    // 设置默认选项（从数据库获取失败时清空）
    setDefaultOptions() {
      console.log('数据库获取失败，清空选项');
      this.instituteOptions = [];
      this.majorOptions = [];
      this.gradeOptions = [];
      this.clazzOptions = [];
    },
    onSubmit() { //数据提交
      if (this.form.studentName == null || this.form.studentName.trim() == "") {
        this.$message({
          message: '请输入姓名',
          type: 'error'
        })
        return
      }
      if (this.form.grade == null || this.form.grade.trim() == "") {
        this.$message({
          message: '请输入年级',
          type: 'error'
        })
        return
      }
      if (this.form.major == null || this.form.major.trim() == "") {
        this.$message({
          message: '请输入专业',
          type: 'error'
        })
        return
      }
      if (this.form.clazz == null || this.form.clazz.trim() == "") {
        this.$message({
          message: '请输入班级',
          type: 'error'
        })
        return
      }
      if (this.form.institute == null || this.form.institute.trim() == "") {
        this.$message({
          message: '请输入学院',
          type: 'error'
        })
        return
      }
      if (this.form.tel == null || this.form.tel.trim() == "") {
        this.$message({
          message: '请输入手机号码',
          type: 'error'
        })
        return
      }
      // 电话号码正则验证（11位数字）
      const telReg = /^1[3-9]\d{9}$/;
      if (!telReg.test(this.form.tel)) {
        this.$message({
          message: '请输入正确的11位手机号码',
          type: 'error'
        })
        return
      }
      // 身份证号正则验证（18位或15位）
      if (this.form.cardId && this.form.cardId.trim() != "") {
        const cardIdReg = /(^\d{15}$)|(^\d{17}(\d|X|x)$)/;
        if (!cardIdReg.test(this.form.cardId)) {
          this.$message({
            message: '请输入正确的身份证号码（15位或18位）',
            type: 'error'
          })
          return
        }
      }
      if (this.form.email == null || this.form.email.trim() == "") {
        this.$message({
          message: '请输入邮箱',
          type: 'error'
        })
        return
      }
      // 邮箱正则验证
      const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      if (!emailReg.test(this.form.email)) {
        this.$message({
          message: '请输入正确的邮箱格式（如：xxx@xxx.com）',
          type: 'error'
        })
        return
      }
      if (this.form.pwd == null || this.form.pwd.trim() == "") {
        this.$message({
          message: '请输入密码',
          type: 'error'
        })
        return
      }
      if (this.form.sex == null || this.form.sex.trim() == "") {
        this.$message({
          message: '请输入性别',
          type: 'error'
        })
        return
      }
      // 构建请求数据，排除studentId（由数据库自动生成）
      const { studentId, ...submitData } = this.form;
      this.$axios({
        url: '/api/student',
        method: 'post',
        data: submitData
      }).then(res => {
        if (res.data.code == 200) {
          this.$message({
            message: '数据添加成功',
            type: 'success'
          })
          // 根据当前路由判断跳转路径
          const isAdmin = this.$route.path.startsWith('/admin');
          this.$router.push({ path: isAdmin ? '/admin/studentManage' : '/studentManage' });
        } else {
          this.$message({
            message: res.data.message || '添加失败',
            type: 'error',
            duration: 3000,
            showClose: true
          })
        }
      }).catch(err => {
        this.$message({
          message: '添加失败，请检查网络连接',
          type: 'error',
          duration: 3000
        })
      })
    },
    cancel() { //取消按钮
      this.form = {}
    },

  }
};
</script>
<style lang="less" scoped>
.add {
  max-width: 600px;
  margin: 0 auto;
  
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
  
  .form-card {
    border-radius: 8px;
    
    .el-form {
      padding: 20px;
      
      .el-input, .el-select {
        width: 100%;
      }
    }
  }
  
  .button-group {
    text-align: center;
    margin-top: 30px;
    
    /deep/ .el-form-item__content {
      display: flex;
      justify-content: center;
      gap: 20px;
    }
    
    .el-button {
      min-width: 120px;
      font-size: 14px;
      padding: 10px 20px;
      border-radius: 6px;
      
      i {
        margin-right: 5px;
      }
    }
    
    .el-button--primary {
      background-color: var(--admin-primary, #2384d6);
      border-color: var(--admin-primary, #2384d6);
      color: #fff;
      
      &:hover {
        opacity: 0.9;
      }
    }
    
    .el-button--danger {
      background-color: var(--admin-primary, #2384d6);
      border-color: var(--admin-primary, #2384d6);
      color: #fff;
      
      &:hover {
        opacity: 0.9;
      }
    }
  }
}
</style>

