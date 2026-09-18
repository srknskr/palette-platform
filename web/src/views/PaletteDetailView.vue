<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { paletteApi } from '@/api/endpoints'
import type { Palette } from '@/api/types'
import { extractErrorMessage } from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import ColorSwatch from '@/components/ColorSwatch.vue'
import LoadingSkeleton from '@/components/LoadingSkeleton.vue'
import ErrorState from '@/components/ErrorState.vue'
import { ArrowLeft, Heart, Share2, Trash2, Tag, Calendar, User as UserIcon } from 'lucide-vue-next'
import { shareUrl } from '@/utils/clipboard'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toastStore = useToastStore()

const palette = ref<Palette | null>(null)
const isLoading = ref(true)
const errorMessage = ref<string | null>(null)
const isLiked = ref(false)
const likesCount = ref(0)
const isTogglingLike = ref(false)
const isDeleting = ref(false)

const paletteId = computed(() => route.params.id as string)

const formattedDate = computed(() => {
  if (!palette.value?.createdAt) return ''
  return new Date(palette.value.createdAt).toLocaleDateString(undefined, {
    dateStyle: 'medium'
  })
})

const isOwner = computed(() => {
  if (!palette.value || !authStore.user) return false
  return palette.value.isOwner || palette.value.creatorId === authStore.user.id
})

const fetchPalette = async () => {
  isLoading.value = true
  errorMessage.value = null
  try {
    const data = await paletteApi.getPaletteById(paletteId.value)
    palette.value = data
    isLiked.value = data.likedByMe ?? data.isLiked ?? false
    likesCount.value = data.likeCount ?? data.likesCount ?? 0
  } catch (err) {
    errorMessage.value = extractErrorMessage(err)
  } finally {
    isLoading.value = false
  }
}

const toggleLike = async () => {
  if (!authStore.isAuthenticated) {
    toastStore.addToast('Please sign in to favorite this palette', 'info')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }

  if (isTogglingLike.value || !palette.value) return
  isTogglingLike.value = true

  const originalState = isLiked.value
  const originalCount = likesCount.value

  isLiked.value = !originalState
  likesCount.value = originalState ? originalCount - 1 : originalCount + 1

  try {
    if (originalState) {
      await paletteApi.unfavoritePalette(palette.value.id)
    } else {
      await paletteApi.favoritePalette(palette.value.id)
    }
  } catch {
    isLiked.value = originalState
    likesCount.value = originalCount
    toastStore.addToast('Failed to update favorite', 'error')
  } finally {
    isTogglingLike.value = false
  }
}

const handleShare = async () => {
  if (!palette.value) return
  const success = await shareUrl(
    palette.value.name,
    `Explore "${palette.value.name}" palette on Palette Platform`,
    window.location.href
  )
  if (success) {
    toastStore.addToast('Palette URL copied to clipboard!', 'success')
  }
}

const handleDelete = async () => {
  if (!palette.value || isDeleting.value) return
  if (!confirm(`Are you sure you want to permanently delete "${palette.value.name}"?`)) {
    return
  }

  isDeleting.value = true
  try {
    await paletteApi.deletePalette(palette.value.id)
    toastStore.addToast('Palette deleted', 'success')
    router.push('/')
  } catch {
    toastStore.addToast('Failed to delete palette', 'error')
  } finally {
    isDeleting.value = false
  }
}

onMounted(() => {
  fetchPalette()
})
</script>

