package com.serkanmusic.palette.shared.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class RateLimitFilter(
    private val requestsPerMinute: Int = 120
) : OncePerRequestFilter() {

    private val requestCounts = ConcurrentHashMap<String, MutableList<Long>>()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI
        val isRateLimitedPath = path.startsWith("/api/v1/auth/") ||
            (request.method in listOf("POST", "PUT", "DELETE") && path.startsWith("/api/v1/"))

        if (!isRateLimitedPath) {
            filterChain.doFilter(request, response)
            return
        }

        val clientKey = getClientKey(request)
        val now = Instant.now().toEpochMilli()
        val oneMinuteAgo = now - 60_000

        val timestamps = requestCounts.compute(clientKey) { _, list ->
            val updated = (list ?: mutableListOf()).filter { it > oneMinuteAgo }.toMutableList()
            updated.add(now)
            updated
        }

        if (timestamps != null && timestamps.size > requestsPerMinute) {
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = MediaType.APPLICATION_PROBLEM_JSON_VALUE
            response.writer.write(
                """{"type":"about:blank","title":"Too Many Requests","status":429,"detail":"Rate limit exceeded. Try again later."}"""
            )
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun getClientKey(request: HttpServletRequest): String {
        val forwarded = request.getHeader("X-Forwarded-For")
        val ip = if (!forwarded.isNullOrBlank()) forwarded.split(",")[0].trim() else request.remoteAddr
        return "${request.method}:${request.requestURI}:$ip"
    }
}
