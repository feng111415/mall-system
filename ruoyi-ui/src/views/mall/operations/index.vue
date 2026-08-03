<template>
  <div class="app-container operations-page">
    <section class="page-heading">
      <div>
        <p class="eyebrow">MALL OPERATIONS · V0.4</p>
        <h1>商城运营职责与权限</h1>
        <p>按业务域组织稳定入口，岗位只获得完成职责所需的菜单和按钮权限。</p>
      </div>
      <div class="identity-card">
        <span>当前登录身份</span>
        <strong>{{ currentIdentity }}</strong>
        <small>菜单由若依 RBAC 按服务端授权生成</small>
      </div>
    </section>

    <el-row :gutter="16" class="role-grid">
      <el-col v-for="role in roleDefinitions" :key="role.key" :xs="24" :sm="12" :lg="8" class="role-column">
        <el-card shadow="never" :class="['role-card', { current: isCurrent(role.key) }]">
          <div slot="header" class="role-card__header">
            <div>
              <span>{{ role.code }}</span>
              <strong>{{ role.name }}</strong>
            </div>
            <el-tag v-if="isCurrent(role.key)" size="mini" type="success">当前岗位</el-tag>
          </div>
          <p class="role-objective">{{ role.objective }}</p>
          <dl>
            <div><dt>负责</dt><dd>{{ role.writes }}</dd></div>
            <div><dt>边界</dt><dd>{{ role.forbidden }}</dd></div>
          </dl>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="matrix-card">
      <div slot="header" class="matrix-heading">
        <div><strong>五岗位权限矩阵</strong><span>以正式菜单和按钮权限为准</span></div>
        <el-tag type="info" effect="plain">消费者等级不在 V0.4 范围</el-tag>
      </div>
      <el-table :data="permissionRows" border class="permission-table">
        <el-table-column prop="capability" label="业务能力" min-width="190" fixed="left" />
        <el-table-column v-for="role in roleDefinitions" :key="role.key" :label="role.name" min-width="118" align="center">
          <template slot-scope="scope">
            <span :class="['permission-level', `is-${scope.row[role.key].type}`]">
              {{ scope.row[role.key].label }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <div class="matrix-note">
        <i class="el-icon-lock" />
        运营主管拥有商城全域权限，但不包含用户、角色、菜单等若依系统级权限；客服审核与财务退款已拆分为独立权限。
      </div>
    </el-card>
  </div>
</template>

<script>
const full = { type: 'full', label: '可操作' }
const read = { type: 'read', label: '只读' }
const none = { type: 'none', label: '—' }

export default {
  name: 'MallOperationsRoles',
  data () {
    return {
      roleDefinitions: [
        { key: 'mall_ops_lead', code: 'ROLE 01', name: '运营主管', objective: '协调商城全链路，处理跨岗位异常并监督关键写操作。', writes: '商品、库存、履约、售后、资金', forbidden: '不配置若依系统级权限' },
        { key: 'mall_product_ops', code: 'ROLE 02', name: '商品运营', objective: '维护可售商品信息与上架质量，关注库存但不直接履约。', writes: '分类、品牌、商品、上下架', forbidden: '不发货、不退款；库存只读' },
        { key: 'mall_customer_service', code: 'ROLE 03', name: '客服售后', objective: '解决会员订单问题并完成售后审核，不接触资金执行。', writes: '会员资料、售后审核', forbidden: '不退款、不调库存、不发货' },
        { key: 'mall_fulfillment', code: 'ROLE 04', name: '仓储履约', objective: '保证库存准确，并按订单完成发货和物流轨迹维护。', writes: '库存调整、发货、物流节点', forbidden: '不改会员、不审售后' },
        { key: 'mall_finance_risk', code: 'ROLE 05', name: '财务风控', objective: '执行已审核退款并处理资金差异和告警。', writes: '售后退款、对账处理、告警确认', forbidden: '不审售后、不改商品、不发货' }
      ],
      permissionRows: [
        { capability: '会员查询与资料维护', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: full, mall_fulfillment: none, mall_finance_risk: none },
        { capability: '分类、品牌与商品维护', mall_ops_lead: full, mall_product_ops: full, mall_customer_service: none, mall_fulfillment: none, mall_finance_risk: none },
        { capability: '库存查询与调整', mall_ops_lead: full, mall_product_ops: read, mall_customer_service: none, mall_fulfillment: full, mall_finance_risk: none },
        { capability: '发货与物流轨迹', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: read, mall_fulfillment: full, mall_finance_risk: none },
        { capability: '售后查询与审核', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: full, mall_fulfillment: none, mall_finance_risk: read },
        { capability: '售后退款执行', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: none, mall_fulfillment: none, mall_finance_risk: full },
        { capability: '对账、差异与告警处理', mall_ops_lead: full, mall_product_ops: none, mall_customer_service: none, mall_fulfillment: none, mall_finance_risk: full }
      ]
    }
  },
  computed: {
    currentRoles () { return this.$store.getters.roles || [] },
    currentIdentity () {
      if (this.currentRoles.includes('admin')) return '超级管理员 · 全部权限'
      const names = this.roleDefinitions.filter(role => this.currentRoles.includes(role.key)).map(role => role.name)
      return names.length ? names.join(' / ') : '其他已授权角色'
    }
  },
  methods: {
    isCurrent (roleKey) { return this.currentRoles.includes(roleKey) }
  }
}
</script>

<style scoped>
.operations-page { color: #303744; background: #f5f7fa; min-height: calc(100vh - 84px); }
.page-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; margin-bottom: 18px; padding: 22px 24px; border: 1px solid #e3e8ef; border-left: 4px solid #409eff; border-radius: 6px; background: #fff; }
.page-heading h1 { margin: 4px 0 8px; font-size: 25px; color: #202b3c; }
.page-heading p { margin: 0; color: #748094; font-size: 13px; }
.page-heading .eyebrow { color: #2979c7; font-size: 10px; font-weight: 700; letter-spacing: .14em; }
.identity-card { min-width: 270px; padding: 13px 16px; border-radius: 5px; background: #17243a; color: #fff; }
.identity-card span, .identity-card strong, .identity-card small { display: block; }
.identity-card span { color: #8fa2bd; font-size: 11px; }
.identity-card strong { margin: 6px 0; font-size: 16px; }
.identity-card small { color: #b8c4d5; font-size: 10px; }
.role-column { margin-bottom: 16px; }
.role-card { min-height: 220px; border-color: #e1e6ed; }
.role-card.current { border-color: #67c23a; box-shadow: inset 0 3px #67c23a; }
.role-card__header { display: flex; align-items: center; justify-content: space-between; }
.role-card__header span, .role-card__header strong { display: block; }
.role-card__header span { margin-bottom: 4px; color: #8a96a8; font-size: 9px; letter-spacing: .12em; }
.role-card__header strong { color: #253247; font-size: 16px; }
.role-objective { min-height: 42px; margin: 0 0 14px; color: #667286; font-size: 12px; line-height: 1.7; }
.role-card dl { margin: 0; }
.role-card dl div { display: grid; grid-template-columns: 46px 1fr; gap: 8px; padding: 8px 0; border-top: 1px solid #eef1f5; font-size: 11px; line-height: 1.5; }
.role-card dt { color: #99a3b2; }
.role-card dd { margin: 0; color: #465266; }
.matrix-card { border-color: #e1e6ed; }
.matrix-heading { display: flex; align-items: center; justify-content: space-between; }
.matrix-heading strong, .matrix-heading span { display: block; }
.matrix-heading strong { color: #253247; font-size: 15px; }
.matrix-heading span { margin-top: 5px; color: #929cac; font-size: 10px; }
.permission-table { width: 100%; }
.permission-level { font-size: 11px; font-weight: 700; }
.permission-level.is-full { color: #2f9b52; }
.permission-level.is-read { color: #d68b19; }
.permission-level.is-none { color: #c1c7d0; }
.matrix-note { margin-top: 14px; padding: 10px 12px; border-radius: 4px; background: #f0f7ff; color: #52708f; font-size: 11px; line-height: 1.6; }
.matrix-note i { margin-right: 6px; color: #409eff; }
@media (max-width: 900px) {
  .page-heading { align-items: stretch; flex-direction: column; }
  .identity-card { min-width: 0; }
}
</style>
