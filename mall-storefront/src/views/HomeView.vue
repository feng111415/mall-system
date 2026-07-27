<script setup>
import { ref } from 'vue'
import { useCartStore } from '../stores/cart'

const cart = useCartStore()
const notice = ref('')
const products = [
  { id: 3, name: '极简复古腕表', category: '腕表配饰', price: 528, oldPrice: 699, image: '/assets/watch.jpg', stock: 12, tag: '今日精选' },
  { id: 4, name: '便携无反相机', category: '影像器材', price: 3499, oldPrice: 3799, image: '/assets/camera.jpg', stock: 4, tag: '编辑推荐' },
  { id: 5, name: '静音头戴耳机', category: '数码音频', price: 699, oldPrice: 799, image: '/assets/headphones.jpg', stock: 0, tag: '暂时售罄' },
  { id: 6, name: '手冲咖啡套装', category: '居家生活', price: 188, oldPrice: 239, image: '/assets/coffee.jpg', stock: 20, tag: '新上架' }
]
async function addProduct(product) {
  if (!product.skuId) { notice.value = '请进入商品详情选择规格后加购'; window.setTimeout(() => { notice.value = '' }, 2200); return }
  try { await cart.add(product); notice.value = `${product.name} 已加入购物车` }
  catch (error) { notice.value = error.response?.data?.msg || '加入购物车失败，请先登录' }
  window.setTimeout(() => { notice.value = '' }, 2200)
}
</script>

<template>
  <section class="home-wrap">
    <div v-if="notice" class="toast">{{ notice }}</div>
    <section class="hero-strip">
      <div class="hero-copy"><p class="eyebrow">本周陈列 · 07.24—07.30</p><h1>挑一些，<br /><em>让日子更顺手的东西。</em></h1><p class="hero-note">从家居、穿搭到日常器物，精选耐用、好看且愿意反复使用的商品。</p><router-link class="primary-button" to="/catalog">逛逛本周精选 <span>→</span></router-link></div>
      <div class="hero-visual"><img src="/assets/lamp.jpg" alt="暖光台灯" /><span>01 / 04</span></div>
    </section>
    <section class="category-row"><div><span class="section-kicker">分类浏览</span><h2>从生活的不同角落开始</h2></div><div class="category-links"><router-link to="/catalog?q=家居">家居</router-link><router-link to="/catalog?q=穿搭">穿搭</router-link><router-link to="/catalog?q=数码">数码</router-link><router-link to="/catalog?q=咖啡">咖啡</router-link></div></section>
    <section class="product-section"><div class="section-heading"><div><span class="section-kicker">精选商品</span><h2>今天也值得好好挑选</h2></div><router-link to="/catalog" class="text-link">查看全部 →</router-link></div><div class="product-grid"><article v-for="product in products" :key="product.id" class="product-card"><div class="product-image"><img :src="product.image" :alt="product.name" /><span :class="['product-tag', { muted: product.stock === 0 }]">{{ product.tag }}</span></div><div class="product-info"><p class="product-category">{{ product.category }}</p><h3>{{ product.name }}</h3><div class="product-bottom"><span class="price">¥{{ product.price.toLocaleString() }} <del>¥{{ product.oldPrice.toLocaleString() }}</del></span><button class="add-button" :disabled="product.stock === 0" @click="addProduct(product)">{{ product.stock === 0 ? '售罄' : '加入袋中' }}</button></div></div></article></div></section>
    <section class="service-row"><div><strong>安心选购</strong><span>7天无理由退换</span></div><div><strong>快速发货</strong><span>现货 48 小时内发出</span></div><div><strong>人工客服</strong><span>工作日 9:00—18:00</span></div></section>
  </section>
</template>
