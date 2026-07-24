/* PROTOTYPE: 内存状态，刷新后重置。 */
const products = [
  { id: 1, name: '北欧白蜡木休闲椅', price: 699, old: 899, image: 'assets/chair.jpg', category: '家居', tag: '新品', stock: 18, sales: 326, spu: 'SPU-10001' },
  { id: 2, name: '轻量缓震城市运动鞋', price: 399, old: 499, image: 'assets/sneaker.jpg', category: '服饰', tag: '热卖', stock: 32, sales: 1028, spu: 'SPU-10002' },
  { id: 3, name: '简约蓝盘石英腕表', price: 529, old: 629, image: 'assets/watch.jpg', category: '数码', tag: '限时', stock: 7, sales: 186, spu: 'SPU-10003' },
  { id: 4, name: '复古微单摄影相机', price: 4599, old: 4899, image: 'assets/camera.jpg', category: '数码', tag: '少量', stock: 3, sales: 92, spu: 'SPU-10004' },
  { id: 5, name: '头戴式主动降噪耳机', price: 899, old: 1099, image: 'assets/headphones.jpg', category: '数码', tag: '热卖', stock: 25, sales: 663, spu: 'SPU-10005' },
  { id: 6, name: '醇香中度烘焙咖啡豆 500g', price: 89, old: 109, image: 'assets/coffee.jpg', category: '食品', tag: '满减', stock: 66, sales: 2146, spu: 'SPU-10006' },
  { id: 7, name: '防泼水通勤双肩包', price: 269, old: 329, image: 'assets/backpack.jpg', category: '服饰', tag: '推荐', stock: 0, sales: 517, spu: 'SPU-10007' },
  { id: 8, name: '原木护眼阅读台灯', price: 319, old: 399, image: 'assets/lamp.jpg', category: '家居', tag: '新品', stock: 12, sales: 241, spu: 'SPU-10008' }
];

const state = {
  mode: 'store', page: 'home', adminPage: 'dashboard', variant: getVariant(),
  search: '', category: '全部', selectedProduct: 1, sku: '原木色 / 标准款', qty: 1,
  cart: [
    { id: 2, qty: 1, checked: true, sku: '云雾白 / 42码' },
    { id: 6, qty: 2, checked: true, sku: '中度烘焙 / 500g' },
    { id: 7, qty: 1, checked: false, sku: '深灰色 / 20L' }
  ],
  orderTab: '全部', submitting: false, loginTab: 'code', codeSeconds: 0
};

const navItems = [
  ['home','首页'], ['category','分类'], ['detail','商品详情'], ['cart','购物车'],
  ['checkout','结算'], ['orders','我的订单'], ['service','售后'], ['profile','个人中心']
];
const adminItems = [
  ['dashboard','layout-dashboard','工作台'], ['products','package','商品管理'],
  ['orders','receipt-text','订单管理'], ['inventory','warehouse','库存管理'],
  ['service','headphones','售后管理'], ['risk','shield-alert','风控中心'],
  ['logs','scroll-text','操作日志']
];

function getVariant() {
  const value = new URLSearchParams(location.search).get('variant');
  return ['A','B','C'].includes(value) ? value : 'A';
}
function money(value) { return `¥${Number(value).toLocaleString('zh-CN',{ minimumFractionDigits: 2 })}`; }
function icon(name, cls = 'icon') { return `<i data-lucide="${name}" class="${cls}"></i>`; }
function findProduct(id) { return products.find(p => p.id === Number(id)); }
function cartCount() { return state.cart.reduce((sum,item) => sum + item.qty,0); }
function cartSelected() { return state.cart.filter(item => item.checked && findProduct(item.id).stock > 0); }
function cartTotal() { return cartSelected().reduce((sum,item) => sum + findProduct(item.id).price * item.qty,0); }
function toast(message, type = 'check-circle-2') {
  const el = document.querySelector('#toast');
  el.innerHTML = `${icon(type)}<span>${message}</span>`;
  lucide.createIcons();
  el.classList.add('show');
  clearTimeout(toast.timer);
  toast.timer = setTimeout(() => el.classList.remove('show'), 2300);
}
function modal(content) {
  document.querySelector('#modal-root').innerHTML = `<div class="modal-backdrop" data-action="close-modal"><div class="modal" role="dialog" aria-modal="true">${content}</div></div>`;
  lucide.createIcons();
}
function closeModal() { document.querySelector('#modal-root').innerHTML = ''; }
function render() {
  const app = document.querySelector('#app');
  app.innerHTML = state.mode === 'store' ? storeShell() : adminShell();
  lucide.createIcons();
  window.scrollTo({ top: 0, behavior: 'instant' });
}

function modeSwitch() {
  return `<div class="mode-switch" aria-label="模型端切换">
    <button class="${state.mode === 'store' ? 'active' : ''}" data-mode="store" title="用户端">${icon('shopping-bag')}<span>用户端</span></button>
    <button class="${state.mode === 'admin' ? 'active' : ''}" data-mode="admin" title="管理后台">${icon('monitor-cog')}<span>管理后台</span></button>
  </div>`;
}
function prototypeStrip() {
  return `<div class="prototype-strip"><b>静态交互验证稿</b><span>数据不会保存</span><span>·</span><span>用户端与管理后台均可点击浏览</span></div>`;
}
function storeShell() {
  return `${prototypeStrip()}${modeSwitch()}
    <header class="store-header">
      <div class="header-main">
        <button class="brand btn-link" data-page="home"><span class="brand-mark">${icon('shopping-basket')}</span><span>日常商店</span></button>
        <div class="search">${icon('search')}<input id="global-search" value="${state.search}" placeholder="搜索商品名称、品类或 SPU" aria-label="搜索商品"><button data-action="search">搜索</button></div>
        <div class="header-actions">
          <button class="icon-btn" data-action="open-login" title="登录">${icon('user-round')}</button>
          <button class="icon-btn" data-page="cart" title="购物车">${icon('shopping-cart')}<span class="badge-count">${cartCount()}</span></button>
        </div>
      </div>
      <nav class="store-nav"><div class="store-nav-inner">${navItems.map(([key,label]) => `<button class="${state.page === key ? 'active' : ''}" data-page="${key}">${label}</button>`).join('')}</div></nav>
    </header>
    <main class="page">${storePage()}</main>
    <footer class="store-footer">日常商店静态模型 · 页面内容与订单数据均为演示数据</footer>
    ${variantSwitcher()}`;
}
function storePage() {
  const pages = { home: homePage, category: categoryPage, detail: detailPage, cart: cartPage, checkout: checkoutPage, orders: ordersPage, service: servicePage, profile: profilePage };
  return (pages[state.page] || homePage)();
}
function variantSwitcher() {
  const names = { A:'陈列型', B:'分类型', C:'任务型' };
  return `<div class="variant-switcher"><div><strong>首页方案 ${state.variant}</strong><br><small>${names[state.variant]} · 左右键切换</small></div>${['A','B','C'].map(v => `<button class="${state.variant === v ? 'active' : ''}" data-variant="${v}" title="方案 ${v}">${v}</button>`).join('')}</div>`;
}

