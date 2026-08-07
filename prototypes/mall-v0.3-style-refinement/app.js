const variants = [
  { key: 'c1plus', label: 'C1+ · 青春波普商店' },
  { key: 'c1', label: 'C1 · 复古波普杂志' },
  { key: 'c2', label: 'C2 · 日系潮玩商店' },
  { key: 'c3', label: 'C3 · 轻运动街头' }
]

const sections = [...document.querySelectorAll('[data-variant]')]
const label = document.querySelector('#variant-label')

function selectedVariant() {
  const requested = new URLSearchParams(window.location.search).get('variant')?.toLowerCase()
  return variants.find(item => item.key === requested) || variants[0]
}

function render(key, updateUrl = true) {
  const variant = variants.find(item => item.key === key) || variants[0]
  sections.forEach(section => { section.hidden = section.dataset.variant !== variant.key })
  label.textContent = variant.label
  document.title = `${variant.label} - 商城 V0.3 风格细化原型`
  if (updateUrl) {
    const url = new URL(window.location.href)
    url.searchParams.set('variant', variant.key)
    window.history.replaceState({}, '', url)
  }
  requestAnimationFrame(() => document.querySelector(`[data-variant="${variant.key}"] [data-autofocus]`)?.focus())
}

function cycle(direction) {
  const current = selectedVariant()
  const index = variants.findIndex(item => item.key === current.key)
  render(variants[(index + direction + variants.length) % variants.length].key)
}

document.querySelector('#previous-variant').addEventListener('click', () => cycle(-1))
document.querySelector('#next-variant').addEventListener('click', () => cycle(1))
document.addEventListener('keydown', event => {
  if (event.target.matches('input, textarea, [contenteditable="true"]')) return
  if (event.key === 'ArrowLeft') cycle(-1)
  if (event.key === 'ArrowRight') cycle(1)
})
window.addEventListener('popstate', () => render(selectedVariant().key, false))

render(selectedVariant().key, false)
