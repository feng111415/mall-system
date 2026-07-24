<template>
  <view class="page-shell homeworks-page">
    <view class="toolbar">
      <view>
        <view class="piano-page-title">作业管理</view>
        <view class="piano-help-text">复制提交码，跟进提交和订正状态。</view>
      </view>
      <button class="mini-button" @click="createHomework">新增</button>
    </view>

    <view class="tabs">
      <view v-for="tab in tabs" :key="tab.value" :class="['tab', status === tab.value ? 'active' : '']" @click="switchStatus(tab.value)">
        {{ tab.label }}
      </view>
    </view>

    <view v-for="item in homeworks" :key="item.homeworkId" class="soft-card homework-card">
      <view class="card-head">
        <view class="title-wrap">
          <view class="homework-title">{{ item.homeworkTitle }}</view>
          <view class="student-name">{{ item.studentName || '未关联学生' }}</view>
        </view>
        <view :class="['status-pill', statusClass(item.homeworkStatus)]">{{ statusLabel(item.homeworkStatus) }}</view>
      </view>

      <view class="meta">截止：{{ item.submitDeadline || '未设置' }}</view>

      <view class="code-row">
        <view>
          <view class="code-label">提交码</view>
          <view class="code">{{ item.submitCode }}</view>
        </view>
        <button class="copy-button" @click="copyCode(item.submitCode)">复制</button>
      </view>

      <view class="card-actions">
        <button class="text-button" @click="viewSubmissions(item.homeworkId)">提交</button>
        <button class="text-button" @click="editHomework(item.homeworkId)">编辑</button>
        <button class="text-button danger" @click="removeHomework(item)">删除</button>
      </view>
    </view>

    <view v-if="!homeworks.length" class="empty-state">
      <image class="empty-image" src="/static/empty-piano.png" mode="aspectFit" />
      <view class="empty-title">当前没有作业</view>
      <view class="piano-help-text">点击右上角新增，给学生布置第一份练习。</view>
    </view>
  </view>
</template>

<script>
import { deleteTeacherHomework, getTeacherHomeworks } from '../../api/piano'

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
      status: '',
      homeworks: [],
      tabs: [
        { label: '全部', value: '' },
        { label: '待提交', value: '0' },
        { label: '待点评', value: '1' },
        { label: '需订正', value: '2' },
        { label: '完成', value: '3' }
      ]
    }
  },
  onShow() {
    this.loadHomeworks()
  },
  methods: {
    loadHomeworks() {
      getTeacherHomeworks({ homeworkStatus: this.status }).then(res => {
        this.homeworks = res.data || []
      })
    },
    switchStatus(value) {
      this.status = value
      this.loadHomeworks()
    },
    statusLabel(value) {
      return statusMap[value] || value || '未知'
    },
    statusClass(value) {
      return `status-${value}`
    },
    createHomework() {
      uni.navigateTo({ url: '/pages/teacher/homework-form' })
    },
    editHomework(homeworkId) {
      uni.navigateTo({ url: `/pages/teacher/homework-form?homeworkId=${homeworkId}` })
    },
    viewSubmissions(homeworkId) {
      uni.navigateTo({ url: `/pages/teacher/submissions?homeworkId=${homeworkId}` })
    },
    copyCode(code) {
      uni.setClipboardData({
        data: code,
        success: () => uni.showToast({ title: '已复制提交码', icon: 'success' })
      })
    },
    removeHomework(item) {
      uni.showModal({
        title: '删除作业',
        content: `确定删除《${item.homeworkTitle}》吗？`,
        success: res => {
          if (!res.confirm) return
          deleteTeacherHomework(item.homeworkId).then(() => {
            uni.showToast({ title: '已删除', icon: 'success' })
            this.loadHomeworks()
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.homeworks-page {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  padding-bottom: 56rpx;
}

.toolbar,
.card-head,
.code-row,
.card-actions {
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

.tabs {
  display: flex;
  gap: 12rpx;
  overflow-x: auto;
  white-space: nowrap;
}

.tab {
  flex-shrink: 0;
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
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

.homework-card {
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

.status-pill {
  flex-shrink: 0;
  padding: 8rpx 16rpx;
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

.code-row {
  margin-top: 18rpx;
  padding: 18rpx;
  border-radius: 16rpx;
  background: rgba(234, 226, 232, 0.28);
}

.code-label {
  color: rgba(153, 153, 153, 0.86);
  font-size: 22rpx;
  font-weight: 300;
}

.code {
  margin-top: 4rpx;
  color: #d8b8c2;
  font-size: 32rpx;
  font-weight: 200;
  letter-spacing: 4rpx;
}

.copy-button,
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

.card-actions {
  justify-content: flex-end;
  margin-top: 18rpx;
}

.text-button.danger {
  color: #bf6d7c;
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
