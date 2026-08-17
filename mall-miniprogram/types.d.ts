/// <reference types="miniprogram-api-typings" />

interface MallMemberProfile {
  memberId: number
  maskedPhone: string
  nickname: string
  avatar?: string
  nicknameChangesRemaining?: number
  nicknameChangeWindowDays?: number
  lastLoginTime?: string
}

interface MallAvatarPreset {
  code: string
  name: string
  url: string
}

interface MallMemberSession {
  sessionId: number
  deviceType: 'MOBILE' | 'DESKTOP'
  deviceName: string
  primaryMobile: boolean
  current: boolean
  loginIp?: string
  loginTime?: string
  lastActiveTime?: string
  expireTime?: string
}

interface MallMemberSessionOverview {
  sessions: MallMemberSession[]
  primaryChangesRemaining: number
  primaryChangeWindowDays: number
}

interface MallHomeProduct {
  spuId: number
  productName: string
  subtitle?: string
  displayImage: string
  displayPrice: string
  salesCount?: number
  badge?: string
}

interface IAppOption {
  globalData: {
    config: Record<string, unknown>
    request: (options: { url: string; method?: string; data?: Record<string, unknown> }) => Promise<unknown>
    member: MallMemberProfile | null
  }
}
