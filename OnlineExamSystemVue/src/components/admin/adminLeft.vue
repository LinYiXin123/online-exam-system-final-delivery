<template>
  <div id="admin-left" :style="sidebarStyle">
    <el-menu
      active-text-color="lightgrey"
      text-color="#fff"
      :default-active="this.$route.path"
      class="el-menu-vertical-demo"
      :collapse="flag"
      menu-trigger="click" router>
      <!-- 折叠按钮 -->
      <div class="collapse-btn" @click="toggleCollapse">
        <i :class="flag ? 'el-icon-s-unfold' : 'el-icon-s-fold'"></i>
      </div>
      
      <!-- 系统概览 -->
      <el-submenu index="1">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="el-icon-s-home" style="font-size: 28px; color: white !important;"></i>
            <span slot="title" class="title" style="color: white !important;">系统概览</span>
          </div>
        </template>
        <el-menu-item-group>
          <el-menu-item index="/admin/dashboard" style="color: white !important;">
            <i class="el-icon-data-analysis" style="font-size: 14px;color: white !important;margin-right: 8px;"></i> <span style="font-size: 14px;color: white !important;">系统仪表盘</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-submenu>

      <!-- 用户管理 -->
      <el-submenu index="2">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="el-icon-user-solid" style="font-size: 28px; color: white !important;"></i>
            <span slot="title" class="title" style="color: white !important;">用户管理</span>
          </div>
        </template>
        <el-menu-item-group>
          <el-menu-item index="/admin/studentManage" style="color: white !important;">
            <i class="el-icon-user" style="font-size: 14px;color: white !important;margin-right: 8px;"></i> <span style="font-size: 14px;color: white !important;">学生管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/addStudent" style="color: white !important;">
            <i class="el-icon-circle-plus-outline" style="font-size: 14px;color: white !important;margin-right: 8px;"></i> <span style="font-size: 14px;color: white !important;">添加学生</span>
          </el-menu-item>
          <el-menu-item index="/admin/teacherManage" style="color: white !important;">
            <i class="el-icon-s-custom" style="font-size: 14px;color: white !important;margin-right: 8px;"></i> <span style="font-size: 14px;color: white !important;">教师管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/addTeacher" style="color: white !important;">
            <i class="el-icon-circle-plus-outline" style="font-size: 14px;color: white !important;margin-right: 8px;"></i> <span style="font-size: 14px;color: white !important;">添加教师</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-submenu>

      <!-- 数据统计 -->
      <el-submenu index="3">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="el-icon-s-data" style="font-size: 28px; color: white !important;"></i>
            <span slot="title" class="title" style="color: white !important;">数据统计</span>
          </div>
        </template>
        <el-menu-item-group>
          <el-menu-item index="/admin/selectExam" style="color: white !important;">
            <i class="el-icon-document" style="font-size: 14px;color: white !important;margin-right: 8px;"></i> <span style="font-size: 14px;color: white !important;">考试列表</span>
          </el-menu-item>
          <el-menu-item index="/admin/allStudentsGrade" style="color: white !important;">
            <i class="el-icon-tickets" style="font-size: 14px;color: white !important;margin-right: 8px;"></i> <span style="font-size: 14px;color: white !important;">成绩查询</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-submenu>

      <!-- 题库管理 -->
      <el-submenu index="4">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="el-icon-office-building" style="font-size: 28px; color: white !important;"></i>
            <span slot="title" class="title" style="color: white !important;">题库管理</span>
          </div>
        </template>
        <el-menu-item-group>
          <el-menu-item index="/admin/selectAnswer" style="color: white !important;">
            <i class="el-icon-notebook-2" style="font-size: 14px;color: white !important;margin-right: 8px;"></i> <span style="font-size: 14px;color: white !important;">题目列表</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-submenu>

      <!-- 内容管理 -->
      <el-submenu index="5">
        <template slot="title">
          <div class="el-item-menu left-width">
            <i class="el-icon-chat-dot-round" style="font-size: 28px; color: white !important;"></i>
            <span slot="title" class="title" style="color: white !important;">内容管理</span>
          </div>
        </template>
        <el-menu-item-group>
          <el-menu-item index="/admin/notificationManage" style="color: white !important;">
            <i class="el-icon-bell" style="font-size: 14px;color: white !important;margin-right: 8px;"></i> <span style="font-size: 14px;color: white !important;">通知管理</span>
          </el-menu-item>
        </el-menu-item-group>
      </el-submenu>
    </el-menu>
  </div>
