<template>
  <div class="message-manage">
    <!-- 标题卡片 - 跟随主题色 -->
    <el-card shadow="never" class="title-card" :body-style="{ padding: 0 }">
      <div class="custom-card-header" :style="{ backgroundColor: currentThemeColor }">
        <span><i class="el-icon-chat-dot-round"></i> 留言管理</span>
      </div>
    </el-card>
    
    <el-card class="manage-card">
      
      <!-- 搜索区域 -->
      <div class="search-area">
        <el-input
          v-model="searchText"
          placeholder="搜索留言标题或内容"
          prefix-icon="el-icon-search"
          clearable
          style="width: 300px"
          @input="handleSearch">
        </el-input>
        <el-button type="danger" :disabled="selectedMessages.length === 0" @click="batchDelete">
          <i class="el-icon-delete"></i> 批量删除 ({{ selectedMessages.length }})
        </el-button>
      </div>

      <!-- 留言列表 -->
      <el-table
        :data="filteredMessages"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe>
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="title" label="标题" width="150">
          <template slot-scope="scope">
            <el-tag size="small" type="primary">{{ scope.row.title }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip></el-table-column>
        <el-table-column prop="time" label="发布时间" width="160"></el-table-column>
        <el-table-column label="回复数" width="100" align="center">
          <template slot-scope="scope">
            <el-badge :value="scope.row.replyCount || 0" :max="99" class="reply-badge">
              <el-button size="mini" type="text" @click="viewReplies(scope.row)">
                查看
              </el-button>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="viewDetail(scope.row)">
              <i class="el-icon-view"></i>
            </el-button>
            <el-button size="mini" type="danger" @click="deleteMessage(scope.row)">
              <i class="el-icon-delete"></i>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalMessages">
      </el-pagination>
    </el-card>

    <!-- 留言详情对话框 -->
    <el-dialog title="留言详情" :visible.sync="detailDialogVisible" width="600px">
      <div class="message-detail" v-if="currentMessage">
        <div class="detail-item">
          <label>标题：</label>
          <span>{{ currentMessage.title }}</span>
        </div>
        <div class="detail-item">
          <label>内容：</label>
          <p>{{ currentMessage.content }}</p>
        </div>
        <div class="detail-item">
          <label>发布时间：</label>
          <span>{{ currentMessage.time }}</span>
        </div>
        
        <!-- 回复列表 -->
        <div class="replies-section" v-if="currentReplies.length > 0">
          <h4>回复列表 ({{ currentReplies.length }})</h4>
          <div class="reply-item" v-for="reply in currentReplies" :key="reply.replayId">
            <p>{{ reply.replay }}</p>
            <el-button size="mini" type="danger" @click="deleteReply(reply)">删除</el-button>
          </div>
        </div>
        <el-empty v-else description="暂无回复"></el-empty>
      </div>
      <span slot="footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'MessageManage',
  data() {
    return {
      loading: false,
      searchText: '',
      messages: [],
      filteredMessages: [],
      selectedMessages: [],
      currentPage: 1,
      pageSize: 10,
      totalMessages: 0,
      detailDialogVisible: false,
      currentMessage: null,
      currentReplies: []
    }
  },
  computed: {
    currentThemeColor() {
      return getComputedStyle(document.documentElement).getPropertyValue('--admin-primary').trim() || '#6366f1';
    }
  },
  mounted() {
    this.loadMessages()
  },
  methods: {
    // 加载留言列表
    async loadMessages() {
      this.loading = true
      try {
        const res = await this.$axios.get('/message/findAll')
        if (res.data) {
          this.messages = res.data
          this.totalMessages = res.data.length
          this.applyPagination()
        }
      } catch (error) {
        console.error('加载留言失败:', error)
        this.$message.error('加载留言失败')
      } finally {
        this.loading = false
      }
    },

    // 搜索过滤
    handleSearch() {
      this.currentPage = 1
      this.applyPagination()
    },

    // 应用分页
    applyPagination() {
      let filtered = this.messages
      if (this.searchText) {
        const keyword = this.searchText.toLowerCase()
        filtered = this.messages.filter(m => 
          (m.title && m.title.toLowerCase().includes(keyword)) ||
          (m.content && m.content.toLowerCase().includes(keyword))
        )
      }
      this.totalMessages = filtered.length
      const start = (this.currentPage - 1) * this.pageSize
      this.filteredMessages = filtered.slice(start, start + this.pageSize)
    },

    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.applyPagination()
    },

    handleCurrentChange(page) {
      this.currentPage = page
      this.applyPagination()
    },

    handleSelectionChange(selection) {
      this.selectedMessages = selection
    },

    // 查看详情
    async viewDetail(message) {
      this.currentMessage = message
      await this.loadReplies(message.id)
      this.detailDialogVisible = true
    },

    // 查看回复
    async viewReplies(message) {
      await this.viewDetail(message)
    },

    // 加载回复
    async loadReplies(messageId) {
      try {
        const res = await this.$axios.get(`/replay/${messageId}`)
        this.currentReplies = res.data || []
      } catch (error) {
        this.currentReplies = []
      }
    },

    // 删除留言
    deleteMessage(message) {
      this.$confirm(`确定删除留言"${message.title}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await this.$axios.delete(`/message/${message.id}`)
          this.$message.success('删除成功')
          this.loadMessages()
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    // 批量删除
    batchDelete() {
      this.$confirm(`确定删除选中的${this.selectedMessages.length}条留言吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          for (const msg of this.selectedMessages) {
            await this.$axios.delete(`/message/${msg.id}`)
          }
          this.$message.success('批量删除成功')
          this.loadMessages()
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    },

    // 删除回复
    deleteReply(reply) {
      this.$confirm('确定删除这条回复吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await this.$axios.delete(`/replay/${reply.replayId}`)
          this.$message.success('删除成功')
          await this.loadReplies(this.currentMessage.id)
        } catch (error) {
          this.$message.error('删除失败')
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped lang="less">
.message-manage {
  .title-card {
    margin-bottom: 20px;
    border-radius: 8px;
    overflow: hidden;
    border: none;
    
    .custom-card-header {
      padding: 15px 20px;
      color: #fff;
      font-size: 18px;
      font-weight: 600;
      display: flex;
      align-items: center;
      
      i {
        margin-right: 10px;
        font-size: 20px;
      }
    }
  }
  
  .manage-card {
    .card-header {
      display: flex;
      align-items: baseline;
      gap: 12px;
      
      .title {
        font-size: 18px;
        font-weight: 600;
        color: var(--text-primary);
      }
      
      .subtitle {
        font-size: 14px;
        color: var(--text-secondary);
      }
    }
  }
  
  .search-area {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  
  .pagination {
    margin-top: 20px;
    text-align: right;
  }
  
  .message-detail {
    .detail-item {
      margin-bottom: 16px;
      
      label {
        font-weight: 600;
        color: var(--text-secondary);
        margin-right: 8px;
      }
      
      p {
        margin-top: 8px;
        padding: 12px;
        background-color: var(--bg-secondary);
        border-radius: 8px;
      }
    }
    
    .replies-section {
      margin-top: 24px;
      
      h4 {
        margin-bottom: 12px;
        color: var(--text-primary);
      }
      
      .reply-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px;
        background-color: var(--bg-secondary);
        border-radius: 8px;
        margin-bottom: 8px;
        
        p {
          margin: 0;
          flex: 1;
        }
      }
    }
  }
}
</style>
