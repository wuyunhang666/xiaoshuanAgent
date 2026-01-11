package com.demo.agent;

import com.demo.agent.assistant.AgentAssistant;
import com.demo.agent.assistant.Assistant;
import com.demo.agent.assistant.SeparateChatAssistant;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
public class ToolsTest {
    @Autowired
    private SeparateChatAssistant separateChatAssistant;

    @Autowired
    private AgentAssistant agentAssistant;
    @Autowired
    private Assistant assistant;
    @Test
    public void testCalculatorTools() {
        String answer = separateChatAssistant
                .chat(100, "1+2等于几，475695037565的平方根是多少？");
            //答案：3，689706.4865
        System.out.println(answer);
    }
    @Test
    public void testAppointmentTools() {
        StringBuilder result = new StringBuilder();
        agentAssistant.chat("12:20", "请你查询一下成都明天的天气")
                .doOnNext(result::append)
                .doOnComplete(() -> {
                    System.out.println("AI助手回复: " + result.toString());
                })
                .blockLast(); // 等待流完成
    }
    @Test
    public void testContentRetriever() {
        StringBuilder result = new StringBuilder();
        agentAssistant.chat("13:20", "请你查询一下成都明天的天气？")
                .doOnNext(result::append)
                .doOnComplete(() -> {
                    System.out.println("AI助手回复: " + result.toString());
                })
                .blockLast(); // 等待流完成
    }
}
