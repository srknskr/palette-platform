package com.serkanmusic.palette.palette.api

import com.serkanmusic.palette.identity.infrastructure.security.AuthenticatedUser
import com.serkanmusic.palette.palette.api.dto.CreatePaletteRequest
import com.serkanmusic.palette.palette.api.dto.PaletteResponse
import com.serkanmusic.palette.palette.api.dto.UpdatePaletteRequest
import com.serkanmusic.palette.palette.api.dto.PaletteCountResponse
import com.serkanmusic.palette.palette.application.PaletteService
import com.serkanmusic.palette.shared.dto.PagedResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Palettes")
class PaletteController(
    private val paletteService: PaletteService
) {

    @GetMapping("/palettes")
    @Operation(summary = "List published palettes with filters and pagination")
    fun listPalettes(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) tag: String?,
        @RequestParam(required = false) hexColor: String?,
        @RequestParam(required = false, defaultValue = "newest") sort: String,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @AuthenticationPrincipal principal: AuthenticatedUser?
    ): ResponseEntity<PagedResponse<PaletteResponse>> {
        val response = paletteService.listPalettes(
            name = name,
            tag = tag,
            hexColor = hexColor,
            sort = sort,
            page = page,
            size = size,
            currentUserId = principal?.id
        )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/palettes/{id}")
    @Operation(summary = "Get palette details by UUID")
    fun getPaletteById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser?
    ): ResponseEntity<PaletteResponse> {
        val response = paletteService.getPaletteById(id, principal?.id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/palettes/random")
    @Operation(summary = "Get a random published palette")
    fun getRandomPalette(
        @AuthenticationPrincipal principal: AuthenticatedUser?
    ): ResponseEntity<PaletteResponse> {
        val response = paletteService.getRandomPalette(principal?.id)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/palettes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new palette", security = [SecurityRequirement(name = "bearerAuth")])
    fun createPalette(
        @Valid @RequestBody request: CreatePaletteRequest,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<PaletteResponse> {
        val response = paletteService.createPalette(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/palettes/{id}")
    @Operation(summary = "Update an existing palette owned by current user", security = [SecurityRequirement(name = "bearerAuth")])
    fun updatePalette(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdatePaletteRequest,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<PaletteResponse> {
        val response = paletteService.updatePalette(id, principal.id, request)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/palettes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a palette owned by current user or as admin", security = [SecurityRequirement(name = "bearerAuth")])
    fun deletePalette(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<Unit> {
        val isAdmin = principal.role == "ADMIN"
        paletteService.deletePalette(id, principal.id, isAdmin)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me/palettes")
    @Operation(summary = "List palettes created by the current user", security = [SecurityRequirement(name = "bearerAuth")])
    fun listMyPalettes(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<PagedResponse<PaletteResponse>> {
        val response = paletteService.listMyPalettes(principal.id, page, size)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/palettes/count")
    fun getPublishedPaletteCount(): ResponseEntity<PaletteCountResponse> {
        val count = paletteService.getPublishedPaletteCount()
        return ResponseEntity.ok(PaletteCountResponse(count))
    }
}
