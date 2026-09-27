package com.palette.mobile

import com.palette.mobile.core.util.ExportFormat
import com.palette.mobile.core.util.PaletteExporter
import com.palette.mobile.palette.model.Palette
import kotlin.test.Test
import kotlin.test.assertTrue

class PaletteExporterTest {

    private val samplePalette = Palette(
        id = "test-1",
        name = "Nordic Frost",
        paletteDescription = "Crisp cool winter palette",
        status = "PUBLISHED",
        likeCount = 10,
        colors = listOf("#2E3440", "#4C566A", "#D8DEE9", "#ECEFF4"),
        tags = listOf("nordic", "winter"),
        createdBy = "u-1",
        createdAt = "2026-09-27T10:00:00Z",
        publishedAt = "2026-09-27T10:00:00Z",
        likedByMe = false
    )

    @Test
    fun testToCssVariables() {
        val css = PaletteExporter.toCssVariables(samplePalette)
        assertTrue(css.startsWith(":root {"))
        assertTrue(css.contains("--color-nordic-frost-1: #2E3440;"))
        assertTrue(css.contains("--color-nordic-frost-4: #ECEFF4;"))
        assertTrue(css.endsWith("}"))
    }

    @Test
    fun testToTailwind() {
        val tailwind = PaletteExporter.toTailwind(samplePalette)
        assertTrue(tailwind.contains("'nordic-frost': {"))
        assertTrue(tailwind.contains("100: '#2E3440'"))
        assertTrue(tailwind.contains("700: '#ECEFF4'"))
    }

    @Test
    fun testToJetpackCompose() {
        val compose = PaletteExporter.toJetpackCompose(samplePalette)
        assertTrue(compose.contains("object NordicFrostPalette {"))
        assertTrue(compose.contains("val Color1 = Color(0xFF2E3440)"))
        assertTrue(compose.contains("val Color4 = Color(0xFFECEFF4)"))
    }

    @Test
    fun testToSwiftUI() {
        val swift = PaletteExporter.toSwiftUI(samplePalette)
        assertTrue(swift.contains("extension Color {"))
        assertTrue(swift.contains("static let nordicFrost1 = Color(hex: \"2E3440\")"))
        assertTrue(swift.contains("static let nordicFrost4 = Color(hex: \"ECEFF4\")"))
    }

    @Test
    fun testToJson() {
        val json = PaletteExporter.toJson(samplePalette)
        assertTrue(json.contains("\"name\": \"Nordic Frost\""))
        assertTrue(json.contains("\"description\": \"Crisp cool winter palette\""))
        assertTrue(json.contains("\"#2E3440\""))
    }

    @Test
    fun testExportWithEnum() {
        val css = PaletteExporter.export(samplePalette, ExportFormat.CSS)
        assertTrue(css.contains(":root {"))
    }
}
