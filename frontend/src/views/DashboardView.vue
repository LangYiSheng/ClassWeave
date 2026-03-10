<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'

import AppShell from '@/components/AppShell.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import DialogModal from '@/components/DialogModal.vue'
import MetricCard from '@/components/MetricCard.vue'
import PageHero from '@/components/PageHero.vue'
import { useAuthStore } from '@/stores/auth'
import { useScheduleStore } from '@/stores/schedules'
import { parseWakeUpSchedule } from '@/utils/wakeupImport'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const scheduleStore = useScheduleStore()

const ownedSchedules = computed(() =>
  scheduleStore.summaries.filter((schedule) => schedule.accessRole === 'OWNER'),
)

const sharedSchedules = computed(() =>
  scheduleStore.summaries.filter((schedule) => schedule.accessRole !== 'OWNER'),
)

const showCreateModal = ref(false)
const createPending = ref(false)
const createError = ref('')
const createForm = ref({
  name: '新课表',
  termLabel: '2026 春',
  description: '新建一张课程表',
  startDate: '2026-02-24',
  totalWeeks: 18,
  maxPeriodsPerDay: 12,
  defaultColor: '#1F6FEB',
})
const showImportModal = ref(false)
const importMode = ref('json')
const importPending = ref(false)
const importError = ref('')
const wakeUpDragging = ref(false)
const wakeUpFile = ref(null)
const wakeUpParsing = ref(false)
const wakeUpError = ref('')
const wakeUpParsed = ref(null)
const importText = ref(`{
  "schedule": {
    "name": "AI 导入课表",
    "termLabel": "2026 春",
    "description": "从结构化 JSON 导入",
    "startDate": "2026-02-24",
    "totalWeeks": 18,
    "maxPeriodsPerDay": 12,
    "defaultColor": "#1F6FEB"
  },
  "timeSlots": [
    { "periodIndex": 1, "startTime": "08:00:00", "endTime": "08:45:00" },
    { "periodIndex": 2, "startTime": "08:55:00", "endTime": "09:40:00" }
  ],
  "courses": [
    {
      "name": "软件工程",
      "weekday": 3,
      "startWeek": 1,
      "endWeek": 16,
      "startPeriod": 3,
      "endPeriod": 4,
      "weekType": "ALL",
      "teacher": "陈老师",
      "location": "A-302",
      "note": "",
      "isTemporary": false
    }
  ]
}`)
const sharedSchedulePendingDelete = ref(null)
const deletePending = ref(false)

onMounted(async () => {
  await scheduleStore.hydrateForUser(authStore.user.id)

  if (route.query.action === 'create') {
    openCreateModal()
    router.replace({ name: 'schedules' })
  }
})

function resetCreateForm() {
  createForm.value = {
    name: '新课表',
    termLabel: '2026 春',
    description: '新建一张课程表',
    startDate: '2026-02-24',
    totalWeeks: 18,
    maxPeriodsPerDay: 12,
    defaultColor: '#1F6FEB',
  }
}

function createRandomColor() {
  return `#${Math.floor(Math.random() * 0xffffff)
    .toString(16)
    .padStart(6, '0')
    .toUpperCase()}`
}

function openCreateModal() {
  resetCreateForm()
  createError.value = ''
  showCreateModal.value = true
}

function closeCreateModal() {
  showCreateModal.value = false
  createPending.value = false
  createError.value = ''
  resetCreateForm()
}

function applyRandomCreateColor() {
  createForm.value.defaultColor = createRandomColor()
}

async function submitCreateSchedule() {
  createError.value = ''
  createPending.value = true

  try {
    const resultScheduleId = await scheduleStore.saveSchedule('new', createForm.value, authStore.user.id)
    closeCreateModal()
    router.push({ name: 'schedule-edit', params: { scheduleId: resultScheduleId } })
  } catch (error) {
    createError.value = error.message || '创建课表失败，请稍后再试。'
  } finally {
    createPending.value = false
  }
}

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

function openImportModal() {
  importMode.value = 'json'
  importError.value = ''
  wakeUpError.value = ''
  showImportModal.value = true
}

function closeImportModal() {
  showImportModal.value = false
  importPending.value = false
  importError.value = ''
  wakeUpDragging.value = false
  wakeUpParsing.value = false
  wakeUpError.value = ''
  wakeUpFile.value = null
  wakeUpParsed.value = null
}

