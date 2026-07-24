<template>
  <view class="page-shell review-page">
    <view v-if="submission" class="soft-card summary-card">
      <view class="summary-head">
        <view class="summary-copy">
          <view class="homework-title">{{ submission.homeworkTitle }}</view>
          <view class="student-name">{{ submission.studentName || '未关联学生' }} · 第 {{ submission.attemptNo }} 次提交</view>
        </view>
        <view class="summary-mark">✓</view>
      </view>
      <view class="meta">{{ submission.submitTime }}</view>
      <view class="content">{{ submission.submitContent || '学生未填写说明' }}</view>
      <video
        v-if="submission.attachmentUrl"
        class="video-player"
        :src="submission.attachmentUrl"
        controls
        object-fit="contain"
      />
      <view v-else class="no-video">学生本次没有上传视频，请结合文字说明点评。</view>
    </view>

    <view class="soft-card form-card">
      <view class="field">
        <view class="field-label">评分</view>
        <input v-model.trim="form.score" class="input-line" type="number" maxlength="3" placeholder="0-100，可不填" placeholder-class="placeholder" />
      </view>
      <view class="field">
        <view class="field-label">老师点评</view>
        <textarea v-model.trim="form.reviewContent" class="textarea-soft review-textarea" maxlength="1000" placeholder="写给学生看的建议，例如节奏、手型、音色、练习方法" placeholder-class="placeholder" />
      </view>
      <view class="field">
        <view class="field-label">处理结果</view>
        <view class="segmented">
          <view :class="['segment', form.correctionRequired === '0' ? 'active' : '']" @click="form.correctionRequired = '0'">通过完成</view>
          <view :class="['segment', form.correctionRequired === '1' ? 'active warn' : '']" @click="form.correctionRequired = '1'">需要订正</view>
        </view>
      </view>
      <button class="primary-button" :loading="saving" @click="saveReview">保存点评</button>
    </view>
  </view>
</template>

<script>
import { getTeacherSubmissionDetail, reviewTeacherSubmission } from '../../api/piano'

export default {
  data() {
    return {
      submissionId: '',
      submission: null,
      saving: false,
      form: {
        score: '',
        reviewContent: '',
        correctionRequired: '0'
      }
    }
  },
  onLoad(options) {
    this.submissionId = options.submissionId || ''
    this.loadSubmission()
  },
  methods: {
    loadSubmission() {
      getTeacherSubmissionDetail(this.submissionId).then(res => {
        this.submission = res.data
        this.form.score = this.submission.score === null || this.submission.score === undefined ? '' : String(this.submission.score)
        this.form.reviewContent = this.submission.reviewContent || ''
        this.form.correctionRequired = this.submission.correctionRequired || '0'
      })
    },
    saveReview() {
      const score = this.form.score === '' ? null : Number(this.form.score)
      if (score !== null && (score < 0 || score > 100)) {
        uni.showToast({ title: '评分需要在 0-100 之间', icon: 'none' })
        return
      }
      if (!this.form.reviewContent) {
        uni.showToast({ title: '请填写点评内容', icon: 'none' })
        return
      }
      this.saving = true
      reviewTeacherSubmission({
        submissionId: Number(this.submissionId),
        score,
        reviewContent: this.form.reviewContent,
        correctionRequired: this.form.correctionRequired
      }).then(() => {
        uni.showToast({ title: '点评已保存', icon: 'success' })
        setTimeout(() => {
          uni.navigateBack()
        }, 700)
        this.saving = false
      }, () => {
        this.saving = false
      })
    }
  }
}
</script>

<style scoped>
.review-page {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding-bottom: 56rpx;
}

.summary-card {
  margin-top: 30rpx;
}

.summary-card,
.form-card {
  padding: 30rpx;
}

.summary-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
}

.summary-copy {
  flex: 1;
  min-width: 0;
}

.summary-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 68rpx;
  height: 68rpx;
  border-radius: 22rpx;
  color: #d8b8c2;
  background: rgba(216, 184, 194, 0.13);
  font-size: 28rpx;
  font-weight: 200;
}

.homework-title {
  color: #3a3a3a;
  font-size: 28rpx;
  font-weight: 400;
  line-height: 1.45;
  word-break: break-word;
}

.student-name,
.meta {
  margin-top: 10rpx;
  color: rgba(153, 153, 153, 0.86);
  font-size: 22rpx;
  font-weight: 300;
}

.content {
  margin-top: 18rpx;
  color: #3a3a3a;
  font-size: 24rpx;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.video-player {
  width: 100%;
  height: 380rpx;
  margin-top: 18rpx;
  border-radius: 16rpx;
  background: #3a3a3a;
}

.no-video {
  margin-top: 18rpx;
  padding: 18rpx;
  border-radius: 16rpx;
  color: rgba(153, 153, 153, 0.86);
  background: rgba(234, 226, 232, 0.28);
  font-size: 22rpx;
  font-weight: 300;
  line-height: 1.7;
}

.field {
  margin-bottom: 28rpx;
}

.field-label {
  color: rgba(58, 58, 58, 0.66);
  font-size: 24rpx;
  font-weight: 300;
}

.review-textarea {
  height: 250rpx;
  margin-top: 12rpx;
}

.segmented {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
  margin-top: 12rpx;
}

.segment {
  height: 72rpx;
  line-height: 72rpx;
  border-radius: 14rpx;
  text-align: center;
  color: #3a3a3a;
  background: rgba(255, 253, 253, 0.78);
  border: 1rpx solid rgba(216, 184, 194, 0.34);
  font-size: 24rpx;
  font-weight: 300;
}

.segment.active {
  color: #fffafa;
  background: #66846f;
  border-color: #66846f;
}

.segment.active.warn {
  background: #e2b8c0;
  border-color: #e2b8c0;
}
</style>
