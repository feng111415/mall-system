<template>
  <view class="page-shell students-page">
    <view class="toolbar">
      <view>
        <view class="piano-page-title">学生档案</view>
        <view class="piano-help-text">维护学生、家长联系方式和学习阶段。</view>
      </view>
      <button class="mini-button" @click="startCreate">新增</button>
    </view>

    <view v-if="editing" class="soft-card form-card">
      <view class="piano-section-title">{{ form.studentId ? '编辑学生' : '新增学生' }}</view>
      <view class="field">
        <view class="field-label">学生姓名</view>
        <input v-model.trim="form.studentName" class="input-line" maxlength="50" placeholder="例如：小雨" placeholder-class="placeholder" />
      </view>
      <view class="field">
        <view class="field-label">家长姓名</view>
        <input v-model.trim="form.parentName" class="input-line" maxlength="50" placeholder="可选" placeholder-class="placeholder" />
      </view>
      <view class="field">
        <view class="field-label">联系电话</view>
        <input v-model.trim="form.parentPhone" class="input-line" maxlength="20" placeholder="可选" placeholder-class="placeholder" />
      </view>
      <view class="field">
        <view class="field-label">学习阶段</view>
        <input v-model.trim="form.levelName" class="input-line" maxlength="50" placeholder="例如：启蒙 / 一级 / 进阶" placeholder-class="placeholder" />
      </view>
      <view class="field">
        <view class="field-label">学习目标</view>
        <textarea v-model.trim="form.learningGoal" class="textarea-soft goal-textarea" maxlength="200" placeholder="可选" placeholder-class="placeholder" />
      </view>
      <view class="field">
        <view class="field-label">状态</view>
        <view class="segmented">
          <view :class="['segment', form.status === '0' ? 'active' : '']" @click="form.status = '0'">在学</view>
          <view :class="['segment', form.status === '1' ? 'active' : '']" @click="form.status = '1'">停用</view>
        </view>
      </view>
      <view class="button-row">
        <button class="ghost-button" @click="cancelEdit">取消</button>
        <button class="primary-button" :loading="saving" @click="saveStudent">保存</button>
      </view>
    </view>

    <view v-for="student in students" :key="student.studentId" class="soft-card student-card">
      <view class="student-main">
        <view class="student-avatar">♪</view>
        <view class="student-text">
          <view class="student-name">{{ student.studentName }}</view>
          <view class="student-meta">{{ student.levelName || '未填写阶段' }}</view>
        </view>
        <view :class="['status', student.status === '0' ? 'active-status' : 'stop-status']">
          {{ student.status === '0' ? '在学' : '停用' }}
        </view>
      </view>

      <view class="student-info">
        <view>家长：{{ student.parentName || '未填写' }}</view>
        <view>电话：{{ student.parentPhone || '未填写' }}</view>
      </view>

      <view v-if="student.learningGoal" class="goal-box">{{ student.learningGoal }}</view>

      <view class="card-actions">
        <button class="text-button" @click="editStudent(student)">编辑</button>
        <button class="text-button danger" @click="removeStudent(student)">删除</button>
      </view>
    </view>

    <view v-if="!students.length" class="empty-card">
      <image class="empty-image" src="/static/empty-piano.png" mode="aspectFit" />
      <view class="empty-title">还没有学生档案</view>
      <view class="piano-help-text">先新增一位学生，再给她布置练习任务。</view>
    </view>
  </view>
</template>

<script>
import { createTeacherStudent, deleteTeacherStudent, getTeacherStudents, updateTeacherStudent } from '../../api/piano'

const emptyForm = () => ({
  studentId: null,
  studentName: '',
  parentName: '',
  parentPhone: '',
  levelName: '',
  learningGoal: '',
  status: '0'
})

