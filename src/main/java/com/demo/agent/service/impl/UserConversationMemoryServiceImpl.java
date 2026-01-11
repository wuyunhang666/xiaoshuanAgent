package com.demo.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.agent.entity.UserConversationMemory;
import com.demo.agent.mapper.UserConversationMemoryMapper;
import com.demo.agent.model.ChatMessages;
import com.demo.agent.service.UserConversationMemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserConversationMemoryServiceImpl extends ServiceImpl<UserConversationMemoryMapper, UserConversationMemory> implements UserConversationMemoryService {

    @Autowired
    private UserConversationMemoryMapper userConversationMemoryMapper;
    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<UserConversationMemory> getUserMemories(Long userId) {
        LambdaQueryWrapper<UserConversationMemory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversationMemory::getUserId, userId)
               .eq(UserConversationMemory::getIsDeleted, 0); // 未删除的记录
        return userConversationMemoryMapper.selectList(wrapper);
    }

    @Override
    public UserConversationMemory getUserMemoryByUserIdAndMemoryId(Long userId, String memoryId) {
        LambdaQueryWrapper<UserConversationMemory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversationMemory::getUserId, userId)
               .eq(UserConversationMemory::getMemoryId, memoryId)
               .eq(UserConversationMemory::getIsDeleted, 0); // 未删除的记录
        return userConversationMemoryMapper.selectOne(wrapper);
    }

    @Override
    public UserConversationMemory saveUserMemory(Long userId, String memoryId, String conversationTitle) {
        // 先检查是否已经存在该记录
        UserConversationMemory existingMemory = getUserMemoryByUserIdAndMemoryId(userId, memoryId);
        if (existingMemory != null) {
            // 如果存在则返回存在的记录
            return existingMemory;
        }

        UserConversationMemory userMemory = new UserConversationMemory();
        userMemory.setUserId(userId);
        userMemory.setMemoryId(memoryId);
        userMemory.setConversationTitle(conversationTitle);
        userMemory.setCreateTime(LocalDateTime.now());
        userMemory.setUpdateTime(LocalDateTime.now());
        userMemory.setIsDeleted(0);
        
        userConversationMemoryMapper.insert(userMemory);
        return userMemory;
    }

    @Override
    public UserConversationMemory updateConversationTitle(Long id, String conversationTitle) {
        UserConversationMemory userMemory = userConversationMemoryMapper.selectById(id);
        if (userMemory != null && userMemory.getIsDeleted() == 0) {
            userMemory.setConversationTitle(conversationTitle);
            userMemory.setUpdateTime(LocalDateTime.now());
            userConversationMemoryMapper.updateById(userMemory);
        }
        return userMemory;
    }

    @Override
    public boolean deleteUserMemory(Long id) {
        UserConversationMemory userMemory = userConversationMemoryMapper.selectById(id);
        if (userMemory != null) {
            // 逻辑删除
            userMemory.setIsDeleted(1);
            userMemory.setUpdateTime(LocalDateTime.now());
            int result = userConversationMemoryMapper.updateById(userMemory);
            return result > 0;
        }
        return false;
    }

    @Override
    public boolean deleteUserMemoriesByUserId(Long userId) {
        // 获取用户的所有会话记忆
        List<UserConversationMemory> userMemories = getUserMemories(userId);
        for (UserConversationMemory userMemory : userMemories) {
            userMemory.setIsDeleted(1);
            userMemory.setUpdateTime(LocalDateTime.now());
            userConversationMemoryMapper.updateById(userMemory);
        }
        return true;
    }

    @Override
    public UserConversationMemory createNewUserMemory(Long userId, String memoryId, String conversationTitle) {
        // 检查是否已存在相同的记录
        UserConversationMemory existingMemory = getUserMemoryByUserIdAndMemoryId(userId, memoryId);
        if (existingMemory != null) {
            // 如果存在，可以选择返回现有记录或抛出异常，这里返回null表示已存在
            return null;
        }
        
        UserConversationMemory userMemory = new UserConversationMemory();
        userMemory.setUserId(userId);
        userMemory.setMemoryId(memoryId);
        userMemory.setConversationTitle(conversationTitle);
        userMemory.setCreateTime(LocalDateTime.now());
        userMemory.setUpdateTime(LocalDateTime.now());
        userMemory.setIsDeleted(0);
        
        userConversationMemoryMapper.insert(userMemory);
        return userMemory;
    }

    @Override
    public Map<String, Object> getAllUserConversationsDetail(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取用户的所有会话记录
        List<UserConversationMemory> userMemories = getUserMemories(userId);
        
        for (UserConversationMemory memory : userMemories) {
            // 获取每个会话的详细聊天内容
            String memoryId = memory.getMemoryId();
            List<dev.langchain4j.data.message.ChatMessage> messages = getMessagesFromMongoDb(memoryId);
            
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("conversation", memory);
            sessionInfo.put("messages", messages);
            result.put(memoryId, sessionInfo);
        }
        
        return result;
    }
    
    // 从MongoDB获取聊天消息的辅助方法
    private List<dev.langchain4j.data.message.ChatMessage> getMessagesFromMongoDb(String memoryId) {
        org.springframework.data.mongodb.core.query.Criteria criteria = 
            org.springframework.data.mongodb.core.query.Criteria.where("memoryId").is(memoryId);
        org.springframework.data.mongodb.core.query.Query query = 
            new org.springframework.data.mongodb.core.query.Query(criteria);
        
        com.demo.agent.model.ChatMessages chatMessages = 
            mongoTemplate.findOne(query, com.demo.agent.model.ChatMessages.class);
        
        if (chatMessages != null) {
            String content = chatMessages.getContent();
            return dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson(content);
        }
        
        return new java.util.ArrayList<>();
    }
}