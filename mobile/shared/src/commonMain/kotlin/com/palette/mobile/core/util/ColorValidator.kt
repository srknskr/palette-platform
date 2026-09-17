package com.palette.mobile.core.util

object ColorValidator {
    private val hexRegex = Regex("^#[0-9A-Fa-f]{6}$")

    fun isValidHex(hex: String): Boolean {
        return hexRegex.matches(hex.trim())
    }

    fun normalizeHex(hex: String): String {
        val trimmed = hex.trim()
        val withPrefix = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
        return withPrefix.uppercase()
    }

    fun validatePaletteColors(colors: List<String>): ValidationResult {
        if (colors.size != 4) {
            return ValidationResult.Invalid("A palette must contain exactly four colors")
        }
        val normalized = mutableListOf<String>()
        val seen = mutableSetOf<String>()

        for (color in colors) {
            if (!isValidHex(color)) {
                return ValidationResult.Invalid("Invalid HEX color: $color")
            }
            val norm = normalizeHex(color)
            if (!seen.add(norm)) {
                return ValidationResult.Invalid("Colors must be distinct within the palette: $norm")
            }
            normalized.add(norm)
        }
        return ValidationResult.Valid(normalized)
    }
}

sealed interface ValidationResult {
    data class Valid(val normalizedColors: List<String>) : ValidationResult
    data class Invalid(val reason: String) : ValidationResult
}
