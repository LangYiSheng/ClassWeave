<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'

import AppShell from '@/components/AppShell.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import MetricCard from '@/components/MetricCard.vue'
import PageHero from '@/components/PageHero.vue'
import { useAuthStore } from '@/stores/auth'
import { useScheduleStore } from '@/stores/schedules'

const authStore = useAuthStore()
const scheduleStore = useScheduleStore()

const ownedSchedules = computed(() =>
  scheduleStore.summaries.filter((schedule) => schedule.accessRole === 'OWNER'),
)

const sharedSchedules = computed(() =>
  scheduleStore.summaries.filter((schedule) => schedule.accessRole !== 'OWNER'),
)

const sharedSchedulePendingDelete = ref(null)
const deletePending = ref(false)

onMounted(async () => {
  await scheduleStore.hydrateForUser(authStore.user.id)
})

function requestRemoveSharedSchedule(schedule) {
  sharedSchedulePendingDelete.value = schedule
}

function closeRemoveSharedScheduleDialog() {
  sharedSchedulePendingDelete.value = null
}

async function confirmRemoveSharedSchedule() {
  if (!sharedSchedulePendingDelete.value) {
    return
  }

  deletePending.value = true

  try {
    await scheduleStore.removeSharedSchedule(sharedSchedulePendingDelete.value.id, authStore.user.id)
    closeRemoveSharedScheduleDialog()
  } finally {
    deletePending.value = false
  }
}
</script>

