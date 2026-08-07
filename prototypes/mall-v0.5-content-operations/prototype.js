const variants = {
  A: {
    label: 'A · 预览工作台',
    html: `<div class="variant-title"><div><h2>编辑首页主视觉</h2><p>配置后即时查看桌面端与移动端的展示结果</p></div><span class="tag draft">草稿 · 最近保存 10:42</span></div><div class="workspace-grid"><div class="panel"><h3>主视觉内容</h3><div class="field"><label>内容标题</label><input value="今天，也要挑点喜欢的。" /></div><div class="field"><label>辅助文案</label><textarea>好看、好用，还能让普通一天有一点新鲜。</textarea></div><div class="field"><label>按钮文字</label><input value="开始逛逛" /></div><div class="field"><label>跳转目标</label><select><option>全部商品 · 销量优先</option><option>指定商品 · 城市运动鞋</option></select></div><div class="check-row"><input type="checkbox" checked /> <span>同时展示公告条</span></div></div><div class="panel"><h3>实时预览</h3><div class="preview-frame"><div class="preview-head"><span>首页 · 桌面端</span><span>移动端预览</span></div><div class="preview-hero"><div class="preview-hero-copy"><span>NEW DROP</span><h4>今天，也要<br />挑点喜欢的。</h4></div><div class="preview-hero-photo"><img src="http://localhost:5174/assets/sneaker.jpg" alt="" /></div></div><div class="preview-tiles"><div class="preview-tile"><img src="http://localhost:5174/assets/lamp.jpg" alt="" /></div><div class="preview-tile"><img src="http://localhost:5174/assets/camera.jpg" alt="" /></div><div class="preview-tile"><img src="http://localhost:5174/assets/backpack.jpg" alt="" /></div></div></div><p class="prototype-note">当前展示 C1+ 拼贴模板，图片内容来自商城真实资源。</p></div><div class="panel publish-card"><h3>发布设置</h3><div class="status-banner"><strong>待发布草稿</strong><br />计划 08 月 12 日 09:00 生效<br />下线时间：不设定</div><div class="field"><label>生效时间</label><input value="2026-08-12 09:00" /></div><div class="field"><label>下线时间</label><input placeholder="可选" /></div><div class="action-row"><button>保存草稿</button><button class="primary">发布排期</button></div><div class="metric-list"><div class="metric"><span>当前线上版本</span><strong>春日上新</strong></div><div class="metric"><span>最近发布人</span><strong>运营主管</strong></div></div></div></div>`
  },
  B: {
    label: 'B · 内容清单',
    html: `<div class="variant-title"><div><h2>首页内容清单</h2><p>以状态、排期和负责人为主，适合批量运营</p></div><button class="screen-switcher primary">＋ 新建内容</button></div><div class="table-panel"><div class="table-tools"><input placeholder="搜索内容名称或跳转目标" /><button>类型 ▾</button><button>状态 ▾</button><button>排期 ▾</button><button class="primary">批量发布</button></div><table class="content-table"><thead><tr><th>内容</th><th>类型</th><th>状态</th><th>生效时间</th><th>下线时间</th><th>更新人</th><th>操作</th></tr></thead><tbody><tr><td><img class="thumb" src="http://localhost:5174/assets/sneaker.jpg" alt="" /> <strong>八月上新主视觉</strong></td><td>主视觉</td><td><span class="tag live">线上</span></td><td>2026-08-05 09:00</td><td>2026-08-31 23:59</td><td>运营主管</td><td><div class="text-actions"><button>预览</button><button>编辑</button></div></td></tr><tr><td><img class="thumb" src="http://localhost:5174/assets/coffee.jpg" alt="" /> <strong>周末咖啡推荐</strong></td><td>推荐专区</td><td><span class="tag draft">草稿</span></td><td>2026-08-09 10:00</td><td>不设定</td><td>商品运营</td><td><div class="text-actions"><button>预览</button><button>编辑</button></div></td></tr><tr><td><img class="thumb" src="http://localhost:5174/assets/backpack.jpg" alt="" /> <strong>通勤季公告条</strong></td><td>公告条</td><td><span class="tag off">已停用</span></td><td>2026-07-01 00:00</td><td>2026-07-31 23:59</td><td>商品运营</td><td><div class="text-actions"><button>复制</button><button>编辑</button></div></td></tr></tbody></table></div><div class="prototype-note">服务端发布时校验时间、状态和内部跳转目标；下架商品不会继续出现在推荐位。</div>`
  },
  C: {
    label: 'C · 页面编排',
    html: `<div class="variant-title"><div><h2>首页页面编排</h2><p>按页面区块查看内容顺序，拖动前先保留草稿</p></div><div class="action-row"><button>预览移动端</button><button class="primary">保存草稿</button></div></div><div class="board"><div class="board-column"><h3>可用内容</h3><div class="board-card"><strong>八月上新主视觉</strong><small>主视觉 · 已排期</small><img src="http://localhost:5174/assets/sneaker.jpg" alt="" /></div><div class="board-card"><strong>通勤季公告条</strong><small>公告条 · 已停用</small><img src="http://localhost:5174/assets/backpack.jpg" alt="" /></div><div class="board-card"><strong>周末咖啡推荐</strong><small>推荐专区 · 草稿</small><img src="http://localhost:5174/assets/coffee.jpg" alt="" /></div></div><div class="board-column"><h3>首页顺序 · 桌面端</h3><div class="section-card"><header><strong>01　主视觉</strong><span>可配置</span></header><small>八月上新主视觉</small></div><div class="section-card"><header><strong>02　公告条</strong><span>可配置</span></header><small>满 199 元免运费 · 每周二上新 · 7 天轻松退换</small></div><div class="section-card"><header><strong>03　推荐专区</strong><span>4 个商品</span></header><div class="mini-product-row"><img src="http://localhost:5174/assets/lamp.jpg" alt="" /><img src="http://localhost:5174/assets/camera.jpg" alt="" /><img src="http://localhost:5174/assets/backpack.jpg" alt="" /><img src="http://localhost:5174/assets/coffee.jpg" alt="" /></div></div></div><div class="board-column"><h3>选中区块</h3><div class="panel"><div class="field"><label>区块名称</label><input value="推荐专区" /></div><div class="field"><label>商品来源</label><select><option>人工优先 · 销量补位</option><option>只显示人工选择</option></select></div><div class="field"><label>展示数量</label><input value="4" /></div><div class="check-row"><input type="checkbox" checked /> <span>库存失效时自动补位</span></div><div class="action-row"><button>取消</button><button class="primary">应用区块</button></div></div></div></div>`
  }
}

