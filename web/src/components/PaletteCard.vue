<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Palette } from '@/api/types'
import { paletteApi } from '@/api/endpoints'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import ColorSwatch from './ColorSwatch.vue'
import { Heart, Share2, Tag, Trash2 } from 'lucide-vue-next'
import { shareUrl } from '@/utils/clipboard'

const props = defineProps<{
  palette: Palette
  showActions?: boolean
  isDeletable?: boolean
}>()

const emit = defineEmits<{
  (e: 'deleted', id: string): void
  (e: 'favorited', id: string, isLiked: boolean): void
}>()

const router = useRouter()
const authStore = useAuthStore()
const toastStore = useToastStore()

const isLiked = ref(props.palette.likedByMe ?? props.palette.isLiked ?? false)
const likesCount = ref(props.palette.likeCount ?? props.palette.likesCount ?? 0)
const isTogglingLike = ref(false)
const isDeleting = ref(false)

const barHeights = ['70px', '45px', '35px', '30px']

const formattedDate = computed(() => {
  if (!props.palette.createdAt) return ''
  const d = new Date(props.palette.createdAt)
  return d.toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric'
  })
})

const navigateToDetail = () => {
  router.push(`/palettes/${props.palette.id}`)
}

const toggleLike = async (e: Event) => {
  e.stopPropagation()
  if (!authStore.isAuthenticated) {
    toastStore.addToast('Please log in to save favorites', 'info')
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }

  if (isTogglingLike.value) return
  isTogglingLike.value = true

  const originalState = isLiked.value
  const originalCount = likesCount.value

  // Optimistic update
  isLiked.value = !originalState
  likesCount.value = originalState ? originalCount - 1 : originalCount + 1

  try {
    if (originalState) {
      await paletteApi.unfavoritePalette(props.palette.id)
    } else {
      await paletteApi.favoritePalette(props.palette.id)
    }
    emit('favorited', props.palette.id, isLiked.value)
  } catch {
    // Rollback
    isLiked.value = originalState
    likesCount.value = originalCount
    toastStore.addToast('Failed to update favorite', 'error')
  } finally {
    isTogglingLike.value = false
  }
}

const handleShare = async (e: Event) => {
  e.stopPropagation()
  const detailUrl = `${window.location.origin}/palettes/${props.palette.id}`
  const success = await shareUrl(
    props.palette.name,
    `Check out the "${props.palette.name}" color palette on Palette Platform!`,
    detailUrl
  )
  if (success) {
    toastStore.addToast('Palette link copied to clipboard!', 'success')
  }
}

const handleDelete = async (e: Event) => {
  e.stopPropagation()
  if (isDeleting.value) return
  if (!confirm(`Are you sure you want to delete "${props.palette.name}"?`)) {
    return
  }

  isDeleting.value = true
  try {
    await paletteApi.deletePalette(props.palette.id)
    toastStore.addToast('Palette deleted successfully', 'success')
    emit('deleted', props.palette.id)
  } catch {
    toastStore.addToast('Failed to delete palette', 'error')
  } finally {
    isDeleting.value = false
  }
}
</script>

<template>
  <div class="card palette-card" @click="navigateToDetail" :tabindex="0" @keydown.enter="navigateToDetail">
    <!-- 4-Bar Stacked Vertical Hierarchy (70px, 45px, 35px, 30px) -->
    <div class="palette-bars" role="img" :aria-label="`Color palette: ${palette.name}`">
      <ColorSwatch
        v-for="(color, index) in palette.colors.slice(0, 4)"
        :key="`${palette.id}-${index}-${color}`"
        :color="color"
        :height="barHeights[index] || '35px'"
        :show-hex="index === 0"
        :can-copy="true"
      />
    </div>

    <!-- Palette Card Meta -->
    <div class="card-meta">
      <div class="meta-header">
        <h3 class="palette-title" :title="palette.name">{{ palette.name }}</h3>
        <div class="meta-actions" @click.stop>
          <button
            class="action-btn like-btn"
            :class="{ active: isLiked }"
            :title="isLiked ? 'Unlike' : 'Like'"
            :aria-label="isLiked ? 'Unlike palette' : 'Like palette'"
            @click="toggleLike"
          >
            <Heart :size="18" :fill="isLiked ? 'currentColor' : 'none'" />
            <span class="like-count">{{ likesCount }}</span>
          </button>
          
          <button
            class="action-btn"
            title="Share palette"
            aria-label="Share palette"
            @click="handleShare"
          >
            <Share2 :size="17" />
          </button>

          <button
            v-if="isDeletable || palette.isOwner"
            class="action-btn delete-btn"
            title="Delete palette"
            aria-label="Delete palette"
            :disabled="isDeleting"
            @click="handleDelete"
          >
            <Trash2 :size="17" />
          </button>
        </div>
      </div>

      <p v-if="palette.description" class="palette-description">
        {{ palette.description }}
      </p>

      <div class="meta-footer">
        <div v-if="palette.tags && palette.tags.length > 0" class="tags-container">
          <span v-for="tag in palette.tags.slice(0, 3)" :key="tag" class="badge">
            <Tag :size="11" />
            {{ tag }}
          </span>
        </div>
        <span v-if="formattedDate" class="created-date">{{ formattedDate }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.palette-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  cursor: pointer;
  outline: none;
}

.palette-card:focus-visible {
  box-shadow: 0 0 0 3px var(--accent-color);
}

.palette-bars {
  display: flex;
  flex-direction: column;
  width: 100%;
  border-top-left-radius: calc(var(--radius-lg) - 1px);
  border-top-right-radius: calc(var(--radius-lg) - 1px);
  overflow: hidden;
}

.card-meta {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  background-color: var(--bg-surface);
}

.meta-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.palette-title {
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin: 0;
}

.meta-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 8px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 0.8rem;
  font-weight: 600;
  transition: all 0.15s ease;
}

.action-btn:hover {
  background-color: var(--bg-surface-hover);
  color: var(--text-primary);
}

.like-btn.active {
  color: var(--like-color);
}

.delete-btn:hover {
  color: var(--danger-color);
  background-color: rgba(229, 62, 62, 0.1);
}

.palette-description {
  font-size: 0.875rem;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  margin: 0;
}

.meta-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
  font-size: 0.75rem;
  color: var(--text-muted);
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.created-date {
  margin-left: auto;
  font-family: var(--font-mono);
}
</style>
