// 给我留言页面
<template>
  <div id="message">

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
        <el-button type="primary" @click="submit()" style="font-size: 18px;"> 提交留言</el-button>
      </div>
      <div class="all">
        <ul class="msglist">
          <li class="list" :id="'message-' + data.id" @mouseenter="enter(index)" @mouseleave="leave(index)" v-for="(data, index) in msg" :key="index" :class="{ 'highlight-message': highlightMessageId == data.id }">
            <p class="title" style="font-size: 20px;"> 
              <i class="iconfont icon-r-user2" style="font-size: 24px;margin-right: 10px;"></i>{{ data.title }}
              <span class="publisher-info" v-if="data.publisherName">
                {{ formatPublisherInfo(data.publisherName, data.publisherRole) }}
              </span>
            </p><br>
            <p class="content">{{ data.content }}</p>
            <p class="date">
              <i class="iconfont icon-date"></i>{{ data.time }}
              <span class="message-delete-btn" @click="deleteMessage(data.id)" v-if="isMyMessage(data.studentId)">删除留言</span>
            </p>
            <div class="comments-section">
              <div v-for="(replayData, index2) in data.replays" :key="index2" :id="'replay-' + replayData.replayId" class="comment" :class="{ 'is-reply': replayData.replyToName, 'highlight-replay': highlightReplayId == replayData.replayId }">
                <div class="comment-main">
                  <i class="iconfont icon-huifuxiaoxi"></i>
                  <div class="comment-body">
                    <span class="replayer-name">{{ formatName(replayData.replayerName) }}</span>
                    <template v-if="replayData.replyToName">
                      <span class="reply-arrow">回复</span>
                      <span class="reply-to-name">@{{ formatName(replayData.replyToName) }}</span>
                    </template>
                    <span class="separator">：</span>
                    <span class="replay-text">{{ replayData.replay }}</span>
                  </div>
                </div>
                <div class="comment-footer">
                  <span class="replay-time">{{ replayData.replayTime }}</span>
                  <span class="reply-btn" @click="replyToComment(data.id, replayData.replayerName, replayData.replayerId)" v-if="flag && index == current">回复</span>
                  <span class="delete-btn" @click="deleteReplay(replayData.replayId, data.id)" v-if="isMyReplay(replayData.replayerId)">删除</span>
                </div>
              </div>
            </div>
            <span class="replay" @click="replay(data.id)" v-if="flag && index == current">回复</span>
          </li>
        </ul>
      </div>
      <div class="pagination">
        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
          :current-page="pagination.current" :page-sizes="[4, 6, 8, 10]" :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper" :total="pagination.total">
        </el-pagination>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Message',
  data() {
    return {
      flag: false,
      current: 0,
      title: "",
      content: "",
      // 搜索相关
      searchName: "",
      searchTitle: "",
      searchDateRange: null,
      // 高亮相关
      highlightMessageId: null,
      highlightReplayId: null,
      pagination: { //分页后的留言列表
        current: 1, //当前页
        total: null, //记录条数
        size: 4 //每页条数
      },
      msg: [],
      allMsg: [] // 保存所有留言用于前端筛选
    }
  },
  created() {
    this.getMsg()
  },
  mounted() {
    // 处理从通知跳转过来的高亮
    this.checkHighlight()
  },
  watch: {
    '$route': {
      handler(to) {
        console.log('路由变化:', to.path, to.query)
        if (to.path === '/message' && to.query.hl === '1') {
          this.checkHighlight()
        }
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    // 格式化发布者信息为"姓名（身份）"格式
    formatPublisherInfo(publisherName, publisherRole) {
      if (!publisherName) {
        return ''
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
      
      return ` ${publisherName}（${role}）`
    },

    // 检查并处理高亮（从 URL 参数读取）
    checkHighlight() {
      const { hl, mid, rid } = this.$route.query
      console.log('checkHighlight - URL参数:', { hl, mid, rid })
      
      if (hl !== '1') return
      
      this.highlightMessageId = mid ? parseInt(mid) : null
      this.highlightReplayId = rid ? parseInt(rid) : null
      console.log('设置高亮ID - messageId:', this.highlightMessageId, 'replayId:', this.highlightReplayId)
      
      // 先获取所有数据确保能找到目标回复
      this.$axios(`/api/messages/1/100`).then(res => {
        if (res.data.code == 200) {
          this.msg = res.data.data.records
          this.pagination.total = res.data.data.total
          
          // 延迟滚动，等待DOM更新
          this.$nextTick(() => {
            setTimeout(() => {
              this.scrollToHighlight()
            }, 500)
          })
        }
      })
      
      // 10秒后清除高亮并恢复正常分页
      setTimeout(() => {
        this.highlightMessageId = null
        this.highlightReplayId = null
        this.$router.replace('/message')
        this.getMsg()
      }, 10000)
    },
    // 滚动到高亮位置
    scrollToHighlight() {
      console.log('scrollToHighlight - replayId:', this.highlightReplayId, 'messageId:', this.highlightMessageId)
      let targetEl = null
      
      // 打印所有可用的回复ID
      const allReplays = document.querySelectorAll('[id^="replay-"]')
      console.log('页面上的所有回复元素:', Array.from(allReplays).map(el => el.id))
      
      if (this.highlightReplayId) {
        targetEl = document.getElementById('replay-' + this.highlightReplayId)
        console.log('查找元素 replay-' + this.highlightReplayId, '结果:', targetEl)
      } else if (this.highlightMessageId) {
        targetEl = document.getElementById('message-' + this.highlightMessageId)
        console.log('查找元素 message-' + this.highlightMessageId, '结果:', targetEl)
      }
      
      if (targetEl) {
        // 直接添加高亮样式
        targetEl.style.backgroundColor = '#fff8e6'
        targetEl.style.borderLeft = '4px solid #e6a23c'
        targetEl.style.boxShadow = '0 0 20px rgba(230, 162, 60, 0.5)'
        targetEl.style.transition = 'all 0.3s ease'
        
        targetEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
        
        this.$message({
          message: '已定位到相关回复',
          type: 'success',
          duration: 2000
        })
        
        // 8秒后移除高亮
        setTimeout(() => {
          targetEl.style.backgroundColor = ''
          targetEl.style.borderLeft = ''
          targetEl.style.boxShadow = ''
        }, 8000)
      } else {
        console.log('未找到目标元素')
        // 如果找不到特定回复，尝试滚动到留言
        if (this.highlightMessageId) {
          const msgEl = document.getElementById('message-' + this.highlightMessageId)
          if (msgEl) {
            msgEl.style.backgroundColor = '#fff8e6'
            msgEl.style.borderLeft = '4px solid #e6a23c'
            msgEl.style.boxShadow = '0 0 20px rgba(230, 162, 60, 0.5)'
            msgEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
            this.$message({
              message: '已定位到相关留言',
              type: 'success',
              duration: 2000
            })
            setTimeout(() => {
              msgEl.style.backgroundColor = ''
              msgEl.style.borderLeft = ''
              msgEl.style.boxShadow = ''
            }, 8000)
          }
        }
      }
    },
    getMsg() {
      this.$axios(`/api/messages/${this.pagination.current}/${this.pagination.size}`).then(res => {
        let status = res.data.code
        if (status == 200) {
          this.msg = res.data.data.records
          this.allMsg = res.data.data.records // 保存所有留言用于筛选
          const data = res.data.data;
          this.pagination = {
            ...data,
            current: parseInt(data.current) || 1,
            total: parseInt(data.total) || 0,
            size: parseInt(data.size) || 4
          };
        }
      })
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = parseInt(val) || 4
      this.pagination.current = 1 // 重置到第一页
      this.getMsg()
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = parseInt(val) || 1
      this.getMsg()
    },
    // 搜索留言 - 先获取所有数据再筛选
    searchMessages() {
      // 获取所有留言（使用大的分页数）
      this.$axios(`/api/messages/1/1000`).then(res => {
        if (res.data.code == 200) {
          let allData = res.data.data.records || []
          let filtered = [...allData]
          
          // 按发布者姓名筛选（模糊搜索）
          if (this.searchName) {
            filtered = filtered.filter(item => 
              item.publisherName && item.publisherName.includes(this.searchName)
            )
          }
          
          // 按标题筛选
          if (this.searchTitle) {
            filtered = filtered.filter(item => 
              item.title && item.title.includes(this.searchTitle)
            )
          }
          
          // 按时间范围筛选
          if (this.searchDateRange && this.searchDateRange.length === 2) {
            const startDate = new Date(this.searchDateRange[0])
            const endDate = new Date(this.searchDateRange[1])
            endDate.setHours(23, 59, 59, 999)
            
            filtered = filtered.filter(item => {
              if (!item.time) return false
              const itemDate = new Date(item.time)
              return itemDate >= startDate && itemDate <= endDate
            })
          }
          
          // 更新显示
          this.msg = filtered
          this.pagination.total = filtered.length
        }
      })
    },
    // 重置搜索
    resetSearch() {
      this.searchName = ""
      this.searchTitle = ""
      this.searchDateRange = null
      this.getMsg()
    },
    // 判断是否是自己的评论
    isMyReplay(replayerId) {
      const currentUserId = sessionStorage.getItem("cid") || this.$cookies.get("cid")
      return String(replayerId) === String(currentUserId)
    },
    // 判断是否是自己的留言
    isMyMessage(studentId) {
      const currentUserId = sessionStorage.getItem("cid") || this.$cookies.get("cid")
      return String(studentId) === String(currentUserId)
    },
    // 删除留言
    deleteMessage(messageId) {
      this.$confirm('确定要删除这条留言吗？删除后相关评论也会被删除。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`/api/message/${messageId}`).then(res => {
          if (res.data === 1 || res.data.code === 200) {
            this.$message.success('删除成功')
            this.getMsg()
          } else {
            this.$message.error('删除失败')
          }
        })
      }).catch(() => {})
    },
    // 删除评论
    deleteReplay(replayId, messageId) {
      this.$confirm('确定要删除这条评论吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`/api/replay/${replayId}`).then(res => {
          if (res.data.code === 200) {
            this.$message.success('删除成功')
            this.getMsg()
          } else {
            this.$message.error('删除失败')
          }
        })
      }).catch(() => {})
    },
    // formatTime(date) { //日期格式化
    //   let year = date.getFullYear()
    //   let month= date.getMonth()+ 1 < 10 ? "0" + (date.getMonth() + 1) : date.getMonth() + 1;
    //   let day=date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
    //   let hours=date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
    //   let minutes=date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
    //   let seconds=date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
    //   // 拼接
    //   return year+"-"+month+"-"+day+" "+hours+":"+minutes+":"+seconds;
    // },
    submit() {
      let date = new Date()
      if (this.title.length == 0 || this.content.length == 0) { //非空判断
        this.$message({
          type: 'error',
          message: '留言标题或内容不能为空',
        })
      } else {
        const studentId = sessionStorage.getItem("cid") || this.$cookies.get("cid")
        this.$axios({
          url: "/api/message",
          method: "post",
          data: {
            studentId: studentId,
            title: this.title,
            content: this.content,
            time: date
          }
        }).then(res => {
          let code = res.data.code
          if (code == 200) {
            this.$message({
              type: "success",
              message: "留言成功"
            })
          }
          this.getMsg()
        })
      }
      this.title = ""
      this.content = ""
      this.getMsg()
    },
    replay(messageId) { //回复留言功能
      this.$prompt('回复留言', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^[\s\S]*.*[^\s][\s\S]*$/,
        inputErrorMessage: '回复不能为空'
      }).then(({ value }) => {
        let date = new Date()
        let replayerName = sessionStorage.getItem("cname") || this.$cookies.get("cname") || '匿名用户'
        console.log(messageId)
        // 获取留言发布者信息用于发送通知
        const messageData = this.msg.find(m => m.id === messageId)
        console.log('留言数据:', messageData)
        const receiverId = messageData ? messageData.studentId : null
        console.log('接收者ID:', receiverId)
        
        const replayerId = sessionStorage.getItem("cid") || this.$cookies.get("cid")
        this.$axios({
          url: '/api/replay',
          method: 'post',
          data: {
            replay: value,
            replayerName: replayerName,
            replayerId: replayerId,
            replayTime: date,
            messageId: messageId
          }
        }).then(res => {
          this.getMsg()
        })
        this.$message({
          type: 'success',
          message: '回复成功'
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '取消输入'
        });
      });
    },
    replyToComment(messageId, replyToName, replyToId) { // 回复评论功能
      this.$prompt(`回复 @${replyToName}`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^[\s\S]*.*[^\s][\s\S]*$/,
        inputErrorMessage: '回复不能为空'
      }).then(({ value }) => {
        let date = new Date()
        let replayerName = sessionStorage.getItem("cname") || this.$cookies.get("cname") || '匿名用户'
        const replayerId = sessionStorage.getItem("cid") || this.$cookies.get("cid")
        this.$axios({
          url: '/api/replay',
          method: 'post',
          data: {
            replay: value,
            replayerName: replayerName,
            replayerId: replayerId,
            replayTime: date,
            messageId: messageId,
            replyToName: replyToName,
            replyToId: replyToId
          }
        }).then(res => {
          this.getMsg()
        })
        this.$message({
          type: 'success',
          message: '回复成功'
        });
      });
    },
    enter(index) {
      this.flag = true
      this.current = index
    },
    leave(index) {
      this.flag = false;
      this.current = index;
    },
    // 格式化名字：当前用户显示"我"
    formatName(name) {
      if (!name) return '匿名用户'
      const currentUserName = sessionStorage.getItem("cname") || this.$cookies.get("cname")
      return name === currentUserName ? '我' : name
    }
  }
}
</script>

