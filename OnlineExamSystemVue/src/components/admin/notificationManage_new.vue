<template>
  <div class="notification-manage">
    <el-card class="box-card">
      <!-- 发送通知区域 -->
      <div slot="header" class="clearfix">
        <span class="card-title">
          <i class="el-icon-s-promotion"></i> 通知管理
        </span>
      </div>

      <!-- 发送通知表单 -->
      <div class="send-section">
        <h3><i class="el-icon-edit-outline"></i> 发送通知</h3>
        <el-form :model="notifyForm" :rules="rules" ref="notifyForm" label-width="100px">
          <el-form-item label="通知类型" prop="type">
            <el-select v-model="notifyForm.type" placeholder="请选择通知类型" style="width: 200px;">
              <el-option label="系统通知" value="system"></el-option>
              <el-option label="考试通知" value="exam"></el-option>
              <el-option label="成绩通知" value="score"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="接收对象" prop="target">
            <el-select v-model="notifyForm.target" placeholder="请选择接收对象" style="width: 200px;" @change="handleTargetChange">
              <el-option label="全体学生" value="all_students"></el-option>
              <el-option label="指定学院" value="institute"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="目标学院" prop="institute" v-if="notifyForm.target === 'institute'">
            <el-select v-model="notifyForm.institute" placeholder="请选择学院" style="width: 300px;">
              <el-option v-for="institute in institutes" :key="institute" :label="institute" :value="institute"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="通知标题" prop="title">
            <el-input v-model="notifyForm.title" placeholder="请输入通知标题" style="width: 400px;"></el-input>
          </el-form-item>
          
          <el-form-item label="通知内容" prop="content">
            <el-input
              type="textarea"
              v-model="notifyForm.content"
              placeholder="请输入通知内容"
              :rows="4"
              style="width: 600px;">
            </el-input>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="sendNotification" :loading="sending">
              <i class="el-icon-s-promotion"></i> 发送通知
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-divider>历史通知记录</el-divider>

      <!-- 搜索区域 -->
      <div class="search-section">
        <el-form :inline="true" class="search-form">
          <el-form-item label="标题">
            <el-input v-model="searchForm.title" placeholder="搜索通知标题" clearable style="width: 200px;"></el-input>
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="searchForm.type" placeholder="选择类型" clearable style="width: 120px;">
              <el-option label="系统通知" value="system"></el-option>
              <el-option label="考试通知" value="exam"></el-option>
              <el-option label="成绩通知" value="score"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="发布者">
            <el-input v-model="searchForm.publisher" placeholder="搜索发布者" clearable style="width: 150px;"></el-input>
          </el-form-item>
          <el-form-item label="身份">
            <el-select v-model="searchForm.role" placeholder="选择身份" clearable style="width: 100px;">
              <el-option label="管理员" value="管理员"></el-option>
              <el-option label="教师" value="教师"></el-option>
              <el-option label="学生" value="学生"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd"
              style="width: 240px;"
              clearable>
            </el-date-picker>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
            <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 通知历史列表 -->
      <el-table :data="paginatedNotifications" style="width: 100%" v-loading="loading" stripe>
        <el-table-column prop="displayId" label="ID" width="80"></el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getTypeColor(scope.row.type)" size="small">
              {{ getTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="200"></el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="targetDesc" label="发布者" width="120"></el-table-column>
        <el-table-column prop="createTime" label="发送时间" width="160"></el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="danger" @click="deleteNotification(scope.row)">
              <i class="el-icon-delete"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
        :current-page="currentPage"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalNotifications">
      </el-pagination>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'NotificationManage',
  data() {
    return {
      loading: false,
      sending: false,
      notifyForm: {
        type: 'system',
        target: 'all_students',
        institute: '',
        title: '',
        content: ''
      },
      rules: {
        type: [{ required: true, message: '请选择通知类型', trigger: 'change' }],
        target: [{ required: true, message: '请选择接收对象', trigger: 'change' }],
        title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
      },
      institutes: ['软件工程学院', '计算机学院', '信息工程学院', '数学与统计学院', '电子工程学院', '人工智能学院', '网络空间安全学院', '大数据学院'],
      notifications: [],
      filteredNotifications: [],
      currentPage: 1,
      pageSize: 10,
      totalNotifications: 0,
      searchForm: {
        title: '',
        type: '',
        publisher: '',
        role: '',
        dateRange: null
      },
      currentThemeColor: '#6366f1',
      themeObserver: null
    }
  },
  computed: {
    // 分页后的通知数据
    paginatedNotifications() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredNotifications.slice(start, end).map((notification, index) => ({
        ...notification,
        displayId: start + index + 1 // 正序ID
      }))
    }
  },
  created() {
    this.loadNotifications();
    this.updateThemeColor();
  },
  mounted() {
    // 监听主题变化
    this.themeObserver = new MutationObserver(() => {
      this.updateThemeColor();
    });
    this.themeObserver.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['style']
    });
  },
  beforeDestroy() {
    if (this.themeObserver) {
      this.themeObserver.disconnect();
    }
  },
  methods: {
    // 更新主题色
    updateThemeColor() {
      const themeColor = getComputedStyle(document.documentElement).getPropertyValue('--admin-primary').trim();
      if (themeColor) {
        this.currentThemeColor = themeColor;
      }
    },
    
    // 加载通知历史 - 从学生交流区获取数据
    async loadNotifications() {
      this.loading = true
      try {
        // 从学生交流区API获取留言数据作为通知数据
        const res = await this.$axios.get('/api/messages/1/100')
        if (res.data && res.data.code === 200) {
          const messages = res.data.data.records || []
          // 将交流区留言转换为通知格式
          this.notifications = messages.map(msg => ({
            id: msg.id,
            type: this.getMessageType(msg.title, msg.content),
            title: msg.title,
            content: msg.content,
            target: 'all_students',
            targetDesc: this.formatPublisherInfo(msg.publisherName, msg.publisherRole),
            createTime: msg.time,
            originalMessage: msg // 保存原始消息数据
          }))
          // 初始化过滤数据
          this.filteredNotifications = [...this.notifications]
          this.totalNotifications = this.filteredNotifications.length
          console.log('从交流区加载到', this.notifications.length, '条消息')
        } else {
          console.log('交流区API返回错误:', res.data)
          this.notifications = []
          this.totalNotifications = 0
        }
      } catch (error) {
        console.error('从交流区加载数据失败:', error)
        this.notifications = []
        this.totalNotifications = 0
      } finally {
        this.loading = false
      }
    },
    
    // 根据留言内容判断消息类型
    getMessageType(title, content) {
      const text = (title + ' ' + content).toLowerCase()
      if (text.includes('考试') || text.includes('exam')) {
        return 'exam'
      } else if (text.includes('成绩') || text.includes('分数') || text.includes('score')) {
        return 'score'
      } else {
        return 'system'
      }
    },

    // 格式化发布者信息为"姓名（身份）"格式
    formatPublisherInfo(publisherName, publisherRole) {
      if (!publisherName) {
        return '匿名（学生）'
      }
      
      // 确定身份
      let role = '学生' // 默认身份
      if (publisherRole) {
        if (publisherRole.includes('管理员') || publisherRole.includes('admin')) {
          role = '管理员'
        } else if (publisherRole.includes('教师') || publisherRole.includes('teacher')) {
          role = '教师'
        } else if (publisherRole.includes('学生') || publisherRole.includes('student')) {
          role = '学生'
        } else {
          role = publisherRole
        }
      }
      
      return `${publisherName}（${role}）`
    },

    // 发送通知 - 发布到学生交流区，并实现学院筛选
    sendNotification() {
      this.$refs.notifyForm.validate(async valid => {
        if (valid) {
          this.sending = true
          try {
            // 获取管理员信息
            const adminId = sessionStorage.getItem('cid') || this.$cookies.get('cid') || '0'
            const adminName = sessionStorage.getItem('cname') || this.$cookies.get('cname') || '管理员'
            
            // 构建消息数据，发布到学生交流区
            const now = new Date()
            const formattedDate = now.getFullYear() + '-' + 
                                String(now.getMonth() + 1).padStart(2, '0') + '-' + 
                                String(now.getDate()).padStart(2, '0')
            
            const messageData = {
              studentId: adminId,
              title: this.notifyForm.title,
              content: this.notifyForm.content,
              time: formattedDate,
              publisherName: adminName,
              publisherRole: '管理员',
              targetType: this.notifyForm.target,
              targetInstitute: this.notifyForm.target === 'institute' ? this.notifyForm.institute : null
            }
            
            console.log('发送通知到交流区:', messageData)
            
            // 发布消息到学生交流区
            const res = await this.$axios({
              url: '/api/message',
              method: 'post',
              data: messageData
            })
            
            if (res.data && (res.data.code === 200 || res.data === 1)) {
              this.$message.success(`通知发送成功，已发布到学生交流区`)
              this.resetForm()
              // 重新加载数据以显示新发布的通知
              this.loadNotifications()
            } else {
              this.$message.error('发送失败：' + (res.data.message || '未知错误'))
            }
          } catch (error) {
            console.error('发送通知失败:', error)
            this.$message.error('发送失败：' + (error.message || '网络错误'))
          } finally {
            this.sending = false
          }
        }
      })
    },

    // 处理目标变化
    handleTargetChange(value) {
      if (value !== 'institute') {
        this.notifyForm.institute = ''
      }
    },

    // 获取类型颜色
    getTypeColor(type) {
      const colorMap = {
        'system': '',
        'exam': 'warning',
        'score': 'success'
      }
      return colorMap[type] || ''
    },

    // 获取类型名称
    getTypeName(type) {
      const nameMap = {
        'system': '系统通知',
        'exam': '考试通知',
        'score': '成绩通知'
      }
      return nameMap[type] || type
    },

    // 搜索功能
    handleSearch() {
      this.filteredNotifications = this.notifications.filter(notification => {
        const titleMatch = !this.searchForm.title || 
                          notification.title.toLowerCase().includes(this.searchForm.title.toLowerCase())
        const typeMatch = !this.searchForm.type || notification.type === this.searchForm.type
        const publisherMatch = !this.searchForm.publisher || 
                              notification.targetDesc.toLowerCase().includes(this.searchForm.publisher.toLowerCase())
        const roleMatch = !this.searchForm.role || 
                         notification.targetDesc.includes(`（${this.searchForm.role}）`)
        
        // 时间范围筛选
        let dateMatch = true
        if (this.searchForm.dateRange && this.searchForm.dateRange.length === 2) {
          const startDate = new Date(this.searchForm.dateRange[0])
          const endDate = new Date(this.searchForm.dateRange[1])
          endDate.setHours(23, 59, 59, 999) // 设置为当天的最后一刻
          
          if (notification.createTime) {
            const notificationDate = new Date(notification.createTime)
            dateMatch = notificationDate >= startDate && notificationDate <= endDate
          }
        }
        
        return titleMatch && typeMatch && publisherMatch && roleMatch && dateMatch
      })
      this.totalNotifications = this.filteredNotifications.length
      this.currentPage = 1 // 重置到第一页
    },

    // 重置搜索
    resetSearch() {
      this.searchForm = {
        title: '',
        type: '',
        publisher: '',
        role: '',
        dateRange: null
      }
      this.filteredNotifications = [...this.notifications]
      this.totalNotifications = this.filteredNotifications.length
      this.currentPage = 1
    },

    resetForm() {
      this.$refs.notifyForm.resetFields()
    },

    // 删除通知 - 删除学生交流区的消息
    deleteNotification(notification) {
      this.$confirm('确定删除这条消息吗？删除后学生交流区中的对应消息也会被删除。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          console.log('删除交流区消息:', notification.id)
          
          const res = await this.$axios({
            url: `/api/message/${notification.id}`,
            method: 'delete'
          })
          
          if (res.data && res.data.code === 200) {
            this.$message.success('删除成功')
            this.loadNotifications() // 重新加载数据
          } else {
            this.$message.error('删除失败：' + (res.data.message || '未知错误'))
          }
        } catch (error) {
          console.error('删除消息失败:', error)
          this.$message.error('删除失败：' + (error.message || '网络错误'))
        }
      }).catch(() => {
        // 用户取消删除
      })
    },

    // 分页相关
    handleCurrentChange(page) {
      this.currentPage = page
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
    }
  }
}
</script>

