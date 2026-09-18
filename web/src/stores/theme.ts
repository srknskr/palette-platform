import { defineStore } from 'pinia'
import { ref, watchEffect } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'system'

export const useThemeStore = defineStore('theme', () => {
  const savedMode = (localStorage.getItem('theme_mode') as ThemeMode) || 'system'
  const mode = ref<ThemeMode>(savedMode)
  const isDark = ref(false)

  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

  const updateTheme = () => {
    if (mode.value === 'dark') {
      isDark.value = true
    } else if (mode.value === 'light') {
      isDark.value = false
    } else {
      isDark.value = mediaQuery.matches
    }

    if (isDark.value) {
      document.documentElement.setAttribute('data-theme', 'dark')
    } else {
      document.documentElement.removeAttribute('data-theme')
    }
  }

  const setMode = (newMode: ThemeMode) => {
    mode.value = newMode
    localStorage.setItem('theme_mode', newMode)
    updateTheme()
  }

  const toggleTheme = () => {
    if (mode.value === 'light') {
      setMode('dark')
    } else if (mode.value === 'dark') {
      setMode('system')
    } else {
      setMode('light')
    }
  }

  // Listen to system OS preference changes
  mediaQuery.addEventListener('change', () => {
    if (mode.value === 'system') {
      updateTheme()
    }
  })

  watchEffect(() => {
    updateTheme()
  })

  return {
    mode,
    isDark,
    setMode,
    toggleTheme
  }
})
