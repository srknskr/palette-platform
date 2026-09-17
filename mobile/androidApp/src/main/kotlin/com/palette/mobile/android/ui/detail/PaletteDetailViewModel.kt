package com.palette.mobile.android.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.favorite.usecase.ToggleFavoriteUseCase
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.usecase.GetPaletteDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val palette: Palette) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

@HiltViewModel
class PaletteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPaletteDetailUseCase: GetPaletteDetailUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    val paletteId: String = checkNotNull(savedStateHandle["paletteId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            when (val result = getPaletteDetailUseCase(paletteId)) {
                is AppResult.Success -> _uiState.value = DetailUiState.Success(result.data)
                is AppResult.Error -> _uiState.value = DetailUiState.Error(result.error.message)
            }
        }
    }

    fun toggleFavorite(onAuthRequired: () -> Unit) {
        val currentSuccess = _uiState.value as? DetailUiState.Success ?: return
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(currentSuccess.palette)) {
                is AppResult.Success -> {
                    val newCount = if (result.data) currentSuccess.palette.likeCount + 1 else maxOf(0L, currentSuccess.palette.likeCount - 1)
                    _uiState.value = DetailUiState.Success(
                        currentSuccess.palette.copy(likedByMe = result.data, likeCount = newCount)
                    )
                }
                is AppResult.Error -> {
                    if (result.error.statusCode == 401) {
                        onAuthRequired()
                    }
                }
            }
        }
    }
}
