import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getMessageSummary } from '../api/message'

export const useMessageStore = defineStore('storefront-message', () => {
  const summaries = ref([])
  const loading = ref(false)
  const unreadCount = computed(() => summaries.value.reduce((total, item) => total + Number(item.unreadCount || 0), 0))

  async function load() {
    if (!sessionStorage.getItem('mall-user-token')) {
      summaries.value = []
      return
    }
    loading.value = true
    try {
      summaries.value = (await getMessageSummary()).data.data || []
    } finally {
      loading.value = false
    }
  }

  function reset() { summaries.value = [] }

  return { summaries, loading, unreadCount, load, reset }
})
