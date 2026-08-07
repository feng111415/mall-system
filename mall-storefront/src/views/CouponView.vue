<script setup>
import { computed, onMounted, ref } from 'vue'
import { Icon as VanIcon } from 'vant'
import { useRouter } from 'vue-router'
import { claimCoupon, getClaimableCoupons, getMemberCoupons } from '../api/coupon'
import { useNoticeStore } from '../stores/notice'

const router = useRouter()
const notice = useNoticeStore()
const owned = ref([])
const claimable = ref([])
const loading = ref(true)
const busyId = ref(null)
const isLoggedIn = computed(() => Boolean(sessionStorage.getItem('mall-user-token')))

onMounted(load)
async function load() {
  if (!isLoggedIn.value) return router.replace('/account')
  loading.value = true
  try {
    const [ownedResponse, claimableResponse] = await Promise.all([getMemberCoupons(), getClaimableCoupons()])
    owned.value = ownedResponse.data.data || []
    claimable.value = claimableResponse.data.data || []
  } catch (error) {
    notice.show(error.response?.data?.msg || '优惠券读取失败，请稍后重试', 'error')
  } finally { loading.value = false }
}
async function claim(item) {
  if (busyId.value) return
  busyId.value = item.couponId
  try { await claimCoupon(item.couponId); notice.show('优惠券已领取'); await load() } catch (error) {
    notice.show(error.response?.data?.msg || '领取失败，请稍后重试', 'error')
  } finally { busyId.value = null }
}
function money(value) { return Number(value || 0).toFixed(2) }
function date(value) { return value ? new Intl.DateTimeFormat('zh-CN').format(new Date(value)) : '-' }
</script>

<template>
  <section class="coupon-page">
    <div class="page-intro"><span class="section-kicker">MEMBER BENEFITS</span><h1>我的优惠券</h1><p>每笔订单最多使用一张，支付成功后核销，未支付关单会释放。</p></div>
    <div v-if="loading" class="empty-state"><p>正在读取优惠券...</p></div>
    <template v-else>
      <section class="coupon-section">
        <div class="section-heading"><div><span class="section-kicker">AVAILABLE TO CLAIM</span><h2>可领取</h2></div><span>{{ claimable.length }} 张</span></div>
        <div v-if="claimable.length" class="coupon-grid"><article v-for="item in claimable" :key="item.couponId" class="coupon-card claimable"><div class="coupon-value"><strong>¥{{ money(item.discountAmount) }}</strong><small>满 ¥{{ money(item.thresholdAmount) }} 可用</small></div><div class="coupon-copy"><b>{{ item.couponName }}</b><small>{{ item.scopeType === 'ALL' ? '全场商品' : item.scopeType === 'CATEGORY' ? '指定分类' : '指定商品' }}</small><small>有效期至 {{ date(item.validTo) }}</small></div><button class="primary-button" type="button" :disabled="busyId === item.couponId" @click="claim(item)">{{ busyId === item.couponId ? '领取中...' : '立即领取' }} <span>→</span></button></article></div>
        <p v-else class="coupon-empty">暂无可领取优惠券</p>
      </section>
      <section class="coupon-section">
        <div class="section-heading"><div><span class="section-kicker">MY COUPONS</span><h2>已领取</h2></div><span>{{ owned.length }} 张</span></div>
        <div v-if="owned.length" class="coupon-grid"><article v-for="item in owned" :key="item.memberCouponId" class="coupon-card" :class="item.status.toLowerCase()"><div class="coupon-value"><strong>¥{{ money(item.discountAmount) }}</strong><small>满 ¥{{ money(item.thresholdAmount) }} 可用</small></div><div class="coupon-copy"><b>{{ item.couponName }}</b><small>{{ item.status === 'AVAILABLE' ? '可使用' : item.status === 'LOCKED' ? '订单占用中' : item.status === 'USED' ? '已使用' : item.status === 'EXPIRED' ? '已过期' : '已失效' }}</small><small>有效期至 {{ date(item.validTo) }}</small></div><VanIcon :name="item.status === 'AVAILABLE' ? 'coupon-o' : 'clock-o'" /></article></div>
        <p v-else class="coupon-empty">还没有优惠券，去领取一张吧</p>
      </section>
    </template>
  </section>
</template>

<style scoped>
.coupon-page{width:min(1184px,calc(100% - 56px));margin:auto;padding:30px 0 90px}.coupon-section{padding-top:30px}.coupon-section>.section-heading{padding:0 0 16px}.coupon-grid{display:grid;gap:12px;margin-top:18px}.coupon-card{display:grid;grid-template-columns:170px 1fr auto;align-items:center;gap:20px;border:2px solid var(--ink);border-radius:8px;padding:16px 18px;background:var(--paper);box-shadow:4px 4px 0 rgb(32 32 42 / 10%)}.coupon-card:not(.available):not(.claimable){opacity:.58}.coupon-value{display:flex;align-items:baseline;gap:10px;border-right:1px dashed var(--line)}.coupon-value strong{color:var(--red);font-size:28px}.coupon-value small,.coupon-copy small{display:block;color:var(--muted);font-size:11px}.coupon-copy{display:grid;gap:5px}.coupon-copy b{font-size:15px}.coupon-card>.van-icon{font-size:28px;color:var(--green)}.coupon-empty{padding:25px 0;color:var(--muted);font-size:13px}@media(max-width:700px){.coupon-page{width:calc(100% - 32px);padding:22px 0 82px}.coupon-card{grid-template-columns:1fr auto;gap:12px}.coupon-value{grid-column:1/-1;border-right:0;border-bottom:1px dashed var(--line);padding-bottom:10px}.coupon-card .primary-button{padding:10px 12px;font-size:12px}.coupon-copy b{font-size:13px}}
</style>
