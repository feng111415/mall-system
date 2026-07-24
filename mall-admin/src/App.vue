<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useSessionStore } from './stores/session'

const route = useRoute(); const session = useSessionStore()
const nav = [
  { label: '工作台', path: '/dashboard', icon: '▦' },
  { label: '商品中心', path: '/products', icon: '□' },
  { label: '订单管理', path: '/orders', icon: '≡' },
  { label: '库存管理', path: '/products?tab=stock', icon: '◈' },
  { label: '售后退款', path: '/orders?tab=after-sale', icon: '↩' },
  { label: '风控与审计', path: '/orders?tab=risk', icon: '◇' }
]
const title = computed(() => route.path === '/dashboard' ? '工作台' : route.path === '/products' ? '商品中心' : '订单管理')
</script>

<template>
  <div class="admin-shell">
    <aside class="sidebar"><div class="admin-brand"><span class="brand-mark">日</span><div><strong>日常商店</strong><small>运营管理后台</small></div></div><div class="workspace-label">商城运营</div><nav><router-link v-for="item in nav" :key="item.label" :to="item.path" :class="{ active: route.path === item.path.split('?')[0] }"><span class="nav-icon">{{ item.icon }}</span>{{ item.label }}</router-link></nav><div class="sidebar-bottom"><span class="status-dot"></span> 系统运行正常<br /><small>版本 V0.1.0</small></div></aside>
    <div class="admin-content"><header class="topbar"><div><span class="breadcrumb">商城运营 /</span><strong>{{ title }}</strong></div><div class="top-actions"><button>⌕</button><button class="notice">♧<i>{{ session.unread }}</i></button><span class="operator"><b>{{ session.displayName.slice(0, 1) }}</b><span>{{ session.displayName }}<small>{{ session.operator.role }}</small></span></span></div></header><main class="admin-main"><router-view /></main></div>
  </div>
</template>
