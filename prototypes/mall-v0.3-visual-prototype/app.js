/* PROTOTYPE: Three visual directions, static in-memory content only. */
const variants = {
  A: { name: '生活方式编辑感', short: '编辑感' },
  B: { name: '现代简洁感', short: '现代感' },
  C: { name: '活泼年轻感', short: '年轻感' }
};

const products = [
  { name: '暖光阅读台灯', category: '家居', price: '269', image: '../mall-static-model/assets/lamp.jpg', tag: '本周热销' },
  { name: '便携无反相机', category: '数码', price: '3,499', image: '../mall-static-model/assets/camera.jpg', tag: '编辑推荐' },
  { name: '城市轻量双肩包', category: '出行', price: '329', image: '../mall-static-model/assets/backpack.jpg', tag: '轻装出发' },
  { name: '手冲咖啡套装', category: '咖啡', price: '188', image: '../mall-static-model/assets/coffee.jpg', tag: '慢享清晨' }
];

const orderItems = [
  { name: '暖光阅读台灯', spec: '砂岩白 / 标准款', qty: 1, price: '269.00', image: '../mall-static-model/assets/lamp.jpg' },
  { name: '手冲咖啡套装', spec: '原木色 / 六件套', qty: 1, price: '188.00', image: '../mall-static-model/assets/coffee.jpg' }
];

const logistics = [
  { title: '运输中', detail: '包裹已到达北京朝阳转运中心', time: '07-29 10:26', active: true },
  { title: '已揽收', detail: '模拟物流已揽收包裹', time: '07-28 18:40' },
  { title: '已发货', detail: '商家完成出库并创建物流单', time: '07-28 16:12' }
];

const state = {
  variant: readVariant(),
  view: readView()
};

function readVariant() {
  const value = new URLSearchParams(location.search).get('variant');
  return variants[value] ? value : 'A';
}

function readView() {
  const value = new URLSearchParams(location.search).get('view');
  return ['home', 'order', 'profile'].includes(value) ? value : 'home';
}

function icon(name, className = 'icon') {
  return `<i data-lucide="${name}" class="${className}"></i>`;
}

function setQuery(next) {
  const params = new URLSearchParams(location.search);
  Object.entries(next).forEach(([key, value]) => params.set(key, value));
  history.replaceState(null, '', `${location.pathname}?${params.toString()}`);
  state.variant = readVariant();
  state.view = readView();
  render();
}

function shell(content) {
  return `<div class="prototype variant-${state.variant.toLowerCase()}">
    <div class="prototype-note"><b>V0.3 静态视觉模型</b><span>悬停可体验动效，数据不会保存</span></div>
    ${header()}
    <main>${content}</main>
    ${switcher()}
  </div>`;
}

function header() {
  const nav = [
    ['home', '首页', 'house'],
    ['order', '订单详情', 'package-check'],
    ['profile', '个人中心', 'user-round']
  ];
  return `<header class="site-header">
    <a class="brand" href="#" data-view="home"><span class="brand-sign">日</span><span><strong>日常商店</strong><small>DAILY STORE</small></span></a>
    <nav>${nav.map(([key, label, name]) => `<button class="${state.view === key ? 'active' : ''}" data-view="${key}">${icon(name)}<span>${label}</span></button>`).join('')}</nav>
    <div class="header-tools"><button title="搜索">${icon('search')}</button><button title="购物车">${icon('shopping-bag')}<b>2</b></button></div>
  </header>`;
}

function switcher() {
  return `<div class="prototype-switcher" role="navigation" aria-label="视觉方案切换">
    <button data-cycle="-1" title="上一个方案">${icon('arrow-left')}</button>
    <span><b>${state.variant}</b><em>${variants[state.variant].name}</em></span>
    <button data-cycle="1" title="下一个方案">${icon('arrow-right')}</button>
  </div>`;
}

function productGrid(limit = 4) {
  return `<div class="product-grid">${products.slice(0, limit).map((product, index) => `<article class="product-card" style="--delay:${index * 35}ms">
    <div class="product-image"><img src="${product.image}" alt="${product.name}"><span>${product.tag}</span><button title="收藏">${icon('heart')}</button></div>
    <div class="product-copy"><small>${product.category}</small><h3>${product.name}</h3><div><strong>¥${product.price}</strong><button>查看详情 ${icon('arrow-up-right')}</button></div></div>
  </article>`).join('')}</div>`;
}

