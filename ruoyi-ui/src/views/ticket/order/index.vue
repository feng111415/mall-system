<template>
  <crud-page module-name="order" title-name="订单" primary-key="orderId" permission="ticket:order" :columns="columns" :form-fields="formFields" :search-fields="searchFields" :defaults="defaults" />
</template>

<script>
import CrudPage from '../components/CrudPage'
import { orderStatus, payStatus } from '../options'

export default {
  name: 'TicketOrder',
  components: { CrudPage },
  data() {
    return {
      defaults: { payStatus: '0', orderStatus: '0', source: '0', discountAmount: 0 },
      searchFields: [
        { label: '订单号', prop: 'orderNo' },
        { label: '用户ID', prop: 'userId', type: 'number' },
        { label: '订单状态', prop: 'orderStatus', type: 'select', options: orderStatus },
        { label: '支付状态', prop: 'payStatus', type: 'select', options: payStatus }
      ],
      columns: [
        { label: 'ID', prop: 'orderId', width: 80 },
        { label: '订单号', prop: 'orderNo', width: 190 },
        { label: '用户ID', prop: 'userId', width: 90 },
        { label: '活动ID', prop: 'activityId', width: 90 },
        { label: '数量', prop: 'totalQuantity', width: 80 },
        { label: '应付', prop: 'payAmount', width: 100 },
        { label: '订单状态', prop: 'orderStatus', options: orderStatus, width: 100 },
        { label: '支付状态', prop: 'payStatus', options: payStatus, width: 100 },
        { label: '过期时间', prop: 'expireTime', type: 'datetime', width: 170 }
      ],
      formFields: [
        { label: '订单号', prop: 'orderNo' },
        { label: '用户ID', prop: 'userId', type: 'number' },
        { label: 'OpenID', prop: 'openid' },
        { label: '活动ID', prop: 'activityId', type: 'number' },
        { label: '场次ID', prop: 'sessionId', type: 'number' },
        { label: '订单标题', prop: 'orderTitle', span: 24 },
        { label: '数量', prop: 'totalQuantity', type: 'number' },
        { label: '总金额', prop: 'totalAmount', type: 'money' },
        { label: '优惠金额', prop: 'discountAmount', type: 'money' },
        { label: '实付金额', prop: 'payAmount', type: 'money' },
        { label: '订单状态', prop: 'orderStatus', type: 'select', options: orderStatus },
        { label: '支付状态', prop: 'payStatus', type: 'select', options: payStatus },
        { label: '过期时间', prop: 'expireTime', type: 'datetime' },
        { label: '备注', prop: 'remark', type: 'textarea', span: 24 }
      ]
    }
  }
}
</script>
