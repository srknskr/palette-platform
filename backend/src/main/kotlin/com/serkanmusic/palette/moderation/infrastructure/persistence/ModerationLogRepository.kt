package com.serkanmusic.palette.moderation.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ModerationLogRepository : JpaRepository<ModerationLogEntity, UUID> {
    fun findAllByPaletteIdOrderByCreatedAtDesc(paletteId: UUID): List<ModerationLogEntity>
}
