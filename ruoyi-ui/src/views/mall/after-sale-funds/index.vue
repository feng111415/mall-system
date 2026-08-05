<template>
  <div class="app-container funds-center">
    <header class="page-heading">
      <div>
        <p class="eyebrow">MALL OPERATIONS · V0.4</p>
        <h1>售后资金处理中心</h1>
      </div>
      <div class="heading-actions">
        <el-button icon="el-icon-refresh" size="small" @click="load">刷新</el-button>
        <el-button v-hasPermi="['mall:compensation:run']" type="primary" icon="el-icon-video-play" size="small" @click="runCompensations">执行到期补偿</el-button>
      </div>
    </header>

    <section class="summary-band" aria-label="售后资金工作项统计">
      <button v-for="item in summaryItems" :key="item.key" type="button" :class="['summary-item', item.tone]" @click="selectSummary(item)">
        <span>{{ item.label }}</span><strong>{{ item.value }}</strong><small>{{ item.unit }}</small>
      </button>
    </section>

    <section class="workbench">
      <div class="queue-toolbar">
        <el-radio-group v-model="query.tab" size="small" @change="changeTab">
          <el-radio-button v-for="item in visibleTabs" :key="item.value" :label="item.value">{{ item.label }}</el-radio-button>
        </el-radio-group>
        <span>{{ total }} 个工作项</span>
      </div>

      <el-form ref="queryForm" :model="query" :inline="true" size="small" class="query-form">
        <el-form-item label="业务单号" prop="businessNo">
          <el-input v-model="query.businessNo" clearable placeholder="售后、差异、告警或任务编号" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="订单号" prop="orderNo">
          <el-input v-model="query.orderNo" clearable placeholder="订单号" @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="query.status" clearable :disabled="!statusOptions.length" placeholder="全部状态">
            <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="更新时间">
          <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="yyyy-MM-dd HH:mm:ss" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="rows" border class="work-table" @row-click="openDetail">
        <el-table-column label="工作项" min-width="210" fixed="left">
          <template slot-scope="scope">
            <el-tag size="mini" :type="kindTagType(scope.row.kind)">{{ kindLabel(scope.row) }}</el-tag>
            <strong class="business-no">{{ scope.row.businessNo }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="orderNo" label="订单号" min-width="180"><template slot-scope="scope">{{ scope.row.orderNo || '-' }}</template></el-table-column>
        <el-table-column label="金额" width="110" align="right"><template slot-scope="scope">{{ scope.row.amount == null ? '-' : '¥' + money(scope.row.amount) }}</template></el-table-column>
        <el-table-column label="状态" width="120"><template slot-scope="scope"><el-tag size="mini" :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="优先级" width="100"><template slot-scope="scope"><span :class="['priority', priorityClass(scope.row.priority)]">{{ priorityLabel(scope.row.priority) }}</span></template></el-table-column>
        <el-table-column label="信号" min-width="160"><template slot-scope="scope">{{ signalLabel(scope.row) }}</template></el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="165" />
        <el-table-column label="操作" width="110" fixed="right" align="center">
          <template slot-scope="scope"><el-button type="text" icon="el-icon-view" @click.stop="openDetail(scope.row)">详情</el-button></template>
        </el-table-column>
      </el-table>
      <pagination v-show="total > 0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="load" />
    </section>

    <el-drawer :title="current ? kindLabel(current) : '工作项详情'" :visible.sync="detailVisible" :size="isMobile ? '100%' : '440px'" append-to-body>
      <div v-if="current" class="detail-drawer">
        <div class="detail-top">
          <el-tag :type="kindTagType(current.kind)">{{ kindLabel(current) }}</el-tag>
          <span :class="['priority', priorityClass(current.priority)]">{{ priorityLabel(current.priority) }}</span>
        </div>
        <dl>
          <div><dt>业务单号</dt><dd>{{ current.businessNo }}</dd></div>
          <div><dt>订单号</dt><dd>{{ current.orderNo || '-' }}</dd></div>
          <div><dt>金额</dt><dd>{{ current.amount == null ? '-' : '¥' + money(current.amount) }}</dd></div>
          <div><dt>状态</dt><dd>{{ statusLabel(current.status) }}</dd></div>
          <div><dt>风险/类型</dt><dd>{{ signalLabel(current) }}</dd></div>
          <div><dt>更新时间</dt><dd>{{ current.updatedAt || '-' }}</dd></div>
        </dl>
        <div class="drawer-actions">
          <el-button v-if="current.kind === 'AFTER_SALE'" type="primary" icon="el-icon-money" @click="goAfterSale(current)">进入售后处理</el-button>
          <el-button v-if="current.kind === 'DIFF'" type="primary" icon="el-icon-s-data" @click="goReconciliation(current, 'diffs')">进入差异处理</el-button>
          <el-button v-if="current.kind === 'ALERT'" type="primary" icon="el-icon-warning-outline" @click="goReconciliation(current, 'alerts')">进入告警中心</el-button>
          <el-button v-if="canRetry(current)" v-hasPermi="['mall:compensation:retry']" type="primary" icon="el-icon-refresh-right" @click="retryCompensation(current)">重试补偿</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getAfterSaleFundsCenter, retryCompensationTask, runCompensationTasks } from '@/api/mall/afterSaleFunds'

