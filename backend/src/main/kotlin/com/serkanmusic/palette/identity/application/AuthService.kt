package com.serkanmusic.palette.identity.application

import com.serkanmusic.palette.identity.api.dto.AuthResponse
import com.serkanmusic.palette.identity.api.dto.LoginRequest
import com.serkanmusic.palette.identity.api.dto.RefreshRequest
import com.serkanmusic.palette.identity.api.dto.RegisterRequest
import com.serkanmusic.palette.identity.api.dto.UserResponse
import com.serkanmusic.palette.identity.domain.Role
import com.serkanmusic.palette.identity.domain.User
import com.serkanmusic.palette.identity.infrastructure.persistence.RefreshTokenEntity
import com.serkanmusic.palette.identity.infrastructure.persistence.RefreshTokenRepository
import com.serkanmusic.palette.identity.infrastructure.persistence.UserEntity
import com.serkanmusic.palette.identity.infrastructure.persistence.UserRepository
import com.serkanmusic.palette.shared.error.ConflictException
import com.serkanmusic.palette.shared.error.ResourceNotFoundException
import com.serkanmusic.palette.shared.error.UnauthorizedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val normalizedEmail = request.email.trim().lowercase()

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw ConflictException("User with email $normalizedEmail already exists")
        }

        val userEntity = UserEntity(
            email = normalizedEmail,
            passwordHash = passwordEncoder.encode(request.password),
            displayName = request.displayName.trim(),
            role = Role.USER
        )
        val saved = userRepository.save(userEntity)

        return generateTokens(saved.toDomain())
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val userEntity = userRepository.findByEmail(normalizedEmail)
            .orElseThrow { UnauthorizedException("Invalid email or password") }

        if (!passwordEncoder.matches(request.password, userEntity.passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }

        return generateTokens(userEntity.toDomain())
    }

    @Transactional
    fun refresh(request: RefreshRequest): AuthResponse {
        val tokenHash = tokenService.hashRefreshToken(request.refreshToken)
        val tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow { UnauthorizedException("Invalid or expired refresh token") }

        if (tokenEntity.revokedAt != null || tokenEntity.expiresAt.isBefore(Instant.now())) {
            throw UnauthorizedException("Invalid or expired refresh token")
        }

        tokenEntity.revokedAt = Instant.now()
        refreshTokenRepository.save(tokenEntity)

        val userEntity = userRepository.findById(tokenEntity.userId)
            .orElseThrow { UnauthorizedException("User not found") }

        return generateTokens(userEntity.toDomain())
    }

    @Transactional
    fun logout(refreshToken: String?, userId: UUID?) {
        if (!refreshToken.isNullOrBlank()) {
            val tokenHash = tokenService.hashRefreshToken(refreshToken)
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent {
                it.revokedAt = Instant.now()
                refreshTokenRepository.save(it)
            }
        } else if (userId != null) {
            val activeTokens = refreshTokenRepository.findAllByUserId(userId)
            val now = Instant.now()
            activeTokens.forEach { token ->
                if (token.revokedAt == null) {
                    token.revokedAt = now
                }
            }
            refreshTokenRepository.saveAll(activeTokens)
        }
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }
        return UserResponse(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            role = user.role.name,
            createdAt = user.createdAt
        )
    }

    private fun generateTokens(user: User): AuthResponse {
        val accessToken = tokenService.generateAccessToken(user.id, user.email, user.role.name)
        val rawRefreshToken = tokenService.generateRefreshToken()
        val hashedRefreshToken = tokenService.hashRefreshToken(rawRefreshToken)
        val expiresAt = tokenService.calculateRefreshTokenExpiry()

        val tokenEntity = RefreshTokenEntity(
            userId = user.id,
            tokenHash = hashedRefreshToken,
            expiresAt = expiresAt
        )
        refreshTokenRepository.save(tokenEntity)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            user = UserResponse(
                id = user.id,
                email = user.email,
                displayName = user.displayName,
                role = user.role.name,
                createdAt = user.createdAt
            )
        )
    }
}
