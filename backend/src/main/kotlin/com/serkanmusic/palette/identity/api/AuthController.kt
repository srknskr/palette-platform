package com.serkanmusic.palette.identity.api

import com.serkanmusic.palette.identity.api.dto.AuthResponse
import com.serkanmusic.palette.identity.api.dto.LoginRequest
import com.serkanmusic.palette.identity.api.dto.LogoutRequest
import com.serkanmusic.palette.identity.api.dto.RefreshRequest
import com.serkanmusic.palette.identity.api.dto.RegisterRequest
import com.serkanmusic.palette.identity.api.dto.UserResponse
import com.serkanmusic.palette.identity.application.AuthService
import com.serkanmusic.palette.identity.infrastructure.security.AuthenticatedUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication & Identity")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Log in with email and password")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/auth/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<AuthResponse> {
        val response = authService.refresh(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Log out and revoke refresh token")
    fun logout(
        @RequestBody(required = false) request: LogoutRequest?,
        @AuthenticationPrincipal principal: AuthenticatedUser?
    ): ResponseEntity<Unit> {
        authService.logout(request?.refreshToken, principal?.id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", security = [SecurityRequirement(name = "bearerAuth")])
    fun getCurrentUser(@AuthenticationPrincipal principal: AuthenticatedUser): ResponseEntity<UserResponse> {
        val response = authService.getCurrentUser(principal.id)
        return ResponseEntity.ok(response)
    }
}
