package com.demo.agent;

import com.demo.agent.controller.UserChatController;
import com.demo.agent.model.ChatMessages;
import com.demo.agent.model.Result;
import com.demo.agent.service.UserChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserChatControllerTest {

    @Mock
    private UserChatService userChatService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserChatController userChatController;

    private ChatMessages sampleChatMessage;
    private List<ChatMessages> sampleChatMessages;

    @BeforeEach
    void setUp() {
        sampleChatMessage = new ChatMessages();
        sampleChatMessage.setMessageId(new ObjectId());
        sampleChatMessage.setMemoryId("memory-001");
        sampleChatMessage.setContent("test content");
        sampleChatMessage.setUserId(100L);

        sampleChatMessages = Arrays.asList(sampleChatMessage);
    }

    @Test
    void testGetUserSessions_UserLoggedIn_Success() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userChatService.getUserChatSessions(anyLong())).thenReturn(sampleChatMessages);

        // Act
        Result<List<ChatMessages>> result = userChatController.getUserSessions(100L, request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertEquals(sampleChatMessages, result.getData());
        verify(userChatService, times(1)).getUserChatSessions(100L);
    }

    @Test
    void testGetUserSessions_UserNotLoggedIn_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        Result<List<ChatMessages>> result = userChatController.getUserSessions(100L, request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("用户未登录", result.getMessage());
        assertNull(result.getData());
        verify(userChatService, never()).getUserChatSessions(anyLong());
    }

    @Test
    void testGetUserSessions_AccessOtherUser_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L); // 当前用户ID是100

        // Act
        Result<List<ChatMessages>> result = userChatController.getUserSessions(200L, request); // 尝试访问用户200的数据

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("无权限访问其他用户的会话信息", result.getMessage());
        assertNull(result.getData());
        verify(userChatService, never()).getUserChatSessions(anyLong());
    }

    @Test
    void testGetUserSessions_EmptyResult() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userChatService.getUserChatSessions(anyLong())).thenReturn(Collections.emptyList());

        // Act
        Result<List<ChatMessages>> result = userChatController.getUserSessions(100L, request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertTrue(result.getData().isEmpty());
        verify(userChatService, times(1)).getUserChatSessions(100L);
    }

    @Test
    void testGetUserAllConversationsDetail_UserLoggedIn_Success() {
        // Arrange
        Map<String, Object> sampleDetailResult = new HashMap<>();
        sampleDetailResult.put("memory-001", "test detail");
        
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L);
        when(userChatService.getUserAllConversationsDetail(anyLong())).thenReturn(sampleDetailResult);

        // Act
        Result<Map<String, Object>> result = userChatController.getUserAllConversationsDetail(100L, request);

        // Assert
        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertTrue(result.isSuccess());
        assertEquals(sampleDetailResult, result.getData());
        verify(userChatService, times(1)).getUserAllConversationsDetail(100L);
    }

    @Test
    void testGetUserAllConversationsDetail_UserNotLoggedIn_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(null);

        // Act
        Result<Map<String, Object>> result = userChatController.getUserAllConversationsDetail(100L, request);

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("用户未登录", result.getMessage());
        assertNull(result.getData());
        verify(userChatService, never()).getUserAllConversationsDetail(anyLong());
    }

    @Test
    void testGetUserAllConversationsDetail_AccessOtherUser_Error() {
        // Arrange
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("userId")).thenReturn(100L); // 当前用户ID是100

        // Act
        Result<Map<String, Object>> result = userChatController.getUserAllConversationsDetail(200L, request); // 尝试访问用户200的数据

        // Assert
        assertNotNull(result);
        assertEquals(500, result.getCode());
        assertFalse(result.isSuccess());
        assertEquals("无权限访问其他用户的会话信息", result.getMessage());
        assertNull(result.getData());
        verify(userChatService, never()).getUserAllConversationsDetail(anyLong());
    }
}