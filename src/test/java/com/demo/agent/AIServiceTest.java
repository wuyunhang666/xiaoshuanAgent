package com.demo.agent;

import com.demo.agent.assistant.Assistant;
import com.demo.agent.assistant.MemoryChatAssistant;
import com.demo.agent.assistant.SeparateChatAssistant;
import com.demo.agent.config.MemoryChatConfig;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
@Slf4j
@SpringBootTest
public class AIServiceTest {
    @Autowired
    private QwenChatModel qwenChatModel;
    @Test
    public void testQwen(){
        Assistant assistant = AiServices.create(Assistant.class, qwenChatModel);
        String answer = assistant.chat("你是谁");
        System.out.println(answer);
    }
    @Autowired
    private Assistant assistant;
    @Test
    public void testSpringBoot(){
        String answer = assistant.chat("你是谁");
        System.out.println(answer);
    }
    @Test
    public void testChatMemory2() {
//第一轮对话
        UserMessage userMessage1 = UserMessage.userMessage("我是环环");
        ChatResponse chatResponse1 = qwenChatModel.chat(userMessage1);
        AiMessage aiMessage1 = chatResponse1.aiMessage();
//输出大语言模型的回复
        System.out.println(aiMessage1.text());
//第二轮对话
        UserMessage userMessage2 = UserMessage.userMessage("你知道我是谁吗");
        ChatResponse chatResponse2 = qwenChatModel.chat(Arrays.asList(userMessage1,
                aiMessage1, userMessage2));
        AiMessage aiMessage2 = chatResponse2.aiMessage();
//输出大语言模型的回复
        System.out.println(aiMessage2.text());
    }
    @Test
    public void testChatMemory() {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        Assistant assistant1 = AiServices.builder(Assistant.class)
                .chatLanguageModel(qwenChatModel)
                .chatMemory(chatMemory)
                .build();
        String answer1 = assistant1.chat("我是张三");
        System.out.println(answer1);
        String answer2 = assistant1.chat("你知道我是谁吗");
        System.out.println(answer2);
    }
    @Autowired
    private MemoryChatAssistant memoryChatAssistant;
    @Test
    public void testChatMemory3() {
        System.out.println(memoryChatAssistant.chat("我是张三"));
        System.out.println(memoryChatAssistant.chat("你知道我是谁吗"));
    }
    @Autowired
    private SeparateChatAssistant separateChatAssistant;
    @Test
    public void testChatMemory4() {
        System.out.println(separateChatAssistant.chat(1,"我是张三"));
        System.out.println(separateChatAssistant.chat(1,"你知道我是谁吗"));
        System.out.println(separateChatAssistant.chat(2, "我是谁？"));
    }
}
