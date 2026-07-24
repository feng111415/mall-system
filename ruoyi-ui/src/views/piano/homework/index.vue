<template>
  <div class="piano-page homework-page">
    <div class="piano-hero">
      <div>
        <div class="eyebrow">Practice Plan</div>
        <h2>作业管理</h2>
        <p>布置练习内容，设置提交截止时间，跟踪每份作业状态。</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd" v-hasPermi="['piano:homework:add']">布置作业</el-button>
    </div>

    <div class="status-tabs">
      <button :class="{ active: queryParams.homeworkStatus === undefined }" @click="setStatus(undefined)">全部</button>
      <button v-for="item in statusOptions" :key="item.value" :class="{ active: queryParams.homeworkStatus === item.value }" @click="setStatus(item.value)">
        {{ item.label }}
      </button>
    </div>

    <div class="filter-band">
      <el-form ref="queryForm" :model="queryParams" size="small" :inline="true">
        <el-form-item label="学生" prop="studentName">
          <el-input v-model="queryParams.studentName" placeholder="学生姓名" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item label="标题" prop="homeworkTitle">
          <el-input v-model="queryParams.homeworkTitle" placeholder="作业标题" clearable @keyup.enter.native="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
          <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
          <el-button type="warning" plain icon="el-icon-download" @click="handleExport" v-hasPermi="['piano:homework:export']">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-empty v-if="!loading && homeworkList.length === 0" description="还没有作业记录" />
    <div v-loading="loading" class="homework-list">
      <div v-for="item in homeworkList" :key="item.homeworkId" class="homework-card">
        <div class="homework-head">
          <div>
            <h3>{{ item.homeworkTitle }}</h3>
            <div class="sub-line">{{ item.studentName || '未知学生' }} · 截止 {{ parseTime(item.submitDeadline) || '未设置' }}</div>
            <div class="submit-code">
              <span>提交码 {{ item.submitCode || '生成中' }}</span>
              <el-button
                v-if="item.submitCode"
                type="text"
                icon="el-icon-document-copy"
                v-clipboard:copy="item.submitCode"
                v-clipboard:success="clipboardSuccess"
              >复制</el-button>
            </div>
          </div>
          <el-tag :type="statusType(item.homeworkStatus)">{{ statusLabel(item.homeworkStatus) }}</el-tag>
        </div>
        <div class="content-block">
          <label>练习内容</label>
          <p>{{ item.practiceContent }}</p>
        </div>
        <div class="content-block muted">
          <label>练习要求</label>
          <p>{{ item.practiceRequirement || '未填写' }}</p>
        </div>
        <div class="card-actions">
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(item)" v-hasPermi="['piano:homework:edit']">编辑</el-button>
          <el-button type="text" icon="el-icon-delete" class="danger-text" @click="handleDelete(item)" v-hasPermi="['piano:homework:remove']">删除</el-button>
        </div>
      </div>
    </div>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学生" prop="studentId">
              <el-select v-model="form.studentId" placeholder="请选择学生" filterable style="width: 100%">
                <el-option v-for="item in studentOptions" :key="item.studentId" :label="item.studentName" :value="item.studentId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止时间" prop="submitDeadline">
              <el-date-picker v-model="form.submitDeadline" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择截止时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="作业标题" prop="homeworkTitle">
              <el-input v-model="form.homeworkTitle" placeholder="请输入作业标题" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="练习内容" prop="practiceContent">
              <el-input v-model="form.practiceContent" type="textarea" :rows="4" placeholder="请输入本次练习内容" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="练习要求" prop="practiceRequirement">
              <el-input v-model="form.practiceRequirement" type="textarea" :rows="3" placeholder="请输入速度、重点小节、注意事项等" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="homeworkStatus">
              <el-select v-model="form.homeworkStatus" style="width: 100%">
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="老师备注" prop="teacherRemark">
              <el-input v-model="form.teacherRemark" type="textarea" :rows="2" placeholder="仅老师可见的备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listHomework, getHomework, delHomework, addHomework, updateHomework } from '@/api/piano/homework'
import { listStudentOptions } from '@/api/piano/student'

