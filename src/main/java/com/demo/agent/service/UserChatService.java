package com.demo.agent.service;

import com.demo.agent.model.ChatMessages;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;
import java.util.Map;

public interface UserChatService {
    /**
     * 根据userId获取该用户的所有会话记录
     */
    List<ChatMessages> getUserChatSessions(Long userId);

    /**
     * 根据userId和memoryId获取特定会话
     */
    ChatMessages getUserChatSession(Long userId, String memoryId);

    /**
     * 获取用户所有会话的详细信息（包含实际消息内容）
     */
    Map<String, Object> getUserAllConversationsDetail(Long userId);
}