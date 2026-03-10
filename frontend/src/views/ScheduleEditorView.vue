<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import AppShell from '@/components/AppShell.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'
import DialogModal from '@/components/DialogModal.vue'
import PageHero from '@/components/PageHero.vue'
import { useAuthStore } from '@/stores/auth'
import { useScheduleStore } from '@/stores/schedules'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const scheduleStore = useScheduleStore()

const scheduleId = computed(() => route.params.scheduleId)
const isNew = computed(() => scheduleId.value === 'new')

const form = ref({
  name: '新课表',
  termLabel: '2026 春',
  description: '新建一张课程表',
  startDate: '2026-02-24',
  totalWeeks: 18,
  maxPeriodsPerDay: 12,
  defaultColor: '#1F6FEB',
})

const timeSlots = ref([
  { periodIndex: 1, startTime: '08:00:00', endTime: '08:45:00' },
  { periodIndex: 2, startTime: '08:55:00', endTime: '09:40:00' },
  { periodIndex: 3, startTime: '10:00:00', endTime: '10:45:00' },
  { periodIndex: 4, startTime: '10:55:00', endTime: '11:40:00' },
])

const courseForm = ref({
  id: null,
  name: '',
  weekday: 1,
  startWeek: 1,
  endWeek: 16,
  startPeriod: 1,
  endPeriod: 2,
  weekType: 'ALL',
  teacher: '',
  location: '',
  note: '',
  isTemporary: false,
})

const detail = computed(() => scheduleStore.detailsById[scheduleId.value])
const courses = computed(() => detail.value?.courses ?? [])
const canManage = computed(() => isNew.value || detail.value?.schedule.accessRole === 'OWNER')

const showBasicsModal = ref(false)
const showTimeSlotsModal = ref(false)
const showCourseModal = ref(false)
const confirmDeleteScheduleOpen = ref(false)
const confirmDeleteCourseOpen = ref(false)
const coursePendingDelete = ref(null)
const deletePending = ref(false)
const flashMessage = ref('')
const errorMessage = ref('')

function normalizeTimeSlots(slots) {
  return slots.map((slot, index) => ({
    ...slot,
    periodIndex: index + 1,
  }))
}

watch(detail, (value) => {
  if (!value) {
    return
  }

  Object.assign(form.value, {
    name: value.schedule.name,
    termLabel: value.schedule.termLabel,
    description: value.schedule.description,
    startDate: value.schedule.startDate,
    totalWeeks: value.schedule.totalWeeks,
    maxPeriodsPerDay: value.schedule.maxPeriodsPerDay,
    defaultColor: value.schedule.defaultColor || '#1F6FEB',
  })
  timeSlots.value = normalizeTimeSlots(value.timeSlots.map((slot) => ({ ...slot })))
}, { immediate: true })

onMounted(async () => {
  if (isNew.value) {
    router.replace({ name: 'schedules', query: { action: 'create' } })
    return
  }

  await scheduleStore.hydrateForUser(authStore.user.id)

  await scheduleStore.ensureDetailLoaded(scheduleId.value, authStore.user.id)
})

function resetCourseForm() {
  courseForm.value = {
    id: null,
    name: '',
    weekday: 1,
    startWeek: 1,
    endWeek: 16,
    startPeriod: 1,
    endPeriod: 2,
    weekType: 'ALL',
    teacher: '',
    location: '',
    note: '',
    isTemporary: false,
  }
}

function createRandomColor() {
  return `#${Math.floor(Math.random() * 0xffffff)
    .toString(16)
    .padStart(6, '0')
    .toUpperCase()}`
}

function applyRandomColor(field) {
  const color = createRandomColor()
  form.value[field] = color
}

async function saveBasics() {
  errorMessage.value = ''

  try {
    const resultId = await scheduleStore.saveSchedule(scheduleId.value, form.value, authStore.user.id)
    flashMessage.value = '课表信息已保存'
    showBasicsModal.value = false

    if (isNew.value) {
      router.replace({ name: 'schedule-edit', params: { scheduleId: resultId } })
    }
  } catch (error) {
    errorMessage.value = error.message
  }
}

async function saveSlots() {
  if (isNew.value) {
    errorMessage.value = '请先创建课表，再设置节次时间。'
    return
  }

  timeSlots.value = normalizeTimeSlots(timeSlots.value)

  await scheduleStore.saveScheduleTimeSlots(scheduleId.value, timeSlots.value, authStore.user.id)
  flashMessage.value = '节次时间已更新'
  showTimeSlotsModal.value = false
}

