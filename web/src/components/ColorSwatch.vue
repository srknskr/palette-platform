<script setup lang="ts">
import { ref } from 'vue'
import { copyToClipboard } from '@/utils/clipboard'
import { getContrastingTextColor } from '@/utils/colorValidator'
import { useToastStore } from '@/stores/toast'
import { Check, Copy } from 'lucide-vue-next'

const props = defineProps<{
  color: string
  height?: string
  showHex?: boolean
  canCopy?: boolean
  label?: string
}>()

const toastStore = useToastStore()
const copied = ref(false)

const handleCopy = async (e?: Event) => {
  if (e) e.stopPropagation()
  if (!props.canCopy) return
  const success = await copyToClipboard(props.color)
  if (success) {
    copied.value = true
    toastStore.addToast(`Copied ${props.color} to clipboard!`, 'success', 2000)
    setTimeout(() => {
      copied.value = false
    }, 1800)
  }
}
</script>

<template>
  <div
    class="color-swatch"
    :style="{
      backgroundColor: color,
      height: height || '100%',
      color: getContrastingTextColor(color)
    }"
    :title="`Color: ${color}. Click to copy.`"
    @click="handleCopy"
  >
    <div v-if="showHex" class="swatch-content">
      <span class="hex-text">{{ color }}</span>
      <span v-if="label" class="label-text">{{ label }}</span>
    </div>
    <div v-if="canCopy" class="swatch-copy-hint">
      <Check v-if="copied" :size="16" class="copy-icon success" />
      <Copy v-else :size="16" class="copy-icon" />
    </div>
  </div>
</template>

<style scoped>
.color-swatch {
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  cursor: pointer;
  user-select: none;
  transition: filter 0.15s ease, opacity 0.15s ease;
}

.color-swatch:hover {
  filter: brightness(1.03);
}

.swatch-content {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-family: var(--font-mono);
  font-size: 0.85rem;
  font-weight: 600;
  text-transform: uppercase;
}

.label-text {
  font-size: 0.75rem;
  opacity: 0.8;
  text-transform: none;
}

.swatch-copy-hint {
  opacity: 0;
  transition: opacity 0.15s ease, transform 0.15s ease;
  transform: scale(0.9);
}

.color-swatch:hover .swatch-copy-hint {
  opacity: 1;
  transform: scale(1);
}

.copy-icon {
  stroke-width: 2.2;
}

.copy-icon.success {
  stroke: var(--success-color);
}
</style>
