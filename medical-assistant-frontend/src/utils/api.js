import axios from 'axios';

// 创建axios实例
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api', // 使用Vite环境变量或默认值
  timeout: 15000, // 请求超时时间
});

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 在发送请求之前做些什么，比如添加认证token
    const token = localStorage.getItem('userToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    // 对请求错误做些什么
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    // 对响应数据做点什么
    return response.data;
  },
  error => {
    // 对响应错误做点什么
    console.error('响应错误:', error);
    
    if (error.response?.status === 401) {
      // 如果是未授权错误，跳转到登录页
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;