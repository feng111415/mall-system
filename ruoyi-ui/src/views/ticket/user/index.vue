<template>
  <crud-page module-name="user" title-name="小程序用户" primary-key="userId" permission="ticket:user" :columns="columns" :form-fields="formFields" :search-fields="searchFields" :defaults="defaults" />
</template>

<script>
import CrudPage from '../components/CrudPage'
import { normalStatus, yesNo } from '../options'

export default {
  name: 'TicketUser',
  components: { CrudPage },
  data() {
    return {
      defaults: { status: '0', delFlag: '0', realNameStatus: '0' },
      searchFields: [
        { label: 'OpenID', prop: 'openid' },
        { label: '手机号', prop: 'mobile' },
        { label: '状态', prop: 'status', type: 'select', options: normalStatus }
      ],
      columns: [
        { label: 'ID', prop: 'userId', width: 80 },
        { label: 'OpenID', prop: 'openid' },
        { label: '昵称', prop: 'nickName' },
        { label: '手机号', prop: 'mobile', width: 120 },
        { label: '实名', prop: 'realNameStatus', options: yesNo, width: 90 },
        { label: '状态', prop: 'status', options: normalStatus, width: 90 },
        { label: '最后登录', prop: 'lastLoginTime', type: 'datetime', width: 170 }
      ],
      formFields: [
        { label: 'OpenID', prop: 'openid' },
        { label: 'UnionID', prop: 'unionid' },
        { label: '昵称', prop: 'nickName' },
        { label: '手机号', prop: 'mobile' },
        { label: '头像', prop: 'avatar', span: 24 },
        { label: '实名状态', prop: 'realNameStatus', type: 'select', options: yesNo },
        { label: '状态', prop: 'status', type: 'select', options: normalStatus },
        { label: '备注', prop: 'remark', type: 'textarea', span: 24 }
      ]
    }
  }
}
</script>
