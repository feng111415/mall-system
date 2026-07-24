<template>
  <crud-page module-name="session" title-name="场次" primary-key="sessionId" permission="ticket:session" :columns="columns" :form-fields="formFields" :search-fields="searchFields" :defaults="defaults" />
</template>

<script>
import CrudPage from '../components/CrudPage'
import { sessionStatus } from '../options'

export default {
  name: 'TicketSession',
  components: { CrudPage },
  data() {
    return {
      defaults: { status: '1', sort: 0, totalStock: 0, soldStock: 0, lockedStock: 0 },
      searchFields: [
        { label: '活动ID', prop: 'activityId', type: 'number' },
        { label: '场次名称', prop: 'sessionName' },
        { label: '状态', prop: 'status', type: 'select', options: sessionStatus }
      ],
      columns: [
        { label: 'ID', prop: 'sessionId', width: 80 },
        { label: '活动ID', prop: 'activityId', width: 90 },
        { label: '场次名称', prop: 'sessionName' },
        { label: '场次编码', prop: 'sessionCode', width: 170 },
        { label: '开始时间', prop: 'sessionStart', type: 'datetime', width: 170 },
        { label: '总库存', prop: 'totalStock', width: 90 },
        { label: '已售', prop: 'soldStock', width: 80 },
        { label: '锁定', prop: 'lockedStock', width: 80 },
        { label: '状态', prop: 'status', options: sessionStatus, width: 100 }
      ],
      formFields: [
        { label: '活动ID', prop: 'activityId', type: 'number' },
        { label: '场次名称', prop: 'sessionName' },
        { label: '场次编码', prop: 'sessionCode' },
        { label: '状态', prop: 'status', type: 'select', options: sessionStatus },
        { label: '开始时间', prop: 'sessionStart', type: 'datetime' },
        { label: '结束时间', prop: 'sessionEnd', type: 'datetime' },
        { label: '开售时间', prop: 'saleStartTime', type: 'datetime' },
        { label: '停售时间', prop: 'saleEndTime', type: 'datetime' },
        { label: '场馆名称', prop: 'venueName' },
        { label: '总库存', prop: 'totalStock', type: 'number' },
        { label: '已售库存', prop: 'soldStock', type: 'number' },
        { label: '锁定库存', prop: 'lockedStock', type: 'number' },
        { label: '排序', prop: 'sort', type: 'number' },
        { label: '备注', prop: 'remark', type: 'textarea', span: 24 }
      ]
    }
  }
}
</script>