function addTimeSlot() {
  const lastSlot = timeSlots.value[timeSlots.value.length - 1]

  timeSlots.value = normalizeTimeSlots([
    ...timeSlots.value,
    {
      periodIndex: timeSlots.value.length + 1,
      startTime: lastSlot?.endTime || '08:00:00',
      endTime: lastSlot?.endTime || '08:45:00',
    },
  ])
}

function removeTimeSlot(periodIndex) {
  timeSlots.value = normalizeTimeSlots(
    timeSlots.value.filter((slot) => slot.periodIndex !== periodIndex),
  )
}

async function submitCourse() {
  if (isNew.value) {
    errorMessage.value = '请先创建课表，再添加课程。'
    return
  }

  await scheduleStore.saveScheduleCourse(scheduleId.value, {
    ...courseForm.value,
    weekday: Number(courseForm.value.weekday),
    startWeek: Number(courseForm.value.startWeek),
    endWeek: Number(courseForm.value.endWeek),
    startPeriod: Number(courseForm.value.startPeriod),
    endPeriod: Number(courseForm.value.endPeriod),
  }, authStore.user.id)

  flashMessage.value = courseForm.value.id ? '课程已更新' : '课程已添加'
  showCourseModal.value = false
  resetCourseForm()
}

function openCreateCourseModal() {
  resetCourseForm()
  showCourseModal.value = true
}

function editCourse(course) {
  courseForm.value = { ...course }
  showCourseModal.value = true
}

function closeCourseModal() {
  showCourseModal.value = false
  resetCourseForm()
}

function requestRemoveCourse(course) {
  coursePendingDelete.value = course
  confirmDeleteCourseOpen.value = true
}

function closeDeleteCourseDialog() {
  confirmDeleteCourseOpen.value = false
  coursePendingDelete.value = null
}

async function confirmRemoveCourse() {
  if (!coursePendingDelete.value) {
    return
  }

  deletePending.value = true

  try {
    await scheduleStore.deleteScheduleCourse(scheduleId.value, coursePendingDelete.value.id, authStore.user.id)
    closeDeleteCourseDialog()
    flashMessage.value = '课程已删除'
  } finally {
    deletePending.value = false
  }
}

function requestRemoveSchedule() {
  confirmDeleteScheduleOpen.value = true
}

function closeDeleteScheduleDialog() {
  confirmDeleteScheduleOpen.value = false
}

async function confirmRemoveSchedule() {
  deletePending.value = true

  try {
    await scheduleStore.deleteSchedule(scheduleId.value, authStore.user.id)
    closeDeleteScheduleDialog()
    router.push({ name: 'schedules' })
  } finally {
    deletePending.value = false
  }
}

</script>

