<template>
  <div class="hotel-dashboard-container">
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalBuildings }}</div>
              <div class="stat-label">总楼栋数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28"><CopyDocument /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalFloors }}</div>
              <div class="stat-label">总楼层数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><Key /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.totalRooms }}</div>
              <div class="stat-label">总房间数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><Ticket /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.activeRoomTypes }}</div>
              <div class="stat-label">在售房型数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab" class="dashboard-tabs">
      <el-tab-pane label="总览" name="overview">
        <el-row :gutter="20" class="section-row">
          <el-col :xs="24" :sm="12">
            <el-card class="status-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span>房间状态分布</span>
                  <el-button
                    v-if="hasPermission('hotel:dashboard:export')"
                    type="primary"
                    size="small"
                    plain
                    @click="handleExport"
                  >
                    <el-icon><Download /></el-icon>导出价格表
                  </el-button>
                </div>
              </template>
              <div class="status-list">
                <div v-for="item in statusStats" :key="item.status" class="status-item">
                  <div class="status-label-row">
                    <span class="status-dot" :style="{ background: statusColors[item.status] }"></span>
                    <span class="status-name">{{ statusLabels[item.status] }}</span>
                    <span class="status-count">{{ item.count }} 间</span>
                    <span class="status-percent">{{ getStatusPercent(item) }}%</span>
                  </div>
                  <el-progress
                    :percentage="getStatusPercent(item)"
                    :color="statusColors[item.status]"
                    :show-text="false"
                    :stroke-width="10"
                  />
                </div>
              </div>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12">
            <el-card class="type-card" shadow="hover">
              <template #header>
                <span>房型统计</span>
              </template>
              <el-table :data="roomTypeStats" stripe style="width: 100%">
                <el-table-column prop="typeName" label="房型" min-width="100" />
                <el-table-column prop="total" label="总数" width="70" align="center" />
                <el-table-column prop="available" label="空闲数" width="70" align="center" />
                <el-table-column label="占用率" min-width="120">
                  <template #default="{ row }">
                    <el-progress
                      :percentage="getOccupancy(row)"
                      :color="getOccupancyColor(getOccupancy(row))"
                      :stroke-width="10"
                    />
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="section-row">
          <el-col :span="24">
            <el-card class="floor-card" shadow="hover">
              <template #header>
                <span>楼层房间分布</span>
              </template>
              <el-table :data="floorStats" stripe style="width: 100%">
                <el-table-column prop="buildingName" label="楼栋" width="140" />
                <el-table-column prop="floorNumber" label="楼层" width="80" align="center" />
                <el-table-column prop="floorName" label="楼层名" width="140" />
                <el-table-column label="房间数" min-width="200">
                  <template #default="{ row }">
                    <div class="room-bar-wrapper">
                      <div
                        class="room-bar"
                        :style="{ width: getBarWidth(row.roomCount) + '%' }"
                      ></div>
                      <span class="room-bar-count">{{ row.roomCount }}</span>
                    </div>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="房型分析" name="roomType">
        <el-card class="tab-card" shadow="hover">
          <el-table
            :data="detailedTypeStats"
            stripe
            style="width: 100%"
            @row-click="handleTypeRowClick"
            class="clickable-table"
          >
            <el-table-column prop="typeName" label="房型名称" min-width="120" />
            <el-table-column prop="total" label="总数" width="80" align="center" />
            <el-table-column prop="free" label="空闲" width="80" align="center">
              <template #default="{ row }">
                <span style="color: #67C23A">{{ row.free }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="occupied" label="已入住" width="80" align="center">
              <template #default="{ row }">
                <span style="color: #409EFF">{{ row.occupied }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="maintenance" label="维修中" width="80" align="center">
              <template #default="{ row }">
                <span style="color: #F56C6C">{{ row.maintenance }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="freeRate" label="空闲率" width="140" sortable align="center">
              <template #default="{ row }">
                <span :style="{ color: getFreeRateColor(row.freeRate) }">
                  {{ (row.freeRate * 100).toFixed(1) }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="楼层分析" name="floor">
        <el-card class="tab-card" shadow="hover">
          <el-table
            :data="detailedFloorStats"
            stripe
            style="width: 100%"
            @row-click="handleFloorRowClick"
            class="clickable-table"
          >
            <el-table-column prop="buildingName" label="楼栋" min-width="120" />
            <el-table-column prop="floorNumber" label="楼层号" width="90" align="center" />
            <el-table-column prop="floorName" label="楼层名" min-width="120" />
            <el-table-column prop="total" label="总数" width="80" align="center" />
            <el-table-column prop="free" label="空闲" width="80" align="center">
              <template #default="{ row }">
                <span style="color: #67C23A">{{ row.free }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="occupied" label="已入住" width="80" align="center">
              <template #default="{ row }">
                <span style="color: #409EFF">{{ row.occupied }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="maintenance" label="维修中" width="80" align="center">
              <template #default="{ row }">
                <span style="color: #F56C6C">{{ row.maintenance }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="utilizationRate" label="利用率" width="140" sortable align="center">
              <template #default="{ row }">
                <span :style="{ color: getUtilRateColor(row.utilizationRate) }">
                  {{ (row.utilizationRate * 100).toFixed(1) }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="属性分布" name="attribute">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12">
            <el-card class="tab-card" shadow="hover">
              <template #header>
                <span>朝向分布</span>
              </template>
              <div ref="orientationChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-card class="tab-card" shadow="hover">
              <template #header>
                <span>景观分布</span>
              </template>
              <div ref="viewTypeChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="状态时长" name="duration">
        <el-card class="tab-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>空闲时段分布</span>
              <span class="total-free-tag">当前空闲: <strong>{{ durationTotalFree }}</strong> 间</span>
            </div>
          </template>
          <div ref="durationChartRef" class="chart-container-lg"></div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, CopyDocument, Key, Ticket, Download } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const statusLabels = { 1: '空闲', 2: '已预订', 3: '已入住', 4: '待清洁', 5: '清洁中', 6: '维修中', 7: '停用' }
const statusColors = { 1: '#67C23A', 2: '#E6A23C', 3: '#409EFF', 4: '#909399', 5: '#909399', 6: '#F56C6C', 7: '#C0C4CC' }

const activeTab = ref('overview')

const overview = ref({
  totalBuildings: 0,
  totalFloors: 0,
  totalRooms: 0,
  activeRoomTypes: 0
})

const statusStats = ref([])
const roomTypeStats = ref([])
const floorStats = ref([])
const detailedTypeStats = ref([])
const detailedFloorStats = ref([])
const durationTotalFree = ref(0)

const orientationChartRef = ref(null)
const viewTypeChartRef = ref(null)
const durationChartRef = ref(null)

let orientationChart = null
let viewTypeChart = null
let durationChart = null

const totalStatusCount = computed(() => {
  return statusStats.value.reduce((sum, item) => sum + item.count, 0)
})

const getStatusPercent = (item) => {
  if (totalStatusCount.value === 0) return 0
  return Math.round((item.count / totalStatusCount.value) * 100)
}

const getOccupancy = (row) => {
  if (!row.total || row.total === 0) return 0
  return Math.round(((row.total - row.available) / row.total) * 100)
}

const getOccupancyColor = (percentage) => {
  if (percentage < 50) return '#67C23A'
  if (percentage < 80) return '#E6A23C'
  return '#F56C6C'
}

const maxFloorRooms = computed(() => {
  if (floorStats.value.length === 0) return 1
  return Math.max(...floorStats.value.map(f => f.roomCount), 1)
})

const getBarWidth = (count) => {
  return Math.round((count / maxFloorRooms.value) * 100)
}

const getFreeRateColor = (rate) => {
  if (rate > 0.6) return '#67C23A'
  if (rate > 0.3) return '#E6A23C'
  return '#F56C6C'
}

const getUtilRateColor = (rate) => {
  if (rate > 0.8) return '#F56C6C'
  if (rate > 0.5) return '#E6A23C'
  return '#67C23A'
}

const handleTypeRowClick = (row) => {
  router.push({ path: '/hotel/room', query: { roomTypeId: row.typeId } })
}

const handleFloorRowClick = (row) => {
  router.push({ path: '/hotel/room', query: { floorId: row.floorId } })
}

const loadOverview = async () => {
  try {
    const res = await api.hotel.getDashboardOverview()
    if (res.code === 200 && res.data) {
      overview.value = res.data
    }
  } catch {}
}

const loadStatusStats = async () => {
  try {
    const res = await api.hotel.getRoomStatusStats()
    if (res.code === 200) {
      statusStats.value = res.data || []
    }
  } catch {}
}

const loadRoomTypeStats = async () => {
  try {
    const res = await api.hotel.getRoomTypeStats()
    if (res.code === 200) {
      roomTypeStats.value = res.data || []
    }
  } catch {}
}

const loadFloorStats = async () => {
  try {
    const res = await api.hotel.getFloorStats()
    if (res.code === 200) {
      floorStats.value = res.data || []
    }
  } catch {}
}

const loadDetailedTypeStats = async () => {
  try {
    const res = await api.hotel.getDetailedRoomTypeStats()
    if (res.code === 200 && res.data) {
      detailedTypeStats.value = res.data.typeStats || []
    }
  } catch {}
}

const loadDetailedFloorStats = async () => {
  try {
    const res = await api.hotel.getDetailedFloorStats()
    if (res.code === 200 && res.data) {
      detailedFloorStats.value = res.data.floorStats || []
    }
  } catch {}
}

const loadAttributeDistribution = async () => {
  try {
    const res = await api.hotel.getAttributeDistribution()
    if (res.code === 200 && res.data) {
      await nextTick()
      renderOrientationChart(res.data.orientationDist || [])
      renderViewTypeChart(res.data.viewTypeDist || [])
    }
  } catch {}
}

const loadStatusDurationStats = async () => {
  try {
    const res = await api.hotel.getStatusDurationStats()
    if (res.code === 200 && res.data) {
      durationTotalFree.value = res.data.totalFree || 0
      await nextTick()
      renderDurationChart(res.data.durationStats || [])
    }
  } catch {}
}

const renderOrientationChart = (data) => {
  if (!orientationChartRef.value) return
  if (!orientationChart) {
    orientationChart = echarts.init(orientationChartRef.value)
  }
  orientationChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 60, right: 30, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.name),
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.count),
      barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      }
    }]
  })
}

