export type ExportFormat = 'css' | 'tailwind' | 'compose' | 'swiftui' | 'json'

export interface ExportFormatOption {
  id: ExportFormat
  name: string
  extension: string
}

export const EXPORT_FORMATS: ExportFormatOption[] = [
  { id: 'css', name: 'CSS Variables', extension: 'css' },
  { id: 'tailwind', name: 'Tailwind CSS', extension: 'js' },
  { id: 'compose', name: 'Jetpack Compose', extension: 'kt' },
  { id: 'swiftui', name: 'SwiftUI', extension: 'swift' },
  { id: 'json', name: 'JSON', extension: 'json' }
]

function sanitizeIdentifier(name: string): string {
  const sanitized = name.trim().toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '')
  return sanitized || 'palette'
}

function sanitizeCamelCase(name: string): string {
  const words = name.trim().split(/[^a-zA-Z0-9]+/).filter(Boolean)
  if (words.length === 0) return 'palette'
  return words[0].toLowerCase() + words.slice(1).map(w => w.charAt(0).toUpperCase() + w.slice(1)).join('')
}

function sanitizePascalCase(name: string): string {
  const words = name.trim().split(/[^a-zA-Z0-9]+/).filter(Boolean)
  if (words.length === 0) return 'Palette'
  return words.map(w => w.charAt(0).toUpperCase() + w.slice(1)).join('')
}

export function toCssVariables(palette: { name: string; colors: string[] }): string {
  const slug = sanitizeIdentifier(palette.name)
  const lines = [':root {']
  palette.colors.forEach((hex, idx) => {
    lines.push(`  --color-${slug}-${idx + 1}: ${hex};`)
  })
  lines.push('}')
  return lines.join('\n')
}

export function toTailwind(palette: { name: string; colors: string[] }): string {
  const slug = sanitizeIdentifier(palette.name)
  const shades = [100, 300, 500, 700]
  const lines = [
    '/** Tailwind CSS color configuration */',
    'module.exports = {',
    '  theme: {',
    '    extend: {',
    '      colors: {',
    `        '${slug}': {`
  ]
  palette.colors.forEach((hex, idx) => {
    const shade = shades[idx] ?? (idx + 1) * 200
    const comma = idx < palette.colors.length - 1 ? ',' : ''
    lines.push(`          ${shade}: '${hex}'${comma}`)
  })
  lines.push('        }')
  lines.push('      }')
  lines.push('    }')
  lines.push('  }')
  lines.push('}')
  return lines.join('\n')
}

export function toJetpackCompose(palette: { name: string; colors: string[] }): string {
  const pascalName = sanitizePascalCase(palette.name)
  const lines = [
    'package com.palette.ui.theme',
    '',
    'import androidx.compose.ui.graphics.Color',
    '',
    `object ${pascalName}Palette {`
  ]
  palette.colors.forEach((hex, idx) => {
    const cleanHex = hex.replace(/^#/, '').toUpperCase()
    lines.push(`    val Color${idx + 1} = Color(0xFF${cleanHex})`)
  })
  lines.push('}')
  return lines.join('\n')
}

export function toSwiftUI(palette: { name: string; colors: string[] }): string {
  const camelName = sanitizeCamelCase(palette.name)
  const lines = [
    'import SwiftUI',
    '',
    'extension Color {',
  ]
  palette.colors.forEach((hex, idx) => {
    const cleanHex = hex.replace(/^#/, '').toUpperCase()
    lines.push(`    static let ${camelName}${idx + 1} = Color(hex: "${cleanHex}")`)
  })
  lines.push('}')
  return lines.join('\n')
}

export function toJson(palette: { name: string; colors: string[]; description?: string }): string {
  const obj: Record<string, unknown> = {
    name: palette.name,
    ...(palette.description ? { description: palette.description } : {}),
    colors: palette.colors
  }
  return JSON.stringify(obj, null, 2)
}

export function exportPalette(
  palette: { name: string; colors: string[]; description?: string },
  format: ExportFormat
): string {
  switch (format) {
    case 'css':
      return toCssVariables(palette)
    case 'tailwind':
      return toTailwind(palette)
    case 'compose':
      return toJetpackCompose(palette)
    case 'swiftui':
      return toSwiftUI(palette)
    case 'json':
      return toJson(palette)
  }
}
