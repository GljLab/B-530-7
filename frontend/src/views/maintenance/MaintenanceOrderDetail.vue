<template>
  <div class="maintenance-detail-container">
    <div class="detail-header">
      <div class="header-left">
        <el-button @click="router.push('/maintenance/order')">
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
        <span class="order-number">{{ orderData?.orderNo || '' }}</span>
        <el-tag v-if="orderData" :type="statusTagType(orderData.status)" size="large" effect="dark">
          {{ statusLabel(orderData.status) }}
        </el-tag>
      </div>
      <div class="header-actions">
        <el-button
          v-if="orderData?.status === 1 && hasPermission('maintenance:order:assign')"
          type="primary"
          @click="openAssignDialog"
        >
          <el-icon><User /></el-icon>分配
        </el-button>
        <el-button
          v-if="orderData?.status === 1 && isAssignedToMe"
          type="success"
          @click="handleAcceptOrder"
        >
          <el-icon><Check /></el-icon>接单
        </el-button>
        <el-button
          v-if="orderData?.status === 2 && isAssignedToMe"
          type="warning"
          @click="openProgressDialog"
        >
          <el-icon><Edit /></el-icon>更新进度
        </el-button>
        <el-button
          v-if="orderData?.status === 2 && isAssignedToMe"
          type="success"
          @click="openFinishDialog"
        >
          <el-icon><CircleCheck /></el-icon>提交完成
        </el-button>
        <el-button
          v-if="orderData?.status === 3 && hasPermission('maintenance:order:inspect')"
          type="primary"
          @click="openInspectDialog"
        >
          <el-icon><View /></el-icon>验收
        </el-button>
      </div>
    </div>

    <el-card v-if="orderData" shadow="never" class="info-card">
      <template #header><span class="card-title">基础信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="房间号">
          <el-link type="primary" @click="goToRoomDetail">
            <span class="field-link">{{ orderData.roomNumber || '-' }}</span>
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="维护类型">
          <span class="field-readonly">{{ orderData.maintenanceTypeName || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="priorityTagType(orderData.priority)" effect="light">
            {{ priorityLabel(orderData.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          <span class="field-readonly">{{ orderData.createTime || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建人">
          <span class="field-readonly">{{ orderData.creatorName || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="预计完成时间">
          <span class="field-readonly">{{ orderData.expectedFinishTime || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="特殊说明" :span="2">
          <span v-if="orderData.specialRemark" class="remark-text">{{ orderData.specialRemark }}</span>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card v-if="orderData" shadow="never" class="info-card">
      <template #header><span class="card-title">问题描述</span></template>
      <div class="problem-content">
        <p v-if="orderData.problemDescription" class="problem-text">{{ orderData.problemDescription }}</p>
        <span v-else class="text-muted">暂无问题描述</span>
      </div>
      <div v-if="problemPhotos.length" class="photo-section">
        <div class="photo-label">问题照片：</div>
        <div class="photo-list">
          <el-image
            v-for="(photo, idx) in problemPhotos"
            :key="idx"
            :src="photo"
            :preview-src-list="problemPhotos"
            :initial-index="idx"
            fit="cover"
            class="photo-item"
          />
        </div>
      </div>
    </el-card>

    <el-card v-if="orderData" shadow="never" class="info-card">
      <template #header><span class="card-title">处理信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="分配人员">
          <span class="field-readonly">{{ orderData.assignedUserName || '未分配' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="接单时间">
          <span class="field-readonly">{{ orderData.acceptTime || '-' }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="progressList.length" class="progress-section">
        <div class="progress-label">处理进度：</div>
        <div class="progress-list">
          <div v-for="(p, idx) in progressList" :key="idx" class="progress-item">
            <div class="progress-header">
              <span class="progress-time">{{ p.createTime }}</span>
              <span class="progress-operator">{{ p.operatorName || '系统' }}</span>
            </div>
            <p v-if="p.note" class="progress-note">{{ p.note }}</p>
            <div v-if="p.photos && p.photos.length" class="progress-photos">
              <el-image
                v-for="(photo, pIdx) in p.photos"
                :key="pIdx"
                :src="photo"
                :preview-src-list="p.photos"
                :initial-index="pIdx"
                fit="cover"
                class="progress-photo"
              />
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无处理进度" :image-size="80" />
    </el-card>

    <el-card v-if="orderData && orderData.status >= 3" shadow="never" class="info-card">
      <template #header><span class="card-title">维护记录</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="实际用时">
          <span class="field-readonly">{{ orderData.actualHours ? orderData.actualHours + ' 小时' : '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="维修费用">
          <span class="field-readonly price">{{ orderData.maintenanceCost ? '¥' + orderData.maintenanceCost : '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="使用配件" :span="2">
          <span v-if="orderData.usedParts" class="field-readonly">{{ orderData.usedParts }}</span>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="维护说明" :span="2">
          <span v-if="orderData.maintenanceDescription" class="remark-text">{{ orderData.maintenanceDescription }}</span>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
      </el-descriptions>
      <div v-if="afterPhotos.length" class="photo-section">
        <div class="photo-label">维护后照片：</div>
        <div class="photo-list">
          <el-image
            v-for="(photo, idx) in afterPhotos"
            :key="idx"
            :src="photo"
            :preview-src-list="afterPhotos"
            :initial-index="idx"
            fit="cover"
            class="photo-item"
          />
        </div>
      </div>
    </el-card>

    <el-card v-if="orderData && orderData.status >= 4" shadow="never" class="info-card">
      <template #header><span class="card-title">验收信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="验收人">
          <span class="field-readonly">{{ orderData.inspectorName || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="验收时间">
          <span class="field-readonly">{{ orderData.inspectTime || '-' }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="验收结果">
          <el-tag v-if="orderData.inspectResult === 1" type="success" effect="light">验收通过</el-tag>
          <el-tag v-else-if="orderData.inspectResult === 2" type="danger" effect="light">验收不通过</el-tag>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
        <el-descriptions-item label="验收意见" :span="2">
          <span v-if="orderData.inspectOpinion" class="remark-text">{{ orderData.inspectOpinion }}</span>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="orderData.inspectResult === 2" label="整改要求" :span="2">
          <span v-if="orderData.rectificationRequirement" class="remark-text">{{ orderData.rectificationRequirement }}</span>
          <span v-else class="text-muted">-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never" class="info-card">
      <template #header><span class="card-title">状态流转记录</span></template>
      <el-timeline v-if="statusLogs.length">
        <el-timeline-item
          v-for="log in statusLogs"
          :key="log.id"
          :timestamp="log.createTime"
          placement="top"
          :type="statusTimelineType(log.newStatus)"
        >
          <div class="log-content">
            <span class="log-operator">{{ log.operatorName || '系统' }}</span>
            <el-tag v-if="log.oldStatus" :type="statusTagType(log.oldStatus)" size="small">
              {{ statusLabel(log.oldStatus) }}
            </el-tag>
            <span v-if="log.oldStatus" class="log-arrow">→</span>
            <el-tag :type="statusTagType(log.newStatus)" size="small">
              {{ statusLabel(log.newStatus) }}
            </el-tag>
            <span v-if="log.remark" class="log-remark">{{ log.remark }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无状态变更记录" :image-size="80" />
    </el-card>

    <el-dialog
      v-model="assignDialogVisible"
      title="分配维修人员"
      width="480px"
      destroy-on-close
    >
      <el-form label-width="100px">
        <el-form-item label="维修人员" required>
          <el-select v-model="assignForm.assignedUserId" placeholder="请选择维修人员" style="width: 100%" filterable>
            <el-option
              v-for="u in staffList"
              :key="u.id"
              :label="u.nickName || u.userName"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignSaving" @click="handleAssign">确认分配</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="progressDialogVisible"
      title="更新处理进度"
      width="560px"
      destroy-on-close
    >
      <el-form label-width="100px">
        <el-form-item label="进度说明" required>
          <el-input
            v-model="progressForm.note"
            type="textarea"
            :rows="4"
            placeholder="请输入进度说明"
          />
        </el-form-item>
        <el-form-item label="补充照片">
          <el-upload
            v-model:file-list="progressForm.fileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleProgressUploadSuccess"
            :before-upload="handleBeforeUpload"
            multiple
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="progressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="progressSaving" @click="handleAddProgress">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="finishDialogVisible"
      title="提交维护完成"
      width="600px"
      destroy-on-close
    >
      <el-form label-width="110px">
        <el-form-item label="实际用时(小时)">
          <el-input-number
            v-model="finishForm.actualHours"
            :min="0"
            :precision="1"
            :step="0.5"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="使用配件">
          <el-input v-model="finishForm.usedParts" placeholder="请输入使用的配件" />
        </el-form-item>
        <el-form-item label="维修费用(元)">
          <el-input-number
            v-model="finishForm.maintenanceCost"
            :min="0"
            :precision="2"
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="维护说明" required>
          <el-input
            v-model="finishForm.maintenanceDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入维护说明"
          />
        </el-form-item>
        <el-form-item label="维护后照片">
          <el-upload
            v-model:file-list="finishForm.fileList"
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleFinishUploadSuccess"
            :before-upload="handleBeforeUpload"
            multiple
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="finishSaving" @click="handleFinish">确认完成</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="inspectDialogVisible"
      title="维护验收"
      width="560px"
      destroy-on-close
    >
      <el-form label-width="100px">
        <el-form-item label="验收结果" required>
          <el-radio-group v-model="inspectForm.inspectResult">
            <el-radio :value="1">验收通过</el-radio>
            <el-radio :value="2">验收不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="验收意见" required>
          <el-input
            v-model="inspectForm.inspectOpinion"
            type="textarea"
            :rows="3"
            placeholder="请输入验收意见"
          />
        </el-form-item>
        <el-form-item v-if="inspectForm.inspectResult === 2" label="整改要求" required>
          <el-input
            v-model="inspectForm.rectificationRequirement"
            type="textarea"
            :rows="3"
            placeholder="请输入整改要求"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="inspectSaving" @click="handleInspect">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  User,
  Check,
  Edit,
  CircleCheck,
  View,
  Plus
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const uploadUrl = '/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}))

const statusLabel = (status) => {
  const map = { 1: '待分配', 2: '处理中', 3: '已完成', 4: '已验收', 5: '已关闭' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { 1: 'info', 2: 'warning', 3: 'primary', 4: 'success', 5: '' }
  return map[status] || 'info'
}

const statusTimelineType = (status) => {
  const map = { 1: 'info', 2: 'warning', 3: 'primary', 4: 'success', 5: '' }
  return map[status] || ''
}

const priorityLabel = (priority) => {
  const map = { 1: '紧急', 2: '高', 3: '中', 4: '低' }
  return map[priority] || '未知'
}

const priorityTagType = (priority) => {
  const map = { 1: 'danger', 2: 'warning', 3: 'primary', 4: 'info' }
  return map[priority] || 'info'
}

const parsePhotoUrls = (photos) => {
  if (!photos) return []
  if (Array.isArray(photos)) {
    return photos.map(p => typeof p === 'string' ? p : (p.photoUrl || p.url || p.imageUrl || ''))
  }
  try {
    const parsed = JSON.parse(photos)
    if (Array.isArray(parsed)) {
      return parsed.map(p => typeof p === 'string' ? p : (p.photoUrl || p.url || p.imageUrl || ''))
    }
  } catch {
    return []
  }
  return []
}

const orderData = ref(null)
const statusLogs = ref([])
const pageLoading = ref(false)
const staffList = ref([])

const problemPhotos = computed(() => parsePhotoUrls(orderData.value?.problemPhotos))
const afterPhotos = computed(() => parsePhotoUrls(orderData.value?.afterPhotos))
const progressList = computed(() => {
  const list = orderData.value?.progressList || orderData.value?.progresses || []
  return list.map(p => ({
    ...p,
    photos: parsePhotoUrls(p.photos)
  }))
})

const isAssignedToMe = computed(() => {
  if (!orderData.value || !userStore.user) return false
  return orderData.value.assignedUserId === userStore.user.id
})

const loadDetail = async () => {
  pageLoading.value = true
  try {
    const id = route.params.id
    const res = await api.hotel.getMaintenanceOrder(id)
    if (res.code === 200 && res.data) {
      orderData.value = res.data
      statusLogs.value = res.data.statusLogs || res.data.logs || []
    } else {
      ElMessage.error(res.message || '获取维护单详情失败')
    }
  } catch {
    ElMessage.error('获取维护单详情失败')
  } finally {
    pageLoading.value = false
  }
}

const goToRoomDetail = () => {
  if (orderData.value?.roomId) {
    router.push(`/hotel/room/${orderData.value.roomId}`)
  }
}

const loadStaffList = async () => {
  try {
    const res = await api.hotel.getMaintenanceStaffList()
    if (res.code === 200) {
      staffList.value = res.data?.rows || res.data || []
    }
  } catch {
    staffList.value = []
  }
}

const assignDialogVisible = ref(false)
const assignSaving = ref(false)
const assignForm = reactive({ assignedUserId: null })

const openAssignDialog = async () => {
  assignForm.assignedUserId = orderData.value?.assignedUserId || null
  await loadStaffList()
  assignDialogVisible.value = true
}

const handleAssign = async () => {
  if (!assignForm.assignedUserId) {
    ElMessage.warning('请选择维修人员')
    return
  }
  assignSaving.value = true
  try {
    const res = await api.hotel.assignMaintenanceOrder(route.params.id, {
      assignedUserId: assignForm.assignedUserId
    })
    if (res.code === 200) {
      ElMessage.success('分配成功')
      assignDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '分配失败')
    }
  } catch {
    ElMessage.error('分配失败')
  } finally {
    assignSaving.value = false
  }
}

const handleAcceptOrder = async () => {
  try {
    await ElMessageBox.confirm('确认接单？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await api.hotel.acceptMaintenanceOrder(route.params.id)
    if (res.code === 200) {
      ElMessage.success('接单成功')
      await loadDetail()
    } else {
      ElMessage.error(res.message || '接单失败')
    }
  } catch {}
}

const progressDialogVisible = ref(false)
const progressSaving = ref(false)
const progressForm = reactive({
  note: '',
  photos: [],
  fileList: []
})

const openProgressDialog = () => {
  progressForm.note = ''
  progressForm.photos = []
  progressForm.fileList = []
  progressDialogVisible.value = true
}

const handleBeforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleProgressUploadSuccess = (response, uploadFile) => {
  if (response.code === 200) {
    progressForm.photos.push(response.data?.url || response.data)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleAddProgress = async () => {
  if (!progressForm.note?.trim()) {
    ElMessage.warning('请输入进度说明')
    return
  }
  progressSaving.value = true
  try {
    const res = await api.hotel.addMaintenanceProgress(route.params.id, {
      note: progressForm.note,
      photos: progressForm.photos
    })
    if (res.code === 200) {
      ElMessage.success('进度更新成功')
      progressDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch {
    ElMessage.error('更新失败')
  } finally {
    progressSaving.value = false
  }
}

const finishDialogVisible = ref(false)
const finishSaving = ref(false)
const finishForm = reactive({
  actualHours: null,
  usedParts: '',
  maintenanceCost: null,
  maintenanceDescription: '',
  afterPhotos: [],
  fileList: []
})

const openFinishDialog = () => {
  finishForm.actualHours = null
  finishForm.usedParts = ''
  finishForm.maintenanceCost = null
  finishForm.maintenanceDescription = ''
  finishForm.afterPhotos = []
  finishForm.fileList = []
  finishDialogVisible.value = true
}

const handleFinishUploadSuccess = (response) => {
  if (response.code === 200) {
    finishForm.afterPhotos.push(response.data?.url || response.data)
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleFinish = async () => {
  if (!finishForm.maintenanceDescription?.trim()) {
    ElMessage.warning('请输入维护说明')
    return
  }
  finishSaving.value = true
  try {
    const res = await api.hotel.finishMaintenanceOrder(route.params.id, {
      actualHours: finishForm.actualHours,
      usedParts: finishForm.usedParts || undefined,
      maintenanceCost: finishForm.maintenanceCost,
      maintenanceDescription: finishForm.maintenanceDescription,
      afterPhotos: finishForm.afterPhotos
    })
    if (res.code === 200) {
      ElMessage.success('提交完成成功')
      finishDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败')
  } finally {
    finishSaving.value = false
  }
}

const inspectDialogVisible = ref(false)
const inspectSaving = ref(false)
const inspectForm = reactive({
  inspectResult: null,
  inspectOpinion: '',
  rectificationRequirement: ''
})

const openInspectDialog = () => {
  inspectForm.inspectResult = null
  inspectForm.inspectOpinion = ''
  inspectForm.rectificationRequirement = ''
  inspectDialogVisible.value = true
}

const handleInspect = async () => {
  if (!inspectForm.inspectResult) {
    ElMessage.warning('请选择验收结果')
    return
  }
  if (!inspectForm.inspectOpinion?.trim()) {
    ElMessage.warning('请输入验收意见')
    return
  }
  if (inspectForm.inspectResult === 2 && !inspectForm.rectificationRequirement?.trim()) {
    ElMessage.warning('请输入整改要求')
    return
  }
  inspectSaving.value = true
  try {
    const res = await api.hotel.inspectMaintenanceOrder(route.params.id, {
      inspectResult: inspectForm.inspectResult,
      inspectOpinion: inspectForm.inspectOpinion,
      rectificationRequirement: inspectForm.inspectResult === 2 ? inspectForm.rectificationRequirement : undefined
    })
    if (res.code === 200) {
      ElMessage.success('验收提交成功')
      inspectDialogVisible.value = false
      await loadDetail()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败')
  } finally {
    inspectSaving.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.maintenance-detail-container {
  padding: 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.order-number {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.info-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a202c;
}

.field-readonly {
  font-weight: 600;
  color: #2d3748;
}

.field-link {
  font-weight: 600;
  color: #409eff;
}

.text-muted {
  color: #c0c4cc;
  font-size: 14px;
}

.price {
  color: #e6a23c;
  font-size: 16px;
}

.remark-text {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.6;
  white-space: pre-wrap;
}

.problem-content {
  margin-bottom: 16px;
}

.problem-text {
  margin: 0;
  font-size: 14px;
  color: #4a5568;
  line-height: 1.6;
  white-space: pre-wrap;
}

.photo-section {
  margin-top: 8px;
}

.photo-label,
.progress-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  font-weight: 500;
}

.photo-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.photo-item {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
}

.progress-section {
  margin-top: 16px;
}

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-item {
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-time {
  font-size: 13px;
  color: #909399;
}

.progress-operator {
  font-size: 13px;
  font-weight: 600;
  color: #2d3748;
}

.progress-note {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #4a5568;
  line-height: 1.6;
  white-space: pre-wrap;
}

.progress-photos {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.progress-photo {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  cursor: pointer;
}

.log-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.log-operator {
  font-weight: 600;
  color: #2d3748;
}

.log-arrow {
  color: #a0aec0;
  font-size: 16px;
}

.log-remark {
  color: #718096;
  font-size: 13px;
}
</style>
