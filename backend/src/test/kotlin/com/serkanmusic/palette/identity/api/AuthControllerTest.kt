package com.serkanmusic.palette.identity.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.serkanmusic.palette.identity.api.dto.AuthResponse
import com.serkanmusic.palette.identity.api.dto.LoginRequest
import com.serkanmusic.palette.identity.api.dto.RefreshRequest
import com.serkanmusic.palette.identity.api.dto.RegisterRequest
import com.serkanmusic.palette.identity.api.dto.UserResponse
import com.serkanmusic.palette.identity.application.AuthService
import com.serkanmusic.palette.identity.application.TokenService
import com.serkanmusic.palette.shared.error.ConflictException
import com.serkanmusic.palette.shared.error.UnauthorizedException
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
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
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var tokenService: TokenService

    @Test
    fun `register with valid payload returns 201 Created`() {
        val request = RegisterRequest(
            email = "tester@example.com",
            password = "securePassword123",
            displayName = "Tester"
        )
        val response = AuthResponse(
            accessToken = "jwt-access-token",
            refreshToken = "opaque-refresh-token",
            user = UserResponse(
                id = UUID.randomUUID(),
                email = "tester@example.com",
                displayName = "Tester",
                role = "USER",
                createdAt = Instant.now()
            )
        )

        every { authService.register(any()) } returns response

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.accessToken").value("jwt-access-token"))
            .andExpect(jsonPath("$.refreshToken").value("opaque-refresh-token"))
            .andExpect(jsonPath("$.user.email").value("tester@example.com"))
    }

    @Test
    fun `register with invalid email returns 400 ProblemDetail`() {
        val request = RegisterRequest(
            email = "invalid-email-format",
            password = "securePassword123",
            displayName = "Tester"
        )

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.email").exists())
    }

    @Test
    fun `register with duplicate email returns 409 Conflict`() {
        val request = RegisterRequest(
            email = "duplicate@example.com",
            password = "securePassword123",
            displayName = "Tester"
        )

        every { authService.register(any()) } throws ConflictException("User with email duplicate@example.com already exists")

        mockMvc.perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("User with email duplicate@example.com already exists"))
    }

    @Test
    fun `login with valid credentials returns tokens`() {
        val request = LoginRequest(
            email = "tester@example.com",
            password = "securePassword123"
        )
        val response = AuthResponse(
            accessToken = "jwt-access-token",
            refreshToken = "opaque-refresh-token",
            user = UserResponse(
                id = UUID.randomUUID(),
                email = "tester@example.com",
                displayName = "Tester",
                role = "USER",
                createdAt = Instant.now()
            )
        )

        every { authService.login(any()) } returns response

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("jwt-access-token"))
    }

    @Test
    fun `login with wrong credentials returns 401 Unauthorized`() {
        val request = LoginRequest(
            email = "tester@example.com",
            password = "wrongPassword"
        )

        every { authService.login(any()) } throws UnauthorizedException("Invalid email or password")

        mockMvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
    }

    @Test
    fun `refresh token returns new tokens`() {
        val request = RefreshRequest(refreshToken = "valid-refresh-token")
        val response = AuthResponse(
            accessToken = "new-jwt-access-token",
            refreshToken = "new-opaque-refresh-token",
            user = UserResponse(
                id = UUID.randomUUID(),
                email = "tester@example.com",
                displayName = "Tester",
                role = "USER",
                createdAt = Instant.now()
            )
        )

        every { authService.refresh(any()) } returns response

        mockMvc.perform(
            post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("new-jwt-access-token"))
    }

    @Test
    fun `me without token returns 401 Unauthorized`() {
        mockMvc.perform(get("/api/v1/me"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.status").value(401))
    }

    @Test
    fun `me with valid token returns user profile`() {
        val userId = UUID.randomUUID()
        val token = tokenService.generateAccessToken(userId, "tester@example.com", "USER")
        val userResponse = UserResponse(
            id = userId,
            email = "tester@example.com",
            displayName = "Tester",
            role = "USER",
            createdAt = Instant.now()
        )

        every { authService.getCurrentUser(userId) } returns userResponse

        mockMvc.perform(
            get("/api/v1/me")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.email").value("tester@example.com"))
    }
}
