package com.demo.agent.config;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class McpConfig {
    private static final Logger logger = LoggerFactory.getLogger(McpConfig.class);

    @Value("${bigmodel.api-key:#{null}}")
    private String apiKey;

    @Bean("mcpToolProvider")
    public McpToolProvider mcpToolProvider() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.warn("MCP配置警告：未设置API密钥，MCP功能将不可用。请在配置文件中设置bigmodel.api-key或环境变量BIGMODEL_KEY");
            // 返回一个空的McpToolProvider，避免应用启动失败
            return McpToolProvider.builder().build();
        }

        try {
            // 配置MCP传输层，连接到智谱AI的MCP服务
            HttpMcpTransport transport = new HttpMcpTransport.Builder()
                    .sseUrl("https://open.bigmodel.cn/api/mcp/web_search/sse?Authorization=" + apiKey)
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            // 创建MCP客户端
            McpClient mcpClient = new DefaultMcpClient.Builder()
                    .key("xiaoshuangMcp")
                    .transport(transport)
                    .build();

            // 创建MCP工具提供者
            McpToolProvider toolProvider = McpToolProvider.builder()
                    .mcpClients(mcpClient)
                    .build();

            logger.info("MCP工具提供者已成功初始化");
            return toolProvider;
        } catch (Exception e) {
            logger.error("初始化MCP工具提供者失败: {}", e.getMessage());
            logger.debug("详细错误信息:", e);
            // 如果发生错误，返回一个空的McpToolProvider
            return McpToolProvider.builder().build();
        }
    }
}