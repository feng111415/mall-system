<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon as VanIcon } from 'vant'
import { cancelOrder, getOrderDetail, getOrderLogistics, confirmReceipt, applyItemAfterSale, getAfterSales, submitReturnTracking } from '../api/order'
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
const afterSaleOpen = ref(false)
const afterSaleType = ref('ONLY_REFUND')
const afterSaleReason = ref('OTHER')
const afterSaleEvidence = ref('')
const afterSaleItems = ref([])
const afterSales = ref([])
const returnTrackingDrafts = ref({})
const logistics = ref(null)
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
const afterSaleStatusLabels = {
  PENDING_REVIEW: '待审核', APPROVED: '审核通过', RETURN_SHIPPED: '退货已寄出',
  REFUNDING: '退款处理中', SUCCESS: '退款成功', REJECTED: '已驳回', FAILED: '退款失败'
}
const logisticsCompanies = [
  { code: 'SF', name: '顺丰速运' }, { code: 'ZTO', name: '中通快递' },
  { code: 'YTO', name: '圆通速递' }, { code: 'STO', name: '申通快递' },
  { code: 'YD', name: '韵达速递' }, { code: 'JD', name: '京东物流' },
  { code: 'EMS', name: '中国邮政 EMS' }, { code: 'JT', name: '极兔速递' },
  { code: 'DEPPON', name: '德邦快递' }
]
const activeAfterSaleStatuses = new Set(['PENDING_REVIEW', 'APPROVED', 'RETURN_SHIPPED', 'REFUNDING', 'SUCCESS'])
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
    const [orderResponse, paymentResponse, afterSaleResponse] = await Promise.all([
      getOrderDetail(orderId), getOrderPayments(orderId), getAfterSales()
    ])
    order.value = orderResponse.data.data
    payments.value = paymentResponse.data.data || []
    afterSales.value = (afterSaleResponse.data.data || []).filter(item => item.orderId === orderId)
    logistics.value = null
    if (['SHIPPED', 'COMPLETED', 'AFTER_SALE'].includes(order.value.status)) {
      try { logistics.value = (await getOrderLogistics(orderId)).data.data } catch { logistics.value = null }
    }
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

async function receiveOrder() {
  if (busy.value || !order.value || !logistics.value) return
  busy.value = true
  message.value = ''
  try { await confirmReceipt(order.value.orderId); notice.show('已确认收货'); await loadDetail() }
  catch (error) { message.value = error.response?.data?.msg || '确认收货失败'; notice.show(message.value, 'error') }
  finally { busy.value = false }
}

function openAfterSale() {
  afterSaleItems.value = (order.value?.items || []).map(item => {
    const remainingQuantity = remainingAfterSaleQuantity(item.orderItemId, item.quantity)
    return { orderItemId: item.orderItemId, productName: item.productName, quantity: remainingQuantity, requestedQuantity: remainingQuantity }
  }).filter(item => item.quantity > 0)
  afterSaleType.value = 'ONLY_REFUND'; afterSaleReason.value = 'OTHER'; afterSaleEvidence.value = ''; afterSaleOpen.value = true
}

function remainingAfterSaleQuantity(orderItemId, orderQuantity) {
  const occupied = afterSales.value
    .filter(item => activeAfterSaleStatuses.has(item.status))
    .flatMap(item => item.items || [])
    .filter(item => item.orderItemId === orderItemId)
    .reduce((total, item) => total + Number(item.requestedQuantity || 0), 0)
  return Math.max(0, Number(orderQuantity || 0) - occupied)
}

function trackingDraft(afterSaleId) {
  if (!returnTrackingDrafts.value[afterSaleId]) {
    returnTrackingDrafts.value[afterSaleId] = { companyCode: '', trackingNo: '' }
  }
  return returnTrackingDrafts.value[afterSaleId]
}

