import { getBeijingNow } from '@/utils/time'

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

const baseTimeSlots = [
  { id: 101, periodIndex: 1, startTime: '08:00:00', endTime: '08:45:00' },
  { id: 102, periodIndex: 2, startTime: '08:55:00', endTime: '09:40:00' },
  { id: 103, periodIndex: 3, startTime: '10:00:00', endTime: '10:45:00' },
  { id: 104, periodIndex: 4, startTime: '10:55:00', endTime: '11:40:00' },
  { id: 105, periodIndex: 5, startTime: '13:30:00', endTime: '14:15:00' },
  { id: 106, periodIndex: 6, startTime: '14:25:00', endTime: '15:10:00' },
  { id: 107, periodIndex: 7, startTime: '15:30:00', endTime: '16:15:00' },
  { id: 108, periodIndex: 8, startTime: '16:25:00', endTime: '17:10:00' },
  { id: 109, periodIndex: 9, startTime: '19:00:00', endTime: '19:45:00' },
  { id: 110, periodIndex: 10, startTime: '19:55:00', endTime: '20:40:00' },
]

const now = getBeijingNow()

const mockDb = {
  users: [
    { id: 1, username: 'lancher', password: '12345678', displayName: 'Lan' },
    { id: 2, username: 'april', password: '12345678', displayName: 'April' },
    { id: 3, username: 'kai', password: '12345678', displayName: 'Kai' },
  ],
  schedules: [
    {
      id: 12,
      ownerUserId: 1,
      name: '大三下课程表',
      termLabel: '2026 春',
      description: '主课表，偏满课日集中在周一到周四。',
      startDate: '2026-02-24',
      totalWeeks: 18,
      maxPeriodsPerDay: 12,
      defaultColor: '#1F6FEB',
      timeSlots: clone(baseTimeSlots),
      courses: [
        {
          id: 9001,
          name: '编译原理',
          weekday: 1,
          startWeek: 1,
          endWeek: 16,
          startPeriod: 1,
          endPeriod: 2,
          weekType: 'ALL',
          teacher: '赵老师',
          location: '信工楼 405',
          note: '周一开场课',
          color: '#1F6FEB',
          isTemporary: false,
        },
        {
          id: 9002,
          name: '分布式系统',
          weekday: 2,
          startWeek: 1,
          endWeek: 16,
          startPeriod: 3,
          endPeriod: 4,
          weekType: 'ALL',
          teacher: 'Liu',
          location: 'A-302',
          note: '',
          color: '#1F6FEB',
          isTemporary: false,
        },
        {
          id: 9003,
          name: '软件工程',
          weekday: 3,
          startWeek: 1,
          endWeek: 16,
          startPeriod: 5,
          endPeriod: 6,
          weekType: 'ALL',
          teacher: '陈老师',
          location: '实验中心 201',
          note: '适合演示状态面板',
          color: '#2A8CFF',
          isTemporary: false,
        },
        {
          id: 9004,
          name: '机器学习',
          weekday: 4,
          startWeek: 1,
          endWeek: 16,
          startPeriod: 7,
          endPeriod: 8,
          weekType: 'ALL',
          teacher: 'Qin',
          location: '理科楼 511',
          note: '',
          color: '#1F6FEB',
          isTemporary: false,
        },
        {
          id: 9005,
          name: '项目研讨',
          weekday: 5,
          startWeek: 1,
          endWeek: 18,
          startPeriod: 2,
          endPeriod: 4,
          weekType: 'ALL',
          teacher: '导师组',
          location: '创新基地',
          note: '',
          color: '#4A84F5',
          isTemporary: false,
        },
      ],
    },
    {
      id: 18,
      ownerUserId: 2,
      name: 'April 大二下',
      termLabel: '2026 春',
      description: '分享加入，语言与经管课程较多，方便约饭前看空档。',
      startDate: '2026-02-24',
      totalWeeks: 18,
      maxPeriodsPerDay: 10,
      defaultColor: '#DF5D43',
      timeSlots: clone(baseTimeSlots),
      courses: [
        {
          id: 9101,
          name: '国际贸易',
          weekday: 1,
          startWeek: 1,
          endWeek: 16,
          startPeriod: 3,
          endPeriod: 4,
          weekType: 'ALL',
          teacher: '徐老师',
          location: '经管 202',
          note: '',
          color: '#DF5D43',
          isTemporary: false,
        },
        {
          id: 9102,
          name: '英语演讲',
          weekday: 2,
          startWeek: 1,
          endWeek: 16,
          startPeriod: 1,
          endPeriod: 2,
          weekType: 'ALL',
          teacher: 'Ms. Sun',
          location: '外语楼 108',
          note: '',
          color: '#D96952',
          isTemporary: false,
        },
        {
          id: 9103,
          name: '市场调研',
          weekday: 3,
          startWeek: 1,
          endWeek: 16,
          startPeriod: 7,
          endPeriod: 8,
          weekType: 'ALL',
          teacher: '唐老师',
          location: '经管 307',
          note: '',
          color: '#DF5D43',
          isTemporary: false,
        },
        {
          id: 9104,
          name: '统计学',
          weekday: 4,
          startWeek: 1,
          endWeek: 16,
          startPeriod: 5,
          endPeriod: 6,
          weekType: 'ALL',
          teacher: '何老师',
          location: '经管 110',
          note: '',
          color: '#DF5D43',
          isTemporary: false,
        },
      ],
    },
    {
      id: 25,
      ownerUserId: 3,
      name: 'Kai 考研作息',
      termLabel: '2026 春',
      description: '不是课表而是学习作息，用来比较共同空闲时段。',
      startDate: '2026-02-24',
      totalWeeks: 18,
      maxPeriodsPerDay: 10,
      defaultColor: '#1D8B8F',
      timeSlots: clone(baseTimeSlots),
      courses: [
        {
          id: 9201,
          name: '高数晨读',
          weekday: 1,
          startWeek: 1,
          endWeek: 18,
          startPeriod: 1,
          endPeriod: 2,
          weekType: 'ALL',
          teacher: '自习',
          location: '图书馆',
          note: '',
          color: '#1D8B8F',
          isTemporary: false,
        },
        {
          id: 9202,
          name: '专业课专注段',
          weekday: 1,
          startWeek: 1,
          endWeek: 18,
          startPeriod: 5,
          endPeriod: 8,
          weekType: 'ALL',
          teacher: '自习',
          location: '图书馆',
          note: '',
          color: '#1D8B8F',
          isTemporary: false,
        },
        {
          id: 9203,
          name: '英语真题',
          weekday: 3,
          startWeek: 1,
          endWeek: 18,
          startPeriod: 9,
          endPeriod: 10,
          weekType: 'ALL',
          teacher: '自习',
          location: '宿舍',
          note: '',
          color: '#1D8B8F',
          isTemporary: false,
        },
      ],
    },
  ],
  accesses: [
    {
      id: 1,
      userId: 1,
      scheduleId: 12,
      accessRole: 'OWNER',
      accessSource: 'OWNER',
      shareLinkId: null,
      displayNameOverride: null,
      displayColor: '#1F6FEB',
      displayOpacity: 0.86,
      isVisibleDefault: true,
    },
    {
      id: 2,
      userId: 1,
      scheduleId: 18,
      accessRole: 'VIEWER',
      accessSource: 'SHARE_LINK',
      shareLinkId: 701,
      displayNameOverride: null,
      displayColor: '#DF5D43',
      displayOpacity: 0.78,
      isVisibleDefault: true,
    },
    {
      id: 3,
      userId: 1,
      scheduleId: 25,
      accessRole: 'VIEWER',
      accessSource: 'SHARE_LINK',
      shareLinkId: 702,
      displayNameOverride: null,
      displayColor: '#1D8B8F',
      displayOpacity: 0.72,
      isVisibleDefault: false,
    },
  ],
  shareLinks: [
    {
      id: 701,
      scheduleId: 18,
      createdByUserId: 2,
      shareToken: 'share-april-2026-spring',
      permissionType: 'VIEW',
      isActive: true,
      expiresAt: null,
      createdAt: `${now.date} ${now.time}`,
    },
    {
      id: 702,
      scheduleId: 25,
      createdByUserId: 3,
      shareToken: 'share-kai-plan',
      permissionType: 'VIEW',
      isActive: true,
      expiresAt: null,
      createdAt: `${now.date} ${now.time}`,
    },
  ],
}

