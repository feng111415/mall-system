<script setup>
import { computed, onMounted, ref } from 'vue'
import { Icon as VanIcon } from 'vant'
import { useRoute, useRouter } from 'vue-router'
import {
  clearBrowseHistory,
  getBrowseHistory,
  getFavorites,
  removeBrowseHistory,
  removeBrowseHistoryBatch,
  removeFavorite
} from '../api/activity'
import { useNoticeStore } from '../stores/notice'

const route = useRoute()
const router = useRouter()
const notice = useNoticeStore()
const activeTab = ref(route.query.tab === 'history' ? 'history' : 'favorites')
const favorites = ref([])
const history = ref([])
const selectedIds = ref(new Set())
const loading = ref(true)
const busyId = ref(null)
const batchBusy = ref(false)
const clearConfirming = ref(false)

const allHistorySelected = computed(() => history.value.length > 0 && selectedIds.value.size === history.value.length)
const historyGroups = computed(() => {
  const groups = new Map()
  history.value.forEach(item => {
    const key = dateKey(item.lastViewTime)
    if (!groups.has(key)) groups.set(key, [])
    groups.get(key).push(item)
  })
  return [...groups.entries()].map(([key, items]) => ({ key, label: dateLabel(key), items }))
})

onMounted(load)

async function load() {
  if (!sessionStorage.getItem('mall-user-token')) {
    router.replace('/account')
    return
  }
  loading.value = true
  try {
    const [favoriteResponse, historyResponse] = await Promise.all([getFavorites(), getBrowseHistory()])
    favorites.value = favoriteResponse.data.data || []
    history.value = historyResponse.data.data || []
    selectedIds.value = new Set([...selectedIds.value].filter(id => history.value.some(item => item.spuId === id)))
  } catch (error) {
    notice.show(error.response?.data?.msg || '收藏与足迹读取失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

function switchTab(tab) {
  activeTab.value = tab
  clearConfirming.value = false
  router.replace({ path: '/activity', query: tab === 'history' ? { tab: 'history' } : {} })
}

function money(value) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function dateKey(value) {
  if (!value) return 'unknown'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return 'unknown'
  const local = new Date(date.getTime() - date.getTimezoneOffset() * 60_000)
  return local.toISOString().slice(0, 10)
}

function dateLabel(key) {
  if (key === 'unknown') return '更早'
  const today = dateKey(new Date())
  const yesterday = dateKey(new Date(Date.now() - 86_400_000))
  if (key === today) return '今天'
  if (key === yesterday) return '昨天'
  return new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'short' }).format(new Date(`${key}T00:00:00`))
}

function timeLabel(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false }).format(new Date(value))
}

function toggleSelected(spuId) {
  const next = new Set(selectedIds.value)
  if (next.has(spuId)) next.delete(spuId)
  else next.add(spuId)
  selectedIds.value = next
}

function toggleAllHistory() {
  selectedIds.value = allHistorySelected.value ? new Set() : new Set(history.value.map(item => item.spuId))
}

async function unfavorite(item) {
  if (busyId.value) return
  busyId.value = item.spuId
  try {
    await removeFavorite(item.spuId)
    favorites.value = favorites.value.filter(current => current.spuId !== item.spuId)
    notice.show('已取消收藏')
  } catch (error) {
    notice.show(error.response?.data?.msg || '取消收藏失败，请稍后重试', 'error')
  } finally {
    busyId.value = null
  }
}

async function deleteOne(item) {
  if (busyId.value) return
  busyId.value = item.spuId
  try {
    await removeBrowseHistory(item.spuId)
    history.value = history.value.filter(current => current.spuId !== item.spuId)
    const next = new Set(selectedIds.value)
    next.delete(item.spuId)
    selectedIds.value = next
    notice.show('浏览记录已删除')
  } catch (error) {
    notice.show(error.response?.data?.msg || '删除失败，请稍后重试', 'error')
  } finally {
    busyId.value = null
  }
}