function productCard(p) {
  return `<article class="product-card">
    <button class="product-img-wrap btn-link" data-product="${p.id}"><img src="${p.image}" alt="${p.name}"><span class="tag ${p.stock === 0 ? 'tag-sold' : p.tag === '热卖' ? 'tag-sale' : ''}">${p.stock === 0 ? '已售罄' : p.tag}</span></button>
    <div class="product-body"><button class="product-name btn-link" data-product="${p.id}">${p.name}</button>
      <div class="price-row"><div><span class="price">${money(p.price)}</span><span class="old-price">${money(p.old)}</span></div>
      <button class="icon-btn mini-add" data-add="${p.id}" title="加入购物车" ${p.stock === 0 ? 'disabled' : ''}>${icon(p.stock === 0 ? 'ban' : 'shopping-cart','icon-sm')}</button></div>
    </div>
  </article>`;
}
function features() {
  return `<div class="feature-band">
    <div class="feature-item">${icon('badge-check')}<div><strong>正品保障</strong><div class="muted small">品牌授权 · 可验真</div></div></div>
    <div class="feature-item">${icon('truck')}<div><strong>当日发货</strong><div class="muted small">16:00 前订单</div></div></div>
    <div class="feature-item">${icon('rotate-ccw')}<div><strong>7 天无理由</strong><div class="muted small">售后流程透明</div></div></div>
    <div class="feature-item">${icon('shield-check')}<div><strong>支付安全</strong><div class="muted small">敏感信息加密</div></div></div>
  </div>`;
}
function homePage() {
  if (state.variant === 'B') return homeB();
  if (state.variant === 'C') return homeC();
  return `<section class="hero"><div class="hero-copy"><div class="eyebrow">夏日居家焕新</div><h1>让每一件日常用品，都经得起反复使用</h1><p>精选家居、数码、服饰和食品。现货商品 16:00 前下单，当日安排发货。</p><div class="hero-actions"><button class="btn btn-primary" data-page="category">${icon('shopping-bag')}查看本周精选</button><button class="btn" data-product="1">查看主推单品</button></div></div><img src="assets/chair.jpg" alt="北欧白蜡木休闲椅"></section>
    <div class="section-head"><h2>本周精选</h2><button class="btn-link" data-page="category">查看全部 ${icon('arrow-right','icon-sm')}</button></div>
    <div class="product-grid">${products.slice(0,8).map(productCard).join('')}</div>${features()}`;
}
function homeB() {
  const visible = state.category === '全部' ? products.slice(0,5) : products.filter(p => p.category === state.category);
  return `<div class="category-layout"><aside class="category-menu">${['全部','家居','数码','服饰','食品'].map(c => `<button class="${state.category === c ? 'active' : ''}" data-category="${c}"><span>${c === '全部' ? '全部商品' : c}</span>${icon('chevron-right','icon-sm')}</button>`).join('')}</aside>
    <section><div class="category-banner"><div><div class="eyebrow">按品类快速选购</div><h1>明确分类，少走一步</h1><p class="muted">左侧选择品类，直接比较价格、库存和销量。</p></div><img src="assets/lamp.jpg" alt="原木护眼阅读台灯"></div>
    <div class="horizontal-products">${visible.map(p => `<article class="horizontal-product"><button class="btn-link" data-product="${p.id}"><img src="${p.image}" alt="${p.name}"></button><div><button class="btn-link" data-product="${p.id}"><h3>${p.name}</h3></button><p class="muted">${p.spu} · 已售 ${p.sales} · ${p.stock ? `库存 ${p.stock}` : '暂时缺货'}</p><span class="price">${money(p.price)}</span></div><div><button class="btn ${p.stock ? 'btn-primary' : ''}" data-add="${p.id}" ${p.stock ? '' : 'disabled'}>${p.stock ? `${icon('shopping-cart')} 加入购物车` : '已售罄'}</button></div></article>`).join('')}</div></section></div>${features()}`;
}
function homeC() {
  return `<div class="task-home"><section class="welcome-panel"><div class="eyebrow">下午好</div><h1>今天想找点什么？</h1><div class="search">${icon('search')}<input placeholder="输入商品关键词" id="task-search"><button data-action="task-search">搜索</button></div><div class="quick-actions">
    <button data-category="家居">${icon('lamp-desk')}家居焕新</button><button data-category="数码">${icon('headphones')}数码好物</button><button data-page="orders">${icon('package-check')}查订单</button><button data-page="service">${icon('messages-square')}申请售后</button>
    </div></section><aside class="member-panel"><div class="member-head"><div class="avatar">访</div><div><strong>访客用户</strong><div class="muted small">登录后同步订单与购物车</div></div></div><button class="btn btn-dark" style="width:100%;margin-top:16px" data-action="open-login">手机号快捷登录</button><div class="order-shortcuts"><button data-page="orders">${icon('wallet-cards')}待付款</button><button data-page="orders">${icon('package')}待收货</button><button data-page="service">${icon('rotate-ccw')}售后</button><button data-page="profile">${icon('ticket-percent')}优惠券</button></div></aside></div>
    <div class="section-head"><h2>根据近期热度推荐</h2><button class="btn-link" data-page="category">更多商品 ${icon('arrow-right','icon-sm')}</button></div><div class="product-grid">${[products[5],products[1],products[4],products[0]].map(productCard).join('')}</div>${features()}`;
}

