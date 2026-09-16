package com.serkanmusic.palette.moderation.infrastructure.persistence

import com.serkanmusic.palette.moderation.domain.ModerationAction
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "moderation_logs")
class ModerationLogEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "palette_id", nullable = false)
    val paletteId: UUID,

    @Column(name = "actor_id", nullable = false)
    val actorId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    val action: ModerationAction,

    @Column(length = 500)
    val reason: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
