<template>
  <div class="app-container product-inventory-page">
    <section class="page-heading">
      <div>
        <p class="eyebrow">MALL OPERATIONS / PRODUCT &amp; INVENTORY</p>
        <h1>商品与库存运营中心</h1>
        <p>统一查看商品状态、SKU 库存和库存风险，具体维护仍由商品与库存原模块负责。</p>
      </div>
      <div class="heading-note"><i class="el-icon-view" /> 只读运营视图</div>
    </section>

    <el-form ref="queryForm" :model="query" :inline="true" size="small" class="query-form">
      <el-form-item label="商品名称" prop="productKeyword"><el-input v-model="query.productKeyword" clearable placeholder="商品名称或副标题" /></el-form-item>
      <el-form-item label="SPU 编码" prop="spuCode"><el-input v-model="query.spuCode" clearable /></el-form-item>
      <el-form-item label="SKU 编码" prop="skuCode"><el-input v-model="query.skuCode" clearable /></el-form-item>
      <el-form-item label="上架状态" prop="publishStatus">
        <el-select v-model="query.publishStatus" clearable placeholder="全部"><el-option label="已上架" value="1" /><el-option label="未上架" value="0" /></el-select>
      </el-form-item>
      <el-form-item label="库存状态" prop="stockStatus">
        <el-select v-model="query.stockStatus" clearable placeholder="全部"><el-option v-for="item in stockStatuses" :key="item.value" :label="item.label" :value="item.value" /></el-select>
      </el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <section class="summary-band" aria-label="当前页库存概览">
      <div><span>当前页商品</span><strong>{{ rows.length }}</strong></div>
      <div><span>可用库存</span><strong>{{ pageAvailable }}</strong></div>
      <div><span>锁定库存</span><strong>{{ pageLocked }}</strong></div>
      <div><span>风险商品</span><strong>{{ pageRisk }}</strong></div>
    </section>

    <el-table v-loading="loading" :data="rows" border class="inventory-table">
      <el-table-column label="商品" min-width="220" fixed="left">
        <template slot-scope="scope"><strong>{{ scope.row.productName }}</strong><small>{{ scope.row.spuCode }} · {{ scope.row.categoryName || '-' }}</small></template>
      </el-table-column>
      <el-table-column label="品牌" min-width="110"><template slot-scope="scope">{{ scope.row.brandName || '-' }}</template></el-table-column>
      <el-table-column label="上架" width="85"><template slot-scope="scope"><el-tag size="mini" :type="scope.row.publishStatus === '1' ? 'success' : 'info'">{{ scope.row.publishStatus === '1' ? '已上架' : '未上架' }}</el-tag></template></el-table-column>
      <el-table-column prop="skuCount" label="SKU" width="75" align="center" />
      <el-table-column prop="availableQuantity" label="可用" width="85" align="right" />
      <el-table-column prop="lockedQuantity" label="锁定" width="85" align="right" />
      <el-table-column prop="soldQuantity" label="已售" width="85" align="right" />
      <el-table-column label="库存状态" width="110"><template slot-scope="scope"><el-tag size="mini" :type="stockTagType(scope.row.stockStatus)">{{ stockStatusLabel(scope.row.stockStatus) }}</el-tag><small v-if="scope.row.outOfStockSkuCount">缺货 {{ scope.row.outOfStockSkuCount }} 个 SKU</small><small v-else-if="scope.row.lowStockSkuCount">低库存 {{ scope.row.lowStockSkuCount }} 个 SKU</small></template></el-table-column>
      <el-table-column prop="updateTime" label="最近变更" width="160" />
      <el-table-column label="操作" width="90" fixed="right" align="center"><template slot-scope="scope"><el-button type="text" icon="el-icon-view" v-hasPermi="['mall:product-inventory:query']" @click="openDetail(scope.row)">详情</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="load" />

    <el-dialog title="商品库存详情" :visible.sync="detailVisible" :width="isMobile ? '100%' : '1000px'" :fullscreen="isMobile" append-to-body>
      <div v-loading="detailLoading" class="detail-body" v-if="detail">
        <header class="detail-heading"><div><strong>{{ detail.productName }}</strong><small>{{ detail.spuCode }} · {{ detail.categoryName || '-' }} · {{ detail.brandName || '-' }}</small></div><el-tag :type="stockTagType(detail.stockStatus)">{{ stockStatusLabel(detail.stockStatus) }}</el-tag></header>
        <el-descriptions :column="isMobile ? 1 : 4" border size="small" class="detail-descriptions"><el-descriptions-item label="上架状态">{{ detail.publishStatus === '1' ? '已上架' : '未上架' }}</el-descriptions-item><el-descriptions-item label="SKU 数量">{{ detail.skuCount }}</el-descriptions-item><el-descriptions-item label="可用库存">{{ detail.availableQuantity }}</el-descriptions-item><el-descriptions-item label="锁定库存">{{ detail.lockedQuantity }}</el-descriptions-item><el-descriptions-item label="已售数量">{{ detail.soldQuantity }}</el-descriptions-item><el-descriptions-item label="低库存 SKU">{{ detail.lowStockSkuCount }}</el-descriptions-item><el-descriptions-item label="缺货 SKU">{{ detail.outOfStockSkuCount }}</el-descriptions-item></el-descriptions>
        <el-table :data="detail.skuList || []" border size="mini" class="sku-table"><el-table-column prop="skuCode" label="SKU 编码" min-width="130"/><el-table-column prop="skuName" label="规格" min-width="130"/><el-table-column prop="price" label="售价" width="90"/><el-table-column prop="availableQuantity" label="可用" width="75"/><el-table-column prop="lockedQuantity" label="锁定" width="75"/><el-table-column prop="soldQuantity" label="已售" width="75"/><el-table-column prop="warningThreshold" label="预警阈值" width="95"/><el-table-column label="状态" width="100"><template slot-scope="scope"><el-tag size="mini" :type="stockTagType(scope.row.stockStatus)">{{ stockStatusLabel(scope.row.stockStatus) }}</el-tag></template></el-table-column><el-table-column label="流水" width="75" align="center"><template slot-scope="scope"><el-button type="text" icon="el-icon-tickets" @click="openLogs(scope.row)">查看</el-button></template></el-table-column></el-table>
      </div>
    </el-dialog>

    <el-dialog title="库存流水" :visible.sync="logsVisible" width="900px" append-to-body><el-table v-loading="logsLoading" :data="logs" border size="mini"><el-table-column prop="operationType" label="类型" width="100"/><el-table-column prop="sourceType" label="来源" width="90"/><el-table-column prop="sourceNo" label="来源单号" min-width="150"/><el-table-column prop="availableChange" label="可用变更" width="90"/><el-table-column prop="lockedChange" label="锁定变更" width="90"/><el-table-column prop="soldChange" label="已售变更" width="90"/><el-table-column prop="reason" label="原因" min-width="160"/><el-table-column prop="operator" label="操作人" width="100"/><el-table-column prop="createTime" label="时间" width="155"/></el-table></el-dialog>
  </div>
