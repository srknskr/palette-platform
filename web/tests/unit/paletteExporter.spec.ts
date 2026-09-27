import { describe, it, expect } from 'vitest'
import {
  toCssVariables,
  toTailwind,
  toJetpackCompose,
  toSwiftUI,
  toJson,
  exportPalette
} from '@/utils/paletteExporter'

describe('paletteExporter', () => {
  const samplePalette = {
    name: 'Sunset Glow',
    colors: ['#FF5733', '#C70039', '#900C3F', '#581845'],
    description: 'A warm summer sunset'
  }

  it('exports CSS variables correctly', () => {
    const css = toCssVariables(samplePalette)
    expect(css).toContain(':root {')
    expect(css).toContain('--color-sunset-glow-1: #FF5733;')
    expect(css).toContain('--color-sunset-glow-4: #581845;')
    expect(css).toContain('}')
  })

  it('exports Tailwind config correctly', () => {
    const tailwind = toTailwind(samplePalette)
    expect(tailwind).toContain("'sunset-glow': {")
    expect(tailwind).toContain("100: '#FF5733',")
    expect(tailwind).toContain("700: '#581845'")
  })

  it('exports Jetpack Compose code correctly', () => {
    const compose = toJetpackCompose(samplePalette)
    expect(compose).toContain('object SunsetGlowPalette {')
    expect(compose).toContain('val Color1 = Color(0xFFFF5733)')
    expect(compose).toContain('val Color4 = Color(0xFF581845)')
  })

  it('exports SwiftUI code correctly', () => {
    const swiftui = toSwiftUI(samplePalette)
    expect(swiftui).toContain('extension Color {')
    expect(swiftui).toContain('static let sunsetGlow1 = Color(hex: "FF5733")')
    expect(swiftui).toContain('static let sunsetGlow4 = Color(hex: "581845")')
  })

  it('exports JSON correctly', () => {
    const jsonStr = toJson(samplePalette)
    const parsed = JSON.parse(jsonStr)
    expect(parsed.name).toBe('Sunset Glow')
    expect(parsed.colors).toEqual(['#FF5733', '#C70039', '#900C3F', '#581845'])
    expect(parsed.description).toBe('A warm summer sunset')
  })

  it('exportPalette delegates to all formats', () => {
    expect(exportPalette(samplePalette, 'css')).toBe(toCssVariables(samplePalette))
    expect(exportPalette(samplePalette, 'tailwind')).toBe(toTailwind(samplePalette))
    expect(exportPalette(samplePalette, 'compose')).toBe(toJetpackCompose(samplePalette))
    expect(exportPalette(samplePalette, 'swiftui')).toBe(toSwiftUI(samplePalette))
    expect(exportPalette(samplePalette, 'json')).toBe(toJson(samplePalette))
  })
})
