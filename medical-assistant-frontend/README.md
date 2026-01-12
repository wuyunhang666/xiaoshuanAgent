# 小双医疗助手前端

这是一个基于Vue3和Axios的小双医疗助手前端应用，帮助用户解答医院挂号问题。

## 功能特性

- 用户登录/注册功能
- 基于地理位置的注册验证
- 智能聊天机器人界面
- 会话管理（创建、删除、重命名）
- 用户消息和AI回复区分显示
- 响应式设计

## 技术栈

- Vue 3
- Vue Router
- Axios
- Vite
- JavaScript

## 项目结构

```
src/
├── assets/           # 静态资源
├── components/       # 公共组件
├── views/           # 页面视图
├── router/          # 路由配置
├── utils/           # 工具函数
├── App.vue          # 根组件
└── main.js          # 入口文件
```

## 安装和运行

1. 克隆项目
2. 安装依赖：
```bash
npm install
```
3. 启动开发服务器：
```bash
npm run dev
```
4. 访问 http://localhost:3000

## API 接口

- `/api/user/login` - 用户登录
- `/api/user/register` - 用户注册

## 代理配置

开发服务器配置了API代理，将 `/api` 请求代理到后端服务器（默认为 http://localhost:8080）。

## 本地存储

- `userToken` - 用户认证令牌
- `userId` - 用户ID
- `chatSessions` - 聊天会话数据

## 注意事项

- 生产环境中需要替换模拟的AI回复为实际的AI服务
- 需要配合后端API实现完整的用户认证和聊天功能
- 地理位置获取需要用户授权浏览器访问位置信息