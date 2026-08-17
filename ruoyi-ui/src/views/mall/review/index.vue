<template>
  <div class="app-container review-admin-page">
    <section class="page-heading"><div><p class="eyebrow">MALL REVIEW CENTER · V0.5</p><h1>商品评价审核</h1><p>确认收货后的订单项评价先进入审核，审核通过后才会展示在商品详情。</p></div></section>
    <el-form ref="queryForm" :inline="true" :model="query" size="small" class="query-form">
      <el-form-item label="评价内容"><el-input v-model="query.keyword" clearable placeholder="商品、订单号或评价内容" @keyup.enter.native="handleQuery" /></el-form-item>
      <el-form-item label="状态"><el-select v-model="query.status" clearable placeholder="全部状态"><el-option label="待审核" value="PENDING" /><el-option label="已公开" value="PUBLISHED" /><el-option label="已驳回" value="REJECTED" /></el-select></el-form-item>
      <el-form-item><el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button><el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button></el-form-item>
    </el-form>
    <section class="summary-band"><div><span>当前页</span><strong>{{ rows.length }}</strong></div><div><span>待审核</span><strong>{{ countStatus('PENDING') }}</strong></div><div><span>已公开</span><strong>{{ countStatus('PUBLISHED') }}</strong></div><div><span>已驳回</span><strong>{{ countStatus('REJECTED') }}</strong></div></section>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column label="商品评价" min-width="250" fixed="left"><template slot-scope="scope"><div class="review-product"><img :src="imageUrl(scope.row.productImage)" alt="商品图片"><div><strong>{{ scope.row.productName }}</strong><small>{{ scope.row.skuName || '默认规格' }}</small><small>订单 {{ scope.row.orderNo }}</small></div></div></template></el-table-column>
      <el-table-column label="评分" width="120"><template slot-scope="scope"><span class="review-stars">{{ stars(scope.row.rating) }}</span></template></el-table-column>
      <el-table-column prop="content" label="评价内容" min-width="260" show-overflow-tooltip />
      <el-table-column prop="reviewerName" label="会员" width="120" />
      <el-table-column label="状态" width="100"><template slot-scope="scope"><el-tag size="mini" :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="165" />
      <el-table-column label="操作" width="210" fixed="right"><template slot-scope="scope"><el-button v-hasPermi="['mall:review:query']" type="text" icon="el-icon-view" @click="showDetail(scope.row)">详情</el-button><el-button v-if="scope.row.status === 'PENDING'" v-hasPermi="['mall:review:audit']" type="text" icon="el-icon-check" @click="publish(scope.row)">通过</el-button><el-button v-if="scope.row.status === 'PENDING'" v-hasPermi="['mall:review:audit']" type="text" class="danger-link" icon="el-icon-close" @click="reject(scope.row)">驳回</el-button></template></el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="load" />

    <el-dialog title="评价详情" :visible.sync="dialog" width="680px" append-to-body>
      <template v-if="current">
        <el-descriptions :column="2" border size="small"><el-descriptions-item label="商品">{{ current.productName }}</el-descriptions-item><el-descriptions-item label="规格">{{ current.skuName || '-' }}</el-descriptions-item><el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item><el-descriptions-item label="会员">{{ current.reviewerName || '匿名用户' }}</el-descriptions-item><el-descriptions-item label="评分"><span class="review-stars">{{ stars(current.rating) }}</span></el-descriptions-item><el-descriptions-item label="状态">{{ statusLabel(current.status) }}</el-descriptions-item><el-descriptions-item label="提交时间">{{ current.createTime }}</el-descriptions-item><el-descriptions-item label="审核人">{{ current.auditBy || '-' }}</el-descriptions-item></el-descriptions>
        <div class="review-content"><h4>评价内容</h4><p>{{ current.content }}</p><div v-if="current.imageUrls && current.imageUrls.length" class="review-images"><el-image v-for="image in current.imageUrls" :key="image" :src="imageUrl(image)" :preview-src-list="current.imageUrls.map(imageUrl)" fit="cover" /></div></div>
        <p v-if="current.auditRemark" class="audit-remark">审核说明：{{ current.auditRemark }}</p>
      </template>
      <div slot="footer"><el-button @click="dialog = false">关闭</el-button><template v-if="current && current.status === 'PENDING'"><el-button v-hasPermi="['mall:review:audit']" type="danger" plain @click="reject(current)">驳回</el-button><el-button v-hasPermi="['mall:review:audit']" type="primary" @click="publish(current)">通过并公开</el-button></template></div>
    </el-dialog>
  </div>
