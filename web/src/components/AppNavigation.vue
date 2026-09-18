<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import {
  Compass,
  Bookmark,
  PlusCircle,
  User as UserIcon,
  Sun,
  Moon,
  Laptop
} from 'lucide-vue-next'

const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const cycleTheme = () => {
  themeStore.toggleTheme()
}
</script>

<template>
  <!-- Desktop Sidebar Navigation -->
  <aside class="desktop-sidebar">
    <div class="sidebar-header">
      <RouterLink to="/" class="brand-logo">
        <div class="brand-bars">
          <span class="bar bar-1"></span>
          <span class="bar bar-2"></span>
          <span class="bar bar-3"></span>
          <span class="bar bar-4"></span>
        </div>
        <span class="brand-name">Palette</span>
      </RouterLink>
    </div>

    <nav class="sidebar-nav">
      <RouterLink
        to="/"
        class="nav-item"
        :class="{ active: route.path === '/' || route.name === 'discover' }"
      >
        <Compass :size="20" />
        <span>Discover</span>
      </RouterLink>

      <RouterLink
        to="/collection"
        class="nav-item"
        :class="{ active: route.path === '/collection' }"
      >
        <Bookmark :size="20" />
        <span>Collection</span>
      </RouterLink>

      <RouterLink
        to="/create"
        class="nav-item create-nav-item"
        :class="{ active: route.path === '/create' }"
      >
        <PlusCircle :size="20" />
        <span>Create</span>
      </RouterLink>
    </nav>

    <div class="sidebar-footer">
      <!-- Theme Switcher Button -->
      <button class="footer-btn" @click="cycleTheme" :title="`Current theme: ${themeStore.mode}. Click to change.`">
        <Sun v-if="themeStore.mode === 'light'" :size="18" />
        <Moon v-else-if="themeStore.mode === 'dark'" :size="18" />
        <Laptop v-else :size="18" />
        <span>{{ themeStore.mode.charAt(0).toUpperCase() + themeStore.mode.slice(1) }} Mode</span>
      </button>

      <!-- User Profile / Auth Link -->
      <RouterLink
        v-if="authStore.isAuthenticated"
        to="/profile"
        class="user-profile-link"
        :class="{ active: route.path === '/profile' }"
      >
        <div class="avatar-circle">
          {{ (authStore.user?.displayName || authStore.user?.email || 'U').charAt(0).toUpperCase() }}
        </div>
        <div class="user-meta">
          <span class="user-name">{{ authStore.user?.displayName || 'User' }}</span>
          <span class="user-email">{{ authStore.user?.email }}</span>
        </div>
      </RouterLink>

      <div v-else class="auth-buttons">
        <RouterLink to="/login" class="btn btn-secondary btn-block">Sign In</RouterLink>
        <RouterLink to="/register" class="btn btn-primary btn-block">Register</RouterLink>
      </div>
    </div>
  </aside>

  <!-- Mobile Compact Bottom Navigation (WCAG AA Compliant) -->
  <nav class="mobile-bottom-nav">
    <RouterLink
      to="/"
      class="mobile-tab"
      :class="{ active: route.path === '/' || route.name === 'discover' }"
    >
      <Compass :size="22" />
      <span>Discover</span>
    </RouterLink>

    <RouterLink
      to="/collection"
      class="mobile-tab"
      :class="{ active: route.path === '/collection' }"
    >
      <Bookmark :size="22" />
      <span>Collection</span>
    </RouterLink>

    <RouterLink
      to="/create"
      class="mobile-tab highlight"
      :class="{ active: route.path === '/create' }"
    >
      <PlusCircle :size="22" />
      <span>Create</span>
    </RouterLink>

    <RouterLink
      :to="authStore.isAuthenticated ? '/profile' : '/login'"
      class="mobile-tab"
      :class="{ active: route.path === '/profile' || route.path === '/login' }"
    >
      <UserIcon :size="22" />
      <span>{{ authStore.isAuthenticated ? 'Profile' : 'Sign In' }}</span>
    </RouterLink>
  </nav>
</template>

<style scoped>
/* Desktop Sidebar */
.desktop-sidebar {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: var(--sidebar-width);
  background-color: var(--bg-surface);
  border-right: 1px solid var(--border-color);
  flex-direction: column;
  z-index: 100;
}

@media (min-width: 769px) {
  .desktop-sidebar {
    display: flex;
  }
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid var(--border-color-subtle);
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-bars {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 20px;
}

.bar {
  display: block;
  width: 100%;
  border-radius: 2px;
}

.bar-1 { height: 6px; background-color: #2D3748; }
.bar-2 { height: 4px; background-color: #4A5568; }
.bar-3 { height: 3px; background-color: #718096; }
.bar-4 { height: 2px; background-color: #CBD5E0; }

.brand-name {
  font-size: 1.25rem;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--text-primary);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 20px 14px;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.15s ease;
}

.nav-item:hover {
  background-color: var(--bg-surface-hover);
  color: var(--text-primary);
}

.nav-item.active {
  background-color: var(--text-primary);
  color: var(--text-inverse);
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--border-color-subtle);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.15s ease;
}

.footer-btn:hover {
  background-color: var(--bg-surface-hover);
  color: var(--text-primary);
}

.user-profile-link {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--radius-md);
  transition: background-color 0.15s ease;
}

.user-profile-link:hover,
.user-profile-link.active {
  background-color: var(--bg-surface-hover);
}

.avatar-circle {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-full);
  background-color: var(--text-primary);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.85rem;
}

.user-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: 0.85rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-email {
  font-size: 0.72rem;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.auth-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.btn-block {
  width: 100%;
}

/* Mobile Bottom Navigation */
.mobile-bottom-nav {
  display: flex;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: var(--bottom-nav-height);
  background-color: var(--bg-surface);
  border-top: 1px solid var(--border-color);
  z-index: 100;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

@media (min-width: 769px) {
  .mobile-bottom-nav {
    display: none;
  }
}

.mobile-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  color: var(--text-muted);
  font-size: 0.7rem;
  font-weight: 600;
  transition: color 0.15s ease;
  user-select: none;
}

.mobile-tab:hover,
.mobile-tab.active {
  color: var(--text-primary);
}
</style>
