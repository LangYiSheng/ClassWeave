<script setup>
import { computed } from 'vue'

import { DAY_NAMES } from '@/config/app'
import { formatDisplayTime, getWeekdayFromDate, toMinuteValue } from '@/utils/time'

const props = defineProps({
  detailsList: {
    type: Array,
    default: () => [],
  },
  currentDate: {
    type: String,
    required: true,
  },
  currentTime: {
    type: String,
    required: true,
  },
  startTime: {
    type: String,
    default: '06:00',
  },
  endTime: {
    type: String,
    default: '22:00',
  },
})

const hourRowHeight = 84
const headerHeight = 64

const startMinute = computed(() => toMinuteValue(`${props.startTime}:00`))
const endMinute = computed(() => toMinuteValue(`${props.endTime}:00`))
const totalMinutes = computed(() => Math.max(60, endMinute.value - startMinute.value))
const totalHours = computed(() => Math.max(1, Math.ceil(totalMinutes.value / 60)))
const timelineHeight = computed(() => totalHours.value * hourRowHeight)
const currentDayIndex = computed(() => getWeekdayFromDate(props.currentDate) - 1)
const visibleDetails = computed(() => props.detailsList.filter(Boolean))
const weekDates = computed(() => {
  const [year, month, day] = String(props.currentDate).split('-').map(Number)

  if (!year || !month || !day) {
    return DAY_NAMES.map(() => '')
  }

  const currentDate = new Date(Date.UTC(year, month - 1, day))
  const currentWeekday = getWeekdayFromDate(props.currentDate)
  const monday = new Date(currentDate)
  monday.setUTCDate(currentDate.getUTCDate() - (currentWeekday - 1))

  return DAY_NAMES.map((_, index) => {
    const date = new Date(monday)
    date.setUTCDate(monday.getUTCDate() + index)
    return `${date.getUTCMonth() + 1}/${date.getUTCDate()}`
  })
})

function getWeekNumber(startDate, currentDate) {
  const [startYear, startMonth, startDay] = String(startDate).split('-').map(Number)
  const [currentYear, currentMonth, currentDay] = String(currentDate).split('-').map(Number)

  if (!startYear || !startMonth || !startDay || !currentYear || !currentMonth || !currentDay) {
    return null
  }

  const start = Date.UTC(startYear, startMonth - 1, startDay)
  const current = Date.UTC(currentYear, currentMonth - 1, currentDay)
  const diffDays = Math.floor((current - start) / (24 * 60 * 60 * 1000))

  return Math.floor(diffDays / 7) + 1
}

const timeAxis = computed(() => {
  const labels = []

  for (let minute = startMinute.value; minute <= endMinute.value; minute += 60) {
    labels.push({
      key: minute,
      label: `${String(Math.floor(minute / 60)).padStart(2, '0')}:00`,
      top: minute === startMinute.value ? 0 : minute - startMinute.value,
    })
  }

  return labels
})

function getTimeSlotRange(course, timeSlots) {
  const startSlot = timeSlots.find((slot) => slot.periodIndex === Number(course.startPeriod))
  const endSlot = timeSlots.find((slot) => slot.periodIndex === Number(course.endPeriod))

  return {
    startTime: startSlot?.startTime ?? '06:00:00',
    endTime: endSlot?.endTime ?? '06:45:00',
  }
}

function getCourseStyle(course, schedule, timeSlots, index, visibleCount) {
  const range = getTimeSlotRange(course, timeSlots)
  const startValue = toMinuteValue(range.startTime)
  const endValue = toMinuteValue(range.endTime)
  const visibleStart = Math.max(startValue, startMinute.value)
  const visibleEnd = Math.min(endValue, endMinute.value)

  if (visibleEnd <= startMinute.value || visibleStart >= endMinute.value || visibleEnd <= visibleStart) {
    return null
  }

  const top = ((visibleStart - startMinute.value) / totalMinutes.value) * 100
  const height = ((visibleEnd - visibleStart) / totalMinutes.value) * 100
  const currentValue = toMinuteValue(props.currentTime)
  const isCurrentDay = course.weekday - 1 === currentDayIndex.value
  const isCurrent = isCurrentDay && currentValue >= startValue && currentValue < endValue

  return {
    top: `${top}%`,
    height: `${height}%`,
    left: `${10 + index * 10}px`,
    right: `${10 + Math.max(0, (visibleCount - index - 1) * 10)}px`,
    backgroundColor: schedule.displayColor ?? schedule.defaultColor,
    opacity: schedule.displayOpacity,
    outline: isCurrent ? '3px solid rgba(255,255,255,0.82)' : 'none',
    boxShadow: isCurrent
      ? '0 0 0 3px rgba(31, 34, 48, 0.08), 0 18px 36px rgba(31, 34, 48, 0.24)'
      : '0 14px 28px rgba(31, 34, 48, 0.18)',
  }
}