const renderViewTypeChart = (data) => {
  if (!viewTypeChartRef.value) return
  if (!viewTypeChart) {
    viewTypeChart = echarts.init(viewTypeChartRef.value)
  }
  viewTypeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#718096' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#4a5568' },
      data: data.map(d => ({ name: d.name, value: d.count })),
      color: ['#667eea', '#43e97b', '#f093fb', '#4facfe', '#f5576c', '#ffa726', '#ab47bc', '#26c6da']
    }]
  })
}

const renderDurationChart = (data) => {
  if (!durationChartRef.value) return
  if (!durationChart) {
    durationChart = echarts.init(durationChartRef.value)
  }
  const colors = ['#67C23A', '#E6A23C', '#409EFF', '#F56C6C']
  durationChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 60, right: 30, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.range),
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: data.map((d, i) => ({
        value: d.count,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: colors[i % colors.length] },
            { offset: 1, color: colors[i % colors.length] + '80' }
          ])
        }
      })),
      barWidth: '45%',
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c} 间' }
    }]
  })
}

const handleResize = () => {
  orientationChart?.resize()
  viewTypeChart?.resize()
  durationChart?.resize()
}

const handleExport = () => {
  ElMessage.info('价格表导出功能')
}

const loadTabData = (tab) => {
  if (tab === 'roomType') {
    loadDetailedTypeStats()
  } else if (tab === 'floor') {
    loadDetailedFloorStats()
  } else if (tab === 'attribute') {
    loadAttributeDistribution()
  } else if (tab === 'duration') {
    loadStatusDurationStats()
  }
}