function getUserById(userId) {
  return mockDb.users.find((user) => user.id === userId)
}

function getSchedule(scheduleId) {
  return mockDb.schedules.find((item) => item.id === Number(scheduleId))
}

export function createToken(user) {
  return `mock-token-${user.id}`
}

export function findUserByUsername(username) {
  return mockDb.users.find((user) => user.username === username)
}

export function createUser({ username, password, displayName }) {
  const id = Math.max(...mockDb.users.map((user) => user.id)) + 1
  const user = { id, username, password, displayName }
  mockDb.users.push(user)
  return clone(user)
}

export function listScheduleSummaries(userId) {
  return mockDb.accesses
    .filter((access) => access.userId === Number(userId))
    .map((access) => {
      const schedule = getSchedule(access.scheduleId)
      const owner = getUserById(schedule.ownerUserId)

      return {
        id: schedule.id,
        name: schedule.name,
        termLabel: schedule.termLabel,
        description: schedule.description,
        startDate: schedule.startDate,
        totalWeeks: schedule.totalWeeks,
        maxPeriodsPerDay: schedule.maxPeriodsPerDay,
        defaultColor: schedule.defaultColor,
        ownerUserId: owner.id,
        ownerDisplayName: owner.displayName,
        accessRole: access.accessRole,
        accessSource: access.accessSource,
        displayNameOverride: access.displayNameOverride,
        displayColor: access.displayColor ?? schedule.defaultColor,
        displayOpacity: access.displayOpacity,
        isVisibleDefault: access.isVisibleDefault,
      }
    })
}

