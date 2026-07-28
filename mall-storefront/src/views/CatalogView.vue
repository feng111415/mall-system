<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategories, getProducts } from '../api/catalog'
import StoreProductCard from '../components/StoreProductCard.vue'

const route = useRoute()
const router = useRouter()
const categories = ref([])
const products = ref([])
const loading = ref(true)
const notice = ref('')
const selectedCategory = computed(() => Number(route.query.categoryId) || null)
const selectedSort = computed(() => ['sales', 'priceAsc', 'priceDesc'].includes(route.query.sort) ? route.query.sort : 'default')
const keyword = computed(() => String(route.query.q || '').trim())
const resultLabel = computed(() => keyword.value ? `“${keyword.value}” 的搜索结果` : selectedCategory.value ? '分类商品' : '全部商品')

function updateQuery(patch) {
  const query = { ...route.query, ...patch }
  Object.keys(query).forEach(key => { if (query[key] === undefined || query[key] === null || query[key] === '') delete query[key] })
  router.push({ path: '/catalog', query })
}

async function loadProducts() {
  loading.value = true
  notice.value = ''
  try { products.value = (await getProducts({ categoryId: selectedCategory.value || undefined, keyword: keyword.value || undefined, sort: selectedSort.value })).data.data || [] }
  catch (error) { products.value = []; notice.value = error.response?.data?.msg || '商品读取失败，请稍后刷新重试' }
  finally { loading.value = false }
}
onMounted(async () => {
  try { categories.value = (await getCategories()).data.data || [] } catch { notice.value = '分类读取失败，暂时只能浏览全部商品' }
  loadProducts()
})
watch(() => [route.query.q, route.query.categoryId, route.query.sort], loadProducts)
</script>

<template>
  <section class="catalog-wrap">
    <div class="page-intro"><span class="section-kicker">全部商品</span><h1>为日常挑选好物</h1><p>{{ loading ? '正在读取商品...' : `${resultLabel} · ${products.length} 件商品` }} <small v-if="notice">· {{ notice }}</small></p></div>
    <div class="catalog-toolbar">
      <div class="filter-tabs" aria-label="商品分类"><button :class="{ active: selectedCategory === null }" @click="updateQuery({ categoryId: undefined })">全部</button><button v-for="item in categories" :key="item.categoryId" :class="{ active: selectedCategory === item.categoryId }" @click="updateQuery({ categoryId: item.categoryId })">{{ item.categoryName }}</button></div>
      <label class="sort-control">排序<select :value="selectedSort" @change="updateQuery({ sort: $event.target.value })"><option value="default">综合推荐</option><option value="sales">销量优先</option><option value="priceAsc">价格从低到高</option><option value="priceDesc">价格从高到低</option></select></label>
    </div>
    <div v-if="loading" class="product-grid catalog-grid"><div v-for="item in 6" :key="item" class="product-skeleton"><span></span><i></i><b></b></div></div>
    <div v-else-if="products.length" class="product-grid catalog-grid"><StoreProductCard v-for="product in products" :key="product.spuId" :product="product" /></div>
    <div v-else class="empty-state"><h2>没有找到合适的商品</h2><p>{{ notice || '换个关键词，或清除分类筛选后再试。' }}</p><button class="primary-button" @click="updateQuery({ q: undefined, categoryId: undefined, sort: undefined })">清除筛选</button></div>
  </section>
</template>
