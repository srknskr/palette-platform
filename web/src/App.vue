<script setup lang="ts">
import { RouterView } from 'vue-router'
import AppNavigation from '@/components/AppNavigation.vue'
import AppHeader from '@/components/AppHeader.vue'
import ToastMessage from '@/components/ToastMessage.vue'
</script>

<template>
  <div class="app-layout">
    <!-- Desktop Sidebar Navigation & Mobile Bottom Navigation -->
    <AppNavigation />

    <!-- Main Content Flow -->
    <div class="main-content">
      <!-- Mobile Top Bar -->
      <AppHeader />

      <main class="page-container" id="main-content" role="main">
        <RouterView />
      </main>
    </div>

    <!-- Global Toast Notifications -->
    <ToastMessage />
  </div>
</template>

<style scoped>
/* App shell styling inherits from main.css */
</style>
