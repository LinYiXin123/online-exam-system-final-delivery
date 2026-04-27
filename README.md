# 在线考试与题库管理系统交付版

本目录是整理后的最终交付版，目标是做到结构清晰、启动路径明确、论文材料集中、拿到后可以快速理解并运行。

## 1. 项目概览

- 后端：`Spring Boot 2.1.2` + `MyBatis Plus`
- 前端：`Vue 2` + `Element UI`
- 数据库：`MySQL 5.7/8.x`
- 后端端口：`9201`
- 前端端口：`9202`
- 数据库名：`online_exam`

本交付版已经保留了：

- 可运行的前后端源码
- 一键导入数据库脚本
- 项目启动脚本
- 必须保留的论文材料、参考文献核验 Excel、检测报告
- README 配图素材

## 2. 目录结构

```text
OnlineExamSystem_FinalDelivery/
├─ OnlineExamSystemApi/              后端源码与已打包 JAR
├─ OnlineExamSystemVue/              前端源码与 node_modules
├─ apache-maven-3.8.8/               项目自带 Maven
├─ database/                         数据库脚本
├─ docs/
│  ├─ 01_必须保留材料/              论文、任务书、开题报告、参考文献核验表、检测报告
│  └─ 02_项目说明/                  数据导入、数据库部署、后端配置、增强分析说明
├─ assets/readme/                    README 截图与 ER 图
├─ start_backend.bat                 后端启动脚本
├─ start_frontend.bat                前端启动脚本
└─ README.md
```

## 3. 界面预览

### 登录页

![登录页](assets/readme/login-page.png)

### 题库管理页

![题库管理页](assets/readme/question-bank-page.png)

### 考试管理相关结构图

![考试管理结构图](assets/readme/er-diagrams/08_考试管理.png)

## 4. 运行环境

启动本项目前，请确保本机具备以下环境：

- `JDK 8`
- `MySQL 5.7` 或 `MySQL 8.x`
- `Node.js 16.13.2` 优先

说明：

- 本目录已自带 `apache-maven-3.8.8`，即使系统未全局安装 Maven，也可以通过脚本打包后端。
- 本目录已保留前端 `node_modules`，避免再次安装依赖。
- 前端依赖较老，推荐 `Node 16.13.2`。`Node 22/24` 直接运行大概率会报错。

## 5. 快速启动

### 第一步：启动 MySQL

请先确保 MySQL 服务已经启动，并且存在数据库 `online_exam`。

如果数据库还没有导入，可以使用本目录中的脚本：

- `database/online_exam_one_click.sql`

参考说明：

- `docs/02_项目说明/数据库部署指南.md`
- `docs/02_项目说明/数据导入和测试指南.md`

### 第二步：启动后端

直接运行根目录：

```bat
start_backend.bat
```

脚本逻辑：

- 如果已存在 `OnlineExamSystemApi/target/OnlineExamSystemApi-0.0.1-SNAPSHOT.jar`，则直接启动
- 如果 JAR 不存在，则自动调用自带 Maven 先打包再启动

后端成功启动后，接口地址为：

```text
http://127.0.0.1:9201
```

### 第三步：启动前端

直接运行根目录：

```bat
start_frontend.bat
```

前端成功启动后，访问地址为：

```text
http://127.0.0.1:9202
```

## 6. 默认测试账号

数据库脚本内包含默认测试数据，可优先使用以下账号：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `9991` | `123456` |
| 教师 | `20081001` | `123456` |
| 学生 | `20220102` | `123456` |

说明：

- 管理员账号来自数据库脚本中的 `admin` 初始化数据
- 教师和学生账号来自数据库脚本中的测试数据
- 个别旧数据密码可能存在明文与加密混合情况，以上三组是优先推荐测试账号

## 7. 关键配置

后端数据库配置文件：

```text
OnlineExamSystemApi/src/main/resources/application.properties
```

当前默认配置要点：

- 数据库地址：`localhost:3306/online_exam`
- 用户名：`root`
- 密码：`123456`
- 服务端口：`9201`

如果你要迁移到别的电脑，通常只需要按实际环境修改这一个文件。

为了后续公开发布或交接，额外提供了一个模板文件：

```text
OnlineExamSystemApi/src/main/resources/application.example.properties
```

## 8. 数据库脚本说明

`database` 目录中保留了两个文件：

- `online_exam_one_click.sql`
  这是推荐使用的一键导入脚本，包含当前项目运行所需主要表结构和测试数据。
- `online_exam_final_schema.sql`
  这是相对精简的最终结构说明，可用于辅助查阅。

## 9. 必须保留材料

根据本次整理要求，下列材料已经集中放入：

```text
docs/01_必须保留材料/
```

其中包括：

- `检测修改论文-基于SpringBoot+Vue的在线考试与题库管理系统的设计与实现.docx`
- `检测修改论文-基于SpringBoot+Vue的在线考试与题库管理系统的设计与实现.pdf`
- `林艺新-开题报告.docx`
- `林艺新-任务书.docx`
- `论文参考文献核验表_同步电子资源修正_20260423.xlsx`
- `检测报告/`

## 10. 本交付版的整理原则

本交付版只保留了对以下目标有价值的内容：

- 项目直接启动
- 数据库直接导入
- 论文与检测材料直接查看
- 配置与部署说明直接查阅

没有纳入本交付版的内容，主要是：

- 历史日志文件
- 中间生成脚本
- 论文修订过程中的临时输出
- 测试性 HTML / Python 工程 / 备份脚本
- 前端 `dist` 构建产物
- IDE 元数据目录

如果你后面还想继续精简原始工程目录，可以参考：

```text
docs/03_整理与发布说明.md
```

## 11. GitHub 公开上传前提醒

如果你要把这个交付版公开上传到 GitHub，请务必先看：

```text
docs/03_整理与发布说明.md
```

原因很重要：

- 当前后端配置文件里含有真实数据库配置与 AI 接口配置
- 必保留论文材料里含有个人信息和论文正文
- 公开仓库不建议直接包含 `node_modules` 和大体积运行时目录

## 12. 推荐交付方式

如果是交老师、客户或答辩使用，推荐直接交这个目录：

```text
D:\1毕业论文接单\OnlineExamSystem_FinalDelivery
```

如果是准备公开代码仓库，建议基于这个目录再做一次“公开版脱敏”处理后再上传。
