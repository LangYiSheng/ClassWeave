export function extractShareToken(input = '') {
  const value = String(input).trim()

  if (!value) {
    return ''
  }

  const matchedFromUrl = value.match(/(?:^|\/)share\/([^/?#]+)/i)
  if (matchedFromUrl) {
    return decodeURIComponent(matchedFromUrl[1])
  }

  return value.replace(/^\/+|\/+$/g, '')
}

export function buildShareUrl(shareToken) {
  const token = String(shareToken).trim()

  if (!token) {
    return ''
  }

  if (typeof window === 'undefined') {
    return `/share/${token}`
  }

  return `${window.location.origin}/share/${token}`
}
