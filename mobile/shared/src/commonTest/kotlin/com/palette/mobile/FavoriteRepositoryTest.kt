package com.palette.mobile

import com.palette.mobile.auth.storage.InMemoryTokenStorage
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.favorite.repository.FavoriteRepository
import com.palette.mobile.network.ApiConfig
import com.palette.mobile.network.NetworkClient
import com.palette.mobile.palette.model.Palette
import com.palette.mobile.palette.repository.PaletteRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoriteRepositoryTest {

    @Test
    fun toggleFavoriteOptimisticallyUpdatesAndRollsBackOnError() = runTest {
        var shouldFail = false
        val mockEngine = MockEngine { request ->
            if (shouldFail) {
                respond(
                    content = """{"title":"Internal Server Error","status":500}""",
                    status = HttpStatusCode.InternalServerError,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            } else {
                respond(
                    content = "",
                    status = HttpStatusCode.NoContent,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val tokenStorage = InMemoryTokenStorage()
        tokenStorage.setAccessToken("valid_token")
        val networkClient = NetworkClient(ApiConfig(), tokenStorage, httpClient)
        val paletteRepository = PaletteRepository(networkClient)
        val favoriteRepository = FavoriteRepository(networkClient, paletteRepository)

        val palette = Palette(
            id = "pal-1",
            name = "Test",
            status = "PUBLISHED",
            likeCount = 10,
            colors = listOf("#111111", "#222222", "#333333", "#444444"),
            tags = listOf("test"),
            createdBy = "u1",
            createdAt = "2026-09-17T08:00:00Z",
            publishedAt = "2026-09-17T08:00:00Z",
            likedByMe = false
        )

        val successResult = favoriteRepository.toggleFavorite(palette)
        assertTrue(successResult is AppResult.Success)
        assertEquals(true, (successResult as AppResult.Success).data)
        assertEquals(1, favoriteRepository.cachedFavorites.first().size)

        shouldFail = true
        val favoritedPalette = palette.copy(likedByMe = true, likeCount = 11)
        val failResult = favoriteRepository.toggleFavorite(favoritedPalette)
        assertTrue(failResult is AppResult.Error)
        assertEquals(1, favoriteRepository.cachedFavorites.first().size)
    }
}