function categoryPage() {
  const query = state.search.toLowerCase();
  let visible = products.filter(p => (state.category === '全部' || p.category === state.category) && (!query || `${p.name}${p.category}${p.spu}`.toLowerCase().includes(query)));
  return `<div class="breadcrumb">首页 ${icon('chevron-right','icon-sm')} 商品分类</div><div class="page-title-row"><div><h1>全部商品</h1><span class="muted">共找到 ${visible.length} 件商品</span></div></div>
    <div class="filter-bar"><strong>分类</strong>${['全部','家居','数码','服饰','食品'].map(c => `<button class="btn ${state.category === c ? 'btn-dark' : ''}" data-category="${c}">${c}</button>`).join('')}<select aria-label="排序"><option>综合排序</option><option>销量从高到低</option><option>价格从低到高</option></select></div>
    <div class="section-head"><h2>${state.search ? `“${state.search}”的搜索结果` : state.category}</h2></div>${visible.length ? `<div class="product-grid">${visible.map(productCard).join('')}</div>` : `<div class="empty">${icon('search-x')}<div><h3>没有找到匹配商品</h3><p>换一个关键词或品类试试</p></div></div>`}`;
}
function detailPage() {
  const p = findProduct(state.selectedProduct);
  const sold = p.stock === 0;
  return `<div class="breadcrumb"><button class="btn-link" data-page="home">首页</button>${icon('chevron-right','icon-sm')}<button class="btn-link" data-page="category">${p.category}</button>${icon('chevron-right','icon-sm')}${p.name}</div>
    <div class="detail-layout"><div><img class="detail-image" src="${p.image}" alt="${p.name}"><div class="notice" style="margin-top:12px">${icon('shield-check')}<span>图片为商品实拍展示；下单时以选中的 SKU 规格为准。</span></div></div>
    <section><span class="status ${sold ? 'gray' : 'green'}">${sold ? '暂时售罄' : '现货'}</span><h1 style="margin-top:10px">${p.name}</h1><p class="muted">${p.spu} · 已售 ${p.sales} 件</p>
      <div class="detail-price"><span class="price">${money(p.price)}</span><span class="old-price">${money(p.old)}</span><div class="small muted" style="margin-top:6px">会员价以结算页为准</div></div>
      <div class="sku-block"><strong>颜色 / 款式</strong><div class="sku-options" style="margin-top:9px"><button class="sku ${state.sku === '原木色 / 标准款' ? 'active' : ''}" data-sku="原木色 / 标准款">原木色 / 标准款</button><button class="sku ${state.sku === '胡桃色 / 标准款' ? 'active' : ''}" data-sku="胡桃色 / 标准款">胡桃色 / 标准款</button><button class="sku" disabled>黑色 / 加宽款</button></div></div>
      <div class="sku-block"><strong>数量</strong><div style="margin-top:9px"><span class="quantity"><button data-action="detail-minus">−</button><span>${state.qty}</span><button data-action="detail-plus" ${sold ? 'disabled' : ''}>＋</button></span><span class="stock-tip">${sold ? '该 SKU 库存为 0，不能加购' : `库存 ${p.stock} 件，每单限购 5 件`}</span></div></div>
      ${sold ? `<div class="notice warn">${icon('circle-alert')}<span>商品已售罄。用户仍可查看详情，但无法选择数量、加入购物车或立即购买。</span></div>` : ''}
      <div class="detail-actions"><button class="btn" data-add="${p.id}" ${sold ? 'disabled' : ''}>${icon('shopping-cart')}加入购物车</button><button class="btn btn-primary" data-buy="${p.id}" ${sold ? 'disabled' : ''}>立即购买</button></div>
    </section></div>`;
}
function cartPage() {
  return `<div class="page-title-row"><div><h1>购物车</h1><span class="muted">现货按仓库实际库存结算</span></div><button class="btn-link" data-page="category">继续购物</button></div>
    <div class="cart-layout"><div class="list-panel">${state.cart.length ? state.cart.map(item => cartRow(item)).join('') : `<div class="empty">${icon('shopping-cart')}<div><h3>购物车还是空的</h3><button class="btn btn-primary" data-page="category">去选商品</button></div></div>`}</div>
    <aside class="summary-panel"><h2>结算明细</h2><div class="summary-line"><span>已选商品</span><span>${cartSelected().reduce((s,i) => s+i.qty,0)} 件</span></div><div class="summary-line"><span>商品金额</span><span>${money(cartTotal())}</span></div><div class="summary-line"><span>运费</span><span>${cartTotal() >= 99 ? '免运费' : money(8)}</span></div><div class="summary-line summary-total"><span>合计</span><span class="price">${money(cartTotal() + (cartTotal() > 0 && cartTotal() < 99 ? 8 : 0))}</span></div><button class="btn btn-primary" data-action="go-checkout" ${cartSelected().length ? '' : 'disabled'}>去结算</button><p class="muted small" style="margin:12px 0 0">提交前会再次校验价格与库存。</p></aside></div>`;
}
function cartRow(item) {
  const p = findProduct(item.id), sold = p.stock === 0;
  return `<div class="cart-row ${sold ? 'soldout' : ''}"><input class="check" type="checkbox" data-cart-check="${p.id}" ${item.checked && !sold ? 'checked' : ''} ${sold ? 'disabled' : ''} aria-label="选择 ${p.name}"><img src="${p.image}" alt="${p.name}"><div><strong>${p.name}</strong><div class="muted small">${item.sku}</div>${sold ? '<span class="status gray" style="margin-top:6px">已售罄，不可勾选</span>' : ''}</div><span class="price">${money(p.price)}</span><span class="quantity"><button data-cart-minus="${p.id}" ${sold ? 'disabled' : ''}>−</button><span>${item.qty}</span><button data-cart-plus="${p.id}" ${sold ? 'disabled' : ''}>＋</button></span><button class="icon-btn" data-cart-remove="${p.id}" title="删除">${icon('trash-2','icon-sm')}</button></div>`;
}
function checkoutPage() {
  const total = cartTotal();
  return `<div class="breadcrumb"><button class="btn-link" data-page="cart">购物车</button>${icon('chevron-right','icon-sm')}确认订单</div><div class="page-title-row"><div><h1>确认订单</h1><span class="muted">请确认收货信息和商品规格</span></div></div>
    <div class="checkout-layout"><div><section class="form-panel"><h2>收货地址</h2><div class="address-card"><div><strong>张先生 138****5678</strong><p style="margin:7px 0 0">北京市朝阳区示例街道 88 号 2 单元 1201</p></div><button class="btn-link" data-action="edit-address">修改</button></div></section>
    <section class="form-panel"><h2>配送与支付</h2><div class="field-grid"><label class="field"><span>配送方式</span><select><option>普通快递（预计 2-3 天）</option></select></label><label class="field"><span>支付方式</span><select><option>在线支付（Mock）</option></select></label></div></section>
    <section class="form-panel"><h2>商品清单</h2>${cartSelected().map(i => { const p=findProduct(i.id); return `<div class="order-product" style="padding:10px 0;border-bottom:1px solid var(--line)"><img src="${p.image}" alt="${p.name}"><div style="flex:1"><strong>${p.name}</strong><div class="muted small">${i.sku} × ${i.qty}</div></div><strong>${money(p.price*i.qty)}</strong></div>`; }).join('') || '<p class="muted">没有可结算商品，请返回购物车选择。</p>'}</section></div>
    <aside class="summary-panel"><h2>应付金额</h2><div class="summary-line"><span>商品合计</span><span>${money(total)}</span></div><div class="summary-line"><span>运费</span><span>${total >= 99 ? money(0) : money(8)}</span></div><div class="summary-line"><span>优惠</span><span style="color:var(--green)">-${money(total ? 20 : 0)}</span></div><div class="summary-line summary-total"><span>实付款</span><span class="price">${money(Math.max(0,total+(total&&total<99?8:0)-(total?20:0)))}</span></div><button id="submit-order" class="btn btn-primary" data-action="submit-order" ${total && !state.submitting ? '' : 'disabled'}>${state.submitting ? `${icon('loader-circle')} 正在提交...` : '提交订单'}</button><div class="notice" style="margin-top:14px">${icon('mouse-pointer-click')}<span>按钮重复点击会被幂等键拦截，只创建一张订单。</span></div></aside></div>`;
}
function ordersPage() {
  const orders = [
    { no:'202607220001', date:'2026-07-22 14:26', p:products[1], qty:1, total:399, status:'待付款', cls:'amber' },
    { no:'202607180126', date:'2026-07-18 09:15', p:products[5], qty:2, total:178, status:'运输中', cls:'blue' },
    { no:'202607090882', date:'2026-07-09 19:42', p:products[0], qty:1, total:699, status:'已完成', cls:'green' }
  ];
  const visible = state.orderTab === '全部' ? orders : orders.filter(o => o.status === state.orderTab);
  return `<div class="page-title-row"><div><h1>我的订单</h1><span class="muted">查看订单状态、物流与售后进度</span></div></div><div class="order-tabs">${['全部','待付款','运输中','已完成','退款/售后'].map(t => `<button class="${state.orderTab === t ? 'active' : ''}" data-order-tab="${t}">${t}</button>`).join('')}</div>${visible.length ? visible.map(orderCard).join('') : `<div class="empty">${icon('package-open')}<div><h3>当前分类暂无订单</h3><p>其他状态的订单仍可在“全部”中查看。</p></div></div>`}`;
}
function orderCard(o) {
  return `<article class="order-card"><div class="order-head"><span>下单时间：${o.date}</span><span>订单号：${o.no}</span></div><div class="order-content"><div class="order-product"><img src="${o.p.image}" alt="${o.p.name}"><div><strong>${o.p.name}</strong><div class="muted small">默认规格 × ${o.qty}</div></div></div><div><span class="status ${o.cls}">${o.status}</span><div style="margin-top:7px"><strong>${money(o.total)}</strong></div></div><div class="order-actions"><button class="btn" data-action="order-detail">查看详情</button>${o.status === '待付款' ? '<button class="btn btn-primary" data-action="mock-pay">立即支付</button>' : o.status === '运输中' ? '<button class="btn-link" data-action="logistics">查看物流</button>' : '<button class="btn-link" data-page="service">申请售后</button>'}</div></div></article>`;
}
function servicePage() {
  return `<div class="page-title-row"><div><h1>退款与售后</h1><span class="muted">提交申请后，客服将在 24 小时内处理</span></div><button class="btn btn-primary" data-action="new-service">${icon('plus')}申请售后</button></div><div class="notice warn">${icon('info')}<span>退货商品请保持包装与配件完整；退款原路退回，到账时间以支付渠道为准。</span></div>
    <article class="order-card"><div class="order-head"><span>售后单：AS202607150018</span><span>申请时间：2026-07-15 10:20</span></div><div class="order-content"><div class="order-product"><img src="assets/headphones.jpg" alt="头戴式主动降噪耳机"><div><strong>头戴式主动降噪耳机</strong><div class="muted small">原因：佩戴不适 · 退款退货</div></div></div><div><span class="status blue">等待寄回</span><div class="small muted" style="margin-top:7px">剩余 4 天 12 小时</div></div><div class="order-actions"><button class="btn" data-action="service-detail">查看进度</button><button class="btn-link" data-action="fill-logistics">填写物流</button></div></div></article>`;
}
function profilePage() {
  return `<div class="page-title-row"><div><h1>个人中心</h1><span class="muted">管理账号、地址与安全设置</span></div></div><div class="profile-layout"><aside class="profile-menu"><div class="profile-summary"><div class="avatar">张</div><strong style="display:block;margin-top:10px">张先生</strong><span class="small" style="color:#b8bec7">138****5678</span></div><button class="active">${icon('user-round')}账号资料</button><button>${icon('map-pin')}收货地址</button><button>${icon('ticket-percent')}优惠券</button><button>${icon('shield-check')}账号安全</button></aside>
    <section class="form-panel"><h2>账号资料</h2><div class="field-grid"><label class="field"><span>昵称</span><input value="张先生"></label><label class="field"><span>手机号</span><input value="138****5678" disabled></label><label class="field"><span>性别</span><select><option>未设置</option><option>男</option><option>女</option></select></label><label class="field"><span>生日</span><input type="date" value="1995-06-18"></label></div><div class="notice" style="margin-top:18px">${icon('smartphone')}<span>手机号只显示脱敏值。更换手机号时，需要校验旧号码或进行人工身份核验。</span></div><button class="btn btn-primary" style="margin-top:18px" data-action="save-profile">保存修改</button></section></div>`;
}

