package com.demo.agent.model;

import lombok.Data;

@Data
public class ChatForm {
    private Long userId;//用户Id
    private String memoryId;//对话id
    private String message;//用户问题
}