</template>

<script>
import {mapState} from 'vuex'
export default {
  name: "adminLeft",
  props: {
    themeColor: {
      type: String,
      default: '#6366f1'
    }
  },
  data() {
    return {
      // 管理员专属菜单
      adminMenu: [
        {
          index: '1',
          title: '系统概览',
          icon: 'el-icon-s-home',
          content:[{item2:'系统仪表盘',path:'/admin/dashboard',icon:"el-icon-data-analysis"}],
        },
        {
          index: '2',
          title: '用户管理',
          icon: 'el-icon-user-solid',
          content:[
            {item2:'学生管理',path:'/admin/studentManage',icon:"el-icon-user"},
            {item3:'添加学生',path:'/admin/addStudent',icon:"el-icon-circle-plus-outline"},
            {item3:'教师管理',path:'/admin/teacherManage',icon:"el-icon-s-custom"},
            {item3:'添加教师',path:'/admin/addTeacher',icon:"el-icon-circle-plus-outline"}
          ],
        },
        {
          index: '3',
          title: '数据统计',
          icon: 'el-icon-s-data',
          content:[
            {item2:'考试列表',path:'/admin/selectExam',icon:"el-icon-document"},
            {item3:'成绩查询',path:'/admin/allStudentsGrade',icon:"el-icon-tickets"}
          ],
        },
        {
          index: '4',
          title: '题库管理',
          icon: 'el-icon-office-building',
          content:[{item2:'题目列表',path:'/admin/selectAnswer',icon:"el-icon-notebook-2"}],
        },
        {
          index: '5',
          title: '内容管理',
          icon: 'el-icon-chat-dot-round',
          content:[
            {item3:'通知管理',path:'/admin/notificationManage',icon:"el-icon-bell"}
          ],
        }
      ]
    }
  },
  computed: {
    ...mapState(["flag"]),
    sidebarStyle() {
      return {
        backgroundColor: this.themeColor
      }
    }
  },
  methods: {
    toggleCollapse() {
      this.$store.commit('setFlag', !this.flag)
    }
  }
}
</script>

<style scoped>
#admin-left {
  height: calc(100vh - 60px);
  z-index: 0;
  transition: all 0.3s ease;
  overflow-y: auto;
  overflow-x: hidden;
}

#admin-left::-webkit-scrollbar {
  width: 6px;
}

#admin-left::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

#admin-left::-webkit-scrollbar-track {
  background: transparent;
}

#admin-left .el-menu-vertical-demo {
  transition: width 0.3s ease;
  border-right: none;
  background-color: transparent !important;
}

#admin-left .el-menu-vertical-demo:not(.el-menu--collapse) {
  min-height: auto;
}

.left-width .iconfont {
  font-size: 18px;
  color: #fff;
}

.left-width {
  width: 213px;
}

.collapse-btn {
  padding: 15px;
  text-align: center;
  cursor: pointer;
  color: #fff;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.collapse-btn:hover {
  background-color: rgba(255,255,255,0.1);
}

.collapse-btn i {
  font-size: 20px;
}

.title {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  margin-left: 14px;
}
</style>

<style>
/* 强制所有菜单元素背景透明 */
#admin-left .el-menu,
#admin-left .el-submenu,
#admin-left .el-submenu__title,
#admin-left .el-menu-item,
#admin-left .el-menu-item-group,
#admin-left .el-menu-item-group__title {
  background-color: transparent !important;
}

#admin-left .el-submenu__title {
  color: #fff !important;
}

/* 强制主菜单标题文字为白色 */
#admin-left .el-submenu__title .title,
#admin-left .el-submenu__title span,
#admin-left .title,
#admin-left .el-submenu__title .el-item-menu .title,
#admin-left .el-submenu__title .el-item-menu span,
#admin-left .el-item-menu .title,
#admin-left .el-item-menu span {
  color: #fff !important;
  font-weight: bold !important;
}

#admin-left .el-submenu__title:hover {
  background-color: rgba(255,255,255,0.2) !important;
}

#admin-left .el-menu-item:hover {
  background-color: rgba(255,255,255,0.2) !important;
}

#admin-left .el-menu-item.is-active {
  background-color: rgba(255,255,255,0.3) !important;
}

#admin-left .el-submenu .el-menu {
  background-color: rgba(0,0,0,0.1) !important;
}

