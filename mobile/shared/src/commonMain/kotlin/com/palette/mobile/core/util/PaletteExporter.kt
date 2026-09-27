package com.palette.mobile.core.util

import com.palette.mobile.palette.model.Palette

enum class ExportFormat(val displayName: String, val extensionName: String) {
    CSS("CSS Variables", "css"),
    TAILWIND("Tailwind CSS", "js"),
    COMPOSE("Jetpack Compose", "kt"),
    SWIFT_UI("SwiftUI", "swift"),
    JSON("JSON", "json")
}

object PaletteExporter {

    private fun sanitizeIdentifier(name: String): String {
        val sanitized = name.trim().lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
        return sanitized.ifBlank { "palette" }
    }

    private fun sanitizeCamelCase(name: String): String {
        val words = name.trim().split(Regex("[^a-zA-Z0-9]+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return "palette"
        return words.first().replaceFirstChar { it.lowercase() } +
            words.drop(1).joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    private fun sanitizePascalCase(name: String): String {
        val words = name.trim().split(Regex("[^a-zA-Z0-9]+")).filter { it.isNotBlank() }
        if (words.isEmpty()) return "Palette"
        return words.joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    fun toCssVariables(palette: Palette): String {
        val slug = sanitizeIdentifier(palette.name)
        val sb = StringBuilder()
        sb.appendLine(":root {")
        palette.colors.forEachIndexed { index, hex ->
            sb.appendLine("  --color-$slug-${index + 1}: $hex;")
        }
        sb.append("}")
        return sb.toString()
    }

    fun toTailwind(palette: Palette): String {
        val slug = sanitizeIdentifier(palette.name)
        val sb = StringBuilder()
        sb.appendLine("/** Tailwind CSS color configuration */")
        sb.appendLine("module.exports = {")
        sb.appendLine("  theme: {")
        sb.appendLine("    extend: {")
        sb.appendLine("      colors: {")
        sb.appendLine("        '$slug': {")
        val shades = listOf(100, 300, 500, 700)
        palette.colors.forEachIndexed { index, hex ->
            val shade = shades.getOrElse(index) { (index + 1) * 200 }
            val comma = if (index < palette.colors.size - 1) "," else ""
            sb.appendLine("          $shade: '$hex'$comma")
        }
        sb.appendLine("        }")
        sb.appendLine("      }")
        sb.appendLine("    }")
        sb.appendLine("  }")
        sb.append("}")
        return sb.toString()
    }

    fun toJetpackCompose(palette: Palette): String {
        val pascalName = sanitizePascalCase(palette.name)
        val sb = StringBuilder()
        sb.appendLine("package com.palette.ui.theme")
        sb.appendLine()
        sb.appendLine("import androidx.compose.ui.graphics.Color")
        sb.appendLine()
        sb.appendLine("object ${pascalName}Palette {")
        palette.colors.forEachIndexed { index, hex ->
            val cleanHex = hex.removePrefix("#").uppercase()
            sb.appendLine("    val Color${index + 1} = Color(0xFF$cleanHex)")
        }
        sb.append("}")
        return sb.toString()
    }

    fun toSwiftUI(palette: Palette): String {
        val camelName = sanitizeCamelCase(palette.name)
        val sb = StringBuilder()
        sb.appendLine("import SwiftUI")
        sb.appendLine()
        sb.appendLine("extension Color {")
        palette.colors.forEachIndexed { index, hex ->
            val cleanHex = hex.removePrefix("#").uppercase()
            sb.appendLine("    static let $camelName${index + 1} = Color(hex: \"$cleanHex\")")
        }
        sb.append("}")
        return sb.toString()
    }

    fun toJson(palette: Palette): String {
        val colorsList = palette.colors.joinToString(", ") { "\"$it\"" }
        val descJson = palette.paletteDescription?.let { "\"description\": \"${it.replace("\"", "\\\"")}\",\n  " } ?: ""
        return """
        {
          "name": "${palette.name.replace("\"", "\\\"")}",
          $descJson"colors": [$colorsList]
        }
        """.trimIndent()
    }

    fun export(palette: Palette, format: ExportFormat): String {
        return when (format) {
            ExportFormat.CSS -> toCssVariables(palette)
            ExportFormat.TAILWIND -> toTailwind(palette)
            ExportFormat.COMPOSE -> toJetpackCompose(palette)
            ExportFormat.SWIFT_UI -> toSwiftUI(palette)
            ExportFormat.JSON -> toJson(palette)
        }
    }
}
