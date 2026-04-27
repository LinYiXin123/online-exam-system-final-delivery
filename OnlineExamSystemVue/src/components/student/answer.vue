<!--考生答题界面-->
<template>
    <div id="answer" :class="{ resizing: isResizing || isLeftResizing }">
        <!--顶部信息栏-->
        <div class="top">
            <ul class="item">
                <li class="item-top">
                    <i class="iconfont icon-menufold icon20" ref="toggle" @click="slider_flag = !slider_flag"></i>
                    <i class="iconfont icon-r-paper" style="font-size: 32px;">
                    </i>
                    <span style="color: #fff;">{{ isPractice ? '试卷练习' : examData.type }}-{{ examData.source }}</span>
                    <span v-if="examData.description" style="color: rgba(255,255,255,0.8); font-size: 14px; margin-left: 15px;">{{ examData.description }}</span>
                </li>
                <li @click="flag = !flag" style="cursor: pointer">
                    <i class="iconfont icon-user icon20"></i>
                    <div class="msg" v-if="flag" @click="flag = !flag">
                        <p>姓名：{{ userInfo.name }}</p>
                        <p>准考证号: {{ userInfo.id }}</p>
                    </div>
                </li>
                <li><i class="iconfont icon-arrLeft icon20"></i></li>
            </ul>
        </div>
        <div class="flexarea">
            <!--左边题目编号区-->
            <transition name="slider-fade">
                <div class="left" v-if="slider_flag" :style="{ width: leftSidebarWidth + 'px' }">
                    <!-- 左侧拖动分隔条 -->
                    <div class="left-resize-handle" @mousedown="startLeftResize"></div>
                    <ul class="l-top">
                        <li>
                            <a href="javascript:;"></a>
                            <span>当前</span>
                        </li>
                        <li>
                            <a href="javascript:;"></a>
                            <span>未答</span>
                        </li>
                        <li>
                            <a href="javascript:;"></a>
                            <span>正确</span>
                        </li>
                        <li>
                            <a href="javascript:;"></a>
                            <span>错误</span>
                        </li>
                        <li>
                            <a href="javascript:;"></a>
                            <span>标记</span>
                        </li>
                    </ul>
                    <!-- 正向计时显示 -->
                    <div class="elapsed-time-display">
                        <i class="el-icon-time"></i>
                        <span class="time-label">答题用时：</span>
                        <span class="time-value">{{ formattedElapsedTime }}</span>
                    </div>
                    <div class="l-bottom">
                        <div class="item">
                            <p>选择题部分</p>
                            <ul>
                                <li v-for="(list, index1) in topic[1]" :key="index1">
                                    <a href="javascript:;" @click="change(index1)" :class="{
                        border:
                            index == index1 &&
                            currentType == 1,
                        bg:
                            bg_flag &&
                            topic[1][index1].isClick ==
                            true,
                        correct:
                            isPractice &&
                            radio[index1] != undefined &&
                            isAnswerCorrect(radio[index1], topic[1][index1].rightAnswer),
                        incorrect:
                            isPractice &&
                            radio[index1] != undefined &&
                            !isAnswerCorrect(radio[index1], topic[1][index1].rightAnswer),
                    }">
                                        <span :class="{
                        mark:
                            topic[1][index1].isMark ==
                            true,
                    }"></span>
                                        {{ index1 + 1 }}
                                    </a>
                                </li>
                            </ul>
                        </div>
                        <div class="item">
                            <p>填空题部分</p>
                            <ul>
                                <li v-for="(list, index2) in topic[2]" :key="index2">
                                    <a href="javascript:;" @click="fill(index2)" :class="{
                        border:
                            index == index2 &&
                            currentType == 2,
                        bg: fillAnswer[index2][3] == true,
                    }"><span :class="{
                        mark:
                            topic[2][index2].isMark ==
                            true,
                    }"></span>{{ topicCount[0] + index2 + 1 }}
                                    </a>
                                </li>
                            </ul>
                        </div>
                        <div class="item">
                            <p>判断题部分</p>
                            <ul>
                                <li v-for="(list, index3) in topic[3]" :key="index3">
                                    <a href="javascript:;" @click="judge(index3)" :class="{
                        border:
                            index == index3 &&
                            currentType == 3,
                        bg:
                            bg_flag &&
                            topic[3][index3].isClick ==
                            true,
                        correct:
                            isPractice &&
                            judgeAnswer[index3] != undefined &&
                            ((judgeAnswer[index3] == 1 && topic[3][index3].answer == 'T') ||
                             (judgeAnswer[index3] == 2 && topic[3][index3].answer == 'F')),
                        incorrect:
                            isPractice &&
                            judgeAnswer[index3] != undefined &&
                            !((judgeAnswer[index3] == 1 && topic[3][index3].answer == 'T') ||
                              (judgeAnswer[index3] == 2 && topic[3][index3].answer == 'F')),
                    }"><span :class="{
                        mark:
                            topic[3][index3].isMark ==
                            true,
                    }"></span>{{
                        topicCount[0] +
                        topicCount[1] +
                        index3 +
                        1
                    }}</a>
                                </li>
                            </ul>
                        </div>
                        <!-- 主观题部分 -->
                        <div class="item" v-if="topic[4] && topic[4].length > 0">
                            <p>主观题部分</p>
                            <ul>
                                <li v-for="(list, index4) in topic[4]" :key="index4">
                                    <a href="javascript:;" @click="essay(index4)" :class="{
                        border:
                            index == index4 &&
                            currentType == 4,
                        bg:
                            bg_flag &&
                            topic[4][index4].isClick ==
                            true,
                    }"><span :class="{
                        mark:
                            topic[4][index4].isMark ==
                            true,
                    }"></span>{{
                        topicCount[0] +
                        topicCount[1] +
                        topicCount[2] +
                        index4 +
                        1
                    }}</a>
                                </li>
                            </ul>
                        </div>
                    </div>
                    <div class="button-area">
                        <div class="circle-buttons">
                            <el-tooltip content="提交试卷" placement="top">
                                <div class="circle-btn submit-btn" @click="commit()">
                                    <i class="el-icon-check"></i>
                                </div>
                            </el-tooltip>
                            <el-tooltip content="清除选择" placement="top">
                                <div class="circle-btn clear-btn" @click="clearAnswers()">
                                    <i class="el-icon-delete"></i>
                                </div>
                            </el-tooltip>
                            <el-tooltip content="返回系统" placement="top">
                                <div class="circle-btn back-btn" @click="backToSystem()">
                                    <i class="el-icon-back"></i>
                                </div>
                            </el-tooltip>
                        </div>
                        <el-button type="primary" style="width: 100%;font-size: 22px;margin: 25px 0 0 0;"
                            @click="finishExam()">{{ isPractice ? '结束练习' : '结束考试' }}</el-button>
                    </div>
                </div>
            </transition>
            <!--右边选择答题区-->
            <transition name="slider-fade">
                <div class="right" :class="{'with-ai-chat': showAiChat}">
                    <div class="title">
                        <p style="font-size: 22px;"><i class="iconfont icon-r-edit" style="font-size: 28px;"></i> {{
                        title }}</p>
                        <i class="iconfont icon-right auto-right"></i>
                        <span>全卷共{{
                        topicCount[0] + topicCount[1] + topicCount[2]
                    }}题，已答{{ answeredCount }}题，剩余{{ remainingCount }}题，标记{{ markedCount }}题<span v-if="isPractice">，正确{{ correctCount }}题，错误{{ incorrectCount }}题，当前正确率{{ correctRate }}%</span>
                            <span v-if="!isPractice">
                                <i class="iconfont icon-time"></i>倒计时：<b :class="{'time-warning': time <= 300, 'time-danger': time <= 60}">{{
                        formattedTime
                    }}</b></span>
                        </span>
                    </div>
                    <div class="content">
                        <p class="topic">
                            <span class="number">{{ number }}</span>{{ showQuestion }}
                        </p>
                        <div v-if="currentType == 1">
                            <el-radio-group v-model="radio[index]" @change="getChangeLabel">
                                <el-radio :label="1">{{
                        showAnswer.answerA
                    }}</el-radio>
                                <el-radio :label="2">{{
                        showAnswer.answerB
                    }}</el-radio>
                                <el-radio :label="3">{{
                        showAnswer.answerC
                    }}</el-radio>
                                <el-radio :label="4">{{
                        showAnswer.answerD
                    }}</el-radio>
                            </el-radio-group>
                            <div class="analysis" v-if="isPractice && radio[index] != undefined">
                                <ul>
                                    <li>
                                        <el-tag type="success" size="medium">正确答案：</el-tag>
                                        <el-tag size="medium" class="answer-tag">{{ reduceAnswer.rightAnswer }}</el-tag>
                                    </li>
                                    <li>
                                        <el-tag type="info" size="medium">我的答案：</el-tag>
                                        <el-tag :type="isAnswerCorrect(radio[index], reduceAnswer.rightAnswer) ? 'success' : 'danger'" size="medium" class="answer-tag">
                                            {{ getAnswerLetter(radio[index]) }}
                                        </el-tag>
                                    </li>
                                    <li class="analysis-header">
                                        <el-tag size="medium">题目解析：</el-tag>
                                        <el-button type="text" size="mini" @click="generateAnalysis(index)" :loading="generatingAnalysis && currentAnalysisIndex === index" :disabled="generatingAnalysis && currentAnalysisIndex === index">
                                            <i class="el-icon-magic-stick"></i> AI生成解析
                                        </el-button>
                                    </li>
                                    <li>
                                        <div v-if="generatingAnalysis && currentAnalysisIndex === index" class="generating">
                                            <span class="wave-text">正</span>
                                            <span class="wave-text">在</span>
                                            <span class="wave-text">生</span>
                                            <span class="wave-text">成</span>
                                            <span class="wave-text">解</span>
                                            <span class="wave-text">析</span>
                                            <span class="wave-text">.</span>
                                            <span class="wave-text">.</span>
                                            <span class="wave-text">.</span>
                                        </div>
                                        <div v-else class="analysis-content" v-html="formatAnalysis(reduceAnswer.analysis || '暂无解析')"></div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <div class="fill" v-if="currentType == 2">
                            <div v-for="(item, currentIndex) in part" :key="currentIndex">
                                <el-input placeholder="请填在此处" v-model="fillAnswer[index][currentIndex]" clearable
                                    @blur="fillBG">
                                </el-input>
                            </div>
                            <div class="analysis" v-if="isPractice &&
                        fillAnswer[index][3] == true
                        ">
                                <ul>
                                    <li>
                                        <el-tag type="success" size="medium">正确答案：</el-tag>
                                        <el-tag size="medium" class="answer-tag">{{ topic[2][index].answer }}</el-tag>
                                    </li>
                                    <li><el-tag size="medium">题目解析：</el-tag></li>
                                    <li>
                                        <div class="analysis-content" v-html="formatAnalysis(topic[2][index].analysis || '暂无解析')"></div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <div class="judge" v-if="currentType == 3">
                            <el-radio-group v-model="judgeAnswer[index]" @change="getJudgeLabel"
                                v-if="currentType == 3">
                                <el-radio :label="1">正确</el-radio>
                                <el-radio :label="2">错误</el-radio>
                            </el-radio-group>
                            <div class="analysis" v-if="isPractice && judgeAnswer[index] != undefined">
                                <ul>
                                    <li>
                                        <el-tag type="success" size="medium">正确答案：</el-tag>
                                        <el-tag size="medium" class="answer-tag">{{ topic[3][index].answer == "T" ? "正确" : "错误" }}</el-tag>
                                    </li>
                                    <li>
                                        <el-tag type="info" size="medium">我的答案：</el-tag>
                                        <el-tag :type="((judgeAnswer[index] == 1 && topic[3][index].answer == 'T') || (judgeAnswer[index] == 2 && topic[3][index].answer == 'F')) ? 'success' : 'danger'" size="medium" class="answer-tag">
                                            {{ judgeAnswer[index] == 1 ? "正确" : "错误" }}
                                        </el-tag>
                                    </li>
                                    <li class="analysis-header">
                                        <el-tag size="medium">题目解析：</el-tag>
                                        <el-button type="text" size="mini" @click="generateJudgeAnalysis(index)" :loading="generatingAnalysis && currentAnalysisIndex === index && currentType === 3" :disabled="generatingAnalysis && currentAnalysisIndex === index && currentType === 3">
                                            <i class="el-icon-magic-stick"></i> AI生成解析
                                        </el-button>
                                    </li>
                                    <li>
                                        <div v-if="generatingAnalysis && currentAnalysisIndex === index && currentType === 3" class="generating">
                                            <span class="wave-text">正</span>
                                            <span class="wave-text">在</span>
                                            <span class="wave-text">生</span>
                                            <span class="wave-text">成</span>
                                            <span class="wave-text">解</span>
                                            <span class="wave-text">析</span>
                                            <span class="wave-text">.</span>
                                            <span class="wave-text">.</span>
                                            <span class="wave-text">.</span>
                                        </div>
                                        <div v-else class="analysis-content" v-html="formatAnalysis(topic[3][index].analysis || '暂无解析')"></div>
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <!-- 主观题答题区域 -->
                        <div class="essay" v-if="currentType == 4">
                            <el-input
                                type="textarea"
                                :rows="8"
                                placeholder="请在此输入你的答案..."
                                v-model="essayAnswer[index]"
                                @blur="saveEssayAnswer">
                            </el-input>
                        </div>
                    </div>
                    <div class="operation">
                        <ul class="end">
                            <li @click="previous()" style="font-size: 24px;">
                                <i class="iconfont icon-r-left" style="font-size: 32px;margin-right: 5px;"></i><span>
                                    上一题</span>
                            </li>
                            <li @click="mark()">
                                <span>标记</span>
                            </li>
                            <li @click="next()" style="font-size: 24px;">
                                <span>下一题 </span><i class="iconfont icon-r-right"
                                    style="font-size: 32px;margin-left: 5px;"></i>
                            </li>
                        </ul>
                    </div>
                </div>
            </transition>
            <!--AI问答窗口-->
            <transition name="slide-left">
                <div class="ai-chat-wrapper" v-if="showAiChat" :style="{ width: aiChatWidth + 'px' }">
                    <!-- 拖动分隔条 -->
                    <div class="resize-handle" @mousedown="startResize"></div>
                    <AiChat :currentQuestion="currentQuestionText" :examCode="examData.examCode" :subject="examData.source" :closable="true" @close="showAiChat = false" />
                </div>
            </transition>
        </div>
        <!--AI助手悬浮按钮-->
        <div class="ai-float-btn" @click="showAiChat = !showAiChat" v-if="!showAiChat">
            <i class="el-icon-chat-dot-round"></i>
            <span>AI助手</span>
        </div>
    </div>
