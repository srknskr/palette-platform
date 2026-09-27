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
import { ArrowLeft, Heart, Share2, Trash2, Tag, Calendar, User as UserIcon, Code2, Copy, Check } from 'lucide-vue-next'
import { shareUrl, copyToClipboard } from '@/utils/clipboard'
import { EXPORT_FORMATS, exportPalette, type ExportFormat } from '@/utils/paletteExporter'

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
const selectedFormat = ref<ExportFormat>('css')
const isCodeCopied = ref(false)

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

const exportedCode = computed(() => {
  if (!palette.value) return ''
  return exportPalette(
    {
      name: palette.value.name,
      colors: palette.value.colors,
      description: palette.value.description
    },
    selectedFormat.value
  )
})

const copyExportCode = async () => {
  if (!exportedCode.value) return
  const success = await copyToClipboard(exportedCode.value)
  if (success) {
    isCodeCopied.value = true
    toastStore.addToast(`Copied ${selectedFormat.value.toUpperCase()} code!`, 'success')
    setTimeout(() => {
      isCodeCopied.value = false
    }, 2000)
  }
}

const scrollToExport = () => {
  const exportSection = document.getElementById('export-section')
  if (exportSection) {
    exportSection.scrollIntoView({ behavior: 'smooth' })
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

              <button class="btn btn-secondary" @click="scrollToExport">
                <Code2 :size="18" />
                <span>Export Code</span>
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

      <!-- Multi-format Export Card -->
      <div id="export-section" class="card export-card">
        <div class="export-header">
          <div>
            <h3 class="title-md">Export Palette Code</h3>
            <p class="subtitle">Drop this palette directly into your web, Android, iOS, or backend project:</p>
          </div>
          <button class="btn btn-secondary copy-btn" @click="copyExportCode">
            <component :is="isCodeCopied ? Check : Copy" :size="16" />
            <span>{{ isCodeCopied ? 'Copied!' : 'Copy Code' }}</span>
          </button>
        </div>

        <div class="format-tabs">
          <button
            v-for="format in EXPORT_FORMATS"
            :key="format.id"
            class="tab-btn"
            :class="{ active: selectedFormat === format.id }"
            @click="selectedFormat = format.id"
          >
            {{ format.name }}
          </button>
        </div>

        <div class="code-container">
          <pre class="code-box"><code>{{ exportedCode }}</code></pre>
        </div>
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
  gap: 16px;
}

.export-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: space-between;
}

@media (min-width: 640px) {
  .export-header {
    flex-direction: row;
    align-items: center;
  }
}

.copy-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  align-self: flex-start;
}

.format-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 12px;
}

.tab-btn {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  border-radius: var(--radius-sm);
  padding: 6px 14px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn:hover {
  background-color: var(--bg-surface-hover);
  color: var(--text-primary);
}

.tab-btn.active {
  background-color: var(--primary-color, #2563eb);
  color: #ffffff;
  border-color: var(--primary-color, #2563eb);
}

.code-container {
  position: relative;
}

.code-box {
  background-color: var(--bg-primary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 18px;
  font-family: var(--font-mono);
  font-size: 0.9rem;
  line-height: 1.5;
  overflow-x: auto;
  color: var(--text-primary);
  margin: 0;
}
</style>
