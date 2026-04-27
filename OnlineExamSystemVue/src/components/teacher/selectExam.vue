//查询所有考试
<template>
  <div class="exam">
    <!-- 查询区域 -->
    <div class="search-area">
      <el-input 
        v-model="searchForm.source" 
        placeholder="搜索科目名称" 
        clearable
        class="search-input"
        @keyup.enter.native="handleSearch"
      >
        <i slot="prefix" class="el-input__icon el-icon-search"></i>
      </el-input>
      <el-select v-model="searchForm.institute" placeholder="全部学院" clearable class="filter-select">
        <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
      </el-select>
      <el-select v-model="searchForm.major" placeholder="全部专业" clearable class="filter-select">
        <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item"></el-option>
      </el-select>
      <el-select v-model="searchForm.grade" placeholder="全部年级" clearable class="filter-select">
        <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item"></el-option>
      </el-select>
      <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
      <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
    </div>
    
    <!-- 记录数显示 -->
    <div class="record-info">
      <span>共 {{ pagination.total }} 条记录</span>
    </div>
    
    <el-table :data="pagination.records" border class="fade-table" :key="tableKey">
      <el-table-column type="index" label="序号" width="60" align="center" fixed="left" :index="getTableIndex"></el-table-column>
      <el-table-column
        fixed="left"
        prop="source"
        label="科目名称"
        width="180"
      ></el-table-column>
      <el-table-column
        prop="description"
        label="介绍"
        width="200"
      ></el-table-column>
      <el-table-column
        prop="institute"
        label="所属学院"
        width="120"
      ></el-table-column>
      <el-table-column
        prop="major"
        label="所属专业"
        width="200"
      ></el-table-column>
      <el-table-column prop="grade" label="年级" width="100"></el-table-column>
      <el-table-column
        prop="examDate"
        label="考试日期"
        width="120"
      ></el-table-column>
      <el-table-column
        prop="totalTime"
        label="持续时间"
        width="120"
      ></el-table-column>
      <el-table-column
        prop="totalScore"
        label="总分"
        width="120"
      ></el-table-column>
      <el-table-column
        prop="type"
        label="试卷类型"
        width="120"
      ></el-table-column>
      <el-table-column
        prop="tips"
        label="考生提示"
        width="200"
        show-overflow-tooltip
      ></el-table-column>
      <el-table-column fixed="right" label="操作" width="420" align="center">
        <template slot-scope="scope">
          <div class="operation-buttons">
            <el-button type="success" @click="getExamDetail(scope.row.examCode, scope.row.paperId)">
              试题详情
            </el-button>
            <el-button @click="edit(scope.row.examCode)" type="primary">
              编辑
            </el-button>
            <el-button @click="deleteRecord(scope.row.examCode)" type="danger">
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="pagination.current"
      :page-sizes="[4, 8, 10]"
      :page-size="pagination.size"
      layout="total, sizes, prev, pager, next, jumper"
      :total="pagination.total"
      class="page"
    >
    </el-pagination>
    <!-- 编辑对话框-->
    <el-dialog
      title="编辑试卷信息"
      :visible.sync="dialogVisible"
      width="30%"
    >
      <section class="update">
        <el-form ref="form" :model="form" label-width="80px">
          <el-form-item label="科目名称">
            <el-select v-model="form.source" filterable allow-create placeholder="请选择或输入科目名称" style="width: 100%;" @change="onSubjectChange">
              <el-option v-for="item in subjectOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="介绍">
            <el-select v-model="form.description" filterable allow-create placeholder="请选择或输入介绍" style="width: 100%;">
              <el-option v-for="item in filteredDescriptionOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="所属学院">
            <el-select v-model="form.institute" filterable allow-create placeholder="请选择或输入学院" style="width: 100%;">
              <el-option v-for="item in instituteOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="所属专业">
            <el-select v-model="form.major" filterable allow-create placeholder="请选择或输入专业" style="width: 100%;">
              <el-option v-for="item in majorOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="年级">
            <el-select v-model="form.grade" filterable allow-create placeholder="请选择或输入年级" style="width: 100%;">
              <el-option v-for="item in gradeOptions" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="考试日期">
            <el-col :span="11">
              <el-date-picker
                type="date"
                placeholder="选择日期"
                v-model="form.examDate"
                value-format="yyyy-MM-dd"
                style="width: 100%"
              ></el-date-picker>
            </el-col>
          </el-form-item>
          <el-form-item label="持续时间（分钟）">
            <el-input v-model="form.totalTime" type="number" oninput="if(value<=0)value=''" :min="1"></el-input>
          </el-form-item>
          <!-- <el-form-item label="总分">
            <el-input v-model="form.totalScore"></el-input>
          </el-form-item> -->
          <el-form-item label="试卷类型">
            <el-select v-model="form.type" filterable allow-create placeholder="请选择试卷类型" style="width: 100%;">
              <el-option label="日常练习" value="日常练习"></el-option>
              <el-option label="期中考试" value="期中考试"></el-option>
              <el-option label="期末考试" value="期末考试"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="考生提示">
            <el-input type="textarea" v-model="form.tips"></el-input>
          </el-form-item>
        </el-form>
      </section>
      <span slot="footer" class="dialog-footer edit-dialog-footer">
        <el-button type="primary" @click="submit()" size="medium" class="dialog-btn">确定</el-button>
        <el-button @click="dialogVisible = false" size="medium" class="dialog-btn">取消</el-button>
      </span>
    </el-dialog>

    <!-- 查询试卷对话框 -->
    <el-dialog title="试卷内容" :visible.sync="showExamDetail" width="60%" @close="onExamDetailClose">
      <span>
        <div class="content">
          <el-collapse v-model="activeName">
            <el-collapse-item class="header" name="0">
              <template slot="title">
                <div class="title">
                  <span class="subject-name">{{ examData.source }}</span>
                  <span class="exam-tip">
                    <i class="el-icon-info"></i>
                    点击展开查看详细题目
                  </span>
                  <span class="score-info">
                    <i class="el-icon-medal"></i>
                    总分：{{ (score[0] || 0) + (score[1] || 0) + (score[2] || 0) + (score[3] || 0) }}分
                  </span>
                  <span class="time-info">
                    <i class="el-icon-time"></i>
                    时长：{{ examData.totalTime }}分钟
                  </span>
                </div>
              </template>
              <el-collapse class="inner">
                <el-collapse-item>
                  <template slot="title">
                    <div class="titlei">
                      <i class="el-icon-edit-outline"></i> 选择题 (共{{ topicCount[0] }}题 共计{{ score[0] }}分)
                    </div>
                  </template>
                  <div class="contenti">
                    <el-table :data="topic[1]" border size="small" class="question-table">
                      <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
                      <el-table-column prop="question" label="题目内容" min-width="300"></el-table-column>
                      <el-table-column prop="rightAnswer" label="正确答案" width="100" align="center"></el-table-column>
                      <el-table-column prop="score" label="分值" width="80" align="center">
                        <template slot-scope="scope">{{ scope.row.score }}分</template>
                      </el-table-column>
                      <el-table-column label="操作" width="100" align="center">
                        <template slot-scope="scope">
                          <el-button type="danger" size="mini" @click="deleteQuestion(1, scope.row.questionId)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-collapse-item>
                <el-collapse-item>
                  <template slot="title">
                    <div class="titlei">
                      <i class="el-icon-edit"></i> 填空题 (共{{ topicCount[1] }}题 共计{{ score[1] }}分)
                    </div>
                  </template>
                  <div class="contenti">
                    <el-table :data="topic[2]" border size="small" class="question-table">
                      <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
                      <el-table-column prop="question" label="题目内容" min-width="300"></el-table-column>
                      <el-table-column prop="answer" label="正确答案" width="150" align="center"></el-table-column>
                      <el-table-column prop="score" label="分值" width="80" align="center">
                        <template slot-scope="scope">{{ scope.row.score }}分</template>
                      </el-table-column>
                      <el-table-column label="操作" width="100" align="center">
                        <template slot-scope="scope">
                          <el-button type="danger" size="mini" @click="deleteQuestion(2, scope.row.questionId)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-collapse-item>
                <el-collapse-item>
                  <template slot="title">
                    <div class="titlei">
                      <i class="el-icon-circle-check"></i> 判断题 (共{{ topicCount[2] }}题 共计{{ score[2] }}分)
                    </div>
                  </template>
                  <div class="contenti">
                    <el-table :data="topic[3]" border size="small" class="question-table">
                      <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
                      <el-table-column prop="question" label="题目内容" min-width="300"></el-table-column>
                      <el-table-column label="正确答案" width="100" align="center">
                        <template slot-scope="scope">{{ scope.row.answer === 'T' ? '正确' : '错误' }}</template>
                      </el-table-column>
                      <el-table-column prop="score" label="分值" width="80" align="center">
                        <template slot-scope="scope">{{ scope.row.score }}分</template>
                      </el-table-column>
                      <el-table-column label="操作" width="100" align="center">
                        <template slot-scope="scope">
                          <el-button type="danger" size="mini" @click="deleteQuestion(3, scope.row.questionId)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-collapse-item>
                <el-collapse-item>
                  <template slot="title">
                    <div class="titlei">
                      <i class="el-icon-document"></i> 主观题 (共{{ topicCount[3] || 0 }}题 共计{{ score[3] || 0 }}分)
                    </div>
                  </template>
                  <div class="contenti">
                    <el-table :data="topic[4] || []" border size="small" class="question-table">
                      <el-table-column type="expand">
                        <template slot-scope="scope">
                          <div class="essay-answer-expand">
                            <strong>参考答案：</strong>
                            <p>{{ scope.row.referenceAnswer || '暂无参考答案' }}</p>
                          </div>
                        </template>
                      </el-table-column>
                      <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
                      <el-table-column prop="question" label="题目内容" min-width="300" show-overflow-tooltip></el-table-column>
                      <el-table-column prop="score" label="分值" width="80" align="center">
                        <template slot-scope="scope">{{ scope.row.score }}分</template>
                      </el-table-column>
                      <el-table-column label="操作" width="100" align="center">
                        <template slot-scope="scope">
                          <el-button type="danger" size="mini" @click="deleteQuestion(4, scope.row.questionId)">删除</el-button>
                        </template>
                      </el-table-column>
                    </el-table>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </el-collapse-item>
          </el-collapse>
        </div>
      </span>
      <span slot="footer" class="dialog-footer exam-detail-footer">
        <el-button type="danger" plain @click="showExamDetail = false" size="medium" class="close-btn">
          <i class="el-icon-close"></i> 关闭
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: {}, //保存点击以后当前试卷的信息
      searchForm: {  // 查询条件
        source: '',
        institute: '',
        major: '',
        grade: ''
      },
      tableKey: 0,  // 用于触发表格动画
      pagination: {
        //分页后的考试信息
        current: 1, //当前页
        total: null, //记录条数
        size: 6, //每页条数
      },
      dialogVisible: false,
      showExamDetail: false, //是否展示试卷
      hasDeletedQuestions: false, // 是否删除过题目，用于关闭对话框时刷新列表
      activeName: '0',  //默认打开序号
      topicCount: [],//每种类型题目的总数
      score: [],  //每种类型分数的总数
      examData: { //考试信息
        // source: null,
        // totalScore: null,
      },
      topic: {  //试卷信息

      },
      examDetailCode: "",   // 当前查看试题详情中，考试编码code
      examDetailPaperId: "",    // 当前查看的试卷详情中，试卷id
      subjectOptions: [],  // 科目选项
      descriptionOptions: [], // 介绍选项
      instituteOptions: [], // 学院选项
      majorOptions: [],    // 专业选项
      gradeOptions: [],    // 年级选项
    };
  },
  computed: {
    // 根据当前选择的科目名称过滤介绍选项
    filteredDescriptionOptions() {
      if (!this.form.source || !this.descriptionOptions.length) {
        return this.descriptionOptions;
      }
      // 只返回包含当前科目名称的介绍
      return this.descriptionOptions.filter(desc => 
        desc && desc.includes(this.form.source)
      );
    }
  },
  created() {
    this.getExamInfo();
    this.loadOptions();
  },
  methods: {
    loadOptions() {
      console.log('开始加载选项数据...');
      // 从数据库获取所有考试数据来提取选项
      this.$axios.get('/api/exams').then(res => {
        console.log('考试数据 API响应:', res.data);
        if (res.data.code === 200 && res.data.data) {
          this.extractOptionsFromExams(res.data.data);
        } else {
          console.log('API返回数据格式不正确，使用默认选项');
          this.setDefaultOptions();
        }
      }).catch(error => {
        console.error('考试数据 API调用失败:', error);
        this.setDefaultOptions();
      });
    },
    
    // 从考试数据中提取选项
    extractOptionsFromExams(exams) {
      console.log('开始提取考试数据选项, 考试数量:', exams ? exams.length : 0);
      
      if (!exams || exams.length === 0) {
        console.log('没有考试数据，使用默认选项');
        this.setDefaultOptions();
        return;
      }
      
      // 提取并去重科目选项
      const subjects = [...new Set(exams.map(exam => exam.source).filter(item => item && item.trim()))];
      console.log('提取到的科目选项:', subjects);
      this.subjectOptions = subjects.sort();
      
      // 提取并去重介绍选项
      const descriptions = [...new Set(exams.map(exam => exam.description).filter(item => item && item.trim()))];
      console.log('提取到的介绍选项:', descriptions);
      this.descriptionOptions = descriptions.sort();
      
      // 提取并去重学院选项
      const institutes = [...new Set(exams.map(exam => exam.institute).filter(item => item && item.trim()))];
      console.log('提取到的学院选项:', institutes);
      this.instituteOptions = institutes.sort();
      
      // 提取并去重专业选项
      const majors = [...new Set(exams.map(exam => exam.major).filter(item => item && item.trim()))];
      console.log('提取到的专业选项:', majors);
      this.majorOptions = majors.sort();
      
      // 提取并去重年级选项
      const grades = [...new Set(exams.map(exam => exam.grade).filter(item => item && item.trim()))];
      console.log('提取到的年级选项:', grades);
      this.gradeOptions = grades.sort();
      
      // 输出最终的选项结果
      console.log('最终的科目选项:', this.subjectOptions);
      console.log('最终的介绍选项:', this.descriptionOptions);
    },
    
    // 设置默认选项（从数据库获取失败时清空）
    setDefaultOptions() {
      console.log('数据库获取失败，清空选项');
      this.subjectOptions = [];
      this.descriptionOptions = [];
      this.instituteOptions = [];
      this.majorOptions = [];
      this.gradeOptions = [];
    },
    
    // 计算表格序号（支持分页）
    getTableIndex(index) {
      return (this.pagination.current - 1) * this.pagination.size + index + 1;
    },
    onSubjectChange(selectedSubject) {
      // 根据选择的科目名称加载相关的介绍选项
      if (selectedSubject) {
        this.$axios('/api/exams').then(res => {
          if (res.data.code === 200) {
            console.log('所有考试数据:', res.data.data);
            const matchedExams = res.data.data.filter(exam => exam.source === selectedSubject);
            console.log('匹配的考试:', matchedExams);
            const descriptions = matchedExams
              .map(exam => exam.description)
              .filter(desc => desc && desc.trim() !== '');
            console.log('过滤后的介绍:', descriptions);
            // 去重但保留所有不同的介绍
            this.descriptionOptions = [...new Set(descriptions)];
            console.log('最终介绍选项:', this.descriptionOptions);
          }
        }).catch(err => {
          console.error('获取考试数据失败:', err);
        });
      } else {
        this.descriptionOptions = [];
      }
    },
    deleteQuestion(type, questionId) {
      // type: 1选择 2填空 3判断
      var paperId = this.examDetailPaperId;
      this.$confirm("确认从试卷中移除该试题？")
        .then((_) => {
          this.$axios.delete(`/api/paper/${paperId}/${type}/${questionId}`).then(res => {  //通过examCode请求试卷详细信息
            this.getExamDetail(this.examDetailCode, paperId);
            this.hasDeletedQuestions = true; // 标记已删除题目
          })  
        })
        .catch((_) => {});
    },
    // 关闭试卷详情对话框时刷新列表
    onExamDetailClose() {
      if (this.hasDeletedQuestions) {
        this.getExamInfo(); // 刷新考试列表以更新总分
        this.hasDeletedQuestions = false;
      }
    },
    getExamDetail(examCode, paperId) {
      this.examDetailCode = examCode;
      this.examDetailPaperId = paperId;
      this.topicCount = [];
      this.score = [];
      this.examData = {},
      this.topic = {},

      this.showExamDetail = true;

      this.$axios(`/api/exam/${examCode}`).then(res => {  //通过examCode请求试卷详细信息
        res.data.data.examDate = res.data.data.examDate.substr(0,10)
        this.examData = { ...res.data.data}
        let paperId = this.examData.paperId
        this.$axios(`/api/paper/${paperId}`).then(res => {  //通过paperId获取试题题目信息
          this.topic = {...res.data}
          // 按固定顺序处理：1=选择题, 2=填空题, 3=判断题, 4=主观题
          const typeOrder = [1, 2, 3, 4]
          typeOrder.forEach(type => {
            let data = this.topic[type] || []
            this.topicCount.push(data.length)
            let currentScore = 0
            for(let i = 0; i < data.length; i++) {
              currentScore += data[i].score || 0
            }
            this.score.push(currentScore)
          })
        })
      })
    },
    edit(examCode) {
      //编辑试卷
      this.dialogVisible = true;
      this.$axios(`/api/exam/${examCode}`).then((res) => {
        //根据试卷id请求后台
        if (res.data.code == 200) {
          this.form = res.data.data;
        }
      });
    },
    submit() {
      //提交修改后的试卷信息
      if (this.form.source == null || this.form.source == "") {
                this.$message({
                    message: "科目名称不能为空",
                    type: "error",
                });
                return;
            }
            if (this.form.description == null || this.form.description == "") {
                this.$message({
                    message: "介绍不能为空",
                    type: "error",
                });
                return;
            }
            // 验证介绍格式：年份(4位)+季节(春/秋)+科目名称+考试类型(2字)
            const descPattern = /^\d{4}(春季|秋季).+.{2}$/;
            if (!descPattern.test(this.form.description)) {
                this.$message({
                    message: "介绍格式不正确，应为：年份+季节(春季/秋季)+科目名称+考试类型(2字)，如：2024春季数据结构期末",
                    type: "error",
                });
                return;
            }
            // 验证试卷类型和介绍最后的类型一致
            const descLast2 = this.form.description.slice(-2); // 介绍最后2个字
            const typeMap = {
                '期末': '期末考试',
                '期中': '期中考试',
                '练习': '日常练习'
            };
            const expectedType = typeMap[descLast2];
            if (expectedType && this.form.type !== expectedType) {
                this.$message({
                    message: `编辑失败：介绍的考试类型"${descLast2}"与试卷类型"${this.form.type}"不符`,
                    type: "error",
                });
                return;
            }
            if (this.form.institute == null || this.form.institute == "") {
                this.$message({
                    message: "所属学院不能为空",
                    type: "error",
                });
                return;
            }
            if (this.form.major == null || this.form.major == "") {
                this.$message({
                    message: "所属专业不能为空",
                    type: "error",
                });
                return;
            }
            if (this.form.grade == null || this.form.grade == "") {
                this.$message({
                    message: "年级不能为空",
                    type: "error",
                });
                return;
            }
            if (this.form.examDate == null || this.form.examDate == "") {
                this.$message({
                    message: "考试日期不能为空",
                    type: "error",
                });
                return;
            }
            if (this.form.totalTime == null || this.form.totalTime == "") {
                this.$message({
                    message: "持续时间不能为空",
                    type: "error",
                });
                return;
            }
            if (this.form.type == null || this.form.type == "") {
                this.$message({
                    message: "考试类型不能为空",
                    type: "error",
                });
                return;
            }
            if (this.form.tips == null || this.form.tips == "") {
                this.$message({
                    message: "考生提示不能为空",
                    type: "error",
                });
                return;
            }
      this.dialogVisible = false;
      
      console.log('提交的表单数据:', this.form);
      this.$axios({
        url: "/api/exam",
        method: "put",
        data: {
          ...this.form,
        },
      }).then((res) => {
        console.log('提交响应:', res.data);
        if (res.data.code == 200) {
          this.$message({
            //成功修改提示
            message: "更新成功",
            type: "success",
          });
          // 延迟刷新确保数据更新
          setTimeout(() => {
            this.getExamInfo();
          }, 500);
        } else {
          this.$message({
            message: "更新失败",
            type: "error",
          });
        }
      }).catch((error) => {
        console.error('更新失败:', error);
        this.$message({
          message: "更新失败",
          type: "error",
        });
      });
    },
    deleteRecord(examCode) {
      this.$confirm("确定删除该记录吗,该操作不可逆！！！", "删除提示", {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "danger",
      })
        .then(() => {
          //确认删除
          this.$axios({
            url: `/api/exam/${examCode}`,
            method: "delete",
          }).then((res) => {
            this.getExamInfo();
          });
        })
        .catch(() => {});
    },
    getExamInfo() {
      //分页查询所有试卷信息（带查询条件）
      const source = encodeURIComponent(this.searchForm.source || '@');
      const institute = encodeURIComponent(this.searchForm.institute || '@');
      const major = encodeURIComponent(this.searchForm.major || '@');
      const grade = encodeURIComponent(this.searchForm.grade || '@');
      
      this.$axios(
        `/api/exams/${this.pagination.current}/${this.pagination.size}/${source}/${institute}/${major}/${grade}`
      )
        .then((res) => {
          const data = res.data.data;
          this.pagination = {
            ...data,
            current: parseInt(data.current) || 1,
            total: parseInt(data.total) || 0,
            size: parseInt(data.size) || 6
          };
          // 触发表格闪入动画
          this.tableKey++;
        })
        .catch((error) => {
          console.error('获取考试列表失败:', error);
        });
    },
    //改变当前记录条数
    handleSizeChange(val) {
      this.pagination.size = parseInt(val) || 6;
      this.pagination.current = 1; // 重置到第一页
      this.getExamInfo();
    },
    //改变当前页码，重新发送请求
    handleCurrentChange(val) {
      this.pagination.current = parseInt(val) || 1;
      this.getExamInfo();
    },
    // 搜索
    handleSearch() {
      this.pagination.current = 1;
      this.getExamInfo();
    },
    // 重置搜索条件
    handleReset() {
      this.searchForm = {
        source: '',
        institute: '',
        major: '',
        grade: ''
      };
      this.pagination.current = 1;
      this.getExamInfo();
    },
  },
};
</script>
<style lang="less" scoped>
.exam {
  padding: 0px 40px;
  
  .search-area {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
    flex-wrap: wrap;
    
    .search-input {
      width: 240px;
    }
    
    .filter-select {
      width: 140px;
    }
  }
  
  .record-info {
    margin-bottom: 12px;
    color: #606266;
    font-size: 14px;
  }
  
  .fade-table {
    animation: fadeIn 0.3s ease-in-out;
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
}

/* 编辑对话框按钮样式 */
.edit-dialog-footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  
  .dialog-btn {
    min-width: 80px;
    padding: 9px 20px;
  }
}

