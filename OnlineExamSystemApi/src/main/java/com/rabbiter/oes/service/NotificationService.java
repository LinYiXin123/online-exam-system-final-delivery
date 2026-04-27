package com.rabbiter.oes.service;

import com.rabbiter.oes.entity.Notification;

import java.util.List;

public interface NotificationService {
    List<Notification> findByReceiverId(String receiverId);
    int countUnread(String receiverId);
    int markAsRead(Integer id);
    int markAllAsRead(String receiverId);
    int add(Notification notification);
    int delete(Integer id);
    int deleteByReplayId(Integer replayId);
    int deleteByMessageId(Integer messageId);
}
