package com.serkanmusic.palette.moderation.application

import com.serkanmusic.palette.moderation.api.dto.ModerationActionRequest
import com.serkanmusic.palette.moderation.domain.ModerationAction
import com.serkanmusic.palette.moderation.infrastructure.persistence.ModerationLogEntity
import com.serkanmusic.palette.moderation.infrastructure.persistence.ModerationLogRepository
import com.serkanmusic.palette.palette.api.dto.PaletteResponse
import com.serkanmusic.palette.palette.domain.PaletteStatus
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteEntity
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
class ModerationService(
    private val paletteRepository: PaletteRepository,
    private val moderationLogRepository: ModerationLogRepository
) {

    @Transactional(readOnly = true)
    fun getPendingPalettes(page: Int, size: Int): PagedResponse<PaletteResponse> {
        val boundedSize = size.coerceIn(1, 50)
        val boundedPage = page.coerceAtLeast(0)
        val pageable = PageRequest.of(boundedPage, boundedSize)

        val pendingPage = paletteRepository.findAllByStatus(PaletteStatus.PENDING, pageable)
        val items = pendingPage.content.map { toResponse(it) }

        return PagedResponse(
            items = items,
            metadata = PageMetadata(
                page = pendingPage.number,
                size = pendingPage.size,
                totalElements = pendingPage.totalElements,
                totalPages = pendingPage.totalPages,
                hasNext = pendingPage.hasNext(),
                hasPrevious = pendingPage.hasPrevious()
            )
        )
    }

    @Transactional
    fun publishPalette(paletteId: UUID, adminId: UUID, request: ModerationActionRequest?): PaletteResponse {
        val palette = paletteRepository.findById(paletteId)
            .orElseThrow { ResourceNotFoundException("Palette with id $paletteId not found") }

        val now = Instant.now()
        palette.status = PaletteStatus.PUBLISHED
        palette.updatedAt = now
        palette.publishedAt = now
        val updated = paletteRepository.save(palette)

        logAction(paletteId, adminId, ModerationAction.PUBLISH, request?.reason, now)

        return toResponse(updated)
    }

    @Transactional
    fun rejectPalette(paletteId: UUID, adminId: UUID, request: ModerationActionRequest?): PaletteResponse {
        val palette = paletteRepository.findById(paletteId)
            .orElseThrow { ResourceNotFoundException("Palette with id $paletteId not found") }

        val now = Instant.now()
        palette.status = PaletteStatus.REJECTED
        palette.updatedAt = now
        val updated = paletteRepository.save(palette)

        logAction(paletteId, adminId, ModerationAction.REJECT, request?.reason, now)

        return toResponse(updated)
    }

    @Transactional
    fun archivePalette(paletteId: UUID, adminId: UUID, request: ModerationActionRequest?): PaletteResponse {
        val palette = paletteRepository.findById(paletteId)
            .orElseThrow { ResourceNotFoundException("Palette with id $paletteId not found") }

        val now = Instant.now()
        palette.status = PaletteStatus.ARCHIVED
        palette.updatedAt = now
        val updated = paletteRepository.save(palette)

        logAction(paletteId, adminId, ModerationAction.ARCHIVE, request?.reason, now)

        return toResponse(updated)
    }

    private fun logAction(paletteId: UUID, actorId: UUID, action: ModerationAction, reason: String?, now: Instant) {
        val log = ModerationLogEntity(
            paletteId = paletteId,
            actorId = actorId,
            action = action,
            reason = reason,
            createdAt = now
        )
        moderationLogRepository.save(log)
    }

    private fun toResponse(entity: PaletteEntity): PaletteResponse {
        val domain = entity.toDomain()
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
            likedByMe = false
        )
    }
}
