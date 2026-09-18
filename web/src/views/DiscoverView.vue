<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { paletteApi } from '@/api/endpoints'
import type { Palette } from '@/api/types'
import { extractErrorMessage } from '@/api/client'
import PaletteCard from '@/components/PaletteCard.vue'
import LoadingSkeleton from '@/components/LoadingSkeleton.vue'
import ErrorState from '@/components/ErrorState.vue'
import { Search, Sparkles, TrendingUp, Clock, Dices, X } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

type SortOption = 'newest' | 'popular' | 'random'

const currentSort = ref<SortOption>((route.query.sort as SortOption) || 'newest')
const searchQuery = ref((route.query.q as string) || '')
const selectedTag = ref((route.query.tag as string) || '')

const palettes = ref<Palette[]>([])
const isLoading = ref(true)
const errorMessage = ref<string | null>(null)
const page = ref(0)
const hasMore = ref(true)
const isLoadingMore = ref(false)

const popularTags = ['vintage', 'pastel', 'neon', 'earthy', 'minimal', 'cyberpunk', 'warm', 'cool']

const fetchPalettes = async (isInitial = true) => {
  if (isInitial) {
    isLoading.value = true
    errorMessage.value = null
    page.value = 0
  } else {
    isLoadingMore.value = true
  }

  try {
    const res = await paletteApi.getPalettes({
      sort: currentSort.value,
      name: searchQuery.value.trim() || undefined,
      tag: selectedTag.value.trim() || undefined,
      page: page.value,
      size: 18
    })

    const items = res.items || (res as unknown as { content: Palette[] }).content || []
    const total = res.metadata?.totalElements ?? 0
    const hasNext = res.metadata?.hasNext ?? false

    if (isInitial) {
      palettes.value = items
    } else {
      palettes.value = [...palettes.value, ...items]
    }

    hasMore.value = hasNext && palettes.value.length < total
  } catch (err) {
    errorMessage.value = extractErrorMessage(err)
  } finally {
    isLoading.value = false
    isLoadingMore.value = false
  }
}

const updateUrlParams = () => {
  const query: Record<string, string> = {}
  if (currentSort.value !== 'newest') query.sort = currentSort.value
  if (searchQuery.value.trim()) query.q = searchQuery.value.trim()
  if (selectedTag.value.trim()) query.tag = selectedTag.value.trim()
  router.replace({ query })
}

const setSort = (sort: SortOption) => {
  if (currentSort.value === sort) return
  currentSort.value = sort
  updateUrlParams()
  fetchPalettes(true)
}

const handleSearch = () => {
  updateUrlParams()
  fetchPalettes(true)
}

const clearSearch = () => {
  searchQuery.value = ''
  updateUrlParams()
  fetchPalettes(true)
}

const toggleTag = (tag: string) => {
  if (selectedTag.value === tag) {
    selectedTag.value = ''
  } else {
    selectedTag.value = tag
  }
  updateUrlParams()
  fetchPalettes(true)
}

const loadMore = () => {
  if (isLoadingMore.value || !hasMore.value) return
  page.value++
  fetchPalettes(false)
}

const handlePaletteDeleted = (id: string) => {
  palettes.value = palettes.value.filter(p => p.id !== id)
}

const getRandomSinglePalette = async () => {
  try {
    const random = await paletteApi.getRandomPalette()
    if (random && random.id) {
      router.push(`/palettes/${random.id}`)
    }
  } catch (err) {
    errorMessage.value = extractErrorMessage(err)
  }
}

onMounted(() => {
  fetchPalettes(true)
})

watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.sort && newQuery.sort !== currentSort.value) {
      currentSort.value = newQuery.sort as SortOption
    }
  }
)
</script>

