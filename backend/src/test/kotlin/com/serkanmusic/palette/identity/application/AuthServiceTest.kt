package com.serkanmusic.palette.identity.application

import com.serkanmusic.palette.identity.api.dto.LoginRequest
import com.serkanmusic.palette.identity.api.dto.RefreshRequest
import com.serkanmusic.palette.identity.api.dto.RegisterRequest
import com.serkanmusic.palette.identity.domain.Role
import com.serkanmusic.palette.identity.domain.User
import com.serkanmusic.palette.identity.infrastructure.persistence.RefreshTokenEntity
import com.serkanmusic.palette.identity.infrastructure.persistence.RefreshTokenRepository
import com.serkanmusic.palette.identity.infrastructure.persistence.UserEntity
import com.serkanmusic.palette.identity.infrastructure.persistence.UserRepository
import com.serkanmusic.palette.shared.error.ConflictException
import com.serkanmusic.palette.shared.error.UnauthorizedException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AuthServiceTest {

    private val userRepository: UserRepository = mockk()
    private val refreshTokenRepository: RefreshTokenRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val tokenService: TokenService = mockk()

    private val authService = AuthService(
        userRepository,
        refreshTokenRepository,
        passwordEncoder,
        tokenService
    )

    @Test
    fun `register succeeds when email is available`() {
        val request = RegisterRequest(
            email = "test@example.com",
            password = "Password123!",
            displayName = "Tester"
        )
        val userId = UUID.randomUUID()
        val userEntity = UserEntity(
            id = userId,
            email = "test@example.com",
            passwordHash = "hashedPassword",
            displayName = "Tester",
            role = Role.USER
        )

        every { userRepository.existsByEmail("test@example.com") } returns false
        every { passwordEncoder.encode(request.password) } returns "hashedPassword"
        every { userRepository.save(any()) } returns userEntity
        every { tokenService.generateAccessToken(userId, "test@example.com", "USER") } returns "access-token-123"
        every { tokenService.generateRefreshToken() } returns "refresh-token-123"
        every { tokenService.hashRefreshToken("refresh-token-123") } returns "hashed-refresh-token"
        every { tokenService.calculateRefreshTokenExpiry() } returns Instant.now().plus(7, ChronoUnit.DAYS)
        every { refreshTokenRepository.save(any()) } returns mockk()

        val response = authService.register(request)

        assertEquals("access-token-123", response.accessToken)
        assertEquals("refresh-token-123", response.refreshToken)
        assertEquals("test@example.com", response.user.email)
        assertEquals("Tester", response.user.displayName)
    }

    @Test
    fun `register fails when email already exists`() {
        val request = RegisterRequest(
            email = "taken@example.com",
            password = "Password123!",
            displayName = "Tester"
        )

        every { userRepository.existsByEmail("taken@example.com") } returns true

        assertThrows<ConflictException> {
            authService.register(request)
        }
    }

    @Test
    fun `login succeeds with valid credentials`() {
        val request = LoginRequest("user@example.com", "validPass123")
        val userId = UUID.randomUUID()
        val userEntity = UserEntity(
            id = userId,
            email = "user@example.com",
            passwordHash = "hashedPassword",
            displayName = "User",
            role = Role.USER
        )

        every { userRepository.findByEmail("user@example.com") } returns Optional.of(userEntity)
        every { passwordEncoder.matches(request.password, "hashedPassword") } returns true
        every { tokenService.generateAccessToken(userId, "user@example.com", "USER") } returns "new-access-token"
        every { tokenService.generateRefreshToken() } returns "new-refresh-token"
        every { tokenService.hashRefreshToken("new-refresh-token") } returns "hashed-new-refresh-token"
        every { tokenService.calculateRefreshTokenExpiry() } returns Instant.now().plus(7, ChronoUnit.DAYS)
        every { refreshTokenRepository.save(any()) } returns mockk()

        val response = authService.login(request)

        assertEquals("new-access-token", response.accessToken)
        assertEquals("new-refresh-token", response.refreshToken)
    }

    @Test
    fun `login fails with invalid credentials`() {
        val request = LoginRequest("user@example.com", "wrongPass")
        val userEntity = UserEntity(
            id = UUID.randomUUID(),
            email = "user@example.com",
            passwordHash = "hashedPassword",
            displayName = "User",
            role = Role.USER
        )

        every { userRepository.findByEmail("user@example.com") } returns Optional.of(userEntity)
        every { passwordEncoder.matches(request.password, "hashedPassword") } returns false

        assertThrows<UnauthorizedException> {
            authService.login(request)
        }
    }

    @Test
    fun `refresh rotates token when valid`() {
        val rawToken = "valid-token"
        val hashedToken = "hashed-valid-token"
        val userId = UUID.randomUUID()
        val existingTokenEntity = RefreshTokenEntity(
            userId = userId,
            tokenHash = hashedToken,
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            revokedAt = null
        )
        val userEntity = UserEntity(
            id = userId,
            email = "user@example.com",
            passwordHash = "hashedPassword",
            displayName = "User",
            role = Role.USER
        )

        every { tokenService.hashRefreshToken(rawToken) } returns hashedToken
        every { refreshTokenRepository.findByTokenHash(hashedToken) } returns Optional.of(existingTokenEntity)
        every { refreshTokenRepository.save(any()) } returns existingTokenEntity
        every { userRepository.findById(userId) } returns Optional.of(userEntity)
        every { tokenService.generateAccessToken(userId, "user@example.com", "USER") } returns "next-access-token"
        every { tokenService.generateRefreshToken() } returns "next-refresh-token"
        every { tokenService.hashRefreshToken("next-refresh-token") } returns "hashed-next-refresh-token"
        every { tokenService.calculateRefreshTokenExpiry() } returns Instant.now().plus(7, ChronoUnit.DAYS)

        val response = authService.refresh(RefreshRequest(rawToken))

        assertNotNull(existingTokenEntity.revokedAt)
        assertEquals("next-access-token", response.accessToken)
        assertEquals("next-refresh-token", response.refreshToken)
    }

    @Test
    fun `refresh fails when token is revoked`() {
        val rawToken = "revoked-token"
        val hashedToken = "hashed-revoked-token"
        val existingTokenEntity = RefreshTokenEntity(
            userId = UUID.randomUUID(),
            tokenHash = hashedToken,
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS),
            revokedAt = Instant.now().minus(1, ChronoUnit.HOURS)
        )

        every { tokenService.hashRefreshToken(rawToken) } returns hashedToken
        every { refreshTokenRepository.findByTokenHash(hashedToken) } returns Optional.of(existingTokenEntity)

        assertThrows<UnauthorizedException> {
            authService.refresh(RefreshRequest(rawToken))
        }
    }

    @Test
    fun `logout revokes refresh token`() {
        val rawToken = "logout-token"
        val hashedToken = "hashed-logout-token"
        val tokenEntity = RefreshTokenEntity(
            userId = UUID.randomUUID(),
            tokenHash = hashedToken,
            expiresAt = Instant.now().plus(1, ChronoUnit.DAYS)
        )

        every { tokenService.hashRefreshToken(rawToken) } returns hashedToken
        every { refreshTokenRepository.findByTokenHash(hashedToken) } returns Optional.of(tokenEntity)
        every { refreshTokenRepository.save(tokenEntity) } returns tokenEntity

        authService.logout(rawToken, null)

        assertNotNull(tokenEntity.revokedAt)
        verify { refreshTokenRepository.save(tokenEntity) }
    }
}
