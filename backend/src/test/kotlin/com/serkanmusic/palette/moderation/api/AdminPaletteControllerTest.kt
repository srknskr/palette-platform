package com.serkanmusic.palette.moderation.api

import com.ninjasquad.springmockk.MockkBean
import com.serkanmusic.palette.identity.application.TokenService
import com.serkanmusic.palette.moderation.application.ModerationService
import com.serkanmusic.palette.palette.api.dto.PaletteResponse
import com.serkanmusic.palette.shared.dto.PageMetadata
import com.serkanmusic.palette.shared.dto.PagedResponse
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminPaletteControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var moderationService: ModerationService

    @Autowired
    private lateinit var tokenService: TokenService

    @Test
    fun `pending palettes forbidden for unauthenticated user`() {
        mockMvc.perform(get("/api/v1/admin/palettes/pending"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `pending palettes forbidden for regular user`() {
        val userToken = tokenService.generateAccessToken(UUID.randomUUID(), "user@example.com", "USER")

        mockMvc.perform(
            get("/api/v1/admin/palettes/pending")
                .header("Authorization", "Bearer $userToken")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `pending palettes accessible for admin user`() {
        val adminId = UUID.randomUUID()
        val adminToken = tokenService.generateAccessToken(adminId, "admin@example.com", "ADMIN")
        val pagedResponse = PagedResponse(
            items = listOf(
                PaletteResponse(
                    id = UUID.randomUUID(),
                    name = "Pending Review",
                    status = "PENDING",
                    likeCount = 0,
                    colors = listOf("#111111", "#222222", "#333333", "#444444"),
                    tags = emptyList(),
                    createdBy = UUID.randomUUID(),
                    createdAt = Instant.now(),
                    publishedAt = null
                )
            ),
            metadata = PageMetadata(0, 20, 1, 1, false, false)
        )

        every { moderationService.getPendingPalettes(0, 20) } returns pagedResponse

        mockMvc.perform(
            get("/api/v1/admin/palettes/pending")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].status").value("PENDING"))
    }

    @Test
    fun `admin publish palette returns 200`() {
        val adminId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val adminToken = tokenService.generateAccessToken(adminId, "admin@example.com", "ADMIN")
        val response = PaletteResponse(
            id = paletteId,
            name = "Approved",
            status = "PUBLISHED",
            likeCount = 0,
            colors = listOf("#111111", "#222222", "#333333", "#444444"),
            tags = emptyList(),
            createdBy = UUID.randomUUID(),
            createdAt = Instant.now(),
            publishedAt = Instant.now()
        )

        every { moderationService.publishPalette(paletteId, adminId, any()) } returns response

        mockMvc.perform(
            post("/api/v1/admin/palettes/$paletteId/publish")
                .header("Authorization", "Bearer $adminToken")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("PUBLISHED"))
    }
}
