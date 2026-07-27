<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getProduct } from '../api/catalog'
import { useCartStore } from '../stores/cart'

const route=useRoute(), cart=useCartStore(), product=ref(null), selected=ref(null), message=ref(''), loading=ref(true)
const stock=computed(()=>selected.value?.availableStock || 0)
onMounted(async()=>{try{product.value=(await getProduct(route.params.id)).data.data;selected.value=product.value?.skuList?.[0]}catch{message.value='商品信息读取失败，请确认后端与数据库已启动'}finally{loading.value=false}})
function add(){if(!selected.value || stock.value<=0)return;cart.add({id:selected.value.skuId,name:`${product.value.productName} ${selected.value.skuName || ''}`,price:Number(selected.value.price),image:selected.value.imageUrl || product.value.mainImage,stock:stock.value});message.value='已加入购物袋'}
</script>
<template><section class="catalog-wrap"><p v-if="loading">正在读取商品...</p><div v-else-if="product" class="detail-layout"><div class="detail-image"><img :src="selected?.imageUrl || product.mainImage" :alt="product.productName" /></div><div class="detail-copy"><span class="section-kicker">{{ product.categoryName }} · {{ product.brandName }}</span><h1>{{ product.productName }}</h1><p>{{ product.subtitle }}</p><strong class="detail-price">¥{{ Number(selected?.price || product.priceMin).toLocaleString() }}</strong><div class="sku-list"><button v-for="sku in product.skuList" :key="sku.skuId" :class="{active:selected?.skuId===sku.skuId}" :disabled="sku.status!=='1'" @click="selected=sku">{{ sku.skuName || sku.skuCode }}</button></div><p class="stock-note">{{ stock > 0 ? `有货，可售 ${stock} 件` : '已售罄，暂时不能加购' }}</p><button class="primary-button" :disabled="stock<=0" @click="add">加入购物袋</button><p v-if="message" class="form-message">{{ message }}</p></div></div><div v-else class="empty-state"><h2>暂时无法查看商品</h2><p>{{ message }}</p><router-link class="primary-button" to="/catalog">返回商品列表</router-link></div></section></template>
