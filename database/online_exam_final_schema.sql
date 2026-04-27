-- =====================================================
-- 在线考试系统整理版最终结构 SQL
-- 说明: 仅保留当前数据库最终生效的表结构，不包含测试数据
-- 生成时间: 2026-04-17
-- =====================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP DATABASE IF EXISTS `online_exam`;
CREATE DATABASE `online_exam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `online_exam`;

-- =====================================================
-- 1. 学生表
-- =====================================================
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
  `studentId` bigint(20) NOT NULL AUTO_INCREMENT,
  `studentName` varchar(20) DEFAULT NULL,
  `grade` varchar(4) DEFAULT NULL,
  `major` varchar(20) DEFAULT NULL,
  `clazz` varchar(10) DEFAULT NULL,
  `institute` varchar(30) DEFAULT NULL,
  `tel` varchar(11) DEFAULT NULL,
  `email` varchar(30) DEFAULT NULL,
  `pwd` varchar(60) DEFAULT NULL,
  `cardId` varchar(18) DEFAULT NULL,
  `sex` varchar(2) DEFAULT NULL,
  `role` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`studentId`)
) ENGINE=InnoDB AUTO_INCREMENT=20230808 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 2. 教师表
-- =====================================================
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher` (
  `teacherId` int(11) NOT NULL AUTO_INCREMENT,
  `teacherName` varchar(20) DEFAULT NULL,
  `institute` varchar(20) DEFAULT NULL,
  `sex` varchar(2) DEFAULT NULL,
  `tel` varchar(11) DEFAULT NULL,
  `email` varchar(30) DEFAULT NULL,
  `pwd` varchar(60) DEFAULT NULL,
  `cardId` varchar(18) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `role` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`teacherId`)
) ENGINE=InnoDB AUTO_INCREMENT=20081016 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 3. 管理员表
-- =====================================================
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `adminId` int(11) NOT NULL AUTO_INCREMENT,
  `adminName` varchar(20) DEFAULT NULL,
  `sex` varchar(2) DEFAULT NULL,
  `tel` varchar(11) DEFAULT NULL,
  `email` varchar(20) DEFAULT NULL,
  `pwd` varchar(60) DEFAULT NULL,
  `cardId` varchar(18) DEFAULT NULL,
  `role` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`adminId`)
) ENGINE=InnoDB AUTO_INCREMENT=9992 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 4. 选择题表
-- =====================================================
DROP TABLE IF EXISTS `multi_question`;
CREATE TABLE `multi_question` (
  `questionId` int(11) NOT NULL AUTO_INCREMENT,
  `subject` varchar(20) DEFAULT NULL,
  `question` varchar(255) DEFAULT NULL,
  `answerA` varchar(255) DEFAULT NULL,
  `answerB` varchar(255) DEFAULT NULL,
  `answerC` varchar(255) DEFAULT NULL,
  `answerD` varchar(255) DEFAULT NULL,
  `rightAnswer` varchar(2) DEFAULT NULL,
  `analysis` varchar(255) DEFAULT NULL,
  `score` int(11) DEFAULT '2',
  `section` varchar(20) DEFAULT NULL,
  `level` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`questionId`)
) ENGINE=InnoDB AUTO_INCREMENT=10301 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 5. 填空题表
-- =====================================================
DROP TABLE IF EXISTS `fill_question`;
CREATE TABLE `fill_question` (
  `questionId` int(11) NOT NULL AUTO_INCREMENT,
  `subject` varchar(20) DEFAULT NULL,
  `question` varchar(255) DEFAULT NULL,
  `answer` varchar(255) DEFAULT NULL,
  `analysis` varchar(255) DEFAULT NULL,
  `score` int(11) DEFAULT '2',
  `level` varchar(10) DEFAULT NULL,
  `section` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`questionId`)
) ENGINE=InnoDB AUTO_INCREMENT=20181 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 6. 判断题表
-- =====================================================
DROP TABLE IF EXISTS `judge_question`;
CREATE TABLE `judge_question` (
  `questionId` int(11) NOT NULL AUTO_INCREMENT,
  `subject` varchar(20) DEFAULT NULL,
  `question` varchar(255) DEFAULT NULL,
  `answer` varchar(2) DEFAULT NULL,
  `analysis` varchar(255) DEFAULT NULL,
  `score` int(11) DEFAULT '2',
  `level` varchar(10) DEFAULT NULL,
  `section` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`questionId`)
) ENGINE=InnoDB AUTO_INCREMENT=30181 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 7. 主观题表
-- =====================================================
DROP TABLE IF EXISTS `essay_question`;
CREATE TABLE `essay_question` (
  `questionId` int(11) NOT NULL AUTO_INCREMENT,
  `subject` varchar(20) DEFAULT NULL COMMENT '科目',
  `question` text COMMENT '题目内容',
  `referenceAnswer` text COMMENT '参考答案',
  `scoringCriteria` text COMMENT '评分标准/要点',
  `score` int(11) DEFAULT '10' COMMENT '分值',
  `section` varchar(20) DEFAULT NULL COMMENT '章节/知识点',
  `level` varchar(10) DEFAULT NULL COMMENT '难度：简单/中等/困难',
  PRIMARY KEY (`questionId`)
) ENGINE=InnoDB AUTO_INCREMENT=40037 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='简答题/主观题表';