function adminShell() {
  return `${prototypeStrip()}${modeSwitch()}<div class="admin-shell"><aside class="admin-sidebar"><div class="admin-logo"><span class="brand-mark">${icon('shopping-basket')}</span><span>日常商店管理台</span></div><nav class="admin-nav"><div class="admin-nav-label">运营管理</div>${adminItems.map(([key,ic,label]) => `<button class="${state.adminPage === key ? 'active' : ''}" data-admin-page="${key}">${icon(ic)}<span>${label}</span></button>`).join('')}</nav></aside>
    <header class="admin-topbar"><div class="admin-top-left"><button class="icon-btn" title="折叠菜单">${icon('panel-left')}</button><strong>${adminItems.find(x => x[0] === state.adminPage)?.[2] || '工作台'}</strong></div><div class="admin-top-right"><button class="icon-btn" title="通知">${icon('bell')}<span class="badge-count">3</span></button><span>管理员</span><div class="avatar" style="width:34px;height:34px">管</div></div></header>
    <main class="admin-content">${adminPage()}</main></div>${variantSwitcher()}`;
}
function adminPage() {
  const pages = { dashboard: dashboardAdmin, products: productsAdmin, orders: ordersAdmin, inventory: inventoryAdmin, service: serviceAdmin, risk: riskAdmin, logs: logsAdmin };
  return (pages[state.adminPage] || dashboardAdmin)();
}
function adminTitle(title, desc, action='') { return `<div class="admin-title"><div><h1>${title}</h1>${desc ? `<div class="muted small" style="margin-top:5px">${desc}</div>` : ''}</div>${action}</div>`; }
function dashboardAdmin() {
  return `${adminTitle('工作台','2026 年 7 月 22 日 · 数据每 5 分钟刷新')}<div class="metrics">
    ${metric('今日成交额','¥28,650','较昨日 +12.6%','up','wallet-cards')}${metric('支付订单','126','支付转化率 68.2%','up','receipt-text')}${metric('待发货','37','最早已等待 2 小时','warn','package')}${metric('库存预警','5','其中 1 个 SKU 售罄','warn','triangle-alert')}
    </div><div class="admin-grid"><section class="admin-panel"><div class="panel-head"><h2>近 7 日成交趋势</h2><button class="btn-link">查看报表</button></div><div class="panel-body"><div class="chart">${[['一',48],['二',62],['三',54],['四',79],['五',67],['六',91],['日',73]].map(([d,h]) => `<div class="bar" data-day="周${d}" style="--h:${h}%" title="周${d}"></div>`).join('')}</div></div></section><section class="admin-panel"><div class="panel-head"><h2>待办事项</h2></div><div class="panel-body todo-list">${todo('待支付超时订单',8,'关闭任务将在 15:30 执行')}${todo('待审核售后',12,'最早等待 3 小时')}${todo('异常订单待核验',3,'疑似重复下单/刷单')}${todo('待同步物流单号',6,'需仓库补录')}</div></section></div>`;
}
function metric(label,value,trend,cls,ic) { return `<div class="metric"><div class="metric-head"><span>${label}</span>${icon(ic)}</div><div class="metric-value">${value}</div><div class="metric-trend ${cls} small">${trend}</div></div>`; }
function todo(label,count,desc) { return `<div class="todo"><div><strong>${count}</strong><div>${label}</div><span class="muted small">${desc}</span></div><button class="btn-link" data-action="admin-todo">处理</button></div>`; }
function productsAdmin() {
  return `${adminTitle('商品管理','按 SPU 管商品信息，按 SKU 管规格、价格和库存。',`<button class="btn btn-primary" data-action="new-product">${icon('plus')}新建商品</button>`)}${adminFilter('搜索商品名称 / SPU','全部分类','销售状态')}<div class="table-wrap"><table><thead><tr><th>商品</th><th>SPU 编码</th><th>分类</th><th>SKU 数</th><th>价格区间</th><th>销量</th><th>状态</th><th>操作</th></tr></thead><tbody>${products.slice(0,6).map(p => `<tr><td><div class="table-product"><img src="${p.image}" alt=""><strong>${p.name}</strong></div></td><td>${p.spu}</td><td>${p.category}</td><td>${p.id%3+2}</td><td>${money(p.price)} - ${money(p.price+120)}</td><td>${p.sales}</td><td><span class="status ${p.stock ? 'green' : 'gray'}">${p.stock ? '销售中' : '已下架'}</span></td><td><button class="btn-link" data-action="edit-product">编辑</button> · <button class="btn-link" data-action="admin-stock">库存</button></td></tr>`).join('')}</tbody></table></div>`;
}
function ordersAdmin() {
  const rows = [
    ['202607220001','张**','轻量缓震城市运动鞋','¥399.00','待付款','amber','正常'],
    ['202607220002','李**','醇香中度烘焙咖啡豆 ×2','¥178.00','待发货','blue','正常'],
    ['202607220003','王**','复古微单摄影相机','¥4,599.00','待核验','amber','疑似刷单'],
    ['202607210936','赵**','北欧白蜡木休闲椅','¥699.00','已发货','green','正常']
  ];
  return `${adminTitle('订单管理','订单创建、支付、发货和关闭状态统一追踪。',`<button class="btn">${icon('download')}导出</button>`)}${adminFilter('订单号 / 手机号 / 收货人','全部状态','风险等级')}<div class="table-wrap"><table><thead><tr><th>订单号</th><th>客户</th><th>商品摘要</th><th>实付金额</th><th>订单状态</th><th>风险标记</th><th>下单时间</th><th>操作</th></tr></thead><tbody>${rows.map((r,i) => `<tr class="${r[6] === '疑似刷单' ? 'risk-row' : ''}"><td>${r[0]}</td><td>${r[1]}</td><td>${r[2]}</td><td><strong>${r[3]}</strong></td><td><span class="status ${r[5]}">${r[4]}</span></td><td>${r[6] === '疑似刷单' ? '<span class="status amber">疑似刷单</span>' : '<span class="muted">正常</span>'}</td><td>07-22 ${14-i}:2${i}</td><td><button class="btn-link" data-action="order-detail">详情</button>${r[4] === '待发货' ? ' · <button class="btn-link" data-action="ship">发货</button>' : ''}</td></tr>`).join('')}</tbody></table></div>`;
}
function inventoryAdmin() {
  return `${adminTitle('库存管理','库存按 SKU 维护；售罄后用户端立即禁止加购。',`<button class="btn" data-action="stock-in">${icon('package-plus')}入库</button>`)}<div class="notice warn" style="margin-bottom:12px">${icon('triangle-alert')}<span>5 个 SKU 低于预警值，1 个 SKU 可售库存为 0。库存调整必须填写原因并记录操作日志。</span></div>${adminFilter('SKU 编码 / 商品名称','全部仓库','库存状态')}<div class="table-wrap"><table><thead><tr><th>商品 / SKU</th><th>SKU 编码</th><th>仓库</th><th>实际库存</th><th>锁定库存</th><th>可售库存</th><th>预警值</th><th>状态</th><th>操作</th></tr></thead><tbody>${products.slice(0,7).map((p,i) => { const locked=p.stock ? Math.min(i+1,p.stock) : 0, available=Math.max(0,p.stock-locked); return `<tr><td><div class="table-product"><img src="${p.image}" alt=""><div><strong>${p.name}</strong><div class="muted small">默认规格</div></div></div></td><td>SKU-${10001+i}</td><td>北京一号仓</td><td>${p.stock}</td><td>${locked}</td><td><strong>${available}</strong></td><td>5</td><td><span class="status ${available===0?'gray':available<=5?'amber':'green'}">${available===0?'售罄':available<=5?'低库存':'正常'}</span></td><td><button class="btn-link" data-action="adjust-stock">调整</button> · <button class="btn-link" data-action="stock-log">流水</button></td></tr>`; }).join('')}</tbody></table></div>`;
}
function serviceAdmin() {
  return `${adminTitle('售后管理','退款、退货和换货申请由客服统一处理。')}${adminFilter('售后单号 / 订单号 / 手机号','全部类型','待处理状态')}<div class="table-wrap"><table><thead><tr><th>售后单</th><th>订单号</th><th>客户</th><th>类型</th><th>退款金额</th><th>原因</th><th>状态</th><th>申请时间</th><th>操作</th></tr></thead><tbody><tr><td>AS202607150018</td><td>202607090882</td><td>张**</td><td>退货退款</td><td>¥899.00</td><td>佩戴不适</td><td><span class="status blue">等待寄回</span></td><td>07-15 10:20</td><td><button class="btn-link" data-action="service-detail">详情</button></td></tr><tr><td>AS202607220006</td><td>202607180126</td><td>李**</td><td>仅退款</td><td>¥89.00</td><td>商品破损</td><td><span class="status amber">待审核</span></td><td>07-22 13:08</td><td><button class="btn-link" data-action="review-service">审核</button></td></tr></tbody></table></div>`;
}
function riskAdmin() {
  return `${adminTitle('风控中心','识别刷单、批量注册、异常频次和重复支付风险。',`<button class="btn">${icon('settings-2')}规则配置</button>`)}<div class="metrics">${metric('今日拦截','18','接口限流 11 次','warn','shield-x')}${metric('人工待审','3','高价值异常订单','warn','user-check')}${metric('重复点击','42','均被幂等机制拦截','up','mouse-pointer-click')}${metric('短信拦截','27','同号码/同 IP 超频','up','message-square-warning')}</div><div class="admin-panel"><div class="panel-head"><h2>风险事件</h2><button class="btn-link">导出记录</button></div><div class="table-wrap" style="border:0;border-radius:0"><table><thead><tr><th>时间</th><th>风险类型</th><th>关联对象</th><th>命中规则</th><th>风险等级</th><th>系统处置</th><th>操作</th></tr></thead><tbody>
    <tr class="risk-row"><td>14:31:08</td><td>疑似刷单</td><td>订单 202607220003</td><td>同设备 10 分钟下单 8 次</td><td><span class="status amber">高</span></td><td>暂停发货，人工审核</td><td><button class="btn-link" data-action="risk-review">审核</button></td></tr>
    <tr><td>14:26:03</td><td>重复提交</td><td>用户 U10086</td><td>相同幂等键重复 4 次</td><td><span class="status blue">中</span></td><td>返回首次订单结果</td><td><button class="btn-link" data-action="risk-detail">详情</button></td></tr>
    <tr><td>13:58:44</td><td>短信超频</td><td>138****9721</td><td>单号码 1 小时请求 12 次</td><td><span class="status amber">高</span></td><td>冻结发送 24 小时</td><td><button class="btn-link" data-action="risk-detail">详情</button></td></tr>
    </tbody></table></div></div>`;
}
function logsAdmin() {
  const rows = [
    ['15:02:18','admin','商品管理','修改商品','SPU-10001','价格 729.00 → 699.00','成功','10.10.2.15'],
    ['14:56:32','warehouse01','库存管理','调整库存','SKU-10004','可售库存 5 → 3；原因：盘亏','成功','10.10.2.28'],
    ['14:42:09','service02','售后管理','审核退款','AS202607220006','拒绝仅退款，要求补充破损图片','成功','10.10.2.36'],
    ['14:31:11','risk-system','风控中心','暂停发货','202607220003','命中规则 RISK-ORDER-008','成功','127.0.0.1'],
    ['14:20:04','admin','权限管理','修改角色','ROLE-OPS','新增商品导出权限','失败','10.10.2.15']
  ];
  return `${adminTitle('操作日志','关键增删改操作均记录操作者、时间、IP、对象和变更内容。',`<button class="btn">${icon('download')}导出日志</button>`)}${adminFilter('操作者 / 业务对象 / 内容','全部模块','执行结果')}<div class="table-wrap"><table><thead><tr><th>时间</th><th>操作者</th><th>模块</th><th>操作</th><th>业务对象</th><th>变更摘要</th><th>结果</th><th>IP 地址</th><th>详情</th></tr></thead><tbody>${rows.map(r => `<tr>${r.map((v,i) => i===5 ? `<td class="log-detail" title="${v}">${v}</td>` : i===6 ? `<td><span class="status ${v==='成功'?'green':'amber'}">${v}</span></td>` : `<td>${v}</td>`).join('')}<td><button class="btn-link" data-action="log-detail">查看</button></td></tr>`).join('')}</tbody></table></div><div class="notice" style="margin-top:12px">${icon('git-branch')}<span>操作日志用于追责和审计；代码回滚由 Git 标签与发布脚本完成，两者职责不同，但发布操作也应写入日志。</span></div>`;
}
function adminFilter(placeholder,opt1,opt2) { return `<div class="admin-filter"><input placeholder="${placeholder}" aria-label="关键词"><select><option>${opt1}</option></select><select><option>${opt2}</option></select><button class="btn btn-dark" data-action="admin-search">${icon('search','icon-sm')}查询</button><button class="btn">重置</button></div>`; }

