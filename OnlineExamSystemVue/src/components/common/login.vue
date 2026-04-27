<template>
  <div class="container">
    <div style="width: 25%; min-width: 320px; min-height: auto; display: flex; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);">
      <div style="flex: 1;width: 50%; padding: 40px;display: flex;flex-direction: column;justify-content: center;" >
        <div style="text-align: center; font-size: 30px; margin-bottom: 30px; color: #000000;">在线考试管理系统</div>

        <!-- 登录表单 -->
        <el-form v-if="!isRegister && !isForgotPassword" :label-position="labelPosition" :model="formLabelAlign">
          <el-form-item>
            <el-input v-model.number="formLabelAlign.username" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input v-model="formLabelAlign.password" placeholder="请输入密码" :type="showLoginPwd ? 'text' : 'password'">
              <i slot="suffix" :class="showLoginPwd ? 'el-icon-view' : 'el-icon-hide'" style="cursor: pointer; font-size: 16px;" @click="showLoginPwd = !showLoginPwd"></i>
            </el-input>
          </el-form-item>
          <el-form-item>
            <button type="button" style="width: 100%; height: 40px; background-color: #409EFF; border: none; border-radius: 4px; color: white; font-size: 16px; cursor: pointer;" @click="login()">登 录</button>
          </el-form-item>
          <div style="text-align: center; margin-top: 15px;">
            <span style="color: #8c8c8c;">还没有账号？</span>
            <el-button type="text" @click="isRegister = true">立即注册</el-button>
            <span style="color: #8c8c8c; margin-left: 10px;">|</span>
            <el-button type="text" @click="isForgotPassword = true">忘记密码</el-button>
          </div>
        </el-form>

        <!-- 找回密码表单 -->
        <el-form v-else-if="isForgotPassword" :label-position="labelPosition" :model="forgotForm" :rules="forgotRules" ref="forgotFormRef">
          <div style="text-align: center; font-size: 20px; margin-bottom: 20px; color: #409EFF;">找回密码</div>
          <el-form-item prop="studentId">
            <el-input v-model.number="forgotForm.studentId" placeholder="请输入学号"></el-input>
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="forgotForm.email" placeholder="请输入注册时的邮箱"></el-input>
          </el-form-item>
          <el-form-item prop="tel">
            <el-input v-model="forgotForm.tel" placeholder="请输入注册时的手机号"></el-input>
          </el-form-item>
          <el-form-item prop="newPwd">
            <el-input v-model="forgotForm.newPwd" placeholder="请输入新密码" :type="showForgotPwd ? 'text' : 'password'">
              <i slot="suffix" :class="showForgotPwd ? 'el-icon-view' : 'el-icon-hide'" style="cursor: pointer; font-size: 16px;" @click="showForgotPwd = !showForgotPwd"></i>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPwd">
            <el-input v-model="forgotForm.confirmPwd" placeholder="请确认新密码" :type="showForgotConfirmPwd ? 'text' : 'password'">
              <i slot="suffix" :class="showForgotConfirmPwd ? 'el-icon-view' : 'el-icon-hide'" style="cursor: pointer; font-size: 16px;" @click="showForgotConfirmPwd = !showForgotConfirmPwd"></i>
            </el-input>
          </el-form-item>
          <div>
            <el-button type="primary" style="width: 100%;margin-top: 10px" @click="resetPassword()">重置密码</el-button>
          </div>
          <div style="text-align: center; margin-top: 15px;">
            <el-button type="text" @click="goBackToLogin()">返回登录</el-button>
          </div>
        </el-form>

        <!-- 注册表单 -->
        <el-form v-else :label-position="labelPosition" :model="registerForm" :rules="registerRules" ref="registerFormRef">
          <el-form-item prop="studentId">
            <el-input v-model.number="registerForm.studentId" placeholder="请输入学号"></el-input>
          </el-form-item>
          <el-form-item prop="studentName">
            <el-input v-model="registerForm.studentName" placeholder="请输入姓名"></el-input>
          </el-form-item>
          <el-form-item prop="pwd">
            <el-input v-model="registerForm.pwd" placeholder="请输入密码" :type="showPwd ? 'text' : 'password'">
              <i slot="suffix" :class="showPwd ? 'el-icon-view' : 'el-icon-hide'" style="cursor: pointer; font-size: 16px;" @click="showPwd = !showPwd"></i>
            </el-input>
          </el-form-item>
          <el-form-item prop="confirmPwd">
            <el-input v-model="registerForm.confirmPwd" placeholder="请确认密码" :type="showConfirmPwd ? 'text' : 'password'">
              <i slot="suffix" :class="showConfirmPwd ? 'el-icon-view' : 'el-icon-hide'" style="cursor: pointer; font-size: 16px;" @click="showConfirmPwd = !showConfirmPwd"></i>
            </el-input>
          </el-form-item>
          <el-form-item prop="sex">
            <el-select v-model="registerForm.sex" placeholder="请选择性别" style="width: 100%;">
              <el-option label="男" value="男"></el-option>
              <el-option label="女" value="女"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="grade">
            <el-select v-model="registerForm.grade" placeholder="请选择年级" style="width: 100%;" filterable>
              <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="major">
            <el-select v-model="registerForm.major" placeholder="请选择专业" style="width: 100%;" filterable>
              <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="clazz">
            <el-select v-model="registerForm.clazz" placeholder="请选择班级" style="width: 100%;" filterable>
              <el-option v-for="item in classOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="institute">
            <el-select v-model="registerForm.institute" placeholder="请选择学院" style="width: 100%;" filterable>
              <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="tel">
            <el-input v-model="registerForm.tel" placeholder="请输入手机号（选填）"></el-input>
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱（选填）"></el-input>
          </el-form-item>
          <div>
            <el-button type="primary" style="width: 100%;margin-top: 10px" @click="register()">注册</el-button>
          </div>
          <div style="text-align: center; margin-top: 15px;">
            <span style="color: #8c8c8c;">已有账号？</span>
            <el-button type="text" @click="isRegister = false">返回登录</el-button>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from "vuex";