export default {
  name: 'PianoHomework',
  data() {
    return {
      loading: false,
      total: 0,
      homeworkList: [],
      studentOptions: [],
      open: false,
      title: '',
      statusOptions: [
        { value: '0', label: '待提交', type: 'info' },
        { value: '1', label: '待点评', type: 'warning' },
        { value: '2', label: '需订正', type: 'danger' },
        { value: '3', label: '已完成', type: 'success' },
        { value: '4', label: '已逾期', type: 'danger' }
      ],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentName: undefined,
        homeworkTitle: undefined,
        homeworkStatus: undefined
      },
      form: {},
      rules: {
        studentId: [{ required: true, message: '学生不能为空', trigger: 'change' }],
        homeworkTitle: [
          { required: true, message: '作业标题不能为空', trigger: 'blur' },
          { max: 100, message: '作业标题不能超过100个字符', trigger: 'blur' }
        ],
        practiceContent: [{ required: true, message: '练习内容不能为空', trigger: 'blur' }],
        practiceRequirement: [{ max: 1000, message: '练习要求不能超过1000个字符', trigger: 'blur' }],
        homeworkStatus: [{ required: true, message: '状态不能为空', trigger: 'change' }],
        teacherRemark: [{ max: 500, message: '老师备注不能超过500个字符', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getStudentOptions()
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listHomework(this.queryParams).then(response => {
        this.homeworkList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    getStudentOptions() {
      listStudentOptions().then(response => {
        this.studentOptions = response.data || []
      })
    },
    statusLabel(value) {
      const item = this.statusOptions.find(option => option.value === value)
      return item ? item.label : value
    },
    statusType(value) {
      const item = this.statusOptions.find(option => option.value === value)
      return item ? item.type : ''
    },
    setStatus(status) {
      this.queryParams.homeworkStatus = status
      this.handleQuery()
    },
    reset() {
      this.form = {
        homeworkId: undefined,
        studentId: undefined,
        homeworkTitle: undefined,
        practiceContent: undefined,
        practiceRequirement: undefined,
        submitCode: undefined,
        submitDeadline: undefined,
        homeworkStatus: '0',
        teacherRemark: undefined
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
      this.queryParams.homeworkStatus = undefined
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '布置作业'
    },
    handleUpdate(row) {
      this.reset()
      getHomework(row.homeworkId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = '编辑作业'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.homeworkId ? updateHomework : addHomework
        request(this.form).then(() => {
          this.$modal.msgSuccess(this.form.homeworkId ? '修改成功' : '布置成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      this.$modal.confirm(`是否确认删除作业“${row.homeworkTitle}”？`).then(() => {
        return delHomework(row.homeworkId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download('/piano/homework/export', { ...this.queryParams }, `piano_homework_${new Date().getTime()}.xlsx`)
    },
    clipboardSuccess() {
      this.$modal.msgSuccess('提交码已复制')
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
.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}
.status-tabs button {
  border: 1px solid #efd8e3;
  background: #fff;
  color: #76596b;
  border-radius: 18px;
  padding: 8px 14px;
  cursor: pointer;
}
.status-tabs button.active {
  background: #b86b8b;
  color: #fff;
  border-color: #b86b8b;
}
.filter-band {
  padding: 16px 18px 2px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #f2dce7;
}
.homework-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  min-height: 180px;
}
.homework-card {
  padding: 18px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #efd8e3;
  box-shadow: 0 8px 22px rgba(184, 107, 139, 0.08);
}
.homework-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.homework-head h3 {
  margin: 0 0 6px;
  color: #342b35;
}
.sub-line {
  color: #987b8d;
  font-size: 13px;
}
.submit-code {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  padding: 5px 9px;
  border-radius: 6px;
  color: #8a536c;
  background: #fff4f8;
  border: 1px solid #f1d5e2;
  font-size: 12px;
  font-weight: 700;
}
.submit-code .el-button {
  padding: 0;
  color: #b86b8b;
}
.content-block {
  margin-top: 12px;
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
.content-block.muted p {
  color: #7d6a78;
}
.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 12px;
}
.danger-text {
  color: #d85b6a;
}
</style>