export function getScheduleDetail(scheduleId, userId) {
  const summary = listScheduleSummaries(userId).find((item) => item.id === Number(scheduleId))
  const schedule = getSchedule(scheduleId)

  if (!summary || !schedule) {
    return null
  }

  return {
    schedule: clone(summary),
    timeSlots: clone(schedule.timeSlots),
    courses: clone(schedule.courses),
  }
}

export function updateSchedule(scheduleId, payload, userId) {
  const detail = getScheduleDetail(scheduleId, userId)

  if (!detail) {
    return null
  }

  const schedule = getSchedule(scheduleId)
  Object.assign(schedule, {
    name: payload.name,
    termLabel: payload.termLabel,
    description: payload.description,
    startDate: payload.startDate,
    totalWeeks: payload.totalWeeks,
    maxPeriodsPerDay: payload.maxPeriodsPerDay,
    defaultColor: payload.defaultColor,
  })

  const access = mockDb.accesses.find(
    (item) => item.userId === Number(userId) && item.scheduleId === Number(scheduleId),
  )

  if (access) {
    access.displayColor = payload.displayColor ?? access.displayColor
    access.displayOpacity = payload.displayOpacity ?? access.displayOpacity
    access.isVisibleDefault = payload.isVisibleDefault ?? access.isVisibleDefault
  }

  return getScheduleDetail(scheduleId, userId)
}

export function replaceTimeSlots(scheduleId, timeSlots) {
  const schedule = getSchedule(scheduleId)
  schedule.timeSlots = timeSlots.map((slot, index) => ({
    id: slot.id ?? Number(`${scheduleId}${index + 1}`),
    periodIndex: Number(slot.periodIndex),
    startTime: slot.startTime,
    endTime: slot.endTime,
  }))
  return clone(schedule.timeSlots)
}

export function saveCourse(scheduleId, coursePayload) {
  const schedule = getSchedule(scheduleId)

  if (coursePayload.id) {
    const course = schedule.courses.find((item) => item.id === coursePayload.id)
    Object.assign(course, coursePayload)
    return clone(course)
  }

  const newId = Math.max(9000, ...schedule.courses.map((course) => course.id)) + 1
  const course = {
    ...coursePayload,
    id: newId,
  }
  schedule.courses.push(course)
  return clone(course)
}

export function removeCourse(scheduleId, courseId) {
  const schedule = getSchedule(scheduleId)
  schedule.courses = schedule.courses.filter((course) => course.id !== Number(courseId))
  return true
}

