package com.demo.agent.service;

import com.demo.agent.entity.UserConversationMemory;

import java.util.List;
import java.util.Map;

public interface UserConversationMemoryService {
    /**
     * 根据用户ID获取所有会话记忆
     */
    List<UserConversationMemory> getUserMemories(Long userId);

    /**
     * 根据用户ID和memoryId获取会话记忆
     */
    UserConversationMemory getUserMemoryByUserIdAndMemoryId(Long userId, String memoryId);

    /**
     * 保存用户会话记忆
     */
    UserConversationMemory saveUserMemory(Long userId, String memoryId, String conversationTitle);

    /**
     * 更新会话标题
     */
    UserConversationMemory updateConversationTitle(Long id, String conversationTitle);

    /**
     * 删除用户会话记忆
     */
    boolean deleteUserMemory(Long id);

    /**
     * 删除用户的所有会话记忆
     */
    boolean deleteUserMemoriesByUserId(Long userId);
    
    /**
     * 创建新的用户会话记忆
     */
    UserConversationMemory createNewUserMemory(Long userId, String memoryId, String conversationTitle);
    
    /**
     * 根据userId获取该用户下所有的会话信息详情
     */
    Map<String, Object> getAllUserConversationsDetail(Long userId);
}