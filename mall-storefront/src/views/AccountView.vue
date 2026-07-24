<script setup>
import { ref } from 'vue'
const phone = ref(''); const code = ref(''); const agreed = ref(false); const sent = ref(false); const message = ref('')
function login() { if (!/^1\d{10}$/.test(phone.value)) return message.value = '请输入正确的手机号'; if (!/^\d{6}$/.test(code.value)) return message.value = '请输入 6 位验证码'; if (!agreed.value) return message.value = '请先同意隐私政策'; message.value = '登录成功（静态演示）' }
function sendCode() { if (/^1\d{10}$/.test(phone.value)) { sent.value = true; message.value = '验证码已发送（演示码：123456）' } else message.value = '请输入正确的手机号' }
</script>

<template><section class="account-wrap"><div class="page-intro"><span class="section-kicker">个人中心</span><h1>欢迎回来</h1><p>使用手机号验证码安全登录</p></div><div class="login-panel"><label>手机号<input v-model="phone" inputmode="numeric" maxlength="11" placeholder="请输入手机号" /></label><label>短信验证码<div class="code-field"><input v-model="code" inputmode="numeric" maxlength="6" placeholder="6 位验证码" /><button type="button" @click="sendCode">{{ sent ? '重新发送' : '获取验证码' }}</button></div></label><label class="agreement"><input v-model="agreed" type="checkbox" /> 我已阅读并同意《用户协议》和《隐私政策》</label><button class="primary-button login-button" @click="login">登录 / 注册</button><p v-if="message" class="form-message">{{ message }}</p></div></section></template>