async function submitTracking(afterSale) {
  const draft = trackingDraft(afterSale.afterSaleId)
  if (!draft.companyCode || !draft.trackingNo?.trim() || busy.value) return
  busy.value = true
  try {
    await submitReturnTracking(afterSale.afterSaleId, { companyCode: draft.companyCode, trackingNo: draft.trackingNo.trim() })
    notice.show('退货物流已提交')
    await loadDetail()
  } catch (error) {
    message.value = error.response?.data?.msg || '退货物流提交失败'
    notice.show(message.value, 'error')
  } finally { busy.value = false }
}
async function submitAfterSale() {
  const items = afterSaleItems.value.filter(item => item.requestedQuantity > 0).map(item => ({ orderItemId: item.orderItemId, quantity: Number(item.requestedQuantity) }))
  if (!items.length || busy.value) return
  busy.value = true
  try { await applyItemAfterSale(order.value.orderId, { type: afterSaleType.value, reasonCode: afterSaleReason.value, evidenceUrl: afterSaleEvidence.value, items }); afterSaleOpen.value = false; notice.show('售后申请已提交'); await loadDetail() }
  catch (error) { message.value = error.response?.data?.msg || '售后申请提交失败'; notice.show(message.value, 'error') }
  finally { busy.value = false }
}

function formatPrice(value) { return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }
function formatTime(value) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-' }
function statusLabel(status) { return statusLabels[status] || status || '-' }
function orderStatusLabel(value) {
  if (value?.status === 'CLOSED' && (value.cancelReason || '').includes('超时')) return '支付超时已关闭'
  return statusLabel(value?.status)
}
function paymentLabel(status) { return paymentLabels[status] || status || '-' }
function attemptLabel(status) { return attemptLabels[status] || status || '-' }
function afterSaleStatusLabel(status) { return afterSaleStatusLabels[status] || status || '-' }
function afterSaleTypeLabel(type) { return type === 'RETURN_REFUND' ? '退货退款' : '仅退款' }
function logisticsCompanyName(code) { return logisticsCompanies.find(item => item.code === code)?.name || code || '-' }
function logisticsNodeLabel(status) { return ({ SHIPPED: '已发货', IN_TRANSIT: '运输中', OUT_FOR_DELIVERY: '派送中', DELIVERED: '已签收', EXCEPTION: '运输异常', CORRECTION: '更正说明' })[status] || status || '-' }
</script>

