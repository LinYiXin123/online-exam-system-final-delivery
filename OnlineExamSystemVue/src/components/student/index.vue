<!--学生考试首页-->

<template>
    <div id="student">
        <!-- 顶部导航栏 -->
        <el-row class="top-header" :style="{ background: currentThemeColor }">
            <el-col :span="24">
                <div class="header-content">
                    <div class="header-left">
                        <img src="../../assets/img/考试.png" class="logo-img" />
                        <span class="logo-text">在线考试系统（学生端）</span>
                    </div>
                    <!-- 调色板按钮 -->
                    <div class="theme-palette">
                        <el-popover
                            placement="bottom"
                            width="280"
                            trigger="click"
                            popper-class="theme-popover">
                            <div class="theme-panel">
                                <div class="theme-title">界面主题</div>
                                <div class="theme-colors">
                                    <div 
                                        v-for="theme in themes" 
                                        :key="theme.name"
                                        :class="['theme-color-item', { active: currentTheme === theme.name }]"
                                        :style="{ background: theme.primary }"
                                        @click="changeTheme(theme.name)"
                                        :title="theme.label">
                                        <i v-if="currentTheme === theme.name" class="el-icon-check"></i>
                                    </div>
                                </div>
                                <div class="theme-name">{{ currentThemeLabel }}</div>
                            </div>
                            <div slot="reference" class="palette-btn">
                                <i class="el-icon-brush"></i>
                            </div>
                        </el-popover>
                    </div>
                    
                    <!-- 消息通知图标 -->
                    <div class="notification-wrapper">
                        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                            <el-button icon="el-icon-bell" circle class="notification-btn" @click="showNotifications"></el-button>
                        </el-badge>
                    </div>
                    
                    <div class="header-right" @mouseenter="flag = true" @mouseleave="flag = false">
                        <el-avatar shape="square" :size="40" :src="require('@/assets/img/avatar.jpg')"></el-avatar>
                        <div class="user-info">
                            <span class="user-name">{{ user.userName }}</span>
                            <i class="el-icon-arrow-down"></i>
                        </div>
                        <transition name="dropdown">
                            <div class="dropdown-menu" v-if="flag">
                                <div class="menu-item" @click="showProfile()">
                                    <i class="el-icon-user"></i>
                                    <span>个人信息</span>
                                </div>
                                <div class="menu-item" @click="manage()">
                                    <i class="el-icon-setting"></i>
                                    <span>修改密码</span>
                                </div>
                                <div class="menu-divider"></div>
                                <div class="menu-item exit" @click="exit()">
                                    <i class="el-icon-switch-button"></i>
                                    <span>退出登录</span>
                                </div>
                            </div>
                        </transition>
                    </div>
                </div>
            </el-col>
        </el-row>
        
        <!-- 主体区域：左侧导航 + 右侧内容 -->
        <div class="main-container">
            <!-- 左侧导航栏 -->
            <student-left ref="studentLeft" :theme-color="currentThemeColorDark" :theme-color-dark="currentThemeColor" @collapse-change="handleCollapseChange"></student-left>
            
            <!-- 右侧内容区域 -->
            <div class="content-area" :class="{ 'collapsed': isCollapsed }">
                <student-top-nav></student-top-nav>
                <div class="page-content">
                    <router-view></router-view>
                </div>
                <v-footer></v-footer>
            </div>
        </div>
        
        <!-- 个人信息弹窗 -->
        <el-dialog title="个人信息" :visible.sync="profileDialogVisible" width="500px" class="profile-dialog" :show-close="false">
            <div class="profile-header" :style="{ backgroundColor: currentThemeColor }">
                <span>个人信息</span>
                <i class="el-icon-close" @click="profileDialogVisible = false"></i>
            </div>
            <el-descriptions :column="1" border class="profile-descriptions">
                <el-descriptions-item label="学号">{{ profileInfo.studentId }}</el-descriptions-item>
                <el-descriptions-item label="姓名">{{ profileInfo.studentName }}</el-descriptions-item>
                <el-descriptions-item label="性别">{{ profileInfo.sex }}</el-descriptions-item>
                <el-descriptions-item label="年级">{{ profileInfo.grade || '未填写' }}</el-descriptions-item>
                <el-descriptions-item label="专业">{{ profileInfo.major || '未填写' }}</el-descriptions-item>
                <el-descriptions-item label="班级">{{ profileInfo.clazz || '未填写' }}</el-descriptions-item>
                <el-descriptions-item label="学院">{{ profileInfo.institute || '未填写' }}</el-descriptions-item>
                <el-descriptions-item label="电话">
                    <template v-if="!isEditingProfile">
                        {{ profileInfo.tel || '未填写' }}
                    </template>
                    <el-input v-else v-model="editProfileForm.tel" size="small" placeholder="请输入电话"></el-input>
                </el-descriptions-item>
                <el-descriptions-item label="邮箱">
                    <template v-if="!isEditingProfile">
                        {{ profileInfo.email || '未填写' }}
                    </template>
                    <el-input v-else v-model="editProfileForm.email" size="small" placeholder="请输入邮箱"></el-input>
                </el-descriptions-item>
            </el-descriptions>
            <span slot="footer" class="dialog-footer">
                <template v-if="isEditingProfile">
                    <el-button @click="cancelEditProfile">取消</el-button>
                    <el-button type="primary" @click="saveProfile" :loading="saveProfileLoading">保存</el-button>
                </template>
                <template v-else>
                    <el-button @click="startEditProfile">编辑信息</el-button>
                    <el-button type="primary" @click="profileDialogVisible = false">关闭</el-button>
                </template>
            </span>
        </el-dialog>
        
        <!-- 消息通知弹窗 -->
        <el-dialog title="消息通知" :visible.sync="notificationDialogVisible" width="450px" class="notification-dialog">
            <div class="notification-header">
                <span>共 {{ notifications.length }} 条消息</span>
                <el-button type="text" @click="markAllRead" v-if="unreadCount > 0">全部已读</el-button>
            </div>
            <div class="notification-list" v-if="notifications.length > 0">
                <div 
                    v-for="item in notifications" 
                    :key="item.id" 
                    class="notification-item"
                    :class="{ unread: item.isRead === 0 }"
                    @click="handleNotificationClick(item)">
                    <div class="notification-icon">
                        <i class="el-icon-chat-dot-round"></i>
                    </div>
                    <div class="notification-content">
                        <div class="notification-title">
                            <span class="sender-name">{{ item.senderName }}</span>
                            <span class="notification-type">回复了你</span>
                        </div>
                        <div class="notification-text">{{ item.content }}</div>
                        <div class="notification-time">{{ item.createTime }}</div>
                    </div>
                    <div class="notification-dot" v-if="item.isRead === 0"></div>
                </div>
            </div>
            <div class="notification-empty" v-else>
                <i class="el-icon-message"></i>
                <p>暂无消息</p>
            </div>
        </el-dialog>
    </div>
