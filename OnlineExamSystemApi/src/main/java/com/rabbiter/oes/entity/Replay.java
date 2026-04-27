package com.rabbiter.oes.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Replay {
    private Integer messageId;

    private Integer replayId;

    private String replay;
    
    private String replayerName; // 回复人姓名
    private String replayerId; // 回复人学号
    private String replyToName; // 被回复人姓名
    private String replyToId; // 被回复人学号（仅用于通知，不入库）

    @JsonFormat(pattern = "yyyy-MM-dd", timezone="GMT+8")
    private Date replayTime;

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    public Integer getReplayId() {
        return replayId;
    }

    public void setReplayId(Integer replayId) {
        this.replayId = replayId;
    }

    public String getReplay() {
        return replay;
    }

    public void setReplay(String replay) {
        this.replay = replay;
    }

    public Date getReplayTime() {
        return replayTime;
    }

    public void setReplayTime(Date replayTime) {
        this.replayTime = replayTime;
    }

    public String getReplayerName() {
        return replayerName;
    }

    public void setReplayerName(String replayerName) {
        this.replayerName = replayerName;
    }

    public String getReplayerId() {
        return replayerId;
    }

    public void setReplayerId(String replayerId) {
        this.replayerId = replayerId;
    }

    public String getReplyToName() {
        return replyToName;
    }

    public void setReplyToName(String replyToName) {
        this.replyToName = replyToName;
    }

    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }

    @Override
    public String toString() {
        return "Replay{" +
                "messageId=" + messageId +
                ", replayId=" + replayId +
                ", replay='" + replay + '\'' +
                ", replayerName='" + replayerName + '\'' +
                ", replyToName='" + replyToName + '\'' +
                ", replayTime=" + replayTime +
                '}';
    }
}
