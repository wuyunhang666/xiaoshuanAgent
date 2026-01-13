package com.demo.agent.controller;

import com.demo.agent.assistant.AgentAssistant;
import com.demo.agent.factory.AgentAssistantFactory;
import com.demo.agent.model.ChatForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "小双医疗助手")
@RestController
@RequestMapping("/xiaoshuang")
public class AgentController {
    @Autowired
    private AgentAssistant agentAssistant;

    @Operation(summary = "对话")
    @PostMapping(value = "/chat", produces = "text/stream;charset=utf-8")
    public Flux<String> chat(@RequestBody ChatForm chatForm){
        return agentAssistant.chat(chatForm.getMemoryId(),chatForm.getMessage());
    }
}
