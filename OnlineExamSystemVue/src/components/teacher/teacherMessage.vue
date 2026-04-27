// 教师端交流区页面
<template>
  <div id="teacher-message" class="teacher-container">
    <div class="title" style="font-size: 24px;">
      <i class="iconfont icon-r-refresh" style="font-size: 30px;"></i>
      交流区
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-form :inline="true" class="search-form">
        <el-form-item label="发布者">
          <el-input v-model="searchName" placeholder="搜索发布者姓名" clearable @clear="searchMessages" style="width: 150px;"></el-input>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="searchTitle" placeholder="搜索留言标题" clearable @clear="searchMessages" style="width: 150px;"></el-input>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            style="width: 240px;"
            unlink-panels
            @change="searchMessages">
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="searchMessages">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="wrapper">
      <div class="title1">
        <el-input placeholder="留言标题" v-model="title" clearable>
        </el-input>
      </div>
      <div class="content">
        <el-input type="textarea" :rows="3" placeholder="留言内容" v-model="content" clearable>
        </el-input>
      </div>
      <div class="btn">
        <el-button type="primary" @click="submit()" style="font-size: 18px;"> 发布留言</el-button>
      </div>
    </div>

    <!-- 留言列表 -->
    <div class="message-list">
      <div class="list-header">
        <h3>留言列表</h3>
        <span class="total-count">共 {{ filteredMessages.length }} 条留言</span>
      </div>
      
      <div class="messages" v-if="filteredMessages.length > 0">
        <div v-for="(message, index) in paginatedMessages" :key="index" class="message-item">
          <div class="message-header">
            <div class="message-info">
              <span class="message-title">{{ message.title }}</span>
              <span class="message-meta">
                <span class="publisher">{{ formatPublisherInfo(message.publisherName, message.publisherRole) }}</span>
                <span class="publish-time">{{ message.time }}</span>
              </span>
            </div>
            <div class="message-actions">
              <el-button size="mini" type="primary" @click="viewReplies(message, index)">
                查看回复 ({{ message.replies ? message.replies.length : 0 }})
              </el-button>
              <el-button size="mini" type="success" @click="replyToMessage(message, index)">回复</el-button>
              <el-button size="mini" type="danger" @click="deleteMessage(message, index)" :loading="message.deleting" v-if="isMyMessage(message.studentId)">
                删除
              </el-button>
            </div>
          </div>
          <div class="message-content">{{ message.content }}</div>
          
          <!-- 回复列表 -->
          <div v-if="message.showReplies && message.replies && message.replies.length > 0" class="replies-section">
            <div class="replies-header">
              <i class="el-icon-chat-line-round"></i>
              <span>回复列表</span>
            </div>
            <div v-for="(reply, replyIndex) in message.replies" :key="replyIndex" class="reply-item">
              <div class="reply-header">
                <span class="replier-name">{{ reply.replayerName }}</span>
                <span class="reply-time">{{ reply.replayTime }}</span>
              </div>
              <div class="reply-content">{{ reply.replay }}</div>
            </div>
          </div>
          
          <!-- 回复输入框 -->
          <div v-if="message.showReplyInput" class="reply-input-section">
            <el-input
              type="textarea"
              :rows="2"
              placeholder="请输入回复内容..."
              v-model="message.replyContent"
              maxlength="500"
              show-word-limit>
            </el-input>
            <div class="reply-actions">
              <el-button size="small" @click="cancelReply(message)">取消</el-button>
              <el-button size="small" type="primary" @click="submitReply(message, index)">发送回复</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="no-messages">
        <i class="el-icon-chat-line-round"></i>
        <p>暂无留言，快来发布第一条留言吧！</p>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="filteredMessages.length > pageSize">
        <el-pagination
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="filteredMessages.length">
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TeacherMessage',
  data() {
    return {
      title: '',
      content: '',
      messages: [],
      filteredMessages: [],
      searchName: '',
      searchTitle: '',
      searchDateRange: [],
      currentPage: 1,
      pageSize: 10,
      user: {
        userId: null,
        userName: '',
        role: '1' // 教师角色
      }
    }
  },
  computed: {
    paginatedMessages() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredMessages.slice(start, end);
    }
  },
  mounted() {
    this.getUserInfo();
    this.getAllMessage();
  },
  methods: {
    getUserInfo() {
      // 获取教师用户信息
      const userId = sessionStorage.getItem("cid") || this.$cookies.get("cid");
      const userName = sessionStorage.getItem("cname") || this.$cookies.get("cname");
      
      this.user.userId = userId;
      this.user.userName = userName;
    },
    isMyMessage(studentId) {
      return String(studentId) === String(this.user.userId)
    },
    
    submit() {
      if (!this.title.trim()) {
        this.$message.warning('请输入留言标题');
        return;
      }
      if (!this.content.trim()) {
        this.$message.warning('请输入留言内容');
        return;
      }
      
      const messageData = {
        title: this.title,
        content: this.content,
        studentId: this.user.userId,
        publisherName: this.user.userName,
        publisherRole: '教师'
      };
      
      this.$axios.post('/api/message/add', messageData)
        .then(res => {
          if (res.data.code === 200) {
            this.$message.success('留言发布成功');
            this.title = '';
            this.content = '';
            this.getAllMessage();
          } else {
            this.$message.error('留言发布失败：' + res.data.message);
          }
        })
        .catch(err => {
          this.$message.error('留言发布失败');
          console.error(err);
        });
    },
    
    getAllMessage() {
      this.$axios.get('/api/message/findAll')
        .then(res => {
          if (res.data.code === 200) {
            this.messages = res.data.data || [];
            this.searchMessages();
          }
        })
        .catch(err => {
          console.error('获取留言失败:', err);
        });
    },
    
    searchMessages() {
      let filtered = [...this.messages];
      
      if (this.searchName) {
        filtered = filtered.filter(msg => 
          msg.publisherName && msg.publisherName.includes(this.searchName)
        );
      }
      
      if (this.searchTitle) {
        filtered = filtered.filter(msg => 
          msg.title && msg.title.includes(this.searchTitle)
        );
      }
      
      if (this.searchDateRange && this.searchDateRange.length === 2) {
        const startDate = new Date(this.searchDateRange[0]);
        const endDate = new Date(this.searchDateRange[1]);
        endDate.setHours(23, 59, 59, 999);
        
        filtered = filtered.filter(msg => {
          const msgDate = new Date(msg.time);
          return msgDate >= startDate && msgDate <= endDate;
        });
      }
      
      this.filteredMessages = filtered;
      this.currentPage = 1;
    },
    
    resetSearch() {
      this.searchName = '';
      this.searchTitle = '';
      this.searchDateRange = [];
      this.searchMessages();
    },
    
    formatPublisherInfo(publisherName, publisherRole) {
      if (!publisherName) {
        return '';
      }
      
      let role = '学生';
      if (publisherRole) {
        if (publisherRole.includes('管理员') || publisherRole.includes('admin')) {
          role = '管理员';
        } else if (publisherRole.includes('教师') || publisherRole.includes('teacher')) {
          role = '教师';
        } else if (publisherRole.includes('学生') || publisherRole.includes('student')) {
          role = '学生';
        } else {
          role = publisherRole;
        }
      }
      
      return `${publisherName}（${role}）`;
    },
    
    viewReplies(message, index) {
      this.$set(message, 'showReplies', !message.showReplies);
      
      if (message.showReplies && (!message.replies || message.replies.length === 0)) {
        this.getReplies(message);
      }
    },
    
    getReplies(message) {
      this.$axios.get(`/api/replay/findByMessageId/${message.id}`)
        .then(res => {
          if (res.data.code === 200) {
            this.$set(message, 'replies', res.data.data || []);
          }
        })
        .catch(err => {
          console.error('获取回复失败:', err);
        });
    },
    
    replyToMessage(message, index) {
      this.$set(message, 'showReplyInput', true);
      this.$set(message, 'replyContent', '');
    },
    
    cancelReply(message) {
      this.$set(message, 'showReplyInput', false);
      this.$set(message, 'replyContent', '');
    },
    
    submitReply(message, index) {
      if (!message.replyContent || !message.replyContent.trim()) {
        this.$message.warning('请输入回复内容');
        return;
      }
      
      const replyData = {
        messageId: message.id,
        replay: message.replyContent,
        replayerName: this.user.userName,
        replayerId: this.user.userId
      };
      
      this.$axios.post('/api/replay/add', replyData)
        .then(res => {
          if (res.data.code === 200) {
            this.$message.success('回复成功');
            this.cancelReply(message);
            this.getReplies(message);
          } else {
            this.$message.error('回复失败：' + res.data.message);
          }
        })
        .catch(err => {
          this.$message.error('回复失败');
          console.error(err);
        });
    },
    
    handleCurrentChange(page) {
      this.currentPage = page;
    },
    
    // 删除留言（教师权限）
    deleteMessage(message, index) {
      this.$confirm('确定要删除这条留言吗？删除后将无法恢复。', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$set(message, 'deleting', true);
        
        this.$axios.delete(`/api/message/delete/${message.id}`)
          .then(res => {
            if (res.data.code === 200) {
              this.$message.success('留言删除成功');
              this.getAllMessage(); // 重新加载留言列表
            } else {
              this.$message.error('删除失败：' + res.data.message);
            }
          })
          .catch(err => {
            this.$message.error('删除失败');
            console.error(err);
          })
          .finally(() => {
            this.$set(message, 'deleting', false);
          });
      }).catch(() => {
        // 用户取消删除
      });
    }
  }
}
</script>

