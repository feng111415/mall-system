<template>
  <div class="app-container fulfillment-page">
    <header class="page-heading">
      <div>
        <p class="eyebrow">ORDER FULFILLMENT</p>
        <h1>物流履约工作台</h1>
      </div>
      <div class="heading-actions">
        <span v-if="refreshedAt"><i class="el-icon-time" /> {{ refreshedAt }}</span>
        <el-button icon="el-icon-refresh" size="small" @click="refresh">刷新</el-button>
      </div>
    </header>

    <el-form ref="queryForm" :model="query" :inline="true" size="small" class="query-form">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="query.orderNo" clearable placeholder="订单号" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="收件人" prop="receiverKeyword">
        <el-input v-model="query.receiverKeyword" clearable placeholder="姓名或手机号" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="物流公司" prop="companyCode">
        <el-select v-model="query.companyCode" clearable filterable placeholder="全部">
          <el-option v-for="item in companies" :key="item.code" :label="item.name" :value="item.code" />
        </el-select>
      </el-form-item>
      <el-form-item label="履约阶段" prop="workflowStatus">
        <el-select v-model="query.workflowStatus" clearable placeholder="全部">
          <el-option v-for="item in workflowStatuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="运营关注" prop="attentionType">
        <el-select v-model="query.attentionType" clearable placeholder="全部">
          <el-option v-for="item in attentionTypes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="下单时间">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="yyyy-MM-dd HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh-left" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <section v-loading="summaryLoading" class="summary-band" aria-label="履约全局汇总">
      <button type="button" :class="{ active: !query.workflowStatus && !query.attentionType }" @click="applySummaryFilter()">
        <span>全部履约</span><strong>{{ summary.totalCount || 0 }}</strong>
      </button>
      <button type="button" :class="{ active: query.workflowStatus === 'WAITING_SHIPMENT' }" @click="applySummaryFilter('WAITING_SHIPMENT')">
        <span>待发货</span><strong>{{ summary.waitingShipmentCount || 0 }}</strong>
      </button>
      <button type="button" :class="{ active: query.workflowStatus === 'IN_TRANSIT' }" @click="applySummaryFilter('IN_TRANSIT')">
        <span>运输中</span><strong>{{ summary.inTransitCount || 0 }}</strong>
      </button>
      <button type="button" :class="{ active: query.workflowStatus === 'ARRIVED' }" @click="applySummaryFilter('ARRIVED')">
        <span>已送达</span><strong>{{ summary.arrivedCount || 0 }}</strong>
      </button>
      <button type="button" :class="{ active: query.workflowStatus === 'DELIVERED_WAIT_RECEIPT' }" @click="applySummaryFilter('DELIVERED_WAIT_RECEIPT')">
        <span>签收待确认</span><strong>{{ summary.deliveredWaitReceiptCount || 0 }}</strong>
      </button>
      <button type="button" :class="{ active: query.attentionType === 'OVERDUE_UNSHIPPED' }" @click="applySummaryFilter(null, 'OVERDUE_UNSHIPPED')">
        <span>超 24h 未发货</span><strong class="warning-count">{{ summary.overdueUnshippedCount || 0 }}</strong>
      </button>
      <button type="button" :class="{ active: query.workflowStatus === 'EXCEPTION' }" @click="applySummaryFilter('EXCEPTION')">
        <span>运输异常</span><strong class="danger-count">{{ summary.exceptionCount || 0 }}</strong>
      </button>
      <button type="button" :class="{ active: query.workflowStatus === 'DATA_INCONSISTENT' }" @click="applySummaryFilter('DATA_INCONSISTENT')">
        <span>数据不一致</span><strong class="danger-count">{{ summary.dataInconsistentCount || 0 }}</strong>
      </button>
    </section>

    <el-table v-loading="loading" :data="rows" border class="fulfillment-table">
      <el-table-column label="订单 / 收件人" min-width="210" fixed="left">
        <template slot-scope="scope">
          <strong class="order-no">{{ scope.row.orderNo }}</strong>
          <small>{{ scope.row.receiverName }} · {{ maskedPhone(scope.row.receiverPhone) }}</small>
          <small>{{ scope.row.orderCreateTime }}</small>
        </template>
      </el-table-column>
      <el-table-column label="履约阶段" width="130">
        <template slot-scope="scope">
          <el-tag size="mini" :type="workflowTagType(scope.row.workflowStatus)">{{ workflowLabel(scope.row.workflowStatus) }}</el-tag>
          <small>{{ orderStatusLabel(scope.row.status) }}</small>
        </template>
      </el-table-column>
      <el-table-column label="物流单" min-width="210">
        <template slot-scope="scope">
          <template v-if="scope.row.shipmentId">
            <strong>{{ scope.row.companyName }}</strong>
            <small>{{ scope.row.trackingNo }}</small>
          </template>
          <span v-else class="empty-value">尚未创建</span>
        </template>
      </el-table-column>
      <el-table-column label="最新轨迹" min-width="250">
        <template slot-scope="scope">
          <template v-if="scope.row.latestNodeStatus">
            <strong>{{ scope.row.latestNodeTitle }}</strong>
            <small>{{ scope.row.latestNodeDescription }}</small>
            <small>{{ scope.row.latestNodeTime }}<template v-if="scope.row.latestNodeLocation"> · {{ scope.row.latestNodeLocation }}</template></small>
          </template>
          <span v-else class="empty-value">暂无轨迹</span>
        </template>
      </el-table-column>
      <el-table-column label="关键时间" width="170">
        <template slot-scope="scope">
          <small class="time-line"><span>发货</span>{{ scope.row.shippedTime || '-' }}</small>
          <small class="time-line"><span>签收</span>{{ scope.row.deliveredTime || '-' }}</small>
        </template>
      </el-table-column>
      <el-table-column label="运营关注" width="145">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.attentionType" size="mini" :type="attentionTagType(scope.row.attentionType)" effect="plain">
            {{ attentionLabel(scope.row.attentionType) }}
          </el-tag>
          <span v-else class="normal-signal"><i class="el-icon-circle-check" /> 正常</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="226" fixed="right">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.workflowStatus === 'WAITING_SHIPMENT'"
            type="text"
            size="mini"
            icon="el-icon-truck"
            v-hasPermi="['mall:logistics:ship']"
            @click="openShip(scope.row)"
          >发货</el-button>
          <el-button
            v-if="scope.row.shipmentId"
            type="text"
            size="mini"
            icon="el-icon-view"
            v-hasPermi="['mall:logistics:query']"
            @click="openDetail(scope.row)"
          >轨迹</el-button>
          <el-button
            v-if="scope.row.shipmentId && scope.row.shipmentStatus !== 'CANCELLED'"
            type="text"
            size="mini"
            icon="el-icon-plus"
            v-hasPermi="['mall:logistics:node']"
            @click="openNode(scope.row)"
          >追加节点</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="loadRows" />

    <el-dialog title="手工发货" :visible.sync="shipVisible" :width="isMobile ? '100%' : '520px'" :fullscreen="isMobile" append-to-body>
      <div class="dialog-order">
        <strong>{{ activeRow ? activeRow.orderNo : '-' }}</strong>
        <span>{{ activeRow ? activeRow.receiverName : '-' }}</span>
      </div>
      <el-form ref="shipForm" :model="shipForm" :rules="shipRules" label-width="92px">
        <el-form-item label="物流公司" prop="companyCode">
          <el-select v-model="shipForm.companyCode" filterable placeholder="请选择" class="full-control">
            <el-option v-for="item in companies" :key="item.code" :label="item.name" :value="item.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="运单号" prop="trackingNo">
          <el-input v-model.trim="shipForm.trackingNo" maxlength="128" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitShip">确认发货</el-button>
      </div>
    </el-dialog>

    <el-dialog title="物流轨迹" :visible.sync="detailVisible" :width="isMobile ? '100%' : '760px'" :fullscreen="isMobile" append-to-body>
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="shipment">
          <header class="shipment-heading">
            <div><strong>{{ shipment.companyName }}</strong><span>{{ shipment.trackingNo }}</span></div>
            <el-tag :type="shipment.status === 'DELIVERED' ? 'success' : ''">{{ shipment.status === 'DELIVERED' ? '已签收' : '运输中' }}</el-tag>
          </header>
          <el-descriptions :column="isMobile ? 1 : 2" border size="small">
            <el-descriptions-item label="发货时间">{{ shipment.shippedTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="签收时间">{{ shipment.deliveredTime || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-timeline v-if="shipment.nodes && shipment.nodes.length" class="shipment-timeline">
            <el-timeline-item
              v-for="node in shipment.nodes"
              :key="node.nodeId"
              :timestamp="node.eventTime"
              :type="nodeTimelineType(node.nodeStatus)"
            >
              <strong>{{ node.title }}</strong>
              <span>{{ node.description }}</span>
              <small v-if="node.location"><i class="el-icon-location-outline" /> {{ node.location }}</small>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无物流轨迹" />
        </template>
      </div>
    </el-dialog>

    <el-dialog title="追加物流节点" :visible.sync="nodeVisible" :width="isMobile ? '100%' : '560px'" :fullscreen="isMobile" append-to-body>
      <div class="dialog-order">
        <strong>{{ activeRow ? activeRow.orderNo : '-' }}</strong>
        <span>{{ activeRow ? activeRow.companyName + ' · ' + activeRow.trackingNo : '-' }}</span>
      </div>
      <el-form ref="nodeForm" :model="nodeForm" :rules="nodeRules" label-width="92px">
        <el-form-item label="节点状态" prop="nodeStatus">
          <el-select v-model="nodeForm.nodeStatus" class="full-control">
            <el-option v-for="item in availableNodeStatuses" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title"><el-input v-model.trim="nodeForm.title" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="说明" prop="description"><el-input v-model.trim="nodeForm.description" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item>
        <el-form-item label="地点" prop="location"><el-input v-model.trim="nodeForm.location" maxlength="100" show-word-limit /></el-form-item>
        <el-form-item label="发生时间" prop="eventTime">
          <el-date-picker v-model="nodeForm.eventTime" type="datetime" value-format="yyyy-MM-ddTHH:mm:ss" placeholder="默认当前时间" class="full-control" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="nodeVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitNode">追加节点</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  appendNode,
  getFulfillmentSummary,
  getShipment,
  listLogisticsCompanies,
  listOrders,
  shipOrder
} from '@/api/mall/logistics'

export default {
  name: 'MallLogistics',
  data () {
    return {
      loading: false,
      summaryLoading: false,
      detailLoading: false,
      submitting: false,
      rows: [],
      total: 0,
      summary: {},
      companies: [],
      dateRange: [],
      refreshedAt: '',
      activeRow: null,
      shipment: null,
      shipVisible: false,
      detailVisible: false,
      nodeVisible: false,
      query: {
        pageNum: 1,
        pageSize: 10,
        orderNo: undefined,
        receiverKeyword: undefined,
        companyCode: undefined,
        workflowStatus: undefined,
        attentionType: undefined
      },
      shipForm: { companyCode: '', trackingNo: '' },
      nodeForm: { nodeStatus: 'IN_TRANSIT', title: '', description: '', location: '', eventTime: null },
      shipRules: {
        companyCode: [{ required: true, message: '请选择物流公司', trigger: 'change' }],
        trackingNo: [{ required: true, message: '请输入运单号', trigger: 'blur' }]
      },
      nodeRules: {
        nodeStatus: [{ required: true, message: '请选择节点状态', trigger: 'change' }],
        title: [{ required: true, message: '请输入节点标题', trigger: 'blur' }],
        description: [{ required: true, message: '请输入节点说明', trigger: 'blur' }]
      },
      workflowStatuses: [
        { value: 'WAITING_SHIPMENT', label: '待发货' },
        { value: 'IN_TRANSIT', label: '运输中' },
        { value: 'OUT_FOR_DELIVERY', label: '派送中' },
        { value: 'ARRIVED', label: '已送达' },
        { value: 'EXCEPTION', label: '运输异常' },
        { value: 'DELIVERED_WAIT_RECEIPT', label: '签收待确认' },
        { value: 'COMPLETED', label: '已完成' },
        { value: 'DATA_INCONSISTENT', label: '数据不一致' }
      ],
      attentionTypes: [
        { value: 'OVERDUE_UNSHIPPED', label: '超 24 小时未发货' },
        { value: 'TRANSPORT_EXCEPTION', label: '运输异常' },
        { value: 'DATA_INCONSISTENT', label: '订单与物流不一致' }
      ],
      nodeStatuses: [
        { value: 'IN_TRANSIT', label: '运输中' },
        { value: 'OUT_FOR_DELIVERY', label: '派送中' },
        { value: 'ARRIVED', label: '已送达' },
        { value: 'DELIVERED', label: '已签收' },
        { value: 'EXCEPTION', label: '运输异常' },
        { value: 'CORRECTION', label: '更正说明' }
      ]
    }
  },
  computed: {
    isMobile () { return this.$store.state.app.device === 'mobile' },
    availableNodeStatuses () {
      if (this.activeRow && this.activeRow.shipmentStatus === 'DELIVERED') return this.nodeStatuses.filter(item => item.value === 'CORRECTION')
      return this.nodeStatuses
    }
  },
  created () {
    if (this.$route.query.orderNo) this.query.orderNo = this.$route.query.orderNo
    this.loadCompanies()
    this.refresh()
  },
  methods: {
    requestParams () {
      return { ...this.query, createStart: this.dateRange[0], createEnd: this.dateRange[1] }
    },
    refresh () {
      this.loadRows()
      this.loadSummary()
    },
    loadRows () {
      this.loading = true
      listOrders(this.requestParams()).then(response => {
        this.rows = response.rows || []
        this.total = response.total || 0
        this.refreshedAt = this.formatRefreshTime()
      }).finally(() => { this.loading = false })
    },
    loadSummary () {
      this.summaryLoading = true
      const params = this.requestParams()
      delete params.pageNum
      delete params.pageSize
      delete params.workflowStatus
      delete params.attentionType
      getFulfillmentSummary(params).then(response => { this.summary = response.data || {} }).finally(() => { this.summaryLoading = false })
    },
    loadCompanies () {
      listLogisticsCompanies().then(response => { this.companies = response.data || [] })
    },
    handleQuery () {
      this.query.pageNum = 1
      this.refresh()
    },
    resetQuery () {
      this.$refs.queryForm.resetFields()
      this.dateRange = []
      this.query.pageNum = 1
      this.refresh()
    },
    applySummaryFilter (workflowStatus, attentionType) {
      this.query.workflowStatus = workflowStatus || undefined
      this.query.attentionType = attentionType || undefined
      this.query.pageNum = 1
      this.refresh()
    },
    openShip (row) {
      this.activeRow = row
      this.shipForm = { companyCode: '', trackingNo: '' }
      this.shipVisible = true
      this.$nextTick(() => this.$refs.shipForm && this.$refs.shipForm.clearValidate())
    },
    submitShip () {
      this.$refs.shipForm.validate(valid => {
        if (!valid) return
        this.submitting = true
        shipOrder(this.activeRow.orderId, this.shipForm).then(() => {
          this.$modal.msgSuccess('发货成功')
          this.shipVisible = false
          this.refresh()
        }).finally(() => { this.submitting = false })
      })
    },
    openDetail (row) {
      this.activeRow = row
      this.shipment = null
      this.detailVisible = true
      this.detailLoading = true
      getShipment(row.shipmentId).then(response => { this.shipment = response.data }).finally(() => { this.detailLoading = false })
    },
    openNode (row) {
      this.activeRow = row
      const defaultStatus = row.shipmentStatus === 'DELIVERED' ? 'CORRECTION' : 'IN_TRANSIT'
      this.nodeForm = { nodeStatus: defaultStatus, title: '', description: '', location: '', eventTime: null }
      this.nodeVisible = true
      this.$nextTick(() => this.$refs.nodeForm && this.$refs.nodeForm.clearValidate())
    },
    submitNode () {
      this.$refs.nodeForm.validate(valid => {
        if (!valid) return
        this.submitting = true
        appendNode(this.activeRow.shipmentId, this.nodeForm).then(() => {
          this.$modal.msgSuccess('物流节点已追加')
          this.nodeVisible = false
          this.refresh()
        }).finally(() => { this.submitting = false })
      })
    },
    workflowLabel (value) { return (this.workflowStatuses.find(item => item.value === value) || {}).label || value || '-' },
    attentionLabel (value) { return (this.attentionTypes.find(item => item.value === value) || {}).label || value || '-' },
    orderStatusLabel (value) { return ({ PENDING_SHIPMENT: '订单待发货', SHIPPED: '订单待收货', COMPLETED: '订单已完成' })[value] || value || '-' },
    workflowTagType (value) { return ({ WAITING_SHIPMENT: 'warning', IN_TRANSIT: '', OUT_FOR_DELIVERY: '', ARRIVED: 'warning', EXCEPTION: 'danger', DELIVERED_WAIT_RECEIPT: 'success', COMPLETED: 'success', DATA_INCONSISTENT: 'danger' })[value] || 'info' },
    attentionTagType (value) { return value === 'OVERDUE_UNSHIPPED' ? 'warning' : 'danger' },
    nodeTimelineType (value) { return ({ ARRIVED: 'warning', DELIVERED: 'success', EXCEPTION: 'danger', CORRECTION: 'warning' })[value] || 'primary' },
    maskedPhone (value) { return value && value.length >= 7 ? value.slice(0, 3) + '****' + value.slice(-4) : value || '-' },
    formatRefreshTime () {
      const now = new Date()
      const pad = value => String(value).padStart(2, '0')
      return '更新于 ' + pad(now.getHours()) + ':' + pad(now.getMinutes()) + ':' + pad(now.getSeconds())
    }
  }
}
</script>

<style scoped>
.fulfillment-page { min-height: calc(100vh - 84px); background: #f5f7fa; color: #303744; }
.page-heading { display: flex; align-items: center; justify-content: space-between; gap: 18px; margin-bottom: 14px; padding: 17px 20px; border: 1px solid #e1e7ef; border-left: 4px solid #409eff; border-radius: 6px; background: #fff; }
.page-heading h1 { margin: 3px 0 0; color: #202c3e; font-size: 22px; line-height: 1.3; }
.eyebrow { margin: 0; color: #3c78ad; font-size: 10px; font-weight: 700; letter-spacing: .12em; }
.heading-actions { display: flex; align-items: center; gap: 12px; color: #7b8798; font-size: 12px; }
.query-form { padding: 15px 16px 3px; border: 1px solid #e1e7ef; border-radius: 5px; background: #fff; }
.query-form .el-input { width: 166px; }
.query-form .el-select { width: 150px; }
.query-form .el-date-editor { width: 350px; }
.summary-band { display: grid; grid-template-columns: repeat(8, minmax(108px, 1fr)); margin: 13px 0; overflow: hidden; border: 1px solid #dfe5ed; border-radius: 5px; background: #fff; }
.summary-band button { position: relative; min-width: 0; min-height: 67px; padding: 11px 14px; border: 0; border-right: 1px solid #e8ecf1; background: #fff; color: inherit; text-align: left; cursor: pointer; }
.summary-band button:last-child { border-right: 0; }
.summary-band button:hover { background: #f7faff; }
.summary-band button.active { background: #eef6ff; }
.summary-band button.active::after { position: absolute; right: 0; bottom: 0; left: 0; height: 3px; background: #409eff; content: ''; }
.summary-band span, .summary-band strong { display: block; }
.summary-band span { overflow: hidden; color: #7d899a; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
.summary-band strong { margin-top: 7px; color: #25354c; font-size: 19px; }
.summary-band .warning-count { color: #b87314; }
.summary-band .danger-count { color: #c84b45; }
.fulfillment-table { width: 100%; }
.fulfillment-table strong, .fulfillment-table small { display: block; }
.fulfillment-table strong { color: #303b4d; font-size: 12px; line-height: 1.45; }
.fulfillment-table small { margin-top: 4px; color: #8994a4; font-size: 11px; line-height: 1.42; overflow-wrap: anywhere; }
.fulfillment-table .order-no { color: #2f6fae; font-size: 12px; }
.empty-value { color: #a1a9b5; font-size: 12px; }
.time-line span { display: inline-block; width: 30px; color: #667286; font-weight: 600; }
.normal-signal { color: #5b8b6a; font-size: 12px; white-space: nowrap; }
.dialog-order { display: flex; justify-content: space-between; gap: 12px; margin: -4px 0 18px; padding: 11px 13px; border-left: 3px solid #409eff; background: #f3f7fc; color: #687589; font-size: 12px; }
.dialog-order strong { color: #26364e; }
.full-control { width: 100%; }
.detail-body { min-height: 260px; }
.shipment-heading { display: flex; align-items: center; justify-content: space-between; gap: 15px; margin-bottom: 14px; padding: 12px 14px; background: #f3f7fc; }
.shipment-heading strong, .shipment-heading span { display: block; }
.shipment-heading strong { color: #26364e; font-size: 15px; }
.shipment-heading span { margin-top: 4px; color: #758196; font-size: 12px; }
.shipment-timeline { margin-top: 22px; padding: 0 8px; }
.shipment-timeline strong, .shipment-timeline span, .shipment-timeline small { display: block; }
.shipment-timeline strong { color: #334158; font-size: 13px; }
.shipment-timeline span { margin-top: 4px; color: #667286; font-size: 12px; line-height: 1.55; }
.shipment-timeline small { margin-top: 4px; color: #9099a8; font-size: 11px; }
@media (max-width: 1200px) {
  .summary-band { grid-template-columns: repeat(4, minmax(120px, 1fr)); }
  .summary-band button:nth-child(4) { border-right: 0; }
  .summary-band button:nth-child(-n+4) { border-bottom: 1px solid #e8ecf1; }
}
@media (max-width: 900px) {
  .page-heading { align-items: flex-start; flex-direction: column; }
  .heading-actions { width: 100%; justify-content: space-between; }
  .query-form .el-form-item, .query-form .el-input, .query-form .el-select, .query-form .el-date-editor { width: 100%; }
  .summary-band { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .summary-band button { border-bottom: 1px solid #e8ecf1; }
  .summary-band button:nth-child(even) { border-right: 0; }
  .summary-band button:last-child { border-bottom: 0; }
  .dialog-order, .shipment-heading { align-items: flex-start; flex-direction: column; }
}
</style>
