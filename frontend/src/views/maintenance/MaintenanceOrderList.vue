<template>
  <div class="maintenance-order-container">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-pending" @click="handleStatClick(1)">
          <div class="stat-content">
            <div class="stat-value">{{ dashboard.pendingAssign || 0 }}</div>
            <div class="stat-label">待分配</div>
          </div>
          <el-icon :size="40" class="stat-icon"><Promotion /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-processing" @click="handleStatClick(2)">
          <div class="stat-content">
            <div class="stat-value">{{ dashboard.processing || 0 }}</div>
            <div class="stat-label">处理中</div>
          </div>
          <el-icon :size="40" class="stat-icon"><Tools /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-inspect" @click="handleStatClick(3)">
          <div class="stat-content">
            <div class="stat-value">{{ dashboard.pendingInspect || 0 }}</div>
            <div class="stat-label">待验收</div>
          </div>
          <el-icon :size="40" class="stat-icon"><CircleCheck /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-finished">
          <div class="stat-content">
            <div class="stat-value">{{ dashboard.monthCompleted || 0 }}</div>
            <div class="stat-label">本月完成</div>
          </div>
          <el-icon :size="40" class="stat-icon"><Finished /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="tabs-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="" />
        <el-tab-pane label="待分配" name="1" />
        <el-tab-pane label="处理中" name="2" />
        <el-tab-pane label="已完成" name="3" />
        <el-tab-pane label="已验收" name="4" />
        <el-tab-pane label="已关闭" name="5" />
      </el-tabs>
    </el-card>

    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="queryParams.orderNo" placeholder="维护单号" clearable style="width: 160px" @keyup.enter="handleSearch" />
        <el-input v-model="queryParams.roomNumber" placeholder="房间号" clearable style="width: 140px" @keyup.enter="handleSearch" />
        <el-select v-model="queryParams.priorityList" placeholder="优先级" multiple clearable collapse-tags style="width: 180px">
          <el-option v-for="p in priorityOptions" :key="p.value" :label="p.label" :value="p.value" />
        </el-select>
        <el-select v-model="queryParams.typeList" placeholder="维护类型" multiple clearable collapse-tags style="width: 200px">
          <el-option v-for="t in maintenanceTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
        </el-select>
        <el-select v-model="queryParams.assignedUserId" placeholder="维修人员" clearable style="width: 160px">
          <el-option v-for="s in staffList" :key="s.id" :label="s.nickname || s.username" :value="s.id" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button
          v-if="hasPermission('maintenance:order:batch:assign') && selectedOrders.length > 0"
          type="primary"
          @click="openBatchAssignDialog"
        >
          <el-icon><UserFilled /></el-icon>批量分配 ({{ selectedOrders.length }})
        </el-button>
        <el-button
          v-if="hasPermission('maintenance:order:assign') && selectedOrders.length > 0"
          type="success"
          @click="handleBatchAutoAssign"
        >
          <el-icon><MagicStick /></el-icon>智能分配
        </el-button>
        <el-button v-if="hasPermission('maintenance:order:export')" type="success" @click="handleExport">
          <el-icon><Download /></el-icon>导出
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
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column prop="orderNo" label="维护单号" width="160" align="center">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="goToDetail(row)">{{ row.orderNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="roomNumber" label="房间号" width="100" align="center" />
        <el-table-column label="维护类型" width="120" align="center">
          <template #default="{ row }">{{ maintenanceTypeLabel(row.maintenanceType) }}</template>
        </el-table-column>
        <el-table-column label="优先级" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="priorityTagType(row.priority)" size="small">{{ priorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
        <el-table-column label="维修人员" width="120" align="center">
          <template #default="{ row }">{{ row.assignedUserName || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="goToDetail(row)">查看详情</el-button>
            <el-button
              v-if="hasPermission('maintenance:order:assign') && row.status === 1"
              type="warning"
              link
              size="small"
              @click="openAssignDialog(row)"
            >分配</el-button>
            <el-button
              v-if="hasPermission('maintenance:order:accept') && row.status === 1 && isAssignedToMe(row)"
              type="success"
              link
              size="small"
              @click="handleAccept(row)"
            >接单</el-button>
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
          @size-change="loadOrders"
          @current-change="loadOrders"
        />
      </div>
    </el-card>

    <el-dialog v-model="assignDialogVisible" title="分配维修人员" width="420px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="维护单号">
          <span>{{ assignForm.orderNo }}</span>
        </el-form-item>
        <el-form-item label="房间号">
          <span>{{ assignForm.roomNumber }}</span>
        </el-form-item>
        <el-form-item label="维修人员" required>
          <el-select v-model="assignForm.assignedUserId" placeholder="请选择维修人员" style="width: 100%">
            <el-option v-for="s in staffList" :key="s.id" :label="s.nickname || s.username" :value="s.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignSaving" @click="handleAssignSubmit">确认分配</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchAssignDialogVisible" title="批量分配维护单" width="700px" destroy-on-close>
      <el-alert
        v-if="selectedOrders.length > 0"
        :title="`已选择 ${selectedOrders.length} 个待分配维护单`"
        type="info"
        :closable="false"
        style="margin-bottom: 16px"
      />
      
      <el-row :gutter="16">
        <el-col :span="14">
          <h4 class="dialog-section-title">待分配维护单</h4>
          <el-table :data="selectedOrders" size="small" border max-height="280" style="width: 100%">
            <el-table-column prop="orderNo" label="维护单号" width="160" />
            <el-table-column prop="roomNumber" label="房间号" width="100" align="center" />
            <el-table-column label="类型" width="100" align="center">
              <template #default="{ row }">{{ maintenanceTypeLabel(row.maintenanceType) }}</template>
            </el-table-column>
            <el-table-column label="优先级" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="priorityTagType(row.priority)" size="small">{{ priorityLabel(row.priority) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="10">
          <h4 class="dialog-section-title">选择维修人员</h4>
          <el-table
            :data="staffWorkload"
            size="small"
            border
            highlight-current-row
            max-height="280"
            @current-change="handleStaffSelect"
            style="width: 100%; cursor: pointer"
          >
            <el-table-column label="维修人员" min-width="100">
              <template #default="{ row }">{{ row.nickname || row.username }}</template>
            </el-table-column>
            <el-table-column label="处理中" width="65" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="warning">{{ row.processingCount || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="待处理" width="65" align="center">
              <template #default="{ row }">
                <el-tag size="small" type="info">{{ row.pendingCount || 0 }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-form label-width="100px" style="margin-top: 16px">
            <el-form-item label="维修人员" required>
              <el-select v-model="batchAssignForm.targetUserId" placeholder="请选择或点击上方列表" style="width: 100%">
                <el-option
                  v-for="s in staffWorkload"
                  :key="s.id"
                  :label="`${s.nickname || s.username}（处理中:${s.processingCount || 0}）`"
                  :value="s.id"
                />
              </el-select>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <div class="assign-tip">
        <el-icon color="#e6a23c"><InfoFilled /></el-icon>
        <span>提示：系统将优先分配给工作量较少的人员，也可以手动选择。使用"智能分配"按钮可自动分配。</span>
      </div>
      
      <template #footer>
        <el-button @click="batchAssignDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="batchAssignLoading" @click="handleBatchAutoAssign">
          <el-icon><MagicStick /></el-icon>智能分配
        </el-button>
        <el-button type="primary" :loading="batchAssignLoading" @click="handleBatchAssignSubmit">
          确认分配
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Download, Promotion, Tools, CircleCheck, Finished,
  UserFilled, MagicStick, InfoFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)
const currentUserId = userStore.user?.id

const priorityOptions = [
  { value: 1, label: '紧急' },
  { value: 2, label: '高' },
  { value: 3, label: '中' },
  { value: 4, label: '低' }
]

const maintenanceTypeOptions = [
  { value: 1, label: '水电维修' },
  { value: 2, label: '空调维修' },
  { value: 3, label: '家具维修' },
  { value: 4, label: '门锁维修' },
  { value: 5, label: '卫生清洁' },
  { value: 6, label: '其他' }
]

const statusOptions = [
  { value: 1, label: '待分配' },
  { value: 2, label: '处理中' },
  { value: 3, label: '已完成' },
  { value: 4, label: '已验收' },
  { value: 5, label: '已关闭' }
]

const priorityLabel = (p) => {
  const map = { 1: '紧急', 2: '高', 3: '中', 4: '低' }
  return map[p] || '未知'
}

const priorityTagType = (p) => {
  const map = { 1: 'danger', 2: 'warning', 3: 'primary', 4: 'info' }
  return map[p] || 'info'
}

const maintenanceTypeLabel = (t) => {
  const map = { 1: '水电维修', 2: '空调维修', 3: '家具维修', 4: '门锁维修', 5: '卫生清洁', 6: '其他' }
  return map[t] || '未知'
}

const statusLabel = (s) => {
  const map = { 1: '待分配', 2: '处理中', 3: '已完成', 4: '已验收', 5: '已关闭' }
  return map[s] || '未知'
}

const statusTagType = (s) => {
  const map = { 1: 'info', 2: 'warning', 3: 'primary', 4: 'success', 5: '' }
  return map[s] || 'info'
}

const dashboard = reactive({
  pendingAssign: 0,
  processing: 0,
  pendingInspect: 0,
  monthCompleted: 0
})

const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)
const staffList = ref([])
const activeTab = ref('')

const queryParams = reactive({
  orderNo: '',
  roomNumber: '',
  priorityList: [],
  typeList: [],
  assignedUserId: null,
  statusList: [],
  pageNum: 1,
  pageSize: 10
})

const loadDashboard = async () => {
  try {
    const res = await api.hotel.getMaintenanceDashboard()
    if (res.code === 200 && res.data) {
      dashboard.pendingAssign = res.data.pendingAssign || 0
      dashboard.processing = res.data.processing || 0
      dashboard.pendingInspect = res.data.pendingInspect || 0
      dashboard.monthCompleted = res.data.monthCompleted || 0
    }
  } catch {}
}

const loadStaffList = async () => {
  try {
    const res = await api.hotel.getMaintenanceStaffList()
    if (res.code === 200) {
      staffList.value = res.data?.records || res.data?.list || []
    }
  } catch { staffList.value = [] }
}

const loadOrders = async () => {
  tableLoading.value = true
  try {
    const params = { ...queryParams }
    if (params.priorityList && params.priorityList.length > 0) {
      params.priorityList = params.priorityList.join(',')
    } else {
      delete params.priorityList
    }
    if (params.typeList && params.typeList.length > 0) {
      params.typeList = params.typeList.join(',')
    } else {
      delete params.typeList
    }
    if (params.statusList && params.statusList.length > 0) {
      params.statusList = params.statusList.join(',')
    } else {
      delete params.statusList
    }
    const res = await api.hotel.getMaintenanceOrderPage(params)
    if (res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch { tableData.value = []; total.value = 0 } finally { tableLoading.value = false }
}

const handleTabChange = (val) => {
  if (val === '') {
    queryParams.statusList = []
  } else {
    queryParams.statusList = [parseInt(val)]
  }
  queryParams.pageNum = 1
  loadOrders()
}

const handleStatClick = (status) => {
  activeTab.value = String(status)
  queryParams.statusList = [status]
  queryParams.pageNum = 1
  loadOrders()
}

const handleSearch = () => { queryParams.pageNum = 1; loadOrders() }

const handleReset = () => {
  queryParams.orderNo = ''
  queryParams.roomNumber = ''
  queryParams.priorityList = []
  queryParams.typeList = []
  queryParams.assignedUserId = null
  queryParams.statusList = []
  queryParams.pageNum = 1
  activeTab.value = ''
  loadOrders()
}

const goToDetail = (row) => {
  router.push(`/maintenance/order/${row.id}`)
}

const isAssignedToMe = (row) => {
  return currentUserId && row.assignedUserId && row.assignedUserId === currentUserId
}

const handleExport = async () => {
  try {
    const params = {
      orderNo: queryParams.orderNo || undefined,
      roomNumber: queryParams.roomNumber || undefined,
      statusList: queryParams.statusList.length > 0 ? queryParams.statusList : undefined,
      typeList: queryParams.typeList.length > 0 ? queryParams.typeList : undefined,
      priorityList: queryParams.priorityList.length > 0 ? queryParams.priorityList : undefined,
      assignedUserId: queryParams.assignedUserId || undefined
    }
    const res = await api.hotel.exportMaintenanceOrders(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `维护单列表_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch { ElMessage.error('导出失败') }
}

const assignDialogVisible = ref(false)
const assignSaving = ref(false)
const assignForm = reactive({
  id: null,
  orderNo: '',
  roomNumber: '',
  assignedUserId: null
})

const openAssignDialog = (row) => {
  assignForm.id = row.id
  assignForm.orderNo = row.orderNo
  assignForm.roomNumber = row.roomNumber
  assignForm.assignedUserId = row.assignedUserId || null
  assignDialogVisible.value = true
}

const handleAssignSubmit = async () => {
  if (!assignForm.assignedUserId) { ElMessage.warning('请选择维修人员'); return }
  assignSaving.value = true
  try {
    const res = await api.hotel.assignMaintenanceOrder(assignForm.id, { assignedUserId: assignForm.assignedUserId })
    if (res.code === 200) {
      ElMessage.success('分配成功')
      assignDialogVisible.value = false
      await loadOrders(); await loadDashboard()
    } else { ElMessage.error(res.message || '分配失败') }
  } catch { ElMessage.error('分配失败') } finally { assignSaving.value = false }
}

const handleAccept = async (row) => {
  try {
    await ElMessageBox.confirm(`确认接单「${row.orderNo}」？`, '提示', { type: 'warning' })
    const res = await api.hotel.acceptMaintenanceOrder(row.id)
    if (res.code === 200) {
      ElMessage.success('接单成功')
      await loadOrders(); await loadDashboard()
    } else { ElMessage.error(res.message || '接单失败') }
  } catch {}
}

const selectedOrders = ref([])
const batchAssignDialogVisible = ref(false)
const batchAssignLoading = ref(false)
const staffWorkload = ref([])
const batchAssignForm = reactive({ targetUserId: null })

const handleSelectionChange = (rows) => {
  selectedOrders.value = rows.filter(r => r.status === 1)
}

const openBatchAssignDialog = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请先选择待分配的维护单')
    return
  }
  batchAssignForm.targetUserId = null
  try {
    const res = await api.hotel.getMaintenanceStaffWorkload()
    if (res.code === 200) staffWorkload.value = res.data || []
  } catch { staffWorkload.value = [] }
  batchAssignDialogVisible.value = true
}

const handleStaffSelect = (row) => {
  if (row) batchAssignForm.targetUserId = row.id
}

const handleBatchAssignSubmit = async () => {
  if (!batchAssignForm.targetUserId) { ElMessage.warning('请选择维修人员'); return }
  batchAssignLoading.value = true
  try {
    const orderIds = selectedOrders.value.map(o => o.id)
    const res = await api.hotel.batchAssignMaintenanceOrders({
      orderIds,
      targetUserId: batchAssignForm.targetUserId
    })
    if (res.code === 200) {
      const data = res.data || {}
      ElMessage.success(`批量分配完成：成功${data.successCount || 0}个，失败${data.failCount || 0}个`)
      batchAssignDialogVisible.value = false
      selectedOrders.value = []
      await loadOrders()
      await loadDashboard()
    } else { ElMessage.error(res.message || '分配失败') }
  } catch { ElMessage.error('分配失败') } finally { batchAssignLoading.value = false }
}

const handleBatchAutoAssign = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请先选择待分配的维护单')
    return
  }
  batchAssignLoading.value = true
  try {
    const orderIds = selectedOrders.value.map(o => o.id)
    const res = await api.hotel.batchAutoAssignMaintenanceOrders({ orderIds })
    if (res.code === 200) {
      const data = res.data || {}
      ElMessage.success(`智能分配完成：成功${data.successCount || 0}个，失败${data.failCount || 0}个`)
      batchAssignDialogVisible.value = false
      selectedOrders.value = []
      await loadOrders()
      await loadDashboard()
    } else { ElMessage.error(res.message || '智能分配失败') }
  } catch { ElMessage.error('智能分配失败') } finally { batchAssignLoading.value = false }
}

onMounted(() => {
  loadDashboard()
  loadStaffList()
  loadOrders()
})
</script>

<style scoped>
.maintenance-order-container {
  padding: 10px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  cursor: pointer;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  margin-top: 4px;
}

.stat-icon {
  opacity: 0.15;
}

.stat-pending .stat-value { color: #909399; }
.stat-pending .stat-icon { color: #909399; }
.stat-processing .stat-value { color: #e6a23c; }
.stat-processing .stat-icon { color: #e6a23c; }
.stat-inspect .stat-value { color: #409eff; }
.stat-inspect .stat-icon { color: #409eff; }
.stat-finished .stat-value { color: #67c23a; }
.stat-finished .stat-icon { color: #67c23a; }

.tabs-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 12px;
}

.tabs-card :deep(.el-tabs__header) {
  margin: 0;
}

.filter-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.dialog-section-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.assign-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: #fdf6ec;
  border-radius: 6px;
  font-size: 13px;
  color: #e6a23c;
  margin-top: 12px;
}
</style>
