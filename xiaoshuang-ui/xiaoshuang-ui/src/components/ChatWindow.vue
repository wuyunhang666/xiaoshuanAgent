<template>
  <div class="app-layout">
    <div class="sidebar">
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="硅谷小智" width="160" height="160" />
        <span class="logo-text">硅谷小智（医疗版）</span>
      </div>
      <el-button class="new-chat-button" @click="newChat">
        <i class="fa-solid fa-plus"></i>
        &nbsp;新会话
      </el-button>
      
      <!-- 会话列表 -->
      <div class="sessions-list">
        <div
          v-for="session in sessions"
          :key="session.memoryId"
          :class="['session-item', { active: session.memoryId === memoryId }]"
          @click="switchSession(session.memoryId)"
        >
          <span class="session-name">会话 {{ session.memoryId.split(':')[1] }}</span>
          <i class="fa-solid fa-trash session-delete" @click.stop="deleteSession(session.memoryId)"></i>
        </div>
      </div>
    </div>
    <div class="main-content">
      <div class="chat-container">
        <div class="message-list" ref="messaggListRef">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="
              message.isUser ? 'message user-message' : 'message bot-message'
            "
          >
            <!-- 会话图标 -->
            <i
              :class="
                message.isUser
                  ? 'fa-solid fa-user message-icon'
                  : 'fa-solid fa-robot message-icon'
              "
            ></i>
            <!-- 会话内容 -->
            <span>
              <span v-html="message.content"></span>
              <!-- loading -->
              <span
                class="loading-dots"
                v-if="message.isTyping || message.isThinking"
              >
                <span class="dot"></span>
                <span class="dot"></span>
              </span>
            </span>
          </div>
        </div>
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            placeholder="请输入消息"
            @keyup.enter="sendMessage"
          ></el-input>
          <el-button @click="sendMessage" :disabled="isSending" type="primary"
            >发送</el-button
          >
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import axios from 'axios'

const messaggListRef = ref()
const isSending = ref(false)
const memoryId = ref()
const inputMessage = ref('')
const messages = ref([])
const userId = ref(localStorage.getItem('userId'))

// 监听localStorage中userId的变化
const checkUserId = () => {
  const currentUserId = localStorage.getItem('userId')
  if (currentUserId !== userId.value) {
    userId.value = currentUserId
    if (userId.value) {
      // userId更新后重新加载会话
      loadSessions()
    }
  }
}

// 定期检查userId是否更新
const userIdCheckInterval = setInterval(checkUserId, 1000)

// 组件卸载时清除定时器
onUnmounted(() => {
  clearInterval(userIdCheckInterval)
})
const sessions = ref([])
const sessionsDetail = ref({})

onMounted(() => {
  loadSessions()
  // 移除 setInterval，改用手动滚动
  watch(messages, () => scrollToBottom(), { deep: true })
})

const scrollToBottom = () => {
  if (messaggListRef.value) {
    messaggListRef.value.scrollTop = messaggListRef.value.scrollHeight
  }
}

// 生成新的memoryId
const generateMemoryId = () => {
  if (!userId.value) {
    console.error('用户未登录或userId不存在')
    return null
  }
  
  // 生成新的memoryId，格式为"userId:随机数"
  const randomNum = Math.floor(Math.random() * 1000000)
  return `${userId.value}:${randomNum}`
}

// 从后端加载会话列表
const loadSessions = () => {
  if (!userId.value) {
    console.error('用户未登录或userId不存在')
    return
  }
  
  // 获取用户所有会话
  axios.get(`/api/user-chat/user/${userId.value}/sessions`)
    .then(response => {
      if (response.data.success) {
        sessions.value = response.data.data
        
        // 如果有会话，默认选择第一个
        if (sessions.value.length > 0) {
          if (!memoryId.value) {
            memoryId.value = sessions.value[0].memoryId
            loadSessionDetail(memoryId.value)
          }
        } else {
          // 没有会话，创建一个新会话
          newChat()
        }
      } else {
        console.error('获取会话列表失败:', response.data.message)
        // 失败时创建一个新会话
        newChat()
      }
    })
    .catch(error => {
      console.error('获取会话列表错误:', error)
      // 错误时创建一个新会话
      newChat()
    })
}

