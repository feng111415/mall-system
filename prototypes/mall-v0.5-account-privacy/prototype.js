const state = { dialog: '', phoneStep: 1 }

const icon = value => `<span class="card-icon" aria-hidden="true">${value}</span>`

function shell(content) {
  return `
    <div class="shell">
      <header class="site-header">
        <div class="header-inner">
          <div class="brand"><span class="brand-mark">日</span><span><strong>日常商店</strong><small>DAILY STORE</small></span></div>
          <nav class="desktop-nav"><span><b>01</b>首页</span><span><b>02</b>全部商品</span><span><b>03</b>我的订单</span><span><b>04</b>消息中心</span><span class="active"><b>05</b>个人中心</span></nav>
          <div class="header-tools" aria-label="商城工具"><span>⌕</span><span>▢</span></div>
        </div>
      </header>
      <section class="page">
        <header class="page-heading">
          <button class="back" type="button" aria-label="返回个人中心">‹</button>
          <div><span class="kicker">账户安全</span><h1>账号与隐私</h1><p>管理登录手机号、授权记录与账号状态</p></div>
          <span class="security-badge"><i></i>账号状态正常</span>
        </header>
        <div class="variant-content">${content}</div>
      </section>
      <nav class="mobile-nav" aria-label="移动端导航"><span data-icon="⌂">首页</span><span data-icon="▦">商品</span><span data-icon="▱">购物车</span><span data-icon="▤">订单</span><span data-icon="◌">消息</span><span class="active" data-icon="○">我的</span></nav>
    </div>`
}

function identityPanel() {
  return `
    <aside class="identity-panel">
      <div class="identity-head"><img src="../../mall-storefront/public/assets/avatars/avatar-coral.svg" alt="当前头像" /><div><h2>用户8606</h2><p>会员编号 57</p></div></div>
      <dl class="identity-facts"><div><dt>登录手机号</dt><dd>188****8606</dd></div><div><dt>在线设备</dt><dd>1 台</dd></div><div><dt>最近登录</dt><dd>今天 15:43</dd></div></dl>
      <p class="privacy-note">协议记录可查且不可改写。交易凭证按履约与审计要求保留。</p>
    </aside>`
}

function phoneCard() {
  return `<section class="section-card"><div class="card-head">${icon('机')}<div class="card-copy"><h2>登录手机号</h2><p>当前绑定 188****8606，更换时依次验证旧号码和新号码。</p></div><button class="action-button" data-action="phone" type="button">更换手机号</button></div></section>`
}

function consentCard() {
  return `<section class="section-card"><div class="card-head">${icon('权')}<div class="card-copy"><h2>授权与协议记录</h2><p>查看登录时确认的协议版本和时间，必要授权不可单独撤回。</p></div></div><div class="consent-list"><div class="consent-row"><span><strong>用户协议</strong><small>版本 1.0 · 2026-08-06 15:43</small></span><em class="required-tag">服务必需</em><button class="text-button" type="button">查看正文</button></div><div class="consent-row"><span><strong>隐私政策</strong><small>版本 1.0 · 2026-08-06 15:43</small></span><em class="required-tag">服务必需</em><button class="text-button" type="button">查看正文</button></div></div></section>`
}

function lifecycleCard() {
  return `<section class="timeline-panel"><h2>账号生命周期</h2><p>身份、授权和安全事件按时间保留</p><div class="timeline"><div class="timeline-item"><i class="timeline-dot"></i><span class="timeline-copy"><strong>当前设备登录</strong><small>Windows 电脑 · 127.0.0.1</small></span><time class="timeline-time">今天 15:43</time></div><div class="timeline-item"><i class="timeline-dot"></i><span class="timeline-copy"><strong>确认隐私政策 1.0</strong><small>服务必需授权 · 记录不可改写</small></span><time class="timeline-time">今天 15:43</time></div><div class="timeline-item"><i class="timeline-dot"></i><span class="timeline-copy"><strong>确认用户协议 1.0</strong><small>服务必需授权 · 记录不可改写</small></span><time class="timeline-time">今天 15:43</time></div><div class="timeline-item"><i class="timeline-dot"></i><span class="timeline-copy"><strong>账号创建</strong><small>登录手机号 188****8606</small></span><time class="timeline-time">今天 15:43</time></div></div></section>`
}

