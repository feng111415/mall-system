<template>
  <div class="piano-page submission-page">
    <div class="piano-hero">
      <div>
        <div class="eyebrow">Review Room</div>
        <h2>提交点评</h2>
        <p>查看学生提交，记录每次订正，给出温柔但清晰的反馈。</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['piano:submission:add']">新增提交</el-button>
    </div>

    <div class="filter-band">
      <el-form ref="queryForm" :model="queryParams" size="small" :inline="true">
        <el-form-item label="学生" prop="studentName">
          <el-input v-model="queryParams.studentName" placeholder="学生姓名" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="作业" prop="homeworkTitle">
          <el-input v-model="queryParams.homeworkTitle" placeholder="作业标题" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="点评" prop="reviewStatus">
          <el-select v-model="queryParams.reviewStatus" placeholder="点评状态" clearable>
            <el-option label="待点评" value="0" />
            <el-option label="已点评" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="最新" prop="latestFlag">
          <el-select v-model="queryParams.latestFlag" placeholder="是否最新" clearable>
            <el-option label="最新提交" value="1" />
            <el-option label="历史提交" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          <el-button type="warning" plain icon="el-icon-download" @click="handleExport" v-hasPermi="['piano:submission:export']">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-empty v-if="!loading && submissionList.length === 0" description="还没有提交记录" />
    <div v-loading="loading" class="submission-list">
      <div v-for="item in submissionList" :key="item.submissionId" class="submission-card">
        <div class="submission-head">
          <div>
            <h3>{{ item.homeworkTitle || '未命名作业' }}</h3>
            <div class="sub-line">{{ item.studentName || '未知学生' }} · 第 {{ item.attemptNo }} 次提交</div>
          </div>
          <div class="tag-stack">
            <el-tag size="mini" :type="item.reviewStatus === '1' ? 'success' : 'warning'">{{ item.reviewStatus === '1' ? '已点评' : '待点评' }}</el-tag>
            <el-tag v-if="item.overdueFlag === '1'" size="mini" type="danger">逾期</el-tag>
            <el-tag v-if="item.latestFlag === '1'" size="mini">最新</el-tag>
          </div>
        </div>

        <div class="content-block">
          <label>提交说明</label>
          <p>{{ item.submitContent || '未填写' }}</p>
        </div>
        <div class="link-line">
          <span>附件</span>
          <a v-if="item.attachmentUrl" :href="item.attachmentUrl" target="_blank">{{ item.attachmentUrl }}</a>
          <em v-else>未填写</em>
        </div>
        <div class="review-box">
          <div><span>评分</span>{{ item.score !== null && item.score !== undefined ? item.score : '未评分' }}</div>
          <div><span>订正</span>{{ item.correctionRequired === '1' ? '需要订正' : '无需订正' }}</div>
          <p>{{ item.reviewContent || '暂无点评' }}</p>
        </div>
        <div class="card-actions">
          <el-button type="text" icon="el-icon-time" @click="openHistory(item)">历史</el-button>
          <el-button type="text" icon="el-icon-edit-outline" @click="handleReview(item)" v-hasPermi="['piano:submission:review']">点评</el-button>
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(item)" v-hasPermi="['piano:submission:edit']">编辑</el-button>
          <el-button type="text" icon="el-icon-delete" class="danger-text" @click="handleDelete(item)" v-hasPermi="['piano:submission:remove']">删除</el-button>
        </div>
      </div>
    </div>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="作业" prop="homeworkId">
          <el-select v-model="form.homeworkId" placeholder="请选择作业" filterable style="width: 100%">
            <el-option v-for="item in homeworkOptions" :key="item.homeworkId" :label="`${item.studentName || '未知学生'} - ${item.homeworkTitle}`" :value="item.homeworkId" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交说明" prop="submitContent">
          <el-input v-model="form.submitContent" type="textarea" :rows="4" placeholder="请输入学生提交说明" />
        </el-form-item>
        <el-form-item label="附件地址" prop="attachmentUrl">
          <el-input v-model="form.attachmentUrl" placeholder="请输入图片、音频或视频链接" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="老师点评" :visible.sync="reviewOpen" width="620px" append-to-body>
      <el-form ref="reviewForm" :model="reviewForm" :rules="reviewRules" label-width="110px">
        <el-form-item label="评分" prop="score">
          <el-input-number v-model="reviewForm.score" :min="0" :max="100" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否订正" prop="correctionRequired">
          <el-radio-group v-model="reviewForm.correctionRequired">
            <el-radio label="0">无需订正</el-radio>
            <el-radio label="1">需要订正</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="点评内容" prop="reviewContent">
          <el-input v-model="reviewForm.reviewContent" type="textarea" :rows="5" placeholder="请输入老师点评" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitReview">保存点评</el-button>
        <el-button @click="reviewOpen = false">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="提交历史" :visible.sync="historyOpen" width="820px" append-to-body>
      <el-timeline v-if="historyList.length">
        <el-timeline-item v-for="item in historyList" :key="item.submissionId" :timestamp="parseTime(item.submitTime)" placement="top">
          <div class="history-item">
            <strong>第 {{ item.attemptNo }} 次 · {{ item.submitType === '1' ? '订正提交' : '首次提交' }}</strong>
            <p>{{ item.submitContent || '未填写提交说明' }}</p>
            <small>点评：{{ item.reviewContent || '暂无' }}</small>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无历史记录" />
    </el-dialog>
  </div>
