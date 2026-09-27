import { describe, it, expect } from 'vitest'
import {
  relativeLuminance,
  contrastRatio,
  evaluateContrast,
  evaluateTextContrastAgainst
} from '@/utils/colorContrast'

describe('colorContrast utils', () => {
  it('calculates maximum contrast for black and white', () => {
    expect(contrastRatio('#000000', '#FFFFFF')).toBe(21)
    const result = evaluateContrast('#000000', '#FFFFFF')
    expect(result.levelNormalText).toBe('AAA')
    expect(result.levelLargeText).toBe('AAA')
    expect(result.passesNormalAA).toBe(true)
    expect(result.passesLargeAA).toBe(true)
  })

  it('calculates minimum contrast for identical colors', () => {
    expect(contrastRatio('#FFFFFF', '#FFFFFF')).toBe(1)
    const result = evaluateContrast('#FFFFFF', '#FFFFFF')
    expect(result.levelNormalText).toBe('Fail')
    expect(result.passesNormalAA).toBe(false)
  })

  it('evaluates text contrast against background correctly', () => {
    const { againstWhite, againstDark } = evaluateTextContrastAgainst('#000000')
    expect(againstWhite.ratio).toBe(21)
    expect(againstDark.passesNormalAA).toBe(false)
  })
})