</template>

<script>
import { mapState } from "vuex";
import AiChat from './AiChat.vue';

export default {
    components: {
        AiChat
    },
    data() {
        return {
            startTime: null, //考试开始时间
            endTime: null, //考试结束时间
            time: null, //考试持续时间
            reduceAnswer: [], //vue官方不支持3层以上数据嵌套,如嵌套则会数据渲染出现问题,此变量直接接收3层嵌套时的数据。
            answerScore: 0, //答题总分数
            bg_flag: false, //已答标识符,已答改变背景色
            isFillClick: false, //选择题是否点击标识符
            slider_flag: true, //左侧显示隐藏标识符
            flag: false, //个人信息显示隐藏标识符
            currentType: 1, //当前题型类型  1--选择题  2--填空题  3--判断题
            radio: [], //保存考生所有选择题的选项
            title: "请选择正确的选项",
            index: 0, //全局index
            userInfo: {
                //用户信息
                name: null,
                id: null,
            },
            topicCount: [], //每种类型题目的总数
            score: [], //每种类型分数的总数
            examData: {
                //考试信息
                // source: null,
                // totalScore: null,
            },
            topic: {
                //试卷信息
            },
            showQuestion: '', //当前显示题目信息
            showAnswer: {}, //当前题目对应的答案选项
            number: 1, //题号
            part: null, //填空题的空格数量
            fillAnswer: [[]], //二维数组保存所有填空题答案
            judgeAnswer: [], //保存所有判断题答案
            essayAnswer: [], //保存所有主观题答案
            topic1Answer: [], //学生选择题作答编号,
            rightAnswer: "",
            timer: "",
            showAiChat: false, //AI问答窗口显示状态
            aiChatWidth: 450, //AI聊天窗口宽度
            isResizing: false, //是否正在拖动
            leftSidebarWidth: 260, //左侧题号区域宽度
            isLeftResizing: false, //左侧是否正在拖动
            generatingAnalysis: false, //是否正在生成解析
            currentAnalysisIndex: null, //当前生成解析的题目索引
            elapsedTime: 0, //正向计时（秒）
            elapsedTimer: null, //正向计时器
            // 防作弊相关
            switchCount: 0, //切屏次数
            maxSwitchCount: 4, //最大允许切屏次数（第4次自动提交）
            isPageVisible: true, //页面是否可见
            isDialogOpen: false, //是否有对话框打开（避免对话框触发切屏检测）
            isSubmitting: false, //防止重复交卷
        };
    },
    created() {
        this.getCookies();
        if (!this.isPractice) {
            this.getExamData();
        } else {
            this.getPracticeExamData();
        }
        // 启动正向计时器
        this.startElapsedTimer();
    },
    mounted() {
        // 确保计时器已启动
        if (!this.elapsedTimer) {
            this.startElapsedTimer();
        }
        // 防作弊：监听页面可见性变化（切屏检测）
        if (!this.isPractice) {
            this.initAntiCheat();
        }
    },
    methods: {
        // 初始化防作弊检测
        initAntiCheat() {
            // 先移除可能存在的旧监听器
            this.removeAntiCheat();
            // 监听页面可见性变化
            document.addEventListener('visibilitychange', this.handleVisibilityChange);
            // 监听窗口失去焦点
            window.addEventListener('blur', this.handleWindowBlur);
        },
        // 移除防作弊检测
        removeAntiCheat() {
            document.removeEventListener('visibilitychange', this.handleVisibilityChange);
            window.removeEventListener('blur', this.handleWindowBlur);
        },
        // 处理页面可见性变化
        handleVisibilityChange() {
            // 练习模式或对话框打开时不检测
            if (this.isPractice || this.isDialogOpen) return;
            if (document.hidden) {
                this.isPageVisible = false;
                this.switchCount++;
                this.showSwitchWarning();
            } else {
                this.isPageVisible = true;
            }
        },
        // 处理窗口失去焦点
        handleWindowBlur() {
            // 如果有对话框打开，不触发切屏检测
            if (this.isDialogOpen) return;
            if (!this.isPractice && this.isPageVisible) {
                this.switchCount++;
                this.showSwitchWarning();
            }
        },
        // 数组乱序（Fisher-Yates洗牌算法）
        shuffleArray(array) {
            const arr = [...array];
            for (let i = arr.length - 1; i > 0; i--) {
                const j = Math.floor(Math.random() * (i + 1));
                [arr[i], arr[j]] = [arr[j], arr[i]];
            }
            return arr;
        },
        // 打乱题目顺序
        shuffleQuestions(topic) {
            const shuffled = {};
            Object.keys(topic).forEach(key => {
                shuffled[key] = this.shuffleArray(topic[key]);
            });
            return shuffled;
        },
        // 打乱选择题选项（保持正确答案对应关系）
        shuffleOptions(question) {
            if (!question.answerA) return question;
            const options = [
                { key: 'A', value: question.answerA },
                { key: 'B', value: question.answerB },
                { key: 'C', value: question.answerC },
                { key: 'D', value: question.answerD }
            ].filter(opt => opt.value);
            
            const correctAnswer = question.rightAnswer;
            const shuffledOptions = this.shuffleArray(options);
            
            // 更新选项和正确答案
            const newQuestion = { ...question };
            const keyMap = ['A', 'B', 'C', 'D'];
            shuffledOptions.forEach((opt, idx) => {
                newQuestion[`answer${keyMap[idx]}`] = opt.value;
                if (opt.key === correctAnswer) {
                    newQuestion.rightAnswer = keyMap[idx];
                }
            });
            return newQuestion;
        },
        // 显示切屏警告
        showSwitchWarning() {
            const remaining = this.maxSwitchCount - this.switchCount;
            if (this.switchCount >= this.maxSwitchCount) {
                this.$alert('您已多次离开考试页面，系统将自动提交试卷！', '警告', {
                    confirmButtonText: '确定',
                    type: 'error',
                    callback: () => {
                        this.submitTest(); // 自动提交
                    }
                });
            } else {
                this.$notify({
                    title: '警告',
                    message: `检测到您离开了考试页面！这是第 ${this.switchCount} 次，还剩 ${remaining} 次机会，超过将自动交卷！`,
                    type: 'warning',
                    duration: 5000
                });
            }
        },
        saveProgress() {
            // 保存当前答题进度到localStorage
            const progress = {
                examCode: this.$route.query.examCode,
                radio: this.radio,
                fillAnswer: this.fillAnswer,
                judgeAnswer: this.judgeAnswer,
                currentIndex: this.index,
                currentType: this.currentType,
                saveTime: new Date().toLocaleString()
            };
            
            localStorage.setItem(`exam_progress_${this.$route.query.examCode}`, JSON.stringify(progress));
            
            this.$message({
                type: 'success',
                message: '答题进度已保存！',
                duration: 2000
            });
        },
        clearAnswers() {
            // 清除所有答案选择
            this.isDialogOpen = true;
            this.$confirm('确定要清除所有答案吗？此操作不可恢复！', '警告', {
                confirmButtonText: '确定清除',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 清空所有答案
                this.radio = [];
                this.fillAnswer = [];
                this.judgeAnswer = [];
                
                // 重置填空题数组
                let len = this.topicCount[1];
                let father = [];
                for (let i = 0; i < len; i++) {
                    let children = [null, null, null, null];
                    father.push(children);
                }
                this.fillAnswer = father;
                
                // 清除题目的已答标记
                if (this.topic[1]) {
                    this.topic[1].forEach(item => {
                        item.isClick = false;
                    });
                }
                if (this.topic[3]) {
                    this.topic[3].forEach(item => {
                        item.isClick = false;
                    });
                }
                
                this.bg_flag = false;
                
                this.$message({
                    type: 'success',
                    message: '已清除所有答案！',
                    duration: 2000
                });
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消清除',
                    duration: 1500
                });
            }).finally(() => {
                this.isDialogOpen = false;
            });
        },
        backToSystem() {
            // 返回考试系统
            this.isDialogOpen = true;
            this.$confirm('确定要返回考试系统吗？未提交的答案将不会保存！', '提示', {
                confirmButtonText: '确定返回',
                cancelButtonText: '取消',
                type: 'warning',
                confirmButtonClass: 'custom-confirm-btn'
            }).then(() => {
                // 清除计时器
                if (this.timer) {
                    clearInterval(this.timer);
                }
                // 移除切屏检测监听
                this.removeAntiCheat();
                // 返回到试卷列表页面
                this.$router.push('/startExam');
            }).catch(() => {
                this.$message({
                    type: 'info',
                    message: '已取消',
                    duration: 1500
                });
            }).finally(() => {
                this.isDialogOpen = false;
            });
        },
        finishExam() {
            if (this.isPractice) {
                // 练习交卷
                const message = this.remainingCount > 0 
                    ? `还剩${this.remainingCount}题没完成，是否结束本次练习？` 
                    : "确认结束本次练习？";
                this.$confirm(message, "友情提示", {
                    confirmButtonText: "立即交卷",
                    cancelButtonText: "继续练习",
                    type: "warning",
                })
                    .then(() => {
                        this.commit();
                    })
                    .catch(() => { });
            } else if (this.time > 0) {
                // 提前交卷
                const message = this.remainingCount > 0 
                    ? `还剩${this.remainingCount}题没完成，是否提前交卷？` 
                    : "考试结束时间未到，是否提前交卷？";
                this.$confirm(message, "友情提示", {
                    confirmButtonText: "提交",
                    cancelButtonText: "取消",
                    type: "warning",
                })
                    .then(() => {
                        this.commit();
                    })
                    .catch(() => { });
            }
        },
        getTime(date) {
            //日期格式化
            let year = date.getFullYear();
            let month =
                date.getMonth() + 1 < 10
                    ? "0" + (date.getMonth() + 1)
                    : date.getMonth() + 1;
            let day =
                date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
            let hours =
                date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
            let minutes =
                date.getMinutes() < 10
                    ? "0" + date.getMinutes()
                    : date.getMinutes();
            let seconds =
                date.getSeconds() < 10
                    ? "0" + date.getSeconds()
                    : date.getSeconds();
            // 拼接
            return (
                year +
                "-" +
                month +
                "-" +
                day +
                " " +
                hours +
                ":" +
                minutes +
                ":" +
                seconds
            );
        },
        buildExamDateTime(dateStr, timeStr) {
            if (!dateStr || !timeStr) {
                return null;
            }
            return new Date(`${dateStr}T${timeStr}`);
        },
        calculateRemainingExamSeconds() {
            const totalSeconds = (this.examData.totalTime || 0) * 60;
            const examDate = this.examData.examDate ? this.examData.examDate.substr(0, 10) : null;
            const now = new Date();
            const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
            if (!examDate || examDate < today) {
                return totalSeconds;
            }
            const endDateTime = this.buildExamDateTime(examDate, this.examData.endTime);
            if (!endDateTime || Number.isNaN(endDateTime.getTime())) {
                return totalSeconds;
            }
            const remainingSeconds = Math.floor((endDateTime.getTime() - Date.now()) / 1000);
            return Math.max(0, Math.min(totalSeconds, remainingSeconds));
        },
        normalizeFillAnswers(answerText) {
            if (!answerText) {
                return [];
            }
            return answerText
                .split(/[，,、\\/\\s]+/)
                .map(item => item && item.trim())
                .filter(Boolean);
        },
        startElapsedTimer() {
            // 启动正向计时器，每秒更新一次
            this.elapsedTimer = setInterval(() => {
                this.elapsedTime++;
            }, 1000);
        },
        getCookies() {
            //获取cookie - 优先从sessionStorage获取（标签页隔离）
            this.userInfo.name = sessionStorage.getItem("cname") || this.$cookies.get("cname");
            this.userInfo.id = sessionStorage.getItem("cid") || this.$cookies.get("cid");
        },
        calcuScore() {
            //计算答题分数
        },
        getPracticeExamData() {
            //获取当前练习所有信息
            let date = new Date();
            this.startTime = this.getTime(date);
            let examCode = this.$route.query.examCode; //获取路由传递过来的试卷编号
            this.$axios(`/api/exam/${examCode}`).then((res) => {
                //通过examCode请求试卷详细信息
                this.examData = { ...res.data.data }; //获取考试详情
                this.index = 0;
                this.$axios(`/api/practice/${this.examData.source}`).then((res) => {
                    //通过examCode获取练习题目信息（根据科目名称读取所有试题）
                    this.topic = { ...res.data };
                    let reduceAnswer = this.topic[1][this.index];
                    this.reduceAnswer = reduceAnswer;
                    let keys = Object.keys(this.topic); //对象转数组
                    keys.forEach((e) => {
                        let data = this.topic[e];
                        this.topicCount.push(data.length);
                        let currentScore = 0;
                        for (let i = 0; i < data.length; i++) {
                            //循环每种题型,计算出总分
                            currentScore += data[i].score;
                            // 初始化isMark属性，确保Vue响应式
                            if (typeof data[i].isMark === 'undefined') {
                                this.$set(data[i], 'isMark', false);
                            }
                        }
                        this.score.push(currentScore); //把每种题型总分存入score
                    });
                    let len = this.topicCount[1];
                    let father = [];
                    for (let i = 0; i < len; i++) {
                        //根据填空题数量创建二维空数组存放每道题答案
                        let children = [null, null, null, null];
                        father.push(children);
                    }
                    this.fillAnswer = father;
                    let dataInit = this.topic[1];
                    this.number = 1;
                    this.showQuestion = dataInit[0].question;
                    this.showAnswer = dataInit[0];
                    
                    // 恢复标记状态（练习模式）
                    this.$nextTick(() => {
                        this.loadMarkStatus();
                    });
                });
            });
        },
        getExamData() {
            //获取当前试卷所有信息
            let date = new Date();
            this.startTime = this.getTime(date);
            let examCode = this.$route.query.examCode; //获取路由传递过来的试卷编号
            this.$axios(`/api/exam/${examCode}`).then((res) => {
                //通过examCode请求试卷详细信息
                this.examData = { ...res.data.data }; //获取考试详情
                this.index = 0;
                this.time = this.calculateRemainingExamSeconds(); // 取考试总时长与实际剩余时间的较小值
                if (this.time <= 0) {
                    this.$message({
                        type: "error",
                        message: "考试已结束，不能继续作答",
                    });
                    this.$router.push({ path: "/examMsg", query: { examCode: examCode } });
                    return;
                }
                if (!this.timer) {
                    this.showTime();
                }
                // 根据模式调用不同的API
                let apiUrl;
                if (this.isPractice) {
                    // 练习模式：根据科目获取题库中所有题目
                    apiUrl = `/api/practice/${encodeURIComponent(this.examData.source)}`;
                } else {
                    // 考试模式：根据试卷ID获取试卷题目
                    let paperId = this.examData.paperId;
                    apiUrl = `/api/paper/${paperId}`;
                }
                
                this.$axios(apiUrl).then((res) => {
                    //获取试题题目信息
                    this.topic = { ...res.data };
                    let reduceAnswer = this.topic[1][this.index];
                    this.reduceAnswer = reduceAnswer;
                    let keys = Object.keys(this.topic); //对象转数组
                    keys.forEach((e) => {
                        let data = this.topic[e];
                        this.topicCount.push(data.length);
                        let currentScore = 0;
                        for (let i = 0; i < data.length; i++) {
                            //循环每种题型,计算出总分
                            currentScore += data[i].score;
                            // 初始化isMark属性，确保Vue响应式
                            if (typeof data[i].isMark === 'undefined') {
                                this.$set(data[i], 'isMark', false);
                            }
                        }
                        this.score.push(currentScore); //把每种题型总分存入score
                    });
                    let len = this.topicCount[1];
                    let father = [];
                    for (let i = 0; i < len; i++) {
                        //根据填空题数量创建二维空数组存放每道题答案
                        let children = [null, null, null, null];
                        father.push(children);
                    }
                    this.fillAnswer = father;
                    let dataInit = this.topic[1];
                    this.number = 1;
                    this.showQuestion = dataInit[0].question;
                    this.showAnswer = dataInit[0];
                    
                    // 恢复标记状态（考试模式）
                    this.$nextTick(() => {
                        this.loadMarkStatus();
                    });
                });
            });
        },
        change(index) {
            //选择题
            this.index = index;
            let reduceAnswer = this.topic[1][this.index];
            this.reduceAnswer = reduceAnswer;
            this.isFillClick = true;
            this.currentType = 1;
            let len = this.topic[1].length;
            if (this.index < len) {
                if (this.index <= 0) {
                    this.index = 0;
                }
                console.log(`总长度${len}`);
                console.log(`当前index:${index}`);
                this.title = "请选择正确的选项";
                let Data = this.topic[1];
                // console.log(Data)
                this.showQuestion = Data[this.index].question; //获取题目信息
                this.showAnswer = Data[this.index];
                this.number = this.index + 1;
            } else if (this.index >= len) {
                this.index = 0;
                this.fill(this.index);
            }
        },
        fillBG() {
            //填空题已答题目 如果已答该题目,设置第四个元素为true为标识符
            if (this.fillAnswer[this.index][0] != null) {
                this.fillAnswer[this.index][3] = true;
            }
        },
        fill(index) {
            //填空题
            let len = this.topic[2].length;
            this.currentType = 2;
            this.index = index;
            if (index < len) {
                if (index < 0) {
                    index = this.topic[1].length - 1;
                    this.change(index);
                } else {
                    this.title = "请在横线处填写答案";
                    let Data = this.topic[2];
                    console.log(Data);
                    this.showQuestion = Data[index].question; //获取题目信息
                    // 根据题目中括号或下划线的数量确定填空横线数量
                    let part = this.showQuestion.split("()").length - 1;
                    if (part === 0) {
                        // 尝试匹配下划线格式 ___
                        part = (this.showQuestion.match(/_{2,}/g) || []).length;
                    }
                    // 确保至少有1个输入框
                    this.part = part > 0 ? part : 1;
                    this.number = this.topicCount[0] + index + 1;
                }
            } else if (index >= len) {
                this.index = 0;
                this.judge(this.index);
            }
        },
        judge(index) {
            //判断题
            let len = this.topic[3].length;
            this.currentType = 3;
            this.index = index;
            if (this.index < len) {
                if (this.index < 0) {
                    this.index = this.topic[2].length - 1;
                    this.fill(this.index);
                } else {
                    console.log(`总长度${len}`);
                    console.log(`当前index:${this.index}`);
                    this.title = "请作出正确判断";
                    let Data = this.topic[3];
                    console.log(Data);
                    this.showQuestion = Data[index].question; //获取题目信息
                    this.number =
                        this.topicCount[0] + this.topicCount[1] + index + 1;
                }
            } else if (this.index >= len) {
                this.index = 0;
                this.change(this.index);
            }
        },
        essay(index) {
            // 主观题
            if (!this.topic[4] || this.topic[4].length === 0) return;
            let len = this.topic[4].length;
            this.currentType = 4;
            this.index = index;
            if (this.index < len) {
                if (this.index < 0) {
                    this.index = this.topic[3].length - 1;
                    this.judge(this.index);
                } else {
                    this.title = "请作答主观题";
                    let Data = this.topic[4];
                    this.showQuestion = Data[index].question;
                    this.number = this.topicCount[0] + this.topicCount[1] + this.topicCount[2] + index + 1;
                }
            } else if (this.index >= len) {
                this.index = 0;
                this.change(this.index);
            }
        },
        getChangeLabel(val) {
            //获取选择题作答选项
            this.radio[this.index] = val; //当前选择的序号
            if (val) {
                let data = this.topic[1];
                this.bg_flag = true;
                data[this.index]["isClick"] = true;
            }
            /* 保存学生答题选项 */
            this.topic1Answer[this.index] = val;
        },
        getJudgeLabel(val) {
            //获取判断题作答选项
            this.judgeAnswer[this.index] = val;
            if (val) {
                let data = this.topic[3];
                this.bg_flag = true;
                data[this.index]["isClick"] = true;
            }
        },
        saveEssayAnswer() {
            // 保存主观题答案
            if (this.essayAnswer[this.index]) {
                let data = this.topic[4];
                if (data && data[this.index]) {
                    this.bg_flag = true;
                    data[this.index]["isClick"] = true;
                }
            }
        },
        previous() {
            //上一题
            this.index--;
            switch (this.currentType) {
                case 1:
                    this.change(this.index);
                    break;
                case 2:
                    this.fill(this.index);
                    break;
                case 3:
                    this.judge(this.index);
                    break;
            }
        },
        next() {
            //下一题
            this.index++;
            switch (this.currentType) {
                case 1:
                    this.change(this.index);
                    break;
                case 2:
                    this.fill(this.index);
                    break;
                case 3:
                    this.judge(this.index);
                    break;
            }
        },
        mark() {
            //标记功能 - 切换标记状态（再次点击取消标记）
            switch (this.currentType) {
                case 1:
                    // 选择题标记切换 - 使用$set确保响应式更新
                    const currentMarkValue1 = this.topic[1][this.index]["isMark"];
                    this.$set(this.topic[1][this.index], "isMark", !currentMarkValue1);
                    break;
                case 2:
                    // 填空题标记切换 - 使用$set确保响应式更新
                    const currentMarkValue2 = this.topic[2][this.index]["isMark"];
                    this.$set(this.topic[2][this.index], "isMark", !currentMarkValue2);
                    break;
                case 3:
                    // 判断题标记切换 - 使用$set确保响应式更新
                    const currentMarkValue3 = this.topic[3][this.index]["isMark"];
                    this.$set(this.topic[3][this.index], "isMark", !currentMarkValue3);
            }
            
            // 保存标记状态到localStorage
            this.saveMarkStatus();
        },
        saveMarkStatus() {
            // 收集所有标记状态
            const markStatus = {
                type1: [], // 选择题标记
                type2: [], // 填空题标记
                type3: []  // 判断题标记
            };
            
            // 收集选择题标记
            if (this.topic[1]) {
                this.topic[1].forEach((item, index) => {
                    if (item.isMark) {
                        markStatus.type1.push(index);
                    }
                });
            }
            
            // 收集填空题标记
            if (this.topic[2]) {
                this.topic[2].forEach((item, index) => {
                    if (item.isMark) {
                        markStatus.type2.push(index);
                    }
                });
            }
            
            // 收集判断题标记
            if (this.topic[3]) {
                this.topic[3].forEach((item, index) => {
                    if (item.isMark) {
                        markStatus.type3.push(index);
                    }
                });
            }
            
            // 保存到localStorage，使用examCode作为key
            const examCode = this.$route.query.examCode;
            const storageKey = `exam_marks_${examCode}`;
            localStorage.setItem(storageKey, JSON.stringify(markStatus));
        },
        loadMarkStatus() {
            // 从localStorage恢复标记状态
            const examCode = this.$route.query.examCode;
            const storageKey = `exam_marks_${examCode}`;
            const savedMarks = localStorage.getItem(storageKey);
            
            if (savedMarks) {
                try {
                    const markStatus = JSON.parse(savedMarks);
                    
                    // 恢复选择题标记
                    if (markStatus.type1 && this.topic[1]) {
                        markStatus.type1.forEach(index => {
                            if (this.topic[1][index]) {
                                this.$set(this.topic[1][index], 'isMark', true);
                            }
                        });
                    }
                    
                    // 恢复填空题标记
                    if (markStatus.type2 && this.topic[2]) {
                        markStatus.type2.forEach(index => {
                            if (this.topic[2][index]) {
                                this.$set(this.topic[2][index], 'isMark', true);
                            }
                        });
                    }
                    
                    // 恢复判断题标记
                    if (markStatus.type3 && this.topic[3]) {
                        markStatus.type3.forEach(index => {
                            if (this.topic[3][index]) {
                                this.$set(this.topic[3][index], 'isMark', true);
                            }
                        });
                    }
                } catch (error) {
                    console.error('恢复标记状态失败:', error);
                }
            }
        },
        saveAnswerRecords() {
            // 保存每道题的答题记录到数据库
            const examCode = this.$route.query.examCode;
            const studentId = this.userInfo.id;
            const answerRecords = [];
            const requestTasks = [];

            // 保存选择题答案
            if (this.topic[1]) {
                this.topic1Answer.forEach((answer, index) => {
                    if (answer != null && this.topic[1][index]) {
                        let studentAnswer = '';
                        switch (answer) {
                            case 1: studentAnswer = 'A'; break;
                            case 2: studentAnswer = 'B'; break;
                            case 3: studentAnswer = 'C'; break;
                            case 4: studentAnswer = 'D'; break;
                        }
                        const isCorrect = studentAnswer === this.topic[1][index].rightAnswer ? 1 : 0;
                        answerRecords.push({
                            examCode: examCode,
                            studentId: studentId,
                            questionId: this.topic[1][index].questionId,
                            questionType: '1',
                            studentAnswer: studentAnswer,
                            isCorrect: isCorrect
                        });
                    }
                });
            }

            // 保存填空题答案
            if (this.topic[2]) {
                this.fillAnswer.forEach((answers, index) => {
                    if (answers && this.topic[2][index]) {
                        // 只取前3个元素作为实际答案，第4个元素是标识符
                        const realAnswers = answers.slice(0, 3).filter(a => a != null && a !== '' && a !== true);
                        const hasAnswer = realAnswers.length > 0;
                        if (hasAnswer) {
                            const expectedAnswers = this.normalizeFillAnswers(this.topic[2][index].answer);
                            const studentAnswers = realAnswers.map(ans => ans.trim());
                            const allCorrect = expectedAnswers.length === studentAnswers.length &&
                                expectedAnswers.every(ans => studentAnswers.includes(ans));
                            answerRecords.push({
                                examCode: examCode,
                                studentId: studentId,
                                questionId: this.topic[2][index].questionId,
                                questionType: '2',
                                studentAnswer: realAnswers.join(','),
                                isCorrect: allCorrect ? 1 : 0
                            });
                        }
                    }
                });
            }

            // 保存判断题答案
            if (this.topic[3]) {
                this.judgeAnswer.forEach((answer, index) => {
                    if (answer != null && this.topic[3][index]) {
                        const isCorrect = (answer == 1 && this.topic[3][index].answer == 'T') ||
                                        (answer == 2 && this.topic[3][index].answer == 'F') ? 1 : 0;
                        answerRecords.push({
                            examCode: examCode,
                            studentId: studentId,
                            questionId: this.topic[3][index].questionId,
                            questionType: '3',
                            studentAnswer: answer.toString(),
                            isCorrect: isCorrect
                        });
                    }
                });
            }

            // 批量提交答题记录
            if (answerRecords.length > 0) {
                requestTasks.push(
                    this.$axios.post('/api/studentAnswer/batchSave', answerRecords)
                        .then(res => {
                            console.log('答题记录保存成功', res.data);
                        })
                        .catch(err => {
                            console.error('答题记录保存失败', err);
                            throw err;
                        })
                );
            }

            // 保存主观题答案到essay_answer表
            if (this.topic[4] && this.topic[4].length > 0) {
                this.essayAnswer.forEach((answer, index) => {
                    if (answer && this.topic[4][index]) {
                        const essayData = {
                            examCode: examCode,
                            studentId: studentId,
                            questionId: this.topic[4][index].questionId,
                            studentAnswer: answer
                        };
                        requestTasks.push(
                            this.$axios.post('/api/essay/answer/save', essayData)
                                .then(res => {
                                    console.log('主观题答案保存成功', res.data);
                                })
                                .catch(err => {
                                    console.error('主观题答案保存失败', err);
                                    throw err;
                                })
                        );
                    }
                });
            }
            return Promise.all(requestTasks);
        },
        async commit() {
            //答案提交计算分数
            if (this.isSubmitting) {
                return;
            }
            this.isSubmitting = true;
            
            // 移除切屏检测监听器
            this.removeAntiCheat();
            
            // 提交时清除该考试的标记数据
            const examCode = this.$route.query.examCode;
            const storageKey = `exam_marks_${examCode}`;
            localStorage.removeItem(storageKey);
            
            /* 计算选择题总分 */
            let topic1Answer = this.topic1Answer;
            let finalScore = 0;
            topic1Answer.forEach((element, index) => {
                //循环每道选择题根据选项计算分数
                let right = null;
                if (element != null) {
                    switch (
                    element //选项1,2,3,4 转换为 "A","B","C","D"
                    ) {
                        case 1:
                            right = "A";
                            break;
                        case 2:
                            right = "B";
                            break;
                        case 3:
                            right = "C";
                            break;
                        case 4:
                            right = "D";
                    }
                    if (right == this.topic[1][index].rightAnswer) {
                        // 当前选项与正确答案对比
                        finalScore += this.topic[1][index].score; // 计算总分数
                    }
                }
            });

            /**计算填空题总分 */
            let fillAnswer = this.fillAnswer;
            console.log(this.topic[2])
            fillAnswer.forEach((element, index) => {
                const studentAnswers = element.slice(0, 3).filter(item => item != null && item !== '' && item !== true).map(item => item.trim());
                if (studentAnswers.length === 0 || !this.topic[2][index]) {
                    return;
                }
                const expectedAnswers = this.normalizeFillAnswers(this.topic[2][index].answer);
                const allCorrect = expectedAnswers.length === studentAnswers.length &&
                    expectedAnswers.every(answer => studentAnswers.includes(answer));
                if (allCorrect) {
                    finalScore += this.topic[2][index].score;
                }
            });
            /** 计算判断题总分 */
            let topic3Answer = this.judgeAnswer;
            topic3Answer.forEach((element, index) => {
                let right = null;
                switch (element) {
                    case 1:
                        right = "T";
                        break;
                    case 2:
                        right = "F";
                }
                if (right == this.topic[3][index].answer) {
                    // 当前选项与正确答案对比
                    finalScore += this.topic[3][index].score; // 计算总分数
                }
            });
            // 保存答题记录到数据库（考试模式和练习模式都保存）
            try {
                await this.saveAnswerRecords();
            
                // 提交分数到数据库
                let date = new Date();
                this.endTime = this.getTime(date);

                let answerDate = this.endTime.substr(0, 10);
                // 根据模式设置成绩类型：练习模式为practice，考试模式为exam
                let scoreType = this.isPractice ? 'practice' : 'exam';
                //提交成绩信息
                const res = await this.$axios({
                    url: "/api/score",
                    method: "post",
                    data: {
                        examCode: this.examData.examCode, //考试编号
                        studentId: this.userInfo.id, //学号
                        subject: this.examData.source, //课程名称
                        ptScore: 0, // 主观题分数由批改流程回写
                        etScore: finalScore, //客观题成绩
                        score: this.examData.totalScore, //试卷总分
                        answerDate: answerDate, //答题日期
                        scoreType: scoreType, // 成绩类型：exam-考试, practice-练习
                    },
                });

                if (res.data.code == 200) {
                    this.$router.push({
                        path: "/studentScore",
                        query: {
                            score: finalScore,
                            startTime: this.startTime,
                            endTime: this.endTime,
                        },
                    });
                } else {
                    this.$message({
                        type: "error",
                        message: res.data.message || "提交失败，请稍后重试",
                    });
                }
            } catch (error) {
                console.error("交卷失败:", error);
                this.$message({
                    type: "error",
                    message: "交卷失败，请稍后重试",
                });
            } finally {
                this.isSubmitting = false;
            }
        },
        showTime() {
            //倒计时
            this.timer = setInterval(() => {
                this.time -= 1;
                // 剩余10分钟提醒
                if (this.time == 600) {
                    this.$message({
                        showClose: true,
                        type: "error",
                        message: "考生注意,考试时间还剩10分钟！！！",
                    });
                }
                // 剩余5分钟提醒
                if (this.time == 300) {
                    this.$message({
                        showClose: true,
                        type: "warning",
                        message: "考生注意,考试时间还剩5分钟！！",
                    });
                }

                if (this.time <= 0) {
                    clearInterval(this.timer);
                    this.$message({
                        showClose: true,
                        type: "error",
                        message: "考试时间到，系统自动提交试卷",
                    });
                    this.commit();
                }
            }, 1000);
        },
        startResize(e) {
            // 开始拖动
            this.isResizing = true;
            document.addEventListener('mousemove', this.handleResize);
            document.addEventListener('mouseup', this.stopResize);
            e.preventDefault();
        },
        handleResize(e) {
            // 处理拖动
            if (!this.isResizing) return;
            
            const containerWidth = window.innerWidth;
            const newWidth = containerWidth - e.clientX;
            
            // 限制宽度范围：300px - 800px
            if (newWidth >= 300 && newWidth <= 800) {
                this.aiChatWidth = newWidth;
            }
        },
        stopResize() {
            // 停止拖动
            this.isResizing = false;
            document.removeEventListener('mousemove', this.handleResize);
            document.removeEventListener('mouseup', this.stopResize);
        },
        startLeftResize(e) {
            // 开始拖动左侧分隔条
            this.isLeftResizing = true;
            document.addEventListener('mousemove', this.handleLeftResize);
            document.addEventListener('mouseup', this.stopLeftResize);
            e.preventDefault();
        },
        handleLeftResize(e) {
            // 处理左侧拖动
            if (!this.isLeftResizing) return;
            
            const newWidth = e.clientX - 10; // 减去左侧边距
            
            // 限制宽度范围：200px - 400px
            if (newWidth >= 200 && newWidth <= 400) {
                this.leftSidebarWidth = newWidth;
            }
        },
        stopLeftResize() {
            // 停止左侧拖动
            this.isLeftResizing = false;
            document.removeEventListener('mousemove', this.handleLeftResize);
            document.removeEventListener('mouseup', this.stopLeftResize);
        },
        isAnswerCorrect(userAnswer, rightAnswer) {
            // 判断答案是否正确
            // userAnswer: 1,2,3,4
            // rightAnswer: 可能是 1,2,3,4 或者 "A","B","C","D"
            
            // 如果rightAnswer是字母，转换为数字
            const answerMap = { 'A': 1, 'B': 2, 'C': 3, 'D': 4 };
            const correctValue = answerMap[rightAnswer] || rightAnswer;
            
            return userAnswer == correctValue;
        },
        getAnswerLetter(answerNum) {
            // 将数字答案转换为字母
            const letterMap = { 1: 'A', 2: 'B', 3: 'C', 4: 'D' };
            return letterMap[answerNum] || answerNum;
        },
        formatAnalysis(text) {
            if (!text || text === '暂无解析') {
                return '<span class="no-analysis">暂无解析</span>';
            }
            
            // 移除Markdown格式符号并格式化
            let formatted = text
                // 移除标题标记 ### ## #
                .replace(/^###\s+(.+)$/gm, '<strong>$1</strong>')
                .replace(/^##\s+(.+)$/gm, '<strong>$1</strong>')
                .replace(/^#\s+(.+)$/gm, '<strong>$1</strong>')
                // 移除加粗标记 **text**
                .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
                // 移除斜体标记 *text*
                .replace(/\*(.*?)\*/g, '$1')
                // 移除代码块标记 ```
                .replace(/```[\s\S]*?```/g, '')
                .replace(/`([^`]+)`/g, '$1')
                // 处理有序列表 1. 2. 3.
                .replace(/^(\d+)\.\s+(.+)$/gm, '<div class="list-item"><span class="list-number">$1.</span> $2</div>')
                // 处理无序列表 - item
                .replace(/^[-•]\s+(.+)$/gm, '<div class="list-item"><span class="bullet">•</span> $1</div>')
                // 处理换行
                .replace(/\n\n/g, '<br><br>')
                .replace(/\n/g, '<br>')
                // 移除多余的空格
                .trim();
            
            return `<div class="formatted-analysis">${formatted}</div>`;
        },
        async generateAnalysis(index) {
            // 生成选择题解析
            this.generatingAnalysis = true;
            this.currentAnalysisIndex = index;
            
            try {
                const question = this.topic[1][index];
                const prompt = `请为以下选择题提供详细解析：

题目：${question.question}
A. ${question.answerA}
B. ${question.answerB}
C. ${question.answerC}
D. ${question.answerD}

正确答案：${question.rightAnswer}

请提供：
1. 正确答案的详细解释
2. 为什么其他选项不正确
3. 相关知识点总结

请用简洁清晰的语言回答。`;

                const response = await this.$axios.post('/api/ai/chat', {
                    question: prompt,
                    context: '',
                    subject: this.examData.source || '通用'
                });

                if (response.data.code === 200) {
                    // 更新题目的解析
                    this.$set(this.topic[1][index], 'analysis', response.data.data.answer);
                    this.reduceAnswer.analysis = response.data.data.answer;
                    
                    this.$message({
                        type: 'success',
                        message: '解析生成成功！',
                        duration: 2000
                    });
                } else {
                    throw new Error(response.data.message || '生成失败');
                }
            } catch (error) {
                console.error('生成解析失败:', error);
                this.$message({
                    type: 'error',
                    message: '解析生成失败，请稍后重试',
                    duration: 2000
                });
            } finally {
                this.generatingAnalysis = false;
                this.currentAnalysisIndex = null;
            }
        },
        async generateJudgeAnalysis(index) {
            // 生成判断题解析
            this.generatingAnalysis = true;
            this.currentAnalysisIndex = index;
            
            try {
                const question = this.topic[3][index];
                const prompt = `请为以下判断题提供详细解析：

题目：${question.question}

正确答案：${question.answer == 'T' ? '正确' : '错误'}

请提供：
1. 为什么这个判断是${question.answer == 'T' ? '正确' : '错误'}的
2. 相关知识点解释
3. 易错点提示

请用简洁清晰的语言回答。`;

                const response = await this.$axios.post('/api/ai/chat', {
                    question: prompt,
                    context: '',
                    subject: this.examData.source || '通用'
                });

                if (response.data.code === 200) {
                    // 更新题目的解析
                    this.$set(this.topic[3][index], 'analysis', response.data.data.answer);
                    
                    this.$message({
                        type: 'success',
                        message: '解析生成成功！',
                        duration: 2000
                    });
                } else {
                    throw new Error(response.data.message || '生成失败');
                }
            } catch (error) {
                console.error('生成解析失败:', error);
                this.$message({
                    type: 'error',
                    message: '解析生成失败，请稍后重试',
                    duration: 2000
                });
            } finally {
                this.generatingAnalysis = false;
                this.currentAnalysisIndex = null;
            }
        },
    },
    beforeDestroy() {
        // 移除切屏检测监听器
        this.removeAntiCheat();
        // 清除计时器
        if (this.timer) {
            clearInterval(this.timer);
        }
        if (this.elapsedTimer) {
            clearInterval(this.elapsedTimer);
        }
        // 清理拖动事件监听器
        document.removeEventListener('mousemove', this.handleResize);
        document.removeEventListener('mouseup', this.stopResize);
        document.removeEventListener('mousemove', this.handleLeftResize);
        document.removeEventListener('mouseup', this.stopLeftResize);
    },
    computed: {
        ...mapState(["isPractice"]),
        // 格式化倒计时显示
        formattedTime() {
            if (this.isPractice || this.time === null) {
                return '';
            }
            const minutes = Math.floor(this.time / 60);
            const seconds = this.time % 60;
            return `${minutes}分${seconds.toString().padStart(2, '0')}秒`;
        },
        // 确保传给AI的题目是字符串格式
        currentQuestionText() {
            if (typeof this.showQuestion === 'string') {
                return this.showQuestion;
            } else if (Array.isArray(this.showQuestion)) {
                return this.showQuestion.join(' ');
            } else if (this.showQuestion && this.showQuestion.question) {
                return this.showQuestion.question;
            }
            return '当前题目信息不可用';
        },
        // 格式化正向计时显示
        formattedElapsedTime() {
            const hours = Math.floor(this.elapsedTime / 3600);
            const minutes = Math.floor((this.elapsedTime % 3600) / 60);
            const seconds = this.elapsedTime % 60;
            
            if (hours > 0) {
                return `${hours}小时${minutes}分${seconds.toString().padStart(2, '0')}秒`;
            } else if (minutes > 0) {
                return `${minutes}分${seconds.toString().padStart(2, '0')}秒`;
            } else {
                return `${seconds}秒`;
            }
        },
        // 已答题数
        answeredCount() {
            let count = 0;
            // 统计选择题已答数量
            count += this.radio.filter(answer => answer != undefined).length;
            // 统计填空题已答数量（至少填写了一个空）
            if (this.fillAnswer && this.fillAnswer.length > 0) {
                count += this.fillAnswer.filter(answers => 
                    answers && answers.some(answer => answer != null && answer != '')
                ).length;
            }
            // 统计判断题已答数量
            count += this.judgeAnswer.filter(answer => answer != undefined).length;
            return count;
        },
        // 剩余题数
        remainingCount() {
            const total = this.topicCount[0] + this.topicCount[1] + this.topicCount[2];
            return total - this.answeredCount;
        },
        // 标记题数
        markedCount() {
            let count = 0;
            // 统计选择题标记数量
            if (this.topic[1]) {
                count += this.topic[1].filter(item => item.isMark === true).length;
            }
            // 统计填空题标记数量
            if (this.topic[2]) {
                count += this.topic[2].filter(item => item.isMark === true).length;
            }
            // 统计判断题标记数量
            if (this.topic[3]) {
                count += this.topic[3].filter(item => item.isMark === true).length;
            }
            return count;
        },
        // 正确题数（仅练习模式）
        correctCount() {
            if (!this.isPractice) return 0;
            
            let count = 0;
            
            // 统计选择题正确数量
            if (this.topic[1]) {
                this.radio.forEach((answer, index) => {
                    if (answer != undefined && this.topic[1][index]) {
                        if (this.isAnswerCorrect(answer, this.topic[1][index].rightAnswer)) {
                            count++;
                        }
                    }
                });
            }
            
            // 统计填空题正确数量
            if (this.topic[2] && this.fillAnswer) {
                this.fillAnswer.forEach((answers, index) => {
                    if (answers && this.topic[2][index]) {
                        // 检查至少填写了一个空
                        const hasAnswer = answers.some(answer => answer != null && answer != '');
                        if (hasAnswer) {
                            // 检查所有填写的答案是否都在正确答案列表中
                            const allCorrect = answers.every((answer, i) => {
                                if (!answer) return true; // 空答案不计
                                return this.topic[2][index].answer.includes(answer);
                            });
                            if (allCorrect) count++;
                        }
                    }
                });
            }
            
            // 统计判断题正确数量
            if (this.topic[3]) {
                this.judgeAnswer.forEach((answer, index) => {
                    if (answer != undefined && this.topic[3][index]) {
                        const correctAnswer = this.topic[3][index].answer;
                        const isCorrect = (answer == 1 && correctAnswer == 'T') || 
                                        (answer == 2 && correctAnswer == 'F');
                        if (isCorrect) count++;
                    }
                });
            }
            
            return count;
        },
        // 错误题数（仅练习模式）
        incorrectCount() {
            if (!this.isPractice) return 0;
            // 错误题数 = 已答题数 - 正确题数
            return this.answeredCount - this.correctCount;
        },
        // 当前正确率（仅练习模式）
        correctRate() {
            if (!this.isPractice || this.answeredCount === 0) return 0;
            return ((this.correctCount / this.answeredCount) * 100).toFixed(1);
        }
    },
};
</script>

<style lang="less">
.iconfont.icon-time {
    color: CornflowerBlue;
    margin: 0px 6px 0px 20px;
}

.time-warning {
    color: #E6A23C !important;
    animation: pulse 2s infinite;
}

.time-danger {
    color: #F56C6C !important;
    animation: pulse 1s infinite;
}

@keyframes pulse {
    0%, 100% {
        opacity: 1;
    }
    50% {
        opacity: 0.6;
    }
}

.analysis {
    margin-top: 20px;
    font-size: 16px;

    .right {
        color: #2776df;
        font-size: 18px;
        border: 1px solid #2776df;
        padding: 2px 8px;
        border-radius: 4px;
        margin-left: 20px;
    }

    ul li:nth-child(2) {
        margin: 20px 0px;
        font-size: 16px;
    }

    ul li:nth-child(3) {
        padding: 12px;
        background-color: #e8f4ff;
        border-radius: 4px;
        font-size: 16px;
        line-height: 1.8;
    }
}

.analysis span:nth-child(1) {
    font-size: 18px;
}

span.mark {
    display: inline-block !important;  // 改为inline-block确保伪元素显示
    position: absolute;
    top: -4px;
    right: -4px;
    width: 16px;
    height: 16px;
    z-index: 100;  // 最高优先级，确保标记在最上层
    pointer-events: none;  // 不阻挡点击事件
    
    // 使用五角星符号（更显眼）
    &::before {
        content: "★";
        display: block;  // 确保伪元素显示
        font-size: 16px;  // 稍微增大
        color: #FFD700;  // 黄色五角星
        position: absolute;
        top: -2px;
        left: 0;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);  // 增强阴影
        filter: drop-shadow(0 0 2px #FFD700);  // 添加发光效果
    }
}

.border {
    position: relative;
    border: 3px solid #ff90aa !important;
    z-index: 2;  // 当前题目边框层级（低于标记，高于背景）
    
    // 确保border状态下，mark也保持正确样式
    span.mark {
        display: inline-block !important;
        position: absolute;
        top: -4px;
        right: -4px;
        width: 16px;
        height: 16px;
        z-index: 100;  // 最高优先级
        pointer-events: none;
        
        &::before {
            content: "★";
            display: block !important;  // 确保伪元素显示
            font-size: 16px;
            color: #FFD700 !important;  // 黄色五角星
            position: absolute;
            top: -2px;
            left: 0;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
            filter: drop-shadow(0 0 2px #FFD700);
        }
    }
}

.bg {
    background-color: #5188b8 !important;
    z-index: 1;  // 背景层级（最低）
    
    // 确保已答状态下，mark也保持正确样式
    span.mark {
        display: inline-block !important;
        position: absolute;
        top: -4px;
        right: -4px;
        width: 16px;
        height: 16px;
        z-index: 100;  // 最高优先级
        pointer-events: none;
        
        &::before {
            content: "★";
            display: block !important;  // 确保伪元素显示
            font-size: 16px;
            color: #FFD700 !important;  // 黄色五角星
            position: absolute;
            top: -2px;
            left: 0;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
            filter: drop-shadow(0 0 2px #FFD700);
        }
    }
}

.generating {
    color: var(--primary-color, #409EFF);
    font-style: italic;
    display: inline-block;
    
    .wave-text {
        display: inline-block;
        animation: wave 1.5s ease-in-out infinite;
        
        &:nth-child(1) { animation-delay: 0s; }
        &:nth-child(2) { animation-delay: 0.1s; }
        &:nth-child(3) { animation-delay: 0.2s; }
        &:nth-child(4) { animation-delay: 0.3s; }
        &:nth-child(5) { animation-delay: 0.4s; }
        &:nth-child(6) { animation-delay: 0.5s; }
        &:nth-child(7) { animation-delay: 0.6s; }
        &:nth-child(8) { animation-delay: 0.7s; }
        &:nth-child(9) { animation-delay: 0.8s; }
    }
}

@keyframes wave {
    0%, 100% {
        transform: scale(1);
    }
    50% {
        transform: scale(1.3);
    }
}

.analysis {
    .el-tag {
        margin-right: 10px;
        
        &.answer-tag {
            min-width: 50px;
            text-align: center;
        }
    }
    
    li {
        display: flex;
        align-items: center;
        margin: 10px 0;
    }
}

.analysis .el-button--text {
    color: var(--primary-color, #409EFF);
    padding: 0 5px;
    
    &:hover {
        color: #66b1ff;
    }
}

.analysis-header {
    display: flex;
    align-items: center;
    background: linear-gradient(135deg, #e8f4fc 0%, #d6eaf8 100%);
    padding: 8px 12px;
    border-radius: 4px;
    margin-bottom: 10px;
}

.generating {
    display: flex;
    align-items: center;
    padding: 10px 0;
}

.wave-text {
    display: inline-block;
    color: var(--primary-color, #409EFF);
    font-size: 16px;
    font-weight: 500;
    animation: wave 1.5s ease-in-out infinite;
}

.wave-text:nth-child(1) { animation-delay: 0s; }
.wave-text:nth-child(2) { animation-delay: 0.1s; }
.wave-text:nth-child(3) { animation-delay: 0.2s; }
.wave-text:nth-child(4) { animation-delay: 0.3s; }
.wave-text:nth-child(5) { animation-delay: 0.4s; }
.wave-text:nth-child(6) { animation-delay: 0.5s; }
.wave-text:nth-child(7) { animation-delay: 0.6s; }
.wave-text:nth-child(8) { animation-delay: 0.7s; }
.wave-text:nth-child(9) { animation-delay: 0.8s; }

@keyframes wave {
    0%, 100% {
        transform: scale(1);
        opacity: 0.7;
    }
    50% {
        transform: scale(1.3);
        opacity: 1;
    }
}

.analysis-content {
    margin-top: 10px;
    line-height: 1.8;
    color: #333;
}

.formatted-analysis {
    padding: 10px;
    background-color: #f9f9f9;
    border-radius: 4px;
    
    strong {
        color: var(--primary-color, #409EFF);
        font-weight: 600;
    }
    
    .list-item {
        margin: 8px 0;
        padding-left: 10px;
        line-height: 1.8;
        
        .list-number {
            color: var(--primary-color, #409EFF);
            font-weight: bold;
            margin-right: 8px;
        }
        
        .bullet {
            color: var(--primary-color, #409EFF);
            margin-right: 8px;
        }
    }
}

.no-analysis {
    color: #999;
    font-style: italic;
}

.fill .el-input {
    display: inline-flex;
    width: 180px;
    margin-left: 20px;

    .el-input__inner {
        border: 1px solid transparent;
        border-bottom: 1px solid #eee;
        padding-left: 20px;
        font-size: 16px;
    }
}

/* slider过渡效果 */
.slider-fade-enter-active {
    transition: all 0.3s ease;
}

.slider-fade-leave-active {
    transition: all 0.3s cubic-bezier(1, 0.5, 0.8, 1);
}

.slider-fade-enter,
.slider-fade-leave-to {
    transform: translateX(-100px);
    opacity: 0;
}

.operation .end li:nth-child(2) {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    background-color: CornflowerBlue;
    border-radius: 50%;
    width: 50px;
    height: 50px;
    color: #fff;
    transition: all 0.3s ease;
    
    &:hover {
        background-color: #5b8dff;
        transform: scale(1.15) translateY(-3px);
        box-shadow: 0 6px 20px rgba(100, 149, 237, 0.5);
    }
    
    &:active {
        transform: scale(1.05) translateY(-1px);
    }
}

.operation .end li {
    cursor: pointer;
    margin: 0 100px;
    transition: all 0.3s ease;
    
    span {
        font-size: 18px;
    }
    
    // 上一题和下一题的悬浮效果
    &:first-child,
    &:last-child {
        display: flex;
        align-items: center;
        color: var(--primary-color, #409EFF);
        
        &:hover {
            color: #66b1ff;
            transform: translateX(0) translateY(-3px);
            text-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
            
            i {
                transform: scale(1.2);
            }
        }
        
        &:active {
            transform: translateY(-1px);
        }
        
        i {
            transition: transform 0.3s ease;
        }
    }
    
    // 上一题特殊效果
    &:first-child:hover {
        transform: translateX(-5px) translateY(-3px);
    }
    
    // 下一题特殊效果
    &:last-child:hover {
        transform: translateX(5px) translateY(-3px);
    }
}

.operation {
    background-color: #fff;
    border-radius: 4px;
    padding: 10px 0px;
    margin-right: 10px;
}

.item-top {
    font-size: 24px;
    color: #fff;
    
    i {
        color: #fff;
    }
}

.operation .end {
    display: flex;
    justify-content: center;
    align-items: center;
    color: CornflowerBlue;
}

.content .number {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    width: 24px;
    height: 24px;
    background-color: CornflowerBlue;
    border-radius: 4px;
    margin-right: 8px;
    font-size: 16px;
}

.content {
    padding: 0px 20px;
}

.content .topic {
    padding: 20px 0px;
    padding-top: 30px;
    font-size: 18px;
    line-height: 1.8;
}

.right .content {
    background-color: #fff;
    margin: 10px;
    margin-left: 0px;
    height: calc(100vh - 280px);
    overflow-y: auto;
    overflow-x: hidden;
}

.content .el-radio-group label {
    color: #000;
    margin: 8px 0px;
    font-size: 16px;
}

.content .el-radio-group {
    display: flex;
    flex-direction: column;
}

.judge .el-radio-group label {
    color: #000;
    margin: 8px 0px;
    font-size: 16px;
}

.right .title p {
    margin-left: 20px;
}

.flexarea {
    display: flex;
    position: relative;
    flex: 1;
    overflow: hidden;
}

.flexarea .right {
    flex: 1;
    transition: all 0.3s ease;

    &.with-ai-chat {
        margin-right: 10px;
    }
}

.ai-chat-wrapper {
    position: relative;
    height: calc(100vh - 120px);
    margin: 10px 10px 0 0;
    flex-shrink: 0;
    min-width: 300px;
    max-width: 800px;
}

.resize-handle {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 6px;
    cursor: ew-resize;
    background-color: transparent;
    z-index: 10;
    transition: background-color 0.2s ease;

    &:hover {
        background-color: var(--primary-color, #409EFF);
    }

    &::before {
        content: '';
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
        width: 2px;
        height: 40px;
        background-color: #dcdfe6;
        border-radius: 1px;
    }

    &:hover::before {
        background-color: #fff;
    }
}

.ai-float-btn {
    position: fixed;
    right: 30px;
    bottom: 30px;
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    transition: all 0.3s ease;
    z-index: 1000;

    i {
        font-size: 24px;
        margin-bottom: 2px;
        color: #fff !important;
    }

    span {
        font-size: 10px;
    }

    &:hover {
        transform: scale(1.1);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.6);
    }
}

/* 滑入动画 */
.slide-left-enter-active,
.slide-left-leave-active {
    transition: all 0.3s ease;
}

.slide-left-enter,
.slide-left-leave-to {
    transform: translateX(100%);
    opacity: 0;
}

.auto-right {
    margin-left: auto;
    color: #2776df;
    margin-right: 10px;
}

.right .title {
    margin-right: 10px;
    padding: 10px 30px 10px 0;
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    margin-top: 10px;
    background-color: #fff;
    min-height: 50px;
    line-height: 1.6;

    span {
        font-size: 16px;
        word-wrap: break-word;
        word-break: break-word;
        margin-left: auto;
        text-align: right;
        max-width: 60%;
    }
    
    p {
        display: flex;
        align-items: center;
        flex-shrink: 0;
        margin-right: 10px;
    }
}

.clearfix {
    clear: both;
}

// 按钮区域样式
.button-area {
    padding: 10px 15px;
    
    .circle-buttons {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 20px;
        margin-bottom: 15px;
        
        .circle-btn {
            width: 50px;
            height: 50px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            background-color: #fff;
            border: 2px solid #e4e7ed;
            
            i {
                font-size: 22px;
                transition: transform 0.3s ease;
            }
            
            &:hover {
                transform: translateY(-3px) scale(1.05);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
            }
        }
        
        .submit-btn {
            border-color: #67C23A;
            i { color: #67C23A; }
            &:hover {
                background-color: #67C23A;
                border-color: #67C23A;
                i { color: #fff; }
            }
        }
        
        .clear-btn {
            border-color: #E6A23C;
            i { color: #E6A23C; }
            &:hover {
                background-color: #E6A23C;
                border-color: #E6A23C;
                i { color: #fff; }
            }
        }
        
        .back-btn {
            border-color: #909399;
            i { color: #909399; }
            &:hover {
                background-color: #909399;
                border-color: #909399;
                i { color: #fff; }
            }
        }
    }
}

// 圆形图标按钮容器（横向布局）
.action-buttons-circle {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 20px;
    margin: 20px 10px 10px 10px;
    padding: 10px 0;
    
    .circle-btn {
        width: 50px;
        height: 50px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        background-color: #fff;
        border: 2px solid #e4e7ed;
        
        i {
            font-size: 22px;
            transition: transform 0.3s ease;
        }
        
        &:hover {
            transform: translateY(-3px) scale(1.05);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
            
            i {
                transform: scale(1.1);
            }
        }
        
        &:active {
            transform: translateY(-1px) scale(1);
        }
    }
    
    .save-btn {
        border-color: #67C23A;
        
        i {
            color: #67C23A;
        }
        
        &:hover {
            background-color: #67C23A;
            border-color: #67C23A;
            
            i {
                color: #fff;
            }
        }
    }
    
    .clear-btn {
        border-color: #E6A23C;
        
        i {
            color: #E6A23C;
        }
        
        &:hover {
            background-color: #E6A23C;
            border-color: #E6A23C;
            
            i {
                color: #fff;
            }
        }
    }
    
    .back-btn {
        border-color: #909399;
        
        i {
            color: #909399;
        }
        
        &:hover {
            background-color: #909399;
            border-color: #909399;
            
            i {
                color: #fff;
            }
        }
    }
}

.action-buttons {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin: 20px 10px 10px 10px;

    .el-button {
        width: 100%;
        margin: 0 !important;
        font-size: 16px;
        padding: 12px 20px;
        border-radius: 4px;
        transition: all 0.3s ease;
        box-sizing: border-box;
        border-width: 1px;
        border-style: solid;

        &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
        }

        /deep/ i {
            font-size: 16px;
            margin-right: 5px;
        }
    }
}

.l-bottom .final {
    cursor: pointer;
    display: inline-block;
    text-align: center;
    background-color: CornflowerBlue;
    width: 240px;
    margin: 20px 0px 20px 10px;
    border-radius: 4px;
    height: 30px;
    line-height: 30px;
    color: #fff;
    margin-top: 22px;
}

#answer .left .item {
    padding: 0px;
    font-size: 16px;
}

.l-bottom {
    border-radius: 4px;
    background-color: #fff;
}

.l-bottom .item p {
    margin-bottom: 15px;
    margin-top: 10px;
    color: #000;
    margin-left: 10px;
    letter-spacing: 2px;
    font-size: 16px;
    font-weight: 500;
}

.l-bottom .item li {
    flex-shrink: 0;
}

.l-bottom .item {
    display: flex;
    flex-direction: column;
}

.l-bottom .item ul {
    width: 100%;
    margin-bottom: 10px;
    display: flex;
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 8px;
    padding: 0 10px;
}

.l-bottom .item ul li a {
    position: relative;
    justify-content: center;
    display: inline-flex;
    align-items: center;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background-color: #fff;
    border: 1px solid #eee;
    text-align: center;
    color: #000;
    font-size: 15px;

    &.correct {
        background-color: #67C23A !important;
        border-color: #67C23A !important;
        color: #fff !important;
        z-index: 1;  // 正确答案背景层级（最低）
        
        // 确保正确答案状态下，mark也保持正确样式
        span.mark {
            display: inline-block !important;
            position: absolute;
            top: -4px;
            right: -4px;
            width: 16px;
            height: 16px;
            z-index: 100;  // 最高优先级
            pointer-events: none;
            
            &::before {
                content: "★";
                display: block !important;  // 确保伪元素显示
                font-size: 16px;
                color: #FFD700 !important;  // 黄色五角星
                position: absolute;
                top: -2px;
                left: 0;
                text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
                filter: drop-shadow(0 0 2px #FFD700);
            }
        }
    }
    
    // 当题目同时是当前题目和正确答案时，显示红色边框
    &.border.correct {
        border: 3px solid #ff90aa !important;
        box-shadow: 0 0 0 1px #ff90aa;  // 添加外部轮廓增强效果
    }

    &.incorrect {
        background-color: #F56C6C !important;
        border-color: #F56C6C !important;
        color: #fff !important;
        z-index: 1;  // 错误答案背景层级（最低）
        
        // 确保错误答案状态下，mark也保持正确样式
        span.mark {
            display: inline-block !important;
            position: absolute;
            top: -4px;
            right: -4px;
            width: 16px;
            height: 16px;
            z-index: 100;  // 最高优先级
            pointer-events: none;
            
            &::before {
                content: "★";
                display: block !important;  // 确保伪元素显示
                font-size: 16px;
                color: #FFD700 !important;  // 黄色五角星
                position: absolute;
                top: -2px;
                left: 0;
                text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);
                filter: drop-shadow(0 0 2px #FFD700);
            }
        }
    }
    
    // 当题目同时是当前题目和错误答案时，显示红色边框
    &.border.incorrect {
        border: 3px solid #ff90aa !important;
        box-shadow: 0 0 0 1px #ff90aa;  // 添加外部轮廓增强效果
    }
}

.left .l-top {
    display: flex;
    justify-content: space-around;
    padding: 12px 8px;
    border: 1px solid #eee;
    border-radius: 4px;
    margin-bottom: 10px;
    background-color: #fff;
    gap: 4px;  // 添加间距
    
    span {
        font-size: 12px;  // 稍微缩小字体以适应5个元素
        margin-top: 4px;
    }
}


// 正向计时显示样式
.elapsed-time-display {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12px 16px;
    margin: 0 10px 10px 10px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    color: #fff;
    transition: all 0.3s ease;
    
    &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
    }
    
    i {
        font-size: 18px;
        margin-right: 8px;
        animation: pulse 2s ease-in-out infinite;
    }
    
    .time-label {
        font-size: 14px;
        font-weight: 500;
        margin-right: 6px;
        opacity: 0.9;
    }
    
    .time-value {
        font-size: 18px;
        font-weight: bold;
        letter-spacing: 1px;
        font-family: 'Courier New', monospace;
    }
}

@keyframes pulse {
    0%, 100% {
        opacity: 1;
        transform: scale(1);
    }
    50% {
        opacity: 0.7;
        transform: scale(1.1);
    }
}

.left {
    position: relative;
    height: calc(100vh - 90px);
    margin: 10px 10px 0px 10px;
    overflow-y: auto;
    min-width: 200px;
    max-width: 400px;
}

.left-resize-handle {
    position: absolute;
    right: 0;
    top: 0;
    bottom: 0;
    width: 6px;
    cursor: ew-resize;
    background-color: transparent;
    z-index: 10;
    transition: background-color 0.2s ease;

    &:hover {
        background-color: var(--primary-color, #409EFF);
    }

    &::before {
        content: '';
        position: absolute;
        left: 50%;
        top: 50%;
        transform: translate(-50%, -50%);
        width: 2px;
        height: 40px;
        background-color: #dcdfe6;
        border-radius: 1px;
    }

    &:hover::before {
        background-color: #fff;
    }
}

.left .l-top li:nth-child(2) a {
    border: 1px solid #eee;
}


.left .l-top li:nth-child(3) a {
    background-color: #67C23A;  // 正确（绿色）
    border: none;
    color: #fff;
}

.left .l-top li:nth-child(4) a {
    background-color: #F56C6C;  // 错误（红色）
    border: none;
    color: #fff;
}

.left .l-top li:nth-child(5) a {
    position: relative;
    border: 1px solid #eee;
}

.left .l-top li:nth-child(5) a::before {
    content: "★";
    position: absolute;
    top: -4px;
    right: -4px;
    font-size: 16px;  // 增大尺寸
    color: #FFD700;  // 黄色五角星
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.4);  // 增强阴影
    filter: drop-shadow(0 0 2px #FFD700);  // 添加发光效果
    width: auto;
    height: auto;
    z-index: 100;
}

.left .l-top li {
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    flex: 1;  // 让每个li平均分配空间
}

.left .l-top li a {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background-color: #fff;
    border: 1px solid #ff90aa;
}

#answer .top {
    background-color: CornflowerBlue;
}

#answer .item {
    color: #fff;
    display: flex;
    padding: 20px;
    font-size: 20px;
}

#answer .top .item li:nth-child(1) {
    margin-right: 10px;
}

#answer .top .item li:nth-child(3) {
    position: relative;
    margin-left: auto;
}

#answer {
    height: 100vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    &.resizing {
        user-select: none;
        cursor: ew-resize;
    }
}

.icon20 {
    font-size: 20px;
    font-weight: bold;
}

.item .msg {
    padding: 10px 15px;
    border-radius: 4px;
    top: 47px;
    right: -30px;
    color: #6c757d;
    position: absolute;
    border: 1px solid rgba(0, 0, 0, 0.15);
    background-color: #fff;
}

.item .msg p {
    font-size: 16px;
    width: 200px;
    text-align: left;
}
</style>

<style>
/* 全局样式 - MessageBox 确认按钮使用普通样式 */
.custom-confirm-btn {
    background-color: #fff !important;
    border-color: #dcdfe6 !important;
    color: #606266 !important;
}

.custom-confirm-btn:hover {
    background-color: #f5f7fa !important;
    border-color: #c0c4cc !important;
    color: #606266 !important;
}
</style>
