package com.demo.agent.config;

import com.demo.agent.sort.MongoChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {
    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;

    @Bean
    public ChatMemoryProvider agentChatMemoryProvider(){
        return memoryId ->
            MessageWindowChatMemory.builder()
                    .id(memoryId)
                    .maxMessages(50)
                    .chatMemoryStore(mongoChatMemoryStore)
                    .build();
    }
}
