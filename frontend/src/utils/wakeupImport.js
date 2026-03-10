function normalizeDate(value) {
  const [year, month, day] = String(value || '')
    .split('-')
    .map((part) => Number(part))

  if (!year || !month || !day) {
    return ''
  }

  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
}

function normalizeTime(value) {
  const [hour = '00', minute = '00'] = String(value || '').split(':')
  return `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:00`
}

function normalizeColor(value) {
  const color = String(value || '').trim()

  if (!color) {
    return '#1F6FEB'
  }

  if (/^#([A-Fa-f0-9]{8})$/.test(color)) {
    return `#${color.slice(3).toUpperCase()}`
  }

  if (/^#([A-Fa-f0-9]{6})$/.test(color)) {
    return color.toUpperCase()
  }

  return '#1F6FEB'
}

function inferTermLabel(dateString) {
  const [, month] = String(dateString).split('-').map(Number)
  const year = String(dateString).slice(0, 4)

  if (!year || !month) {
    return ''
  }

  if (month >= 2 && month <= 7) {
    return `${year} 春`
  }

  if (month >= 8 && month <= 12) {
    return `${year} 秋`
  }

  return `${year} 冬`
}

function mapWeekType(type) {
  if (type === 1) {
    return 'ODD'
  }

  if (type === 2) {
    return 'EVEN'
  }

  return 'ALL'
}

function parseWakeUpLines(text) {
  const lines = String(text)
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  if (lines.length < 5) {
    throw new Error('备份文件格式不完整，至少需要包含 5 段数据。')
  }

  return lines.slice(0, 5).map((line, index) => {
    try {
      return JSON.parse(line)
    } catch {
      throw new Error(`第 ${index + 1} 段数据不是有效的 JSON。`)
    }
  })
}

export function parseWakeUpSchedule(text) {
  const [tableMeta, rawTimeSlots, scheduleConfig, courseCatalog, rawCourseDetails] = parseWakeUpLines(text)

  const selectedTimeTableId = scheduleConfig.timeTable
  const maxPeriodsPerDay = Number(scheduleConfig.nodes || tableMeta.courseLen || 12)
  const startDate = normalizeDate(scheduleConfig.startDate)

  const timeSlots = rawTimeSlots
    .filter((slot) =>
      slot.timeTable === selectedTimeTableId &&
      Number(slot.node) <= maxPeriodsPerDay &&
      !(String(slot.startTime) === '00:00' && String(slot.endTime) === '00:45'),
    )
    .map((slot) => ({
      periodIndex: Number(slot.node),
      startTime: normalizeTime(slot.startTime),
      endTime: normalizeTime(slot.endTime),
    }))
    .sort((left, right) => left.periodIndex - right.periodIndex)

  const courseById = new Map(
    courseCatalog.map((course) => [
      course.id,
      {
        name: course.courseName || '未命名课程',
        note: course.note || '',
      },
    ]),
  )

  const courses = rawCourseDetails
    .map((detail, index) => {
      const courseInfo = courseById.get(detail.id)

      if (!courseInfo) {
        return {
          id: `unknown-${index}`,
          name: '未命名课程',
          weekday: Number(detail.day) || 1,
          startWeek: Number(detail.startWeek) || 1,
          endWeek: Number(detail.endWeek) || Number(scheduleConfig.maxWeek) || 1,
          startPeriod: Number(detail.startNode) || 1,
          endPeriod: (Number(detail.startNode) || 1) + Math.max(Number(detail.step) || 1, 1) - 1,
          weekType: mapWeekType(Number(detail.type)),
          teacher: detail.teacher || '',
          location: detail.room || '',
          note: '',
          isTemporary: Boolean(detail.ownTime),
        }
      }

      return {
        id: `${detail.id}-${index}`,
        name: courseInfo.name,
        weekday: Number(detail.day) || 1,
        startWeek: Number(detail.startWeek) || 1,
        endWeek: Number(detail.endWeek) || Number(scheduleConfig.maxWeek) || 1,
        startPeriod: Number(detail.startNode) || 1,
        endPeriod: (Number(detail.startNode) || 1) + Math.max(Number(detail.step) || 1, 1) - 1,
        weekType: mapWeekType(Number(detail.type)),
        teacher: detail.teacher || '',
        location: detail.room || '',
        note: courseInfo.note || '',
        isTemporary: Boolean(detail.ownTime),
      }
    })
    .sort((left, right) => {
      if (left.weekday !== right.weekday) {
        return left.weekday - right.weekday
      }

      if (left.startPeriod !== right.startPeriod) {
        return left.startPeriod - right.startPeriod
      }

      return left.startWeek - right.startWeek
    })

  const payload = {
    schedule: {
      name: scheduleConfig.tableName || scheduleConfig.name || '导入课表',
      termLabel: inferTermLabel(startDate),
      description: scheduleConfig.school ? `${scheduleConfig.school} · 从 WakeUp 课程表导入` : '从 WakeUp 课程表导入',
      startDate,
      totalWeeks: Number(scheduleConfig.maxWeek) || 16,
      maxPeriodsPerDay,
      defaultColor: normalizeColor(courseCatalog[0]?.color),
    },
    timeSlots,
    courses: courses.map(({ id, ...course }) => course),
  }

  return {
    raw: {
      tableMeta,
      scheduleConfig,
      courseCatalog,
      rawCourseDetails,
    },
    payload,
    preview: {
      name: payload.schedule.name,
      termLabel: payload.schedule.termLabel || '未识别学期',
      school: scheduleConfig.school || '未填写学校',
      startDate: payload.schedule.startDate,
      totalWeeks: payload.schedule.totalWeeks,
      maxPeriodsPerDay: payload.schedule.maxPeriodsPerDay,
      courseCount: payload.courses.length,
      timeSlotCount: payload.timeSlots.length,
    },
  }
}
