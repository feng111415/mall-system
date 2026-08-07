<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useNoticeStore } from '../stores/notice'
import {
  cancelMemberAccount,
  completePhoneChange,
  getAccountLifecycle,
  sendAccountCancellationCode,
  sendPhoneChangeNewCode,
  sendPhoneChangeOldCode,
  startPhoneChange,
  verifyPhoneChangeOld
} from '../api/member'

const router = useRouter()
const notice = useNoticeStore()
const loading = ref(false)
const error = ref('')
const account = ref(null)
const phoneDialog = ref(false)
const phoneStep = ref(1)
const phoneBusy = ref(false)
const phoneRequest = ref({ requestId: null, ticket: '', newPhone: '', code: '' })
const cancellationDialog = ref(false)
const cancellationBusy = ref(false)
const cancellationCode = ref('')

const eligibility = computed(() => account.value?.cancellationEligibility || { unfinishedOrderCount: 0, inProgressAfterSaleCount: 0 })
const canCancel = computed(() => eligibility.value.unfinishedOrderCount === 0 && eligibility.value.inProgressAfterSaleCount === 0)
const lifecycleEvents = computed(() => account.value?.lifecycle || [])
const consents = computed(() => account.value?.consents || [])

onMounted(loadAccount)

async function loadAccount() {
  if (!sessionStorage.getItem('mall-user-token')) return
  loading.value = true
  error.value = ''
  try {
    const response = await getAccountLifecycle()
    account.value = response.data.data
  } catch (requestError) {
    error.value = requestError.response?.data?.msg || '账号信息加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function openPhoneDialog() {
  phoneRequest.value = { requestId: null, ticket: '', newPhone: '', code: '' }
  phoneStep.value = 1
  phoneDialog.value = true
}

function closePhoneDialog() {
  if (!phoneBusy.value) phoneDialog.value = false
}

async function beginPhoneChange() {
  if (!/^1[3-9]\d{9}$/.test(phoneRequest.value.newPhone)) {
    notice.show('请输入正确的新手机号', 'error')
    return
  }
  phoneBusy.value = true
  try {
    const response = await startPhoneChange(phoneRequest.value.newPhone)
    phoneRequest.value.requestId = response.data.data.requestId
    await sendPhoneChangeOldCode(phoneRequest.value.requestId)
    phoneStep.value = 2
    notice.show('当前手机号验证码已发送')
  } catch (requestError) {
    notice.show(requestError.response?.data?.msg || '手机号更换发起失败', 'error')
  } finally {
    phoneBusy.value = false
  }
}

async function verifyOldPhone() {
  if (!/^\d{6}$/.test(phoneRequest.value.code)) {
    notice.show('请输入6位验证码', 'error')
    return
  }
  phoneBusy.value = true
  try {
    const response = await verifyPhoneChangeOld({ requestId: phoneRequest.value.requestId, code: phoneRequest.value.code })
    phoneRequest.value.ticket = response.data.data.ticket
    phoneRequest.value.code = ''
    await sendPhoneChangeNewCode({ requestId: phoneRequest.value.requestId, ticket: phoneRequest.value.ticket })
    phoneStep.value = 3
    notice.show('新手机号验证码已发送')
  } catch (requestError) {
    notice.show(requestError.response?.data?.msg || '当前手机号验证失败', 'error')
  } finally {
    phoneBusy.value = false
  }
}

async function finishPhoneChange() {
  if (!/^\d{6}$/.test(phoneRequest.value.code)) {
    notice.show('请输入6位验证码', 'error')
    return
  }
  phoneBusy.value = true
  try {
    await completePhoneChange({ requestId: phoneRequest.value.requestId, ticket: phoneRequest.value.ticket, code: phoneRequest.value.code })
    phoneDialog.value = false
    notice.show('手机号更换成功，请使用新手机号重新登录')
    sessionStorage.removeItem('mall-user-token')
    router.push('/account')
  } catch (requestError) {
    notice.show(requestError.response?.data?.msg || '手机号更换失败', 'error')
  } finally {
    phoneBusy.value = false
  }
}

async function openCancellationDialog() {
  if (!canCancel.value) {
    notice.show('请先完成未完成订单或处理中售后', 'error')
    return
  }
  cancellationBusy.value = true
  try {
    await sendAccountCancellationCode()
    cancellationCode.value = ''
    cancellationDialog.value = true
    notice.show('注销验证码已发送')
  } catch (requestError) {
    notice.show(requestError.response?.data?.msg || '暂时无法发起注销', 'error')
  } finally {
    cancellationBusy.value = false
  }
}

async function confirmCancellation() {
  if (!/^\d{6}$/.test(cancellationCode.value)) {
    notice.show('请输入6位验证码', 'error')
    return
  }
  cancellationBusy.value = true
  try {
    await cancelMemberAccount(cancellationCode.value)
    cancellationDialog.value = false
    sessionStorage.removeItem('mall-user-token')
    notice.show('账号已停用')
    router.push('/account')
  } catch (requestError) {
    notice.show(requestError.response?.data?.msg || '账号注销失败', 'error')
  } finally {
    cancellationBusy.value = false
  }
}

function formatTime(value) {
  if (!value) return '未知时间'
  return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}
</script>

<template>
  <main class="account-privacy-page">
    <header class="account-privacy-header">
      <button class="privacy-back" type="button" aria-label="返回个人中心" @click="router.push('/account')">←</button>
      <div><span class="privacy-kicker">ACCOUNT / PRIVACY</span><h1>账号与隐私</h1><p>查看身份、授权和账号生命周期，重要操作都会留下可追溯记录。</p></div>
    </header>

    <p v-if="loading" class="privacy-state">正在加载账号信息...</p>
    <p v-else-if="error" class="privacy-state privacy-error">{{ error }}</p>
    <template v-else-if="account">
      <section class="privacy-overview">
        <article class="privacy-identity">
          <div class="privacy-avatar"><img v-if="account.avatar" :src="account.avatar" alt="" /><span v-else>{{ account.nickname?.slice(0, 1) }}</span></div>
          <div><span class="privacy-label">会员身份</span><h2>{{ account.nickname }}</h2><p>M{{ account.memberId }} · 正常使用中</p></div>
        </article>
        <article class="privacy-phone">
          <span class="privacy-label">登录手机号</span><strong>{{ account.maskedPhone }}</strong><button class="privacy-link" type="button" @click="openPhoneDialog">更换手机号</button>
          <small>更换成功后，所有已登录设备会立即下线。</small>
        </article>
      </section>

      <section class="privacy-grid">
        <article class="privacy-panel">
          <div class="privacy-panel-heading"><div><span class="privacy-label">SERVICE CONSENT</span><h2>授权与协议</h2></div><span class="privacy-status">服务必需</span></div>
          <div v-if="consents.length" class="consent-list">
            <div v-for="consent in consents" :key="consent.consentId" class="consent-row"><span>{{ consent.consentType === 'USER_AGREEMENT' ? '用户协议' : '隐私政策' }}</span><b>v{{ consent.consentVersion }}</b><time>{{ formatTime(consent.consentTime) }}</time></div>
          </div>
          <p v-else class="privacy-muted">暂无授权记录</p>
          <p class="privacy-hint">当前仅保留完成登录和交易所必需的授权，不展示不存在的营销开关。</p>
        </article>

        <article class="privacy-panel eligibility-panel">
          <div class="privacy-panel-heading"><div><span class="privacy-label">ACCOUNT STATUS</span><h2>注销资格</h2></div><span :class="['privacy-status', { blocked: !canCancel }]">{{ canCancel ? '可申请' : '暂不可用' }}</span></div>
          <div class="eligibility-row"><span>未完成订单</span><strong>{{ eligibility.unfinishedOrderCount }}</strong><small>{{ eligibility.unfinishedOrderCount ? '完成后才可注销' : '已完成' }}</small></div>
          <div class="eligibility-row"><span>处理中售后</span><strong>{{ eligibility.inProgressAfterSaleCount }}</strong><small>{{ eligibility.inProgressAfterSaleCount ? '处理结束后才可注销' : '无处理中事项' }}</small></div>
          <button class="danger-button" type="button" :disabled="!canCancel || cancellationBusy" @click="openCancellationDialog">注销账号</button>
        </article>
      </section>

      <section class="privacy-panel lifecycle-panel">
        <div class="privacy-panel-heading"><div><span class="privacy-label">AUDIT TRAIL</span><h2>账号生命周期</h2></div><span class="privacy-status">最近 {{ lifecycleEvents.length }} 条</span></div>
        <ol v-if="lifecycleEvents.length" class="lifecycle-timeline">
          <li v-for="event in lifecycleEvents" :key="`${event.eventType}-${event.eventTime}`"><span class="timeline-dot"></span><div><time>{{ formatTime(event.eventTime) }}</time><h3>{{ event.title }}</h3><p>{{ event.summary }}</p></div></li>
        </ol>
        <p v-else class="privacy-muted">暂无生命周期记录</p>
        <p class="privacy-hint">交易、售后和财务审计凭证不会因账号注销而物理删除。</p>
      </section>

      <section class="privacy-danger-zone"><div><span class="privacy-label">IRREVERSIBLE ACTION</span><h2>停用账号</h2><p>账号停用后立即退出所有设备，登录手机号不能再用于恢复本账号。</p></div><button class="danger-outline" type="button" :disabled="!canCancel || cancellationBusy" @click="openCancellationDialog">开始注销</button></section>
    </template>
    <section v-else class="privacy-login-hint"><h2>请先登录商城账号</h2><button class="privacy-primary" type="button" @click="router.push('/account')">前往个人中心</button></section>

    <div v-if="phoneDialog" class="privacy-modal-backdrop" @click.self="closePhoneDialog">
      <section class="privacy-modal" role="dialog" aria-modal="true" aria-labelledby="phone-dialog-title"><div class="modal-heading"><div><span class="privacy-label">PHONE CHANGE</span><h2 id="phone-dialog-title">更换手机号</h2></div><button class="modal-close" type="button" aria-label="关闭" @click="closePhoneDialog">×</button></div>
        <ol class="step-indicator"><li :class="{ active: phoneStep >= 1 }">1 <span>填写新号码</span></li><li :class="{ active: phoneStep >= 2 }">2 <span>验证当前号码</span></li><li :class="{ active: phoneStep >= 3 }">3 <span>验证新号码</span></li></ol>
        <form v-if="phoneStep === 1" @submit.prevent="beginPhoneChange"><label>新手机号<input v-model.trim="phoneRequest.newPhone" inputmode="numeric" maxlength="11" placeholder="请输入11位手机号" /></label><button class="privacy-primary" type="submit" :disabled="phoneBusy">{{ phoneBusy ? '处理中...' : '发送当前号码验证码' }}</button></form>
        <form v-else-if="phoneStep === 2" @submit.prevent="verifyOldPhone"><p class="modal-note">验证码已发送至当前手机号 {{ account.maskedPhone }}</p><label>当前手机号验证码<input v-model.trim="phoneRequest.code" inputmode="numeric" maxlength="6" autocomplete="one-time-code" placeholder="6位验证码" /></label><button class="privacy-primary" type="submit" :disabled="phoneBusy">{{ phoneBusy ? '验证中...' : '验证并发送新号码验证码' }}</button></form>
        <form v-else @submit.prevent="finishPhoneChange"><p class="modal-note">验证码已发送至新手机号 {{ phoneRequest.newPhone.slice(0, 3) }}****{{ phoneRequest.newPhone.slice(7) }}</p><label>新手机号验证码<input v-model.trim="phoneRequest.code" inputmode="numeric" maxlength="6" autocomplete="one-time-code" placeholder="6位验证码" /></label><button class="privacy-primary" type="submit" :disabled="phoneBusy">{{ phoneBusy ? '更换中...' : '确认更换并退出所有设备' }}</button></form>
      </section>
    </div>

    <div v-if="cancellationDialog" class="privacy-modal-backdrop" @click.self="cancellationDialog = false"><section class="privacy-modal" role="dialog" aria-modal="true" aria-labelledby="cancel-dialog-title"><div class="modal-heading"><div><span class="privacy-label">ACCOUNT CANCELLATION</span><h2 id="cancel-dialog-title">确认注销账号</h2></div><button class="modal-close" type="button" aria-label="关闭" @click="cancellationDialog = false">×</button></div><p class="modal-warning">注销会立即停用账号并下线全部设备，交易、售后和财务审计凭证会按要求保留。</p><form @submit.prevent="confirmCancellation"><label>当前手机号验证码<input v-model.trim="cancellationCode" inputmode="numeric" maxlength="6" autocomplete="one-time-code" placeholder="6位验证码" /></label><button class="danger-button" type="submit" :disabled="cancellationBusy">{{ cancellationBusy ? '注销中...' : '确认注销账号' }}</button></form></section></div>
  </main>
</template>

<style scoped>
.account-privacy-page { width: min(1120px, calc(100% - 40px)); margin: 0 auto; padding: 40px 0 88px; color: var(--ink, #20202a); }
.account-privacy-header { display: flex; align-items: flex-start; gap: 18px; margin-bottom: 30px; }
.account-privacy-header > div { min-width: 0; }
.privacy-back { width: 42px; height: 42px; border: 2px solid var(--ink, #20202a); border-radius: 8px; background: var(--yellow, #ffd24a); color: var(--ink, #20202a); font-size: 24px; cursor: pointer; }
.privacy-kicker, .privacy-label { color: var(--muted, #6f6870); font-size: 11px; font-weight: 900; letter-spacing: .12em; }
.account-privacy-header h1 { margin: 8px 0 6px; font-size: clamp(32px, 5vw, 54px); line-height: 1; }
.account-privacy-header p { margin: 0; color: var(--muted, #6f6870); overflow-wrap: anywhere; }
.privacy-overview, .privacy-grid { display: grid; grid-template-columns: 1.3fr .7fr; gap: 18px; margin-bottom: 18px; }
.privacy-identity, .privacy-phone, .privacy-panel, .privacy-danger-zone { border: 2px solid var(--ink, #20202a); border-radius: 8px; background: var(--paper, #fff8e8); box-shadow: 5px 5px 0 rgb(32 32 42 / 13%); }
.privacy-identity { display: flex; align-items: center; gap: 20px; padding: 26px; background: var(--mint, #d6f1dd); }
.privacy-avatar { display: grid; width: 82px; height: 82px; flex: 0 0 82px; place-items: center; overflow: hidden; border: 2px solid var(--ink, #20202a); border-radius: 50%; background: var(--coral, #f05d45); color: #fff; font-size: 30px; font-weight: 900; }
.privacy-avatar img { width: 100%; height: 100%; object-fit: cover; }
.privacy-identity h2 { margin: 7px 0 5px; font-size: 26px; }
.privacy-identity p, .privacy-phone small { margin: 0; color: var(--muted, #6f6870); }
.privacy-phone { display: flex; flex-direction: column; gap: 8px; padding: 24px; }
.privacy-phone strong { font-size: 28px; letter-spacing: .06em; }
.privacy-link, .privacy-primary, .danger-button, .danger-outline { min-height: 42px; border: 2px solid var(--ink, #20202a); border-radius: 7px; padding: 9px 15px; font-weight: 900; cursor: pointer; }
.privacy-link { align-self: flex-start; background: transparent; color: var(--red, #d64837); }
.privacy-grid { grid-template-columns: 1fr 1fr; }
.privacy-panel { padding: 24px; }
.privacy-panel-heading, .modal-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 14px; border-bottom: 2px solid var(--ink, #20202a); padding-bottom: 14px; }
.privacy-panel h2, .privacy-danger-zone h2, .privacy-modal h2 { margin: 6px 0 0; font-size: 24px; }
.privacy-status { border: 2px solid var(--ink, #20202a); border-radius: 5px; padding: 5px 8px; background: var(--yellow, #ffd24a); font-size: 11px; font-weight: 900; white-space: nowrap; }
.privacy-status.blocked { background: #f4c7be; }
.consent-list { display: grid; gap: 10px; padding-top: 16px; }
.consent-row { display: grid; grid-template-columns: 1fr auto auto; align-items: center; gap: 10px; padding: 10px 0; border-bottom: 1px solid var(--line, #ded5c7); }
.consent-row b { font-size: 12px; }
.consent-row time, .privacy-muted, .privacy-hint { color: var(--muted, #6f6870); font-size: 12px; }
.privacy-hint { margin: 16px 0 0; line-height: 1.7; }
.eligibility-row { display: grid; grid-template-columns: 1fr auto; gap: 3px 12px; padding: 15px 0; border-bottom: 1px solid var(--line, #ded5c7); }
.eligibility-row strong { color: var(--red, #d64837); font-size: 24px; }
.eligibility-row small { grid-column: 1 / -1; color: var(--muted, #6f6870); }
.danger-button { width: 100%; margin-top: 18px; background: var(--red, #d64837); color: #fff; }
.danger-button:disabled, .danger-outline:disabled { cursor: not-allowed; opacity: .45; }
.lifecycle-panel { margin-bottom: 18px; }
.lifecycle-timeline { display: grid; gap: 0; margin: 20px 0 0; padding: 0; list-style: none; }
.lifecycle-timeline li { position: relative; display: grid; grid-template-columns: 18px 1fr; gap: 14px; padding-bottom: 22px; }
.lifecycle-timeline li:not(:last-child)::before { position: absolute; top: 15px; bottom: 0; left: 7px; width: 2px; background: var(--line, #ded5c7); content: ''; }
.timeline-dot { z-index: 1; width: 16px; height: 16px; border: 2px solid var(--ink, #20202a); border-radius: 50%; background: var(--coral, #f05d45); }
.lifecycle-timeline time { color: var(--muted, #6f6870); font-size: 12px; }
.lifecycle-timeline h3 { margin: 3px 0; font-size: 16px; }
.lifecycle-timeline p { margin: 0; color: var(--muted, #6f6870); font-size: 13px; }
.privacy-danger-zone { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 24px; border-color: var(--red, #d64837); background: #fff0ed; }
.privacy-danger-zone p { margin: 8px 0 0; color: var(--muted, #6f6870); }
.danger-outline { background: transparent; color: var(--red, #d64837); }
.privacy-state, .privacy-login-hint { padding: 45px 24px; border: 2px solid var(--ink, #20202a); border-radius: 8px; background: var(--paper, #fff8e8); text-align: center; }
.privacy-error { color: var(--red, #d64837); }
.privacy-login-hint h2 { margin: 0 0 18px; }
.privacy-primary { background: var(--yellow, #ffd24a); color: var(--ink, #20202a); }
.privacy-modal-backdrop { position: fixed; z-index: 100; inset: 0; display: grid; place-items: center; padding: 18px; background: rgb(32 32 42 / 52%); }
.privacy-modal { width: min(470px, 100%); border: 2px solid var(--ink, #20202a); border-radius: 8px; padding: 24px; background: var(--paper, #fff8e8); box-shadow: 8px 8px 0 var(--ink, #20202a); }
.modal-close { border: 0; background: transparent; color: var(--ink, #20202a); font-size: 26px; cursor: pointer; }
.step-indicator { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin: 20px 0; padding: 0; list-style: none; }
.step-indicator li { display: grid; gap: 4px; border-bottom: 3px solid var(--line, #ded5c7); padding-bottom: 8px; color: var(--muted, #6f6870); font-weight: 900; }
.step-indicator li.active { border-color: var(--red, #d64837); color: var(--ink, #20202a); }
.step-indicator span { font-size: 10px; font-weight: 600; }
.privacy-modal form { display: grid; gap: 14px; }
.privacy-modal label { display: grid; gap: 7px; font-size: 13px; font-weight: 900; }
.privacy-modal input { min-height: 44px; border: 2px solid var(--ink, #20202a); border-radius: 6px; padding: 0 12px; background: #fff; color: var(--ink, #20202a); font-size: 16px; }
.modal-note, .modal-warning { margin: 0; color: var(--muted, #6f6870); line-height: 1.7; }
.modal-warning { border-left: 4px solid var(--red, #d64837); padding: 10px 12px; background: #fff0ed; }
@media (max-width: 760px) {
  .account-privacy-page { width: auto; max-width: 360px; margin: 0 auto; padding-top: 24px; overflow-x: hidden; }
  .account-privacy-header { display: grid; grid-template-columns: 42px minmax(0, 1fr); gap: 16px; }
  .account-privacy-header p { max-width: 100%; line-height: 1.6; }
  .privacy-state, .privacy-login-hint { box-sizing: border-box; width: 100%; max-width: 100%; }
  .privacy-overview, .privacy-grid { grid-template-columns: 1fr; }
  .privacy-danger-zone { align-items: flex-start; flex-direction: column; }
  .danger-outline { width: 100%; }
  .privacy-identity { padding: 20px; }
}
@media (prefers-reduced-motion: reduce) { * { scroll-behavior: auto !important; transition: none !important; } }
</style>
