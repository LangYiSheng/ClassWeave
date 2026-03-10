<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import DialogModal from '@/components/DialogModal.vue'
import { mainNavigation } from '@/config/navigation'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const sidebarOpen = ref(false)
const showProfileModal = ref(false)
const showPasswordModal = ref(false)
const profilePending = ref(false)
const profileError = ref('')
const profileSuccess = ref('')
const profileForm = ref({
  displayName: '',
})
const passwordPending = ref(false)
const passwordError = ref('')
const passwordSuccess = ref('')
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})
let mediaQuery

const currentUserName = computed(() => authStore.user?.displayName ?? 'Guest')

function isActive(path) {
  if (path === '/schedules') {
    return route.path.startsWith('/schedules')
  }

  return route.path.startsWith(path)
}

function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}

function openProfileModal() {
  profileForm.value = {
    displayName: authStore.user?.displayName ?? '',
  }
  profileError.value = ''
  profileSuccess.value = ''
  showProfileModal.value = true
}

function closeProfileModal() {
  showProfileModal.value = false
  profilePending.value = false
  profileError.value = ''
  profileSuccess.value = ''
}

async function submitProfileUpdate() {
  profileError.value = ''
  profileSuccess.value = ''

  if (!profileForm.value.displayName.trim()) {
    profileError.value = '请先填写显示名称。'
    return
  }

  profilePending.value = true

  try {
    await authStore.updateProfile({
      displayName: profileForm.value.displayName.trim(),
    })
    profileSuccess.value = '显示名称已更新。'
  } catch (error) {
    profileError.value = error.message || '更新显示名称失败，请稍后再试。'
  } finally {
    profilePending.value = false
  }
}

function openPasswordModal() {
  showPasswordModal.value = true
  passwordError.value = ''
  passwordSuccess.value = ''
}

function closePasswordModal() {
  showPasswordModal.value = false
  passwordPending.value = false
  passwordError.value = ''
  passwordSuccess.value = ''
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  }
}

async function submitPasswordChange() {
  passwordError.value = ''
  passwordSuccess.value = ''

  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    passwordError.value = '请先填写当前密码和新密码。'
    return
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致。'
    return
  }

  passwordPending.value = true

  try {
    await authStore.changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    })
    passwordSuccess.value = '密码已更新。'
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    }
  } catch (error) {
    passwordError.value = error.message || '修改密码失败，请稍后再试。'
  } finally {
    passwordPending.value = false
  }
}

function syncSidebarByViewport(event) {
  sidebarOpen.value = event.matches
}

onMounted(() => {
  mediaQuery = window.matchMedia('(min-width: 1024px)')
  syncSidebarByViewport(mediaQuery)
  mediaQuery.addEventListener('change', syncSidebarByViewport)
})

onBeforeUnmount(() => {
  mediaQuery?.removeEventListener('change', syncSidebarByViewport)
})
</script>

