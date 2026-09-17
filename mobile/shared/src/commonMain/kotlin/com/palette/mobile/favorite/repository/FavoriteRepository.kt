package com.palette.mobile.favorite.repository

import com.palette.mobile.core.model.AppResult
import com.palette.mobile.core.model.PageMetadata
import com.palette.mobile.core.model.PagedList
import com.palette.mobile.core.model.map
import com.palette.mobile.network.NetworkClient
import com.palette.mobile.network.toDomain
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.repository.PaletteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FavoriteRepository(
    private val networkClient: NetworkClient,
    private val paletteRepository: PaletteRepository
) {
    private val favoritesCache = mutableMapOf<String, Palette>()
    private val cacheMutex = Mutex()
    private val _cachedFavorites = MutableStateFlow<List<Palette>>(emptyList())
    val cachedFavorites: Flow<List<Palette>> = _cachedFavorites.asStateFlow()

    suspend fun getUserFavorites(page: Int = 0, size: Int = 20): AppResult<PagedList<Palette>> {
        val result = networkClient.getUserFavorites(page = page, size = size)
            .map { it.toDomain { dto -> dto.toDomain() } }

        if (result is AppResult.Success) {
            cacheMutex.withLock {
                if (page == 0) {
                    favoritesCache.clear()
                }
                result.data.items.forEach {
                    favoritesCache[it.id] = it
                }
                _cachedFavorites.value = favoritesCache.values.toList()
            }
        } else if (page == 0) {
            cacheMutex.withLock {
                if (_cachedFavorites.value.isNotEmpty()) {
                    val cached = _cachedFavorites.value
                    return AppResult.Success(
                        PagedList(
                            items = cached,
                            metadata = PageMetadata(
                                page = 0,
                                size = cached.size,
                                totalElements = cached.size.toLong(),
                                totalPages = 1,
                                hasNext = false,
                                hasPrevious = false
                            )
                        )
                    )
                }
            }
        }
        return result
    }

    suspend fun toggleFavorite(palette: Palette): AppResult<Boolean> {
        val willLike = !palette.likedByMe

        cacheMutex.withLock {
            if (willLike) {
                favoritesCache[palette.id] = palette.copy(likedByMe = true, likeCount = palette.likeCount + 1)
            } else {
                favoritesCache.remove(palette.id)
            }
            _cachedFavorites.value = favoritesCache.values.toList()
        }
        paletteRepository.updateFavoriteInCache(palette.id, willLike)

        val apiResult = if (willLike) {
            networkClient.favoritePalette(palette.id)
        } else {
            networkClient.unfavoritePalette(palette.id)
        }

        return when (apiResult) {
            is AppResult.Success -> AppResult.Success(willLike)
            is AppResult.Error -> {
                cacheMutex.withLock {
                    if (willLike) {
                        favoritesCache.remove(palette.id)
                    } else {
                        favoritesCache[palette.id] = palette
                    }
                    _cachedFavorites.value = favoritesCache.values.toList()
                }
                paletteRepository.updateFavoriteInCache(palette.id, palette.likedByMe)
                apiResult
            }
        }
    }
}
