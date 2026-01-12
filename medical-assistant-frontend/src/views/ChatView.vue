<template>
  <div class="chat-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>会话列表</h3>
        <button class="btn btn-new-session" @click="newSession">+ 新建会话</button>
      </div>
      <div class="session-list">
        <div 
          v-for="session in sessions" 
          :key="session.id"
          :class="['session-item', { active: session.id === currentSessionId }]"
          @click="switchSession(session.id)"
        >
          <div class="session-title">{{ session.title }}</div>
          <div class="session-actions">
            <span @click.stop="renameSession(session)" title="重命名">✏️</span>
            <span @click.stop="deleteSession(session.id)" title="删除">🗑️</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div class="chat-area">
      <div class="chat-header">
        <h3>{{ currentSessionTitle }}</h3>
      </div>
      
      <!-- 聊天消息容器 -->
      <div class="messages-container" ref="messagesContainer">
        <div 
          v-for="message in currentMessages" 
          :key="message.id"
          :class="['message', message.sender]"
        >
          <div class="message-avatar">
            {{ message.sender === 'user' ? 'U' : 'AI' }}
          </div>
          <div class="message-content">
            <div class="message-text">{{ message.content }}</div>
            <div class="message-time">{{ formatDate(message.timestamp) }}</div>
          </div>
        </div>
      </div>
      
      <!-- 输入区域 -->
      <div class="input-container">
        <textarea
          v-model="inputMessage"
          @keydown.enter.exact.prevent="sendMessage"
          placeholder="输入消息..."
          rows="3"
        ></textarea>
        <button class="send-btn" @click="sendMessage" :disabled="!inputMessage.trim()">发送</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ChatService } from '../utils/chatService'

