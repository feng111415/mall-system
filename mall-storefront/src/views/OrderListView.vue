<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { applyRefund, cancelOrder, getOrderDetail, getOrderLogistics, getOrders } from '../api/order'

const router = useRouter()
const orders = ref([])
const selectedOrder = ref(null)
const selectedStatus = ref('')
const loading = ref(true)
const message = ref('')
const busy = ref(false)
const logistics = ref(null)
const logisticsLoading = ref(false)
const logisticsMessage = ref('')
const refundReason = ref('商品问题')
const refundBusy = ref(false)
const statusOptions = [
  { label: '全部', value: '' }, { label: '待付款', value: 'PENDING_PAYMENT' },
  { label: '待发货', value: 'PENDING_SHIPMENT' }, { label: '已发货', value: 'SHIPPED' }, { label: '售后中', value: 'AFTER_SALE' },
  { label: '已完成', value: 'COMPLETED' }, { label: '已取消', value: 'CANCELLED' }, { label: '已关闭', value: 'CLOSED' }
]
const statusLabels = Object.fromEntries(statusOptions.map(item => [item.value, item.label]))

onMounted(loadOrders)
watch(selectedStatus, loadOrders)

async function loadOrders() {
  if (!sessionStorage.getItem('mall-user-token')) return router.replace('/account')
  loading.value = true; message.value = ''
  try { orders.value = (await getOrders({ status: selectedStatus.value || undefined, limit: 50 })).data.data || [] }
  catch (error) { message.value = error.response?.data?.msg || '订单读取失败，请稍后重试' }
  finally { loading.value = false }
}

async function showDetail(orderId) {
  busy.value = true; message.value = ''
  logistics.value = null; logisticsMessage.value = ''
  try {
    const detail = (await getOrderDetail(orderId)).data.data
    selectedOrder.value = detail
    const embeddedLogistics = detail?.logistics || detail?.shipment || detail?.logisticsShipment
    if (embeddedLogistics) logistics.value = normalizeLogistics(embeddedLogistics)
    if (isShippedStatus(detail?.status) && !logistics.value) await loadLogistics(orderId)
  }
  catch (error) { message.value = error.response?.data?.msg || '订单详情读取失败' }
  finally { busy.value = false }
}

async function loadLogistics(orderId) {
  logisticsLoading.value = true
  logisticsMessage.value = ''
  try {
    const response = await getOrderLogistics(orderId)
    logistics.value = normalizeLogistics(response.data?.data || response.data)
  } catch (error) {
    // 物流渠道暂时没有轨迹时，订单详情仍然可以正常查看。
    if (error.response?.status !== 404) logisticsMessage.value = error.response?.data?.msg || '物流信息暂时无法获取'
  } finally {
    logisticsLoading.value = false
  }
}

function normalizeLogistics(value) {
  const raw = Array.isArray(value) ? value[0] : value
  if (!raw || typeof raw !== 'object') return null
  const nodes = raw.nodes || raw.traces || raw.trackingNodes || raw.records || []
  return { ...raw, nodes: Array.isArray(nodes) ? nodes : [] }
}

function isShippedStatus(status) {
  return ['SHIPPED', 'DELIVERING', 'IN_TRANSIT', 'PENDING_RECEIPT', 'DELIVERED', 'COMPLETED'].includes(status)
}

function logisticsStatusLabel(status) {
  return ({ IN_TRANSIT: '运输中', DELIVERED: '已签收', CANCELLED: '已取消' }[status] || status || '运输中')
}

function logisticsCompany(shipment) { return shipment?.companyName || shipment?.companyCode || shipment?.company || '物流配送' }
function logisticsTrackingNo(shipment) { return shipment?.trackingNo || shipment?.trackingNumber || shipment?.waybillNo || '-' }
function logisticsNodeTitle(node) { return node?.title || node?.statusText || node?.nodeStatus || '物流节点' }
function logisticsNodeDescription(node) { return node?.description || node?.context || node?.detail || node?.title || '-' }
function logisticsNodeTime(node) { return node?.eventTime || node?.time || node?.acceptTime || node?.createTime }

const logisticsNodes = computed(() => logistics.value?.nodes || [])

async function cancelSelected() {
  if (!selectedOrder.value || busy.value) return
  busy.value = true
  try { selectedOrder.value = (await cancelOrder(selectedOrder.value.orderId, '会员主动取消')).data.data; await loadOrders() }
  catch (error) { message.value = error.response?.data?.msg || '取消订单失败' }
  finally { busy.value = false }
}

async function requestRefund() {
  if (!selectedOrder.value || refundBusy.value || !refundReason.value.trim()) return
  refundBusy.value = true; message.value = ''
  try {
    await applyRefund(selectedOrder.value.orderId, refundReason.value.trim())
    await showDetail(selectedOrder.value.orderId)
    await loadOrders()
  } catch (error) { message.value = error.response?.data?.msg || '退款申请提交失败' }
  finally { refundBusy.value = false }
}