-- =====================================================
-- 8. 考试管理表
-- =====================================================
DROP TABLE IF EXISTS `exam_manage`;
CREATE TABLE `exam_manage` (
  `examCode` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(50) DEFAULT NULL,
  `source` varchar(20) DEFAULT NULL,
  `paperId` int(11) DEFAULT NULL,
  `examDate` varchar(20) DEFAULT NULL,
  `startTime` time DEFAULT NULL COMMENT '考试开始时间',
  `endTime` time DEFAULT NULL COMMENT '考试结束时间',
  `totalTime` int(11) DEFAULT NULL,
  `grade` varchar(10) DEFAULT NULL,
  `term` varchar(10) DEFAULT NULL,
  `major` varchar(20) DEFAULT NULL,
  `institute` varchar(30) DEFAULT NULL,
  `totalScore` int(11) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `tips` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`examCode`)
) ENGINE=InnoDB AUTO_INCREMENT=20230007 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 9. 试卷组成表
-- =====================================================
DROP TABLE IF EXISTS `paper_manage`;
CREATE TABLE `paper_manage` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `paperId` int(11) DEFAULT NULL,
  `questionType` int(11) DEFAULT NULL,
  `questionId` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_paperId` (`paperId`)
) ENGINE=InnoDB AUTO_INCREMENT=296 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 10. 成绩表
-- =====================================================
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score` (
  `scoreId` int(11) NOT NULL AUTO_INCREMENT,
  `examCode` int(11) DEFAULT NULL,
  `studentId` bigint(20) DEFAULT NULL,
  `subject` varchar(20) DEFAULT NULL,
  `ptScore` int(11) DEFAULT NULL,
  `etScore` int(11) DEFAULT NULL,
  `score` int(11) DEFAULT NULL,
  `answerDate` varchar(20) DEFAULT NULL,
  `scoreType` varchar(10) DEFAULT 'exam',
  PRIMARY KEY (`scoreId`)
) ENGINE=InnoDB AUTO_INCREMENT=183 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 11. 答题记录表
-- =====================================================
DROP TABLE IF EXISTS `answer_record`;
CREATE TABLE `answer_record` (
  `recordId` int(11) NOT NULL AUTO_INCREMENT,
  `studentId` bigint(20) DEFAULT NULL,
  `examCode` int(11) DEFAULT NULL,
  `questionId` int(11) DEFAULT NULL,
  `questionType` int(11) DEFAULT NULL,
  `studentAnswer` varchar(255) DEFAULT NULL,
  `isCorrect` int(11) DEFAULT NULL,
  PRIMARY KEY (`recordId`)
) ENGINE=InnoDB AUTO_INCREMENT=10538 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 12. 主观题答案表
-- =====================================================
DROP TABLE IF EXISTS `essay_answer`;
CREATE TABLE `essay_answer` (
  `answerId` int(11) NOT NULL AUTO_INCREMENT,
  `examCode` int(11) DEFAULT NULL COMMENT '考试编号',
  `studentId` bigint(20) DEFAULT NULL COMMENT '学生ID',
  `questionId` int(11) DEFAULT NULL COMMENT '题目ID',
  `studentAnswer` text COMMENT '学生作答内容',
  `aiScore` int(11) DEFAULT NULL COMMENT 'AI评分',
  `aiComment` text COMMENT 'AI评语',
  `teacherScore` int(11) DEFAULT NULL COMMENT '教师评分(可覆盖AI)',
  `teacherComment` text COMMENT '教师评语',
  `finalScore` int(11) DEFAULT NULL COMMENT '最终得分',
  `gradingStatus` varchar(20) DEFAULT 'pending' COMMENT '评分状态：pending/ai_graded/teacher_reviewed',
  `gradingTime` datetime DEFAULT NULL COMMENT '评分时间',
  PRIMARY KEY (`answerId`),
  KEY `idx_exam_student` (`examCode`,`studentId`)
) ENGINE=InnoDB AUTO_INCREMENT=440 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生主观题答案表';

