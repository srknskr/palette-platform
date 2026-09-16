package com.serkanmusic.palette.shared.domain

object ColorValidator {

    private val HEX_COLOR_REGEX = Regex("^#[0-9A-F]{6}$")

    fun normalizeAndValidate(colors: List<String>): List<String> {
        require(colors.size == 4) { "Palette must contain exactly 4 colors" }

        val normalized = colors.map { color ->
            val trimmed = color.trim().uppercase()
            require(HEX_COLOR_REGEX.matches(trimmed)) { "Color $color is not a valid 6-digit hex color" }
            trimmed
        }

        require(normalized.toSet().size == 4) { "Colors within a palette must be unique" }

        return normalized
    }
}
