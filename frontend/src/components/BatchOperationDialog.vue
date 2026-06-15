<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    width="780px"
    destroy-on-close
    @update:model-value="handleVisibleChange"
    @close="resetForm"
  >
    <div class="batch-dialog-content">
      <div class="stats-bar">
        <el-tag type="primary">已选 {{ totalCount }} 个</el-tag>
        <el-tag type="success">符合 {{ validCount }} 个</el-tag>
        <el-tag type="danger">不符合 {{ invalidCount }} 个</el-tag>
      </div>

      <el-table
        :data="selectedRooms"
        border
        size="small"
        style="width: 100%; max-height: 260px; overflow-y: auto"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="roomNumber" label="房号" width="120" align="center" />
        <el-table-column label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roomTypeName" label="房型" min-width="140" align="center" />
        <el-table-column label="是否可操作" width="180" align="center">
          <template #default="{ row }">
            <template v-if="isRoomValid(row)">
              <el-tag type="success" size="small">可操作</el-tag>
            </template>
            <template v-else>
              <el-tag type="danger" size="small">{{ getInvalidReason(row) }}</el-tag>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-divider />

      <div v-if="operationType === 'status'" class="operation-section">
        <h4 class="section-title">状态设置</h4>
        <el-form label-width="100px">
          <el-form-item label="目标状态" required>
            <el-select v-model="form.targetStatus" placeholder="请选择目标状态" style="width: 100%">
              <el-option
                v-for="s in targetStatusOptions"
                :key="s.value"
                :label="s.label"
                :value="s.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="operationType === 'attr'" class="operation-section">
        <h4 class="section-title">属性设置</h4>
        <el-form label-width="100px">
          <el-form-item label="属性类型" required>
            <el-select v-model="form.attrType" placeholder="请选择属性类型" style="width: 100%">
              <el-option label="朝向" value="orientation" />
              <el-option label="景观" value="viewType" />
              <el-option label="位置特点" value="locationFeatures" />
              <el-option label="特殊标识" value="specialTags" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作模式" required>
            <el-radio-group v-model="form.attrMode">
              <el-radio :value="1">覆盖</el-radio>
              <el-radio :value="2">添加</el-radio>
              <el-radio :value="3">移除</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="属性值" required>
            <template v-if="isMultiValueAttr(form.attrType)">
              <el-select
                v-model="form.attrValueList"
                multiple
                placeholder="请选择属性值"
                style="width: 100%"
              >
                <el-option
                  v-for="opt in getAttrOptions(form.attrType)"
                  :key="opt"
                  :label="opt"
                  :value="opt"
                />
              </el-select>
            </template>
            <template v-else>
              <el-select v-model="form.attrValue" placeholder="请选择属性值" clearable style="width: 100%">
                <el-option
                  v-for="opt in getAttrOptions(form.attrType)"
                  :key="opt"
                  :label="opt"
                  :value="opt"
                />
              </el-select>
            </template>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="operationType === 'delete'" class="operation-section">
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          title="删除确认"
          :message="`将删除 ${validCount} 个可操作房间，此操作不可恢复，请确认。`"
        />
      </div>

      <el-divider />

      <el-form label-width="100px">
        <el-form-item label="操作原因" required>
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入操作原因（必填，限500字符）"
          />
        </el-form-item>
        <el-form-item label=" " :label-width="0">
          <el-checkbox v-model="form.skipInvalid">
            跳过不符合条件的房间并继续
          </el-checkbox>
        </el-form-item>
      </el-form>
    </div>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleConfirm">
        执行
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '批量操作'
  },
  operationType: {
    type: String,
    default: 'status',
    validator: (val) => ['status', 'attr', 'delete'].includes(val)
  },
  selectedRooms: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const submitting = ref(false)

const statusOptions = [
  { value: 1, label: '空闲' },
  { value: 2, label: '已预订' },
  { value: 3, label: '已入住' },
  { value: 4, label: '待清洁' },
  { value: 5, label: '清洁中' },
  { value: 6, label: '维修中' },
  { value: 7, label: '停用' }
]

const targetStatusOptions = [
  { value: 1, label: '空闲' },
  { value: 2, label: '已预订' },
  { value: 4, label: '待清洁' },
  { value: 5, label: '清洁中' },
  { value: 7, label: '停用' }
]

