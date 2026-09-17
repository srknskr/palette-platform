package com.palette.mobile.android.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.core.model.PagedList
import com.palette.mobile.favorite.usecase.ToggleFavoriteUseCase
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.model.PaletteFilter
import com.palette.mobile.palette.model.PaletteSort
import com.palette.mobile.palette.usecase.GetPalettesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DiscoverUiState {
    data object Loading : DiscoverUiState
    data class Success(val palettes: List<Palette>, val hasNext: Boolean, val isOffline: Boolean = false) : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
    data object Empty : DiscoverUiState
}

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val getPalettesUseCase: GetPalettesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow(PaletteFilter())
    val filter: StateFlow<PaletteFilter> = _filter.asStateFlow()

    private var currentPage = 0
    private var isLoadingMore = false
    private val currentItems = mutableListOf<Palette>()

    init {
        loadPalettes()
    }

    fun loadPalettes(isRefresh: Boolean = false) {
        if (isRefresh) {
            currentPage = 0
            currentItems.clear()
        }
        viewModelScope.launch {
            if (currentItems.isEmpty()) {
                _uiState.value = DiscoverUiState.Loading
            }
            when (val result = getPalettesUseCase(_filter.value, page = currentPage, size = 20)) {
                is AppResult.Success -> {
                    val paged = result.data
                    if (currentPage == 0) {
                        currentItems.clear()
                    }
                    currentItems.addAll(paged.items)

                    if (currentItems.isEmpty()) {
                        _uiState.value = DiscoverUiState.Empty
                    } else {
                        _uiState.value = DiscoverUiState.Success(
                            palettes = currentItems.toList(),
                            hasNext = paged.metadata.hasNext
                        )
                    }
                }
                is AppResult.Error -> {
                    if (currentItems.isEmpty()) {
                        _uiState.value = DiscoverUiState.Error(result.error.message)
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        val currentState = _uiState.value
        if (currentState is DiscoverUiState.Success && currentState.hasNext && !isLoadingMore) {
            isLoadingMore = true
            currentPage++
            viewModelScope.launch {
                when (val result = getPalettesUseCase(_filter.value, page = currentPage, size = 20)) {
                    is AppResult.Success -> {
                        currentItems.addAll(result.data.items)
                        _uiState.value = DiscoverUiState.Success(
                            palettes = currentItems.toList(),
                            hasNext = result.data.metadata.hasNext
                        )
                    }
                    is AppResult.Error -> {
                        currentPage--
                    }
                }
                isLoadingMore = false
            }
        }
    }

    fun setSort(sort: PaletteSort) {
        if (_filter.value.sort != sort) {
            _filter.value = _filter.value.copy(sort = sort)
            loadPalettes(isRefresh = true)
        }
    }

    fun setSearchQuery(query: String) {
        val trimmed = query.trim()
        val newQuery = if (trimmed.isBlank()) null else trimmed
        if (_filter.value.query != newQuery) {
            _filter.value = _filter.value.copy(query = newQuery)
            loadPalettes(isRefresh = true)
        }
    }

    fun toggleFavorite(palette: Palette, onAuthRequired: () -> Unit) {
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(palette)) {
                is AppResult.Success -> {
                    val index = currentItems.indexOfFirst { it.id == palette.id }
                    if (index != -1) {
                        val newLikeCount = if (result.data) palette.likeCount + 1 else maxOf(0L, palette.likeCount - 1)
                        currentItems[index] = palette.copy(likedByMe = result.data, likeCount = newLikeCount)
                        val currentSuccess = _uiState.value as? DiscoverUiState.Success
                        if (currentSuccess != null) {
                            _uiState.value = currentSuccess.copy(palettes = currentItems.toList())
                        }
                    }
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
