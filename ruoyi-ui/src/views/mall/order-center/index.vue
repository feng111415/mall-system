<template>
  <div class="app-container order-center">
    <el-form ref="queryForm" :model="query" :inline="true" size="small" class="query-form">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="query.orderNo" clearable placeholder="订单号" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="会员ID" prop="memberId">
        <el-input-number v-model="query.memberId" :min="1" :controls="false" placeholder="会员ID" />
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="query.status" clearable placeholder="全部">
          <el-option v-for="item in orderStatuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="paymentStatus">
        <el-select v-model="query.paymentStatus" clearable placeholder="全部">
          <el-option v-for="item in paymentStatuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="风控状态" prop="riskStatus">
        <el-select v-model="query.riskStatus" clearable placeholder="全部">
          <el-option v-for="item in riskStatuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="物流状态" prop="logisticsStatus">
        <el-select v-model="query.logisticsStatus" clearable placeholder="全部">
          <el-option label="运输中" value="IN_TRANSIT" />
          <el-option label="已签收" value="DELIVERED" />
        </el-select>
      </el-form-item>
      <el-form-item label="售后状态" prop="afterSaleStatus">
        <el-select v-model="query.afterSaleStatus" clearable placeholder="全部">
          <el-option label="无售后" value="NONE" />
          <el-option v-for="item in afterSaleStatuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" value-format="yyyy-MM-dd HH:mm:ss" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <section class="summary-band" aria-label="当前页订单摘要">
      <div><span>当前页订单</span><strong>{{ rows.length }}</strong></div>
      <div><span>应付金额</span><strong>¥{{ pagePayableAmount }}</strong></div>
      <div><span>待发货</span><strong>{{ pendingShipmentCount }}</strong></div>
      <div><span>关联售后</span><strong>{{ afterSaleOrderCount }}</strong></div>
    </section>

    <el-table v-loading="loading" :data="rows" border class="order-table">
      <el-table-column label="订单号" min-width="190" fixed="left">
        <template slot-scope="scope">
          <el-button type="text" class="order-no" @click="openDetail(scope.row)">{{ scope.row.orderNo }}</el-button>
          <small>#{{ scope.row.orderId }}</small>
        </template>
      </el-table-column>
      <el-table-column label="会员" min-width="130">
        <template slot-scope="scope">
          <span>{{ scope.row.memberNickname || '未命名会员' }}</span>
          <small>ID {{ scope.row.memberId }}</small>
        </template>
      </el-table-column>
      <el-table-column label="商品" width="90" align="center">
        <template slot-scope="scope">{{ scope.row.itemCount }} 项 / {{ scope.row.totalQuantity }} 件</template>
      </el-table-column>
      <el-table-column label="应付金额" width="110" align="right">
        <template slot-scope="scope">¥{{ money(scope.row.payableAmount) }}</template>
      </el-table-column>
      <el-table-column label="订单状态" width="110">
        <template slot-scope="scope"><el-tag size="mini" :type="orderTagType(scope.row.status)">{{ orderStatusLabel(scope.row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="支付" width="100">
        <template slot-scope="scope"><span class="chain-state">{{ paymentStatusLabel(scope.row.paymentStatus) }}</span></template>
      </el-table-column>
      <el-table-column label="风控" width="100">
        <template slot-scope="scope"><span class="chain-state">{{ riskStatusLabel(scope.row.riskStatus) }}</span></template>
      </el-table-column>
      <el-table-column label="库存" width="100">
        <template slot-scope="scope"><span class="chain-state">{{ inventoryStatusLabel(scope.row.inventoryStatus) }}</span></template>
      </el-table-column>
      <el-table-column label="履约" min-width="140">
        <template slot-scope="scope">
          <span class="chain-state">{{ logisticsStatusLabel(scope.row.logisticsStatus, scope.row.status) }}</span>
          <small v-if="scope.row.logisticsCompanyName">{{ scope.row.logisticsCompanyName }} {{ scope.row.trackingNo }}</small>
        </template>
      </el-table-column>
      <el-table-column label="售后" min-width="120">
        <template slot-scope="scope">
          <span class="chain-state">{{ scope.row.afterSaleCount ? afterSaleStatusLabel(scope.row.latestAfterSaleStatus) : '无售后' }}</span>
          <small v-if="scope.row.afterSaleCount">{{ scope.row.afterSaleCount }} 笔</small>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="92" fixed="right" align="center">
        <template slot-scope="scope"><el-button type="text" icon="el-icon-view" v-hasPermi="['mall:order-center:query']" @click="openDetail(scope.row)">详情</el-button></template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="load" />

    <el-dialog title="订单运营详情" :visible.sync="detailVisible" :width="isMobile ? '100%' : '88%'" :fullscreen="isMobile" top="5vh" append-to-body>
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detail">
          <header class="detail-heading">
            <div>
              <span>{{ detail.orderNo }}</span>
              <small>会员 {{ detail.memberNickname || '-' }} · ID {{ detail.memberId }}</small>
            </div>
            <div class="detail-statuses">
              <el-tag size="small" :type="orderTagType(detail.status)">{{ orderStatusLabel(detail.status) }}</el-tag>
              <el-tag size="small" type="info">{{ paymentStatusLabel(detail.paymentStatus) }}</el-tag>
              <el-tag size="small" :type="riskTagType(detail.riskStatus)">{{ riskStatusLabel(detail.riskStatus) }}</el-tag>
              <strong>¥{{ money(detail.payableAmount) }}</strong>
            </div>
          </header>

          <el-descriptions :column="isMobile ? 1 : 3" border size="small" class="order-descriptions">
            <el-descriptions-item label="商品金额">¥{{ money(detail.productAmount) }}</el-descriptions-item>
            <el-descriptions-item label="运费">¥{{ money(detail.shippingFee) }}</el-descriptions-item>
            <el-descriptions-item label="优惠">-¥{{ money(detail.discountAmount) }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ detail.payTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="关闭时间">{{ detail.closeTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="收货信息" :span="isMobile ? 1 : 3">{{ receiverText }}</el-descriptions-item>
            <el-descriptions-item label="订单备注" :span="isMobile ? 1 : 3">{{ detail.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.cancelReason" label="关闭原因" :span="isMobile ? 1 : 3">{{ detail.cancelReason }}</el-descriptions-item>
          </el-descriptions>

          <el-tabs v-model="activeTab" class="detail-tabs">
            <el-tab-pane label="订单商品" name="items">
              <el-table :data="detail.items || []" size="mini" border>
                <el-table-column prop="productName" label="商品" min-width="180" />
                <el-table-column prop="skuName" label="规格" min-width="130" />
                <el-table-column prop="skuCode" label="SKU" min-width="130" />
                <el-table-column prop="unitPrice" label="单价" width="100"><template slot-scope="scope">¥{{ money(scope.row.unitPrice) }}</template></el-table-column>
                <el-table-column prop="quantity" label="数量" width="80" />
                <el-table-column prop="lineAmount" label="小计" width="110"><template slot-scope="scope">¥{{ money(scope.row.lineAmount) }}</template></el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="支付与库存" name="payment">
              <h3 class="section-title">支付记录</h3>
              <el-table :data="detail.payments || []" size="mini" border>
                <el-table-column prop="paymentNo" label="支付单号" min-width="190" />
                <el-table-column prop="paymentMethod" label="方式" width="90" />
                <el-table-column prop="amount" label="金额" width="100"><template slot-scope="scope">¥{{ money(scope.row.amount) }}</template></el-table-column>
                <el-table-column label="状态" width="110"><template slot-scope="scope">{{ paymentDetailStatusLabel(scope.row.status) }}</template></el-table-column>
                <el-table-column prop="providerPaymentNo" label="渠道流水" min-width="170" />
                <el-table-column prop="createTime" label="创建时间" width="160" />
                <el-table-column prop="failureReason" label="失败原因" min-width="160" />
              </el-table>
              <h3 class="section-title">库存预占</h3>
              <el-table :data="detail.reservations || []" size="mini" border>
                <el-table-column prop="skuId" label="SKU ID" width="100" />
                <el-table-column prop="quantity" label="数量" width="90" />
                <el-table-column label="状态" min-width="120"><template slot-scope="scope">{{ inventoryStatusLabel(scope.row.status) }}</template></el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="物流履约" name="logistics">
              <div class="tab-toolbar">
                <span>{{ detail.shipment ? detail.shipment.companyName + ' · ' + detail.shipment.trackingNo : '尚未创建物流单' }}</span>
                <el-button size="mini" type="primary" icon="el-icon-truck" v-hasPermi="['mall:logistics:list']" @click="goLogistics">进入发货工作台</el-button>
              </div>
              <el-timeline v-if="detail.shipment && detail.shipment.nodes">
                <el-timeline-item v-for="node in detail.shipment.nodes" :key="node.nodeId" :timestamp="node.eventTime">
                  <strong>{{ node.title }}</strong><span>{{ node.description }}</span><small v-if="node.location">{{ node.location }}</small>
                </el-timeline-item>
              </el-timeline>
              <el-empty v-else description="暂无物流轨迹" />
            </el-tab-pane>
            <el-tab-pane :label="`售后 (${(detail.afterSales || []).length})`" name="afterSale">
              <el-table :data="detail.afterSales || []" size="mini" border>
                <el-table-column prop="afterSaleNo" label="售后单号" min-width="190" />
                <el-table-column label="类型" width="100"><template slot-scope="scope">{{ afterSaleTypeLabel(scope.row.type) }}</template></el-table-column>
                <el-table-column label="状态" width="120"><template slot-scope="scope">{{ afterSaleStatusLabel(scope.row.status) }}</template></el-table-column>
                <el-table-column label="退款总额" width="110"><template slot-scope="scope">¥{{ afterSaleAmount(scope.row) }}</template></el-table-column>
                <el-table-column prop="reason" label="原因" min-width="160" />
                <el-table-column label="操作" width="90"><template slot-scope="scope"><el-button type="text" icon="el-icon-link" v-hasPermi="['mall:after-sale:query']" @click="goAfterSale(scope.row)">处理</el-button></template></el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="风控与日志" name="audit">
              <h3 class="section-title">风控记录</h3>
              <el-table :data="detail.riskRecords || []" size="mini" border>
                <el-table-column prop="ruleCode" label="规则" width="120" />
                <el-table-column prop="riskScore" label="分值" width="80" />
                <el-table-column label="决策" width="100"><template slot-scope="scope">{{ riskDecisionLabel(scope.row.decision) }}</template></el-table-column>
                <el-table-column prop="reason" label="说明" min-width="180" />
                <el-table-column prop="createTime" label="时间" width="160" />
              </el-table>
              <h3 class="section-title">订单操作记录</h3>
              <el-timeline>
                <el-timeline-item v-for="item in detail.operations || []" :key="item.operationId" :timestamp="item.createTime">
                  <strong>{{ operationStatusLabel(item.fromStatus) }} → {{ operationStatusLabel(item.toStatus) }}</strong>
                  <span>{{ item.remark || '-' }}</span><small>{{ operatorTypeLabel(item.operatorType) }} · {{ item.operatorId || 'system' }}</small>
                </el-timeline-item>
              </el-timeline>
            </el-tab-pane>
          </el-tabs>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOrderCenter, getOrderCenterDetail } from '@/api/mall/orderCenter'

export default {
  name: 'MallOrderOperationsCenter',
  data () {
    return {
      loading: false,
      detailLoading: false,
      detailVisible: false,
      rows: [],
      total: 0,
      detail: null,
      activeTab: 'items',
      dateRange: [],
      query: { pageNum: 1, pageSize: 10, orderNo: undefined, memberId: undefined, status: undefined, paymentStatus: undefined, riskStatus: undefined, logisticsStatus: undefined, afterSaleStatus: undefined },
      orderStatuses: [
        { value: 'PENDING_PAYMENT', label: '待付款' }, { value: 'RISK_REVIEW', label: '风控审核' },
        { value: 'PENDING_SHIPMENT', label: '待发货' }, { value: 'SHIPPED', label: '待收货' },
        { value: 'COMPLETED', label: '已完成' }, { value: 'AFTER_SALE', label: '售后中' },
        { value: 'CANCELLED', label: '已取消' }, { value: 'CLOSED', label: '已关闭' }
      ],
      paymentStatuses: [
        { value: 'UNPAID', label: '未支付' }, { value: 'PAYING', label: '支付中' }, { value: 'PAID', label: '已支付' },
        { value: 'FAILED', label: '支付失败' }, { value: 'REFUNDING', label: '退款中' }, { value: 'REFUNDED', label: '已退款' }
      ],
      riskStatuses: [
        { value: 'PENDING_CHECK', label: '待检查' }, { value: 'PASSED', label: '已通过' },
        { value: 'REVIEW', label: '人工复核' }, { value: 'REJECTED', label: '已拒绝' }
      ],
      afterSaleStatuses: [
        { value: 'PENDING_REVIEW', label: '待审核' }, { value: 'APPROVED', label: '已通过' },
        { value: 'RETURN_SHIPPED', label: '退货已寄出' }, { value: 'REFUNDING', label: '退款处理中' },
        { value: 'SUCCESS', label: '退款成功' }, { value: 'REJECTED', label: '已驳回' }, { value: 'FAILED', label: '退款失败' }
      ]
    }
  },
  computed: {
    isMobile () { return this.$store.state.app.device === 'mobile' },
    pagePayableAmount () { return this.rows.reduce((sum, row) => sum + Number(row.payableAmount || 0), 0).toFixed(2) },
    pendingShipmentCount () { return this.rows.filter(row => row.status === 'PENDING_SHIPMENT').length },
    afterSaleOrderCount () { return this.rows.filter(row => Number(row.afterSaleCount || 0) > 0).length },
    receiverText () {
      if (!this.detail) return '-'
      return [this.detail.receiverName, this.detail.receiverPhone, this.detail.receiverProvince, this.detail.receiverCity, this.detail.receiverDistrict, this.detail.receiverDetailAddress].filter(Boolean).join(' ')
    }
  },
  created () { this.load() },
  methods: {
    load () {
      this.loading = true
      const params = { ...this.query, createStart: this.dateRange[0], createEnd: this.dateRange[1] }
      listOrderCenter(params).then(response => { this.rows = response.rows || []; this.total = response.total || 0 }).finally(() => { this.loading = false })
    },
    handleQuery () { this.query.pageNum = 1; this.load() },
    resetQuery () { this.$refs.queryForm.resetFields(); this.dateRange = []; this.query.pageNum = 1; this.load() },
    openDetail (row) {
      this.detailVisible = true; this.detailLoading = true; this.activeTab = 'items'; this.detail = null
      getOrderCenterDetail(row.orderId).then(response => { this.detail = response.data }).finally(() => { this.detailLoading = false })
    },
    goLogistics () { this.detailVisible = false; this.$router.push({ path: '/mall/order-fulfillment/logistics', query: { orderNo: this.detail.orderNo } }) },
    goAfterSale (row) { this.detailVisible = false; this.$router.push({ path: '/mall/after-sale-funds/after-sale', query: { afterSaleId: row.afterSaleId } }) },
    money (value) { return Number(value || 0).toFixed(2) },
    orderStatusLabel (value) { return (this.orderStatuses.find(item => item.value === value) || {}).label || value || '-' },
    paymentStatusLabel (value) { return (this.paymentStatuses.find(item => item.value === value) || {}).label || value || '-' },
    riskStatusLabel (value) { return (this.riskStatuses.find(item => item.value === value) || {}).label || value || '-' },
    paymentDetailStatusLabel (value) { return ({ CREATING: '创建中', PAYING: '支付中', SUCCESS: '支付成功', CLOSED: '已关闭', FAILED: '失败', REFUNDING: '退款中', REFUNDED: '已退款' })[value] || value || '-' },
    inventoryStatusLabel (value) { return ({ LOCKED: '已锁定', CONFIRMED: '已扣减', RELEASED: '已释放', NOT_RESERVED: '未预占' })[value] || value || '-' },
    logisticsStatusLabel (value, orderStatus) { if (value === 'DELIVERED') return '已签收'; if (value === 'IN_TRANSIT') return '运输中'; return orderStatus === 'PENDING_SHIPMENT' ? '待发货' : '未建物流单' },
    operationStatusLabel (value) { return ({ SHIPPED: '已发货', IN_TRANSIT: '运输中', OUT_FOR_DELIVERY: '派送中', ARRIVED: '已送达', DELIVERED: '已签收', EXCEPTION: '运输异常', CORRECTION: '更正说明' })[value] || this.orderStatusLabel(value) },
    operatorTypeLabel (value) { return ({ ADMIN: '管理员', MEMBER: '会员', PAYMENT: '支付系统', SYSTEM: '系统' })[value] || value || '系统' },
    riskDecisionLabel (value) { return ({ PASS: '通过', REVIEW: '人工复核', REJECT: '拒绝' })[value] || value || '-' },
    afterSaleStatusLabel (value) { return (this.afterSaleStatuses.find(item => item.value === value) || {}).label || value || '-' },
    afterSaleTypeLabel (value) { return value === 'RETURN_REFUND' ? '退货退款' : '仅退款' },
    afterSaleAmount (row) { return (Number(row.refundAmount || 0) + Number(row.shippingRefundAmount || 0)).toFixed(2) },
    orderTagType (value) { return ({ PENDING_PAYMENT: 'warning', RISK_REVIEW: 'danger', PENDING_SHIPMENT: '', SHIPPED: '', COMPLETED: 'success', AFTER_SALE: 'warning', CANCELLED: 'info', CLOSED: 'info' })[value] || 'info' },
    riskTagType (value) { return ({ PENDING_CHECK: 'warning', PASSED: 'success', REVIEW: 'danger', REJECTED: 'danger' })[value] || 'info' }
  }
}
</script>

<style scoped>
.order-center { min-height: calc(100vh - 84px); background: #f5f7fa; color: #303744; }
.query-form { padding: 16px 16px 4px; border: 1px solid #e3e8ef; border-radius: 5px; background: #fff; }
.query-form .el-input { width: 176px; }
.query-form .el-input-number { width: 142px; }
.query-form .el-select { width: 142px; }
.query-form .el-date-editor { width: 350px; }
.summary-band { display: grid; grid-template-columns: repeat(4, minmax(140px, 1fr)); margin: 14px 0; border: 1px solid #dfe5ed; border-radius: 5px; background: #fff; }
.summary-band div { min-height: 68px; padding: 13px 18px; border-right: 1px solid #e8ecf1; }
.summary-band div:last-child { border-right: 0; }
.summary-band span, .summary-band strong { display: block; }
.summary-band span { color: #8792a3; font-size: 12px; }
.summary-band strong { margin-top: 7px; color: #24334a; font-size: 20px; }
.order-table { width: 100%; }
.order-table small, .detail-heading small, .el-timeline-item small { display: block; margin-top: 4px; color: #8b96a6; font-size: 11px; line-height: 1.45; overflow-wrap: anywhere; }
.order-no { padding: 0; font-weight: 700; }
.chain-state { color: #445169; font-size: 12px; font-weight: 600; }
.detail-body { min-height: 360px; }
.detail-heading { display: flex; align-items: center; justify-content: space-between; gap: 18px; margin-bottom: 14px; padding: 14px 16px; border-left: 4px solid #409eff; background: #f4f8fd; }
.detail-heading span { display: block; color: #21314a; font-size: 17px; font-weight: 700; }
.detail-statuses { display: flex; align-items: center; gap: 9px; }
.detail-statuses strong { margin-left: 8px; color: #d84f3d; font-size: 18px; }
.order-descriptions { margin-bottom: 14px; }
.detail-tabs { min-height: 330px; }
.section-title { margin: 18px 0 10px; color: #37445a; font-size: 13px; }
.section-title:first-child { margin-top: 4px; }
.tab-toolbar { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 16px; padding: 10px 12px; background: #f6f8fb; color: #596579; }
.el-timeline-item strong, .el-timeline-item span { display: block; }
.el-timeline-item span { margin-top: 4px; color: #667286; font-size: 12px; line-height: 1.55; }
@media (max-width: 900px) {
  .summary-band { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .summary-band div:nth-child(2) { border-right: 0; }
  .summary-band div:nth-child(-n+2) { border-bottom: 1px solid #e8ecf1; }
  .query-form .el-form-item, .query-form .el-input, .query-form .el-input-number, .query-form .el-select, .query-form .el-date-editor { width: 100%; }
  .detail-heading, .detail-statuses, .tab-toolbar { align-items: flex-start; flex-direction: column; }
  .detail-statuses strong { margin-left: 0; }
}
</style>
