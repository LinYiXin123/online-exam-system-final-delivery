// 我的试卷页面
<template>
    <div id="myExam">
        <div class="title" style="font-size: 24px;">
            <i class="iconfont icon-r-paper" style="font-size: 32px;"></i>
            考试中心
        </div>
        <div class="wrapper">
            <ul class="top">
                <li  style="font-size: 20px;"><i style="font-size: 26px;margin-right: 10px;" class="iconfont icon-r-list"></i> 试卷列表</li>
                <li class="view-switch">
                    <el-tooltip content="卡片视图" placement="top">
                        <el-button size="small" :class="{ active: viewMode === 'card' }" @click="viewMode = 'card'">
                            <i class="el-icon-s-grid"></i>
                        </el-button>
                    </el-tooltip>
                    <el-tooltip content="表格视图" placement="top">
                        <el-button size="small" :class="{ active: viewMode === 'table' }" @click="viewMode = 'table'">
                            <i class="el-icon-s-unfold"></i>
                        </el-button>
                    </el-tooltip>
                </li>
                <li class="search-li">
                    <div class="icon">
                        <input type="text" placeholder="试卷名称" class="search" v-model="key" /><i class="el-icon-search"></i>
                    </div>
                </li>
                <li>
                    <el-button type="primary" @click="search()" class="search-btn"> 搜索试卷</el-button>
                </li>
            </ul>
            <!-- 卡片视图 -->
            <ul class="paper" v-if="viewMode === 'card'" v-loading="loading">
                <li @click="toExamMsg(item)" class="item" v-for="(item, index) in pagination.records" :key="index">
                    <div style="font-size: 22px;">
                        <i class="iconfont icon-r-paper" style="font-size: 28px;"></i>
                        {{ item.source }}
                    </div>
                    <p class="name">{{ item.source }}-{{ item.description }}</p>
                    <div class="info">
                        <span>考试时间：{{ item.examDate.substr(0, 10) }}</span>
                        <span v-if="item.totalTime != null">限时{{ item.totalTime }}分钟</span>
                        <span>满分{{ item.totalScore }}分</span>
                    </div>
                </li>
            </ul>
            <!-- 表格视图 -->
            <div class="table-view" v-if="viewMode === 'table'" v-loading="loading">
                <el-table :data="pagination.records" style="width: 100%" @row-click="toExamMsg" :row-style="{cursor: 'pointer'}">
                    <el-table-column label="科目名称" min-width="150">
                        <template slot-scope="scope">
                            <i class="iconfont icon-r-paper" style="font-size: 18px; margin-right: 8px; color: var(--primary-color, #0195ff);"></i>
                            <span style="font-weight: 600;">{{ scope.row.source }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column prop="description" label="考试描述" min-width="200"></el-table-column>
                    <el-table-column label="考试时间" width="130">
                        <template slot-scope="scope">
                            {{ scope.row.examDate.substr(0, 10) }}
                        </template>
                    </el-table-column>
                    <el-table-column label="限时" width="100">
                        <template slot-scope="scope">
                            <span v-if="scope.row.totalTime">{{ scope.row.totalTime }}分钟</span>
                            <span v-else>不限</span>
                        </template>
                    </el-table-column>
                    <el-table-column label="满分" width="80">
                        <template slot-scope="scope">
                            <el-tag type="primary" size="small">{{ scope.row.totalScore }}分</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="100">
                        <template slot-scope="scope">
                            <el-button type="primary" size="mini" @click.stop="toExamMsg(scope.row)">进入</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div class="pagination">
                <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                    :current-page="pagination.current" :page-sizes="[6, 10, 20, 40]" :page-size="pagination.size"
                    layout="total, sizes, prev, pager, next, jumper" :total="pagination.total">
                </el-pagination>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    // name: 'myExam'
    data() {
        return {
            loading: false,
            key: null, //搜索关键字
            allExam: null, //所有考试信息
            viewMode: 'card', //视图模式: card卡片 list列表
            pagination: {
                //分页后的考试信息
                current: 1, //当前页
                total: null, //记录条数
                size: 6, //每页条数
            },
        };
    },
    created() {
        this.getExamInfo();
        this.loading = true;
    },
    // watch: {

    // },
    methods: {
        //获取当前所有考试信息（根据学生学院和专业筛选）
        getExamInfo() {
            // 获取学生学院和专业信息
            const institute = sessionStorage.getItem("cinstitute") || this.$cookies.get("cinstitute") || "@";
            const major = sessionStorage.getItem("cmajor") || this.$cookies.get("cmajor") || "@";
            // 使用带条件的API，筛选学院和专业
            const apiUrl = `/api/exams/${this.pagination.current}/${this.pagination.size}/@/${encodeURIComponent(institute)}/${encodeURIComponent(major)}/@`;
            this.$axios(apiUrl)
                .then((res) => {
                    console.log("试卷列表响应:", res.data);
                    if (res.data && res.data.data) {
                        const data = res.data.data;
                        this.pagination = {
                            ...data,
                            current: parseInt(data.current) || 1,
                            total: parseInt(data.total) || 0,
                            size: parseInt(data.size) || 6
                        };
                    } else {
                        // 如果没有数据，设置空列表
                        this.pagination.records = [];
                        this.pagination.total = 0;
                    }
                    this.loading = false;
                })
                .catch((error) => {
                    console.log("试卷列表错误:", error);
                    this.loading = false;
                    this.pagination.records = [];
                    this.pagination.total = 0;
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
        //搜索试卷（同时考虑学院和专业筛选）
        search() {
            const institute = sessionStorage.getItem("cinstitute") || this.$cookies.get("cinstitute") || "";
            const major = sessionStorage.getItem("cmajor") || this.$cookies.get("cmajor") || "";
            this.$axios("/api/exams").then((res) => {
                if (res.data.code == 200) {
                    let allExam = res.data.data;
                    // 按学院、专业筛选，再按关键字搜索
                    let newPage = allExam.filter((item) => {
                        const matchInstitute = !institute || item.institute === institute;
                        const matchMajor = !major || item.major === major;
                        const matchKey = !this.key || item.source.includes(this.key);
                        return matchInstitute && matchMajor && matchKey;
                    });
                    this.pagination.records = newPage;
                    this.pagination.total = newPage.length;
                }
            });
        },
        //跳转到试卷详情页
        toExamMsg(exam) {
            const status = this.getExamStatus(exam);
            if (status === 'not_started') {
                this.$message({
                    message: "考试尚未开始",
                    type: "error",
                });
                return;
            }
            if (status === 'ended') {
                this.$message({
                    message: "考试已结束",
                    type: "error",
                });
                return;
            }
            // 设置为考试模式（非练习模式）
            this.$store.commit("practice", false);
            this.$router.push({
                path: "/examMsg",
                query: { examCode: exam.examCode },
            });
        },
        buildExamDateTime(dateStr, timeStr) {
            if (!dateStr || !timeStr) {
                return null;
            }
            return new Date(`${dateStr.substr(0, 10)}T${timeStr}`);
        },
        getExamStatus(exam) {
            const now = new Date();
            const examDay = exam.examDate ? exam.examDate.substr(0, 10) : null;
            const start = exam.startTime ? this.buildExamDateTime(exam.examDate, exam.startTime) : this.buildExamDateTime(exam.examDate, '00:00:00');
            const end = exam.endTime ? this.buildExamDateTime(exam.examDate, exam.endTime) : null;
            if (examDay && examDay > this.formatDateNow()) {
                return 'not_started';
            }
            if (examDay === this.formatDateNow() && start && now < start) {
                return 'not_started';
            }
            if (examDay === this.formatDateNow() && end && now > end) {
                return 'ended';
            }
            return 'available';
        },
        formatDateNow() {
            var date = new Date(); // 当前日期时间
            var year = date.getFullYear(); // 年份
            var month = ("0" + (date.getMonth() + 1)).slice(-2); // 月份+1
            var day = ("0" + date.getDate()).slice(-2); // 日
            return year + "-" + month + "-" + day;
        },
    },
};
</script>

<style lang="less" scoped>
.pagination {
    padding: 20px 0px 30px 0px;

    .el-pagination {
        display: flex;
        justify-content: center;
    }
}

.paper {
    h4 {
        cursor: pointer;
    }
}

.paper .item a {
    color: #000;
}

.wrapper .top .order {
    cursor: pointer;
}

.wrapper .top .order:hover {
    color: var(--primary-color, #0195ff);
    border-bottom: 2px solid var(--primary-color, #0195ff);
}

.wrapper .top .order:visited {
    color: var(--primary-color, #0195ff);
    border-bottom: 2px solid var(--primary-color, #0195ff);
}

.item .info i {
    margin-right: 5px;
    color: var(--primary-color, #0195ff);
}

.item .info span {
    margin-right: 14px;
}

.paper .item {
    flex: 1;
    min-width: 280px;
    max-width: 400px;
    border-radius: 8px;
    padding: 20px;
    border: 1px solid #eee;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;
    background: #fff;
}

.item {
    cursor: pointer;
}

.paper .item:hover {
    box-shadow: 0 0 4px 2px rgba(140, 193, 248, 0.45);
    transform: scale(1.03);
}

.paper .item .info {
    font-size: 14px;
    color: #88949b;
}

.paper .item .name {
    font-size: 14px;
    color: #88949b;
}

.paper * {
    margin: 20px 0;
}

.wrapper .paper {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    padding: 20px;
}

.top .el-icon-search {
    position: absolute;
    right: 10px;
    top: 10px;
}

.top .icon {
    position: relative;
}

.wrapper .top {
    border-bottom: 1px solid #eee;
    margin-bottom: 20px;
}

#myExam .search-li {
    margin-left: auto;
}

.top .search-li {
    margin-left: auto;
}

.top li {
    display: flex;
    align-items: center;
}

.top .search {
    margin-left: auto;
    padding: 10px;
    border-radius: 4px;
    border: 1px solid #eee;
    box-shadow: inset 0 1px 1px rgba(0, 0, 0, 0.075);
    transition: border-color ease-in-out 0.15s, box-shadow ease-in-out 0.15s;
}

.top .search:hover {
    color: var(--primary-color, #0195ff);
    border-color: var(--primary-color, #0195ff);
}

.wrapper .top {
    display: flex;
}

.wrapper .top li {
    margin: 20px;
}

#myExam {
    padding: 20px;
    flex: 1;
}

#myExam .title {
    margin: 0 0 20px 0;
    padding: 15px 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

#myExam .wrapper {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 视图切换按钮 */
.view-switch {
    display: flex;
    align-items: center;
    gap: 5px;
    margin-left: 15px;
}

.view-switch .el-button {
    padding: 8px 10px;
    border-radius: 4px;
}

.view-switch .el-button.active {
    background-color: var(--primary-color, #0195ff);
    border-color: var(--primary-color, #0195ff);
    color: #fff;
}

.view-switch .el-button:not(.active) {
    background-color: #f5f7fa;
    border-color: #dcdfe6;
    color: #606266;
}

.view-switch .el-button:not(.active):hover {
    background-color: var(--primary-light, #e6f0fa);
    border-color: var(--primary-color, #0195ff);
    color: var(--primary-color, #0195ff);
}

/* 表格视图样式 */
.table-view {
    padding: 0 20px 20px 20px;
}

.table-view .el-table {
    border-radius: 8px;
    overflow: hidden;
}

.table-view .el-table th {
    background-color: var(--primary-color, #f5f7fa) !important;
    color: #fff !important;
    font-weight: 600;
}

/deep/ .el-table th {
    background-color: var(--primary-color, #f5f7fa) !important;
    color: #fff !important;
}

/deep/ .el-table th .cell {
    color: #fff !important;
}

/deep/ .el-table__header th {
    background-color: var(--primary-color, #f5f7fa) !important;
}

/deep/ .el-table__header th .cell {
    color: #fff !important;
}

.table-view .el-table td {
    color: #333 !important;
}

.table-view .el-table tr:hover > td {
    background-color: var(--primary-light, #ecf5ff) !important;
}
</style>

<style>
/* 全局样式 - 确保视图切换按钮使用主题色 */
#myExam .view-switch .el-button.active {
    background-color: var(--primary-color, #0195ff) !important;
    border-color: var(--primary-color, #0195ff) !important;
    color: #fff !important;
}

#myExam .view-switch .el-button.active i {
    color: #fff !important;
}

#myExam .view-switch .el-button:not(.active):hover {
    background-color: var(--primary-light, #e6f0fa) !important;
    border-color: var(--primary-color, #0195ff) !important;
    color: var(--primary-color, #0195ff) !important;
}

/* 搜索按钮美化样式 */
#myExam .search-btn {
    background-color: var(--primary-color, #409EFF) !important;
    border-color: var(--primary-color, #409EFF) !important;
    color: #fff !important;
}

#myExam .search-btn span {
    color: #fff !important;
}

#myExam .search-btn i {
    color: #fff !important;
}

#myExam .search-btn:hover {
    opacity: 0.9;
}
</style>
