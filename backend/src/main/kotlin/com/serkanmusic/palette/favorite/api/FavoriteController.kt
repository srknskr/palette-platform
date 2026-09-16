package com.serkanmusic.palette.favorite.api

import com.serkanmusic.palette.favorite.application.FavoriteService
import com.serkanmusic.palette.identity.infrastructure.security.AuthenticatedUser
import com.serkanmusic.palette.palette.api.dto.PaletteResponse
import com.serkanmusic.palette.shared.dto.PagedResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Favorites & Collections")
class FavoriteController(
    private val favoriteService: FavoriteService
) {

    @PostMapping("/palettes/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Favorite a palette (idempotent)", security = [SecurityRequirement(name = "bearerAuth")])
    fun favoritePalette(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<Unit> {
        favoriteService.favoritePalette(principal.id, id)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/palettes/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unfavorite a palette (idempotent)", security = [SecurityRequirement(name = "bearerAuth")])
    fun unfavoritePalette(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<Unit> {
        favoriteService.unfavoritePalette(principal.id, id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me/favorites")
    @Operation(summary = "List palettes favorited by current user", security = [SecurityRequirement(name = "bearerAuth")])
    fun getUserFavorites(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "20") size: Int,
        @AuthenticationPrincipal principal: AuthenticatedUser
    ): ResponseEntity<PagedResponse<PaletteResponse>> {
        val response = favoriteService.getUserFavorites(principal.id, page, size)
        return ResponseEntity.ok(response)
    }
}
