const HEX_COLOR_REGEX = /^#([A-Fa-f0-9]{6})$/

export function isValidHexColor(hex: string): boolean {
  return HEX_COLOR_REGEX.test(hex.trim())
}

export function normalizeHexColor(hex: string): string {
  const trimmed = hex.trim()
  if (trimmed.startsWith('#')) {
    return trimmed.toUpperCase()
  }
  return `#${trimmed}`.toUpperCase()
}

export function validatePaletteColors(colors: string[]): { valid: boolean; error?: string } {
  if (!Array.isArray(colors)) {
    return { valid: false, error: 'Palette colors must be an array' }
  }
  if (colors.length !== 4) {
    return { valid: false, error: 'Palette must contain exactly 4 colors' }
  }
  for (let i = 0; i < colors.length; i++) {
    if (!isValidHexColor(colors[i])) {
      return {
        valid: false,
        error: `Color #${i + 1} ("${colors[i]}") is not a valid 6-character hex color (e.g. #FFFFFF)`
      }
    }
  }
  return { valid: true }
}

export function getRandomHexColor(): string {
  const letters = '0123456789ABCDEF'
  let color = '#'
  for (let i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)]
  }
  return color
}

export function getContrastingTextColor(hexColor: string): '#FFFFFF' | '#1A202C' {
  if (!isValidHexColor(hexColor)) return '#1A202C'
  const hex = hexColor.replace('#', '')
  const r = parseInt(hex.substring(0, 2), 16)
  const g = parseInt(hex.substring(2, 4), 16)
  const b = parseInt(hex.substring(4, 6), 16)
  
  // Calculate relative luminance based on sRGB
  const yiq = (r * 299 + g * 587 + b * 114) / 1000
  return yiq >= 128 ? '#1A202C' : '#FFFFFF'
}
