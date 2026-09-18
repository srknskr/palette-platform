<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { UserPlus } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toastStore = useToastStore()

const displayName = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const isSubmitting = ref(false)
const errorMessage = ref<string | null>(null)

const handleSubmit = async () => {
  errorMessage.value = null

  if (!email.value || !password.value) {
    errorMessage.value = 'Please provide an email and password'
    return
  }

  if (password.value.length < 8) {
    errorMessage.value = 'Password must be at least 8 characters long'
    return
  }

  if (password.value !== confirmPassword.value) {
    errorMessage.value = 'Passwords do not match'
    return
  }

  isSubmitting.value = true
  try {
    await authStore.register({
      email: email.value.trim(),
      password: password.value,
      displayName: displayName.value.trim() || undefined
    })
    toastStore.addToast('Account created successfully!', 'success')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch {
    errorMessage.value = authStore.error || 'Registration failed'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="card auth-card">
      <div class="auth-header">
        <h1 class="title-md">Create Account</h1>
        <p class="subtitle">Join Palette Platform to curate and share color palettes.</p>
      </div>

      <div v-if="errorMessage" class="alert-box">
        <span>{{ errorMessage }}</span>
      </div>

      <form class="auth-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="register-name">Display Name (optional)</label>
          <input
            id="register-name"
            v-model="displayName"
            type="text"
            autocomplete="name"
            placeholder="e.g. Alex Morgan"
            maxlength="50"
          />
        </div>

        <div class="form-group">
          <label for="register-email">Email Address</label>
          <input
            id="register-email"
            v-model="email"
            type="email"
            required
            autocomplete="email"
            placeholder="you@example.com"
          />
        </div>

        <div class="form-group">
          <label for="register-password">Password (min. 8 characters)</label>
          <input
            id="register-password"
            v-model="password"
            type="password"
            required
            autocomplete="new-password"
            placeholder="••••••••"
            minlength="8"
          />
        </div>

        <div class="form-group">
          <label for="register-confirm">Confirm Password</label>
          <input
            id="register-confirm"
            v-model="confirmPassword"
            type="password"
            required
            autocomplete="new-password"
            placeholder="••••••••"
            minlength="8"
          />
        </div>

        <button type="submit" class="btn btn-primary submit-btn" :disabled="isSubmitting">
          <UserPlus :size="18" />
          <span v-if="isSubmitting">Creating Account...</span>
          <span v-else>Register</span>
        </button>
      </form>

      <div class="auth-footer">
        <span class="footer-text">Already have an account?</span>
        <RouterLink
          :to="{ path: '/login', query: route.query }"
          class="auth-link"
        >
          Sign in
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 160px);
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  padding: 32px 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.auth-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
  text-align: center;
}

.alert-box {
  padding: 10px 14px;
  background-color: rgba(229, 62, 62, 0.1);
  border: 1px solid var(--danger-color);
  border-radius: var(--radius-md);
  color: var(--danger-color);
  font-size: 0.875rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-primary);
}

.submit-btn {
  margin-top: 8px;
  padding: 11px 20px;
}

.auth-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 0.875rem;
  padding-top: 12px;
  border-top: 1px solid var(--border-color-subtle);
}

.footer-text {
  color: var(--text-secondary);
}

.auth-link {
  font-weight: 600;
  color: var(--accent-color);
}

.auth-link:hover {
  text-decoration: underline;
}
</style>