const params = new URLSearchParams(location.search)
let variant = (params.get('variant') || 'A').toUpperCase()
if (!variants[variant]) variant = 'A'
const initialScreen = params.get('screen') === 'admin' ? 'admin' : 'storefront'
const variantRoot = document.querySelector('#admin-variant')
const variantLabel = document.querySelector('#variant-label')
function renderVariant() {
  variantRoot.innerHTML = variants[variant].html
  variantLabel.textContent = variants[variant].label
  const nextUrl = new URL(location.href)
  nextUrl.searchParams.set('variant', variant)
  history.replaceState(null, '', nextUrl)
}
function move(step) {
  const keys = Object.keys(variants)
  variant = keys[(keys.indexOf(variant) + step + keys.length) % keys.length]
  renderVariant()
}
document.querySelectorAll('.model-tab').forEach(tab => tab.addEventListener('click', () => {
  document.querySelectorAll('.model-tab').forEach(item => item.classList.toggle('active', item === tab))
  document.querySelectorAll('.screen').forEach(item => item.classList.remove('active-screen'))
  document.querySelector(`#${tab.dataset.screen}-screen`).classList.add('active-screen')
  document.querySelector('.variant-switcher').style.display = tab.dataset.screen === 'admin' ? 'flex' : 'none'
}))
document.querySelector('#variant-prev').addEventListener('click', () => move(-1))
document.querySelector('#variant-next').addEventListener('click', () => move(1))
document.addEventListener('keydown', event => {
  if (['INPUT', 'TEXTAREA', 'SELECT'].includes(document.activeElement?.tagName)) return
  if (event.key === 'ArrowLeft') move(-1)
  if (event.key === 'ArrowRight') move(1)
})
renderVariant()
if (initialScreen === 'admin') {
  const tab = document.querySelector('.model-tab[data-screen="admin"]')
  tab.click()
}
