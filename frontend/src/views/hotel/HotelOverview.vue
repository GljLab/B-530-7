<template>
  <div class="hotel-overview-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card v-if="!hotelInfo" class="empty-hotel-card" shadow="hover">
          <div class="empty-state">
            <el-icon :size="64" color="#c0c4cc"><House /></el-icon>
            <p class="empty-text">暂未配置酒店信息</p>
            <el-button v-if="hasPermission('hotel:info:edit')" type="primary" @click="openInfoDialog">
              配置酒店信息
            </el-button>
          </div>
        </el-card>

        <el-card v-else class="hotel-info-card" shadow="hover">
          <div class="hotel-info-content">
            <div class="hotel-info-main">
              <div class="hotel-name-row">
                <h2 class="hotel-name">{{ hotelInfo.hotelName }}</h2>
                <el-rate v-model="hotelInfo.starRating" disabled :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']" />
              </div>
              <div class="hotel-meta">
                <el-tag v-if="hotelInfo.brand" type="info" size="small" class="meta-tag">
                  <el-icon><Flag /></el-icon>{{ hotelInfo.brand }}
                </el-tag>
                <span class="meta-item">
                  <el-icon><Location /></el-icon>{{ hotelInfo.address }}
                </span>
                <span class="meta-item">
                  <el-icon><Phone /></el-icon>{{ hotelInfo.contactPhone }}
                </span>
              </div>
            </div>
            <el-button
              v-if="hasPermission('hotel:info:edit')"
              type="primary"
              plain
              @click="openInfoDialog"
            >
              <el-icon><Edit /></el-icon>编辑
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row v-if="hotelInfo" :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="carousel-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>酒店图片</span>
              <el-button
                v-if="hasPermission('hotel:image:edit')"
                type="primary"
                size="small"
                @click="triggerUpload"
              >
                <el-icon><Plus /></el-icon>上传图片
              </el-button>
              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                multiple
                style="display: none"
                @change="handleFileChange"
              />
            </div>
          </template>
          <div v-if="images.length > 0" class="carousel-wrapper">
            <el-carousel height="320px" :interval="4000" arrow="always">
              <el-carousel-item v-for="(img, index) in images" :key="img.id || index">
                <div class="carousel-item-wrapper">
                  <img :src="img.url" class="carousel-img" />
                  <div v-if="hasPermission('hotel:image:edit')" class="image-actions">
                    <el-button
                      v-if="!img.isMain"
                      size="small"
                      type="warning"
                      plain
                      @click="handleSetMain(img)"
                    >
                      设为主图
                    </el-button>
                    <el-tag v-else size="small" type="success">主图</el-tag>
                    <el-button
                      size="small"
                      type="danger"
                      plain
                      @click="handleDeleteImage(img)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>
          <div v-else class="empty-images">
            <el-icon :size="40" color="#c0c4cc"><Picture /></el-icon>
            <p>暂无酒店图片</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewStats.totalBuildings }}</div>
              <div class="stat-label">楼栋总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><Key /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewStats.totalRooms }}</div>
              <div class="stat-label">房间总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28"><PriceTag /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewStats.activeRoomTypes }}</div>
              <div class="stat-label">活跃房型</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overviewStats.availableRooms }}</div>
              <div class="stat-label">可用房间</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="facility-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>酒店设施</span>
              <el-button
                v-if="hasPermission('hotel:facility:edit')"
                type="primary"
                size="small"
                plain
                @click="openFacilityDialog"
              >
                <el-icon><Edit /></el-icon>编辑设施
              </el-button>
            </div>
          </template>
          <div v-if="facilities.length > 0" class="facility-grid">
            <div v-for="item in facilities" :key="item.id || item.name" class="facility-item">
              <el-icon :size="28" :color="item.color || '#667eea'">
                <component :is="item.icon || 'Coin'" />
              </el-icon>
              <span class="facility-name">{{ item.name }}</span>
              <span class="facility-time">{{ item.openTime || '24小时' }}</span>
            </div>
          </div>
          <div v-else class="empty-facilities">
            <el-icon :size="40" color="#c0c4cc"><Coin /></el-icon>
            <p>暂未配置酒店设施</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="quick-access-card" shadow="hover">
          <template #header>
            <span>快捷入口</span>
          </template>
          <div class="quick-access-grid">
            <div class="quick-item" @click="$router.push('/hotel/building')">
              <el-icon :size="32" color="#667eea"><OfficeBuilding /></el-icon>
              <span>楼栋楼层管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/hotel/roomType')">
              <el-icon :size="32" color="#f5576c"><PriceTag /></el-icon>
              <span>房型管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/hotel/room')">
              <el-icon :size="32" color="#4facfe"><Key /></el-icon>
              <span>房间管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/hotel/dashboard')">
              <el-icon :size="32" color="#43e97b"><DataAnalysis /></el-icon>
              <span>统计看板</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="infoDialogVisible"
      :title="hotelInfo ? '编辑酒店信息' : '配置酒店信息'"
      width="640px"
      destroy-on-close
    >
      <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="100px">
        <el-form-item label="酒店名称" prop="hotelName">
          <el-input v-model="infoForm.hotelName" placeholder="请输入酒店名称" />
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="infoForm.brand" placeholder="请输入品牌" />
        </el-form-item>
        <el-form-item label="星级" prop="starRating">
          <el-select v-model="infoForm.starRating" placeholder="请选择星级" style="width: 100%">
            <el-option :value="3" label="三星" />
            <el-option :value="4" label="四星" />
            <el-option :value="5" label="五星" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="infoForm.address" placeholder="请输入酒店地址" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="infoForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业执照号" prop="licenseNumber">
          <el-input v-model="infoForm.licenseNumber" placeholder="请输入营业执照号" />
        </el-form-item>
        <el-form-item label="开业日期" prop="openingDate">
          <el-date-picker
            v-model="infoForm.openingDate"
            type="date"
            placeholder="请选择开业日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="infoForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入酒店描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="infoDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="infoSaving" @click="handleSaveInfo">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="facilityDialogVisible"
      title="编辑酒店设施"
      width="680px"
      destroy-on-close
    >
      <div class="facility-edit-list">
        <div v-for="(item, index) in facilityEditList" :key="index" class="facility-edit-item">
          <el-input v-model="item.name" placeholder="设施名称" style="width: 140px" />
          <el-input v-model="item.icon" placeholder="图标名称" style="width: 120px" />
          <el-input v-model="item.openTime" placeholder="开放时间" style="width: 120px" />
          <el-input v-model="item.color" placeholder="颜色" style="width: 100px" />
          <el-button type="danger" :icon="Delete" circle size="small" @click="removeFacilityItem(index)" />
        </div>
      </div>
      <el-button type="primary" plain size="small" @click="addFacilityItem" style="margin-top: 12px">
        <el-icon><Plus /></el-icon>添加设施
      </el-button>
      <template #footer>
        <el-button @click="facilityDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="facilitySaving" @click="handleSaveFacilities">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  House, Edit, Location, Phone, Flag, Plus, Delete,
  OfficeBuilding, Key, PriceTag, Check, Picture, Coin,
  DataAnalysis, Van, Coffee, SetUp, Dish, ChatDotRound
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const hotelInfo = ref(null)
const images = ref([])
const facilities = ref([])
const overviewStats = ref({
  totalBuildings: 0,
  totalRooms: 0,
  activeRoomTypes: 0,
  availableRooms: 0
})