<style lang="less" scoped>
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

#message {
  padding: 20px;
  flex: 1;
}

/* 搜索区域样式 */
.search-section {
  background: #fff;
  padding: 16px 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  
  .search-form {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 12px;
    
    .el-form-item {
      margin-bottom: 0;
      margin-right: 0;
    }
    
    .el-form-item__label {
      color: #333;
      font-weight: 500;
    }
    
    .el-button--primary {
      &:hover, &:focus, &:active {
        background-color: var(--primary-color, #409EFF) !important;
        border-color: var(--primary-color, #409EFF) !important;
      }
    }
  }
}

.btn .el-button--primary {
  &:hover, &:focus, &:active {
    background-color: var(--primary-color, #409EFF) !important;
    border-color: var(--primary-color, #409EFF) !important;
  }
}

.title {
  margin: 0 0 20px 0;
  padding: 15px 20px;
  background: var(--primary-color, #409EFF);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  color: #fff !important;
  font-weight: 600;
  
  i {
    color: #fff !important;
  }
}

.title1 {
  margin-bottom: 10px;
}

.content {
  padding: 10px 0px;
}

#message {
  .btn {
    padding-bottom: 20px;
    text-align: right;
  }

  .all {
    .msglist {
      padding: 0;
      margin: 0;
      list-style: none;
    }
    
    .date {
      color: var(--primary-color, #409EFF);
      line-height: 35px;
      font-size: 12px;
      
      .message-delete-btn {
        color: #f56c6c;
        margin-left: 16px;
        cursor: pointer;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }

    .list {
      background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
      padding: 16px 20px;
      border-radius: 8px;
      margin: 12px 0px;
      position: relative;
      transition: all .3s ease;
      border-left: 4px solid var(--primary-color, #409EFF);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
      }

      .title {
        color: #333 !important;
        margin: 0px;
        padding: 0;
        font-size: 16px;
        font-weight: 600;
        line-height: 28px;
        background: transparent !important;
        box-shadow: none !important;
        border-radius: 0;
        
        .publisher-info {
          margin-left: 12px;
          font-size: 14px;
          font-weight: 500;
          color: #666;
          background-color: #f0f0f0;
          padding: 2px 8px;
          border-radius: 12px;
          display: inline-block;
          
          .publisher-name {
            color: var(--primary-color, #409EFF);
          }
          
          .publisher-role {
            color: #909399;
          }
        }
      }

      .content {
        padding: 8px 0;
        color: #666;
        font-size: 14px;
        line-height: 1.6;
      }

      .icon-r-user2 {
        color: var(--primary-color, #409EFF) !important;
      }

      .icon-date {
        font-size: 12px;
        margin-right: 4px;
        color: var(--primary-color, #409EFF);
      }

      .replay {
        position: absolute;
        right: 20px;
        top: 16px;
        color: #fff !important;
        background: var(--primary-color, #409EFF);
        padding: 4px 12px;
        border-radius: 4px;
        font-size: 12px;
        cursor: pointer;
        transition: all .3s ease;

        &:hover {
          color: #fff !important;
          transform: scale(1.05);
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.4);
        }
      }

      .comment {
        margin: 8px 0px;
        margin-bottom: 12px;
        padding: 10px 12px;
        font-size: 13px;
        color: #333;
        background: rgba(64, 158, 255, 0.08);
        border-radius: 4px;
        border-left: 3px solid var(--primary-color, #409EFF);
        display: flex;
        justify-content: space-between;
        align-items: center;
        transition: all .3s ease;

        i {
          margin-right: 6px;
          color: var(--primary-color, #409EFF);
        }
        
        .comment-content {
          flex: 1;
        }
        
        .replayer-name {
          color: var(--primary-color, #409EFF);
          font-weight: 600;
        }
        
        .replay-text {
          color: #333;
        }
        
        .replay-time {
          color: #999;
          font-size: 12px;
          white-space: nowrap;
        }
        
        .comments-section {
          margin-top: 8px;
        }
        
        .comment-main {
          display: flex;
          align-items: flex-start;
          gap: 8px;
        }
        
        .comment-body {
          flex: 1;
          line-height: 1.6;
        }
        
        .reply-arrow {
          color: #999;
          margin: 0 4px;
        }
        
        .separator {
          color: #666;
        }
        
        .comment-footer {
          display: flex;
          align-items: center;
          gap: 12px;
          margin-top: 4px;
          margin-left: 24px;
        }
        
        .reply-btn {
          color: var(--primary-color, #409EFF);
          font-size: 12px;
          cursor: pointer;
          
          &:hover {
            text-decoration: underline;
          }
        }
        
        .delete-btn {
          color: #f56c6c;
          font-size: 12px;
          cursor: pointer;
          margin-left: 12px;
          
          &:hover {
            text-decoration: underline;
          }
        }
        
        .reply-to-name {
          color: var(--primary-color, #409EFF);
          font-weight: 500;
        }
        
        .is-reply {
          margin-left: 20px;
          border-left: 2px solid var(--primary-color, #409EFF);
          padding-left: 12px;
        }
      }
    }
  }
}

#message .wrapper {
  background-color: #fff;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

</style>

<!-- 非 scoped 样式，确保动画生效 -->
<style lang="less">
/* 高亮留言样式 */
#message .list.highlight-message {
  animation: highlightPulse 0.8s ease-in-out 5 !important;
  border-left: 4px solid #e6a23c !important;
  box-shadow: 0 0 20px rgba(230, 162, 60, 0.5) !important;
  background: linear-gradient(135deg, #fff8e6 0%, #fff 100%) !important;
}

/* 高亮回复样式 */
#message .comment.highlight-replay {
  animation: highlightPulse 0.8s ease-in-out 5 !important;
  background: rgba(230, 162, 60, 0.25) !important;
  border-left: 3px solid #e6a23c !important;
  box-shadow: 0 0 15px rgba(230, 162, 60, 0.5) !important;
}

/* 高亮闪烁动画 */
@keyframes highlightPulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.01);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>

<style>
/* 全局覆盖按钮悬停效果 - 交流区页面 */
#message .el-button--primary,
#message .el-button--primary:hover,
#message .el-button--primary:focus,
#message .el-button--primary:active,
#message .el-button--primary.is-active,
#message .el-button--primary.is-disabled,
#message .el-button--primary:not(.is-disabled):hover,
#message .el-button--primary:not(.is-disabled):focus,
#message .el-button--primary:not(.is-disabled):active {
  background-color: var(--primary-color, #409EFF) !important;
  border-color: var(--primary-color, #409EFF) !important;
  color: #fff !important;
  opacity: 1 !important;
  box-shadow: none !important;
}

#message .el-button--primary > span,
#message .btn .el-button--primary span {
  color: #fff !important;
}

#message .search-section .el-button--primary i,
#message .btn .el-button--primary i {
  color: #fff !important;
}
</style>
