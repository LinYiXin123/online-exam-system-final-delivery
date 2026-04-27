<template>
    <div class="exam-detail">
        <div class="detail-header">
            <el-page-header @back="goBack" content="答卷详情"></el-page-header>
        </div>
        
        <div class="exam-info" v-loading="loading">
            <el-card class="info-card">
                <div slot="header" class="clearfix">
                    <i class="el-icon-document"></i>
                    <span>考试信息</span>
                </div>
                <el-descriptions :column="2" border>
                    <el-descriptions-item label="课程名称">{{ examInfo.subject }}</el-descriptions-item>
                    <el-descriptions-item label="考试分数">
                        <el-tag :type="isPassExam ? 'success' : 'danger'" size="medium">
                            {{ examInfo.totalScoreActual || examInfo.etScore }}分
                        </el-tag>
                        <span v-if="examInfo.subjectiveScore > 0" style="margin-left: 8px; color: #909399; font-size: 12px;">
                            (客观题{{ examInfo.etScore }}分 + 主观题{{ examInfo.subjectiveScore }}分)
                        </span>
                    </el-descriptions-item>
                    <el-descriptions-item label="考试日期">{{ examInfo.answerDate }}</el-descriptions-item>
                    <el-descriptions-item label="是否及格">
                        <el-tag :type="isPassExam ? 'success' : 'danger'">
                            {{ isPassExam ? '及格' : '不及格' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="试卷总分">{{ examInfo.totalScore || '-' }}分</el-descriptions-item>
                    <el-descriptions-item label="考试时长">{{ examInfo.totalTime || '-' }}分钟</el-descriptions-item>
                    <el-descriptions-item label="试卷介绍" :span="2">{{ examInfo.examDescription || '暂无介绍' }}</el-descriptions-item>
                </el-descriptions>
            </el-card>

            <!-- 答题统计 -->
            <el-card class="stats-card">
                <div slot="header" class="clearfix">
                    <i class="el-icon-data-analysis"></i>
                    <span>答题统计</span>
                </div>
                <el-row :gutter="20">
                    <el-col :span="6">
                        <div class="stat-item total">
                            <div class="stat-number">{{ totalQuestions }}</div>
                            <div class="stat-label">总题数</div>
                        </div>
                    </el-col>
                    <el-col :span="6">
                        <div class="stat-item correct">
                            <div class="stat-number">{{ correctQuestions }}</div>
                            <div class="stat-label">答对题数</div>
                        </div>
                    </el-col>
                    <el-col :span="6">
                        <div class="stat-item incorrect">
                            <div class="stat-number">{{ incorrectQuestions }}</div>
                            <div class="stat-label">答错题数</div>
                        </div>
                    </el-col>
                    <el-col :span="6">
                        <div class="stat-item rate">
                            <div class="stat-number">{{ correctRate }}%</div>
                            <div class="stat-label">正确率</div>
                        </div>
                    </el-col>
                </el-row>
            </el-card>

            <!-- 答题详情 - 折叠式显示 -->
            <el-card class="question-card">
                <div slot="header" class="clearfix">
                    <i class="el-icon-notebook-2"></i>
                    <span>答题详情</span>
                    <span style="margin-left: 10px; color: #909399; font-size: 14px;">点击题目可展开/收起详情</span>
                </div>
                
                <el-collapse v-model="activeQuestions">
                    <!-- 遍历所有题目 -->
                    <el-collapse-item 
                        v-for="item in allQuestionsList" 
                        :key="item.key" 
                        :name="item.key"
                        :class="getQuestionClass(item)">
                        <template slot="title">
                            <div class="collapse-title">
                                <span class="question-num">第{{ item.questionNumber }}题</span>
                                <el-tag :type="getQuestionTagType(item)" size="mini" style="margin-left: 10px;">
                                    {{ item.typeName }}
                                </el-tag>
                                <el-tag v-if="item.type !== 'essay'" :type="item.isCorrect ? 'success' : (item.yourAnswer ? 'danger' : 'info')" size="mini" style="margin-left: 5px;">
                                    {{ item.isCorrect ? '✓ 正确' : (item.yourAnswer ? '✗ 错误' : '未作答') }}
                                </el-tag>
                                <el-tag v-else :type="getGradingStatusType(item.gradingStatus)" size="mini" style="margin-left: 5px;">
                                    {{ getGradingStatusText(item.gradingStatus) }}
                                </el-tag>
                            </div>
                        </template>
                        
                        <!-- 选择题内容 -->
                        <div v-if="item.type === 'choice'" class="question-detail">
                            <div class="question-content">{{ item.question }}</div>
                            <div class="question-options">
                                <div class="option" :class="{ 'correct-option': item.rightAnswer === 'A', 'your-option': item.yourAnswer === 'A' }">A. {{ item.answerA }}</div>
                                <div class="option" :class="{ 'correct-option': item.rightAnswer === 'B', 'your-option': item.yourAnswer === 'B' }">B. {{ item.answerB }}</div>
                                <div class="option" :class="{ 'correct-option': item.rightAnswer === 'C', 'your-option': item.yourAnswer === 'C' }">C. {{ item.answerC }}</div>
                                <div class="option" :class="{ 'correct-option': item.rightAnswer === 'D', 'your-option': item.yourAnswer === 'D' }">D. {{ item.answerD }}</div>
                            </div>
                            <div class="answer-info">
                                <span class="label">正确答案：</span><el-tag type="success" size="small">{{ item.rightAnswer }}</el-tag>
                                <span class="label" style="margin-left: 20px;">你的答案：</span>
                                <el-tag :type="item.isCorrect ? 'success' : 'danger'" size="small">{{ item.yourAnswer || '未作答' }}</el-tag>
                            </div>
                        </div>
                        
                        <!-- 填空题内容 -->
                        <div v-if="item.type === 'fill'" class="question-detail">
                            <div class="question-content">{{ item.question }}</div>
                            <div class="answer-info">
                                <div class="answer-row">
                                    <span class="label">正确答案：</span>
                                    <el-tag type="success" size="small" v-for="(ans, i) in item.answer" :key="'ans-'+i" style="margin-right: 5px;">{{ ans }}</el-tag>
                                </div>
                                <div class="answer-row" style="margin-top: 10px;">
                                    <span class="label">你的答案：</span>
                                    <template v-if="item.yourAnswer && item.yourAnswer.length > 0">
                                        <el-tag :type="item.isCorrect ? 'success' : 'danger'" size="small" v-for="(ans, i) in item.yourAnswer" :key="'your-'+i" style="margin-right: 5px;">{{ ans }}</el-tag>
                                    </template>
                                    <el-tag v-else type="info" size="small">未作答</el-tag>
                                </div>
                            </div>
                        </div>
                        
                        <!-- 判断题内容 -->
                        <div v-if="item.type === 'judge'" class="question-detail">
                            <div class="question-content">{{ item.question }}</div>
                            <div class="answer-info">
                                <span class="label">正确答案：</span><el-tag type="success" size="small">{{ item.answer === 'T' ? '正确' : '错误' }}</el-tag>
                                <span class="label" style="margin-left: 20px;">你的答案：</span>
                                <el-tag :type="item.isCorrect ? 'success' : 'danger'" size="small">
                                    {{ item.yourAnswer === 1 ? '正确' : item.yourAnswer === 2 ? '错误' : '未作答' }}
                                </el-tag>
                            </div>
                        </div>
                        
                        <!-- 主观题内容 -->
                        <div v-if="item.type === 'essay'" class="question-detail essay-detail">
                            <div class="question-content">{{ item.question }}</div>
                            <div class="essay-answer-section">
                                <div class="answer-block">
                                    <div class="answer-title">你的答案：</div>
                                    <div class="answer-content">{{ item.yourAnswer || '未作答' }}</div>
                                </div>
                                <div class="answer-block" v-if="item.referenceAnswer">
                                    <div class="answer-title">参考答案：</div>
                                    <div class="answer-content reference">{{ item.referenceAnswer }}</div>
                                </div>
                            </div>
                            <div class="grading-info" v-if="item.aiScore !== null || item.teacherScore !== null">
                                <el-divider content-position="left">评分信息</el-divider>
                                <el-row :gutter="20">
                                    <el-col :span="12" v-if="item.aiScore !== null">
                                        <div class="score-card ai-score">
                                            <div class="score-label">AI评分</div>
                                            <div class="score-value">{{ item.aiScore }}分</div>
                                        </div>
                                    </el-col>
                                    <el-col :span="12" v-if="item.teacherScore !== null">
                                        <div class="score-card teacher-score">
                                            <div class="score-label">教师评分</div>
                                            <div class="score-value">{{ item.teacherScore }}分</div>
                                        </div>
                                    </el-col>
                                </el-row>
                            </div>
                        </div>
                    </el-collapse-item>
                </el-collapse>
            </el-card>
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            loading: true,
            examCode: null,
            studentId: null,
            examInfo: {
                subject: '',
                etScore: 0,
                answerDate: '',
                ptScore: 0
            },
            questions: {
                choice: [],
                fill: [],
                judge: [],
                essay: []
            },
            totalQuestions: 0,
            correctQuestions: 0,
            incorrectQuestions: 0,
            correctRate: 0,
            activeQuestions: [] // 当前展开的题目
        };
    },
    computed: {
        // 判断是否及格：最终分数 >= 试卷总分 × 60%
        isPassExam() {
            const finalScore = this.examInfo.totalScoreActual || this.examInfo.etScore || 0
            const totalScore = this.examInfo.totalScore || 100
            const passLine = totalScore * 0.6
            return finalScore >= passLine
        },
        // 将所有题目合并成一个按题号排序的列表
        allQuestionsList() {
            let list = [];
            // 选择题
            if (this.questions.choice) {
                this.questions.choice.forEach((item, index) => {
                    list.push({
                        ...item,
                        type: 'choice',
                        typeName: '选择题',
                        key: 'choice-' + index
                    });
                });
            }
            // 填空题
            if (this.questions.fill) {
                this.questions.fill.forEach((item, index) => {
                    list.push({
                        ...item,
                        type: 'fill',
                        typeName: '填空题',
                        key: 'fill-' + index
                    });
                });
            }
            // 判断题
            if (this.questions.judge) {
                this.questions.judge.forEach((item, index) => {
                    list.push({
                        ...item,
                        type: 'judge',
                        typeName: '判断题',
                        key: 'judge-' + index
                    });
                });
            }
            // 主观题
            if (this.questions.essay) {
                this.questions.essay.forEach((item, index) => {
                    list.push({
                        ...item,
                        type: 'essay',
                        typeName: '主观题',
                        key: 'essay-' + index
                    });
                });
            }
            // 按题号排序
            return list.sort((a, b) => (a.questionNumber || 0) - (b.questionNumber || 0));
        }
    },
    created() {
        this.examCode = this.$route.query.examCode;
        this.studentId = this.$route.query.studentId;
        this.loadExamDetail();
    },
    methods: {
        goBack() {
            this.$router.go(-1);
        },
        async loadExamDetail() {
            try {
                // 获取考试基本信息
                const scoreRes = await this.$axios.get(`/api/scoreDetail/${this.examCode}/${this.studentId}`);
                if (scoreRes.data.code === 200) {
                    this.examInfo = scoreRes.data.data;
                }

                // 获取答题详情
                console.log('请求答题详情:', `/api/answer/detail/${this.examCode}/${this.studentId}`);
                const detailRes = await this.$axios.get(`/api/answer/detail/${this.examCode}/${this.studentId}`);
                console.log('答题详情返回:', detailRes.data);
                if (detailRes.data.code === 200) {
                    const data = detailRes.data.data;
                    console.log('questions数据:', data.questions);
                    this.questions = data.questions || { choice: [], fill: [], judge: [], essay: [] };
                    
                    // 计算统计信息
                    this.calculateStats();
                }
            } catch (error) {
                this.$message.error('加载答卷详情失败');
                console.error(error);
            } finally {
                this.loading = false;
            }
        },
        calculateStats() {
            let total = 0;
            let correct = 0;

            // 统计选择题
            if (this.questions.choice) {
                total += this.questions.choice.length;
                correct += this.questions.choice.filter(q => q.isCorrect).length;
            }

            // 统计填空题
            if (this.questions.fill) {
                total += this.questions.fill.length;
                correct += this.questions.fill.filter(q => q.isCorrect).length;
            }

            // 统计判断题
            if (this.questions.judge) {
                total += this.questions.judge.length;
                correct += this.questions.judge.filter(q => q.isCorrect).length;
            }

            this.totalQuestions = total;
            this.correctQuestions = correct;
            this.incorrectQuestions = total - correct;
            this.correctRate = total > 0 ? ((correct / total) * 100).toFixed(1) : 0;
        },
        // 获取主观题题号
        getEssayNumber(index) {
            const choiceLen = this.questions.choice ? this.questions.choice.length : 0;
            const fillLen = this.questions.fill ? this.questions.fill.length : 0;
            const judgeLen = this.questions.judge ? this.questions.judge.length : 0;
            return choiceLen + fillLen + judgeLen + index + 1;
        },
        // 获取评分状态类型
        getGradingStatusType(status) {
            if (status === 'teacher_reviewed') return 'success';
            if (status === 'ai_graded') return 'warning';
            return 'info';
        },
        // 获取评分状态文本
        getGradingStatusText(status) {
            if (status === 'teacher_reviewed') return '已批改';
            if (status === 'ai_graded') return 'AI已评分';
            return '待批改';
        },
        // 获取题目类型标签颜色
        getQuestionTagType(item) {
            if (item.type === 'choice') return '';
            if (item.type === 'fill') return 'warning';
            if (item.type === 'judge') return 'info';
            if (item.type === 'essay') return 'danger';
            return '';
        },
        // 获取题目折叠项的样式类
        getQuestionClass(item) {
            if (item.type === 'essay') return 'essay-collapse-item';
            if (item.isCorrect) return 'correct-collapse-item';
            if (item.yourAnswer) return 'incorrect-collapse-item';
            return 'unanswered-collapse-item';
        }
    }
};
</script>

