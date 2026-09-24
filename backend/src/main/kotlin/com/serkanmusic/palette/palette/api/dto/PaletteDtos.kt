package com.serkanmusic.palette.palette.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class CreatePaletteRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 80)
    val name: String,

    @field:Size(max = 250)
    val description: String? = null,

    @field:NotEmpty
    @field:Size(min = 4, max = 4)
    val colors: List<String>,

    val tags: Set<String> = emptySet(),

    val publish: Boolean = true
)

data class UpdatePaletteRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 80)
    val name: String,

    @field:Size(max = 250)
    val description: String? = null,

    @field:NotEmpty
    @field:Size(min = 4, max = 4)
    val colors: List<String>,

    val tags: Set<String> = emptySet()
)

data class PaletteResponse(
    val id: UUID,
    val name: String,
    val description: String?,
    val status: String,
    val likeCount: Long,
    val colors: List<String>,
    val tags: List<String>,
    val createdBy: UUID,
    val createdAt: Instant,
    val publishedAt: Instant?,
    val likedByMe: Boolean = false
)

data class PaletteCountResponse(
    val count: Long
)
