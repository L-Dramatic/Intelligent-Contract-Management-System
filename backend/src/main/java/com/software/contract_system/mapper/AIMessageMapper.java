package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.AIMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI消息Mapper
 */
@Mapper
public interface AIMessageMapper extends BaseMapper<AIMessage> {

    /**
     * 获取会话的最近N条消息（用于上下文）
     */
    @Select("SELECT * FROM t_ai_message " +
            "WHERE session_id = #{sessionId} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<AIMessage> getRecentMessages(@Param("sessionId") String sessionId, @Param("limit") int limit);

    /**
     * 获取会话的所有消息
     */
    @Select("SELECT * FROM t_ai_message " +
            "WHERE session_id = #{sessionId} " +
            "ORDER BY created_at ASC")
    List<AIMessage> getBySessionId(@Param("sessionId") String sessionId);
}
