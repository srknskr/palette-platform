package com.serkanmusic.palette.palette.infrastructure.persistence

import com.serkanmusic.palette.palette.domain.Palette
import com.serkanmusic.palette.palette.domain.PaletteColor
import com.serkanmusic.palette.palette.domain.PaletteStatus
import com.serkanmusic.palette.palette.domain.Tag
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "palettes")
class PaletteEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "created_by", nullable = false)
    val createdBy: UUID,

    @Column(nullable = false, length = 80)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: PaletteStatus = PaletteStatus.DRAFT,

    @Column(name = "like_count", nullable = false)
    var likeCount: Long = 0,

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "palette_id")
    @OrderBy("position ASC")
    var colors: MutableList<PaletteColorEntity> = mutableListOf(),

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "palette_tags",
        joinColumns = [JoinColumn(name = "palette_id")],
        inverseJoinColumns = [JoinColumn(name = "tag_id")]
    )
    var tags: MutableSet<TagEntity> = mutableSetOf(),

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "published_at")
    var publishedAt: Instant? = null
) {
    fun toDomain(): Palette = Palette(
        id = id,
        createdBy = createdBy,
        name = name,
        status = status,
        likeCount = likeCount,
        colors = colors.map { PaletteColor(it.id.position.toInt(), it.hexValue) },
        tags = tags.map { Tag(it.id, it.slug, it.displayName) }.toSet(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        publishedAt = publishedAt
    )
}
