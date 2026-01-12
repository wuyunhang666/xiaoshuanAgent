# API 接口文档

## 认证接口

### 用户登录
- **POST** `/api/user/login`
- 请求体:
```json
{
  "username": "string",
  "password": "string"
}
```
- 响应:
```json
{
  "success": true,
  "token": "string",
  "userId": "string",
  "message": "登录成功"
}
```

### 用户注册
- **POST** `/api/user/register`
- 请求体:
```json
{
  "username": "string",
  "password": "string",
  "location": "string"
}
```
- 响应:
```json
{
  "success": true,
  "message": "注册成功"
}
```

## 会话接口

### 获取用户会话列表
- **GET** `/api/user/{userId}/sessions`
- 请求头: `Authorization: Bearer {token}`
- 响应:
```json
[
  {
    "id": "session_id",
    "title": "会话标题",
    "createdAt": "2023-01-01T00:00:00Z",
    "messages": []
  }
]
```

### 创建会话
- **POST** `/api/user/{userId}/sessions`
- 请求头: `Authorization: Bearer {token}`
- 请求体:
```json
{
  "title": "会话标题",
  "createdAt": "2023-01-01T00:00:00Z"
}
```
- 响应:
```json
{
  "id": "session_id",
  "title": "会话标题",
  "createdAt": "2023-01-01T00:00:00Z",
  "userId": "user_id"
}
```

### 更新会话
- **PUT** `/api/user/{userId}/sessions/{sessionId}`
- 请求头: `Authorization: Bearer {token}`
- 请求体:
```json
{
  "title": "新会话标题"
}
```

### 删除会话
- **DELETE** `/api/user/{userId}/sessions/{sessionId}`
- 请求头: `Authorization: Bearer {token}`

### 获取特定会话消息
- **GET** `/api/user/{userId}/sessions/{sessionId}/messages`
- 请求头: `Authorization: Bearer {token}`
- 响应:
```json
[
  {
    "id": "message_id",
    "sender": "user|ai",
    "content": "消息内容",
    "timestamp": "2023-01-01T00:00:00Z"
  }
]
```

## 消息接口

### 发送消息到AI
- **POST** `/api/chat/send`
- 请求头: `Authorization: Bearer {token}`
- 请求体:
```json
{
  "sessionId": "session_id",
  "userId": "user_id",
  "message": "用户消息内容"
}
```
- 响应:
```json
{
  "success": true,
  "data": {
    "id": "ai_message_id",
    "sender": "ai",
    "content": "AI回复内容",
    "timestamp": "2023-01-01T00:00:00Z"
  }
}
```

## 错误响应格式

所有错误响应都遵循以下格式:
```json
{
  "success": false,
  "message": "错误信息",
  "code": "错误代码"
}
```

## 认证

所有需要认证的接口都需要在请求头中包含:
```
Authorization: Bearer {token}
```

## 本地存储

前端使用以下 localStorage 键来存储数据:
- `userToken`: 用户认证令牌
- `userId`: 用户唯一标识
- `chatSessions`: 本地会话数据（当后端API不可用时的备用方案）

## 代理配置

开发环境中，所有 `/api` 请求都会被代理到后端服务（默认为 http://localhost:8080）。