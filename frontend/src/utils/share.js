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

  const basePath = import.meta.env.BASE_URL || '/'
  const normalizedBasePath = basePath.endsWith('/') ? basePath.slice(0, -1) : basePath
  const sharePath = `${normalizedBasePath}/share/${token}`.replace(/\/{2,}/g, '/')

  if (typeof window === 'undefined') {
    return sharePath
  }

  return `${window.location.origin}${sharePath}`
}
