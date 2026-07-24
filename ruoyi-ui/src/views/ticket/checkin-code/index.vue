<template>
  <crud-page module-name="checkin-code" title-name="核销码" primary-key="codeId" permission="ticket:checkin-code" :columns="columns" :form-fields="formFields" :search-fields="searchFields" :defaults="defaults" />
</template>

<script>
import CrudPage from '../components/CrudPage'

export default {
  name: 'TicketCheckinCode',
  components: { CrudPage },
  data() {
    const status = [
      { label: '未核销', value: '0', tag: 'warning' },
      { label: '已核销', value: '1', tag: 'success' },
      { label: '已作废', value: '2', tag: 'danger' }
    ]
    return {
      defaults: { status: '0' },
      searchFields: [
        { label: '订单号', prop: 'orderNo' },
        { label: '票号', prop: 'ticketNo' },
        { label: '核销码', prop: 'checkinCode' },
        { label: '状态', prop: 'status', type: 'select', options: status }
      ],
      columns: [
        { label: 'ID', prop: 'codeId', width: 80 },
        { label: '订单号', prop: 'orderNo', width: 190 },
        { label: '票号', prop: 'ticketNo', width: 180 },
        { label: '核销码', prop: 'checkinCode', width: 180 },
        { label: '状态', prop: 'status', options: status, width: 90 },
        { label: '核销时间', prop: 'checkinTime', type: 'datetime', width: 170 }
      ],
      formFields: [
        { label: '订单ID', prop: 'orderId', type: 'number' },
        { label: '订单号', prop: 'orderNo' },
        { label: '明细ID', prop: 'itemId', type: 'number' },
        { label: '票号', prop: 'ticketNo' },
        { label: '核销码', prop: 'checkinCode' },
        { label: '状态', prop: 'status', type: 'select', options: status },
        { label: '核销人', prop: 'checkinUser' },
        { label: '核销时间', prop: 'checkinTime', type: 'datetime' },
        { label: '备注', prop: 'remark', type: 'textarea', span: 24 }
      ]
    }
  }
}
</script>