</template>

<script>
import myFooter from "@/components/student/myFooter";
import StudentLeft from "@/components/student/studentLeft";
import StudentTopNav from "@/components/student/studentTopNav";
import { mapState } from "vuex";
export default {
    components: {
        "v-footer": myFooter,
        "student-left": StudentLeft,
        "student-top-nav": StudentTopNav,
    },
    data() {
        return {
            flag: false,
            user: {},
            profileDialogVisible: false,
            profileInfo: {},
            isEditingProfile: false,
            saveProfileLoading: false,
            editProfileForm: {
                tel: '',
                email: ''
            },
            isCollapsed: false,
            currentTheme: 'blue',
            themes: [
                { name: 'blue', label: '经典蓝', primary: '#2384d6' },
                { name: 'green', label: '清新绿', primary: '#42b983' },
                { name: 'purple', label: '优雅紫', primary: '#7c3aed' },
                { name: 'orange', label: '活力橙', primary: '#f97316' },
                { name: 'red', label: '热情红', primary: '#ef4444' },
                { name: 'teal', label: '青碧色', primary: '#14b8a6' },
                { name: 'pink', label: '浪漫粉', primary: '#ec4899' },
                { name: 'indigo', label: '靛青蓝', primary: '#6366f1' }
            ],
            // 通知相关
            notificationDialogVisible: false,
            notifications: [],
            unreadCount: 0
        };
    },
    computed: {
        currentThemeColor() {
            return getComputedStyle(document.documentElement).getPropertyValue('--primary-color').trim() || '#2384d6';
        }
    },
    created() {
        this.userInfo();
        this.initTheme();
        this.loadUnreadCount();
        // 每30秒刷新未读数量
        this.notificationTimer = setInterval(() => {
            this.loadUnreadCount();
        }, 30000);
    },
    methods: {
        exit() {
            //退出登录
            this.$router.push({ path: "/" }); //跳转到登录页面
            // 清除sessionStorage
            sessionStorage.removeItem("cname");
            sessionStorage.removeItem("cid");
            sessionStorage.removeItem("role");
            // 清除cookie
            this.$cookies.remove("cname");
            this.$cookies.remove("cid");
            this.$cookies.remove("rb_token");
            this.$cookies.remove("rb_role");
        },
        manage() {
            //跳转到修改密码页面
            this.$router.push({ path: "/manager" });
        },
        showProfile() {
            // 获取学生详细信息 - 优先从sessionStorage获取
            let studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid");
            console.log("获取个人信息，studentId:", studentId, "类型:", typeof studentId);
            if (!studentId) {
                this.$message.error("未找到学生ID，请重新登录");
                return;
            }
            const requestUrl = `/api/student/${studentId}`;
            console.log("请求URL:", requestUrl);
            this.$axios.get(requestUrl).then(res => {
                console.log("完整响应对象:", res);
                console.log("res.status:", res.status);
                console.log("res.data:", res.data);
                console.log("个人信息完整响应:", JSON.stringify(res.data));
                console.log("res.data.code:", res.data ? res.data.code : "res.data为空");
                console.log("res.data.data:", res.data ? res.data.data : "res.data为空");
                if (res.data && res.data.code == 200 && res.data.data) {
                    this.profileInfo = res.data.data;
                    this.profileDialogVisible = true;
                } else {
                    this.$message.error("获取个人信息失败: " + (res.data.msg || "未知错误"));
                }
            }).catch(err => {
                console.log("获取个人信息错误:", err);
                this.$message.error("获取个人信息失败: " + (err.message || "网络错误"));
            });
        },
        // 开始编辑个人信息
        startEditProfile() {
            this.editProfileForm.tel = this.profileInfo.tel || '';
            this.editProfileForm.email = this.profileInfo.email || '';
            this.isEditingProfile = true;
        },
        // 取消编辑
        cancelEditProfile() {
            this.isEditingProfile = false;
        },
        // 保存个人信息
        saveProfile() {
            this.saveProfileLoading = true;
            const studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid");
            this.$axios({
                url: `/api/student/${studentId}`,
                method: 'put',
                data: {
                    tel: this.editProfileForm.tel,
                    email: this.editProfileForm.email
                }
            }).then(res => {
                if (res.data && res.data.code == 200) {
                    this.profileInfo.tel = this.editProfileForm.tel;
                    this.profileInfo.email = this.editProfileForm.email;
                    this.isEditingProfile = false;
                    this.$message.success('保存成功');
                } else {
                    this.$message.error('保存失败: ' + (res.data.msg || '未知错误'));
                }
            }).catch(err => {
                this.$message.error('保存失败: ' + (err.message || '网络错误'));
            }).finally(() => {
                this.saveProfileLoading = false;
            });
        },
        userInfo() {
            // 优先从sessionStorage获取（标签页隔离），否则从Cookie获取
            let studentName = sessionStorage.getItem("cname") || this.$cookies.get("cname");
            let studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid");
            console.log(`studentId${studentId}`);
            console.log(`studentName ${studentName}`);
            this.user.userName = studentName;
            this.user.studentId = studentId;
        },
        // 加载未读通知数量
        loadUnreadCount() {
            const studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid");
            if (!studentId) return;
            this.$axios.get(`/api/notification/unread/${studentId}`).then(res => {
                if (res.data && res.data.code === 200) {
                    this.unreadCount = res.data.data.count || 0;
                }
            }).catch(() => {});
        },
        // 显示通知列表
        showNotifications() {
            this.notificationDialogVisible = true;
            this.loadNotifications();
        },
        // 加载通知列表
        loadNotifications() {
            const studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid");
            if (!studentId) return;
            this.$axios.get(`/api/notification/${studentId}`).then(res => {
                if (res.data && res.data.code === 200) {
                    this.notifications = res.data.data || [];
                }
            });
        },
        // 点击通知
        handleNotificationClick(item) {
            if (item.isRead === 0) {
                this.$axios.put(`/api/notification/read/${item.id}`).then(() => {
                    item.isRead = 1;
                    this.unreadCount = Math.max(0, this.unreadCount - 1);
                });
            }
            // 跳转到交流区
            this.$router.push('/message');
            this.notificationDialogVisible = false;
        },
        // 全部标记已读
        markAllRead() {
            const studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid");
            this.$axios.put(`/api/notification/readAll/${studentId}`).then(() => {
                this.notifications.forEach(item => item.isRead = 1);
                this.unreadCount = 0;
                this.$message.success('已全部标记为已读');
            });
        },
        practice() {
            //跳转练习模式
            let isPractice = true;
            this.$store.commit("practice", isPractice);
            this.$router.push({ path: "/startExam" });
        },
        exam() {
            //跳转考试模式
            let isPractice = false;
            this.$store.commit("practice", isPractice);
            this.$router.push({ path: "/student" });
        },
        handleCollapseChange(collapsed) {
            this.isCollapsed = collapsed;
        },
        // 初始化主题
        initTheme() {
            const savedTheme = localStorage.getItem('student-theme') || 'blue';
            this.currentTheme = savedTheme;
            this.applyTheme(savedTheme);
        },
        // 切换主题
        changeTheme(themeName) {
            this.currentTheme = themeName;
            localStorage.setItem('student-theme', themeName);
            this.applyTheme(themeName);
            this.$message({
                message: `已切换至${this.currentThemeLabel}主题`,
                type: 'success',
                duration: 1500
            });
        },
        // 应用主题
        applyTheme(themeName) {
            const theme = this.themes.find(t => t.name === themeName);
            if (theme) {
                document.documentElement.style.setProperty('--primary-color', theme.primary);
                document.documentElement.style.setProperty('--primary-light', this.lightenColor(theme.primary, 0.9));
                document.documentElement.style.setProperty('--primary-dark', this.darkenColor(theme.primary, 0.2));
            }
        },
        // 颜色变亮
        lightenColor(hex, percent) {
            const num = parseInt(hex.replace('#', ''), 16);
            const amt = Math.round(2.55 * percent * 100);
            const R = Math.min(255, (num >> 16) + amt);
            const G = Math.min(255, ((num >> 8) & 0x00FF) + amt);
            const B = Math.min(255, (num & 0x0000FF) + amt);
            return '#' + (0x1000000 + R * 0x10000 + G * 0x100 + B).toString(16).slice(1);
        },
        // 颜色变暗
        darkenColor(hex, percent) {
            const num = parseInt(hex.replace('#', ''), 16);
            const amt = Math.round(2.55 * percent * 100);
            const R = Math.max(0, (num >> 16) - amt);
            const G = Math.max(0, ((num >> 8) & 0x00FF) - amt);
            const B = Math.max(0, (num & 0x0000FF) - amt);
            return '#' + (0x1000000 + R * 0x10000 + G * 0x100 + B).toString(16).slice(1);
        },
    },
    computed: {
        ...mapState(["isPractice"]),
        currentThemeLabel() {
            const theme = this.themes.find(t => t.name === this.currentTheme);
            return theme ? theme.label : '经典蓝';
        },
        currentThemeColor() {
            const theme = this.themes.find(t => t.name === this.currentTheme);
            return theme ? theme.primary : '#2384d6';
        },
        currentThemeColorDark() {
            const theme = this.themes.find(t => t.name === this.currentTheme);
            return theme ? this.darkenColor(theme.primary, 0.2) : '#1a6cb3';
        }
    },
};
</script>

