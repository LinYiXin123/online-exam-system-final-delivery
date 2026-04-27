//查询所有题库
<template>
  <div class="exam">
    <!-- 搜索区域 -->
    <div class="search-area">
      <el-input 
        v-model="subject" 
        placeholder="搜索科目名称" 
        clearable
        class="search-input"
        @keyup.enter.native="getAnswerInfo"
      >
        <i slot="prefix" class="el-input__icon el-icon-search"></i>
      </el-input>
      <el-input 
        v-model="section" 
        placeholder="搜索章节" 
        clearable
        class="search-input"
        @keyup.enter.native="getAnswerInfo"
      >
        <i slot="prefix" class="el-input__icon el-icon-folder"></i>
      </el-input>
      <el-input 
        v-model="question" 
        placeholder="搜索题目内容" 
        clearable
        class="search-input search-input-wide"
        @keyup.enter.native="getAnswerInfo"
      >
        <i slot="prefix" class="el-input__icon el-icon-document"></i>
      </el-input>
      <el-select v-model="questionType" placeholder="全部类型" clearable class="filter-select">
        <el-option label="选择题" value="选择题"></el-option>
        <el-option label="填空题" value="填空题"></el-option>
        <el-option label="判断题" value="判断题"></el-option>
        <el-option label="主观题" value="主观题"></el-option>
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="getAnswerInfo">搜索</el-button>
      <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
    </div>
    
    <!-- 记录数显示 -->
    <div class="record-info">
      <span>共 {{ pagination.total || 0 }} 条记录</span>
    </div>

    <el-table :data="pagination.records" border :row-class-name="tableRowClassName">
      <el-table-column type="index" label="序号" width="60" align="center" :index="getTableIndex"></el-table-column>
      <el-table-column prop="subject" label="科目名称" min-width="180" show-overflow-tooltip></el-table-column>
      <el-table-column prop="question" label="题目信息" min-width="400" show-overflow-tooltip></el-table-column>
      <el-table-column prop="section" label="所属章节" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column prop="type" label="题目类型" min-width="120" show-overflow-tooltip></el-table-column>
      <el-table-column prop="score" label="试题分数" min-width="100" show-overflow-tooltip></el-table-column>
      <el-table-column prop="level" label="难度等级" min-width="100" show-overflow-tooltip></el-table-column>
      <el-table-column fixed="right" label="操作" width="200" align="center">
        <template slot-scope="scope">
          <div class="action-btns">
            <el-button @click="toEdit(scope.row.type, scope.row.questionId)" type="primary" size="mini">编辑</el-button>
            <el-button @click="deleteQuestion(scope.row.questionId)" type="danger" size="mini">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.current"
      :page-sizes="[6, 10]"
      :page-size="pagination.size"
      layout="total, sizes, prev, pager, next, jumper"
      :total="pagination.total"
      class="page"
    ></el-pagination>
  </div>
</template>