<style lang="less" scoped>
.notification-manage {
  padding: 20px;
  background-color: var(--bg-primary);
}

.box-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: 1px solid var(--border-color);
  background-color: var(--card-bg);
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  
  i {
    margin-right: 8px;
    color: var(--primary-color);
  }
}

.send-section {
  margin-bottom: 30px;
  
  h3 {
    color: var(--text-primary);
    margin-bottom: 20px;
    font-size: 16px;
    font-weight: 600;
    display: flex;
    align-items: center;
    
    i {
      margin-right: 8px;
      color: var(--primary-color);
    }
  }
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: var(--bg-secondary);
  border-radius: 6px;
  border: 1px solid var(--border-color);
}

.search-form {
  .el-form-item {
    margin-bottom: 10px;
  }
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

// 主题适配
.pagination /deep/ .el-pagination .btn-next,
.pagination /deep/ .el-pagination .btn-prev {
  background-color: var(--card-bg);
  color: var(--text-primary);
  border-color: var(--border-color);
}

.pagination /deep/ .el-pagination .el-pager li {
  background-color: var(--card-bg);
  color: var(--text-primary);
  border-color: var(--border-color);
}

.pagination /deep/ .el-pagination .el-pager li.active {
  background-color: var(--admin-primary, #6366f1);
  border-color: var(--admin-primary, #6366f1);
}

.pagination /deep/ .el-pagination .el-pagination__jump {
  color: #606266;
}
</style>
