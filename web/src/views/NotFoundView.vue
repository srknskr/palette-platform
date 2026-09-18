<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { Compass, HelpCircle } from 'lucide-vue-next'
</script>

<template>
  <div class="not-found-page">
    <div class="card not-found-card">
      <div class="code-badge">404</div>
      <h1 class="title-lg">Page Not Found</h1>
      <p class="subtitle">
        The palette or page you are looking for doesn't exist or may have been moved.
      </p>

      <div class="actions-row">
        <RouterLink to="/" class="btn btn-primary">
          <Compass :size="18" />
          <span>Explore Palettes</span>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 200px);
  padding: 20px;
}

.not-found-card {
  max-width: 480px;
  width: 100%;
  padding: 48px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 16px;
}

.code-badge {
  font-family: var(--font-mono);
  font-size: 3rem;
  font-weight: 800;
  color: var(--text-muted);
  line-height: 1;
}

.actions-row {
  margin-top: 12px;
}
</style>
