package com.demo.agent;

import com.demo.agent.model.ChatMessages;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@SpringBootTest
public class MongoCRUDTest {
    @Resource
    private MongoTemplate mongoTemplate;

//    @Test
//    public void insertTest(){
//        mongoTemplate.insert(new ChatMessages(1L,"聊天记录"));
//    }
    @Test
    public void insertTest2(){
        ChatMessages messages = new ChatMessages();
        messages.setContent("5555");
        mongoTemplate.insert(messages);
    }
    @Test
    public void findTest(){
        ChatMessages messages = mongoTemplate.findById("695e178e533f010fe2546a30", ChatMessages.class);
        System.out.println(messages);
    }
    @Test
    public void updateTest(){
        Criteria criteria = Criteria.where("_id").is("100");
        Query query = new Query(criteria);
        Update update = new Update();
        update.set("content", "修改或新增后的内容");

        mongoTemplate.upsert(query, update, ChatMessages.class);
    }
    @Test
    public void deleteTest(){
        Criteria criteria = Criteria.where("_id").is("100");
        Query query = new Query(criteria);
        mongoTemplate.remove(query, ChatMessages.class);
    }

}