<template>
  <AppShell>
    <div class="grid gap-6">
      <PageHero
        eyebrow="课程表总览"
        title="把自己的课表和共享课表放在一起管理"
        description="在这里查看你创建的课表、别人分享给你的课表，并快速进入编辑、叠加查看或分享操作。"
      >
        <template #actions>
          <RouterLink class="btn-primary" :to="{ name: 'schedule-edit', params: { scheduleId: 'new' } }">
            创建新课表
          </RouterLink>
          <RouterLink
            class="btn-ghost"
            :to="{ name: 'schedule-edit', params: { scheduleId: 'new' }, query: { mode: 'import' } }"
          >
            通过 JSON 导入新课表
          </RouterLink>
          <RouterLink class="btn-ghost" to="/board">去叠加查看</RouterLink>
        </template>

        <template #side>
          <MetricCard
            v-for="metric in scheduleStore.dashboardMetrics"
            :key="metric.label"
            :description="metric.description"
            :label="metric.label"
            :value="metric.value"
          />
        </template>
      </PageHero>

      <section class="panel-card">
        <div class="mb-5 flex items-start justify-between gap-4">
          <div>
            <p class="section-tag">课表列表</p>
            <h3 class="font-heading text-3xl tracking-[-0.04em]">我的课表</h3>
          </div>
        </div>

        <div class="grid gap-8">
          <section class="grid gap-4">
            <div class="flex items-center justify-between gap-3">
              <h4 class="text-lg font-semibold text-ink">我创建的课表</h4>
              <span class="rounded-full bg-ink/5 px-3 py-1 text-xs text-muted">
                {{ ownedSchedules.length }} 张
              </span>
            </div>

            <div
              v-if="ownedSchedules.length"
              class="grid gap-4 xl:grid-cols-2 2xl:grid-cols-3"
            >
              <article
                v-for="schedule in ownedSchedules"
                :key="schedule.id"
                class="rounded-[24px] border border-ink/8 bg-white/72 p-5"
              >
                <div class="flex items-start justify-between gap-4">
                  <div class="flex gap-3">
                    <span
                      class="mt-1 h-3.5 w-3.5 rounded-full"
                      :style="{ backgroundColor: schedule.displayColor ?? schedule.defaultColor }"
                    ></span>
                    <div>
                      <div class="text-lg font-semibold">{{ schedule.name }}</div>
                      <div class="mt-1 text-xs text-muted">
                        {{ schedule.ownerDisplayName }} · {{ schedule.termLabel || '未命名学期' }}
                      </div>
                    </div>
                  </div>
                  <span class="rounded-full bg-ink/5 px-3 py-1 text-xs text-muted">
                    我的课表
                  </span>
                </div>
                <p class="mt-4 text-sm leading-6 text-muted">{{ schedule.description }}</p>
                <div class="mt-4 grid grid-cols-3 gap-3 text-sm">
                  <div class="rounded-2xl bg-white/80 px-3 py-3">
                    <div class="text-xs text-muted">开学时间</div>
                    <div class="mt-1 font-semibold">{{ schedule.startDate }}</div>
                  </div>
                  <div class="rounded-2xl bg-white/80 px-3 py-3">
                    <div class="text-xs text-muted">总周数</div>
                    <div class="mt-1 font-semibold">{{ schedule.totalWeeks }}</div>
                  </div>
                  <div class="rounded-2xl bg-white/80 px-3 py-3">
                    <div class="text-xs text-muted">展示透明度</div>
                    <div class="mt-1 font-semibold">{{ Math.round(schedule.displayOpacity * 100) }}%</div>
                  </div>
                </div>
                <div class="mt-5 flex gap-2">
                  <RouterLink class="btn-ghost flex-1 px-4 py-2 text-xs" :to="{ name: 'schedule-edit', params: { scheduleId: schedule.id } }">
                    打开详情
                  </RouterLink>
                  <RouterLink class="btn-ghost flex-1 px-4 py-2 text-xs" to="/shares">分享管理</RouterLink>
                </div>
              </article>
            </div>

            <div
              v-else
              class="rounded-[22px] border border-dashed border-ink/15 bg-white/55 px-5 py-8 text-sm text-muted"
            >
              你还没有创建自己的课表，可以从上方直接新建或导入。
            </div>
          </section>

          <section class="grid gap-4">
            <div class="flex items-center justify-between gap-3">
              <h4 class="text-lg font-semibold text-ink">共享课表</h4>
              <span class="rounded-full bg-ink/5 px-3 py-1 text-xs text-muted">
                {{ sharedSchedules.length }} 张
              </span>
            </div>

            <div
              v-if="sharedSchedules.length"
              class="grid gap-4 xl:grid-cols-2 2xl:grid-cols-3"
            >
              <article
                v-for="schedule in sharedSchedules"
                :key="schedule.id"
                class="rounded-[24px] border border-ink/8 bg-white/72 p-5"
              >
                <div class="flex items-start justify-between gap-4">
                  <div class="flex gap-3">
                    <span
                      class="mt-1 h-3.5 w-3.5 rounded-full"
                      :style="{ backgroundColor: schedule.displayColor ?? schedule.defaultColor }"
                    ></span>
                    <div>
                      <div class="text-lg font-semibold">{{ schedule.name }}</div>
                      <div class="mt-1 text-xs text-muted">
                        {{ schedule.ownerDisplayName }} · {{ schedule.termLabel || '未命名学期' }}
                      </div>
                    </div>
                  </div>
                  <span class="rounded-full bg-ink/5 px-3 py-1 text-xs text-muted">
                    共享课表
                  </span>
                </div>
                <p class="mt-4 text-sm leading-6 text-muted">{{ schedule.description }}</p>
                <div class="mt-4 grid grid-cols-3 gap-3 text-sm">
                  <div class="rounded-2xl bg-white/80 px-3 py-3">
                    <div class="text-xs text-muted">开学时间</div>
                    <div class="mt-1 font-semibold">{{ schedule.startDate }}</div>
                  </div>
                  <div class="rounded-2xl bg-white/80 px-3 py-3">
                    <div class="text-xs text-muted">总周数</div>
                    <div class="mt-1 font-semibold">{{ schedule.totalWeeks }}</div>
                  </div>
                  <div class="rounded-2xl bg-white/80 px-3 py-3">
                    <div class="text-xs text-muted">展示透明度</div>
                    <div class="mt-1 font-semibold">{{ Math.round(schedule.displayOpacity * 100) }}%</div>
                  </div>
                </div>
                <div class="mt-5 flex gap-2">
                  <RouterLink class="btn-ghost flex-1 px-4 py-2 text-xs" :to="{ name: 'schedule-edit', params: { scheduleId: schedule.id } }">
                    打开详情
                  </RouterLink>
                  <button class="btn-ghost flex-1 px-4 py-2 text-xs" type="button" @click="requestRemoveSharedSchedule(schedule)">
                    移出列表
                  </button>
                </div>
              </article>
            </div>

            <div
              v-else
              class="rounded-[22px] border border-dashed border-ink/15 bg-white/55 px-5 py-8 text-sm text-muted"
            >
              你还没有加入共享课表，可以在分享管理里输入分享口令。
            </div>
          </section>
        </div>

      </section>

      <ConfirmDialog
        v-if="sharedSchedulePendingDelete"
        :description="`移除后，课表「${sharedSchedulePendingDelete.name}」会从你的列表中消失，但不会影响原拥有者的课表内容。`"
        :pending="deletePending"
        confirm-text="确认移除"
        title="确认从我的列表中移除这张共享课表？"
        @close="closeRemoveSharedScheduleDialog"
        @confirm="confirmRemoveSharedSchedule"
      />
    </div>
  </AppShell>
</template>
