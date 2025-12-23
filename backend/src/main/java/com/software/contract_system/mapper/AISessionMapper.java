package com.software.contract_system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.software.contract_system.entity.AISession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * AI会话Mapper
 */
@Mapper
public interface AISessionMapper extends BaseMapper<AISession> {

    /**
     * 根据sessionId获取会话
     */
    @Select("SELECT * FROM t_ai_session WHERE session_id = #{sessionId}")
    AISession getBySessionId(@Param("sessionId") String sessionId);

    /**
     * 更新最后活跃时间
     */
    @Update("UPDATE t_ai_session SET last_active_at = NOW(), message_count = message_count + 1 " +
            "WHERE session_id = #{sessionId}")
    int updateLastActive(@Param("sessionId") String sessionId);

    /**
     * 更新合同快照
     */
    @Update("UPDATE t_ai_session SET contract_snapshot = #{snapshot}, last_active_at = NOW() " +
            "WHERE session_id = #{sessionId}")
    int updateContractSnapshot(@Param("sessionId") String sessionId, @Param("snapshot") String snapshot);
}
