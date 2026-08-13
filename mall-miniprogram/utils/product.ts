interface MallProductSkuInput {
  skuId: number
  skuCode?: string
  skuName?: string
  imageUrl?: string
  price?: number
  marketPrice?: number
  availableStock?: number
  status?: string
}

interface MallProductMediaInput {
  mediaUrl?: string
  mediaType?: string
  sortNo?: number
}

interface MallProductDetailInput {
  spuId: number
  productName: string
  subtitle?: string
  categoryName?: string
  brandName?: string
  mainImage?: string
  detailHtml?: string
  priceMin?: number
  priceMax?: number
  salesCount?: number
  skuList?: MallProductSkuInput[]
  mediaList?: MallProductMediaInput[]
}

interface MallProductReviewInput {
  reviewId: number
  reviewerName?: string
  rating?: number
  content?: string
  skuName?: string
  createTime?: string
}

const LOCAL_ASSETS = new Set([
  '/assets/backpack.jpg', '/assets/camera.jpg', '/assets/chair.jpg', '/assets/coffee.jpg',
  '/assets/headphones.jpg', '/assets/lamp.jpg', '/assets/sneaker.jpg', '/assets/watch.jpg'
])

function localProductImage(path?: string, fallback = '/assets/backpack.jpg') {
  return path && LOCAL_ASSETS.has(path) ? path : fallback
}

function formatPrice(value?: number) {
  const amount = Number(value || 0)
  return amount % 1 === 0 ? String(amount) : amount.toFixed(2)
}

function detailText(html?: string) {
  if (!html) return '商品详情以页面展示及实际收到的商品为准。'
  return html
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/p\s*>/gi, '\n')
    .replace(/<\/li\s*>/gi, '\n')
    .replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/gi, ' ')
    .replace(/&amp;/gi, '&')
    .replace(/&lt;/gi, '<')
    .replace(/&gt;/gi, '>')
    .replace(/&quot;/gi, '"')
    .replace(/&#39;/gi, "'")
    .replace(/\n{3,}/g, '\n\n')
    .trim() || '商品详情以页面展示及实际收到的商品为准。'
}

function stockState(sku: MallProductSkuInput) {
  const stock = Math.max(0, Number(sku.availableStock || 0))
  if (sku.status !== '1' || stock <= 0) return { stockTone: 'soldout', stockLabel: '暂时售罄' }
  if (stock <= 3) return { stockTone: 'limited', stockLabel: `库存紧张，仅剩 ${stock} 件` }
  return { stockTone: 'ready', stockLabel: `有货，可售 ${stock} 件` }
}

function normalizeSku(sku: MallProductSkuInput, fallbackImage?: string) {
  const stock = Math.max(0, Number(sku.availableStock || 0))
  return {
    ...sku,
    skuName: sku.skuName || sku.skuCode || '默认规格',
    availableStock: stock,
    displayImage: localProductImage(sku.imageUrl, localProductImage(fallbackImage)),
    displayPrice: formatPrice(sku.price),
    displayMarketPrice: Number(sku.marketPrice || 0) > Number(sku.price || 0) ? formatPrice(sku.marketPrice) : '',
    selectable: sku.status === '1',
    soldOut: sku.status !== '1' || stock <= 0,
    ...stockState(sku)
  }
}

function normalizeProductDetail(product: MallProductDetailInput) {
  const skus = (product.skuList || []).map(sku => normalizeSku(sku, product.mainImage))
  const selected = skus.find(sku => sku.selectable && !sku.soldOut) || skus.find(sku => sku.selectable) || skus[0] || null
  const galleryValues = [
    product.mainImage,
    ...(product.mediaList || []).filter(item => !item.mediaType || item.mediaType === 'IMAGE').map(item => item.mediaUrl),
    ...(product.skuList || []).map(item => item.imageUrl)
  ]
  const gallery = Array.from(new Set(galleryValues.filter(Boolean))).map(path => localProductImage(path))
  return {
    ...product,
    categoryBrand: [product.categoryName, product.brandName].filter(Boolean).join(' · '),
    displayImage: selected?.displayImage || localProductImage(product.mainImage),
    displayPrice: selected?.displayPrice || formatPrice(product.priceMin),
    displayMarketPrice: selected?.displayMarketPrice || '',
    detailText: detailText(product.detailHtml),
    salesText: `已售 ${Math.max(0, Number(product.salesCount || 0))}`,
    skus,
    selectedSkuId: selected?.skuId || 0,
    selectedSku: selected,
    gallery: gallery.length ? gallery : [localProductImage(product.mainImage)]
  }
}

function normalizeReviews(value: Record<string, any> | null | undefined) {
  const summary = value?.summary || {}
  return {
    summary: {
      reviewCount: Math.max(0, Number(summary.reviewCount || 0)),
      averageRating: Number(summary.averageRating || 0).toFixed(1),
      fiveStarCount: Math.max(0, Number(summary.fiveStarCount || 0)),
      fourStarCount: Math.max(0, Number(summary.fourStarCount || 0))
    },
    reviews: ((value?.reviews || []) as MallProductReviewInput[]).slice(0, 3).map(review => ({
      ...review,
      reviewerName: review.reviewerName || '匿名用户',
      stars: '★'.repeat(Math.max(0, Math.min(5, Number(review.rating || 0)))),
      displayDate: review.createTime ? String(review.createTime).slice(0, 10) : ''
    }))
  }
}

function normalizeCart(value: Record<string, any> | null | undefined) {
  const items = ((value?.items || []) as Array<Record<string, any>>).map(item => {
    const quantity = Math.max(1, Number(item.quantity || 1))
    const availableStock = Math.max(0, Number(item.availableStock || 0))
    const valid = item.valid !== false
    const stockShortage = item.stockShortage === true || availableStock < quantity
    return {
      ...item,
      quantity,
      availableStock,
      valid,
      stockShortage,
      selected: item.selectedFlag === '1',
      displayImage: localProductImage(item.productImage),
      displayPrice: formatPrice(item.price),
      displayLineAmount: formatPrice(item.lineAmount),
      statusLabel: !valid ? '商品已失效' : stockShortage ? `库存不足，仅剩 ${availableStock} 件` : availableStock <= 3 ? `库存紧张，仅剩 ${availableStock} 件` : '现货可售'
    }
  })
  return {
    items,
    totalCount: Math.max(0, Number(value?.totalCount || 0)),
    totalPrice: formatPrice(value?.totalPrice),
    canCheckout: Boolean(value?.canCheckout)
  }
}

module.exports = { normalizeProductDetail, normalizeReviews, normalizeSku, normalizeCart, localProductImage, detailText, formatPrice }
