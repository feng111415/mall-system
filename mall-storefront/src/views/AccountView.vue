<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useCascaderAreaData } from '@vant/area-data'
import { Icon as VanIcon } from 'vant'
import {
  addAddress,
  createSmsChallenge,
  deleteAddress,
  getAddresses,
  getAvatarPresets,
  getMemberSessions,
  getProfile,
  loginBySms,
  logoutMember,
  replacePrimaryDevice,
  revokeMemberSession,
  selectPresetAvatar,
  sendSmsCode,
  verifySmsChallenge,
  sendPrimaryDeviceCode,
  updateNickname,
  uploadAvatar,
  updateAddress
} from '../api/member'
import { getOrders } from '../api/order'
import { getCart } from '../api/cart'
import { useCartStore } from '../stores/cart'
import { useNoticeStore } from '../stores/notice'
import { useMessageStore } from '../stores/message'

const cart = useCartStore()
const notice = useNoticeStore()
const messageStore = useMessageStore()
const SMS_COOLDOWN_KEY = 'mall-sms-code-cooldown'
const SMS_COOLDOWN_MS = 60 * 1000

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
const profileEditing = ref(false)
const nicknameDraft = ref('')
const nicknameSaving = ref(false)
const avatarPresets = ref([])
const avatarEditorOpen = ref(false)
const avatarSaving = ref(false)
const avatarInput = ref(null)
const sessionOverview = ref({ sessions: [], primaryChangesRemaining: 0, primaryChangeWindowDays: 30 })
const sessionBusyId = ref(null)
const sessionRevokeConfirmId = ref(null)
const primaryCodeOpen = ref(false)
const primaryCode = ref('')
const primarySeconds = ref(0)
const primaryCodeSending = ref(false)
const primaryReplacing = ref(false)
const smsChallengeOpen = ref(false)
const smsChallengeLoading = ref(false)
const smsChallengeVerifying = ref(false)
const smsChallengeError = ref('')
const smsChallenge = ref(null)
const smsChallengePosition = ref(12)
const cropOpen = ref(false)
const cropImageUrl = ref('')
const cropZoom = ref(1)
const cropX = ref(50)
const cropY = ref(50)
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
let primaryTimer
let cropSourceImage = null

onMounted(async () => {
  restoreSmsCooldown()
  if (!sessionStorage.getItem('mall-user-token')) return
  await loadProfile()
})
onUnmounted(() => {
  clearInterval(timer)
  clearInterval(primaryTimer)
  releaseCropImage()
})

async function loadProfile() {
  profileLoading.value = true
  try {
    const [profileResponse, addressResponse, orderResponse, cartResponse, presetResponse, sessionResponse] = await Promise.all([
      getProfile(), getAddresses(), getOrders({ limit: 50 }), getCart(),
      getAvatarPresets().catch(() => ({ data: { data: [] } })), getMemberSessions()
    ])
    member.value = profileResponse.data.data
    nicknameDraft.value = member.value.nickname
    addresses.value = addressResponse.data.data || []
    orderCount.value = (orderResponse.data.data || []).length
    cartCount.value = (cartResponse.data.data?.items || []).length
    avatarPresets.value = presetResponse.data.data || []
    sessionOverview.value = sessionResponse.data.data || sessionOverview.value
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

function startSmsCooldown(value, expiresAt = Date.now() + SMS_COOLDOWN_MS) {
  sessionStorage.setItem(SMS_COOLDOWN_KEY, JSON.stringify({ phone: value, expiresAt }))
  clearInterval(timer)

  const updateSeconds = () => {
    seconds.value = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000))
    if (seconds.value > 0) return
    clearInterval(timer)
    sessionStorage.removeItem(SMS_COOLDOWN_KEY)
  }

  updateSeconds()
  if (seconds.value > 0) timer = setInterval(updateSeconds, 1000)
}

function restoreSmsCooldown() {
  const stored = sessionStorage.getItem(SMS_COOLDOWN_KEY)
  if (!stored) return

  try {
    const cooldown = JSON.parse(stored)
    if (!/^1[3-9]\d{9}$/.test(cooldown.phone) || !Number.isFinite(cooldown.expiresAt) || cooldown.expiresAt <= Date.now()) {
      sessionStorage.removeItem(SMS_COOLDOWN_KEY)
      return
    }
    phone.value = cooldown.phone
    startSmsCooldown(cooldown.phone, cooldown.expiresAt)
  } catch {
    sessionStorage.removeItem(SMS_COOLDOWN_KEY)
  }
}

