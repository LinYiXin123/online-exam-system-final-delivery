/*
 * @Description: 
 * @Author: 
 * @Date: 2024-03-08 20:38:49
 */
import VUE from 'vue'
import VUEX from 'vuex'

VUE.use(VUEX)

// 教师端默认菜单
const teacherMenu = [
  {
    index: '1',
    title: '考试管理',
    icon: 'icon-r-paper',
    content: [{ item2: '考试管理', path: '/selectExam', icon:"iconfont icon-r-find" }, { item3: '添加考试', path: '/addExam', icon:"iconfont icon-r-add" }],
  },
  {
    index: '2',
    title: '题库管理',
    icon: 'icon-r-building',
    content: [{ item2: '题目列表', path: '/selectAnswer', icon:"iconfont icon-r-list" }, { item3: '新增题目', path: '/addAnswer', icon:"iconfont icon-r-add" }, { item3: '编辑题目', path: '/addAnswerChildren', icon:"iconfont icon-r-edit" }],
  },
  {
    index: '3',
    title: '成绩查询',
    icon: 'icon-r-find',
    content: [{ item2: '学生成绩查询', path: '/allStudentsGrade', icon:"iconfont icon-r-find" }, { item3: '成绩分段查询', path: '/selectExamToPart', icon:"iconfont icon-r-find" }],
  },
  {
    index: '4',
    title: '学生管理',
    icon: 'icon-r-team',
    content: [{ item2: '学生管理', path: '/studentManage', icon:"iconfont icon-r-user2" }, { item3: '添加学生', path: '/addStudent', icon:"iconfont icon-r-add" }],
  },
  {
    index: '5',
    title: '智能组卷',
    icon: 'icon-r-paper',
    content: [{ item2: '自动组卷', path: '/autoPaper', icon:"iconfont icon-r-add" }, { item3: 'AI生成题目', path: '/aiGenerateQuestion', icon:"iconfont icon-r-add" }, { item3: 'AI智能改卷', path: '/aiGrading', icon:"iconfont icon-r-find" }],
  },
  {
    index: '6',
    title: '交流区',
    icon: 'icon-r-refresh',
    content: [{ item2: '教师交流区', path: '/teacherMessage', icon:"iconfont icon-r-refresh" }],
  }
]

// 管理员专属菜单
const adminMenu = [
  {
    index: '1',
    title: '系统概览',
    icon: 'icon-r-set',
    content:[{item2:'系统仪表盘',path:'/adminDashboard',icon:"el-icon-data-analysis"}],
  },
  {
    index: '2',
    title: '用户管理',
    icon: 'icon-r-team',
    content:[{item2:'学生管理',path:'/studentManage',icon:"el-icon-user"},{item3:'教师管理',path:'/teacherManage',icon:"el-icon-s-custom"}],
  },
  {
    index: '3',
    title: '考试管理',
    icon: 'icon-r-paper',
    content:[{item2:'考试列表',path:'/selectExam',icon:"iconfont icon-r-find"},{item3:'成绩查询',path:'/allStudentsGrade',icon:"iconfont icon-r-find"}],
  },
  {
    index: '4',
    title: '题库管理',
    icon: 'icon-r-building',
    content:[{item2:'题目列表',path:'/selectAnswer',icon:"iconfont icon-r-list"}],
  }
]

const state = {
  isPractice: false, //练习模式标志
  flag: false, //菜单栏左右滑动标志
  userInfo: null,
  menu: [
  {
    index: '1',
    title: '考试管理',
    icon: 'icon-r-paper',
    content: [{ item2: '考试管理', path: '/selectExam', icon:"iconfont icon-r-find" }, { item3: '添加考试', path: '/addExam', icon:"iconfont icon-r-add" }],
  },
  {
    index: '2',
    title: '题库管理',
    icon: 'icon-r-building',
    content: [{ item2: '题目列表', path: '/selectAnswer', icon:"iconfont icon-r-list" }, { item3: '新增题目', path: '/addAnswer', icon:"iconfont icon-r-add" }, { item3: '编辑题目', path: '/addAnswerChildren', icon:"iconfont icon-r-edit" }],
  },
  {
    index: '3',
    title: '成绩查询',
    icon: 'icon-r-find',
    content: [{ item2: '学生成绩查询', path: '/allStudentsGrade', icon:"iconfont icon-r-find" }, { item3: '成绩分段查询', path: '/selectExamToPart', icon:"iconfont icon-r-find" }],
  },
  {
    index: '4',
    title: '学生管理',
    icon: 'icon-r-team',
    content: [{ item2: '学生管理', path: '/studentManage', icon:"iconfont icon-r-user2" }, { item3: '添加学生', path: '/addStudent', icon:"iconfont icon-r-add" }],
  },
  {
    index: '5',
    title: '智能组卷',
    icon: 'icon-r-paper',
    content: [{ item2: '自动组卷', path: '/autoPaper', icon:"iconfont icon-r-add" }, { item3: 'AI生成题目', path: '/aiGenerateQuestion', icon:"iconfont icon-r-add" }, { item3: 'AI智能改卷', path: '/aiGrading', icon:"iconfont icon-r-find" }],
  },
  {
    index: '6',
    title: '交流区',
    icon: 'icon-r-refresh',
    content: [{ item2: '教师交流区', path: '/teacherMessage', icon:"iconfont icon-r-refresh" }],
  }
  ],
}
const mutations = {
  practice(state, status) {
    state.isPractice = status
  },
  toggle(state) {
    state.flag = !state.flag
  },
  setFlag(state, value) {
    state.flag = value
  },
  changeUserInfo(state, info) {
    state.userInfo = info
  },
  // 根据角色设置菜单
  setMenuByRole(state, role) {
    if (role == 0) {
      // 管理员菜单
      state.menu = JSON.parse(JSON.stringify(adminMenu))
    } else {
      // 教师菜单
      state.menu = JSON.parse(JSON.stringify(teacherMenu))
    }
  }
}
const getters = {

}

import '@/../config/initialize'
const actions = {
  getUserInfo(context, info) {
    context.commit('changeUserInfo', info)
  },
  getPractice(context, status) {
    context.commit('practice', status)
  }
}
export default new VUEX.Store({
  state,
  mutations,
  getters,
  actions,
  // store
})
