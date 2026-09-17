package com.palette.mobile.android.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.favorite.usecase.GetUserFavoritesUseCase
import com.palette.mobile.favorite.usecase.ToggleFavoriteUseCase
import com.palette.mobile.palette.model.Palette
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CollectionUiState {
    data object Loading : CollectionUiState
    data class Success(val palettes: List<Palette>, val hasNext: Boolean) : CollectionUiState
    data class Error(val message: String) : CollectionUiState
    data object Empty : CollectionUiState
}

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val getUserFavoritesUseCase: GetUserFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectionUiState>(CollectionUiState.Loading)
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private val currentItems = mutableListOf<Palette>()

    fun loadFavorites(isRefresh: Boolean = false) {
        if (isRefresh) {
            currentPage = 0
            currentItems.clear()
        }
        viewModelScope.launch {
            if (currentItems.isEmpty()) {
                _uiState.value = CollectionUiState.Loading
            }
            when (val result = getUserFavoritesUseCase(page = currentPage, size = 20)) {
                is AppResult.Success -> {
                    if (currentPage == 0) {
                        currentItems.clear()
                    }
                    currentItems.addAll(result.data.items)
                    if (currentItems.isEmpty()) {
                        _uiState.value = CollectionUiState.Empty
                    } else {
                        _uiState.value = CollectionUiState.Success(
                            palettes = currentItems.toList(),
                            hasNext = result.data.metadata.hasNext
                        )
                    }
                }
                is AppResult.Error -> {
                    if (currentItems.isEmpty()) {
                        _uiState.value = CollectionUiState.Error(result.error.message)
                    }
                }
            }
        }
    }

    fun toggleFavorite(palette: Palette, onAuthRequired: () -> Unit) {
        viewModelScope.launch {
            val original = currentItems.toList()
            currentItems.removeAll { it.id == palette.id }
            if (currentItems.isEmpty()) {
                _uiState.value = CollectionUiState.Empty
            } else {
                _uiState.value = CollectionUiState.Success(palettes = currentItems.toList(), hasNext = false)
            }

            when (val result = toggleFavoriteUseCase(palette)) {
                is AppResult.Success -> {
                }
                is AppResult.Error -> {
                    if (result.error.statusCode == 401) {
                        onAuthRequired()
                    }
                    currentItems.clear()
                    currentItems.addAll(original)
                    _uiState.value = CollectionUiState.Success(palettes = currentItems.toList(), hasNext = false)
                }
            }
        }
    }
}
