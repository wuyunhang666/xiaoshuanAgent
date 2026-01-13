<template>
  <div class="location-picker">
    <el-button type="primary" @click="getLocation" :loading="loading">
      {{ loading ? '获取位置中...' : '自动获取位置' }}
    </el-button>
    <div v-if="locationInfo" class="location-info">
      <p>当前位置：{{ locationInfo.address }}</p>
      <p v-if="locationInfo.coordinates">经纬度：{{ locationInfo.coordinates.latitude }}, {{ locationInfo.coordinates.longitude }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, defineEmits } from 'vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['location-selected'])
const loading = ref(false)
const locationInfo = ref(null)

// 获取位置信息
const getLocation = () => {
  loading.value = true
  
  // 检查浏览器是否支持地理位置API
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持地理位置API')
    loading.value = false
    return
  }
  
  // 获取当前位置
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const latitude = position.coords.latitude
      const longitude = position.coords.longitude
      
      // 这里可以调用逆地理编码API获取详细地址信息
      // 例如使用百度地图、高德地图等API
      // 这里先模拟一个地址信息
      const mockAddress = '北京市海淀区'
      
      locationInfo.value = {
        address: mockAddress,
        coordinates: {
          latitude,
          longitude
        }
      }
      
      // 发送位置信息给父组件
      emit('location-selected', locationInfo.value)
      
      ElMessage.success('位置获取成功')
      loading.value = false
    },
    (error) => {
      let errorMsg = ''
      switch (error.code) {
        case error.PERMISSION_DENIED:
          errorMsg = '您拒绝了位置请求'
          break
        case error.POSITION_UNAVAILABLE:
          errorMsg = '位置信息不可用'
          break
        case error.TIMEOUT:
          errorMsg = '位置请求超时'
          break
        case error.UNKNOWN_ERROR:
          errorMsg = '发生未知错误'
          break
      }
      ElMessage.error(errorMsg)
      loading.value = false
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}
</script>

<style scoped>
.location-picker {
  margin-bottom: 20px;
}

.location-info {
  margin-top: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
}
</style>