package com.demo.agent.controller;

import com.demo.agent.entity.UserConversationMemory;
import com.demo.agent.model.ChatMessages;
import com.demo.agent.model.Result;
import com.demo.agent.service.UserChatService;
import com.demo.agent.service.UserConversationMemoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "用户聊天会话管理", description = "管理用户聊天会话")
@RestController
@RequestMapping("/user-chat")
public class UserChatController {

    @Autowired
    private UserChatService userChatService;
    
    @Autowired
    private UserConversationMemoryService userConversationMemoryService;

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

    @Operation(summary = "新增会话", description = "新增一个新的聊天会话")
    @PostMapping("/user/{userId}/create")
    public  Result<String> createUserSessions(@PathVariable Long userId, String memoryId){
        Boolean result = userChatService.createUserSessions(memoryId);
        if(result==false){
            log.error("新增会话失败！userId: {}",userId);
            return Result.error("新增会话失败！");
        }
        return Result.success("新增会话成功！");
    }

    @Operation(summary = "删除会话", description = "删除指定的聊天会话")
    @PostMapping("/user/{userId}/delete/{memoryId}")
    public  Result<String> deleteUserSessions(@PathVariable Long userId, @PathVariable String memoryId){
        try{
            userChatService.deleteUserSessions(memoryId);
            return Result.success("删除会话成功！");
        }catch (Exception e){
            log.error("删除会话失败：memoryId:{}",memoryId);
            return Result.error("删除失败！");
        }

    }
}