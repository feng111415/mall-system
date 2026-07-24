<template>
  <crud-page
    module-name="activity"
    title-name="活动"
    primary-key="activityId"
    permission="ticket:activity"
    :columns="columns"
    :form-fields="formFields"
    :search-fields="searchFields"
    :defaults="defaults"
  />
</template>

<script>
import CrudPage from '../components/CrudPage'
import { activityStatus, yesNo } from '../options'

export default {
  name: 'TicketActivity',
  components: { CrudPage },
  data() {
    return {
      defaults: { status: '1', delFlag: '0', needRealName: '1', needSeat: '0', purchaseLimit: 1, orderTimeout: 900 },
      searchFields: [
        { label: '活动名称', prop: 'activityName' },
        { label: '活动编码', prop: 'activityCode' },
        { label: '状态', prop: 'status', type: 'select', options: activityStatus }
      ],
      columns: [
        { label: 'ID', prop: 'activityId', width: 80 },
        { label: '活动名称', prop: 'activityName' },
        { label: '活动编码', prop: 'activityCode', width: 170 },
        { label: '场馆', prop: 'venueName' },
        { label: '开售时间', prop: 'saleStartTime', type: 'datetime', width: 170 },
        { label: '开始时间', prop: 'startTime', type: 'datetime', width: 170 },
        { label: '限购', prop: 'purchaseLimit', width: 80 },
        { label: '状态', prop: 'status', options: activityStatus, width: 90 }
      ],
      formFields: [
        { label: '活动名称', prop: 'activityName' },
        { label: '活动编码', prop: 'activityCode' },
        { label: '活动类型', prop: 'activityType' },
        { label: '状态', prop: 'status', type: 'select', options: activityStatus },
        { label: '场馆名称', prop: 'venueName' },
        { label: '场馆地址', prop: 'venueAddress' },
        { label: '开售时间', prop: 'saleStartTime', type: 'datetime' },
        { label: '停售时间', prop: 'saleEndTime', type: 'datetime' },
        { label: '开始时间', prop: 'startTime', type: 'datetime' },
        { label: '结束时间', prop: 'endTime', type: 'datetime' },
        { label: '每人限购', prop: 'purchaseLimit', type: 'number' },
        { label: '支付超时秒', prop: 'orderTimeout', type: 'number' },
        { label: '是否实名', prop: 'needRealName', type: 'select', options: yesNo },
        { label: '是否选座', prop: 'needSeat', type: 'select', options: yesNo },
        { label: '封面图', prop: 'coverUrl', span: 24 },
        { label: '活动详情', prop: 'description', type: 'textarea', span: 24 },
        { label: '备注', prop: 'remark', type: 'textarea', span: 24 }
      ]
    }
  }
}
</script>
