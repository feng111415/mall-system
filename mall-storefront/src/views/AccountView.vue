<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { getProfile, loginBySms, logoutMember, sendSmsCode } from '../api/member'

const phone = ref('')
const code = ref('')
const agreed = ref(false)
const message = ref('')
const loading = ref(false)
const seconds = ref(0)
const member = ref(null)
let timer

onMounted(async () => {
  if (!sessionStorage.getItem('mall-user-token')) return
  try { member.value = (await getProfile()).data.data } catch { sessionStorage.removeItem('mall-user-token') }
})
onUnmounted(() => clearInterval(timer))

async function sendCode() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) return message.value = '请输入正确的中国大陆手机号'
  if (seconds.value) return
  loading.value = true
  try {
    await sendSmsCode(phone.value)
    message.value = '验证码已发送，开发环境 Mock 验证码由服务配置提供'
    seconds.value = 60
    timer = setInterval(() => { if (--seconds.value <= 0) clearInterval(timer) }, 1000)
  } catch (error) { message.value = error.response?.data?.msg || '验证码发送失败，请稍后重试' }
  finally { loading.value = false }
}
async function login() {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) return message.value = '请输入正确的中国大陆手机号'
  if (!/^\d{6}$/.test(code.value)) return message.value = '请输入 6 位验证码'
  if (!agreed.value) return message.value = '请先同意用户协议和隐私政策'
  loading.value = true
  try {
    const response = await loginBySms({ phone: phone.value, code: code.value, agreed: true, userAgreementVersion: '1.0', privacyPolicyVersion: '1.0' })
    sessionStorage.setItem('mall-user-token', response.data.data.token)
    member.value = response.data.data.member
    message.value = response.data.data.newMember ? '账号已创建并登录' : '登录成功'
  } catch (error) { message.value = error.response?.data?.msg || '登录失败，请检查验证码' }
  finally { loading.value = false }
}
async function logout() {
  try { await logoutMember() } finally { sessionStorage.removeItem('mall-user-token'); member.value = null; message.value = '已退出登录' }
}
</script>

<template>
  <section class="account-wrap">
    <div class="page-intro"><span class="section-kicker">个人中心</span><h1>{{ member ? `你好，${member.nickname}` : '欢迎回来' }}</h1><p>{{ member ? `手机号 ${member.maskedPhone || member.phone}` : '使用手机号验证码安全登录，未注册号码验证后将创建商城账号' }}</p></div>
    <div v-if="member" class="profile-panel">
      <div class="profile-avatar">{{ member.nickname?.slice(0, 1) }}</div><div><strong>{{ member.nickname }}</strong><p>会员编号 M{{ member.memberId }}</p></div>
      <button class="add-button" @click="logout">退出登录</button>
    </div>
    <div v-else class="login-panel">
      <label>手机号<input v-model.trim="phone" inputmode="numeric" maxlength="11" placeholder="请输入手机号" /></label>
      <label>短信验证码<div class="code-field"><input v-model.trim="code" inputmode="numeric" maxlength="6" placeholder="6 位验证码" /><button type="button" :disabled="loading || seconds > 0" @click="sendCode">{{ seconds ? `${seconds}s 后重发` : '获取验证码' }}</button></div></label>
      <label class="agreement"><input v-model="agreed" type="checkbox" /> 我已阅读并同意《用户协议》和《隐私政策》</label>
      <button class="primary-button login-button" :disabled="loading" @click="login">{{ loading ? '处理中...' : '登录 / 注册' }}</button>
    </div>
    <p v-if="message" class="form-message">{{ message }}</p>
  </section>
</template>
