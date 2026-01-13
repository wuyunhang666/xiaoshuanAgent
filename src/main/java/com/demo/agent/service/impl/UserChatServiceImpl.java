package com.demo.agent.service.impl;

import com.demo.agent.model.ChatMessages;
import com.demo.agent.service.UserChatService;
import com.demo.agent.sort.MixedChatMemoryStore;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserChatServiceImpl implements UserChatService {

    @Autowired
    private MixedChatMemoryStore mixedChatMemoryStore;

    @Override
    public List<ChatMessages> getUserChatSessions(Long userId) {
        return mixedChatMemoryStore.getChatMessagesByUserId(userId);
    }

    @Override
    public ChatMessages getUserChatSession(Long userId, String memoryId) {
        // 获取用户的所有会话
        List<ChatMessages> userSessions = getUserChatSessions(userId);
        
        // 查找匹配的会话
        for (ChatMessages session : userSessions) {
            if (memoryId.equals(session.getMemoryId())) {
                return session;
            }
        }
        
        return null; // 未找到匹配的会话
    }

    @Override
    public Map<String, Object> getUserAllConversationsDetail(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取用户的所有会话记录
        List<ChatMessages> userSessions = getUserChatSessions(userId);
        
        for (ChatMessages session : userSessions) {
            String memoryId = session.getMemoryId();
            
            // 解析聊天消息
            List<ChatMessage> messages = new ArrayList<>();
            if (session.getContent() != null) {
                messages = ChatMessageDeserializer.messagesFromJson(session.getContent());
            }
            
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("conversation", session);
            sessionInfo.put("messages", messages);
            result.put(memoryId, sessionInfo);
        }
        
        return result;
    }

    @Override
    public Boolean createUserSessions(String memoryId) {

        return mixedChatMemoryStore.createUserChat(memoryId);
    }

    @Override
    public void deleteUserSessions(String memoryId) {
        mixedChatMemoryStore.deleteMessages(memoryId);
    }
}