-- =====================================================
-- 13. 消息表
-- =====================================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `studentId` varchar(20) DEFAULT NULL COMMENT '发布者学号',
  `title` varchar(20) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 14. 回复表
-- =====================================================
DROP TABLE IF EXISTS `replay`;
CREATE TABLE `replay` (
  `messageId` int(11) DEFAULT NULL,
  `replayId` int(11) NOT NULL AUTO_INCREMENT,
  `replay` varchar(255) DEFAULT NULL,
  `replayTime` datetime DEFAULT NULL,
  `replayerName` varchar(50) DEFAULT NULL COMMENT '回复人姓名',
  `replayerId` varchar(20) DEFAULT NULL COMMENT '回复人学号',
  `replyToName` varchar(50) DEFAULT NULL COMMENT '被回复人姓名',
  PRIMARY KEY (`replayId`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- 15. 通知表
-- =====================================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `receiverId` varchar(20) DEFAULT NULL COMMENT '接收者学号',
  `senderId` varchar(20) DEFAULT NULL COMMENT '发送者学号',
  `senderName` varchar(50) DEFAULT NULL COMMENT '发送者姓名',
  `type` varchar(20) DEFAULT 'reply' COMMENT '通知类型: reply',
  `title` varchar(100) DEFAULT NULL COMMENT '通知标题',
  `content` varchar(255) DEFAULT NULL COMMENT '通知内容',
  `messageId` int(11) DEFAULT NULL COMMENT '关联留言ID',
  `replayId` int(11) DEFAULT NULL COMMENT '关联回复ID',
  `isRead` tinyint(4) DEFAULT '0' COMMENT '是否已读: 0未读 1已读',
  `createTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiverId`),
  KEY `idx_message` (`messageId`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息通知表';

-- =====================================================
-- 16. 管理员通知表
-- =====================================================
DROP TABLE IF EXISTS `admin_notification`;
CREATE TABLE `admin_notification` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '管理员通知ID',
  `type` varchar(20) DEFAULT NULL COMMENT '通知类型',
  `title` varchar(100) DEFAULT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `targetDesc` varchar(50) DEFAULT NULL COMMENT '通知对象说明',
  `createTime` varchar(20) DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员通知历史表';

SET FOREIGN_KEY_CHECKS = 1;
