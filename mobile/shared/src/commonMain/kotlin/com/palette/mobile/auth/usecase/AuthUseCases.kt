package com.palette.mobile.auth.usecase

import com.palette.mobile.auth.model.AuthState
import com.palette.mobile.auth.model.User
import com.palette.mobile.auth.repository.AuthRepository
import com.palette.mobile.core.model.AppResult
import kotlinx.coroutines.flow.Flow

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AppResult<User> {
        return authRepository.login(email.trim(), password)
    }
}

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, displayName: String): AppResult<User> {
        return authRepository.register(email.trim(), password, displayName.trim())
    }
}

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): AppResult<Unit> {
        return authRepository.logout()
    }
}

class GetAuthStateUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<AuthState> = authRepository.authState
}

class InitializeAuthUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke() = authRepository.initialize()
}
