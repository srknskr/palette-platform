export type WcagLevel = 'AAA' | 'AA' | 'AA Large' | 'Fail'

export interface ContrastResult {
  foregroundHex: string
  backgroundHex: string
  ratio: number
  levelNormalText: WcagLevel
  levelLargeText: WcagLevel
  passesNormalAA: boolean
  passesLargeAA: boolean
}

/**
 * Calculates the relative luminance of a color according to WCAG 2.1 specifications.
 * Normalized relative luminance ranges from 0 (black) to 1 (white).
 */
export function relativeLuminance(hex: string): number {
  const clean = hex.replace(/^#/, '').trim()
  if (clean.length !== 6) return 0

  const r = parseInt(clean.substring(0, 2), 16) || 0
  const g = parseInt(clean.substring(2, 4), 16) || 0
  const b = parseInt(clean.substring(4, 6), 16) || 0

  function channelLuminance(value: number): number {
    const s = value / 255
    return s <= 0.04045 ? s / 12.92 : Math.pow((s + 0.055) / 1.055, 2.4)
  }

  const rs = channelLuminance(r)
  const gs = channelLuminance(g)
  const bs = channelLuminance(b)

  return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs
}

/**
 * Calculates the contrast ratio between two hex colors: (L1 + 0.05) / (L2 + 0.05)
 * Result is between 1.0 and 21.0.
 */
export function contrastRatio(hex1: string, hex2: string): number {
  const l1 = relativeLuminance(hex1)
  const l2 = relativeLuminance(hex2)
  const lighter = Math.max(l1, l2)
  const darker = Math.min(l1, l2)
  const ratio = (lighter + 0.05) / (darker + 0.05)
  return Math.round(ratio * 100) / 100
}

/**
 * Evaluates WCAG 2.1 compliance for given foreground and background colors.
 */
export function evaluateContrast(foregroundHex: string, backgroundHex: string): ContrastResult {
  const ratio = contrastRatio(foregroundHex, backgroundHex)
  const levelNormal: WcagLevel =
    ratio >= 7.0 ? 'AAA' : ratio >= 4.5 ? 'AA' : ratio >= 3.0 ? 'AA Large' : 'Fail'
  const levelLarge: WcagLevel =
    ratio >= 4.5 ? 'AAA' : ratio >= 3.0 ? 'AA' : 'Fail'

  return {
    foregroundHex,
    backgroundHex,
    ratio,
    levelNormalText: levelNormal,
    levelLargeText: levelLarge,
    passesNormalAA: ratio >= 4.5,
    passesLargeAA: ratio >= 3.0
  }
}

/**
 * Evaluates contrast scores for a color against standard white (#FFFFFF) and dark (#111827) text.
 */
export function evaluateTextContrastAgainst(backgroundColorHex: string): {
  againstWhite: ContrastResult
  againstDark: ContrastResult
} {
  return {
    againstWhite: evaluateContrast('#FFFFFF', backgroundColorHex),
    againstDark: evaluateContrast('#111827', backgroundColorHex)
  }
}
