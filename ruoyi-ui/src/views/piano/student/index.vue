<template>
  <div class="student-page">
    <div class="page-header">
      <div>
        <h2>学生管理</h2>
        <p>测试 test_student 表的增删改查功能</p>
      </div>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增学生</el-button>
    </div>

    <el-form ref="queryForm" :model="queryParams" :inline="true" size="small" class="query-form">
      <el-form-item label="姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="年龄" prop="age">
        <el-input v-model="queryParams.age" placeholder="请输入年龄" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="studentList" border stripe>
      <el-table-column label="编号" prop="studentId" width="100" />
      <el-table-column label="姓名" prop="studentName" min-width="160" />
      <el-table-column label="年龄" prop="age" width="100" />
      <el-table-column label="状态" prop="status" width="120">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
            {{ scope.row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="160" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button type="text" icon="el-icon-delete" class="danger-text" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="studentName">
          <el-input v-model="form.studentName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="form.age" :min="1" :max="150" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="0">正常</el-radio>
            <el-radio label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listStudent, getStudent, delStudent, addStudent, updateStudent } from '@/api/piano/student'

export default {
  name: 'PianoStudent',
  data() {
    return {
      loading: false,
      total: 0,
      studentList: [],
      open: false,
      title: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        studentName: undefined,
        age: undefined,
        status: undefined
      },
      form: {},
      rules: {
        studentName: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
        age: [{ required: true, message: '年龄不能为空', trigger: 'blur' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listStudent(this.queryParams).then(response => {
        this.studentList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    reset() {
      this.form = {
        studentId: undefined,
        studentName: undefined,
        age: undefined,
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增学生'
    },
    handleUpdate(row) {
      this.reset()
      getStudent(row.studentId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = '编辑学生'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const action = this.form.studentId ? updateStudent : addStudent
        action(this.form).then(() => {
          this.$modal.msgSuccess(this.form.studentId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除学生“' + row.studentName + '”？').then(() => {
        return delStudent(row.studentId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.student-page {
  min-height: calc(100vh - 84px);
  padding: 20px;
  background: #f6f8fb;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  margin-bottom: 16px;
  background: #fff;
  border: 1px solid #e6e9ef;
  border-radius: 6px;
}
.page-header h2 {
  margin: 0 0 8px;
  color: #303133;
  font-size: 22px;
}
.page-header p {
  margin: 0;
  color: #909399;
}
.query-form {
  padding: 18px 18px 2px;
  margin-bottom: 16px;
  background: #fff;
  border: 1px solid #e6e9ef;
  border-radius: 6px;
}
.danger-text {
  color: #f56c6c;
}
</style>
