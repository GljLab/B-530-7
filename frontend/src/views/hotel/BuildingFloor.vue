<template>
  <div class="building-floor-container">
    <div class="left-panel">
      <el-card shadow="never" class="tree-card">
        <template #header>
          <div class="tree-header">
            <span class="tree-title">楼栋楼层</span>
            <div class="tree-actions">
              <el-button size="small" link @click="toggleExpandAll">
                {{ allExpanded ? '收起全部' : '展开全部' }}
              </el-button>
              <el-button
                v-if="hasPermission('hotel:building:add')"
                type="primary"
                size="small"
                @click="openBuildingDialog(null)"
              >
                新增楼栋
              </el-button>
            </div>
          </div>
        </template>
        <el-tree
          ref="treeRef"
          :data="treeData"
          :props="treeProps"
          node-key="nodeKey"
          highlight-current
          default-expand-all
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <div class="tree-node">
              <span class="tree-node-label">{{ data.label }}</span>
              <el-tag
                v-if="data.nodeType === 'building'"
                :type="data.status === 1 ? 'success' : 'danger'"
                size="small"
                class="tree-node-tag"
              >
                {{ data.status === 1 ? '启用' : '停用' }}
              </el-tag>
              <el-badge
                v-if="data.nodeType === 'floor' && data.roomCount > 0"
                :value="data.roomCount"
                type="info"
                class="tree-node-badge"
              />
            </div>
          </template>
        </el-tree>
        <div v-if="treeData.length === 0" class="tree-empty">
          <el-icon :size="40" color="#c0c4cc"><OfficeBuilding /></el-icon>
          <p>暂无楼栋数据</p>
        </div>
      </el-card>
    </div>

    <div class="right-panel">
      <div v-if="!selectedNode" class="empty-detail">
        <el-icon :size="64" color="#c0c4cc"><OfficeBuilding /></el-icon>
        <p>请选择左侧楼栋或楼层查看详情</p>
      </div>

      <template v-if="selectedNode && selectedNode.nodeType === 'building'">
        <el-card shadow="never" class="detail-card">
          <template #header>
            <div class="detail-header">
              <span>楼栋详情</span>
              <div class="detail-actions">
                <el-button
                  v-if="hasPermission('hotel:building:edit')"
                  type="primary"
                  plain
                  size="small"
                  @click="openBuildingDialog(selectedBuilding)"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="hasPermission('hotel:building:delete')"
                  type="danger"
                  plain
                  size="small"
                  @click="handleDeleteBuilding(selectedBuilding)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="楼栋名称">{{ selectedBuilding.buildingName }}</el-descriptions-item>
            <el-descriptions-item label="楼栋编码">{{ selectedBuilding.buildingCode }}</el-descriptions-item>
            <el-descriptions-item label="总楼层数">{{ selectedBuilding.totalFloors }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-switch
                :model-value="selectedBuilding.status === 1"
                active-text="启用"
                inactive-text="停用"
                @change="handleBuildingStatusChange"
              />
            </el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ selectedBuilding.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="floor-list-card">
          <template #header>
            <div class="detail-header">
              <span>楼层列表</span>
              <el-button
                v-if="hasPermission('hotel:floor:add')"
                type="primary"
                size="small"
                @click="openFloorDialog(null, selectedBuilding.id)"
              >
                新增楼层
              </el-button>
            </div>
          </template>
          <el-table :data="selectedBuilding.floors || []" stripe>
            <el-table-column prop="floorNumber" label="楼层号" width="100" align="center" />
            <el-table-column prop="floorName" label="楼层名称" min-width="120" />
            <el-table-column prop="features" label="特色标签" min-width="160">
              <template #default="{ row }">
                <template v-if="row.features">
                  <el-tag
                    v-for="feat in parseFeatures(row.features)"
                    :key="feat"
                    size="small"
                    type="info"
                    class="feature-tag"
                  >
                    {{ feat }}
                  </el-tag>
                </template>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="roomCount" label="房间数" width="90" align="center" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="floorStatusType(row.status)" size="small">
                  {{ floorStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="hasPermission('hotel:floor:edit')"
                  type="primary"
                  link
                  size="small"
                  @click="openFloorDialog(row, selectedBuilding.id)"
                >
                  编辑
                </el-button>
                <el-button
                  v-if="hasPermission('hotel:floor:delete')"
                  type="danger"
                  link
                  size="small"
                  @click="handleDeleteFloor(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="!selectedBuilding.floors || selectedBuilding.floors.length === 0" class="table-empty">
            暂无楼层数据
          </div>
        </el-card>
      </template>

      <template v-if="selectedNode && selectedNode.nodeType === 'floor'">
        <el-card shadow="never" class="detail-card">
          <template #header>
            <div class="detail-header">
              <span>楼层详情</span>
              <div class="detail-actions">
                <el-button
                  v-if="hasPermission('hotel:floor:edit')"
                  type="primary"
                  plain
                  size="small"
                  @click="openFloorDialog(selectedFloorData, selectedFloorData.buildingId)"
                >
                  编辑
                </el-button>
              </div>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="楼层号">{{ selectedFloorData.floorNumber }}</el-descriptions-item>
            <el-descriptions-item label="楼层名称">{{ selectedFloorData.floorName }}</el-descriptions-item>
            <el-descriptions-item label="所属楼栋">{{ selectedFloorData.buildingName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="特色标签">
              <template v-if="selectedFloorData.features">
                <el-tag
                  v-for="feat in parseFeatures(selectedFloorData.features)"
                  :key="feat"
                  size="small"
                  type="info"
                  class="feature-tag"
                >
                  {{ feat }}
                </el-tag>
              </template>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="floorStatusType(selectedFloorData.status)" size="small">
                {{ floorStatusLabel(selectedFloorData.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="room-list-card">
          <template #header>
            <span>房间列表</span>
          </template>
          <el-table :data="floorRooms" stripe>
            <el-table-column prop="roomNumber" label="房号" width="120" align="center" />
            <el-table-column prop="roomTypeName" label="房型" min-width="140" />
            <el-table-column label="状态" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="roomStatusType(row.status)" size="small">
                  {{ roomStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div v-if="floorRooms.length === 0" class="table-empty">
            暂无房间数据
          </div>
        </el-card>
      </template>
    </div>

    <el-dialog
      v-model="buildingDialogVisible"
      :title="buildingForm.id ? '编辑楼栋' : '新增楼栋'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="buildingFormRef" :model="buildingForm" :rules="buildingRules" label-width="100px">
        <el-form-item label="楼栋名称" prop="buildingName">
          <el-input v-model="buildingForm.buildingName" placeholder="请输入楼栋名称" />
        </el-form-item>
        <el-form-item label="楼栋编码" prop="buildingCode">
          <el-input v-model="buildingForm.buildingCode" placeholder="请输入楼栋编码" />
        </el-form-item>
        <el-form-item label="总楼层数" prop="totalFloors">
          <el-input-number v-model="buildingForm.totalFloors" :min="1" :max="99" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="buildingForm.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="buildingForm.statusSwitch" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buildingDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="buildingSaving" @click="handleSaveBuilding">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="floorDialogVisible"
      :title="floorForm.id ? '编辑楼层' : '新增楼层'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="floorFormRef" :model="floorForm" :rules="floorRules" label-width="100px">
        <el-form-item label="所属楼栋" prop="buildingId">
          <el-select v-model="floorForm.buildingId" placeholder="请选择楼栋" style="width: 100%">
            <el-option
              v-for="b in buildings"
              :key="b.id"
              :label="b.buildingName"
              :value="b.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层号" prop="floorNumber">
          <el-input-number v-model="floorForm.floorNumber" :min="-5" :max="99" />
        </el-form-item>
        <el-form-item label="楼层名称" prop="floorName">
          <el-input v-model="floorForm.floorName" placeholder="请输入楼层名称" />
        </el-form-item>
        <el-form-item label="特色标签" prop="features">
          <el-select v-model="floorForm.featuresList" multiple placeholder="请选择特色标签" style="width: 100%">
            <el-option v-for="opt in featureOptions" :key="opt" :label="opt" :value="opt" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="floorForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option :value="1" label="正常" />
            <el-option :value="2" label="维修中" />
            <el-option :value="3" label="暂停" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="floorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="floorSaving" @click="handleSaveFloor">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const treeRef = ref(null)
const buildings = ref([])
const allExpanded = ref(true)
const selectedNode = ref(null)
const floorRooms = ref([])

const featureOptions = ['无烟', '安静区域', '高楼层', '低楼层', '靠近大堂']

const treeProps = {
  children: 'children',
  label: 'label'
}

const treeData = computed(() => {
  return buildings.value.map(b => ({
    nodeKey: `building-${b.id}`,
    nodeType: 'building',
    label: b.buildingName,
    status: b.status,
    buildingId: b.id,
    children: (b.floors || []).map(f => ({
      nodeKey: `floor-${f.id}`,
      nodeType: 'floor',
      label: `${f.floorNumber}F - ${f.floorName}`,
      floorId: f.id,
      roomCount: f.roomCount || 0,
      status: f.status
    }))
  }))
})

const selectedBuilding = computed(() => {
  if (!selectedNode.value || selectedNode.value.nodeType !== 'building') return null
  return buildings.value.find(b => b.id === selectedNode.value.buildingId) || null
})

const selectedFloorData = computed(() => {
  if (!selectedNode.value || selectedNode.value.nodeType !== 'floor') return null
  const b = buildings.value.find(b => b.floors?.some(f => f.id === selectedNode.value.floorId))
  if (!b) return null
  const f = b.floors.find(f => f.id === selectedNode.value.floorId)
  if (!f) return null
  return { ...f, buildingName: b.buildingName }
})

const parseFeatures = (features) => {
  if (!features) return []
  if (Array.isArray(features)) return features
  return features.split(',').filter(Boolean)
}

const floorStatusType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger' }
  return map[status] || 'info'
}

const floorStatusLabel = (status) => {
  const map = { 1: '正常', 2: '维修中', 3: '暂停' }
  return map[status] || '未知'
}

const roomStatusType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: 'primary', 4: 'info', 5: 'info', 6: 'danger', 7: 'info' }
  return map[status] || 'info'
}

const roomStatusLabel = (status) => {
  const map = { 1: '空闲', 2: '已预订', 3: '已入住', 4: '待清洁', 5: '清洁中', 6: '维修中', 7: '停用' }
  return map[status] || '未知'
}

const toggleExpandAll = () => {
  const tree = treeRef.value
  if (!tree) return
  const nodes = tree.store._getAllNodes()
  allExpanded.value = !allExpanded.value
  nodes.forEach(node => {
    node.expanded = allExpanded.value
  })
}

const handleNodeClick = (data) => {
  selectedNode.value = data
  if (data.nodeType === 'floor') {
    loadFloorRooms(data.floorId)
  }
}

const loadBuildings = async () => {
  try {
    const res = await api.hotel.getBuildings()
    if (res.code === 200) {
      buildings.value = res.data || []
    }
  } catch {
    buildings.value = []
  }
}

const loadFloorRooms = async (floorId) => {
  try {
    const res = await api.hotel.getRoomPage({ floorId, pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      floorRooms.value = res.data?.records || res.data || []
    } else {
      floorRooms.value = []
    }
  } catch {
    floorRooms.value = []
  }
}

const buildingDialogVisible = ref(false)
const buildingSaving = ref(false)
const buildingFormRef = ref(null)
const buildingForm = reactive({
  id: null,
  buildingName: '',
  buildingCode: '',
  totalFloors: 1,
  description: '',
  statusSwitch: true
})

const buildingRules = {
  buildingName: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }],
  buildingCode: [{ required: true, message: '请输入楼栋编码', trigger: 'blur' }],
  totalFloors: [{ required: true, message: '请输入总楼层数', trigger: 'change' }]
}

const openBuildingDialog = (building) => {
  if (building) {
    Object.assign(buildingForm, {
      id: building.id,
      buildingName: building.buildingName || '',
      buildingCode: building.buildingCode || '',
      totalFloors: building.totalFloors || 1,
      description: building.description || '',
      statusSwitch: building.status === 1
    })
  } else {
    Object.assign(buildingForm, {
      id: null,
      buildingName: '',
      buildingCode: '',
      totalFloors: 1,
      description: '',
      statusSwitch: true
    })
  }
  buildingDialogVisible.value = true
}

const handleSaveBuilding = async () => {
  const valid = await buildingFormRef.value.validate().catch(() => false)
  if (!valid) return

  buildingSaving.value = true
  try {
    const payload = {
      buildingName: buildingForm.buildingName,
      buildingCode: buildingForm.buildingCode,
      totalFloors: buildingForm.totalFloors,
      description: buildingForm.description,
      status: buildingForm.statusSwitch ? 1 : 0
    }
    let res
    if (buildingForm.id) {
      res = await api.hotel.updateBuilding({ id: buildingForm.id, ...payload })
    } else {
      res = await api.hotel.addBuilding(payload)
    }
    if (res.code === 200) {
      ElMessage.success(buildingForm.id ? '更新成功' : '新增成功')
      buildingDialogVisible.value = false
      await loadBuildings()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    buildingSaving.value = false
  }
}

const handleBuildingStatusChange = async (val) => {
  if (!selectedBuilding.value) return
  try {
    const res = await api.hotel.updateBuildingStatus({
      id: selectedBuilding.value.id,
      status: val ? 1 : 0
    })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      await loadBuildings()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch {
    ElMessage.error('更新失败')
  }
}

const handleDeleteBuilding = async (building) => {
  try {
    await ElMessageBox.confirm(`确认删除楼栋「${building.buildingName}」？`, '提示', { type: 'warning' })
    const res = await api.hotel.deleteBuilding(building.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      selectedNode.value = null
      await loadBuildings()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // cancelled
  }
}

const floorDialogVisible = ref(false)
const floorSaving = ref(false)
const floorFormRef = ref(null)
const floorForm = reactive({
  id: null,
  buildingId: null,
  floorNumber: 1,
  floorName: '',
  featuresList: [],
  status: 1
})

const floorRules = {
  buildingId: [{ required: true, message: '请选择所属楼栋', trigger: 'change' }],
  floorNumber: [{ required: true, message: '请输入楼层号', trigger: 'change' }],
  floorName: [{ required: true, message: '请输入楼层名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const openFloorDialog = (floor, buildingId) => {
  if (floor) {
    Object.assign(floorForm, {
      id: floor.id,
      buildingId: floor.buildingId || buildingId,
      floorNumber: floor.floorNumber || 1,
      floorName: floor.floorName || '',
      featuresList: parseFeatures(floor.features),
      status: floor.status || 1
    })
  } else {
    Object.assign(floorForm, {
      id: null,
      buildingId: buildingId || null,
      floorNumber: 1,
      floorName: '',
      featuresList: [],
      status: 1
    })
  }
  floorDialogVisible.value = true
}

const handleSaveFloor = async () => {
  const valid = await floorFormRef.value.validate().catch(() => false)
  if (!valid) return

  floorSaving.value = true
  try {
    const payload = {
      buildingId: floorForm.buildingId,
      floorNumber: floorForm.floorNumber,
      floorName: floorForm.floorName,
      features: floorForm.featuresList.join(','),
      status: floorForm.status
    }
    let res
    if (floorForm.id) {
      res = await api.hotel.updateFloor({ id: floorForm.id, ...payload })
    } else {
      res = await api.hotel.addFloor(payload)
    }
    if (res.code === 200) {
      ElMessage.success(floorForm.id ? '更新成功' : '新增成功')
      floorDialogVisible.value = false
      await loadBuildings()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    floorSaving.value = false
  }
}

const handleDeleteFloor = async (floor) => {
  try {
    await ElMessageBox.confirm(`确认删除楼层「${floor.floorName}」？`, '提示', { type: 'warning' })
    const res = await api.hotel.deleteFloor(floor.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      if (selectedNode.value?.floorId === floor.id) {
        selectedNode.value = null
      }
      await loadBuildings()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadBuildings()
})
</script>

<style scoped>
.building-floor-container {
  display: flex;
  gap: 16px;
  height: calc(100vh - 130px);
  padding: 10px;
}

.left-panel {
  width: 30%;
  min-width: 260px;
}

.tree-card {
  height: 100%;
  border-radius: 12px;
  border: none;
}

.tree-card :deep(.el-card__body) {
  overflow-y: auto;
  flex: 1;
}

.tree-card :deep(.el-card) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tree-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.tree-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.tree-node-label {
  font-size: 14px;
}

.tree-node-tag {
  flex-shrink: 0;
}

.tree-node-badge {
  flex-shrink: 0;
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

.right-panel {
  width: 70%;
  overflow-y: auto;
}

.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 16px;
  color: #909399;
}

.empty-detail p {
  margin: 0;
  font-size: 16px;
}

.detail-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-actions {
  display: flex;
  gap: 8px;
}

.floor-list-card,
.room-list-card {
  border-radius: 12px;
  border: none;
}

.feature-tag {
  margin-right: 4px;
  margin-bottom: 2px;
}

.table-empty {
  text-align: center;
  padding: 32px 0;
  color: #909399;
  font-size: 14px;
}
</style>
