<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" @tab-click="handleTabClick">
      <el-tab-pane label="对账差异" name="diffs">
        <el-form v-show="showSearch" :model="diffQuery" ref="diffQuery" size="small" :inline="true" label-width="80px">
          <el-form-item label="业务类型" prop="diffType">
            <el-select v-model="diffQuery.diffType" clearable placeholder="全部类型">
              <el-option label="支付" value="PAYMENT"/><el-option label="退款" value="REFUND"/>
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态" prop="status">
            <el-select v-model="diffQuery.status" clearable placeholder="全部状态">
              <el-option label="待处理" value="OPEN"/><el-option label="人工处理" value="MANUAL"/>
              <el-option label="已解决" value="RESOLVED"/><el-option label="已忽略" value="IGNORED"/>
            </el-select>
          </el-form-item>
          <el-form-item label="业务单号" prop="businessNo"><el-input v-model="diffQuery.businessNo" clearable @keyup.enter.native="getDiffs"/></el-form-item>
          <el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="getDiffs">查询</el-button><el-button icon="el-icon-refresh" size="mini" @click="resetDiffs">重置</el-button></el-form-item>
        </el-form>
        <el-row :gutter="10" class="mb8"><el-col :span="1.5"><el-button type="primary" plain icon="el-icon-refresh" size="mini" @click="getDiffs">刷新</el-button></el-col><right-toolbar :showSearch.sync="showSearch" @queryTable="getDiffs"/></el-row>
        <el-table v-loading="loading" :data="diffList">
          <el-table-column label="差异编号" prop="diffNo" min-width="190"/>
          <el-table-column label="类型" prop="diffType" width="80"><template slot-scope="s">{{ s.row.diffType === 'PAYMENT' ? '支付' : '退款' }}</template></el-table-column>
          <el-table-column label="业务单号" prop="businessNo" min-width="150"/>
          <el-table-column label="本地状态" prop="localStatus" width="110"/>
          <el-table-column label="外部状态" prop="externalStatus" width="110"/>
          <el-table-column label="差异原因" prop="diffCode" min-width="160"/>
          <el-table-column label="处理状态" prop="status" width="100"><template slot-scope="s"><el-tag :type="statusType(s.row.status)">{{ statusText(s.row.status) }}</el-tag></template></el-table-column>
          <el-table-column label="创建时间" prop="createTime" width="165"/>
          <el-table-column label="操作" width="220" fixed="right"><template slot-scope="s"><el-button v-if="canHandle(s.row)" type="text" size="mini" icon="el-icon-circle-check" v-hasPermi="['mall:reconciliation:handle']" @click="ignore(s.row)">忽略</el-button><el-button v-if="canHandle(s.row)" type="text" size="mini" icon="el-icon-s-operation" v-hasPermi="['mall:reconciliation:handle']" @click="compensate(s.row)">创建补偿</el-button></template></el-table-column>
        </el-table>
        <pagination v-show="diffTotal > 0" :total="diffTotal" :page.sync="diffQuery.pageNum" :limit.sync="diffQuery.pageSize" @pagination="getDiffs"/>
      </el-tab-pane>
      <el-tab-pane label="告警中心" name="alerts">
        <el-form :model="alertQuery" size="small" :inline="true" label-width="80px"><el-form-item label="告警状态"><el-select v-model="alertQuery.status" clearable placeholder="全部状态"><el-option label="待确认" value="OPEN"/><el-option label="已确认" value="ACKED"/></el-select></el-form-item><el-form-item label="业务单号"><el-input v-model="alertQuery.businessNo" clearable @keyup.enter.native="getAlerts"/></el-form-item><el-form-item><el-button type="primary" icon="el-icon-search" size="mini" @click="getAlerts">查询</el-button></el-form-item></el-form>
        <el-table v-loading="alertLoading" :data="alertList"><el-table-column label="告警编号" prop="alertNo" min-width="190"/><el-table-column label="差异ID" prop="diffId" width="90"/><el-table-column label="级别" prop="alertLevel" width="90"><template slot-scope="s"><el-tag type="danger">{{ s.row.alertLevel }}</el-tag></template></el-table-column><el-table-column label="告警内容" prop="alertMessage" min-width="280" show-overflow-tooltip/><el-table-column label="状态" prop="status" width="90"><template slot-scope="s">{{ s.row.status === 'OPEN' ? '待确认' : '已确认' }}</template></el-table-column><el-table-column label="创建时间" prop="createTime" width="165"/><el-table-column label="操作" width="100"><template slot-scope="s"><el-button v-if="s.row.status === 'OPEN'" type="text" size="mini" icon="el-icon-check" v-hasPermi="['mall:reconciliation:alert:ack']" @click="ackAlert(s.row)">确认</el-button></template></el-table-column></el-table>
        <pagination v-show="alertTotal > 0" :total="alertTotal" :page.sync="alertQuery.pageNum" :limit.sync="alertQuery.pageSize" @pagination="getAlerts"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { listDiff, ignoreDiff, createCompensation, listAlerts, acknowledgeAlert } from '@/api/mall/reconciliation'
