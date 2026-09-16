package com.serkanmusic.palette.identity.infrastructure.security

import com.serkanmusic.palette.identity.application.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class JwtAuthenticationFilter(
    private val tokenService: TokenService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            try {
                val claims = tokenService.parseAccessToken(token)
                val userId = UUID.fromString(claims.subject)
                val role = claims.get("role", String::class.java) ?: "USER"
                val email = claims.get("email", String::class.java) ?: ""

                val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
                val authentication = UsernamePasswordAuthenticationToken(
                    AuthenticatedUser(userId, email, role),
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
            } catch (@Suppress("SwallowedException", "TooGenericExceptionCaught") ex: Exception) {
                SecurityContextHolder.clearContext()
            }
        }

        filterChain.doFilter(request, response)
    }
}

data class AuthenticatedUser(
    val id: UUID,
    val email: String,
    val role: String
)
