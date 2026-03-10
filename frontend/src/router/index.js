import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guestOnly: true },
  },
  {
    path: '/',
    redirect: '/schedules',
  },
  {
    path: '/schedules',
    name: 'schedules',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/board',
    name: 'board',
    component: () => import('@/views/BoardView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/schedules/:scheduleId/edit',
    name: 'schedule-edit',
    component: () => import('@/views/ScheduleEditorView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/shares',
    name: 'shares',
    component: () => import('@/views/SharesView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/share/:shareToken',
    name: 'share-accept',
    component: () => import('@/views/ShareAcceptView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  authStore.initialize()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'schedules' }
  }

  return true
})

export default router
