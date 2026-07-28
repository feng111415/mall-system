import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useNoticeStore = defineStore('storefront-notice', () => {
  const message = ref('')
  const type = ref('success')
  let timer

  function show(nextMessage, nextType = 'success', duration = 2400) {
    window.clearTimeout(timer)
    message.value = nextMessage
    type.value = nextType
    if (duration > 0) timer = window.setTimeout(clear, duration)
  }

  function clear() {
    window.clearTimeout(timer)
    message.value = ''
  }

  return { message, type, show, clear }
})
