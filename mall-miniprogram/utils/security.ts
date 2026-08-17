const PRIMARY_CODE_COOLDOWN_KEY = 'mall_primary_device_code_cooldown'

interface PrimaryCodeCooldown {
  expiresAt: number
}

function formatTime(value?: string) {
  if (!value) return '未知时间'
  const date = new Date(value)
  if (!Number.isFinite(date.getTime())) return '未知时间'
  const pad = (part: number) => String(part).padStart(2, '0')
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function normalizeSession(value: Partial<MallMemberSession>) {
  const deviceType = value.deviceType === 'MOBILE' ? 'MOBILE' : 'DESKTOP'
  return {
    sessionId: Number(value.sessionId || 0),
    deviceType,
    deviceName: String(value.deviceName || (deviceType === 'MOBILE' ? '移动设备' : '电脑设备')),
    deviceIcon: deviceType === 'MOBILE' ? '/assets/icons/smartphone.svg' : '/assets/icons/monitor.svg',
    primaryMobile: Boolean(value.primaryMobile),
    current: Boolean(value.current),
    loginIp: String(value.loginIp || '未知网络'),
    loginTimeLabel: formatTime(value.loginTime),
    lastActiveTimeLabel: formatTime(value.lastActiveTime),
    expireTimeLabel: formatTime(value.expireTime),
    sortTime: new Date(value.lastActiveTime || value.loginTime || 0).getTime() || 0
  }
}

function normalizeSessions(values?: Array<Partial<MallMemberSession>>) {
  return (values || []).map(normalizeSession).filter(value => value.sessionId > 0).sort((left, right) => {
    if (left.current !== right.current) return left.current ? -1 : 1
    if (left.primaryMobile !== right.primaryMobile) return left.primaryMobile ? -1 : 1
    return right.sortTime - left.sortTime
  })
}

function canReplacePrimary(sessions: ReturnType<typeof normalizeSessions>, remaining: number) {
  const current = sessions.find(value => value.current)
  return Boolean(current && current.deviceType === 'MOBILE' && !current.primaryMobile && remaining > 0)
}

function readPrimaryCodeCooldown(): PrimaryCodeCooldown | null {
  const stored = wx.getStorageSync(PRIMARY_CODE_COOLDOWN_KEY)
  if (!stored || typeof stored !== 'object') return null
  const expiresAt = Number((stored as Partial<PrimaryCodeCooldown>).expiresAt || 0)
  if (!Number.isFinite(expiresAt) || expiresAt <= Date.now()) {
    wx.removeStorageSync(PRIMARY_CODE_COOLDOWN_KEY)
    return null
  }
  return { expiresAt }
}

function startPrimaryCodeCooldown(resendAfter = 60) {
  const cooldown = { expiresAt: Date.now() + Math.max(1, Number(resendAfter) || 60) * 1000 }
  wx.setStorageSync(PRIMARY_CODE_COOLDOWN_KEY, cooldown)
  return cooldown
}

function clearPrimaryCodeCooldown() {
  wx.removeStorageSync(PRIMARY_CODE_COOLDOWN_KEY)
}

function getPrimaryCodeSeconds(cooldown: PrimaryCodeCooldown | null) {
  return cooldown ? Math.max(0, Math.ceil((cooldown.expiresAt - Date.now()) / 1000)) : 0
}

module.exports = {
  formatTime,
  normalizeSession,
  normalizeSessions,
  canReplacePrimary,
  readPrimaryCodeCooldown,
  startPrimaryCodeCooldown,
  clearPrimaryCodeCooldown,
  getPrimaryCodeSeconds
}
