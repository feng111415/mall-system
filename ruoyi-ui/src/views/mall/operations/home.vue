<template>
  <div class="mall-operations-home">
    <header class="home-heading">
      <div>
        <p class="eyebrow">MALL OPERATIONS · V0.4</p>
        <h1>{{ isAdmin ? '商城运营岗位总览' : activeRole.name + '工作台' }}</h1>
        <p>{{ activeRole.objective }}</p>
      </div>
      <div class="identity-block">
        <span>当前登录账号</span>
        <strong>{{ nickName || userName }}</strong>
        <small>{{ isAdmin ? '超级管理员 · 可查看全部岗位' : activeRole.name + ' · ' + activeRole.code }}</small>
      </div>
    </header>

    <el-radio-group v-if="availableRoles.length > 1" v-model="activeRoleKey" size="small" class="role-switch" aria-label="岗位视图">
      <el-radio-button v-for="role in availableRoles" :key="role.key" :label="role.key">{{ role.name }}</el-radio-button>
    </el-radio-group>

    <section class="role-summary">
      <div class="summary-section duties">
        <h2><i class="el-icon-s-flag" /> 岗位职责</h2>
        <ul><li v-for="item in activeRole.responsibilities" :key="item">{{ item }}</li></ul>
      </div>
      <div class="summary-section change">
        <h2><i class="el-icon-refresh" /> 最新权限同步</h2>
        <el-tag size="mini" type="primary" effect="plain">V0.4 模块 4</el-tag>
        <p>{{ activeRole.latestChange }}</p>
      </div>
      <div class="summary-section boundary">
        <h2><i class="el-icon-lock" /> 职责边界</h2>
        <p>{{ activeRole.forbidden }}</p>
        <small>实际按钮与接口仍以若依 RBAC 服务端授权为准。</small>
      </div>
    </section>

    <section class="dashboard-section" v-loading="dashboardLoading" aria-label="今日运营概览">
      <div class="section-heading dashboard-heading">
        <div><h2>今日运营概览</h2><span>{{ dashboard.asOfDate ? `数据日期：${dashboard.asOfDate}` : '正在读取真实业务数据' }}</span></div>
        <el-button type="text" icon="el-icon-refresh" :loading="dashboardLoading" @click="loadDashboard">刷新数据</el-button>
      </div>
      <div v-if="dashboard.metrics && dashboard.metrics.length" class="metric-grid">
        <button v-for="metric in dashboard.metrics" :key="metric.key" type="button" class="metric-card" :class="'metric-' + metric.severity" @click="goFeature(metric.path)">
          <span class="metric-label">{{ metric.label }}</span>
          <strong>{{ formatMetric(metric) }}</strong>
          <small>{{ metric.hint }}</small>
          <i class="el-icon-arrow-right" aria-hidden="true" />
        </button>
      </div>
      <el-empty v-else-if="!dashboardLoading" description="当前岗位暂无可查看的运营指标" :image-size="72" />
      <p v-if="dashboardError" class="dashboard-error"><i class="el-icon-warning-outline" /> {{ dashboardError }}</p>
    </section>

    <section class="function-section">
      <div class="section-heading">
        <div><h2>我的功能</h2><span>{{ activeRole.features.length }} 个已授权业务入口</span></div>
        <el-button type="text" icon="el-icon-s-grid" @click="goResponsibilities">查看完整权限矩阵</el-button>
      </div>
      <div class="function-list">
        <article v-for="feature in activeRole.features" :key="feature.name" class="function-item">
          <div class="function-icon"><i :class="featureIcon(feature.name)" /></div>
          <div class="function-copy">
            <div><strong>{{ feature.name }}</strong><el-tag size="mini" :type="feature.access === '可操作' ? 'success' : 'warning'" effect="plain">{{ feature.access }}</el-tag></div>
            <p>{{ feature.description }}</p>
          </div>
          <el-button circle icon="el-icon-arrow-right" :title="'进入' + feature.name" @click="goFeature(feature.path)" />
        </article>
      </div>
    </section>
  </div>
