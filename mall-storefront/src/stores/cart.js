import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { addCartItem, getCart, removeCartItem, updateCartQuantity, updateCartSelected } from '../api/cart'

export const useCartStore = defineStore('storefront-cart', () => {
  const items = ref([])
  const loading = ref(false)
  const totalCount = ref(0)
  const totalPrice = ref(0)
  const canCheckout = ref(false)
  const availableItems = computed(() => items.value.filter(item => item.valid && !item.stockShortage))

  function apply(data) {
    items.value = data?.items || []
    totalCount.value = data?.totalCount || 0
    totalPrice.value = Number(data?.totalPrice || 0)
    canCheckout.value = Boolean(data?.canCheckout)
  }
  async function load() {
    if (!sessionStorage.getItem('mall-user-token')) { apply(null); return }
    loading.value = true
    try { apply((await getCart()).data.data) } finally { loading.value = false }
  }
  async function add(item, quantity = 1) {
    if (!item?.skuId) return false
    await addCartItem(item.skuId, quantity)
    await load()
    return true
  }
  async function remove(item) { await removeCartItem(item.skuId); await load() }
  async function setQuantity(item, quantity) { await updateCartQuantity(item.skuId, Number(quantity)); await load() }
  async function setSelected(item, selected) { await updateCartSelected(item.skuId, selected); await load() }
  return { items, loading, totalCount, totalPrice, canCheckout, availableItems, load, add, remove, setQuantity, setSelected }
})
