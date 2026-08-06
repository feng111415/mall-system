<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Icon as VanIcon } from 'vant'
import { getMessages, markAllMessagesRead, markMessageRead } from '../api/message'
import { useMessageStore } from '../stores/message'
import { useNoticeStore } from '../stores/notice'

const route = useRoute()
const router = useRouter()
const messageStore = useMessageStore()
const notice = useNoticeStore()
const messages = ref([])
const loading = ref(true)
const errorMessage = ref('')
const selected = ref(null)
const activeCategory = computed(() => String(route.query.category || '').toUpperCase())
const currentSummary = computed(() => messageStore.summaries.find(item => item.category === activeCategory.value))
const categoryMeta = {
  ORDER: { icon: 'orders-o', tone: 'coral' },
  LOGISTICS: { icon: 'logistics', tone: 'mint' },
  AFTER_SALE: { icon: 'after-sale', tone: 'yellow' },
  ACCOUNT: { icon: 'shield-o', tone: 'sky' }
}

watch(() => route.query.category, load, { immediate: true })

async function load() {
  if (!sessionStorage.getItem('mall-user-token')) return router.replace('/account')
  loading.value = true
  errorMessage.value = ''
  selected.value = null
  try {
    await messageStore.load()
    messages.value = activeCategory.value ? (await getMessages(activeCategory.value)).data.data || [] : []
  } catch (error) {
    errorMessage.value = error.response?.data?.msg || '消息读取失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function openCategory(category) {
  router.push({ path: '/messages', query: { category } })
}

async function openMessage(item) {
  selected.value = item
  if (item.readFlag === '1') return
  try {
    await markMessageRead(item.messageId)
    item.readFlag = '1'
    await messageStore.load()
  } catch (error) {
    notice.show(error.response?.data?.msg || '消息已读状态更新失败', 'error')
  }
}

async function readAll() {
  try {
    await markAllMessagesRead(activeCategory.value)
    messages.value.forEach(item => { item.readFlag = '1' })
    await messageStore.load()
    notice.show('本组消息已全部标记为已读')
  } catch (error) {
    notice.show(error.response?.data?.msg || '全部已读失败，请稍后重试', 'error')
  }
}

function goBusiness() {
  const path = selected.value?.actionPath
  selected.value = null
  if (path) router.push(path)
}

function formatTime(value) {
  if (!value) return ''
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false
  }).format(new Date(value))
}
</script>

<template>
  <section class="message-wrap">
    <div class="message-heading">
      <button v-if="activeCategory" class="message-back" type="button" aria-label="返回消息中心" title="返回消息中心" @click="router.push('/messages')"><VanIcon name="arrow-left" /></button>
      <div>
        <span class="section-kicker">会员消息</span>
        <h1>{{ currentSummary?.label || '消息中心' }}</h1>
        <p>{{ currentSummary?.description || '按业务查看订单、物流、售后和账户动态。' }}</p>
      </div>
      <button v-if="activeCategory && currentSummary?.unreadCount" class="message-read-all" type="button" @click="readAll">全部已读</button>
    </div>

    <p v-if="errorMessage" class="form-message" role="alert">{{ errorMessage }}</p>
    <p v-if="loading" class="loading-note">正在读取消息...</p>

    <div v-else-if="!activeCategory" class="message-conversations">
      <button v-for="item in messageStore.summaries" :key="item.category" class="message-conversation" type="button" @click="openCategory(item.category)">
        <span :class="['message-conversation-icon', categoryMeta[item.category]?.tone]"><VanIcon :name="categoryMeta[item.category]?.icon" /></span>
        <span class="message-conversation-copy">
          <span><strong>{{ item.label }}</strong><b v-if="item.unreadCount">{{ item.unreadCount > 99 ? '99+' : item.unreadCount }}</b></span>
          <small>{{ item.latestSummary || item.description }}</small>
          <em v-if="item.latestTime">{{ formatTime(item.latestTime) }}</em>
        </span>
        <VanIcon name="arrow" />
      </button>
    </div>

    <div v-else-if="messages.length" class="message-list">
      <button v-for="item in messages" :key="item.messageId" :class="['message-item', { unread: item.readFlag !== '1' }]" type="button" @click="openMessage(item)">
        <span class="message-unread-dot" aria-hidden="true"></span>
        <span class="message-item-copy"><strong>{{ item.title }}</strong><small>{{ item.summary }}</small><em>{{ formatTime(item.createTime) }}</em></span>
        <VanIcon name="arrow" />
      </button>
    </div>

    <div v-else-if="activeCategory" class="message-empty">
      <VanIcon :name="categoryMeta[activeCategory]?.icon || 'chat-o'" />
      <h2>暂时没有消息</h2>
      <p>新的业务进度会出现在这里。</p>
    </div>

    <div v-if="selected" class="message-sheet-backdrop" role="presentation" @click.self="selected = null">
      <section class="message-sheet" role="dialog" aria-modal="true" aria-labelledby="message-sheet-title">
        <div class="message-sheet-head">
          <span :class="['message-conversation-icon', categoryMeta[selected.category]?.tone]"><VanIcon :name="categoryMeta[selected.category]?.icon" /></span>
          <div><small>{{ currentSummary?.label }}</small><h2 id="message-sheet-title">{{ selected.title }}</h2></div>
          <button type="button" aria-label="关闭消息详情" title="关闭" @click="selected = null"><VanIcon name="cross" /></button>
        </div>
        <p>{{ selected.content || selected.summary }}</p>
        <time>{{ formatTime(selected.createTime) }}</time>
        <button v-if="selected.actionPath" class="primary-button message-sheet-action" type="button" @click="goBusiness">查看业务详情 <VanIcon name="arrow" /></button>
      </section>
    </div>
  </section>
</template>
