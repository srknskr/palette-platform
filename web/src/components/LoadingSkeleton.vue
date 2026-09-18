<script setup lang="ts">
withDefaults(
  defineProps<{
    count?: number
  }>(),
  {
    count: 6
  }
)
</script>

<template>
  <div class="skeleton-grid">
    <div v-for="i in count" :key="i" class="card skeleton-card">
      <div class="skeleton-bars">
        <div class="skeleton-bar h-70"></div>
        <div class="skeleton-bar h-45"></div>
        <div class="skeleton-bar h-35"></div>
        <div class="skeleton-bar h-30"></div>
      </div>
      <div class="skeleton-meta">
        <div class="skeleton-line w-60"></div>
        <div class="skeleton-line w-90"></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.skeleton-card {
  overflow: hidden;
  border-radius: var(--radius-lg);
}

.skeleton-bars {
  display: flex;
  flex-direction: column;
}

.skeleton-bar {
  background: linear-gradient(90deg, var(--bg-muted) 25%, var(--bg-surface-hover) 50%, var(--bg-muted) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.h-70 { height: 70px; }
.h-45 { height: 45px; }
.h-35 { height: 35px; }
.h-30 { height: 30px; }

.skeleton-meta {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.skeleton-line {
  height: 14px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, var(--bg-muted) 25%, var(--bg-surface-hover) 50%, var(--bg-muted) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.w-60 { width: 60%; }
.w-90 { width: 90%; }

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
