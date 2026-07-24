<template>
  <div class="app-container">
    <el-card shadow="never">
      <div slot="header">模拟抢票下单</div>
      <el-form ref="form" :model="form" label-width="120px" style="max-width: 720px">
        <el-form-item label="活动ID">
          <el-input-number v-model="form.activityId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="场次ID">
          <el-input-number v-model="form.sessionId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="票种ID">
          <el-input-number v-model="form.typeId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input-number v-model="form.userId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="OpenID">
          <el-input v-model="form.openid" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交抢票</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="result" type="success" :closable="false" style="margin-top: 16px">
        <pre>{{ result }}</pre>
      </el-alert>
    </el-card>
  </div>
</template>

<script>
import { rushOrder } from '@/api/ticket/crud'

export default {
  name: 'TicketRush',
  data() {
    return {
      form: {
        activityId: 1,
        sessionId: 1,
        typeId: 1,
        quantity: 1,
        userId: 1,
        openid: 'admin-openid'
      },
      result: ''
    }
  },
  methods: {
    submit() {
      rushOrder(this.form).then(response => {
        this.result = JSON.stringify(response.data || response, null, 2)
        this.$modal.msgSuccess('下单成功')
      })
    }
  }
}
</script>
