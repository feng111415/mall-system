<template>
  <div class="app-container member-page">
    <el-form :inline="true" size="small" :model="query" class="query-form">
      <el-form-item label="手机号">
        <el-input v-model="query.phone" clearable placeholder="输入完整手机号" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="query.nickname" clearable placeholder="输入会员昵称" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column prop="memberId" label="会员ID" width="90" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="nickname" label="昵称" min-width="130" />
      <el-table-column prop="registerSource" label="注册来源" width="100" />
      <el-table-column prop="lastLoginIp" label="最近登录IP" min-width="130" />
      <el-table-column label="最近登录时间" width="165">
        <template slot-scope="scope">{{ parseTime(scope.row.lastLoginTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.status === '0' ? 'success' : 'info'">
            {{ scope.row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button v-hasPermi="['mall:member:query']" type="text" icon="el-icon-view" @click="openLifecycle(scope.row)">账号与隐私</el-button>
          <el-button v-hasPermi="['mall:member:edit']" type="text" icon="el-icon-edit" @click="openEdit(scope.row)">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" :page.sync="query.pageNum" :limit.sync="query.pageSize" @pagination="load" />

    <el-dialog title="修改会员" :visible.sync="dialog" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </div>
    </el-dialog>

    <el-drawer
      title="账号与隐私生命周期"
      :visible.sync="lifecycleVisible"
      :size="isMobile ? '100%' : '560px'"
      append-to-body
      destroy-on-close
    >
      <div v-loading="lifecycleLoading" class="lifecycle-drawer">
        <template v-if="account">
          <section class="account-summary">
            <div>
              <span class="section-label">会员身份</span>
              <h3>{{ account.nickname || '未设置昵称' }}</h3>
              <p>ID {{ account.memberId }} · {{ account.maskedPhone || '-' }}</p>
            </div>
            <el-tag :type="account.status === '0' ? 'success' : 'info'">
              {{ account.status === '0' ? '正常' : '已停用' }}
            </el-tag>
          </section>

          <section class="metric-grid">
            <div><span>在线设备</span><strong>{{ account.onlineDeviceCount || 0 }}</strong><small>当前有效会话</small></div>
            <div><span>未完成订单</span><strong>{{ eligibility.unfinishedOrderCount || 0 }}</strong><small>注销前须处理</small></div>
            <div><span>处理中售后</span><strong>{{ eligibility.inProgressAfterSaleCount || 0 }}</strong><small>注销前须处理</small></div>
          </section>

          <section class="drawer-section">
            <div class="section-heading">
              <h4>注销资格</h4>
              <el-tag size="mini" :type="eligibility.eligible ? 'success' : 'warning'">
                {{ eligibility.eligible ? '当前符合' : '暂不符合' }}
              </el-tag>
            </div>
            <p class="section-note">
              {{ eligibility.eligible ? '当前无未完成订单和处理中售后。后台仅供查看，不能代会员注销账号。' : '存在未完成业务，会员暂时不能发起账号注销。' }}
            </p>
          </section>

          <section class="drawer-section">
            <h4>授权记录</h4>
            <el-table v-if="account.consents && account.consents.length" :data="account.consents" size="mini" border>
              <el-table-column label="类型" min-width="120">
                <template slot-scope="scope">{{ consentLabel(scope.row.consentType) }}</template>
              </el-table-column>
              <el-table-column prop="consentVersion" label="版本" width="90" />
              <el-table-column label="同意时间" min-width="150">
                <template slot-scope="scope">{{ parseTime(scope.row.consentTime) || '-' }}</template>
              </el-table-column>
            </el-table>
            <el-empty v-else :image-size="72" description="暂无授权记录" />
          </section>

          <section class="drawer-section timeline-section">
            <h4>账号生命周期</h4>
            <el-timeline v-if="account.lifecycle && account.lifecycle.length">
              <el-timeline-item
                v-for="(event, index) in account.lifecycle"
                :key="event.eventType + '-' + event.eventTime + '-' + index"
                :timestamp="event.eventTime"
                placement="top"
                :type="eventType(event.eventType)"
              >
                <strong>{{ event.title }}</strong>
                <p>{{ event.summary || '无补充说明' }}</p>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else :image-size="72" description="暂无生命周期记录" />
          </section>

          <p class="audit-note">注册时间：{{ parseTime(account.registerTime) || '-' }}　最近登录：{{ parseTime(account.lastLoginTime) || '-' }}</p>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getMemberAccountLifecycle, listMembers, updateMember } from '@/api/mall/member'

export default {
  name: 'MallMember',
  data () {
    return {
      loading: false,
      list: [],
      total: 0,
      query: { pageNum: 1, pageSize: 10, phone: undefined, nickname: undefined },
      dialog: false,
      form: {},
      lifecycleVisible: false,
      lifecycleLoading: false,
      account: null
    }
  },
  computed: {
    isMobile () { return this.$store.state.app.device === 'mobile' },
    eligibility () {
      return this.account && this.account.cancellationEligibility
        ? this.account.cancellationEligibility
        : { unfinishedOrderCount: 0, inProgressAfterSaleCount: 0, eligible: false }
    }
  },
  created () { this.load() },
  methods: {
    load () {
      this.loading = true
      listMembers(this.query).then(response => {
        this.list = response.rows || (response.data && response.data.rows) || []
        this.total = response.total || (response.data && response.data.total) || this.list.length
      }).finally(() => { this.loading = false })
    },
    handleQuery () { this.query.pageNum = 1; this.load() },
    resetQuery () {
      this.query.phone = undefined
      this.query.nickname = undefined
      this.query.pageNum = 1
      this.load()
    },
    openEdit (row) { this.form = Object.assign({}, row); this.dialog = true },
    save () {
      updateMember(this.form).then(() => {
        this.$modal.msgSuccess('保存成功')
        this.dialog = false
        this.load()
      })
    },
    openLifecycle (row) {
      this.account = null
      this.lifecycleVisible = true
      this.lifecycleLoading = true
      getMemberAccountLifecycle(row.memberId).then(response => {
        this.account = response.data || null
      }).catch(() => {
        this.lifecycleVisible = false
      }).finally(() => { this.lifecycleLoading = false })
    },
    consentLabel (value) {
      return ({ USER_AGREEMENT: '用户协议', PRIVACY_POLICY: '隐私政策' })[value] || value || '-'
    },
    eventType (value) {
      if (value === 'ACCOUNT_DEACTIVATED') return 'danger'
      if (value === 'ACCOUNT_CANCELLATION_BLOCKED') return 'warning'
      if (value === 'PHONE_CHANGED') return 'success'
      return 'primary'
    }
  }
}
</script>

<style scoped>
.query-form { margin-bottom: 4px; }
.lifecycle-drawer { min-height: 240px; padding: 0 24px 28px; color: #303133; }
.account-summary { display: flex; align-items: flex-start; justify-content: space-between; padding: 4px 0 20px; border-bottom: 1px solid #ebeef5; }
.account-summary h3 { margin: 6px 0; font-size: 20px; letter-spacing: 0; }
.account-summary p, .section-note, .audit-note { margin: 0; color: #606266; font-size: 13px; line-height: 1.7; }
.section-label { color: #909399; font-size: 12px; }
.metric-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); margin: 20px 0; border: 1px solid #ebeef5; }
.metric-grid div { min-width: 0; padding: 14px; border-right: 1px solid #ebeef5; }
.metric-grid div:last-child { border-right: 0; }
.metric-grid span, .metric-grid small { display: block; color: #909399; font-size: 12px; }
.metric-grid strong { display: block; margin: 8px 0 5px; font-size: 22px; }
.drawer-section { margin-top: 24px; }
.drawer-section h4 { margin: 0 0 12px; font-size: 15px; letter-spacing: 0; }
.section-heading { display: flex; align-items: center; justify-content: space-between; }
.section-heading h4 { margin-bottom: 0; }
.timeline-section strong { font-size: 14px; }
.timeline-section p { margin: 5px 0 0; color: #606266; font-size: 13px; line-height: 1.6; }
.audit-note { margin-top: 22px; padding-top: 16px; border-top: 1px solid #ebeef5; }
@media (max-width: 767px) {
  .lifecycle-drawer { padding: 0 16px 24px; }
  .metric-grid { grid-template-columns: 1fr; }
  .metric-grid div { border-right: 0; border-bottom: 1px solid #ebeef5; }
  .metric-grid div:last-child { border-bottom: 0; }
}
</style>
