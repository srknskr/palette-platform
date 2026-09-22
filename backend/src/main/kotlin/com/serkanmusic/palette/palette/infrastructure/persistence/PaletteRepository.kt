package com.serkanmusic.palette.palette.infrastructure.persistence

import com.serkanmusic.palette.palette.domain.PaletteStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
interface PaletteRepository : JpaRepository<PaletteEntity, UUID> {

    @EntityGraph(attributePaths = ["colors", "tags"])
    override fun findById(id: UUID): Optional<PaletteEntity>

    @Query(
        """
        SELECT p FROM PaletteEntity p
        WHERE p.status = 'PUBLISHED'
        AND (cast(:name as string) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', cast(:name as string), '%')))
        AND (cast(:tagSlug as string) IS NULL OR EXISTS (SELECT t FROM p.tags t WHERE t.slug = cast(:tagSlug as string)))
        AND (cast(:hexColor as string) IS NULL OR EXISTS (SELECT c FROM p.colors c WHERE UPPER(c.hexValue) = UPPER(cast(:hexColor as string))))
        """
    )
    fun findPublishedPalettes(
        @Param("name") name: String?,
        @Param("tagSlug") tagSlug: String?,
        @Param("hexColor") hexColor: String?,
        pageable: Pageable
    ): Page<PaletteEntity>

    fun findAllByCreatedByOrderByCreatedAtDesc(createdBy: UUID, pageable: Pageable): Page<PaletteEntity>

    fun findAllByStatus(status: PaletteStatus, pageable: Pageable): Page<PaletteEntity>

    @Query(
        value = "SELECT * FROM palettes WHERE status = 'PUBLISHED' ORDER BY RANDOM() LIMIT 1",
        nativeQuery = true
    )
    fun findRandomPublishedPaletteNative(): Optional<PaletteEntity>

    @Query(
        value = "SELECT * FROM palettes WHERE status = 'PUBLISHED' LIMIT 1",
        nativeQuery = true
    )
    fun findFirstPublishedPaletteNative(): Optional<PaletteEntity>

    @Modifying
    @Query("UPDATE PaletteEntity p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    fun incrementLikeCount(@Param("id") id: UUID): Int

    @Modifying
    @Query("UPDATE PaletteEntity p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :id")
    fun decrementLikeCount(@Param("id") id: UUID): Int

    fun countByStatus(status: PaletteStatus): Long
}
