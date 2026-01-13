<template>
  <div class="register-container">
    <div class="register-form">
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="小双医疗助手" width="120" height="120" />
        <h2>小双医疗助手</h2>
      </div>
      <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" clearable></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password clearable></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" show-password clearable></el-input>
        </el-form-item>
        
        <!-- 位置选择组件 -->
        <el-form-item label="位置信息">
          <LocationPicker @location-selected="handleLocationSelected" />
        </el-form-item>
        
        <el-form-item label="省" prop="province">
          <el-input v-model="registerForm.province" placeholder="请输入省份" clearable></el-input>
        </el-form-item>
        <el-form-item label="市" prop="city">
          <el-input v-model="registerForm.city" placeholder="请输入城市" clearable></el-input>
        </el-form-item>
        <el-form-item label="区/县" prop="district">
          <el-input v-model="registerForm.district" placeholder="请输入区/县" clearable></el-input>
        </el-form-item>
        <el-form-item label="详细地址" prop="address">
          <el-input v-model="registerForm.address" placeholder="请输入详细地址" type="textarea" :rows="2"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="register" :loading="loading" style="width: 100%">注册</el-button>
        </el-form-item>
        <div class="login-link">
          <span>已有账号？</span>
          <el-link type="primary" @click="toLogin">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import LocationPicker from './LocationPicker.vue'

const router = useRouter()
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: '',
  longitude: ''
})

const loading = ref(false)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区/县', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, message: '详细地址至少5个字符', trigger: 'blur' }
  ]
}

// 处理位置选择事件
const handleLocationSelected = (location) => {
  // 这里可以根据实际情况更新表单中的地址信息
  // 例如调用逆地理编码API获取省份、城市等信息
  // 目前只是示例，实际应用中需要根据API返回结果进行解析
  if (location.coordinates) {
    registerForm.latitude = location.coordinates.latitude
    registerForm.longitude = location.coordinates.longitude
  }
}

const register = () => {
  loading.value = true
  // 调用后端注册接口
  // 注意：后端address字段是字符串类型，所以我们将地址信息转换为JSON字符串
  const userData = {
    userName: registerForm.username, // 与后端字段名保持一致
    password: registerForm.password,
    address: JSON.stringify({
      province: registerForm.province,
      city: registerForm.city,
      district: registerForm.district,
      detail: registerForm.address,
      latitude: registerForm.latitude,
      longitude: registerForm.longitude
    })
  }
  
  // 注意：使用/api前缀以启用代理转发
  axios.post('/api/user/register', userData)
    .then(response => {
      if (response.data.success) {
        ElMessage.success('注册成功')
        router.push('/login')
      } else {
        ElMessage.error(response.data.message || '注册失败')
      }
    })
    .catch(error => {
      console.error('注册错误:', error)
      ElMessage.error('注册失败，请稍后重试')
    })
    .finally(() => {
      loading.value = false
    })
}

const toLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.register-form {
  width: 450px;
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

.login-link {
  text-align: center;
  margin-top: 15px;
  color: #666;
}
</style>