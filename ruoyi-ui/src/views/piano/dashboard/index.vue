<template>
  <div class="piano-dashboard">
    <div class="piano-hero">
      <div>
        <div class="eyebrow">Today Studio</div>
        <h2>今日工作台</h2>
        <p>快速查看今天最需要处理的作业、点评和订正。</p>
      </div>
      <el-button type="primary" icon="el-icon-refresh" @click="getDashboard">刷新</el-button>
    </div>

    <div v-loading="loading">
      <div class="metric-grid">
        <div v-for="item in metrics" :key="item.label" class="metric-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
          <em>{{ item.hint }}</em>
        </div>
      </div>

      <el-row :gutter="16">
        <el-col :xs="24" :lg="12">
          <div class="panel">
            <div class="panel-head">
              <h3>待提交作业</h3>
              <span>最近 6 条</span>
            </div>
            <el-empty v-if="todoHomeworks.length === 0" description="暂无待提交作业" />
            <div v-for="item in todoHomeworks" :key="item.homeworkId" class="work-item">
              <div>
                <strong>{{ item.homeworkTitle }}</strong>
                <p>{{ item.studentName || '未知学生' }} · 截止 {{ parseTime(item.submitDeadline) || '未设置' }}</p>
              </div>
              <el-tag size="mini">待提交</el-tag>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="12">
          <div class="panel">
            <div class="panel-head">
              <h3>最近提交</h3>
              <span>最新提交动态</span>
            </div>
            <el-empty v-if="recentSubmissions.length === 0" description="暂无提交动态" />
            <div v-for="item in recentSubmissions" :key="item.submissionId" class="work-item">
              <div>
                <strong>{{ item.homeworkTitle || '未命名作业' }}</strong>
                <p>{{ item.studentName || '未知学生' }} · 第 {{ item.attemptNo }} 次 · {{ parseTime(item.submitTime) }}</p>
              </div>
              <el-tag size="mini" :type="item.reviewStatus === '1' ? 'success' : 'warning'">
                {{ item.reviewStatus === '1' ? '已点评' : '待点评' }}
              </el-tag>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { getPianoDashboard } from '@/api/piano/dashboard'

export default {
  name: 'PianoDashboard',
  data() {
    return {
      loading: false,
      dashboard: {
        studentCount: 0,
        weekHomeworkCount: 0,
        pendingReviewCount: 0,
        correctionCount: 0,
        overdueCount: 0,
        todoHomeworks: [],
        recentSubmissions: []
      }
    }
  },
  computed: {
    metrics() {
      return [
        { label: '在学学生', value: this.dashboard.studentCount || 0, hint: '当前可布置作业的学生' },
        { label: '本周作业', value: this.dashboard.weekHomeworkCount || 0, hint: '本周新布置数量' },
        { label: '待点评', value: this.dashboard.pendingReviewCount || 0, hint: '需要老师处理' },
        { label: '需订正', value: this.dashboard.correctionCount || 0, hint: '等待学生再次提交' },
        { label: '逾期未完成', value: this.dashboard.overdueCount || 0, hint: '建议优先跟进' }
      ]
    },
    todoHomeworks() {
      return this.dashboard.todoHomeworks || []
    },
    recentSubmissions() {
      return this.dashboard.recentSubmissions || []
    }
  },
  created() {
    this.getDashboard()
  },
  methods: {
    getDashboard() {
      this.loading = true
      getPianoDashboard().then(response => {
        this.dashboard = response.data || this.dashboard
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
.piano-dashboard {
  padding: 20px;
  background: #fff8fb;
  min-height: calc(100vh - 84px);
}
.piano-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #fff, #fff0f6);
  border: 1px solid #f4d8e6;
}
.piano-hero h2 {
  margin: 4px 0 8px;
  color: #3f3340;
  font-size: 26px;
  font-weight: 700;
}
.piano-hero p {
  margin: 0;
  color: #7d6a78;
}
.eyebrow {
  color: #b86b8b;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}
.metric-card {
  padding: 18px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #efd8e3;
  box-shadow: 0 8px 22px rgba(184, 107, 139, 0.08);
}
.metric-card span {
  display: block;
  color: #9a7287;
}
.metric-card strong {
  display: block;
  margin: 8px 0;
  color: #3f3340;
  font-size: 30px;
}
.metric-card em {
  color: #aa8b9d;
  font-style: normal;
  font-size: 12px;
}
.panel {
  min-height: 320px;
  padding: 18px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #efd8e3;
}
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.panel-head h3 {
  margin: 0;
  color: #3f3340;
}
.panel-head span {
  color: #aa8b9d;
}
.work-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 13px 0;
  border-bottom: 1px dashed #efd8e3;
}
.work-item strong {
  color: #423541;
}
.work-item p {
  margin: 6px 0 0;
  color: #8a7483;
}
</style>
