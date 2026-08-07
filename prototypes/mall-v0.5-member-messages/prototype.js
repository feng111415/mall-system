const messages = [
  { type: '订单', icon: '单', title: '订单 M202608061026 已创建', body: '订单已提交，请在 30 分钟内完成支付。', time: '刚刚', unread: true },
  { type: '物流', icon: '运', title: '包裹已到达派送网点', body: '顺丰速运正在为你安排派送。', time: '今天 09:42', unread: true },
  { type: '售后', icon: '售', title: '售后申请已提交', body: '客服将在 24 小时内完成审核。', time: '昨天 18:20', unread: false },
  { type: '账户', icon: '安', title: '登录设备发生变化', body: '检测到一台新的移动设备登录。', time: '08-05', unread: false },
  { type: '订单', icon: '单', title: '订单 M202608041318 已完成', body: '感谢你的购买，欢迎评价商品。', time: '08-04', unread: false }
]
const variants = {
  A: 'A · 分类消息列表',
  B: 'B · 业务会话聚合',
  C: 'C · 订单时间线'
}
const app = document.querySelector('#app')
const label = document.querySelector('#variant-label')
let detail = null

function shell(content) {
  return `<div class="page"><header class="mini-header"><div><h1>消息中心</h1><p>订单、物流和账户动态都在这里</p></div><button class="header-action" data-action="read-all" type="button">全部已读</button></header>${content}<nav class="bottom-tabs"><a href="#"><b>⌂</b>首页</a><a href="#"><b>▦</b>商品</a><a href="#"><b>▣</b>订单</a><a class="active" href="#"><b>●</b>消息</a><a href="#"><b>○</b>我的</a></nav></div>`
}

function summary() {
  return `<section class="summary"><article class="summary-item primary"><strong>2</strong><span>未读消息</span><small>有 2 条需要查看</small></article><article class="summary-item"><strong>1</strong><span>待处理售后</span><small>审核进行中</small></article><article class="summary-item"><strong>1</strong><span>运输中包裹</span><small>预计今日送达</small></article></section>`
}

function tabs() {
  return `<div class="tabs" role="tablist"><button class="active" type="button">全部 <b>2</b></button><button type="button">订单</button><button type="button">物流</button><button type="button">售后</button><button type="button">账户</button></div>`
}

function row(item) {
  return `<button class="message-row" data-message="${item.title}" type="button"><span class="message-icon">${item.icon}</span><span class="message-copy"><strong>${item.title}</strong><p>${item.body}</p></span><span class="message-meta">${item.unread ? '<i class="unread-dot"></i>' : ''}<time>${item.time}</time></span></button>`
}

function variantA() {
  return shell(`${summary()}<section class="section-head"><h2>全部消息</h2><span>点击消息查看详情</span></section>${tabs()}<section class="message-list">${messages.map(row).join('')}</section>`)
}

function variantB() {
  const groups = [
    ['订单消息', '订单创建、支付和完成状态', '订单', 2],
    ['物流提醒', '包裹节点和收货提醒', '物流', 1],
    ['售后进度', '审核、退货和退款状态', '售后', 0],
    ['账户安全', '登录、设备和资料变更', '账户', 0]
  ]
  return shell(`${summary()}<section class="section-head"><h2>按业务查看</h2><span>聚合展示，减少打扰</span></section><section class="conversation-list">${groups.map(([title, body, icon, count]) => `<button class="conversation" data-message="${title}" type="button"><span class="conversation-icon">${icon}</span><span class="conversation-copy"><strong>${title}</strong><p>${body}</p></span>${count ? `<b class="conversation-count">${count}</b>` : '<span></span>'}</button>`).join('')}</section>`)
}

function variantC() {
  return shell(`${summary()}<section class="section-head"><h2>最近动态</h2><span>按时间倒序</span></section><section class="timeline">${messages.map(item => `<button class="timeline-item" data-message="${item.title}" type="button"><span class="timeline-dot">${item.icon}</span><span class="timeline-copy"><strong>${item.title}${item.unread ? ' · 未读' : ''}</strong><p>${item.body}</p><time>${item.time}</time></span></button>`).join('')}</section>`)
}

function showVariant(key) {
  const render = key === 'B' ? variantB : key === 'C' ? variantC : variantA
  app.innerHTML = render()
  label.textContent = variants[key]
  app.querySelectorAll('[data-message]').forEach(node => node.addEventListener('click', () => openDetail(node.dataset.message)))
  app.querySelector('[data-action="read-all"]')?.addEventListener('click', event => { event.currentTarget.textContent = '已全部读'; event.currentTarget.disabled = true })
}

function openDetail(title) {
  closeDetail()
  const item = messages.find(value => value.title === title) || { title, body: '这里展示对应业务消息的完整内容和操作入口。' }
  detail = document.createElement('section')
  detail.className = 'detail-sheet'
  detail.innerHTML = `<button class="detail-close" type="button" aria-label="关闭详情">×</button><h2>${item.title}</h2><p>${item.body}<br />消息详情、业务单号和下一步操作会在正式版本中根据消息类型展示。</p>`
  detail.querySelector('button').addEventListener('click', closeDetail)
  document.body.append(detail)
}

function closeDetail() { detail?.remove(); detail = null }

let current = new URLSearchParams(location.search).get('variant')?.toUpperCase() || 'A'
if (!variants[current]) current = 'A'
showVariant(current)
document.querySelector('#prev').addEventListener('click', () => { current = current === 'A' ? 'C' : String.fromCharCode(current.charCodeAt(0) - 1); history.replaceState(null, '', `?variant=${current}`); showVariant(current) })
document.querySelector('#next').addEventListener('click', () => { current = current === 'C' ? 'A' : String.fromCharCode(current.charCodeAt(0) + 1); history.replaceState(null, '', `?variant=${current}`); showVariant(current) })
