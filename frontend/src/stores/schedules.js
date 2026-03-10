import { computed, reactive, ref } from 'vue'
import { defineStore } from 'pinia'

import { APP_CONFIG } from '@/config/app'
import {
  deleteCourseById,
  fetchScheduleDetail,
  fetchSchedules,
  importScheduleJson,
  removeScheduleAccess,
  removeScheduleById,
  saveScheduleBasics,
  saveScheduleDisplaySettings,
  saveTimeSlots,
  upsertCourse,
} from '@/services/scheduleService'
import { generateShareLink, fetchShareLinks, deactivateShareLink, acceptShare } from '@/services/shareService'
import { fetchStatusPanel } from '@/services/statusService'
import { getBeijingNow, toMinuteValue } from '@/utils/time'

export const useScheduleStore = defineStore('schedules', () => {
  const summaries = ref([])
  const detailsById = reactive({})
  const shareLinks = ref([])
  const loading = ref(false)
  const statusPanel = ref(null)

  const now = getBeijingNow()
  const analysisDate = ref(now.date)
  const analysisTime = ref(now.time)
  const analysisWindowMinutes = ref(APP_CONFIG.defaultAnalysisWindowMinutes)
  const boardStartTime = ref('06:00')
  const boardEndTime = ref('22:00')
  const selectedScheduleIds = ref([])

  const activeSummaries = computed(() =>
    summaries.value.filter((item) => selectedScheduleIds.value.includes(item.id)),
  )

  const activeDetails = computed(() =>
    selectedScheduleIds.value.map((id) => detailsById[id]).filter(Boolean),
  )

  const dashboardMetrics = computed(() => {
    const owned = summaries.value.filter((item) => item.accessRole === 'OWNER').length
    const shared = summaries.value.filter((item) => item.accessRole !== 'OWNER').length

    return [
      {
        label: '可访问课表',
        value: String(summaries.value.length),
        description: '包含自己与通过分享加入的课表',
      },
      {
        label: '我创建的课表',
        value: String(owned),
        description: '可编辑、可生成分享链接',
      },
      {
        label: '共享课表',
        value: String(shared),
        description: '作为只读叠加源使用',
      },
    ]
  })

  async function hydrateForUser(userId) {
    loading.value = true
    summaries.value = await fetchSchedules()

    if (!selectedScheduleIds.value.length) {
      selectedScheduleIds.value = summaries.value
        .filter((item) => item.isVisibleDefault)
        .map((item) => item.id)
    }

    await Promise.all(selectedScheduleIds.value.map((id) => ensureDetailLoaded(id, userId)))
    statusPanel.value = await fetchStatusPanel({
      scheduleIds: selectedScheduleIds.value,
      date: analysisDate.value,
      time: analysisTime.value,
      windowMinutes: analysisWindowMinutes.value,
    })
    shareLinks.value = await fetchShareLinks(
      summaries.value.filter((item) => item.accessRole === 'OWNER'),
    )
    loading.value = false
  }

  async function ensureDetailLoaded(scheduleId, userId) {
    if (detailsById[scheduleId]) {
      return detailsById[scheduleId]
    }

    const detail = await fetchScheduleDetail(scheduleId)
    detailsById[scheduleId] = detail
    return detail
  }

  async function refreshStatus(userId) {
    statusPanel.value = await fetchStatusPanel({
      scheduleIds: selectedScheduleIds.value,
      date: analysisDate.value,
      time: analysisTime.value,
      windowMinutes: analysisWindowMinutes.value,
    })
  }

  async function toggleScheduleSelection(scheduleId, userId) {
    const shouldSelect = !selectedScheduleIds.value.includes(scheduleId)

    if (shouldSelect) {
      await ensureDetailLoaded(scheduleId, userId)
    }

    await saveDisplaySettings(scheduleId, { isVisibleDefault: shouldSelect }, userId, false)
    selectedScheduleIds.value = shouldSelect
      ? [...selectedScheduleIds.value, scheduleId]
      : selectedScheduleIds.value.filter((id) => id !== scheduleId)
    await refreshStatus(userId)
  }

  function buildDisplaySettingsPayload(scheduleId, payload = {}) {
    const summary = summaries.value.find((item) => item.id === scheduleId)
    const detailSchedule = detailsById[scheduleId]?.schedule

    return {
      displayColor:
        payload.displayColor ??
        summary?.displayColor ??
        detailSchedule?.displayColor ??
        summary?.defaultColor ??
        detailSchedule?.defaultColor ??
        '#1F6FEB',
      displayOpacity: Number(
        payload.displayOpacity ??
          summary?.displayOpacity ??
          detailSchedule?.displayOpacity ??
          0.85,
      ),
      isVisibleDefault: Boolean(
        payload.isVisibleDefault ??
          summary?.isVisibleDefault ??
          detailSchedule?.isVisibleDefault ??
          true,
      ),
    }
  }

  function applyDisplaySettingsLocally(scheduleId, payload) {
    const summary = summaries.value.find((item) => item.id === scheduleId)

    if (summary) {
      if (payload.displayColor) {
        summary.displayColor = payload.displayColor
      }

      if (payload.displayOpacity !== undefined) {
        summary.displayOpacity = Number(payload.displayOpacity)
      }

      if (payload.isVisibleDefault !== undefined) {
        summary.isVisibleDefault = Boolean(payload.isVisibleDefault)
      }
    }

    if (detailsById[scheduleId]?.schedule) {
      Object.assign(detailsById[scheduleId].schedule, payload)
    }
  }

  async function saveDisplaySettings(scheduleId, payload, userId, shouldRefreshStatus = false) {
    const fullPayload = buildDisplaySettingsPayload(scheduleId, payload)

    await saveScheduleDisplaySettings(scheduleId, fullPayload)
    applyDisplaySettingsLocally(scheduleId, fullPayload)

    if (shouldRefreshStatus) {
      await refreshStatus(userId)
    }
  }

  async function updateAnalysis(payload, userId) {
    let shouldRefreshStatus = false
    let nextBoardStartTime = boardStartTime.value
    let nextBoardEndTime = boardEndTime.value

    if (payload.date) {
      analysisDate.value = payload.date
      shouldRefreshStatus = true
    }

    if (payload.time) {
      analysisTime.value = payload.time.length === 5 ? `${payload.time}:00` : payload.time
      shouldRefreshStatus = true
    }

    if (payload.windowMinutes) {
      analysisWindowMinutes.value = Number(payload.windowMinutes)
      shouldRefreshStatus = true
    }

    if (payload.boardStartTime) {
      nextBoardStartTime = payload.boardStartTime
    }

    if (payload.boardEndTime) {
      nextBoardEndTime = payload.boardEndTime
    }

    if (toMinuteValue(`${nextBoardEndTime}:00`) > toMinuteValue(`${nextBoardStartTime}:00`)) {
      boardStartTime.value = nextBoardStartTime
      boardEndTime.value = nextBoardEndTime
    }

    if (shouldRefreshStatus) {
      await refreshStatus(userId)
    }
  }

  async function saveSchedule(scheduleId, payload, userId) {
    const result = await saveScheduleBasics(scheduleId, {
      name: payload.name,
      termLabel: payload.termLabel,
      description: payload.description,
      startDate: payload.startDate,
      totalWeeks: Number(payload.totalWeeks),
      maxPeriodsPerDay: Number(payload.maxPeriodsPerDay),
      defaultColor: payload.defaultColor,
    })

    if (scheduleId === 'new') {
      await hydrateForUser(userId)
      return result.id
    }

    detailsById[scheduleId] = result
    summaries.value = await fetchSchedules()
    return scheduleId
  }

  async function saveScheduleTimeSlots(scheduleId, timeSlots, userId) {
    const result = await saveTimeSlots(scheduleId, timeSlots)
    if (detailsById[scheduleId]) {
      detailsById[scheduleId].timeSlots = result
    }
    await refreshStatus(userId)
  }

  async function saveScheduleCourse(scheduleId, payload, userId) {
    const result = await upsertCourse(scheduleId, payload)
    detailsById[scheduleId] = await fetchScheduleDetail(scheduleId)
    await refreshStatus(userId)
    return result
  }

  async function deleteScheduleCourse(scheduleId, courseId, userId) {
    await deleteCourseById(scheduleId, courseId)
    detailsById[scheduleId] = await fetchScheduleDetail(scheduleId)
    await refreshStatus(userId)
  }

  async function importFromJson(payload, userId) {
    const result = await importScheduleJson(payload)
    await hydrateForUser(userId)
    return result.scheduleId
  }

  async function deleteSchedule(scheduleId, userId) {
    await removeScheduleById(scheduleId)
    delete detailsById[scheduleId]
    selectedScheduleIds.value = selectedScheduleIds.value.filter((id) => id !== Number(scheduleId))
    await hydrateForUser(userId)
  }

  async function removeSharedSchedule(scheduleId, userId) {
    await removeScheduleAccess(scheduleId)
    delete detailsById[scheduleId]
    selectedScheduleIds.value = selectedScheduleIds.value.filter((id) => id !== Number(scheduleId))
    await hydrateForUser(userId)
  }

  async function createShare(scheduleId, userId, expiresAt = null) {
    await generateShareLink(scheduleId, expiresAt)
    shareLinks.value = await fetchShareLinks(
      summaries.value.filter((item) => item.accessRole === 'OWNER'),
    )
  }

  async function disableShare(shareLinkId, userId) {
    await deactivateShareLink(shareLinkId)
    shareLinks.value = await fetchShareLinks(
      summaries.value.filter((item) => item.accessRole === 'OWNER'),
    )
  }

  async function acceptShareLink(shareToken, userId) {
    await acceptShare(shareToken)
    await hydrateForUser(userId)
  }

  return {
    summaries,
    detailsById,
    shareLinks,
    loading,
    analysisDate,
    analysisTime,
    analysisWindowMinutes,
    boardStartTime,
    boardEndTime,
    selectedScheduleIds,
    activeSummaries,
    activeDetails,
    statusPanel,
    dashboardMetrics,
    hydrateForUser,
    ensureDetailLoaded,
    toggleScheduleSelection,
    updateAnalysis,
    applyDisplaySettingsLocally,
    saveDisplaySettings,
    saveSchedule,
    saveScheduleTimeSlots,
    saveScheduleCourse,
    deleteScheduleCourse,
    deleteSchedule,
    removeSharedSchedule,
    importFromJson,
    createShare,
    disableShare,
    acceptShareLink,
  }
})