function homeA() {
  return `<div class="home-page home-a">
    <section class="hero-a" style="background-image:url('../mall-static-model/assets/lamp.jpg')">
      <div class="hero-a-copy"><span>VOL. 03 / LIGHT & LIFE</span><h1>让日常，<br>多一点从容。</h1><p>从一束舒服的光开始，重新挑选那些值得长期留在身边的东西。</p><button>浏览本周陈列 ${icon('arrow-right')}</button></div>
      <aside><b>01</b><span>暖光阅读台灯</span><small>无极调光 · 低眩光灯罩</small></aside>
    </section>
    <section class="section editorial-products"><div class="section-title"><div><span>EDITOR'S PICK</span><h2>认真挑选，慢慢使用</h2></div><p>不追逐短暂流行，只留下愿意反复使用的日常器物。</p></div>${productGrid()}</section>
  </div>`;
}

function homeB() {
  return `<div class="home-page home-b page-width">
    <section class="hero-b"><div><span>本周精选 · 8 件新品</span><h1>简单选，放心用。</h1><p>真实库存、清楚价格、快速送达。为每天都要使用的物品，做更轻松的选择。</p><div class="hero-actions"><button>查看热销商品</button><button>浏览全部分类</button></div></div><img src="../mall-static-model/assets/camera.jpg" alt="便携无反相机"></section>
    <section class="quick-categories"><button>${icon('lamp-desk')}<span>家居照明</span><b>12</b></button><button>${icon('camera')}<span>数码影像</span><b>18</b></button><button>${icon('coffee')}<span>咖啡器具</span><b>9</b></button><button>${icon('backpack')}<span>城市出行</span><b>14</b></button></section>
    <section class="section"><div class="section-title"><div><span>POPULAR NOW</span><h2>最近热销</h2></div><button class="text-button">查看全部 ${icon('arrow-right')}</button></div>${productGrid()}</section>
  </div>`;
}

function homeC() {
  return `<div class="home-page home-c page-width">
    <section class="hero-c"><div class="hero-c-copy"><span>NEW DROP</span><h1>今天，也要<br>挑点喜欢的。</h1><p>好看、好用，还能让普通一天有一点新鲜。</p><button>开始逛逛 ${icon('sparkles')}</button></div><div class="hero-c-collage"><img class="collage-main" src="../mall-static-model/assets/sneaker.jpg" alt="城市运动鞋"><img class="collage-small" src="../mall-static-model/assets/headphones.jpg" alt="静音头戴耳机"><span>本周上新<br><b>08</b> 件</span></div></section>
    <section class="ticker"><span>FREE SHIPPING</span><b>满 199 元免运费</b><span>NEW ARRIVALS</span><b>每周二上新</b><span>EASY RETURN</span><b>7 天轻松退换</b></section>
    <section class="section"><div class="section-title"><div><span>HOT PICKS</span><h2>大家正在买</h2></div><button class="text-button">再看更多 ${icon('move-right')}</button></div>${productGrid()}</section>
  </div>`;
}

function orderHeader() {
  return `<div class="order-heading"><div><a href="#" data-view="home">我的订单</a><span>/</span><b>订单详情</b></div><span class="status-badge">运输中</span><h1>M20260729105639934103</h1><p>下单时间 2026-07-28 14:36</p></div>`;
}

function orderItemsHtml() {
  return `<div class="order-items">${orderItems.map(item => `<div class="order-item"><img src="${item.image}" alt="${item.name}"><div><h3>${item.name}</h3><p>${item.spec}</p><small>数量 × ${item.qty}</small></div><strong>¥${item.price}</strong></div>`).join('')}</div>`;
}

function logisticsHtml() {
  return `<ol class="logistics-list">${logistics.map(node => `<li class="${node.active ? 'active' : ''}"><span></span><div><strong>${node.title}</strong><p>${node.detail}</p><small>${node.time}</small></div></li>`).join('')}</ol>`;
}

