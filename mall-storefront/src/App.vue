<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon as VanIcon } from 'vant'
import { useCartStore } from './stores/cart'
import { useNoticeStore } from './stores/notice'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const notice = useNoticeStore()
const search = ref('')
const navItems = [
  { label: '首页', to: '/' },
  { label: '全部商品', to: '/catalog' },
  { label: '我的订单', to: '/orders' },
  { label: '个人中心', to: '/account' }
]
function isActive(path) {
  if (path === '/catalog') return route.path === '/catalog' || route.path.startsWith('/product/')
  if (path === '/cart') return route.path === '/cart' || route.path === '/checkout'
  return route.path === path
}
const mobileNavItems = [
  { label: '首页', to: '/', icon: 'home-o' },
  { label: '商品', to: '/catalog', icon: 'apps-o' },
  { label: '购物车', to: '/cart', icon: 'cart-o', cart: true },
  { label: '订单', to: '/orders', icon: 'orders-o' },
  { label: '我的', to: '/account', icon: 'user-o' }
]
function submitSearch() {
  const value = search.value.trim()
  router.push({ path: '/catalog', query: value ? { q: value } : {} })
}
function clearSearch() { search.value = '' }
watch(() => route.fullPath, () => {
  search.value = route.path === '/catalog' ? String(route.query.q || '') : ''
  cart.load().catch(() => {})
}, { immediate: true })
</script>

<template>
  <div class="storefront-shell">
    <header class="site-header"><div class="header-inner"><router-link class="brand" to="/"><span class="brand-mark">日</span><span><strong>日常商城</strong><small>把好东西带回家</small></span></router-link><form class="search-box" @submit.prevent="submitSearch"><input v-model="search" aria-label="搜索商品" placeholder="搜索商品、品牌或分类" /><button v-if="search" class="icon-button search-clear" type="button" aria-label="清除搜索" title="清除搜索" @click="clearSearch"><VanIcon name="cross" /></button><button class="icon-button" type="submit" aria-label="搜索" title="搜索"><VanIcon name="search" /></button></form><nav class="header-actions"><router-link v-for="item in navItems" :key="item.to" :class="{ active: isActive(item.to) }" :to="item.to">{{ item.label }}</router-link><router-link class="cart-link" to="/cart" aria-label="购物车" title="购物车"><VanIcon name="cart-o" /><b v-if="cart.totalCount">{{ cart.totalCount }}</b></router-link></nav></div></header>
    <main><router-view /></main>
    <footer class="site-footer"><span>日常商城 · V0.2 体验版</span><span>配送范围：全国大部分地区</span></footer>
    <nav class="mobile-nav" aria-label="移动端导航"><router-link v-for="item in mobileNavItems" :key="item.to" :class="{ active: isActive(item.to) }" :to="item.to"><span class="mobile-nav-icon"><VanIcon :name="item.icon" /><b v-if="item.cart && cart.totalCount">{{ cart.totalCount }}</b></span><span>{{ item.label }}</span></router-link></nav>
    <Transition name="notice"><div v-if="notice.message" :class="['global-notice', notice.type, { 'above-cart-summary': route.path === '/cart' }]" role="status" aria-live="polite"><VanIcon :name="notice.type === 'error' ? 'warning-o' : 'success'" /><span>{{ notice.message }}</span><button class="notice-close" type="button" aria-label="关闭提示" title="关闭提示" @click="notice.clear"><VanIcon name="cross" /></button></div></Transition>
  </div>
</template>
