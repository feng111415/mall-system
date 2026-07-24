<template>
  <view class="page-shell submissions-page">
    <view class="toolbar">
      <view>
        <view class="piano-page-title">提交点评</view>
        <view class="piano-help-text">只显示每份作业的最新提交，订正会覆盖到这里。</view>
      </view>
    </view>

    <view class="tabs">
      <view v-for="tab in tabs" :key="tab.value" :class="['tab', currentTab === tab.value ? 'active' : '']" @click="switchTab(tab.value)">
        {{ tab.label }}
      </view>
    </view>

    <view v-for="item in submissions" :key="item.submissionId" class="soft-card submission-card">
      <view class="card-head">
        <view class="title-wrap">
          <view class="homework-title">{{ item.homeworkTitle }}</view>
          <view class="student-name">{{ item.studentName || '未关联学生' }}</view>
        </view>
        <view :class="['review-pill', item.reviewStatus === '0' ? 'pending' : 'done']">
          {{ item.reviewStatus === '0' ? '待点评' : '已点评' }}
        </view>
      </view>

      <view class="meta">第 {{ item.attemptNo }} 次提交 · {{ item.submitTime }}</view>
      <view class="content">{{ item.submitContent || '学生未填写说明' }}</view>

      <view v-if="item.attachmentUrl" class="video-chip">
        <view class="video-mark">▶</view>
        <view class="video-text">学生已上传视频，进入点评页查看和听辨</view>
      </view>

      <view v-if="item.reviewStatus === '1'" class="review-box">
        <view>评分：{{ item.score === null || item.score === undefined ? '未评分' : item.score }}</view>
        <view>{{ item.reviewContent || '已点评' }}</view>
        <view>{{ item.correctionRequired === '1' ? '需要订正' : '已完成' }}</view>
      </view>

      <view class="card-actions">
        <button class="primary-small" @click="goReview(item.submissionId)">
          {{ item.reviewStatus === '0' ? '去点评' : '修改点评' }}
        </button>
      </view>
    </view>

    <view v-if="!submissions.length" class="empty-state">
      <image class="empty-image" src="/static/empty-piano.png" mode="aspectFit" />
      <view class="empty-title">当前没有提交记录</view>
      <view class="piano-help-text">学生提交视频后，会出现在这里等待点评。</view>
    </view>
  </view>
</template>

<script>
import { getTeacherSubmissions } from '../../api/piano'

export default {
  data() {
    return {
      homeworkId: '',
      currentTab: 'pending',
      submissions: [],
      tabs: [
        { label: '待点评', value: 'pending' },
        { label: '已点评', value: 'reviewed' },
        { label: '全部', value: 'all' }
      ]
    }
  },
  onLoad(options) {
    this.homeworkId = options.homeworkId || ''
  },
  onShow() {
    this.loadSubmissions()
  },
  methods: {
    buildParams() {
      const params = { latestFlag: '1' }
      if (this.homeworkId) {
        params.homeworkId = this.homeworkId
      }
      if (this.currentTab === 'pending') {
        params.reviewStatus = '0'
      }
      if (this.currentTab === 'reviewed') {
        params.reviewStatus = '1'
      }
      return params
    },
    loadSubmissions() {
      getTeacherSubmissions(this.buildParams()).then(res => {
        this.submissions = res.data || []
      })
    },
    switchTab(value) {
      this.currentTab = value
      this.loadSubmissions()
    },
    goReview(submissionId) {
      uni.navigateTo({ url: `/pages/teacher/review?submissionId=${submissionId}` })
    }
  }
}
</script>

<style scoped>
.submissions-page {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  padding-bottom: 56rpx;
}

.toolbar {
  padding-top: 44rpx;
}

.card-head,
.card-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
}

.tab {
  height: 68rpx;
  line-height: 68rpx;
  border-radius: 999rpx;
  text-align: center;
  color: #3a3a3a;
  background: rgba(255, 253, 253, 0.78);
  border: 1rpx solid rgba(216, 184, 194, 0.34);
  font-size: 22rpx;
  font-weight: 300;
}

.tab.active {
  color: #fffafa;
  background: linear-gradient(135deg, #e0c6d0 0%, #d8b8c2 100%);
  border-color: transparent;
}

.submission-card {
  padding: 28rpx;
}

.title-wrap {
  flex: 1;
  min-width: 0;
}

.homework-title {
  color: #3a3a3a;
  font-size: 26rpx;
  font-weight: 400;
  line-height: 1.45;
  word-break: break-word;
}

.student-name,
.meta {
  margin-top: 8rpx;
  color: rgba(153, 153, 153, 0.86);
  font-size: 22rpx;
  font-weight: 300;
  line-height: 1.6;
}

.review-pill {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 300;
}

.review-pill.pending {
  color: #bf6d7c;
  background: rgba(226, 184, 192, 0.18);
}

.review-pill.done {
  color: #66846f;
  background: rgba(190, 215, 196, 0.22);
}

.content {
  margin-top: 14rpx;
  color: #3a3a3a;
  font-size: 24rpx;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.video-chip {
  display: flex;
  align-items: center;
  gap: 14rpx;
  margin-top: 16rpx;
  padding: 18rpx;
  border-radius: 16rpx;
  background: rgba(234, 226, 232, 0.28);
}

.video-mark {
  width: 44rpx;
  height: 44rpx;
  line-height: 44rpx;
  border-radius: 16rpx;
  text-align: center;
  color: #d8b8c2;
  background: rgba(216, 184, 194, 0.14);
  font-size: 20rpx;
}

.video-text {
  flex: 1;
  color: rgba(153, 153, 153, 0.86);
  font-size: 22rpx;
  font-weight: 300;
  line-height: 1.6;
}

.review-box {
  margin-top: 16rpx;
  padding: 18rpx;
  border-radius: 16rpx;
  color: #3a3a3a;
  background: rgba(234, 226, 232, 0.28);
  font-size: 24rpx;
  line-height: 1.7;
}

.card-actions {
  justify-content: flex-end;
  margin-top: 18rpx;
}

.primary-small {
  width: 172rpx;
  height: 62rpx;
  line-height: 62rpx;
  margin: 0;
  border-radius: 16rpx;
  color: #fffafa;
  background: linear-gradient(135deg, #e0c6d0 0%, #d8b8c2 100%);
  font-size: 24rpx;
  font-weight: 300;
}

.empty-state {
  padding: 34rpx 20rpx;
  text-align: center;
}

.empty-image {
  width: 300rpx;
  height: 210rpx;
}

.empty-title {
  color: #3a3a3a;
  font-size: 28rpx;
  font-weight: 300;
}
</style>