<style scoped>
.teacher-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.title {
  color: #409EFF;
  margin-bottom: 20px;
  font-weight: bold;
}

.search-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.search-form {
  margin: 0;
}

.wrapper {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.title1 {
  margin-bottom: 15px;
}

.content {
  margin-bottom: 15px;
}

.btn {
  text-align: right;
}

.message-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.list-header {
  padding: 20px 20px 10px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header h3 {
  margin: 0;
  color: #303133;
}

.total-count {
  color: #909399;
  font-size: 14px;
}

.messages {
  padding: 0 20px 20px;
}

.message-item {
  border-bottom: 1px solid #f0f0f0;
  padding: 15px 0;
}

.message-item:last-child {
  border-bottom: none;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.message-info {
  flex: 1;
}

.message-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: block;
  margin-bottom: 5px;
}

.message-meta {
  font-size: 12px;
  color: #909399;
}

.publisher {
  margin-right: 15px;
  font-weight: bold;
  color: #409EFF;
}

.message-actions {
  display: flex;
  gap: 8px;
}

.message-content {
  color: #606266;
  line-height: 1.6;
  margin-bottom: 10px;
}

.replies-section {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 15px;
  margin-top: 15px;
}

.replies-header {
  color: #409EFF;
  font-weight: bold;
  margin-bottom: 10px;
}

.replies-header i {
  margin-right: 5px;
}

.reply-item {
  background: white;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 8px;
}

.reply-item:last-child {
  margin-bottom: 0;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  font-size: 12px;
}

.replier-name {
  font-weight: bold;
  color: #409EFF;
}

.reply-time {
  color: #909399;
}

.reply-content {
  color: #606266;
  font-size: 14px;
}

.reply-input-section {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.reply-actions {
  margin-top: 10px;
  text-align: right;
}

.reply-actions .el-button {
  margin-left: 8px;
}

.no-messages {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.no-messages i {
  font-size: 48px;
  margin-bottom: 15px;
  display: block;
}

.pagination-wrapper {
  padding: 20px;
  text-align: center;
  border-top: 1px solid #ebeef5;
}
</style>