<template>
  <div class="discover-view">
    <!-- Hero / Discovery Header -->
    <div class="discover-header">
      <div class="header-intro">
        <h1 class="title-lg">Discover Palettes</h1>
        <p class="subtitle">Explore thousands of beautifully curated 4-color palettes for your next design project.</p>
      </div>

      <!-- Quick Random Button -->
      <button class="btn btn-secondary random-btn" @click="getRandomSinglePalette">
        <Dices :size="18" />
        <span>I'm Feeling Lucky</span>
      </button>
    </div>

    <!-- Search & Filter Controls -->
    <div class="filter-bar">
      <!-- Search Input -->
      <div class="search-box">
        <Search :size="18" class="search-icon" />
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Search palettes by name..."
          @keydown.enter="handleSearch"
        />
        <button
          v-if="searchQuery"
          class="clear-btn"
          aria-label="Clear search"
          @click="clearSearch"
        >
          <X :size="16" />
        </button>
      </div>

      <!-- Sort Tabs -->
      <div class="sort-tabs" role="tablist" aria-label="Sort palettes">
        <button
          class="sort-tab"
          :class="{ active: currentSort === 'newest' }"
          role="tab"
          :aria-selected="currentSort === 'newest'"
          @click="setSort('newest')"
        >
          <Clock :size="16" />
          <span>Newest</span>
        </button>

        <button
          class="sort-tab"
          :class="{ active: currentSort === 'popular' }"
          role="tab"
          :aria-selected="currentSort === 'popular'"
          @click="setSort('popular')"
        >
          <TrendingUp :size="16" />
          <span>Popular</span>
        </button>

        <button
          class="sort-tab"
          :class="{ active: currentSort === 'random' }"
          role="tab"
          :aria-selected="currentSort === 'random'"
          @click="setSort('random')"
        >
          <Sparkles :size="16" />
          <span>Random</span>
        </button>
      </div>
    </div>

    <!-- Popular Tag Pills -->
    <div class="tags-scroller">
      <button
        v-for="tag in popularTags"
        :key="tag"
        class="tag-pill"
        :class="{ active: selectedTag === tag }"
        @click="toggleTag(tag)"
      >
        #{{ tag }}
      </button>
    </div>

    <!-- Loading Skeleton State -->
    <LoadingSkeleton v-if="isLoading" :count="6" />

    <!-- Error State -->
    <ErrorState
      v-else-if="errorMessage"
      :message="errorMessage"
      :can-retry="true"
      @retry="fetchPalettes(true)"
    />

    <!-- Empty State -->
    <div v-else-if="palettes.length === 0" class="empty-state">
      <p class="empty-text">No color palettes found matching your criteria.</p>
      <button class="btn btn-secondary" @click="clearSearch">Reset Filters</button>
    </div>

    <!-- Palettes Grid -->
    <div v-else class="palettes-container">
      <div class="palettes-grid">
        <PaletteCard
          v-for="palette in palettes"
          :key="palette.id"
          :palette="palette"
          @deleted="handlePaletteDeleted"
        />
      </div>

      <!-- Load More Button -->
      <div v-if="hasMore" class="load-more-section">
        <button
          class="btn btn-secondary load-more-btn"
          :disabled="isLoadingMore"
          @click="loadMore"
        >
          <span v-if="isLoadingMore">Loading more...</span>
          <span v-else>Load More Palettes</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.discover-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.discover-header {
  display: flex;
  flex-direction: column;
  gap: 16px;
  justify-content: space-between;
}

@media (min-width: 640px) {
  .discover-header {
    flex-direction: row;
    align-items: flex-end;
  }
}

.header-intro {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.random-btn {
  flex-shrink: 0;
}

.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

@media (min-width: 768px) {
  .filter-bar {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
  flex: 1;
  max-width: 480px;
}

.search-icon {
  position: absolute;
  left: 14px;
  color: var(--text-muted);
  pointer-events: none;
}

.search-box input {
  width: 100%;
  padding-left: 42px;
  padding-right: 36px;
}

.clear-btn {
  position: absolute;
  right: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
}

.clear-btn:hover {
  color: var(--text-primary);
}

.sort-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
  background-color: var(--bg-surface);
  border: 1px solid var(--border-color);
  padding: 4px;
  border-radius: var(--radius-md);
}

.sort-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
  transition: all 0.15s ease;
}

.sort-tab:hover {
  color: var(--text-primary);
}

.sort-tab.active {
  background-color: var(--text-primary);
  color: var(--text-inverse);
}

.tags-scroller {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 4px;
  scrollbar-width: thin;
}

.tag-pill {
  white-space: nowrap;
  padding: 5px 12px;
  border-radius: var(--radius-full);
  font-size: 0.8rem;
  font-weight: 600;
  background-color: var(--bg-surface);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  transition: all 0.15s ease;
}

.tag-pill:hover {
  border-color: var(--border-color-focus);
  color: var(--text-primary);
}

.tag-pill.active {
  background-color: var(--text-primary);
  color: var(--text-inverse);
  border-color: var(--text-primary);
}

.palettes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 20px;
  text-align: center;
  gap: 16px;
  color: var(--text-secondary);
}

.load-more-section {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}

.load-more-btn {
  padding: 10px 24px;
}
</style>
