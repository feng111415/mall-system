import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getAdminInfo, loginAdmin } from '../api/auth'

export const useSessionStore = defineStore('admin-session', () => {
  const token = ref(sessionStorage.getItem('mall-admin-token') || '')
  const operator = ref({ name: '运营管理员', role: '商城运营' })
  const unread = ref(0)
  const displayName = computed(() => operator.value.name)

  async function login(payload) {
    const response = await loginAdmin(payload)
    token.value = response.data.token
    sessionStorage.setItem('mall-admin-token', token.value)
    await loadProfile()
  }

  async function loadProfile() {
    if (!token.value) return
    const response = await getAdminInfo()
    operator.value = {
      name: response.data.user?.nickName || response.data.user?.userName || '运营管理员',
      role: response.data.roles?.[0] || '商城运营'
    }
  }

  function logout() {
    token.value = ''
    sessionStorage.removeItem('mall-admin-token')
  }

  return { token, operator, unread, displayName, login, loadProfile, logout }
})