async function openSmsChallenge() {
  smsChallengeLoading.value = true
  smsChallengeError.value = ''
  try {
    const response = await createSmsChallenge(phone.value)
    smsChallenge.value = response.data.data
    smsChallengePosition.value = smsChallenge.value?.pieceX ?? 12
    smsChallengeOpen.value = true
  } catch (error) {
    smsChallengeError.value = error.response?.data?.msg || '安全验证加载失败，请稍后重试'
    message.value = smsChallengeError.value
  } finally {
    smsChallengeLoading.value = false
  }
}

function closeSmsChallenge() {
  smsChallengeOpen.value = false
  smsChallenge.value = null
  smsChallengeError.value = ''
}

async function verifySmsChallengeAndSend() {
  if (!smsChallenge.value || smsChallengeVerifying.value) return
  smsChallengeVerifying.value = true
  smsChallengeError.value = ''
  try {
    const response = await verifySmsChallenge({
      challengeId: smsChallenge.value.challengeId,
      position: Number(smsChallengePosition.value)
    })
    closeSmsChallenge()
    await sendCode(response.data.data.ticket)
  } catch (error) {
    smsChallengeError.value = error.response?.data?.msg || '拼图位置不正确，请重试'
  } finally {
    smsChallengeVerifying.value = false
  }
}

async function sendCode(challengeTicket = '') {
  if (!/^1[3-9]\d{9}$/.test(phone.value)) return message.value = '请输入正确的中国大陆手机号'
  if (seconds.value) return
  loading.value = true
  try {
    const response = await sendSmsCode(phone.value, challengeTicket)
    if (response.data.data?.challengeRequired) {
      await openSmsChallenge()
      return
    }
    message.value = '验证码已发送，开发环境 Mock 验证码由服务配置提供'
    startSmsCooldown(phone.value)
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
    await messageStore.load().catch(() => {})
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
    messageStore.reset()
    member.value = null
    addresses.value = []
    sessionOverview.value = { sessions: [], primaryChangesRemaining: 0, primaryChangeWindowDays: 30 }
    primaryCodeOpen.value = false
    primaryCode.value = ''
    clearInterval(primaryTimer)
    avatarEditorOpen.value = false
    profileEditing.value = false
    releaseCropImage()
    closeAddressForm()
    message.value = '已退出登录'
    notice.show(message.value)
  }
}

function formatSessionTime(value) {
  if (!value) return '未知时间'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false
  }).format(new Date(value))
}

async function revokeSession(session) {
  if (session.current || sessionBusyId.value) return
  sessionBusyId.value = session.sessionId
  try {
    const response = await revokeMemberSession(session.sessionId)
    sessionOverview.value = response.data.data
    sessionRevokeConfirmId.value = null
    notice.show('设备已下线')
  } catch (error) {
    notice.show(error.response?.data?.msg || '设备下线失败，请稍后重试', 'error')
  } finally {
    sessionBusyId.value = null
  }
}

function openPrimaryDeviceChange() {
  primaryCode.value = ''
  primaryCodeOpen.value = true
  nextTick(() => {
    const form = document.querySelector('.primary-device-form')
    const navigation = document.querySelector('.mobile-nav')
    if (!form) return
    const navigationHeight = navigation && getComputedStyle(navigation).display !== 'none'
      ? navigation.getBoundingClientRect().height : 0
    const visibleBottom = window.innerHeight - navigationHeight - 12
    const formBottom = form.getBoundingClientRect().bottom
    if (formBottom > visibleBottom) {
      window.scrollBy({
        top: formBottom - visibleBottom,
        behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'auto' : 'smooth'
      })
    }
  })
}

function closePrimaryDeviceChange() {
  if (primaryCodeSending.value || primaryReplacing.value) return
  primaryCodeOpen.value = false
  primaryCode.value = ''
}

async function requestPrimaryDeviceCode() {
  if (primarySeconds.value || primaryCodeSending.value) return
  primaryCodeSending.value = true
  try {
    await sendPrimaryDeviceCode()
    primarySeconds.value = 60
    clearInterval(primaryTimer)
    primaryTimer = setInterval(() => {
      if (--primarySeconds.value <= 0) clearInterval(primaryTimer)
    }, 1000)
    notice.show('验证码已发送')
  } catch (error) {
    notice.show(error.response?.data?.msg || '验证码发送失败，请稍后重试', 'error')
  } finally {
    primaryCodeSending.value = false
  }
}

