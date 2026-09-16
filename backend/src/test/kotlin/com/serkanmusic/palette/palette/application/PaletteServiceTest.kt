package com.serkanmusic.palette.palette.application

import com.serkanmusic.palette.favorite.infrastructure.persistence.FavoriteRepository
import com.serkanmusic.palette.palette.api.dto.CreatePaletteRequest
import com.serkanmusic.palette.palette.api.dto.UpdatePaletteRequest
import com.serkanmusic.palette.palette.domain.PaletteStatus
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteColorEntity
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteColorId
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteEntity
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteRepository
import com.serkanmusic.palette.palette.infrastructure.persistence.TagEntity
import com.serkanmusic.palette.palette.infrastructure.persistence.TagRepository
import com.serkanmusic.palette.shared.error.ForbiddenException
import com.serkanmusic.palette.shared.error.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals

class PaletteServiceTest {

    private val paletteRepository: PaletteRepository = mockk()
    private val tagRepository: TagRepository = mockk()
    private val favoriteRepository: FavoriteRepository = mockk()

    private val paletteService = PaletteService(
        paletteRepository,
        tagRepository,
        favoriteRepository
    )

    @Test
    fun `createPalette persists published palette with 4 colors`() {
        val userId = UUID.randomUUID()
        val request = CreatePaletteRequest(
            name = "Warm Sunset",
            colors = listOf("#ff5733", "#33ff57", "#3357ff", "#f3ff33"),
            tags = setOf("sunset", "warm"),
            publish = true
        )

        every { tagRepository.findAllBySlugIn(any()) } returns emptyList()
        every { tagRepository.saveAll<TagEntity>(any()) } answers { firstArg<List<TagEntity>>() }
        every { paletteRepository.save(any()) } answers { firstArg<PaletteEntity>() }

        val result = paletteService.createPalette(userId, request)

        assertEquals("Warm Sunset", result.name)
        assertEquals(PaletteStatus.PUBLISHED.name, result.status)
        assertEquals(4, result.colors.size)
        assertEquals(listOf("#FF5733", "#33FF57", "#3357FF", "#F3FF33"), result.colors)
        verify { paletteRepository.save(any()) }
    }

    @Test
    fun `createPalette with invalid colors throws exception`() {
        val userId = UUID.randomUUID()
        val request = CreatePaletteRequest(
            name = "Bad Palette",
            colors = listOf("#FF5733", "#33FF57", "#3357FF"),
            tags = emptySet(),
            publish = true
        )

        assertThrows<IllegalArgumentException> {
            paletteService.createPalette(userId, request)
        }
    }

    @Test
    fun `updatePalette updates own palette successfully`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val existing = PaletteEntity(
            id = paletteId,
            createdBy = userId,
            name = "Old Name",
            status = PaletteStatus.PUBLISHED,
            likeCount = 5,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        existing.colors = mutableListOf(
            PaletteColorEntity(PaletteColorId(paletteId, 0), "#111111"),
            PaletteColorEntity(PaletteColorId(paletteId, 1), "#222222"),
            PaletteColorEntity(PaletteColorId(paletteId, 2), "#333333"),
            PaletteColorEntity(PaletteColorId(paletteId, 3), "#444444")
        )

        val request = UpdatePaletteRequest(
            name = "Updated Name",
            colors = listOf("#AAAAAA", "#BBBBBB", "#CCCCCC", "#DDDDDD"),
            tags = setOf("fresh")
        )

        every { paletteRepository.findById(paletteId) } returns Optional.of(existing)
        every { tagRepository.findAllBySlugIn(any()) } returns emptyList()
        every { tagRepository.saveAll<TagEntity>(any()) } answers { firstArg<List<TagEntity>>() }
        every { paletteRepository.save(existing) } returns existing
        every { favoriteRepository.existsByIdUserIdAndIdPaletteId(userId, paletteId) } returns true

        val result = paletteService.updatePalette(paletteId, userId, request)

        assertEquals("Updated Name", result.name)
        assertEquals(listOf("#AAAAAA", "#BBBBBB", "#CCCCCC", "#DDDDDD"), result.colors)
        assertEquals(true, result.likedByMe)
    }

