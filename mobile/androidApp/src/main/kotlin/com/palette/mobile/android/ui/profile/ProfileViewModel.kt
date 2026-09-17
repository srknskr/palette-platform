package com.palette.mobile.android.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palette.mobile.auth.model.AuthState
import com.palette.mobile.auth.model.User
import com.palette.mobile.auth.usecase.GetAuthStateUseCase
import com.palette.mobile.auth.usecase.LogoutUseCase
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.usecase.GetMyPalettesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data object Unauthenticated : ProfileUiState
    data class Authenticated(
        val user: User,
        val myPalettes: List<Palette>,
        val isDarkTheme: Boolean = false
    ) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getAuthStateUseCase: GetAuthStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getMyPalettesUseCase: GetMyPalettesUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = getAuthStateUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Loading)

    private val _myPalettes = MutableStateFlow<List<Palette>>(emptyList())
    val myPalettes: StateFlow<List<Palette>> = _myPalettes.asStateFlow()

    fun loadMyPalettes() {
        viewModelScope.launch {
            when (val result = getMyPalettesUseCase(page = 0, size = 50)) {
                is AppResult.Success -> {
                    _myPalettes.value = result.data.items
                }
                is AppResult.Error -> {
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _myPalettes.value = emptyList()
        }
    }
}
