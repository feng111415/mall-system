<template>
  <crud-page module-name="refund-log" title-name="退款日志" primary-key="refundId" permission="ticket:refund-log" :columns="columns" :form-fields="formFields" :search-fields="searchFields" :defaults="defaults" />
</template>

<script>
import CrudPage from '../components/CrudPage'

export default {
  name: 'TicketRefundLog',
  components: { CrudPage },
  data() {
    const status = [
      { label: '申请中', value: '0', tag: 'warning' },
      { label: '成功', value: '1', tag: 'success' },
      { label: '失败', value: '2', tag: 'danger' },
      { label: '关闭', value: '3', tag: 'info' }
    ]
    return {
      defaults: { refundStatus: '0' },
      searchFields: [
        { label: '订单号', prop: 'orderNo' },
        { label: '退款单号', prop: 'refundNo' },
        { label: '退款状态', prop: 'refundStatus', type: 'select', options: status }
      ],
      columns: [
        { label: 'ID', prop: 'refundId', width: 80 },
        { label: '订单号', prop: 'orderNo', width: 190 },
        { label: '退款单号', prop: 'refundNo', width: 190 },
        { label: '金额', prop: 'refundAmount', width: 100 },
        { label: '状态', prop: 'refundStatus', options: status, width: 90 },
        { label: '回调时间', prop: 'notifyTime', type: 'datetime', width: 170 }
      ],
      formFields: [
        { label: '订单ID', prop: 'orderId', type: 'number' },
        { label: '订单号', prop: 'orderNo' },
        { label: '退款单号', prop: 'refundNo' },
        { label: '用户ID', prop: 'userId', type: 'number' },
        { label: '退款金额', prop: 'refundAmount', type: 'money' },
        { label: '退款状态', prop: 'refundStatus', type: 'select', options: status },
        { label: '退款原因', prop: 'refundReason', span: 24 },
        { label: '备注', prop: 'remark', type: 'textarea', span: 24 }
      ]
    }
  }
}
</script>