function statusLabel(status) { return statusLabels[status] || status }
function formatPrice(value) { return Number(value || 0).toLocaleString() }
function formatTime(value) { return value ? new Date(value).toLocaleString() : '-' }
</script>

<template>
  <section class="orders-wrap">
    <div class="page-intro"><span class="section-kicker">我的订单</span><h1>订单中心</h1><p>查看订单状态、金额和商品快照。</p></div>
    <div class="order-tabs"><button v-for="item in statusOptions" :key="item.value" :class="{ active: selectedStatus === item.value }" @click="selectedStatus = item.value">{{ item.label }}</button></div>
    <p v-if="message" class="form-message">{{ message }}</p><p v-if="loading" class="loading-note">正在读取订单...</p>
    <div v-else-if="orders.length" class="order-list"><article v-for="item in orders" :key="item.orderId" class="order-card" @click="showDetail(item.orderId)"><div class="order-card-head"><span>{{ item.orderNo }}</span><strong>{{ statusLabel(item.status) }}</strong></div><div class="order-card-body"><span>{{ formatTime(item.createTime) }}</span><b>¥{{ formatPrice(item.payableAmount) }}</b></div></article></div>
    <div v-else class="empty-state"><h2>暂时没有订单</h2><p>完成一次结算后，订单会显示在这里。</p><router-link class="primary-button" to="/catalog">去逛商品</router-link></div>
    <div v-if="selectedOrder" class="order-detail-panel"><div class="order-detail-head"><div><span class="section-kicker">订单详情</span><h2>{{ selectedOrder.orderNo }}</h2></div><button class="remove-button" aria-label="关闭详情" @click="selectedOrder = null">×</button></div><p class="order-detail-status">{{ statusLabel(selectedOrder.status) }} · {{ formatTime(selectedOrder.createTime) }}</p><div v-for="item in selectedOrder.items" :key="item.orderItemId" class="order-detail-item"><span>{{ item.productName }} {{ item.skuName || item.skuCode }} × {{ item.quantity }}</span><b>¥{{ formatPrice(item.lineAmount) }}</b></div><div class="order-detail-total"><span>应付金额</span><strong>¥{{ formatPrice(selectedOrder.payableAmount) }}</strong></div><p class="order-address">{{ selectedOrder.receiverName }} {{ selectedOrder.receiverPhone }}<br />{{ selectedOrder.receiverProvince }} {{ selectedOrder.receiverCity }} {{ selectedOrder.receiverDistrict }} {{ selectedOrder.receiverDetailAddress }}</p><button v-if="selectedOrder.status === 'PENDING_PAYMENT'" class="primary-button" :disabled="busy" @click="cancelSelected">{{ busy ? '处理中...' : '取消订单' }}</button>
      <section v-if="isShippedStatus(selectedOrder.status) || logistics" class="order-logistics"><div class="order-logistics-head"><div><span class="section-kicker">物流跟踪</span><h3>配送信息</h3></div><span v-if="logistics" class="logistics-status">{{ logisticsStatusLabel(logistics.status) }}</span></div><p v-if="logisticsLoading" class="loading-note">正在查询物流...</p><p v-else-if="logisticsMessage" class="form-message">{{ logisticsMessage }}</p><template v-else-if="logistics"><div class="logistics-summary"><span>{{ logisticsCompany(logistics) }}</span><strong>{{ logisticsTrackingNo(logistics) }}</strong></div><ol v-if="logisticsNodes.length" class="logistics-timeline"><li v-for="(node, index) in logisticsNodes" :key="node.nodeId || node.id || `${logistics.trackingNo}-${index}`" :class="{ current: index === 0 }"><span class="logistics-dot"></span><div><strong>{{ logisticsNodeTitle(node) }}</strong><p>{{ logisticsNodeDescription(node) }}</p><small>{{ formatTime(logisticsNodeTime(node)) }}<template v-if="node.location"> · {{ node.location }}</template></small></div></li></ol><p v-else class="loading-note">物流公司已揽收，暂无新的轨迹节点。</p></template><p v-else class="loading-note">物流单生成后，这里会显示配送进度。</p></section>
      <section v-if="selectedOrder.status === 'SHIPPED' || selectedOrder.status === 'COMPLETED'" class="refund-panel"><h3>申请售后</h3><textarea v-model="refundReason" maxlength="255" rows="3" placeholder="请填写退款原因"></textarea><button class="primary-button" :disabled="refundBusy || !refundReason.trim()" @click="requestRefund">{{ refundBusy ? '正在提交...' : '申请退款' }}</button></section>
    </div>
  </section>
</template>
