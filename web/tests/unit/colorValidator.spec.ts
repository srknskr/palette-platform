import { describe, it, expect } from 'vitest'
import {
  isValidHexColor,
  normalizeHexColor,
  validatePaletteColors,
  getContrastingTextColor
} from '@/utils/colorValidator'

describe('colorValidator utils', () => {
  describe('isValidHexColor', () => {
    it('accepts valid 6-digit hex color with hash', () => {
      expect(isValidHexColor('#FFFFFF')).toBe(true)
      expect(isValidHexColor('#000000')).toBe(true)
      expect(isValidHexColor('#f6f4ef')).toBe(true)
      expect(isValidHexColor('#2A9D8F')).toBe(true)
    })

    it('rejects invalid hex formats', () => {
      expect(isValidHexColor('FFFFFF')).toBe(false)
      expect(isValidHexColor('#FFF')).toBe(false)
      expect(isValidHexColor('#GGGGGG')).toBe(false)
      expect(isValidHexColor('#1234567')).toBe(false)
      expect(isValidHexColor('')).toBe(false)
    })
  })

  describe('normalizeHexColor', () => {
    it('ensures leading hash and uppercase characters', () => {
      expect(normalizeHexColor('#abcdef')).toBe('#ABCDEF')
      expect(normalizeHexColor('1a202c')).toBe('#1A202C')
      expect(normalizeHexColor('#1A202C')).toBe('#1A202C')
    })
  })

  describe('validatePaletteColors', () => {
    it('validates array of exactly 4 hex colors', () => {
      const validPalette = ['#112233', '#445566', '#778899', '#AABBCC']
      const result = validatePaletteColors(validPalette)
      expect(result.valid).toBe(true)
      expect(result.error).toBeUndefined()
    })

    it('fails if length is not 4', () => {
      const short = ['#112233', '#445566', '#778899']
      expect(validatePaletteColors(short).valid).toBe(false)

      const long = ['#112233', '#445566', '#778899', '#AABBCC', '#DDEEFF']
      expect(validatePaletteColors(long).valid).toBe(false)
    })

    it('fails if any color in array is invalid', () => {
      const invalid = ['#112233', 'INVALID', '#778899', '#AABBCC']
      const result = validatePaletteColors(invalid)
      expect(result.valid).toBe(false)
      expect(result.error).toContain('Color #2')
    })
  })

  describe('getContrastingTextColor', () => {
    it('returns dark text for bright colors', () => {
      expect(getContrastingTextColor('#FFFFFF')).toBe('#1A202C')
      expect(getContrastingTextColor('#F6F4EF')).toBe('#1A202C')
      expect(getContrastingTextColor('#FFFF00')).toBe('#1A202C')
    })

    it('returns white text for dark colors', () => {
      expect(getContrastingTextColor('#000000')).toBe('#FFFFFF')
      expect(getContrastingTextColor('#1A202C')).toBe('#FFFFFF')
      expect(getContrastingTextColor('#264653')).toBe('#FFFFFF')
    })
  })
})
