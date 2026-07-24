<template>
  <view class="page-shell login-page">
    <view class="login-head">
      <view class="music-mark">♬</view>
      <view class="piano-page-title">老师工作台</view>
      <view class="piano-help-text">在手机上完成布置作业、查看视频、点评订正和学生管理。</view>
    </view>

    <view class="soft-card login-card">
      <view class="piano-section-title">输入老师口令</view>
      <view class="field">
        <view class="field-label">口令</view>
        <input
          v-model.trim="teacherCode"
          class="input-line"
          password
          maxlength="50"
          placeholder="请输入老师端口令"
          placeholder-class="placeholder"
          @confirm="handleLogin"
        />
      </view>
      <button class="primary-button" :loading="loading" @click="handleLogin">进入工作台</button>
      <view class="piano-help-text tip">默认口令用于本地演示，正式给老师使用前请修改为私密口令。</view>
    </view>
  </view>
</template>

<script>
import { getTeacherCode, loginTeacher, setTeacherCode } from '../../api/piano'

export default {
  data() {
    return {
      teacherCode: '',
      loading: false
    }
  },
  onShow() {
    this.teacherCode = getTeacherCode()
  },
  methods: {
    handleLogin() {
      const code = (this.teacherCode || '').trim()
      if (!code) {
        uni.showToast({ title: '请输入老师口令', icon: 'none' })
        return
      }
      this.loading = true
      loginTeacher(code).then(() => {
        setTeacherCode(code)
        uni.redirectTo({ url: '/pages/teacher/home' })
        this.loading = false
      }, () => {
        this.loading = false
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  gap: 34rpx;
  padding-bottom: 56rpx;
}

.login-head {
  padding: 58rpx 22rpx 8rpx;
  text-align: center;
}

.music-mark {
  width: 78rpx;
  height: 78rpx;
  line-height: 78rpx;
  margin: 0 auto 20rpx;
  border-radius: 26rpx;
  color: #d8b8c2;
  background: rgba(216, 184, 194, 0.13);
  font-size: 36rpx;
  font-weight: 200;
}

.login-head .piano-help-text {
  width: 520rpx;
  margin: 16rpx auto 0;
}

.login-card {
  padding: 30rpx;
}

.field {
  margin: 28rpx 0 34rpx;
}

.field-label {
  color: rgba(58, 58, 58, 0.66);
  font-size: 24rpx;
  font-weight: 300;
}

.tip {
  margin-top: 18rpx;
}
</style>
