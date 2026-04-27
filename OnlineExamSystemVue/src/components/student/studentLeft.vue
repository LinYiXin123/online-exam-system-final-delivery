<!-- 学生端左侧导航栏 - 与教师端风格一致 -->
<template>
  <div id="student-left" :class="{ 'collapsed': isCollapsed }" :style="{ backgroundColor: themeColorDark }">
    <!-- themeColorDark 现在是正常色（主菜单用），themeColor 是深色（子菜单用） -->
    <el-menu
      active-text-color="lightgrey"
      text-color="#fff"
      :default-active="$route.path"
      class="el-menu-vertical-demo"
      @open="handleOpen"
      @close="handleClose"
      :collapse="isCollapsed"
      menu-trigger="click"
      router
    >
      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="toggleCollapse">
        <i :class="isCollapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
      </div>

      <!-- 考试管理 -->
      <el-submenu index="1">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="el-icon-s-home" style="font-size: 28px;"></i>
            <span slot="title" class="title">考试中心</span>
          </div>
        </template>
        <el-menu-item-group>
          <el-menu-item index="/student" style="color:white;">
            <i class="el-icon-document" style="font-size: 14px;color: white;margin-right: 8px;"></i> <span style="font-size: 14px;color: white;">考试列表</span>
          </el-menu-item>
          <el-menu-item index="/startExam" style="color:white;">
            <i class="el-icon-edit-outline" style="font-size: 14px;color: white;margin-right: 8px;"></i> <span style="font-size: 14px;color: white;">试卷练习</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-submenu>

      <!-- 我的分数 -->
      <el-submenu index="2">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="el-icon-data-analysis" style="font-size: 28px;"></i>
            <span slot="title" class="title">我的分数</span>
          </div>
        </template>
        <el-menu-item-group>
          <el-menu-item index="/scoreTable" style="color:white;">
            <i class="el-icon-tickets" style="font-size: 14px;color: white;margin-right: 8px;"></i> <span style="font-size: 14px;color: white;">成绩列表</span>
          </el-menu-item>
          <el-menu-item index="/studentTrend" style="color:white;">
            <i class="el-icon-data-line" style="font-size: 14px;color: white;margin-right: 8px;"></i> <span style="font-size: 14px;color: white;">成绩趋势</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-submenu>

      <!-- 交流区 -->
      <el-submenu index="3">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="el-icon-chat-dot-round" style="font-size: 28px;"></i>
            <span slot="title" class="title">交流区</span>
          </div>
        </template>
        <el-menu-item-group>
          <el-menu-item index="/message" style="color:white;">
            <i class="el-icon-chat-line-round" style="font-size: 14px;color: white;margin-right: 8px;"></i> <span style="font-size: 14px;color: white;">留言交流</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-submenu>

      <!-- 修改密码 -->
      <el-submenu index="4">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="el-icon-setting" style="font-size: 28px;"></i>
            <span slot="title" class="title">修改密码</span>
          </div>
        </template>
        <el-menu-item-group>
          <el-menu-item index="/manager" style="color:white;">
            <i class="el-icon-key" style="font-size: 14px;color: white;margin-right: 8px;"></i> <span style="font-size: 14px;color: white;">密码修改</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-submenu>
    </el-menu>
  </div>
</template>

<script>
export default {
  name: 'StudentLeft',
  props: {
    themeColor: {
      type: String,
      default: '#2384d6'
    },
    themeColorDark: {
      type: String,
      default: '#1a6cb3'
    }
  },
  data() {
    return {
      isCollapsed: false
    }
  },
  mounted() {
    // 禁用 Element UI 的悬停背景色变化
    this.disableHoverEffect();
  },
  beforeDestroy() {
    // 清理定时器
    if (this.hoverInterval) {
      clearInterval(this.hoverInterval);
    }
  },
  methods: {
    disableHoverEffect() {
      // 空方法 - 暂时禁用悬停效果处理
    },
    handleOpen(key, keyPath) {
      // console.log(key, keyPath);
    },
    handleClose(key, keyPath) {
      // console.log(key, keyPath);
    },
    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed
      this.$emit('collapse-change', this.isCollapsed)
    }
  }
}
</script>

<style>
/* 与教师端完全一致的样式 */
#student-left .el-menu-vertical-demo .el-submenu__title {
  overflow: hidden;
}

#student-left .left-width i {
  font-size: 28px;
  color: #fff !important;
}

#student-left .left-width {
  width: 213px;
}

#student-left .el-menu-vertical-demo:not(.el-menu--collapse) {
  min-height: calc(100vh - 60px);
}

/* 确保 el-menu 背景色正确 */
#student-left .el-menu-vertical-demo {
  background-color: inherit !important;
}

#student-left {
  height: calc(100vh - 60px);
  background-color: var(--primary-color, #2384d6);
  z-index: 99;
  position: fixed;
  left: 0;
  top: 60px;
  transition: width 0.3s ease;
}