const infoDialogVisible = ref(false)
const infoSaving = ref(false)
const infoFormRef = ref(null)
const infoForm = reactive({
  hotelName: '',
  brand: '',
  starRating: null,
  address: '',
  contactPhone: '',
  licenseNumber: '',
  openingDate: '',
  description: ''
})

const infoRules = {
  hotelName: [{ required: true, message: '请输入酒店名称', trigger: 'blur' }],
  starRating: [{ required: true, message: '请选择星级', trigger: 'change' }]
}

const facilityDialogVisible = ref(false)
const facilitySaving = ref(false)
const facilityEditList = ref([])

const fileInputRef = ref(null)

const loadHotelInfo = async () => {
  try {
    const res = await api.hotel.getInfo()
    if (res.code === 200 && res.data) {
      hotelInfo.value = res.data
    } else {
      hotelInfo.value = null
    }
  } catch {
    hotelInfo.value = null
  }
}

const loadImages = async () => {
  if (!hotelInfo.value) return
  try {
    const res = await api.hotel.getImages('hotel', hotelInfo.value.id)
    if (res.code === 200) {
      images.value = res.data || []
    }
  } catch {
    images.value = []
  }
}

const loadFacilities = async () => {
  if (!hotelInfo.value) return
  try {
    const res = await api.hotel.getFacilities(hotelInfo.value.id)
    if (res.code === 200) {
      facilities.value = (res.data || []).map(f => ({
        ...f,
        name: f.facilityName || f.name,
        icon: f.facilityIcon || f.icon,
        color: f.description || f.color || '#667eea'
      }))
    }
  } catch {
    facilities.value = []
  }
}

const loadOverviewStats = async () => {
  try {
    const res = await api.hotel.getDashboardOverview()
    if (res.code === 200 && res.data) {
      overviewStats.value = res.data
    }
  } catch {
    // keep defaults
  }
}

