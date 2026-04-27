<!-- 顶部信息栏 -->
<template>
    <header id="topbar">
        <el-dialog
            :append-to-body="true"
            style="z-index: 2028px"
            title="修改密码"
            :visible.sync="dialogVisible"
            width="30%"
        >
            <el-form
                status-icon
                ref="ruleForm2"
                label-width="100px"
                class="demo-ruleForm"
            >
                <el-form-item label="旧密码" prop="pass">
                    <el-input
                        type="password"
                        v-model="oldPsw"
                        autocomplete="off"
                        show-password
                    ></el-input>
                </el-form-item>
                <el-form-item label="新密码" prop="pass">
                    <el-input
                        type="password"
                        v-model="newPsw"
                        autocomplete="off"
                        show-password
                    ></el-input>
                </el-form-item>
                <el-form-item label="确认新密码" prop="checkPass">
                    <el-input
                        type="password"
                        v-model="confirmNewPsw"
                        autocomplete="off"
                        show-password
                    ></el-input>
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false" style="font-size: 20px;"> 取 消</el-button>
                <el-button type="primary" @click="resetPsw"> 确 定</el-button>
            </span>
        </el-dialog>

        <el-row>
            <el-col :span="6" class="topbar-left">
                <div class="title-container">
                    <img src="../../assets/img/考试.png" alt="" width="50" height="50" class="logo-img">
                    <span class="system-title">{{ systemTitle }}</span>
                </div>
            </el-col>
            <el-col :span="18" class="topbar-right">
                <i @click="toggle()"></i>
                <div class="theme-controls-center">
                    <el-tooltip :content="isDarkMode ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
                        <el-button 
                            @click="toggleTheme" 
                            :icon="isDarkMode ? 'el-icon-sunny' : 'el-icon-moon'"
                            circle 
                            size="medium"
                            class="theme-toggle-btn"
                        ></el-button>
                    </el-tooltip>
                </div>
                <div class="user">
                    <span>{{ user.userName }}</span>
                    <img
                        src="@/assets/img/R-C.jpg"
                        class="user-img"
                        ref="img"
                        @click="showSetting()"
                    />
                    <transition name="fade">
                        <div class="out" ref="out" v-show="login_flag">
                            <ul>
                                <li>
                                    <a
                                        @click="dialogVisible = true"
                                        href="javascript:;"
                                        >修改密码</a
                                    >
                                </li>
                                <li class="exit" @click="exit()">
                                    <a href="javascript:;">退出登录</a>
                                </li>
                            </ul>
                        </div>
                    </transition>
                </div>
            </el-col>
        </el-row>
    </header>
</template>