async function deleteSelected() {
  if (!selectedIds.value.size || batchBusy.value) return
  batchBusy.value = true
  try {
    const ids = [...selectedIds.value]
    for (let index = 0; index < ids.length; index += 100) {
      await removeBrowseHistoryBatch(ids.slice(index, index + 100))
    }
    history.value = history.value.filter(item => !selectedIds.value.has(item.spuId))
    selectedIds.value = new Set()
    notice.show(`已删除 ${ids.length} 条浏览记录`)
  } catch (error) {
    await load()
    notice.show(error.response?.data?.msg || '批量删除失败，请稍后重试', 'error')
  } finally {
    batchBusy.value = false
  }
}

async function clearAll() {
  if (!clearConfirming.value) {
    clearConfirming.value = true
    return
  }
  batchBusy.value = true
  try {
    await clearBrowseHistory()
    history.value = []
    selectedIds.value = new Set()
    clearConfirming.value = false
    notice.show('浏览足迹已清空')
  } catch (error) {
    notice.show(error.response?.data?.msg || '清空失败，请稍后重试', 'error')
  } finally {
    batchBusy.value = false
  }
}
</script>

<template>
  <section class="activity-page">
    <div class="page-intro activity-intro">
      <div><span class="section-kicker">MY COLLECTION</span><h1>收藏与足迹</h1><p>把喜欢的留在收藏里，也可以从最近浏览继续挑选。</p></div>
      <router-link class="text-link" to="/account">返回个人中心</router-link>
    </div>

    <div class="activity-tabs" role="tablist" aria-label="收藏与浏览足迹">
      <button :class="{ active: activeTab === 'favorites' }" type="button" role="tab" :aria-selected="activeTab === 'favorites'" @click="switchTab('favorites')"><VanIcon name="like-o" />我的收藏 <span>{{ favorites.length }}</span></button>
      <button :class="{ active: activeTab === 'history' }" type="button" role="tab" :aria-selected="activeTab === 'history'" @click="switchTab('history')"><VanIcon name="clock-o" />浏览足迹 <span>{{ history.length }}</span></button>
    </div>

    <div v-if="loading" class="empty-state compact"><p>正在读取收藏与足迹...</p></div>
    <template v-else>
      <div v-if="activeTab === 'history' && history.length" class="activity-toolbar">
        <label><input type="checkbox" :checked="allHistorySelected" @change="toggleAllHistory" />全选</label>
        <span v-if="selectedIds.size">已选 {{ selectedIds.size }} 条</span>
        <button type="button" :disabled="!selectedIds.size || batchBusy" @click="deleteSelected"><VanIcon name="delete-o" />{{ batchBusy && selectedIds.size ? '删除中...' : '删除所选' }}</button>
        <button class="clear-history-button" type="button" :disabled="batchBusy" @click="clearAll"><VanIcon name="delete-o" />{{ clearConfirming ? '再次点击确认清空' : '清空足迹' }}</button>
        <button v-if="clearConfirming" type="button" :disabled="batchBusy" @click="clearConfirming = false">取消</button>
      </div>

      <div v-if="activeTab === 'favorites' && favorites.length" class="activity-grid">
        <article v-for="item in favorites" :key="item.spuId" class="activity-card" :class="{ invalid: !item.available }">
          <router-link v-if="item.available" class="activity-card-main" :to="`/product/${item.spuId}`">
            <span class="activity-card-image"><img :src="item.mainImage || '/assets/chair.jpg'" :alt="item.productName" /></span>
            <span class="activity-card-copy"><em>已收藏</em><strong>{{ item.productName }}</strong><small>{{ item.subtitle || '商品详情以页面展示为准' }}</small><b>¥{{ money(item.price) }}</b></span>
          </router-link>
          <div v-else class="activity-card-main" aria-disabled="true">
            <span class="activity-card-image"><img :src="item.mainImage || '/assets/chair.jpg'" :alt="item.productName" /><i>已失效</i></span>
            <span class="activity-card-copy"><em>商品已失效</em><strong>{{ item.productName }}</strong><small>该商品已下架或删除，暂时不能查看和购买</small><b>¥{{ money(item.price) }}</b></span>
          </div>
          <button class="activity-remove" type="button" :disabled="busyId === item.spuId" :aria-label="`取消收藏${item.productName}`" title="取消收藏" @click="unfavorite(item)"><VanIcon name="like" /></button>
        </article>
      </div>

      <div v-else-if="activeTab === 'history' && history.length" class="history-groups">
        <section v-for="group in historyGroups" :key="group.key" class="history-group">
          <div class="history-date"><h2>{{ group.label }}</h2><span>{{ group.items.length }} 件商品</span></div>
          <div class="activity-grid">
            <article v-for="item in group.items" :key="item.spuId" class="activity-card history-card" :class="{ invalid: !item.available, selected: selectedIds.has(item.spuId) }">
              <label class="history-select"><input type="checkbox" :checked="selectedIds.has(item.spuId)" :aria-label="`选择${item.productName}`" @change="toggleSelected(item.spuId)" /></label>
              <router-link v-if="item.available" class="activity-card-main" :to="`/product/${item.spuId}`">
                <span class="activity-card-image"><img :src="item.mainImage || '/assets/chair.jpg'" :alt="item.productName" /></span>
                <span class="activity-card-copy"><em>{{ timeLabel(item.lastViewTime) }} · 浏览 {{ item.viewCount }} 次</em><strong>{{ item.productName }}</strong><small>{{ item.subtitle || '商品详情以页面展示为准' }}</small><b>¥{{ money(item.price) }}</b></span>
              </router-link>
              <div v-else class="activity-card-main" aria-disabled="true">
                <span class="activity-card-image"><img :src="item.mainImage || '/assets/chair.jpg'" :alt="item.productName" /><i>已失效</i></span>
                <span class="activity-card-copy"><em>{{ timeLabel(item.lastViewTime) }} · 浏览 {{ item.viewCount }} 次</em><strong>{{ item.productName }}</strong><small>该商品已下架或删除，暂时不能查看和购买</small><b>¥{{ money(item.price) }}</b></span>
              </div>
              <button class="activity-remove" type="button" :disabled="busyId === item.spuId" :aria-label="`删除${item.productName}的浏览记录`" title="删除记录" @click="deleteOne(item)"><VanIcon name="delete-o" /></button>
            </article>
          </div>
        </section>
      </div>

      <div v-else class="empty-state activity-empty">
        <VanIcon :name="activeTab === 'favorites' ? 'like-o' : 'clock-o'" />
        <h2>{{ activeTab === 'favorites' ? '还没有收藏商品' : '还没有浏览足迹' }}</h2>
        <p>{{ activeTab === 'favorites' ? '在商品详情点击收藏，喜欢的商品就会出现在这里。' : '登录后浏览商品，最近看过的内容会保留在这里。' }}</p>
        <router-link class="primary-button" to="/catalog">去逛逛 <span>→</span></router-link>
      </div>
    </template>
  </section>
