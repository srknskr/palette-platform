package com.serkanmusic.palette.favorite.application

import com.serkanmusic.palette.favorite.infrastructure.persistence.FavoriteEntity
import com.serkanmusic.palette.favorite.infrastructure.persistence.FavoriteId
import com.serkanmusic.palette.favorite.infrastructure.persistence.FavoriteRepository
import com.serkanmusic.palette.palette.api.dto.PaletteResponse
import com.serkanmusic.palette.palette.domain.Palette
import com.serkanmusic.palette.palette.domain.PaletteStatus
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteRepository
import com.serkanmusic.palette.shared.dto.PageMetadata
import com.serkanmusic.palette.shared.dto.PagedResponse
import com.serkanmusic.palette.shared.error.ResourceNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class FavoriteService(
    private val favoriteRepository: FavoriteRepository,
    private val paletteRepository: PaletteRepository
) {

    @Transactional
    fun favoritePalette(userId: UUID, paletteId: UUID) {
        val palette = paletteRepository.findById(paletteId)
            .orElseThrow { ResourceNotFoundException("Palette with id $paletteId not found") }

        val favoriteId = FavoriteId(userId = userId, paletteId = paletteId)
        if (!favoriteRepository.existsById(favoriteId)) {
            favoriteRepository.save(FavoriteEntity(id = favoriteId, createdAt = Instant.now()))
            paletteRepository.incrementLikeCount(palette.id)
        }
    }

    @Transactional
    fun unfavoritePalette(userId: UUID, paletteId: UUID) {
        val favoriteId = FavoriteId(userId = userId, paletteId = paletteId)
        if (favoriteRepository.existsById(favoriteId)) {
            favoriteRepository.deleteById(favoriteId)
            paletteRepository.decrementLikeCount(paletteId)
        }
    }

    @Transactional(readOnly = true)
    fun getUserFavorites(userId: UUID, page: Int, size: Int): PagedResponse<PaletteResponse> {
        val boundedSize = size.coerceIn(1, 50)
        val boundedPage = page.coerceAtLeast(0)
        val pageable = PageRequest.of(boundedPage, boundedSize)

        val favoritesPage = favoriteRepository.findAllByIdUserIdOrderByCreatedAtDesc(userId, pageable)
        val paletteIds = favoritesPage.content.map { it.id.paletteId }

        val palettesMap = if (paletteIds.isNotEmpty()) {
            paletteRepository.findAllById(paletteIds).associateBy { it.id }
        } else {
            emptyMap()
        }

        val responses = paletteIds.mapNotNull { id ->
            palettesMap[id]?.let { toResponse(it.toDomain()) }
        }

        return PagedResponse(
            items = responses,
            metadata = PageMetadata(
                page = favoritesPage.number,
                size = favoritesPage.size,
                totalElements = favoritesPage.totalElements,
                totalPages = favoritesPage.totalPages,
                hasNext = favoritesPage.hasNext(),
                hasPrevious = favoritesPage.hasPrevious()
            )
        )
    }

    private fun toResponse(domain: Palette): PaletteResponse {
        return PaletteResponse(
            id = domain.id,
            name = domain.name,
            status = domain.status.name,
            likeCount = domain.likeCount,
            colors = domain.colors.sortedBy { it.position }.map { it.hexValue },
            tags = domain.tags.map { it.slug },
            createdBy = domain.createdBy,
            createdAt = domain.createdAt,
            publishedAt = domain.publishedAt,
            likedByMe = true
        )
    }
}