</template>

<script>
import { roleDefinitions } from './roles'
import { getOperationsDashboard } from '@/api/mall/operations'

export default {
  name: 'MallOperationsHome',
  data () {
    return {
      activeRoleKey: 'mall_ops_lead',
      dashboardLoading: false,
      dashboardError: '',
      dashboard: { asOfDate: '', metrics: [] }
    }
  },
  created () { this.loadDashboard() },
  computed: {
    currentRoles () { return this.$store.getters.roles || [] },
    isAdmin () { return this.currentRoles.includes('admin') },
    availableRoles () {
      if (this.isAdmin) return roleDefinitions
      return roleDefinitions.filter(role => this.currentRoles.includes(role.key))
    },
    activeRole () { return this.availableRoles.find(role => role.key === this.activeRoleKey) || this.availableRoles[0] || roleDefinitions[0] },
    nickName () { return this.$store.getters.nickName },
    userName () { return this.$store.getters.name }
  },
  watch: {
    availableRoles: {
      immediate: true,
      handler (roles) {
        if (roles.length && !roles.some(role => role.key === this.activeRoleKey)) this.activeRoleKey = roles[0].key
      }
    }
  },
  methods: {
    loadDashboard () {
      this.dashboardLoading = true
      this.dashboardError = ''
      getOperationsDashboard().then(response => {
        this.dashboard = response.data || { asOfDate: '', metrics: [] }
      }).catch(() => {
        this.dashboard = { asOfDate: '', metrics: [] }
        this.dashboardError = '运营指标暂时无法加载，请刷新重试'
      }).finally(() => { this.dashboardLoading = false })
    },
    formatMetric (metric) {
      if (metric.valueType === 'AMOUNT') return `¥${Number(metric.amount || 0).toFixed(2)}`
      return String(metric.count || 0)
    },
    goFeature (path) { this.$router.push(path) },
    goResponsibilities () { this.$router.push('/mall/operations-roles') },
    featureIcon (name) {
      if (name.includes('物流')) return 'el-icon-truck'
      if (name.includes('订单')) return 'el-icon-s-order'
      if (name.includes('库存') || name.includes('商品')) return 'el-icon-goods'
      if (name.includes('会员')) return 'el-icon-user'
      if (name.includes('售后') || name.includes('对账')) return 'el-icon-money'
      if (name.includes('分类')) return 'el-icon-menu'
      if (name.includes('品牌')) return 'el-icon-medal'
      return 'el-icon-s-operation'
    }
  }
}
</script>

