package com.demo.agent.sort;

import com.demo.agent.async.AsyncMongoWriter;
import com.demo.agent.model.ChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private AsyncMongoWriter asyncMongoWriter;

    // 设置会话数据在Redis中的过期时间为24小时
    private static final long EXPIRATION_TIME = 24 * 60 * 60; // 24小时，单位秒

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = buildKey(memoryId.toString());
        Object value = redisTemplate.opsForValue().get(key);
        
        if (value == null) {
            return new LinkedList<>();
        }
        
        if (value instanceof String) {
            String content = (String) value;
            try {
                List<ChatMessage> messageList = ChatMessageDeserializer.messagesFromJson(content);
                return messageList != null ? messageList : new LinkedList<>();
            } catch (Exception e) {
                // 解析失败时返回空列表
                return new LinkedList<>();
            }
        }
        
        return new LinkedList<>();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 第一步：直接修改Redis中的会话数据（保证实时性）
        String key = buildKey(memoryId.toString());
        String messagesToJson = ChatMessageSerializer.messagesToJson(list);
        
        redisTemplate.opsForValue().set(key, messagesToJson, EXPIRATION_TIME, TimeUnit.SECONDS);
        
        // 第二步：解析memoryId获取userId（如果格式为userId:memoryId）
        Long userId = extractUserIdFromMemoryId(memoryId.toString());
        
        // 第三步：立即将"修改指令"提交到异步线程池，由异步任务完成MongoDB的同步
        asyncMongoWriter.updateMessagesInMongo(memoryId, list, userId);
        // 第四步：直接返回，无需等待MongoDB同步完成（用户无感知延迟）
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // 删除Redis中的数据
        String key = buildKey(memoryId.toString());
        redisTemplate.delete(key);
        
        // 异步删除MongoDB中的数据
        asyncMongoWriter.deleteMessagesInMongo(memoryId);
    }

    /**
     * 构建Redis存储的key
     */
    private String buildKey(String memoryId) {
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
}