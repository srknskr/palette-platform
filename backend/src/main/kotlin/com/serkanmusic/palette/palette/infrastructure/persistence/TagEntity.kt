package com.serkanmusic.palette.palette.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tags")
class TagEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 60)
    var slug: String,

    @Column(name = "display_name", nullable = false, length = 60)
    var displayName: String
)
