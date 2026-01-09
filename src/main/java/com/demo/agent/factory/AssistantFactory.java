package com.demo.agent.factory;

import com.demo.agent.assistant.AgentAssistant;
import com.demo.agent.assistant.Assistant;
import com.demo.agent.tools.AppointmentTools;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AssistantFactory {
    @Resource
    private StreamingChatModel qwenStreamingChatModel;
    @Resource
    private ChatMemoryProvider agentChatMemoryProvider;
    @Resource
    private AppointmentTools appointmentTools;
    @Resource
    private ContentRetriever contentRetrieverPincone;
    @Resource
    private McpToolProvider mcpToolProvider;
    @Bean
    public Assistant assistant() {
        // 会话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        AiServices<Assistant> builder = AiServices.builder(Assistant.class)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemoryProvider(agentChatMemoryProvider)
                .tools(appointmentTools)
                .contentRetriever(contentRetrieverPincone);
        
        // 只有当mcpToolProvider不为null时才添加
        if (mcpToolProvider != null) {
            builder.toolProvider(mcpToolProvider);
        }
        
        return builder.build();
    }
}
