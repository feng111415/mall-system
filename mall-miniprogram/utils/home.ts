interface HomepageContent {
  hero?: MallHomeHero | null
  tickerItems?: MallHomeTicker[] | null
  recommendation?: MallHomeRecommendation | null
  recommendationSpuIds?: number[] | null
}

interface MallHomeHero {
  heroEyebrow?: string
  heroTitle?: string
  heroSubtitle?: string
  heroActionText?: string
  heroTargetValue?: string
  heroPrimaryImage?: string
  heroNewItemCount?: number
}

interface MallHomeTicker {
  label?: string
  text?: string
}

interface MallHomeRecommendation {
  recommendationKicker?: string
  recommendationTitle?: string
  recommendationDisplayCount?: number
  recommendationSpuIds?: number[]
}

interface MallCatalogProduct {
  spuId: number
  productName: string
  subtitle?: string
  mainImage?: string
  priceMin?: number
  priceMax?: number
  salesCount?: number
}

interface MallOrderSummaryItem {
  status?: string
  displayStatus?: string
}

const DEFAULT_HERO = {
  heroEyebrow: '限时推荐',
  heroTitle: '通勤装备\n一站配齐',
  heroSubtitle: '好看好用，为每天出发多一点新鲜感。',
  heroActionText: '去选购',
  heroTargetValue: '/catalog',
  heroPrimaryImage: '/assets/backpack.jpg',
  heroNewItemCount: 0
}

const LOCAL_ASSETS = new Set([
  '/assets/backpack.jpg', '/assets/camera.jpg', '/assets/chair.jpg', '/assets/coffee.jpg',
  '/assets/headphones.jpg', '/assets/lamp.jpg', '/assets/sneaker.jpg', '/assets/watch.jpg'
])

function localAsset(path: string | undefined, fallback = '/assets/backpack.jpg') {
  return path && LOCAL_ASSETS.has(path) ? path : fallback
}

function formatPrice(value: number | undefined) {
  const amount = Number(value || 0)
  return amount % 1 === 0 ? String(amount) : amount.toFixed(2)
}

function selectProducts(content: HomepageContent, products: MallCatalogProduct[]) {
  const recommendation = content.recommendation || {}
  const ids = (recommendation.recommendationSpuIds || content.recommendationSpuIds || []).map(Number)
  const selected = ids.map(id => products.find(item => Number(item.spuId) === id)).filter(Boolean) as MallCatalogProduct[]
  const selectedIds = new Set(selected.map(item => Number(item.spuId)))
  const fallback = products.filter(item => !selectedIds.has(Number(item.spuId)))
  const count = Math.min(6, Math.max(1, Number(recommendation.recommendationDisplayCount || 4)))
  return [...selected, ...fallback].slice(0, count).map((item, index) => ({
    ...item,
    displayImage: localAsset(item.mainImage),
    displayPrice: formatPrice(item.priceMin),
    badge: index === 0 ? '本周热销' : ''
  }))
}

function summarizeOrders(orders: MallOrderSummaryItem[]) {
  const summary = { pendingPayment: 0, pendingShipment: 0, pendingReceipt: 0, pendingReview: 0 }
  orders.forEach(order => {
    const status = order.displayStatus || order.status
    if (status === 'PENDING_PAYMENT') summary.pendingPayment += 1
    if (status === 'PENDING_SHIPMENT') summary.pendingShipment += 1
    if (status === 'SHIPPED') summary.pendingReceipt += 1
    if (status === 'COMPLETED') summary.pendingReview += 1
  })
  return summary
}

function normalizeHome(content: HomepageContent, products: MallCatalogProduct[]) {
  const hero = content.hero || {}
  const recommendation = content.recommendation || {}
  return {
    hero: {
      ...DEFAULT_HERO,
      ...hero,
      heroEyebrow: hero.heroEyebrow || DEFAULT_HERO.heroEyebrow,
      heroTitle: hero.heroTitle || DEFAULT_HERO.heroTitle,
      heroSubtitle: hero.heroSubtitle || DEFAULT_HERO.heroSubtitle,
      heroActionText: hero.heroActionText || DEFAULT_HERO.heroActionText,
      heroTargetValue: hero.heroTargetValue || DEFAULT_HERO.heroTargetValue,
      displayImage: localAsset(hero.heroPrimaryImage)
    },
    tickers: (content.tickerItems || []).filter(item => item.label || item.text),
    recommendationKicker: recommendation.recommendationKicker || 'FOR YOU',
    recommendationTitle: recommendation.recommendationTitle || '猜你喜欢',
    products: selectProducts(content, products)
  }
}

module.exports = { normalizeHome, summarizeOrders, localAsset, formatPrice }
