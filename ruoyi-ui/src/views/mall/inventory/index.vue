<template>
  <div class="app-container inventory-page">
    <el-form ref="queryForm" :model="query" :inline="true" size="small" class="query-form">
      <el-form-item label="商品名称" prop="productName"><el-input v-model="query.productName" clearable /></el-form-item>
      <el-form-item label="SKU 编码" prop="skuCode"><el-input v-model="query.skuCode" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="上架状态" prop="publishStatus"><el-select v-model="query.publishStatus" clearable placeholder="全部"><el-option label="已上架" value="1" /><el-option label="未上架" value="0" /></el-select></el-form-item>
      <el-form-item label="库存状态" prop="stockStatus"><el-select v-model="query.stockStatus" clearable placeholder="全部"><el-option label="库存正常" value="HEALTHY" /><el-option label="低库存" value="LOW_STOCK" /><el-option label="缺货" value="OUT_OF_STOCK" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="rows" border class="inventory-table">
      <el-table-column label="商品 / SKU" min-width="220" fixed="left"><template slot-scope="scope"><strong>{{ scope.row.productName }}</strong><small>{{ scope.row.skuCode }} · {{ scope.row.skuName || '-' }}</small></template></el-table-column>
      <el-table-column label="商品状态" width="95"><template slot-scope="scope"><el-tag size="mini" :type="scope.row.publishStatus === '1' ? 'success' : 'info'">{{ scope.row.publishStatus === '1' ? '已上架' : '未上架' }}</el-tag></template></el-table-column>
      <el-table-column prop="availableQuantity" label="可用库存" width="100" align="right" />
      <el-table-column prop="lockedQuantity" label="锁定库存" width="100" align="right" />
      <el-table-column prop="soldQuantity" label="已售数量" width="100" align="right" />
      <el-table-column prop="warningThreshold" label="预警阈值" width="100" align="right" />
      <el-table-column label="库存状态" width="105"><template slot-scope="scope"><el-tag size="mini" :type="stockTagType(scope.row.stockStatus)">{{ stockStatusLabel(scope.row.stockStatus) }}</el-tag></template></el-table-column>
      <el-table-column prop="updateTime" label="最近变更" width="160" />
      <el-table-column label="操作" width="180" fixed="right"><template slot-scope="scope"><el-button type="text" icon="el-icon-edit" v-hasPermi="['mall:inventory:adjust']" @click="openAdjust(scope.row)">调整</el-button><el-button type="text" icon="el-icon-tickets" v-hasPermi="['mall:inventory:query']" @click="openLogs(scope.row)">流水</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="load" />

    <el-dialog title="调整库存" :visible.sync="adjustVisible" :width="isMobile ? '100%' : '480px'" :fullscreen="isMobile" append-to-body>
      <el-form :model="adjustForm" label-width="96px">
        <el-form-item label="SKU"><span>{{ adjustForm.skuCode }} · {{ adjustForm.skuName || '-' }}</span></el-form-item>
        <el-form-item label="当前可用"><strong>{{ adjustForm.availableQuantity }}</strong></el-form-item>
        <el-form-item label="调整数量"><el-input-number v-model="adjustForm.delta" :controls="true" /></el-form-item>
        <el-form-item label="预警阈值"><el-input-number v-model="adjustForm.warningThreshold" :min="0" /></el-form-item>
        <el-form-item label="调整原因" required><el-input v-model.trim="adjustForm.reason" type="textarea" :rows="3" maxlength="255" show-word-limit /></el-form-item>
      </el-form>
      <div class="adjust-result">调整后可用库存：<strong>{{ adjustedAvailable }}</strong></div>
      <div slot="footer"><el-button @click="adjustVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="saveAdjust">确认调整</el-button></div>
    </el-dialog>

    <el-dialog title="库存流水" :visible.sync="logsVisible" :width="isMobile ? '100%' : '920px'" :fullscreen="isMobile" append-to-body>
      <el-table v-loading="logsLoading" :data="logs" border size="mini"><el-table-column label="类型" width="100"><template slot-scope="scope">{{ operationLabel(scope.row.operationType) }}</template></el-table-column><el-table-column prop="sourceNo" label="来源单号" min-width="150"/><el-table-column prop="availableChange" label="可用变更" width="90"/><el-table-column prop="lockedChange" label="锁定变更" width="90"/><el-table-column prop="soldChange" label="已售变更" width="90"/><el-table-column prop="availableAfter" label="变更后可用" width="105"/><el-table-column prop="reason" label="原因" min-width="170"/><el-table-column prop="operator" label="操作人" width="100"/><el-table-column prop="createTime" label="时间" width="155"/></el-table>
    </el-dialog>
  </div>
