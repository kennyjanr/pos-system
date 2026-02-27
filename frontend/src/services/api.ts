import axios from 'axios'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL as string) || 'http://localhost:8080/api'

const api = axios.create({
  baseURL: apiBaseUrl,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor for adding auth tokens (placeholder for future use)
api.interceptors.request.use(
  (config) => {
    // TODO: Add authorization header if needed
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor for handling errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export default api
