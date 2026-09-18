export interface User {
  id: string
  email: string
  displayName?: string
  createdAt?: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  displayName?: string
}

export interface RefreshRequest {
  refreshToken: string
}

export interface Palette {
  id: string
  name: string
  description?: string
  colors: string[] // Exactly 4 hex colors
  tags: string[]
  likesCount: number
  isLiked?: boolean
  isOwner?: boolean
  creatorId?: string
  creatorName?: string
  createdAt: string
  updatedAt?: string
}

export interface CreatePaletteRequest {
  name: string
  description?: string
  colors: string[] // Exactly 4 hex colors
  tags?: string[]
}

export interface UpdatePaletteRequest {
  name?: string
  description?: string
  colors?: string[] // Exactly 4 hex colors
  tags?: string[]
}

export interface PageResponse<T> {
  content: T[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface PaletteQueryParams {
  name?: string
  tag?: string
  hexColor?: string
  sort?: 'newest' | 'popular' | 'random'
  page?: number
  size?: number
}

export interface ApiError {
  message: string
  status?: number
  timestamp?: string
  path?: string
  errors?: Record<string, string>
}