</template>

<script>
import { listProductInventory, getProductInventoryDetail } from '@/api/mall/productInventory'
import { listStockLogs } from '@/api/mall/inventory'

export default {
  name: 'MallProductInventoryCenter',
  data () {
    return {
      loading: false, detailLoading: false, logsLoading: false, detailVisible: false, logsVisible: false,
      rows: [], total: 0, detail: null, logs: [],
      query: { pageNum: 1, pageSize: 10, productKeyword: undefined, spuCode: undefined, skuCode: undefined, publishStatus: undefined, stockStatus: undefined },
      stockStatuses: [{ value: 'HEALTHY', label: '库存正常' }, { value: 'LOW_STOCK', label: '低库存' }, { value: 'OUT_OF_STOCK', label: '缺货' }]
    }
  },
  computed: {
    isMobile () { return this.$store.state.app.device === 'mobile' },
    pageAvailable () { return this.rows.reduce((sum, row) => sum + Number(row.availableQuantity || 0), 0) },
    pageLocked () { return this.rows.reduce((sum, row) => sum + Number(row.lockedQuantity || 0), 0) },
    pageRisk () { return this.rows.filter(row => row.stockStatus !== 'HEALTHY').length }
  },
  created () { this.load() },
  methods: {
    load () { this.loading = true; listProductInventory(this.query).then(response => { this.rows = response.rows || []; this.total = response.total || 0 }).finally(() => { this.loading = false }) },
    handleQuery () { this.query.pageNum = 1; this.load() },
    resetQuery () { this.$refs.queryForm.resetFields(); this.query.pageNum = 1; this.load() },
    openDetail (row) { this.detailVisible = true; this.detailLoading = true; this.detail = null; getProductInventoryDetail(row.spuId).then(response => { this.detail = response.data }).finally(() => { this.detailLoading = false }) },
    openLogs (row) { this.logsVisible = true; this.logsLoading = true; this.logs = []; listStockLogs(row.skuId).then(response => { this.logs = response.data || [] }).finally(() => { this.logsLoading = false }) },
    stockStatusLabel (value) { return ({ HEALTHY: '库存正常', LOW_STOCK: '低库存', OUT_OF_STOCK: '缺货' })[value] || value || '-' },
    stockTagType (value) { return ({ HEALTHY: 'success', LOW_STOCK: 'warning', OUT_OF_STOCK: 'danger' })[value] || 'info' }
  }
}
</script>

