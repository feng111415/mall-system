const PRESET_AVATAR = /^\/assets\/avatars\/avatar-(berry|coral|graphite|mint|sky|sunny)\.svg$/

function normalizeNickname(value?: string) {
  return String(value || '').trim().replace(/\s+/g, ' ')
}

function validateNickname(value?: string) {
  const nickname = normalizeNickname(value)
  if (nickname.length < 2 || nickname.length > 20) return '昵称长度需为 2 至 20 个字符'
  return ''
}

function resolveAvatarUrl(value?: string, apiBaseUrl = '') {
  const avatar = String(value || '').trim()
  if (PRESET_AVATAR.test(avatar)) return avatar
  if (/^https?:\/\//i.test(avatar)) return avatar
  if (avatar.startsWith('/profile/') && /^https?:\/\/[^/]+/i.test(apiBaseUrl)) {
    return `${apiBaseUrl.replace(/\/$/, '')}${avatar}`
  }
  return ''
}

module.exports = { normalizeNickname, validateNickname, resolveAvatarUrl }
