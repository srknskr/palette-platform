<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { paletteApi } from '@/api/endpoints'
import { useToastStore } from '@/stores/toast'
import { extractErrorMessage } from '@/api/client'
import { validatePaletteColors, getRandomHexColor } from '@/utils/colorValidator'
import ColorSwatch from '@/components/ColorSwatch.vue'
import ColorInput from '@/components/ColorInput.vue'
import { Dices, Shuffle, Plus, ArrowLeft } from 'lucide-vue-next'

const router = useRouter()
const toastStore = useToastStore()

const name = ref('')
const description = ref('')
const tagsInput = ref('')

// Initialize with 4 beautiful modern colors
const colors = ref<string[]>([
  '#2B2D42',
  '#8D99AE',
  '#EDF2F4',
  '#EF233C'
])

const isSubmitting = ref(false)
const clientError = ref<string | null>(null)

const parsedTags = computed(() => {
  if (!tagsInput.value.trim()) return []
  return tagsInput.value
    .split(',')
    .map(t => t.trim().toLowerCase())
    .filter(t => t.length > 0)
})

const barHeights = ['110px', '75px', '55px', '45px']

const randomizeAll = () => {
  colors.value = [
    getRandomHexColor(),
    getRandomHexColor(),
    getRandomHexColor(),
    getRandomHexColor()
  ]
}

const shuffleColors = () => {
  const shuffled = [...colors.value]
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    const temp = shuffled[i]
    shuffled[i] = shuffled[j]
    shuffled[j] = temp
  }
  colors.value = shuffled
}

const handleSubmit = async () => {
  clientError.value = null

  if (!name.value.trim()) {
    clientError.value = 'Please provide a name for your palette'
    return
  }

  const validation = validatePaletteColors(colors.value)
  if (!validation.valid) {
    clientError.value = validation.error || 'Invalid colors'
    return
  }

  isSubmitting.value = true
  try {
    const created = await paletteApi.createPalette({
      name: name.value.trim(),
      description: description.value.trim() || undefined,
      colors: colors.value,
      tags: parsedTags.value
    })

    toastStore.addToast('Palette created successfully!', 'success')
    router.push(`/palettes/${created.id}`)
  } catch (err) {
    clientError.value = extractErrorMessage(err)
    toastStore.addToast(clientError.value, 'error')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="create-palette-view">
    <div class="header-nav">
      <button class="btn btn-ghost" @click="router.back()">
        <ArrowLeft :size="18" />
        <span>Back</span>
      </button>
    </div>

    <div class="create-header">
      <h1 class="title-lg">Create New Palette</h1>
      <p class="subtitle">Craft a harmonious 4-color palette and share it with the community.</p>
    </div>

    <div class="create-layout">
      <!-- Live Preview Column -->
      <div class="preview-col">
        <div class="preview-sticky">
          <div class="card preview-card">
            <div class="preview-label">Live Preview</div>
            <div class="preview-bars">
              <ColorSwatch
                v-for="(color, index) in colors"
                :key="`preview-${index}-${color}`"
                :color="color"
                :height="barHeights[index]"
                :show-hex="true"
                :can-copy="false"
                :label="`Color ${index + 1}`"
              />
            </div>
            <div class="preview-meta">
              <div class="palette-preview-title">{{ name || 'Untitled Palette' }}</div>
              <div class="preview-tags">
                <span v-for="tag in parsedTags" :key="tag" class="badge">#{{ tag }}</span>
              </div>
            </div>
          </div>

          <!-- Quick Palette Actions -->
          <div class="quick-actions">
            <button class="btn btn-secondary action-flex" @click="shuffleColors">
              <Shuffle :size="16" />
              <span>Shuffle Order</span>
            </button>
            <button class="btn btn-secondary action-flex" @click="randomizeAll">
              <Dices :size="16" />
              <span>Randomize Colors</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Editor Form Column -->
      <div class="form-col">
        <form class="card form-card" @submit.prevent="handleSubmit">
          <div v-if="clientError" class="alert-box">
            <span>{{ clientError }}</span>
          </div>

          <!-- Palette Name -->
          <div class="form-group">
            <label for="palette-name">Palette Name <span class="required">*</span></label>
            <input
              id="palette-name"
              v-model="name"
              type="text"
              required
              placeholder="e.g. Nordic Twilight, Matcha Latte..."
              maxlength="100"
            />
          </div>

          <!-- Palette Description -->
          <div class="form-group">
            <div class="label-with-counter">
              <label for="palette-desc">Description (optional)</label>
              <span :class="['char-counter', { 'char-counter-danger': description.length > 250 }]">
                {{ description.length }}/250
              </span>
            </div>
            <textarea
              id="palette-desc"
              v-model="description"
              rows="3"
              placeholder="Describe the mood, inspiration, or intended use cases..."
              maxlength="250"
            ></textarea>
          </div>

          <!-- Color Inputs (Exactly 4) -->
          <div class="form-group">
            <label>Colors (Exactly 4 Hex Codes) <span class="required">*</span></label>
            <div class="color-inputs-list">
              <ColorInput
                v-for="(_, index) in colors"
                :key="index"
                v-model="colors[index]"
                :index="index"
                :label="`Color ${index + 1}`"
              />
            </div>
          </div>

          <!-- Tags Input -->
          <div class="form-group">
            <label for="palette-tags">Tags (comma-separated)</label>
            <input
              id="palette-tags"
              v-model="tagsInput"
              type="text"
              placeholder="minimal, dark, ui, vintage"
            />
            <span class="field-hint">Separate tags with commas.</span>
          </div>

          <button
            type="submit"
            class="btn btn-primary submit-btn"
            :disabled="isSubmitting"
          >
            <Plus :size="18" />
            <span v-if="isSubmitting">Publishing Palette...</span>
            <span v-else>Publish Palette</span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.create-palette-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.create-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.create-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 32px;
  align-items: start;
}

@media (min-width: 960px) {
  .create-layout {
    grid-template-columns: 380px 1fr;
  }
}

.preview-sticky {
  position: sticky;
  top: 88px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-card {
  overflow: hidden;
}

.preview-label {
  padding: 10px 16px;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-muted);
  background-color: var(--bg-surface-hover);
  border-bottom: 1px solid var(--border-color);
}

.preview-bars {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.preview-meta {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.palette-preview-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.quick-actions {
  display: flex;
  gap: 10px;
}

.action-flex {
  flex: 1;
}

.form-card {
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.alert-box {
  padding: 12px 16px;
  background-color: rgba(229, 62, 62, 0.1);
  border: 1px solid var(--danger-color);
  border-radius: var(--radius-md);
  color: var(--danger-color);
  font-size: 0.9rem;
  font-weight: 500;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-primary);
}

.label-with-counter {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.char-counter {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.char-counter-danger {
  color: var(--danger-color);
  font-weight: 600;
}

.required {
  color: var(--danger-color);
}

.color-inputs-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.submit-btn {
  padding: 12px 24px;
  font-size: 1rem;
  margin-top: 8px;
}
</style>