</template>

<style scoped>
.activity-page{width:min(1184px,calc(100% - 56px));margin:auto;padding:30px 0 90px}.activity-intro{display:flex;align-items:flex-end;justify-content:space-between;gap:24px}.activity-intro h1{margin-bottom:8px}.activity-intro p{margin:0;color:var(--muted)}.activity-tabs{display:grid;grid-template-columns:repeat(2,minmax(0,220px));gap:10px;margin:30px 0 22px}.activity-tabs button{display:flex;min-height:48px;align-items:center;justify-content:center;gap:8px;border:2px solid var(--ink);border-radius:8px;background:var(--paper);color:var(--ink);font-weight:800}.activity-tabs button.active{background:var(--yellow);box-shadow:4px 4px 0 var(--ink)}.activity-tabs span{display:grid;min-width:20px;height:20px;place-items:center;border-radius:10px;background:var(--ink);color:#fff;font-size:10px}.activity-toolbar{display:flex;min-height:52px;align-items:center;gap:16px;border-block:1px solid var(--line);margin-bottom:24px;color:var(--muted);font-size:12px}.activity-toolbar label{display:flex;align-items:center;gap:6px;color:var(--ink)}.activity-toolbar input,.history-select input{width:17px;height:17px;accent-color:var(--ink)}.activity-toolbar button{display:inline-flex;align-items:center;gap:4px;border:0;background:transparent;color:var(--ink);font-size:12px}.activity-toolbar button:disabled{opacity:.4}.activity-toolbar .clear-history-button{margin-left:auto;color:var(--red)}.activity-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:18px}.activity-card{position:relative;min-width:0;overflow:hidden;border:2px solid var(--ink);border-radius:8px;background:var(--paper);box-shadow:5px 5px 0 rgb(32 32 42 / 14%)}.activity-card-main{display:block;color:var(--ink)}.activity-card-image{position:relative;display:block;aspect-ratio:4/3;overflow:hidden;background:#e8e6df}.activity-card-image img{width:100%;height:100%;object-fit:cover;transition:transform .22s ease}.activity-card-main:hover .activity-card-image img{transform:scale(1.035)}.activity-card-image i{position:absolute;inset:0;display:grid;place-items:center;background:rgb(32 32 42 / 55%);color:#fff;font-style:normal;font-weight:900}.activity-card-copy{display:grid;min-height:150px;align-content:start;gap:6px;padding:16px 52px 16px 16px}.activity-card-copy em{color:var(--red);font-size:11px;font-style:normal;font-weight:800}.activity-card-copy strong{overflow:hidden;font-size:16px;text-overflow:ellipsis;white-space:nowrap}.activity-card-copy small{display:-webkit-box;overflow:hidden;color:var(--muted);font-size:11px;line-height:1.5;-webkit-box-orient:vertical;-webkit-line-clamp:2}.activity-card-copy b{margin-top:6px;font-size:16px}.activity-remove{position:absolute;right:12px;bottom:14px;display:grid;width:34px;height:34px;place-items:center;border:2px solid var(--ink);border-radius:7px;background:var(--paper);color:var(--red);font-size:18px}.activity-remove:hover:not(:disabled){background:var(--yellow);box-shadow:2px 2px 0 var(--ink);transform:translate(-1px,-1px)}.activity-remove:disabled{opacity:.45}.activity-card.invalid{border-color:#8c8982;box-shadow:none}.activity-card.invalid .activity-card-copy{opacity:.72}.history-groups{display:grid;gap:36px}.history-group{display:grid;gap:14px}.history-date{display:flex;align-items:baseline;gap:10px;border-bottom:2px solid var(--ink);padding-bottom:10px}.history-date h2{margin:0;font-size:20px}.history-date span{color:var(--muted);font-size:11px}.history-card.selected{outline:4px solid var(--yellow);outline-offset:2px}.history-select{position:absolute;z-index:2;top:12px;left:12px;display:grid;width:32px;height:32px;place-items:center;border:2px solid var(--ink);border-radius:7px;background:var(--paper)}.activity-empty>.van-icon{font-size:38px;color:var(--red)}.activity-empty .primary-button{display:inline-flex;margin-top:10px}.empty-state.compact{min-height:240px}@media(max-width:900px){.activity-page{width:calc(100% - 32px);padding:22px 0 82px}.activity-grid{grid-template-columns:repeat(2,minmax(0,1fr))}.activity-card-main:hover .activity-card-image img{transform:none}}@media(max-width:620px){.activity-intro{align-items:flex-start;flex-direction:column}.activity-tabs{grid-template-columns:repeat(2,minmax(0,1fr));margin-top:22px}.activity-tabs button{font-size:12px}.activity-toolbar{align-items:flex-start;gap:10px;padding:12px 0;flex-wrap:wrap}.activity-toolbar .clear-history-button{margin-left:0}.activity-grid{grid-template-columns:1fr}.activity-card{display:grid;grid-template-columns:112px minmax(0,1fr);min-height:132px}.activity-card-main{display:contents}.activity-card-image{height:100%;min-height:132px;aspect-ratio:auto}.activity-card-copy{min-height:132px;padding:14px 48px 14px 12px}.activity-card-copy strong{font-size:14px}.activity-remove{right:10px;bottom:12px}.history-select{top:8px;left:8px}.history-date h2{font-size:17px}}
</style>
