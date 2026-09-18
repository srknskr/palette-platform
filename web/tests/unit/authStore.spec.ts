import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  it('initializes with unauthenticated state when storage is empty', () => {
    const auth = useAuthStore()
    expect(auth.isAuthenticated).toBe(false)
    expect(auth.user).toBeNull()
    expect(auth.token).toBeNull()
  })

  it('restores state from localStorage if present', () => {
    const mockUser = { id: 'u1', email: 'test@example.com', displayName: 'Tester' }
    localStorage.setItem('access_token', 'mock_token')
    localStorage.setItem('user', JSON.stringify(mockUser))

    const auth = useAuthStore()
    expect(auth.isAuthenticated).toBe(true)
    expect(auth.token).toBe('mock_token')
    expect(auth.user?.email).toBe('test@example.com')
  })

  it('clears state on logout', async () => {
    localStorage.setItem('access_token', 'mock_token')
    localStorage.setItem('refresh_token', 'mock_refresh')
    localStorage.setItem('user', JSON.stringify({ id: 'u1', email: 'a@b.com' }))

    const auth = useAuthStore()
    await auth.logout()

    expect(auth.isAuthenticated).toBe(false)
    expect(auth.token).toBeNull()
    expect(auth.refreshToken).toBeNull()
    expect(auth.user).toBeNull()
    expect(localStorage.getItem('access_token')).toBeNull()
  })
})