<style lang="less" scoped>
.exam-detail {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.detail-header {
    margin-bottom: 20px;
}

.info-card, .stats-card, .question-card {
    margin-bottom: 20px;
}

.stat-item {
    text-align: center;
    padding: 20px;
    border-radius: 8px;
    transition: all 0.3s;
    
    &:hover {
        transform: translateY(-3px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    .stat-number {
        font-size: 32px;
        font-weight: bold;
        margin-bottom: 8px;
    }
    
    .stat-label {
        font-size: 14px;
        color: #666;
    }
    
    &.total {
        background: #f0f9ff;
        border: 1px solid #b3d8ff;
        
        .stat-number {
            color: var(--primary-color, #409EFF);
        }
        .stat-label {
            color: var(--primary-color, #409EFF);
        }
    }
    
    &.correct {
        background: #f0f9eb;
        border: 1px solid #c2e7b0;
        
        .stat-number {
            color: #67C23A;
        }
        .stat-label {
            color: #67C23A;
        }
    }
    
    &.incorrect {
        background: #fef0f0;
        border: 1px solid #fbc4c4;
        
        .stat-number {
            color: #F56C6C;
        }
        .stat-label {
            color: #F56C6C;
        }
    }
    
    &.rate {
        background: #fdf6ec;
        border: 1px solid #f5dab1;
        
        .stat-number {
            color: #E6A23C;
        }
        .stat-label {
            color: #E6A23C;
        }
    }
}

.question-section {
    margin-bottom: 30px;
    
    h3 {
        color: var(--primary-color, #409EFF);
        margin-bottom: 15px;
        padding-bottom: 10px;
        border-bottom: 2px solid var(--primary-color, #409EFF);
        
        i {
            margin-right: 8px;
        }
    }
}

.question-item {
    margin-bottom: 20px;
    padding: 20px;
    border-radius: 8px;
    border: 1px solid #e4e7ed;
    transition: all 0.3s;
    
    &:hover {
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }
    
    &.correct-bg {
        background: #f0f9ff;
        border-left: 4px solid #67C23A;
    }
    
    &.incorrect-bg {
        background: #fff5f5;
        border-left: 4px solid #F56C6C;
    }
}

.question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    
    .question-number {
        font-weight: bold;
        font-size: 16px;
        color: var(--primary-color, #409EFF);
    }
}

.question-content {
    font-size: 16px;
    line-height: 1.8;
    margin-bottom: 15px;
    color: #303133;
}

.question-options {
    margin-bottom: 15px;
    
    .option {
        padding: 8px 12px;
        margin-bottom: 8px;
        background: #f5f7fa;
        border-radius: 4px;
        font-size: 14px;
    }
}

.answer-info {
    padding-top: 12px;
    border-top: 1px dashed #dcdfe6;
    
    .label {
        font-weight: bold;
        color: #606266;
        margin-right: 8px;
    }
    
    .answer-row {
        margin-bottom: 8px;
    }
}

.clearfix::after {
    content: "";
    display: table;
    clear: both;
}

/* 主观题样式 */
.essay-item {
    background: #fafafa;
    border-left: 4px solid var(--primary-color, #409EFF);
}

.essay-answer-section {
    margin: 15px 0;
    
    .answer-block {
        margin-bottom: 15px;
        padding: 12px;
        background: #fff;
        border-radius: 6px;
        border: 1px solid #e4e7ed;
        
        .answer-title {
            font-weight: bold;
            color: #606266;
            margin-bottom: 8px;
            font-size: 14px;
        }
        
        .answer-content {
            font-size: 14px;
            line-height: 1.8;
            color: #303133;
            white-space: pre-wrap;
            
            &.reference {
                color: #67C23A;
                background: #f0f9eb;
                padding: 10px;
                border-radius: 4px;
            }
        }
    }
}

.grading-info {
    margin-top: 15px;
    
    .score-card {
        padding: 15px;
        border-radius: 8px;
        text-align: center;
        
        .score-label {
            font-size: 12px;
            color: #909399;
            margin-bottom: 5px;
        }
        
        .score-value {
            font-size: 24px;
            font-weight: bold;
            margin-bottom: 8px;
        }
        
        .score-comment {
            font-size: 12px;
            color: #606266;
            text-align: left;
            background: rgba(255,255,255,0.5);
            padding: 8px;
            border-radius: 4px;
            margin-top: 8px;
        }
        
        &.ai-score {
            background: #ecf5ff;
            .score-value { color: var(--primary-color, #409EFF); }
        }
        
        &.teacher-score {
            background: #f0f9eb;
            .score-value { color: #67C23A; }
        }
    }
    
    .final-score {
        margin-top: 15px;
        text-align: center;
        font-size: 16px;
        
        .score-highlight {
            font-size: 24px;
            font-weight: bold;
            color: #E6A23C;
        }
    }
}

/* 折叠项样式 */
.collapse-title {
    display: flex;
    align-items: center;
    
    .question-num {
        font-weight: bold;
        color: #303133;
    }
}

.question-detail {
    padding: 15px;
    background: #fafafa;
    border-radius: 6px;
}

.correct-collapse-item {
    /deep/ .el-collapse-item__header {
        background: #f0f9eb;
        border-left: 3px solid #67C23A;
    }
}

.incorrect-collapse-item {
    /deep/ .el-collapse-item__header {
        background: #fef0f0;
        border-left: 3px solid #F56C6C;
    }
}

.unanswered-collapse-item {
    /deep/ .el-collapse-item__header {
        background: #f4f4f5;
        border-left: 3px solid #909399;
    }
}

.essay-collapse-item {
    /deep/ .el-collapse-item__header {
        background: #fdf6ec;
        border-left: 3px solid #E6A23C;
    }
}

.correct-option {
    background: #e1f3d8 !important;
    border: 1px solid #67C23A;
}

.your-option {
    border: 2px solid var(--primary-color, #409EFF) !important;
}
</style>
