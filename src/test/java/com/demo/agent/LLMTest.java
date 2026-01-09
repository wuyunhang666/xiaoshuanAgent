package com.demo.agent;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Component
public class LLMTest {
    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String key;
    @Test
    void LLM(){
        String apiKey = System.getenv("api-key");
        System.out.println("读取到的api-key值：" + apiKey);
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

//    @Autowired
//    private ChatLanguageModel chatLanguageModel;
//    @Test
//    public void testSpringBoot(){
//        String answer = chatLanguageModel.chat("我是谁");
//        System.out.println(answer);
//    }
    @Autowired
    private QwenChatModel qwenChatModel;
    @Test
    public void testQwen(){
        String answer = qwenChatModel.chat("你是谁");
        System.out.println(answer);
    }
    @Test
    public void testDashScopeWanx(){

        System.out.println("key : "+key);
        WanxImageModel wanxImageModel = WanxImageModel.builder()
                .modelName("wanx2.1-t2i-plus")
                .apiKey(key)
                .build();
        Response<Image> response = wanxImageModel.generate("奇幻森林精灵：在一片弥漫着轻柔薄雾的\n" +
                "古老森林深处，阳光透过茂密枝叶洒下金色光斑。一位身材娇小、长着透明薄翼的精灵少女站在一朵硕大的蘑菇上。她\n" +
                "有着海藻般的绿色长发，发间点缀着蓝色的小花，皮肤泛着珍珠般的微光。身上穿着由翠绿树叶和白色藤蔓编织而成的\n" +
                "连衣裙，手中捧着一颗散发着柔和光芒的水晶球，周围环绕着五彩斑斓的蝴蝶，脚下是铺满苔藓的地面，蘑菇和蕨类植\n" +
                "物丛生，营造出神秘而梦幻的氛围。");
                System.out.println(response.content().url());
    }
}
