<template>
  <view class="page-shell detail-page">
    <view v-if="homework" class="content">
      <view class="soft-card summary-card">
        <view class="summary-top">
          <view>
            <view class="student-name">{{ homework.studentName || '同学' }}</view>
            <view class="homework-title">{{ homework.homeworkTitle }}</view>
          </view>
          <view class="status" :class="statusClass">{{ statusLabel }}</view>
        </view>
        <view class="meta-list">
          <view>提交码：{{ homework.submitCode }}</view>
          <view>截止：{{ homework.submitDeadline || '老师未设置' }}</view>
        </view>
      </view>

      <view class="soft-card section-card">
        <view class="piano-section-title">练习内容</view>
        <view class="section-text">{{ homework.practiceContent }}</view>
      </view>

      <view class="soft-card section-card">
        <view class="piano-section-title">练习要求</view>
        <view class="section-text muted">{{ homework.practiceRequirement || '老师暂未填写额外要求' }}</view>
      </view>

      <view v-if="homework.latestSubmission" class="soft-card section-card">
        <view class="piano-section-title">最近一次提交</view>
        <view class="history-item compact">
          <view class="history-head">
            <text>第 {{ homework.latestSubmission.attemptNo }} 次</text>
            <text>{{ homework.latestSubmission.submitTime }}</text>
          </view>
          <view class="section-text">{{ homework.latestSubmission.submitContent || '未填写说明' }}</view>
          <view v-if="homework.latestSubmission.attachmentUrl" class="link-text">
            {{ homework.latestSubmission.attachmentUrl }}
          </view>
          <view v-if="homework.latestSubmission.reviewStatus === '1'" class="review-box">
            <view class="review-title">老师点评</view>
            <view>评分：{{ homework.latestSubmission.score || '未评分' }}</view>
            <view>{{ homework.latestSubmission.reviewContent || '老师已点评' }}</view>
          </view>
        </view>
      </view>

      <button class="primary-button submit-button" @click="goSubmit">{{ submitButtonText }}</button>

      <view v-if="history.length" class="soft-card section-card">
        <view class="piano-section-title">提交历史</view>
        <view v-for="item in history" :key="item.submissionId" class="history-item">
          <view class="history-head">
            <text>第 {{ item.attemptNo }} 次</text>
            <text>{{ item.submitTime }}</text>
          </view>
          <view class="section-text">{{ item.submitContent || '未填写说明' }}</view>
          <view v-if="item.attachmentUrl" class="link-text">{{ item.attachmentUrl }}</view>
          <view v-if="item.reviewStatus === '1'" class="review-box">
            <view class="review-title">老师点评</view>
            <view>评分：{{ item.score || '未评分' }}</view>
            <view>{{ item.reviewContent || '老师已点评' }}</view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getHomework } from '../../api/piano'

const statusMap = {
  0: '待提交',
  1: '待点评',
  2: '需订正',
  3: '已完成',
  4: '已逾期'
}

export default {
  data() {
    return {
      submitCode: '',
      homework: null
    }
  },
  computed: {
    history() {
      return this.homework && this.homework.submissionHistory ? this.homework.submissionHistory : []
    },
    statusLabel() {
      return this.homework ? (statusMap[this.homework.homeworkStatus] || this.homework.homeworkStatus) : ''
    },
    statusClass() {
      return this.homework ? `status-${this.homework.homeworkStatus}` : ''
    },
    submitButtonText() {
      if (!this.homework) return '提交作业'
      return this.homework.homeworkStatus === '2' ? '提交订正' : '提交作业'
    }
  },
  onLoad(options) {
    this.submitCode = decodeURIComponent(options.submitCode || '')
    this.loadHomework()
  },
  onShow() {
    if (this.submitCode) {
      this.loadHomework()
    }
  },
  methods: {
    loadHomework() {
      getHomework(this.submitCode).then(response => {
        this.homework = response.data
      })
    },
    goSubmit() {
      if (this.homework.homeworkStatus === '3') {
        uni.showToast({ title: '作业已完成', icon: 'none' })
        return
      }
      uni.navigateTo({
        url: `/pages/submission/form?submitCode=${encodeURIComponent(this.submitCode)}`
      })
    }
  }
}
</script>

<style scoped>
.detail-page {
  padding-bottom: 56rpx;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 26rpx;
}

.summary-card,
.section-card {
  padding: 30rpx;
}

.summary-card {
  margin-top: 30rpx;
}

.summary-top {
  display: flex;
  justify-content: space-between;
  gap: 20rpx;
}

.student-name {
  color: #d8b8c2;
  font-size: 24rpx;
  font-weight: 300;
}

.homework-title {
  margin-top: 8rpx;
  color: #3a3a3a;
  font-size: 32rpx;
  font-weight: 400;
  line-height: 1.45;
  word-break: break-word;
}

.status {
  flex-shrink: 0;
  height: 48rpx;
  line-height: 48rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  color: #a57e8b;
  background: rgba(216, 184, 194, 0.14);
  font-size: 22rpx;
  font-weight: 300;
}

.status-2,
.status-4 {
  color: #bf6d7c;
  background: rgba(226, 184, 192, 0.18);
}

.status-3 {
  color: #66846f;
  background: rgba(190, 215, 196, 0.22);
}

.meta-list {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  margin-top: 22rpx;
  color: rgba(153, 153, 153, 0.86);
  font-size: 22rpx;
  font-weight: 300;
}

.section-text {
  margin-top: 14rpx;
  color: #3a3a3a;
  font-size: 24rpx;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.muted {
  color: rgba(153, 153, 153, 0.86);
}

.submit-button {
  margin-top: 4rpx;
}

.history-item {
  padding: 24rpx 0;
  border-top: 1rpx solid #eae2e8;
}

.history-item:first-of-type {
  margin-top: 10rpx;
}

.history-item.compact {
  padding-bottom: 0;
}

.history-head {
  display: flex;
  justify-content: space-between;
  gap: 18rpx;
  color: rgba(153, 153, 153, 0.86);
  font-size: 22rpx;
  font-weight: 300;
}

.link-text {
  margin-top: 12rpx;
  color: #d8b8c2;
  font-size: 22rpx;
  line-height: 1.6;
  word-break: break-all;
}

.review-box {
  margin-top: 16rpx;
  padding: 20rpx;
  border-radius: 16rpx;
  color: #3a3a3a;
  background: rgba(234, 226, 232, 0.28);
  font-size: 24rpx;
  line-height: 1.7;
}

.review-title {
  margin-bottom: 8rpx;
  color: #d8b8c2;
  font-size: 26rpx;
  font-weight: 300;
}
</style>