<template>
  <div class="min-h-screen px-4 py-4 lg:px-6 lg:py-6">
    <div
      v-if="sidebarOpen"
      class="fixed inset-0 z-20 bg-ink/28 backdrop-blur-sm lg:hidden"
      @click="sidebarOpen = false"
    ></div>

    <button
      class="glass-card fixed top-4 z-40 inline-flex h-12 w-12 items-center justify-center p-0 transition-[left] duration-200 lg:top-6"
      :class="sidebarOpen ? 'left-[326px] lg:left-[336px]' : 'left-4 lg:left-6'"
      type="button"
      :aria-label="sidebarOpen ? '收起侧边栏' : '打开侧边栏'"
      @click="sidebarOpen = !sidebarOpen"
    >
      <svg aria-hidden="true" class="h-5 w-5" fill="none" viewBox="0 0 20 20">
        <path d="M3 5.25H17" stroke="currentColor" stroke-linecap="round" stroke-width="1.8" />
        <path d="M6 10H17" stroke="currentColor" stroke-linecap="round" stroke-width="1.8" />
        <path d="M3 14.75H14" stroke="currentColor" stroke-linecap="round" stroke-width="1.8" />
      </svg>
    </button>

    <aside
      class="fixed inset-y-4 left-4 z-30 w-[280px] transition duration-200 lg:inset-y-6 lg:left-6"
      :class="sidebarOpen ? 'translate-x-0 opacity-100' : 'pointer-events-none -translate-x-[120%] opacity-0'"
    >
      <div class="glass-card flex h-full flex-col p-5">
        <div class="rounded-[24px] bg-white/72 p-5">
          <p class="section-tag">ClassWeave</p>
          <h1 class="font-heading text-3xl leading-none tracking-[-0.04em] text-ink">
            课程安排，一眼看清
          </h1>
          <p class="mt-4 text-sm leading-6 text-muted">
            管理自己的课表，查看共享课表，快速判断谁现在方便联系。
          </p>
        </div>

        <nav class="mt-5 grid gap-3">
          <RouterLink
            v-for="item in mainNavigation"
            :key="item.to"
            :to="item.to"
            class="rounded-[20px] border px-4 py-4 transition"
            :class="
              isActive(item.to)
                ? 'border-white/80 bg-white/82 shadow-glow'
                : 'border-ink/5 bg-white/45 hover:border-white/70 hover:bg-white/70'
            "
          >
            <div class="text-sm font-semibold">{{ item.label }}</div>
            <div class="mt-1 text-xs leading-5 text-muted">{{ item.description }}</div>
          </RouterLink>
        </nav>

        <div class="mt-auto rounded-[20px] border border-ink/10 bg-white/72 p-4">
          <div class="text-sm font-semibold">{{ currentUserName }}</div>
          <div class="mt-1 text-xs text-muted">欢迎回来，继续查看你的课程安排。</div>
          <button class="btn-ghost mt-4 w-full" type="button" @click="openProfileModal">修改显示名称</button>
          <button class="btn-ghost mt-4 w-full" type="button" @click="openPasswordModal">重设密码</button>
          <button class="btn-ghost mt-4 w-full" type="button" @click="handleLogout">退出登录</button>
        </div>
      </div>
    </aside>

    <main
      class="pt-14 transition-[padding-left] duration-200 lg:pt-0"
      :class="sidebarOpen ? 'lg:pl-[320px]' : 'lg:pl-0'"
    >
      <div class="mx-auto max-w-[1700px]">
        <Transition name="shell-content" mode="out-in" appear>
          <div :key="route.fullPath" class="mx-auto max-w-[1440px]">
            <slot />
          </div>
        </Transition>
      </div>
    </main>

    <DialogModal
      v-if="showProfileModal"
      title="修改显示名称"
      description="设置其他用户看到你的名字。"
      @close="closeProfileModal"
    >
      <label class="field-label">
        显示名称
        <input v-model="profileForm.displayName" class="field-input" type="text">
      </label>

      <p v-if="profileError" class="mt-4 rounded-[20px] bg-coral/10 px-4 py-3 text-sm text-coral">
        {{ profileError }}
      </p>
      <p v-if="profileSuccess" class="mt-4 rounded-[20px] bg-teal/10 px-4 py-3 text-sm text-teal">
        {{ profileSuccess }}
      </p>

      <div class="mt-5 flex gap-3">
        <button class="btn-primary" :disabled="profilePending" type="button" @click="submitProfileUpdate">
          {{ profilePending ? '保存中...' : '保存显示名称' }}
        </button>
        <button class="btn-ghost" :disabled="profilePending" type="button" @click="closeProfileModal">取消</button>
      </div>
    </DialogModal>

    <DialogModal
      v-if="showPasswordModal"
      title="重设密码"
      description="输入当前密码，并设置一个新的登录密码。"
      @close="closePasswordModal"
    >
      <div class="grid gap-4">
        <label class="field-label">
          当前密码
          <input v-model="passwordForm.oldPassword" class="field-input" type="password">
        </label>
        <label class="field-label">
          新密码
          <input v-model="passwordForm.newPassword" class="field-input" type="password">
        </label>
        <label class="field-label">
          再输入一次新密码
          <input v-model="passwordForm.confirmPassword" class="field-input" type="password">
        </label>
      </div>

      <p v-if="passwordError" class="mt-4 rounded-[20px] bg-coral/10 px-4 py-3 text-sm text-coral">
        {{ passwordError }}
      </p>
      <p v-if="passwordSuccess" class="mt-4 rounded-[20px] bg-teal/10 px-4 py-3 text-sm text-teal">
        {{ passwordSuccess }}
      </p>

      <div class="mt-5 flex gap-3">
        <button class="btn-primary" :disabled="passwordPending" type="button" @click="submitPasswordChange">
          {{ passwordPending ? '提交中...' : '保存新密码' }}
        </button>
        <button class="btn-ghost" :disabled="passwordPending" type="button" @click="closePasswordModal">取消</button>
      </div>
    </DialogModal>
  </div>
</template>

<style scoped>
.shell-content-enter-active,
.shell-content-leave-active {
  transition:
    opacity 220ms ease,
    transform 220ms ease;
}

.shell-content-enter-from,
.shell-content-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

@media (prefers-reduced-motion: reduce) {
  .shell-content-enter-active,
  .shell-content-leave-active {
    transition: none;
  }

  .shell-content-enter-from,
  .shell-content-leave-to {
    opacity: 1;
    transform: none;
  }
}
</style>
