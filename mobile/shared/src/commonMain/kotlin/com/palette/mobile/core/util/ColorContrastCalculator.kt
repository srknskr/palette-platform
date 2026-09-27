package com.palette.mobile.core.util

import kotlin.math.pow
import kotlin.math.roundToInt

enum class WcagLevel(val label: String) {
    AAA("AAA"),
    AA("AA"),
    AA_LARGE("AA Large"),
    FAIL("Fail")
}

data class ContrastResult(
    val foregroundHex: String,
    val backgroundHex: String,
    val ratio: Double,
    val levelNormalText: WcagLevel,
    val levelLargeText: WcagLevel,
    val passesNormalAA: Boolean,
    val passesLargeAA: Boolean
)

object ColorContrastCalculator {

    /**
     * Calculates the relative luminance of a color according to WCAG 2.1 specifications.
     * Normalized relative luminance ranges from 0 (black) to 1 (white).
     */
    fun relativeLuminance(hex: String): Double {
        val clean = hex.removePrefix("#").trim()
        if (clean.length != 6) return 0.0

        val r = clean.substring(0, 2).toIntOrNull(16) ?: 0
        val g = clean.substring(2, 4).toIntOrNull(16) ?: 0
        val b = clean.substring(4, 6).toIntOrNull(16) ?: 0

        fun channelLuminance(value: Int): Double {
            val s = value / 255.0
            return if (s <= 0.04045) {
                s / 12.92
            } else {
                ((s + 0.055) / 1.055).pow(2.4)
            }
        }

        val rs = channelLuminance(r)
        val gs = channelLuminance(g)
        val bs = channelLuminance(b)

        return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs
    }

    /**
     * Calculates the contrast ratio between two hex colors: (L1 + 0.05) / (L2 + 0.05)
     * Result is between 1.0 and 21.0.
     */
    fun contrastRatio(hex1: String, hex2: String): Double {
        val l1 = relativeLuminance(hex1)
        val l2 = relativeLuminance(hex2)
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        val ratio = (lighter + 0.05) / (darker + 0.05)
        return (ratio * 100.0).roundToInt() / 100.0
    }

    /**
     * Evaluates WCAG 2.1 compliance for given foreground and background colors.
     */
    fun evaluateContrast(foregroundHex: String, backgroundHex: String): ContrastResult {
        val ratio = contrastRatio(foregroundHex, backgroundHex)
        val levelNormal = when {
            ratio >= 7.0 -> WcagLevel.AAA
            ratio >= 4.5 -> WcagLevel.AA
            ratio >= 3.0 -> WcagLevel.AA_LARGE
            else -> WcagLevel.FAIL
        }
        val levelLarge = when {
            ratio >= 4.5 -> WcagLevel.AAA
            ratio >= 3.0 -> WcagLevel.AA
            else -> WcagLevel.FAIL
        }
        return ContrastResult(
            foregroundHex = foregroundHex,
            backgroundHex = backgroundHex,
            ratio = ratio,
            levelNormalText = levelNormal,
            levelLargeText = levelLarge,
            passesNormalAA = ratio >= 4.5,
            passesLargeAA = ratio >= 3.0
        )
    }

    /**
     * Calculates contrast scores for a color against standard white (#FFFFFF) and dark (#111827) text.
     */
    fun evaluateTextContrastAgainst(backgroundColorHex: String): Pair<ContrastResult, ContrastResult> {
        val againstWhite = evaluateContrast("#FFFFFF", backgroundColorHex)
        val againstDark = evaluateContrast("#111827", backgroundColorHex)
        return Pair(againstWhite, againstDark)
    }
}
