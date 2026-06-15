import axios from 'axios'
import { ElMessage } from 'element-plus'

// 消息去重集合
const recentMessages = new Set()

// 显示错误消息（带去重）
const showError = (message) => {
  if (!message || recentMessages.has(message)) return
  recentMessages.add(message)
  setTimeout(() => recentMessages.delete(message), 2000)
  ElMessage.error({ message, grouping: true })
}

// 显示成功消息
const showSuccess = (message) => {
  if (!message) return
  ElMessage.success({ message, grouping: true })
}

// 创建 axios 实例
const request = axios.create({
  baseURL: '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果响应没有 code 字段，直接返回 data
    if (res.code === undefined) {
      return response
    }
    
    // 业务逻辑成功
    if (res.code === 200) {
      return res
    }
    
    // 业务逻辑错误 - 显示后端返回的错误消息
    const message = res.message || '操作失败'
    showError(message)
    
    // 标记为业务错误，防止重复显示
    const error = new Error(message)
    error._isBusinessError = true
    return Promise.reject(error)
  },
  (error) => {
    // 如果已经是业务错误，跳过错误拦截器的处理
    if (error._isBusinessError) {
      return Promise.reject(error)
    }
    
    let message = '请求失败'
    
    // HTTP 错误处理
    if (error.response) {
      const { status, data } = error.response
      
      // 优先使用后端返回的 message
      if (data && data.message) {
        message = data.message
      } else {
        // 根据状态码提供默认消息
        switch (status) {
          case 400:
            message = '请求参数错误'
            break
          case 401:
            message = '登录已过期，请重新登录'
            // 清除登录状态并跳转
            localStorage.removeItem('token')
            setTimeout(() => {
              window.location.href = '/login'
            }, 1500)
            break
          case 403:
            message = '权限不足，无法访问'
            break
          case 404:
            message = '请求的资源不存在'
            break
          case 405:
            message = '请求方法不支持'
            break
          case 500:
            message = '服务器错误，请稍后重试'
            break
          default:
            message = `请求失败 (${status})`
        }
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时，请稍后重试'
    } else if (error.message === 'Network Error') {
      message = '网络错误，请检查网络连接'
    }
    
    showError(message)
    
    // 标记错误已显示
    error._isBusinessError = true
    return Promise.reject(error)
  }
)

// 导出工具方法
export { showError, showSuccess }
export default request
