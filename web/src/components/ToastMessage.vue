<script setup lang="ts">
import { useToastStore } from '@/stores/toast'
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-vue-next'

const toastStore = useToastStore()
</script>

<template>
  <div class="toast-container" aria-live="polite">
    <TransitionGroup name="toast-slide">
      <div
        v-for="toast in toastStore.toasts"
        :key="toast.id"
        class="toast-item"
        :class="`toast-${toast.type}`"
      >
        <CheckCircle2 v-if="toast.type === 'success'" :size="18" class="toast-icon" />
        <AlertCircle v-else-if="toast.type === 'error'" :size="18" class="toast-icon" />
        <Info v-else :size="18" class="toast-icon" />

        <span class="toast-message">{{ toast.message }}</span>

        <button
          class="toast-close"
          aria-label="Dismiss notification"
          @click="toastStore.removeToast(toast.id)"
        >
          <X :size="14" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-container {
  position: fixed;
  bottom: 80px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 380px;
  pointer-events: none;
}

@media (min-width: 769px) {
  .toast-container {
    bottom: 24px;
  }
}

.toast-item {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: var(--radius-md);
  background-color: var(--bg-elevated);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-lg);
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text-primary);
}

.toast-success {
  border-left: 4px solid var(--success-color);
}
.toast-success .toast-icon {
  color: var(--success-color);
}

.toast-error {
  border-left: 4px solid var(--danger-color);
}
.toast-error .toast-icon {
  color: var(--danger-color);
}

.toast-info {
  border-left: 4px solid var(--accent-color);
}
.toast-info .toast-icon {
  color: var(--accent-color);
}

.toast-message {
  flex: 1;
}

.toast-close {
  color: var(--text-muted);
  display: inline-flex;
  padding: 4px;
  border-radius: var(--radius-sm);
}

.toast-close:hover {
  color: var(--text-primary);
  background-color: var(--bg-surface-hover);
}

/* Transitions */
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.25s ease;
}

.toast-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.toast-slide-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
