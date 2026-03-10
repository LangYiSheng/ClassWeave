import { APP_CONFIG, DAY_NAMES } from '@/config/app'

export function padTime(value) {
  return String(value).padStart(2, '0')
}

export function minutesToTime(minutes) {
  const hour = Math.floor(minutes / 60)
  const minute = minutes % 60
  return `${padTime(hour)}:${padTime(minute)}:00`
}

export function toMinuteValue(timeString) {
  const [hour = '0', minute = '0'] = String(timeString).split(':')
  return Number(hour) * 60 + Number(minute)
}

export function formatDisplayTime(timeString) {
  const [hour = '00', minute = '00'] = String(timeString).split(':')
  return `${hour}:${minute}`
}

export function getBeijingNow() {
  const formatter = new Intl.DateTimeFormat('sv-SE', {
    timeZone: APP_CONFIG.timezone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })

  const parts = Object.fromEntries(
    formatter
      .formatToParts(new Date())
      .filter((item) => item.type !== 'literal')
      .map((item) => [item.type, item.value]),
  )

  return {
    date: `${parts.year}-${parts.month}-${parts.day}`,
    time: `${parts.hour}:${parts.minute}:${parts.second}`,
  }
}

export function getWeekdayFromDate(dateString) {
  const [year, month, dayOfMonth] = String(dateString).split('-').map(Number)

  if (!year || !month || !dayOfMonth) {
    return 1
  }

  const date = new Date(Date.UTC(year, month - 1, dayOfMonth))
  const day = date.getUTCDay()
  return day === 0 ? 7 : day
}

export function getDayNameByDate(dateString) {
  return DAY_NAMES[getWeekdayFromDate(dateString) - 1]
}
