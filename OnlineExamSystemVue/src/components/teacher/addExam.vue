<!-- 添加考试 -->
<template>
    <section class="add">
        <div class="form-header" style="background: linear-gradient(135deg, #e8f0ff 0%, #dbeafe 100%); padding: 16px 20px; border-bottom: 1px solid #bfdbfe; border-radius: 8px 8px 0 0;">
            <h3 style="color: #1e40af; margin: 0; font-weight: 600; font-size: 18px;">添加考试</h3>
        </div>
        <el-form ref="form" :model="form" label-width="80px" style="padding: 20px;">
            <el-form-item label="科目名称">
                <el-select v-model="form.source" filterable allow-create placeholder="请选择或输入科目名称" style="width: 100%;" @change="onPaperChange">
                    <el-option v-for="item in paperOptions" :key="item.source" :label="item.source" :value="item.source" :data="item"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="介绍">
                <el-input v-model="form.description" placeholder="请输入或选择试卷后自动填充"></el-input>
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
                <el-date-picker
                    placeholder="选择考试日期"
                    v-model="form.examDate"
                    style="width: 100%"
                    value-format="yyyy-MM-dd"
                ></el-date-picker>
            </el-form-item>
            <el-form-item label="开始时间">
                <el-time-picker
                    v-model="form.startTime"
                    placeholder="选择开始时间"
                    style="width: 100%"
                    value-format="HH:mm:ss"
                    format="HH:mm"
                    @change="calculateEndTime">
                </el-time-picker>
            </el-form-item>
            <el-form-item label="结束时间">
                <el-time-picker
                    v-model="form.endTime"
                    placeholder="选择结束时间"
                    style="width: 100%"
                    value-format="HH:mm:ss"
                    format="HH:mm"
                    @change="calculateDuration">
                </el-time-picker>
            </el-form-item>
            <el-form-item label="持续时间（分钟）">
                <el-input 
                    v-model="form.totalTime" 
                    type="number" 
                    oninput="if(value<=0)value=''" 
                    :min="1"
                    @input="calculateEndTimeFromDuration"
                    placeholder="自动计算或手动输入">
                </el-input>
                <div style="color: #909399; font-size: 12px; margin-top: 4px;">
                    提示：修改开始时间、结束时间或持续时间，其他时间会自动计算
                </div>
            </el-form-item>
            <el-form-item label="考试类型">
                <el-select v-model="form.type" placeholder="请选择考试类型" style="width: 100%;">
                    <el-option label="日常练习" value="日常练习"></el-option>
                    <el-option label="期中考试" value="期中考试"></el-option>
                    <el-option label="期末考试" value="期末考试"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="考生提示">
                <el-input type="textarea" v-model="form.tips"></el-input>
            </el-form-item>
            <el-form-item class="button-group">
                <el-button type="primary" @click="onSubmit()" size="medium">立即创建</el-button>
                <el-button type="danger" @click="cancel()" size="medium">取消</el-button>
            </el-form-item>
        </el-form>
    </section>
</template>

