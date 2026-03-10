<script setup>
defineProps({
  panel: {
    type: Object,
    default: null,
  },
})

const sections = [
  { key: 'current', title: '正在上课' },
  { key: 'free', title: '当前空闲' },
  { key: 'soonStart', title: '即将上课' },
  { key: 'soonEnd', title: '即将下课' },
]

function formatItem(item, key) {
  if (key === 'free') {
    return `${item.ownerDisplayName} · 当前空闲`
  }

  if (key === 'current') {
    return `${item.ownerDisplayName} · ${item.courseName} (${item.startTime}-${item.endTime})`
  }

  if (key === 'soonStart') {
    return `${item.ownerDisplayName} · ${item.startTime} 上课 · ${item.courseName}`
  }

  return `${item.ownerDisplayName} · ${item.endTime} 下课 · ${item.courseName}`
}
</script>

<template>
  <section class="panel-card">
    <div class="mb-5 flex items-start justify-between gap-4">
      <div>
        <p class="section-tag">实时状态</p>
        <h3 class="font-heading text-3xl tracking-[-0.04em]">谁现在有空</h3>
      </div>
      <div v-if="$slots.action">
        <slot name="action" />
      </div>
    </div>

    <div class="grid gap-3 lg:grid-cols-4">
      <article
        v-for="section in sections"
        :key="section.key"
        class="rounded-[22px] border border-ink/8 bg-white/72 p-4"
      >
        <h4 class="text-sm font-semibold">{{ section.title }}</h4>
        <div class="mt-3 flex flex-wrap gap-2">
          <span
            v-for="item in panel?.[section.key] ?? []"
            :key="`${section.key}-${item.scheduleId}-${item.courseId || item.ownerDisplayName}`"
            class="inline-flex rounded-full bg-ink/6 px-3 py-2 text-xs leading-5 text-ink"
          >
            {{ formatItem(item, section.key) }}
          </span>
          <span
            v-if="!(panel?.[section.key] ?? []).length"
            class="text-xs leading-5 text-muted"
          >
            暂无
          </span>
        </div>
      </article>
    </div>
  </section>
</template>
