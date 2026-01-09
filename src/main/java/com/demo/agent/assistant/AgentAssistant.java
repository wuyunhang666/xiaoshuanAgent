package com.demo.agent.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

public interface AgentAssistant {

    @SystemMessage(fromResource = "AgentPrompt.txt")
    Flux<String> chat(@MemoryId Long memoryId, @UserMessage String userMessage);
}