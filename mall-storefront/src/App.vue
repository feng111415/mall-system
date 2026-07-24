<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from './stores/cart'

const route = useRoute()
const router = useRouter()
const cart = useCartStore()
const search = ref('')
const navItems = [
  { label: '首页', to: '/' },
  { label: '全部商品', to: '/catalog' },
  { label: '我的订单', to: '/account' }
]
const isActive = computed(() => path => route.path === path)
function submitSearch() {
  router.push({ path: '/catalog', query: search.value ? { q: search.value } : {} })
}
</script>

<template>
  <div class="storefront-shell">
    <header class="site-header">
      <div class="header-inner">
        <router-link class="brand" to="/">
          <span class="brand-mark">日</span>
          <span><strong>日常商店</strong><small>把好东西带回家</small></span>
        </router-link>
        <form class="search-box" @submit.prevent="submitSearch">
          <input v-model="search" aria-label="搜索商品" placeholder="搜索商品、品牌或分类" />
          <button type="submit" aria-label="搜索">⌕</button>
        </form>
        <nav class="header-actions">
          <router-link v-for="item in navItems" :key="item.to" :class="{ active: isActive(item.to) }" :to="item.to">{{ item.label }}</router-link>
          <router-link class="cart-link" to="/cart">购物袋 <b>{{ cart.totalCount }}</b></router-link>
        </nav>
      </div>
    </header>
    <main><router-view /></main>
    <footer class="site-footer"><span>日常商店 · V0.1 验证稿</span><span>配送范围：全国大部分地区</span></footer>
  </div>
</template>
