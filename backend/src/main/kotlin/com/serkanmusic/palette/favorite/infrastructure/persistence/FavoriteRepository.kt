package com.serkanmusic.palette.favorite.infrastructure.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FavoriteRepository : JpaRepository<FavoriteEntity, FavoriteId> {
    fun existsByIdUserIdAndIdPaletteId(userId: UUID, paletteId: UUID): Boolean

    @Query("SELECT f.id.paletteId FROM FavoriteEntity f WHERE f.id.userId = :userId AND f.id.paletteId IN :paletteIds")
    fun findFavoritedPaletteIds(
        @Param("userId") userId: UUID,
        @Param("paletteIds") paletteIds: Collection<UUID>
    ): List<UUID>

    fun findAllByIdUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<FavoriteEntity>
}
