<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon as VanIcon } from 'vant'
import { createOrder, getCheckoutPreview } from '../api/checkout'
import { createPayment, mockPaymentSuccess } from '../api/payment'
import { useNoticeStore } from '../stores/notice'

const router = useRouter()
const notice = useNoticeStore()
const preview = ref(null)
const loading = ref(true)
const message = ref('')
const selectedAddressId = ref(null)
const selectedCouponId = ref(null)
const remark = ref('')
const busy = ref(false)
const paymentBusy = ref(false)
const order = ref(null)
const payment = ref(null)
const hasToken = computed(() => Boolean(sessionStorage.getItem('mall-user-token')))
const itemCount = computed(() => (preview.value?.items || []).reduce((total, item) => total + Number(item.quantity || 0), 0))
const selectedAddress = computed(() => (preview.value?.addresses || []).find(item => item.addressId === selectedAddressId.value))
const currentStep = computed(() => payment.value?.status === 'SUCCESS' ? 3 : order.value ? 2 : 1)
const canSubmit = computed(() => Boolean(preview.value?.canSubmit && selectedAddressId.value && !order.value))
const randomUuid = globalThis.crypto?.randomUUID?.bind(globalThis.crypto)
const idempotencyKey = randomUuid ? randomUuid() : `checkout-${Date.now()}-${Math.random().toString(16).slice(2)}`
const paymentIdempotencyKey = randomUuid ? randomUuid() : `payment-${Date.now()}-${Math.random().toString(16).slice(2)}`

onMounted(loadPreview)

async function loadPreview(couponId = selectedCouponId.value) {
  if (!hasToken.value) return router.replace('/account')
  loading.value = true
  message.value = ''
  try {
    preview.value = (await getCheckoutPreview(couponId)).data.data
    selectedCouponId.value = preview.value.selectedMemberCouponId || couponId || null
    selectedAddressId.value = preview.value.defaultAddressId || preview.value.addresses?.[0]?.addressId || null
  } catch (error) {
    preview.value = null
    message.value = error.response?.data?.msg || '结算信息读取失败，请返回购物车重试'
  } finally {
    loading.value = false
  }
}

async function chooseCoupon(couponId) {
  if (order.value) return
  selectedCouponId.value = couponId || null
  await loadPreview(selectedCouponId.value)
}

function formatPrice(value) { return Number(value || 0).toLocaleString() }

async function submitOrder() {
  if (!canSubmit.value || busy.value) return
  busy.value = true
  message.value = ''
  try {
    order.value = (await createOrder({ idempotencyKey, addressId: selectedAddressId.value, memberCouponId: selectedCouponId.value || undefined, remark: remark.value.trim() || undefined })).data.data
    notice.show('订单创建成功，请继续完成模拟支付')
  } catch (error) {
    message.value = error.response?.data?.msg || '订单创建失败，请稍后重试'
    notice.show(message.value, 'error')
  } finally {
    busy.value = false
  }
}

async function startPayment() {
  if (!order.value || paymentBusy.value || payment.value) return
  paymentBusy.value = true
  message.value = ''
  try {
    payment.value = (await createPayment(order.value.orderId, paymentIdempotencyKey)).data.data
    notice.show('模拟支付单已创建')
  } catch (error) {
    message.value = error.response?.data?.msg || '支付单创建失败，请稍后重试'
    notice.show(message.value, 'error')
  } finally {
    paymentBusy.value = false
  }
}

async function confirmMockPayment() {
  if (!payment.value || payment.value.status !== 'PAYING' || paymentBusy.value) return
  paymentBusy.value = true
  message.value = ''
  try {
    payment.value = (await mockPaymentSuccess(payment.value.paymentNo)).data.data
    notice.show('支付成功，订单已进入待发货')
  } catch (error) {
    message.value = error.response?.data?.msg || '支付确认失败，请稍后重试'
    notice.show(message.value, 'error')
  } finally {
    paymentBusy.value = false
  }
}
</script>

