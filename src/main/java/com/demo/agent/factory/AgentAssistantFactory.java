package com.demo.agent.factory;

import com.demo.agent.assistant.AgentAssistant;
import com.demo.agent.tools.AppointmentTools;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentAssistantFactory {

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Autowired
    private ChatMemoryProvider agentChatMemoryProvider;

    @Autowired
    private AppointmentTools appointmentTools;

    @Autowired
    private ContentRetriever contentRetrieverPincone;

    @Autowired
    private McpToolProvider mcpToolProvider;
    @Bean
    public AgentAssistant agentAssistant() {
        AiServices<AgentAssistant> builder = AiServices.builder(AgentAssistant.class)
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
