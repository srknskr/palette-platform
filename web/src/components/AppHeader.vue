<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { Sun, Moon, Laptop, LogIn, Plus } from 'lucide-vue-next'

const authStore = useAuthStore()
const themeStore = useThemeStore()
</script>

<template>
  <header class="app-header">
    <div class="header-mobile-brand">
      <RouterLink to="/" class="brand-link">
        <div class="brand-bars">
          <span class="bar bar-1"></span>
          <span class="bar bar-2"></span>
          <span class="bar bar-3"></span>
          <span class="bar bar-4"></span>
        </div>
        <span class="brand-title">Palette</span>
      </RouterLink>
    </div>

    <div class="header-actions">
      <!-- Theme Switcher for mobile / quick toggle -->
      <button
        class="header-action-btn"
        aria-label="Toggle color theme"
        @click="themeStore.toggleTheme"
      >
        <Sun v-if="themeStore.mode === 'light'" :size="20" />
        <Moon v-else-if="themeStore.mode === 'dark'" :size="20" />
        <Laptop v-else :size="20" />
      </button>

      <RouterLink to="/create" class="btn btn-primary create-btn-header">
        <Plus :size="18" />
        <span>Create</span>
      </RouterLink>

      <div v-if="!authStore.isAuthenticated" class="header-auth-cta">
        <RouterLink to="/login" class="btn btn-secondary">
          <LogIn :size="16" />
          <span>Sign In</span>
        </RouterLink>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: var(--bg-surface);
  border-bottom: 1px solid var(--border-color);
  position: sticky;
  top: 0;
  z-index: 90;
}

@media (min-width: 769px) {
  .app-header {
    display: none; /* Desktop sidebar already handles navigation and branding */
  }
}

.header-mobile-brand {
  display: flex;
  align-items: center;
}

.brand-link {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-bars {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 18px;
}

.bar {
  display: block;
  width: 100%;
  border-radius: 2px;
}

.bar-1 { height: 5px; background-color: #2D3748; }
.bar-2 { height: 3.5px; background-color: #4A5568; }
.bar-3 { height: 2.5px; background-color: #718096; }
.bar-4 { height: 2px; background-color: #CBD5E0; }

.brand-title {
  font-size: 1.15rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
}

.create-btn-header {
  padding: 6px 12px;
  font-size: 0.85rem;
}
</style>
