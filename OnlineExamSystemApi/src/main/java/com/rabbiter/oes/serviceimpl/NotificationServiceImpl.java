package com.rabbiter.oes.serviceimpl;

import com.rabbiter.oes.entity.Notification;
import com.rabbiter.oes.mapper.NotificationMapper;
import com.rabbiter.oes.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public List<Notification> findByReceiverId(String receiverId) {
        return notificationMapper.findByReceiverId(receiverId);
    }

    @Override
    public int countUnread(String receiverId) {
        return notificationMapper.countUnread(receiverId);
    }

    @Override
    public int markAsRead(Integer id) {
        return notificationMapper.markAsRead(id);
    }

    @Override
    public int markAllAsRead(String receiverId) {
        return notificationMapper.markAllAsRead(receiverId);
    }

    @Override
    public int add(Notification notification) {
        return notificationMapper.add(notification);
    }

    @Override
    public int delete(Integer id) {
        return notificationMapper.delete(id);
    }
    
    @Override
    public int deleteByReplayId(Integer replayId) {
        return notificationMapper.deleteByReplayId(replayId);
    }

    @Override
    public int deleteByMessageId(Integer messageId) {
        return notificationMapper.deleteByMessageId(messageId);
    }
}
