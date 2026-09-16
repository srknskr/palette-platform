package com.serkanmusic.palette.moderation.application

import com.serkanmusic.palette.moderation.api.dto.ModerationActionRequest
import com.serkanmusic.palette.moderation.domain.ModerationAction
import com.serkanmusic.palette.moderation.infrastructure.persistence.ModerationLogEntity
import com.serkanmusic.palette.moderation.infrastructure.persistence.ModerationLogRepository
import com.serkanmusic.palette.palette.domain.PaletteStatus
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteEntity
import com.serkanmusic.palette.palette.infrastructure.persistence.PaletteRepository
import com.serkanmusic.palette.shared.error.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ModerationServiceTest {

    private val paletteRepository: PaletteRepository = mockk()
    private val moderationLogRepository: ModerationLogRepository = mockk()

    private val moderationService = ModerationService(
        paletteRepository,
        moderationLogRepository
    )

    @Test
    fun `getPendingPalettes returns page of pending items`() {
        val palette = PaletteEntity(
            id = UUID.randomUUID(),
            createdBy = UUID.randomUUID(),
            name = "Pending 1",
            status = PaletteStatus.PENDING
        )
        every { paletteRepository.findAllByStatus(PaletteStatus.PENDING, any()) } returns PageImpl(listOf(palette))

        val response = moderationService.getPendingPalettes(0, 10)

        assertEquals(1, response.items.size)
        assertEquals("Pending 1", response.items[0].name)
        assertEquals(PaletteStatus.PENDING.name, response.items[0].status)
    }

    @Test
    fun `publishPalette changes status to PUBLISHED and logs action`() {
        val paletteId = UUID.randomUUID()
        val adminId = UUID.randomUUID()
        val palette = PaletteEntity(
            id = paletteId,
            createdBy = UUID.randomUUID(),
            name = "To Publish",
            status = PaletteStatus.PENDING
        )

        every { paletteRepository.findById(paletteId) } returns Optional.of(palette)
        every { paletteRepository.save(palette) } returns palette
        every { moderationLogRepository.save(any()) } answers { firstArg<ModerationLogEntity>() }

        val response = moderationService.publishPalette(paletteId, adminId, ModerationActionRequest("Approved by QA"))

        assertEquals(PaletteStatus.PUBLISHED.name, response.status)
        assertNotNull(response.publishedAt)
        verify(exactly = 1) {
            moderationLogRepository.save(match {
                it.action == ModerationAction.PUBLISH && it.reason == "Approved by QA" && it.actorId == adminId
            })
        }
    }

    @Test
    fun `rejectPalette changes status to REJECTED and logs action`() {
        val paletteId = UUID.randomUUID()
        val adminId = UUID.randomUUID()
        val palette = PaletteEntity(
            id = paletteId,
            createdBy = UUID.randomUUID(),
            name = "To Reject",
            status = PaletteStatus.PENDING
        )

        every { paletteRepository.findById(paletteId) } returns Optional.of(palette)
        every { paletteRepository.save(palette) } returns palette
        every { moderationLogRepository.save(any()) } answers { firstArg<ModerationLogEntity>() }

        val response = moderationService.rejectPalette(paletteId, adminId, ModerationActionRequest("Violates guidelines"))

        assertEquals(PaletteStatus.REJECTED.name, response.status)
        verify(exactly = 1) {
            moderationLogRepository.save(match {
                it.action == ModerationAction.REJECT && it.reason == "Violates guidelines" && it.actorId == adminId
            })
        }
    }

    @Test
    fun `archivePalette changes status to ARCHIVED and logs action`() {
        val paletteId = UUID.randomUUID()
        val adminId = UUID.randomUUID()
        val palette = PaletteEntity(
            id = paletteId,
            createdBy = UUID.randomUUID(),
            name = "To Archive",
            status = PaletteStatus.PUBLISHED
        )

        every { paletteRepository.findById(paletteId) } returns Optional.of(palette)
        every { paletteRepository.save(palette) } returns palette
        every { moderationLogRepository.save(any()) } answers { firstArg<ModerationLogEntity>() }

        val response = moderationService.archivePalette(paletteId, adminId, null)

        assertEquals(PaletteStatus.ARCHIVED.name, response.status)
        verify(exactly = 1) {
            moderationLogRepository.save(match {
                it.action == ModerationAction.ARCHIVE && it.reason == null && it.actorId == adminId
            })
        }
    }

    @Test
    fun `moderation actions throw ResourceNotFoundException for missing palette`() {
        val missingId = UUID.randomUUID()
        val adminId = UUID.randomUUID()
        every { paletteRepository.findById(missingId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            moderationService.publishPalette(missingId, adminId, null)
        }
    }
}
