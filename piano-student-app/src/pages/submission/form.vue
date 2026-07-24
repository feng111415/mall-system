<template>
  <view class="page-shell form-page">
    <view class="page-head">
      <view class="piano-page-title">提交练习记录</view>
      <view class="piano-help-text">写下今天的练习情况，也可以粘贴附件或网盘链接。</view>
    </view>

    <view class="soft-card form-card">
      <view class="field">
        <view class="field-label">练习说明</view>
        <textarea
          v-model.trim="form.submitContent"
          class="textarea-soft practice-textarea"
          maxlength="1000"
          placeholder="例如：今天练了三遍，第二段还需要再慢练。"
          placeholder-class="placeholder"
        />
      </view>

      <view class="field">
        <view class="field-label">附件链接</view>
        <input
          v-model.trim="form.attachmentUrl"
          class="input-line"
          maxlength="500"
          placeholder="可选：粘贴网盘、图片或视频链接"
          placeholder-class="placeholder"
        />
      </view>

      <button class="primary-button" :loading="submitting" @click="handleSubmit">提交给老师</button>
    </view>
  </view>
</template>

<script>
import { submitHomework } from '../../api/piano'

export default {
  data() {
    return {
      submitCode: '',
      submitting: false,
      form: {
        submitContent: '',
        attachmentUrl: ''
      }
    }
  },
  onLoad(options) {
    this.submitCode = decodeURIComponent(options.submitCode || '')
  },
  methods: {
    handleSubmit() {
      if (!this.form.submitContent && !this.form.attachmentUrl) {
        uni.showToast({ title: '请填写说明或附件链接', icon: 'none' })
        return
      }
      this.submitting = true
      submitHomework({
        submitCode: this.submitCode,
        submitContent: this.form.submitContent,
        attachmentUrl: this.form.attachmentUrl
      }).then(() => {
        uni.showToast({ title: '提交成功', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 700)
        this.submitting = false
      }, () => {
        this.submitting = false
      })
    }
  }
}
</script>

<style scoped>
.form-page {
  display: flex;
  flex-direction: column;
  gap: 30rpx;
  padding-bottom: 56rpx;
}

.page-head {
  padding: 46rpx 8rpx 4rpx;
}

.form-card {
  padding: 30rpx;
}

.field {
  margin-bottom: 30rpx;
}

.field-label {
  margin-bottom: 12rpx;
  color: rgba(58, 58, 58, 0.66);
  font-size: 24rpx;
  font-weight: 300;
}

.practice-textarea {
  height: 260rpx;
}

.primary-button {
  margin-top: 10rpx;
}
</style>
