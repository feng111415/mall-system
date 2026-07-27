<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { cancelOrder, getOrderDetail, getOrders } from '../api/order'

const router = useRouter()
const orders = ref([])
const selectedOrder = ref(null)
const selectedStatus = ref('')
const loading = ref(true)
const message = ref('')
const busy = ref(false)
const statusOptions = [
  { label: '全部', value: '' }, { label: '待付款', value: 'PENDING_PAYMENT' },
  { label: '待发货', value: 'PENDING_SHIPMENT' }, { label: '已发货', value: 'SHIPPED' },
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
  try { selectedOrder.value = (await getOrderDetail(orderId)).data.data }
  catch (error) { message.value = error.response?.data?.msg || '订单详情读取失败' }
  finally { busy.value = false }
}

async function cancelSelected() {
  if (!selectedOrder.value || busy.value) return
  busy.value = true
  try { selectedOrder.value = (await cancelOrder(selectedOrder.value.orderId, '会员主动取消')).data.data; await loadOrders() }
  catch (error) { message.value = error.response?.data?.msg || '取消订单失败' }
  finally { busy.value = false }
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
    <div v-if="selectedOrder" class="order-detail-panel"><div class="order-detail-head"><div><span class="section-kicker">订单详情</span><h2>{{ selectedOrder.orderNo }}</h2></div><button class="remove-button" aria-label="关闭详情" @click="selectedOrder = null">×</button></div><p class="order-detail-status">{{ statusLabel(selectedOrder.status) }} · {{ formatTime(selectedOrder.createTime) }}</p><div v-for="item in selectedOrder.items" :key="item.orderItemId" class="order-detail-item"><span>{{ item.productName }} {{ item.skuName || item.skuCode }} × {{ item.quantity }}</span><b>¥{{ formatPrice(item.lineAmount) }}</b></div><div class="order-detail-total"><span>应付金额</span><strong>¥{{ formatPrice(selectedOrder.payableAmount) }}</strong></div><p class="order-address">{{ selectedOrder.receiverName }} {{ selectedOrder.receiverPhone }}<br />{{ selectedOrder.receiverProvince }} {{ selectedOrder.receiverCity }} {{ selectedOrder.receiverDistrict }} {{ selectedOrder.receiverDetailAddress }}</p><button v-if="selectedOrder.status === 'PENDING_PAYMENT'" class="primary-button" :disabled="busy" @click="cancelSelected">{{ busy ? '处理中...' : '取消订单' }}</button></div>
  </section>
</template>
