package com.demo.agent.async;

import com.demo.agent.model.ChatMessages;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncMongoWriter {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Async
    public void updateMessagesInMongo(Object memoryId, java.util.List<ChatMessage> list, Long userId) {
        try {
            String messagesToJson = ChatMessageSerializer.messagesToJson(list);
            
            Criteria criteria = Criteria.where("memoryId").is(memoryId);
            Query query = new Query(criteria);
            Update update = new Update();
            update.set("content", messagesToJson);
            
            // 设置userId（如果提供）
            if (userId != null) {
                update.set("userId", userId);
            }
            
            mongoTemplate.upsert(query, update, ChatMessages.class);
        } catch (Exception e) {
            // 记录日志或处理异常
            e.printStackTrace();
        }
    }

    @Async
    public void updateMessagesInMongoWithoutUserId(Object memoryId, java.util.List<ChatMessage> list) {
        try {
            String messagesToJson = ChatMessageSerializer.messagesToJson(list);
            
            Criteria criteria = Criteria.where("memoryId").is(memoryId);
            Query query = new Query(criteria);
            Update update = new Update();
            update.set("content", messagesToJson);
            
            mongoTemplate.upsert(query, update, ChatMessages.class);
        } catch (Exception e) {
            // 记录日志或处理异常
            e.printStackTrace();
        }
    }

    @Async
    public void deleteMessagesInMongo(Object memoryId) {
        try {
            Criteria criteria = Criteria.where("memoryId").is(memoryId);
            Query query = new Query(criteria);
            mongoTemplate.remove(query, ChatMessages.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}