<style scoped>
#student {
    min-height: 100vh;
    background-color: #f5f7fa;
}

/* 顶部导航栏 - 使用深色主题色 */
.top-header {
    background: var(--primary-dark, #1a6cb3);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    position: sticky;
    top: 0;
    z-index: 100;
}

.header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 60px;
    padding: 0 24px;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    flex: 1;
}

/* 调色板按钮 */
.theme-palette {
    margin-right: 16px;
}

.palette-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 8px;
    background: #fff;
    cursor: pointer;
    transition: all 0.3s;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.palette-btn:hover {
    background: rgba(255, 255, 255, 0.9);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.palette-btn i {
    font-size: 18px;
    color: var(--primary-color, #2384d6);
}

.logo-img {
    width: 36px;
    height: 36px;
    -webkit-user-drag: none;
}

.logo-text {
    font-size: 20px;
    font-weight: 600;
    color: #fff !important;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    padding: 8px 12px;
    border-radius: 8px;
    transition: all 0.3s;
    position: relative;
}

.header-right:hover {
    background: rgba(255, 255, 255, 0.1);
}

.user-info {
    display: flex;
    align-items: center;
    gap: 6px;
}

.user-name {
    color: #fff !important;
    font-size: 14px;
    font-weight: 500;
}

.user-info i {
    color: rgba(255, 255, 255, 0.8);
    font-size: 12px;
    transition: transform 0.3s;
}

.header-right:hover .user-info i {
    transform: rotate(180deg);
}

/* 下拉菜单 */
.dropdown-menu {
    position: absolute;
    top: 100%;
    right: 0;
    margin-top: 8px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    min-width: 160px;
    overflow: hidden;
    z-index: 1000;
}

.menu-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    color: #333;
    font-size: 14px;
    transition: all 0.2s;
    cursor: pointer;
}

.menu-item i {
    font-size: 16px;
    color: #666;
}

.menu-item:hover {
    background: #f5f7fa;
    color: #409EFF;
}

.menu-item:hover i {
    color: #409EFF;
}

.menu-item.exit:hover {
    background: #fff5f5;
    color: #f56c6c;
}

.menu-item.exit:hover i {
    color: #f56c6c;
}

.menu-divider {
    height: 1px;
    background: #eee;
    margin: 4px 0;
}

/* 下拉动画 */
.dropdown-enter-active,
.dropdown-leave-active {
    transition: all 0.2s ease;
}

.dropdown-enter,
.dropdown-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}

