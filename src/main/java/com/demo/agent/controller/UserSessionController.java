package com.demo.agent.controller;

import com.demo.agent.entity.UserConversationMemory;
import com.demo.agent.model.Result;
import com.demo.agent.service.UserConversationMemoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "用户会话管理", description = "管理用户AI会话记忆")
@RestController
@RequestMapping("/user-session")
public class UserSessionController {

    @Autowired
    private UserConversationMemoryService userConversationMemoryService;

    @Operation(summary = "获取用户所有会话", description = "获取当前登录用户的所有AI会话记录")
    @GetMapping("/conversations")
    public Result<List<UserConversationMemory>> getUserConversations(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        List<UserConversationMemory> conversations = userConversationMemoryService.getUserMemories(userId);
        return Result.success(conversations);
    }

    @Operation(summary = "获取特定会话", description = "获取指定memoryId的会话记录")
    @GetMapping("/conversation/{memoryId}")
    public Result<UserConversationMemory> getUserConversation(
            @PathVariable String memoryId, 
            HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        UserConversationMemory conversation = userConversationMemoryService.getUserMemoryByUserIdAndMemoryId(userId, memoryId);
        if (conversation != null) {
            return Result.success(conversation);
        } else {
            return Result.error("会话不存在");
        }
    }

    @Operation(summary = "更新会话标题", description = "更新指定会话的标题")
    @PutMapping("/conversation/{id}/title")
    public Result<UserConversationMemory> updateConversationTitle(
            @PathVariable Long id,
            @RequestParam String title,
            HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        UserConversationMemory updatedMemory = userConversationMemoryService.updateConversationTitle(id, title);
        if (updatedMemory != null) {
            return Result.success(updatedMemory);
        } else {
            return Result.error("更新失败");
        }
    }

    @Operation(summary = "删除会话", description = "删除指定的会话记录")
    @DeleteMapping("/conversation/{id}")
    public Result<Boolean> deleteConversation(
            @PathVariable Long id,
            HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        boolean success = userConversationMemoryService.deleteUserMemory(id);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("删除失败");
        }
    }
    
    @Operation(summary = "创建新会话", description = "创建一个新的会话记录")
    @PostMapping("/conversation")
    public Result<UserConversationMemory> createNewConversation(
            @RequestParam String memoryId,
            @RequestParam(required = false, defaultValue = "未命名会话") String title,
            HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        
        if (userId == null) {
            return Result.error("用户未登录");
        }
        
        UserConversationMemory newMemory = userConversationMemoryService.createNewUserMemory(userId, memoryId, title);
        if (newMemory != null) {
            return Result.success(newMemory);
        } else {
            return Result.error("会话已存在或创建失败");
        }
    }
    
    @Operation(summary = "获取用户所有会话信息", description = "获取指定用户的所有会话详细信息")
    @GetMapping("/user/{userId}/conversations/detail")
    public Result<Map<String, Object>> getAllUserConversationsDetail(
            @PathVariable Long userId,
            HttpServletRequest request) {
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
        
        // 使用服务方法获取用户的所有会话详细信息
        Map<String, Object> result = userConversationMemoryService.getAllUserConversationsDetail(userId);

        return Result.success(result);
    }
}