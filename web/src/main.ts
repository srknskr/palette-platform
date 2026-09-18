import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useThemeStore } from './stores/theme'
import { useAuthStore } from './stores/auth'

import './styles/main.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Initialize theme and restore user session
const themeStore = useThemeStore()
const authStore = useAuthStore()

// If authenticated, refresh current user details in background
if (authStore.isAuthenticated) {
  authStore.fetchProfile().catch(() => {})
}

app.mount('#app')