</template>

<script>
import { auditProductReview, getProductReview, listProductReviews } from '@/api/mall/review'

export default {
  name: 'MallProductReviewManage',
  data () { return { loading: false, rows: [], total: 0, dialog: false, current: null, query: { pageNum: 1, pageSize: 10, keyword: undefined, status: undefined } } },
  created () { this.load() },
  methods: {
    load () { this.loading = true; listProductReviews(this.query).then(response => { this.rows = response.rows || []; this.total = response.total || 0 }).finally(() => { this.loading = false }) },
    handleQuery () { this.query.pageNum = 1; this.load() },
    resetQuery () { this.query.keyword = undefined; this.query.status = undefined; this.handleQuery() },
    showDetail (row) { getProductReview(row.reviewId).then(response => { this.current = response.data; this.dialog = true }) },
    publish (row) { this.$modal.confirm('审核通过后该评价会在商品详情公开，继续吗？').then(() => auditProductReview(row.reviewId, { status: 'PUBLISHED' })).then(() => { this.$modal.msgSuccess('评价已公开'); this.dialog = false; this.load() }).catch(() => {}) },
    reject (row) { this.$prompt('请输入驳回原因', '驳回评价', { inputPattern: /\S+/, inputErrorMessage: '原因不能为空' }).then(({ value }) => auditProductReview(row.reviewId, { status: 'REJECTED', remark: value })).then(() => { this.$modal.msgSuccess('评价已驳回'); this.dialog = false; this.load() }).catch(() => {}) },
    countStatus (value) { return this.rows.filter(row => row.status === value).length },
    statusLabel (value) { return ({ PENDING: '待审核', PUBLISHED: '已公开', REJECTED: '已驳回' })[value] || value || '-' },
    statusType (value) { return ({ PUBLISHED: 'success', REJECTED: 'info' })[value] || 'warning' },
    stars (value) { return '★'.repeat(Number(value || 0)) + '☆'.repeat(Math.max(0, 5 - Number(value || 0))) },
    imageUrl (value) { if (!value) return '/assets/chair.jpg'; if (/^https?:\/\//.test(value) || value.startsWith('/assets/')) return value; return `${process.env.VUE_APP_BASE_API || ''}${value}` }
  }
}
</script>

<style scoped>
.review-admin-page{min-height:calc(100vh - 84px);background:#f5f7fa;color:#303744}.page-heading{display:flex;align-items:flex-end;margin-bottom:16px;padding:20px 22px;border:1px solid #e3e8ef;border-left:4px solid #409eff;border-radius:6px;background:#fff}.page-heading h1{margin:4px 0 8px;color:#202b3c;font-size:24px}.page-heading p{margin:0;color:#748094;font-size:13px;line-height:1.6}.eyebrow{color:#2979c7!important;font-size:10px!important;font-weight:700;letter-spacing:.12em}.query-form{padding:16px 16px 4px;border:1px solid #e3e8ef;border-radius:5px;background:#fff}.query-form .el-input{width:250px}.query-form .el-select{width:135px}.summary-band{display:grid;grid-template-columns:repeat(4,1fr);margin:14px 0;border:1px solid #dfe5ed;border-radius:5px;background:#fff}.summary-band div{min-height:64px;padding:12px 16px;border-right:1px solid #e8ecf1}.summary-band div:last-child{border-right:0}.summary-band span,.summary-band strong{display:block}.summary-band span{color:#8792a3;font-size:12px}.summary-band strong{margin-top:7px;color:#24334a;font-size:19px}.review-product{display:flex;align-items:center;gap:10px}.review-product img{width:48px;height:48px;object-fit:cover;border:1px solid #e3e8ef}.review-product div{display:grid;gap:3px;min-width:0}.review-product strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.review-product small{color:#8792a3;font-size:11px}.review-stars{color:#ee9b24;letter-spacing:1px;white-space:nowrap}.review-content{margin-top:18px;padding:16px;background:#fafbfc}.review-content h4{margin:0 0 8px}.review-content p{margin:0;color:#4b5563;line-height:1.7;white-space:pre-wrap}.review-images{display:flex;gap:8px;margin-top:14px;flex-wrap:wrap}.review-images .el-image{width:82px;height:82px;border:1px solid #e3e8ef}.audit-remark{color:#909399;font-size:12px}@media(max-width:760px){.summary-band{grid-template-columns:repeat(2,1fr)}.summary-band div:nth-child(2){border-right:0}.query-form .el-input{width:180px}}
</style>
