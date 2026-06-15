<template>
  <div class="maintenance-order-create">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">创建维护单</span>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="110px"
        label-position="right"
      >
        <el-divider content-position="left">基础信息</el-divider>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="房间号" prop="roomId">
              <el-select
                v-model="form.roomId"
                placeholder="请选择房间"
                style="width: 100%"
                filterable
              >
                <el-option
                  v-for="room in roomList"
                  :key="room.id"
                  :label="room.roomNumber"
                  :value="room.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级" prop="priority">
              <el-radio-group v-model="form.priority">
                <el-radio :value="1">
                  <el-tag type="danger" size="small">紧急</el-tag>
                </el-radio>
                <el-radio :value="2">
                  <el-tag type="warning" size="small">高</el-tag>
                </el-radio>
                <el-radio :value="3">
                  <el-tag type="info" size="small">中</el-tag>
                </el-radio>
                <el-radio :value="4">
                  <el-tag size="small">低</el-tag>
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="24">
            <el-form-item label="维护类型" prop="maintenanceType">
              <el-radio-group v-model="form.maintenanceType">
                <el-radio :value="1">设施维修</el-radio>
                <el-radio :value="2">定期保养</el-radio>
                <el-radio :value="3">深度清洁</el-radio>
                <el-radio :value="4">设备更换</el-radio>
                <el-radio :value="5">装修改造</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">问题描述</el-divider>
        <el-form-item label="问题详情" prop="problemDescription">
          <el-input
            v-model="form.problemDescription"
            type="textarea"
            :rows="4"
            placeholder="请详细描述问题情况"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="上传照片">
          <el-upload
            v-model:file-list="fileList"
            :action="uploadAction"
            :headers="uploadHeaders"
            :limit="5"
            :on-exceed="handleExceed"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :on-error="handleUploadError"
            list-type="picture-card"
            accept="image/*"
            multiple
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="upload-tip">最多上传5张图片，支持 jpg、png、gif 格式</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-divider content-position="left">时间安排</el-divider>
        <el-form-item label="预计完成时间" prop="expectedFinishTime">
          <el-date-picker
            v-model="form.expectedFinishTime"
            type="datetime"
            placeholder="请选择预计完成时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-divider content-position="left">特殊说明</el-divider>
        <el-form-item label="其他备注" prop="specialRemark">
          <el-input
            v-model="form.specialRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入其他备注信息（选填）"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              <el-icon><Check /></el-icon>提交
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, Check } from '@element-plus/icons-vue'
import api from '@/api'

const router = useRouter()

const maintenanceTypeOptions = [
  { value: 1, label: '设施维修' },
  { value: 2, label: '定期保养' },
  { value: 3, label: '深度清洁' },
  { value: 4, label: '设备更换' },
  { value: 5, label: '装修改造' }
]

const priorityOptions = [
  { value: 1, label: '紧急' },
  { value: 2, label: '高' },
  { value: 3, label: '中' },
  { value: 4, label: '低' }
]

const formRef = ref(null)
const submitting = ref(false)
const roomList = ref([])
const fileList = ref([])
const uploadedUrls = ref([])

const form = reactive({
  roomId: null,
  maintenanceType: null,
  priority: null,
  problemDescription: '',
  expectedFinishTime: '',
  specialRemark: ''
})

const rules = {
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  maintenanceType: [{ required: true, message: '请选择维护类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  problemDescription: [{ required: true, message: '请输入问题描述', trigger: 'blur' }]
}

const uploadAction = '/api/file/upload'
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
}

const loadRoomList = async () => {
  try {
    const res = await api.hotel.getRoomList()
    if (res.code === 200) {
      roomList.value = res.data || []
    }
  } catch {
    roomList.value = []
  }
}

const beforeUpload = (file) => {
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

const handleExceed = () => {
  ElMessage.warning('最多只能上传5张图片')
}

const handleUploadSuccess = (response, uploadFile) => {
  if (response.code === 200 && response.data) {
    const url = typeof response.data === 'string' ? response.data : response.data.url
    uploadedUrls.value.push(url)
    uploadFile.url = url
  } else {
    ElMessage.error(response.message || '上传失败')
    const idx = fileList.value.indexOf(uploadFile)
    if (idx > -1) fileList.value.splice(idx, 1)
  }
}

const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

const handleRemove = (uploadFile) => {
  if (uploadFile.url) {
    const idx = uploadedUrls.value.indexOf(uploadFile.url)
    if (idx > -1) uploadedUrls.value.splice(idx, 1)
  }
}

const handleCancel = () => {
  router.push('/maintenance/order')
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    const payload = {
      roomId: form.roomId,
      maintenanceType: form.maintenanceType,
      priority: form.priority,
      problemDescription: form.problemDescription,
      expectedFinishTime: form.expectedFinishTime || undefined,
      specialRemark: form.specialRemark || undefined,
      problemPhotos: uploadedUrls.value.length > 0 ? uploadedUrls.value : undefined
    }
    const res = await api.hotel.createMaintenanceOrder(payload)
    if (res.code === 200) {
      ElMessage.success('创建维护单成功')
      router.push('/maintenance/order')
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch {
    ElMessage.error('创建失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadRoomList()
})
</script>

<style scoped lang="scss">
.maintenance-order-create {
  padding: 16px;
}

.form-card {
  max-width: 960px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 12px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 8px;
}

:deep(.el-radio) {
  margin-right: 16px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>
