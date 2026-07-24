<template>
  <crud-page module-name="pay-log" title-name="支付日志" primary-key="payId" permission="ticket:pay-log" :columns="columns" :form-fields="formFields" :search-fields="searchFields" :defaults="defaults" />
</template>

<script>
import CrudPage from '../components/CrudPage'

export default {
  name: 'TicketPayLog',
  components: { CrudPage },
  data() {
    const status = [
      { label: '待支付', value: '0', tag: 'warning' },
      { label: '成功', value: '1', tag: 'success' },
      { label: '失败', value: '2', tag: 'danger' },
      { label: '关闭', value: '3', tag: 'info' }
    ]
    return {
      defaults: { payChannel: '0', payStatus: '0' },
      searchFields: [
        { label: '订单号', prop: 'orderNo' },
        { label: '支付流水', prop: 'payNo' },
        { label: '支付状态', prop: 'payStatus', type: 'select', options: status }
      ],
      columns: [
        { label: 'ID', prop: 'payId', width: 80 },
        { label: '订单号', prop: 'orderNo', width: 190 },
        { label: '支付流水', prop: 'payNo', width: 190 },
        { label: '金额', prop: 'payAmount', width: 100 },
        { label: '状态', prop: 'payStatus', options: status, width: 90 },
        { label: '回调时间', prop: 'notifyTime', type: 'datetime', width: 170 }
      ],
      formFields: [
        { label: '订单ID', prop: 'orderId', type: 'number' },
        { label: '订单号', prop: 'orderNo' },
        { label: '支付流水', prop: 'payNo' },
        { label: '用户ID', prop: 'userId', type: 'number' },
        { label: 'OpenID', prop: 'openid' },
        { label: '支付金额', prop: 'payAmount', type: 'money' },
        { label: '支付状态', prop: 'payStatus', type: 'select', options: status },
        { label: '微信交易号', prop: 'transactionId' },
        { label: '备注', prop: 'remark', type: 'textarea', span: 24 }
      ]
    }
  }
}
</script>
