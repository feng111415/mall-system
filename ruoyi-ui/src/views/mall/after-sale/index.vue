<template>
  <div class="app-container">
    <el-form :inline="true" size="small">
      <el-form-item label="状态">
        <el-select v-model="status" clearable placeholder="全部状态">
          <el-option v-for="item in statuses" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="load">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list">
      <el-table-column prop="afterSaleNo" label="售后单号" min-width="190" />
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column label="类型" width="110">
        <template slot-scope="scope">{{ typeLabel(scope.row.type) }}</template>
      </el-table-column>
      <el-table-column label="退款总额" width="110">
        <template slot-scope="scope">¥{{ refundTotal(scope.row) }}</template>
      </el-table-column>
      <el-table-column label="风险信号" min-width="130">
        <template slot-scope="scope">{{ riskLabel(scope.row.riskSignal) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template slot-scope="scope">{{ statusLabel(scope.row.status) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="340">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-view" v-hasPermi="['mall:after-sale:query']" @click="showDetail(scope.row)">详情</el-button>
          <el-button v-if="scope.row.status === 'PENDING_REVIEW'" type="text" size="mini" icon="el-icon-check" v-hasPermi="['mall:after-sale:audit']" @click="approve(scope.row)">审核通过</el-button>
          <el-button v-if="scope.row.status === 'PENDING_REVIEW'" type="text" size="mini" icon="el-icon-close" v-hasPermi="['mall:after-sale:audit']" @click="reject(scope.row)">驳回</el-button>
          <el-button v-if="canRefund(scope.row)" type="text" size="mini" icon="el-icon-money" v-hasPermi="['mall:after-sale:audit']" @click="refund(scope.row)">{{ refundActionLabel(scope.row) }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="售后详情" :visible.sync="dialog" width="720px">
      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="售后类型">{{ typeLabel(current.type) }}</el-descriptions-item>
          <el-descriptions-item label="当前状态">{{ statusLabel(current.status) }}</el-descriptions-item>
          <el-descriptions-item label="原因">{{ current.reasonCode }} / {{ current.reason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="风险信号">{{ riskLabel(current.riskSignal) }}</el-descriptions-item>
          <el-descriptions-item label="售后期限">{{ current.deadlineTime }}</el-descriptions-item>
          <el-descriptions-item label="退款总额">¥{{ refundTotal(current) }}</el-descriptions-item>
          <el-descriptions-item label="退货物流">{{ companyName(current.returnCompanyCode) }} {{ current.returnTrackingNo || '' }}</el-descriptions-item>
          <el-descriptions-item label="凭证地址"><span class="evidence-value">{{ current.evidenceUrl || '-' }}</span></el-descriptions-item>
          <el-descriptions-item v-if="current.failureReason" label="处理说明">{{ current.failureReason }}</el-descriptions-item>
          <el-descriptions-item v-if="current.providerRefundNo" label="退款流水">{{ current.providerRefundNo }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="current.items" size="mini">
          <el-table-column prop="productName" label="商品" min-width="150" />
          <el-table-column prop="skuName" label="规格" min-width="100" />
          <el-table-column prop="requestedQuantity" label="申请数量" width="100" />
          <el-table-column prop="refundAmount" label="退款金额" width="100" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { listAfterSales, getAfterSale, approveAfterSale, rejectAfterSale, refundAfterSale } from '@/api/mall/afterSale'

const logisticsCompanies = {
  SF: '顺丰速运', ZTO: '中通快递', YTO: '圆通速递', STO: '申通快递',
  YD: '韵达速递', JD: '京东物流', EMS: '中国邮政 EMS', JT: '极兔速递', DEPPON: '德邦快递'
}

export default {
  name: 'MallAfterSale',
  data () {
    return {
      loading: false,
      list: [],
      status: undefined,
      dialog: false,
      current: null,
      statuses: [
        { value: 'PENDING_REVIEW', label: '待审核' }, { value: 'APPROVED', label: '已通过' },
        { value: 'RETURN_SHIPPED', label: '退货已寄出' }, { value: 'REFUNDING', label: '退款处理中' },
        { value: 'SUCCESS', label: '退款成功' }, { value: 'REJECTED', label: '已驳回' },
        { value: 'FAILED', label: '退款失败' }
      ]
    }
  },
  created () { this.load() },
  methods: {
    load () {
      this.loading = true
      listAfterSales({ status: this.status }).then(response => { this.list = response.data || [] }).finally(() => { this.loading = false })
    },
    showDetail (row) {
      getAfterSale(row.afterSaleId).then(response => { this.current = response.data; this.dialog = true })
    },
    approve (row) {
      this.$modal.confirm('确认通过该售后申请？').then(() => approveAfterSale(row.afterSaleId)).then(() => {
        this.$modal.msgSuccess('审核通过')
        this.load()
      })
    },
    reject (row) {
      this.$prompt('请输入驳回原因', '驳回售后', { inputPattern: /\S+/, inputErrorMessage: '原因不能为空' })
        .then(({ value }) => rejectAfterSale(row.afterSaleId, value)).then(() => {
          this.$modal.msgSuccess('已驳回')
          this.load()
        })
    },
    refund (row) {
      const message = row.type === 'RETURN_REFUND'
        ? '请确认已收到退货。确认后将按服务端金额发起退款，是否继续？'
        : '确认按服务端计算金额发起退款？'
      this.$modal.confirm(message).then(() => refundAfterSale(row.afterSaleId)).then(() => {
        this.$modal.msgSuccess('退款结果已处理')
        this.load()
      })
    },
    canRefund (row) {
      return (row.type === 'ONLY_REFUND' && ['APPROVED', 'REFUNDING'].includes(row.status)) ||
        (row.type === 'RETURN_REFUND' && ['RETURN_SHIPPED', 'REFUNDING'].includes(row.status))
    },
    refundActionLabel (row) { return row.type === 'RETURN_REFUND' ? '确认收货并退款' : '发起退款' },
    typeLabel (type) { return type === 'RETURN_REFUND' ? '退货退款' : '仅退款' },
    statusLabel (status) { return (this.statuses.find(item => item.value === status) || {}).label || status || '-' },
    riskLabel (risk) { return risk === 'MANUAL_REVIEW' ? '建议人工复核' : (risk || '无') },
    companyName (code) { return logisticsCompanies[code] || code || '-' },
    refundTotal (row) { return (Number(row.refundAmount || 0) + Number(row.shippingRefundAmount || 0)).toFixed(2) }
  }
}
</script>

<style scoped>
.evidence-value { overflow-wrap: anywhere; }
</style>
