<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const mode = ref('login')
const pending = ref(false)
const errorMessage = ref('')
const form = ref({
  username: '',
  password: '',
  displayName: '',
})
const redirectTarget = computed(() => String(route.query.redirect || ''))
const cameFromShareLink = computed(() => redirectTarget.value.startsWith('/share/'))

async function submit() {
  pending.value = true
  errorMessage.value = ''

  try {
    if (mode.value === 'login') {
      await authStore.signIn({
        username: form.value.username,
        password: form.value.password,
      })
    } else {
      await authStore.signUp(form.value)
    }

    router.push(String(route.query.redirect || '/schedules'))
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    pending.value = false
  }
}
</script>

<template>
  <div class="min-h-screen px-4 py-4 lg:flex lg:items-center lg:px-6 lg:py-6">
    <div class="mx-auto grid w-full max-w-[1400px] items-stretch gap-6 xl:grid-cols-[minmax(0,1.3fr)_420px]">
      <section class="glass-card relative h-full overflow-hidden p-8 lg:p-10">
        <div class="absolute left-0 top-0 h-full w-full bg-[radial-gradient(circle_at_20%_10%,rgba(223,93,67,0.16),transparent_26%),radial-gradient(circle_at_80%_20%,rgba(31,111,235,0.12),transparent_22%)]"></div>
        <div class="relative flex h-full flex-col">
          <p class="section-tag">ClassWeave</p>
          <h1 class="max-w-[10ch] font-heading text-6xl leading-[0.92] tracking-[-0.06em] lg:text-8xl">
            共享课程安排，从此不用反复问时间
          </h1>
          <p class="mt-6 max-w-2xl text-base leading-8 text-muted">
            在 ClassWeave 里，你可以管理自己的多张课程表，把课表分享给朋友，叠加查看多人安排，并快速判断谁在上课、谁现在空闲、谁快下课。
          </p>

          <div class="mt-auto pt-10">
            <div class="grid gap-4 md:grid-cols-3">
              <article class="metric-card">
                <div class="text-sm text-muted">多课表管理</div>
                <div class="mt-2 text-lg font-semibold">按学期、用途自由整理</div>
              </article>
              <article class="metric-card">
                <div class="text-sm text-muted">叠加查看</div>
                <div class="mt-2 text-lg font-semibold">多人安排放进同一张周视图</div>
              </article>
              <article class="metric-card">
                <div class="text-sm text-muted">状态判断</div>
                <div class="mt-2 text-lg font-semibold">更快看出谁现在方便联系</div>
              </article>
            </div>
          </div>
        </div>
      </section>

      <section class="panel-card flex h-full flex-col">
        <div class="rounded-full bg-ink/5 p-1">
          <button
            class="w-1/2 rounded-full px-4 py-3 text-sm font-semibold"
            :class="mode === 'login' ? 'bg-white text-ink' : 'text-muted'"
            type="button"
            @click="mode = 'login'"
          >
            登录
          </button>
          <button
            class="w-1/2 rounded-full px-4 py-3 text-sm font-semibold"
            :class="mode === 'register' ? 'bg-white text-ink' : 'text-muted'"
            type="button"
            @click="mode = 'register'"
          >
            注册
          </button>
        </div>

        <div class="mt-6">
          <p class="section-tag">{{ mode === 'login' ? '登录入口' : '注册入口' }}</p>
          <h2 class="font-heading text-4xl tracking-[-0.05em]">
            {{ mode === 'login' ? '登录后开始管理课表' : '创建账号，建立你的课表空间' }}
          </h2>
          <p class="mt-3 text-sm leading-6 text-muted">
            {{ mode === 'login' ? '继续查看你的课程安排、共享课表和实时状态。' : '注册后即可创建课表、导入课程并生成分享链接。' }}
          </p>
          <p v-if="cameFromShareLink" class="mt-4 rounded-[20px] bg-blue/10 px-4 py-3 text-sm text-blue">
            登录或注册后，你还需要再确认一次，才能把别人分享的课表导入到你的列表里。
          </p>
        </div>

        <form class="mt-6 grid flex-1 gap-4" @submit.prevent="submit">
          <label class="field-label">
            账号
            <input v-model="form.username" class="field-input" type="text" placeholder="请输入账号">
          </label>

          <label v-if="mode === 'register'" class="field-label">
            显示名称
            <input v-model="form.displayName" class="field-input" type="text" placeholder="例如：小林">
          </label>

          <label class="field-label">
            密码
            <input v-model="form.password" class="field-input" type="password" placeholder="请输入密码">
          </label>

          <p v-if="errorMessage" class="rounded-2xl bg-coral/10 px-4 py-3 text-sm text-coral">
            {{ errorMessage }}
          </p>

          <div class="mt-auto">
            <button class="btn-primary w-full" :disabled="pending" type="submit">
              {{ pending ? '处理中...' : mode === 'login' ? '登录并进入系统' : '注册并进入系统' }}
            </button>
          </div>
        </form>
      </section>
    </div>
  </div>
</template>
