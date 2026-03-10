<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  value: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <article class="metric-card flex h-full min-h-[132px] min-w-0 flex-col">
    <div class="text-sm text-muted">{{ label }}</div>
    <div class="mt-2 text-3xl font-semibold tracking-[-0.04em] text-ink">{{ value }}</div>
    <p v-if="description" class="mt-2 text-xs leading-5 text-muted">{{ description }}</p>
  </article>
</template>
