<template>
  <div class="floor-permission-container">
    <el-card shadow="never" class="header-card">
      <div class="page-header">
        <div>
          <h2 class="page-title">楼层权限配置</h2>
          <p class="page-desc">配置用户可访问的酒店楼栋楼层范围，用于数据权限控制</p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" class="main-row">
      <el-col :span="8">
        <el-card shadow="never" class="user-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户列表</span>
              <el-input
                v-model="searchKeyword"
                placeholder="搜索用户名/昵称"
                clearable
                size="small"
                style="width: 200px"
                @input="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </template>

          <el-table
            ref="userTableRef"
            :data="filteredUserList"
            v-loading="loading"
            border
            stripe
            height="calc(100vh - 280px)"
            @selection-change="handleSelectionChange"
            @current-change="handleCurrentChange"
          >
            <el-table-column type="selection" width="45" align="center" />
            <el-table-column prop="username" label="用户名" width="110" />
            <el-table-column prop="nickname" label="昵称" width="110" />
            <el-table-column prop="roleNames" label="角色" min-width="100">
              <template #default="{ row }">
                <el-tag v-if="row.roleNames" size="small" type="info">{{ row.roleNames }}</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="权限摘要" min-width="180">
              <template #default="{ row }">
                <el-tag
                  v-if="row.permissionSummary === '全部楼层'"
                  size="small"
                  type="success"
                >
                  全部楼层
                </el-tag>
                <el-tag
                  v-else-if="row.permissionSummary === '未配置' || !row.permissionSummary"
                  size="small"
                  type="info"
                >
                  未配置
                </el-tag>
                <span v-else class="permission-summary-text">
                  {{ row.permissionSummary }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="config-card">
          <template #header>
            <div class="config-header">
              <div class="config-title-area">
                <span class="card-title">楼层权限配置</span>
                <el-tag v-if="selectedUserIds.length === 1" type="primary" size="small" class="selected-tag">
                  {{ selectedUser?.nickname || selectedUser?.username }}
                </el-tag>
                <el-tag v-else-if="selectedUserIds.length > 1" type="warning" size="small" class="selected-tag">
                  批量配置（{{ selectedUserIds.length }}个用户）
                </el-tag>
              </div>
              <div v-if="selectedUserIds.length > 0" class="config-summary">
                <span class="summary-label">当前权限：</span>
                <el-tag
                  v-if="currentPermissionSummary === '全部楼层'"
                  type="success"
                  size="small"
                >
                  全部楼层
                </el-tag>
                <el-tag
                  v-else-if="currentPermissionSummary === '未配置' || !currentPermissionSummary"
                  type="info"
                  size="small"
                >
                  未配置
                </el-tag>
                <span v-else class="summary-text">{{ currentPermissionSummary }}</span>
              </div>
            </div>
          </template>

          <div v-if="selectedUserIds.length === 0" class="empty-config">
            <el-icon :size="64" color="#c0c4cc"><User /></el-icon>
            <p>请选择左侧用户进行楼层权限配置</p>
            <p class="empty-desc">支持多选进行批量配置</p>
          </div>

          <template v-else>
            <div class="tree-toolbar">
              <el-button
                size="small"
                :disabled="!hasEditPermission"
                @click="handleCheckAll"
              >
                全选
              </el-button>
              <el-button
                size="small"
                :disabled="!hasEditPermission"
                @click="handleUncheckAll"
              >
                取消全选
              </el-button>
              <el-button
                size="small"
                @click="handleExpandAll"
              >
                {{ allExpanded ? '收起全部' : '展开全部' }}
              </el-button>
            </div>

            <div class="tree-container">
              <el-tree
                ref="treeRef"
                :data="treeData"
                :props="treeProps"
                node-key="id"
                show-checkbox
                default-expand-all
                :default-checked-keys="checkedFloorIds"
                :disabled="!hasEditPermission"
                @check="handleCheckChange"
              >
                <template #default="{ node, data }">
                  <div class="tree-node">
                    <span v-if="data.nodeType === 'building'" class="building-label">
                      <el-icon class="building-icon"><OfficeBuilding /></el-icon>
                      {{ data.label }}
                    </span>
                    <span v-else class="floor-label">
                      <span class="floor-number">{{ data.floorNumber }}F</span>
                      <span class="floor-name">{{ data.floorName }}</span>
                      <el-badge
                        v-if="data.roomCount > 0"
                        :value="`${data.roomCount}间`"
                        type="info"
                        class="floor-badge"
                      />
                    </span>
                  </div>
                </template>
              </el-tree>
              <div v-if="treeData.length === 0" class="tree-empty">
                <el-icon :size="40" color="#c0c4cc"><OfficeBuilding /></el-icon>
                <p>暂无楼栋数据</p>
              </div>
            </div>

            <div class="config-footer">
              <el-button
                type="primary"
                :loading="saving"
                :disabled="!hasEditPermission"
                @click="handleSave"
              >
                保存配置
              </el-button>
              <el-button
                :disabled="!hasEditPermission || checkedFloorIds.length === 0"
                @click="handleCancel"
              >
                取消
              </el-button>
              <el-button
                type="danger"
                :loading="clearing"
                :disabled="!hasEditPermission"
                @click="handleClear"
              >
                清空权限
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, User, OfficeBuilding } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)
const hasEditPermission = computed(() => hasPermission('hotel:floorPermission:edit'))

