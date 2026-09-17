package com.palette.mobile

import com.palette.mobile.core.util.ColorValidator
import com.palette.mobile.core.util.ValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ColorValidatorTest {

    @Test
    fun validHexFormatReturnsTrue() {
        assertTrue(ColorValidator.isValidHex("#FFFFFF"))
        assertTrue(ColorValidator.isValidHex("#000000"))
        assertTrue(ColorValidator.isValidHex("#2E3440"))
        assertTrue(ColorValidator.isValidHex("#abcdef"))
    }

    @Test
    fun invalidHexFormatReturnsFalse() {
        assertFalse(ColorValidator.isValidHex("123456"))
        assertFalse(ColorValidator.isValidHex("#FFF"))
        assertFalse(ColorValidator.isValidHex("#12345G"))
        assertFalse(ColorValidator.isValidHex("#1234567"))
        assertFalse(ColorValidator.isValidHex(""))
    }

    @Test
    fun normalizeHexAddsPrefixAndUppercases() {
        assertEquals("#FFFFFF", ColorValidator.normalizeHex("ffffff"))
        assertEquals("#2E3440", ColorValidator.normalizeHex("#2e3440"))
        assertEquals("#1A2B3C", ColorValidator.normalizeHex("1a2b3c"))
    }

    @Test
    fun validateFourDistinctHexColorsSucceeds() {
        val colors = listOf("#2E3440", "#4C566A", "#D8DEE9", "#ECEFF4")
        val result = ColorValidator.validatePaletteColors(colors)
        assertTrue(result is ValidationResult.Valid)
        assertEquals(4, (result as ValidationResult.Valid).normalizedColors.size)
    }

    @Test
    fun validateDuplicateColorsFails() {
        val colors = listOf("#2E3440", "#4C566A", "#2E3440", "#ECEFF4")
        val result = ColorValidator.validatePaletteColors(colors)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validateLessThanFourColorsFails() {
        val colors = listOf("#2E3440", "#4C566A", "#ECEFF4")
        val result = ColorValidator.validatePaletteColors(colors)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun validateMoreThanFourColorsFails() {
        val colors = listOf("#2E3440", "#4C566A", "#D8DEE9", "#ECEFF4", "#88C0D0")
        val result = ColorValidator.validatePaletteColors(colors)
        assertTrue(result is ValidationResult.Invalid)
    }
}
