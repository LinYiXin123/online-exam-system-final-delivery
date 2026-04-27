<!--
 * @Description: 仪表板首页组件
 * @Author: 
 * @Date: 2024-12-28
-->
<template>
  <div class="dashboard">
    <div class="dashboard-container">
      <!-- 轮播图区域 -->
      <div class="carousel-section">
        <el-carousel :interval="4000" height="200px" indicator-position="outside">
          <el-carousel-item v-for="item in carouselImages" :key="item.id">
            <img :src="item.url" :alt="item.alt" class="carousel-image" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <p class="welcome-subtitle">智能化的教学管理平台，让教育更高效</p>
      </div>

      <!-- 功能模块网格 -->
      <div class="modules-grid">
        <!-- 学生管理模块 -->
        <div class="module-card" @click="navigateTo('/studentManage')">
          <div class="module-icon">
            <i class="el-icon-user-solid"></i>
          </div>
          <div class="module-content">
            <h3 class="module-title">学生管理</h3>
            <p class="module-description">管理学生信息，包括添加、编辑、删除学生档案，支持批量导入导出</p>
            <div class="module-stats">
              <span class="stat-item">
                <i class="el-icon-document"></i>
                总学生数: {{ studentCount }}
              </span>
            </div>
          </div>
          <div class="module-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>

        <!-- 考试管理模块 -->
        <div class="module-card" @click="navigateTo('/selectExam')">
          <div class="module-icon">
            <i class="el-icon-document-checked"></i>
          </div>
          <div class="module-content">
            <h3 class="module-title">考试管理</h3>
            <p class="module-description">创建和管理考试，设置考试时间、题目分配，监控考试进度</p>
            <div class="module-stats">
              <span class="stat-item">
                <i class="el-icon-time"></i>
                进行中考试: {{ activeExamCount }}
              </span>
            </div>
          </div>
          <div class="module-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>

        <!-- 智能组卷模块 -->
        <div class="module-card" @click="navigateTo('/autoPaper')">
          <div class="module-icon">
            <i class="el-icon-magic-stick"></i>
          </div>
          <div class="module-content">
            <h3 class="module-title">智能组卷</h3>
            <p class="module-description">AI智能组卷系统，根据知识点和难度自动生成试卷</p>
            <div class="module-stats">
              <span class="stat-item">
                <i class="el-icon-cpu"></i>
                AI辅助组卷
              </span>
            </div>
          </div>
          <div class="module-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>

        <!-- 成绩分析模块 -->
        <div class="module-card" @click="navigateTo('/allStudentsGrade')">
          <div class="module-icon">
            <i class="el-icon-data-analysis"></i>
          </div>
          <div class="module-content">
            <h3 class="module-title">成绩分析</h3>
            <p class="module-description">全面的成绩统计分析，包括分数分布、趋势分析等</p>
            <div class="module-stats">
              <span class="stat-item">
                <i class="el-icon-pie-chart"></i>
                数据可视化
              </span>
            </div>
          </div>
          <div class="module-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>

        <!-- 题库管理模块 -->
        <div class="module-card" @click="navigateTo('/selectAnswer')">
          <div class="module-icon">
            <i class="el-icon-collection"></i>
          </div>
          <div class="module-content">
            <h3 class="module-title">题库管理</h3>
            <p class="module-description">管理试题库，支持多种题型，分类管理试题资源</p>
            <div class="module-stats">
              <span class="stat-item">
                <i class="el-icon-folder-opened"></i>
                题目分类管理
              </span>
            </div>
          </div>
          <div class="module-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>

        <!-- 系统设置模块 -->
        <div class="module-card" @click="showSettings">
          <div class="module-icon">
            <i class="el-icon-setting"></i>
          </div>
          <div class="module-content">
            <h3 class="module-title">系统设置</h3>
            <p class="module-description">系统配置管理，用户权限设置，主题切换等</p>
            <div class="module-stats">
              <span class="stat-item">
                <i class="el-icon-tools"></i>
                个性化配置
              </span>
            </div>
          </div>
          <div class="module-arrow">
            <i class="el-icon-arrow-right"></i>
          </div>
        </div>
      </div>

      <!-- 快速操作区域 -->
      <div class="quick-actions">
        <h3 class="section-title">快速操作</h3>
        <div class="action-buttons">
          <el-button type="primary" icon="el-icon-plus" @click="navigateTo('/addStudent')">
            添加学生
          </el-button>
          <el-button type="success" icon="el-icon-edit" @click="navigateTo('/addExam')">
            创建考试
          </el-button>
          <el-button type="warning" icon="el-icon-magic-stick" @click="navigateTo('/autoPaper')">
            智能组卷
          </el-button>
          <el-button type="info" icon="el-icon-view" @click="navigateTo('/allStudentsGrade')">
            查看成绩
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Dashboard',
  data() {
    return {
      studentCount: 0,
      activeExamCount: 0,
      carouselImages: [
        { id: 1, url: '/api/image/教师首页轮播图1.png', alt: '教师首页轮播图1' },
        { id: 2, url: '/api/image/教师首页轮播图2.png', alt: '教师首页轮播图2' },
        { id: 3, url: '/api/image/教师首页轮播图3.png', alt: '教师首页轮播图3' },
        { id: 4, url: '/api/image/教师首页轮播图4.png', alt: '教师首页轮播图4' }
      ]
    }
  },
  created() {
    this.loadDashboardData();
  },
  methods: {
    // 导航到指定页面
    navigateTo(path) {
      // 根据当前路由判断是管理员端还是教师端
      const isAdmin = this.$route.path.startsWith('/admin');
      
      // 如果是管理员端，添加 /admin 前缀
      let targetPath = path;
      if (isAdmin) {
        // 管理员端路由映射
        const adminPaths = {
          '/studentManage': '/admin/studentManage',
          '/addStudent': '/admin/addStudent',
          '/selectExam': '/admin/selectExam',
          '/allStudentsGrade': '/admin/allStudentsGrade',
          '/selectAnswer': '/admin/selectAnswer'
        };
        targetPath = adminPaths[path] || path;
      }
      
      // 根据路径确定对应的菜单索引并发送给面包屑导航
      const pathToMenuIndex = {
        '/selectExam': '1',        // 考试管理
        '/addExam': '1',           // 添加考试
        '/selectAnswer': '2',      // 题目列表
        '/addAnswer': '2',         // 新增题目
        '/addAnswerChildren': '2', // 新增题目子页面
        '/allStudentsGrade': '3',  // 学生成绩查询
        '/selectExamToPart': '3',  // 成绩分段查询
        '/studentManage': '4',     // 学生管理
        '/addStudent': '4',        // 添加学生
        '/autoPaper': '5',         // 自动组卷
        '/aiGenerateQuestion': '5', // AI生成题目
        '/aiGrading': '5'          // AI智能改卷
      };
      
      const menuIndex = pathToMenuIndex[path];
      if (menuIndex) {
        this.bus.$emit('sendIndex', menuIndex);
      }
      
      this.$router.push(targetPath);
    },
    
    // 显示设置
    showSettings() {
      this.$message({
        message: '系统设置功能开发中...',
        type: 'info'
      });
    },
    
    // 加载仪表板数据
    loadDashboardData() {
      // 直接使用备用方案获取学生总数（更可靠）
      console.log('直接使用学生分页API获取学生总数...');
      this.fallbackGetStudentCount();
      
      // 同时尝试原始API（用于调试）
      this.$axios.get('/api/students/count').then(res => {
        console.log('原始count API响应:', res.data);
      }).catch(error => {
        console.error('原始count API失败:', error);
      });
      
      // 获取进行中的考试数量（根据当前日期判断）
      this.loadActiveExamCount();
    },
    
    // 加载进行中考试数量（根据当前系统日期判断）
    loadActiveExamCount() {
      this.$axios.get('/api/exams').then(res => {
        if (res.data.code === 200 && res.data.data) {
          const exams = res.data.data;
          const today = new Date();
          today.setHours(0, 0, 0, 0); // 只比较日期部分
          
          // 统计考试日期为今天的考试数量
          let activeCount = 0;
          exams.forEach(exam => {
            if (exam.examDate) {
              const examDate = new Date(exam.examDate);
              examDate.setHours(0, 0, 0, 0);
              // 考试日期等于今天，则为进行中考试
              if (examDate.getTime() === today.getTime()) {
                activeCount++;
              }
            }
          });
          this.activeExamCount = activeCount;
          console.log('进行中考试数量:', activeCount, '(当前日期:', today.toLocaleDateString(), ')');
        }
      }).catch(error => {
        console.error('获取考试列表失败:', error);
        this.activeExamCount = 0;
      });
    },
    
    // 备用方案：从学生管理API获取学生数据并计算总数
    fallbackGetStudentCount() {
      console.log('使用备用方案获取学生总数...');
      this.$axios.get('/api/students/1/1000/@/@/@/@/@/@').then(res => {
        console.log('备用API完整响应:', JSON.stringify(res.data, null, 2));
        if (res.data.code === 200 && res.data.data) {
          // 检查是否有total字段
          if (res.data.data.total !== undefined) {
            this.studentCount = res.data.data.total;
            console.log('从total字段获取学生总数:', this.studentCount);
          } else if (res.data.data.records && Array.isArray(res.data.data.records)) {
            // 使用records数组的长度
            this.studentCount = res.data.data.records.length;
            console.log('从records数组计算学生总数:', this.studentCount);
            console.log('records数组内容:', res.data.data.records);
          } else {
            console.warn('备用API数据结构异常:', res.data.data);
            this.studentCount = 0;
          }
        } else {
          console.warn('备用API返回错误:', res.data);
          this.studentCount = 0;
        }
      }).catch(error => {
        console.error('备用API请求失败:', error);
        this.studentCount = 0;
      });
    }
  }
}
</script>

