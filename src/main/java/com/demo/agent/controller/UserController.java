package com.demo.agent.controller;

import com.demo.agent.entity.User;
import com.demo.agent.model.Result;
import com.demo.agent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册和登录相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户")
    public Result<String> register(@RequestBody User user) {
        if (user.getUserName() == null || user.getUserName().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("密码不能为空");
        }

        boolean success = userService.register(user);
        if (success) {
            return Result.success("注册成功");
        } else {
            return Result.error("用户名已存在或注册失败");
        }
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录验证")
    public Result<User> login(@RequestParam String userName, @RequestParam String password) {
        if (userName == null || userName.isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        }

        User user = userService.login(userName, password);
        if (user != null) {
            // 登录成功，返回用户信息（不包含密码）
            User responseUser = new User();
            responseUser.setId(user.getId());
            responseUser.setUserName(user.getUserName());
            responseUser.setAddress(user.getAddress());
            return Result.success(responseUser);
        } else {
            return Result.error("用户名或密码错误");
        }
    }
}