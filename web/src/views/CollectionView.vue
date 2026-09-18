<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { paletteApi } from '@/api/endpoints'
import type { Palette } from '@/api/types'
import { useAuthStore } from '@/stores/auth'
import { extractErrorMessage } from '@/api/client'
import PaletteCard from '@/components/PaletteCard.vue'
import LoadingSkeleton from '@/components/LoadingSkeleton.vue'
import ErrorState from '@/components/ErrorState.vue'
import { Bookmark, Compass } from 'lucide-vue-next'

const router = useRouter()
const authStore = useAuthStore()

const favorites = ref<Palette[]>([])
const isLoading = ref(true)
const errorMessage = ref<string | null>(null)
const page = ref(0)
const hasMore = ref(true)
const isLoadingMore = ref(false)

const fetchFavorites = async (isInitial = true) => {
  if (isInitial) {
    isLoading.value = true
    errorMessage.value = null
    page.value = 0
  } else {
    isLoadingMore.value = true
  }

  try {
    const res = await paletteApi.getMyFavorites(page.value, 18)
    if (isInitial) {
      favorites.value = res.content
    } else {
      favorites.value = [...favorites.value, ...res.content]
    }
    hasMore.value = !res.last && favorites.value.length < res.totalElements
  } catch (err) {
    errorMessage.value = extractErrorMessage(err)
  } finally {
    isLoading.value = false
    isLoadingMore.value = false
  }
}

const handleUnfavorited = (id: string, isLiked: boolean) => {
  if (!isLiked) {
    favorites.value = favorites.value.filter(p => p.id !== id)
  }
}

const loadMore = () => {
  if (isLoadingMore.value || !hasMore.value) return
  page.value++
  fetchFavorites(false)
}

onMounted(() => {
  if (!authStore.isAuthenticated) {
    router.replace({ path: '/login', query: { redirect: '/collection' } })
    return
  }
  fetchFavorites(true)
})
</script>

<template>
  <div class="collection-view">
    <div class="collection-header">
      <h1 class="title-lg">Your Collection</h1>
      <p class="subtitle">Saved and favorited palettes in your personal library.</p>
    </div>

    <!-- Loading State -->
    <LoadingSkeleton v-if="isLoading" :count="6" />

    <!-- Error State -->
    <ErrorState
      v-else-if="errorMessage"
      :message="errorMessage"
      :can-retry="true"
      @retry="fetchFavorites(true)"
    />

    <!-- Empty State -->
    <div v-else-if="favorites.length === 0" class="empty-collection card">
      <div class="empty-icon">
        <Bookmark :size="36" />
      </div>
      <h3 class="title-md">No saved palettes yet</h3>
      <p class="empty-desc">
        When you discover colors you love, hit the heart button to save them to your collection.
      </p>
      <RouterLink to="/" class="btn btn-primary">
        <Compass :size="18" />
        <span>Explore Palettes</span>
      </RouterLink>
    </div>

    <!-- Collection Grid -->
    <div v-else class="collection-content">
      <div class="palettes-grid">
        <PaletteCard
          v-for="palette in favorites"
          :key="palette.id"
          :palette="palette"
          @favorited="handleUnfavorited"
        />
      </div>

      <div v-if="hasMore" class="load-more-container">
        <button
          class="btn btn-secondary"
          :disabled="isLoadingMore"
          @click="loadMore"
        >
          <span v-if="isLoadingMore">Loading more...</span>
          <span v-else>Load More Favorites</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.collection-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.collection-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.empty-collection {
  padding: 64px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 16px;
  max-width: 520px;
  margin: 40px auto;
}

.empty-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-full);
  background-color: var(--bg-surface-hover);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-desc {
  font-size: 0.95rem;
  color: var(--text-secondary);
  max-width: 400px;
}

.palettes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.load-more-container {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}
</style>
