<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProduct, getProducts } from '../api/catalog'
import { getProductReviews } from '../api/review'
import { useCartStore } from '../stores/cart'
import { useNoticeStore } from '../stores/notice'
import StoreProductCard from '../components/StoreProductCard.vue'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const notice = useNoticeStore()
const product = ref(null)
const related = ref([])
const selected = ref(null)
const selectedImage = ref('')
const quantity = ref(1)
const loading = ref(true)
const relatedLoading = ref(false)
const reviewLoading = ref(false)
const reviewView = ref({ summary: {}, reviews: [] })
const busy = ref(false)
const message = ref('')

const availableStock = computed(() => Number(selected.value?.availableStock || 0))
const isSoldOut = computed(() => !selected.value || selected.value.status !== '1' || availableStock.value <= 0)
const stockLabel = computed(() => {
  if (isSoldOut.value) return '已售罄，暂时不能加购'
  if (availableStock.value <= 3) return `库存紧张，仅剩 ${availableStock.value} 件`
  return `有货，可售 ${availableStock.value} 件`
})
const stockTone = computed(() => isSoldOut.value ? 'soldout' : availableStock.value <= 3 ? 'limited' : 'ready')
const price = computed(() => Number(selected.value?.price || product.value?.priceMin || 0).toLocaleString())
const galleryImages = computed(() => {
  const values = [product.value?.mainImage, ...(product.value?.mediaList || []).map(item => item.mediaUrl), ...(product.value?.skuList || []).map(item => item.imageUrl)]
  return [...new Set(values.filter(Boolean))]
})
const safeDetailHtml = computed(() => sanitizeHtml(product.value?.detailHtml || '<p>这件商品适合慢慢挑选，详情以页面展示为准。</p>'))

function sanitizeHtml(raw) {
  if (!raw || typeof DOMParser === 'undefined') return ''
  const template = document.createElement('template')
  template.innerHTML = raw
  const allowed = new Set(['P', 'BR', 'STRONG', 'EM', 'UL', 'OL', 'LI'])
  template.content.querySelectorAll('*').forEach(node => {
    if (!allowed.has(node.tagName)) {
      node.replaceWith(...node.childNodes)
      return
    }
    Array.from(node.attributes).forEach(attribute => node.removeAttribute(attribute.name))
  })
  return template.innerHTML
}

function selectSku(sku) {
  if (!sku || sku.status !== '1') return
  selected.value = sku
  quantity.value = Math.min(quantity.value, Number(sku.availableStock || 0)) || 1
  selectedImage.value = sku.imageUrl || product.value?.mainImage || ''
  message.value = ''
}

function changeQuantity(delta) {
  if (isSoldOut.value) return
  quantity.value = Math.min(availableStock.value, Math.max(1, quantity.value + delta))
}

async function loadProduct() {
  loading.value = true
  product.value = null
  message.value = ''
  try {
    const response = await getProduct(route.params.id)
    product.value = response.data.data
    const firstAvailable = product.value?.skuList?.find(item => item.status === '1' && Number(item.availableStock) > 0)
      || product.value?.skuList?.find(item => item.status === '1')
      || product.value?.skuList?.[0]
    selectSku(firstAvailable)
    selectedImage.value = firstAvailable?.imageUrl || product.value?.mainImage || ''
    await loadRelated()
    await loadReviews()
    rememberProduct(product.value)
  } catch (error) {
    message.value = error.response?.data?.msg || '商品信息读取失败，请确认后端与数据库已启动'
  } finally {
    loading.value = false
  }
}

async function loadRelated() {
  if (!product.value?.categoryId) return
  relatedLoading.value = true
  try {
    related.value = ((await getProducts({ categoryId: product.value.categoryId, sort: 'sales' })).data.data || [])
      .filter(item => item.spuId !== product.value.spuId)
      .slice(0, 3)
  } catch { related.value = [] }
  finally { relatedLoading.value = false }
}

async function loadReviews() {
  if (!product.value?.spuId) return
  reviewLoading.value = true
  try { reviewView.value = (await getProductReviews(product.value.spuId, { limit: 8 })).data.data || { summary: {}, reviews: [] } }
  catch { reviewView.value = { summary: {}, reviews: [] } }
  finally { reviewLoading.value = false }
}

function rememberProduct(value) {
  if (!value?.spuId) return
  try {
    const previous = JSON.parse(localStorage.getItem('mall-recent-products') || '[]')
    const next = [value.spuId, ...previous.filter(id => id !== value.spuId)].slice(0, 8)
    localStorage.setItem('mall-recent-products', JSON.stringify(next))
  } catch { /* 浏览器禁用本地存储时不影响购买 */ }
}

async function addToCart() {
  if (isSoldOut.value || busy.value) return
  busy.value = true
  message.value = ''
  try {
    await cart.add({ skuId: selected.value.skuId }, quantity.value)
    notice.show(`${product.value.productName} × ${quantity.value} 已加入购物车`)
  } catch (error) {
    message.value = error.response?.data?.msg || '加入购物车失败，请先登录'
    notice.show(message.value, 'error')
  } finally { busy.value = false }
}

function chooseAnother() { router.push('/catalog') }
function reviewStars(rating) { return '★'.repeat(Number(rating || 0)) + '☆'.repeat(Math.max(0, 5 - Number(rating || 0))) }
function reviewDate(value) { return value ? new Date(value).toLocaleDateString('zh-CN') : '' }