/* 主体容器 */
.main-container {
    display: flex;
    min-height: calc(100vh - 60px);
}

/* 内容区域 */
.content-area {
    flex: 1;
    margin-left: 220px;
    background: #f5f7fa;
    min-height: calc(100vh - 60px);
    display: flex;
    flex-direction: column;
    transition: margin-left 0.3s ease;
    width: calc(100% - 220px);
    overflow-x: hidden;
}

.content-area.collapsed {
    margin-left: 64px;
    width: calc(100% - 64px);
}

.page-content {
    flex: 1;
    padding: 15px 20px;
    overflow-y: auto;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
    .logo-text {
        display: none;
    }
    
    .header-content {
        padding: 0 12px;
    }
}
</style>

<!-- 全局样式用于主题面板 -->
<style>
/* 主题面板样式 */
.theme-popover {
    border-radius: 12px !important;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15) !important;
    border: none !important;
}

.theme-panel {
    padding: 8px;
}

.theme-title {
    font-size: 14px;
    font-weight: 600;
    color: #333;
    margin-bottom: 12px;
    text-align: center;
}

.theme-colors {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
    margin-bottom: 12px;
}

.theme-color-item {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    border: 3px solid transparent;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.theme-color-item:hover {
    transform: scale(1.1);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.theme-color-item.active {
    border-color: #fff;
    box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.3), 0 4px 12px rgba(0, 0, 0, 0.2);
}

.theme-color-item i {
    color: #fff;
    font-size: 18px;
    font-weight: bold;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.theme-name {
    text-align: center;
    font-size: 13px;
    color: #666;
    padding-top: 8px;
    border-top: 1px solid #eee;
}

/* 全局主题色覆盖 - Element UI 组件 */

/* 主要按钮 */
.el-button--primary {
    background-color: var(--primary-color, #2384d6) !important;
    border-color: var(--primary-color, #2384d6) !important;
}

.el-button--primary:hover,
.el-button--primary:focus {
    background-color: var(--primary-dark, #1a6cb3) !important;
    border-color: var(--primary-dark, #1a6cb3) !important;
}

/* 链接按钮 */
.el-button--text {
    color: var(--primary-color, #2384d6) !important;
}

.el-button--text:hover {
    color: var(--primary-dark, #1a6cb3) !important;
}

/* 分页器 */
.el-pagination .el-pager li.active {
    background-color: var(--primary-color, #2384d6) !important;
    color: #fff !important;
}

.el-pagination .el-pager li:hover {
    color: var(--primary-color, #2384d6) !important;
}

.el-pagination button:hover {
    color: var(--primary-color, #2384d6) !important;
}

/* 标签页 */
.el-tabs__item.is-active {
    color: var(--primary-color, #2384d6) !important;
}

.el-tabs__item:hover {
    color: var(--primary-color, #2384d6) !important;
}

.el-tabs__active-bar {
    background-color: var(--primary-color, #2384d6) !important;
}

/* 标签 */
.el-tag--primary {
    background-color: var(--primary-light, #ecf5ff) !important;
    border-color: var(--primary-color, #2384d6) !important;
    color: var(--primary-color, #2384d6) !important;
}

/* 链接 */
.el-link--primary {
    color: var(--primary-color, #2384d6) !important;
}

.el-link--primary:hover {
    color: var(--primary-dark, #1a6cb3) !important;
}

/* 复选框 */
.el-checkbox__input.is-checked .el-checkbox__inner {
    background-color: var(--primary-color, #2384d6) !important;
    border-color: var(--primary-color, #2384d6) !important;
}

.el-checkbox__input.is-checked + .el-checkbox__label {
    color: var(--primary-color, #2384d6) !important;
}

/* 单选框 */
.el-radio__input.is-checked .el-radio__inner {
    background-color: var(--primary-color, #2384d6) !important;
    border-color: var(--primary-color, #2384d6) !important;
}

.el-radio__input.is-checked + .el-radio__label {
    color: var(--primary-color, #2384d6) !important;
}

/* 开关 */
.el-switch.is-checked .el-switch__core {
    background-color: var(--primary-color, #2384d6) !important;
    border-color: var(--primary-color, #2384d6) !important;
}

/* 输入框聚焦 */
.el-input__inner:focus {
    border-color: var(--primary-color, #2384d6) !important;
}

.el-textarea__inner:focus {
    border-color: var(--primary-color, #2384d6) !important;
}

/* 选择框 */
.el-select .el-input.is-focus .el-input__inner {
    border-color: var(--primary-color, #2384d6) !important;
}

/* 下拉菜单选中项 */
.el-select-dropdown__item.selected {
    color: var(--primary-color, #2384d6) !important;
}

/* 日期选择器 */
.el-date-picker__header-label.active,
.el-date-picker__header-label:hover {
    color: var(--primary-color, #2384d6) !important;
}

.el-picker-panel__shortcut:hover {
    color: var(--primary-color, #2384d6) !important;
}

.el-date-table td.current:not(.disabled) span {
    background-color: var(--primary-color, #2384d6) !important;
}

.el-date-table td.today span {
    color: var(--primary-color, #2384d6) !important;
}

/* 进度条 */
.el-progress-bar__inner {
    background-color: var(--primary-color, #2384d6) !important;
}

/* 消息提示 */
.el-message--primary .el-message__content {
    color: var(--primary-color, #2384d6) !important;
}

/* 菜单激活项 */
#student-left .el-menu-item.is-active {
    background-color: rgba(255, 255, 255, 0.2) !important;
}

#student-left .el-submenu.is-active > .el-submenu__title {
    color: #fff !important;
}

/* 面包屑链接 */
.el-breadcrumb__inner a:hover,
.el-breadcrumb__inner.is-link:hover {
    color: var(--primary-color, #2384d6) !important;
}

/* 表格排序图标 */
.el-table .sort-caret.ascending {
    border-bottom-color: var(--primary-color, #2384d6) !important;
}

.el-table .sort-caret.descending {
    border-top-color: var(--primary-color, #2384d6) !important;
}

/* 加载动画 */
.el-loading-spinner .circular .path {
    stroke: var(--primary-color, #2384d6) !important;
}

/* 步骤条 */
.el-step__head.is-finish {
    color: var(--primary-color, #2384d6) !important;
    border-color: var(--primary-color, #2384d6) !important;
}

.el-step__title.is-finish {
    color: var(--primary-color, #2384d6) !important;
}

.el-step__head.is-process {
    color: var(--primary-color, #2384d6) !important;
    border-color: var(--primary-color, #2384d6) !important;
}

/* 折叠面板激活 */
.el-collapse-item__header.is-active {
    color: var(--primary-color, #2384d6) !important;
}

/* 折叠后弹出菜单样式修复 */
.el-menu--vertical .el-menu--popup {
    background-color: var(--primary-color, #2384d6) !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item {
    background-color: var(--primary-color, #2384d6) !important;
    color: #fff !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item:hover,
.el-menu--vertical .el-menu--popup .el-menu-item:focus {
    background-color: rgba(255, 255, 255, 0.2) !important;
}

/* 折叠后submenu标题悬停 */
.el-menu--collapse .el-submenu .el-submenu__title:hover {
    background-color: inherit !important;
}

/* 强制覆盖 Element UI 的悬停样式 - 全局级别 */
#student-left .el-submenu__title:hover,
#student-left .el-submenu__title:focus,
#student-left .el-menu-item:hover,
#student-left .el-menu-item:focus,
.el-menu .el-submenu__title:hover,
.el-menu .el-submenu__title:focus {
    background-color: inherit !important;
}

/* 覆盖 Element UI 计算后的内联样式 */
[style*="background"] .el-submenu__title:hover {
    background-color: inherit !important;
}

/* 使用 CSS 变量覆盖 Element UI 的悬停颜色 */
.el-menu {
    --el-menu-hover-bg-color: transparent !important;
}

/* 强制所有菜单项悬停时保持原色 */
.el-menu .el-submenu__title,
.el-menu .el-menu-item {
    transition: none !important;
}

.el-menu .el-submenu__title:hover,
.el-menu .el-menu-item:hover {
    background: inherit !important;
    background-color: inherit !important;
}

/* 针对学生端菜单的特殊处理 */
#student-left * {
    transition: none !important;
}

#student-left .el-submenu__title[style],
#student-left .el-menu-item[style] {
    background-color: inherit !important;
}

/* MessageBox 确认按钮样式 - 使用普通样式 */
.el-message-box .el-button--primary {
    background-color: #fff !important;
    border-color: #dcdfe6 !important;
    color: #606266 !important;
}

.el-message-box .el-button--primary:hover {
    background-color: #f5f7fa !important;
    border-color: #c0c4cc !important;
    color: #606266 !important;
}

/* 个人信息弹窗样式 */
.profile-dialog .el-dialog {
    border-radius: 16px !important;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15) !important;
}

.profile-dialog .el-dialog__header {
    display: none !important;
}

.profile-dialog .el-dialog__body {
    padding: 0 !important;
}

.profile-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    border-radius: 16px 16px 0 0;
}

.profile-header span {
    color: #fff !important;
}

.profile-header i {
    cursor: pointer;
    font-size: 20px;
    color: #fff;
    transition: all 0.3s ease;
}

.profile-header i:hover {
    opacity: 0.8;
    transform: rotate(90deg);
}

.profile-descriptions {
    padding: 24px;
}

.profile-dialog .el-descriptions-item__label {
    font-weight: 600;
    color: #606266;
}

.profile-dialog .el-descriptions-item__content {
    color: #303133;
}

.profile-dialog .el-descriptions--border .el-descriptions-item__cell {
    padding: 14px 20px;
}

.profile-dialog .dialog-footer {
    display: flex;
    justify-content: center;
    padding: 20px 24px;
    border-top: 1px solid #f0f0f0;
    gap: 12px;
}

.profile-dialog .dialog-footer .el-button {
    min-width: 100px;
    height: 40px;
    border-radius: 20px;
    font-weight: 500;
    transition: all 0.3s ease;
}

.profile-dialog .dialog-footer .el-button:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.profile-dialog .dialog-footer .el-button--primary {
    background-color: var(--primary-color, #409EFF) !important;
    border-color: var(--primary-color, #409EFF) !important;
}

.profile-dialog .dialog-footer .el-button--default {
    border: 2px solid var(--primary-color, #409EFF) !important;
    color: var(--primary-color, #409EFF) !important;
    background: #fff !important;
}

/* 通知按钮样式 */
.notification-wrapper {
    margin-right: 16px;
}

.notification-btn {
    background: #fff !important;
    border: none !important;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.notification-btn i {
    color: var(--primary-color, #409EFF);
    font-size: 18px;
}

/* 通知弹窗样式 */
.notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid #eee;
    margin-bottom: 12px;
}

.notification-list {
    max-height: 400px;
    overflow-y: auto;
}

.notification-item {
    display: flex;
    padding: 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: background 0.3s;
    position: relative;
}

.notification-item:hover {
    background: #f5f7fa;
}

.notification-item.unread {
    background: rgba(64, 158, 255, 0.05);
}

.notification-icon {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: var(--primary-color, #409EFF);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
}

.notification-icon i {
    color: #fff;
    font-size: 18px;
}

.notification-content {
    flex: 1;
}

.notification-title {
    margin-bottom: 4px;
}

.sender-name {
    font-weight: 600;
    color: #333;
}

.notification-type {
    color: #666;
    margin-left: 4px;
}

.notification-text {
    color: #666;
    font-size: 13px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 280px;
}

.notification-time {
    color: #999;
    font-size: 12px;
    margin-top: 4px;
}

.notification-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #f56c6c;
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
}

.notification-empty {
    text-align: center;
    padding: 40px 0;
    color: #999;
}

.notification-empty i {
    font-size: 48px;
    margin-bottom: 12px;
}
</style>
