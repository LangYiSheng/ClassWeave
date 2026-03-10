<script setup>
import { RouterLink } from 'vue-router'

const props = defineProps({
  schedules: {
    type: Array,
    default: () => [],
  },
  selectedScheduleIds: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['toggle', 'opacity-change', 'color-change'])

function isSelected(scheduleId) {
  return props.selectedScheduleIds.includes(scheduleId)
}

function createRandomColor() {
  return `#${Math.floor(Math.random() * 0xffffff)
    .toString(16)
    .padStart(6, '0')
    .toUpperCase()}`
}

function updateColor(scheduleId, displayColor) {
  emit('color-change', { scheduleId, displayColor })
}

function applyRandomColor(scheduleId) {
  updateColor(scheduleId, createRandomColor())
}
</script>

<template>
  <section class="panel-card">
    <div class="mb-5 flex items-start justify-between gap-4">
      <div>
        <p class="section-tag">课程表仓库</p>
        <h3 class="font-heading text-3xl tracking-[-0.04em]">可叠加课表</h3>
      </div>
      <RouterLink class="btn-ghost px-4 py-2 text-xs" :to="{ name: 'schedules', query: { action: 'create' } }">
        新建
      </RouterLink>
    </div>

    <div class="grid gap-4">
      <article
        v-for="schedule in schedules"
        :key="schedule.id"
        class="rounded-[24px] border border-ink/8 bg-white/70 p-4"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="flex gap-3">
            <span
              class="mt-1 h-3.5 w-3.5 rounded-full"
              :style="{ backgroundColor: schedule.displayColor ?? schedule.defaultColor }"
            ></span>
            <div>
              <div class="text-base font-semibold">{{ schedule.displayNameOverride ?? schedule.name }}</div>
              <div class="mt-1 text-xs text-muted">
                {{ schedule.ownerDisplayName }} · {{ schedule.termLabel || '未命名学期' }}
              </div>
            </div>
          </div>
          <span class="rounded-full bg-ink/5 px-3 py-1 text-xs text-muted">
            {{ schedule.accessRole === 'OWNER' ? '我的课表' : '共享课表' }}
          </span>
        </div>

        <p class="mt-3 text-sm leading-6 text-muted">{{ schedule.description }}</p>

        <div class="mt-4 grid gap-3">
          <label class="inline-flex items-center gap-3 text-sm font-semibold">
            <input
              :checked="isSelected(schedule.id)"
              class="h-4 w-4 rounded border-ink/30 text-blue focus:ring-blue/20"
              type="checkbox"
              @change="emit('toggle', schedule.id)"
            >
            叠加显示
          </label>

          <div class="grid grid-cols-[1fr_auto] items-center gap-2 text-sm">
            <span class="text-muted">透明度</span>
            <output class="font-semibold text-ink">{{ Math.round(schedule.displayOpacity * 100) }}%</output>
          </div>

          <input
            class="h-2 w-full cursor-pointer appearance-none rounded-full bg-ink/10 accent-blue"
            :value="Math.round(schedule.displayOpacity * 100)"
            max="100"
            min="35"
            step="1"
            type="range"
            @input="emit('opacity-change', { scheduleId: schedule.id, opacity: Number($event.target.value) / 100 })"
          >

          <label class="field-label">
            展示颜色
            <div class="relative">
              <span
                class="pointer-events-none absolute left-4 top-1/2 h-3.5 w-3.5 -translate-y-1/2 rounded-full border border-ink/10"
                :style="{ backgroundColor: schedule.displayColor ?? schedule.defaultColor }"
              ></span>
              <input
                :value="schedule.displayColor ?? schedule.defaultColor"
                class="field-input pl-10 pr-[116px] uppercase"
                type="text"
                @input="updateColor(schedule.id, $event.target.value)"
              >
              <div class="absolute inset-y-0 right-2 flex items-center gap-2">
                <button
                  class="rounded-full bg-ink/5 px-3 py-1.5 text-[11px] font-semibold text-ink transition hover:bg-ink/10"
                  type="button"
                  @click="applyRandomColor(schedule.id)"
                >
                  随机
                </button>
                <label class="relative inline-flex cursor-pointer items-center justify-center rounded-full bg-ink/5 px-3 py-1.5 text-[11px] font-semibold text-ink transition hover:bg-ink/10">
                  选色
                  <input
                    :value="schedule.displayColor ?? schedule.defaultColor"
                    class="absolute inset-0 cursor-pointer opacity-0"
                    type="color"
                    @input="updateColor(schedule.id, $event.target.value)"
                  >
                </label>
              </div>
            </div>
          </label>

          <div class="flex gap-2 pt-1">
            <RouterLink class="btn-ghost flex-1 px-4 py-2 text-xs" :to="{ name: 'schedule-edit', params: { scheduleId: schedule.id } }">
              {{ schedule.accessRole === 'OWNER' ? '编辑' : '详情' }}
            </RouterLink>
            <RouterLink class="btn-ghost flex-1 px-4 py-2 text-xs" :to="schedule.accessRole === 'OWNER' ? '/shares' : '/schedules'">
              {{ schedule.accessRole === 'OWNER' ? '分享' : '课表页' }}
            </RouterLink>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>
