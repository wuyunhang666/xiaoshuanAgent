package com.demo.agent.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(
        wiringMode = EXPLICIT,
        chatModel = "qwenChatModel",
        chatMemoryProvider = "agentChatMemoryProvider"
)
public interface AgentAssistant {

    @SystemMessage(fromResource = "AgentPrompt.txt")
    String chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}