#student-left .el-menu-vertical-demo {
  transition: width 0.3s ease;
}

#student-left .el-menu-vertical-demo .title {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  margin-left: 14px;
}

#student-left .el-submenu {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
}

/* 主菜单项背景色 - 使用正常色 */
#student-left .el-submenu .el-submenu__title {
  background-color: var(--primary-color, #2384d6) !important;
}

#student-left .el-submenu.is-opened > .el-submenu__title {
  background-color: var(--primary-color, #2384d6) !important;
}

/* 悬停样式 - 保持原色不变 */
#student-left .el-submenu__title:hover,
#student-left .el-menu-vertical-demo .el-submenu__title:hover {
  background-color: inherit !important;
  color: #ffffff !important;
  transition: all 0.2s ease !important;
}

#student-left .el-submenu__title i {
  color: #fbfbfc !important;
}

/* 菜单项悬停样式 - 保持原色不变 */
#student-left .el-menu-item:hover,
#student-left .el-menu-vertical-demo .el-menu-item:hover {
  background-color: inherit !important;
  color: #ffffff !important;
  transition: all 0.2s ease !important;
}

/* 深度选择器覆盖Element UI内置样式 - 保持原色不变 */
#student-left .el-menu-item:hover,
#student-left .el-menu-item:focus {
  background-color: inherit !important;
  color: #ffffff !important;
}

#student-left .el-submenu__title:hover,
#student-left .el-submenu__title:focus {
  background-color: inherit !important;
  color: #ffffff !important;
}

/* 子菜单项样式 - 使用深色 */
#student-left .el-menu-item-group {
  background-color: var(--primary-dark, #1a6cb3) !important;
}

#student-left .el-menu-item-group .el-menu-item {
  color: #fff !important;
  background-color: var(--primary-dark, #1a6cb3) !important;
  padding-left: 50px !important;
}

#student-left .el-menu-item-group .el-menu-item i,
#student-left .el-menu-item-group .el-menu-item span {
  color: #fff !important;
}

#student-left .el-menu-item-group .el-menu-item:hover {
  background-color: inherit !important;
  color: #fff !important;
}

#student-left .el-menu-item-group .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.25) !important;
  color: #fff !important;
  border-left: 3px solid #fff !important;
}

/* 折叠按钮样式 - 使用主题色 */
#student-left .collapse-btn {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s;
  background-color: inherit;
}

#student-left .collapse-btn i {
  font-size: 20px;
  color: #fff !important;
}

#student-left .collapse-btn:hover {
  background-color: inherit;
}

/* 折叠状态 */
#student-left.collapsed {
  width: 64px !important;
}

#student-left.collapsed .el-menu-vertical-demo {
  width: 64px !important;
}

/* 折叠后子菜单弹出样式 - 子菜单用深色 */
.el-menu--vertical .el-menu--popup {
  background-color: var(--primary-dark, #1a6cb3) !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item {
  color: #fff !important;
  background-color: var(--primary-dark, #1a6cb3) !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item span {
  color: #fff !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item i {
  color: #fff !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item:hover {
  background-color: inherit !important;
}

/* 修复悬停时变回蓝色的问题 - 强制覆盖 Element UI 样式 */
#student-left .el-submenu__title,
#student-left .el-menu-item {
  background-color: inherit !important;
  transition: background-color 0.2s ease !important;
}

/* 强制覆盖所有悬停状态 - 保持原色不变 */
#student-left .el-menu .el-submenu__title:hover,
#student-left .el-menu .el-submenu__title:focus,
#student-left .el-menu-vertical-demo .el-submenu__title:hover,
#student-left .el-menu-vertical-demo .el-submenu__title:focus,
#student-left .el-submenu .el-submenu__title:hover,
#student-left .el-submenu .el-submenu__title:focus,
#student-left .el-menu-item:hover,
#student-left .el-menu-item:focus,
.el-menu--vertical .el-submenu__title:hover,
.el-menu--vertical .el-submenu__title:focus {
  background-color: inherit !important;
  color: #fff !important;
}

/* 折叠状态下的样式 - 保持原色不变 */
#student-left.collapsed .el-submenu .el-submenu__title:hover,
#student-left.collapsed .el-menu .el-submenu__title:hover {
  background-color: inherit !important;
}

/* 覆盖 Element UI 默认的蓝色悬停 - 保持原色不变 */
.el-menu-item:focus,
.el-menu-item:hover {
  outline: none;
  background-color: inherit !important;
}

/* 使用伪元素覆盖悬停背景 */
#student-left .el-submenu__title {
  position: relative;
  overflow: hidden;
}

#student-left .el-submenu__title::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--primary-color, #e74c3c);
  z-index: 1;
  pointer-events: none;
}

#student-left .el-submenu__title > * {
  position: relative;
  z-index: 2;
}
</style>
