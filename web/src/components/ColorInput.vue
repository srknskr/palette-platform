<script setup lang="ts">
import { ref, watch } from 'vue'
import { isValidHexColor, normalizeHexColor, getRandomHexColor } from '@/utils/colorValidator'
import { Dices } from 'lucide-vue-next'

const props = defineProps<{
  modelValue: string
  label: string
  index: number
  disabled?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const hexInput = ref(props.modelValue)

watch(
  () => props.modelValue,
  (newVal) => {
    hexInput.value = newVal
  }
)

const handleTextInput = (e: Event) => {
  const target = e.target as HTMLInputElement
  const raw = target.value
  hexInput.value = raw
  if (isValidHexColor(raw)) {
    emit('update:modelValue', normalizeHexColor(raw))
  }
}

const handleColorPicker = (e: Event) => {
  const target = e.target as HTMLInputElement
  const color = target.value
  hexInput.value = color.toUpperCase()
  emit('update:modelValue', color.toUpperCase())
}

const randomize = () => {
  const randomColor = getRandomHexColor()
  hexInput.value = randomColor
  emit('update:modelValue', randomColor)
}
</script>

<template>
  <div class="color-input-item">
    <div class="color-preview-box" :style="{ backgroundColor: modelValue }">
      <input
        type="color"
        :value="modelValue"
        class="native-color-picker"
        aria-label="Pick color with native color picker"
        @input="handleColorPicker"
      />
    </div>

    <div class="input-details">
      <label class="color-index-label">{{ label }}</label>
      <div class="input-with-actions">
        <input
          type="text"
          :value="hexInput"
          placeholder="#000000"
          maxlength="7"
          class="hex-field"
          :class="{ invalid: !isValidHexColor(hexInput) }"
          @input="handleTextInput"
        />
        <button
          type="button"
          class="btn-icon dice-btn"
          title="Randomize this color"
          @click="randomize"
        >
          <Dices :size="16" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.color-input-item {
  display: flex;
  align-items: center;
  gap: 14px;
  background-color: var(--bg-surface);
  border: 1px solid var(--border-color);
  padding: 10px 14px;
  border-radius: var(--radius-md);
}

.color-preview-box {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  box-shadow: var(--shadow-sm);
}

.native-color-picker {
  position: absolute;
  top: -10px;
  left: -10px;
  width: 64px;
  height: 64px;
  opacity: 0;
  cursor: pointer;
}

.input-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.color-index-label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--text-muted);
}

.input-with-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hex-field {
  font-family: var(--font-mono);
  font-size: 0.95rem;
  font-weight: 600;
  padding: 6px 10px;
  width: 110px;
  text-transform: uppercase;
}

.hex-field.invalid {
  border-color: var(--danger-color);
}

.dice-btn {
  color: var(--text-secondary);
}

.dice-btn:hover {
  color: var(--text-primary);
  background-color: var(--bg-surface-hover);
}
</style>
