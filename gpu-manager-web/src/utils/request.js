import axios from 'axios'
import router from '../router'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    return response
  },

  error => {
    const status = error.response?.status

    if (status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')

      ElMessage.error('登录已失效，请重新登录')

      router.push('/login')
    }

    else if (status === 403) {
      ElMessage.error('没有权限执行该操作')
    }

    else if (status >= 500) {
      ElMessage.error('服务器异常，请稍后再试')
    }

    return Promise.reject(error)
  }
)

export default request