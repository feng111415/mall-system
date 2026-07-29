<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useCascaderAreaData } from '@vant/area-data'
import { Icon as VanIcon } from 'vant'
import {
  addAddress,
  deleteAddress,
  getAddresses,
  getProfile,
  loginBySms,
  logoutMember,
  sendSmsCode,
  updateAddress
} from '../api/member'
import { getOrders } from '../api/order'
import { getCart } from '../api/cart'
import { useCartStore } from '../stores/cart'
import { useNoticeStore } from '../stores/notice'

const cart = useCartStore()
const notice = useNoticeStore()

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
const addressEditingId = ref(null)
const addressBusyId = ref(null)
const deleteConfirmId = ref(null)
const addressErrors = ref({})
const regionOptions = useCascaderAreaData()
const provinceCode = ref('')
const cityCode = ref('')
const districtCode = ref('')
const cityOptions = computed(() => regionOptions.find(item => item.value === provinceCode.value)?.children || [])
const districtOptions = computed(() => cityOptions.value.find(item => item.value === cityCode.value)?.children || [])
const emptyAddress = () => ({ receiverName: '', receiverPhone: '', province: '', city: '', district: '', detailAddress: '', postalCode: '', isDefault: '0' })
const addressForm = ref(emptyAddress())
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
  } catch {
    sessionStorage.removeItem('mall-user-token')
  } finally {
    profileLoading.value = false
  }
}

async function refreshAddresses() {
  const response = await getAddresses()
  addresses.value = response.data.data || []
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
  } catch (error) {
    message.value = error.response?.data?.msg || '验证码发送失败，请稍后重试'
  } finally {
    loading.value = false
  }
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
    await cart.load().catch(() => {})
    await loadProfile()
    notice.show(message.value)
  } catch (error) {
    message.value = error.response?.data?.msg || '登录失败，请检查验证码'
    notice.show(message.value, 'error')
  } finally {
    loading.value = false
  }
}

async function logout() {
  try {
    await logoutMember()
  } finally {
    sessionStorage.removeItem('mall-user-token')
    cart.reset()
    member.value = null
    addresses.value = []
    closeAddressForm()
    message.value = '已退出登录'
    notice.show(message.value)
  }
}

function addressPayload(address) {
  return {
    receiverName: address.receiverName?.trim(),
    receiverPhone: address.receiverPhone?.trim(),
    province: address.province?.trim(),
    city: address.city?.trim(),
    district: address.district?.trim(),
    detailAddress: address.detailAddress?.trim(),
    postalCode: address.postalCode?.trim(),
    isDefault: address.isDefault === '1' ? '1' : '0'
  }
}

function validateAddress() {
  const value = addressPayload(addressForm.value)
  const errors = {}
  if (!value.receiverName) errors.receiverName = '请输入收货人姓名'
  else if (value.receiverName.length > 50) errors.receiverName = '收货人不能超过 50 个字符'
  if (!/^1[3-9]\d{9}$/.test(value.receiverPhone || '')) errors.receiverPhone = '请输入正确的中国大陆手机号'
  if (!value.province || !value.city || !value.district) errors.region = '请选择完整的省、市、区县'
  if (!value.detailAddress) errors.detailAddress = '请输入详细地址'
  else if (value.detailAddress.length > 255) errors.detailAddress = '详细地址不能超过 255 个字符'
  addressErrors.value = errors
  return Object.keys(errors).length === 0
}

function openAddressForm(address = null) {
  addressEditingId.value = address?.addressId || null
  addressForm.value = address ? addressPayload(address) : emptyAddress()
  addressErrors.value = {}
  deleteConfirmId.value = null
  setRegionCodes(address)
  addressFormOpen.value = true
}

function closeAddressForm() {
  addressFormOpen.value = false
  addressEditingId.value = null
  addressErrors.value = {}
  addressForm.value = emptyAddress()
  provinceCode.value = ''
  cityCode.value = ''
  districtCode.value = ''
}

function setRegionCodes(address) {
  const province = regionOptions.find(item => item.text === address?.province)
  const city = province?.children?.find(item => item.text === address?.city)
  const district = city?.children?.find(item => item.text === address?.district)
  provinceCode.value = province?.value || ''
  cityCode.value = city?.value || ''
  districtCode.value = district?.value || ''
}

async function saveAddress() {
  if (!validateAddress()) return
  addressSaving.value = true
  try {
    const payload = addressPayload(addressForm.value)
    if (addressEditingId.value) await updateAddress(addressEditingId.value, payload)
    else await addAddress(payload)
    await refreshAddresses()
    notice.show(addressEditingId.value ? '收货地址已更新' : '收货地址已新增')
    closeAddressForm()
  } catch (error) {
    notice.show(error.response?.data?.msg || '收货地址保存失败，请稍后重试', 'error')
  } finally {
    addressSaving.value = false
  }
}

