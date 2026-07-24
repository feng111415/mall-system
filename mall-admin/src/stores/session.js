import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export const useSessionStore = defineStore('admin-session', () => {
  const operator = ref({ name: '运营管理员', role: '商品与订单管理员' })
  const unread = ref(3)
  const displayName = computed(() => operator.value.name)
  return { operator, unread, displayName }
})
