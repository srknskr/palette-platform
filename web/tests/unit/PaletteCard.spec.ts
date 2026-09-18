import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import PaletteCard from '@/components/PaletteCard.vue'
import type { Palette } from '@/api/types'

// Mock vue-router
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    currentRoute: { value: { fullPath: '/' } }
  })
}))

describe('PaletteCard.vue', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  const mockPalette: Palette = {
    id: 'test-palette-1',
    name: 'Sunset Glow',
    description: 'A soothing gradient of sunset hues',
    colors: ['#FF5E7E', '#FF9966', '#FFD166', '#06D6A0'],
    tags: ['warm', 'sunset'],
    likesCount: 12,
    isLiked: false,
    createdAt: new Date().toISOString()
  }

  it('renders the palette name and description', () => {
    const wrapper = mount(PaletteCard, {
      props: {
        palette: mockPalette
      }
    })

    expect(wrapper.text()).toContain('Sunset Glow')
    expect(wrapper.text()).toContain('A soothing gradient of sunset hues')
    expect(wrapper.text()).toContain('12')
    expect(wrapper.text()).toContain('sunset')
  })

  it('renders 4 color swatches corresponding to the 4 colors', () => {
    const wrapper = mount(PaletteCard, {
      props: {
        palette: mockPalette
      }
    })

    const swatches = wrapper.findAllComponents({ name: 'ColorSwatch' })
    expect(swatches.length).toBe(4)
    expect(swatches[0].props('color')).toBe('#FF5E7E')
    expect(swatches[1].props('color')).toBe('#FF9966')
    expect(swatches[2].props('color')).toBe('#FFD166')
    expect(swatches[3].props('color')).toBe('#06D6A0')
  })
})
