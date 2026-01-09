package com.demo.agent.service;

import com.demo.agent.entity.User;

public interface UserService {
    /**
     * 用户注册
     */
    boolean register(User user);

    /**
     * 用户登录
     */
    User login(String userName, String password);

    /**
     * 根据用户名查询用户
     */
    User findByUserName(String userName);
}