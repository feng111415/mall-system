<script setup>
import { computed, onMounted, ref } from 'vue'
import { getHomepage, getProducts } from '../api/catalog'
import StoreProductCard from '../components/StoreProductCard.vue'

const products = ref([])
const homepage = ref({})
const loading = ref(true)
const message = ref('')
const fallback = {
  hero: { heroEyebrow: 'NEW DROP', heroTitle: '今天，也要挑点喜欢的。', heroSubtitle: '好看、好用，还能让普通一天有一点新鲜。', heroActionText: '开始逛逛', heroTargetValue: '/catalog', heroPrimaryImage: '/assets/sneaker.jpg', heroSecondaryImage: '/assets/headphones.jpg', heroNewItemCount: 8 },
  ticker: { tickerItems: [{ label: 'FREE SHIPPING', text: '满 199 元免运费' }, { label: 'NEW ARRIVALS', text: '每周二上新' }, { label: 'EASY RETURN', text: '7 天轻松退换' }] },
  recommendation: { recommendationKicker: 'HOT PICKS', recommendationTitle: '大家正在买', recommendationSpuIds: [2, 3, 8, 7], recommendationDisplayCount: 4 }
}
const hero = computed(() => homepage.value.hero || fallback.hero)
const tickerItems = computed(() => homepage.value.ticker?.tickerItems?.length ? homepage.value.ticker.tickerItems : fallback.ticker.tickerItems)
const recommendation = computed(() => homepage.value.recommendation || fallback.recommendation)
const featuredSpuIds = computed(() => (recommendation.value.recommendationSpuIds || []).map(Number))
const featured = computed(() => {
  const count = Number(recommendation.value.recommendationDisplayCount || 4)
  const selected = featuredSpuIds.value.map(id => products.value.find(product => Number(product.spuId) === id)).filter(Boolean)
  const selectedIds = new Set(selected.map(product => Number(product.spuId)))
  const salesFallback = products.value.filter(product => !selectedIds.has(Number(product.spuId)))
  return [...selected, ...salesFallback].slice(0, count)
})

async function loadHome() {
  loading.value = true
  message.value = ''
  try {
    const [homeResponse, productResponse] = await Promise.all([getHomepage(), getProducts({ sort: 'sales' })])
    homepage.value = homeResponse.data.data || {}
    products.value = productResponse.data.data || []
  } catch (error) {
    message.value = error.response?.data?.msg || '精选商品读取失败，请稍后刷新重试'
    try { const response = await getProducts({ sort: 'sales' }); products.value = response.data.data || [] } catch { /* fallback remains visible */ }
  } finally {
    loading.value = false
  }
}

onMounted(loadHome)
</script>

<template>
  <section class="home-wrap">
    <section class="hero-c">
      <div class="hero-c-copy"><span>{{ hero.heroEyebrow }}</span><h1>{{ hero.heroTitle || fallback.hero.heroTitle }}</h1><p>{{ hero.heroSubtitle || fallback.hero.heroSubtitle }}</p><router-link class="hero-c-action" :to="hero.heroTargetValue || '/catalog'">{{ hero.heroActionText || '开始逛逛' }} <span aria-hidden="true">✦</span></router-link></div>
      <div class="hero-c-collage"><router-link to="/product/6"><img class="collage-main" :src="hero.heroPrimaryImage || fallback.hero.heroPrimaryImage" alt="主视觉商品" /></router-link><router-link to="/product/4"><img class="collage-small" :src="hero.heroSecondaryImage || fallback.hero.heroSecondaryImage" alt="主视觉商品" /></router-link><span>本周上新<br /><b>{{ hero.heroNewItemCount || 8 }}</b> 件</span></div>
    </section>
    <section class="ticker" aria-label="公告条"><div v-for="item in tickerItems" :key="item.label + item.text" class="ticker-item"><span>{{ item.label }}</span><b>{{ item.text }}</b></div></section>
    <section class="product-section"><div class="section-heading"><div><span class="section-kicker">{{ recommendation.recommendationKicker || 'HOT PICKS' }}</span><h2>{{ recommendation.recommendationTitle || '大家正在买' }}</h2></div><router-link :to="{ path: '/catalog', query: { sort: 'sales' } }" class="text-link">再看更多 →</router-link></div><div v-if="loading" class="product-grid"><div v-for="item in 4" :key="item" class="product-skeleton"><span></span><i></i><b></b></div></div><div v-else-if="featured.length" class="product-grid"><StoreProductCard v-for="(product, index) in featured" :key="product.spuId" :product="product" :badge="index === 0 ? '本周热销' : ''" /></div><div v-else class="empty-state compact"><h2>精选商品正在整理</h2><p>{{ message || '请稍后刷新，或先浏览全部商品。' }}</p><router-link class="primary-button" to="/catalog">查看全部商品</router-link></div></section>
  </section>
</template>