<style scoped>
.mall-operations-home { min-height: calc(100vh - 124px); color: #303744; }
.home-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 22px; padding: 21px 23px; border: 1px solid #dfe6ee; border-left: 4px solid #409eff; border-radius: 6px; background: #fff; }
.home-heading h1 { margin: 4px 0 7px; color: #202c3e; font-size: 24px; line-height: 1.3; }
.home-heading p { margin: 0; color: #6f7c90; font-size: 13px; line-height: 1.6; }
.home-heading .eyebrow { color: #2d78b6; font-size: 10px; font-weight: 700; letter-spacing: .12em; }
.identity-block { min-width: 270px; padding: 13px 16px; border-radius: 5px; background: #17243a; color: #fff; }
.identity-block span, .identity-block strong, .identity-block small { display: block; }
.identity-block span { color: #91a3bc; font-size: 11px; }
.identity-block strong { margin: 5px 0; font-size: 16px; }
.identity-block small { color: #bac6d6; font-size: 10px; }
.role-switch { display: flex; flex-wrap: wrap; margin: 14px 0; }
.role-summary { display: grid; grid-template-columns: 1.15fr 1fr 1fr; margin: 14px 0; overflow: hidden; border: 1px solid #dfe5ed; border-radius: 5px; background: #fff; }
.summary-section { min-height: 174px; padding: 17px 19px; border-right: 1px solid #e7ebf0; }
.summary-section:last-child { border-right: 0; }
.summary-section h2 { margin: 0 0 13px; color: #344157; font-size: 14px; }
.summary-section h2 i { margin-right: 6px; color: #4483bb; }
.summary-section ul { margin: 0; padding-left: 18px; color: #5f6c80; font-size: 12px; line-height: 2; }
.summary-section p { margin: 11px 0 0; color: #5f6c80; font-size: 12px; line-height: 1.75; }
.summary-section small { display: block; margin-top: 12px; color: #939dac; font-size: 10px; line-height: 1.5; }
.change { background: #f7fbff; }
.boundary { background: #fcfaf7; }
.dashboard-section { margin: 14px 0; border: 1px solid #dfe5ed; border-radius: 5px; background: #fff; }
.dashboard-heading { border-bottom: 1px solid #e7ebf0; }
.metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); }
.metric-card { position: relative; min-height: 112px; padding: 16px 40px 14px 17px; border: 0; border-right: 1px solid #e9edf2; border-bottom: 1px solid #e9edf2; background: #fff; color: #303744; text-align: left; transition: background .15s ease; }
.metric-card:nth-child(4n) { border-right: 0; }
.metric-card:hover { background: #f7fbff; }
.metric-card > span, .metric-card > strong, .metric-card > small { display: block; }
.metric-label { color: #66748a; font-size: 12px; }
.metric-card strong { margin-top: 9px; color: #26364e; font-size: 22px; line-height: 1.1; }
.metric-card small { margin-top: 8px; overflow: hidden; color: #929cab; font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }
.metric-card i { position: absolute; top: 17px; right: 17px; color: #a8b2c0; font-size: 13px; }
.metric-warning strong { color: #b77818; }
.metric-danger strong { color: #c94e49; }
.dashboard-error { margin: 0; padding: 10px 17px; border-top: 1px solid #f1d4d1; color: #c94e49; font-size: 12px; }
.function-section { border: 1px solid #dfe5ed; border-radius: 5px; background: #fff; }
.section-heading { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 15px 18px; border-bottom: 1px solid #e7ebf0; }
.section-heading h2, .section-heading span { display: block; }
.section-heading h2 { margin: 0; color: #2f3d52; font-size: 15px; }
.section-heading span { margin-top: 4px; color: #929cab; font-size: 10px; }
.function-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); }
.function-item { display: grid; grid-template-columns: 38px minmax(0, 1fr) 34px; align-items: center; gap: 12px; min-height: 92px; padding: 13px 17px; border-right: 1px solid #e9edf2; border-bottom: 1px solid #e9edf2; }
.function-item:nth-child(even) { border-right: 0; }
.function-icon { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; border-radius: 5px; background: #edf5fd; color: #357cb8; font-size: 17px; }
.function-copy { min-width: 0; }
.function-copy > div { display: flex; align-items: center; gap: 8px; }
.function-copy strong { color: #334158; font-size: 13px; }
.function-copy p { margin: 6px 0 0; color: #7b8697; font-size: 11px; line-height: 1.5; }
@media (max-width: 900px) {
  .home-heading { align-items: stretch; flex-direction: column; }
  .identity-block { min-width: 0; }
  .role-summary { grid-template-columns: 1fr; }
  .summary-section { min-height: 0; border-right: 0; border-bottom: 1px solid #e7ebf0; }
  .summary-section:last-child { border-bottom: 0; }
  .function-list { grid-template-columns: 1fr; }
  .function-item { border-right: 0; }
  .section-heading { align-items: flex-start; flex-direction: column; }
  .metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .metric-card:nth-child(4n) { border-right: 1px solid #e9edf2; }
  .metric-card:nth-child(2n) { border-right: 0; }
}
@media (max-width: 560px) {
  .metric-grid { grid-template-columns: 1fr; }
  .metric-card, .metric-card:nth-child(2n), .metric-card:nth-child(4n) { border-right: 0; }
}
</style>
