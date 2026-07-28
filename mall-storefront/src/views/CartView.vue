<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Icon as VanIcon } from 'vant'
import { useCartStore } from '../stores/cart'
import { useNoticeStore } from '../stores/notice'

const cart = useCartStore()
const router = useRouter()
const notice = useNoticeStore()
const message = ref('')
const busySkuId = ref(null)
const bulkBusy = ref(false)
const hasToken = computed(() => Boolean(sessionStorage.getItem('mall-user-token')))
const selectableItems = computed(() => cart.items.filter(item => item.valid && !item.stockShortage && Number(item.availableStock) > 0))
const invalidItems = computed(() => cart.items.filter(item => !item.valid))
const selectedItems = computed(() => cart.items.filter(item => item.selectedFlag === '1'))
const selectedCount = computed(() => selectedItems.value.reduce((total, item) => total + Number(item.quantity || 0), 0))
const allSelected = computed(() => selectableItems.value.length > 0 && selectableItems.value.every(item => item.selectedFlag === '1'))
const checkoutMessage = computed(() => {
  if (message.value) return message.value
  if (!selectedItems.value.length) return '请选择需要结算的商品'
  if (selectedItems.value.some(item => !item.valid)) return '已选商品中有失效商品，请取消选择或清理后再结算'
  if (selectedItems.value.some(item => item.stockShortage)) return '已选商品库存不足，请调整数量后再结算'
  if (!cart.canCheckout) return '购物车信息已变化，请刷新后重新确认'
  return ''
})

onMounted(loadCart)

async function loadCart() {
  message.value = ''
  try { await cart.load() }
  catch (error) { message.value = error.response?.data?.msg || '购物车读取失败，请刷新重试' }
}

async function run(item, action, successMessage) {
  busySkuId.value = item?.skuId || null
  try {
    await action()
    message.value = ''
    if (successMessage) notice.show(successMessage)
  } catch (error) {
    message.value = error.response?.data?.msg || '购物车更新失败，请刷新重试'
    notice.show(message.value, 'error')
  } finally {
    busySkuId.value = null
  }
}

async function runBulk(action, successMessage) {
  bulkBusy.value = true
  try {
    await action()
    message.value = ''
    notice.show(successMessage)
  } catch (error) {
    message.value = error.response?.data?.msg || '批量更新失败，请刷新重试'
    notice.show(message.value, 'error')
  } finally {
    bulkBusy.value = false
  }
}

function isBusy(item) { return bulkBusy.value || busySkuId.value === item.skuId }
function toggleAll(selected) { return runBulk(() => cart.setSelection(selectableItems.value, selected), selected ? '已选择全部可结算商品' : '已取消全部选择') }
function clearInvalid() { return runBulk(() => cart.removeMany(invalidItems.value), '失效商品已清理') }
function formatPrice(value) { return Number(value || 0).toLocaleString() }
</script>

<template>
  <section class="cart-wrap">
    <div class="page-intro"><span class="section-kicker">购物车</span><h1>准备好带回家</h1><p>{{ cart.totalCount }} 件商品<span v-if="selectedCount"> · 已选 {{ selectedCount }} 件</span></p></div>
    <div v-if="!hasToken" class="empty-state"><h2>登录后查看购物车</h2><p>购物车会跟随商城会员账号保存，不会占用商品库存。</p><router-link class="primary-button" to="/account">去登录</router-link></div>
    <div v-else-if="cart.loading && !cart.items.length" class="cart-loading" aria-label="正在读取购物车"><span v-for="item in 3" :key="item"></span></div>
    <div v-else-if="cart.items.length" class="cart-content">
      <div class="cart-toolbar">
        <label class="cart-select-all"><input type="checkbox" :checked="allSelected" :disabled="bulkBusy || !selectableItems.length" @change="toggleAll($event.target.checked)" />全选可结算商品 <span>{{ selectedItems.length }}/{{ selectableItems.length }}</span></label>
        <div><button v-if="invalidItems.length" class="text-button" type="button" :disabled="bulkBusy" @click="clearInvalid">清理失效商品</button><button class="icon-button cart-refresh" type="button" title="刷新购物车" aria-label="刷新购物车" :disabled="cart.loading || bulkBusy" @click="loadCart"><VanIcon name="replay" /></button></div>
      </div>
      <div class="cart-list">
        <div v-for="item in cart.items" :key="item.skuId" class="cart-item" :class="{ soldout: !item.valid, shortage: item.stockShortage }">
          <label class="cart-check"><input type="checkbox" :checked="item.selectedFlag === '1'" :disabled="!item.valid || isBusy(item)" @change="run(item, () => cart.setSelected(item, $event.target.checked), $event.target.checked ? '已选中商品' : '已取消选择')" /><span></span></label>
          <img :src="item.productImage || '/assets/chair.jpg'" :alt="item.productName" />
          <div class="cart-item-main"><span :class="['cart-status', { danger: !item.valid || item.stockShortage }]">{{ !item.valid ? '商品已失效' : item.stockShortage ? `库存不足，仅剩 ${item.availableStock} 件` : Number(item.availableStock) <= 3 ? `库存紧张，仅剩 ${item.availableStock} 件` : '现货可售' }}</span><h3>{{ item.productName || '商品已下架' }}</h3><p class="cart-sku">{{ item.skuName || item.skuCode }}</p><div class="cart-item-actions"><div class="quantity"><button type="button" :disabled="!item.valid || isBusy(item) || item.quantity <= 1" @click="run(item, () => cart.setQuantity(item, item.quantity - 1), '商品数量已更新')" aria-label="减少数量"><VanIcon name="minus" /></button><span>{{ item.quantity }}</span><button type="button" :disabled="!item.valid || isBusy(item) || item.quantity >= item.availableStock" @click="run(item, () => cart.setQuantity(item, item.quantity + 1), '商品数量已更新')" aria-label="增加数量"><VanIcon name="plus" /></button></div><small>单价 ¥{{ formatPrice(item.price) }}</small></div></div>
          <strong class="cart-price"><small>小计</small>¥{{ formatPrice(item.lineAmount) }}</strong><button class="remove-button" type="button" title="移除商品" aria-label="移除商品" :disabled="isBusy(item)" @click="run(item, () => cart.remove(item), '商品已从购物车移除')"><VanIcon name="delete-o" /></button>
        </div>
      </div>
      <div class="cart-summary"><div><span>已选 {{ selectedCount }} 件商品</span><strong>¥{{ formatPrice(cart.totalPrice) }}</strong><p>运费和优惠将在结算页确认</p><p v-if="checkoutMessage" class="form-message" aria-live="polite">{{ checkoutMessage }}</p></div><button class="primary-button" :disabled="!cart.canCheckout || bulkBusy || busySkuId !== null" @click="router.push('/checkout')">去结算 <span>→</span></button></div>
    </div>
    <div v-else-if="!cart.loading" class="empty-state"><h2>购物车还是空的</h2><p>去挑几件喜欢的东西，库存会在结算时再次校验。</p><router-link class="primary-button" to="/catalog">去逛逛</router-link></div>
  </section>
</template>
