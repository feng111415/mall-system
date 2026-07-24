<template>
  <view class="page-shell index-page">
    <view class="hero">
      <view class="hero-mark">♪</view>
      <view class="hero-title">钢琴作业</view>
      <view class="hero-subtitle">输入老师给你的提交码，查看练习任务并提交视频作业。</view>
    </view>

    <view class="soft-card code-card">
      <view class="piano-section-title">学生入口</view>
      <view class="field">
        <view class="field-label">提交码</view>
        <input
          v-model.trim="submitCode"
          class="input-line code-input"
          maxlength="16"
          placeholder="例如 P123456789"
          placeholder-class="placeholder"
          confirm-type="search"
          @confirm="handleSearch"
        />
      </view>
      <button class="primary-button" :loading="loading" @click="handleSearch">查看作业</button>
      <view class="piano-help-text">提交码由老师提供。老师点评后，可以继续订正并再次提交视频。</view>
    </view>

    <view v-if="recentCode" class="recent-card">
      <view>
        <view class="recent-title">上次查看</view>
        <view class="recent-code">{{ recentCode }}</view>
      </view>
      <button class="ghost-button recent-button" @click="useRecentCode">继续</button>
    </view>

    <view class="teacher-entry" @click="goTeacher">
      <view class="entry-icon">♬</view>
      <view class="entry-copy">
        <view class="entry-title">老师入口</view>
        <view class="entry-desc">手机端布置作业、查看视频、点评订正和管理学生</view>
      </view>
      <view class="entry-arrow">›</view>
    </view>
  </view>
</template>

<script>
import { getHomework } from '../../api/piano'

export default {
  data() {
    return {
      submitCode: '',
      recentCode: '',
      loading: false
    }
  },
  onShow() {
    this.recentCode = uni.getStorageSync('pianoSubmitCode') || ''
  },
  methods: {
    useRecentCode() {
      this.submitCode = this.recentCode
      this.handleSearch()
    },
    handleSearch() {
      const code = (this.submitCode || '').trim().toUpperCase()
      if (!code) {
        uni.showToast({ title: '请输入提交码', icon: 'none' })
        return
      }
      this.loading = true
      getHomework(code).then(() => {
        uni.setStorageSync('pianoSubmitCode', code)
        uni.navigateTo({
          url: `/pages/homework/detail?submitCode=${encodeURIComponent(code)}`
        })
        this.loading = false
      }, () => {
        this.loading = false
      })
    },
    goTeacher() {
      uni.navigateTo({
        url: '/pages/teacher/login'
      })
    }
  }
}
</script>

<style scoped>
.index-page {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
  padding-bottom: 54rpx;
}

.hero {
  padding: 54rpx 16rpx 20rpx;
  text-align: center;
}

.hero-mark {
  width: 72rpx;
  height: 72rpx;
  line-height: 72rpx;
  margin: 0 auto 20rpx;
  border-radius: 24rpx;
  color: #d8b8c2;
  background: rgba(216, 184, 194, 0.13);
  font-size: 38rpx;
  font-weight: 200;
}

.hero-title {
  color: #d8b8c2;
  font-size: 36rpx;
  font-weight: 200;
  line-height: 1.35;
}

.hero-subtitle {
  width: 520rpx;
  margin: 16rpx auto 0;
  color: rgba(153, 153, 153, 0.84);
  font-size: 24rpx;
  font-weight: 300;
  line-height: 1.7;
}

.code-card {
  padding: 30rpx;
}

.field {
  margin: 24rpx 0 30rpx;
}

.field-label {
  color: rgba(58, 58, 58, 0.66);
  font-size: 24rpx;
  font-weight: 300;
}

.code-input {
  margin-top: 6rpx;
  font-size: 30rpx;
}

.piano-help-text {
  margin-top: 18rpx;
}

.recent-card,
.teacher-entry {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 30rpx;
  border-radius: 16rpx;
  background: rgba(255, 250, 246, 0.86);
  box-shadow: 0 12rpx 32rpx rgba(216, 184, 194, 0.1);
}

.recent-card {
  justify-content: space-between;
}

.recent-title {
  color: rgba(153, 153, 153, 0.84);
  font-size: 22rpx;
  font-weight: 300;
}

.recent-code {
  margin-top: 6rpx;
  color: #3a3a3a;
  font-size: 28rpx;
  font-weight: 400;
}

.recent-button {
  width: 150rpx;
  margin: 0;
}

.entry-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  color: #d8b8c2;
  background: rgba(216, 184, 194, 0.13);
  font-size: 30rpx;
  font-weight: 200;
}

.entry-copy {
  flex: 1;
}

.entry-title {
  color: #3a3a3a;
  font-size: 26rpx;
  font-weight: 400;
}

.entry-desc {
  margin-top: 8rpx;
  color: rgba(153, 153, 153, 0.84);
  font-size: 22rpx;
  font-weight: 300;
  line-height: 1.6;
}

.entry-arrow {
  color: #d8b8c2;
  font-size: 42rpx;
  font-weight: 200;
}
</style>
