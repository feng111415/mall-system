<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCaptcha } from '../api/auth'
import { useSessionStore } from '../stores/session'

const router = useRouter()
const session = useSessionStore()
const loading = ref(false)
const captchaEnabled = ref(true)
const captchaImage = ref('')
const form = reactive({ username: '', password: '', code: '', uuid: '' })

async function refreshCaptcha() {
  try {
    const response = await getCaptcha()
    captchaEnabled.value = response.data.captchaEnabled !== false
    form.uuid = response.data.uuid || ''
    captchaImage.value = response.data.img ? `data:image/gif;base64,${response.data.img}` : ''
  } catch {
    captchaEnabled.value = false
  }
}

async function submit() {
  if (!form.username || !form.password || (captchaEnabled.value && !form.code)) {
    return ElMessage.warning('请填写完整登录信息')
  }
  loading.value = true
  try {
    await session.login(form)
    await router.replace('/dashboard')
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '登录失败，请检查账号和验证码')
    form.code = ''
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(refreshCaptcha)
</script>

<template>
  <main class="admin-login">
    <section class="login-form">
      <span class="eyebrow">日常商店 · 运营后台</span>
      <h1>管理员登录</h1>
      <p>使用若依后台账号登录，商城会员账号不能进入这里。</p>
      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名"><el-input v-model.trim="form.username" autocomplete="username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password autocomplete="current-password" @keyup.enter="submit" /></el-form-item>
        <el-form-item v-if="captchaEnabled" label="验证码">
          <div class="captcha-field"><el-input v-model.trim="form.code" maxlength="8" @keyup.enter="submit" /><button type="button" title="刷新验证码" @click="refreshCaptcha"><img :src="captchaImage" alt="验证码" /></button></div>
        </el-form-item>
        <el-button type="primary" :loading="loading" class="login-submit" @click="submit">登录</el-button>
      </el-form>
    </section>
  </main>
</template>