    @Test
    fun `updatePalette forbidden for non-owner`() {
        val ownerId = UUID.randomUUID()
        val strangerId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val existing = PaletteEntity(
            id = paletteId,
            createdBy = ownerId,
            name = "Owner Palette",
            status = PaletteStatus.PUBLISHED,
            likeCount = 0,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val request = UpdatePaletteRequest(
            name = "Hacked Name",
            colors = listOf("#AAAAAA", "#BBBBBB", "#CCCCCC", "#DDDDDD"),
            tags = emptySet()
        )

        every { paletteRepository.findById(paletteId) } returns Optional.of(existing)

        assertThrows<ForbiddenException> {
            paletteService.updatePalette(paletteId, strangerId, request)
        }
    }

    @Test
    fun `deletePalette by owner succeeds`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val existing = PaletteEntity(
            id = paletteId,
            createdBy = userId,
            name = "To Delete",
            status = PaletteStatus.PUBLISHED,
            likeCount = 0,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { paletteRepository.findById(paletteId) } returns Optional.of(existing)
        every { paletteRepository.delete(existing) } returns Unit

        paletteService.deletePalette(paletteId, userId, isAdmin = false)

        verify { paletteRepository.delete(existing) }
    }

    @Test
    fun `deletePalette by non-owner without admin role throws ForbiddenException`() {
        val ownerId = UUID.randomUUID()
        val strangerId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val existing = PaletteEntity(
            id = paletteId,
            createdBy = ownerId,
            name = "Protected",
            status = PaletteStatus.PUBLISHED,
            likeCount = 0,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { paletteRepository.findById(paletteId) } returns Optional.of(existing)

        assertThrows<ForbiddenException> {
            paletteService.deletePalette(paletteId, strangerId, isAdmin = false)
        }
    }

    @Test
    fun `deletePalette by admin succeeds even if not owner`() {
        val ownerId = UUID.randomUUID()
        val adminId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val existing = PaletteEntity(
            id = paletteId,
            createdBy = ownerId,
            name = "Target",
            status = PaletteStatus.PUBLISHED,
            likeCount = 0,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        every { paletteRepository.findById(paletteId) } returns Optional.of(existing)
        every { paletteRepository.delete(existing) } returns Unit

        paletteService.deletePalette(paletteId, adminId, isAdmin = true)

        verify { paletteRepository.delete(existing) }
    }

    @Test
    fun `getPaletteById throws ResourceNotFoundException if not found`() {
        val missingId = UUID.randomUUID()
        every { paletteRepository.findById(missingId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            paletteService.getPaletteById(missingId, null)
        }
    }

    @Test
    fun `listPalettes maps results to PagedResponse`() {
        val paletteId = UUID.randomUUID()
        val entity = PaletteEntity(
            id = paletteId,
            createdBy = UUID.randomUUID(),
            name = "Autumn",
            status = PaletteStatus.PUBLISHED,
            likeCount = 10,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            publishedAt = Instant.now()
        )
        entity.colors = mutableListOf(
            PaletteColorEntity(PaletteColorId(paletteId, 0), "#111111"),
            PaletteColorEntity(PaletteColorId(paletteId, 1), "#222222"),
            PaletteColorEntity(PaletteColorId(paletteId, 2), "#333333"),
            PaletteColorEntity(PaletteColorId(paletteId, 3), "#444444")
        )

        val page = PageImpl(listOf(entity))
        every { paletteRepository.findPublishedPalettes(any(), any(), any(), any()) } returns page
        every { favoriteRepository.findFavoritedPaletteIds(any(), any()) } returns emptyList()

        val result = paletteService.listPalettes(null, null, null, "popular", 0, 20, UUID.randomUUID())

        assertEquals(1, result.items.size)
        assertEquals("Autumn", result.items[0].name)
        assertEquals(10L, result.items[0].likeCount)
    }
}
