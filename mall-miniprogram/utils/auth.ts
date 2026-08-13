const SMS_COOLDOWN_KEY = 'mall_sms_code_cooldown'
const DEFAULT_RESEND_SECONDS = 60

interface SmsCooldown {
  phone: string
  expiresAt: number
}

function readSmsCooldown(): SmsCooldown | null {
  const stored = wx.getStorageSync(SMS_COOLDOWN_KEY)
  if (!stored || typeof stored !== 'object') return null
  const value = stored as Partial<SmsCooldown>
  if (!/^1[3-9]\d{9}$/.test(value.phone || '') || !Number.isFinite(value.expiresAt) || Number(value.expiresAt) <= Date.now()) {
    wx.removeStorageSync(SMS_COOLDOWN_KEY)
    return null
  }
  return { phone: value.phone as string, expiresAt: Number(value.expiresAt) }
}

function startSmsCooldown(phone: string, resendAfter = DEFAULT_RESEND_SECONDS) {
  const cooldown = { phone, expiresAt: Date.now() + Math.max(1, resendAfter) * 1000 }
  wx.setStorageSync(SMS_COOLDOWN_KEY, cooldown)
  return cooldown
}

function clearSmsCooldown() {
  wx.removeStorageSync(SMS_COOLDOWN_KEY)
}

function getCooldownSeconds(cooldown: SmsCooldown | null) {
  return cooldown ? Math.max(0, Math.ceil((cooldown.expiresAt - Date.now()) / 1000)) : 0
}

module.exports = { readSmsCooldown, startSmsCooldown, clearSmsCooldown, getCooldownSeconds }