export default {
  name: 'MallAfterSaleFundsCenter',
  data () {
    return {
      loading: false,
      detailVisible: false,
      current: null,
      rows: [],
      total: 0,
      summary: {},
      dateRange: [],
      query: { tab: 'ALL', status: undefined, businessNo: undefined, orderNo: undefined, pageNum: 1, pageSize: 10 },
      tabs: [
        { value: 'ALL', label: '全部工作项' }, { value: 'AFTER_SALE', label: '售后审核' },
        { value: 'REFUND', label: '退款执行' }, { value: 'DIFF', label: '对账差异', finance: true },
        { value: 'ALERT', label: '异常告警', finance: true }, { value: 'COMPENSATION', label: '补偿任务', finance: true }
      ],
      statusMap: {
        AFTER_SALE: [{ value: 'PENDING_REVIEW', label: '待审核' }],
        REFUND: [{ value: 'APPROVED', label: '待退款' }, { value: 'RETURN_SHIPPED', label: '退货已寄出' }, { value: 'REFUNDING', label: '退款处理中' }, { value: 'FAILED', label: '退款失败' }, { value: 'SUCCESS', label: '退款成功' }],
        DIFF: [{ value: 'OPEN', label: '待处理' }, { value: 'MANUAL', label: '人工处理' }, { value: 'RESOLVED', label: '已解决' }, { value: 'IGNORED', label: '已忽略' }],
        ALERT: [{ value: 'OPEN', label: '待确认' }, { value: 'ACKED', label: '已确认' }],
        COMPENSATION: [{ value: 'PENDING', label: '待执行' }, { value: 'PROCESSING', label: '执行中' }, { value: 'FAILED', label: '执行失败' }, { value: 'MANUAL', label: '人工处理' }, { value: 'SUCCESS', label: '已完成' }]
      }
    }
  },
  computed: {
    isMobile () { return this.$store.state.app.device === 'mobile' },
    permissions () { return this.$store.getters.permissions || [] },
    canFinance () { return this.hasPerm('mall:reconciliation:list') || this.hasPerm('mall:compensation:list') },
    visibleTabs () { return this.tabs.filter(item => !item.finance || this.canFinance) },
    statusOptions () { return this.statusMap[this.query.tab] || [] },
    summaryItems () {
      const items = [
        { key: 'pendingReview', label: '待审核', value: this.summary.pendingReview || 0, unit: '售后单', tab: 'AFTER_SALE', tone: 'blue' },
        { key: 'refundPending', label: '待退款', value: this.summary.refundPending || 0, unit: '资金处理', tab: 'REFUND', tone: 'amber' },
        { key: 'refundFailed', label: '退款失败', value: this.summary.refundFailed || 0, unit: '需要关注', tab: 'REFUND', status: 'FAILED', tone: 'red' }
      ]
      if (this.canFinance) items.push(
        { key: 'openDiffs', label: '待处理差异', value: this.summary.openDiffs || 0, unit: '对账工作项', tab: 'DIFF', tone: 'violet' },
        { key: 'openAlerts', label: '未确认告警', value: this.summary.openAlerts || 0, unit: '异常信号', tab: 'ALERT', status: 'OPEN', tone: 'red' },
        { key: 'manualCompensations', label: '人工补偿', value: this.summary.manualCompensations || 0, unit: '补偿任务', tab: 'COMPENSATION', status: 'MANUAL', tone: 'amber' }
      )
      return items
    }
  },
  created () { this.load() },
  methods: {
    hasPerm (permission) { return this.permissions.includes('*:*:*') || this.permissions.includes(permission) },
    load () {
      this.loading = true
      const params = { ...this.query, from: this.dateRange[0], to: this.dateRange[1] }
      getAfterSaleFundsCenter(params).then(response => {
        const data = response.data || {}
        this.summary = data.summary || {}; this.rows = data.rows || []; this.total = Number(data.total || 0)
      }).finally(() => { this.loading = false })
    },
    handleQuery () { this.query.pageNum = 1; this.load() },
    changeTab () { this.query.status = undefined; this.query.pageNum = 1; this.load() },
    resetQuery () { this.query.status = undefined; this.query.businessNo = undefined; this.query.orderNo = undefined; this.query.pageNum = 1; this.dateRange = []; this.load() },
    selectSummary (item) { this.query.tab = item.tab; this.query.status = item.status; this.query.pageNum = 1; this.load() },
    openDetail (row) { this.current = row; this.detailVisible = true },
    goAfterSale (row) { this.detailVisible = false; this.$router.push({ path: '/mall/after-sale-funds/after-sale', query: { afterSaleId: row.recordId } }) },
    goReconciliation (row, tab) { this.detailVisible = false; this.$router.push({ path: '/mall/after-sale-funds/reconciliation', query: { tab, businessNo: row.businessNo } }) },
    canRetry (row) { return row.kind === 'COMPENSATION' && ['FAILED', 'MANUAL'].includes(row.status) },
    retryCompensation (row) {
      this.$modal.confirm('确认将该补偿任务重置为待执行吗？').then(() => retryCompensationTask(row.recordId)).then(() => {
        this.$modal.msgSuccess('补偿任务已重置'); this.detailVisible = false; this.load()
      }).catch(() => {})
    },
    runCompensations () {
      this.$modal.confirm('确认立即执行当前到期的补偿任务吗？').then(() => runCompensationTasks()).then(response => {
        this.$modal.msgSuccess((response.data || 0) + ' 个任务已处理'); this.load()
      }).catch(() => {})
    },
    kindLabel (row) { return ({ AFTER_SALE: row && row.status === 'PENDING_REVIEW' ? '售后审核' : '退款执行', DIFF: '对账差异', ALERT: '异常告警', COMPENSATION: '补偿任务' })[row.kind] || row.kind },
    kindTagType (kind) { return ({ AFTER_SALE: 'warning', DIFF: 'danger', ALERT: 'danger', COMPENSATION: 'info' })[kind] || 'info' },
    statusLabel (value) { return ({ PENDING_REVIEW: '待审核', APPROVED: '待退款', RETURN_SHIPPED: '退货已寄出', REFUNDING: '退款处理中', FAILED: '执行失败', SUCCESS: '已完成', OPEN: '待处理', MANUAL: '人工处理', RESOLVED: '已解决', IGNORED: '已忽略', ACKED: '已确认', PENDING: '待执行', PROCESSING: '执行中' })[value] || value || '-' },
    statusTagType (value) { return ({ SUCCESS: 'success', RESOLVED: 'success', ACKED: 'success', IGNORED: 'info', FAILED: 'danger', OPEN: 'danger', MANUAL: 'warning', PENDING_REVIEW: 'warning', REFUNDING: 'warning' })[value] || 'info' },
    priorityLabel (value) { if (Number(value) >= 90) return '紧急'; if (Number(value) >= 75) return '优先'; return '常规' },
    priorityClass (value) { if (Number(value) >= 90) return 'urgent'; if (Number(value) >= 75) return 'important'; return 'normal' },
    signalLabel (row) {
      if (!row.riskSignal) return '-'
      return ({ MANUAL_REVIEW: '建议人工复核', STATUS_AMOUNT_MISMATCH: '状态与金额不一致', STATUS_MISMATCH: '状态不一致', AMOUNT_MISMATCH: '金额不一致', HIGH: '高等级告警', CRITICAL: '严重告警', MEDIUM: '中等级告警', PAYMENT_CONFIRM: '支付状态补偿', REFUND_CONFIRM: '退款状态补偿', LATE_PAYMENT_REFUND: '迟到支付退款', INVENTORY_RELEASE: '库存释放补偿', INVENTORY_CONFIRM: '库存确认补偿' })[row.riskSignal] || row.riskSignal
    },
    money (value) { return Number(value || 0).toFixed(2) }
  }
}
</script>

