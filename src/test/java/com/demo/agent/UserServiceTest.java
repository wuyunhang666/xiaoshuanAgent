package com.demo.agent;

import com.demo.agent.AgentApplication;
import com.demo.agent.entity.User;
import com.demo.agent.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AgentApplication.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testRegisterAndLogin() {
        // 创建测试用户
        User user = new User();
        user.setUserName("testuser");
        user.setPassword("testpassword");
        user.setAddress("Test Address");

        // 注册用户
        boolean registerSuccess = userService.register(user);
        System.out.println("注册结果: " + registerSuccess);

        // 尝试登录
        User loginUser = userService.login("testuser", "testpassword");
        System.out.println("登录结果: " + (loginUser != null ? "成功" : "失败"));

        if (loginUser != null) {
            System.out.println("用户ID: " + loginUser.getId());
            System.out.println("用户名: " + loginUser.getUserName());
            System.out.println("地址: " + loginUser.getAddress());
        }
    }
}