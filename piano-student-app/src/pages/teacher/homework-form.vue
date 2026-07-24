<template>
  <view class="page-shell homework-form-page">
    <view class="page-head">
      <view>
        <view class="piano-page-title">{{ homeworkId ? '编辑作业' : '布置作业' }}</view>
        <view class="piano-help-text">发布后学生随时可以提交，只设置截止时间。</view>
      </view>
      <view class="head-mark">♪</view>
    </view>

    <view class="soft-card form-card">
      <view class="field">
        <view class="field-label">学生</view>
        <picker :range="studentNames" :value="studentIndex" @change="handleStudentChange">
          <view class="picker-value">{{ selectedStudentName || '请选择学生' }}</view>
        </picker>
      </view>

      <view class="field">
        <view class="field-label">作业标题</view>
        <input v-model.trim="form.homeworkTitle" class="input-line" maxlength="100" placeholder="例如：拜厄第 42 条" placeholder-class="placeholder" />
      </view>

      <view class="field">
        <view class="field-label">练习内容</view>
        <textarea v-model.trim="form.practiceContent" class="textarea-soft large" maxlength="1000" placeholder="写清曲目、段落、速度或练习重点" placeholder-class="placeholder" />
      </view>

      <view class="field">
        <view class="field-label">练习要求</view>
        <textarea v-model.trim="form.practiceRequirement" class="textarea-soft" maxlength="1000" placeholder="可选，例如每天三遍，注意手型和节奏" placeholder-class="placeholder" />
      </view>

      <view class="field">
        <view class="field-label">截止时间</view>
        <view class="deadline-row">
          <picker mode="date" :value="deadlineDate" @change="deadlineDate = $event.detail.value">
            <view class="picker-value">{{ deadlineDate || '选择日期' }}</view>
          </picker>
          <picker mode="time" :value="deadlineTime" @change="deadlineTime = $event.detail.value">
            <view class="picker-value">{{ deadlineTime || '选择时间' }}</view>
          </picker>
        </view>
      </view>

      <button class="primary-button" :loading="saving" @click="saveHomework">{{ homeworkId ? '保存修改' : '发布作业' }}</button>
    </view>

    <view v-if="createdHomework" class="soft-card result-card">
      <view class="result-title">作业已发布</view>
      <view class="result-code">{{ createdHomework.submitCode }}</view>
      <button class="ghost-button" @click="copyCode(createdHomework.submitCode)">复制提交码</button>
    </view>
  </view>
</template>

<script>
import { createTeacherHomework, getTeacherHomeworkDetail, getTeacherStudents, updateTeacherHomework } from '../../api/piano'

const emptyForm = () => ({
  studentId: null,
  homeworkTitle: '',
  practiceContent: '',
  practiceRequirement: '',
  submitDeadline: '',
  homeworkStatus: '0'
})

export default {
  data() {
    return {
      homeworkId: null,
      students: [],
      studentIndex: -1,
      deadlineDate: '',
      deadlineTime: '20:00',
      form: emptyForm(),
      saving: false,
      createdHomework: null
    }
  },
  computed: {
    studentNames() {
      return this.students.map(item => `${item.studentName}${item.levelName ? ' · ' + item.levelName : ''}`)
    },
    selectedStudentName() {
      return this.studentIndex >= 0 ? this.studentNames[this.studentIndex] : ''
    }
  },
  onLoad(options) {
    this.homeworkId = options.homeworkId || null
    this.loadStudents()
  },
  methods: {
    loadStudents() {
      getTeacherStudents().then(res => {
        this.students = res.data || []
        if (this.homeworkId) {
          this.loadHomework()
        }
      })
    },
    loadHomework() {
      getTeacherHomeworkDetail(this.homeworkId).then(res => {
        const homework = res.data.homework || {}
        this.form = {
          studentId: homework.studentId,
          homeworkTitle: homework.homeworkTitle || '',
          practiceContent: homework.practiceContent || '',
          practiceRequirement: homework.practiceRequirement || '',
          submitDeadline: homework.submitDeadline || '',
          homeworkStatus: homework.homeworkStatus || '0'
        }
        this.studentIndex = this.students.findIndex(item => item.studentId === homework.studentId)
        if (homework.submitDeadline) {
          const parts = homework.submitDeadline.split(' ')
          this.deadlineDate = parts[0] || ''
          this.deadlineTime = parts[1] ? parts[1].slice(0, 5) : '20:00'
        }
      })
    },
    handleStudentChange(event) {
      this.studentIndex = Number(event.detail.value)
      this.form.studentId = this.students[this.studentIndex].studentId
    },
    buildDeadline() {
      if (!this.deadlineDate) return ''
      return `${this.deadlineDate} ${this.deadlineTime || '20:00'}:00`
    },
    saveHomework() {
      if (!this.form.studentId) {
        uni.showToast({ title: '请选择学生', icon: 'none' })
        return
      }
      if (!this.form.homeworkTitle) {
        uni.showToast({ title: '请输入作业标题', icon: 'none' })
        return
      }
      if (!this.form.practiceContent) {
        uni.showToast({ title: '请输入练习内容', icon: 'none' })
        return
      }
      const payload = {
        ...this.form,
        submitDeadline: this.buildDeadline()
      }
      this.saving = true
      const action = this.homeworkId ? updateTeacherHomework(this.homeworkId, payload) : createTeacherHomework(payload)
      action.then(res => {
        uni.showToast({ title: this.homeworkId ? '已保存' : '已发布', icon: 'success' })
        if (!this.homeworkId) {
          this.createdHomework = res.data
        }
        this.saving = false
      }, () => {
        this.saving = false
      })
    },
    copyCode(code) {
      uni.setClipboardData({
        data: code,
        success: () => uni.showToast({ title: '已复制提交码', icon: 'success' })
      })
    }
  }
}
</script>

<style scoped>
.homework-form-page {
  display: flex;
  flex-direction: column;
  gap: 26rpx;
  padding-bottom: 56rpx;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18rpx;
  padding-top: 44rpx;
}

.head-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 72rpx;
  height: 72rpx;
  border-radius: 24rpx;
  color: #d8b8c2;
  background: rgba(216, 184, 194, 0.13);
  font-size: 32rpx;
  font-weight: 200;
}

.form-card,
.result-card {
  padding: 30rpx;
}

.field {
  margin-bottom: 28rpx;
}

.field-label {
  margin-bottom: 10rpx;
  color: rgba(58, 58, 58, 0.66);
  font-size: 24rpx;
  font-weight: 300;
}

.picker-value {
  height: 82rpx;
  line-height: 82rpx;
  padding: 0;
  border-bottom: 1rpx solid #eae2e8;
  color: #3a3a3a;
  font-size: 26rpx;
  font-weight: 400;
}

.textarea-soft {
  height: 168rpx;
}

.textarea-soft.large {
  height: 236rpx;
}

.deadline-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18rpx;
}

.result-card {
  text-align: center;
}

.result-title {
  color: #66846f;
  font-size: 26rpx;
  font-weight: 300;
}

.result-code {
  margin: 16rpx 0 22rpx;
  color: #d8b8c2;
  font-size: 42rpx;
  font-weight: 200;
  letter-spacing: 4rpx;
}
</style>