<template>
  <section class="checkout-wrap">
    <div class="page-intro"><span class="section-kicker">结算预览</span><h1>确认订单信息</h1><p>价格与库存已重新校验，提交后将为订单锁定库存。</p></div>
    <ol class="checkout-progress" aria-label="结算进度"><li v-for="(label, index) in ['确认订单', '模拟支付', '支付完成']" :key="label" :class="{ active: currentStep >= index + 1, current: currentStep === index + 1 }"><span>{{ index + 1 }}</span>{{ label }}</li></ol>
    <div v-if="loading" class="checkout-loading" aria-label="正在读取结算信息"><div><span></span><span></span><span></span></div><aside></aside></div>
    <div v-else-if="preview" class="checkout-layout">
      <div class="checkout-main">
        <section class="checkout-section">
          <div class="checkout-heading"><div><span class="checkout-step-label">01</span><h2>收货地址</h2></div><router-link to="/account">管理地址</router-link></div>
          <div v-if="preview.addresses?.length" class="address-list"><label v-for="address in preview.addresses" :key="address.addressId" class="address-option" :class="{ active: selectedAddressId === address.addressId, disabled: order }"><input v-model="selectedAddressId" type="radio" :value="address.addressId" :disabled="Boolean(order)" /><span><strong>{{ address.receiverName }} {{ address.receiverPhone }} <em v-if="address.isDefault === '1'">默认</em></strong><small>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}</small></span><VanIcon v-if="selectedAddressId === address.addressId" name="success" /></label></div>
          <div v-else class="checkout-empty"><p>还没有可用的收货地址。</p><router-link class="text-link" to="/account">添加收货地址 →</router-link></div>
        </section>
        <section class="checkout-section">
          <div class="checkout-heading"><div><span class="checkout-step-label">02</span><h2>商品清单</h2></div><router-link v-if="!order" to="/cart">返回购物车</router-link></div>
          <div class="checkout-list"><div v-for="item in preview.items" :key="item.skuId" class="checkout-item"><img :src="item.productImage || '/assets/chair.jpg'" :alt="item.productName" /><div><strong>{{ item.productName }}</strong><small>{{ item.skuName || item.skuCode }}</small><span>¥{{ formatPrice(item.unitPrice) }} × {{ item.quantity }}</span></div><b>¥{{ formatPrice(item.lineAmount) }}</b></div></div>
          <p v-if="!preview.items.length" class="checkout-empty">没有已勾选的有效商品。</p>
        </section>
        <section class="checkout-section checkout-remark">
          <div class="checkout-heading"><div><span class="checkout-step-label">03</span><h2>订单备注</h2></div><small>{{ remark.length }}/200</small></div>
          <textarea v-model.trim="remark" maxlength="200" rows="3" :disabled="Boolean(order)" placeholder="选填，可填写配送时间等说明；商品规格请以已选 SKU 为准"></textarea>
        </section>
        <section class="checkout-section checkout-coupons">
          <div class="checkout-heading"><div><span class="checkout-step-label">04</span><h2>优惠券</h2></div><router-link to="/coupons">去领取</router-link></div>
          <div v-if="preview.coupons?.filter(item => item.status === 'AVAILABLE').length" class="checkout-coupon-options">
            <label class="checkout-coupon-option" :class="{ active: !selectedCouponId }"><input type="radio" :checked="!selectedCouponId" :disabled="Boolean(order)" @change="chooseCoupon(null)" /><span><strong>不使用优惠券</strong><small>本单保留优惠券</small></span></label>
            <label v-for="coupon in preview.coupons.filter(item => item.status === 'AVAILABLE')" :key="coupon.memberCouponId" class="checkout-coupon-option" :class="{ active: selectedCouponId === coupon.memberCouponId }"><input type="radio" :value="coupon.memberCouponId" :checked="selectedCouponId === coupon.memberCouponId" :disabled="Boolean(order)" @change="chooseCoupon(coupon.memberCouponId)" /><span><strong>{{ coupon.couponName }} · 减 ¥{{ formatPrice(coupon.discountAmount) }}</strong><small>满 ¥{{ formatPrice(coupon.thresholdAmount) }} 可用</small></span></label>
          </div>
          <p v-else class="checkout-empty">暂无可用优惠券</p>
        </section>
      </div>
      <aside class="checkout-summary">
        <div class="checkout-summary-head"><div><span class="section-kicker">订单汇总</span><h2>{{ itemCount }} 件商品</h2></div><VanIcon name="orders-o" /></div>
        <div><span>商品金额</span><b>¥{{ formatPrice(preview.productAmount) }}</b></div><div><span>运费</span><b>{{ Number(preview.shippingFee || 0) ? `¥${formatPrice(preview.shippingFee)}` : '免运费' }}</b></div><div><span>优惠</span><b>-¥{{ formatPrice(preview.discountAmount) }}</b></div><div class="checkout-total"><span>应付金额</span><strong>¥{{ formatPrice(preview.payableAmount) }}</strong></div>
        <div v-if="selectedAddress" class="checkout-address-summary"><span>配送至</span><p>{{ selectedAddress.receiverName }} · {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}</p></div>
        <ul v-if="preview.validationMessages?.length" class="checkout-validation"><li v-for="item in preview.validationMessages" :key="item"><VanIcon name="warning-o" />{{ item }}</li></ul>
        <p v-if="message" class="form-message" aria-live="polite">{{ message }}</p>
        <div v-if="order" class="checkout-result"><VanIcon :name="payment?.status === 'SUCCESS' ? 'passed' : 'orders-o'" /><span><strong>{{ payment?.status === 'SUCCESS' ? '支付成功' : '订单已创建' }}</strong><small>订单号 {{ order.orderNo }}</small><small v-if="payment">支付单 {{ payment.paymentNo }}</small></span></div>
        <button v-if="!order" class="primary-button" :disabled="!canSubmit || busy" @click="submitOrder">{{ busy ? '正在提交...' : canSubmit ? '提交订单' : '暂不能提交订单' }} <span>→</span></button>
        <button v-else-if="!payment" class="primary-button" :disabled="paymentBusy" @click="startPayment">{{ paymentBusy ? '正在创建支付单...' : '创建模拟支付' }} <span>→</span></button>
        <button v-else-if="payment.status === 'PAYING'" class="primary-button" :disabled="paymentBusy" @click="confirmMockPayment">{{ paymentBusy ? '正在确认支付...' : '模拟支付成功' }} <span>→</span></button>
        <router-link v-else-if="payment.status === 'SUCCESS'" class="primary-button checkout-order-link" to="/orders">查看我的订单 <span>→</span></router-link>
        <p v-if="!order && !canSubmit" class="checkout-submit-note">{{ !selectedAddressId ? '请选择收货地址' : '请根据页面提示处理商品后再提交' }}</p>
        <p class="checkout-assurance"><VanIcon name="shield-o" />提交订单受幂等保护，重复点击不会重复创建订单。</p>
      </aside>
    </div>
    <div v-else class="empty-state"><h2>暂时无法进入结算</h2><p>{{ message }}</p><button class="primary-button" type="button" @click="loadPreview">重新加载</button><router-link class="text-link checkout-back-link" to="/cart">返回购物车</router-link></div>
  </section>
</template>
