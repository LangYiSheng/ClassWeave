<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="flex min-h-screen items-center justify-center px-4">
    <div class="glass-card max-w-lg p-8 text-center">
      <p class="section-tag">404</p>
      <h1 class="font-heading text-5xl tracking-[-0.05em]">页面不存在</h1>
      <p class="mt-4 text-sm leading-7 text-muted">
        你访问的页面不存在，返回首页继续查看课表和共享安排。
      </p>
      <RouterLink class="btn-primary mt-6" to="/schedules">返回首页</RouterLink>
    </div>
  </div>
</template>