const openInfoDialog = () => {
  if (hotelInfo.value) {
    Object.assign(infoForm, {
      hotelName: hotelInfo.value.hotelName || '',
      brand: hotelInfo.value.brand || '',
      starRating: hotelInfo.value.starRating || null,
      address: hotelInfo.value.address || '',
      contactPhone: hotelInfo.value.contactPhone || '',
      licenseNumber: hotelInfo.value.licenseNumber || '',
      openingDate: hotelInfo.value.openingDate || '',
      description: hotelInfo.value.description || ''
    })
  } else {
    Object.assign(infoForm, {
      hotelName: '',
      brand: '',
      starRating: null,
      address: '',
      contactPhone: '',
      licenseNumber: '',
      openingDate: '',
      description: ''
    })
  }
  infoDialogVisible.value = true
}

const handleSaveInfo = async () => {
  const valid = await infoFormRef.value.validate().catch(() => false)
  if (!valid) return

  infoSaving.value = true
  try {
    const res = await api.hotel.saveInfo({ ...infoForm })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      infoDialogVisible.value = false
      await loadHotelInfo()
      if (hotelInfo.value) {
        await Promise.all([loadImages(), loadFacilities()])
      }
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    infoSaving.value = false
  }
}

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (e) => {
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
          isMain: images.value.length === 0,
          refType: 'hotel',
          refId: hotelInfo.value.id
        }
        images.value.push(newImage)
      }
    } catch {
      ElMessage.error('图片上传失败')
    }
  }

  try {
    await api.hotel.saveImages('hotel', hotelInfo.value.id, images.value)
    await loadImages()
  } catch {
    ElMessage.error('保存图片信息失败')
  }

  e.target.value = ''
}

const handleSetMain = async (img) => {
  try {
    const res = await api.hotel.setMainImage(img.id, 'hotel', hotelInfo.value.id)
    if (res.code === 200) {
      ElMessage.success('设置成功')
      await loadImages()
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
      await loadImages()
    }
  } catch {
    // cancelled or error
  }
}

const openFacilityDialog = () => {
  facilityEditList.value = facilities.value.length > 0
    ? facilities.value.map(f => ({ ...f }))
    : []
  facilityDialogVisible.value = true
}

const addFacilityItem = () => {
  facilityEditList.value.push({
    name: '',
    icon: 'Coin',
    openTime: '24小时',
    color: '#667eea'
  })
}

const removeFacilityItem = (index) => {
  facilityEditList.value.splice(index, 1)
}

const handleSaveFacilities = async () => {
  facilitySaving.value = true
  try {
    const payload = facilityEditList.value.map((f, index) => ({
      ...f,
      facilityName: f.name,
      facilityIcon: f.icon,
      description: f.color,
      sortOrder: index
    }))
    const res = await api.hotel.saveFacilities(hotelInfo.value.id, payload)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      facilityDialogVisible.value = false
      await loadFacilities()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('保存失败')
  } finally {
    facilitySaving.value = false
  }
}

onMounted(async () => {
  await loadHotelInfo()
  await loadOverviewStats()
  if (hotelInfo.value) {
    await Promise.all([loadImages(), loadFacilities()])
  }
})
</script>

<style scoped>
.hotel-overview-container {
  padding: 10px;
}

.empty-hotel-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 16px;
}

.empty-text {
  font-size: 16px;
  color: #909399;
  margin: 0;
}

.hotel-info-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 20px;
}

.hotel-info-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hotel-info-main {
  flex: 1;
}

.hotel-name-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.hotel-name {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #2d3748;
}

.hotel-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  color: #718096;
  font-size: 14px;
}

.meta-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.section-row {
  margin-bottom: 20px;
}

.carousel-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.carousel-wrapper {
  border-radius: 8px;
  overflow: hidden;
}

.carousel-item-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.carousel-img {
  width: 100%;
  height: 320px;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  bottom: 16px;
  right: 16px;
  display: flex;
  gap: 8px;
}

.empty-images,
.empty-facilities {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  gap: 8px;
  color: #909399;
}

.empty-images p,
.empty-facilities p {
  margin: 0;
  font-size: 14px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2d3748;
}

.stat-label {
  font-size: 14px;
  color: #718096;
}

.facility-card {
  border-radius: 12px;
  border: none;
}

.facility-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
}

.facility-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 20px 12px;
  background: #f7fafc;
  border-radius: 12px;
  transition: all 0.3s;
}

.facility-item:hover {
  background: #edf2f7;
  transform: translateY(-3px);
}

.facility-name {
  font-size: 14px;
  font-weight: 500;
  color: #2d3748;
}

.facility-time {
  font-size: 12px;
  color: #a0aec0;
}

.quick-access-card {
  border-radius: 12px;
  border: none;
}

.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 20px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  background: #f7fafc;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-item:hover {
  background: #edf2f7;
  transform: translateY(-3px);
}

.quick-item span {
  font-size: 14px;
  color: #4a5568;
}

.facility-edit-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.facility-edit-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
