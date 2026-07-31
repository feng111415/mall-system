<script setup>
import { computed, onMounted, ref } from 'vue'
import { getProducts } from '../api/catalog'
import StoreProductCard from '../components/StoreProductCard.vue'

const products = ref([])
const loading = ref(true)
const message = ref('')
const featuredSpuIds = [2, 3, 8, 7]
const featured = computed(() => featuredSpuIds
  .map(spuId => products.value.find(product => Number(product.spuId) === spuId))
  .filter(Boolean))

async function loadHome() {
  loading.value = true
  message.value = ''
  try {
    const response = await getProducts({ sort: 'sales' })
    products.value = response.data.data || []
  } catch (error) {
    message.value = error.response?.data?.msg || '精选商品读取失败，请稍后刷新重试'
  } finally {
    loading.value = false
  }
}

onMounted(loadHome)
</script>

<template>
  <section class="home-wrap">
    <section class="hero-c">
      <div class="hero-c-copy"><span>NEW DROP</span><h1>今天，也要<br />挑点喜欢的。</h1><p>好看、好用，还能让普通一天有一点新鲜。</p><router-link class="hero-c-action" :to="{ path: '/catalog', query: { sort: 'sales' } }">开始逛逛 <span aria-hidden="true">✦</span></router-link></div>
      <div class="hero-c-collage"><router-link to="/product/6"><img class="collage-main" src="/assets/sneaker.jpg" alt="城市运动鞋" /></router-link><router-link to="/product/4"><img class="collage-small" src="/assets/headphones.jpg" alt="静音头戴耳机" /></router-link><span>本周上新<br /><b>08</b> 件</span></div>
    </section>
    <section class="ticker"><span>FREE SHIPPING</span><b>满 199 元免运费</b><span>NEW ARRIVALS</span><b>每周二上新</b><span>EASY RETURN</span><b>7 天轻松退换</b></section>
    <section class="product-section"><div class="section-heading"><div><span class="section-kicker">HOT PICKS</span><h2>大家正在买</h2></div><router-link :to="{ path: '/catalog', query: { sort: 'sales' } }" class="text-link">再看更多 →</router-link></div><div v-if="loading" class="product-grid"><div v-for="item in 4" :key="item" class="product-skeleton"><span></span><i></i><b></b></div></div><div v-else-if="featured.length" class="product-grid"><StoreProductCard v-for="(product, index) in featured" :key="product.spuId" :product="product" :badge="index === 0 ? '本周热销' : ''" /></div><div v-else class="empty-state compact"><h2>精选商品正在整理</h2><p>{{ message || '请稍后刷新，或先浏览全部商品。' }}</p><router-link class="primary-button" to="/catalog">查看全部商品</router-link></div></section>
  </section>
</template>