watch(() => route.params.id, loadProduct)
onMounted(loadProduct)
</script>

<template>
  <section class="catalog-wrap detail-page">
    <div v-if="loading" class="detail-loading"><div class="detail-loading-image"></div><div><span></span><i></i><b></b><em></em></div></div>
    <template v-else-if="product">
      <div class="detail-layout">
        <div class="detail-gallery">
          <div class="detail-image"><img :src="selectedImage || product.mainImage || '/assets/chair.jpg'" :alt="product.productName" /></div>
          <div v-if="galleryImages.length > 1" class="detail-thumbnails"><button v-for="image in galleryImages" :key="image" :class="{ active: (selectedImage || product.mainImage) === image }" type="button" @click="selectedImage = image"><img :src="image" alt="商品图片缩略图" /></button></div>
        </div>
        <div class="detail-copy">
          <span class="section-kicker">{{ product.categoryName }} <template v-if="product.brandName">· {{ product.brandName }}</template></span>
          <h1>{{ product.productName }}</h1>
          <p class="detail-subtitle">{{ product.subtitle }}</p>
          <div class="detail-price-row"><strong class="detail-price">¥{{ price }}</strong><del v-if="selected?.marketPrice && selected.marketPrice > selected.price">¥{{ Number(selected.marketPrice).toLocaleString() }}</del></div>
          <div class="detail-option"><div class="detail-option-heading"><span>选择规格</span><small>{{ selected?.skuName || '请选择' }}</small></div><div class="sku-list"><button v-for="sku in product.skuList" :key="sku.skuId" :class="{ active: selected?.skuId === sku.skuId, unavailable: Number(sku.availableStock) <= 0 }" :disabled="sku.status !== '1'" type="button" @click="selectSku(sku)">{{ sku.skuName || sku.skuCode }}<small v-if="Number(sku.availableStock) <= 0">售罄</small></button></div></div>
          <div :class="['stock-note', stockTone]"><span class="stock-dot"></span>{{ stockLabel }}</div>
          <div class="detail-buy-row"><div class="quantity detail-quantity"><button type="button" aria-label="减少数量" :disabled="isSoldOut || quantity <= 1 || busy" @click="changeQuantity(-1)">−</button><span>{{ quantity }}</span><button type="button" aria-label="增加数量" :disabled="isSoldOut || quantity >= availableStock || busy" @click="changeQuantity(1)">＋</button></div><button class="primary-button detail-add-button" type="button" :disabled="isSoldOut || busy" @click="addToCart">{{ busy ? '正在加入...' : isSoldOut ? '暂时售罄' : '加入购物车' }} <span>→</span></button></div>
          <p v-if="message" class="form-message">{{ message }}</p>
          <div class="detail-service"><span>7天无理由退换</span><span>现货 48 小时内发出</span><span>库存下单时再次确认</span></div>
        </div>
      </div>
      <section class="detail-description"><div><span class="section-kicker">商品详情</span><h2>关于这件日常好物</h2></div><div class="detail-richtext" v-html="safeDetailHtml"></div></section>
      <section class="detail-reviews">
        <div class="detail-reviews-heading"><div><span class="section-kicker">买家评价</span><h2>大家怎么说</h2></div><span v-if="reviewView.summary?.reviewCount">{{ reviewView.summary.reviewCount }} 条已审核评价</span></div>
        <div class="review-summary"><strong>{{ Number(reviewView.summary?.averageRating || 0).toFixed(1) }}</strong><div><span class="review-stars">{{ reviewStars(Math.round(Number(reviewView.summary?.averageRating || 0))) }}</span><small>综合评分</small></div><div class="review-summary-count"><span>五星 {{ reviewView.summary?.fiveStarCount || 0 }}</span><span>四星 {{ reviewView.summary?.fourStarCount || 0 }}</span></div></div>
        <div v-if="reviewLoading" class="loading-note">正在读取评价...</div>
        <div v-else-if="reviewView.reviews?.length" class="review-list">
          <article v-for="review in reviewView.reviews" :key="review.reviewId" class="review-card"><header><div><strong>{{ review.reviewerName || '匿名用户' }}</strong><span class="review-stars">{{ reviewStars(review.rating) }}</span></div><time>{{ reviewDate(review.createTime) }}</time></header><p>{{ review.content }}</p><div v-if="review.imageUrls?.length" class="review-images"><img v-for="image in review.imageUrls" :key="image" :src="image" alt="评价图片" /></div><small v-if="review.skuName" class="review-sku">规格：{{ review.skuName }}</small></article>
        </div>
        <p v-else class="review-empty">还没有公开评价，确认收货后欢迎分享你的体验。</p>
      </section>
      <section v-if="relatedLoading || related.length" class="related-section"><div class="section-heading"><div><span class="section-kicker">你可能还喜欢</span><h2>同一类的其他选择</h2></div><router-link to="/catalog" class="text-link">返回商品列表 →</router-link></div><div v-if="relatedLoading" class="loading-note">正在寻找相近商品...</div><div v-else class="product-grid related-grid"><StoreProductCard v-for="item in related" :key="item.spuId" :product="item" /></div></section>
    </template>
    <div v-else class="empty-state"><h2>暂时无法查看商品</h2><p>{{ message }}</p><button class="primary-button" type="button" @click="chooseAnother">返回商品列表</button></div>
  </section>
</template>
