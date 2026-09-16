package com.serkanmusic.palette.moderation.api

import com.serkanmusic.palette.identity.infrastructure.security.AuthenticatedUser
import com.serkanmusic.palette.moderation.api.dto.ModerationActionRequest
import com.serkanmusic.palette.moderation.application.ModerationService
import com.serkanmusic.palette.palette.api.dto.PaletteResponse
import com.serkanmusic.palette.shared.dto.PagedResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/admin/palettes")
@Tag(name = "Admin & Moderation")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
class AdminPaletteController(
    private val moderationService: ModerationService
) {

    @GetMapping("/pending")
    @Operation(summary = "List palettes pending moderation")
    fun getPendingPalettes(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int
    ): ResponseEntity<PagedResponse<PaletteResponse>> {
        val response = moderationService.getPendingPalettes(page, size)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "Publish a palette")
    fun publishPalette(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: ModerationActionRequest?,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<PaletteResponse> {
        val response = moderationService.publishPalette(id, principal.id, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a palette")
    fun rejectPalette(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: ModerationActionRequest?,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<PaletteResponse> {
        val response = moderationService.rejectPalette(id, principal.id, request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive a palette")
    fun archivePalette(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: ModerationActionRequest?,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<PaletteResponse> {
        val response = moderationService.archivePalette(id, principal.id, request)
        return ResponseEntity.ok(response)
    }
}