function loginModal() {
  modal(`<div class="modal-head"><h2>手机号登录 / 注册</h2><button class="icon-btn" data-action="close-modal">${icon('x')}</button></div><div class="modal-body"><div class="login-tabs"><button class="${state.loginTab === 'code' ? 'active' : ''}" data-login-tab="code">验证码登录</button><button class="${state.loginTab === 'password' ? 'active' : ''}" data-login-tab="password">密码登录</button></div>
    <label class="field"><span>中国大陆手机号</span><input id="login-phone" inputmode="tel" maxlength="11" placeholder="请输入 11 位手机号"></label>
    ${state.loginTab === 'code' ? `<label class="field" style="margin-top:14px"><span>短信验证码</span><span class="code-field"><input id="login-code" inputmode="numeric" maxlength="6" placeholder="6 位验证码"><button class="btn" data-action="send-code">${state.codeSeconds ? `${state.codeSeconds}s 后重试` : '获取验证码'}</button></span></label>` : `<label class="field" style="margin-top:14px"><span>密码</span><input type="password" placeholder="请输入密码"></label>`}
    <label class="consent"><input id="login-consent" type="checkbox"><span>我已阅读并同意《用户协议》和《隐私政策》，允许系统为登录、注册与安全验证处理本手机号。未勾选时不会发送验证码。</span></label><div class="notice" style="margin-top:14px">${icon('shield-check')}<span>前端只展示脱敏手机号；验证码限时有效、单次使用，并按手机号、设备和 IP 限流。</span></div></div><div class="modal-foot"><button class="btn" data-action="close-modal">取消</button><button class="btn btn-primary" data-action="login">登录 / 注册</button></div>`);
}
function simpleModal(title, body, confirm='我知道了') {
  modal(`<div class="modal-head"><h2>${title}</h2><button class="icon-btn" data-action="close-modal">${icon('x')}</button></div><div class="modal-body">${body}</div><div class="modal-foot"><button class="btn btn-primary" data-action="close-modal">${confirm}</button></div>`);
}

