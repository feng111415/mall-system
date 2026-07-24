import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

export const useCartStore = defineStore('storefront-cart', () => {
  const items = ref([
    { id: 1, name: '北欧原木餐椅', price: 399, quantity: 1, image: '/assets/chair.jpg', stock: 8 },
    { id: 2, name: '轻量城市跑鞋', price: 299, quantity: 1, image: '/assets/sneaker.jpg', stock: 0 }
  ])
  const availableItems = computed(() => items.value.filter(item => item.stock > 0))
  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalPrice = computed(() => availableItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))
  function add(item) {
    if (item.stock < 1) return false
    const found = items.value.find(existing => existing.id === item.id)
    if (found) found.quantity = Math.min(found.quantity + 1, found.stock)
    else items.value.push({ ...item, quantity: 1 })
    return true
  }
  function remove(id) { items.value = items.value.filter(item => item.id !== id) }
  function setQuantity(item, quantity) { item.quantity = Math.max(1, Math.min(Number(quantity) || 1, item.stock || 1)) }
  return { items, availableItems, totalCount, totalPrice, add, remove, setQuantity }
})
