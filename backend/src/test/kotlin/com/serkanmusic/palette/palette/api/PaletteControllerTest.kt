package com.serkanmusic.palette.palette.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.serkanmusic.palette.identity.application.TokenService
import com.serkanmusic.palette.palette.api.dto.CreatePaletteRequest
import com.serkanmusic.palette.palette.api.dto.PaletteResponse
import com.serkanmusic.palette.palette.api.dto.UpdatePaletteRequest
import com.serkanmusic.palette.palette.application.PaletteService
import com.serkanmusic.palette.shared.dto.PageMetadata
import com.serkanmusic.palette.shared.dto.PagedResponse
import com.serkanmusic.palette.shared.error.ForbiddenException
import com.serkanmusic.palette.shared.error.ResourceNotFoundException
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaletteControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var paletteService: PaletteService

    @Autowired
    private lateinit var tokenService: TokenService

    @Test
    fun `public list palettes returns 200 without auth`() {
        val pagedResponse = PagedResponse(
            items = listOf(
                PaletteResponse(
                    id = UUID.randomUUID(),
                    name = "Palette 1",
                    status = "PUBLISHED",
                    likeCount = 10,
                    colors = listOf("#111111", "#222222", "#333333", "#444444"),
                    tags = listOf("dark"),
                    createdBy = UUID.randomUUID(),
                    createdAt = Instant.now(),
                    publishedAt = Instant.now()
                )
            ),
            metadata = PageMetadata(0, 20, 1, 1, false, false)
        )

        every { paletteService.listPalettes(any(), any(), any(), any(), any(), any(), any()) } returns pagedResponse

        mockMvc.perform(get("/api/v1/palettes"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].name").value("Palette 1"))
            .andExpect(jsonPath("$.metadata.totalElements").value(1))
    }

    @Test
    fun `get palette by id returns 404 when missing`() {
        val missingId = UUID.randomUUID()
        every { paletteService.getPaletteById(missingId, any()) } throws ResourceNotFoundException("Palette not found")

        mockMvc.perform(get("/api/v1/palettes/$missingId"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.status").value(404))
    }

    @Test
    fun `create palette requires authentication`() {
        val request = CreatePaletteRequest(
            name = "Test",
            colors = listOf("#111111", "#222222", "#333333", "#444444")
        )

        mockMvc.perform(
            post("/api/v1/palettes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `create palette with auth returns 201 Created`() {
        val userId = UUID.randomUUID()
        val token = tokenService.generateAccessToken(userId, "user@example.com", "USER")
        val request = CreatePaletteRequest(
            name = "Sunny",
            colors = listOf("#111111", "#222222", "#333333", "#444444")
        )
        val response = PaletteResponse(
            id = UUID.randomUUID(),
            name = "Sunny",
            status = "PUBLISHED",
            likeCount = 0,
            colors = listOf("#111111", "#222222", "#333333", "#444444"),
            tags = emptyList(),
            createdBy = userId,
            createdAt = Instant.now(),
            publishedAt = Instant.now()
        )

        every { paletteService.createPalette(userId, any()) } returns response

        mockMvc.perform(
            post("/api/v1/palettes")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value("Sunny"))
    }

    @Test
    fun `update palette forbidden when not owner`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val token = tokenService.generateAccessToken(userId, "user@example.com", "USER")
        val request = UpdatePaletteRequest(
            name = "Modified",
            colors = listOf("#111111", "#222222", "#333333", "#444444")
        )

        every { paletteService.updatePalette(paletteId, userId, any()) } throws ForbiddenException("Not authorized")

        mockMvc.perform(
            put("/api/v1/palettes/$paletteId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.status").value(403))
    }

    @Test
    fun `delete palette by owner returns 204 No Content`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val token = tokenService.generateAccessToken(userId, "user@example.com", "USER")

        every { paletteService.deletePalette(paletteId, userId, false) } returns Unit

        mockMvc.perform(
            delete("/api/v1/palettes/$paletteId")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isNoContent)
    }
}