// 从后端加载会话详情
const loadSessionDetail = (memoryIdValue) => {
  if (!userId.value || !memoryIdValue) {
    console.error('userId或memoryId不存在')
    return
  }
  
  // 获取会话详情
  axios.get(`/api/user-chat/user/${userId.value}/sessions`)
    .then(response => {
      if (response.data.success) {
        sessionsDetail.value = response.data.data
        
        // 加载当前会话的消息
        loadConversationFromDatabase(memoryIdValue)
      } else {
        console.error('获取会话详情失败:', response.data.message)
        messages.value = []
        hello() // 失败时发送问候
      }
    })
    .catch(error => {
      console.error('获取会话详情错误:', error)
      messages.value = []
      hello() // 错误时发送问候
    })
}

// 从数据库加载会话信息
const loadConversationFromDatabase = (currentMemoryId) => {
  if (!userId.value || !currentMemoryId) {
    console.error('userId或memoryId不存在')
    return
  }
  
  // 检查当前memoryId是否有对应的会话
  if (sessionsDetail.value[currentMemoryId]) {
    const sessionInfo = sessionsDetail.value[currentMemoryId]
    const sessionMessages = sessionInfo.messages
    
    // 如果有历史消息，加载到界面
    if (sessionMessages && sessionMessages.length > 0) {
      // 清空现有消息
      messages.value = []
      
      // 转换消息格式并加载到界面
      sessionMessages.forEach(msg => {
        if (msg.type === 'user' || msg.content?.text) {
          // 处理用户消息
          messages.value.push({
            isUser: true,
            content: msg.content?.text || msg.content || '',
            isTyping: false,
            isThinking: false
          })
        } else if (msg.type === 'ai' || msg.content?.text) {
          // 处理AI消息
          messages.value.push({
            isUser: false,
            content: msg.content?.text || msg.content || '',
            isTyping: false,
            isThinking: false
          })
        }
      })
    } else {
      // 没有历史消息，发送问候
      messages.value = []
      hello()
    }
  } else {
    // 没有对应的会话，发送问候
    messages.value = []
    hello()
  }
}

const hello = () => {
  sendRequest('你好')
}

const sendMessage = () => {
  if (inputMessage.value.trim()) {
    sendRequest(inputMessage.value.trim())
    inputMessage.value = ''
  }
}

const sendRequest = (message) => {
  if (!memoryId.value) {
    console.error('memoryId不存在')
    return
  }
  
  isSending.value = true
  const userMsg = {
    isUser: true,
    content: message,
    isTyping: false,
    isThinking: false,
  }
  //第一条默认发送的用户消息"你好"不放入会话列表
  if(messages.value.length > 0){
    messages.value.push(userMsg)
  }

  // 添加机器人加载消息
  const botMsg = {
    isUser: false,
    content: '', // 增量填充
    isTyping: true, // 显示加载动画
    isThinking: false,
  }
  messages.value.push(botMsg)
  const lastMsg = messages.value[messages.value.length - 1]
  scrollToBottom()

  axios
    .post(
      '/api/xiaoshuang/chat',
      { memoryId: memoryId.value, message },
      {
        responseType: 'stream', // 必须为合法值 "text"
        onDownloadProgress: (e) => {
          const fullText = e.event.target.responseText // 累积的完整文本
          let newText = fullText.substring(lastMsg.content.length)
          lastMsg.content += newText //增量更新
          console.log(lastMsg)
          scrollToBottom() // 实时滚动
        },
      }
    )
    .then(() => {
      // 流结束后隐藏加载动画
      messages.value.at(-1).isTyping = false
      isSending.value = false
    })
    .catch((error) => {
      console.error('流式错误:', error)
      messages.value.at(-1).content = '请求失败，请重试'
      messages.value.at(-1).isTyping = false
      isSending.value = false
    })
}

// 转换特殊字符
const convertStreamOutput = (output) => {
  return output
    .replace(/\n/g, '<br>')
    .replace(/\t/g, '&nbsp;&nbsp;&nbsp;&nbsp;')
    .replace(/&/g, '&amp;') // 新增转义，避免 HTML 注入
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
}

// 新增会话
const newChat = () => {
  // 生成新的memoryId
  const newMemoryId = generateMemoryId()
  
  // 调用后端新增会话接口
  axios.post(`/api/user-chat/user/${userId.value}/create?memoryId=${newMemoryId}`)
    .then(response => {
      if (response.data.success) {
        memoryId.value = newMemoryId
        // 重新加载会话列表
        loadSessions()
        // 清空消息列表
        messages.value = []
        // 发送问候消息
        hello()
      } else {
        console.error('创建会话失败:', response.data.message)
        // 失败时使用本地生成的memoryId
        memoryId.value = newMemoryId
        messages.value = []
        hello()
      }
    })
    .catch(error => {
      console.error('创建会话错误:', error)
      // 错误时使用本地生成的memoryId
      memoryId.value = newMemoryId
      messages.value = []
      hello()
    })
}

