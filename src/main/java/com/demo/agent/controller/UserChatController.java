package com.demo.agent.controller;

import com.demo.agent.model.ChatMessages;
import com.demo.agent.model.Result;
import com.demo.agent.service.UserChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "用户聊天会话管理", description = "管理用户聊天会话")
@RestController
@RequestMapping("/user-chat")
public class UserChatController {

    @Autowired
    private UserChatService userChatService;

    @Operation(summary = "获取用户所有会话", description = "获取指定用户的所有聊天会话记录")
    @GetMapping("/user/{userId}/sessions")
    public Result<List<ChatMessages>> getUserSessions(@PathVariable Long userId, HttpServletRequest request) {
        // 验证当前登录用户是否有权限访问
        HttpSession session = request.getSession();
        Long currentUserId = (Long) session.getAttribute("userId");
        
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        
        // 只允许用户访问自己的会话信息
        if (!currentUserId.equals(userId)) {
            return Result.error("无权限访问其他用户的会话信息");
        }
        
        List<ChatMessages> sessions = userChatService.getUserChatSessions(userId);
        return Result.success(sessions);
    }

    @Operation(summary = "获取用户会话详情", description = "获取指定用户的所有会话详细信息")
    @GetMapping("/user/{userId}/sessions/detail")
    public Result<Map<String, Object>> getUserAllConversationsDetail(@PathVariable Long userId, HttpServletRequest request) {
        // 验证当前登录用户是否有权限访问
        HttpSession session = request.getSession();
        Long currentUserId = (Long) session.getAttribute("userId");
        
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }
        
        // 只允许用户访问自己的会话信息
        if (!currentUserId.equals(userId)) {
            return Result.error("无权限访问其他用户的会话信息");
        }
        
        Map<String, Object> sessionsDetail = userChatService.getUserAllConversationsDetail(userId);
        return Result.success(sessionsDetail);
    }
}