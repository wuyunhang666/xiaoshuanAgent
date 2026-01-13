<template>
  <div class="login-container">
    <div class="login-form">
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="硅谷小智" width="120" height="120" />
        <h2>小双医疗助手</h2>
      </div>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="login" :loading="loading" style="width: 100%">登录</el-button>
        </el-form-item>
        <div class="register-link">
          <span>还没有账号？</span>
          <el-link type="primary" @click="toRegister">立即注册</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const loginForm = ref({
  username: '',
  password: ''
})

const loading = ref(false)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const login = () => {
  loading.value = true
  // 调用后端登录接口
  // 注意：后端使用@RequestParam，需要传递查询参数
  // 注意：使用/api前缀以启用代理转发
  console.log('开始登录请求...')
  axios.post('/api/user/login', null, {
    params: {
      userName: loginForm.value.username, // 与后端字段名保持一致
      password: loginForm.value.password
    }
  })
    .then(response => {
      console.log('登录请求返回:', response)
      console.log('登录返回数据:', response.data)
      
      if (response.data.success) {
        console.log('登录成功，开始存储token...')
        localStorage.setItem('userToken', response.data.token || '')
        localStorage.setItem('username', loginForm.value.username)
        // 存储userId到localStorage
        localStorage.setItem('userId', response.data.data?.id || '') // 根据后端返回结构调整，添加可选链
        
        console.log('token存储成功:', localStorage.getItem('userToken'))
        console.log('userId存储成功:', localStorage.getItem('userId'))
        
        ElMessage.success('登录成功')
        console.log('执行路由跳转: /chat')
        // 使用nextTick确保DOM更新后再进行路由跳转
        nextTick(() => {
          router.push('/chat').then(() => {
            console.log('路由跳转成功')
          }).catch(err => {
            console.error('路由跳转失败:', err)
          })
        })
      } else {
        console.log('登录失败，原因:', response.data.message)
        ElMessage.error(response.data.message || '登录失败')
      }
    })
    .catch(error => {
      console.error('登录错误:', error)
      console.error('错误详情:', error.response?.data || error.message)
      ElMessage.error('登录失败，请稍后重试')
    })
    .finally(() => {
      loading.value = false
      console.log('登录请求结束')
    })
}

const toRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-form {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.logo-section {
  text-align: center;
  margin-bottom: 30px;
}

.logo-section h2 {
  margin-top: 15px;
  color: #333;
}

.register-link {
  text-align: center;
  margin-top: 15px;
  color: #666;
}
</style>