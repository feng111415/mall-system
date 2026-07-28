<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: { type: Object, required: true },
  badge: { type: String, default: '' }
})

const productLink = computed(() => `/product/${props.product.spuId}`)
const priceText = computed(() => {
  const min = Number(props.product.priceMin || 0)
  const max = Number(props.product.priceMax || min)
  return min === max ? `¥${min.toLocaleString()}` : `¥${min.toLocaleString()} 起`
})
const productBadge = computed(() => props.badge || (Number(props.product.salesCount || 0) >= 200 ? '热销' : '精选'))
</script>

<template>
  <article class="product-card">
    <router-link class="product-card-link" :to="productLink">
      <div class="product-image">
        <img :src="product.mainImage || '/assets/chair.jpg'" :alt="product.productName" />
        <span class="product-tag">{{ productBadge }}</span>
      </div>
      <div class="product-info">
        <p class="product-category">{{ product.categoryName }} <span v-if="product.brandName">· {{ product.brandName }}</span></p>
        <h3>{{ product.productName }}</h3>
        <p class="product-subtitle">{{ product.subtitle || '为日常而选的耐用好物' }}</p>
      </div>
    </router-link>
    <div class="product-bottom">
      <span class="price">{{ priceText }}</span>
      <router-link class="card-action" :to="productLink">选择规格 <span aria-hidden="true">→</span></router-link>
    </div>
  </article>
</template>
