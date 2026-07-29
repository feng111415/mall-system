<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon as VanIcon } from 'vant'
import { cancelOrder, getOrderDetail } from '../api/order'
import { createPayment, getOrderPayments, mockPaymentSuccess } from '../api/payment'
import { useNoticeStore } from '../stores/notice'

const route = useRoute()
const router = useRouter()
const notice = useNoticeStore()
const order = ref(null)
const payments = ref([])
const loading = ref(true)
const busy = ref(false)
const message = ref('')
const now = ref(Date.now())
let timer

const statusLabels = {
  PENDING_PAYMENT: '待付款', PENDING_SHIPMENT: '待发货', SHIPPED: '已发货',
  AFTER_SALE: '售后中', COMPLETED: '已完成', CANCELLED: '已取消', CLOSED: '已关闭'
}
const paymentLabels = {
  UNPAID: '未支付', PAYING: '等待支付结果', PAID: '已支付', FAILED: '支付未完成',
  REFUNDING: '异常支付退款中', REFUNDED: '异常支付已退款'
}
const attemptLabels = {
  CREATING: '正在创建', PAYING: '等待支付', SUCCESS: '支付成功', FAILED: '创建失败',
  CLOSED: '已超时关闭', REFUNDING: '退款处理中', REFUNDED: '已原路退款'
}
const latestPayment = computed(() => payments.value[0] || null)
const operations = computed(() => [...(order.value?.operations || [])].reverse())
const activeDeadline = computed(() => order.value?.paymentStatus === 'PAYING'
  ? order.value?.paymentResultDeadline : order.value?.paymentCreateDeadline)
const remainingSeconds = computed(() => activeDeadline.value
  ? Math.max(0, Math.ceil((new Date(activeDeadline.value).getTime() - now.value) / 1000)) : 0)
