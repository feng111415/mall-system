<script setup>
import { computed, onMounted, ref } from 'vue'
import { useCartStore } from '../stores/cart'

const cart = useCartStore()
const message = ref('')
const busy = ref(false)
const checkoutReady = false
const hasToken = computed(() => Boolean(sessionStorage.getItem('mall-user-token')))

onMounted(() => cart.load().catch(error => { message.value = error.response?.data?.msg || '请先登录后查看购物车' }))
async function run(action) {
  busy.value = true
  try { await action(); message.value = '' }
  catch (error) { message.value = error.response?.data?.msg || '购物车更新失败，请刷新重试' }
  finally { busy.value = false }
}
function formatPrice(value) { return Number(value || 0).toLocaleString() }
</script>

<template>
  <section class="cart-wrap">
    <div class="page-intro"><span class="section-kicker">购物车</span><h1>准备好带回家</h1><p>{{ cart.totalCount }} 件商品</p></div>
    <div v-if="!hasToken" class="empty-state"><h2>登录后查看购物车</h2><p>购物车会跟随商城会员账号保存，不会占用商品库存。</p><router-link class="primary-button" to="/account">去登录</router-link></div>
    <div v-else-if="cart.items.length" class="cart-list">
      <div v-for="item in cart.items" :key="item.skuId" class="cart-item" :class="{ soldout: !item.valid, shortage: item.stockShortage }">
        <label class="cart-check"><input type="checkbox" :checked="item.selectedFlag === '1'" :disabled="!item.valid || busy" @change="run(() => cart.setSelected(item, $event.target.checked))" /><span></span></label>
        <img :src="item.productImage || '/assets/chair.jpg'" :alt="item.productName" />
        <div class="cart-item-main"><span class="product-category">{{ !item.valid ? '已失效' : item.stockShortage ? `仅剩 ${item.availableStock} 件` : '现货' }}</span><h3>{{ item.productName || '商品已下架' }}</h3><p class="cart-sku">{{ item.skuName || item.skuCode }}</p><div class="quantity"><button :disabled="!item.valid || busy || item.quantity <= 1" @click="run(() => cart.setQuantity(item, item.quantity - 1))" aria-label="减少数量">−</button><span>{{ item.quantity }}</span><button :disabled="!item.valid || busy || item.quantity >= item.availableStock" @click="run(() => cart.setQuantity(item, item.quantity + 1))" aria-label="增加数量">+</button></div></div>
        <strong class="cart-price">¥{{ formatPrice(item.lineAmount) }}</strong><button class="remove-button" aria-label="移除商品" :disabled="busy" @click="run(() => cart.remove(item))">×</button>
      </div>
      <div class="cart-summary"><div><span>已选商品合计</span><strong>¥{{ formatPrice(cart.totalPrice) }}</strong><p v-if="message || !cart.canCheckout" class="form-message">{{ message || '请处理失效或库存不足的商品后再结算' }}</p></div><button class="primary-button" :disabled="!cart.canCheckout || busy || !checkoutReady">去结算（下一阶段） <span>→</span></button></div>
    </div>
    <div v-else-if="!cart.loading" class="empty-state"><h2>购物车还是空的</h2><p>去挑几件喜欢的东西，库存会在结算时再次校验。</p><router-link class="primary-button" to="/catalog">去逛逛</router-link></div>
    <p v-if="cart.loading" class="loading-note">正在读取购物车...</p>
  </section>
</template>
