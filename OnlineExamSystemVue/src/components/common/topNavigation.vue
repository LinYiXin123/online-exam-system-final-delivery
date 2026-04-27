<!--
 * @Description: 顶部导航栏组件
 * @Author: 
 * @Date: 2024-12-28
-->
<template>
  <div class="top-navigation">
    <div class="nav-container">
      <div class="nav-tabs">
        <div 
          v-for="tab in navigationTabs" 
          :key="tab.path"
          :class="['nav-tab', { 'active': isActiveTab(tab.path) }]"
          @click="navigateToTab(tab)">
          <i :class="tab.icon" class="tab-icon"></i>
          <span class="tab-text">{{ tab.name }}</span>
          <i 
            v-if="tab.closable && openTabs.length > 1" 
            class="el-icon-close tab-close"
            @click.stop="closeTab(tab)">
          </i>
        </div>
      </div>
      <div class="nav-actions">
        <el-dropdown @command="handleTabAction">
          <span class="el-dropdown-link">
            <i class="el-icon-more"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="closeOthers">关闭其他</el-dropdown-item>
            <el-dropdown-item command="closeAll">关闭所有</el-dropdown-item>
            <el-dropdown-item command="refresh">刷新当前</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TopNavigation',
  data() {
    return {
      // 默认打开的标签页
      defaultTabs: [
        {
          name: '首页',
          path: '/index',
          icon: 'el-icon-house',
          closable: false
        }
      ],
      // 所有可用的导航选项
      availableTabs: [
        {
          name: '首页',
          path: '/index',
          icon: 'el-icon-house',
          closable: false
        },
        {
          name: '学生管理',
          path: '/studentManage',
          icon: 'el-icon-user',
          closable: true
        },
        {
          name: '添加学生',
          path: '/addStudent',
          icon: 'el-icon-plus',
          closable: true
        },
        {
          name: '考试管理',
          path: '/selectExam',
          icon: 'el-icon-document',
          closable: true
        },
        {
          name: '添加考试',
          path: '/addExam',
          icon: 'el-icon-edit',
          closable: true
        },
        {
          name: '智能组卷',
          path: '/autoPaper',
          icon: 'el-icon-magic-stick',
          closable: true
        },
        {
          name: '成绩查询',
          path: '/allStudentsGrade',
          icon: 'el-icon-data-analysis',
          closable: true
        },
        {
          name: '成绩分段',
          path: '/selectExamToPart',
          icon: 'el-icon-pie-chart',
          closable: true
        },
        {
          name: '系统仪表盘',
          path: '/adminDashboard',
          icon: 'el-icon-data-analysis',
          closable: true
        },
        {
          name: '教师管理',
          path: '/teacherManage',
          icon: 'el-icon-s-custom',
          closable: true
        },
        {
          name: '添加教师',
          path: '/addTeacher',
          icon: 'el-icon-plus',
          closable: true
        }
      ],
      // 当前打开的标签页
      openTabs: []
    }
  },
  computed: {
    navigationTabs() {
      return this.openTabs;
    },
    currentPath() {
      return this.$route.path;
    }
  },
  watch: {
    '$route'(to, from) {
      this.addTabFromRoute(to);
    }
  },
  created() {
    // 初始化默认标签页
    this.openTabs = [...this.defaultTabs];
    // 根据当前路由添加标签页
    this.addTabFromRoute(this.$route);
  },
  methods: {
    // 根据路由添加标签页
    addTabFromRoute(route) {
      const tab = this.availableTabs.find(t => t.path === route.path);
      if (tab) {
        this.addTab(tab);
      }
    },
    // 添加标签页
    addTab(tab) {
      const existingTab = this.openTabs.find(t => t.path === tab.path);
      if (!existingTab) {
        this.openTabs.push({ ...tab });
      }
    },
    // 检查是否为活动标签页
    isActiveTab(path) {
      return this.currentPath === path;
    },
    // 导航到标签页
    navigateToTab(tab) {
      if (this.currentPath !== tab.path) {
        this.$router.push(tab.path);
      }
    },
    // 关闭标签页
    closeTab(tab) {
      const index = this.openTabs.findIndex(t => t.path === tab.path);
      if (index > -1 && tab.closable) {
        this.openTabs.splice(index, 1);
        
        // 如果关闭的是当前活动标签页，切换到其他标签页
        if (this.isActiveTab(tab.path)) {
          const nextTab = this.openTabs[index] || this.openTabs[index - 1] || this.openTabs[0];
          if (nextTab) {
            this.$router.push(nextTab.path);
          }
        }
      }
    },
    // 处理标签页操作
    handleTabAction(command) {
      switch (command) {
        case 'closeOthers':
          this.closeOtherTabs();
          break;
        case 'closeAll':
          this.closeAllTabs();
          break;
        case 'refresh':
          this.refreshCurrentTab();
          break;
      }
    },
    // 关闭其他标签页
    closeOtherTabs() {
      const currentTab = this.openTabs.find(t => this.isActiveTab(t.path));
      const defaultTabs = this.openTabs.filter(t => !t.closable);
      
      if (currentTab && currentTab.closable) {
        this.openTabs = [...defaultTabs, currentTab];
      } else {
        this.openTabs = [...defaultTabs];
      }
    },
    // 关闭所有可关闭的标签页
    closeAllTabs() {
      this.openTabs = this.openTabs.filter(t => !t.closable);
      // 导航到首页
      if (!this.isActiveTab('/index')) {
        this.$router.push('/index');
      }
    },
    // 刷新当前标签页
    refreshCurrentTab() {
      this.$emit('refresh-page');
      this.$message({
        message: '页面已刷新',
        type: 'success',
        duration: 1500
      });
    }
  }
}
</script>

