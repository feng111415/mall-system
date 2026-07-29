<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon as VanIcon } from 'vant'
import { getOrders } from '../api/order'

const router = useRouter()
const orders = ref([])
const selectedStatus = ref('')
const loading = ref(true)
const message = ref('')
const statusOptions = [
  { label: '全部', value: '' },
  { label: '待付款', value: 'PENDING_PAYMENT' },
  { label: '待发货', value: 'PENDING_SHIPMENT' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '售后中', value: 'AFTER_SALE' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '已关闭', value: 'CLOSED' }
]
const statusLabels = Object.fromEntries(statusOptions.map(item => [item.value, item.label]))

onMounted(loadOrders)
watch(selectedStatus, loadOrders)

async function loadOrders() {
  if (!sessionStorage.getItem('mall-user-token')) return router.replace('/account')
  loading.value = true
  message.value = ''
  try {
    orders.value = (await getOrders({ status: selectedStatus.value || undefined, limit: 50 })).data.data || []
  } catch (error) {
    message.value = error.response?.data?.msg || '订单读取失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function statusLabel(status) { return statusLabels[status] || status }
function formatPrice(value) { return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }
function formatTime(value) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-' }
</script>

<template>
  <section class="orders-wrap">
    <div class="page-intro">
      <span class="section-kicker">我的订单</span>
      <h1>订单中心</h1>
      <p>查看交易进度、付款状态和订单明细。</p>
    </div>
    <div class="order-tabs" role="tablist" aria-label="订单状态">
      <button v-for="item in statusOptions" :key="item.value" :class="{ active: selectedStatus === item.value }" role="tab" :aria-selected="selectedStatus === item.value" @click="selectedStatus = item.value">{{ item.label }}</button>
    </div>
    <p v-if="message" class="form-message" aria-live="polite">{{ message }}</p>
    <p v-if="loading" class="loading-note">正在读取订单...</p>
    <div v-else-if="orders.length" class="order-list">
      <router-link v-for="item in orders" :key="item.orderId" class="order-card" :to="`/orders/${item.orderId}`">
        <div class="order-card-head"><span>{{ item.orderNo }}</span><strong>{{ statusLabel(item.status) }}</strong></div>
        <div class="order-card-body"><span>{{ formatTime(item.createTime) }}</span><b>¥{{ formatPrice(item.payableAmount) }}</b><VanIcon name="arrow" aria-hidden="true" /></div>
      </router-link>
    </div>
    <div v-else class="empty-state">
      <h2>暂时没有订单</h2><p>完成一次结算后，订单会显示在这里。</p>
      <router-link class="primary-button" to="/catalog">去逛商品 <VanIcon name="arrow" /></router-link>
    </div>
  </section>
</template>
