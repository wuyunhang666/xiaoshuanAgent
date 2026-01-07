package com.demo.agent.assistant;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT,
          chatModel = "qwenChatModel",
          chatMemory = "chatMemory")

public interface MemoryChatAssistant {
    @UserMessage("你是我的好朋友，请你用比较古风的话回答我的问题，回答问题可以适当添加一些表情符号。{{m}}")
    String chat(@V("m")String userMessage);
}