export default {
    name: "login",
    data() {
        // 确认密码验证器
        const validateConfirmPwd = (rule, value, callback) => {
            if (value === '') {
                callback(new Error('请确认密码'));
            } else if (value !== this.registerForm.pwd) {
                callback(new Error('两次输入的密码不一致'));
            } else {
                callback();
            }
        };
        return {
            role: 2,
            labelPosition: "left",
            isRegister: false,
            isForgotPassword: false,
            showLoginPwd: false,
            showPwd: false,
            showConfirmPwd: false,
            showForgotPwd: false,
            showForgotConfirmPwd: false,
            formLabelAlign: {
                username: "",
                password: "",
            },
            forgotForm: {
                studentId: "",
                email: "",
                tel: "",
                newPwd: "",
                confirmPwd: "",
            },
            registerForm: {
                studentId: "",
                studentName: "",
                pwd: "",
                confirmPwd: "",
                sex: "",
                grade: "",
                major: "",
                clazz: "",
                institute: "",
                tel: "",
                email: "",
            },
            gradeOptions: [],
            majorOptions: [],
            classOptions: [],
            instituteOptions: [],
            registerRules: {
                studentId: [
                    { required: true, message: '请输入学号', trigger: 'blur' },
                    { pattern: /^\d{1,18}$/, message: '学号应为1-18位数字', trigger: 'blur' }
                ],
                studentName: [
                    { required: true, message: '请输入姓名', trigger: 'blur' }
                ],
                pwd: [
                    { required: true, message: '请输入密码', trigger: 'blur' },
                    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
                ],
                confirmPwd: [
                    { required: true, validator: validateConfirmPwd, trigger: 'blur' }
                ],
                sex: [
                    { required: true, message: '请选择性别', trigger: 'change' }
                ],
                grade: [
                    { required: true, message: '请选择年级', trigger: 'change' }
                ],
                major: [
                    { required: true, message: '请选择专业', trigger: 'change' }
                ],
                clazz: [
                    { required: true, message: '请选择班级', trigger: 'change' }
                ],
                institute: [
                    { required: true, message: '请选择学院', trigger: 'change' }
                ],
                tel: [
                    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
                ],
                email: [
                    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
                ]
            },
            forgotRules: {
                studentId: [
                    { required: true, message: '请输入学号', trigger: 'blur' },
                    { pattern: /^\d{1,18}$/, message: '学号应为1-18位数字', trigger: 'blur' }
                ],
                email: [
                    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
                ],
                tel: [
                    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
                ],
                newPwd: [
                    { required: true, message: '请输入新密码', trigger: 'blur' },
                    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
                ],
                confirmPwd: [
                    { required: true, message: '请确认新密码', trigger: 'blur' }
                ]
            },
        };
    },
    methods: {
        // 返回登录页面
        goBackToLogin() {
            this.isRegister = false;
            this.isForgotPassword = false;
        },
        // 找回密码
        resetPassword() {
            this.$refs.forgotFormRef.validate((valid) => {
                if (!valid) {
                    return false;
                }
                // 验证两次密码是否一致
                if (this.forgotForm.newPwd !== this.forgotForm.confirmPwd) {
                    this.$message.error('两次输入的密码不一致');
                    return;
                }
                // 验证至少填写邮箱或手机号
                if (!this.forgotForm.email && !this.forgotForm.tel) {
                    this.$message.error('请至少填写邮箱或手机号');
                    return;
                }
                // 发送找回密码请求
                this.$axios({
                    url: `/api/forgot-password`,
                    method: "post",
                    data: {
                        studentId: Number(this.forgotForm.studentId),
                        email: this.forgotForm.email || null,
                        tel: this.forgotForm.tel || null,
                        newPwd: this.forgotForm.newPwd,
                    },
                })
                    .then((res) => {
                        if (res.data.code === 200) {
                            this.$message.success("密码重置成功，请登录");
                            // 清空表单
                            this.forgotForm = {
                                studentId: "",
                                email: "",
                                tel: "",
                                newPwd: "",
                                confirmPwd: "",
                            };
                            // 返回登录页面
                            this.isForgotPassword = false;
                        } else {
                            this.$message.error(res.data.message || "密码重置失败");
                        }
                    })
                    .catch((e) => {
                        console.log(e);
                        if (e.response && e.response.data && e.response.data.message) {
                            this.$message.error(e.response.data.message);
                        } else {
                            this.$message.error("密码重置失败，请检查信息是否正确");
                        }
                    });
            });
        },
        // 学生注册
        register() {
            // 使用 Element UI 表单验证
            this.$refs.registerFormRef.validate((valid) => {
                if (!valid) {
                    return false;
                }
                // 发送注册请求
                this.submitRegister();
            });
        },
        // 提交注册请求
        submitRegister() {
            // 发送注册请求
            this.$axios({
                url: `/api/register`,
                method: "post",
                data: {
                    studentId: Number(this.registerForm.studentId),
                    studentName: this.registerForm.studentName,
                    pwd: this.registerForm.pwd,
                    sex: this.registerForm.sex,
                    grade: this.registerForm.grade,
                    major: this.registerForm.major,
                    clazz: this.registerForm.clazz,
                    institute: this.registerForm.institute,
                    tel: this.registerForm.tel || null,
                    email: this.registerForm.email || null,
                },
            })
                .then((res) => {
                    if (res.data.code === 200) {
                        this.$message.success("注册成功，请登录");
                        // 清空注册表单
                        this.registerForm = {
                            studentId: "",
                            studentName: "",
                            pwd: "",
                            confirmPwd: "",
                            sex: "",
                            grade: "",
                            major: "",
                            clazz: "",
                            institute: "",
                            tel: "",
                            email: "",
                        };
                        // 切换到登录页面
                        this.isRegister = false;
                    } else {
                        this.$message.error(res.data.message || "注册失败");
                    }
                })
                .catch((e) => {
                    console.log(e);
                    if (e.response && e.response.data && e.response.data.message) {
                        this.$message.error(e.response.data.message);
                    } else {
                        this.$message.error("注册失败，请检查网络连接");
                    }
                });
        },
        // 加载注册选项
        loadRegisterOptions() {
            // 加载年级选项
            this.$axios.get('/api/options/grades').then(res => {
                if (res.data.code === 200) {
                    this.gradeOptions = res.data.data || [];
                }
            });
            // 加载专业选项
            this.$axios.get('/api/options/majors').then(res => {
                if (res.data.code === 200) {
                    this.majorOptions = res.data.data || [];
                }
            });
            // 加载班级选项
            this.$axios.get('/api/options/classes').then(res => {
                if (res.data.code === 200) {
                    this.classOptions = res.data.data || [];
                }
            });
            // 加载学院选项
            this.$axios.get('/api/options/institutes').then(res => {
                if (res.data.code === 200) {
                    this.instituteOptions = res.data.data || [];
                }
            });
        },
        // 用户登录请求后台处理
        login() {
            if (
                this.formLabelAlign.username == undefined ||
                this.formLabelAlign.username == ""
            ) {
                this.$message("请输入用户名");
                return;
            }
            if (
                !/^\d+$/.test(this.formLabelAlign.username) ||
                this.formLabelAlign.username.toString().length > 18
            ) {
                this.$message("用户名有误");
                return;
            }
            if (this.formLabelAlign.password == "") {
                this.$message("请输入密码");
                return;
            }
            this.$axios({
                url: `/api/login`,
                method: "post",
                data: {
                    ...this.formLabelAlign,
                },
            })
                .then((res) => {
                    let resData = res.data.data;
                    if (resData != null) {
                        switch (resData.role) {
                            case "0": //管理员
                                // 使用sessionStorage存储当前标签页的用户信息（标签页隔离）
                                sessionStorage.setItem("cname", resData.adminName);
                                sessionStorage.setItem("cid", resData.adminId);
                                sessionStorage.setItem("role", "0");
                                // 同时保留Cookie用于兼容性
                                this.$cookies.set("cname", resData.adminName);
                                this.$cookies.set("cid", resData.adminId);
                                this.$cookies.set("role", 0);
                                this.$router.push({ path: "/admin" }); //跳转到管理员页面
                                break;
                            case "1": //教师
                                sessionStorage.setItem("cname", resData.teacherName);
                                sessionStorage.setItem("cid", resData.teacherId);
                                sessionStorage.setItem("role", "1");
                                this.$cookies.set("cname", resData.teacherName);
                                this.$cookies.set("cid", resData.teacherId);
                                this.$cookies.set("role", 1);
                                // 设置教师菜单
                                this.$store.commit('setMenuByRole', 1);
                                this.$router.push({ path: "/index" }); //跳转到教师用户
                                break;
                            case "2": //学生
                                sessionStorage.setItem("cname", resData.studentName);
                                sessionStorage.setItem("cid", resData.studentId);
                                sessionStorage.setItem("role", "2");
                                sessionStorage.setItem("cinstitute", resData.institute || "");
                                sessionStorage.setItem("cmajor", resData.major || "");
                                this.$cookies.set("cname", resData.studentName);
                                this.$cookies.set("cid", resData.studentId);
                                this.$cookies.set("cinstitute", resData.institute || "");
                                this.$cookies.set("cmajor", resData.major || "");
                                this.$router.push({ path: "/student" });
                                break;
                        }
                    }
                    if (resData == null) {
                        //错误提示
                        this.$message({
                            showClose: true,
                            type: "error",
                            message: "用户名或者密码错误",
                        });
                    }
                })
                .catch((e) => {
                    console.log(e);
                    if (
                        e.response == undefined ||
                        e.response.data == undefined
                    ) {
                        this.$message({
                            showClose: true,
                            message: e,
                            type: "error",
                            duration: 5000,
                        });
                    } else {
                        this.$message({
                            showClose: true,
                            message: e.response.data,
                            type: "error",
                            duration: 5000,
                        });
                    }
                });
        },
        clickTag(key) {
            this.role = key;
        },
    },
    computed: mapState(["userInfo"]),
    mounted() {
        // 页面加载时获取注册选项
        this.loadRegisterOptions();
    },
};
</script>