async function setDefaultAddress(address) {
  if (address.isDefault === '1' || addressBusyId.value) return
  addressBusyId.value = address.addressId
  deleteConfirmId.value = null
  try {
    await updateAddress(address.addressId, { ...addressPayload(address), isDefault: '1' })
    await refreshAddresses()
    notice.show('默认收货地址已更新')
  } catch (error) {
    notice.show(error.response?.data?.msg || '默认地址设置失败，请稍后重试', 'error')
  } finally {
    addressBusyId.value = null
  }
}

async function removeAddress(address) {
  if (addressBusyId.value) return
  addressBusyId.value = address.addressId
  try {
    await deleteAddress(address.addressId)
    await refreshAddresses()
    if (addressEditingId.value === address.addressId) closeAddressForm()
    deleteConfirmId.value = null
    notice.show('收货地址已删除')
  } catch (error) {
    notice.show(error.response?.data?.msg || '收货地址删除失败，请稍后重试', 'error')
  } finally {
    addressBusyId.value = null
  }
}

function handleProvinceChange() {
  const province = regionOptions.find(item => item.value === provinceCode.value)
  addressForm.value.province = province?.text || ''
  addressForm.value.city = ''
  addressForm.value.district = ''
  cityCode.value = ''
  districtCode.value = ''
  delete addressErrors.value.region
}

function handleCityChange() {
  const city = cityOptions.value.find(item => item.value === cityCode.value)
  addressForm.value.city = city?.text || ''
  addressForm.value.district = ''
  districtCode.value = ''
  delete addressErrors.value.region
}

function handleDistrictChange() {
  const district = districtOptions.value.find(item => item.value === districtCode.value)
  addressForm.value.district = district?.text || ''
  delete addressErrors.value.region
}
</script>