function formatFileSize(bytes = 0) {
  if (!bytes) {
    return '0 B'
  }

  if (bytes < 1024) {
    return `${bytes} B`
  }

  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }

  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
}

function formatFileTime(value) {
  if (!value) {
    return '未知'
  }

  return new Date(value).toLocaleString('zh-CN', {
    hour12: false,
  })
}

async function updateWakeUpFile(file) {
  if (!file) {
    return
  }

  wakeUpFile.value = file
  wakeUpDragging.value = false
  wakeUpParsing.value = true
  wakeUpError.value = ''
  wakeUpParsed.value = null

  try {
    const content = await file.text()
    wakeUpParsed.value = parseWakeUpSchedule(content)
  } catch (error) {
    wakeUpError.value = error.message || '无法解析这个 WakeUp 备份文件。'
  } finally {
    wakeUpParsing.value = false
  }
}

function handleWakeUpFileChange(event) {
  void updateWakeUpFile(event.target.files?.[0] || null)
  event.target.value = ''
}

function handleWakeUpDrop(event) {
  event.preventDefault()
  void updateWakeUpFile(event.dataTransfer?.files?.[0] || null)
}

const wakeUpPreview = computed(() => {
  if (!wakeUpFile.value) {
    return null
  }

  return {
    fileName: wakeUpFile.value.name,
    fileSize: formatFileSize(wakeUpFile.value.size),
    fileType: wakeUpFile.value.type || '未知类型',
    updatedAt: formatFileTime(wakeUpFile.value.lastModified),
  }
})

async function importWakeUpFile() {
  if (!wakeUpParsed.value) {
    wakeUpError.value = '请先选择并解析一个有效的 WakeUp 备份文件。'
    return
  }

  wakeUpError.value = ''
  importPending.value = true

  try {
    const resultScheduleId = await scheduleStore.importFromJson(wakeUpParsed.value.payload, authStore.user.id)
    closeImportModal()
    router.push({ name: 'schedule-edit', params: { scheduleId: resultScheduleId } })
  } catch (error) {
    wakeUpError.value = `导入失败：${error.message}`
  } finally {
    importPending.value = false
  }
}

