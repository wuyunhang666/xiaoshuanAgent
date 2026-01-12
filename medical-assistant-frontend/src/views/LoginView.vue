<template>
  <div class="login-container">
    <div class="login-form">
      <h2>小双医疗助手</h2>
      <div v-if="!showRegister" class="login-section">
        <h3>用户登录</h3>
        <form @submit.prevent="handleLogin">
          <div class="input-group">
            <label for="username">用户名</label>
            <input 
              type="text" 
              id="username" 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              required
            >
          </div>
          <div class="input-group">
            <label for="password">密码</label>
            <input 
              type="password" 
              id="password" 
              v-model="loginForm.password" 
              placeholder="请输入密码" 
              required
            >
          </div>
          <button type="submit" class="btn btn-primary">登录</button>
          <p class="switch-form">
            还没有账号？<a href="#" @click="showRegister = true">立即注册</a>
          </p>
        </form>
      </div>
      
      <div v-else class="register-section">
        <h3>用户注册</h3>
        <form @submit.prevent="handleRegister">
          <div class="input-group">
            <label for="reg-username">用户名</label>
            <input 
              type="text" 
              id="reg-username" 
              v-model="registerForm.username" 
              placeholder="请输入用户名" 
              required
            >
          </div>
          <div class="input-group">
            <label for="reg-password">密码</label>
            <input 
              type="password" 
              id="reg-password" 
              v-model="registerForm.password" 
              placeholder="请输入密码" 
              required
            >
          </div>
          <div class="input-group">
            <label for="location">位置信息</label>
            <input 
              type="text" 
              id="location" 
              v-model="registerForm.location" 
              placeholder="正在获取您的位置..." 
              readonly
            >
            <button type="button" class="btn btn-secondary" @click="getLocation">获取位置</button>
          </div>
          <button type="submit" class="btn btn-primary">注册</button>
          <p class="switch-form">
            已有账号？<a href="#" @click="showRegister = false">立即登录</a>
          </p>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import apiClient from '../utils/api'

export default {
  name: 'LoginView',
  data() {
    return {
      showRegister: false,
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        password: '',
        location: ''
      }
    }
  },
  methods: {
    async handleLogin() {
      try {
        const response = await apiClient.post('/user/login', this.loginForm)
        if (response.success) {
          // 存储用户信息和token
          localStorage.setItem('userToken', response.token)
          localStorage.setItem('userId', response.userId)
          
          // 跳转到聊天页面
          this.$router.push('/chat')
        } else {
          alert(response.message || '登录失败')
        }
      } catch (error) {
        console.error('登录错误:', error)
        alert('登录失败，请检查用户名和密码')
      }
    },
    async handleRegister() {
      if (!this.registerForm.location) {
        alert('请先获取位置信息')
        return
      }
      
      const userData = {
        username: this.registerForm.username,
        password: this.registerForm.password,
        location: this.registerForm.location
      }
      
      try {
        const response = await apiClient.post('/user/register', userData)
        if (response.success) {
          alert('注册成功，请登录')
          this.showRegister = false
          this.registerForm = {
            username: '',
            password: '',
            location: ''
          }
        } else {
          alert(response.message || '注册失败')
        }
      } catch (error) {
        console.error('注册错误:', error)
        
        // 如果API不可用，则将用户保存到本地存储
        const users = JSON.parse(localStorage.getItem('registeredUsers') || '[]')
        const existingUser = users.find(u => u.username === userData.username)
        
        if (existingUser) {
          alert('用户名已存在')
          return
        }
        
        users.push(userData)
        localStorage.setItem('registeredUsers', JSON.stringify(users))
        alert('注册成功，请登录')
        this.showRegister = false
        this.registerForm = {
          username: '',
          password: '',
          location: ''
        }
      }
    },
    getLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const lat = position.coords.latitude
            const lng = position.coords.longitude
            // 这里可以调用后端API获取具体位置信息
            this.registerForm.location = `纬度: ${lat}, 经度: ${lng}`
          },
          (error) => {
            console.error('获取位置失败:', error)
            alert('无法获取您的位置信息，请确保浏览器允许位置访问')
          }
        )
      } else {
        alert('您的浏览器不支持地理位置获取')
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  background: white;
  padding: 2rem;
  border-radius: 10px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-form h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.input-group {
  margin-bottom: 1rem;
}

.input-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: bold;
}

.input-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 1rem;
  box-sizing: border-box;
}

.btn {
  width: 100%;
  padding: 0.75rem;
  border: none;
  border-radius: 5px;
  font-size: 1rem;
  cursor: pointer;
  margin-bottom: 1rem;
}

.btn-primary {
  background-color: #667eea;
  color: white;
}

.btn-primary:hover {
  background-color: #5a6fd8;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
  width: auto;
  margin-top: 0.5rem;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.switch-form {
  text-align: center;
  margin-top: 1rem;
}

.switch-form a {
  color: #667eea;
  text-decoration: none;
}

.switch-form a:hover {
  text-decoration: underline;
}
</style>