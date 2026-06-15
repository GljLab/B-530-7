<template>
  <div class="room-type-container">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar">
        <div class="toolbar-filters">
          <el-select v-model="filterBedType" placeholder="床型" clearable style="width: 130px">
            <el-option label="全部" value="" />
            <el-option label="单人床" value="single" />
            <el-option label="大床" value="king" />
            <el-option label="双床" value="twin" />
          </el-select>
          <el-select v-model="filterSaleStatus" placeholder="售卖状态" clearable style="width: 130px">
            <el-option label="全部" value="" />
            <el-option label="在售" value="1" />
            <el-option label="停售" value="0" />
          </el-select>
          <div class="price-range">
            <el-input-number v-model="filterPriceMin" :min="0" :controls="false" placeholder="最低价" style="width: 100px" />
            <span class="price-sep">-</span>
            <el-input-number v-model="filterPriceMax" :min="0" :controls="false" placeholder="最高价" style="width: 100px" />
          </div>
        </div>
        <div class="toolbar-actions">
          <el-button v-if="hasPermission('hotel:roomType:add')" type="primary" @click="openFormDialog(null)">
            <el-icon><Plus /></el-icon>新增房型
          </el-button>
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button value="card">
              <el-icon><Grid /></el-icon>
            </el-radio-button>
            <el-radio-button value="list">
              <el-icon><List /></el-icon>
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </el-card>

    <div v-if="viewMode === 'card'" class="card-grid">
      <div
        v-for="item in filteredList"
        :key="item.id"
        class="room-type-card"
        @click="openDetail(item)"
      >
        <div class="card-image">
          <img v-if="item.mainImageUrl" :src="item.mainImageUrl" alt="" />
          <div v-else class="card-image-placeholder">
            <el-icon :size="48" color="#c0c4cc"><Picture /></el-icon>
          </div>
          <el-tag
            :type="item.saleStatus === 1 ? 'success' : 'info'"
            class="card-status-tag"
            size="small"
          >
            {{ item.saleStatus === 1 ? '在售' : '停售' }}
          </el-tag>
        </div>
        <div class="card-body">
          <div class="card-title">{{ item.typeName }}</div>
          <div class="card-meta">
            <el-tag size="small" :type="bedTypeTagType(item.bedType)">{{ bedTypeLabel(item.bedType) }}</el-tag>
            <span class="card-area">{{ item.area }}m²</span>
          </div>
          <div class="card-price">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ item.basePrice }}</span>
            <span class="price-unit">/晚</span>
          </div>
          <div class="card-room-count">剩余房间：{{ item.roomCount || 0 }}间</div>
        </div>
      </div>
      <div v-if="filteredList.length === 0" class="empty-state">
        <el-icon :size="64" color="#c0c4cc"><PriceTag /></el-icon>
        <p>暂无房型数据</p>
      </div>
    </div>

    <el-card v-else shadow="never" class="list-card">
      <el-table :data="filteredList" stripe @row-click="openDetail">
        <el-table-column prop="typeName" label="房型名称" min-width="140" />
        <el-table-column prop="typeCode" label="编码" width="120" />
        <el-table-column label="床型" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="bedTypeTagType(row.bedType)">{{ bedTypeLabel(row.bedType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="area" label="面积(m²)" width="100" align="center" />
        <el-table-column label="基础价格" width="120" align="center">
          <template #default="{ row }">¥{{ row.basePrice }}/晚</template>
        </el-table-column>
        <el-table-column prop="roomCount" label="房间数" width="90" align="center">
          <template #default="{ row }">{{ row.roomCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.saleStatus === 1 ? 'success' : 'info'" size="small">
              {{ row.saleStatus === 1 ? '在售' : '停售' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer
      v-model="detailVisible"
      :title="detailData?.typeName || '房型详情'"
      size="560px"
      destroy-on-close
    >
      <div v-if="detailData" class="detail-content">
        <div class="detail-gallery">
          <div v-if="detailImages.length > 0" class="gallery-grid">
            <div v-for="(img, idx) in detailImages" :key="img.id || idx" class="gallery-item">
              <img :src="img.url" alt="" />
              <div v-if="hasPermission('hotel:image:edit')" class="gallery-item-actions">
                <el-icon
                  :class="{ 'star-active': img.isMain }"
                  @click.stop="handleSetMainImage(img)"
                ><Star /></el-icon>
                <el-icon @click.stop="handleDeleteImage(img)"><Delete /></el-icon>
              </div>
              <div v-if="hasPermission('hotel:image:edit')" class="gallery-item-sort">
                <el-icon v-if="idx > 0" @click.stop="moveImage(idx, -1)"><ArrowUp /></el-icon>
                <el-icon v-if="idx < detailImages.length - 1" @click.stop="moveImage(idx, 1)"><ArrowDown /></el-icon>
              </div>
              <el-tag v-if="img.isMain" type="success" size="small" class="gallery-main-tag">主图</el-tag>
            </div>
          </div>
          <div v-else class="gallery-empty">
            <el-icon :size="40" color="#c0c4cc"><Picture /></el-icon>
            <p>暂无图片</p>
          </div>
          <div v-if="hasPermission('hotel:image:edit')" class="gallery-upload">
            <el-button size="small" type="primary" plain @click="triggerDetailUpload">
              <el-icon><Plus /></el-icon>上传图片
            </el-button>
            <input
              ref="detailFileInputRef"
              type="file"
              accept="image/*"
              multiple
              style="display: none"
              @change="handleDetailFileChange"
            />
          </div>
        </div>

        <el-descriptions :column="2" border class="detail-section">
          <el-descriptions-item label="房型名称">{{ detailData.typeName }}</el-descriptions-item>
          <el-descriptions-item label="编码">{{ detailData.typeCode }}</el-descriptions-item>
          <el-descriptions-item label="面积">{{ detailData.area }}m²</el-descriptions-item>
          <el-descriptions-item label="床型">
            <el-tag size="small" :type="bedTypeTagType(detailData.bedType)">{{ bedTypeLabel(detailData.bedType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最大入住">{{ detailData.maxOccupancy }}人</el-descriptions-item>
          <el-descriptions-item label="加床政策">{{ extraBedLabel(detailData.extraBedPolicy) }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-section" v-if="parsedFacilities.length > 0">
          <h4 class="section-title">设施</h4>
          <div class="facility-tags">
            <el-tag v-for="f in parsedFacilities" :key="f" size="small" type="info" class="facility-tag">{{ f }}</el-tag>
          </div>
        </div>

        <el-descriptions :column="2" border class="detail-section">
          <el-descriptions-item label="基础价格">¥{{ detailData.basePrice }}/晚</el-descriptions-item>
          <el-descriptions-item label="周末价格">¥{{ detailData.weekendPrice }}/晚</el-descriptions-item>
          <el-descriptions-item v-if="hasPermission('hotel:roomType:cost:view')" label="成本价格">
            ¥{{ detailData.costPrice }}/晚
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="detailData.description" class="detail-section">
          <h4 class="section-title">描述</h4>
          <p class="detail-desc">{{ detailData.description }}</p>
        </div>

        <div class="detail-footer">
          <el-button
            v-if="hasPermission('hotel:roomType:edit')"
            type="primary"
            @click="openFormDialog(detailData)"
          >编辑</el-button>
          <el-button
            v-if="hasPermission('hotel:roomType:delete')"
            type="danger"
            @click="handleDelete(detailData)"
          >删除</el-button>
          <el-button
            v-if="hasPermission('hotel:roomType:edit')"
            :type="detailData.saleStatus === 1 ? 'info' : 'success'"
            @click="handleToggleSaleStatus(detailData)"
          >
            {{ detailData.saleStatus === 1 ? '停售' : '在售' }}
          </el-button>
        </div>
      </div>
    </el-drawer>

    <el-dialog
      v-model="formDialogVisible"
      :title="formModel.id ? '编辑房型' : '新增房型'"
      width="680px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="100px">
        <el-form-item label="房型名称" prop="typeName">
          <el-input v-model="formModel.typeName" placeholder="请输入房型名称" />
        </el-form-item>
        <el-form-item label="房型编码" prop="typeCode">
          <el-input v-model="formModel.typeCode" placeholder="请输入房型编码" :disabled="!!formModel.id" />
        </el-form-item>
        <el-form-item label="面积(m²)" prop="area">
          <el-input-number v-model="formModel.area" :min="1" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="床型" prop="bedType">
          <el-select v-model="formModel.bedType" placeholder="请选择床型" style="width: 100%">
            <el-option label="单人床" value="single" />
            <el-option label="大床" value="king" />
            <el-option label="双床" value="twin" />
          </el-select>
        </el-form-item>
        <el-form-item label="最大入住" prop="maxOccupancy">
          <el-input-number v-model="formModel.maxOccupancy" :min="1" :max="20" style="width: 100%" />
        </el-form-item>
        <el-form-item label="加床政策" prop="extraBedPolicy">
          <el-select v-model="formModel.extraBedPolicy" placeholder="请选择加床政策" style="width: 100%">
            <el-option label="不可加床" value="none" />
            <el-option label="可加床(收费)" value="paid" />
            <el-option label="可加床(免费)" value="free" />
          </el-select>
        </el-form-item>
        <el-form-item label="设施" prop="facilitiesList">
          <div class="facility-checkbox-group">
            <div v-for="cat in facilityCategories" :key="cat.label" class="facility-category">
              <div class="category-label">{{ cat.label }}</div>
              <el-checkbox-group v-model="formModel.facilitiesList">
                <el-checkbox v-for="opt in cat.options" :key="opt" :label="opt" :value="opt" />
              </el-checkbox-group>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="基础价格" prop="basePrice">
          <el-input-number v-model="formModel.basePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="周末价格" prop="weekendPrice">
          <el-input-number v-model="formModel.weekendPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="hasPermission('hotel:roomType:price:edit')" label="成本价格" prop="costPrice">
          <el-input-number v-model="formModel.costPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="售卖状态">
          <el-switch v-model="formModel.saleStatusSwitch" active-text="在售" inactive-text="停售" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formModel.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formSaving" @click="handleSaveForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Delete, Picture, PriceTag, Grid, List,
  Star, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const roomTypes = ref([])
const viewMode = ref('card')
const filterBedType = ref('')
const filterSaleStatus = ref('')
const filterPriceMin = ref(undefined)
const filterPriceMax = ref(undefined)

const detailVisible = ref(false)
const detailData = ref(null)
const detailImages = ref([])
const detailFileInputRef = ref(null)

const formDialogVisible = ref(false)
const formSaving = ref(false)
const formRef = ref(null)
const formModel = reactive({
  id: null,
  typeName: '',
  typeCode: '',
  area: 30,
  bedType: '',
  maxOccupancy: 2,
  extraBedPolicy: 'none',
  facilitiesList: [],
  basePrice: 0,
  weekendPrice: 0,
  costPrice: 0,
  saleStatusSwitch: true,
  description: ''
})

const formRules = {
  typeName: [{ required: true, message: '请输入房型名称', trigger: 'blur' }],
  typeCode: [{ required: true, message: '请输入房型编码', trigger: 'blur' }],
  area: [{ required: true, message: '请输入面积', trigger: 'change' }],
  bedType: [{ required: true, message: '请选择床型', trigger: 'change' }],
  basePrice: [{ required: true, message: '请输入基础价格', trigger: 'change' }]
}

const facilityCategories = [
  { label: '床品', options: ['高级床品', '记忆棉枕'] },
  { label: '卫浴', options: ['独立淋浴', '浴缸', '免费洗护用品'] },
  { label: '家电', options: ['电视', '迷你吧', '保险箱'] },
  { label: '智能设备', options: ['智能门锁', '智能灯光', '智能音箱'] },
  { label: '办公', options: ['书桌', '办公椅', '免费WiFi'] },
  { label: '特色', options: ['阳台', '厨房', '按摩浴缸'] }
]

const bedTypeLabel = (type) => {
  const map = { single: '单人床', king: '大床', twin: '双床' }
  return map[type] || type || '-'
}

const bedTypeTagType = (type) => {
  const map = { single: 'info', king: 'warning', twin: '' }
  return map[type] || 'info'
}

const extraBedLabel = (policy) => {
  const map = { none: '不可加床', paid: '可加床(收费)', free: '可加床(免费)' }
  return map[policy] || policy || '-'
}

const filteredList = computed(() => {
  return roomTypes.value.filter(item => {
    if (filterBedType.value && item.bedType !== filterBedType.value) return false
    if (filterSaleStatus.value !== '') {
      const status = Number(filterSaleStatus.value)
      if (item.saleStatus !== status) return false
    }
    if (filterPriceMin.value != null && item.basePrice < filterPriceMin.value) return false
    if (filterPriceMax.value != null && item.basePrice > filterPriceMax.value) return false
    return true
  })
})

const parsedFacilities = computed(() => {
  if (!detailData.value || !detailData.value.facilities) return []
  try {
    const parsed = JSON.parse(detailData.value.facilities)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

const loadRoomTypes = async () => {
  try {
    const res = await api.hotel.getRoomTypes()
    if (res.code === 200) {
      roomTypes.value = res.data || []
    }
  } catch {
    roomTypes.value = []
  }
}

const openDetail = async (row) => {
  try {
    const res = await api.hotel.getRoomType(row.id)
    if (res.code === 200 && res.data) {
      detailData.value = res.data
      detailVisible.value = true
      await loadDetailImages(row.id)
    }
  } catch {
    ElMessage.error('获取房型详情失败')
  }
}

const loadDetailImages = async (id) => {
  try {
    const res = await api.hotel.getImages('roomType', id)
    if (res.code === 200) {
      detailImages.value = res.data || []
    }
  } catch {
    detailImages.value = []
  }
}

const triggerDetailUpload = () => {
  detailFileInputRef.value?.click()
}

const handleDetailFileChange = async (e) => {
  const files = e.target.files
  if (!files || files.length === 0) return

  for (const file of files) {
    const formData = new FormData()
    formData.append('file', file)
    try {
      const uploadRes = await api.hotel.uploadFile(formData)
      if (uploadRes.code === 200 && uploadRes.data) {
        const newImage = {
          url: uploadRes.data.url || uploadRes.data,
          isMain: detailImages.value.length === 0,
          refType: 'roomType',
          refId: detailData.value.id
        }
        detailImages.value.push(newImage)
      }
    } catch {
      ElMessage.error('图片上传失败')
    }
  }

  try {
    await api.hotel.saveImages('roomType', detailData.value.id, detailImages.value)
    await loadDetailImages(detailData.value.id)
  } catch {
    ElMessage.error('保存图片信息失败')
  }

  e.target.value = ''
}

const handleSetMainImage = async (img) => {
  try {
    const res = await api.hotel.setMainImage(img.id, 'roomType', detailData.value.id)
    if (res.code === 200) {
      ElMessage.success('设置主图成功')
      await loadDetailImages(detailData.value.id)
    }
  } catch {
    ElMessage.error('设置失败')
  }
}

const handleDeleteImage = async (img) => {
  try {
    await ElMessageBox.confirm('确认删除该图片？', '提示', { type: 'warning' })
    const res = await api.hotel.deleteImage(img.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadDetailImages(detailData.value.id)
    }
  } catch {
    // cancelled
  }
}

const moveImage = async (index, direction) => {
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= detailImages.value.length) return
  const list = [...detailImages.value]
  const temp = list[newIndex]
  list[newIndex] = list[index]
  list[index] = temp
  try {
    await api.hotel.saveImages('roomType', detailData.value.id, list)
    await loadDetailImages(detailData.value.id)
  } catch {
    ElMessage.error('排序失败')
  }
}

const openFormDialog = (row) => {
  if (row) {
    Object.assign(formModel, {
      id: row.id,
      typeName: row.typeName || '',
      typeCode: row.typeCode || '',
      area: row.area || 30,
      bedType: row.bedType || '',
      maxOccupancy: row.maxOccupancy || 2,
      extraBedPolicy: row.extraBedPolicy || 'none',
      facilitiesList: parseFacilitiesList(row.facilities),
      basePrice: row.basePrice || 0,
      weekendPrice: row.weekendPrice || 0,
      costPrice: row.costPrice || 0,
      saleStatusSwitch: row.saleStatus === 1,
      description: row.description || ''
    })
  } else {
    Object.assign(formModel, {
      id: null,
      typeName: '',
      typeCode: '',
      area: 30,
      bedType: '',
      maxOccupancy: 2,
      extraBedPolicy: 'none',
      facilitiesList: [],
      basePrice: 0,
      weekendPrice: 0,
      costPrice: 0,
      saleStatusSwitch: true,
      description: ''
    })
  }
  formDialogVisible.value = true
}

const parseFacilitiesList = (facilities) => {
  if (!facilities) return []
  try {
    const parsed = JSON.parse(facilities)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

const handleSaveForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  formSaving.value = true
  try {
    const payload = {
      typeName: formModel.typeName,
      typeCode: formModel.typeCode,
      area: formModel.area,
      bedType: formModel.bedType,
      maxOccupancy: formModel.maxOccupancy,
      extraBedPolicy: formModel.extraBedPolicy,
      facilities: JSON.stringify(formModel.facilitiesList),
      basePrice: formModel.basePrice,
      weekendPrice: formModel.weekendPrice,
      saleStatus: formModel.saleStatusSwitch ? 1 : 0,
      description: formModel.description
    }
    if (hasPermission('hotel:roomType:price:edit')) {
      payload.costPrice = formModel.costPrice
    }
    let res
    if (formModel.id) {
      res = await api.hotel.updateRoomType({ id: formModel.id, ...payload })
    } else {
      res = await api.hotel.addRoomType(payload)
    }
    if (res.code === 200) {
      ElMessage.success(formModel.id ? '更新成功' : '新增成功')
      formDialogVisible.value = false
      detailVisible.value = false
      await loadRoomTypes()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch {
    ElMessage.error('操作失败')
  } finally {
    formSaving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除房型「${row.typeName}」？`, '提示', { type: 'warning' })
    const res = await api.hotel.deleteRoomType(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      detailVisible.value = false
      await loadRoomTypes()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch {
    // cancelled
  }
}

const handleToggleSaleStatus = async (row) => {
  const newStatus = row.saleStatus === 1 ? 0 : 1
  const label = newStatus === 1 ? '在售' : '停售'
  try {
    await ElMessageBox.confirm(`确认将房型「${row.typeName}」切换为${label}？`, '提示', { type: 'warning' })
    const res = await api.hotel.updateRoomTypeSaleStatus({ id: row.id, saleStatus: newStatus })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      await loadRoomTypes()
      row.saleStatus = newStatus
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch {
    // cancelled
  }
}

onMounted(() => {
  loadRoomTypes()
})
</script>

<style scoped>
.room-type-container {
  padding: 10px;
}

.toolbar-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 4px;
}

.price-sep {
  color: #a0aec0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.room-type-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  border: 1px solid #edf2f7;
}

.room-type-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  border-color: #667eea;
}

.card-image {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.room-type-card:hover .card-image img {
  transform: scale(1.05);
}

.card-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-status-tag {
  position: absolute;
  top: 10px;
  right: 10px;
}

.card-body {
  padding: 14px 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.card-area {
  font-size: 13px;
  color: #718096;
}

.card-price {
  margin-bottom: 6px;
}

.price-symbol {
  font-size: 14px;
  color: #f5576c;
  font-weight: 600;
}

.price-value {
  font-size: 22px;
  color: #f5576c;
  font-weight: 700;
}

.price-unit {
  font-size: 13px;
  color: #a0aec0;
}

.card-room-count {
  font-size: 13px;
  color: #a0aec0;
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
  color: #909399;
}

.empty-state p {
  margin: 0;
  font-size: 16px;
}

.list-card {
  border-radius: 12px;
  border: none;
}

.detail-content {
  padding: 0 4px;
}

.detail-gallery {
  margin-bottom: 20px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
}

.gallery-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  aspect-ratio: 1;
}

.gallery-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gallery-item-actions {
  position: absolute;
  top: 4px;
  right: 4px;
  display: flex;
  gap: 4px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  padding: 2px 4px;
}

.gallery-item-actions .el-icon {
  color: #fff;
  cursor: pointer;
  font-size: 14px;
}

.gallery-item-actions .el-icon:hover {
  color: #409eff;
}

.gallery-item-actions .star-active {
  color: #f7ba2a;
}

.gallery-item-sort {
  position: absolute;
  bottom: 4px;
  right: 4px;
  display: flex;
  gap: 2px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 4px;
  padding: 2px 4px;
}

.gallery-item-sort .el-icon {
  color: #fff;
  cursor: pointer;
  font-size: 12px;
}

.gallery-item-sort .el-icon:hover {
  color: #409eff;
}

.gallery-main-tag {
  position: absolute;
  bottom: 4px;
  left: 4px;
}

.gallery-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  gap: 8px;
  color: #909399;
}

.gallery-empty p {
  margin: 0;
  font-size: 13px;
}

.gallery-upload {
  margin-top: 10px;
}

.detail-section {
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
}

.facility-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.facility-tag {
  border-radius: 14px;
}

.detail-desc {
  margin: 0;
  font-size: 14px;
  color: #4a5568;
  line-height: 1.6;
  white-space: pre-wrap;
}

.detail-footer {
  display: flex;
  gap: 10px;
  padding-top: 16px;
  border-top: 1px solid #edf2f7;
}

.facility-checkbox-group {
  width: 100%;
}

.facility-category {
  margin-bottom: 8px;
}

.category-label {
  font-size: 13px;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 4px;
}

.facility-category :deep(.el-checkbox-group) {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
}

.facility-category :deep(.el-checkbox) {
  margin-right: 0;
}
</style>
