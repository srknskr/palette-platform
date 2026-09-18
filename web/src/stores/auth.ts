import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/endpoints'
import type { LoginRequest, RegisterRequest, User } from '@/api/types'
import { extractErrorMessage } from '@/api/client'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('access_token'))
  const refreshToken = ref<string | null>(localStorage.getItem('refresh_token'))
  
  const savedUser = localStorage.getItem('user')
  const user = ref<User | null>(savedUser ? JSON.parse(savedUser) : null)
  
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  const isAuthenticated = computed(() => !!token.value && !!user.value)

  const login = async (credentials: LoginRequest) => {
    isLoading.value = true
    error.value = null
    try {
      const data = await authApi.login(credentials)
      token.value = data.accessToken
      refreshToken.value = data.refreshToken
      user.value = data.user

      localStorage.setItem('access_token', data.accessToken)
      localStorage.setItem('refresh_token', data.refreshToken)
      localStorage.setItem('user', JSON.stringify(data.user))
      return data
    } catch (err) {
      error.value = extractErrorMessage(err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const register = async (userData: RegisterRequest) => {
    isLoading.value = true
    error.value = null
    try {
      const data = await authApi.register(userData)
      token.value = data.accessToken
      refreshToken.value = data.refreshToken
      user.value = data.user

      localStorage.setItem('access_token', data.accessToken)
      localStorage.setItem('refresh_token', data.refreshToken)
      localStorage.setItem('user', JSON.stringify(data.user))
      return data
    } catch (err) {
      error.value = extractErrorMessage(err)
      throw err
    } finally {
      isLoading.value = false
    }
  }

  const logout = async () => {
    try {
      if (refreshToken.value) {
        await authApi.logout(refreshToken.value)
      }
    } catch {
      // Ignore logout errors and proceed to clean local state
    } finally {
      token.value = null
      refreshToken.value = null
      user.value = null
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      localStorage.removeItem('user')
    }
  }

  const fetchProfile = async () => {
    if (!token.value) return null
    try {
      const currentUser = await authApi.getMe()
      user.value = currentUser
      localStorage.setItem('user', JSON.stringify(currentUser))
      return currentUser
    } catch {
      return null
    }
  }

  // Handle global auth expiration event dispatched by Axios interceptor
  window.addEventListener('auth:expired', () => {
    token.value = null
    refreshToken.value = null
    user.value = null
  })

  return {
    token,
    refreshToken,
    user,
    isLoading,
    error,
    isAuthenticated,
    login,
    register,
    logout,
    fetchProfile
  }
})