function isCourseVisibleThisWeek(course, schedule) {
  const weekNumber = getWeekNumber(schedule.startDate, props.currentDate)

  if (!weekNumber || weekNumber < 1) {
    return false
  }

  if (schedule.totalWeeks && weekNumber > Number(schedule.totalWeeks)) {
    return false
  }

  if (weekNumber < Number(course.startWeek) || weekNumber > Number(course.endWeek)) {
    return false
  }

  if (course.weekType === 'ODD' && weekNumber % 2 === 0) {
    return false
  }

  if (course.weekType === 'EVEN' && weekNumber % 2 === 1) {
    return false
  }

  return true
}
</script>

<template>
  <section class="panel-card overflow-hidden">
    <div class="mb-5 flex flex-wrap items-start justify-between gap-4">
      <div>
        <p class="section-tag">周视图叠加</p>
        <h3 class="font-heading text-3xl tracking-[-0.04em]">{{ startTime }} - {{ endTime }} 课程对比</h3>
      </div>
      <div class="flex flex-wrap gap-2">
        <span
          v-for="detail in visibleDetails"
          :key="detail.schedule.id"
          class="inline-flex items-center gap-2 rounded-full bg-white/80 px-3 py-2 text-xs"
        >
          <span
            class="h-3 w-3 rounded-full"
            :style="{ backgroundColor: detail.schedule.displayColor ?? detail.schedule.defaultColor }"
          ></span>
          {{ detail.schedule.displayNameOverride ?? detail.schedule.name }}
        </span>
      </div>
    </div>

    <div class="overflow-x-auto pb-2">
      <div class="grid min-w-[980px] grid-cols-[72px_minmax(0,1fr)] gap-0">
        <div class="h-16"></div>

        <div class="row-span-2 overflow-hidden rounded-[24px] border border-line bg-white/50">
          <div class="grid grid-cols-7 border-b border-line">
            <header
              v-for="(dayName, dayIndex) in DAY_NAMES"
              :key="`header-${dayName}`"
              class="relative flex h-16 flex-col items-center justify-center border-r border-line bg-[#f6f1e8] text-sm font-semibold last:border-r-0"
            >
              <span>{{ dayName }}</span>
              <span class="mt-1 text-[11px] font-medium text-muted">{{ weekDates[dayIndex] }}</span>
              <span
                v-if="dayIndex === currentDayIndex"
                class="absolute inset-x-4 bottom-2 h-1.5 rounded-full bg-coral/55"
              ></span>
            </header>
          </div>

          <div class="grid grid-cols-7" :style="{ height: `${timelineHeight}px` }">
            <section
              v-for="(dayName, dayIndex) in DAY_NAMES"
              :key="`body-${dayName}`"
              class="relative border-r border-line last:border-r-0"
              :style="{
                backgroundImage: 'linear-gradient(180deg, transparent calc(84px - 1px), rgba(31,34,48,0.08) calc(84px - 1px))',
                backgroundSize: '100% 84px',
              }"
            >
              <article
                v-for="(detail, visibleIndex) in visibleDetails"
                :key="`${dayName}-${detail.schedule.id}`"
              >
                <div
                  v-for="course in detail.courses.filter((item) => item.weekday === dayIndex + 1 && isCourseVisibleThisWeek(item, detail.schedule))"
                  :key="course.id"
                  v-show="getCourseStyle(course, detail.schedule, detail.timeSlots, visibleIndex, visibleDetails.length)"
                  class="absolute rounded-[18px] p-3 text-white"
                  :style="getCourseStyle(course, detail.schedule, detail.timeSlots, visibleIndex, visibleDetails.length)"
                >
                  <strong class="relative z-[1] block text-sm leading-5">{{ course.name }}</strong>
                  <span class="relative z-[1] mt-1 block text-[11px] leading-4 text-white/90">
                    {{ detail.schedule.ownerDisplayName }} / {{ course.teacher }}
                  </span>
                  <span class="relative z-[1] mt-1 block text-[11px] leading-4 text-white/90">
                    {{ course.location }}
                  </span>
                  <span class="relative z-[1] mt-1 block text-[11px] leading-4 text-white/90">
                    {{ formatDisplayTime(getTimeSlotRange(course, detail.timeSlots).startTime) }}
                    -
                    {{ formatDisplayTime(getTimeSlotRange(course, detail.timeSlots).endTime) }}
                  </span>
                  <span class="pointer-events-none absolute inset-0 rounded-[18px] bg-gradient-to-b from-white/20 to-transparent"></span>
                </div>
              </article>
            </section>
          </div>
        </div>

        <div class="relative pr-4" :style="{ height: `${timelineHeight}px` }">
          <span
            v-for="tick in timeAxis"
            :key="tick.key"
            class="absolute right-3 text-xs leading-none text-muted"
            :style="{ top: `${(tick.top / totalMinutes) * 100}%`, transform: 'translateY(-50%)' }"
          >
            {{ tick.label }}
          </span>
        </div>
      </div>
    </div>
  </section>
</template>