export function createSchedule(payload, userId) {
  const nextId = Math.max(...mockDb.schedules.map((item) => item.id)) + 1
  const schedule = {
    id: nextId,
    ownerUserId: Number(userId),
    name: payload.name,
    termLabel: payload.termLabel,
    description: payload.description,
    startDate: payload.startDate,
    totalWeeks: payload.totalWeeks,
    maxPeriodsPerDay: payload.maxPeriodsPerDay,
    defaultColor: payload.defaultColor,
    timeSlots: clone(payload.timeSlots ?? baseTimeSlots),
    courses: clone(payload.courses ?? []),
  }

  mockDb.schedules.push(schedule)
  mockDb.accesses.push({
    id: Math.max(...mockDb.accesses.map((item) => item.id)) + 1,
    userId: Number(userId),
    scheduleId: nextId,
    accessRole: 'OWNER',
    accessSource: 'OWNER',
    shareLinkId: null,
    displayNameOverride: null,
    displayColor: payload.defaultColor,
    displayOpacity: 0.85,
    isVisibleDefault: true,
  })

  return nextId
}

export function listShareLinks(scheduleId) {
  return mockDb.shareLinks
    .filter((link) => link.scheduleId === Number(scheduleId))
    .map((link) => ({
      ...clone(link),
      shareUrl: `https://classweave.local/share/${link.shareToken}`,
    }))
}

export function listAllShareLinksForOwner(userId) {
  return mockDb.shareLinks
    .filter((link) => link.createdByUserId === Number(userId))
    .map((link) => {
      const schedule = getSchedule(link.scheduleId)

      return {
        ...clone(link),
        scheduleName: schedule?.name ?? '未知课表',
        shareUrl: `https://classweave.local/share/${link.shareToken}`,
      }
    })
}

export function createShareLink(scheduleId, userId, expiresAt = null) {
  const current = getBeijingNow()
  const id = Math.max(...mockDb.shareLinks.map((item) => item.id)) + 1
  const token = `share-${scheduleId}-${id}`
  const link = {
    id,
    scheduleId: Number(scheduleId),
    createdByUserId: Number(userId),
    shareToken: token,
    permissionType: 'VIEW',
    isActive: true,
    expiresAt,
    createdAt: `${current.date} ${current.time}`,
  }
  mockDb.shareLinks.unshift(link)
  return {
    ...clone(link),
    shareUrl: `https://classweave.local/share/${token}`,
  }
}

export function disableShareLink(shareLinkId) {
  const target = mockDb.shareLinks.find((item) => item.id === Number(shareLinkId))
  if (target) {
    target.isActive = false
  }
  return true
}

export function acceptShareToken(shareToken, userId) {
  const link = mockDb.shareLinks.find((item) => item.shareToken === shareToken && item.isActive)
  if (!link) {
    return null
  }

  const existing = mockDb.accesses.find(
    (item) => item.userId === Number(userId) && item.scheduleId === link.scheduleId,
  )

  if (!existing) {
    mockDb.accesses.push({
      id: Math.max(...mockDb.accesses.map((item) => item.id)) + 1,
      userId: Number(userId),
      scheduleId: link.scheduleId,
      accessRole: 'VIEWER',
      accessSource: 'SHARE_LINK',
      shareLinkId: link.id,
      displayNameOverride: null,
      displayColor: getSchedule(link.scheduleId).defaultColor,
      displayOpacity: 0.8,
      isVisibleDefault: true,
    })
  }

  return { scheduleId: link.scheduleId }
}

export function importSchedule(payload, userId) {
  return createSchedule(payload, userId)
}

export function deleteSchedule(scheduleId, userId) {
  const numericScheduleId = Number(scheduleId)
  const schedule = getSchedule(numericScheduleId)

  if (!schedule || schedule.ownerUserId !== Number(userId)) {
    return false
  }

  mockDb.schedules = mockDb.schedules.filter((item) => item.id !== numericScheduleId)
  mockDb.accesses = mockDb.accesses.filter((item) => item.scheduleId !== numericScheduleId)
  mockDb.shareLinks = mockDb.shareLinks.filter((item) => item.scheduleId !== numericScheduleId)
  return true
}
