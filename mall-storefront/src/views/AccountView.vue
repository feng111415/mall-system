<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { addAddress, getAddresses, getProfile, loginBySms, logoutMember, sendSmsCode } from '../api/member'
import { getOrders } from '../api/order'
import { getCart } from '../api/cart'

const phone = ref('')
const code = ref('')
const agreed = ref(false)
const message = ref('')
const loading = ref(false)
const seconds = ref(0)
const member = ref(null)
const addresses = ref([])
const orderCount = ref(0)
const cartCount = ref(0)
const profileLoading = ref(false)
const addressFormOpen = ref(false)
const addressSaving = ref(false)
const addressForm = ref({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', postalCode: '', isDefault: '0' })
let timer

onMounted(async () => {
  if (!sessionStorage.getItem('mall-user-token')) return
  await loadProfile()
})
onUnmounted(() => clearInterval(timer))

async function loadProfile() {
  profileLoading.value = true
  try {
    const [profileResponse, addressResponse, orderResponse, cartResponse] = await Promise.all([
      getProfile(), getAddresses(), getOrders({ limit: 50 }), getCart()
    ])
    member.value = profileResponse.data.data
    addresses.value = addressResponse.data.data || []
    orderCount.value = (orderResponse.data.data || []).length
    cartCount.value = (cartResponse.data.data?.items || []).length
  } catch { sessionStorage.removeItem('mall-user-token') }
  finally { profileLoading.value = false }
}

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
async function saveAddress() {
  const value = addressForm.value
  if (!value.receiverName || !/^1[3-9]\d{9}$/.test(value.receiverPhone) || !value.province || !value.city || !value.district || !value.detailAddress) {
    message.value = '请完整填写收货人、手机号和收货地址'
    return
  }
  addressSaving.value = true
  message.value = ''
  try {
    const response = await addAddress(value)
    const saved = response.data.data
    if (saved) addresses.value = [saved, ...addresses.value]
    addressFormOpen.value = false
    addressForm.value = { receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', postalCode: '', isDefault: '0' }
    message.value = '收货地址已保存'
  } catch (error) {
    message.value = error.response?.data?.msg || '收货地址保存失败，请稍后重试'
  } finally { addressSaving.value = false }
}
</script>

<template>
  <section class="account-wrap">
    <div class="page-intro"><span class="section-kicker">个人中心</span><h1>{{ member ? `你好，${member.nickname}` : '欢迎回来' }}</h1><p>{{ member ? `手机号 ${member.maskedPhone || member.phone}` : '使用手机号验证码安全登录，未注册号码验证后将创建商城账号' }}</p></div>
    <p v-if="profileLoading" class="loading-note">正在加载个人中心...</p>
    <template v-else-if="member">
    <div class="profile-panel">
      <div class="profile-avatar">{{ member.nickname?.slice(0, 1) }}</div><div><strong>{{ member.nickname }}</strong><p>会员编号 M{{ member.memberId }}</p></div>
      <button class="add-button" @click="logout">退出登录</button>
    </div>
    <div class="account-overview">
      <router-link to="/orders"><strong>{{ orderCount }}</strong><span>我的订单</span></router-link>
      <router-link to="/cart"><strong>{{ cartCount }}</strong><span>购物车商品</span></router-link>
      <div><strong>{{ addresses.length }}</strong><span>收货地址</span></div>
    </div>
    <div class="account-sections">
      <section class="account-section"><div class="section-heading"><div><span class="section-kicker">账户资料</span><h2>登录信息</h2></div></div><dl class="profile-details"><div><dt>手机号</dt><dd>{{ member.maskedPhone }}</dd></div><div><dt>会员编号</dt><dd>M{{ member.memberId }}</dd></div><div><dt>最近登录</dt><dd>{{ member.lastLoginTime || '本次登录' }}</dd></div></dl></section>
      <section class="account-section"><div class="section-heading"><div><span class="section-kicker">收货地址</span><h2>地址簿</h2></div><div class="address-actions"><button class="text-button" type="button" @click="addressFormOpen = !addressFormOpen">{{ addressFormOpen ? '取消新增' : '新增地址' }}</button><router-link class="text-link" to="/checkout">去结算管理地址</router-link></div></div><div v-if="addressFormOpen" class="address-form"><div class="address-form-grid"><input v-model.trim="addressForm.receiverName" placeholder="收货人姓名" /><input v-model.trim="addressForm.receiverPhone" inputmode="numeric" maxlength="11" placeholder="收货人手机号" /><input v-model.trim="addressForm.province" placeholder="省/自治区" /><input v-model.trim="addressForm.city" placeholder="城市" /><input v-model.trim="addressForm.district" placeholder="区/县" /></div><input v-model.trim="addressForm.detailAddress" placeholder="详细地址" /><input v-model.trim="addressForm.postalCode" maxlength="12" placeholder="邮政编码（可选）" /><label class="default-address"><input v-model="addressForm.isDefault" true-value="1" false-value="0" type="checkbox" />设为默认地址</label><button class="primary-button" type="button" :disabled="addressSaving" @click="saveAddress">{{ addressSaving ? '正在保存...' : '保存地址' }}</button></div><div v-if="addresses.length" class="account-address-list"><article v-for="address in addresses" :key="address.addressId" class="account-address"><strong>{{ address.receiverName }} {{ address.receiverPhone }}</strong><span>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}</span><em v-if="address.isDefault === '1'">默认地址</em></article></div><p v-else class="loading-note">暂未保存收货地址，下单时可以添加。</p></section>
    </div>
    </template>
    <div v-else class="login-panel">
      <label>手机号<input v-model.trim="phone" inputmode="numeric" maxlength="11" placeholder="请输入手机号" /></label>
      <label>短信验证码<div class="code-field"><input v-model.trim="code" inputmode="numeric" maxlength="6" placeholder="6 位验证码" /><button type="button" :disabled="loading || seconds > 0" @click="sendCode">{{ seconds ? `${seconds}s 后重发` : '获取验证码' }}</button></div></label>
      <label class="agreement"><input v-model="agreed" type="checkbox" /> 我已阅读并同意《用户协议》和《隐私政策》</label>
      <button class="primary-button login-button" :disabled="loading" @click="login">{{ loading ? '处理中...' : '登录 / 注册' }}</button>
    </div>
    <p v-if="message" class="form-message">{{ message }}</p>
  </section>
</template>
