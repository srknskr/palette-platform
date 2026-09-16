package com.serkanmusic.palette.identity.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshTokenEntity, UUID> {
    fun findByTokenHash(tokenHash: String): Optional<RefreshTokenEntity>
    fun findAllByUserId(userId: UUID): List<RefreshTokenEntity>
}