<style scoped>
.product-inventory-page { min-height: calc(100vh - 84px); background: #f5f7fa; color: #303744; }
.page-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; margin-bottom: 16px; padding: 20px 22px; border: 1px solid #e3e8ef; border-left: 4px solid #409eff; border-radius: 6px; background: #fff; }
.page-heading h1 { margin: 4px 0 8px; color: #202b3c; font-size: 24px; }
.page-heading p { margin: 0; color: #748094; font-size: 13px; line-height: 1.6; }
.page-heading .eyebrow { color: #2979c7; font-size: 10px; font-weight: 700; letter-spacing: .12em; }
.heading-note { padding: 8px 12px; border-radius: 4px; background: #eef6ff; color: #3575ab; font-size: 12px; white-space: nowrap; }
.query-form { padding: 16px 16px 4px; border: 1px solid #e3e8ef; border-radius: 5px; background: #fff; }
.query-form .el-input { width: 170px; }
.query-form .el-select { width: 130px; }
.summary-band { display: grid; grid-template-columns: repeat(4, minmax(130px, 1fr)); margin: 14px 0; border: 1px solid #dfe5ed; border-radius: 5px; background: #fff; }
.summary-band div { min-height: 64px; padding: 12px 16px; border-right: 1px solid #e8ecf1; }
.summary-band div:last-child { border-right: 0; }
.summary-band span, .summary-band strong { display: block; }
.summary-band span { color: #8792a3; font-size: 12px; }
.summary-band strong { margin-top: 7px; color: #24334a; font-size: 19px; }
.inventory-table small, .detail-heading small { display: block; margin-top: 4px; color: #8b96a6; font-size: 11px; line-height: 1.4; }
.inventory-table .el-tag + small { margin-top: 3px; }
.detail-body { min-height: 300px; }
.detail-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; padding: 12px 14px; border-left: 4px solid #409eff; background: #f4f8fd; }
.detail-heading strong { color: #21314a; font-size: 17px; }
.detail-descriptions { margin-bottom: 14px; }
.sku-table { width: 100%; }
@media (max-width: 900px) {
  .page-heading { align-items: stretch; flex-direction: column; }
  .heading-note { align-self: flex-start; }
  .summary-band { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .summary-band div:nth-child(2) { border-right: 0; }
  .summary-band div:nth-child(-n+2) { border-bottom: 1px solid #e8ecf1; }
  .query-form .el-form-item, .query-form .el-input, .query-form .el-select { width: 100%; }
}
</style>
