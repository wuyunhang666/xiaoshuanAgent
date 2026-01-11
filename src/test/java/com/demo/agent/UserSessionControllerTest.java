package com.demo.agent;

import com.demo.agent.controller.UserSessionController;
import com.demo.agent.entity.UserConversationMemory;
import com.demo.agent.model.Result;
import com.demo.agent.service.UserConversationMemoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionControllerTest {

    @Mock
    private UserConversationMemoryService userConversationMemoryService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserSessionController userSessionController;

    private UserConversationMemory sampleMemory;
    private List<UserConversationMemory> sampleMemories;
    private Map<String, Object> sampleDetailedConversations;

    @BeforeEach
    void setUp() {
        sampleMemory = new UserConversationMemory();
        sampleMemory.setId(1L);
        sampleMemory.setUserId(100L);
        sampleMemory.setMemoryId("memory-001");
        sampleMemory.setConversationTitle("测试会话");
        sampleMemory.setCreateTime(LocalDateTime.now());
        sampleMemory.setUpdateTime(LocalDateTime.now());
        sampleMemory.setIsDeleted(0);

        sampleMemories = Arrays.asList(sampleMemory);
        
        sampleDetailedConversations = new HashMap<>();
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("conversation", sampleMemory);
        sessionInfo.put("messages", Collections.emptyList());
        sampleDetailedConversations.put("memory-001", sessionInfo);
    }

    @Test
    void testGetAllUserConversationsDetail_UserLoggedIn_Success() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.getAllUserConversationsDetail(anyLong()))
                .thenReturn(sampleDetailedConversations);

        // Act
        Result<Map<String, Object>> result = userSessionController.getAllUserConversationsDetail(100L, request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertEquals(sampleDetailedConversations, result.getData());
        verify(userConversationMemoryService, times(1)).getAllUserConversationsDetail(100L);
    }

    @Test
    void testGetAllUserConversationsDetail_UserNotLoggedIn_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        Result<Map<String, Object>> result = userSessionController.getAllUserConversationsDetail(100L, request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("用户未登录", result.getMessage());
        assertNull(result.getData());
        verify(userConversationMemoryService, never()).getAllUserConversationsDetail(anyLong());
    }

    @Test
    void testGetAllUserConversationsDetail_AccessOtherUser_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L); // 当前用户ID是100

        // Act
        Result<Map<String, Object>> result = userSessionController.getAllUserConversationsDetail(200L, request); // 尝试访问用户200的数据

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("无权限访问其他用户的会话信息", result.getMessage());
        assertNull(result.getData());
        verify(userConversationMemoryService, never()).getAllUserConversationsDetail(anyLong());
    }

    @Test
    void testGetAllUserConversationsDetail_EmptyResult() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.getAllUserConversationsDetail(anyLong()))
                .thenReturn(new HashMap<>());

        // Act
        Result<Map<String, Object>> result = userSessionController.getAllUserConversationsDetail(100L, request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
        verify(userConversationMemoryService, times(1)).getAllUserConversationsDetail(100L);
    }

    @Test
    void testGetUserConversations_UserLoggedIn_Success() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.getUserMemories(anyLong())).thenReturn(sampleMemories);

        // Act
        Result<List<UserConversationMemory>> result = userSessionController.getUserConversations(request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertEquals(sampleMemories, result.getData());
        verify(userConversationMemoryService, times(1)).getUserMemories(100L);
    }

    @Test
    void testGetUserConversations_UserNotLoggedIn_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        Result<List<UserConversationMemory>> result = userSessionController.getUserConversations(request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("用户未登录", result.getMessage());
        assertNull(result.getData());
        verify(userConversationMemoryService, never()).getUserMemories(anyLong());
    }

    @Test
    void testGetUserConversations_EmptyResult() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.getUserMemories(anyLong())).thenReturn(Collections.emptyList());

        // Act
        Result<List<UserConversationMemory>> result = userSessionController.getUserConversations(request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
        verify(userConversationMemoryService, times(1)).getUserMemories(100L);
    }

    @Test
    void testGetUserConversation_UserLoggedIn_Success() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.getUserMemoryByUserIdAndMemoryId(anyLong(), anyString()))
                .thenReturn(sampleMemory);

        // Act
        Result<UserConversationMemory> result = userSessionController.getUserConversation("memory-001", request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertEquals(sampleMemory, result.getData());
        verify(userConversationMemoryService, times(1)).getUserMemoryByUserIdAndMemoryId(100L, "memory-001");
    }

    @Test
    void testGetUserConversation_UserNotLoggedIn_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        Result<UserConversationMemory> result = userSessionController.getUserConversation("memory-001", request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("用户未登录", result.getMessage());
        assertNull(result.getData());
        verify(userConversationMemoryService, never()).getUserMemoryByUserIdAndMemoryId(anyLong(), anyString());
    }

    @Test
    void testGetUserConversation_SessionNotFound() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.getUserMemoryByUserIdAndMemoryId(anyLong(), anyString()))
                .thenReturn(null);

        // Act
        Result<UserConversationMemory> result = userSessionController.getUserConversation("memory-001", request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("会话不存在", result.getMessage());
        assertNull(result.getData());
        verify(userConversationMemoryService, times(1)).getUserMemoryByUserIdAndMemoryId(100L, "memory-001");
    }

    @Test
    void testUpdateConversationTitle_UserLoggedIn_Success() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.updateConversationTitle(anyLong(), anyString()))
                .thenReturn(sampleMemory);

        // Act
        Result<UserConversationMemory> result = userSessionController.updateConversationTitle(1L, "新标题", request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertEquals(sampleMemory, result.getData());
        verify(userConversationMemoryService, times(1)).updateConversationTitle(1L, "新标题");
    }

    @Test
    void testUpdateConversationTitle_UserNotLoggedIn_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        Result<UserConversationMemory> result = userSessionController.updateConversationTitle(1L, "新标题", request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("用户未登录", result.getMessage());
        assertNull(result.getData());
        verify(userConversationMemoryService, never()).updateConversationTitle(anyLong(), anyString());
    }

    @Test
    void testUpdateConversationTitle_UpdateFailed() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.updateConversationTitle(anyLong(), anyString()))
                .thenReturn(null);

        // Act
        Result<UserConversationMemory> result = userSessionController.updateConversationTitle(1L, "新标题", request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("更新失败", result.getMessage());
        assertNull(result.getData());
        verify(userConversationMemoryService, times(1)).updateConversationTitle(1L, "新标题");
    }

    @Test
    void testDeleteConversation_UserLoggedIn_Success() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.deleteUserMemory(anyLong())).thenReturn(true);

        // Act
        Result<Boolean> result = userSessionController.deleteConversation(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertEquals(Boolean.TRUE, result.getData());
        verify(userConversationMemoryService, times(1)).deleteUserMemory(1L);
    }

    @Test
    void testDeleteConversation_UserNotLoggedIn_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        Result<Boolean> result = userSessionController.deleteConversation(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("用户未登录", result.getMessage());
        assertNull(result.getData());
        verify(userConversationMemoryService, never()).deleteUserMemory(anyLong());
    }

    @Test
    void testDeleteConversation_DeleteFailed() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userConversationMemoryService.deleteUserMemory(anyLong())).thenReturn(false);

        // Act
        Result<Boolean> result = userSessionController.deleteConversation(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("删除失败", result.getMessage());
        assertNull(result.getData());
        verify(userConversationMemoryService, times(1)).deleteUserMemory(1L);
    }
}