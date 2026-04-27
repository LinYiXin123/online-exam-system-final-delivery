<!--管理中心-->
<template>
  <div id='manager'>
    <el-form :model="ruleForm2" status-icon :rules="rules2" ref="ruleForm2" label-width="100px" class="demo-ruleForm">
      <h3 class="alter">修改你的密码</h3>
        <el-form-item label="密码" prop="pass" class="pass">
        <el-input :type="showPass ? 'text' : 'password'" v-model="ruleForm2.pass" autocomplete="off">
          <i slot="suffix" :class="showPass ? 'el-icon-view' : 'el-icon-hide'" style="cursor: pointer; font-size: 16px;" @click="showPass = !showPass"></i>
        </el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="checkPass">
        <el-input :type="showCheckPass ? 'text' : 'password'" v-model="ruleForm2.checkPass" autocomplete="off">
          <i slot="suffix" :class="showCheckPass ? 'el-icon-view' : 'el-icon-hide'" style="cursor: pointer; font-size: 16px;" @click="showCheckPass = !showCheckPass"></i>
        </el-input>
      </el-form-item>
      <el-form-item class="btn-group">
        <div class="btn-wrapper">
          <el-button type="primary" @click="submitForm('ruleForm2')">提交</el-button>
          <el-button @click="resetForm('ruleForm2')">重置</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
  export default {
    data() {
      var validatePass = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请输入密码'));
        } else {
          if (this.ruleForm2.checkPass !== '') {
            this.$refs.ruleForm2.validateField('checkPass');
          }
          callback();
        }
      };
      var validatePass2 = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'));
        } else if (value !== this.ruleForm2.pass) {
          callback(new Error('两次输入密码不一致!'));
        } else {
          callback();
        }
      };
      return {
        ispass: true,
        showPass: false,
        showCheckPass: false,
        ruleForm2: {
          pass: '',
          checkPass: ''
        },
        rules2: {
          pass: [
            { validator: validatePass, trigger: 'blur' }
          ],
          checkPass: [
            { validator: validatePass2, trigger: 'blur' }
          ]
        }
      };
    },
    methods: {
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid")
            // 将 studentId 转换为数字类型
            let studentIdNum = Number(studentId)
            console.log("修改密码，studentId:", studentIdNum, "类型:", typeof studentIdNum)
            this.$axios({ //修改密码
              url: '/api/studentPWD',
              method: 'put',
              data: {
                pwd: this.ruleForm2.pass,
                studentId: studentIdNum
              }
            }).then(res => {
              console.log("修改密码响应:", res.data)
              if(res.data != null && res.data.code == 200) { //修改成功提示
                this.$message({
                  message: '密码修改成功...',
                  type: 'success'
                })
              } else {
                this.$message({
                  message: '密码修改失败',
                  type: 'error'
                })
              }
            }).catch(err => {
              console.log("修改密码错误:", err)
              this.$message({
                message: '密码修改失败',
                type: 'error'
              })
            })
          } else {
            console.log('error submit!!');
            return false;
          }
        });
      },
      resetForm(formName) {
        this.$refs[formName].resetFields();
      }
    }
  }
</script>

<style scoped>
#manager {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: linear-gradient(135deg, #e8f4fc 0%, #f5f7fa 100%);
}
#manager .demo-ruleForm {
  width: 420px;
  padding: 50px 40px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(64, 158, 255, 0.12);
  border-top: 4px solid var(--primary-color, #409EFF);
}
#manager .alter {
  margin: 0 0 35px 0;
  text-align: center;
  color: var(--primary-color, #409EFF);
  font-size: 24px;
  font-weight: 600;
}
#manager .el-form-item {
  margin-bottom: 28px;
}
#manager .btn-group {
  margin-top: 30px;
}
#manager .btn-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}
#manager .btn-wrapper .el-button {
  width: 120px;
  height: 42px;
  font-size: 15px;
  border-radius: 8px;
}
#manager .btn-wrapper .el-button--primary {
  background: var(--primary-color, #409EFF);
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
#manager .btn-wrapper .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}
#manager .btn-wrapper .el-button--default {
  border: 2px solid var(--primary-color, #409EFF);
  color: var(--primary-color, #409EFF);
  background: #fff;
}
#manager .btn-wrapper .el-button--default:hover {
  background: var(--primary-light, rgba(64, 158, 255, 0.1));
}
</style>