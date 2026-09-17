package com.palette.mobile.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val role: Role,
    val createdAt: String
)

enum class Role {
    USER,
    ADMIN;

    companion object {
        fun fromString(value: String): Role {
            return when (value.uppercase()) {
                "ADMIN" -> ADMIN
                else -> USER
            }
        }
    }
}

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: User) : AuthState
}
