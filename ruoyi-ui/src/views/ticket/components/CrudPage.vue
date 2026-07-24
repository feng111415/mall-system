<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item v-for="field in searchFields" :key="field.prop" :label="field.label" :prop="field.prop">
        <el-input
          v-if="!field.type || field.type === 'text'"
          v-model="queryParams[field.prop]"
          :placeholder="'请输入' + field.label"
          clearable
          @keyup.enter.native="handleQuery"
        />
        <el-input-number
          v-else-if="field.type === 'number'"
          v-model="queryParams[field.prop]"
          :min="0"
          controls-position="right"
          clearable
        />
        <el-select v-else-if="field.type === 'select'" v-model="queryParams[field.prop]" :placeholder="'请选择' + field.label" clearable>
          <el-option v-for="option in field.options || []" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="[`${permission}:add`]">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate()" v-hasPermi="[`${permission}:edit`]">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete()" v-hasPermi="[`${permission}:remove`]">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="[`${permission}:export`]">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="rows" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :label="column.label"
        :prop="column.prop"
        :width="column.width"
        :show-overflow-tooltip="column.tooltip !== false"
        align="center"
      >
        <template slot-scope="scope">
          <span v-if="column.type === 'datetime'">{{ parseTime(scope.row[column.prop]) }}</span>
          <el-tag v-else-if="column.options" size="mini" :type="optionTag(column, scope.row[column.prop])">
            {{ optionLabel(column, scope.row[column.prop]) }}
          </el-tag>
          <span v-else>{{ scope.row[column.prop] }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="[`${permission}:edit`]">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="[`${permission}:remove`]">删除</el-button>
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

    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="16">
          <el-col v-for="field in formFields" :key="field.prop" :span="field.span || 12">
            <el-form-item :label="field.label" :prop="field.prop">
              <el-input v-if="!field.type || field.type === 'text'" v-model="form[field.prop]" :placeholder="'请输入' + field.label" />
              <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop]" type="textarea" :rows="4" :placeholder="'请输入' + field.label" />
              <el-input-number v-else-if="field.type === 'number'" v-model="form[field.prop]" :min="field.min || 0" controls-position="right" style="width: 100%" />
              <el-input-number v-else-if="field.type === 'money'" v-model="form[field.prop]" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
              <el-date-picker v-else-if="field.type === 'datetime'" v-model="form[field.prop]" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择时间" style="width: 100%" />
              <el-select v-else-if="field.type === 'select'" v-model="form[field.prop]" :placeholder="'请选择' + field.label" style="width: 100%">
                <el-option v-for="option in field.options || []" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listTicket, getTicket, addTicket, updateTicket, delTicket } from '@/api/ticket/crud'

export default {
  name: 'TicketCrudPage',
  props: {
    moduleName: { type: String, required: true },
    titleName: { type: String, required: true },
    primaryKey: { type: String, required: true },
    permission: { type: String, required: true },
    columns: { type: Array, required: true },
    formFields: { type: Array, required: true },
    searchFields: { type: Array, default: () => [] },
    defaults: { type: Object, default: () => ({}) }
  },
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      rows: [],
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      form: {},
      rules: {}
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listTicket(this.moduleName, this.queryParams).then(response => {
        this.rows = response.rows || []
        this.total = response.total || 0
        this.loading = false
      })
    },
    optionLabel(column, value) {
      const option = (column.options || []).find(item => item.value === value)
      return option ? option.label : value
    },
    optionTag(column, value) {
      const option = (column.options || []).find(item => item.value === value)
      return option && option.tag ? option.tag : ''
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = Object.assign({}, this.defaults)
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
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item[this.primaryKey])
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = `新增${this.titleName}`
    },
    handleUpdate(row) {
      this.reset()
      const id = row ? row[this.primaryKey] : this.ids[0]
      getTicket(this.moduleName, id).then(response => {
        this.form = response.data || {}
        this.open = true
        this.title = `修改${this.titleName}`
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form[this.primaryKey] !== undefined ? updateTicket : addTicket
        request(this.moduleName, this.form).then(() => {
          this.$modal.msgSuccess(this.form[this.primaryKey] !== undefined ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const ids = row ? row[this.primaryKey] : this.ids
      this.$modal.confirm(`是否确认删除${this.titleName}编号为"${ids}"的数据项？`).then(() => {
        return delTicket(this.moduleName, ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleExport() {
      this.download(`/ticket/${this.moduleName}/export`, {
        ...this.queryParams
      }, `${this.moduleName}_${new Date().getTime()}.xlsx`)
    }
  }
}
</script>
