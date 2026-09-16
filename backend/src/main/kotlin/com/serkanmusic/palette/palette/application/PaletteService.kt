package com.serkanmusic.palette.palette.application

import com.serkanmusic.palette.favorite.infrastructure.persistence.FavoriteRepository
import com.serkanmusic.palette.palette.api.dto.CreatePaletteRequest
import com.serkanmusic.palette.palette.api.dto.PaletteResponse
import com.serkanmusic.palette.palette.api.dto.UpdatePaletteRequest
import com.serkanmusic.palette.palette.domain.Palette
import com.serkanmusic.palette.palette.domain.PaletteStatus
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteColorEntity
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteColorId
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteEntity
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteRepository
import com.serkanmusic.palette.palette.infrastructure.persistence.TagEntity
import com.serkanmusic.palette.palette.infrastructure.persistence.TagRepository
import com.serkanmusic.palette.shared.domain.ColorValidator
import com.serkanmusic.palette.shared.dto.PagedResponse
import com.serkanmusic.palette.shared.error.ForbiddenException
import com.serkanmusic.palette.shared.error.ResourceNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class PaletteService(
    private val paletteRepository: PaletteRepository,
    private val tagRepository: TagRepository,
    private val favoriteRepository: FavoriteRepository
) {

    @Transactional
    fun createPalette(userId: UUID, request: CreatePaletteRequest): PaletteResponse {
        val normalizedColors = ColorValidator.normalizeAndValidate(request.colors)
        val paletteId = UUID.randomUUID()
        val now = Instant.now()
        val initialStatus = if (request.publish) PaletteStatus.PUBLISHED else PaletteStatus.DRAFT
        val publishedAt = if (request.publish) now else null

        val tagEntities = resolveTags(request.tags)

        val paletteEntity = PaletteEntity(
            id = paletteId,
            createdBy = userId,
            name = request.name.trim(),
            status = initialStatus,
            likeCount = 0,
            tags = tagEntities.toMutableSet(),
            createdAt = now,
            updatedAt = now,
            publishedAt = publishedAt
        )

        paletteEntity.colors = normalizedColors.mapIndexed { index, hex ->
            PaletteColorEntity(
                id = PaletteColorId(paletteId = paletteId, position = index.toShort()),
                hexValue = hex
            )
        }.toMutableList()

        val saved = paletteRepository.save(paletteEntity)
        return toResponse(saved.toDomain(), isLiked = false)
    }

    @Transactional
    fun updatePalette(paletteId: UUID, userId: UUID, request: UpdatePaletteRequest): PaletteResponse {
        val paletteEntity = paletteRepository.findById(paletteId)
            .orElseThrow { ResourceNotFoundException("Palette with id $paletteId not found") }

        if (paletteEntity.createdBy != userId) {
            throw ForbiddenException("You are not authorized to modify this palette")
        }

        val normalizedColors = ColorValidator.normalizeAndValidate(request.colors)
        val tagEntities = resolveTags(request.tags)

        paletteEntity.name = request.name.trim()
        paletteEntity.updatedAt = Instant.now()
        paletteEntity.tags = tagEntities.toMutableSet()

        paletteEntity.colors.clear()
        paletteEntity.colors.addAll(
            normalizedColors.mapIndexed { index, hex ->
                PaletteColorEntity(
                    id = PaletteColorId(paletteId = paletteId, position = index.toShort()),
                    hexValue = hex
                )
            }
        )

        val updated = paletteRepository.save(paletteEntity)
        val isLiked = favoriteRepository.existsByIdUserIdAndIdPaletteId(userId, paletteId)
        return toResponse(updated.toDomain(), isLiked = isLiked)
    }

    @Transactional
    fun deletePalette(paletteId: UUID, userId: UUID, isAdmin: Boolean) {
        val paletteEntity = paletteRepository.findById(paletteId)
            .orElseThrow { ResourceNotFoundException("Palette with id $paletteId not found") }

        if (paletteEntity.createdBy != userId && !isAdmin) {
            throw ForbiddenException("You are not authorized to delete this palette")
        }

        paletteRepository.delete(paletteEntity)
    }

    @Transactional(readOnly = true)
    fun getPaletteById(id: UUID, currentUserId: UUID?): PaletteResponse {
        val paletteEntity = paletteRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Palette with id $id not found") }

        val isLiked = if (currentUserId != null) {
            favoriteRepository.existsByIdUserIdAndIdPaletteId(currentUserId, id)
        } else {
            false
        }

        return toResponse(paletteEntity.toDomain(), isLiked = isLiked)
    }

    @Transactional(readOnly = true)
    fun getRandomPalette(currentUserId: UUID?): PaletteResponse {
        val paletteEntity = try {
            paletteRepository.findRandomPublishedPaletteNative()
        } catch (@Suppress("SwallowedException", "TooGenericExceptionCaught") ex: Exception) {
            paletteRepository.findFirstPublishedPaletteNative()
        }.orElseThrow { ResourceNotFoundException("No published palettes found") }

        val isLiked = if (currentUserId != null) {
            favoriteRepository.existsByIdUserIdAndIdPaletteId(currentUserId, paletteEntity.id)
        } else {
            false
        }

        return toResponse(paletteEntity.toDomain(), isLiked = isLiked)
    }

    @Transactional(readOnly = true)
    fun listPalettes(
        name: String?,
        tag: String?,
        hexColor: String?,
        sort: String?,
        page: Int,
        size: Int,
        currentUserId: UUID?
    ): PagedResponse<PaletteResponse> {
        val boundedSize = size.coerceIn(1, 50)
        val boundedPage = page.coerceAtLeast(0)

        val sortOrder = if (sort.equals("popular", ignoreCase = true)) {
            Sort.by(Sort.Order.desc("likeCount"), Sort.Order.desc("publishedAt"), Sort.Order.desc("id"))
        } else {
            Sort.by(Sort.Order.desc("publishedAt"), Sort.Order.desc("id"))
        }

        val pageable = PageRequest.of(boundedPage, boundedSize, sortOrder)
        val normalizedTag = tag?.trim()?.lowercase()
        val normalizedHex = hexColor?.trim()?.uppercase()
        val normalizedName = name?.trim()?.takeIf { it.isNotBlank() }

        val palettesPage = paletteRepository.findPublishedPalettes(
            name = normalizedName,
            tagSlug = normalizedTag,
            hexColor = normalizedHex,
            pageable = pageable
        )

        return mapToPagedResponse(palettesPage, currentUserId)
    }

    @Transactional(readOnly = true)
    fun listMyPalettes(userId: UUID, page: Int, size: Int): PagedResponse<PaletteResponse> {
        val boundedSize = size.coerceIn(1, 50)
        val boundedPage = page.coerceAtLeast(0)
        val pageable = PageRequest.of(boundedPage, boundedSize)

        val pageResult = paletteRepository.findAllByCreatedByOrderByCreatedAtDesc(userId, pageable)
        return mapToPagedResponse(pageResult, currentUserId = userId)
    }

    private fun mapToPagedResponse(page: Page<PaletteEntity>, currentUserId: UUID?): PagedResponse<PaletteResponse> {
        val paletteIds = page.content.map { it.id }
        val favoritedIds = if (currentUserId != null && paletteIds.isNotEmpty()) {
            favoriteRepository.findFavoritedPaletteIds(currentUserId, paletteIds).toSet()
        } else {
            emptySet()
        }

        val responses = page.content.map { entity ->
            toResponse(entity.toDomain(), isLiked = favoritedIds.contains(entity.id))
        }

        return PagedResponse(
            items = responses,
            metadata = com.serkanmusic.palette.shared.dto.PageMetadata(
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                hasNext = page.hasNext(),
                hasPrevious = page.hasPrevious()
            )
        )
    }

    private fun resolveTags(tagNames: Set<String>): List<TagEntity> {
        if (tagNames.isEmpty()) return emptyList()

        val normalizedSlugs = tagNames.map { it.trim().lowercase().replace(" ", "-") }.filter { it.isNotBlank() }.toSet()
        val existingTags = tagRepository.findAllBySlugIn(normalizedSlugs)
        val existingSlugs = existingTags.map { it.slug }.toSet()

        val newTags = normalizedSlugs.filterNot { existingSlugs.contains(it) }.map { slug ->
            TagEntity(
                slug = slug,
                displayName = slug.replace("-", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            )
        }

        val savedNewTags = if (newTags.isNotEmpty()) tagRepository.saveAll(newTags) else emptyList()
        return existingTags + savedNewTags
    }

    private fun toResponse(domain: Palette, isLiked: Boolean): PaletteResponse {
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
            likedByMe = isLiked
        )
    }
}