</template>

<script>
import { listHomework } from '@/api/piano/homework'
import { listSubmission, getSubmission, listSubmissionHistory, addSubmission, updateSubmission, reviewSubmission, delSubmission } from '@/api/piano/submission'

export default {
  name: 'PianoSubmission',
  data() {
    return {
      loading: false,
      total: 0,
      submissionList: [],
      homeworkOptions: [],
      historyList: [],
      open: false,
      reviewOpen: false,
      historyOpen: false,
      title: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentName: undefined,
        homeworkTitle: undefined,
        reviewStatus: undefined,
        latestFlag: '1'
      },
      form: {},
      reviewForm: {},
      rules: {
        homeworkId: [{ required: true, message: '作业不能为空', trigger: 'change' }],
        submitContent: [{ max: 1000, message: '提交说明不能超过1000个字符', trigger: 'blur' }],
        attachmentUrl: [{ max: 500, message: '附件地址不能超过500个字符', trigger: 'blur' }]
      },
      reviewRules: {
        score: [{ required: true, message: '评分不能为空', trigger: 'blur' }],
        correctionRequired: [{ required: true, message: '是否订正不能为空', trigger: 'change' }],
        reviewContent: [{ max: 1000, message: '点评内容不能超过1000个字符', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getHomeworkOptions()
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listSubmission(this.queryParams).then(response => {
        this.submissionList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getHomeworkOptions() {
      listHomework({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.homeworkOptions = response.rows || []
      })
    },
    reset() {
      this.form = {
        submissionId: undefined,
        homeworkId: undefined,
        submitContent: undefined,
        attachmentUrl: undefined,
        remark: undefined
      }
      this.resetForm('form')
    },
    cancel() {
      this.open = false
      this.reset()
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.queryParams.latestFlag = '1'
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增提交'
    },
    handleUpdate(row) {
      this.reset()
      getSubmission(row.submissionId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = '编辑提交'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.submissionId ? updateSubmission : addSubmission
        request(this.form).then(() => {
          this.$modal.msgSuccess(this.form.submissionId ? '修改成功' : '提交成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleReview(row) {
      this.reviewForm = {
        submissionId: row.submissionId,
        score: row.score,
        reviewContent: row.reviewContent,
        correctionRequired: row.correctionRequired || '0'
      }
      this.reviewOpen = true
      this.$nextTick(() => this.resetForm('reviewForm'))
    },
    submitReview() {
      this.$refs.reviewForm.validate(valid => {
        if (!valid) return
        reviewSubmission(this.reviewForm).then(() => {
          this.$modal.msgSuccess('点评成功')
          this.reviewOpen = false
          this.getList()
        })
      })
    },
    openHistory(row) {
      listSubmissionHistory(row.homeworkId).then(response => {
        this.historyList = response.data || []
        this.historyOpen = true
      })
    },
    handleDelete(row) {
      this.$modal.confirm(`是否确认删除第 ${row.attemptNo} 次提交记录？`).then(() => {
        return delSubmission(row.submissionId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('/piano/submission/export', { ...this.queryParams }, `piano_submission_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>

<style scoped>
.piano-page {
  padding: 20px;
  background: #fff8fb;
  min-height: calc(100vh - 84px);
}
.piano-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #fff, #fff0f6);
  border: 1px solid #f4d8e6;
}
.piano-hero h2 {
  margin: 4px 0 8px;
  color: #3f3340;
  font-size: 26px;
  font-weight: 700;
}
.piano-hero p {
  margin: 0;
  color: #7d6a78;
}
.eyebrow {
  color: #b86b8b;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}
.filter-band {
  padding: 16px 18px 2px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #f2dce7;
}
.submission-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
  gap: 16px;
  min-height: 180px;
}
.submission-card {
  padding: 18px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #efd8e3;
  box-shadow: 0 8px 22px rgba(184, 107, 139, 0.08);
}
.submission-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.submission-head h3 {
  margin: 0 0 6px;
  color: #342b35;
}
.sub-line {
  color: #987b8d;
  font-size: 13px;
}
.tag-stack {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.content-block label {
  display: block;
  color: #b86b8b;
  font-weight: 700;
  margin-bottom: 6px;
}
.content-block p {
  margin: 0;
  color: #514651;
  line-height: 1.7;
  white-space: pre-wrap;
}
.link-line {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  color: #7d6a78;
}
.link-line span {
  color: #b86b8b;
  font-weight: 700;
}
.link-line a {
  color: #8a3d61;
  word-break: break-all;
}
.link-line em {
  font-style: normal;
}
.review-box {
  margin-top: 14px;
  padding: 12px;
  border-radius: 8px;
  background: #fff7fa;
  color: #514651;
}
.review-box div {
  display: inline-flex;
  gap: 6px;
  margin-right: 16px;
  margin-bottom: 8px;
}
.review-box span {
  color: #b86b8b;
  font-weight: 700;
}
.review-box p {
  margin: 0;
  line-height: 1.7;
  white-space: pre-wrap;
}
.card-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 12px;
}
.danger-text {
  color: #d85b6a;
}
.history-item {
  padding: 12px;
  border-radius: 8px;
  background: #fff7fa;
}
.history-item p {
  margin: 8px 0;
  color: #514651;
}
.history-item small {
  color: #7d6a78;
}
</style>
