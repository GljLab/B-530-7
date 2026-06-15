<template>
  <div class="room-change-log-container">
    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="queryParams.roomNumber" placeholder="房间号" clearable style="width: 150px" @keyup.enter="handleSearch" />
        <el-select v-model="queryParams.operatorId" placeholder="操作人" clearable filterable style="width: 160px">
          <el-option v-for="u in userList" :key="u.id" :label="u.nickname || u.username" :value="u.id" />
        </el-select>
        <el-date-picker
          v-model="dateTimeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 340px"
        />
        <el-select v-model="queryParams.changeTypes" placeholder="变更类型" multiple clearable collapse-tags style="width: 220px">
          <el-option v-for="t in changeTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
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
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-detail">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="变更时间">{{ row.changeTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="操作人">{{ row.operatorName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="操作类型">
                  <el-tag :type="getChangeTypeTag(row.changeType)" size="small">{{ getChangeTypeLabel(row.changeType) }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="操作终端/IP">{{ row.terminal || row.ip || '-' }}</el-descriptions-item>
                <el-descriptions-item label="变更字段" :span="2">{{ row.fieldName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="原值" :span="2">
                  <div class="detail-value old-value">{{ formatValue(row.oldValue) }}</div>
                </el-descriptions-item>
                <el-descriptions-item label="新值" :span="2">
                  <div class="detail-value new-value">{{ formatValue(row.newValue) }}</div>
                </el-descriptions-item>
                <el-descriptions-item label="变更原因" :span="2">{{ row.reason || '-' }}</el-descriptions-item>
                <el-descriptions-item label="关联维护单号" v-if="row.maintenanceOrderNo" :span="2">
                  <el-link type="primary" @click="goToMaintenanceOrder(row.maintenanceOrderId)">
                    {{ row.maintenanceOrderNo }}
                  </el-link>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="changeTime" label="变更时间" width="180" align="center" />
        <el-table-column prop="roomNumber" label="房间号" width="110" align="center">
          <template #default="{ row }">
            <el-link type="primary" @click="goToRoomDetail(row.roomId)">{{ row.roomNumber }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="operatorName" label="操作人" width="120" align="center">
          <template #default="{ row }">{{ row.operatorName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getChangeTypeTag(row.changeType)" size="small">
              {{ getChangeTypeLabel(row.changeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fieldName" label="变更字段" min-width="140">
          <template #default="{ row }">{{ row.fieldName || '-' }}</template>
        </el-table-column>
        <el-table-column label="变更内容" min-width="200">
          <template #default="{ row }">
            <div class="change-content">
              <span class="old-value">{{ formatValue(row.oldValue) }}</span>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
              <span class="new-value">{{ formatValue(row.newValue) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="关联维护单" width="140" align="center">
          <template #default="{ row }">
            <el-link v-if="row.maintenanceOrderNo" type="primary" @click="goToMaintenanceOrder(row.maintenanceOrderId)">
              {{ row.maintenanceOrderNo }}
            </el-link>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="终端/IP" width="160" align="center">
          <template #default="{ row }">{{ row.terminal || row.ip || '-' }}</template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadChangeLogs"
          @current-change="loadChangeLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()

const changeTypeOptions = [
  { value: 1, label: '创建' },
  { value: 2, label: '修改' },
  { value: 3, label: '状态变更' },
  { value: 4, label: '删除' },
  { value: 5, label: '维护单关联' }
]

const userList = ref([])
const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)
const dateTimeRange = ref([])

const queryParams = reactive({
  roomNumber: '',
  operatorId: null,
  changeTypes: [],
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 10
})

const getChangeTypeLabel = (type) => {
  const map = { 1: '创建', 2: '修改', 3: '状态变更', 4: '删除', 5: '维护单关联' }
  return map[type] || '未知'
}

const getChangeTypeTag = (type) => {
  const map = { 1: 'success', 2: 'warning', 3: 'primary', 4: 'danger', 5: 'info' }
  return map[type] || 'info'
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

const loadUserList = async () => {
  try {
    const res = await api.user.list({ pageNum: 1, pageSize: 1000, status: 1 })
    if (res.code === 200) {
      userList.value = res.data?.list || res.data?.records || []
    }
  } catch {
    userList.value = []
  }
}

const loadChangeLogs = async () => {
  tableLoading.value = true
  try {
    const params = { ...queryParams }
    if (params.changeTypes && params.changeTypes.length > 0) {
      params.changeType = params.changeTypes.join(',')
    }
    delete params.changeTypes
    if (dateTimeRange.value && dateTimeRange.value.length === 2) {
      params.startTime = dateTimeRange.value[0]
      params.endTime = dateTimeRange.value[1]
    } else {
      delete params.startTime
      delete params.endTime
    }
    const res = await api.hotel.getChangeLogPage(params)
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
  loadChangeLogs()
}

const handleReset = () => {
  queryParams.roomNumber = ''
  queryParams.operatorId = null
  queryParams.changeTypes = []
  queryParams.startTime = ''
  queryParams.endTime = ''
  queryParams.pageNum = 1
  dateTimeRange.value = []
  loadChangeLogs()
}

const goToRoomDetail = (roomId) => {
  if (roomId) {
    router.push(`/hotel/room/${roomId}`)
  }
}

const goToMaintenanceOrder = (orderId) => {
  if (orderId) {
    router.push(`/maintenance/order/${orderId}`)
  }
}

onMounted(() => {
  loadUserList()
  loadChangeLogs()
})
</script>

<style scoped>
.room-change-log-container {
  padding: 16px;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.table-card {
  margin-bottom: 16px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.change-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.change-content .old-value {
  color: #f56c6c;
  text-decoration: line-through;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.change-content .new-value {
  color: #67c23a;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-icon {
  color: #909399;
  font-size: 14px;
}

.expand-detail {
  padding: 8px 16px;
}

.detail-value {
  max-width: 100%;
  word-break: break-all;
}

.detail-value.old-value {
  color: #f56c6c;
  text-decoration: line-through;
}

.detail-value.new-value {
  color: #67c23a;
}
</style>