<style lang="less" scoped>

.container {
  height: 100vh;
  overflow: hidden;
  //background-color: #0195ff;
  background-image: url("../../assets/bg6.jpg");
  background-size: 100% 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
}


.remind {
    border-radius: 4px;
    padding: 10px 20px;
    display: flex;
    position: fixed;
    right: 20px;
    bottom: 50%;
    flex-direction: column;
    color: #606266;
    background-color: #fff;
    border-left: 4px solid #409eff;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
}

//.container {
//    margin-bottom: 32px;
//}

.title-icon {
    font-size: 44px;
    margin-right: 10px;
}

.container .el-radio-group {
    margin: 30px 0px;
}

a:link {
    color: #ff962a;
    text-decoration: none;
}

#login {
    font-size: 14px;
    color: #000;
    background-color: #fff;
}

#login .bg {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    overflow-y: auto;
    height: 100%;
    background: url("../../assets/img/77771.jpg") center top / cover no-repeat;
    background-color: #b6bccdd1 !important;
}

#login .main-container {
    display: flex;
    justify-content: center;
    align-items: center;
}

#login .main-container .top {
    margin-top: 100px;
    font-size: 30px;
    color: #ff962a;
    display: flex;
    justify-content: center;
}

#login .top .icon-kaoshi {
    font-size: 80px;
}