document.addEventListener('click', e => {
  let target = e.target.closest('button,[data-page],[data-product]');
  if (!target && e.target.matches('[data-action]')) target = e.target;
  if (!target || target.disabled) return;
  if (target.dataset.mode) { state.mode = target.dataset.mode; render(); return; }
  if (target.dataset.page) { state.page = target.dataset.page; render(); return; }
  if (target.dataset.adminPage) { state.adminPage = target.dataset.adminPage; render(); return; }
  if (target.dataset.variant) { setVariant(target.dataset.variant); return; }
  if (target.dataset.product) { state.selectedProduct = Number(target.dataset.product); state.page='detail'; state.qty=1; render(); return; }
  if (target.dataset.category) { state.category=target.dataset.category; if(state.page!=='home'||state.variant==='C') state.page='category'; render(); return; }
  if (target.dataset.sku) { state.sku=target.dataset.sku; render(); return; }
  if (target.dataset.add) { addToCart(Number(target.dataset.add)); return; }
  if (target.dataset.buy) { addToCart(Number(target.dataset.buy),state.qty,false); state.page='checkout'; render(); return; }
  if (target.dataset.orderTab) { state.orderTab=target.dataset.orderTab; render(); return; }
  if (target.dataset.cartPlus) { changeCart(Number(target.dataset.cartPlus),1); return; }
  if (target.dataset.cartMinus) { changeCart(Number(target.dataset.cartMinus),-1); return; }
  if (target.dataset.cartRemove) { state.cart=state.cart.filter(i=>i.id!==Number(target.dataset.cartRemove)); render(); toast('商品已从购物车移除'); return; }
  if (target.dataset.loginTab) { state.loginTab=target.dataset.loginTab; loginModal(); return; }
  const action = target.dataset.action;
  if (!action) return;
  handleAction(action,target);
});
document.addEventListener('change', e => {
  if (e.target.dataset.cartCheck) { const item=state.cart.find(i=>i.id===Number(e.target.dataset.cartCheck)); if(item) item.checked=e.target.checked; render(); }
});
document.addEventListener('keydown', e => {
  if (!['ArrowLeft','ArrowRight'].includes(e.key) || ['INPUT','TEXTAREA','SELECT'].includes(document.activeElement.tagName)) return;
  const vars=['A','B','C'], idx=vars.indexOf(state.variant);
  setVariant(vars[(idx+(e.key==='ArrowRight'?1:2))%3]);
});