<script>
export default {
    data() {
        return {
            form: {
                //表单数据初始化
                source: null,
                description: null,
                institute: null,
                major: null,
                grade: null,
                examDate: null,
                startTime: null,
                endTime: null,
                totalTime: null,
                totalScore: null,
                type: null,
                tips: null,
                paperId: null,
            },
            instituteOptions: [],
            majorOptions: [],
            gradeOptions: [],
            paperOptions: [],
        };
    },
    created() {
        this.loadOptions();
    },
    methods: {
        loadOptions() {
            this.$axios('/api/examOptions').then(res => {
                if (res.data.code === 200) {
                    this.instituteOptions = res.data.data.institutes || [];
                    this.majorOptions = res.data.data.majors || [];
                    this.gradeOptions = res.data.data.grades || [];
                }
            });
            // 加载已存在的试卷列表
            this.$axios('/api/exams').then(res => {
                if (res.data.code === 200) {
                    // 按科目名称去重
                    const data = res.data.data || [];
                    const seen = new Set();
                    this.paperOptions = data.filter(item => {
                        const source = (item.source || '').trim();
                        if (seen.has(source)) {
                            return false;
                        }
                        seen.add(source);
                        return true;
                    });
                }
            }).catch(error => {
                console.error('加载试卷列表失败:', error);
            });
        },
        onPaperChange(selectedSource) {
            // 当选择试卷时，自动填充相关信息
            const selectedPaper = this.paperOptions.find(paper => paper.source === selectedSource);
            if (selectedPaper) {
                this.form.description = selectedPaper.description;
                this.form.institute = selectedPaper.institute;
                this.form.major = selectedPaper.major;
                this.form.grade = selectedPaper.grade;
                this.form.type = selectedPaper.type;
                this.form.tips = selectedPaper.tips;
            }
        },
        // 时间自动计算方法
        calculateEndTime() {
            // 当开始时间改变时，根据持续时间计算结束时间
            if (this.form.startTime && this.form.totalTime) {
                const [hours, minutes] = this.form.startTime.split(':');
                const startMinutes = parseInt(hours) * 60 + parseInt(minutes);
                const endMinutes = startMinutes + parseInt(this.form.totalTime);
                
                const endHours = Math.floor(endMinutes / 60) % 24;
                const endMins = endMinutes % 60;
                
                this.form.endTime = `${String(endHours).padStart(2, '0')}:${String(endMins).padStart(2, '0')}:00`;
            }
        },
        
        calculateDuration() {
            // 当结束时间改变时，根据开始时间计算持续时间
            if (this.form.startTime && this.form.endTime) {
                const [startHours, startMinutes] = this.form.startTime.split(':');
                const [endHours, endMinutes] = this.form.endTime.split(':');
                
                const startTotalMinutes = parseInt(startHours) * 60 + parseInt(startMinutes);
                const endTotalMinutes = parseInt(endHours) * 60 + parseInt(endMinutes);
                
                let duration = endTotalMinutes - startTotalMinutes;
                
                // 处理跨天的情况
                if (duration < 0) {
                    duration += 24 * 60;
                }
                
                this.form.totalTime = duration;
            }
        },
        
        calculateEndTimeFromDuration() {
            // 当持续时间改变时，根据开始时间计算结束时间
            if (this.form.startTime && this.form.totalTime) {
                this.calculateEndTime();
            }
        },

        formatTime(date) {
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
        onSubmit() {
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
                    message: `添加失败：介绍的考试类型"${descLast2}"与试卷类型"${this.form.type}"不符`,
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
            if (this.form.startTime == null || this.form.startTime == "") {
                this.$message({
                    message: "开始时间不能为空",
                    type: "error",
                });
                return;
            }
            if (this.form.endTime == null || this.form.endTime == "") {
                this.$message({
                    message: "结束时间不能为空",
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
            this.$axios(`/api/examManagePaperId`).then((res) => {
                console.log(res)
                if(res.data.data == null || res.data.data.paperId == null) {
                    res.data.data = {
                        paperId : 1000
                    }
                }
                this.form.paperId = res.data.data.paperId + 1; //实现paperId自增1
                this.$axios({
                    url: "/api/exam",
                    method: "post",
                    data: {
                        ...this.form,
                    },
                }).then((res) => {
                    if (res.data.code == 200) {
                        this.$message({
                            message: "数据添加成功",
                            type: "success",
                        });
                        this.$router.push({ path: "/selectExam" });
                    }
                });
            });
        },
        cancel() {
            //取消按钮
            this.form = {};
        },
    },
};
</script>
<style lang="less" scoped>
.add {
    padding: 20px;
    max-width: 600px;
    margin: 0 auto;
    background-color: var(--card-bg);
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 var(--shadow-color);
    
    .el-form {
        width: 100%;
    }
    
    .el-form-item__label {
        color: var(--text-primary) !important;
        font-weight: 500;
    }
    
    .button-group {
        text-align: center;
        margin-top: 30px;
        
        .el-button {
            width: 120px;
            margin: 0 10px;
            font-size: 16px;
            padding: 12px 24px;
            border-radius: 6px;
            font-weight: 500;
            transition: all 0.3s ease;
            
            &:hover {
                transform: translateY(-2px);
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            }
        }
        
        .el-button--primary {
            background: linear-gradient(135deg, var(--button-primary), var(--button-secondary));
            border: none;
            
            &:hover {
                background: linear-gradient(135deg, var(--button-secondary), var(--button-primary));
            }
        }
        
        .el-button--danger {
            background: linear-gradient(135deg, #f56565, #e53e3e);
            border: none;
            
            &:hover {
                background: linear-gradient(135deg, #e53e3e, #c53030);
            }
        }
    }
}
</style>
