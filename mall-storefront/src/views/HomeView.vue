<script setup>
import { computed, onMounted, ref } from 'vue'
import { getCategories, getProducts } from '../api/catalog'
import StoreProductCard from '../components/StoreProductCard.vue'

const categories = ref([])
const products = ref([])
const loading = ref(true)
const message = ref('')
const categoryVisuals = {
  家居: { image: '/assets/lamp.jpg', note: '把空间收拾得更顺手' },
  数码: { image: '/assets/camera.jpg', note: '记录，也沉浸其中' },
  穿搭: { image: '/assets/sneaker.jpg', note: '走得更远一点' },
  咖啡: { image: '/assets/coffee.jpg', note: '慢慢开始一天' },
  出行: { image: '/assets/backpack.jpg', note: '轻装，也装得下' }
}
const featured = computed(() => products.value.slice(0, 8))
const featuredCategories = computed(() => categories.value
  .filter(item => categoryVisuals[item.categoryName])
  .slice(0, 5)
  .map(item => ({ ...item, ...categoryVisuals[item.categoryName] })))

async function loadHome() {
  loading.value = true
  message.value = ''
  try {
    const [categoryResponse, productResponse] = await Promise.all([
      getCategories(), getProducts({ sort: 'sales' })
    ])
    categories.value = categoryResponse.data.data || []
    products.value = productResponse.data.data || []
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
    <section class="hero-strip">
      <div class="hero-copy"><p class="eyebrow">本周陈列 · 日常精选</p><h1>挑一些，<br /><em>让日子更顺手的东西。</em></h1><p class="hero-note">从家居、穿搭到随身器物，精选耐用、好看且愿意反复使用的商品。</p><router-link class="primary-button" :to="{ path: '/catalog', query: { sort: 'sales' } }">查看热销好物 <span>→</span></router-link></div>
      <router-link class="hero-visual" to="/product/2"><img src="/assets/lamp.jpg" alt="暖光阅读台灯" /><span>暖光阅读台灯 · 查看详情</span></router-link>
    </section>
    <section class="category-row"><div><span class="section-kicker">分类浏览</span><h2>从生活的不同角落开始</h2></div><router-link to="/catalog" class="text-link">查看全部分类 →</router-link></section>
    <div v-if="featuredCategories.length" class="category-showcase"><router-link v-for="category in featuredCategories" :key="category.categoryId" class="category-tile" :to="{ path: '/catalog', query: { categoryId: category.categoryId } }"><img :src="category.image" :alt="category.categoryName" /><span><strong>{{ category.categoryName }}</strong><small>{{ category.note }}</small></span></router-link></div>
    <section class="product-section"><div class="section-heading"><div><span class="section-kicker">热销商品</span><h2>最近大家都在认真挑的</h2></div><router-link :to="{ path: '/catalog', query: { sort: 'sales' } }" class="text-link">按销量查看 →</router-link></div><div v-if="loading" class="product-grid"><div v-for="item in 4" :key="item" class="product-skeleton"><span></span><i></i><b></b></div></div><div v-else-if="featured.length" class="product-grid"><StoreProductCard v-for="(product, index) in featured" :key="product.spuId" :product="product" :badge="index === 0 ? '本周热销' : ''" /></div><div v-else class="empty-state compact"><h2>精选商品正在整理</h2><p>{{ message || '请稍后刷新，或先浏览全部商品。' }}</p><router-link class="primary-button" to="/catalog">查看全部商品</router-link></div></section>
    <section class="service-row"><div><strong>安心选购</strong><span>7天无理由退换</span></div><div><strong>快速发货</strong><span>现货 48 小时内发出</span></div><div><strong>人工客服</strong><span>工作日 9:00—18:00</span></div></section>
  </section>
</template>