const userTableRef = ref(null)
const treeRef = ref(null)
const loading = ref(false)
const saving = ref(false)
const clearing = ref(false)
const allExpanded = ref(true)

const userList = ref([])
const selectedUserIds = ref([])
const buildings = ref([])
const checkedFloorIds = ref([])
const searchKeyword = ref('')

const treeProps = {
  children: 'children',
  label: 'label'
}

const treeData = computed(() => {
  return buildings.value.map(b => ({
    id: `building-${b.id}`,
    nodeType: 'building',
    label: b.buildingName,
    buildingId: b.id,
    children: (b.floors || []).map(f => ({
      id: f.id,
      nodeType: 'floor',
      label: `${f.floorNumber}F - ${f.floorName}`,
      floorId: f.id,
      floorNumber: f.floorNumber,
      floorName: f.floorName,
      roomCount: f.roomCount || 0
    }))
  }))
})

const allFloorIds = computed(() => {
  const ids = []
  buildings.value.forEach(b => {
    (b.floors || []).forEach(f => {
      ids.push(f.id)
    })
  })
  return ids
})

const filteredUserList = computed(() => {
  if (!searchKeyword.value) return userList.value
  const kw = searchKeyword.value.toLowerCase()
  return userList.value.filter(u =>
    u.username?.toLowerCase().includes(kw) ||
    u.nickname?.toLowerCase().includes(kw)
  )
})

const selectedUser = computed(() => {
  if (selectedUserIds.value.length !== 1) return null
  return userList.value.find(u => u.id === selectedUserIds.value[0]) || null
})

const currentPermissionSummary = computed(() => {
  if (selectedUserIds.value.length === 1 && selectedUser.value) {
    return selectedUser.value.permissionSummary || '未配置'
  }
  if (selectedUserIds.value.length > 1) {
    return '批量配置模式'
  }
  return '未配置'
})

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await api.hotel.getFloorPermissionUsers()
    if (res.code === 200) {
      userList.value = res.data || []
    }
  } catch (error) {
    userList.value = []
  } finally {
    loading.value = false
  }
}

const loadBuildings = async () => {
  try {
    const res = await api.hotel.getBuildings()
    if (res.code === 200) {
      buildings.value = res.data || []
    }
  } catch (error) {
    buildings.value = []
  }
}

const loadUserPermissions = async (userId) => {
  try {
    const res = await api.hotel.getUserFloorPermissions(userId)
    if (res.code === 200) {
      checkedFloorIds.value = res.data || []
      await nextTick()
      if (treeRef.value) {
        treeRef.value.setCheckedKeys(checkedFloorIds.value)
      }
    }
  } catch (error) {
    checkedFloorIds.value = []
  }
}

const loadPermissionSummary = async (userId) => {
  try {
    const res = await api.hotel.getUserFloorPermissionSummary(userId)
    if (res.code === 200) {
      const user = userList.value.find(u => u.id === userId)
      if (user) {
        user.permissionSummary = res.data || '未配置'
      }
    }
  } catch (error) {
    // ignore
  }
}

const handleSearch = () => {
  // filteredUserList computed handles this
}

const handleSelectionChange = (selection) => {
  selectedUserIds.value = selection.map(u => u.id)
  if (selection.length === 1) {
    loadUserPermissions(selection[0].id)
  } else if (selection.length > 1) {
    checkedFloorIds.value = []
    nextTick(() => {
      if (treeRef.value) {
        treeRef.value.setCheckedKeys([])
      }
    })
  }
}

const handleCurrentChange = (row) => {
  if (row) {
    userTableRef.value?.toggleRowSelection(row, true)
  }
}

