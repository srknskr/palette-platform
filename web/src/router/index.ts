import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

import DiscoverView from '@/views/DiscoverView.vue'
import PaletteDetailView from '@/views/PaletteDetailView.vue'
import CreatePaletteView from '@/views/CreatePaletteView.vue'
import CollectionView from '@/views/CollectionView.vue'
import ProfileView from '@/views/ProfileView.vue'
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'discover',
    component: DiscoverView,
    meta: { title: 'Discover Palettes' }
  },
  {
    path: '/palettes/:id',
    name: 'palette-detail',
    component: PaletteDetailView,
    meta: { title: 'Palette Details' }
  },
  {
    path: '/create',
    name: 'create-palette',
    component: CreatePaletteView,
    meta: { requiresAuth: true, title: 'Create Palette' }
  },
  {
    path: '/collection',
    name: 'collection',
    component: CollectionView,
    meta: { requiresAuth: true, title: 'Your Collection' }
  },
  {
    path: '/profile',
    name: 'profile',
    component: ProfileView,
    meta: { requiresAuth: true, title: 'My Profile' }
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { guestOnly: true, title: 'Sign In' }
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { guestOnly: true, title: 'Create Account' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: NotFoundView,
    meta: { title: 'Page Not Found' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// Navigation Guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // Update document title
  const pageTitle = to.meta.title as string | undefined
  document.title = pageTitle ? `${pageTitle} — Palette Platform` : 'Palette Platform'

  // Route requires authentication
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }

  // Guest only routes (login, register)
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    next({ path: '/' })
    return
  }

  next()
})

export default router
