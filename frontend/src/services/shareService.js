import { apiClient } from '@/services/http'

export async function fetchShareLinks(ownerSchedules) {
  if (!ownerSchedules.length) {
    return []
  }

  const responses = await Promise.all(
    ownerSchedules.map((schedule) => apiClient.get(`/schedules/${schedule.id}/share-links`)),
  )

  return responses.flatMap((response, index) =>
    response.data.data.map((link) => ({
      ...link,
      scheduleName: ownerSchedules[index].name,
    })),
  )
}

export async function generateShareLink(scheduleId, expiresAt = null) {
  const response = await apiClient.post(`/schedules/${scheduleId}/share-links`, { expiresAt })

  return response.data.data
}

export async function deactivateShareLink(shareLinkId) {
  const response = await apiClient.delete(`/share-links/${shareLinkId}`)

  return response.data.data
}

export async function acceptShare(shareToken) {
  const response = await apiClient.post(`/shares/${shareToken}/accept`)

  return response.data.data
}
