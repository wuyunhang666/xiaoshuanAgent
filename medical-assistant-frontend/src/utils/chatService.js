import apiClient from './api'

export class ChatService {
  // 获取会话列表
  static async getSessions(userId) {
    try {
      const response = await apiClient.get(`/user/${userId}/sessions`)
      return response.data
    } catch (error) {
      console.error('获取会话列表失败:', error)
      // 如果API失败，从本地存储获取
      const storedSessions = localStorage.getItem('chatSessions')
      return storedSessions ? JSON.parse(storedSessions) : []
    }
  }

  // 创建新会话
  static async createSession(userId, sessionData) {
    try {
      const response = await apiClient.post(`/user/${userId}/sessions`, sessionData)
      return response.data
    } catch (error) {
      console.error('创建会话失败:', error)
      return null
    }
  }

  // 更新会话
  static async updateSession(userId, sessionId, sessionData) {
    try {
      const response = await apiClient.put(`/user/${userId}/sessions/${sessionId}`, sessionData)
      return response.data
    } catch (error) {
      console.error('更新会话失败:', error)
      return null
    }
  }

  // 删除会话
  static async deleteSession(userId, sessionId) {
    try {
      const response = await apiClient.delete(`/user/${userId}/sessions/${sessionId}`)
      return response.data
    } catch (error) {
      console.error('删除会话失败:', error)
      return null
    }
  }

  // 获取特定会话的消息
  static async getSessionMessages(userId, sessionId) {
    try {
      const response = await apiClient.get(`/user/${userId}/sessions/${sessionId}/messages`)
      return response.data
    } catch (error) {
      console.error('获取会话消息失败:', error)
      return []
    }
  }

  // 发送消息到AI
  static async sendMessage(messageData) {
    try {
      // 在实际应用中，这里会调用后端AI接口
      // 现在我们模拟一个延迟响应
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve({
            success: true,
            data: {
              id: 'ai_' + Date.now(),
              content: this.generateAIResponse(messageData.content),
              sender: 'ai',
              timestamp: new Date().toISOString()
            }
          })
        }, 1000)
      })
    } catch (error) {
      console.error('发送消息失败:', error)
      return {
        success: false,
        message: error.message
      }
    }
  }

  // 生成AI响应（模拟）
  static generateAIResponse(userMessage) {
    // 根据用户输入生成相关的回复
    if (userMessage.includes('挂号') || userMessage.includes('预约')) {
      return "关于挂号问题，您可以选择相应科室，我们会根据您的症状推荐合适的医生。您可以通过我们的系统进行预约。"
    } else if (userMessage.includes('症状') || userMessage.includes('不舒服')) {
      return "了解您的症状，建议您详细描述具体不适，以便更好地为您提供挂号建议。一般情况下，我们会根据症状推荐合适的科室。"
    } else if (userMessage.includes('医院') || userMessage.includes('科室')) {
      return "我们合作的医院有多科室可以为您提供服务，包括内科、外科、儿科等。您可以告诉我具体需求，我会为您推荐合适的科室。"
    } else if (userMessage.includes('感冒') || userMessage.includes('发烧') || userMessage.includes('咳嗽')) {
      return "感冒、发烧、咳嗽等症状通常需要看内科或呼吸科。如果症状严重或持续不退，请及时就医。"
    } else if (userMessage.includes('头疼') || userMessage.includes('头痛')) {
      return "头痛问题可能涉及神经内科，如果伴有其他症状，可能需要进一步检查。建议您尽快预约专科医生。"
    } else if (userMessage.includes('肚子') || userMessage.includes('腹痛')) {
      return "腹痛可能涉及消化内科或外科，请准确描述疼痛位置和性质，以便我们为您推荐合适的科室。"
    }
    
    // 默认响应
    const responses = [
      "您好，关于您提到的问题，建议您及时就医咨询专业医生。",
      "根据您的描述，这可能是一些常见症状，但确切诊断需要专业医师面诊。",
      "感谢您的咨询，我会尽力为您提供相关的医疗建议。",
      "针对您提到的症状，平时要注意休息，保持良好的生活习惯。",
      "如果您有持续不适，建议尽快预约相关科室的专业医生。",
      "了解您的情况，挂号方面我可以为您提供一些指导。",
      "对于这类问题，通常需要结合临床检查才能给出准确建议。",
      "请您详细描述症状，我会根据您的情况推荐合适的科室。",
      "我们有多个科室可以处理这类问题，我可以帮您了解挂号流程。"
    ]
    
    return responses[Math.floor(Math.random() * responses.length)]
  }
}