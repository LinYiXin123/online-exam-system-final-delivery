<!--AI问答组件-->
<template>
    <div class="ai-chat-container" :class="{ resizing: isChatResizing }">
        <div class="ai-chat-header">
            <i class="el-icon-chat-dot-round"></i>
            <span>AI 学习助手</span>
            <i class="el-icon-delete clear-btn" @click="resetChat" title="清空聊天记录"></i>
            <i class="el-icon-close close-btn" @click="$emit('close')" v-if="closable"></i>
        </div>
        <div class="ai-chat-messages" ref="messageContainer" :style="{ height: messagesHeight + 'px' }">
            <div v-for="(msg, index) in messages" :key="index" 
                 :class="['message-item', msg.role === 'user' ? 'user-message' : 'ai-message']">
                <div class="message-avatar">
                    <i :class="msg.role === 'user' ? 'el-icon-user' : 'el-icon-cpu'"></i>
                </div>
                <div class="message-content">
                    <!-- 用户消息 -->
                    <div v-if="msg.role === 'user'">
                        <!-- 图片显示 -->
                        <div v-if="msg.images && msg.images.length > 0" class="message-images">
                            <img v-for="(imgUrl, imgIndex) in msg.images" 
                                 :key="imgIndex" 
                                 :src="imgUrl" 
                                 class="message-image"
                                 @click="previewImage(imgUrl)">
                        </div>
                        <!-- 文件显示 -->
                        <div v-if="msg.files && msg.files.length > 0" class="message-files">
                            <div v-for="(file, fileIndex) in msg.files" :key="fileIndex" class="message-file-item" @click="openFile(file.url, file.name)">
                                <i :class="getFileIcon(file.type)" class="file-icon"></i>
                                <div class="file-info">
                                    <div class="file-name">{{ file.name }}</div>
                                    <div class="file-size">{{ file.size }}</div>
                                </div>
                            </div>
                        </div>
                        <!-- 文本内容 -->
                        <div class="message-text" v-if="msg.content">{{ msg.content }}</div>
                    </div>
                    <!-- AI消息 -->
                    <div class="message-text ai-formatted-content" v-else v-html="formatMessage(msg.content)"></div>
                    <div class="message-time">{{ msg.time }}</div>
                </div>
            </div>
            <div v-if="isLoading" class="message-item ai-message">
                <div class="message-avatar">
                    <i class="el-icon-cpu"></i>
                </div>
                <div class="message-content">
                    <div class="message-text loading">
                        <span class="dot"></span>
                        <span class="dot"></span>
                        <span class="dot"></span>
                    </div>
                </div>
            </div>
        </div>
        <!-- 录音状态提示 -->
        <transition name="fade">
            <div v-if="recordingStatus" 
                 class="recording-status" 
                 :class="getStatusClass()"
                 :style="{ top: (messagesHeight + 60 - 50) + 'px' }">
                <i :class="getStatusIcon()"></i>
                {{ recordingStatus }}
            </div>
        </transition>
        <!-- 可拖动的分隔条 -->
        <div class="chat-resize-handle" @mousedown="startChatResize">
            <div class="resize-line"></div>
        </div>
        <div class="ai-chat-input">
            <div class="input-wrapper">
                <!-- 图片预览区域 -->
                <div v-if="uploadedImages.length > 0" 
                     class="image-preview-container" 
                     :style="{ height: calculateImagePreviewHeight(uploadedImages.length) + 'px' }">
                    <div v-for="(img, index) in uploadedImages" :key="index" class="image-preview-item">
                        <img :src="img.url" :alt="img.name" @click="previewImage(img.url)">
                        <div class="image-name">{{ img.name }}</div>
                        <i class="el-icon-circle-close remove-image" @click.stop="removeImage(index)"></i>
                    </div>
                </div>
                <!-- 文件预览区域 -->
                <div v-if="uploadedFiles.length > 0" class="file-preview-container">
                    <div v-for="(file, index) in uploadedFiles" :key="index" class="file-preview-item" @click="openFile(URL.createObjectURL(file.file), file.name)">
                        <i :class="getFileIcon(file.type)" class="file-icon"></i>
                        <div class="file-info">
                            <div class="file-name">{{ file.name }}</div>
                            <div class="file-size">{{ file.size }}</div>
                        </div>
                        <i class="el-icon-close remove-file" @click.stop="removeFile(index)"></i>
                    </div>
                </div>
                <el-input
                    type="textarea"
                    placeholder="输入问题，AI 助手将为你解答...（支持直接 Ctrl+V 粘贴截图）"
                    v-model="inputMessage"
                    @keyup.enter.native="sendMessage"
                    @paste.native="handlePaste"
                    :disabled="isLoading"
                    resize="none"
                    ref="textarea"
                >
                </el-input>
            </div>
            <div class="send-area">
                <!-- 功能按钮组 -->
                <div class="input-tools">
                    <el-tooltip :content="isRecording ? '停止录音' : '语音转文字'" placement="top">
                        <el-button class="tool-btn" :class="{ 'recording': isRecording }" circle @click="voiceInput">
                            <i v-if="!isRecording" class="el-icon-microphone"></i>
                            <span v-else class="recording-dots">
                                <span class="dot"></span>
                                <span class="dot"></span>
                                <span class="dot"></span>
                            </span>
                        </el-button>
                    </el-tooltip>
                    <el-tooltip content="截图" placement="top">
                        <el-button class="tool-btn" icon="el-icon-scissors" circle @click="screenshot"></el-button>
                    </el-tooltip>
                    <el-tooltip content="添加文件" placement="top">
                        <el-button class="tool-btn" icon="el-icon-paperclip" circle @click="addFile"></el-button>
                    </el-tooltip>
                </div>
                <el-button 
                    v-if="!isLoading"
                    type="primary" 
                    icon="el-icon-s-promotion" 
                    @click="sendMessage"
                    :disabled="!inputMessage.trim()"
                    class="send-btn"
                >
                    发送
                </el-button>
                <el-button 
                    v-else
                    type="warning" 
                    icon="el-icon-video-pause" 
                    @click="stopGeneration"
                    class="send-btn"
                >
                    停止生成
                </el-button>
            </div>
        </div>
        <div class="ai-chat-tips">
            <el-tag size="mini" @click="quickQuestion('这道题怎么解？')" style="cursor: pointer; margin: 2px;">
                这道题怎么解？
            </el-tag>
            <el-tag size="mini" @click="quickQuestion('解释一下相关知识点')" style="cursor: pointer; margin: 2px;">
                解释知识点
            </el-tag>
            <el-tag size="mini" @click="quickQuestion('给我一些解题思路')" style="cursor: pointer; margin: 2px;">
                解题思路
            </el-tag>
        </div>
        <!-- 图片预览模态框 -->
        <transition name="fade">
            <div v-if="showImagePreview" class="image-preview-modal" @click="closePreview">
                <div class="preview-close" @click="closePreview">
                    <i class="el-icon-close"></i>
                </div>
                <div class="preview-content" @click.stop>
                    <img :src="previewImageUrl" alt="预览图片">
                </div>
            </div>
        </transition>
    </div>
