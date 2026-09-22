import { apiClient } from './client'
import type {
  AuthResponse,
  CreatePaletteRequest,
  LoginRequest,
  PageResponse,
  Palette,
  PaletteCountResponse,
  PaletteQueryParams,
  RegisterRequest,
  UpdatePaletteRequest,
  User
} from './types'

export const authApi = {
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const res = await apiClient.post<AuthResponse>('/api/v1/auth/register', data)
    return res.data
  },

  async login(data: LoginRequest): Promise<AuthResponse> {
    const res = await apiClient.post<AuthResponse>('/api/v1/auth/login', data)
    return res.data
  },

  async logout(refreshToken: string): Promise<void> {
    await apiClient.post('/api/v1/auth/logout', { refreshToken })
  },

  async getMe(): Promise<User> {
    const res = await apiClient.get<User>('/api/v1/me')
    return res.data
  }
}

export const paletteApi = {
  async getPalettes(params: PaletteQueryParams = {}): Promise<PageResponse<Palette>> {
    const res = await apiClient.get<PageResponse<Palette>>('/api/v1/palettes', { params })
    return res.data
  },

  async getPaletteById(id: string): Promise<Palette> {
    const res = await apiClient.get<Palette>(`/api/v1/palettes/${id}`)
    return res.data
  },

  async getRandomPalette(): Promise<Palette> {
    const res = await apiClient.get<Palette>('/api/v1/palettes/random')
    return res.data
  },

  async getPaletteCount(): Promise<number> {
    const res = await apiClient.get<PaletteCountResponse>('/api/v1/palettes/count')
    return res.data.count
  },

  async createPalette(data: CreatePaletteRequest): Promise<Palette> {
    const res = await apiClient.post<Palette>('/api/v1/palettes', data)
    return res.data
  },

  async updatePalette(id: string, data: UpdatePaletteRequest): Promise<Palette> {
    const res = await apiClient.put<Palette>(`/api/v1/palettes/${id}`, data)
    return res.data
  },

  async deletePalette(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/palettes/${id}`)
  },

  async favoritePalette(id: string): Promise<Palette> {
    const res = await apiClient.post<Palette>(`/api/v1/palettes/${id}/favorite`)
    return res.data
  },

  async unfavoritePalette(id: string): Promise<Palette> {
    const res = await apiClient.delete<Palette>(`/api/v1/palettes/${id}/favorite`)
    return res.data
  },

  async getMyFavorites(page = 0, size = 20): Promise<PageResponse<Palette>> {
    const res = await apiClient.get<PageResponse<Palette>>('/api/v1/me/favorites', {
      params: { page, size }
    })
    return res.data
  },

  async getMyPalettes(page = 0, size = 20): Promise<PageResponse<Palette>> {
    const res = await apiClient.get<PageResponse<Palette>>('/api/v1/me/palettes', {
      params: { page, size }
    })
    return res.data
  }
}
