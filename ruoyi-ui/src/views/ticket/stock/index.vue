<template>
  <crud-page module-name="stock" title-name="库存" primary-key="stockId" permission="ticket:stock" :columns="columns" :form-fields="formFields" :search-fields="searchFields" :defaults="defaults" />
</template>

<script>
import CrudPage from '../components/CrudPage'
import { normalStatus } from '../options'

export default {
  name: 'TicketStock',
  components: { CrudPage },
  data() {
    return {
      defaults: { status: '0', totalStock: 0, availableStock: 0, lockedStock: 0, soldStock: 0, version: 0 },
      searchFields: [
        { label: '活动ID', prop: 'activityId', type: 'number' },
        { label: '场次ID', prop: 'sessionId', type: 'number' },
        { label: '票种ID', prop: 'typeId', type: 'number' }
      ],
      columns: [
        { label: 'ID', prop: 'stockId', width: 80 },
        { label: '活动ID', prop: 'activityId', width: 90 },
        { label: '场次ID', prop: 'sessionId', width: 90 },
        { label: '票种ID', prop: 'typeId', width: 90 },
        { label: '总库存', prop: 'totalStock', width: 90 },
        { label: '可售', prop: 'availableStock', width: 90 },
        { label: '锁定', prop: 'lockedStock', width: 90 },
        { label: '已售', prop: 'soldStock', width: 90 },
        { label: '状态', prop: 'status', options: normalStatus, width: 90 }
      ],
      formFields: [
        { label: '活动ID', prop: 'activityId', type: 'number' },
        { label: '场次ID', prop: 'sessionId', type: 'number' },
        { label: '票种ID', prop: 'typeId', type: 'number' },
        { label: '总库存', prop: 'totalStock', type: 'number' },
        { label: '可售库存', prop: 'availableStock', type: 'number' },
        { label: '锁定库存', prop: 'lockedStock', type: 'number' },
        { label: '已售库存', prop: 'soldStock', type: 'number' },
        { label: '版本', prop: 'version', type: 'number' },
        { label: '状态', prop: 'status', type: 'select', options: normalStatus },
        { label: '备注', prop: 'remark', type: 'textarea', span: 24 }
      ]
    }
  }
}
</script>
