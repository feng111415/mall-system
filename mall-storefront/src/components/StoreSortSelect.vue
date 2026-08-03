<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Icon as VanIcon } from 'vant'

const props = defineProps({
  modelValue: { type: String, default: 'default' }
})
const emit = defineEmits(['update:modelValue'])

const options = [
  { value: 'default', label: '综合推荐' },
  { value: 'sales', label: '销量优先' },
  { value: 'priceAsc', label: '价格从低到高' },
  { value: 'priceDesc', label: '价格从高到低' }
]
const root = ref(null)
const trigger = ref(null)
const optionElements = ref([])
const open = ref(false)
const current = computed(() => options.find(item => item.value === props.modelValue) || options[0])
let mobileMedia

function setOptionElement(element, index) {
  if (element) optionElements.value[index] = element
}

function syncBodyLock() {
  document.body.classList.toggle('sort-sheet-open', open.value && Boolean(mobileMedia?.matches))
}

async function showMenu(focusIndex = -1) {
  open.value = true
  syncBodyLock()
  if (focusIndex >= 0) {
    await nextTick()
    optionElements.value[focusIndex]?.focus()
  }
}

function closeMenu({ restoreFocus = false } = {}) {
  if (!open.value) return
  open.value = false
  syncBodyLock()
  if (restoreFocus) nextTick(() => trigger.value?.focus())
}

function toggleMenu() {
  if (open.value) closeMenu()
  else showMenu()
}

function selectOption(option) {
  if (option.value !== props.modelValue) emit('update:modelValue', option.value)
  closeMenu({ restoreFocus: true })
}

function handleTriggerKeydown(event) {
  if (!['ArrowDown', 'ArrowUp'].includes(event.key)) return
  event.preventDefault()
  const selectedIndex = Math.max(0, options.findIndex(item => item.value === props.modelValue))
  showMenu(event.key === 'ArrowDown' ? selectedIndex : options.length - 1)
}

function handleOptionKeydown(event, index) {
  if (event.key === 'Escape') {
    event.preventDefault()
    closeMenu({ restoreFocus: true })
    return
  }
  if (!['ArrowDown', 'ArrowUp', 'Home', 'End'].includes(event.key)) return
  event.preventDefault()
  let nextIndex = index
  if (event.key === 'ArrowDown') nextIndex = (index + 1) % options.length
  if (event.key === 'ArrowUp') nextIndex = (index - 1 + options.length) % options.length
  if (event.key === 'Home') nextIndex = 0
  if (event.key === 'End') nextIndex = options.length - 1
  optionElements.value[nextIndex]?.focus()
}

function handleDocumentPointerdown(event) {
  if (open.value && !root.value?.contains(event.target)) closeMenu()
}

function handleDocumentKeydown(event) {
  if (open.value && event.key === 'Escape') closeMenu({ restoreFocus: true })
}

function handleMediaChange() {
  syncBodyLock()
}

onMounted(() => {
  mobileMedia = window.matchMedia('(max-width: 900px)')
  mobileMedia.addEventListener('change', handleMediaChange)
  document.addEventListener('pointerdown', handleDocumentPointerdown)
  document.addEventListener('keydown', handleDocumentKeydown)
})

onBeforeUnmount(() => {
  open.value = false
  syncBodyLock()
  mobileMedia?.removeEventListener('change', handleMediaChange)
  document.removeEventListener('pointerdown', handleDocumentPointerdown)
  document.removeEventListener('keydown', handleDocumentKeydown)
})

watch(() => props.modelValue, () => closeMenu())
</script>

<template>
  <div ref="root" :class="['sort-control', { open }]">
    <span class="sort-label">排序</span>
    <button ref="trigger" class="sort-trigger" type="button" aria-haspopup="listbox" :aria-expanded="open" aria-controls="store-sort-options" @click="toggleMenu" @keydown="handleTriggerKeydown">
      <b>{{ current.label }}</b><VanIcon name="arrow-down" />
    </button>
    <button class="sort-sheet-backdrop" type="button" aria-label="关闭排序菜单" tabindex="-1" @click="closeMenu()"></button>
    <div id="store-sort-options" class="sort-menu" role="listbox" aria-label="商品排序" :aria-hidden="!open">
      <header><strong>选择排序方式</strong><button class="sort-close" type="button" aria-label="关闭排序菜单" @click="closeMenu({ restoreFocus: true })"><VanIcon name="cross" /></button></header>
      <button v-for="(option, index) in options" :key="option.value" :ref="element => setOptionElement(element, index)" type="button" role="option" :aria-selected="option.value === modelValue" :class="{ selected: option.value === modelValue }" @click="selectOption(option)" @keydown="handleOptionKeydown($event, index)">
        <span>{{ option.label }}</span><VanIcon v-if="option.value === modelValue" name="success" />
      </button>
    </div>
  </div>
</template>
