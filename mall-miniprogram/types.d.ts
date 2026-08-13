/// <reference types="miniprogram-api-typings" />

interface MallMemberProfile {
  memberId: number
  maskedPhone: string
  nickname: string
  avatar?: string
  lastLoginTime?: string
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
