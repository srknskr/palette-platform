package com.palette.mobile.favorite.usecase

import com.palette.mobile.core.model.AppResult
import com.palette.mobile.core.model.PagedList
import com.palette.mobile.favorite.repository.FavoriteRepository
import com.palette.mobile.palette.model.Palette
import kotlinx.coroutines.flow.Flow

class ToggleFavoriteUseCase(private val favoriteRepository: FavoriteRepository) {
    suspend operator fun invoke(palette: Palette): AppResult<Boolean> {
        return favoriteRepository.toggleFavorite(palette)
    }
}

class GetUserFavoritesUseCase(private val favoriteRepository: FavoriteRepository) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): AppResult<PagedList<Palette>> {
        return favoriteRepository.getUserFavorites(page, size)
    }
}

class ObserveFavoritesUseCase(private val favoriteRepository: FavoriteRepository) {
    operator fun invoke(): Flow<List<Palette>> {
        return favoriteRepository.cachedFavorites
    }
}
