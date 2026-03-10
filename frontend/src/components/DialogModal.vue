<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['close'])
</script>

<template>
  <teleport to="body">
    <div class="fixed inset-0 z-50 flex items-center justify-center bg-ink/30 px-4 py-8">
      <div class="panel-card max-h-[88vh] w-full max-w-3xl overflow-y-auto">
        <div class="mb-5 flex items-start justify-between gap-4">
          <div>
            <h3 class="font-heading text-3xl tracking-[-0.05em]">{{ title }}</h3>
            <p v-if="description" class="mt-2 text-sm leading-6 text-muted">{{ description }}</p>
          </div>
          <button class="btn-ghost px-4 py-2 text-xs" type="button" @click="emit('close')">关闭</button>
        </div>
        <slot />
      </div>
    </div>
  </teleport>
</template>