function orderA() {
  return `<div class="order-page order-a page-width">${orderHeader()}<div class="order-a-progress"><span class="done">已付款</span><i></i><span class="done">已发货</span><i></i><span class="current">运输中</span><i></i><span>待收货</span></div><div class="order-a-layout"><section><div class="block-title"><span>01</span><h2>商品与金额</h2></div>${orderItemsHtml()}<dl class="money-lines"><div><dt>商品金额</dt><dd>¥457.00</dd></div><div><dt>运费</dt><dd>¥0.00</dd></div><div><dt>实付金额</dt><dd>¥457.00</dd></div></dl></section><aside><div class="block-title"><span>02</span><h2>配送进度</h2></div><div class="tracking-number"><span>模拟物流</span><button>MOCK-20260729-3018 ${icon('copy')}</button></div>${logisticsHtml()}<button class="outline-action">确认收货</button></aside></div><section class="order-address-band"><div><span>收货信息</span><strong>林晓 · 138****2480</strong><p>北京市 朝阳区 建国路 88 号 2 单元 1203</p></div><div><span>订单备注</span><p>工作日晚上配送，放在门卫处即可。</p></div></section></div>`;
}

function orderB() {
  return `<div class="order-page order-b page-width">${orderHeader()}<div class="order-b-layout"><aside class="order-summary"><h2>订单概览</h2><div class="summary-status">${icon('truck')}<span><b>包裹运输中</b><small>预计明天送达</small></span></div><dl><div><dt>实付金额</dt><dd>¥457.00</dd></div><div><dt>商品数量</dt><dd>2 件</dd></div><div><dt>支付方式</dt><dd>模拟支付</dd></div></dl><button>确认收货</button><button class="secondary">申请售后</button></aside><div class="order-b-main"><section><div class="block-title"><h2>物流跟踪</h2><span>模拟物流 · MOCK-20260729-3018</span></div>${logisticsHtml()}</section><section><div class="block-title"><h2>商品清单</h2><span>共 2 件</span></div>${orderItemsHtml()}</section><section class="address-inline"><div>${icon('map-pin')}<span><b>林晓 · 138****2480</b><p>北京市朝阳区建国路 88 号 2 单元 1203</p></span></div><button>复制地址</button></section></div></div></div>`;
}

function orderC() {
  return `<div class="order-page order-c page-width">${orderHeader()}<section class="delivery-hero"><div>${icon('package-check')}<span><small>最新物流</small><h2>你的包裹正在向你靠近</h2><p>刚刚到达北京朝阳转运中心</p></span></div><b>预计<br><strong>明天</strong><br>送达</b></section><div class="order-c-tabs"><button class="active">物流进度</button><button>商品信息</button><button>收货信息</button></div><div class="order-c-grid"><section><div class="block-title"><h2>配送时间线</h2><span>3 个节点</span></div>${logisticsHtml()}</section><aside><h2>订单小结</h2>${orderItemsHtml()}<div class="order-c-total"><span>实付</span><strong>¥457.00</strong></div><button>确认收货 ${icon('check')}</button></aside></div></div>`;
}

function avatarEditor() {
  return `<div class="avatar-editor"><div class="avatar">林<button title="更换头像">${icon('camera')}</button></div><div><h2>林晓</h2><p>普通会员 · 加入 32 天</p><button>更换头像</button></div></div>`;
}

function profileA() {
  return `<div class="profile-page profile-a page-width"><section class="profile-a-hero">${avatarEditor()}<div class="profile-quote"><span>MY DAILY</span><p>“把生活里常用的东西，<br>选得更认真一点。”</p></div></section><nav class="profile-a-nav"><button class="active">个人资料</button><button>收货地址</button><button>账号安全</button><button>通知设置</button></nav><div class="profile-a-layout"><section><div class="block-title"><span>01</span><h2>基本资料</h2></div><div class="form-lines"><label><span>昵称</span><input value="林晓"><button>修改</button></label><label><span>手机号</span><input value="138****2480" disabled><button>更换</button></label><label><span>个人简介</span><textarea>喜欢咖啡、摄影和周末散步。</textarea><button>保存</button></label></div></section><aside><div class="block-title"><span>02</span><h2>账号概览</h2></div><div class="profile-stats"><div><strong>6</strong><span>全部订单</span></div><div><strong>2</strong><span>运输中</span></div><div><strong>3</strong><span>收货地址</span></div></div><div class="security-note">${icon('shield-check')}<span><b>账号状态良好</b><p>最近登录：今天 10:26 · 本机</p></span></div></aside></div></div>`;
}

