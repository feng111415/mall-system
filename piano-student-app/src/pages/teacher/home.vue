<template>
  <view class="page-shell home-page">
    <view class="studio-head">
      <view>
        <view class="piano-page-title">今日工作台</view>
        <view class="piano-help-text">手机端完成琴行课后作业闭环。</view>
      </view>
      <button class="logout" @click="logout">退出</button>
    </view>

    <view class="stats">
      <view class="soft-card stat-card">
        <view class="stat-value">{{ studentCount }}</view>
        <view class="stat-label">学生</view>
      </view>
      <view class="soft-card stat-card">
        <view class="stat-value">{{ homeworkCount }}</view>
        <view class="stat-label">作业</view>
      </view>
      <view class="soft-card stat-card">
        <view class="stat-value urgent">{{ pendingCount }}</view>
        <view class="stat-label">待点评</view>
      </view>
    </view>

    <view class="actions">
      <view v-for="item in actions" :key="item.url" class="soft-card action-card" @click="go(item.url)">
        <view class="action-icon">{{ item.mark }}</view>
        <view class="action-copy">
          <view class="action-title">{{ item.title }}</view>
          <view class="action-desc">{{ item.desc }}</view>
        </view>
        <view class="action-arrow">›</view>
      </view>
    </view>
  </view>
</template>

<script>
import { clearTeacherCode, getTeacherHomeworks, getTeacherStudents, getTeacherSubmissions } from '../../api/piano'

export default {
  data() {
    return {
      studentCount: 0,
      homeworkCount: 0,
      pendingCount: 0,
      actions: [
        { title: '布置作业', desc: '选择学生，填写练习内容和截止时间', mark: '♪', url: '/pages/teacher/homework-form' },
        { title: '提交点评', desc: '看学生视频，评分或要求订正', mark: '✓', url: '/pages/teacher/submissions' },
        { title: '作业管理', desc: '复制提交码，查看进度和历史提交', mark: '⌕', url: '/pages/teacher/homeworks' },
        { title: '学生档案', desc: '维护学生、家长联系方式和学习阶段', mark: '◎', url: '/pages/teacher/students' }
      ]
    }
  },
  onShow() {
    this.loadStats()
  },
  methods: {
    loadStats() {
      getTeacherStudents().then(res => {
        this.studentCount = (res.data || []).length
      })
      getTeacherHomeworks().then(res => {
        this.homeworkCount = (res.data || []).length
      })
      getTeacherSubmissions({ reviewStatus: '0', latestFlag: '1' }).then(res => {
        this.pendingCount = (res.data || []).length
      })
    },
    go(url) {
      uni.navigateTo({ url })
    },
    logout() {
      clearTeacherCode()
      uni.redirectTo({ url: '/pages/teacher/login' })
    }
  }
}
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 28rpx;
  padding-bottom: 56rpx;
}

.studio-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20rpx;
  padding-top: 44rpx;
}

.logout {
  width: 120rpx;
  height: 58rpx;
  line-height: 58rpx;
  margin: 0;
  border-radius: 12rpx;
  color: #3a3a3a;
  background: rgba(255, 253, 253, 0.78);
  border: 1rpx solid rgba(216, 184, 194, 0.34);
  font-size: 23rpx;
  font-weight: 300;
}

.stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.stat-card {
  padding: 24rpx 10rpx;
  text-align: center;
}

.stat-value {
  color: #d8b8c2;
  font-size: 42rpx;
  font-weight: 200;
  line-height: 1.2;
}

.stat-value.urgent {
  color: #e2b8c0;
}

.stat-label {
  margin-top: 8rpx;
  color: rgba(153, 153, 153, 0.84);
  font-size: 22rpx;
  font-weight: 300;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx;
}

.action-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: 22rpx;
  color: #d8b8c2;
  background: rgba(216, 184, 194, 0.13);
  font-size: 30rpx;
  font-weight: 200;
}

.action-copy {
  flex: 1;
  min-width: 0;
}

.action-title {
  color: #3a3a3a;
  font-size: 26rpx;
  font-weight: 400;
}

.action-desc {
  margin-top: 8rpx;
  color: rgba(153, 153, 153, 0.84);
  font-size: 22rpx;
  font-weight: 300;
  line-height: 1.6;
}

.action-arrow {
  color: #d8b8c2;
  font-size: 40rpx;
  font-weight: 200;
}
</style>
