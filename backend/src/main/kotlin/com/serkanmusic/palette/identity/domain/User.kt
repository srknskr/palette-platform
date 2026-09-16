package com.serkanmusic.palette.identity.domain

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val displayName: String,
    val role: Role,
    val createdAt: Instant,
    val updatedAt: Instant
)
