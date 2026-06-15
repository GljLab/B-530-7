<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 欢迎卡片 -->
      <el-col :span="24">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-text">
              <h2>欢迎回来，{{ userStore.user?.nickname || '用户' }}！</h2>
              <p>今天是 {{ currentDate }}，祝您工作愉快！</p>
            </div>
            <div class="welcome-avatar">
              <el-avatar :size="80" :src="userStore.user?.avatar">
                {{ userStore.user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ hotelStats.totalBuildings }}</div>
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
              <div class="stat-value">{{ hotelStats.totalRooms }}</div>
              <div class="stat-label">房间总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ hotelStats.activeRoomTypes }}</div>
              <div class="stat-label">在售房型</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ hotelStats.availableRooms }}</div>
              <div class="stat-label">空闲房间</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20">
      <!-- 快捷入口 -->
      <el-col :span="24">
        <el-card class="quick-access-card">
          <template #header>
            <span>快捷入口</span>
          </template>
          <div class="quick-access-grid">
            <div class="quick-item" @click="$router.push('/hotel/overview')" v-if="hasPermission('hotel:info:list')">
              <el-icon :size="32" color="#667eea"><OfficeBuilding /></el-icon>
              <span>酒店概览</span>
            </div>
            <div class="quick-item" @click="$router.push('/hotel/building')" v-if="hasPermission('hotel:building:list')">
              <el-icon :size="32" color="#f5576c"><School /></el-icon>
              <span>楼栋楼层</span>
            </div>
            <div class="quick-item" @click="$router.push('/hotel/roomType')" v-if="hasPermission('hotel:roomType:list')">
              <el-icon :size="32" color="#4facfe"><Tickets /></el-icon>
              <span>房型管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/hotel/room')" v-if="hasPermission('hotel:room:list')">
              <el-icon :size="32" color="#43e97b"><Key /></el-icon>
              <span>房间管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/hotel/dashboard')" v-if="hasPermission('hotel:dashboard:list')">
              <el-icon :size="32" color="#e6a23c"><DataAnalysis /></el-icon>
              <span>统计看板</span>
            </div>
            <div class="quick-item" @click="$router.push('/system/user')" v-if="hasPermission('system:user:list')">
              <el-icon :size="32" color="#667eea"><User /></el-icon>
              <span>用户管理</span>
            </div>
            <div class="quick-item" @click="$router.push('/profile')">
              <el-icon :size="32" color="#909399"><Setting /></el-icon>
              <span>个人中心</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { User, Avatar, Menu, Lock, Setting, Key, Tickets, OfficeBuilding, School, DataAnalysis } from '@element-plus/icons-vue'
import api from '@/api'

const userStore = useUserStore()

const hotelStats = ref({
  totalBuildings: 0,
  totalFloors: 0,
  totalRooms: 0,
  activeRoomTypes: 0,
  availableRooms: 0
})

const currentDate = computed(() => {
  const date = new Date()
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  return date.toLocaleDateString('zh-CN', options)
})

const hasPermission = (permission) => {
  return userStore.hasPermission(permission)
}

onMounted(async () => {
  try {
    const res = await api.hotel.getDashboardOverview()
    if (res.code === 200 && res.data) {
      hotelStats.value = res.data
    }
  } catch (e) {
    // ignore
  }
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 12px;
  margin-bottom: 20px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.welcome-text h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.welcome-text p {
  margin: 0;
  opacity: 0.9;
}

.stat-row {
  margin-bottom: 20px;
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

.quick-access-card {
  border-radius: 12px;
  border: none;
}

.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
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
</style>
