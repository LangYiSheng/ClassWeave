import { computed } from 'vue'

import { DAY_NAMES } from '@/config/app'
import { formatDisplayTime, getDayNameByDate, toMinuteValue } from '@/utils/time'

function getCourseTimeRange(course, timeSlots) {
  const startSlot = timeSlots.find((slot) => slot.periodIndex === Number(course.startPeriod))
  const endSlot = timeSlots.find((slot) => slot.periodIndex === Number(course.endPeriod))

  return {
    startTime: startSlot?.startTime ?? '00:00:00',
    endTime: endSlot?.endTime ?? '00:00:00',
  }
}

export function buildStatusPanel(detailsList, currentDate, currentTime, windowMinutes) {
  const weekday = DAY_NAMES.indexOf(getDayNameByDate(currentDate)) + 1
  const currentMinuteValue = toMinuteValue(currentTime)

  const groups = {
    current: [],
    free: [],
    soonStart: [],
    soonEnd: [],
  }

  detailsList.forEach((detail) => {
    const summary = detail.schedule
    const todayCourses = detail.courses
      .filter((course) => Number(course.weekday) === weekday)
      .map((course) => {
        const range = getCourseTimeRange(course, detail.timeSlots)
        return {
          ...course,
          ...range,
          startMinuteValue: toMinuteValue(range.startTime),
          endMinuteValue: toMinuteValue(range.endTime),
        }
      })
      .sort((left, right) => left.startMinuteValue - right.startMinuteValue)

    const currentCourse = todayCourses.find(
      (course) =>
        currentMinuteValue >= course.startMinuteValue && currentMinuteValue < course.endMinuteValue,
    )
    const nextCourse = todayCourses.find((course) => course.startMinuteValue > currentMinuteValue)

    if (currentCourse) {
      groups.current.push({
        scheduleId: summary.id,
        scheduleName: summary.name,
        ownerDisplayName: summary.ownerDisplayName,
        courseId: currentCourse.id,
        courseName: currentCourse.name,
        startTime: formatDisplayTime(currentCourse.startTime),
        endTime: formatDisplayTime(currentCourse.endTime),
        location: currentCourse.location,
      })

      if (currentCourse.endMinuteValue - currentMinuteValue <= windowMinutes) {
        groups.soonEnd.push({
          scheduleId: summary.id,
          scheduleName: summary.name,
          ownerDisplayName: summary.ownerDisplayName,
          courseId: currentCourse.id,
          courseName: currentCourse.name,
          endTime: formatDisplayTime(currentCourse.endTime),
          minutesLeft: currentCourse.endMinuteValue - currentMinuteValue,
        })
      }
    } else {
      groups.free.push({
        scheduleId: summary.id,
        scheduleName: summary.name,
        ownerDisplayName: summary.ownerDisplayName,
      })
    }

    if (nextCourse && nextCourse.startMinuteValue - currentMinuteValue <= windowMinutes) {
      groups.soonStart.push({
        scheduleId: summary.id,
        scheduleName: summary.name,
        ownerDisplayName: summary.ownerDisplayName,
        courseId: nextCourse.id,
        courseName: nextCourse.name,
        startTime: formatDisplayTime(nextCourse.startTime),
        minutesLeft: nextCourse.startMinuteValue - currentMinuteValue,
      })
    }
  })

  return groups
}

export function useWeeklyLoadChart(detailsList) {
  return computed(() => {
    const source = Array.isArray(detailsList) ? detailsList : detailsList.value ?? []

    return source.map((detail) => {
      const dayBuckets = DAY_NAMES.map((dayName, index) => ({
        dayName,
        totalPeriods: detail.courses
          .filter((course) => Number(course.weekday) === index + 1)
          .reduce((total, course) => total + Number(course.endPeriod) - Number(course.startPeriod) + 1, 0),
      }))

      return {
        scheduleId: detail.schedule.id,
        scheduleName: detail.schedule.displayNameOverride ?? detail.schedule.name,
        color: detail.schedule.displayColor ?? detail.schedule.defaultColor,
        data: dayBuckets,
      }
    })
  })
}