#login .top .title {
    margin-top: 20px;
}

#login .bottom {
    display: flex;
    justify-content: center;
    background-color: #fff;
    border-radius: 5px;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
}

#login .bottom .title {
    text-align: center;
    font-size: 30px;
}

.bottom .container .title {
    margin: 30px 0px;
}

.bottom .submit .row-login {
    width: 100%;
    margin: 20px 0px 10px 0px;
    padding: 15px 20px;
    background-color: rgb(133, 174, 191);
    border-color: rgb(133, 174, 191);
    color: white
}

.bottom .submit {
    display: flex;
    justify-content: center;
}

.footer {
    margin-top: 50px;
    text-align: center;
}

.footer .msg1 {
    font-size: 18px;
    color: #fff;
    margin-bottom: 15px;
}

.footer .msg2 {
    font-size: 14px;
    color: #e3e3e3;
    margin-top: 70px;
}

.bottom .options {
    margin-bottom: 40px;
    color: #ff962a;
    display: flex;
    justify-content: space-between;
}

.bottom .options>a {
    color: #ff962a;
}

.bottom .options .register span:nth-child(1) {
    color: #8c8c8c;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
    #login .main-container .top {
        margin-top: 50px;
        font-size: 24px;
    }
    #login .top .icon-kaoshi {
        font-size: 50px;
    }
    #login .top .title {
        margin-top: 10px;
        writing-mode: horizontal-tb;
    }
    #login .bottom {
        margin: 10px;
        width: calc(100% - 20px);
    }
    #login .bottom .title {
        font-size: 24px;
    }
    .bottom .container {
        padding: 10px 20px;
    }
}

@media screen and (max-width: 480px) {
    #login .main-container .top {
        margin-top: 30px;
        font-size: 20px;
        flex-direction: column;
        align-items: center;
    }
    #login .top .icon-kaoshi {
        font-size: 40px;
    }
    #login .bottom .title {
        font-size: 20px;
    }
    .bottom .container {
        padding: 10px 15px;
    }
}

/* 登录页面始终保持浅色风格，不受深色模式影响 */
.dark-theme .container {
    background-image: url("../../assets/bg6.jpg") !important;
    background-color: transparent !important;
}
.dark-theme .container > div {
    background-color: #ffffff !important;
}
.dark-theme .container .el-input__inner {
    background-color: #f5f7fa !important;
    color: #303133 !important;
    border-color: #dcdfe6 !important;
}
</style>