function profileB() {
  return `<div class="profile-page profile-b page-width"><div class="profile-b-layout"><aside>${avatarEditor()}<nav><button class="active">${icon('user-round')}个人资料</button><button>${icon('map-pin')}收货地址</button><button>${icon('shield')}账号安全</button><button>${icon('bell')}通知设置</button></nav><div class="member-code"><small>会员编号</small><b>M000028</b></div></aside><main><div class="profile-title"><div><span>账户设置</span><h1>个人资料</h1><p>管理公开昵称、头像和联系方式。</p></div><button>保存更改</button></div><section class="profile-form"><label><span>昵称</span><input value="林晓"><small>最多 20 个字符，30 天内可修改 3 次</small></label><label><span>手机号</span><div><input value="138****2480" disabled><button>验证并更换</button></div></label><label><span>个人简介</span><textarea>喜欢咖啡、摄影和周末散步。</textarea></label></section><section class="security-panel"><div>${icon('shield-check')}<span><b>安全等级：良好</b><p>短信验证已启用，最近没有异常登录。</p></span></div><button>进入安全中心</button></section></main></div></div>`;
}

function profileC() {
  return `<div class="profile-page profile-c page-width"><section class="profile-c-banner"><div>${avatarEditor()}</div><div class="profile-c-stats"><span><b>6</b>订单</span><span><b>3</b>地址</span><span><b>128</b>成长值</span></div></section><div class="profile-c-shortcuts"><button>${icon('package')}<span><b>我的订单</b><small>2 个包裹运输中</small></span>${icon('chevron-right')}</button><button>${icon('map-pinned')}<span><b>地址簿</b><small>3 个常用地址</small></span>${icon('chevron-right')}</button><button>${icon('shield-check')}<span><b>安全中心</b><small>账号状态良好</small></span>${icon('chevron-right')}</button></div><section class="profile-c-form"><div class="block-title"><div><span>EDIT PROFILE</span><h2>把资料换成喜欢的样子</h2></div><button>保存全部</button></div><div><label><span>昵称</span><input value="林晓"></label><label><span>个人简介</span><input value="喜欢咖啡、摄影和周末散步。"></label></div></section></div>`;
}

function pageContent() {
  const pages = {
    home: { A: homeA, B: homeB, C: homeC },
    order: { A: orderA, B: orderB, C: orderC },
    profile: { A: profileA, B: profileB, C: profileC }
  };
  return pages[state.view][state.variant]();
}

function render() {
  document.querySelector('#app').innerHTML = shell(pageContent());
  lucide.createIcons();
  document.documentElement.dataset.variant = state.variant;
}

document.addEventListener('click', (event) => {
  const viewButton = event.target.closest('[data-view]');
  if (viewButton) {
    event.preventDefault();
    setQuery({ view: viewButton.dataset.view });
    return;
  }
  const cycleButton = event.target.closest('[data-cycle]');
  if (cycleButton) {
    const keys = Object.keys(variants);
    const current = keys.indexOf(state.variant);
    const next = (current + Number(cycleButton.dataset.cycle) + keys.length) % keys.length;
    setQuery({ variant: keys[next] });
  }
});

document.addEventListener('keydown', (event) => {
  const tag = document.activeElement?.tagName;
  if (['INPUT', 'TEXTAREA', 'SELECT'].includes(tag) || document.activeElement?.isContentEditable) return;
  if (!['ArrowLeft', 'ArrowRight'].includes(event.key)) return;
  const keys = Object.keys(variants);
  const current = keys.indexOf(state.variant);
  const direction = event.key === 'ArrowRight' ? 1 : -1;
  setQuery({ variant: keys[(current + direction + keys.length) % keys.length] });
});

render();
