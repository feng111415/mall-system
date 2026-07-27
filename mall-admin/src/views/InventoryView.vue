<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adjustInventory, getInventories, getInventoryLogs } from '../api/mall'

const rows = ref([])
const loading = ref(false)
const keyword = ref('')
const lowOnly = ref(false)
const dialog = ref(false)
const logDialog = ref(false)
const logs = ref([])
const current = ref(null)
const form = reactive({ delta: 0, warningThreshold: null, reason: '' })

async function load() {
  loading.value = true
  try {
    const response = await getInventories({ skuCode: keyword.value, lowStockOnly: lowOnly.value, pageNum: 1, pageSize: 100 })
    rows.value = response.data.data?.rows || response.data.data || []
  } catch (error) {
    rows.value = []
    ElMessage.error(error.response?.data?.msg || '库存加载失败')
  } finally { loading.value = false }
}
function openAdjust(row) {
  current.value = row
  Object.assign(form, { delta: 0, warningThreshold: row.warningThreshold, reason: '' })
  dialog.value = true
}
async function submitAdjust() {
  if (!form.delta || !form.reason.trim()) return ElMessage.warning('请填写调整数量和原因')
  try { await adjustInventory(current.value.skuId, form); ElMessage.success('库存调整成功'); dialog.value = false; load() }
  catch (error) { ElMessage.error(error.response?.data?.msg || '库存调整失败') }
}
async function showLogs(row) {
  current.value = row
  try { logs.value = (await getInventoryLogs(row.skuId)).data.data || []; logDialog.value = true }
  catch (error) { ElMessage.error(error.response?.data?.msg || '流水加载失败') }
}
onMounted(load)
</script>

<template>
  <section>
    <div class="page-title">
      <div><span class="eyebrow">交易核心 / INVENTORY</span><h1>库存管理</h1><p>可用、锁定、已售分开统计，所有变更都保留来源和流水。</p></div>
    </div>
    <div class="panel table-panel">
      <div class="table-toolbar">
        <div class="tabs"><button :class="{ active: !lowOnly }" @click="lowOnly = false; load()">全部库存</button><button :class="{ active: lowOnly }" @click="lowOnly = true; load()">低库存预警</button></div>
        <div class="table-filters"><input v-model="keyword" placeholder="SKU 编码" @keyup.enter="load"><button class="outline-button" @click="load">查询</button></div>
      </div>
      <table v-loading="loading"><thead><tr><th>SKU 编码</th><th>规格名称</th><th>可用库存</th><th>锁定库存</th><th>已售数量</th><th>预警阈值</th><th>状态</th><th>操作</th></tr></thead>
        <tbody><tr v-for="row in rows" :key="row.skuId"><td class="order-no">{{ row.skuCode }}</td><td>{{ row.skuName || '-' }}</td><td :class="row.availableQuantity <= row.warningThreshold ? 'danger' : ''">{{ row.availableQuantity }}</td><td>{{ row.lockedQuantity }}</td><td>{{ row.soldQuantity }}</td><td>{{ row.warningThreshold }}</td><td><span class="status" :class="row.availableQuantity <= row.warningThreshold ? 'warning' : 'success'">{{ row.availableQuantity <= row.warningThreshold ? '需要补货' : '正常' }}</span></td><td><button class="link-button" @click="openAdjust(row)">调整</button><button class="link-button" @click="showLogs(row)">流水</button></td></tr><tr v-if="!rows.length"><td colspan="8" class="muted-text">暂无库存数据</td></tr></tbody>
      </table>
    </div>
    <el-dialog v-model="dialog" title="人工调整库存" width="460px"><p class="muted-text">{{ current?.skuCode }} 当前可用 {{ current?.availableQuantity }} 件</p><el-form label-position="top"><el-form-item label="调整数量（负数表示扣减）"><el-input-number v-model="form.delta" :precision="0" :step="1" controls-position="right" /></el-form-item><el-form-item label="低库存阈值"><el-input-number v-model="form.warningThreshold" :min="0" :precision="0" controls-position="right" /></el-form-item><el-form-item label="调整原因"><el-input v-model="form.reason" type="textarea" maxlength="255" show-word-limit /></el-form-item></el-form><template #footer><el-button @click="dialog = false">取消</el-button><el-button type="primary" @click="submitAdjust">确认调整</el-button></template></el-dialog>
    <el-dialog v-model="logDialog" title="库存流水" width="850px"><table><thead><tr><th>时间</th><th>操作</th><th>来源</th><th>可用变化</th><th>锁定变化</th><th>已售变化</th><th>原因</th></tr></thead><tbody><tr v-for="row in logs" :key="row.logId"><td>{{ row.createTime }}</td><td>{{ row.operationType }}</td><td>{{ row.sourceType }} {{ row.sourceNo || '' }}</td><td>{{ row.availableChange }}</td><td>{{ row.lockedChange }}</td><td>{{ row.soldChange }}</td><td>{{ row.reason }}</td></tr></tbody></table></el-dialog>
  </section>
</template>