// 切换会话
const switchSession = (newMemoryId) => {
  memoryId.value = newMemoryId
  loadSessionDetail(newMemoryId)
}

// 删除会话
const deleteSession = (sessionMemoryId) => {
  // 调用后端删除会话接口
  axios.post(`/api/user-chat/user/${userId.value}/delete/${sessionMemoryId}`)
    .then(response => {
      if (response.data.success) {
        // 如果删除的是当前会话，则切换到第一个会话或创建新会话
        if (sessionMemoryId === memoryId.value) {
          if (sessions.value.length > 1) {
            // 切换到第一个会话
            const remainingSessions = sessions.value.filter(s => s.memoryId !== sessionMemoryId)
            if (remainingSessions.length > 0) {
              memoryId.value = remainingSessions[0].memoryId
              loadSessionDetail(memoryId.value)
            } else {
              // 没有其他会话，创建新会话
              newChat()
            }
          } else {
            // 没有其他会话，创建新会话
            newChat()
          }
        }
        // 重新加载会话列表
        loadSessions()
      } else {
        console.error('删除会话失败:', response.data.message)
      }
    })
    .catch(error => {
      console.error('删除会话错误:', error)
    })
}
</script>
<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 250px;
  background-color: #f4f4f9;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow-y: auto;
}

.sessions-list {
  width: 100%;
  margin-top: 20px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  margin-bottom: 5px;
  background-color: #e9ecef;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.session-item:hover {
  background-color: #dee2e6;
}

.session-item.active {
  background-color: #007bff;
  color: white;
}

.session-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-delete {
  margin-left: 10px;
  color: #dc3545;
  cursor: pointer;
  transition: color 0.3s;
}

.session-delete:hover {
  color: #bd2130;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  margin-top: 10px;
}

.new-chat-button {
  width: 100%;
  margin-top: 20px;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  background-color: #fff;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
}

.message {
  margin-bottom: 10px;
  padding: 10px;
  border-radius: 4px;
  display: flex;
  /* align-items: center; */
}

.user-message {
  max-width: 70%;
  background-color: #e1f5fe;
  align-self: flex-end;
  flex-direction: row-reverse;
}

.bot-message {
  max-width: 100%;
  background-color: #f1f8e9;
  align-self: flex-start;
}

.message-icon {
  margin: 0 10px;
  font-size: 1.2em;
}

.loading-dots {
  padding-left: 5px;
}

.dot {
  display: inline-block;
  margin-left: 5px;
  width: 8px;
  height: 8px;
  background-color: #000000;
  border-radius: 50%;
  animation: pulse 1.2s infinite ease-in-out both;
}

.dot:nth-child(2) {
  animation-delay: -0.6s;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(0.6);
    opacity: 0.4;
  }

  50% {
    transform: scale(1);
    opacity: 1;
  }
}
.input-container {
  display: flex;
}

.input-container .el-input {
  flex: 1;
  margin-right: 10px;
}

/* 媒体查询，当设备宽度小于等于 768px 时应用以下样式 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px 0 10px 0;
  }
  .app-layout {
    flex-direction: column;
  }

  .sidebar {
    /* display: none; */
    width: 100%;
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
    padding: 10px;
    height: auto;
  }
  
  .sessions-list {
    width: 100%;
    max-height: 200px;
    margin-top: 10px;
  }
  
  .session-item {
    padding: 8px;
    margin-bottom: 3px;
  }

  .logo-section {
    flex-direction: row;
    align-items: center;
  }

  .logo-text {
    font-size: 20px;
  }

  .logo-section img {
    width: 40px;
    height: 40px;
  }

  .new-chat-button {
    margin-right: 30px;
    width: auto;
    margin-top: 5px;
  }
}

/* 媒体查询，当设备宽度大于 768px 时应用原来的样式 */
@media (min-width: 769px) {
  .main-content {
    padding: 0 0 10px 10px;
  }

  .app-layout {
    display: flex;
    height: 100vh;
  }

  .sidebar {
    width: 250px;
    background-color: #f4f4f9;
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .logo-section {
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  .logo-text {
    font-size: 18px;
    font-weight: bold;
    margin-top: 10px;
  }

  .new-chat-button {
    width: 100%;
    margin-top: 20px;
  }
}
</style>