watch(activeTab, (val) => {
  loadTabData(val)
})

onMounted(() => {
  Promise.all([loadOverview(), loadStatusStats(), loadRoomTypeStats(), loadFloorStats()])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  orientationChart?.dispose()
  viewTypeChart?.dispose()
  durationChart?.dispose()
})
</script>

<style scoped>
.hotel-dashboard-container {
  padding: 10px;
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

.section-row {
  margin-bottom: 20px;
}

.status-card,
.type-card,
.floor-card,
.tab-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.total-free-tag {
  font-size: 14px;
  color: #718096;
}

.total-free-tag strong {
  color: #67C23A;
  font-size: 18px;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.status-label-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-name {
  font-size: 14px;
  color: #2d3748;
  min-width: 56px;
}

.status-count {
  font-size: 14px;
  color: #718096;
  flex: 1;
}

.status-percent {
  font-size: 13px;
  color: #a0aec0;
  min-width: 36px;
  text-align: right;
}

.room-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.room-bar {
  height: 16px;
  border-radius: 4px;
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  transition: width 0.4s ease;
  min-width: 2px;
}

.room-bar-count {
  font-size: 13px;
  color: #4a5568;
  min-width: 28px;
}

.dashboard-tabs {
  margin-top: 10px;
}

.dashboard-tabs :deep(.el-tabs__item) {
  font-size: 15px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.chart-container-lg {
  width: 100%;
  height: 400px;
}

.clickable-table :deep(.el-table__row) {
  cursor: pointer;
}

.clickable-table :deep(.el-table__row:hover) {
  background-color: #f0f5ff;
}
</style>
