package com.palette.mobile

import com.palette.mobile.core.util.ColorContrastCalculator
import com.palette.mobile.core.util.WcagLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorContrastCalculatorTest {

    @Test
    fun testBlackAndWhiteContrast() {
        val ratio = ColorContrastCalculator.contrastRatio("#000000", "#FFFFFF")
        assertEquals(21.0, ratio)

        val result = ColorContrastCalculator.evaluateContrast("#000000", "#FFFFFF")
        assertEquals(WcagLevel.AAA, result.levelNormalText)
        assertEquals(WcagLevel.AAA, result.levelLargeText)
        assertTrue(result.passesNormalAA)
        assertTrue(result.passesLargeAA)
    }

    @Test
    fun testSameColorContrast() {
        val ratio = ColorContrastCalculator.contrastRatio("#FFFFFF", "#FFFFFF")
        assertEquals(1.0, ratio)

        val result = ColorContrastCalculator.evaluateContrast("#FFFFFF", "#FFFFFF")
        assertEquals(WcagLevel.FAIL, result.levelNormalText)
        assertEquals(WcagLevel.FAIL, result.levelLargeText)
    }

    @Test
    fun testTypicalColorPairs() {
        // Dark blue on white
        val blueResult = ColorContrastCalculator.evaluateContrast("#003366", "#FFFFFF")
        assertTrue(blueResult.ratio > 10.0)
        assertEquals(WcagLevel.AAA, blueResult.levelNormalText)

        // Light yellow on white (fails)
        val yellowResult = ColorContrastCalculator.evaluateContrast("#FFFFAA", "#FFFFFF")
        assertTrue(yellowResult.ratio < 2.0)
        assertEquals(WcagLevel.FAIL, yellowResult.levelNormalText)
    }

    @Test
    fun testEvaluateTextContrastAgainst() {
        val (againstWhite, againstDark) = ColorContrastCalculator.evaluateTextContrastAgainst("#000000")
        assertEquals(21.0, againstWhite.ratio)
        assertTrue(againstDark.ratio < 2.0)
    }
}
