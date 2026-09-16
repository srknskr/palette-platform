package com.serkanmusic.palette.palette.domain

import java.time.Instant
import java.util.UUID

data class PaletteColor(
    val position: Int,
    val hexValue: String
)

data class Tag(
    val id: UUID,
    val slug: String,
    val displayName: String
)

data class Palette(
    val id: UUID,
    val createdBy: UUID,
    val name: String,
    val status: PaletteStatus,
    val likeCount: Long,
    val colors: List<PaletteColor>,
    val tags: Set<Tag>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val publishedAt: Instant?
)
