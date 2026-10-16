import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

api.interceptors.response.use(
  res => {
    const data = res.data
    if (data.code && data.code !== 200) {
      ElMessage.error(data.message || '操作失败')
      return Promise.reject(new Error(data.message))
    }
    return data
  },
  err => {
    const status = err.response?.status
    const msg = err.response?.data?.message || '请求失败'
    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('nickname')
      localStorage.removeItem('userId')
      window.location.href = '/login'
      return Promise.reject(err)
    }
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default api
