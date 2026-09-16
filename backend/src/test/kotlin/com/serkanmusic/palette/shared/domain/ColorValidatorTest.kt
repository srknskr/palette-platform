package com.serkanmusic.palette.shared.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class ColorValidatorTest {

    @Test
    fun `valid four hex colors normalize to uppercase`() {
        val input = listOf("#ffffff", "#000000", "#ff5733", "#33ff57")
        val result = ColorValidator.normalizeAndValidate(input)

        assertEquals(listOf("#FFFFFF", "#000000", "#FF5733", "#33FF57"), result)
    }

    @Test
    fun `less than four colors throws exception`() {
        val input = listOf("#FFFFFF", "#000000", "#FF5733")
        assertThrows<IllegalArgumentException> {
            ColorValidator.normalizeAndValidate(input)
        }
    }

    @Test
    fun `more than four colors throws exception`() {
        val input = listOf("#FFFFFF", "#000000", "#FF5733", "#33FF57", "#123456")
        assertThrows<IllegalArgumentException> {
            ColorValidator.normalizeAndValidate(input)
        }
    }

    @Test
    fun `invalid hex code throws exception`() {
        val input = listOf("#FFFFF", "#000000", "#FF5733", "#33FF57")
        assertThrows<IllegalArgumentException> {
            ColorValidator.normalizeAndValidate(input)
        }
    }

    @Test
    fun `duplicate colors within palette throws exception`() {
        val input = listOf("#FFFFFF", "#FFFFFF", "#FF5733", "#33FF57")
        assertThrows<IllegalArgumentException> {
            ColorValidator.normalizeAndValidate(input)
        }
    }
}