function cancelCard() {
  return `<section class="section-card danger"><div class="card-head">${icon('止')}<div class="card-copy"><h2>注销账号</h2><p>系统先检查订单和售后；验证当前手机号后立即停用账号并下线全部设备。</p></div><button class="danger-button" data-action="cancel" type="button">申请注销</button></div></section>`
}

function finalLayout() {
  return `<div class="overview-layout">${identityPanel()}<div class="stack">${phoneCard()}${consentCard()}${lifecycleCard()}${cancelCard()}</div></div>`
}

function phoneDialog() {
  const title = state.phoneStep === 1 ? '验证当前手机号' : state.phoneStep === 2 ? '验证新手机号' : '手机号更换确认'
  const body = state.phoneStep === 1
    ? `<label>当前手机号<div class="field"><input value="188****8606" disabled /></div></label><label>短信验证码<div class="field"><input inputmode="numeric" maxlength="6" placeholder="6 位验证码" /><button type="button">获取验证码</button></div></label>`
    : state.phoneStep === 2
      ? `<label>新手机号<div class="field"><input inputmode="numeric" maxlength="11" placeholder="请输入新手机号" /></div></label><label>新号码验证码<div class="field"><input inputmode="numeric" maxlength="6" placeholder="6 位验证码" /><button type="button">获取验证码</button></div></label>`
      : `<p class="danger-copy">确认更换后，当前设备和其他全部设备都会退出登录。下次请使用新手机号登录。</p>`
  return `<div class="dialog-backdrop" data-dismiss><section class="dialog" role="dialog" aria-modal="true"><header class="dialog-head"><div><span class="kicker">身份验证</span><h2>${title}</h2></div><button class="close" data-close type="button" aria-label="关闭">×</button></header><div class="dialog-steps"><span class="${state.phoneStep === 1 ? 'active' : ''}">1 验证旧号码</span><span class="${state.phoneStep === 2 ? 'active' : ''}">2 验证新号码</span><span class="${state.phoneStep === 3 ? 'active' : ''}">3 确认更换</span></div>${body}<footer class="dialog-actions"><button class="ghost-button" data-close type="button">取消</button><button class="action-button" data-phone-next type="button">${state.phoneStep === 3 ? '确认并退出登录' : '下一步'}</button></footer></section></div>`
}

function cancelDialog() {
  return `<div class="dialog-backdrop" data-dismiss><section class="dialog" role="dialog" aria-modal="true"><header class="dialog-head"><div><span class="kicker">危险操作</span><h2>注销账号</h2></div><button class="close" data-close type="button" aria-label="关闭">×</button></header><div class="check-list"><div><span>未完成订单</span><b class="pass">0 笔，通过</b></div><div><span>处理中售后</span><b class="pass">0 笔，通过</b></div></div><p class="danger-copy">注销立即生效：账号停用、全部设备退出、收货地址等非交易资料停止使用。订单、售后与财务凭证按审计要求保留。</p><label>当前手机号验证码<div class="field"><input inputmode="numeric" maxlength="6" placeholder="6 位验证码" /><button type="button">获取验证码</button></div></label><label class="confirm-check"><input type="checkbox" />我已了解注销结果，并确认继续</label><footer class="dialog-actions"><button class="ghost-button" data-close type="button">暂不注销</button><button class="danger-button" type="button">确认注销账号</button></footer></section></div>`
}

function render() {
  document.querySelector('#app').innerHTML = shell(finalLayout()) + (state.dialog === 'phone' ? phoneDialog() : state.dialog === 'cancel' ? cancelDialog() : '')
  bindEvents()
}

function bindEvents() {
  document.querySelectorAll('[data-action]').forEach(button => button.addEventListener('click', () => {
    state.dialog = button.dataset.action
    state.phoneStep = 1
    render()
  }))
  document.querySelectorAll('[data-close]').forEach(button => button.addEventListener('click', () => { state.dialog = ''; render() }))
  document.querySelector('[data-dismiss]')?.addEventListener('click', event => { if (event.target === event.currentTarget) { state.dialog = ''; render() } })
  document.querySelector('[data-phone-next]')?.addEventListener('click', () => {
    if (state.phoneStep < 3) state.phoneStep += 1
    else state.dialog = ''
    render()
  })
}

render()
