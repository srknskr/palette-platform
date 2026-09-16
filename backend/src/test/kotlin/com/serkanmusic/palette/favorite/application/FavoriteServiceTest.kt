package com.serkanmusic.palette.favorite.application

import com.serkanmusic.palette.favorite.infrastructure.persistence.FavoriteEntity
import com.serkanmusic.palette.favorite.infrastructure.persistence.FavoriteId
import com.serkanmusic.palette.favorite.infrastructure.persistence.FavoriteRepository
import com.serkanmusic.palette.palette.domain.PaletteStatus
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteColorEntity
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteColorId
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteEntity
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteRepository
import com.serkanmusic.palette.shared.error.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class FavoriteServiceTest {

    private val favoriteRepository: FavoriteRepository = mockk()
    private val paletteRepository: PaletteRepository = mockk()

    private val favoriteService = FavoriteService(
        favoriteRepository,
        paletteRepository
    )

    @Test
    fun `favoritePalette increments like count on first attempt`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val palette = PaletteEntity(
            id = paletteId,
            createdBy = UUID.randomUUID(),
            name = "Test",
            status = PaletteStatus.PUBLISHED,
            likeCount = 0
        )
        val favoriteId = FavoriteId(userId, paletteId)

        every { paletteRepository.findById(paletteId) } returns Optional.of(palette)
        every { favoriteRepository.existsById(favoriteId) } returns false
        every { favoriteRepository.save(any()) } answers { firstArg<FavoriteEntity>() }
        every { paletteRepository.incrementLikeCount(paletteId) } returns 1

        favoriteService.favoritePalette(userId, paletteId)

        verify(exactly = 1) { favoriteRepository.save(any()) }
        verify(exactly = 1) { paletteRepository.incrementLikeCount(paletteId) }
    }

    @Test
    fun `favoritePalette is idempotent when already favorited`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val palette = PaletteEntity(
            id = paletteId,
            createdBy = UUID.randomUUID(),
            name = "Test",
            status = PaletteStatus.PUBLISHED,
            likeCount = 1
        )
        val favoriteId = FavoriteId(userId, paletteId)

        every { paletteRepository.findById(paletteId) } returns Optional.of(palette)
        every { favoriteRepository.existsById(favoriteId) } returns true

        favoriteService.favoritePalette(userId, paletteId)

        verify(exactly = 0) { favoriteRepository.save(any()) }
        verify(exactly = 0) { paletteRepository.incrementLikeCount(paletteId) }
    }

    @Test
    fun `favoritePalette throws ResourceNotFoundException if palette does not exist`() {
        val userId = UUID.randomUUID()
        val missingId = UUID.randomUUID()

        every { paletteRepository.findById(missingId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            favoriteService.favoritePalette(userId, missingId)
        }
    }

    @Test
    fun `unfavoritePalette decrements count when previously favorited`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val favoriteId = FavoriteId(userId, paletteId)

        every { favoriteRepository.existsById(favoriteId) } returns true
        every { favoriteRepository.deleteById(favoriteId) } returns Unit
        every { paletteRepository.decrementLikeCount(paletteId) } returns 1

        favoriteService.unfavoritePalette(userId, paletteId)

        verify(exactly = 1) { favoriteRepository.deleteById(favoriteId) }
        verify(exactly = 1) { paletteRepository.decrementLikeCount(paletteId) }
    }

    @Test
    fun `unfavoritePalette is safely idempotent when not currently favorited`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val favoriteId = FavoriteId(userId, paletteId)

        every { favoriteRepository.existsById(favoriteId) } returns false

        favoriteService.unfavoritePalette(userId, paletteId)

        verify(exactly = 0) { favoriteRepository.deleteById(favoriteId) }
        verify(exactly = 0) { paletteRepository.decrementLikeCount(paletteId) }
    }

    @Test
    fun `getUserFavorites returns list of favorited palettes`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val favorite = FavoriteEntity(
            id = FavoriteId(userId, paletteId),
            createdAt = Instant.now()
        )
        val palette = PaletteEntity(
            id = paletteId,
            createdBy = UUID.randomUUID(),
            name = "Fav Palette",
            status = PaletteStatus.PUBLISHED,
            likeCount = 3
        )
        palette.colors = mutableListOf(
            PaletteColorEntity(PaletteColorId(paletteId, 0), "#111111"),
            PaletteColorEntity(PaletteColorId(paletteId, 1), "#222222"),
            PaletteColorEntity(PaletteColorId(paletteId, 2), "#333333"),
            PaletteColorEntity(PaletteColorId(paletteId, 3), "#444444")
        )

        val page = PageImpl(listOf(favorite))
        every { favoriteRepository.findAllByIdUserIdOrderByCreatedAtDesc(userId, any()) } returns page
        every { paletteRepository.findAllById(listOf(paletteId)) } returns listOf(palette)

        val response = favoriteService.getUserFavorites(userId, 0, 20)

        assertEquals(1, response.items.size)
        assertEquals("Fav Palette", response.items[0].name)
        assertEquals(true, response.items[0].likedByMe)
    }
}
