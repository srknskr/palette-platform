package com.palette.mobile.palette.repository

import com.palette.mobile.core.model.AppResult
import com.palette.mobile.core.model.PageMetadata
import com.palette.mobile.core.model.PagedList
import com.palette.mobile.core.model.map
import com.palette.mobile.network.NetworkClient
import com.palette.mobile.network.dto.CreatePaletteRequestDto
import com.palette.mobile.network.dto.UpdatePaletteRequestDto
import com.palette.mobile.network.toDomain
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.model.PaletteFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PaletteRepository(
    private val networkClient: NetworkClient
) {
    private val memoryCache = mutableMapOf<String, Palette>()
    private val cacheMutex = Mutex()
    private val _cachedPalettes = MutableStateFlow<List<Palette>>(emptyList())
    val cachedPalettes: Flow<List<Palette>> = _cachedPalettes.asStateFlow()

    suspend fun getPalettes(
        filter: PaletteFilter = PaletteFilter(),
        page: Int = 0,
        size: Int = 20
    ): AppResult<PagedList<Palette>> {
        val result = networkClient.listPalettes(
            name = filter.query,
            tag = filter.tag,
            hexColor = filter.hexColor,
            sort = filter.sort.queryValue,
            page = page,
            size = size
        ).map { it.toDomain { dto -> dto.toDomain() } }

        if (result is AppResult.Success) {
            cacheMutex.withLock {
                result.data.items.forEach { palette ->
                    memoryCache[palette.id] = palette
                }
                if (page == 0 && filter == PaletteFilter()) {
                    _cachedPalettes.value = result.data.items
                }
            }
        } else if (page == 0 && filter == PaletteFilter()) {
            cacheMutex.withLock {
                if (_cachedPalettes.value.isNotEmpty()) {
                    val cached = _cachedPalettes.value
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

    suspend fun getPaletteById(id: String): AppResult<Palette> {
        cacheMutex.withLock {
            val cached = memoryCache[id]
            if (cached != null) {
                return AppResult.Success(cached)
            }
        }
        val result = networkClient.getPaletteById(id).map { it.toDomain() }
        if (result is AppResult.Success) {
            cacheMutex.withLock {
                memoryCache[result.data.id] = result.data
            }
        }
        return result
    }

    suspend fun getRandomPalette(): AppResult<Palette> {
        val result = networkClient.getRandomPalette().map { it.toDomain() }
        if (result is AppResult.Success) {
            cacheMutex.withLock {
                memoryCache[result.data.id] = result.data
            }
        }
        return result
    }

    suspend fun createPalette(
        name: String,
        colors: List<String>,
        tags: List<String>,
        publish: Boolean = true
    ): AppResult<Palette> {
        val result = networkClient.createPalette(
            CreatePaletteRequestDto(name = name, colors = colors, tags = tags, publish = publish)
        ).map { it.toDomain() }

        if (result is AppResult.Success) {
            cacheMutex.withLock {
                memoryCache[result.data.id] = result.data
            }
        }
        return result
    }

    suspend fun updatePalette(
        id: String,
        name: String,
        colors: List<String>,
        tags: List<String>
    ): AppResult<Palette> {
        val result = networkClient.updatePalette(
            id = id,
            request = UpdatePaletteRequestDto(name = name, colors = colors, tags = tags)
        ).map { it.toDomain() }

        if (result is AppResult.Success) {
            cacheMutex.withLock {
                memoryCache[result.data.id] = result.data
            }
        }
        return result
    }

    suspend fun deletePalette(id: String): AppResult<Unit> {
        val result = networkClient.deletePalette(id)
        if (result is AppResult.Success) {
            cacheMutex.withLock {
                memoryCache.remove(id)
                _cachedPalettes.value = _cachedPalettes.value.filter { it.id != id }
            }
        }
        return result
    }

    suspend fun getMyPalettes(page: Int = 0, size: Int = 20): AppResult<PagedList<Palette>> {
        return networkClient.listMyPalettes(page = page, size = size).map { it.toDomain { dto -> dto.toDomain() } }
    }

    suspend fun updateFavoriteInCache(id: String, liked: Boolean) {
        cacheMutex.withLock {
            val palette = memoryCache[id]
            if (palette != null) {
                val updatedLikes = if (liked) palette.likeCount + 1 else maxOf(0L, palette.likeCount - 1)
                val updated = palette.copy(likedByMe = liked, likeCount = updatedLikes)
                memoryCache[id] = updated
                _cachedPalettes.value = _cachedPalettes.value.map {
                    if (it.id == id) updated else it
                }
            }
        }
    }

    suspend fun getPublishedPaletteCount(): AppResult<Long> {
        return networkClient.getPaletteCount().map { response ->
            response.count
        }
    }
}
