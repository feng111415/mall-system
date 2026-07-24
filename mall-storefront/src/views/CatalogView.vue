<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '../stores/cart'

const route = useRoute(); const cart = useCartStore(); const filter = ref('全部')
const products = [
  { id: 10, name: '原木餐椅', category: '家居', price: 399, image: '/assets/chair.jpg', stock: 8 },
  { id: 11, name: '城市跑鞋', category: '穿搭', price: 299, image: '/assets/sneaker.jpg', stock: 0 },
  { id: 12, name: '复古腕表', category: '穿搭', price: 528, image: '/assets/watch.jpg', stock: 12 },
  { id: 13, name: '无反相机', category: '数码', price: 3499, image: '/assets/camera.jpg', stock: 4 },
  { id: 14, name: '手冲咖啡套装', category: '咖啡', price: 188, image: '/assets/coffee.jpg', stock: 20 },
  { id: 15, name: '通勤双肩包', category: '穿搭', price: 459, image: '/assets/backpack.jpg', stock: 6 }
]
const categories = ['全部', '家居', '穿搭', '数码', '咖啡']
const visible = computed(() => products.filter(item => filter.value === '全部' || item.category === filter.value).filter(item => !route.query.q || `${item.name}${item.category}`.includes(route.query.q)))
</script>

<template><section class="catalog-wrap"><div class="page-intro"><span class="section-kicker">全部商品</span><h1>为日常挑选好物</h1><p>当前展示 {{ visible.length }} 件商品</p></div><div class="catalog-toolbar"><div class="filter-tabs"><button v-for="item in categories" :key="item" :class="{ active: filter === item }" @click="filter = item">{{ item }}</button></div><span class="sort-note">按精选排序</span></div><div class="product-grid catalog-grid"><article v-for="product in visible" :key="product.id" class="product-card"><div class="product-image"><img :src="product.image" :alt="product.name" /><span v-if="product.stock === 0" class="product-tag muted">暂时售罄</span></div><div class="product-info"><p class="product-category">{{ product.category }}</p><h3>{{ product.name }}</h3><div class="product-bottom"><span class="price">¥{{ product.price.toLocaleString() }}</span><button class="add-button" :disabled="product.stock === 0" @click="cart.add(product)">{{ product.stock === 0 ? '售罄' : '加入袋中' }}</button></div></div></article></div></section></template>
