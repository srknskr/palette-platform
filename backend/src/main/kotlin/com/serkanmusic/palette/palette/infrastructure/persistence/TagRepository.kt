package com.serkanmusic.palette.palette.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface TagRepository : JpaRepository<TagEntity, UUID> {
    fun findBySlug(slug: String): Optional<TagEntity>
    fun findAllBySlugIn(slugs: Collection<String>): List<TagEntity>
}
