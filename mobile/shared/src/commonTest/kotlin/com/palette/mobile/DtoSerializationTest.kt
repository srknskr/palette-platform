package com.palette.mobile

import com.palette.mobile.network.dto.AuthResponseDto
import com.palette.mobile.network.dto.PaletteResponseDto
import com.palette.mobile.network.dto.ProblemDetailDto
import com.palette.mobile.network.toDomain
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DtoSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun deserializePaletteResponseDtoAndMapToDomain() {
        val jsonString = """
            {
                "id": "c3f8e6c4-1234-5678-9abc-def012345678",
                "name": "Nordic Frost",
                "description": "A crisp winter palette",
                "status": "PUBLISHED",
                "likeCount": 42,
                "colors": ["#2E3440", "#4C566A", "#D8DEE9", "#ECEFF4"],
                "tags": ["nordic", "cool"],
                "createdBy": "a1b2c3d4-0000-0000-0000-000000000000",
                "createdAt": "2026-09-17T08:00:00Z",
                "publishedAt": "2026-09-17T08:05:00Z",
                "likedByMe": true
            }
        """.trimIndent()

        val dto = json.decodeFromString<PaletteResponseDto>(jsonString)
        val domain = dto.toDomain()

        assertEquals("c3f8e6c4-1234-5678-9abc-def012345678", domain.id)
        assertEquals("Nordic Frost", domain.name)
        assertEquals("A crisp winter palette", domain.paletteDescription)
        assertEquals(42L, domain.likeCount)
        assertEquals(4, domain.colors.size)
        assertTrue(domain.likedByMe)
    }

    @Test
    fun deserializeAuthResponseDto() {
        val jsonString = """
            {
                "accessToken": "access123",
                "refreshToken": "refresh456",
                "tokenType": "Bearer",
                "user": {
                    "id": "user123",
                    "email": "test@example.com",
                    "displayName": "Tester",
                    "role": "USER",
                    "createdAt": "2026-09-17T08:00:00Z"
                }
            }
        """.trimIndent()

        val dto = json.decodeFromString<AuthResponseDto>(jsonString)
        assertEquals("access123", dto.accessToken)
        assertEquals("test@example.com", dto.user.email)
    }

    @Test
    fun deserializeProblemDetailDto() {
        val jsonString = """
            {
                "type": "about:blank",
                "title": "Bad Request",
                "status": 400,
                "detail": "Validation failed",
                "errors": {
                    "email": "must be a well-formed email address"
                }
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProblemDetailDto>(jsonString)
        assertEquals(400, dto.status)
        assertEquals("Validation failed", dto.detail)
        assertEquals("must be a well-formed email address", dto.errors?.get("email"))
    }
}
