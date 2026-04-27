<template>
  <div class="admin-top-navigation">
    <div class="nav-container">
      <div class="nav-tabs">
        <div 
          v-for="tab in openTabs" 
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
        <el-dropdown @command="handleTabAction" trigger="click">
          <el-button size="small" type="text" style="color: var(--text-secondary);">
            <i class="el-icon-more"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="closeOthers">关闭其他</el-dropdown-item>
            <el-dropdown-item command="closeAll">关闭所有</el-dropdown-item>
            <el-dropdown-item command="refresh" divided>刷新当前页</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminTopNav',
  data() {
    return {
      defaultTabs: [
        {
          name: '仪表盘',
          path: '/admin/dashboard',
          icon: 'el-icon-data-analysis',
          closable: false
        }
      ],
      availableTabs: [
        { name: '仪表盘', path: '/admin/dashboard', icon: 'el-icon-data-analysis', closable: false },
        { name: '学生管理', path: '/admin/studentManage', icon: 'el-icon-user', closable: true },
        { name: '添加学生', path: '/admin/addStudent', icon: 'el-icon-plus', closable: true },
        { name: '教师管理', path: '/admin/teacherManage', icon: 'el-icon-s-custom', closable: true },
        { name: '添加教师', path: '/admin/addTeacher', icon: 'el-icon-plus', closable: true },
        { name: '考试列表', path: '/admin/selectExam', icon: 'el-icon-document', closable: true },
        { name: '成绩查询', path: '/admin/allStudentsGrade', icon: 'el-icon-data-analysis', closable: true },
        { name: '题目列表', path: '/admin/selectAnswer', icon: 'el-icon-tickets', closable: true },
        { name: '留言管理', path: '/admin/messageManage', icon: 'el-icon-chat-dot-round', closable: true },
        { name: '通知管理', path: '/admin/notificationManage', icon: 'el-icon-bell', closable: true }
      ],
      openTabs: []
    }
  },
  created() {
    this.openTabs = [...this.defaultTabs];
    this.addTabFromRoute(this.$route);
  },
  watch: {
    '$route'(to) {
      this.addTabFromRoute(to);
    }
  },
  methods: {
    addTabFromRoute(route) {
      const tab = this.availableTabs.find(t => t.path === route.path);
      if (tab) {
        this.addTab(tab);
      }
    },
    addTab(tab) {
      const existingTab = this.openTabs.find(t => t.path === tab.path);
      if (!existingTab) {
        this.openTabs.push({ ...tab });
      }
    },
    isActiveTab(path) {
      return this.$route.path === path;
    },
    navigateToTab(tab) {
      if (this.$route.path !== tab.path) {
        this.$router.push(tab.path);
      }
    },
    closeTab(tab) {
      const index = this.openTabs.findIndex(t => t.path === tab.path);
      if (index > -1 && tab.closable) {
        this.openTabs.splice(index, 1);
        if (this.isActiveTab(tab.path)) {
          const nextTab = this.openTabs[index] || this.openTabs[index - 1] || this.openTabs[0];
          if (nextTab) {
            this.$router.push(nextTab.path);
          }
        }
      }
    },
    handleTabAction(command) {
      switch (command) {
        case 'closeOthers':
          this.openTabs = this.openTabs.filter(t => !t.closable || this.isActiveTab(t.path));
          break;
        case 'closeAll':
          this.openTabs = this.openTabs.filter(t => !t.closable);
          if (!this.isActiveTab('/admin/dashboard')) {
            this.$router.push('/admin/dashboard');
          }
          break;
        case 'refresh':
          this.$emit('refresh-page');
          break;
      }
    }
  }
}
</script>

<style lang="less" scoped>
.admin-top-navigation {
  .nav-container {
    display: flex;
    align-items: center;
    padding: 0 20px;
    height: 48px;
    
    .nav-tabs {
      display: flex;
      align-items: center;
      flex: 1;
      overflow-x: auto;
      gap: 8px;
      
      &::-webkit-scrollbar {
        height: 4px;
      }
      
      .nav-tab {
        display: flex;
        align-items: center;
        padding: 8px 16px;
        border-radius: 6px;
        cursor: pointer;
        white-space: nowrap;
        transition: all 0.2s ease;
        background-color: var(--bg-secondary);
        border: 1px solid var(--border-color);
        
        &:hover {
          background-color: rgba(124, 58, 237, 0.1);
          border-color: #7c3aed;
        }
        
        &.active {
          background-color: rgba(255, 255, 255, 0.95);
          color: #333;
          border-color: rgba(255, 255, 255, 0.95);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          
          .tab-icon, .tab-text {
            color: #333;
          }
          
          .tab-close {
            color: rgba(0, 0, 0, 0.5);
            
            &:hover {
              color: #333;
              background-color: rgba(0, 0, 0, 0.1);
            }
          }
        }
        
        .tab-icon {
          font-size: 14px;
          margin-right: 6px;
          color: var(--text-secondary);
        }
        
        .tab-text {
          font-size: 13px;
          color: var(--text-primary);
        }
        
        .tab-close {
          margin-left: 8px;
          font-size: 12px;
          color: var(--text-muted);
          border-radius: 50%;
          padding: 2px;
          
          &:hover {
            background-color: rgba(0, 0, 0, 0.1);
            color: var(--text-primary);
          }
        }
      }
    }
    
    .nav-actions {
      margin-left: 16px;
      flex-shrink: 0;
    }
  }
}
</style>
