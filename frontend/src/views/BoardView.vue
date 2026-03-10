<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

import AppShell from '@/components/AppShell.vue'
import DialogModal from '@/components/DialogModal.vue'
import OverlayTimetable from '@/components/OverlayTimetable.vue'
import PageHero from '@/components/PageHero.vue'
import ScheduleLibraryList from '@/components/ScheduleLibraryList.vue'
import StatusPanel from '@/components/StatusPanel.vue'
import { useAuthStore } from '@/stores/auth'
import { useScheduleStore } from '@/stores/schedules'

const authStore = useAuthStore()
const scheduleStore = useScheduleStore()
const showBoardSettings = ref(false)
const displaySettingTimers = new Map()
const pendingDisplaySettings = new Map()

onMounted(async () => {
  await scheduleStore.hydrateForUser(authStore.user.id)
})

async function toggleSchedule(scheduleId) {
  await scheduleStore.toggleScheduleSelection(scheduleId, authStore.user.id)
}

function queueDisplaySettingsSave(scheduleId, payload) {
  const mergedPayload = {
    ...(pendingDisplaySettings.get(scheduleId) || {}),
    ...payload,
  }

  pendingDisplaySettings.set(scheduleId, mergedPayload)
  scheduleStore.applyDisplaySettingsLocally(scheduleId, payload)

  const existingTimer = displaySettingTimers.get(scheduleId)
  if (existingTimer) {
    window.clearTimeout(existingTimer)
  }

  const nextTimer = window.setTimeout(async () => {
    const latestPayload = pendingDisplaySettings.get(scheduleId)

    pendingDisplaySettings.delete(scheduleId)
    displaySettingTimers.delete(scheduleId)

    if (!latestPayload) {
      return
    }

    try {
      await scheduleStore.saveDisplaySettings(scheduleId, latestPayload, authStore.user.id)
    } catch {
      // Keep the UI from exploding on transient save failures.
    }
  }, 320)

  displaySettingTimers.set(scheduleId, nextTimer)
}

function updateOpacity({ scheduleId, opacity }) {
  queueDisplaySettingsSave(scheduleId, { displayOpacity: opacity })
}

function updateColor({ scheduleId, displayColor }) {
  queueDisplaySettingsSave(scheduleId, { displayColor })
}

async function updateAnalysis(field, event) {
  await scheduleStore.updateAnalysis({ [field]: event.target.value }, authStore.user.id)
}

onBeforeUnmount(() => {
  displaySettingTimers.forEach((timerId) => {
    window.clearTimeout(timerId)
  })
  displaySettingTimers.clear()
  pendingDisplaySettings.clear()
})
</script>

<template>
  <AppShell>
    <div class="grid gap-6">
      <PageHero
        eyebrow="多人叠加视图"
        title="叠加查看多人安排"
        description="选择要显示的课表，调整时间和预警范围，快速看出谁在上课、谁现在空闲，以及接下来谁快有课。"
      />

      <section class="grid gap-6 2xl:grid-cols-[360px_minmax(0,1fr)]">
        <ScheduleLibraryList
          :schedules="scheduleStore.summaries"
          :selected-schedule-ids="scheduleStore.selectedScheduleIds"
          @color-change="updateColor"
          @opacity-change="updateOpacity"
          @toggle="toggleSchedule"
        />

        <div class="grid gap-6">
          <StatusPanel :panel="scheduleStore.statusPanel">
            <template #action>
              <button class="btn-ghost px-4 py-2 text-xs" type="button" @click="showBoardSettings = true">
                查看条件
              </button>
            </template>
          </StatusPanel>

          <OverlayTimetable
            :current-date="scheduleStore.analysisDate"
            :current-time="scheduleStore.analysisTime"
            :details-list="scheduleStore.activeDetails"
            :start-time="scheduleStore.boardStartTime"
            :end-time="scheduleStore.boardEndTime"
          />
        </div>
      </section>

      <DialogModal
        v-if="showBoardSettings"
        title="查看条件"
        description="调整当前分析时间、预警范围，以及周视图里显示的时间区间。"
        @close="showBoardSettings = false"
      >
        <div class="grid gap-4 md:grid-cols-2">
          <label class="field-label">
            分析日期
            <input
              :value="scheduleStore.analysisDate"
              class="field-input"
              type="date"
              @change="updateAnalysis('date', $event)"
            >
          </label>
          <label class="field-label">
            分析时间
            <input
              :value="scheduleStore.analysisTime.slice(0, 5)"
              class="field-input"
              type="time"
              @change="updateAnalysis('time', $event)"
            >
          </label>
          <label class="field-label">
            预警窗口
            <select
              :value="scheduleStore.analysisWindowMinutes"
              class="field-input"
              @change="updateAnalysis('windowMinutes', $event)"
            >
              <option :value="15">15 分钟</option>
              <option :value="30">30 分钟</option>
              <option :value="45">45 分钟</option>
              <option :value="60">60 分钟</option>
              <option :value="90">90 分钟</option>
              <option :value="120">120 分钟</option>
            </select>
          </label>
          <label class="field-label">
            周视图起始时间
            <input
              :value="scheduleStore.boardStartTime"
              class="field-input"
              type="time"
              @change="updateAnalysis('boardStartTime', $event)"
            >
          </label>
          <label class="field-label">
            周视图结束时间
            <input
              :value="scheduleStore.boardEndTime"
              class="field-input"
              type="time"
              @change="updateAnalysis('boardEndTime', $event)"
            >
          </label>
        </div>
      </DialogModal>
    </div>
  </AppShell>
</template>