<template>
  <section class="order-page">
    <router-link class="order-back" to="/orders"><VanIcon name="arrow-left" /> 返回订单列表</router-link>
    <p v-if="message" class="form-message" aria-live="polite">{{ message }}</p>
    <p v-if="loading" class="loading-note">正在读取订单详情...</p>
    <template v-else-if="order">
      <header class="order-page-header">
        <div><span class="section-kicker">订单详情</span><h1>{{ order.orderNo }}</h1><p>下单于 {{ formatTime(order.createTime) }}</p></div>
        <div class="order-state"><strong>{{ orderStatusLabel(order) }}</strong><span>{{ paymentLabel(order.paymentStatus) }}</span></div>
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

          <section v-if="logistics" class="order-section order-logistics-section">
            <div class="order-section-title"><span>04</span><h2>物流信息</h2></div>
            <div class="logistics-summary"><span>{{ logistics.companyName }}</span><strong>{{ logistics.trackingNo }}</strong></div>
            <ol class="logistics-timeline">
              <li v-for="(node, index) in logistics.nodes" :key="node.nodeId" :class="{ current: index === 0 }">
                <i class="logistics-dot"></i><div><strong>{{ logisticsNodeLabel(node.nodeStatus) }} · {{ node.title }}</strong><p>{{ node.description }}<template v-if="node.location">（{{ node.location }}）</template></p><small>{{ formatTime(node.eventTime) }}</small></div>
              </li>
            </ol>
          </section>

          <section v-if="afterSales.length" class="order-section">
            <div class="order-section-title"><span>05</span><h2>售后记录</h2></div>
            <article v-for="afterSale in afterSales" :key="afterSale.afterSaleId" class="after-sale-record">
              <header><div><strong>{{ afterSaleTypeLabel(afterSale.type) }}</strong><small>{{ afterSale.afterSaleNo }}</small></div><span>{{ afterSaleStatusLabel(afterSale.status) }}</span></header>
              <div class="after-sale-record-amount"><span>商品退款 ¥{{ formatPrice(afterSale.refundAmount) }}</span><span v-if="Number(afterSale.shippingRefundAmount)">运费退款 ¥{{ formatPrice(afterSale.shippingRefundAmount) }}</span></div>
              <p v-if="afterSale.failureReason">{{ afterSale.failureReason }}</p>
              <ul><li v-for="item in afterSale.items" :key="item.afterSaleItemId">{{ item.productName }} × {{ item.requestedQuantity }}，退款 ¥{{ formatPrice(item.refundAmount) }}</li></ul>
              <p v-if="afterSale.returnTrackingNo">退货物流：{{ logisticsCompanyName(afterSale.returnCompanyCode) }} {{ afterSale.returnTrackingNo }}</p>
              <form v-if="afterSale.type === 'RETURN_REFUND' && afterSale.status === 'APPROVED'" class="return-tracking-form" @submit.prevent="submitTracking(afterSale)">
                <select v-model="trackingDraft(afterSale.afterSaleId).companyCode" aria-label="退货物流公司" required><option value="" disabled>选择物流公司</option><option v-for="company in logisticsCompanies" :key="company.code" :value="company.code">{{ company.name }}</option></select>
                <input v-model="trackingDraft(afterSale.afterSaleId).trackingNo" aria-label="退货运单号" maxlength="128" placeholder="填写退货运单号" required />
                <button class="primary-button" :disabled="busy" type="submit">提交物流</button>
              </form>
            </article>
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
          <button v-if="order.status === 'SHIPPED' && logistics?.status === 'DELIVERED'" class="primary-button order-action" :disabled="busy" @click="receiveOrder"><VanIcon name="passed" />{{ busy ? '正在处理...' : '确认收货' }}</button>
          <button v-if="['SHIPPED', 'COMPLETED'].includes(order.status) && order.paymentStatus === 'PAID' && order.items.some(item => remainingAfterSaleQuantity(item.orderItemId, item.quantity) > 0)" class="order-secondary-action" :disabled="busy" @click="openAfterSale">申请售后</button>
        </aside>
      </div>
      <div v-if="afterSaleOpen" class="after-sale-dialog"><div class="after-sale-panel"><h2>申请售后</h2><label>类型<select v-model="afterSaleType"><option value="ONLY_REFUND">仅退款</option><option value="RETURN_REFUND">退货退款</option></select></label><label>原因<select v-model="afterSaleReason"><option value="OTHER">其他</option><option value="QUALITY">质量问题</option><option value="NOT_RECEIVED">未收到货</option></select></label><label v-if="afterSaleReason === 'QUALITY'">凭证地址<input v-model="afterSaleEvidence" placeholder="请填写凭证地址" /></label><div v-for="item in afterSaleItems" :key="item.orderItemId" class="after-sale-item"><span>{{ item.productName }}（最多 {{ item.quantity }} 件）</span><input v-model.number="item.requestedQuantity" type="number" min="0" :max="item.quantity" /></div><div class="after-sale-actions"><button class="order-secondary-action" @click="afterSaleOpen = false">取消</button><button class="primary-button" :disabled="busy" @click="submitAfterSale">提交申请</button></div></div></div>
    </template>
    <div v-else-if="!loading" class="empty-state"><h2>无法查看此订单</h2><p>{{ message }}</p><router-link class="primary-button" to="/orders">返回订单列表</router-link></div>
  </section>
</template>
