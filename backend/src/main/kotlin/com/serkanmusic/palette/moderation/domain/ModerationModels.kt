package com.serkanmusic.palette.moderation.domain

import java.time.Instant
import java.util.UUID

enum class ModerationAction {
    PUBLISH,
    REJECT,
    ARCHIVE
}

data class ModerationLog(
    val id: UUID,
    val paletteId: UUID,
    val actorId: UUID,
    val action: ModerationAction,
    val reason: String?,
    val createdAt: Instant
)
