package com.demo.agent.service;

import com.demo.agent.model.ChatMessages;
import com.demo.agent.sort.RedisChatMemoryStore;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天记忆同步服务，用于在MongoDB和Redis之间同步聊天数据
 */
@Service
public class ChatMemorySyncService {

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private RedisChatMemoryStore redisChatMemoryStore;
    
    /**
     * 将指定memoryId的聊天记录从MongoDB同步到Redis
     */
    public void syncMemoryFromMongoToRedis(String memoryId) {
        // 从MongoDB获取聊天消息
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);
        
        ChatMessages chatMessages = mongoTemplate.findOne(query, ChatMessages.class);
        if (chatMessages != null) {
            // 解析聊天消息
            String content = chatMessages.getContent();
            List<ChatMessage> messageList = ChatMessageDeserializer.messagesFromJson(content);
            
            // 存储到Redis
            if (messageList != null) {
                redisChatMemoryStore.updateMessages(memoryId, messageList);
            }
        }
    }
    
    /**
     * 将指定memoryId的聊天记录从Redis同步到MongoDB
     * 注意：在新的异步模式下，这个方法主要用于特殊情况下的数据同步
     */
    public void syncMemoryFromRedisToMongo(String memoryId) {
        // 从Redis获取聊天消息
        List<ChatMessage> messages = redisChatMemoryStore.getMessages(memoryId);
        
        // 由于现在采用Redis写入后异步写入MongoDB的模式，
        // 此方法主要用于手动触发同步或数据修复
        if (messages != null && !messages.isEmpty()) {
            String content = dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson(messages);
            
            Criteria criteria = Criteria.where("memoryId").is(memoryId);
            org.springframework.data.mongodb.core.query.Query query = new org.springframework.data.mongodb.core.query.Query(criteria);
            
            // 使用upsert更新或插入
            org.springframework.data.mongodb.core.query.Update update = 
                org.springframework.data.mongodb.core.query.Update.update("content", content);
            mongoTemplate.upsert(query, update, ChatMessages.class);
        }
    }
    
    /**
     * 将用户的会话记忆从MongoDB批量同步到Redis
     */
    public void syncUserMemoriesToRedis(Long userId, UserConversationMemoryService userConversationMemoryService) {
        // 获取用户的所有会话记忆
        var userMemories = userConversationMemoryService.getUserMemories(userId);
        
        // 同步每个会话到Redis
        for (var memory : userMemories) {
            syncMemoryFromMongoToRedis(memory.getMemoryId());
        }
    }
}