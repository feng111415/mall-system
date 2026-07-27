<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getCheckoutPreview } from '../api/checkout'

const router = useRouter()
const preview = ref(null)
const loading = ref(true)
const message = ref('')
const selectedAddressId = ref(null)
const hasToken = computed(() => Boolean(sessionStorage.getItem('mall-user-token')))

onMounted(async () => {
  if (!hasToken.value) return router.replace('/account')
  try {
    preview.value = (await getCheckoutPreview()).data.data
    selectedAddressId.value = preview.value.defaultAddressId
  } catch (error) { message.value = error.response?.data?.msg || '结算信息读取失败，请返回购物车重试' }
  finally { loading.value = false }
})
function formatPrice(value) { return Number(value || 0).toLocaleString() }
</script>

<template>
  <section class="checkout-wrap">
    <div class="page-intro"><span class="section-kicker">结算预览</span><h1>确认订单信息</h1><p>提交订单前，系统会再次校验价格、商品状态和库存。</p></div>
    <p v-if="loading" class="loading-note">正在读取结算信息...</p>
    <div v-else-if="preview" class="checkout-layout">
      <div class="checkout-main">
        <section class="checkout-section"><div class="checkout-heading"><h2>收货地址</h2><router-link to="/account">管理地址</router-link></div><div v-if="preview.addresses.length" class="address-list"><label v-for="address in preview.addresses" :key="address.addressId" class="address-option" :class="{ active: selectedAddressId === address.addressId }"><input v-model="selectedAddressId" type="radio" :value="address.addressId" /><span><strong>{{ address.receiverName }} {{ address.receiverPhone }}</strong><small>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}</small></span></label></div><p v-else class="checkout-empty">还没有收货地址，请先到个人中心添加。</p></section>
        <section class="checkout-section"><div class="checkout-heading"><h2>商品清单</h2><router-link to="/cart">返回购物车</router-link></div><div v-for="item in preview.items" :key="item.skuId" class="checkout-item"><img :src="item.productImage || '/assets/chair.jpg'" :alt="item.productName" /><div><strong>{{ item.productName }}</strong><small>{{ item.skuName || item.skuCode }} · {{ item.quantity }} 件</small></div><b>¥{{ formatPrice(item.lineAmount) }}</b></div><p v-if="!preview.items.length" class="checkout-empty">没有已勾选的有效商品。</p></section>
      </div>
      <aside class="checkout-summary"><h2>费用明细</h2><div><span>商品金额</span><b>¥{{ formatPrice(preview.productAmount) }}</b></div><div><span>运费</span><b>¥{{ formatPrice(preview.shippingFee) }}</b></div><div><span>优惠</span><b>-¥{{ formatPrice(preview.discountAmount) }}</b></div><div class="checkout-total"><span>应付金额</span><strong>¥{{ formatPrice(preview.payableAmount) }}</strong></div><p v-if="preview.validationMessages.length" class="form-message"><span v-for="item in preview.validationMessages" :key="item">{{ item }}<br /></span></p><button class="primary-button" disabled>{{ preview.canSubmit ? '提交订单（下一步）' : '暂不能提交订单' }} <span>→</span></button></aside>
    </div>
    <div v-else class="empty-state"><h2>暂时无法进入结算</h2><p>{{ message }}</p><router-link class="primary-button" to="/cart">返回购物车</router-link></div>
  </section>
</template>
