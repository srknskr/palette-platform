package com.palette.mobile

import com.palette.mobile.auth.model.AuthState
import com.palette.mobile.auth.repository.AuthRepository
import com.palette.mobile.auth.storage.InMemoryTokenStorage
import com.palette.mobile.core.model.AppResult
import com.palette.mobile.network.ApiConfig
import com.palette.mobile.network.NetworkClient
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

class AuthRepositoryTest {

    @Test
    fun loginSuccessStoresTokensAndEmitsAuthenticated() = runTest {
        val mockEngine = MockEngine { request ->
            if (request.url.encodedPath.endsWith("/api/v1/auth/login")) {
                respond(
                    content = """
                        {
                            "accessToken": "acc123",
                            "refreshToken": "ref456",
                            "tokenType": "Bearer",
                            "user": {
                                "id": "u1",
                                "email": "test@example.com",
                                "displayName": "Tester",
                                "role": "USER",
                                "createdAt": "2026-09-17T08:00:00Z"
                            }
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            } else {
                error("Unhandled ${request.url.encodedPath}")
            }
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val tokenStorage = InMemoryTokenStorage()
        val networkClient = NetworkClient(ApiConfig(), tokenStorage, httpClient)
        val authRepository = AuthRepository(networkClient, tokenStorage)

        val result = authRepository.login("test@example.com", "Password123!")
        assertTrue(result is AppResult.Success)
        assertEquals("test@example.com", result.data.email)
        assertEquals("acc123", tokenStorage.getAccessToken())
        assertEquals("ref456", tokenStorage.getRefreshToken())

        val state = authRepository.authState.first()
        assertTrue(state is AuthState.Authenticated)
        assertEquals("test@example.com", (state as AuthState.Authenticated).user.email)
    }

    @Test
    fun logoutClearsStorageAndEmitsUnauthenticated() = runTest {
        val mockEngine = MockEngine { request ->
            respond(
                content = "",
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val tokenStorage = InMemoryTokenStorage()
        tokenStorage.setAccessToken("token")
        tokenStorage.setRefreshToken("ref")

        val networkClient = NetworkClient(ApiConfig(), tokenStorage, httpClient)
        val authRepository = AuthRepository(networkClient, tokenStorage)

        val result = authRepository.logout()
        assertTrue(result is AppResult.Success)
        assertEquals(null, tokenStorage.getAccessToken())
        assertEquals(null, tokenStorage.getRefreshToken())

        val state = authRepository.authState.first()
        assertTrue(state is AuthState.Unauthenticated)
    }
}
