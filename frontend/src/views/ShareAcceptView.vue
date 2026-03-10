<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AppShell from '@/components/AppShell.vue'
import { useAuthStore } from '@/stores/auth'
import { useScheduleStore } from '@/stores/schedules'
import { buildShareUrl } from '@/utils/share'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const scheduleStore = useScheduleStore()

const pending = ref(false)
const errorMessage = ref('')

const shareToken = computed(() => String(route.params.shareToken || '').trim())
const shareUrl = computed(() => buildShareUrl(shareToken.value))
const currentUserName = computed(() => authStore.user?.displayName || authStore.user?.username || '当前账号')

async function confirmImport() {
  if (!shareToken.value || pending.value) {
    return
  }

  pending.value = true
  errorMessage.value = ''

  try {
    await scheduleStore.acceptShareLink(shareToken.value, authStore.user.id)
    router.replace({ name: 'schedules' })
  } catch (error) {
    errorMessage.value = error.message || '导入失败，请检查分享链接是否仍然有效。'
  } finally {
    pending.value = false
  }
}

function cancelImport() {
  router.replace({ name: 'schedules' })
}
</script>

<template>
  <AppShell>
    <div class="mx-auto flex min-h-[calc(100vh-8rem)] max-w-[920px] items-center">
      <section class="glass-card w-full overflow-hidden p-6 sm:p-8 lg:p-10">
        <div class="grid gap-8 lg:grid-cols-[minmax(0,1.2fr)_320px]">
          <div>
            <p class="section-tag">共享课表</p>
            <h1 class="font-heading text-4xl tracking-[-0.05em] sm:text-5xl">
              将这张课表导入到你的列表中？
            </h1>
            <p class="mt-4 max-w-2xl text-sm leading-7 text-muted sm:text-base">
              导入后，这张共享课表会出现在“我的课表”中，你可以在周视图里叠加查看，也可以随时决定是否继续保留。
            </p>

            <div class="mt-6 rounded-[24px] border border-ink/10 bg-white/72 p-5">
              <div class="text-sm font-semibold text-ink">当前将导入到</div>
              <div class="mt-2 text-2xl font-semibold tracking-[-0.03em]">{{ currentUserName }}</div>
              <div class="mt-4 text-xs leading-6 text-muted">分享口令</div>
              <div class="mt-1 break-all rounded-[18px] bg-[#fffaf2] px-4 py-3 text-sm text-ink">
                {{ shareToken }}
              </div>
              <div class="mt-4 text-xs leading-6 text-muted">分享链接</div>
              <div class="mt-1 break-all rounded-[18px] bg-[#fffaf2] px-4 py-3 text-sm text-ink">
                {{ shareUrl }}
              </div>
            </div>

            <p v-if="errorMessage" class="mt-5 rounded-[20px] bg-coral/10 px-4 py-3 text-sm text-coral">
              {{ errorMessage }}
            </p>
          </div>

          <aside class="panel-card flex flex-col justify-between gap-6 bg-white/74">
            <div>
              <p class="section-tag">确认导入</p>
              <h2 class="font-heading text-3xl tracking-[-0.04em]">准备加入你的课表页</h2>
              <p class="mt-3 text-sm leading-6 text-muted">
                确认后会立即导入这张共享课表，并跳转到“我的课表”页面。
              </p>
            </div>

            <div class="grid gap-3">
              <button class="btn-primary w-full" :disabled="pending" type="button" @click="confirmImport">
                {{ pending ? '导入中...' : '确认导入' }}
              </button>
              <button class="btn-ghost w-full" :disabled="pending" type="button" @click="cancelImport">
                先不导入
              </button>
            </div>
          </aside>
        </div>
      </section>
    </div>
  </AppShell>
</template>