const handleCheckChange = () => {
  checkedFloorIds.value = treeRef.value?.getCheckedKeys(true) || []
}

const handleCheckAll = () => {
  checkedFloorIds.value = [...allFloorIds.value]
  nextTick(() => {
    if (treeRef.value) {
      treeRef.value.setCheckedKeys(checkedFloorIds.value)
    }
  })
}

const handleUncheckAll = () => {
  checkedFloorIds.value = []
  nextTick(() => {
    if (treeRef.value) {
      treeRef.value.setCheckedKeys([])
    }
  })
}

const handleExpandAll = () => {
  const tree = treeRef.value
  if (!tree) return
  const nodes = tree.store._getAllNodes()
  allExpanded.value = !allExpanded.value
  nodes.forEach(node => {
    node.expanded = allExpanded.value
  })
}

const handleSave = async () => {
  if (selectedUserIds.value.length === 0) {
    ElMessage.warning('请先选择用户')
    return
  }

  saving.value = true
  try {
    const promises = selectedUserIds.value.map(userId =>
      api.hotel.saveUserFloorPermissions(userId, { floorIds: checkedFloorIds.value })
    )
    const results = await Promise.all(promises)
    const success = results.every(r => r.code === 200)
    if (success) {
      ElMessage.success(`已成功保存 ${selectedUserIds.value.length} 个用户的权限配置`)
      const summaryPromises = selectedUserIds.value.map(id => loadPermissionSummary(id))
      await Promise.all(summaryPromises)
    } else {
      ElMessage.error('部分用户权限保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  if (selectedUserIds.value.length === 1) {
    loadUserPermissions(selectedUserIds.value[0])
  } else {
    handleUncheckAll()
  }
}

const handleClear = async () => {
  if (selectedUserIds.value.length === 0) {
    ElMessage.warning('请先选择用户')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定清空 ${selectedUserIds.value.length} 个用户的楼层权限吗？`,
      '提示',
      { type: 'warning' }
    )
  } catch {
    return
  }

  clearing.value = true
  try {
    const promises = selectedUserIds.value.map(userId =>
      api.hotel.clearUserFloorPermissions(userId)
    )
    const results = await Promise.all(promises)
    const success = results.every(r => r.code === 200)
    if (success) {
      ElMessage.success('权限已清空')
      handleUncheckAll()
      const summaryPromises = selectedUserIds.value.map(id => loadPermissionSummary(id))
      await Promise.all(summaryPromises)
    } else {
      ElMessage.error('部分用户权限清空失败')
    }
  } catch (error) {
    ElMessage.error('清空失败')
  } finally {
    clearing.value = false
  }
}

watch(selectedUserIds, (newVal) => {
  if (newVal.length === 0) {
    checkedFloorIds.value = []
  }
})

onMounted(async () => {
  await Promise.all([loadUsers(), loadBuildings()])
})
</script>

<style scoped>
.floor-permission-container {
  padding: 16px;
}

.header-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
}

.page-desc {
  margin: 6px 0 0 0;
  font-size: 13px;
  color: #909399;
}

.main-row {
  display: flex;
}

.user-card,
.config-card {
  border-radius: 12px;
  border: none;
  height: calc(100vh - 200px);
  display: flex;
  flex-direction: column;
}

.user-card :deep(.el-card__body),
.config-card :deep(.el-card__body) {
  overflow: hidden;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.permission-summary-text {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.config-title-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.selected-tag {
  margin-left: 4px;
}

.config-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.summary-label {
  color: #909399;
}

.summary-text {
  color: #606266;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.empty-config {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 12px;
  color: #909399;
}

.empty-config p {
  margin: 0;
  font-size: 15px;
}

.empty-config .empty-desc {
  font-size: 13px;
  color: #c0c4cc;
}

.tree-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.tree-container {
  flex: 1;
  overflow-y: auto;
  padding: 4px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  padding: 2px 0;
}

.building-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  font-size: 14px;
}

.building-icon {
  color: #409eff;
}

.floor-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.floor-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  padding: 0 6px;
  height: 22px;
  background: #ecf5ff;
  color: #409eff;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.floor-name {
  color: #606266;
}

.floor-badge {
  margin-left: 4px;
}

.tree-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 12px;
  color: #909399;
}

.tree-empty p {
  margin: 0;
  font-size: 14px;
}

.config-footer {
  display: flex;
  gap: 10px;
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid #ebeef5;
}
</style>
