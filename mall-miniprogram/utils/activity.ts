const productUtils = require('./product')

function parseTime(value: unknown) {
  if (!value) return null
  if (typeof value === 'object' && typeof (value as { getTime?: unknown }).getTime === 'function') {
    const timestamp = (value as { getTime: () => number }).getTime()
    return Number.isNaN(timestamp) ? null : new Date(timestamp)
  }
  const date = new Date(String(value).replace(' ', 'T'))
  return Number.isNaN(date.getTime()) ? null : date
}

function dateKey(value: unknown) {
  const date = parseTime(value)
  if (!date) return 'unknown'
  const pad = (number: number) => String(number).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function dateLabel(key: string, now = new Date()) {
  if (key === 'unknown') return '更早'
  const today = dateKey(now)
  const yesterday = dateKey(new Date(now.getTime() - 86400000))
  if (key === today) return '今天'
  if (key === yesterday) return '昨天'
  const parts = key.split('-').map(Number)
  return parts.length === 3 ? `${parts[1]} 月 ${parts[2]} 日` : key
}

function timeLabel(value: unknown) {
  const date = parseTime(value)
  if (!date) return ''
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function normalizeActivityItem(value: Record<string, any>) {
  const available = value.available === true
  return {
    ...value,
    available,
    displayImage: productUtils.localProductImage(value.mainImage),
    displayPrice: productUtils.formatPrice(value.price),
    displayFavoriteTime: timeLabel(value.favoriteTime),
    displayViewTime: timeLabel(value.lastViewTime),
    viewCount: Math.max(0, Number(value.viewCount || 0)),
    selected: false
  }
}

function groupHistory(values: Array<Record<string, any>>, now = new Date()) {
  const groups = new Map<string, Array<Record<string, any>>>()
  ;(values || []).map(normalizeActivityItem).forEach(item => {
    const key = dateKey((item as Record<string, any>).lastViewTime)
    if (!groups.has(key)) groups.set(key, [])
    groups.get(key)!.push(item)
  })
  return Array.from(groups.entries()).map(([key, items]) => ({ key, label: dateLabel(key, now), items }))
}

module.exports = { normalizeActivityItem, groupHistory, dateKey, dateLabel, timeLabel }
