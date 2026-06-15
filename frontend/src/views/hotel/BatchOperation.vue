<template>
  <div class="batch-operation-container" v-if="hasPermission('hotel:room:batch:log')">
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="queryParams.batchNo" placeholder="批次号" clearable style="width: 180px" @keyup.enter="handleSearch" />
        <el-select v-model="queryParams.operationType" placeholder="操作类型" clearable style="width: 180px">
          <el-option v-for="t in operationTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-input v-model="queryParams.operatorName" placeholder="操作人" clearable style="width: 150px" @keyup.enter="handleSearch" />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 280px"
        />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>重置
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        stripe
        border
        v-loading="tableLoading"
        style="width: 100%"
      >
        <el-table-column prop="batchNo" label="批次号" width="200" align="center" />
        <el-table-column label="操作类型" width="150" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTag(row.operationType)" size="small">
              {{ getOperationTypeLabel(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="操作原因" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reason || '-' }}</template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" align="center">
          <template #default="{ row }">{{ row.operatorName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180" align="center" />
        <el-table-column prop="totalCount" label="总数" width="80" align="center" />
        <el-table-column label="成功" width="80" align="center">
          <template #default="{ row }">
            <span style="color: #67C23A; font-weight: 600">{{ row.successCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="失败" width="80" align="center">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: 600">{{ row.failCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="跳过" width="80" align="center">
          <template #default="{ row }">
            <span style="color: #909399">{{ row.skipCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small" effect="light">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailDialogVisible"
      title="批量操作详情"
      width="1000px"
      :close-on-click-modal="false"
    >
      <div class="detail-section" v-if="currentBatch">
        <el-descriptions :column="3" border size="small" class="batch-info">
          <el-descriptions-item label="批次号">{{ currentBatch.batchNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTypeTag(currentBatch.operationType)" size="small">
              {{ getOperationTypeLabel(currentBatch.operationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTag(currentBatch.status)" size="small" effect="light">
              {{ getStatusLabel(currentBatch.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作原因" :span="3">{{ currentBatch.reason || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ currentBatch.operatorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ currentBatch.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ currentBatch.finishTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="总数">{{ currentBatch.totalCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="成功">
            <span style="color: #67C23A; font-weight: 600">{{ currentBatch.successCount || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="失败">
            <span style="color: #F56C6C; font-weight: 600">{{ currentBatch.failCount || 0 }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="跳过">
            <span style="color: #909399">{{ currentBatch.skipCount || 0 }}</span>
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-table-title">操作明细</div>
        <el-table
          :data="detailList"
          stripe
          border
          v-loading="detailLoading"
          style="width: 100%; max-height: 400px"
          max-height="400"
        >
          <el-table-column prop="targetNo" label="目标编号" width="150" align="center">
            <template #default="{ row }">{{ row.targetNo || row.roomNo || row.orderNo || '-' }}</template>
          </el-table-column>
          <el-table-column label="结果" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.result === 1" type="success" size="small">成功</el-tag>
              <el-tag v-else-if="row.result === 2" type="danger" size="small">失败</el-tag>
              <el-tag v-else type="info" size="small">跳过</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="failReason" label="失败原因" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">{{ row.failReason || '-' }}</template>
          </el-table-column>
          <el-table-column label="原值" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="old-value">{{ formatValue(row.oldValue) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="新值" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="new-value">{{ formatValue(row.newValue) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const operationTypeOptions = [
  { value: '', label: '全部' },
  { value: 1, label: '批量修改状态' },
  { value: 2, label: '批量修改属性' },
  { value: 3, label: '批量删除' }
]

const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)
const dateRange = ref([])

const queryParams = reactive({
  batchNo: '',
  operationType: '',
  operatorName: '',
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 10
})

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const currentBatch = ref(null)
const detailList = ref([])

const getOperationTypeLabel = (type) => {
  const map = { 1: '批量修改状态', 2: '批量修改属性', 3: '批量删除' }
  return map[type] || '未知'
}

const getOperationTypeTag = (type) => {
  const map = { 1: 'primary', 2: 'warning', 3: 'danger' }
  return map[type] || 'info'
}

const getStatusLabel = (status) => {
  const map = { 1: '处理中', 2: '已完成', 3: '部分失败' }
  return map[status] || '未知'
}

const getStatusTag = (status) => {
  const map = { 1: 'warning', 2: 'success', 3: 'danger' }
  return map[status] || 'info'
}

const formatValue = (val) => {
  if (val === null || val === undefined || val === '') return '-'
  if (typeof val === 'object') {
    try {
      return JSON.stringify(val)
    } catch {
      return String(val)
    }
  }
  return String(val)
}

const loadList = async () => {
  tableLoading.value = true
  try {
    const params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    } else {
      delete params.startTime
      delete params.endTime
    }
    if (!params.operationType) {
      delete params.operationType
    }
    const res = await api.hotel.getBatchOperationPage(params)
    if (res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    tableLoading.value = false
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadList()
}

const handleReset = () => {
  queryParams.batchNo = ''
  queryParams.operationType = ''
  queryParams.operatorName = ''
  queryParams.startTime = ''
  queryParams.endTime = ''
  queryParams.pageNum = 1
  dateRange.value = []
  loadList()
}

const handleViewDetail = async (row) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  currentBatch.value = null
  detailList.value = []
  try {
    const [batchRes, detailRes] = await Promise.all([
      api.hotel.getBatchOperation(row.batchNo),
      api.hotel.getBatchOperationDetails(row.batchNo)
    ])
    if (batchRes.code === 200) {
      currentBatch.value = batchRes.data
    }
    if (detailRes.code === 200) {
      detailList.value = detailRes.data?.records || detailRes.data?.list || detailRes.data || []
    }
  } catch {
    currentBatch.value = row
    detailList.value = []
  } finally {
    detailLoading.value = false
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.batch-operation-container {
  padding: 16px;
}

.filter-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: none;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.table-card {
  margin-bottom: 16px;
  border-radius: 12px;
  border: none;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-section {
  width: 100%;
}

.batch-info {
  margin-bottom: 16px;
}

.detail-table-title {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
  padding-left: 4px;
  border-left: 3px solid #409EFF;
}

.old-value {
  color: #f56c6c;
  text-decoration: line-through;
}

.new-value {
  color: #67c23a;
}
</style>