/* 试卷详情对话框样式 */
.content {
  .header {
    .title {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;
      flex-wrap: wrap;
      
      .subject-name {
        color: #1e40af;
        font-size: 18px;
        font-weight: 700;
        background: linear-gradient(135deg, #e8f0ff 0%, #dbeafe 100%);
        padding: 6px 14px;
        border-radius: 8px;
        border: 1px solid #93c5fd;
      }
      
      .exam-tip {
        display: flex;
        align-items: center;
        color: #666;
        font-size: 13px;
        font-weight: 500;
        background: #f5f5f5;
        padding: 6px 12px;
        border-radius: 8px;
        border: 1px solid #e0e0e0;
        
        i {
          margin-right: 6px;
          color: #999;
        }
      }
      
      .score-info {
        display: flex;
        align-items: center;
        background: #fffbeb;
        border: 1px solid #fcd34d;
        color: #b45309;
        font-size: 14px;
        font-weight: 600;
        padding: 6px 12px;
        border-radius: 8px;
        
        i {
          margin-right: 6px;
          color: #f59e0b;
        }
      }
      
      .time-info {
        display: flex;
        align-items: center;
        background: #eff6ff;
        border: 1px solid #93c5fd;
        color: #1d4ed8;
        font-size: 14px;
        font-weight: 600;
        padding: 6px 12px;
        border-radius: 8px;
        
        i {
          margin-right: 6px;
          color: #3b82f6;
        }
      }
    }
  }
  
  .inner {
    .titlei {
      color: #1e40af;
      font-weight: 600;
      font-size: 15px;
      display: flex;
      align-items: center;
      
      i {
        margin-right: 8px;
        color: #3b82f6;
      }
    }
    
    .contenti {
      padding: 10px 0;
      
      .question-table {
        width: 100%;
        
        .el-table__header th {
          background: #f0f7ff !important;
          color: #1e40af;
          font-weight: 600;
        }
        
        .el-table__row:hover > td {
          background: #f5f9ff !important;
        }
      }
    }
  }
}

/* 题目表格样式 */
.question-table {
  .el-table__header-wrapper th {
    background: linear-gradient(135deg, #e8f0ff 0%, #dbeafe 100%) !important;
    color: #1e40af !important;
  }
}

/* 操作按钮居中样式 */
.operation-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.operation-buttons .el-button {
  margin: 2px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 主观题参考答案展开样式 */
.essay-answer-expand {
  padding: 15px 20px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-left: 4px solid #0ea5e9;
  border-radius: 4px;
  margin: 5px 0;
}
.essay-answer-expand strong {
  color: #0369a1;
  font-size: 14px;
}
.essay-answer-expand p {
  margin: 10px 0 0 0;
  color: #334155;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 试卷详情关闭按钮样式 */
.exam-detail-footer {
  display: flex;
  justify-content: center;
  width: 100%;
  
  .close-btn {
    min-width: 120px;
    background-color: #fef2f2 !important;
    border-color: #fca5a5 !important;
    color: #dc2626 !important;
    
    &:hover {
      background-color: #fee2e2 !important;
      border-color: #f87171 !important;
    }
  }
}

/* 深色模式下试卷内容弹窗样式 */
.dark-theme .content {
  background: #1a2e3e !important;
}
.dark-theme .content .el-collapse {
  background: #1a2e3e !important;
  border-color: #3a5a7a !important;
}
.dark-theme .content .el-collapse-item__header {
  background: #1e3a5a !important;
  color: #e0f0ff !important;
  border-color: #3a5a7a !important;
}
.dark-theme .content .el-collapse-item__wrap {
  background: #1a2e3e !important;
  border-color: #3a5a7a !important;
}
.dark-theme .content .el-collapse-item__content {
  background: #1a2e3e !important;
  color: #e0f0ff !important;
}
.dark-theme .content .title {
  background: transparent !important;
}
.dark-theme .content .subject-name {
  background: #1e4a6e !important;
  color: #7dd3fc !important;
  border-color: #3a7a9a !important;
}
.dark-theme .content .exam-tip {
  background: #2a3a4a !important;
  color: #a0b0c0 !important;
  border-color: #4a5a6a !important;
}
.dark-theme .content .score-info {
  background: #3a4a2a !important;
  color: #fcd34d !important;
  border-color: #5a6a3a !important;
}
.dark-theme .content .time-info {
  background: #1e3a5a !important;
  color: #7dd3fc !important;
  border-color: #3a5a7a !important;
}
.dark-theme .question-table .el-table__header-wrapper th {
  background: #1e3a5f !important;
  color: #e0f0ff !important;
}
.dark-theme .question-table {
  background: #1a2e3e !important;
}
.dark-theme .question-table .el-table__body-wrapper {
  background: #1a2e3e !important;
}
.dark-theme .question-table td,
.dark-theme .question-table tr {
  background: #1a2e3e !important;
  color: #e0f0ff !important;
}
.dark-theme .question-table .el-table__row:hover > td {
  background: #2a4a6a !important;
}
.dark-theme .essay-answer-expand {
  background: linear-gradient(135deg, #1a3a5a 0%, #1e4a6e 100%) !important;
  border-left-color: #0ea5e9 !important;
}
.dark-theme .essay-answer-expand strong {
  color: #7dd3fc !important;
}
.dark-theme .essay-answer-expand p {
  color: #e0f0ff !important;
}
.dark-theme .contenti {
  background: #1a2e3e !important;
}
.dark-theme .titlei {
  color: #e0f0ff !important;
}

/* 深色模式下内部折叠面板样式 - 使用深穿透 */
/deep/ .dark-theme .inner.el-collapse {
  background: #1a2e3e !important;
  border-color: #3a5a7a !important;
}
/deep/ .dark-theme .inner .el-collapse-item__header {
  background: #1e3a5a !important;
  color: #e0f0ff !important;
  border-color: #3a5a7a !important;
}
/deep/ .dark-theme .inner .el-collapse-item__wrap {
  background: #1a2e3e !important;
  border-color: #3a5a7a !important;
}
/deep/ .dark-theme .inner .el-collapse-item__content {
  background: #1a2e3e !important;
  color: #e0f0ff !important;
}
/* 深色模式下关闭按钮 */
.dark-theme .exam-detail-footer .close-btn {
  background-color: #3a2a2a !important;
  border-color: #6a4a4a !important;
  color: #f87171 !important;
}
.dark-theme .exam-detail-footer .close-btn:hover {
  background-color: #4a3a3a !important;
  border-color: #8a5a5a !important;
}
</style>

<style lang="less">
/* 非scoped样式 - 深色模式下试卷内容弹窗 */
body.dark-theme .inner.el-collapse {
  background: #1a2e3e !important;
  border-color: #3a5a7a !important;
}
body.dark-theme .inner .el-collapse-item__header {
  background: #1e3a5a !important;
  color: #ffffff !important;
  border-color: #3a5a7a !important;
}
body.dark-theme .inner .el-collapse-item__wrap {
  background: #1a2e3e !important;
  border-color: #3a5a7a !important;
}
body.dark-theme .inner .el-collapse-item__content {
  background: #1a2e3e !important;
  color: #e0f0ff !important;
}
body.dark-theme .inner .el-collapse-item__arrow {
  color: #ffffff !important;
}
body.dark-theme .inner .titlei {
  color: #ffffff !important;
}
body.dark-theme .inner .titlei i {
  color: #5dade2 !important;
}
/* 展开后的表格容器 */
body.dark-theme .inner .contenti {
  background: #1a2e3e !important;
}
body.dark-theme .inner .question-table {
  background: #1a2e3e !important;
}
body.dark-theme .inner .question-table .el-table__body-wrapper {
  background: #1a2e3e !important;
}
body.dark-theme .inner .question-table td,
body.dark-theme .inner .question-table tr {
  background: #1a2e3e !important;
  color: #e0f0ff !important;
}
body.dark-theme .inner .question-table .el-table__header-wrapper th {
  background: #1e3a5a !important;
  color: #ffffff !important;
}
body.dark-theme .inner .question-table .el-table__row:hover > td {
  background: #2a4a5a !important;
}
/* 展开后的空白区域修复 */
body.dark-theme .inner .el-table::before {
  background-color: #3a5a7a !important;
}
body.dark-theme .inner .el-table--border::after {
  background-color: #3a5a7a !important;
}
body.dark-theme .inner .el-table__empty-block {
  background: #1a2e3e !important;
}
body.dark-theme .inner .el-table__empty-text {
  color: #e0f0ff !important;
}
/* 科目名称、总分、时长标签修复 - 统一白色字体 */
body.dark-theme .content .subject-name {
  background: #1e4a6e !important;
  color: #ffffff !important;
  border-color: #3a6a8a !important;
}
body.dark-theme .content .score-info {
  background: #1e4a6e !important;
  color: #ffffff !important;
  border-color: #3a6a8a !important;
}
body.dark-theme .content .score-info i {
  color: #ffffff !important;
}
body.dark-theme .content .time-info {
  background: #1e4a6e !important;
  color: #ffffff !important;
  border-color: #3a6a8a !important;
}
body.dark-theme .content .time-info i {
  color: #ffffff !important;
}
body.dark-theme .content .exam-tip {
  background: #1e4a6e !important;
  color: #ffffff !important;
  border-color: #3a6a8a !important;
}
body.dark-theme .content .exam-tip i {
  color: #ffffff !important;
}

/* 教师端专用样式 - 覆盖管理员端主题变量污染 */
.exam .el-pagination .btn-next,
.exam .el-pagination .btn-prev {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
}

.exam .el-pagination .btn-next:hover,
.exam .el-pagination .btn-prev:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
}

.exam .el-pagination .el-pager li.active {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
  color: #fff !important;
}

.exam .el-pagination .el-pager li:hover {
  color: #409EFF !important;
}

.exam .el-button--primary {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
}

.exam .el-button--primary:hover {
  background-color: #66b1ff !important;
  border-color: #66b1ff !important;
}

/* 确保教师端不受管理员端主题变量影响 */
.exam {
  --el-color-primary: #409EFF !important;
  --admin-primary: #409EFF !important;
}
</style>
