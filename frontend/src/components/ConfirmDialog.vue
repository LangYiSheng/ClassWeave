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
  confirmText: {
    type: String,
    default: '确认',
  },
  pending: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['close', 'confirm'])
</script>

<template>
  <teleport to="body">
    <div class="fixed inset-0 z-[60] flex items-center justify-center bg-ink/35 px-4 py-8">
      <div class="panel-card w-full max-w-xl">
        <h3 class="font-heading text-3xl tracking-[-0.05em] text-ink">{{ title }}</h3>
        <p v-if="description" class="mt-3 text-sm leading-7 text-muted">
          {{ description }}
        </p>

        <div class="mt-6 flex justify-end gap-3">
          <button class="btn-ghost px-4 py-2 text-sm" type="button" @click="emit('close')">取消</button>
          <button class="btn-primary px-4 py-2 text-sm" :disabled="pending" type="button" @click="emit('confirm')">
            {{ pending ? '处理中...' : confirmText }}
          </button>
        </div>
      </div>
    </div>
  </teleport>
</template>
