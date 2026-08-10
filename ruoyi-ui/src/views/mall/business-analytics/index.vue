<template>
  <div class="app-container analytics-page">
    <header class="page-heading">
      <div>
        <p class="eyebrow">MALL BUSINESS ANALYTICS · V0.5</p>
        <h1>经营分析中心</h1>
        <p>汇总销售、商品、会员复购、履约与售后资金结果，不展示会员或订单明细。</p>
      </div>
      <div class="heading-actions">
        <el-radio-group v-model="days" size="small" @change="loadData">
          <el-radio-button :label="7">近 7 天</el-radio-button>
          <el-radio-button :label="30">近 30 天</el-radio-button>
          <el-radio-button :label="90">近 90 天</el-radio-button>
        </el-radio-group>
        <el-button icon="el-icon-refresh" circle :loading="loading" title="刷新经营数据" @click="loadData" />
      </div>
    </header>

    <section class="scope-bar">
      <div><i class="el-icon-lock" /><strong>当前岗位数据范围</strong></div>
      <div class="scope-tags">
        <el-tag v-for="section in analytics.sections" :key="section" size="small" effect="plain" :type="sectionTone(section)">
          {{ sectionLabel(section) }}
        </el-tag>
      </div>
      <span>{{ rangeLabel }}</span>
    </section>

    <section v-loading="loading" class="analytics-content">
      <el-empty v-if="!loading && !analytics.sections.length" description="当前岗位没有经营分析数据权限" :image-size="80" />

      <div v-else>
        <div class="metric-grid">
          <article v-for="metric in analytics.metrics" :key="metric.key" class="metric-card" :class="metricTone(metric.key)">
            <div class="metric-title">
              <span>{{ metric.label }}</span>
              <i :class="metricIcon(metric.key)" />
            </div>
            <strong>{{ formatMetric(metric) }}</strong>
            <small>{{ metricHint(metric.key) }}</small>
          </article>
        </div>

        <div v-if="showSalesTrend || showProductRanking" class="primary-grid" :class="{ single: !(showSalesTrend && showProductRanking) }">
          <section v-if="showSalesTrend" class="panel trend-panel">
            <div class="panel-heading">
              <div><strong>销售与订单趋势</strong><span>按支付日期汇总，不含未支付订单</span></div>
              <el-tag size="mini" type="success" effect="plain">聚合数据</el-tag>
            </div>
            <div ref="salesChart" class="sales-chart" />
          </section>

          <section v-if="showProductRanking" class="panel ranking-panel">
            <div class="panel-heading">
              <div><strong>商品销售排行</strong><span>按实付订单商品金额排序</span></div>
              <el-tag size="mini" type="warning" effect="plain">TOP 8</el-tag>
            </div>
            <el-table :data="analytics.productRanking" size="mini" class="ranking-table">
              <el-table-column type="index" label="#" width="44" align="center" />
              <el-table-column prop="label" label="商品" min-width="150" show-overflow-tooltip />
              <el-table-column prop="quantity" label="销量" width="76" align="right" />
              <el-table-column label="销售额" width="110" align="right">
                <template slot-scope="scope">¥{{ money(scope.row.amount) }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-if="!analytics.productRanking.length" description="当前周期暂无成交商品" :image-size="56" />
          </section>
        </div>

        <section class="domain-panel">
          <div class="panel-heading">
            <div><strong>岗位经营摘要</strong><span>只显示当前职责内的聚合指标</span></div>
          </div>
          <div class="domain-grid">
            <article v-for="domain in visibleDomains" :key="domain.key" class="domain-item">
              <div class="domain-icon" :class="domain.key"><i :class="domain.icon" /></div>
              <div>
                <strong>{{ domain.label }}</strong>
                <p>{{ domain.description }}</p>
                <span>{{ domain.metricCount }} 项指标</span>
              </div>
            </article>
          </div>
        </section>
      </div>
    </section>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getBusinessAnalytics } from '@/api/mall/businessAnalytics'

