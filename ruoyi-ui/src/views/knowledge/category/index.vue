<template>
  <div class="app-container">
    <el-form :model="queryParams" :inline="true" size="small">
      <el-form-item label="分类名称">
        <el-input
          v-model="queryParams.categoryName"
          placeholder="请输入分类名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="categoryList">
      <el-table-column label="编号" prop="categoryId" width="100" />
      <el-table-column label="分类名称" prop="categoryName" />
      <el-table-column label="显示顺序" prop="orderNum" width="120" />
      <el-table-column label="状态" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
            {{ scope.row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="scope">
          <el-button type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
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
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" controls-position="right" />
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
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  listCategory,
  getCategory,
  addCategory,
  updateCategory,
  delCategory
} from '@/api/knowledge/category'

export default {
  name: 'KbCategory',
  data() {
    return {
      loading: false,
      total: 0,
      categoryList: [],
      open: false,
      title: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        categoryName: undefined
      },
      form: {},
      rules: {
        categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
        orderNum: [{ required: true, message: '显示顺序不能为空', trigger: 'blur' }],
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
      listCategory(this.queryParams).then(response => {
        this.categoryList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    resetQuery() {
      this.queryParams.categoryName = undefined
      this.handleQuery()
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    reset() {
      this.form = {
        categoryId: undefined,
        categoryName: undefined,
        orderNum: 0,
        status: '0',
        remark: undefined
      }
      this.resetForm('form')
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增分类'
    },
    handleUpdate(row) {
      this.reset()
      getCategory(row.categoryId).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = '修改分类'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const action = this.form.categoryId ? updateCategory : addCategory
        action(this.form).then(() => {
          this.$modal.msgSuccess(this.form.categoryId ? '修改成功' : '新增成功')
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
      this.$modal.confirm('是否确认删除分类“' + row.categoryName + '”？').then(() => {
        return delCategory(row.categoryId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.danger-text {
  color: #f56c6c;
}
</style>
