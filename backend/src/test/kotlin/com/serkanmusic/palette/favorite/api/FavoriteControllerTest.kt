package com.serkanmusic.palette.favorite.api

import com.ninjasquad.springmockk.MockkBean
import com.serkanmusic.palette.favorite.application.FavoriteService
import com.serkanmusic.palette.identity.application.TokenService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FavoriteControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var favoriteService: FavoriteService

    @Autowired
    private lateinit var tokenService: TokenService

    @Test
    fun `favorite palette requires auth`() {
        val paletteId = UUID.randomUUID()
        mockMvc.perform(post("/api/v1/palettes/$paletteId/favorite"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `favorite palette with auth returns 204 No Content`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val token = tokenService.generateAccessToken(userId, "user@example.com", "USER")

        every { favoriteService.favoritePalette(userId, paletteId) } returns Unit

        mockMvc.perform(
            post("/api/v1/palettes/$paletteId/favorite")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `unfavorite palette with auth returns 204 No Content`() {
        val userId = UUID.randomUUID()
        val paletteId = UUID.randomUUID()
        val token = tokenService.generateAccessToken(userId, "user@example.com", "USER")

        every { favoriteService.unfavoritePalette(userId, paletteId) } returns Unit

        mockMvc.perform(
            delete("/api/v1/palettes/$paletteId/favorite")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `get user favorites returns list with likedByMe true`() {
        val userId = UUID.randomUUID()
        val token = tokenService.generateAccessToken(userId, "user@example.com", "USER")
        val pagedResponse = PagedResponse(
            items = listOf(
                PaletteResponse(
                    id = UUID.randomUUID(),
                    name = "Favorited",
                    status = "PUBLISHED",
                    likeCount = 1,
                    colors = listOf("#111111", "#222222", "#333333", "#444444"),
                    tags = emptyList(),
                    createdBy = UUID.randomUUID(),
                    createdAt = Instant.now(),
                    publishedAt = Instant.now(),
                    likedByMe = true
                )
            ),
            metadata = PageMetadata(0, 20, 1, 1, false, false)
        )

        every { favoriteService.getUserFavorites(userId, 0, 20) } returns pagedResponse

        mockMvc.perform(
            get("/api/v1/me/favorites")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.items[0].likedByMe").value(true))
    }
}
