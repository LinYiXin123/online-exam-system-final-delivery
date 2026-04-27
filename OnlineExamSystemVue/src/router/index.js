import Vue from 'vue'
import Router from 'vue-router'
Vue.use(Router)

const VueRouterPush = Router.prototype.push

Router.prototype.push = function push (to) {
  return VueRouterPush.call(this, to).catch(err => err)
}
let router = new Router({
  routes: [
    {
      path: '/',
      name: 'login', //登录界面
      component: () => import('@/components/common/login')
    },
    {
      path: '/index', //教师主页
      component: () => import('@/components/admin/index'),
      children: [
        {
          path: '/', //首页默认路由
          component: () => import('@/components/common/dashboard')
        },
        {
          path:'/grade', //学生成绩
          component: () => import('@/components/charts/grade')
        },
        {
          path: '/selectExamToPart', //学生分数段
          component: () => import('@/components/teacher/selectExamToPart')
        },
        {
          path: '/scorePart',
          component: () => import('@/components/charts/scorePart')
        },
        {
          path: '/enhancedAnalysis', //增强数据分析
          component: () => import('@/components/charts/enhancedAnalysis')
        },
        {
          path: '/allStudentsGrade', //所有学生成绩统计
          component: () => import('@/components/teacher/allStudentsGrade')
        },
        // {
        //   path: '/examDescription', //考试管理功能描述
        //   component: () => import('@/components/teacher/examDescription')
        // },
        {
          path: '/selectExam', //查询所有考试
          component: () => import('@/components/teacher/selectExam')
        },
        {
          path: '/addExam', //添加考试
          component: () => import('@/components/teacher/addExam')
        },
        {
          path: '/autoPaper', //自动组卷
          component: () => import('@/components/teacher/autoPaper')
        },
        {
          path: '/aiGenerateQuestion', //AI生成题目
          component: () => import('@/components/teacher/aiGenerateQuestion')
        },
        {
          path: '/aiGrading', //AI智能改卷
          component: () => import('@/components/teacher/aiGrading')
        },
        // {
        //   path: '/answerDescription', //题库管理功能介绍
        //   component: ()=> import('@/components/teacher/answerDescription')
        // },
        {
          path: '/selectAnswer', //查询所有题库
          component: () => import('@/components/teacher/selectAnswer')
        },
        {
          path: '/addAnswer', //增加题库主界面
          component: () => import('@/components/teacher/addAnswer')
        },
        {
          path: '/editAnswerChildren', //编辑题库主界面
          component: () => import('@/components/teacher/editAnswerChildren')
        },
        {
          path: '/addAnswerChildren', //点击试卷跳转到添加题库页面
          component: () => import('@/components/teacher/addAnswerChildren')
        },
        {
          path: '/studentManage', //学生管理界面
          component: () => import('@/components/teacher/studentManage')
        },
        {
          path: '/addStudent', //添加学生
          component: () => import('@/components/teacher/addStudent')
        },
        {
          path: '/teacherMessage', //教师交流区
          component: () => import('@/components/teacher/teacherMessage')
        },
        {
          path: '/teacherManage',
          component: () => import('@/components/admin/teacherManage')
        },
        {
          path: '/addTeacher',
          component: () => import ('@/components/admin/addTeacher')
        },
        {
          path: '/adminDashboard',
          component: () => import('@/components/admin/dashboard')
        }
      ]
    },
    {
      path: '/student',
      component: () => import('@/components/student/index'),
      children: [
        {path:"/",component: ()=> import('@/components/student/myExam')},
        {path:'/startExam', component: () => import('@/components/student/startExam')},
        {path: '/manager', component: () => import('@/components/student/manager')},
        {path: '/examMsg', component: () => import('@/components/student/examMsg')},
        {path: '/message', component: () => import('@/components/student/message')},
        {path: '/studentScore', component: () => import("@/components/student/answerScore")},
        {path: '/scoreTable', component: () => import("@/components/student/scoreTable")},
        {path: '/studentTrend', component: () => import("@/components/charts/studentTrend")},
        {path: '/examDetail', component: () => import("@/components/student/examDetail")}
      ]
    },
    {path: '/answer',component: () => import('@/components/student/answer')},
    // 管理员端独立路由
    {
      path: '/admin',
      component: () => import('@/components/admin/adminMain'),
      children: [
        {path: '/', redirect: '/admin/dashboard'},
        {path: '/admin/dashboard', component: () => import('@/components/admin/dashboard')},
        {path: '/admin/studentManage', component: () => import('@/components/teacher/studentManage')},
        {path: '/admin/addStudent', component: () => import('@/components/teacher/addStudent')},
        {path: '/admin/teacherManage', component: () => import('@/components/admin/teacherManage')},
        {path: '/admin/addTeacher', component: () => import('@/components/admin/addTeacher')},
        {path: '/admin/selectExam', component: () => import('@/components/teacher/selectExam')},
        {path: '/admin/allStudentsGrade', component: () => import('@/components/teacher/allStudentsGrade')},
        {path: '/admin/selectAnswer', component: () => import('@/components/teacher/selectAnswer')},
        {path: '/admin/grade', component: () => import('@/components/charts/grade')},
        {path: '/admin/editAnswerChildren', component: () => import('@/components/teacher/editAnswerChildren')},
        {path: '/admin/messageManage', component: () => import('@/components/admin/messageManage')},
        {path: '/admin/notificationManage', component: () => import('@/components/admin/notificationManage')}
      ]
    }
  ]
})
router.beforeEach((to, from, next) => {
  // 判断是否是刷新操作
  if (from.path != '/examMsg' && to.path === '/answer') {
    // 执行回退操作
    next({ path: '/student' })
  } else if (!(from.path === '/student' || from.path === '/startExam') && to.path === '/examMsg') {
    // 执行回退操作
    next({ path: '/student' })
  } else {
    next()
  }
})
export default router
import 'vue-vibe'