function setVariant(value) {
  state.variant=value; state.page='home';
  const url=new URL(location.href); url.searchParams.set('variant',value); history.replaceState({},'',url);
  render(); toast(`已切换到首页方案 ${value}`,'panels-top-left');
}
function addToCart(id, qty=1, show=true) {
  const p=findProduct(id);
  if (!p.stock) { toast('该商品已售罄，不能加入购物车','circle-alert'); return; }
  const item=state.cart.find(i=>i.id===id);
  if(item) item.qty=Math.min(5,item.qty+qty); else state.cart.push({id,qty,checked:true,sku:state.sku});
  if(show) { render(); toast('已加入购物车'); }
}
function changeCart(id,delta) {
  const item=state.cart.find(i=>i.id===id); if(!item) return;
  item.qty=Math.max(1,Math.min(5,item.qty+delta)); render();
  if(item.qty===5&&delta>0) toast('每单最多购买 5 件','circle-alert');
}
function handleAction(action,target) {
  if(action==='close-modal') return closeModal();
  if(action==='search') { state.search=document.querySelector('#global-search').value.trim(); state.page='category'; render(); return; }
  if(action==='task-search') { state.search=document.querySelector('#task-search').value.trim(); state.page='category'; render(); return; }
  if(action==='open-login') return loginModal();
  if(action==='detail-minus') { state.qty=Math.max(1,state.qty-1); render(); return; }
  if(action==='detail-plus') { const p=findProduct(state.selectedProduct); state.qty=Math.min(5,p.stock,state.qty+1); render(); if(state.qty===5) toast('该商品每单限购 5 件','circle-alert'); return; }
  if(action==='go-checkout') { state.page='checkout'; render(); return; }
  if(action==='submit-order') return submitOrder(target);
  if(action==='send-code') return sendCode();
  if(action==='login') return doLogin();
  if(action==='save-profile') return toast('账号资料已保存');
  if(action==='mock-pay') return toast('Mock 支付已发起，本模型不会真实扣款','wallet-cards');
  if(action==='admin-search') return toast('查询条件已应用','search-check');
  if(action==='ship') return toast('发货前将再次校验订单风险状态','truck');
  if(action==='adjust-stock') return simpleModal('调整库存',`<div class="field-grid"><label class="field"><span>调整数量</span><input type="number" value="10"></label><label class="field"><span>调整类型</span><select><option>盘盈入库</option><option>盘亏出库</option></select></label></div><label class="field" style="margin-top:14px"><span>调整原因（必填）</span><input value="仓库盘点"></label><div class="notice warn" style="margin-top:14px">${icon('scroll-text')}<span>确认后将生成库存流水与操作日志。</span></div>`,'确认调整');
  if(action==='log-detail') return simpleModal('操作日志详情',`<p><strong>请求编号：</strong>REQ-20260722-150218-8842</p><p><strong>操作者：</strong>admin（商城管理员）</p><p><strong>变更前：</strong>price = 729.00</p><p><strong>变更后：</strong>price = 699.00</p><div class="notice">${icon('lock-keyhole')}<span>日志内容只对具备审计权限的角色开放，敏感参数已脱敏。</span></div>`);
  if(action==='risk-review') return simpleModal('风险订单审核',`<div class="notice warn">${icon('shield-alert')}<span>同设备 10 分钟内提交 8 张高价值订单，收货地址分散，支付账户存在关联。</span></div><p style="margin-top:16px">建议核验支付人与收货人关系。在人工审核完成前，系统保持“暂停发货”。</p>`,'保持暂停');
  const labels = { 'edit-address':'地址编辑为演示交互', 'order-detail':'订单详情已打开', logistics:'物流轨迹已刷新', 'new-service':'售后申请入口已打开', 'service-detail':'售后进度已打开', 'fill-logistics':'退货物流填写入口已打开', 'new-product':'新建商品表单已打开', 'edit-product':'商品编辑页已打开', 'admin-stock':'已跳转该商品库存', 'stock-in':'入库单创建入口已打开', 'stock-log':'库存流水已打开', 'review-service':'售后审核入口已打开', 'risk-detail':'风险事件详情已打开', 'admin-todo':'待办已领取' };
  toast(labels[action] || '该操作已触发');
}
function submitOrder(button) {
  if(state.submitting) { toast('订单正在提交，请勿重复点击','circle-alert'); return; }
  state.submitting=true; button.disabled=true; button.innerHTML=`${icon('loader-circle')} 正在提交...`; lucide.createIcons();
  toast('请求已受理，幂等键：ORDER-DEMO-001','shield-check');
  setTimeout(() => { state.submitting=false; state.page='orders'; render(); toast('订单创建成功：202607220001'); },1200);
}
function sendCode() {
  const phone=document.querySelector('#login-phone')?.value.trim(), consent=document.querySelector('#login-consent')?.checked;
  if(!/^1[3-9]\d{9}$/.test(phone||'')) { toast('请输入合规的 11 位中国大陆手机号','circle-alert'); return; }
  if(!consent) { toast('请先阅读并同意协议与隐私政策','circle-alert'); return; }
  state.codeSeconds=60; loginModal(); toast('演示验证码已发送：123456','message-square-check');
  clearInterval(state.codeTimer); state.codeTimer=setInterval(()=>{ state.codeSeconds--; if(state.codeSeconds<=0) clearInterval(state.codeTimer); },1000);
}
function doLogin() {
  const phone=document.querySelector('#login-phone')?.value.trim(), consent=document.querySelector('#login-consent')?.checked;
  if(!/^1[3-9]\d{9}$/.test(phone||'')) return toast('请输入合规手机号','circle-alert');
  if(!consent) return toast('请先同意用户协议与隐私政策','circle-alert');
  closeModal(); toast('登录成功，手机号已脱敏展示');
}

render();
