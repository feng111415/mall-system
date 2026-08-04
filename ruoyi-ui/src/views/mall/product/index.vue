<template>
  <div class="app-container product-page">
    <el-form ref="queryForm" :model="query" :inline="true" size="small" class="query-form">
      <el-form-item label="商品名称" prop="productName"><el-input v-model="query.productName" clearable @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="分类" prop="categoryId"><el-select v-model="query.categoryId" clearable placeholder="全部"><el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" /></el-select></el-form-item>
      <el-form-item label="品牌" prop="brandId"><el-select v-model="query.brandId" clearable placeholder="全部"><el-option v-for="item in brands" :key="item.brandId" :label="item.brandName" :value="item.brandId" /></el-select></el-form-item>
      <el-form-item label="上架状态" prop="publishStatus"><el-select v-model="query.publishStatus" clearable placeholder="全部"><el-option label="未上架" value="0" /><el-option label="已上架" value="1" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>

    <el-row class="mb8"><el-button type="primary" plain icon="el-icon-plus" size="mini" v-hasPermi="['mall:product:add']" @click="openAdd">新增商品</el-button><right-toolbar :showSearch.sync="showSearch" @queryTable="load" /></el-row>

    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="spuCode" label="SPU 编码" width="140" />
      <el-table-column label="商品" min-width="210"><template slot-scope="scope"><strong>{{ scope.row.productName }}</strong><small>{{ scope.row.subtitle || '-' }}</small></template></el-table-column>
      <el-table-column prop="categoryName" label="分类" min-width="100" />
      <el-table-column prop="brandName" label="品牌" min-width="100" />
      <el-table-column label="价格区间" width="150"><template slot-scope="scope">¥{{ money(scope.row.priceMin) }} - ¥{{ money(scope.row.priceMax) }}</template></el-table-column>
      <el-table-column label="状态" width="90"><template slot-scope="scope"><el-tag size="mini" :type="scope.row.publishStatus === '1' ? 'success' : 'info'">{{ scope.row.publishStatus === '1' ? '已上架' : '未上架' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="230" fixed="right"><template slot-scope="scope"><el-button type="text" icon="el-icon-edit" v-hasPermi="['mall:product:edit']" @click="openEdit(scope.row)">修改</el-button><el-button type="text" :icon="scope.row.publishStatus === '1' ? 'el-icon-bottom' : 'el-icon-top'" v-hasPermi="['mall:product:publish']" @click="togglePublish(scope.row)">{{ scope.row.publishStatus === '1' ? '下架' : '上架' }}</el-button><el-button type="text" icon="el-icon-delete" v-hasPermi="['mall:product:remove']" @click="removeProduct(scope.row)">删除</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="load" />

    <el-dialog :title="title" :visible.sync="dialogVisible" :width="isMobile ? '100%' : '860px'" :fullscreen="isMobile" append-to-body>
      <el-form ref="productForm" :model="form" label-width="96px" class="product-form">
        <el-row :gutter="14"><el-col :xs="24" :sm="12"><el-form-item label="商品名称" required><el-input v-model.trim="form.productName" /></el-form-item></el-col><el-col :xs="24" :sm="12"><el-form-item label="SPU 编码" required><el-input v-model.trim="form.spuCode" :disabled="Boolean(form.spuId)" /></el-form-item></el-col></el-row>
        <el-row :gutter="14"><el-col :xs="24" :sm="12"><el-form-item label="商品分类" required><el-select v-model="form.categoryId" filterable><el-option v-for="item in categories" :key="item.categoryId" :label="item.categoryName" :value="item.categoryId" /></el-select></el-form-item></el-col><el-col :xs="24" :sm="12"><el-form-item label="品牌"><el-select v-model="form.brandId" clearable filterable><el-option v-for="item in brands" :key="item.brandId" :label="item.brandName" :value="item.brandId" /></el-select></el-form-item></el-col></el-row>
        <el-form-item label="副标题"><el-input v-model.trim="form.subtitle" /></el-form-item>
        <el-form-item label="主图地址"><el-input v-model.trim="form.mainImage" /></el-form-item>
        <el-form-item label="详情内容"><el-input v-model="form.detailHtml" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="SKU 列表" required>
          <div class="sku-note"><i class="el-icon-info" /> 已有 SKU 库存只读，数量与预警阈值请由仓储履约在“库存管理”中调整。</div>
          <div v-for="(sku, index) in form.skuList" :key="sku.skuId || `new-${index}`" class="sku-row">
            <el-input v-model.trim="sku.skuCode" placeholder="SKU 编码" :disabled="Boolean(sku.skuId)" />
            <el-input v-model.trim="sku.skuName" placeholder="规格名称" />
            <el-input-number v-model="sku.price" :min="0" :precision="2" :controls="false" placeholder="售价" />
            <el-input v-model.trim="sku.specJson" placeholder='规格 JSON，如 {"颜色":"白色"}' />
            <el-input-number v-if="!form.spuId" v-model="sku.availableStock" :min="0" :controls="false" placeholder="初始库存" />
            <span v-else class="stock-readonly">库存 {{ sku.availableStock || 0 }}</span>
            <el-switch v-model="sku.status" active-value="1" inactive-value="0" active-text="启用" />
            <el-button type="text" icon="el-icon-delete" title="移除 SKU" @click="removeSku(index)" />
          </div>
          <el-button size="mini" plain icon="el-icon-plus" @click="addSku">添加 SKU</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer"><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { listProducts, getProduct, addProduct, updateProduct, publishProduct, delProduct, listCategories, listBrands } from '@/api/mall/catalog'

export default {
  name: 'MallProduct',
  data () { return { showSearch: true, loading: false, saving: false, rows: [], total: 0, categories: [], brands: [], query: { pageNum: 1, pageSize: 10, productName: undefined, categoryId: undefined, brandId: undefined, publishStatus: undefined }, dialogVisible: false, title: '', form: { skuList: [] } } },
  computed: { isMobile () { return this.$store.state.app.device === 'mobile' } },
  created () { this.loadOptions(); this.load() },
  methods: {
    loadOptions () { Promise.all([listCategories({ status: '0' }), listBrands({ status: '0' })]).then(([categoryResponse, brandResponse]) => { this.categories = categoryResponse.data || []; this.brands = brandResponse.data || [] }) },
    load () { this.loading = true; listProducts(this.query).then(response => { this.rows = response.rows || []; this.total = response.total || 0 }).finally(() => { this.loading = false }) },
    handleQuery () { this.query.pageNum = 1; this.load() },
    resetQuery () { this.$refs.queryForm.resetFields(); this.query.pageNum = 1; this.load() },
    emptySku () { return { skuCode: '', skuName: '', specJson: '', price: 0, marketPrice: 0, availableStock: 0, status: '1' } },
    openAdd () { this.title = '新增商品'; this.form = { publishStatus: '0', sortNo: 0, skuList: [this.emptySku()] }; this.dialogVisible = true },
    openEdit (row) { getProduct(row.spuId).then(response => { this.title = '修改商品'; this.form = response.data || {}; this.form.skuList = this.form.skuList || []; this.dialogVisible = true }) },
    addSku () { this.form.skuList.push(this.emptySku()) },
    removeSku (index) { if (this.form.skuList.length <= 1) return this.$modal.msgError('商品至少保留一个 SKU'); this.form.skuList.splice(index, 1) },
    save () { if (!this.form.productName || !this.form.spuCode || !this.form.categoryId || !this.form.skuList.length) return this.$modal.msgError('请完整填写商品和 SKU 信息'); if (this.form.skuList.some(item => !item.skuCode || item.price === null || item.price === undefined)) return this.$modal.msgError('请填写每个 SKU 的编码和售价'); this.saving = true; (this.form.spuId ? updateProduct : addProduct)(this.form).then(() => { this.$modal.msgSuccess('保存成功'); this.dialogVisible = false; this.load() }).finally(() => { this.saving = false }) },
    togglePublish (row) { const status = row.publishStatus === '1' ? '0' : '1'; publishProduct(row.spuId, status).then(() => { this.$modal.msgSuccess('状态已更新'); this.load() }) },
    removeProduct (row) { this.$modal.confirm('删除商品前会检查 SKU 的库存和履约记录，确认继续？').then(() => delProduct(row.spuId)).then(() => { this.$modal.msgSuccess('删除成功'); this.load() }).catch(() => {}) },
    money (value) { return Number(value || 0).toFixed(2) }
  }
}
</script>

<style scoped>
.product-page { min-height: calc(100vh - 84px); background: #f5f7fa; }
.query-form { padding: 16px 16px 4px; border: 1px solid #e3e8ef; border-radius: 5px; background: #fff; }
.query-form .el-input, .query-form .el-select { width: 160px; }
.el-table small { display: block; margin-top: 4px; color: #8b96a6; font-size: 11px; }
.product-form .el-select { width: 100%; }
.sku-note { margin-bottom: 10px; padding: 9px 11px; border-radius: 4px; background: #f0f7ff; color: #54708c; font-size: 12px; line-height: 1.5; }
.sku-row { display: grid; grid-template-columns: 135px 130px 100px minmax(160px, 1fr) 95px 90px 34px; gap: 8px; align-items: center; margin-bottom: 9px; }
.stock-readonly { color: #617086; font-size: 12px; text-align: center; }
@media (max-width: 900px) { .query-form .el-form-item, .query-form .el-input, .query-form .el-select { width: 100%; } .sku-row { grid-template-columns: 1fr; padding: 12px; border: 1px solid #e4e9f0; border-radius: 5px; } .stock-readonly { text-align: left; } }
</style>