/* 统一子菜单项样式 */
#admin-left .el-menu-item {
  color: #fff !important;
  font-size: 14px !important;
  height: 50px !important;
  line-height: 50px !important;
  padding-left: 60px !important;
  background-color: transparent !important;
  display: flex !important;
  align-items: center !important;
}

#admin-left .el-menu-item i {
  color: #fff !important;
  font-size: 14px !important;
  margin-right: 8px !important;
  width: 20px !important;
  text-align: center !important;
}

#admin-left .el-menu-item span {
  color: #fff !important;
  font-size: 14px !important;
}

/* 子菜单箭头颜色 */
#admin-left .el-submenu__icon-arrow {
  color: #fff !important;
}

/* 主菜单图标颜色 */
#admin-left .el-submenu__title i,
#admin-left .el-item-menu i,
#admin-left .left-width i {
  color: #fff !important;
}

/* 确保所有菜单项背景透明，解决滚动时变色问题 */
#admin-left .el-menu-item-group__title {
  padding-left: 50px !important;
  color: rgba(255, 255, 255, 0.6) !important;
  background-color: transparent !important;
}

#admin-left .el-submenu .el-menu-item {
  background-color: transparent !important;
}

#admin-left .el-submenu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
}

/* 子菜单文字颜色强制白色 */
#admin-left .el-menu-item-group .el-menu-item {
  color: #fff !important;
}

/* 折叠按钮颜色 */
#admin-left .collapse-btn i {
  color: #fff !important;
}

/* 修复悬停时变色的问题 - 使用 inherit 继承父元素背景 */
#admin-left .el-submenu__title,
#admin-left .el-menu-item {
  background-color: inherit !important;
}

/* 强制覆盖所有悬停状态 - 保持原色不变 */
#admin-left .el-menu .el-submenu__title:hover,
#admin-left .el-menu .el-submenu__title:focus,
#admin-left .el-menu-vertical-demo .el-submenu__title:hover,
#admin-left .el-menu-vertical-demo .el-submenu__title:focus,
#admin-left .el-submenu .el-submenu__title:hover,
#admin-left .el-submenu .el-submenu__title:focus,
#admin-left .el-menu-item:hover,
#admin-left .el-menu-item:focus {
  background-color: inherit !important;
  color: #fff !important;
}

/* 覆盖 Element UI 默认的悬停背景 */
.el-menu-item:focus,
.el-menu-item:hover {
  outline: none;
  background-color: inherit !important;
}

/* 所有菜单元素使用 inherit */
#admin-left .el-menu--inline,
#admin-left .el-menu-item-group,
#admin-left .el-menu-item-group ul,
#admin-left .el-menu-item-group li {
  background-color: inherit !important;
}

/* 折叠后弹出子菜单样式 - 使用主题色 */
.el-menu--vertical .el-menu--popup {
  background-color: var(--admin-primary, #6366f1) !important;
  min-width: 180px !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item {
  color: #fff !important;
  background-color: var(--admin-primary, #6366f1) !important;
  font-size: 14px !important;
  height: 45px !important;
  line-height: 45px !important;
  padding-left: 20px !important;
  display: flex !important;
  align-items: center !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item span {
  color: #fff !important;
  font-size: 14px !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item i {
  color: #fff !important;
  font-size: 14px !important;
  width: 20px !important;
  text-align: center !important;
  margin-right: 8px !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
}

.el-menu--vertical .el-menu--popup .el-menu-item-group__title {
  color: rgba(255, 255, 255, 0.7) !important;
  background-color: var(--admin-primary, #6366f1) !important;
}

/* 最终强制覆盖 - 确保所有主菜单标题为白色 */
#admin-left * {
  color: inherit;
}

#admin-left .el-submenu__title,
#admin-left .el-submenu__title *,
#admin-left .title,
#admin-left .el-item-menu .title,
#admin-left .el-item-menu span.title {
  color: #ffffff !important;
}

/* 强制所有菜单项为白色 - 解决颜色覆盖问题 */
#admin-left .el-menu-item,
#admin-left .el-menu-item *,
#admin-left .el-menu-item i,
#admin-left .el-menu-item span {
  color: #ffffff !important;
}

/* 特别强制成绩查询菜单项 */
#admin-left .el-menu-item[index="/admin/allStudentsGrade"],
#admin-left .el-menu-item[index="/admin/allStudentsGrade"] *,
#admin-left .el-menu-item[index="/admin/allStudentsGrade"] i,
#admin-left .el-menu-item[index="/admin/allStudentsGrade"] span {
  color: #ffffff !important;
}
</style>
