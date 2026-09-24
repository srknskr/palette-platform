package com.palette.mobile.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class RefreshRequestDto(
    val refreshToken: String
)

@Serializable
data class LogoutRequestDto(
    val refreshToken: String? = null
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val user: UserResponseDto
)

@Serializable
data class UserResponseDto(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String,
    val createdAt: String
)

@Serializable
data class CreatePaletteRequestDto(
    val name: String,
    val description: String? = null,
    val colors: List<String>,
    val tags: List<String> = emptyList(),
    val publish: Boolean = true
)

@Serializable
data class UpdatePaletteRequestDto(
    val name: String,
    val description: String? = null,
    val colors: List<String>,
    val tags: List<String> = emptyList()
)

@Serializable
data class PaletteResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val status: String,
    val likeCount: Long,
    val colors: List<String>,
    val tags: List<String>,
    val createdBy: String,
    val createdAt: String,
    val publishedAt: String? = null,
    val likedByMe: Boolean = false
)

@Serializable
data class PageMetadataDto(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

@Serializable
data class PagedResponseDto<T>(
    val items: List<T>,
    val metadata: PageMetadataDto
)

@Serializable
data class ProblemDetailDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null,
    val errors: Map<String, String>? = null
)

@Serializable
data class PaletteCountResponseDto(
    val count: Long
)
