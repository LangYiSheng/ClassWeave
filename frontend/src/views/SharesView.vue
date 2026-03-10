<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import AppShell from '@/components/AppShell.vue'
import MetricCard from '@/components/MetricCard.vue'
import PageHero from '@/components/PageHero.vue'
import { useAuthStore } from '@/stores/auth'
import { useScheduleStore } from '@/stores/schedules'
import { buildShareUrl, extractShareToken } from '@/utils/share'

const router = useRouter()
const authStore = useAuthStore()
const scheduleStore = useScheduleStore()

const selectedOwnerScheduleId = ref('')
const incomingShareToken = ref('')
const message = ref('')
const messageType = ref('success')

onMounted(async () => {
  await scheduleStore.hydrateForUser(authStore.user.id)
  selectedOwnerScheduleId.value =
    scheduleStore.summaries.find((item) => item.accessRole === 'OWNER')?.id ?? ''
})

const ownerSchedules = computed(() =>
  scheduleStore.summaries.filter((item) => item.accessRole === 'OWNER'),
)

async function createShareLink() {
  if (!selectedOwnerScheduleId.value) {
    return
  }

  await scheduleStore.createShare(selectedOwnerScheduleId.value, authStore.user.id)
  message.value = '分享链接已生成'
  messageType.value = 'success'
}

async function disableLink(shareLinkId) {
  await scheduleStore.disableShare(shareLinkId, authStore.user.id)
  message.value = '分享链接已停用'
  messageType.value = 'success'
}

async function acceptShare() {
  const shareToken = extractShareToken(incomingShareToken.value)

  if (!shareToken) {
    message.value = '请输入有效的分享链接或口令'
    messageType.value = 'error'
    return
  }

  message.value = ''
  messageType.value = 'success'
  router.push({
    name: 'share-accept',
    params: { shareToken },
  })
}

function resolveShareUrl(shareToken) {
  return buildShareUrl(shareToken)
}
</script>

<template>
  <AppShell>
    <div class="grid gap-6">
      <PageHero
        eyebrow="分享管理"
        title="把课表分享给别人，也把别人的课表加入进来"
        description="为自己的课表生成分享链接，或输入别人发来的分享口令，把共享课表加入你的可查看列表。"
      >
        <template #side>
          <MetricCard label="已创建链接" :value="String(scheduleStore.shareLinks.length)" description="可随时停用不再使用的链接" />
          <MetricCard label="可分享课表" :value="String(ownerSchedules.length)" description="从你的课表里选择一张生成分享链接" />
        </template>
      </PageHero>

      <section class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_420px]">
        <article class="panel-card">
          <div class="mb-5 flex items-start justify-between gap-4">
            <div>
              <p class="section-tag">已生成的分享</p>
              <h3 class="font-heading text-3xl tracking-[-0.04em]">链接列表</h3>
            </div>
          </div>

          <div class="grid gap-4">
            <article
              v-for="link in scheduleStore.shareLinks"
              :key="link.id"
              class="rounded-[24px] border border-ink/8 bg-white/72 p-5"
            >
              <div class="flex items-start justify-between gap-4">
                <div>
                  <div class="text-lg font-semibold">{{ link.scheduleName }}</div>
                  <div class="mt-1 text-xs text-muted">{{ link.shareToken }}</div>
                </div>
                <span class="rounded-full px-3 py-1 text-xs" :class="link.isActive ? 'bg-teal/10 text-teal' : 'bg-ink/6 text-muted'">
                  {{ link.isActive ? '生效中' : '已停用' }}
                </span>
              </div>

              <div class="mt-4 rounded-[20px] bg-[#fffaf2] px-4 py-3 text-sm text-ink">
                {{ resolveShareUrl(link.shareToken) }}
              </div>

              <div class="mt-4 flex gap-2">
                <button class="btn-ghost px-4 py-2 text-xs" type="button" @click="disableLink(link.id)">
                  停用链接
                </button>
              </div>
            </article>
          </div>
        </article>

        <div class="grid gap-6">
          <article class="panel-card">
            <div class="mb-5">
              <p class="section-tag">创建分享</p>
              <h3 class="font-heading text-3xl tracking-[-0.04em]">生成新链接</h3>
            </div>

            <label class="field-label">
              选择我的课表
              <select v-model="selectedOwnerScheduleId" class="field-input">
                <option disabled value="">请选择</option>
                <option v-for="schedule in ownerSchedules" :key="schedule.id" :value="schedule.id">
                  {{ schedule.name }}
                </option>
              </select>
            </label>

            <button class="btn-primary mt-4 w-full" type="button" @click="createShareLink">生成分享链接</button>
          </article>

          <article class="panel-card">
            <div class="mb-5">
              <p class="section-tag">领取共享课表</p>
              <h3 class="font-heading text-3xl tracking-[-0.04em]">粘贴分享链接或口令</h3>
            </div>

            <label class="field-label">
              分享内容
              <input
                v-model="incomingShareToken"
                class="field-input"
                type="text"
                placeholder="可直接粘贴完整链接，或只输入 share/ 后面的口令"
              >
            </label>

            <button class="btn-primary mt-4 w-full" type="button" @click="acceptShare">继续导入共享课表</button>
            <p
              v-if="message"
              class="mt-4 rounded-[20px] px-4 py-3 text-sm"
              :class="messageType === 'error' ? 'bg-coral/10 text-coral' : 'bg-teal/10 text-teal'"
            >
              {{ message }}
            </p>
          </article>
        </div>
      </section>
    </div>
  </AppShell>
</template>