export default {
  name: 'MallReconciliation',
  data() { return { activeTab: 'diffs', showSearch: true, loading: false, alertLoading: false, diffList: [], alertList: [], diffTotal: 0, alertTotal: 0, diffQuery: { pageNum: 1, pageSize: 10, diffType: undefined, status: undefined, businessNo: undefined }, alertQuery: { pageNum: 1, pageSize: 10, status: undefined, businessNo: undefined } } },
  created() { this.initRouteQuery(); this.activeTab === 'alerts' ? this.getAlerts() : this.getDiffs() },
  methods: {
    initRouteQuery() { const tab = this.$route.query.tab; if (tab === 'alerts' || tab === 'diffs') this.activeTab = tab; const businessNo = this.$route.query.businessNo; if (businessNo) { this.diffQuery.businessNo = businessNo; this.alertQuery.businessNo = businessNo } },
    getDiffs() { this.loading = true; listDiff({ diffType: this.diffQuery.diffType, status: this.diffQuery.status, businessNo: this.diffQuery.businessNo, limit: this.diffQuery.pageSize, offset: (this.diffQuery.pageNum - 1) * this.diffQuery.pageSize }).then(r => { const d = r.data || r; this.diffList = d.rows || (Array.isArray(d) ? d : []); this.diffTotal = d.total || this.diffList.length }).finally(() => { this.loading = false }) },
    getAlerts() { this.alertLoading = true; listAlerts({ status: this.alertQuery.status, businessNo: this.alertQuery.businessNo, limit: this.alertQuery.pageSize, offset: (this.alertQuery.pageNum - 1) * this.alertQuery.pageSize }).then(r => { const d = r.data || r; this.alertList = d.rows || (Array.isArray(d) ? d : []); this.alertTotal = d.total || this.alertList.length }).finally(() => { this.alertLoading = false }) },
    handleTabClick() { if (this.activeTab === 'alerts' && !this.alertList.length) this.getAlerts() },
    resetDiffs() { this.$refs.diffQuery.resetFields(); this.diffQuery.pageNum = 1; this.getDiffs() },
    canHandle(row) { return ['OPEN', 'MANUAL'].indexOf(row.status) >= 0 },
    statusText(s) { return ({ OPEN: '待处理', MANUAL: '人工处理', RESOLVED: '已解决', IGNORED: '已忽略' })[s] || s },
    statusType(s) { return s === 'OPEN' ? 'danger' : s === 'RESOLVED' ? 'success' : s === 'IGNORED' ? 'info' : 'warning' },
    ignore(row) { this.$modal.prompt('请输入忽略原因', '处理对账差异').then(({ value }) => ignoreDiff(row.diffId, value).then(() => { this.$modal.msgSuccess('已忽略'); this.getDiffs() })).catch(() => {}) },
    compensate(row) { this.$modal.confirm('确认创建该业务的补偿任务吗？补偿任务仍会经过幂等校验。').then(() => createCompensation(row.diffId).then(() => { this.$modal.msgSuccess('补偿任务已创建'); this.getDiffs() })).catch(() => {}) },
    ackAlert(row) { this.$modal.confirm('确认已知悉该告警吗？').then(() => acknowledgeAlert(row.alertId).then(() => { this.$modal.msgSuccess('告警已确认'); this.getAlerts() })).catch(() => {}) }
  }
}
</script>