const orientationOptions = ['东', '南', '西', '北', '东南', '东北', '西南', '西北']
const viewTypeOptions = ['江景', '海景', '山景', '园景', '城景']
const locationFeatureOptions = ['靠近电梯', '转角房', '安静区域', '靠近楼梯', '高楼层', '低楼层']
const specialTagOptions = ['残疾人房', '连通房', '禁烟房', '允许宠物', '行政房', 'VIP房']

const statusTagType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: '', 4: 'info', 5: 'info', 6: 'danger', 7: 'info' }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = { 1: '空闲', 2: '已预订', 3: '已入住', 4: '待清洁', 5: '清洁中', 6: '维修中', 7: '停用' }
  return map[status] || '未知'
}

const form = reactive({
  targetStatus: null,
  attrType: '',
  attrMode: 1,
  attrValue: '',
  attrValueList: [],
  reason: '',
  skipInvalid: true
})

const totalCount = computed(() => props.selectedRooms.length)
const validCount = computed(() => props.selectedRooms.filter(r => isRoomValid(r)).length)
const invalidCount = computed(() => props.selectedRooms.filter(r => !isRoomValid(r)).length)

const isRoomValid = (room) => {
  return room.status !== 3 && room.status !== 6
}

const getInvalidReason = (room) => {
  if (room.status === 3) return '已入住，不可操作'
  if (room.status === 6) return '维修中，不可操作'
  return '不可操作'
}

const getRowClassName = ({ row }) => {
  return isRoomValid(row) ? '' : 'row-invalid'
}

const isMultiValueAttr = (attrType) => {
  return attrType === 'locationFeatures' || attrType === 'specialTags'
}

const getAttrOptions = (attrType) => {
  switch (attrType) {
    case 'orientation':
      return orientationOptions
    case 'viewType':
      return viewTypeOptions
    case 'locationFeatures':
      return locationFeatureOptions
    case 'specialTags':
      return specialTagOptions
    default:
      return []
  }
}

const handleVisibleChange = (val) => {
  emit('update:modelValue', val)
}

const resetForm = () => {
  form.targetStatus = null
  form.attrType = ''
  form.attrMode = 1
  form.attrValue = ''
  form.attrValueList = []
  form.reason = ''
  form.skipInvalid = true
}

const validateForm = () => {
  if (!form.reason || !form.reason.trim()) {
    ElMessage.warning('请输入操作原因')
    return false
  }

  if (validCount.value === 0 && !form.skipInvalid) {
    ElMessage.warning('没有可操作的房间，请选择跳过不符合条件的房间')
    return false
  }

  if (props.operationType === 'status') {
    if (!form.targetStatus) {
      ElMessage.warning('请选择目标状态')
      return false
    }
  }

  if (props.operationType === 'attr') {
    if (!form.attrType) {
      ElMessage.warning('请选择属性类型')
      return false
    }
    if (!form.attrMode) {
      ElMessage.warning('请选择操作模式')
      return false
    }
    if (isMultiValueAttr(form.attrType)) {
      if (!form.attrValueList || form.attrValueList.length === 0) {
        ElMessage.warning('请选择属性值')
        return false
      }
    } else {
      if (!form.attrValue) {
        ElMessage.warning('请选择属性值')
        return false
      }
    }
  }

  return true
}

const handleCancel = () => {
  emit('update:modelValue', false)
}

const handleConfirm = () => {
  if (!validateForm()) return

  const payload = {
    reason: form.reason.trim(),
    skipInvalid: form.skipInvalid
  }

  if (props.operationType === 'status') {
    payload.targetStatus = form.targetStatus
  }

  if (props.operationType === 'attr') {
    payload.attrType = form.attrType
    payload.attrMode = form.attrMode
    if (isMultiValueAttr(form.attrType)) {
      payload.attrValue = form.attrValueList
    } else {
      payload.attrValue = form.attrValue
    }
  }

  submitting.value = true
  emit('confirm', payload)
  submitting.value = false
}

watch(() => props.modelValue, (val) => {
  if (val) {
    resetForm()
  }
})
</script>

<style scoped>
.batch-dialog-content {
  padding: 0;
}

.stats-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.operation-section {
  margin-bottom: 4px;
}

:deep(.row-invalid) {
  --el-table-tr-bg-color: #fef0f0;
}

:deep(.row-invalid td) {
  background-color: #fef0f0 !important;
}
</style>