async function importJson() {
  importError.value = ''
  importPending.value = true

  try {
    const parsed = JSON.parse(importText.value)
    const resultScheduleId = await scheduleStore.importFromJson(parsed, authStore.user.id)
    closeImportModal()
    router.push({ name: 'schedule-edit', params: { scheduleId: resultScheduleId } })
  } catch (error) {
    importError.value = `导入失败：${error.message}`
  } finally {
    importPending.value = false
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
          <button class="btn-primary" type="button" @click="openCreateModal">
            创建新课表
          </button>
          <button class="btn-ghost" type="button" @click="openImportModal">
            导入课程表
          </button>
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

      <DialogModal
        v-if="showCreateModal"
        title="创建新课表"
        description="先设置课表名称、学期信息和默认颜色，保存后再进入详情页继续编辑。"
        @close="closeCreateModal"
      >
        <div class="grid gap-4 md:grid-cols-2">
          <label class="field-label">
            课表名称
            <input v-model="createForm.name" class="field-input" type="text">
          </label>
          <label class="field-label">
            学期标签
            <input v-model="createForm.termLabel" class="field-input" type="text">
          </label>
          <label class="field-label md:col-span-2">
            描述
            <textarea v-model="createForm.description" class="field-input min-h-[110px]"></textarea>
          </label>
          <label class="field-label">
            开学第一天
            <input v-model="createForm.startDate" class="field-input" type="date">
          </label>
          <label class="field-label">
            总周数
            <input v-model="createForm.totalWeeks" class="field-input" type="number" min="1" max="30">
          </label>
          <label class="field-label">
            每天最大节次
            <input v-model="createForm.maxPeriodsPerDay" class="field-input" type="number" min="1" max="20">
          </label>
          <label class="field-label">
            默认颜色
            <div class="relative">
              <span
                class="pointer-events-none absolute left-4 top-1/2 h-3.5 w-3.5 -translate-y-1/2 rounded-full border border-ink/10"
                :style="{ backgroundColor: createForm.defaultColor || '#1F6FEB' }"
              ></span>
              <input
                v-model="createForm.defaultColor"
                class="field-input pl-10 pr-[120px]"
                type="text"
              >
              <div class="absolute inset-y-0 right-2 flex items-center gap-2">
                <button
                  class="rounded-full bg-ink/5 px-3 py-1.5 text-[11px] font-semibold text-ink transition hover:bg-ink/10"
                  type="button"
                  @click="applyRandomCreateColor"
                >
                  随机
                </button>
                <label class="relative inline-flex cursor-pointer items-center justify-center rounded-full bg-ink/5 px-3 py-1.5 text-[11px] font-semibold text-ink transition hover:bg-ink/10">
                  选色
                  <input
                    v-model="createForm.defaultColor"
                    class="absolute inset-0 cursor-pointer opacity-0"
                    type="color"
                  >
                </label>
              </div>
            </div>
          </label>
        </div>

        <p v-if="createError" class="mt-4 rounded-[20px] bg-coral/10 px-4 py-3 text-sm text-coral">
          {{ createError }}
        </p>

        <div class="mt-5 flex gap-3">
          <button class="btn-primary" :disabled="createPending" type="button" @click="submitCreateSchedule">
            {{ createPending ? '创建中...' : '保存并继续编辑' }}
          </button>
          <button class="btn-ghost" :disabled="createPending" type="button" @click="closeCreateModal">取消</button>
        </div>
      </DialogModal>

      <DialogModal
        v-if="showImportModal"
        title="导入课程表"
        description="你可以直接粘贴标准 JSON，也可以选择 WakeUp 课程表备份文件。"
        :show-close-button="false"
        @close="closeImportModal"
      >
        <div class="mb-5 flex flex-wrap gap-2 rounded-[22px] bg-ink/5 p-1">
          <button
            class="rounded-full px-4 py-2 text-sm font-semibold transition"
            :class="importMode === 'json' ? 'bg-white text-ink shadow-sm' : 'text-muted'"
            type="button"
            @click="importMode = 'json'"
          >
            通过 JSON 导入
          </button>
          <button
            class="rounded-full px-4 py-2 text-sm font-semibold transition"
            :class="importMode === 'wakeup' ? 'bg-white text-ink shadow-sm' : 'text-muted'"
            type="button"
            @click="importMode = 'wakeup'"
          >
            通过 WakeUp 文件导入
          </button>
        </div>

        <div v-if="importMode === 'json'">
          <textarea v-model="importText" class="field-input min-h-[460px] font-mono text-xs"></textarea>
          <p v-if="importError" class="mt-4 rounded-[20px] bg-coral/10 px-4 py-3 text-sm text-coral">
            {{ importError }}
          </p>
          <div class="mt-5 flex gap-3">
            <button class="btn-primary" :disabled="importPending" type="button" @click="importJson">
              {{ importPending ? '导入中...' : '开始导入' }}
            </button>
            <button class="btn-ghost" :disabled="importPending" type="button" @click="closeImportModal">取消</button>
          </div>
        </div>

        <div v-else>
          <div class="mb-4 rounded-[22px] border border-ink/10 bg-white/72 px-5 py-4">
            <div class="text-sm font-semibold text-ink">文件导出方法</div>
            <p class="mt-2 text-sm leading-6 text-muted">
              在 WakeUp 课程表 App 中点击顶部分享按钮，选择导出为备份，然后保存文件，再在此处上传文件。
            </p>
          </div>

          <div
            class="rounded-[24px] border-2 border-dashed px-6 py-10 text-center transition"
            :class="wakeUpDragging ? 'border-blue bg-blue/8' : 'border-ink/12 bg-white/68'"
            @dragenter.prevent="wakeUpDragging = true"
            @dragover.prevent="wakeUpDragging = true"
            @dragleave.prevent="wakeUpDragging = false"
            @drop="handleWakeUpDrop"
          >
            <p class="font-heading text-2xl tracking-[-0.04em] text-ink">拖入 WakeUp 备份文件</p>
            <p class="mt-3 text-sm leading-6 text-muted">
              支持直接拖入文件，也可以点击下面的按钮手动选择。
            </p>
            <label class="btn-primary mt-5 cursor-pointer">
              {{ wakeUpParsing ? '解析中...' : '选择备份文件' }}
              <input class="hidden" type="file" @change="handleWakeUpFileChange">
            </label>
          </div>

          <p v-if="wakeUpError" class="mt-4 rounded-[20px] bg-coral/10 px-4 py-3 text-sm text-coral">
            {{ wakeUpError }}
          </p>

          <div class="mt-5 grid gap-4 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
            <section class="rounded-[22px] border border-ink/10 bg-white/72 p-5">
              <div class="text-sm font-semibold text-ink">已接收的文件</div>
              <div v-if="wakeUpPreview" class="mt-4 grid gap-3 text-sm">
                <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                  <div class="text-xs text-muted">文件名</div>
                  <div class="mt-1 break-all font-semibold">{{ wakeUpPreview.fileName }}</div>
                </div>
                <div class="grid gap-3 sm:grid-cols-2">
                  <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                    <div class="text-xs text-muted">文件大小</div>
                    <div class="mt-1 font-semibold">{{ wakeUpPreview.fileSize }}</div>
                  </div>
                  <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                    <div class="text-xs text-muted">文件类型</div>
                    <div class="mt-1 font-semibold">{{ wakeUpPreview.fileType }}</div>
                  </div>
                </div>
                <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                  <div class="text-xs text-muted">最后修改</div>
                  <div class="mt-1 font-semibold">{{ wakeUpPreview.updatedAt }}</div>
                </div>
              </div>
              <p v-else class="mt-4 text-sm leading-6 text-muted">
                还没有选择文件。选好后，这里会显示文件的基础信息。
              </p>
            </section>

            <section class="rounded-[22px] border border-ink/10 bg-white/72 p-5">
              <div class="text-sm font-semibold text-ink">课程表信息预览</div>
              <div v-if="wakeUpParsed" class="mt-4 grid gap-3 text-sm">
                <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                  <div class="text-xs text-muted">课程表名称</div>
                  <div class="mt-1 font-semibold">{{ wakeUpParsed.preview.name }}</div>
                </div>
                <div class="grid gap-3 sm:grid-cols-2">
                  <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                    <div class="text-xs text-muted">学期标签</div>
                    <div class="mt-1 font-semibold">{{ wakeUpParsed.preview.termLabel }}</div>
                  </div>
                  <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                    <div class="text-xs text-muted">课程数量</div>
                    <div class="mt-1 font-semibold">{{ wakeUpParsed.preview.courseCount }}</div>
                  </div>
                </div>
                <div class="grid gap-3 sm:grid-cols-2">
                  <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                    <div class="text-xs text-muted">学校</div>
                    <div class="mt-1 font-semibold">{{ wakeUpParsed.preview.school }}</div>
                  </div>
                  <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                    <div class="text-xs text-muted">开学日期</div>
                    <div class="mt-1 font-semibold">{{ wakeUpParsed.preview.startDate }}</div>
                  </div>
                </div>
                <div class="grid gap-3 sm:grid-cols-2">
                  <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                    <div class="text-xs text-muted">总周数</div>
                    <div class="mt-1 font-semibold">{{ wakeUpParsed.preview.totalWeeks }}</div>
                  </div>
                  <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                    <div class="text-xs text-muted">节次数量</div>
                    <div class="mt-1 font-semibold">{{ wakeUpParsed.preview.timeSlotCount }}</div>
                  </div>
                </div>
                <div class="rounded-2xl bg-[#fffaf2] px-4 py-3">
                  <div class="text-xs text-muted">当前状态</div>
                  <div class="mt-1 font-semibold">已完成格式转换，可以直接导入。</div>
                </div>
              </div>
              <p v-else class="mt-4 text-sm leading-6 text-muted">
                选中文件后，这里会展示从 WakeUp 备份里提取出的课程表信息。
              </p>
            </section>
          </div>

          <div class="mt-5 flex gap-3">
            <button class="btn-primary" :disabled="importPending || wakeUpParsing || !wakeUpParsed" type="button" @click="importWakeUpFile">
              {{ importPending ? '导入中...' : '导入这张课程表' }}
            </button>
            <button class="btn-ghost" type="button" @click="closeImportModal">关闭</button>
          </div>
        </div>
      </DialogModal>
    </div>
  </AppShell>
</template>
