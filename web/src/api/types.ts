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
  status?: string
  likeCount: number
  likesCount?: number // alias for compatibility
  colors: string[] // Exactly 4 hex colors
  tags: string[]
  createdBy?: string
  creatorId?: string // alias
  creatorName?: string
  createdAt: string
  publishedAt?: string
  likedByMe?: boolean
  isLiked?: boolean // alias
  isOwner?: boolean
}

export interface CreatePaletteRequest {
  name: string
  description?: string
  colors: string[] // Exactly 4 hex colors
  tags?: string[]
  publish?: boolean
}

export interface UpdatePaletteRequest {
  name?: string
  colors?: string[] // Exactly 4 hex colors
  tags?: string[]
}

export interface PageMetadata {
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export interface PagedResponse<T> {
  items: T[]
  metadata: PageMetadata
}

// Alias for backward compatibility
export type PageResponse<T> = PagedResponse<T>

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

export interface PaletteCountResponse {
  count: number
}