</template>

<script>
import { listStocks, listStockLogs, adjustStock } from '@/api/mall/inventory'

export default {
  name: 'MallInventory',
  data () { return { loading: false, saving: false, logsLoading: false, rows: [], total: 0, query: { pageNum: 1, pageSize: 10, productName: undefined, skuCode: undefined, publishStatus: undefined, stockStatus: undefined }, adjustVisible: false, logsVisible: false, adjustForm: {}, logs: [] } },
  computed: { isMobile () { return this.$store.state.app.device === 'mobile' }, adjustedAvailable () { return Number(this.adjustForm.availableQuantity || 0) + Number(this.adjustForm.delta || 0) } },
  created () { if (this.$route.query.skuCode) this.query.skuCode = this.$route.query.skuCode; this.load() },
  methods: {
    load () { this.loading = true; listStocks(this.query).then(response => { this.rows = response.rows || []; this.total = response.total || 0 }).finally(() => { this.loading = false }) },
    handleQuery () { this.query.pageNum = 1; this.load() },
    resetQuery () { this.$refs.queryForm.resetFields(); this.query.pageNum = 1; this.load() },
    openAdjust (row) { this.adjustForm = { skuId: row.skuId, skuCode: row.skuCode, skuName: row.skuName, availableQuantity: row.availableQuantity, delta: 0, warningThreshold: row.warningThreshold, originalWarningThreshold: row.warningThreshold, reason: '' }; this.adjustVisible = true },
    saveAdjust () { if (!this.adjustForm.reason) return this.$modal.msgError('请填写调整原因'); if (!this.adjustForm.delta && this.adjustForm.warningThreshold === this.adjustForm.originalWarningThreshold) return this.$modal.msgError('库存数量和预警阈值均未变化'); if (this.adjustedAvailable < 0) return this.$modal.msgError('调整后可用库存不能小于 0'); this.saving = true; adjustStock(this.adjustForm.skuId, { delta: this.adjustForm.delta, warningThreshold: this.adjustForm.warningThreshold, reason: this.adjustForm.reason }).then(() => { this.$modal.msgSuccess('库存已调整'); this.adjustVisible = false; this.load() }).finally(() => { this.saving = false }) },
    openLogs (row) { this.logsVisible = true; this.logsLoading = true; this.logs = []; listStockLogs(row.skuId).then(response => { this.logs = response.data || [] }).finally(() => { this.logsLoading = false }) },
    stockStatusLabel (value) { return ({ HEALTHY: '库存正常', LOW_STOCK: '低库存', OUT_OF_STOCK: '缺货' })[value] || value || '-' },
    stockTagType (value) { return ({ HEALTHY: 'success', LOW_STOCK: 'warning', OUT_OF_STOCK: 'danger' })[value] || 'info' },
    operationLabel (value) { return ({ ADJUST: '人工调整', RESERVE: '订单锁定', RELEASE: '订单释放', CONFIRM: '销售扣减' })[value] || value || '-' }
  }
}
</script>

<style scoped>
.inventory-page { min-height: calc(100vh - 84px); background: #f5f7fa; }
.query-form { margin-bottom: 14px; padding: 16px 16px 4px; border: 1px solid #e3e8ef; border-radius: 5px; background: #fff; }
.query-form .el-input { width: 170px; }
.query-form .el-select { width: 130px; }
.inventory-table small { display: block; margin-top: 4px; color: #8b96a6; font-size: 11px; }
.adjust-result { margin-top: 8px; padding: 10px 12px; border-radius: 4px; background: #f4f7fb; color: #607087; font-size: 12px; }
.adjust-result strong { color: #25344b; font-size: 16px; }
@media (max-width: 900px) { .query-form .el-form-item, .query-form .el-input, .query-form .el-select { width: 100%; } }
</style>
