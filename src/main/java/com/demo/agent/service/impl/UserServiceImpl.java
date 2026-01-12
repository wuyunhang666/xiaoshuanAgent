package com.demo.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.demo.agent.entity.User;
import com.demo.agent.mapper.UserMapper;
import com.demo.agent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean register(User user) {
        // 检查用户名是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", user.getUserName());
        User existingUser = userMapper.selectOne(wrapper);
        if (existingUser != null) {
            return false; // 用户名已存在
        }

        // 对密码进行加密（使用MD5）
        String encryptedPassword = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(encryptedPassword);

        // 插入新用户
        int result = userMapper.insert(user);
        return result > 0;
    }

    @Override
    public User login(String userName, String password) {
        // 对输入的密码进行加密
        String encryptedPassword = DigestUtils.md5DigestAsHex(password.getBytes());

        // 查询用户
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName);
        wrapper.eq("password", encryptedPassword);
        
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User findByUserName(String userName) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userName);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public String getLocation(Long id) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id);
        return userMapper.selectOne( wrapper).getAddress();
    }
}