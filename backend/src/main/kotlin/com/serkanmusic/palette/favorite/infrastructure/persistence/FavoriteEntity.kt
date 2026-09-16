package com.serkanmusic.palette.favorite.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.io.Serializable
import java.time.Instant
import java.util.UUID

@Embeddable
data class FavoriteId(
    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID(),

    @Column(name = "palette_id", nullable = false)
    var paletteId: UUID = UUID.randomUUID()
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

@Entity
@Table(name = "favorites")
class FavoriteEntity(
    @EmbeddedId
    var id: FavoriteId = FavoriteId(),

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
