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
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class MongoChatMemoryStore implements ChatMemoryStore {
    @Resource
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private AsyncMongoWriter asyncMongoWriter;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Criteria criteria = Criteria.where("memoryId").is(memoryId);
        Query query = new Query(criteria);

        ChatMessages chatMessages = mongoTemplate.findOne(query, ChatMessages.class);
        if(chatMessages == null){
            return new LinkedList<>();
        }
        String content = chatMessages.getContent();
        List<ChatMessage> messageList = ChatMessageDeserializer.messagesFromJson(content);
        return messageList;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        // 解析memoryId获取userId（如果格式为userId:memoryId）
        Long userId = extractUserIdFromMemoryId(memoryId.toString());
        
        // 异步更新MongoDB
        asyncMongoWriter.updateMessagesInMongo(memoryId, list, userId);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // 异步删除MongoDB中的数据
        asyncMongoWriter.deleteMessagesInMongo(memoryId);
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
        Criteria criteria = Criteria.where("userId").is(userId);
        Query query = new Query(criteria);
        return mongoTemplate.find(query, ChatMessages.class);
    }
}