const sectionMeta = {
  sales: { label: '销售汇总', tone: 'success', icon: 'el-icon-data-line', description: '订单量、销售额与成交趋势。' },
  product: { label: '商品分析', tone: 'warning', icon: 'el-icon-goods', description: '商品销售排行与低库存风险。' },
  member: { label: '会员聚合', tone: '', icon: 'el-icon-user', description: '成交会员、复购人数与复购率。' },
  fulfillment: { label: '库存与履约', tone: 'info', icon: 'el-icon-truck', description: '订单量、库存、发货和运输状态。' },
  afterSale: { label: '售后汇总', tone: 'danger', icon: 'el-icon-refresh-left', description: '期间售后与当前待处理数量。' },
  finance: { label: '售后资金', tone: 'danger', icon: 'el-icon-money', description: '销售、退款、净收入与资金异常。' }
}

const metricHints = {
  orderCount: '当前统计周期创建的订单',
  salesAmount: '当前周期已支付订单金额',
  lowStockSkuCount: '库存已达到预警阈值',
  paidMemberCount: '当前周期有成交的会员数',
  repeatMemberCount: '当前周期成交超过一次的会员',
  repeatRate: '复购会员占成交会员比例',
  pendingShipment: '已支付等待发货的订单',
  inTransit: '当前仍在运输中的物流单',
  delivered: '当前周期完成签收的物流单',
  availableStock: '全部在售 SKU 可售数量',
  afterSaleCount: '当前周期发起的售后单',
  afterSalePending: '仍需审核或退款处理',
  refundAmount: '当前周期成功退款金额',
  netRevenue: '销售额减去成功退款金额',
  financialAnomalies: '未关闭的差异、告警与补偿'
}

export default {
  name: 'MallBusinessAnalytics',
  data () {
    return {
      days: 30,
      loading: false,
      chart: null,
      analytics: { from: '', to: '', sections: [], metrics: [], salesTrend: [], productRanking: [] }
    }
  },
  computed: {
    rangeLabel () { return this.analytics.from && this.analytics.to ? `${this.analytics.from} 至 ${this.analytics.to}` : '正在读取统计周期' },
    showSalesTrend () { return this.analytics.sections.includes('sales') || this.analytics.sections.includes('finance') },
    showProductRanking () { return this.analytics.sections.includes('product') },
    visibleDomains () {
      return this.analytics.sections.map(key => ({
        key,
        ...sectionMeta[key],
        metricCount: this.analytics.metrics.filter(metric => this.metricDomain(metric.key) === key || (key === 'sales' && ['orderCount', 'salesAmount'].includes(metric.key))).length
      })).filter(item => item.label)
    }
  },
  created () { this.loadData() },
  mounted () { window.addEventListener('resize', this.resizeChart) },
  beforeDestroy () {
    window.removeEventListener('resize', this.resizeChart)
    if (this.chart) this.chart.dispose()
  },
  methods: {
    loadData () {
      this.loading = true
      getBusinessAnalytics(this.days).then(response => {
        this.analytics = Object.assign({ from: '', to: '', sections: [], metrics: [], salesTrend: [], productRanking: [] }, response.data || {})
        this.$nextTick(this.renderChart)
      }).finally(() => { this.loading = false })
    },
    renderChart () {
      if (!this.showSalesTrend || !this.$refs.salesChart) return
      if (!this.chart) this.chart = echarts.init(this.$refs.salesChart)
      const points = this.analytics.salesTrend || []
      this.chart.setOption({
        animationDuration: 300,
        color: ['#2f8f64', '#4b78a8'],
        tooltip: { trigger: 'axis', formatter: params => `${params[0].axisValue}<br/>销售额：¥${this.money(params[0].value)}<br/>订单量：${params[1].value} 单` },
        legend: { top: 4, right: 8, itemWidth: 14, textStyle: { color: '#647184', fontSize: 11 }, data: ['销售额', '订单量'] },
        grid: { left: 52, right: 46, top: 42, bottom: 34 },
        xAxis: { type: 'category', boundaryGap: false, data: points.map(item => item.date.slice(5)), axisLine: { lineStyle: { color: '#dce2e9' } }, axisLabel: { color: '#7b8797', fontSize: 10 } },
        yAxis: [
          { type: 'value', axisLabel: { color: '#7b8797', fontSize: 10, formatter: value => `¥${value}` }, splitLine: { lineStyle: { color: '#eef1f5' } } },
          { type: 'value', axisLabel: { color: '#7b8797', fontSize: 10 }, splitLine: { show: false } }
        ],
        series: [
          { name: '销售额', type: 'line', smooth: true, symbolSize: 6, data: points.map(item => Number(item.amount || 0)) },
          { name: '订单量', type: 'bar', yAxisIndex: 1, barMaxWidth: 18, data: points.map(item => Number(item.orderCount || 0)) }
        ]
      }, true)
    },
    resizeChart () { if (this.chart) this.chart.resize() },
    formatMetric (metric) {
      if (metric.amount !== null && metric.amount !== undefined) return `¥${this.money(metric.amount)}`
      const value = metric.value !== null && metric.value !== undefined ? metric.value : Number(metric.count || 0).toLocaleString('zh-CN')
      return `${value}${metric.unit || ''}`
    },
    money (value) { return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) },
    sectionLabel (section) { return sectionMeta[section] ? sectionMeta[section].label : section },
    sectionTone (section) { return sectionMeta[section] ? sectionMeta[section].tone : 'info' },
    metricHint (key) { return metricHints[key] || '当前统计周期聚合结果' },
    metricIcon (key) {
      if (key.includes('Amount') || key === 'netRevenue') return 'el-icon-money'
      if (key.includes('Stock') || key.includes('Sku')) return 'el-icon-box'
      if (key.includes('Member') || key === 'repeatRate') return 'el-icon-user'
      if (key.includes('Shipment') || key === 'inTransit' || key === 'delivered') return 'el-icon-truck'
      if (key.includes('afterSale')) return 'el-icon-refresh-left'
      if (key.includes('Anomal')) return 'el-icon-warning-outline'
      return 'el-icon-s-data'
    },
    metricTone (key) {
      if (key === 'financialAnomalies' || key === 'afterSalePending') return 'danger'
      if (key === 'lowStockSkuCount' || key === 'pendingShipment') return 'warning'
      if (key === 'netRevenue' || key === 'salesAmount') return 'positive'
      return ''
    },
    metricDomain (key) {
      if (['paidMemberCount', 'repeatMemberCount', 'repeatRate'].includes(key)) return 'member'
      if (['pendingShipment', 'inTransit', 'delivered', 'availableStock'].includes(key)) return 'fulfillment'
      if (['lowStockSkuCount'].includes(key)) return 'product'
      if (['afterSaleCount', 'afterSalePending'].includes(key)) return 'afterSale'
      if (['refundAmount', 'netRevenue', 'financialAnomalies'].includes(key)) return 'finance'
      return 'sales'
    }
  }
}
</script>

