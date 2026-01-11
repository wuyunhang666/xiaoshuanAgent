package com.demo.agent;

import com.demo.agent.model.ChatMessages;
import com.demo.agent.sort.MixedChatMemoryStore;
import com.demo.agent.sort.MongoChatMemoryStore;
import dev.langchain4j.data.message.ChatMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class RedisTest {
   @Resource
    private MixedChatMemoryStore mixedChatMemoryStore;

   @Resource
   private MongoChatMemoryStore mongoChatMemoryStore;
   @Test
    public void testRedis(){
       List<ChatMessage> messages = mixedChatMemoryStore.getMessages("3:102");
        System.out.println(messages);
   }
   @Test
    public void testMongo(){
      List<ChatMessages> list = mongoChatMemoryStore.getChatMessagesByUserId(3L);
       System.out.println(list);
   }
}
