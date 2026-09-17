package com.palette.mobile.auth.repository

import com.palette.mobile.auth.model.AuthState
import com.palette.mobile.auth.model.User
import com.palette.mobile.auth.storage.TokenStorage
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.core.model.map
import com.palette.mobile.network.NetworkClient
import com.palette.mobile.network.dto.LoginRequestDto
import com.palette.mobile.network.dto.RegisterRequestDto
import com.palette.mobile.network.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val networkClient: NetworkClient,
    private val tokenStorage: TokenStorage
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun getCurrentAuthState(): AuthState = _authState.value

    suspend fun initialize() {
        val user = tokenStorage.getUser()
        val token = tokenStorage.getAccessToken()
        if (user != null && token != null) {
            _authState.value = AuthState.Authenticated(user)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    suspend fun register(email: String, password: String, displayName: String): AppResult<User> {
        val result = networkClient.register(RegisterRequestDto(email, password, displayName))
        return when (result) {
            is AppResult.Success -> {
                tokenStorage.setAccessToken(result.data.accessToken)
                tokenStorage.setRefreshToken(result.data.refreshToken)
                val user = result.data.user.toDomain()
                tokenStorage.setUser(user)
                _authState.value = AuthState.Authenticated(user)
                AppResult.Success(user)
            }
            is AppResult.Error -> result
        }
    }

    suspend fun login(email: String, password: String): AppResult<User> {
        val result = networkClient.login(LoginRequestDto(email, password))
        return when (result) {
            is AppResult.Success -> {
                tokenStorage.setAccessToken(result.data.accessToken)
                tokenStorage.setRefreshToken(result.data.refreshToken)
                val user = result.data.user.toDomain()
                tokenStorage.setUser(user)
                _authState.value = AuthState.Authenticated(user)
                AppResult.Success(user)
            }
            is AppResult.Error -> result
        }
    }

    suspend fun logout(): AppResult<Unit> {
        val refreshToken = tokenStorage.getRefreshToken()
        val result = networkClient.logout(refreshToken)
        tokenStorage.clear()
        _authState.value = AuthState.Unauthenticated
        return result
    }

    suspend fun getCurrentUser(): AppResult<User> {
        val result = networkClient.getCurrentUser().map { it.toDomain() }
        if (result is AppResult.Success) {
            tokenStorage.setUser(result.data)
            _authState.value = AuthState.Authenticated(result.data)
        }
        return result
    }

    suspend fun isAuthenticated(): Boolean {
        return tokenStorage.getAccessToken() != null
    }
}
