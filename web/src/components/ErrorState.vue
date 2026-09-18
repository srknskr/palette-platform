<script setup lang="ts">
import { AlertCircle, RefreshCw } from 'lucide-vue-next'

defineProps<{
  title?: string
  message: string
  canRetry?: boolean
}>()

const emit = defineEmits<{
  (e: 'retry'): void
}>()
</script>

<template>
  <div class="error-state">
    <div class="icon-circle">
      <AlertCircle :size="32" />
    </div>
    <h3 class="error-title">{{ title || 'Something went wrong' }}</h3>
    <p class="error-message">{{ message }}</p>
    <button v-if="canRetry" class="btn btn-secondary retry-btn" @click="emit('retry')">
      <RefreshCw :size="16" />
      Try Again
    </button>
  </div>
</template>

<style scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 48px 24px;
  background-color: var(--bg-surface);
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-lg);
  max-width: 480px;
  margin: 32px auto;
}

.icon-circle {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-full);
  background-color: rgba(229, 62, 62, 0.1);
  color: var(--danger-color);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.error-title {
  font-size: 1.15rem;
  font-weight: 700;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.error-message {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin-bottom: 20px;
  line-height: 1.5;
}

.retry-btn {
  gap: 8px;
}
</style>
