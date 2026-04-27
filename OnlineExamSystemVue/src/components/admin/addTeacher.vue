<!--
 * @Description: 
 * @Author: 
 * @Date: 2024-03-08 20:38:49
-->
<!-- 添加教师 -->
<template>
  <section class="add">
    <!-- 标题卡片 - 跟随主题色 -->
    <el-card shadow="never" class="title-card" :body-style="{ padding: 0 }">
      <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
        <span><i class="el-icon-user"></i> 添加教师</span>
      </div>
    </el-card>
    
    <el-card class="form-card">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="teacherName">
          <el-input v-model="form.teacherName" placeholder="请输入教师姓名"></el-input>
        </el-form-item>
        <el-form-item label="学院" prop="institute">
          <el-select v-model="form.institute" filterable allow-create placeholder="请选择或输入学院" style="width: 100%;">
            <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="form.sex" placeholder="请选择性别" style="width: 100%;">
            <el-option label="男" value="男"></el-option>
            <el-option label="女" value="女"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="职称" prop="type">
          <el-select v-model="form.type" filterable allow-create placeholder="请选择或输入职称" style="width: 100%;">
            <el-option v-for="item in typeOptions" :key="item" :label="item" :value="item"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="电话号码" prop="tel">
          <el-input v-model="form.tel" placeholder="请输入11位手机号码"></el-input>
        </el-form-item>
        <el-form-item label="身份证号" prop="cardId">
          <el-input v-model="form.cardId" placeholder="请输入15或18位身份证号"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="pwd">
          <el-input v-model="form.pwd" type="password" show-password placeholder="请输入密码"></el-input>
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
      form: {
        teacherName: "",
        institute: "",
        sex: "",
        tel: "",
        pwd: "",
        cardId: "",
        type: ""
      },
      // 下拉框选项
      instituteOptions: [],
      typeOptions: [],
      // 所有教师数据（用于唯一性检查）
      allTeachers: [],
      // 表单验证规则
      rules: {
        teacherName: [
          { required: true, message: '请输入教师姓名', trigger: 'blur' }
        ],
        institute: [
          { required: true, message: '请选择或输入学院', trigger: 'change' }
        ],
        sex: [
          { required: true, message: '请选择性别', trigger: 'change' }
        ],
        type: [
          { required: true, message: '请选择或输入职称', trigger: 'change' }
        ],
        tel: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
        ],
        cardId: [
          { required: true, message: '请输入身份证号码', trigger: 'blur' },
          { pattern: /(^\d{15}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号码（15位或18位）', trigger: 'blur' }
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
    
    // 加载下拉框选项和教师数据
    async loadOptions() {
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
    
    // 检查电话号码唯一性
    checkTelUnique(tel) {
      return !this.allTeachers.some(t => t.tel === tel);
    },
    
    // 检查身份证号唯一性
    checkCardIdUnique(cardId) {
      return !this.allTeachers.some(t => t.cardId === cardId);
    },
    
    onSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return;
        
        // 检查电话号码唯一性
        if (!this.checkTelUnique(this.form.tel)) {
          this.$message.error('该电话号码已被其他教师使用');
          return;
        }
        
        // 检查身份证号唯一性
        if (!this.checkCardIdUnique(this.form.cardId)) {
          this.$message.error('该身份证号已被其他教师使用');
          return;
        }
        
        try {
          const res = await this.$axios({
            url: '/api/teacher',
            method: 'post',
            data: { ...this.form }
          });
          
          if (res.data.code === 200) {
            this.$message.success('教师添加成功');
            // 根据当前路由判断跳转路径
            const isAdmin = this.$route.path.startsWith('/admin');
            this.$router.push({ path: isAdmin ? '/admin/teacherManage' : '/teacherManage' });
          } else {
            this.$message.error(res.data.message || '添加失败');
          }
        } catch (error) {
          this.$message.error('添加失败，请检查网络连接');
        }
      });
    },
    
    cancel() {
      this.$refs.form.resetFields();
    }
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
      background-color: var(--admin-primary, #6366f1);
      border-color: var(--admin-primary, #6366f1);
      color: #fff;
      
      &:hover {
        opacity: 0.9;
      }
    }
    
    .el-button--danger {
      background-color: var(--admin-primary, #6366f1);
      border-color: var(--admin-primary, #6366f1);
      color: #fff;
      
      &:hover {
        opacity: 0.9;
      }
    }
  }
}
</style>

