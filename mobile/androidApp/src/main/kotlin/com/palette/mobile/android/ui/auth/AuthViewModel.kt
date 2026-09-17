package com.palette.mobile.android.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palette.mobile.auth.model.User
import com.palette.mobile.auth.usecase.LoginUseCase
import com.palette.mobile.auth.usecase.RegisterUseCase
import com.palette.mobile.core.model.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Submitting : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String, val fieldErrors: Map<String, String> = emptyMap()) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Submitting
            when (val result = loginUseCase(trimmedEmail, pass)) {
                is AppResult.Success -> _uiState.value = AuthUiState.Success(result.data)
                is AppResult.Error -> _uiState.value = AuthUiState.Error(
                    result.error.message,
                    result.error.validationErrors
                )
            }
        }
    }

    fun register(email: String, pass: String, displayName: String) {
        val trimmedEmail = email.trim()
        val trimmedName = displayName.trim()
        if (trimmedEmail.isBlank() || pass.isBlank() || trimmedName.isBlank()) {
            _uiState.value = AuthUiState.Error("All fields are required")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Submitting
            when (val result = registerUseCase(trimmedEmail, pass, trimmedName)) {
                is AppResult.Success -> _uiState.value = AuthUiState.Success(result.data)
                is AppResult.Error -> _uiState.value = AuthUiState.Error(
                    result.error.message,
                    result.error.validationErrors
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