<template>
  <AppShell>
    <div class="grid gap-6">
      <PageHero
        eyebrow="课表详情"
        :title="isNew ? '新建一张课表' : form.name"
        :description="isNew ? '先设置课表基本信息，再继续补充节次时间和课程。' : '在这里查看课程列表，并按需修改课表信息、节次设置或课程内容。'"
      />

      <p v-if="flashMessage" class="rounded-[20px] bg-teal/10 px-4 py-3 text-sm text-teal">
        {{ flashMessage }}
      </p>
      <p v-if="errorMessage" class="rounded-[20px] bg-coral/10 px-4 py-3 text-sm text-coral">
        {{ errorMessage }}
      </p>

      <section class="flex flex-wrap gap-3">
        <button v-if="canManage" class="btn-primary" type="button" @click="showBasicsModal = true">
          {{ isNew ? '设置课表信息' : '编辑课表信息' }}
        </button>
        <button v-if="canManage" class="btn-ghost" type="button" @click="showTimeSlotsModal = true">
          设置节次时间
        </button>
        <button v-if="canManage" class="btn-ghost" type="button" @click="openCreateCourseModal">
          添加课程
        </button>
        <button v-if="!isNew && canManage" class="btn-ghost" type="button" @click="requestRemoveSchedule">
          删除课表
        </button>
      </section>

      <section class="grid gap-6">
        <article class="panel-card">
          <div class="mb-5 flex items-start justify-between gap-4">
            <div>
              <p class="section-tag">课程列表</p>
              <h3 class="font-heading text-3xl tracking-[-0.04em]">这张课表里的课程</h3>
            </div>
          </div>

          <div v-if="courses.length" class="grid gap-3">
            <article
              v-for="course in courses"
              :key="course.id"
              class="rounded-[22px] border border-ink/8 bg-white/72 p-4"
            >
              <div class="flex items-start justify-between gap-3">
                <div>
                  <div class="text-base font-semibold">{{ course.name }}</div>
                  <div class="mt-1 text-xs text-muted">
                    周{{ ['一', '二', '三', '四', '五', '六', '日'][course.weekday - 1] }}
                    · {{ course.startWeek }}-{{ course.endWeek }} 周
                    · {{ course.startPeriod }}-{{ course.endPeriod }} 节
                  </div>
                  <div class="mt-2 text-sm text-muted">
                    {{ course.teacher || '未填写教师' }} · {{ course.location || '未填写地点' }}
                  </div>
                </div>
                <div v-if="canManage" class="flex gap-2">
                  <button class="btn-ghost px-4 py-2 text-xs" type="button" @click="editCourse(course)">编辑</button>
                  <button class="btn-ghost px-4 py-2 text-xs" type="button" @click="requestRemoveCourse(course)">删除</button>
                </div>
              </div>
            </article>
          </div>

          <div v-else class="rounded-[22px] border border-dashed border-ink/15 bg-white/55 px-5 py-10 text-center text-sm text-muted">
            {{ isNew ? '先保存课表信息，然后再继续设置节次时间和课程。' : '这张课表还没有课程，可以从上方直接添加第一门。' }}
          </div>
        </article>
      </section>

      <DialogModal
        v-if="showBasicsModal"
        title="课表信息"
        description="设置课表名称、学期信息和这张课表自己的默认颜色。"
        @close="showBasicsModal = false"
      >
        <div class="grid gap-4 md:grid-cols-2">
          <label class="field-label">
            课表名称
            <input v-model="form.name" class="field-input" type="text">
          </label>
          <label class="field-label">
            学期标签
            <input v-model="form.termLabel" class="field-input" type="text">
          </label>
          <label class="field-label md:col-span-2">
            描述
            <textarea v-model="form.description" class="field-input min-h-[110px]"></textarea>
          </label>
          <label class="field-label">
            开学第一天
            <input v-model="form.startDate" class="field-input" type="date">
          </label>
          <label class="field-label">
            总周数
            <input v-model="form.totalWeeks" class="field-input" type="number" min="1" max="30">
          </label>
          <label class="field-label">
            每天最大节次
            <input v-model="form.maxPeriodsPerDay" class="field-input" type="number" min="1" max="20">
          </label>
          <label class="field-label">
            默认颜色
            <div class="relative">
              <span
                class="pointer-events-none absolute left-4 top-1/2 h-3.5 w-3.5 -translate-y-1/2 rounded-full border border-ink/10"
                :style="{ backgroundColor: form.defaultColor || '#1F6FEB' }"
              ></span>
              <input
                v-model="form.defaultColor"
                class="field-input pl-10 pr-[120px]"
                type="text"
              >
              <div class="absolute inset-y-0 right-2 flex items-center gap-2">
                <button
                  class="rounded-full bg-ink/5 px-3 py-1.5 text-[11px] font-semibold text-ink transition hover:bg-ink/10"
                  type="button"
                  @click="applyRandomColor('defaultColor')"
                >
                  随机
                </button>
                <label class="relative inline-flex cursor-pointer items-center justify-center rounded-full bg-ink/5 px-3 py-1.5 text-[11px] font-semibold text-ink transition hover:bg-ink/10">
                  选色
                  <input
                    v-model="form.defaultColor"
                    class="absolute inset-0 cursor-pointer opacity-0"
                    type="color"
                  >
                </label>
              </div>
            </div>
            <span class="text-xs text-muted">这张课表被分享或首次加入时，会优先使用这个默认颜色。</span>
          </label>
        </div>
        <div class="mt-5 flex gap-3">
          <button class="btn-primary" type="button" @click="saveBasics">保存</button>
          <button class="btn-ghost" type="button" @click="showBasicsModal = false">取消</button>
        </div>
      </DialogModal>

      <DialogModal
        v-if="showTimeSlotsModal"
        title="节次时间"
        description="定义每天第几节对应的开始时间和结束时间。节次编号会自动保持为 1、2、3……"
        @close="showTimeSlotsModal = false"
      >
        <div class="mb-4 flex justify-end">
          <button class="btn-ghost" type="button" @click="addTimeSlot">新增节次</button>
        </div>
        <div class="grid gap-3">
          <div
            v-for="slot in timeSlots"
            :key="slot.periodIndex"
            class="grid gap-3 rounded-[22px] border border-ink/8 bg-white/72 p-4 md:grid-cols-[100px_1fr_1fr_auto]"
          >
            <div class="field-label">
              节次
              <div class="field-input bg-white/65 font-semibold">{{ slot.periodIndex }}</div>
            </div>
            <label class="field-label">
              开始时间
              <input v-model="slot.startTime" class="field-input" type="text">
            </label>
            <label class="field-label">
              结束时间
              <input v-model="slot.endTime" class="field-input" type="text">
            </label>
            <div class="flex items-end">
              <button
                class="btn-ghost px-4 py-3 text-xs"
                type="button"
                :disabled="timeSlots.length <= 1"
                @click="removeTimeSlot(slot.periodIndex)"
              >
                删除
              </button>
            </div>
          </div>
        </div>
        <div class="mt-5 flex gap-3">
          <button class="btn-primary" type="button" @click="saveSlots">保存节次</button>
          <button class="btn-ghost" type="button" @click="showTimeSlotsModal = false">取消</button>
        </div>
      </DialogModal>

      <DialogModal
        v-if="showCourseModal"
        :title="courseForm.id ? '编辑课程' : '添加课程'"
        description="填写课程的上课时间、地点和备注信息。"
        @close="closeCourseModal"
      >
        <div class="grid gap-4 md:grid-cols-2">
          <label class="field-label">
            课程名称
            <input v-model="courseForm.name" class="field-input" type="text">
          </label>
          <label class="field-label">
            星期
            <select v-model="courseForm.weekday" class="field-input">
              <option :value="1">周一</option>
              <option :value="2">周二</option>
              <option :value="3">周三</option>
              <option :value="4">周四</option>
              <option :value="5">周五</option>
              <option :value="6">周六</option>
              <option :value="7">周日</option>
            </select>
          </label>
          <label class="field-label">
            开始周
            <input v-model="courseForm.startWeek" class="field-input" type="number">
          </label>
          <label class="field-label">
            结束周
            <input v-model="courseForm.endWeek" class="field-input" type="number">
          </label>
          <label class="field-label">
            开始节次
            <input v-model="courseForm.startPeriod" class="field-input" type="number">
          </label>
          <label class="field-label">
            结束节次
            <input v-model="courseForm.endPeriod" class="field-input" type="number">
          </label>
          <label class="field-label">
            教师
            <input v-model="courseForm.teacher" class="field-input" type="text">
          </label>
          <label class="field-label">
            地点
            <input v-model="courseForm.location" class="field-input" type="text">
          </label>
          <label class="field-label md:col-span-2">
            备注
            <textarea v-model="courseForm.note" class="field-input min-h-[96px]"></textarea>
          </label>
        </div>

        <div class="mt-5 flex gap-3">
          <button class="btn-primary" type="button" @click="submitCourse">
            {{ courseForm.id ? '保存修改' : '添加课程' }}
          </button>
          <button class="btn-ghost" type="button" @click="closeCourseModal">取消</button>
        </div>
      </DialogModal>

      <ConfirmDialog
        v-if="confirmDeleteCourseOpen"
        :description="coursePendingDelete ? `删除后将无法恢复「${coursePendingDelete.name}」这门课程。` : '删除后将无法恢复这门课程。'"
        :pending="deletePending"
        confirm-text="确认删除课程"
        title="确认删除这门课程？"
        @close="closeDeleteCourseDialog"
        @confirm="confirmRemoveCourse"
      />

      <ConfirmDialog
        v-if="confirmDeleteScheduleOpen"
        :description="`删除后，课表「${form.name}」及其课程、节次和分享链接都会一起移除。`"
        :pending="deletePending"
        confirm-text="确认删除课表"
        title="确认删除这张课表？"
        @close="closeDeleteScheduleDialog"
        @confirm="confirmRemoveSchedule"
      />
    </div>
  </AppShell>
</template>
