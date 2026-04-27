<template>
  <div id="admin-main">
    <!-- 顶部导航栏 -->
    <div class="admin-header" :style="{ background: currentThemeColor }">
      <div class="header-left">
        <i class="el-icon-s-platform logo-icon"></i>
        <span class="logo-text">在线考试系统（管理员）</span>
      </div>
      <div class="header-right">
        <!-- 主题调色板 -->
        <div class="theme-palette">
          <el-popover placement="bottom" width="280" trigger="click" popper-class="theme-popover">
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
              <i class="el-icon-brush" :style="{ color: currentThemeColor }"></i>
            </div>
          </el-popover>
        </div>
        <!-- 用户信息 -->
        <div class="user-info">
          <el-dropdown trigger="click">
            <span class="user-dropdown">
              <el-avatar :size="36" icon="el-icon-user-solid" :style="{ backgroundColor: '#fff', color: currentThemeColor }"></el-avatar>
              <span class="username">{{ adminName }}</span>
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="showPasswordDialog">
                <i class="el-icon-key"></i> 修改密码
              </el-dropdown-item>
              <el-dropdown-item divided @click.native="logout">
                <i class="el-icon-switch-button"></i> 退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog
      title="修改密码"
      :visible.sync="passwordDialogVisible"
      width="400px"
      :close-on-click-modal="false">
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="80px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            type="password"
            v-model="passwordForm.oldPassword"
            placeholder="请输入旧密码"
            show-password>
          </el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            type="password"
            v-model="passwordForm.newPassword"
            placeholder="请输入新密码"
            show-password>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            type="password"
            v-model="passwordForm.confirmPassword"
            placeholder="请确认新密码"
            show-password>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelPasswordChange">取 消</el-button>
        <el-button type="primary" @click="confirmPasswordChange" :loading="passwordLoading">确 定</el-button>
      </div>
    </el-dialog>
    
    <section class="container">
      <div class="left_side">
        <adminLeft ref="adminLeft" :theme-color="currentThemeColor"></adminLeft>
      </div>
      <div class="main_wrapper">
        <div class="content-wrapper">
          <adminTopNav class="top-nav" @refresh-page="refreshCurrentPage"></adminTopNav>
          <div class="page-content">
            <router-view></router-view>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
import adminLeft from '@/components/admin/adminLeft'
import adminTopNav from '@/components/admin/adminTopNav'

export default {
  name: 'AdminMain',
  components:{
    adminLeft: adminLeft,
    adminTopNav: adminTopNav
  },
  data() {
    return {
      adminName: '',
      currentTheme: 'purple',
      themes: [
        { name: 'purple', label: '管理紫', primary: '#6366f1' },
        { name: 'blue', label: '经典蓝', primary: '#2384d6' },
        { name: 'green', label: '清新绿', primary: '#42b983' },
        { name: 'orange', label: '活力橙', primary: '#f59e0b' },
        { name: 'red', label: '热情红', primary: '#ef4444' },
        { name: 'teal', label: '青碧色', primary: '#14b8a6' },
        { name: 'pink', label: '浪漫粉', primary: '#ec4899' },
        { name: 'indigo', label: '靛青蓝', primary: '#4f46e5' }
      ],
      // 修改密码相关
      passwordDialogVisible: false,
      passwordLoading: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [
          { required: true, message: '请输入旧密码', trigger: 'blur' }
        ],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    currentThemeLabel() {
      const theme = this.themes.find(t => t.name === this.currentTheme);
      return theme ? theme.label : '管理紫';
    },
    currentThemeColor() {
      const theme = this.themes.find(t => t.name === this.currentTheme);
      return theme ? theme.primary : '#6366f1';
    }
  },
  created() {
    // 优先使用sessionStorage（标签页隔离），其次使用Cookie
    this.adminName = sessionStorage.getItem('cname') || this.$cookies.get('cname') || '管理员';
    // 验证角色是否为管理员
    const role = sessionStorage.getItem('role') || this.$cookies.get('role');
    if (role !== '0' && role !== 0) {
      // 如果不是管理员，跳转到登录页
      this.$router.push('/');
      return;
    }
    this.initTheme();
  },
  methods: {
    refreshCurrentPage() {
      this.$nextTick(() => {
        const currentRoute = this.$route;
        this.$router.replace({ path: '/temp' });
        this.$nextTick(() => {
          this.$router.replace(currentRoute);
        });
      });
    },
    initTheme() {
      const savedTheme = localStorage.getItem('admin-theme') || 'purple';
      this.currentTheme = savedTheme;
      this.applyTheme(savedTheme);
    },
    changeTheme(themeName) {
      this.currentTheme = themeName;
      localStorage.setItem('admin-theme', themeName);
      this.applyTheme(themeName);
      this.$message({
        message: `已切换至${this.currentThemeLabel}主题`,
        type: 'success',
        duration: 1500
      });
    },
    applyTheme(themeName) {
      const theme = this.themes.find(t => t.name === themeName);
      if (theme) {
        document.documentElement.style.setProperty('--admin-primary', theme.primary);
        // 更新侧边栏颜色
        if (this.$refs.adminLeft) {
          this.$refs.adminLeft.$forceUpdate();
        }
      }
    },
    // 显示修改密码对话框
    showPasswordDialog() {
      this.passwordDialogVisible = true;
    },
    
    // 验证确认密码
    validateConfirmPassword(rule, value, callback) {
      if (value === '') {
        callback(new Error('请确认新密码'));
      } else if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'));
      } else {
        callback();
      }
    },
    
    // 取消修改密码
    cancelPasswordChange() {
      this.passwordDialogVisible = false;
      this.resetPasswordForm();
    },
    
    // 确认修改密码
    confirmPasswordChange() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (valid) {
          this.passwordLoading = true;
          try {
            const response = await this.$axios.post(
              `/api/admin/resetPsw`,
              {
                oldPsw: this.passwordForm.oldPassword,
                newPsw: this.passwordForm.newPassword
              }
            );
            
            if (response.data && response.data.code === 200) {
              if (response.data.data === true) {
                this.$message.success('密码修改成功');
                this.passwordDialogVisible = false;
                this.resetPasswordForm();
              } else {
                this.$message.error(response.data.data || '密码修改失败');
              }
            } else {
              this.$message.error('密码修改失败');
            }
          } catch (error) {
            console.error('修改密码失败:', error);
            this.$message.error('修改密码失败：' + (error.message || '网络错误'));
          } finally {
            this.passwordLoading = false;
          }
        }
      });
    },
    
    // 重置密码表单
    resetPasswordForm() {
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      };
      if (this.$refs.passwordForm) {
        this.$refs.passwordForm.clearValidate();
      }
    },
    
    logout() {
      this.$cookies.remove('cname');
      this.$cookies.remove('cid');
      this.$cookies.remove('role');
      sessionStorage.clear();
      this.$router.push('/');
      this.$message.success('已退出登录');
    }
  }
}
</script>

