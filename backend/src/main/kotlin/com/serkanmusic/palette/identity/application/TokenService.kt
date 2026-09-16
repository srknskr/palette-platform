package com.serkanmusic.palette.identity.application

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Service
class TokenService(
    @Value("\${jwt.secret:developmentSecretKeyMustBeAtLeast32BytesLongForHS256Security123456}")
    private val jwtSecret: String,
    @Value("\${jwt.access-token-expiration-minutes:15}")
    private val accessTokenExpirationMinutes: Long,
    @Value("\${jwt.refresh-token-expiration-days:7}")
    private val refreshTokenExpirationDays: Long
) {
    private val secureRandom = SecureRandom()

    private val signingKey: SecretKey by lazy {
        val keyBytes = jwtSecret.toByteArray(StandardCharsets.UTF_8)
        if (keyBytes.size < 32) {
            val padded = ByteArray(32)
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.size)
            Keys.hmacShaKeyFor(padded)
        } else {
            Keys.hmacShaKeyFor(keyBytes)
        }
    }

    fun generateAccessToken(userId: UUID, email: String, role: String): String {
        val now = Instant.now()
        val expiry = now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey)
            .compact()
    }

    fun parseAccessToken(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    fun generateRefreshToken(): String {
        val randomBytes = ByteArray(64)
        secureRandom.nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }

    fun hashRefreshToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(token.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashedBytes)
    }

    fun calculateRefreshTokenExpiry(): Instant {
        return Instant.now().plus(refreshTokenExpirationDays, ChronoUnit.DAYS)
    }
}
