package com.demo.agent;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        // ********** 关键调试：打印所有环境变量，确认api-key是否存在 **********
        String apiKey = System.getenv("api-key");
        System.out.println("读取到的api-key值：" + apiKey); // 先看这行输出是不是null
        // 可选：打印所有环境变量，排查是否配置错名称
        System.getenv().forEach((k, v) -> {
            if (k.contains("api")) { // 只打印包含api的环境变量，方便排查
                System.out.println("环境变量[" + k + "]=" + v);
            }
        });

        try {
            Generation gen = new Generation();
            List<Message> messages = new ArrayList<>();
            Message msg = Message.builder()
                    .role(Role.USER.getValue())
                    .content("你是谁")
                    .build();
            messages.add(msg);

            GenerationParam param = GenerationParam.builder()
                    .apiKey(apiKey)  // 这里用读取到的apiKey
                    .model("qwen-plus")
                    .messages(messages)
                    .build();

            GenerationResult result = gen.call(param);
            System.out.println(result);
            String content = result.getOutput().getChoices().get(0).getMessage().getContent();
            System.out.println("回答: " + content);

        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}