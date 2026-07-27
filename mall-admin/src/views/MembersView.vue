<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMembers, updateMember } from '../api/mall'

const loading=ref(false), keyword=ref(''), members=ref([]), fallback=ref(false)
async function load(){loading.value=true;try{const r=await getMembers({phone:keyword.value,pageNum:1,pageSize:20});members.value=r.data.rows||[]}catch{fallback.value=true;members.value=[{memberId:10001,phone:'138****8000',nickname:'林女士',status:'0',registerSource:'H5',lastLoginTime:'2026-07-24 10:42:00'}]}finally{loading.value=false}}
async function toggle(row){const next=row.status==='0'?'1':'0';try{await updateMember({...row,status:next});row.status=next;ElMessage.success(next==='0'?'会员已启用':'会员已停用')}catch{ElMessage.warning('后端未登录，演示数据不执行修改')}}
onMounted(load)
</script>
<template><section><div class="page-title"><div><span class="eyebrow">会员身份</span><h1>会员管理</h1><p>商城会员与若依后台账号相互独立，手机号默认脱敏展示。</p></div></div><div class="panel table-panel"><div class="table-toolbar"><div class="tabs"><button class="active">会员列表</button></div><div class="table-filters"><input v-model="keyword" placeholder="搜索手机号" @keyup.enter="load"/><button class="outline-button" @click="load">查询</button></div></div><p v-if="fallback" class="inline-notice">后端未登录，当前展示演示数据</p><table v-loading="loading"><thead><tr><th>会员ID</th><th>昵称</th><th>手机号</th><th>来源</th><th>最后登录</th><th>状态</th><th>操作</th></tr></thead><tbody><tr v-for="row in members" :key="row.memberId"><td class="order-no">M{{ row.memberId }}</td><td>{{ row.nickname }}</td><td>{{ row.phone }}</td><td>{{ row.registerSource }}</td><td class="muted-text">{{ row.lastLoginTime || '-' }}</td><td><span class="status" :class="row.status==='0'?'success':'warning'">{{ row.status==='0'?'正常':'停用' }}</span></td><td><button class="link-button" @click="toggle(row)">{{ row.status==='0'?'停用':'启用' }}</button></td></tr></tbody></table></div></section></template>