<script>
import { mapState, mapMutations } from "vuex";
export default {
    data() {
        return {
            login_flag: false,
            user: {
                //用户信息
                userName: null,
                userId: null,
            },
            dialogVisible: false,
            oldPsw: "",
            newPsw: "",
            confirmNewPsw: "",
            role: 0,
            isDarkMode: false, // 主题模式状态
        };
    },
    created() {
        this.getUserInfo();
        this.role = this.$cookies.get("role");
        this.initTheme(); // 初始化主题
    },
    computed: {
        ...mapState(["flag", "menu"]),
        // 根据用户角色动态显示系统标题
        systemTitle() {
            const role = sessionStorage.getItem("role") || this.$cookies.get("role") || this.role;
            console.log('当前用户角色:', role);
            
            switch(role) {
                case '0': // 管理员
                    return '在线考试系统（管理员）';
                case '1': // 教师
                    return '在线考试系统（教师端）';
                case '2': // 学生
                    return '在线考试系统（学生端）';
                default:
                    return '在线考试系统';
            }
        }
    },
    methods: {
        // 管理员重置密码
        resetPsw() {
            if (this.oldPsw == "") {
                this.$message("请输入旧密码");
                return;
            }
            if (this.newPsw == "") {
                this.$message("请输入新密码");
                return;
            }
            if (this.confirmNewPsw != this.newPsw) {
                this.$message("两次新密码不一致");
                return;
            }
            this.$axios({
                url: `/api/admin/resetPsw`,
                method: "post",
                data: {
                    oldPsw: this.oldPsw,
                    newPsw: this.newPsw,
                },
            }).then((res) => {
                let status = res.data.code;
                if (status == 200) {
                    if (res.data.data != true) {
                        this.$message(res.data.data);
                    } else {
                        // 修改成功
                        this.$message("修改成功");
                        this.dialogVisible = false;
                        this.oldPsw = "";
                        this.newPsw = "";
                        this.confirmNewPsw = "";
                    }
                }
            });
        },
        //显示、隐藏退出按钮
        showSetting() {
            this.login_flag = !this.login_flag;
        },
        //左侧栏放大缩小
        ...mapMutations(["toggle"]),
        getUserInfo() {
            //获取用户信息 - 优先从sessionStorage获取（标签页隔离）
            let userName = sessionStorage.getItem("cname") || this.$cookies.get("cname");
            let userId = sessionStorage.getItem("cid") || this.$cookies.get("cid");
            this.user.userName = userName;
            this.user.userId = userId;
        },
        index() {
            this.$router.push({ path: "/index" });
        },
        exit() {
            let role = sessionStorage.getItem("role") || this.$cookies.get("role");
            this.$router.push({ path: "/" }); //跳转到登录页面
            // 清除sessionStorage
            sessionStorage.removeItem("cname");
            sessionStorage.removeItem("cid");
            sessionStorage.removeItem("role");
            // 清除cookie
            this.$cookies.remove("cname");
            this.$cookies.remove("cid");
            this.$cookies.remove("role");
            this.$cookies.remove("rb_token");
            this.$cookies.remove("rb_role");
            if (role == 0 || role == "0") {
                this.menu.pop();
            }
        },
        // 初始化主题
        initTheme() {
            const savedTheme = localStorage.getItem('theme-mode');
            this.isDarkMode = savedTheme === 'dark';
            this.applyTheme();
        },
        // 切换主题
        toggleTheme() {
            this.isDarkMode = !this.isDarkMode;
            this.applyTheme();
            localStorage.setItem('theme-mode', this.isDarkMode ? 'dark' : 'light');
            this.$message.success(this.isDarkMode ? '已切换到深色模式' : '已切换到浅色模式');
        },
        // 应用主题
        applyTheme() {
            const body = document.body;
            if (this.isDarkMode) {
                body.classList.add('dark-theme');
                body.classList.remove('light-theme');
            } else {
                body.classList.add('light-theme');
                body.classList.remove('dark-theme');
            }
            // 发射全局主题变化事件
            window.dispatchEvent(new CustomEvent('theme-changed', { detail: { isDark: this.isDarkMode } }));
        },
    },
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.5s;
}
.fade-enter, .fade-leave-to /* .fade-leave-active below version 2.1.8 */ {
    opacity: 0;
}
#topbar {
    position: relative;
    z-index: 10;
    background-color: #2384d9;
    height: 80px;
    line-height: 80px;
    color: #fff;
    box-shadow: 5px 0px 10px rgba(0, 0, 0, 0.5);
}
#topbar .topbar-left {
    height: 80px;
    display: flex;
    align-items: center;
    color: #fff;
    padding-left: 20px;
}

.title-container {
    display: flex;
    align-items: center;
    width: 100%;
    min-width: 0; /* 允许容器缩小 */
}

.logo-img {
    margin-top: 15px;
    margin-right: 12px;
    flex-shrink: 0; /* 防止图片缩小 */
}

.system-title {
    font-size: 22px;
    font-weight: 400;
    color: white;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    min-width: 0;
    flex: 1;
}

/* 系统标题文字在头部导航栏中始终显示为白色 */
.system-title-text {
    font-size: 28px;
    font-weight: 400;
    color: white !important;
    white-space: nowrap;
}

.topbar-left .icon-kaoshi {
    font-size: 60px;
}
.topbar-left .title {
    font-size: 20px;
    cursor: pointer;
}
.topbar-right {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    position: relative;
}
.topbar-right .user-img {
    width: 50px;
    height: 50px;
    border-radius: 50%;
}
.topbar-right .el-icon-menu {
    font-size: 30px;
    margin-left: 20px;
}
.topbar-right .user {
    position: relative;
    margin-right: 40px;
    display: flex;
}
.topbar-right .user .user-img {
    margin-top: 15px;
    margin-left: 10px;
    cursor: pointer;
}
.user .out {
    font-size: 14px;
    position: absolute;
    top: 80px;
    right: 0px;
    background-color: #fff;
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19);
    padding: 12px;
}
.user .out ul {
    list-style: none;
}
.user .out ul > li {
    height: 26px;
    line-height: 26px;
}
.out a {
    text-decoration: none;
    color: #000 !important;
}
.out ul li {
    color: #000 !important;
}
.out ul li a {
    color: #000 !important;
}
.out a:hover {
    color: #409eff !important;
}
.out .exit {
    margin-top: 4px;
    padding-top: 4px;
    border-top: 1px solid #ccc;
}
.theme-controls-center {
    position: fixed;
    left: 50%;
    top: 15px;
    transform: translateX(-50%);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
}
.theme-toggle-btn {
    background-color: rgba(255, 255, 255, 0.1) !important;
    border-color: rgba(255, 255, 255, 0.3) !important;
    color: white !important;
    transition: all 0.3s ease;
}
.theme-toggle-btn:hover {
    background-color: rgba(255, 255, 255, 0.2) !important;
    border-color: rgba(255, 255, 255, 0.5) !important;
    transform: scale(1.1);
}
</style>