<style scoped>
.funds-center { min-height: calc(100vh - 84px); background: #f5f7fa; color: #303744; }
.page-heading { display: flex; align-items: center; justify-content: space-between; gap: 18px; padding: 18px 21px; border: 1px solid #e1e7ee; border-left: 4px solid #409eff; border-radius: 5px; background: #fff; }
.page-heading h1 { margin: 4px 0 0; color: #243248; font-size: 23px; }
.eyebrow { margin: 0; color: #347dbb; font-size: 10px; font-weight: 700; letter-spacing: .12em; }
.heading-actions { display: flex; gap: 8px; }
.summary-band { display: grid; grid-template-columns: repeat(6, minmax(118px, 1fr)); margin: 14px 0; overflow: hidden; border: 1px solid #dfe5ed; border-radius: 5px; background: #fff; }
.summary-item { position: relative; min-height: 88px; padding: 13px 16px; border: 0; border-right: 1px solid #e7ebf0; background: #fff; color: #27354a; text-align: left; cursor: pointer; }
.summary-item:last-child { border-right: 0; }
.summary-item::before { position: absolute; top: 0; left: 0; width: 100%; height: 3px; background: #7c8da4; content: ''; }
.summary-item.blue::before { background: #409eff; }.summary-item.amber::before { background: #e6a23c; }.summary-item.red::before { background: #e35d55; }.summary-item.violet::before { background: #7f73c8; }
.summary-item span, .summary-item strong, .summary-item small { display: block; }
.summary-item span { color: #7d899a; font-size: 11px; }.summary-item strong { margin: 6px 0 2px; font-size: 24px; }.summary-item small { color: #a0a9b5; font-size: 10px; }
.summary-item:hover { background: #f8fafc; }
.workbench { border: 1px solid #dfe5ed; border-radius: 5px; background: #fff; }
.queue-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 16px; padding: 14px 16px; border-bottom: 1px solid #e7ebf0; }
.queue-toolbar span { color: #8994a4; font-size: 11px; }
.query-form { padding: 15px 16px 2px; background: #fafbfd; }
.query-form .el-input { width: 210px; }.query-form .el-select { width: 145px; }.query-form .el-date-editor { width: 340px; }
.work-table { width: 100%; border-right: 0; border-left: 0; }
.business-no { display: block; margin-top: 6px; color: #334157; font-size: 12px; overflow-wrap: anywhere; }
.priority { display: inline-flex; align-items: center; min-height: 22px; padding: 0 7px; border-radius: 3px; font-size: 10px; font-weight: 700; }
.priority.urgent { background: #fdebea; color: #c63f37; }.priority.important { background: #fff3df; color: #bd7310; }.priority.normal { background: #eef3f7; color: #627085; }
.detail-drawer { padding: 0 20px 24px; }
.detail-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 15px; }
.detail-drawer dl { margin: 0; border-top: 1px solid #e5eaf0; }
.detail-drawer dl div { display: grid; grid-template-columns: 88px minmax(0, 1fr); gap: 12px; padding: 13px 2px; border-bottom: 1px solid #e5eaf0; font-size: 12px; line-height: 1.55; }
.detail-drawer dt { color: #8b95a4; }.detail-drawer dd { margin: 0; color: #3d4a5e; overflow-wrap: anywhere; }
.drawer-actions { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 20px; }
@media (max-width: 1100px) { .summary-band { grid-template-columns: repeat(3, minmax(0, 1fr)); }.summary-item:nth-child(3) { border-right: 0; }.summary-item:nth-child(-n+3) { border-bottom: 1px solid #e7ebf0; } }
@media (max-width: 900px) {
  .page-heading, .queue-toolbar { align-items: stretch; flex-direction: column; }
  .heading-actions { flex-wrap: wrap; }.heading-actions .el-button { margin-left: 0; }
  .summary-band { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .summary-item:nth-child(3) { border-right: 1px solid #e7ebf0; }.summary-item:nth-child(even) { border-right: 0; }.summary-item { border-bottom: 1px solid #e7ebf0; }.summary-item:nth-last-child(-n+2) { border-bottom: 0; }
  .queue-toolbar .el-radio-group { display: flex; overflow-x: auto; }
  .query-form .el-form-item, .query-form .el-input, .query-form .el-select, .query-form .el-date-editor { width: 100%; }
  .drawer-actions .el-button { width: 100%; margin-left: 0; }
}
@media (prefers-reduced-motion: reduce) { .summary-item { transition: none; } }
</style>