</template>

<script>
export default {
    name: 'AiChat',
    props: {
        closable: {
            type: Boolean,
            default: false
        },
        currentQuestion: {
            type: String,
            default: ''
        },
        examCode: {
            type: [String, Number],
            default: ''
        },
        subject: {
            type: String,
            default: ''
        }
    },
    data() {
        return {
            messages: [],
            inputMessage: '',
            isLoading: false,
            lastExamCode: '',
            cancelTokenSource: null,  // 用于取消请求
            messagesHeight: 0,  // 消息区域高度，初始化时计算
            isChatResizing: false,  // 是否正在拖动
            isRecording: false,  // 是否正在录音
            recognition: null,  // 语音识别对象
            recordingStatus: '',  // 录音状态文本
            uploadedImages: [],  // 上传的图片列表
            uploadedFiles: [],  // 上传的文件列表
            waitingForScreenshot: false,  // 是否正在等待截图
            baseMessagesHeight: 0,  // 基础消息区域高度（无图片时）
            previewImageUrl: '',  // 预览图片的URL
            showImagePreview: false  // 是否显示图片预览
        };
    },
    watch: {
        examCode(newCode, oldCode) {
            // 当切换试卷时，清空聊天历史
            if (newCode && oldCode && newCode !== oldCode) {
                this.resetChat();
            }
        },
        subject(newSubject, oldSubject) {
            // 当科目改变时，清空聊天历史
            if (newSubject && oldSubject && newSubject !== oldSubject) {
                this.resetChat();
            }
        },
        uploadedImages: {
            handler(newImages) {
                // 当图片数量变化时，调整消息区域高度
                if (this.baseMessagesHeight > 0) {
                    const imagePreviewHeight = this.calculateImagePreviewHeight(newImages.length);
                    const filePreviewHeight = this.calculateFilePreviewHeight(this.uploadedFiles.length);
                    const totalPreviewHeight = imagePreviewHeight + filePreviewHeight;
                    const newHeight = this.baseMessagesHeight - totalPreviewHeight;
                    
                    // 确保对话区不会小于最小高度200px（增加保护高度）
                    this.messagesHeight = Math.max(newHeight, 200);
                    
                    // 滚动到底部
                    this.$nextTick(() => {
                        this.scrollToBottom();
                    });
                }
            },
            deep: true
        },
        uploadedFiles: {
            handler(newFiles) {
                // 当文件数量变化时，调整消息区域高度
                if (this.baseMessagesHeight > 0) {
                    const imagePreviewHeight = this.calculateImagePreviewHeight(this.uploadedImages.length);
                    const filePreviewHeight = this.calculateFilePreviewHeight(newFiles.length);
                    const totalPreviewHeight = imagePreviewHeight + filePreviewHeight;
                    const newHeight = this.baseMessagesHeight - totalPreviewHeight;
                    
                    // 确保对话区不会小于最小高度200px
                    this.messagesHeight = Math.max(newHeight, 200);
                    
                    // 滚动到底部
                    this.$nextTick(() => {
                        this.scrollToBottom();
                    });
                }
            },
            deep: true
        }
    },
    mounted() {
        this.lastExamCode = this.examCode;
        
        // 计算初始消息区域高度
        this.$nextTick(() => {
            const container = this.$el;
            if (container) {
                const containerHeight = container.clientHeight;
                const headerHeight = 60;
                const inputHeight = 180; // 输入区域期望高度
                const handleHeight = 6;
                // 对话区域占大部分空间
                const initialHeight = containerHeight - headerHeight - inputHeight - handleHeight;
                this.messagesHeight = initialHeight;
                this.baseMessagesHeight = initialHeight;  // 保存基础高度
            }
        });
        
        // 添加初始欢迎消息
        let welcomeMsg = '你好！我是 AI 学习助手，有任何学习问题都可以问我哦～';
        if (this.subject) {
            welcomeMsg = `你好！我是 AI 学习助手，当前科目是【${this.subject}】，有任何学习问题都可以问我哦～`;
        }
        this.messages.push({
            role: 'ai',
            content: welcomeMsg,
            time: this.getCurrentTime()
        });
    },
    beforeDestroy() {
        // 组件销毁前取消正在进行的请求
        if (this.cancelTokenSource) {
            this.cancelTokenSource.cancel('组件销毁');
            this.cancelTokenSource = null;
        }
    },
    methods: {
        getCurrentTime() {
            const now = new Date();
            return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`;
        },
        quickQuestion(question) {
            this.inputMessage = question;
            this.sendMessage();
        },
        async sendMessage() {
            if ((!this.inputMessage.trim() && this.uploadedImages.length === 0 && this.uploadedFiles.length === 0) || this.isLoading) return;

            const userMessage = {
                role: 'user',
                content: this.inputMessage,
                images: this.uploadedImages.map(img => img.url),  // 保存图片URL用于显示
                files: this.uploadedFiles.map(f => ({
                    name: f.name,
                    size: f.size,
                    type: f.type,
                    file: f.file,  // 保存原始文件对象
                    url: URL.createObjectURL(f.file)  // 创建本地URL用于打开文件
                })),  // 保存文件信息用于显示
                time: this.getCurrentTime()
            };
            
            this.messages.push(userMessage);
            const question = this.inputMessage;
            const images = [...this.uploadedImages];  // 保存图片数据的副本
            this.inputMessage = '';
            this.uploadedImages = [];  // 清空图片列表
            this.uploadedFiles = [];  // 清空文件列表
            this.isLoading = true;

            // 滚动到底部
            this.$nextTick(() => {
                this.scrollToBottom();
            });

            try {
                // 调用 AI API，传入问题和图片
                const response = await this.callAiApi(question, images);
                
                const aiMessage = {
                    role: 'ai',
                    content: response,
                    time: this.getCurrentTime()
                };
                
                this.messages.push(aiMessage);
            } catch (error) {
                // 如果是用户主动停止生成，不显示错误消息（stopGeneration已经添加了提示）
                if (error.message !== '已停止生成') {
                    const errorMessage = {
                        role: 'ai',
                        content: '抱歉，我暂时无法回答这个问题。请稍后再试。',
                        time: this.getCurrentTime()
                    };
                    this.messages.push(errorMessage);
                }
            } finally {
                this.isLoading = false;
                this.$nextTick(() => {
                    this.scrollToBottom();
                });
            }
        },
        async callAiApi(question, images = []) {
            try {
                // 创建取消令牌
                const CancelToken = this.$axios.CancelToken;
                this.cancelTokenSource = CancelToken.source();

                // 准备请求数据
                const requestData = {
                    question: question,
                    context: this.currentQuestion,
                    subject: this.subject  // 传递科目信息
                };
                
                // 如果有图片，添加图片数据
                if (images && images.length > 0) {
                    requestData.images = images.map(img => ({
                        url: img.url,  // base64数据
                        name: img.name
                    }));
                }

                // 调用真实的后端 AI 接口
                const response = await this.$axios.post('/api/ai/chat', requestData, {
                    cancelToken: this.cancelTokenSource.token
                });

                if (response.data.code === 200) {
                    return response.data.data.answer;
                } else {
                    throw new Error(response.data.message || 'AI服务响应错误');
                }
            } catch (error) {
                // 如果是用户主动取消，不显示错误
                if (this.$axios.isCancel(error)) {
                    throw new Error('已停止生成');
                }
                
                console.error('AI API调用失败:', error);
                // 如果API调用失败，返回友好的错误提示
                if (error.response) {
                    throw new Error('AI服务暂时不可用，请稍后再试');
                } else if (error.request) {
                    throw new Error('网络连接失败，请检查网络设置');
                } else {
                    throw new Error(error.message || '未知错误');
                }
            }
        },
        scrollToBottom() {
            const container = this.$refs.messageContainer;
            if (container) {
                container.scrollTop = container.scrollHeight;
            }
        },
        formatMessage(text) {
            if (!text) {
                return '';
            }
            
            // 格式化Markdown内容
            let formatted = text
                // 处理加粗 **text**
                .replace(/\*\*(.*?)\*\*/g, '<strong class="bold-text">$1</strong>')
                // 处理斜体 *text*
                .replace(/(?<!\*)\*(?!\*)([^*]+)\*(?!\*)/g, '<em>$1</em>')
                // 处理代码块 ```code```
                .replace(/```([\s\S]*?)```/g, '<pre class="code-block"><code>$1</code></pre>')
                // 处理行内代码 `code`
                .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
                // 处理标题 ### Title
                .replace(/^###\s+(.+)$/gm, '<h3 class="heading-3">$1</h3>')
                .replace(/^##\s+(.+)$/gm, '<h2 class="heading-2">$1</h2>')
                .replace(/^#\s+(.+)$/gm, '<h1 class="heading-1">$1</h1>')
                // 处理有序列表 1. 2. 3.
                .replace(/^(\d+)\.\s+(.+)$/gm, '<div class="list-item numbered"><span class="list-number">$1.</span><span class="list-content">$2</span></div>')
                // 处理无序列表 - item 或 * item
                .replace(/^[-*]\s+(.+)$/gm, '<div class="list-item bulleted"><span class="bullet">•</span><span class="list-content">$1</span></div>')
                // 处理链接 [text](url)
                .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" class="link">$1</a>')
                // 处理换行
                .replace(/\n\n/g, '<br><br>')
                .replace(/\n/g, '<br>')
                .trim();
            
            return formatted;
        },
        stopGeneration() {
            // 取消正在进行的请求
            if (this.cancelTokenSource) {
                this.cancelTokenSource.cancel('用户停止生成');
                this.cancelTokenSource = null;
            }
            // 立即结束加载状态
            this.isLoading = false;
            
            // 添加停止提示消息
            const stopMessage = {
                role: 'ai',
                content: '已停止生成回答。',
                time: this.getCurrentTime()
            };
            this.messages.push(stopMessage);
            
            this.$nextTick(() => {
                this.scrollToBottom();
            });
        },
        resetChat() {
            // 取消正在进行的请求
            if (this.cancelTokenSource) {
                this.cancelTokenSource.cancel('重置聊天');
                this.cancelTokenSource = null;
            }
            
            // 清空聊天历史，重置为初始状态
            let welcomeMsg = '你好！我是 AI 学习助手，有任何学习问题都可以问我哦～';
            if (this.subject) {
                welcomeMsg = `你好！我是 AI 学习助手，当前科目是【${this.subject}】，有任何学习问题都可以问我哦～`;
            }
            this.messages = [
                {
                    role: 'ai',
                    content: welcomeMsg,
                    time: this.getCurrentTime()
                }
            ];
            this.inputMessage = '';
            this.uploadedImages = [];  // 清空图片列表
            this.uploadedFiles = [];  // 清空文件列表
            this.isLoading = false;
        },
        startChatResize(e) {
            // 开始拖动
            this.isChatResizing = true;
            document.addEventListener('mousemove', this.handleChatResize);
            document.addEventListener('mouseup', this.stopChatResize);
            e.preventDefault();
        },
        handleChatResize(e) {
            // 处理拖动
            if (!this.isChatResizing) return;
            
            const container = this.$el;
            const containerRect = container.getBoundingClientRect();
            const headerHeight = 60; // 头部高度
            const inputMinHeight = 120; // 输入区域最小高度
            const handleHeight = 6; // 分隔条高度
            
            const newMessagesHeight = e.clientY - containerRect.top - headerHeight;
            const minMessagesHeight = 200; // 对话区域最小高度（保证快捷问题可见）
            const maxMessagesHeight = containerRect.height - headerHeight - inputMinHeight - handleHeight;
            
            // 限制高度范围：200px - maxHeight
            if (newMessagesHeight >= minMessagesHeight && newMessagesHeight <= maxMessagesHeight) {
                this.messagesHeight = newMessagesHeight;
            }
        },
        stopChatResize() {
            // 停止拖动
            this.isChatResizing = false;
            document.removeEventListener('mousemove', this.handleChatResize);
            document.removeEventListener('mouseup', this.stopChatResize);
            
            // 更新基础高度（考虑当前图片和文件数量）
            const imagePreviewHeight = this.calculateImagePreviewHeight(this.uploadedImages.length);
            const filePreviewHeight = this.calculateFilePreviewHeight(this.uploadedFiles.length);
            this.baseMessagesHeight = this.messagesHeight + imagePreviewHeight + filePreviewHeight;
        },
        getStatusIcon() {
            // 根据状态文本返回对应的图标
            if (this.recordingStatus.includes('正在录音')) {
                return 'el-icon-info';  // 蓝色信息图标
            } else if (this.recordingStatus.includes('完成') || 
                       this.recordingStatus.includes('已添加')) {
                return 'el-icon-success';  // 绿色成功图标
            } else if (this.recordingStatus.includes('失败') || 
                       this.recordingStatus.includes('最多支持')) {
                return 'el-icon-warning';  // 红色警告图标
            } else {
                return 'el-icon-info';  // 默认信息图标
            }
        },
        getStatusClass() {
            // 根据状态返回对应的样式类
            if (this.recordingStatus.includes('失败') || 
                this.recordingStatus.includes('最多支持')) {
                return 'error-status';
            }
            return '';
        },
        calculateImagePreviewHeight(imageCount) {
            // 计算图片预览区域需要的高度
            if (imageCount === 0) return 0;
            
            // 每行最多显示更多图片，减少行数
            const imagesPerRow = 10;  // 从8改为10，减少换行
            const rows = Math.ceil(imageCount / imagesPerRow);
            
            // 计算高度：上下padding 16px + 每行高度68px（60px图片 + 8px间距）
            const calculatedHeight = 16 + rows * 68;
            
            // 最大高度限制为136px（最多2行，避免遮挡快捷问题）
            return Math.min(calculatedHeight, 136);
        },
        calculateFilePreviewHeight(fileCount) {
            // 计算文件预览区域需要的高度
            if (fileCount === 0) return 0;
            
            // 每个文件卡片高度：56px（48px内容 + 8px间距）
            // 上下padding: 24px
            const calculatedHeight = 24 + fileCount * 56;
            
            // 最大高度限制为200px（最多3个文件完整显示）
            return Math.min(calculatedHeight, 200);
        },
        voiceInput() {
            // 如果正在录音，停止录音
            if (this.isRecording) {
                if (this.recognition) {
                    this.recognition.stop();
                    this.isRecording = false;
                    this.recordingStatus = '';
                }
                return;
            }
            
            // 语音转文字功能
            if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
                this.$message({
                    type: 'warning',
                    message: '您的浏览器不支持语音识别功能，请使用Chrome浏览器'
                });
                return;
            }
            
            const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
            this.recognition = new SpeechRecognition();
            this.recognition.lang = 'zh-CN';
            this.recognition.continuous = true;  // 持续录音
            this.recognition.interimResults = true;  // 启用实时识别结果
            
            let lastTranscript = '';  // 记录上次的文本
            
            this.recognition.onstart = () => {
                this.isRecording = true;
                this.recordingStatus = '正在录音，请说话...';
                lastTranscript = this.inputMessage;  // 保存开始录音时的文本
            };
            
            this.recognition.onresult = (event) => {
                let interimTranscript = '';
                let finalTranscript = '';
                
                // 遍历识别结果
                for (let i = event.resultIndex; i < event.results.length; i++) {
                    const transcript = event.results[i][0].transcript;
                    if (event.results[i].isFinal) {
                        // 最终结果
                        finalTranscript += transcript;
                    } else {
                        // 临时结果（实时显示）
                        interimTranscript += transcript;
                    }
                }
                
                // 实时更新输入框：基础文本 + 最终文本 + 临时文本
                this.inputMessage = lastTranscript + finalTranscript + interimTranscript;
                
                // 如果有最终结果，更新基础文本
                if (finalTranscript) {
                    lastTranscript = lastTranscript + finalTranscript;
                }
            };
            
            this.recognition.onerror = (event) => {
                this.isRecording = false;
                this.recordingStatus = '语音识别失败: ' + event.error;
                setTimeout(() => {
                    this.recordingStatus = '';
                }, 3000);
            };
            
            this.recognition.onend = () => {
                this.isRecording = false;
                if (this.inputMessage && this.inputMessage !== lastTranscript) {
                    this.recordingStatus = '语音识别完成';
                    setTimeout(() => {
                        this.recordingStatus = '';
                    }, 2000);
                } else {
                    this.recordingStatus = '';
                }
            };
            
            this.recognition.start();
        },
        screenshot() {
            // 温馨提示：浏览器无法自动触发系统快捷键，引导用户操作
            this.recordingStatus = '💡 快捷操作：Win+Shift+S 截图后，在输入框 Ctrl+V 粘贴';
            
            // 5秒后自动隐藏提示
            setTimeout(() => {
                this.recordingStatus = '';
            }, 5000);
        },
        handlePaste(e) {
            // 处理粘贴事件
            if (!e.clipboardData || !e.clipboardData.items) {
                return;  // 如果没有clipboardData，正常粘贴
            }
            
            // 检查图片数量限制
            if (this.uploadedImages.length >= 10) {
                this.recordingStatus = '最多支持10张图片';
                setTimeout(() => {
                    this.recordingStatus = '';
                }, 3000);
                return;
            }
            
            const items = e.clipboardData.items;
            let hasImage = false;
            
            for (let i = 0; i < items.length; i++) {
                if (items[i].type.indexOf('image') !== -1) {
                    hasImage = true;
                    e.preventDefault();  // 只在有图片时阻止默认粘贴行为
                    
                    const blob = items[i].getAsFile();
                    if (!blob) continue;
                    
                    const reader = new FileReader();
                    reader.onload = (event) => {
                        // 再次检查数量限制（防止快速粘贴）
                        if (this.uploadedImages.length >= 10) {
                            this.recordingStatus = '最多支持10张图片';
                            setTimeout(() => {
                                this.recordingStatus = '';
                            }, 3000);
                            return;
                        }
                        
                        // 添加到图片列表
                        this.uploadedImages.push({
                            url: event.target.result,
                            name: blob.name || `截图_${Date.now()}.png`,
                            file: blob
                        });
                        
                        this.waitingForScreenshot = false;
                        this.recordingStatus = '图片已添加';
                        setTimeout(() => {
                            this.recordingStatus = '';
                        }, 2000);
                    };
                    reader.readAsDataURL(blob);
                    break;  // 只处理第一张图片
                }
            }
        },
        removeImage(index) {
            // 删除图片
            this.uploadedImages.splice(index, 1);
        },
        previewImage(url) {
            // 预览图片
            this.previewImageUrl = url;
            this.showImagePreview = true;
        },
        closePreview() {
            // 关闭预览
            this.showImagePreview = false;
            this.previewImageUrl = '';
        },
        addFile() {
            // 添加文件功能
            const input = document.createElement('input');
            input.type = 'file';
            input.accept = '.txt,.pdf,.doc,.docx,.jpg,.jpeg,.png';
            input.multiple = true;
            
            input.onchange = (e) => {
                const files = e.target.files;
                if (files.length > 0) {
                    // 将文件添加到文件列表
                    Array.from(files).forEach(file => {
                        this.uploadedFiles.push({
                            name: file.name,
                            size: this.formatFileSize(file.size),
                            type: file.type || this.getFileType(file.name),
                            file: file
                        });
                    });
                    
                    this.recordingStatus = `已添加${files.length}个文件`;
                    setTimeout(() => {
                        this.recordingStatus = '';
                    }, 2000);
                }
            };
            
            input.click();
        },
        removeFile(index) {
            // 删除文件
            this.uploadedFiles.splice(index, 1);
        },
        formatFileSize(bytes) {
            // 格式化文件大小
            if (bytes === 0) return '0 B';
            const k = 1024;
            const sizes = ['B', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
        },
        getFileType(filename) {
            // 根据文件名获取文件类型
            const ext = filename.split('.').pop().toLowerCase();
            const types = {
                'pdf': 'application/pdf',
                'doc': 'application/msword',
                'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                'txt': 'text/plain',
                'jpg': 'image/jpeg',
                'jpeg': 'image/jpeg',
                'png': 'image/png'
            };
            return types[ext] || 'application/octet-stream';
        },
        getFileIcon(type) {
            // 根据文件类型返回图标
            if (type.includes('pdf')) return 'el-icon-document';
            if (type.includes('word') || type.includes('doc')) return 'el-icon-document';
            if (type.includes('text')) return 'el-icon-tickets';
            if (type.includes('image')) return 'el-icon-picture';
            return 'el-icon-document';
        },
        openFile(url, filename) {
            // 方案1：直接在新窗口打开（浏览器会根据文件类型调用默认应用）
            // 对于Office文档，Windows会尝试用WPS等默认应用打开
            const newWindow = window.open(url, '_blank');
            
            // 如果弹窗被阻止，使用方案2：创建临时链接
            if (!newWindow) {
                const a = document.createElement('a');
                a.href = url;
                a.target = '_blank';
                // 不设置download属性，让浏览器/系统决定如何处理
                // 对于Office文档，系统会提示用户选择打开方式（包括WPS）
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
            }
        }
    },
    beforeDestroy() {
        // 清理拖动事件监听器
        document.removeEventListener('mousemove', this.handleChatResize);
        document.removeEventListener('mouseup', this.stopChatResize);
        
        // 清理语音识别
        if (this.recognition) {
            this.recognition.stop();
            this.recognition = null;
        }
    }
};
</script>

<style lang="less" scoped>
.ai-chat-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    background-color: #f5f7fa;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    
    &.resizing {
        user-select: none;
        cursor: ns-resize;
    }
}

.ai-chat-header {
    display: flex;
    align-items: center;
    padding: 15px 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    font-size: 18px;
    font-weight: bold;

    i:first-child {
        font-size: 22px;
        margin-right: 10px;
        color: #fff !important;
    }

    .clear-btn {
        margin-left: auto;
        cursor: pointer;
        font-size: 16px;
        margin-right: 10px;
        color: #fff !important;
        
        &:hover {
            opacity: 0.8;
        }
    }

    .close-btn {
        cursor: pointer;
        font-size: 18px;
        color: #fff !important;
        
        &:hover {
            opacity: 0.8;
        }
    }
}

.ai-chat-messages {
    overflow-y: auto;
    padding: 20px;
    background-color: #fff;
    flex-shrink: 0;

    &::-webkit-scrollbar {
        width: 10px;
        background-color: #f5f7fa;
    }

    &::-webkit-scrollbar-thumb {
        background-color: #909399;
        border-radius: 5px;
        
        &:hover {
            background-color: #606266;
        }
    }
    
    &::-webkit-scrollbar-track {
        background-color: #f5f7fa;
        border-radius: 5px;
    }
}

.recording-status {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    background-color: #fff;
    padding: 12px 24px;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
    z-index: 1000;
    font-size: 14px;
    color: #606266;
    display: flex;
    align-items: center;
    gap: 8px;
    white-space: nowrap;
    
    i {
        font-size: 16px;
        
        &.el-icon-info {
            color: var(--primary-color, #409EFF);
        }
        
        &.el-icon-success {
            color: #67C23A;
        }
        
        &.el-icon-error {
            color: #F56C6C;
        }
        
        &.el-icon-warning {
            color: #E6A23C;
        }
    }
    
    &.error-status {
        background-color: #FEF0F0;
        border: 1px solid #FBC4C4;
        color: #F56C6C;
        
        i.el-icon-warning {
            color: #F56C6C;
        }
    }
}

.fade-enter-active, .fade-leave-active {
    transition: opacity 0.3s;
}

.fade-enter, .fade-leave-to {
    opacity: 0;
}

.chat-resize-handle {
    height: 6px;
    background-color: #e4e7ed;
    cursor: ns-resize;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background-color 0.2s;
    flex-shrink: 0;

    &:hover {
        background-color: var(--primary-color, #409EFF);
    }

    .resize-line {
        width: 40px;
        height: 3px;
        background-color: #909399;
        border-radius: 2px;
    }

    &:hover .resize-line {
        background-color: #fff;
    }
}

.message-item {
    display: flex;
    margin-bottom: 20px;
    animation: fadeIn 0.3s ease-in;

    &.user-message {
        flex-direction: row-reverse;

        .message-avatar {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            margin-left: 10px;
            margin-right: 0;
            
            i {
                color: #fff !important;
            }
        }

        .message-content {
            align-items: flex-end;
        }

        .message-text {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff !important;
            
            * {
                color: #fff !important;
            }
        }
        
        .message-images {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-bottom: 8px;
            
            .message-image {
                max-width: 200px;
                max-height: 200px;
                border-radius: 8px;
                object-fit: contain;
                cursor: pointer;
                transition: all 0.2s;
                border: 2px solid transparent;
                
                &:hover {
                    transform: scale(1.05);
                    border-color: rgba(64, 158, 255, 0.5);
                    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
                }
            }
        }
        
        .message-files {
            display: flex;
            flex-direction: column;
            gap: 8px;
            margin-bottom: 8px;
            
            .message-file-item {
                display: flex;
                align-items: center;
                padding: 10px 14px;
                background-color: rgba(255, 255, 255, 0.9);
                border: 1px solid rgba(64, 158, 255, 0.2);
                border-radius: 8px;
                cursor: pointer;
                transition: all 0.2s;
                max-width: 300px;
                
                &:hover {
                    background-color: rgba(64, 158, 255, 0.05);
                    border-color: rgba(64, 158, 255, 0.4);
                    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
                    transform: translateY(-2px);
                }
                
                .file-icon {
                    font-size: 28px;
                    color: var(--primary-color, #409eff);
                    margin-right: 12px;
                    flex-shrink: 0;
                }
                
                .file-info {
                    flex: 1;
                    min-width: 0;
                    
                    .file-name {
                        font-size: 13px;
                        color: #303133;
                        font-weight: 500;
                        white-space: nowrap;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        margin-bottom: 3px;
                    }
                    
                    .file-size {
                        font-size: 11px;
                        color: #909399;
                    }
                }
            }
        }
    }

    &.ai-message {
        .message-avatar {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            
            i {
                color: #fff !important;
            }
        }

        .message-text {
            background-color: #f0f2f5;
            color: #333;
        }
    }
}

.message-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    flex-shrink: 0;
    margin-right: 10px;
}

.message-content {
    display: flex;
    flex-direction: column;
    max-width: 70%;
}

.message-text {
    padding: 12px 16px;
    border-radius: 8px;
    line-height: 1.8;
    word-wrap: break-word;
    white-space: pre-wrap;
    font-size: 15px;

    &.loading {
        display: flex;
        align-items: center;
        padding: 15px 20px;

        .dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background-color: #999;
            margin: 0 3px;
            animation: bounce 1.4s infinite ease-in-out both;

            &:nth-child(1) {
                animation-delay: -0.32s;
            }

            &:nth-child(2) {
                animation-delay: -0.16s;
            }
        }
    }
}

.message-time {
    font-size: 13px;
    color: #999;
    margin-top: 5px;
}

.ai-formatted-content {
    line-height: 1.8;
    
    .bold-text {
        color: var(--primary-color, #409EFF);
        font-weight: 600;
    }
    
    .heading-1 {
        font-size: 20px;
        font-weight: bold;
        color: #303133;
        margin: 15px 0 10px 0;
        padding-bottom: 5px;
        border-bottom: 2px solid var(--primary-color, #409EFF);
    }
    
    .heading-2 {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
        margin: 12px 0 8px 0;
    }
    
    .heading-3 {
        font-size: 16px;
        font-weight: bold;
        color: #606266;
        margin: 10px 0 6px 0;
    }
    
    .list-item {
        margin: 8px 0;
        padding-left: 10px;
        display: flex;
        align-items: flex-start;
        
        &.numbered {
            .list-number {
                color: var(--primary-color, #409EFF);
                font-weight: bold;
                margin-right: 8px;
                min-width: 30px;
            }
        }
        
        &.bulleted {
            .bullet {
                color: var(--primary-color, #409EFF);
                margin-right: 8px;
                font-size: 16px;
            }
        }
        
        .list-content {
            flex: 1;
            line-height: 1.8;
        }
    }
    
    .code-block {
        background-color: #282c34;
        color: #abb2bf;
        padding: 12px;
        border-radius: 6px;
        margin: 10px 0;
        overflow-x: auto;
        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
        font-size: 14px;
        line-height: 1.6;
        
        code {
            background: none;
            padding: 0;
            color: inherit;
        }
    }
    
    .inline-code {
        background-color: #f5f7fa;
        color: #e96900;
        padding: 2px 6px;
        border-radius: 3px;
        font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
        font-size: 14px;
    }
    
    .link {
        color: var(--primary-color, #409EFF);
        text-decoration: none;
        border-bottom: 1px solid transparent;
        transition: all 0.3s;
        
        &:hover {
            border-bottom-color: var(--primary-color, #409EFF);
        }
    }
}

.ai-chat-input {
    display: flex;
    padding: 15px;
    background-color: #fff;
    gap: 10px;
    flex: 1;
    min-height: 0;
    align-items: stretch;
    
    .input-wrapper {
        flex: 1;
        display: flex;
        flex-direction: column;
        min-height: 0;
        gap: 10px;
    }
    
    .image-preview-container {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        padding: 8px;
        background-color: transparent;
        flex-shrink: 0;  // 不压缩
        max-height: 136px;  // 最大高度限制（最多2行图片）
        overflow-y: auto;  // 超出滚动
        
        // 滚动条样式
        &::-webkit-scrollbar {
            width: 6px;
        }
        
        &::-webkit-scrollbar-thumb {
            background-color: #dcdfe6;
            border-radius: 3px;
            
            &:hover {
                background-color: #c0c4cc;
            }
        }
        
        &::-webkit-scrollbar-track {
            background-color: transparent;
        }
    }
    
    .image-preview-item {
        position: relative;
        width: 60px;
        height: 60px;
        border-radius: 8px;
        overflow: visible;
        border: 1px solid #e4e7ed;
        background-color: #f5f7fa;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        img {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
            border-radius: 6px;
            cursor: pointer;
            transition: opacity 0.2s;
            
            &:hover {
                opacity: 0.8;
            }
        }
        
        .image-name {
            display: none;
        }
        
        .remove-image {
            position: absolute;
            top: -6px;
            right: -6px;
            font-size: 18px;
            color: #fff;
            background-color: #909399;
            border-radius: 50%;
            cursor: pointer;
            transition: all 0.2s;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            
            &:hover {
                background-color: #F56C6C;
                transform: scale(1.1);
            }
        }
    }
    
    // 文件预览容器
    .file-preview-container {
        padding: 12px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        border-bottom: 1px solid #ebeef5;
        background-color: #f9fafb;
        max-height: 200px;
        overflow-y: auto;
        flex-shrink: 0;
        
        &::-webkit-scrollbar {
            width: 6px;
        }
        
        &::-webkit-scrollbar-thumb {
            background-color: #dcdfe6;
            border-radius: 3px;
            
            &:hover {
                background-color: #c0c4cc;
            }
        }
        
        .file-preview-item {
            display: flex;
            align-items: center;
            padding: 8px 12px;
            background-color: #fff;
            border: 1px solid #e4e7ed;
            border-radius: 8px;
            transition: all 0.2s;
            
            &:hover {
                border-color: var(--primary-color, #409eff);
                box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
                
                .remove-file {
                    opacity: 1;
                }
            }
            
            .file-icon {
                font-size: 32px;
                color: var(--primary-color, #409eff);
                margin-right: 12px;
                flex-shrink: 0;
            }
            
            .file-info {
                flex: 1;
                min-width: 0;
                
                .file-name {
                    font-size: 14px;
                    color: #303133;
                    font-weight: 500;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    margin-bottom: 4px;
                }
                
                .file-size {
                    font-size: 12px;
                    color: #909399;
                }
            }
            
            .remove-file {
                font-size: 16px;
                color: #909399;
                cursor: pointer;
                opacity: 0;
                transition: all 0.2s;
                padding: 4px;
                margin-left: 8px;
                flex-shrink: 0;
                
                &:hover {
                    color: #f56c6c;
                    transform: scale(1.2);
                }
            }
        }
    }

    /deep/ .el-textarea {
        height: 100%;
        display: flex;
        flex-direction: column;
    }

    /deep/ .el-textarea__inner {
        font-size: 15px;
        height: 100% !important;
        resize: none;
        flex: 1;
    }
    
    .send-area {
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        gap: 8px;
    }
    
    .input-tools {
        display: flex;
        gap: 5px;
        justify-content: center;
        
        .tool-btn {
            width: 28px;
            height: 28px;
            min-width: 28px;
            padding: 0;
            border: 1px solid #dcdfe6;
            background-color: #fff;
            color: #606266;
            border-radius: 50%;
            display: inline-flex;
            justify-content: center;
            align-items: center;
            
            &:hover {
                color: var(--primary-color, #409EFF);
                border-color: #c6e2ff;
                background-color: #ecf5ff;
            }
            
            &.recording {
                background-color: var(--primary-color, #409EFF);
                border-color: var(--primary-color, #409EFF);
                color: #fff;
                
                &:hover {
                    background-color: #66b1ff;
                    border-color: #66b1ff;
                }
            }
            
            /deep/ i {
                font-size: 14px;
            }
            
            .recording-dots {
                display: flex;
                gap: 3px;
                align-items: center;
                justify-content: center;
                
                .dot {
                    width: 4px;
                    height: 4px;
                    border-radius: 50%;
                    background-color: #fff;
                    animation: recording-pulse 1.2s ease-in-out infinite;
                    
                    &:nth-child(1) {
                        animation-delay: 0s;
                    }
                    
                    &:nth-child(2) {
                        animation-delay: 0.4s;
                    }
                    
                    &:nth-child(3) {
                        animation-delay: 0.8s;
                    }
                }
            }
        }
    }
    
    @keyframes recording-pulse {
        0%, 100% {
            transform: scale(1);
            opacity: 0.6;
        }
        50% {
            transform: scale(1.8);
            opacity: 1;
        }
    }

    .send-btn {
        font-size: 15px;
        padding: 12px 20px;
        min-width: 100px;
        
        &.el-button--warning {
            background: #E6A23C;
            border-color: #E6A23C;
            
            &:hover {
                background: #EBB563;
                border-color: #EBB563;
            }
        }
    }
}

.ai-chat-tips {
    padding: 10px 15px;
    background-color: #fff;
    border-top: 1px solid #e4e7ed;
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    flex-shrink: 0;

    .el-tag {
        font-size: 13px;
        padding: 6px 10px;
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes bounce {
    0%, 80%, 100% {
        transform: scale(0);
    }
    40% {
        transform: scale(1);
    }
}

// 图片预览模态框
.image-preview-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.8);
    z-index: 9999;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: zoom-out;
    
    .preview-close {
        position: absolute;
        top: 20px;
        right: 20px;
        width: 40px;
        height: 40px;
        background-color: rgba(255, 255, 255, 0.2);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s;
        z-index: 10000;
        
        &:hover {
            background-color: rgba(255, 255, 255, 0.3);
            transform: scale(1.1);
        }
        
        i {
            font-size: 24px;
            color: #fff;
        }
    }
    
    .preview-content {
        max-width: 90%;
        max-height: 90%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: default;
        
        img {
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.5);
        }
    }
}
</style>
