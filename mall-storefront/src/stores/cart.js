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
  function reset() { apply(null) }
  async function load() {
    if (!sessionStorage.getItem('mall-user-token')) { reset(); return }
    loading.value = true
    try { apply((await getCart()).data.data) }
    catch (error) { if (!sessionStorage.getItem('mall-user-token')) reset(); throw error }
    finally { loading.value = false }
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
  async function setSelection(nextItems, selected) {
    if (!nextItems?.length) return
    await Promise.all(nextItems.map(item => updateCartSelected(item.skuId, selected)))
    await load()
  }
  async function removeMany(nextItems) {
    if (!nextItems?.length) return
    await Promise.all(nextItems.map(item => removeCartItem(item.skuId)))
    await load()
  }
  return { items, loading, totalCount, totalPrice, canCheckout, availableItems, load, reset, add, remove, setQuantity, setSelected, setSelection, removeMany }
})
