export const APP_CONFIG = {
  name: 'ClassWeave',
  timezone: 'Asia/Shanghai',
  defaultAnalysisWindowMinutes: 60,
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api/v1',
}

export const DAY_NAMES = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