<style scoped>
.analytics-page { min-height: calc(100vh - 84px); background: #f5f7fa; color: #2f3a4a; }
.page-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 22px; padding: 20px 22px; border: 1px solid #dfe5ed; border-left: 4px solid #2f8f64; border-radius: 6px; background: #fff; }
.page-heading h1 { margin: 4px 0 6px; color: #223047; font-size: 24px; line-height: 1.3; }
.page-heading p { margin: 0; color: #748094; font-size: 12px; line-height: 1.6; }
.page-heading .eyebrow { color: #2f8f64; font-size: 10px; font-weight: 700; letter-spacing: 0; }
.heading-actions { display: flex; align-items: center; gap: 10px; flex: none; }
.scope-bar { display: grid; grid-template-columns: auto minmax(0, 1fr) auto; align-items: center; gap: 15px; margin: 12px 0; padding: 10px 14px; border: 1px solid #e0e5ec; border-radius: 5px; background: #fff; }
.scope-bar > div:first-child { color: #45546a; font-size: 12px; white-space: nowrap; }
.scope-bar i { margin-right: 7px; color: #4b78a8; }
.scope-tags { display: flex; flex-wrap: wrap; gap: 6px; }
.scope-bar > span { color: #8994a3; font-size: 10px; white-space: nowrap; }
.analytics-content { min-height: 320px; }
.metric-grid { display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 10px; }
.metric-card { min-width: 0; min-height: 116px; padding: 15px 16px; border: 1px solid #e1e6ed; border-top: 3px solid #8190a3; border-radius: 5px; background: #fff; }
.metric-card.positive { border-top-color: #2f8f64; }
.metric-card.warning { border-top-color: #d28b21; }
.metric-card.danger { border-top-color: #cf5652; }
.metric-title { display: flex; align-items: center; justify-content: space-between; gap: 8px; color: #6d7a8d; font-size: 11px; }
.metric-title i { color: #9ca7b5; font-size: 15px; }
.metric-card strong { display: block; margin: 12px 0 8px; overflow: hidden; color: #26364e; font-size: 21px; line-height: 1.1; text-overflow: ellipsis; white-space: nowrap; }
.metric-card small { display: block; overflow: hidden; color: #98a1ae; font-size: 10px; text-overflow: ellipsis; white-space: nowrap; }
.primary-grid { display: grid; grid-template-columns: minmax(0, 1.55fr) minmax(340px, .85fr); gap: 12px; margin-top: 12px; }
.primary-grid.single { grid-template-columns: minmax(0, 1fr); }
.panel, .domain-panel { border: 1px solid #dfe5ed; border-radius: 5px; background: #fff; }
.panel-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 14px 16px; border-bottom: 1px solid #e8ecf1; }
.panel-heading strong, .panel-heading span { display: block; }
.panel-heading strong { color: #334158; font-size: 14px; }
.panel-heading span { margin-top: 4px; color: #929cab; font-size: 10px; }
.sales-chart { width: 100%; height: 316px; }
.ranking-table { width: 100%; }
.ranking-panel ::v-deep .el-table::before { display: none; }
.ranking-panel ::v-deep .el-table th { background: #fafbfc; color: #738094; font-size: 10px; font-weight: 500; }
.ranking-panel ::v-deep .el-table td { color: #455166; font-size: 11px; }
.domain-panel { margin-top: 12px; }
.domain-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); }
.domain-item { display: grid; grid-template-columns: 38px minmax(0, 1fr); gap: 12px; min-height: 102px; padding: 16px; border-right: 1px solid #e8ecf1; border-bottom: 1px solid #e8ecf1; }
.domain-item:nth-child(3n) { border-right: 0; }
.domain-icon { display: flex; align-items: center; justify-content: center; width: 36px; height: 36px; border-radius: 5px; background: #edf4fb; color: #4b78a8; font-size: 17px; }
.domain-icon.sales { background: #eaf5ef; color: #2f8f64; }
.domain-icon.product { background: #fff4df; color: #b77818; }
.domain-icon.afterSale, .domain-icon.finance { background: #fbeceb; color: #c94e49; }
.domain-item strong { color: #334158; font-size: 12px; }
.domain-item p { margin: 5px 0 8px; color: #7d8898; font-size: 10px; line-height: 1.5; }
.domain-item span { color: #9aa3af; font-size: 9px; }
@media (max-width: 1280px) {
  .metric-grid { grid-template-columns: repeat(4, minmax(0, 1fr)); }
}
@media (max-width: 900px) {
  .page-heading { align-items: stretch; flex-direction: column; }
  .heading-actions { justify-content: space-between; }
  .scope-bar { grid-template-columns: 1fr; gap: 9px; }
  .metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .primary-grid { grid-template-columns: 1fr; }
  .domain-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .domain-item:nth-child(3n) { border-right: 1px solid #e8ecf1; }
  .domain-item:nth-child(2n) { border-right: 0; }
}
@media (max-width: 560px) {
  .analytics-page { padding: 12px 10px; }
  .page-heading { padding: 16px 14px; }
  .page-heading h1 { font-size: 21px; }
  .heading-actions { align-items: stretch; flex-direction: column; }
  .heading-actions .el-radio-group { display: flex; }
  .heading-actions ::v-deep .el-radio-button { flex: 1; }
  .heading-actions ::v-deep .el-radio-button__inner { width: 100%; padding-right: 7px; padding-left: 7px; }
  .metric-grid { grid-template-columns: 1fr 1fr; gap: 8px; }
  .metric-card { min-height: 106px; padding: 13px; }
  .metric-card strong { font-size: 18px; }
  .sales-chart { height: 270px; }
  .domain-grid { grid-template-columns: 1fr; }
  .domain-item, .domain-item:nth-child(2n), .domain-item:nth-child(3n) { border-right: 0; }
}
</style>