<style lang="less" scoped>
.top-navigation {
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  
  .nav-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    height: 48px;
    
    .nav-tabs {
      display: flex;
      align-items: center;
      flex: 1;
      overflow-x: auto;
      
      &::-webkit-scrollbar {
        height: 3px;
      }
      
      &::-webkit-scrollbar-track {
        background: transparent;
      }
      
      &::-webkit-scrollbar-thumb {
        background: var(--border-color);
        border-radius: 3px;
      }
      
      .nav-tab {
        display: flex;
        align-items: center;
        padding: 8px 16px;
        margin-right: 4px;
        border-radius: 6px 6px 0 0;
        background-color: var(--bg-secondary);
        border: 1px solid var(--border-color);
        border-bottom: none;
        cursor: pointer;
        transition: all 0.3s ease;
        white-space: nowrap;
        min-width: 120px;
        position: relative;
        
        &:hover {
          background-color: var(--table-row-hover);
        }
        
        &.active {
          background-color: var(--card-bg);
          color: var(--primary-color);
          border-color: var(--primary-color);
          
          &::after {
            content: '';
            position: absolute;
            bottom: -1px;
            left: 0;
            right: 0;
            height: 2px;
            background-color: var(--primary-color);
          }
        }
        
        .tab-icon {
          font-size: 14px;
          margin-right: 6px;
          color: var(--text-primary);
        }
        
        .tab-text {
          font-size: 13px;
          font-weight: 500;
          color: var(--text-primary);
        }
        
        .tab-close {
          margin-left: 8px;
          font-size: 12px;
          padding: 2px;
          border-radius: 50%;
          transition: all 0.2s ease;
          color: var(--text-secondary);
          
          &:hover {
            background-color: var(--danger-color);
            color: white;
          }
        }
      }
    }
    
    .nav-actions {
      margin-left: 16px;
      
      .el-dropdown-link {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 32px;
        height: 32px;
        border-radius: 6px;
        background-color: var(--bg-secondary);
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          background-color: var(--table-row-hover);
        }
        
        i {
          font-size: 16px;
          color: var(--text-secondary);
        }
      }
    }
  }
}

// 暗色模式适配 - 使用 body.dark-theme
:global(body.dark-theme) {
  .top-navigation {
    .nav-container {
      .nav-tabs {
        .nav-tab {
          &.active {
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
          }
        }
      }
    }
  }
  
  // 下拉菜单暗色模式样式
  .el-dropdown-menu {
    background-color: #1e293b !important;
    border-color: rgba(255, 255, 255, 0.1) !important;
    
    .el-dropdown-menu__item {
      color: #e2e8f0 !important;
      
      &:hover, &:focus {
        background-color: rgba(129, 140, 248, 0.2) !important;
        color: #818cf8 !important;
      }
    }
    
    .popper__arrow {
      border-bottom-color: #1e293b !important;
      
      &::after {
        border-bottom-color: #1e293b !important;
      }
    }
  }
}
</style>
