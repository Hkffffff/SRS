import axios from 'axios'

const runtimeBaseUrl =
  import.meta.env.VITE_API_BASE_URL ||
  `${window.location.protocol}//${window.location.hostname}:8080`

const request = axios.create({
  baseURL: import.meta.env.DEV ? '' : runtimeBaseUrl,
  timeout: 10000,
  withCredentials: true,
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload.code !== 'undefined') {
      if (payload.code === 200) {
        return payload
      }

      const error = new Error(payload.msg || '请求失败')
      error.code = payload.code
      error.payload = payload
      throw error
    }

    return payload
  },
  (error) => {
    const payload = error.response?.data
    if (payload && typeof payload.code !== 'undefined') {
      const normalizedError = new Error(payload.msg || '请求失败')
      normalizedError.code = payload.code
      normalizedError.payload = payload
      throw normalizedError
    }

    throw new Error(error.message || '网络异常')
  },
)

export default request