async function confirmPrimaryDevice() {
  if (!/^\d{6}$/.test(primaryCode.value)) {
    notice.show('请输入 6 位验证码', 'error')
    return
  }
  primaryReplacing.value = true
  try {
    const response = await replacePrimaryDevice(primaryCode.value)
    sessionOverview.value = response.data.data
    primaryCodeOpen.value = false
    primaryCode.value = ''
    notice.show('当前设备已设为主设备')
  } catch (error) {
    notice.show(error.response?.data?.msg || '主设备更换失败，请稍后重试', 'error')
  } finally {
    primaryReplacing.value = false
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

function openNicknameEditor() {
  nicknameDraft.value = member.value?.nickname || ''
  profileEditing.value = true
  nextTick(() => document.querySelector('.nickname-form')?.scrollIntoView({ block: 'center' }))
}

function closeNicknameEditor() {
  nicknameDraft.value = member.value?.nickname || ''
  profileEditing.value = false
}

function toggleAvatarEditor() {
  avatarEditorOpen.value = !avatarEditorOpen.value
  if (avatarEditorOpen.value) {
    nextTick(() => document.querySelector('.avatar-editor')?.scrollIntoView({ block: 'center' }))
  }
}

async function saveNickname() {
  const nickname = nicknameDraft.value.trim().replace(/\s+/g, ' ')
  const length = Array.from(nickname).length
  if (length < 2 || length > 20) {
    notice.show('昵称长度需为 2 至 20 个字符', 'error')
    return
  }
  nicknameSaving.value = true
  try {
    const response = await updateNickname(nickname)
    member.value = response.data.data
    nicknameDraft.value = member.value.nickname
    profileEditing.value = false
    notice.show('昵称已更新')
  } catch (error) {
    notice.show(error.response?.data?.msg || '昵称修改失败，请稍后重试', 'error')
  } finally {
    nicknameSaving.value = false
  }
}

async function choosePreset(preset) {
  if (avatarSaving.value) return
  avatarSaving.value = true
  try {
    const response = await selectPresetAvatar(preset.code)
    member.value = response.data.data
    avatarEditorOpen.value = false
    notice.show('头像已更新')
  } catch (error) {
    notice.show(error.response?.data?.msg || '头像修改失败，请稍后重试', 'error')
  } finally {
    avatarSaving.value = false
  }
}

function releaseCropImage() {
  if (cropImageUrl.value) URL.revokeObjectURL(cropImageUrl.value)
  cropImageUrl.value = ''
  cropOpen.value = false
  cropSourceImage = null
}

function selectAvatarFile() {
  if (!avatarSaving.value) avatarInput.value?.click()
}

function handleAvatarFile(event) {
  const file = event.target.files?.[0]
  event.target.value = ''
  if (!file) return
  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    notice.show('头像仅支持 JPG、JPEG、PNG 格式', 'error')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    notice.show('头像图片不能超过 5MB', 'error')
    return
  }
  releaseCropImage()
  const imageUrl = URL.createObjectURL(file)
  const image = new Image()
  image.onload = () => {
    if (image.naturalWidth < 64 || image.naturalHeight < 64) {
      URL.revokeObjectURL(imageUrl)
      notice.show('头像尺寸不能小于 64×64 像素', 'error')
      return
    }
    cropSourceImage = image
    cropImageUrl.value = imageUrl
    cropZoom.value = 1
    cropX.value = 50
    cropY.value = 50
    cropOpen.value = true
  }
  image.onerror = () => {
    URL.revokeObjectURL(imageUrl)
    notice.show('头像文件不是有效图片', 'error')
  }
  image.src = imageUrl
}

async function confirmAvatarCrop() {
  if (!cropSourceImage || avatarSaving.value) return
  const width = cropSourceImage.naturalWidth
  const height = cropSourceImage.naturalHeight
  const cropSize = Math.min(width, height) / cropZoom.value
  const sourceX = (width - cropSize) * (cropX.value / 100)
  const sourceY = (height - cropSize) * (cropY.value / 100)
  const canvas = document.createElement('canvas')
  canvas.width = 512
  canvas.height = 512
  const context = canvas.getContext('2d')
  context.imageSmoothingEnabled = true
  context.imageSmoothingQuality = 'high'
  context.drawImage(cropSourceImage, sourceX, sourceY, cropSize, cropSize, 0, 0, 512, 512)
  const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/png', 0.92))
  if (!blob) {
    notice.show('头像裁剪失败，请重新选择', 'error')
    return
  }
  avatarSaving.value = true
  try {
    const response = await uploadAvatar(new File([blob], 'avatar.png', { type: 'image/png' }))
    member.value = response.data.data
    avatarEditorOpen.value = false
    releaseCropImage()
    notice.show('头像已更新')
  } catch (error) {
    notice.show(error.response?.data?.msg || '头像上传失败，请稍后重试', 'error')
  } finally {
    avatarSaving.value = false
  }
}
</script>

<template>
  <section class="account-wrap">
    <div v-if="!member" class="page-intro">
      <span class="section-kicker">个人中心</span>
      <h1>{{ member ? `你好，${member.nickname}` : '欢迎回来' }}</h1>
      <p>{{ member ? `手机号 ${member.maskedPhone || member.phone}` : '使用手机号验证码安全登录，未注册号码验证后将创建商城账号' }}</p>
    </div>

    <p v-if="profileLoading" class="loading-note">正在加载个人中心...</p>

    <template v-else-if="member">
      <section class="account-hero-c">
        <div class="profile-panel">
          <div class="profile-avatar">
            <img v-if="member.avatar" :src="member.avatar" alt="" />
            <span v-else>{{ member.nickname?.slice(0, 1) }}</span>
          </div>
          <div><strong>{{ member.nickname }}</strong><p>普通会员 · 会员编号 M{{ member.memberId }}</p></div>
          <div class="profile-panel-actions">
            <button class="text-button" type="button" @click="toggleAvatarEditor"><VanIcon name="photograph" />更换头像</button>
            <button class="add-button" type="button" @click="logout">退出登录</button>
          </div>
        </div>
        <div class="account-overview">
          <router-link to="/orders"><strong>{{ orderCount }}</strong><span>订单</span></router-link>
          <router-link to="/cart"><strong>{{ cartCount }}</strong><span>购物车</span></router-link>
          <a href="#address-book"><strong>{{ addresses.length }}</strong><span>地址</span></a>
        </div>
      </section>

      <nav class="account-shortcuts-c" aria-label="个人中心快捷入口">
        <router-link to="/orders"><VanIcon name="orders-o" /><span><strong>我的订单</strong><small>查看交易和物流进度</small></span><VanIcon name="arrow" /></router-link>
        <a href="#address-book"><VanIcon name="location-o" /><span><strong>地址簿</strong><small>{{ addresses.length }} 个常用地址</small></span><VanIcon name="arrow" /></a>
        <a href="#account-security"><VanIcon name="shield-o" /><span><strong>安全中心</strong><small>管理登录设备</small></span><VanIcon name="arrow" /></a>
      </nav>

      <div class="account-sections">
        <section class="account-section">
          <div class="section-heading profile-heading">
            <router-link class="text-button" to="/account/privacy"><VanIcon name="shield-o" />账号与隐私</router-link>
            <div><span class="section-kicker">账户资料</span><h2>个人资料</h2></div>
            <button v-if="!profileEditing" class="text-button" type="button" @click="openNicknameEditor"><VanIcon name="edit" />修改昵称</button>
          </div>
          <form v-if="profileEditing" class="nickname-form" @submit.prevent="saveNickname">
            <label>
              <span>昵称</span>
              <input v-model="nicknameDraft" maxlength="40" autocomplete="nickname" />
            </label>
            <small>30 天内还可修改 {{ member.nicknameChangesRemaining ?? 0 }} 次</small>
            <div>
              <button class="primary-button" type="submit" :disabled="nicknameSaving">{{ nicknameSaving ? '保存中...' : '保存昵称' }}</button>
              <button class="text-button" type="button" :disabled="nicknameSaving" @click="closeNicknameEditor">取消</button>
            </div>
          </form>
          <dl class="profile-details">
            <div><dt>昵称</dt><dd>{{ member.nickname }}</dd></div>
            <div><dt>手机号</dt><dd>{{ member.maskedPhone }}</dd></div>
            <div><dt>会员编号</dt><dd>M{{ member.memberId }}</dd></div>
            <div><dt>最近登录</dt><dd>{{ member.lastLoginTime || '本次登录' }}</dd></div>
          </dl>
          <div v-if="avatarEditorOpen" class="avatar-editor">
            <div class="avatar-editor-heading">
              <strong>选择头像</strong>
              <button class="icon-button" type="button" aria-label="关闭头像选择" title="关闭" @click="avatarEditorOpen = false"><VanIcon name="cross" /></button>
            </div>
            <div class="avatar-preset-grid">
              <button v-for="preset in avatarPresets" :key="preset.code" type="button" :class="{ active: member.avatar === preset.url }" :disabled="avatarSaving" :aria-label="`选择${preset.name}头像`" @click="choosePreset(preset)">
                <img :src="preset.url" alt="" /><span>{{ preset.name }}</span>
              </button>
            </div>
            <input ref="avatarInput" class="visually-hidden" type="file" accept="image/jpeg,image/png" @change="handleAvatarFile" />
            <button class="avatar-upload-button" type="button" :disabled="avatarSaving" @click="selectAvatarFile"><VanIcon name="upgrade" />{{ avatarSaving ? '处理中...' : '上传头像' }}</button>
          </div>
        </section>

        <section id="address-book" class="account-section address-book-section">
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

        <section id="account-security" class="account-section session-section">
          <div class="section-heading session-heading">
            <div><span class="section-kicker">账户安全</span><h2>登录设备</h2></div>
            <span class="session-quota">30 天内还可更换主设备 {{ sessionOverview.primaryChangesRemaining }} 次</span>
          </div>
          <div v-if="sessionOverview.sessions.length" class="session-list">
            <article v-for="session in sessionOverview.sessions" :key="session.sessionId" class="session-item">
              <div class="session-icon" aria-hidden="true"><VanIcon :name="session.deviceType === 'MOBILE' ? 'phone-o' : 'desktop-o'" /></div>
              <div class="session-copy">
                <div class="session-name">
                  <strong>{{ session.deviceName }}</strong>
                  <span v-if="session.current">当前设备</span>
                  <em v-if="session.primaryMobile">主设备</em>
                </div>
                <p>{{ session.loginIp || '未知网络' }} · 登录于 {{ formatSessionTime(session.loginTime) }}</p>
                <small>有效期至 {{ formatSessionTime(session.expireTime) }}</small>
              </div>
              <div class="session-actions">
                <button v-if="session.current && session.deviceType === 'MOBILE' && !session.primaryMobile" class="text-button" type="button" @click="openPrimaryDeviceChange">设为主设备</button>
                <button v-if="!session.current" class="session-revoke" type="button" :disabled="Boolean(sessionBusyId)" @click="sessionRevokeConfirmId = session.sessionId"><VanIcon name="close" />下线</button>
              </div>
              <div v-if="sessionRevokeConfirmId === session.sessionId" class="session-confirm" role="alert">
                <span>确认让这台设备退出登录？</span>
                <button type="button" :disabled="sessionBusyId === session.sessionId" @click="revokeSession(session)">{{ sessionBusyId === session.sessionId ? '处理中...' : '确认下线' }}</button>
                <button type="button" :disabled="sessionBusyId === session.sessionId" @click="sessionRevokeConfirmId = null">取消</button>
              </div>
            </article>
          </div>
          <p v-else class="session-empty">暂无在线设备</p>
          <form v-if="primaryCodeOpen" class="primary-device-form" @submit.prevent="confirmPrimaryDevice">
            <div>
              <strong>验证当前手机号</strong>
              <button class="icon-button" type="button" aria-label="关闭主设备验证" title="关闭" @click="closePrimaryDeviceChange"><VanIcon name="cross" /></button>
            </div>
            <label><span>短信验证码</span><div class="primary-code-field"><input v-model.trim="primaryCode" inputmode="numeric" maxlength="6" autocomplete="one-time-code" placeholder="6 位验证码" /><button type="button" :disabled="primaryCodeSending || primarySeconds > 0" @click="requestPrimaryDeviceCode">{{ primarySeconds ? `${primarySeconds}s 后重发` : (primaryCodeSending ? '发送中...' : '获取验证码') }}</button></div></label>
            <button class="primary-button" type="submit" :disabled="primaryReplacing">{{ primaryReplacing ? '验证中...' : '确认设为主设备' }}</button>
          </form>
        </section>
      </div>
    </template>

    <div v-else class="login-panel">
      <label>手机号<input v-model.trim="phone" inputmode="numeric" maxlength="11" placeholder="请输入手机号" /></label>
      <label>短信验证码<div class="code-field"><input v-model.trim="code" inputmode="numeric" maxlength="6" placeholder="6 位验证码" /><button type="button" :disabled="loading || seconds > 0" @click="sendCode()">{{ seconds ? `${seconds}s 后重发` : '获取验证码' }}</button></div></label>
      <label class="agreement"><input v-model="agreed" type="checkbox" /> 我已阅读并同意《用户协议》和《隐私政策》</label>
      <button class="primary-button login-button" :disabled="loading" @click="login">{{ loading ? '处理中...' : '登录 / 注册' }}</button>
    </div>
    <div v-if="cropOpen" class="avatar-crop-dialog" role="dialog" aria-modal="true" aria-label="裁剪头像" @click.self="releaseCropImage">
      <div class="avatar-crop-panel">
        <div class="avatar-editor-heading">
          <h2>裁剪头像</h2>
          <button class="icon-button" type="button" aria-label="关闭裁剪" title="关闭" :disabled="avatarSaving" @click="releaseCropImage"><VanIcon name="cross" /></button>
        </div>
        <div class="avatar-crop-preview">
          <img :src="cropImageUrl" alt="" :style="{ transform: `scale(${cropZoom})`, objectPosition: `${cropX}% ${cropY}%` }" />
        </div>
        <div class="avatar-crop-controls">
          <label><span>缩放</span><input v-model.number="cropZoom" type="range" min="1" max="3" step="0.05" /></label>
          <label><span>水平</span><input v-model.number="cropX" type="range" min="0" max="100" step="1" /></label>
          <label><span>垂直</span><input v-model.number="cropY" type="range" min="0" max="100" step="1" /></label>
        </div>
        <div class="avatar-crop-actions">
          <button class="text-button" type="button" :disabled="avatarSaving" @click="releaseCropImage">取消</button>
          <button class="primary-button" type="button" :disabled="avatarSaving" @click="confirmAvatarCrop">{{ avatarSaving ? '上传中...' : '确认头像' }}</button>
        </div>
      </div>
    </div>
    <div v-if="smsChallengeOpen" class="sms-challenge-backdrop" role="presentation" @click.self="closeSmsChallenge">
      <section class="sms-challenge-modal" role="dialog" aria-modal="true" aria-labelledby="sms-challenge-title">
        <div class="sms-challenge-heading">
          <div><span class="section-kicker">安全验证</span><h2 id="sms-challenge-title">请完成拼图</h2></div>
          <button class="icon-button" type="button" aria-label="关闭安全验证" title="关闭" @click="closeSmsChallenge"><VanIcon name="cross" /></button>
        </div>
        <p class="sms-challenge-note">拖动下方拼图块到缺口位置，验证通过后继续发送短信。</p>
        <div class="sms-challenge-scene">
          <span class="sms-challenge-grid"></span><span class="sms-challenge-sun"></span><span class="sms-challenge-mountain"></span>
          <span class="sms-challenge-gap" :style="{ left: `${smsChallenge?.targetX || 70}%` }" aria-hidden="true"></span>
          <span class="sms-challenge-piece" :style="{ left: `${smsChallengePosition}%` }" aria-hidden="true"></span>
        </div>
        <div class="sms-challenge-track" :style="{ '--challenge-position': smsChallengePosition }">
          <input v-model.number="smsChallengePosition" type="range" min="0" max="100" step="1" aria-label="拼图位置" />
          <span>向右拖动完成拼图</span>
        </div>
        <p v-if="smsChallengeError" class="sms-challenge-error" role="alert">{{ smsChallengeError }}</p>
        <div class="sms-challenge-actions">
          <button class="text-button" type="button" :disabled="smsChallengeVerifying" @click="openSmsChallenge">换一张</button>
          <button class="primary-button" type="button" :disabled="smsChallengeLoading || smsChallengeVerifying" @click="verifySmsChallengeAndSend">{{ smsChallengeVerifying ? '验证中...' : '验证并发送' }}</button>
        </div>
      </section>
    </div>
    <p v-if="message" class="form-message">{{ message }}</p>
  </section>
</template>
