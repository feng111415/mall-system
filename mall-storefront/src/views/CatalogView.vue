<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getProducts } from '../api/catalog'

const route = useRoute()
const selectedCategory = ref(null)
const categories = ref([])
const products = ref([])
const loading = ref(true)
const notice = ref('')
const fallback = [
  { spuId: 10, productName: '北欧原木餐椅', categoryName: '家居', priceMin: 399, mainImage: '/assets/chair.jpg' },
  { spuId: 11, productName: '轻量城市跑鞋', categoryName: '穿搭', priceMin: 299, mainImage: '/assets/sneaker.jpg' },
  { spuId: 12, productName: '极简复古腕表', categoryName: '穿搭', priceMin: 528, mainImage: '/assets/watch.jpg' },
  { spuId: 13, productName: '便携无反相机', categoryName: '数码', priceMin: 3499, mainImage: '/assets/camera.jpg' }
]
const visible = computed(() => products.value.filter(item => !route.query.q || `${item.productName}${item.categoryName || ''}`.includes(route.query.q)))
async function loadProducts() {
  loading.value = true
  try { products.value = (await getProducts({ categoryId: selectedCategory.value || undefined, productName: route.query.q || undefined })).data.data || [] }
  catch { products.value = fallback; notice.value = '后端未启动，当前展示演示商品' }
  finally { loading.value = false }
}
onMounted(async () => {
  try { categories.value = (await getCategories()).data.data || [] } catch { categories.value = [{categoryId:1,categoryName:'家居'},{categoryId:2,categoryName:'数码'},{categoryId:3,categoryName:'穿搭'}] }
  loadProducts()
})
watch(() => route.query.q, loadProducts)
</script>

<template><section class="catalog-wrap"><div class="page-intro"><span class="section-kicker">全部商品</span><h1>为日常挑选好物</h1><p>{{ loading ? '正在读取商品...' : `当前展示 ${visible.length} 件商品` }} <small v-if="notice">· {{ notice }}</small></p></div><div class="catalog-toolbar"><div class="filter-tabs"><button :class="{active:selectedCategory===null}" @click="selectedCategory=null;loadProducts()">全部</button><button v-for="item in categories" :key="item.categoryId" :class="{active:selectedCategory===item.categoryId}" @click="selectedCategory=item.categoryId;loadProducts()">{{ item.categoryName }}</button></div><span class="sort-note">按精选排序</span></div><div class="product-grid catalog-grid"><router-link v-for="product in visible" :key="product.spuId" class="product-card" :to="`/product/${product.spuId}`"><div class="product-image"><img :src="product.mainImage || '/assets/chair.jpg'" :alt="product.productName" /></div><div class="product-info"><p class="product-category">{{ product.categoryName }}</p><h3>{{ product.productName }}</h3><div class="product-bottom"><span class="price">¥{{ Number(product.priceMin).toLocaleString() }}</span><span class="add-button">查看规格</span></div></div></router-link></div></section></template>