export default {
  name: 'ChatView',
  data() {
    return {
      sessions: [],
      currentSessionId: null,
      currentSessionTitle: '新建会话',
      currentMessages: [],
      inputMessage: '',
      userId: null
    }
  },
  async created() {
    // 获取用户ID
    this.userId = localStorage.getItem('userId') || Math.floor(Math.random() * 10000)
    
    // 初始化会话
    await this.loadSessions()
    if (this.sessions.length === 0) {
      await this.newSession()
    } else {
      // 加载第一个会话
      this.switchSession(this.sessions[0].id)
    }
  },
  mounted() {
    this.scrollToBottom()
  },
  updated() {
    this.scrollToBottom()
  },
  methods: {
    // 格式化时间
    formatDate(timestamp) {
      const date = new Date(timestamp)
      return date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    },
    
    // 滚动到底部
    scrollToBottom() {
      this.$nextTick(() => {
        if (this.$refs.messagesContainer) {
          this.$refs.messagesContainer.scrollTop = this.$refs.messagesContainer.scrollHeight
        }
      })
    },
    
    // 加载会话列表
    async loadSessions() {
      try {
        this.sessions = await ChatService.getSessions(this.userId)
      } catch (error) {
        console.error('加载会话失败:', error)
        this.sessions = []
      }
    },
    
    // 切换会话
    async switchSession(sessionId) {
      this.currentSessionId = sessionId
      
      // 查找当前会话
      const session = this.sessions.find(s => s.id === sessionId)
      if (session) {
        this.currentSessionTitle = session.title
        this.currentMessages = session.messages || []
      }
      
      // 在实际应用中，这里应该从后端API加载特定会话的消息
    },
    
    // 新建会话
    async newSession() {
      const sessionId = 'session_' + Date.now()
      const newSession = {
        id: sessionId,
        title: '新建会话',
        messages: [],
        createdAt: new Date().toISOString()
      }
      
      this.sessions.unshift(newSession)
      await this.saveSessions()
      await this.switchSession(sessionId)
    },
    
    // 删除会话
    async deleteSession(sessionId) {
      if (confirm('确定要删除这个会话吗？')) {
        this.sessions = this.sessions.filter(s => s.id !== sessionId)
        
        if (this.currentSessionId === sessionId) {
          if (this.sessions.length > 0) {
            await this.switchSession(this.sessions[0].id)
          } else {
            await this.newSession()
          }
        }
        
        await this.saveSessions()
      }
    },
    
    // 重命名会话
    async renameSession(session) {
      const newTitle = prompt('请输入新的会话名称:', session.title)
      if (newTitle && newTitle.trim()) {
        session.title = newTitle.trim()
        await this.saveSessions()
        
        if (session.id === this.currentSessionId) {
          this.currentSessionTitle = session.title
        }
      }
    },
    
    // 保存会话到本地存储
    async saveSessions() {
      try {
        localStorage.setItem('chatSessions', JSON.stringify(this.sessions))
      } catch (error) {
        console.error('保存会话失败:', error)
      }
    },
    
    // 发送消息
    async sendMessage() {
      if (!this.inputMessage.trim()) return
      
      const userMessage = {
        id: 'msg_' + Date.now(),
        sender: 'user',
        content: this.inputMessage,
        timestamp: new Date().toISOString()
      }
      
      // 添加用户消息到当前会话
      this.currentMessages.push(userMessage)
      
      // 清空输入框
      const messageToSend = this.inputMessage
      this.inputMessage = ''
      
      // 更新会话标题（如果还是默认标题）
      if (this.currentSessionTitle === '新建会话') {
        const title = messageToSend.length > 20 ? messageToSend.substring(0, 20) + '...' : messageToSend
        this.currentSessionTitle = title
        const currentSession = this.sessions.find(s => s.id === this.currentSessionId)
        if (currentSession) {
          currentSession.title = title
          await this.saveSessions()
        }
      }
      
      // 更新当前会话的消息
      const currentSession = this.sessions.find(s => s.id === this.currentSessionId)
      if (currentSession) {
        currentSession.messages = [...this.currentMessages]
        await this.saveSessions()
      }
      
      try {
        // 发送消息到AI服务
        const response = await ChatService.sendMessage({
          sessionId: this.currentSessionId,
          userId: this.userId,
          content: messageToSend,
          timestamp: new Date().toISOString()
        })
        
        if (response.success) {
          this.currentMessages.push(response.data)
          
          // 更新会话中的消息
          const currentSession = this.sessions.find(s => s.id === this.currentSessionId)
          if (currentSession) {
            currentSession.messages = [...this.currentMessages]
            await this.saveSessions()
          }
        } else {
          // 如果AI服务失败，使用本地模拟响应
          const aiResponse = {
            id: 'msg_' + Date.now(),
            sender: 'ai',
            content: ChatService.generateAIResponse(messageToSend),
            timestamp: new Date().toISOString()
          }
          
          this.currentMessages.push(aiResponse)
          
          // 更新会话中的消息
          const currentSession = this.sessions.find(s => s.id === this.currentSessionId)
          if (currentSession) {
            currentSession.messages = [...this.currentMessages]
            await this.saveSessions()
          }
        }
      } catch (error) {
        console.error('发送消息失败:', error)
        
        // 使用本地模拟响应
        const aiResponse = {
          id: 'msg_' + Date.now(),
          sender: 'ai',
          content: ChatService.generateAIResponse(messageToSend),
          timestamp: new Date().toISOString()
        }
        
        this.currentMessages.push(aiResponse)
        
        // 更新会话中的消息
        const currentSession = this.sessions.find(s => s.id === this.currentSessionId)
        if (currentSession) {
          currentSession.messages = [...this.currentMessages]
          await this.saveSessions()
        }
      }
    },
    

  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}

.sidebar {
  width: 280px;
  background-color: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 1rem;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.btn-new-session {
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0.4rem 0.8rem;
  cursor: pointer;
  font-size: 0.9rem;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.session-item {
  padding: 0.8rem;
  border-radius: 5px;
  margin-bottom: 0.5rem;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background-color 0.2s;
}

.session-item:hover {
  background-color: #f0f0f0;
}

.session-item.active {
  background-color: #e6f0ff;
  color: #667eea;
}

.session-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-actions {
  display: none;
}

.session-item:hover .session-actions {
  display: flex;
}

.session-actions span {
  margin-left: 0.5rem;
  cursor: pointer;
  padding: 0.2rem;
  border-radius: 2px;
}

.session-actions span:hover {
  background-color: #e0e0e0;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 1rem;
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
}

.chat-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.messages-container {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  background-color: #fafafa;
}

.message {
  display: flex;
  margin-bottom: 1rem;
  max-width: 80%;
}

.message.user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.message.ai {
  margin-right: auto;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.9rem;
  flex-shrink: 0;
  margin: 0 0.5rem;
}

.message.user .message-avatar {
  background-color: #667eea;
  margin-left: 0.5rem;
  margin-right: 0;
}

.message.ai .message-avatar {
  background-color: #764ba2;
  margin-right: 0.5rem;
  margin-left: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
}

.message-text {
  padding: 0.8rem;
  border-radius: 10px;
  margin-bottom: 0.2rem;
  word-wrap: break-word;
  line-height: 1.4;
}

.message.user .message-text {
  background-color: #667eea;
  color: white;
  border-top-right-radius: 0;
}

.message.ai .message-text {
  background-color: white;
  border: 1px solid #e0e0e0;
  border-top-left-radius: 0;
}

.message-time {
  font-size: 0.75rem;
  color: #999;
  align-self: flex-end;
}

.input-container {
  padding: 1rem;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
}

.input-container textarea {
  flex: 1;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 5px;
  resize: vertical;
  min-height: 60px;
  max-height: 120px;
  font-family: inherit;
}

.send-btn {
  margin-left: 0.8rem;
  padding: 0.8rem 1.5rem;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  align-self: flex-end;
}

.send-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.send-btn:not(:disabled):hover {
  background-color: #5a6fd8;
}
</style>