export default {
  data() {
    return {
      students: [],
      editing: false,
      saving: false,
      form: emptyForm()
    }
  },
  onShow() {
    this.loadStudents()
  },
  methods: {
    loadStudents() {
      getTeacherStudents().then(res => {
        this.students = res.data || []
      })
    },
    startCreate() {
      this.form = emptyForm()
      this.editing = true
    },
    editStudent(student) {
      this.form = { ...student }
      this.editing = true
    },
    cancelEdit() {
      this.editing = false
      this.form = emptyForm()
    },
    saveStudent() {
      if (!this.form.studentName) {
        uni.showToast({ title: '请输入学生姓名', icon: 'none' })
        return
      }
      this.saving = true
      const action = this.form.studentId
        ? updateTeacherStudent(this.form.studentId, this.form)
        : createTeacherStudent(this.form)
      action.then(() => {
        uni.showToast({ title: '已保存', icon: 'success' })
        this.cancelEdit()
        this.loadStudents()
        this.saving = false
      }, () => {
        this.saving = false
      })
    },
    removeStudent(student) {
      uni.showModal({
        title: '删除学生',
        content: `确定删除 ${student.studentName} 吗？`,
        success: res => {
          if (!res.confirm) return
          deleteTeacherStudent(student.studentId).then(() => {
            uni.showToast({ title: '已删除', icon: 'success' })
            this.loadStudents()
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.students-page {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  padding-bottom: 56rpx;
}

.toolbar,
.student-main,
.card-actions,
.button-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.toolbar {
  padding-top: 44rpx;
}

.mini-button {
  width: 126rpx;
  height: 62rpx;
  line-height: 62rpx;
  margin: 0;
  border-radius: 16rpx;
  color: #fffafa;
  background: linear-gradient(135deg, #e0c6d0 0%, #d8b8c2 100%);
  font-size: 24rpx;
  font-weight: 300;
}

.form-card,
.student-card {
  padding: 28rpx;
}

.field {
  margin-top: 24rpx;
}

.field-label {
  color: rgba(58, 58, 58, 0.66);
  font-size: 24rpx;
  font-weight: 300;
}

.goal-textarea {
  height: 150rpx;
  margin-top: 12rpx;
}

.segmented {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12rpx;
  margin-top: 12rpx;
}

.segment {
  height: 70rpx;
  line-height: 70rpx;
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
  background: linear-gradient(135deg, #e0c6d0 0%, #d8b8c2 100%);
  border-color: transparent;
}

.button-row {
  margin-top: 28rpx;
}

.button-row button {
  flex: 1;
}

.student-avatar {
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

.student-text {
  flex: 1;
  min-width: 0;
}

.student-name {
  color: #3a3a3a;
  font-size: 28rpx;
  font-weight: 400;
}

.student-meta,
.student-info {
  color: rgba(153, 153, 153, 0.86);
  font-size: 22rpx;
  font-weight: 300;
  line-height: 1.7;
}

.status {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 300;
}

.active-status {
  color: #66846f;
  background: rgba(190, 215, 196, 0.22);
}

.stop-status {
  color: rgba(153, 153, 153, 0.9);
  background: rgba(234, 226, 232, 0.34);
}

.student-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-top: 18rpx;
}

.goal-box {
  margin-top: 18rpx;
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

.text-button {
  width: 112rpx;
  height: 56rpx;
  line-height: 56rpx;
  margin: 0;
  border-radius: 12rpx;
  color: #3a3a3a;
  background: rgba(255, 253, 253, 0.78);
  border: 1rpx solid rgba(216, 184, 194, 0.34);
  font-size: 22rpx;
  font-weight: 300;
}

.text-button.danger {
  color: #bf6d7c;
}

.empty-card {
  padding: 54rpx 32rpx;
  text-align: center;
}

.empty-image {
  width: 260rpx;
  height: 180rpx;
}

.empty-title {
  margin-top: 16rpx;
  color: #3a3a3a;
  font-size: 28rpx;
  font-weight: 300;
}
</style>