<style lang="less" scoped>
.dashboard {
  min-height: 100vh;
  background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
  padding: 0;
  
  .dashboard-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px 20px 40px;
  }
  
  .carousel-section {
    margin-bottom: 20px;
    border-radius: 12px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    
    .carousel-image {
      width: 100%;
      height: 200px;
      object-fit: cover;
    }
    
    /deep/ .el-carousel__indicators--outside {
      margin-top: 8px;
    }
    
    /deep/ .el-carousel__button {
      width: 10px;
      height: 10px;
      border-radius: 50%;
    }
  }
  
  .welcome-section {
    text-align: center;
    margin-bottom: 50px;
    
    .welcome-title {
      font-size: 32px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 16px 0;
      background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    
    .welcome-subtitle {
      font-size: 16px;
      color: var(--text-secondary);
      margin: 0;
      opacity: 0.8;
    }
  }
  
  .modules-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
    gap: 24px;
    margin-bottom: 50px;
    
    .module-card {
      background: var(--card-bg);
      border-radius: 16px;
      padding: 24px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
      border: 1px solid var(--border-color);
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      gap: 20px;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
        border-color: var(--primary-color);
        
        .module-arrow {
          transform: translateX(4px);
        }
      }
      
      .module-icon {
        flex-shrink: 0;
        width: 60px;
        height: 60px;
        border-radius: 12px;
        background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
        display: flex;
        align-items: center;
        justify-content: center;
        
        i {
          font-size: 28px;
          color: white;
        }
      }
      
      .module-content {
        flex: 1;
        
        .module-title {
          font-size: 18px;
          font-weight: 600;
          color: var(--text-primary);
          margin: 0 0 8px 0;
        }
        
        .module-description {
          font-size: 14px;
          color: var(--text-secondary);
          margin: 0 0 12px 0;
          line-height: 1.5;
        }
        
        .module-stats {
          .stat-item {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: var(--text-tertiary);
            background: var(--bg-secondary);
            padding: 4px 8px;
            border-radius: 4px;
            
            i {
              font-size: 12px;
            }
          }
        }
      }
      
      .module-arrow {
        flex-shrink: 0;
        color: var(--text-tertiary);
        transition: transform 0.3s ease;
        
        i {
          font-size: 16px;
        }
      }
    }
  }
  
  .quick-actions {
    text-align: center;
    
    .section-title {
      font-size: 20px;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 24px 0;
    }
    
    .action-buttons {
      display: flex;
      justify-content: center;
      gap: 16px;
      flex-wrap: wrap;
      
      .el-button {
        height: 44px;
        padding: 0 24px;
        border-radius: 8px;
        font-weight: 500;
        font-size: 14px;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .dashboard {
    .dashboard-container {
      padding: 20px 16px;
    }
    
    .modules-grid {
      grid-template-columns: 1fr;
      gap: 16px;
      
      .module-card {
        padding: 20px;
        
        .module-icon {
          width: 50px;
          height: 50px;
          
          i {
            font-size: 24px;
          }
        }
      }
    }
    
    .welcome-section {
      .welcome-title {
        font-size: 24px;
      }
    }
    
    .quick-actions {
      .action-buttons {
        .el-button {
          height: 40px;
          padding: 0 20px;
          font-size: 13px;
        }
      }
    }
  }
}
</style>