<script>
export default {
  data() {
    return {
      pagination: {
        //分页后的考试信息
        current: 1, //当前页
        total: null, //记录条数
        size: 6 //每页条数

      },
      section: "",
      subject: "",
      question: "",
      questionType: "",
    };
  },
  created() {
    this.getAnswerInfo();
  },
  methods: {
    toEdit(type, id) { 
      // 前往编辑题目
      this.$router.push({path:'/admin/editAnswerChildren',query: {type: type, questionId: id}})
    },
    getAnswerInfo(size, current) {
      //分页查询所有试卷信息
      if(typeof size === 'number' && !isNaN(size)) {
        this.pagination.size = size;
      }
      if(typeof current === 'number' && !isNaN(current)) {
        this.pagination.current = current;
      } else {
        this.pagination.current = 1;
      }
      var subject = this.subject;
      if(this.subject.trim() == "") {
        subject = "@";
      }
      var section = this.section;
      if(this.section.trim() == "") {
        section = "@";
      }
      var question = this.question;
      if(this.question.trim() == "") {
        question = "@";
      }
      var questionType = this.questionType;
      if(!this.questionType || this.questionType.trim() == "") {
        questionType = "@";
      }
      this.$axios(
        `/api/answers/${this.pagination.current}/${this.pagination.size}/${subject}/${section}/${question}/${questionType}`
      )
        .then(res => {
          const data = res.data.data;
          this.pagination = {
            ...data,
            current: parseInt(data.current) || 1,
            total: parseInt(data.total) || 0,
            size: parseInt(data.size) || 6
          };
        })
        .catch(error => {});
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = parseInt(val) || 6;
      this.pagination.current = 1; // 重置到第一页
      this.getAnswerInfo();
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = parseInt(val) || 1;
      this.getAnswerInfo(this.pagination.size, this.pagination.current);
    },
    
    // 计算表格序号
    getTableIndex(index) {
      return (this.pagination.current - 1) * this.pagination.size + index + 1;
    },
    
    // 删除题目
    deleteQuestion(questionId) {
      this.$confirm('确认删除该题目？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.delete(`/api/questions/${questionId}`).then(res => {
          if (res.data.code === 200) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            });
            this.getAnswerInfo(); // 刷新列表
          } else {
            this.$message({
              type: 'error',
              message: res.data.message || '删除失败!'
            });
          }
        }).catch(error => {
          this.$message({
            type: 'error',
            message: '删除失败!'
          });
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    
    tableRowClassName({ row, rowIndex }) {
      if (rowIndex % 2 == 0) {
        return "warning-row";
      } else {
        return "success-row";
      }
    },
    // 重置搜索条件
    handleReset() {
      this.subject = "";
      this.section = "";
      this.question = "";
      this.questionType = "";
      this.pagination.current = 1;
      this.getAnswerInfo();
    }
  }
};
</script>
<style lang="less" scoped>
.exam {
  padding: 0px 40px;
  
  // 搜索区域样式
  .search-area {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    margin-bottom: 16px;
    padding: 16px 20px;
    background: var(--card-bg, #fff);
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    
    .search-input {
      width: 160px;
      
      /deep/ .el-input__inner {
        border-radius: 6px;
        background: var(--input-bg, #f5f7fa);
        border-color: var(--border-color, #dcdfe6);
        color: var(--text-primary, #303133);
        
        &::placeholder {
          color: var(--text-secondary, #909399);
        }
        
        &:focus {
          border-color: var(--primary-color, #409eff);
        }
      }
    }
    
    .search-input-wide {
      width: 220px;
    }
    
    .filter-select {
      width: 120px;
      
      /deep/ .el-input__inner {
        border-radius: 6px;
        background: var(--input-bg, #f5f7fa);
        border-color: var(--border-color, #dcdfe6);
        color: var(--text-primary, #303133);
      }
    }
    
    .el-button {
      border-radius: 6px;
    }
  }
  
  // 记录数显示样式
  .record-info {
    margin-bottom: 12px;
    padding: 8px 0;
    color: var(--text-secondary, #606266);
    font-size: 14px;
  }
  
  .page {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  .edit {
    margin-left: 20px;
  }
  
  // 操作按钮水平排列样式
  .action-btns {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
    
    .el-button {
      margin: 0;
      padding: 6px 15px;
      font-size: 12px;
      min-width: 60px;
      border-radius: 4px;
    }
  }
}

// 深色模式样式
.dark-theme {
  .exam {
    .search-area {
      background: var(--card-bg, #1e1e2d);
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
      
      .search-input,
      .filter-select {
        /deep/ .el-input__inner {
          background: var(--input-bg, #2d2d3a);
          border-color: var(--border-color, #3d3d4d);
          color: var(--text-primary, #e0e0e0);
          
          &::placeholder {
            color: var(--text-secondary, #888);
          }
        }
      }
    }
    
    .record-info {
      color: var(--text-secondary, #aaa);
    }
  }
}
</style>
