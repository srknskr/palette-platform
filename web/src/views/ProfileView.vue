<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { paletteApi } from '@/api/endpoints'
import type { Palette } from '@/api/types'
import PaletteCard from '@/components/PaletteCard.vue'
import LoadingSkeleton from '@/components/LoadingSkeleton.vue'
import { LogOut, Sun, Moon, Laptop, PlusCircle, Sparkles } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()

const myPalettes = ref<Palette[]>([])
const isLoading = ref(true)

const fetchMyPalettes = async () => {
  isLoading.value = true
  try {
    const res = await paletteApi.getMyPalettes(0, 50)
    myPalettes.value = res.items || (res as unknown as { content: Palette[] }).content || []
  } catch (err) {
    console.error('Failed to load my palettes', err)
  } finally {
    isLoading.value = false
  }
}

const handlePaletteDeleted = (id: string) => {
  myPalettes.value = myPalettes.value.filter(p => p.id !== id)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/')
}

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.replace('/login')
    return
  }
  authStore.fetchProfile()
  fetchMyPalettes()
})
</script>

<template>
  <div class="profile-view">
    <!-- User Overview Card -->
    <div class="card profile-header-card">
      <div class="user-info-section">
        <div class="profile-avatar">
          {{ (authStore.user?.displayName || authStore.user?.email || 'U').charAt(0).toUpperCase() }}
        </div>
        <div class="user-details">
          <h1 class="title-md">{{ authStore.user?.displayName || 'Designer' }}</h1>
          <p class="subtitle">{{ authStore.user?.email }}</p>
          <div class="user-stats">
            <span class="stat-badge">
              <strong>{{ myPalettes.length }}</strong> Created Palettes
            </span>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="profile-actions">
        <button class="btn btn-secondary logout-btn" @click="handleLogout">
          <LogOut :size="16" />
          <span>Sign Out</span>
        </button>
      </div>
    </div>

    <!-- Preferences Section -->
    <div class="card preferences-card">
      <h2 class="section-title">Preferences</h2>
      <div class="pref-row">
        <div>
          <h4 class="pref-title">Interface Theme</h4>
          <p class="pref-desc">Select your preferred appearance for the Palette platform.</p>
        </div>
        <div class="theme-picker">
          <button
            class="theme-btn"
            :class="{ active: themeStore.mode === 'light' }"
            @click="themeStore.setMode('light')"
          >
            <Sun :size="16" />
            <span>Light</span>
          </button>
          <button
            class="theme-btn"
            :class="{ active: themeStore.mode === 'dark' }"
            @click="themeStore.setMode('dark')"
          >
            <Moon :size="16" />
            <span>Dark</span>
          </button>
          <button
            class="theme-btn"
            :class="{ active: themeStore.mode === 'system' }"
            @click="themeStore.setMode('system')"
          >
            <Laptop :size="16" />
            <span>System</span>
          </button>
        </div>
      </div>
    </div>

    <!-- My Published Palettes -->
    <div class="my-palettes-section">
      <div class="section-header">
        <div>
          <h2 class="title-md">My Created Palettes</h2>
          <p class="subtitle">Palettes published by your account.</p>
        </div>
        <RouterLink to="/create" class="btn btn-primary">
          <PlusCircle :size="18" />
          <span>Create New</span>
        </RouterLink>
      </div>

      <LoadingSkeleton v-if="isLoading" :count="3" />

      <div v-else-if="myPalettes.length === 0" class="card empty-state">
        <Sparkles :size="32" class="empty-icon" />
        <h3 class="title-md">No palettes created yet</h3>
        <p class="subtitle">Express your creativity and publish your first color palette.</p>
        <RouterLink to="/create" class="btn btn-primary">Create Palette</RouterLink>
      </div>

      <div v-else class="palettes-grid">
        <PaletteCard
          v-for="palette in myPalettes"
          :key="palette.id"
          :palette="palette"
          :is-deletable="true"
          @deleted="handlePaletteDeleted"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-view {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.profile-header-card {
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  justify-content: space-between;
}

@media (min-width: 640px) {
  .profile-header-card {
    flex-direction: row;
    align-items: center;
  }
}

.user-info-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  width: 68px;
  height: 68px;
  border-radius: var(--radius-full);
  background-color: var(--text-primary);
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.75rem;
  font-weight: 800;
  box-shadow: var(--shadow-md);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-stats {
  margin-top: 6px;
}

.stat-badge {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.preferences-card {
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  font-size: 1.15rem;
  font-weight: 700;
}

.pref-row {
  display: flex;
  flex-direction: column;
  gap: 16px;
  justify-content: space-between;
}

@media (min-width: 640px) {
  .pref-row {
    flex-direction: row;
    align-items: center;
  }
}

.pref-title {
  font-size: 0.95rem;
  font-weight: 600;
}

.pref-desc {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.theme-picker {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  padding: 4px;
  border-radius: var(--radius-md);
}

.theme-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 0.85rem;
  font-weight: 600;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  transition: all 0.15s ease;
}

.theme-btn:hover {
  color: var(--text-primary);
}

.theme-btn.active {
  background-color: var(--bg-surface);
  color: var(--text-primary);
  box-shadow: var(--shadow-sm);
}

.my-palettes-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.empty-state {
  padding: 48px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
}

.empty-icon {
  color: var(--text-muted);
}

.palettes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}
</style>
