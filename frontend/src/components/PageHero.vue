<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: 'ClassWeave',
  },
  title: {
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
  <section class="panel-card">
    <div class="min-w-0">
      <p class="section-tag">{{ eyebrow }}</p>
      <h2 class="font-heading text-3xl tracking-[-0.05em] text-ink lg:text-4xl">
        {{ title }}
      </h2>
      <p v-if="description" class="mt-3 max-w-3xl text-sm leading-7 text-muted">
        {{ description }}
      </p>
    </div>

    <div
      v-if="$slots.side"
      class="mt-5 grid gap-3 md:grid-cols-2 xl:grid-flow-col xl:auto-cols-fr"
    >
      <slot name="side" />
    </div>

    <div v-if="$slots.actions" class="mt-5 flex flex-wrap gap-3">
      <slot name="actions" />
    </div>
  </section>
</template>