<template>
  <section class="account-wrap">
    <div class="page-intro">
      <span class="section-kicker">个人中心</span>
      <h1>{{ member ? `你好，${member.nickname}` : '欢迎回来' }}</h1>
      <p>{{ member ? `手机号 ${member.maskedPhone || member.phone}` : '使用手机号验证码安全登录，未注册号码验证后将创建商城账号' }}</p>
    </div>

    <p v-if="profileLoading" class="loading-note">正在加载个人中心...</p>

    <template v-else-if="member">
      <div class="profile-panel">
        <div class="profile-avatar">{{ member.nickname?.slice(0, 1) }}</div>
        <div><strong>{{ member.nickname }}</strong><p>会员编号 M{{ member.memberId }}</p></div>
        <button class="add-button" type="button" @click="logout">退出登录</button>
      </div>

      <div class="account-overview">
        <router-link to="/orders"><strong>{{ orderCount }}</strong><span>我的订单</span></router-link>
        <router-link to="/cart"><strong>{{ cartCount }}</strong><span>购物车商品</span></router-link>
        <div><strong>{{ addresses.length }}</strong><span>收货地址</span></div>
      </div>

      <div class="account-sections">
        <section class="account-section">
          <div class="section-heading"><div><span class="section-kicker">账户资料</span><h2>登录信息</h2></div></div>
          <dl class="profile-details">
            <div><dt>手机号</dt><dd>{{ member.maskedPhone }}</dd></div>
            <div><dt>会员编号</dt><dd>M{{ member.memberId }}</dd></div>
            <div><dt>最近登录</dt><dd>{{ member.lastLoginTime || '本次登录' }}</dd></div>
          </dl>
        </section>

        <section class="account-section address-book-section">
          <div class="section-heading">
            <div><span class="section-kicker">收货地址</span><h2>地址簿</h2></div>
            <div class="address-actions">
              <button v-if="addressFormOpen" class="text-button" type="button" @click="closeAddressForm">取消</button>
              <button v-else class="text-button address-add" type="button" @click="openAddressForm()"><VanIcon name="plus" />新增地址</button>
              <router-link class="text-link" to="/checkout">去结算</router-link>
            </div>
          </div>

          <form v-if="addressFormOpen" class="address-form" @submit.prevent="saveAddress">
            <div class="address-form-title">
              <strong>{{ addressEditingId ? '编辑收货地址' : '新增收货地址' }}</strong>
              <span v-if="addressEditingId">编号 A{{ addressEditingId }}</span>
            </div>
            <div class="address-form-grid">
              <label>
                <span>收货人</span>
                <input v-model.trim="addressForm.receiverName" maxlength="50" :aria-invalid="Boolean(addressErrors.receiverName)" placeholder="姓名" @input="delete addressErrors.receiverName" />
                <small v-if="addressErrors.receiverName">{{ addressErrors.receiverName }}</small>
              </label>
              <label>
                <span>手机号</span>
                <input v-model.trim="addressForm.receiverPhone" inputmode="numeric" maxlength="11" :aria-invalid="Boolean(addressErrors.receiverPhone)" placeholder="11 位手机号" @input="delete addressErrors.receiverPhone" />
                <small v-if="addressErrors.receiverPhone">{{ addressErrors.receiverPhone }}</small>
              </label>
            </div>
            <div class="address-region-grid">
              <label>
                <span>省/自治区</span>
                <select v-model="provinceCode" :aria-invalid="Boolean(addressErrors.region)" @change="handleProvinceChange">
                  <option value="">{{ addressForm.province ? `当前：${addressForm.province}` : '请选择' }}</option>
                  <option v-for="item in regionOptions" :key="item.value" :value="item.value">{{ item.text }}</option>
                </select>
              </label>
              <label>
                <span>城市</span>
                <select v-model="cityCode" :disabled="!provinceCode" :aria-invalid="Boolean(addressErrors.region)" @change="handleCityChange">
                  <option value="">{{ addressForm.city ? `当前：${addressForm.city}` : '请选择' }}</option>
                  <option v-for="item in cityOptions" :key="item.value" :value="item.value">{{ item.text }}</option>
                </select>
              </label>
              <label>
                <span>区/县</span>
                <select v-model="districtCode" :disabled="!cityCode" :aria-invalid="Boolean(addressErrors.region)" @change="handleDistrictChange">
                  <option value="">{{ addressForm.district ? `当前：${addressForm.district}` : '请选择' }}</option>
                  <option v-for="item in districtOptions" :key="item.value" :value="item.value">{{ item.text }}</option>
                </select>
              </label>
            </div>
            <small v-if="addressErrors.region" class="field-error">{{ addressErrors.region }}</small>
            <label>
              <span>详细地址</span>
              <input v-model.trim="addressForm.detailAddress" maxlength="255" :aria-invalid="Boolean(addressErrors.detailAddress)" placeholder="街道、楼栋、门牌号" @input="delete addressErrors.detailAddress" />
              <small v-if="addressErrors.detailAddress">{{ addressErrors.detailAddress }}</small>
            </label>
            <label>
              <span>邮政编码 <small>选填</small></span>
              <input v-model.trim="addressForm.postalCode" maxlength="12" placeholder="邮政编码" />
            </label>
            <div class="address-form-footer">
              <label class="default-address"><input v-model="addressForm.isDefault" true-value="1" false-value="0" type="checkbox" />设为默认地址</label>
              <button class="primary-button" type="submit" :disabled="addressSaving">{{ addressSaving ? '正在保存...' : (addressEditingId ? '保存修改' : '保存地址') }}</button>
            </div>
          </form>

          <div v-if="addresses.length" class="account-address-list">
            <article v-for="address in addresses" :key="address.addressId" class="account-address" :class="{ 'is-default': address.isDefault === '1' }">
              <div class="account-address-copy">
                <div class="account-address-name"><strong>{{ address.receiverName }}</strong><span>{{ address.receiverPhone }}</span><em v-if="address.isDefault === '1'">默认</em></div>
                <p>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}</p>
                <small v-if="address.postalCode">邮编 {{ address.postalCode }}</small>
              </div>
              <div class="account-address-tools">
                <button v-if="address.isDefault !== '1'" type="button" :disabled="Boolean(addressBusyId)" @click="setDefaultAddress(address)"><VanIcon name="location-o" />设为默认</button>
                <button type="button" :disabled="Boolean(addressBusyId)" @click="openAddressForm(address)"><VanIcon name="edit" />编辑</button>
                <button class="address-delete" type="button" :disabled="Boolean(addressBusyId)" @click="deleteConfirmId = address.addressId"><VanIcon name="delete-o" />删除</button>
              </div>
              <div v-if="deleteConfirmId === address.addressId" class="address-delete-confirm" role="alert">
                <span>确认删除这条地址？</span>
                <button type="button" :disabled="addressBusyId === address.addressId" @click="removeAddress(address)">{{ addressBusyId === address.addressId ? '删除中...' : '确认删除' }}</button>
                <button type="button" :disabled="addressBusyId === address.addressId" @click="deleteConfirmId = null">取消</button>
              </div>
            </article>
          </div>
          <div v-else class="account-address-empty">
            <VanIcon name="location-o" />
            <p>暂未保存收货地址</p>
            <button class="text-button" type="button" @click="openAddressForm()">新增地址</button>
          </div>
        </section>
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