<template>
  <div class="palette-detail-view">
    <!-- Back Navigation -->
    <div class="top-nav">
      <button class="btn btn-ghost back-btn" @click="router.back()">
        <ArrowLeft :size="18" />
        <span>Back</span>
      </button>
    </div>

    <!-- Loading State -->
    <LoadingSkeleton v-if="isLoading" :count="1" />

    <!-- Error State -->
    <ErrorState
      v-else-if="errorMessage"
      title="Palette Not Found"
      :message="errorMessage"
      :can-retry="true"
      @retry="fetchPalette"
    />

    <!-- Detail Content -->
    <div v-else-if="palette" class="palette-detail-container">
      <!-- Full Size Stacked Palette (Expanded visual hierarchy) -->
      <div class="detail-card card">
        <div class="detail-palette-bars">
          <ColorSwatch
            v-for="(color, idx) in palette.colors.slice(0, 4)"
            :key="`${palette.id}-${color}-${idx}`"
            :color="color"
            :height="idx === 0 ? '140px' : idx === 1 ? '100px' : idx === 2 ? '80px' : '70px'"
            :show-hex="true"
            :can-copy="true"
            :label="`Color ${idx + 1}`"
          />
        </div>

        <div class="detail-meta">
          <div class="meta-main-row">
            <div>
              <h1 class="palette-name title-lg">{{ palette.name }}</h1>
              <p v-if="palette.description" class="palette-desc">
                {{ palette.description }}
              </p>
            </div>

            <!-- Action Buttons -->
            <div class="detail-actions">
              <button
                class="btn btn-secondary like-btn"
                :class="{ active: isLiked }"
                @click="toggleLike"
              >
                <Heart :size="18" :fill="isLiked ? 'currentColor' : 'none'" />
                <span>{{ likesCount }} {{ likesCount === 1 ? 'Like' : 'Likes' }}</span>
              </button>

              <button class="btn btn-secondary" @click="handleShare">
                <Share2 :size="18" />
                <span>Share</span>
              </button>

              <button
                v-if="isOwner"
                class="btn btn-danger delete-btn"
                :disabled="isDeleting"
                @click="handleDelete"
              >
                <Trash2 :size="18" />
                <span>Delete</span>
              </button>
            </div>
          </div>

          <!-- Metadata Badges -->
          <div class="detail-info-row">
            <div v-if="palette.creatorName" class="info-pill">
              <UserIcon :size="14" />
              <span>Created by {{ palette.creatorName }}</span>
            </div>

            <div v-if="formattedDate" class="info-pill">
              <Calendar :size="14" />
              <span>{{ formattedDate }}</span>
            </div>

            <div v-if="palette.tags && palette.tags.length > 0" class="tags-group">
              <span v-for="tag in palette.tags" :key="tag" class="badge">
                <Tag :size="12" />
                {{ tag }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Export / CSS Code Snippet -->
      <div class="card export-card">
        <h3 class="title-md">CSS Variables Export</h3>
        <p class="subtitle">Easily drop this color palette into your stylesheet:</p>
        <pre class="code-box"><code>:root {
  --color-1: {{ palette.colors[0] }};
  --color-2: {{ palette.colors[1] }};
  --color-3: {{ palette.colors[2] }};
  --color-4: {{ palette.colors[3] }};
}</code></pre>
      </div>
    </div>
  </div>
</template>

<style scoped>
.palette-detail-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.top-nav {
  display: flex;
  align-items: center;
}

.back-btn {
  padding: 8px 12px;
}

.palette-detail-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-card {
  overflow: hidden;
}

.detail-palette-bars {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.detail-meta {
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.meta-main-row {
  display: flex;
  flex-direction: column;
  gap: 16px;
  justify-content: space-between;
}

@media (min-width: 768px) {
  .meta-main-row {
    flex-direction: row;
    align-items: flex-start;
  }
}

.palette-name {
  margin-bottom: 8px;
}

.palette-desc {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.detail-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.like-btn.active {
  color: var(--like-color);
  border-color: var(--like-color);
}

.detail-info-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color-subtle);
  color: var(--text-muted);
  font-size: 0.85rem;
}

.info-pill {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tags-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.export-card {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.code-box {
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16px;
  font-family: var(--font-mono);
  font-size: 0.9rem;
  overflow-x: auto;
  color: var(--text-primary);
}
</style>
