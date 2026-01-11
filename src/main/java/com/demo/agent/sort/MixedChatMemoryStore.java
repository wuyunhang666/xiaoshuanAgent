package com.demo.agent.sort;

import com.demo.agent.async.AsyncMongoWriter;
import com.demo.agent.model.ChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 混合聊天记忆存储实现，采用实时同步方案（写Redis的同时异步写MongoDB）
 */
@Component
public class MixedChatMemoryStore implements ChatMemoryStore {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private AsyncMongoWriter asyncMongoWriter;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        // 优先从Redis获取（保证实时性）
        String redisKey = buildRedisKey(memoryId.toString());
        Object redisValue = redisTemplate.opsForValue().get(redisKey);
        
        if (redisValue != null) {
            // Redis中有数据，直接返回
            if (redisValue instanceof String) {
                String content = (String) redisValue;
                try {
                    List<ChatMessage> messageList = ChatMessageDeserializer.messagesFromJson(content);
                    return messageList != null ? messageList : new LinkedList<>();
                } catch (Exception e) {
                    // 解析失败时返回空列表
                    return new LinkedList<>();
                }
            }
        }
        
        // Redis中没有数据，从MongoDB获取
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);

        ChatMessages chatMessages = mongoTemplate.findOne(query, ChatMessages.class);
        if (chatMessages == null) {
            return new LinkedList<>();
        }
        
        String content = chatMessages.getContent();
        List<ChatMessage> messageList = ChatMessageDeserializer.messagesFromJson(content);
        
        // 将从MongoDB获取的数据写入Redis（并设置过期时间）
        if (messageList != null) {
            String messagesToJson = ChatMessageSerializer.messagesToJson(messageList);
            redisTemplate.opsForValue().set(redisKey, messagesToJson, 24 * 60 * 60, java.util.concurrent.TimeUnit.SECONDS);
        }
        
        return messageList != null ? messageList : new LinkedList<>();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 第一步：直接修改Redis中的会话数据（保证实时性）
        String messagesToJson = ChatMessageSerializer.messagesToJson(list);
        String redisKey = buildRedisKey(memoryId.toString());
        redisTemplate.opsForValue().set(redisKey, messagesToJson, 24 * 60 * 60, java.util.concurrent.TimeUnit.SECONDS);
        
        // 第二步：解析memoryId获取userId（如果格式为userId:memoryId）
        Long userId = extractUserIdFromMemoryId(memoryId.toString());
        
        // 第三步：立即将"修改指令"提交到异步线程池，由异步任务完成MongoDB的同步
        asyncMongoWriter.updateMessagesInMongo(memoryId, list, userId);
        // 第四步：直接返回，无需等待MongoDB同步完成（用户无感知延迟）
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // 删除Redis中的数据
        String redisKey = buildRedisKey(memoryId.toString());
        redisTemplate.delete(redisKey);
        
        // 异步删除MongoDB中的数据
        asyncMongoWriter.deleteMessagesInMongo(memoryId);
    }

    /**
     * 构建Redis存储的key
     */
    private String buildRedisKey(String memoryId) {
        return "chat:memory:" + memoryId;
    }
    
    /**
     * 从memoryId中提取userId（如果格式为userId:memoryId）
     */
    private Long extractUserIdFromMemoryId(String memoryIdStr) {
        if (memoryIdStr.contains(":")) {
            String[] parts = memoryIdStr.split(":", 2);
            if (parts.length >= 1) {
                try {
                    return Long.parseLong(parts[0]);
                } catch (NumberFormatException e) {
                    // 如果解析失败，则返回null
                    return null;
                }
            }
        }
        return null;
    }
    
    /**
     * 根据userId获取该用户的所有会话记录
     */
    public List<ChatMessages> getChatMessagesByUserId(Long userId) {
        // 直接从MongoDB查询，确保获取最新数据
        return mongoTemplate.find(
            new org.springframework.data.mongodb.core.query.Query(
                org.springframework.data.mongodb.core.query.Criteria.where("userId").is(userId)
            ), 
            ChatMessages.class
        );
    }
}