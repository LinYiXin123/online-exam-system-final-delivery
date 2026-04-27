package com.rabbiter.oes.mapper;

import com.rabbiter.oes.entity.Replay;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReplayMapper {

    @Select("select messageId,replayId,replay,replayerName,replayerId,replyToName,replayTime from replay")
    List<Replay> findAll();

    @Select("select messageId,replayId,replay,replayerName,replayerId,replyToName,replayTime from replay where messageId = #{messageId}")
    List<Replay> findAllById(Integer messageId);

    @Select("select messageId,replayId,replay,replayerName,replayerId,replyToName,replayTime from replay where replayId = #{replayId}")
    Replay findById(Integer replayId);

    @Delete("delete from replay where replayId = #{replayId}")
    int delete(Integer replayId);

    @Delete("delete from replay where messageId = #{messageId}")
    int deleteByMessageId(Integer messageId);

    @Update("update replay set replay = #{replay}, replayTime = #{replayTime} where replayId = #{replayId}")
    int update(Replay replay);

    @Options(useGeneratedKeys = true,keyProperty = "replayId")
    @Insert("insert into replay(messageId,replay,replayerName,replayerId,replyToName,replayTime) values(#{messageId}, #{replay},#{replayerName},#{replayerId},#{replyToName},#{replayTime})")
    int add(Replay replay);
}
