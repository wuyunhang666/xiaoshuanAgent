package com.demo.agent.config;

import com.demo.agent.sort.MixedChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryChatConfig {
    
    @Autowired
    private MixedChatMemoryStore mixedChatMemoryStore;
    
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryStore(mixedChatMemoryStore) // 使用新的混合存储
                .build();
    }
}