const countdown = computed(() => {
  const minutes = Math.floor(remainingSeconds.value / 60)
  const seconds = remainingSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

onMounted(async () => {
  if (!sessionStorage.getItem('mall-user-token')) return router.replace('/account')
  timer = window.setInterval(() => { now.value = Date.now() }, 1000)
  await loadDetail()
})
onBeforeUnmount(() => window.clearInterval(timer))

async function loadDetail() {
  loading.value = true
  message.value = ''
  try {
    const orderId = Number(route.params.orderId)
    const [orderResponse, paymentResponse] = await Promise.all([
      getOrderDetail(orderId), getOrderPayments(orderId)
    ])
    order.value = orderResponse.data.data
    payments.value = paymentResponse.data.data || []
  } catch (error) {
    order.value = null
    message.value = error.response?.data?.msg || '订单详情读取失败'
  } finally {
    loading.value = false
  }
}

async function startPayment() {
  if (!order.value?.canCreatePayment || busy.value) return
  busy.value = true
  message.value = ''
  try {
    const key = globalThis.crypto?.randomUUID?.() || `payment-${Date.now()}-${Math.random().toString(16).slice(2)}`
    await createPayment(order.value.orderId, key)
    notice.show('模拟支付单已创建')
    await loadDetail()
  } catch (error) {
    message.value = error.response?.data?.msg || '支付单创建失败'
    notice.show(message.value, 'error')
  } finally { busy.value = false }
}

async function confirmPayment() {
  if (!order.value?.canConfirmPayment || latestPayment.value?.status !== 'PAYING' || busy.value) return
  busy.value = true
  message.value = ''
  try {
    const result = (await mockPaymentSuccess(latestPayment.value.paymentNo)).data.data
    notice.show(result.status === 'REFUNDED' ? '订单已超时，款项已原路退回' : '支付成功')
    await loadDetail()
  } catch (error) {
    message.value = error.response?.data?.msg || '支付结果确认失败'
    notice.show(message.value, 'error')
  } finally { busy.value = false }
}

async function cancelCurrentOrder() {
  if (!order.value?.canCancel || busy.value) return
  busy.value = true
  message.value = ''
  try {
    await cancelOrder(order.value.orderId, '会员主动取消')
    notice.show('订单已取消')
    await loadDetail()
  } catch (error) {
    message.value = error.response?.data?.msg || '取消订单失败'
    notice.show(message.value, 'error')
  } finally { busy.value = false }
}

function formatPrice(value) { return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }
function formatTime(value) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-' }
function statusLabel(status) { return statusLabels[status] || status || '-' }
function paymentLabel(status) { return paymentLabels[status] || status || '-' }
function attemptLabel(status) { return attemptLabels[status] || status || '-' }
</script>

<template>
  <section class="order-page">
    <router-link class="order-back" to="/orders"><VanIcon name="arrow-left" /> 返回订单列表</router-link>
    <p v-if="message" class="form-message" aria-live="polite">{{ message }}</p>
    <p v-if="loading" class="loading-note">正在读取订单详情...</p>
    <template v-else-if="order">
      <header class="order-page-header">
        <div><span class="section-kicker">订单详情</span><h1>{{ order.orderNo }}</h1><p>下单于 {{ formatTime(order.createTime) }}</p></div>
        <div class="order-state"><strong>{{ statusLabel(order.status) }}</strong><span>{{ paymentLabel(order.paymentStatus) }}</span></div>
      </header>

      <section v-if="order.status === 'PENDING_PAYMENT'" class="payment-deadline" aria-live="polite">
        <div><VanIcon name="clock-o" /><span>{{ order.paymentStatus === 'PAYING' ? '支付结果等待时间' : '支付创建剩余时间' }}</span></div>
        <strong>{{ countdown }}</strong>
        <p v-if="remainingSeconds === 0">期限已结束，系统正在关闭订单或处理迟到支付。</p>
      </section>

      <div class="order-detail-grid">
        <main>
          <section class="order-section">
            <div class="order-section-title"><span>01</span><h2>商品明细</h2></div>
            <article v-for="item in order.items" :key="item.orderItemId" class="order-product">
              <img :src="item.productImage || '/assets/chair.jpg'" :alt="item.productName" />
              <div><strong>{{ item.productName }}</strong><small>{{ item.skuName || item.skuCode }}</small><span>¥{{ formatPrice(item.unitPrice) }} × {{ item.quantity }}</span></div>
              <b>¥{{ formatPrice(item.lineAmount) }}</b>
            </article>
          </section>

          <section class="order-section">
            <div class="order-section-title"><span>02</span><h2>收货信息</h2></div>
            <div class="order-address-detail"><strong>{{ order.receiverName }} · {{ order.receiverPhone }}</strong><p>{{ order.receiverProvince }} {{ order.receiverCity }} {{ order.receiverDistrict }} {{ order.receiverDetailAddress }}</p><small v-if="order.remark">订单备注：{{ order.remark }}</small></div>
          </section>

          <section class="order-section">
            <div class="order-section-title"><span>03</span><h2>订单进度</h2></div>
            <ol class="order-operation-list">
              <li v-for="item in operations" :key="item.operationId"><i></i><div><strong>{{ statusLabel(item.toStatus) }}</strong><p>{{ item.remark || '订单状态已更新' }}</p><small>{{ formatTime(item.createTime) }}</small></div></li>
            </ol>
          </section>
        </main>

        <aside class="order-summary-panel">
          <span class="section-kicker">费用汇总</span>
          <div><span>商品金额</span><b>¥{{ formatPrice(order.productAmount) }}</b></div>
          <div><span>运费</span><b>¥{{ formatPrice(order.shippingFee) }}</b></div>
          <div><span>优惠</span><b>-¥{{ formatPrice(order.discountAmount) }}</b></div>
          <div class="order-summary-total"><span>实付金额</span><strong>¥{{ formatPrice(order.payableAmount) }}</strong></div>

          <section class="payment-attempts">
            <h2>支付记录</h2>
            <div v-if="payments.length">
              <article v-for="item in payments" :key="item.paymentId"><span>{{ attemptLabel(item.status) }}</span><strong>{{ item.paymentNo }}</strong><small>{{ formatTime(item.createTime) }}</small><small v-if="item.providerRefundNo">退款流水 {{ item.providerRefundNo }}</small></article>
            </div>
            <p v-else>尚未创建支付单</p>
          </section>

          <p v-if="message" class="form-message">{{ message }}</p>
          <button v-if="order.canCreatePayment" class="primary-button order-action" :disabled="busy" @click="startPayment"><VanIcon name="balance-pay" />{{ busy ? '正在处理...' : '创建模拟支付' }}</button>
          <button v-if="order.canConfirmPayment && latestPayment?.status === 'PAYING'" class="primary-button order-action" :disabled="busy" @click="confirmPayment"><VanIcon name="passed" />{{ busy ? '正在处理...' : '模拟支付成功' }}</button>
          <button v-if="order.canCancel" class="order-secondary-action" :disabled="busy" @click="cancelCurrentOrder">取消订单</button>
        </aside>
      </div>
    </template>
    <div v-else-if="!loading" class="empty-state"><h2>无法查看此订单</h2><p>{{ message }}</p><router-link class="primary-button" to="/orders">返回订单列表</router-link></div>
  </section>
</template>
