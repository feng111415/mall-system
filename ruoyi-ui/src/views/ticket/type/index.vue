<template>
  <crud-page module-name="type" title-name="票种" primary-key="typeId" permission="ticket:type" :columns="columns" :form-fields="formFields" :search-fields="searchFields" :defaults="defaults" />
</template>

<script>
import CrudPage from '../components/CrudPage'
import { normalStatus } from '../options'

export default {
  name: 'TicketType',
  components: { CrudPage },
  data() {
    return {
      defaults: { status: '0', minBuy: 1, maxBuy: 1, totalStock: 0, soldStock: 0, lockedStock: 0, sort: 0 },
      searchFields: [
        { label: '活动ID', prop: 'activityId', type: 'number' },
        { label: '场次ID', prop: 'sessionId', type: 'number' },
        { label: '票种名称', prop: 'typeName' },
        { label: '状态', prop: 'status', type: 'select', options: normalStatus }
      ],
      columns: [
        { label: 'ID', prop: 'typeId', width: 80 },
        { label: '活动ID', prop: 'activityId', width: 90 },
        { label: '场次ID', prop: 'sessionId', width: 90 },
        { label: '票种名称', prop: 'typeName' },
        { label: '票价', prop: 'price', width: 100 },
        { label: '总库存', prop: 'totalStock', width: 90 },
        { label: '已售', prop: 'soldStock', width: 80 },
        { label: '锁定', prop: 'lockedStock', width: 80 },
        { label: '状态', prop: 'status', options: normalStatus, width: 90 }
      ],
      formFields: [
        { label: '活动ID', prop: 'activityId', type: 'number' },
        { label: '场次ID', prop: 'sessionId', type: 'number' },
        { label: '票种名称', prop: 'typeName' },
        { label: '票种编码', prop: 'typeCode' },
        { label: '票价', prop: 'price', type: 'money' },
        { label: '市场价', prop: 'marketPrice', type: 'money' },
        { label: '总库存', prop: 'totalStock', type: 'number' },
        { label: '已售库存', prop: 'soldStock', type: 'number' },
        { label: '锁定库存', prop: 'lockedStock', type: 'number' },
        { label: '最小购买', prop: 'minBuy', type: 'number' },
        { label: '最大购买', prop: 'maxBuy', type: 'number' },
        { label: '状态', prop: 'status', type: 'select', options: normalStatus },
        { label: '排序', prop: 'sort', type: 'number' },
        { label: '备注', prop: 'remark', type: 'textarea', span: 24 }
      ]
    }
  }
}
</script>
