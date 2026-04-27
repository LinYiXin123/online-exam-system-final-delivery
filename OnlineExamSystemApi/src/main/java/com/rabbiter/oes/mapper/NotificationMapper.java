package com.rabbiter.oes.mapper;

import com.rabbiter.oes.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Select("SELECT * FROM notification WHERE receiverId = #{receiverId} ORDER BY createTime DESC")
    List<Notification> findByReceiverId(String receiverId);

    @Select("SELECT * FROM notification WHERE id = #{id}")
    Notification findById(Integer id);

    @Select("SELECT COUNT(*) FROM notification WHERE receiverId = #{receiverId} AND isRead = 0")
    int countUnread(String receiverId);

    @Update("UPDATE notification SET isRead = 1 WHERE id = #{id}")
    int markAsRead(Integer id);

    @Update("UPDATE notification SET isRead = 1 WHERE receiverId = #{receiverId}")
    int markAllAsRead(String receiverId);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO notification(receiverId, senderId, senderName, type, title, content, messageId, replayId, isRead, createTime) " +
            "VALUES(#{receiverId}, #{senderId}, #{senderName}, #{type}, #{title}, #{content}, #{messageId}, #{replayId}, #{isRead}, #{createTime})")
    int add(Notification notification);

    @Delete("DELETE FROM notification WHERE id = #{id}")
    int delete(Integer id);
    
    @Delete("DELETE FROM notification WHERE replayId = #{replayId}")
    int deleteByReplayId(Integer replayId);

    @Delete("DELETE FROM notification WHERE messageId = #{messageId}")
    int deleteByMessageId(Integer messageId);
}