<style lang="less" scoped>
#admin-main {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--bg-primary);
}

.admin-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 100;
  
  .header-left {
    display: flex;
    align-items: center;
    
    .logo-icon {
      font-size: 28px;
      color: #fff !important;
      margin-right: 12px;
    }
    
    .logo-text {
      font-size: 18px;
      font-weight: 600;
      color: #fff !important;
      letter-spacing: 1px;
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;
  }
  
  .theme-palette {
    .palette-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      
      &:hover {
        background: #fff;
        transform: scale(1.1);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
      }
      
      i {
        font-size: 18px;
        color: var(--primary-color, #6366f1);
      }
    }
  }
  
  .user-info {
    .user-dropdown {
      display: flex;
      align-items: center;
      cursor: pointer;
      color: #fff;
      
      .username {
        margin: 0 8px;
        font-size: 14px;
        color: #fff !important;
      }
      
      i {
        font-size: 12px;
      }
    }
  }
}

#admin-main .container {
  display: flex;
  flex: 1;
  overflow: hidden;
}

#admin-main .left_side {
  width: auto;
  background-color: transparent;
  overflow-y: auto;
  border-right: none;
}

#admin-main .main_wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--bg-secondary);
}

#admin-main .content-wrapper {
  flex: 1;
  overflow: hidden;
  background-color: var(--bg-primary);
  display: flex;
  flex-direction: column;
}

#admin-main .top-nav {
  flex-shrink: 0;
  margin: 16px 20px 0 20px;
  border-radius: 8px;
  background-color: var(--card-bg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
}

#admin-main .page-content {
  flex: 1;
  overflow: auto;
  padding: 16px 20px 20px 20px;
}
</style>

<style>
/* 管理员主题面板样式 */
.theme-popover.el-popover {
  border-radius: 16px !important;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2) !important;
  border: none !important;
  padding: 16px !important;
}

.theme-panel {
  padding: 8px;
}

.theme-panel .theme-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  text-align: center;
}

.theme-panel .theme-colors {
  display: grid !important;
  grid-template-columns: repeat(4, 1fr) !important;
  gap: 12px !important;
  margin-bottom: 16px;
  justify-items: center;
}

.theme-panel .theme-color-item {
  width: 52px !important;
  height: 52px !important;
  border-radius: 12px !important;
  cursor: pointer;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.2s ease;
  border: 3px solid transparent !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.theme-panel .theme-color-item:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.25);
}

.theme-panel .theme-color-item.active {
  border-color: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.2), 0 6px 16px rgba(0, 0, 0, 0.25) !important;
}

.theme-panel .theme-color-item i {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
}

.theme-panel .theme-name {
  text-align: center;
  font-size: 14px;
  color: #666;
  font-weight: 500;
  margin-top: 8px;
}
</style>
