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
                    @click="openExportDialog"
                  >
                    <el-icon><Download /></el-icon>导出
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
          <template #header>
            <div class="card-header">
              <span>房型分析</span>
              <el-button
                v-if="hasPermission('hotel:dashboard:export')"
                type="primary"
                size="small"
                plain
                @click="openExportDialog"
              >
                <el-icon><Download /></el-icon>导出
              </el-button>
            </div>
          </template>
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
          <template #header>
            <div class="card-header">
              <span>楼层分析</span>
              <el-button
                v-if="hasPermission('hotel:dashboard:export')"
                type="primary"
                size="small"
                plain
                @click="openExportDialog"
              >
                <el-icon><Download /></el-icon>导出
              </el-button>
            </div>
          </template>
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
                <div class="card-header">
                  <span>朝向分布</span>
                  <el-button
                    v-if="hasPermission('hotel:dashboard:export')"
                    type="primary"
                    size="small"
                    plain
                    @click="openExportDialog"
                  >
                    <el-icon><Download /></el-icon>导出
                  </el-button>
                </div>
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

      <el-tab-pane label="状态趋势" name="statusTrend">
        <el-card class="tab-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>状态趋势分析</span>
              <el-button
                v-if="hasPermission('hotel:dashboard:export')"
                type="primary"
                size="small"
                plain
                @click="openExportDialog"
              >
                <el-icon><Download /></el-icon>导出
              </el-button>
            </div>
          </template>
          <div class="trend-filter">
            <el-select v-model="trendDays" size="small" style="width: 140px; margin-right: 16px" @change="loadStatusTrend">
              <el-option label="近7天" :value="7" />
              <el-option label="近30天" :value="30" />
              <el-option label="近90天" :value="90" />
            </el-select>
            <el-select
              v-model="selectedStatuses"
              multiple
              collapse-tags
              collapse-tags-tooltip
              placeholder="选择状态"
              size="small"
              style="min-width: 280px"
              @change="reRenderStatusTrendChart"
            >
              <el-option v-for="(label, key) in statusLabels" :key="key" :label="label" :value="Number(key)" />
            </el-select>
          </div>
          <div ref="statusTrendChartRef" class="chart-container-lg"></div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="楼层使用率" name="floorUsage">
        <el-card class="tab-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>楼层使用率统计</span>
              <div class="header-actions">
                <el-radio-group v-model="floorUsageSortOrder" size="small" @change="sortFloorUsage">
                  <el-radio-button label="desc">使用率从高到低</el-radio-button>
                  <el-radio-button label="asc">使用率从低到高</el-radio-button>
                </el-radio-group>
                <el-button
                  v-if="hasPermission('hotel:dashboard:export')"
                  type="primary"
                  size="small"
                  plain
                  style="margin-left: 12px"
                  @click="openExportDialog"
                >
                  <el-icon><Download /></el-icon>导出
                </el-button>
              </div>
            </div>
          </template>
          <el-table
            :data="sortedFloorUsage"
            stripe
            style="width: 100%"
            @row-click="handleFloorUsageRowClick"
            class="clickable-table"
          >
            <el-table-column prop="buildingName" label="楼栋" min-width="120" />
            <el-table-column prop="floorNumber" label="楼层" width="80" align="center" />
            <el-table-column prop="total" label="总数" width="70" align="center" />
            <el-table-column prop="free" label="空闲" width="70" align="center">
              <template #default="{ row }">
                <span style="color: #67C23A">{{ row.free }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="occupied" label="已入住" width="80" align="center">
              <template #default="{ row }">
                <span style="color: #409EFF">{{ row.occupied }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="maintenance" label="维修" width="70" align="center">
              <template #default="{ row }">
                <span style="color: #F56C6C">{{ row.maintenance }}</span>
              </template>
            </el-table-column>
            <el-table-column label="使用率" min-width="180">
              <template #default="{ row }">
                <el-progress
                  :percentage="getFloorUsageRate(row)"
                  :color="getFloorUsageColor(getFloorUsageRate(row))"
                  :stroke-width="12"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="房型热度" name="roomTypeHeat">
        <el-card class="tab-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>房型热度分析</span>
              <el-button
                v-if="hasPermission('hotel:dashboard:export')"
                type="primary"
                size="small"
                plain
                @click="openExportDialog"
              >
                <el-icon><Download /></el-icon>导出
              </el-button>
            </div>
          </template>
          <el-table :data="roomTypeHeatStats" stripe style="width: 100%" class="heat-table">
            <el-table-column prop="typeName" label="房型名称" min-width="160" />
            <el-table-column prop="total" label="总数" width="80" align="center" />
            <el-table-column prop="freeCount" label="空闲数" width="90" align="center" />
            <el-table-column label="空闲率" width="160" align="center">
              <template #default="{ row }">
                <el-tag
                  :type="getRoomTypeHeatTagType(row.freeRate)"
                  effect="light"
                  size="default"
                >
                  {{ (row.freeRate * 100).toFixed(1) }}%
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="chart-wrapper">
            <div ref="roomTypeHeatChartRef" class="chart-container-lg"></div>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="状态时长" name="statusDuration">
        <el-card class="tab-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>状态时长分析</span>
              <el-button
                v-if="hasPermission('hotel:dashboard:export')"
                type="primary"
                size="small"
                plain
                @click="openExportDialog"
              >
                <el-icon><Download /></el-icon>导出
              </el-button>
            </div>
          </template>
          <el-row :gutter="20" class="duration-stats-row">
            <el-col :xs="12" :sm="6">
              <div class="duration-stat-item">
                <div class="duration-stat-icon blue">
                  <el-icon :size="24"><Brush /></el-icon>
                </div>
                <div class="duration-stat-info">
                  <div class="duration-stat-value">{{ enhancedDurationStats.avgCleanHours || '0.0' }} 小时</div>
                  <div class="duration-stat-label">平均清洁时长</div>
                </div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="duration-stat-item">
                <div class="duration-stat-icon red">
                  <el-icon :size="24"><Tools /></el-icon>
                </div>
                <div class="duration-stat-info">
                  <div class="duration-stat-value">{{ enhancedDurationStats.avgRepairDays || '0.0' }} 天</div>
                  <div class="duration-stat-label">平均维修时长</div>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" class="section-row">
            <el-col :xs="24" :sm="12">
              <el-card class="inner-chart-card" shadow="never">
                <template #header>
                  <span>空闲时长分布</span>
                </template>
                <div ref="freeDurationPieChartRef" class="chart-container"></div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-card class="inner-chart-card" shadow="never">
                <template #header>
                  <span>长期空闲房间（超过15天）</span>
                </template>
                <el-table :data="longIdleRooms" stripe style="width: 100%" height="320" max-height="320">
                  <el-table-column prop="roomNo" label="房号" width="90" />
                  <el-table-column prop="buildingName" label="楼栋" width="100" />
                  <el-table-column prop="floorNumber" label="楼层" width="60" align="center" />
                  <el-table-column prop="typeName" label="房型" min-width="100" />
                  <el-table-column label="空闲天数" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag type="danger" effect="light">{{ row.idleDays }}</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog
      v-model="exportDialogVisible"
      title="导出报表"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form :model="exportForm" label-width="100px" ref="exportFormRef">
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportForm.scope">
            <el-radio label="current">当前标签页</el-radio>
            <el-radio label="all">全部标签页</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="exportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="楼层">
          <el-select v-model="exportForm.floorIds" multiple placeholder="全部楼层" style="width: 100%">
            <el-option
              v-for="floor in allFloors"
              :key="floor.floorId"
              :label="`${floor.buildingName} - ${floor.floorName}`"
              :value="floor.floorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="房型">
          <el-select v-model="exportForm.roomTypeIds" multiple placeholder="全部房型" style="width: 100%">
            <el-option
              v-for="rt in allRoomTypes"
              :key="rt.typeId"
              :label="rt.typeName"
              :value="rt.typeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="包含图表">
          <el-switch v-model="exportForm.includeChart" />
        </el-form-item>
        <el-form-item label="导出预览">
          <el-descriptions :column="1" border size="small" class="export-preview">
            <el-descriptions-item label="标签页">
              {{ exportForm.scope === 'current' ? tabLabels[activeTab] : '全部' }}
            </el-descriptions-item>
            <el-descriptions-item label="日期范围">
              {{ formatPreviewDateRange }}
            </el-descriptions-item>
            <el-descriptions-item label="楼层数量">
              {{ exportForm.floorIds.length === 0 ? '全部' : exportForm.floorIds.length + ' 个' }}
            </el-descriptions-item>
            <el-descriptions-item label="房型数量">
              {{ exportForm.roomTypeIds.length === 0 ? '全部' : exportForm.roomTypeIds.length + ' 个' }}
            </el-descriptions-item>
            <el-descriptions-item label="包含图表">
              {{ exportForm.includeChart ? '是' : '否' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="exporting" @click="handleConfirmExport">确认导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { OfficeBuilding, CopyDocument, Key, Ticket, Download, Brush, Tools } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import api from '@/api'

const router = useRouter()
const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const statusLabels = { 1: '空闲', 2: '已预订', 3: '已入住', 4: '待清洁', 5: '清洁中', 6: '维修中', 7: '停用' }
const statusColors = { 1: '#67C23A', 2: '#E6A23C', 3: '#409EFF', 4: '#909399', 5: '#909399', 6: '#F56C6C', 7: '#C0C4CC' }

const tabLabels = {
  overview: '总览',
  roomType: '房型分析',
  floor: '楼层分析',
  attribute: '属性分布',
  statusTrend: '状态趋势',
  floorUsage: '楼层使用率',
  roomTypeHeat: '房型热度',
  statusDuration: '状态时长'
}

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

const orientationChartRef = ref(null)
const viewTypeChartRef = ref(null)
const statusTrendChartRef = ref(null)
const roomTypeHeatChartRef = ref(null)
const freeDurationPieChartRef = ref(null)

let orientationChart = null
let viewTypeChart = null
let statusTrendChart = null
let roomTypeHeatChart = null
let freeDurationPieChart = null

const trendDays = ref(30)
const selectedStatuses = ref([1, 2, 3, 4, 5, 6, 7])
const statusTrendData = ref([])

const floorUsageStats = ref([])
const floorUsageSortOrder = ref('desc')

const roomTypeHeatStats = ref([])

const enhancedDurationStats = ref({})
const longIdleRooms = ref([])

const exportDialogVisible = ref(false)
const exporting = ref(false)
const exportFormRef = ref(null)
const allFloors = ref([])
const allRoomTypes = ref([])
const exportForm = ref({
  scope: 'current',
  dateRange: [],
  floorIds: [],
  roomTypeIds: [],
  includeChart: true
})

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

const getFloorUsageRate = (row) => {
  const available = (row.total || 0) - (row.maintenance || 0)
  if (available <= 0) return 0
  return Math.round(((row.occupied || 0) / available) * 100)
}

const getFloorUsageColor = (percentage) => {
  if (percentage > 80) return '#F56C6C'
  if (percentage > 50) return '#E6A23C'
  return '#67C23A'
}

const getRoomTypeHeatTagType = (rate) => {
  if (rate < 0.2) return 'danger'
  if (rate < 0.5) return 'warning'
  return 'success'
}

const sortedFloorUsage = computed(() => {
  const list = [...floorUsageStats.value]
  list.sort((a, b) => {
    const rateA = getFloorUsageRate(a)
    const rateB = getFloorUsageRate(b)
    return floorUsageSortOrder.value === 'desc' ? rateB - rateA : rateA - rateB
  })
  return list
})

const formatPreviewDateRange = computed(() => {
  if (!exportForm.value.dateRange || exportForm.value.dateRange.length === 0) return '全部'
  const [start, end] = exportForm.value.dateRange
  return `${start} 至 ${end}`
})

const handleTypeRowClick = (row) => {
  router.push({ path: '/hotel/room', query: { roomTypeId: row.typeId } })
}

const handleFloorRowClick = (row) => {
  router.push({ path: '/hotel/room', query: { floorId: row.floorId } })
}

const handleFloorUsageRowClick = (row) => {
  router.push({ path: '/hotel/room', query: { floorId: row.floorId } })
}

const sortFloorUsage = () => {}

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

const loadStatusTrend = async () => {
  try {
    const res = await api.hotel.getStatusTrend(trendDays.value)
    if (res.code === 200 && res.data) {
      statusTrendData.value = res.data
      await nextTick()
      renderStatusTrendChart()
    }
  } catch {}
}

const reRenderStatusTrendChart = () => {
  if (statusTrendData.value.length > 0) {
    nextTick(() => renderStatusTrendChart())
  }
}

const loadFloorUsageStats = async () => {
  try {
    const res = await api.hotel.getFloorUsageStats()
    if (res.code === 200) {
      floorUsageStats.value = res.data || []
    }
  } catch {}
}

const loadRoomTypeHeatStats = async () => {
  try {
    const res = await api.hotel.getRoomTypeHeatStats()
    if (res.code === 200 && res.data) {
      roomTypeHeatStats.value = res.data
      await nextTick()
      renderRoomTypeHeatChart()
    }
  } catch {}
}

const loadStatusDurationStatsEnhanced = async () => {
  try {
    const res = await api.hotel.getStatusDurationStatsEnhanced()
    if (res.code === 200 && res.data) {
      enhancedDurationStats.value = res.data || {}
      longIdleRooms.value = res.data.longIdleRooms || []
      await nextTick()
      renderFreeDurationPieChart(res.data.freeDurationDist || [])
    }
  } catch {}
}

const loadExportOptions = async () => {
  try {
    const [floorRes, typeRes] = await Promise.all([
      api.hotel.getFloorStats(),
      api.hotel.getRoomTypeStats()
    ])
    if (floorRes.code === 200) {
      allFloors.value = floorRes.data || []
    }
    if (typeRes.code === 200) {
      allRoomTypes.value = typeRes.data || []
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

const renderStatusTrendChart = () => {
  if (!statusTrendChartRef.value) return
  if (!statusTrendChart) {
    statusTrendChart = echarts.init(statusTrendChartRef.value)
  }
  const data = statusTrendData.value || []
  if (data.length === 0) {
    statusTrendChart.clear()
    return
  }
  const dates = data.map(d => d.date)
  const series = selectedStatuses.value.map(status => ({
    name: statusLabels[status],
    type: 'line',
    smooth: true,
    data: data.map(d => d[`status${status}`] || 0),
    itemStyle: { color: statusColors[status] },
    lineStyle: { color: statusColors[status], width: 2 },
    symbol: 'circle',
    symbolSize: 6
  }))

  statusTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      textStyle: { color: '#718096' },
      data: selectedStatuses.value.map(s => statusLabels[s])
    },
    grid: { left: 50, right: 30, top: 50, bottom: 50 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series
  })
}

const renderRoomTypeHeatChart = () => {
  if (!roomTypeHeatChartRef.value) return
  if (!roomTypeHeatChart) {
    roomTypeHeatChart = echarts.init(roomTypeHeatChartRef.value)
  }
  const data = roomTypeHeatStats.value || []
  const names = data.map(d => d.typeName)
  const rates = data.map(d => Number((d.freeRate * 100).toFixed(1)))

  roomTypeHeatChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>空闲率: {c}%' },
    grid: { left: 80, right: 30, top: 30, bottom: 60 },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#718096', rotate: 30 }
    },
    yAxis: {
      type: 'value',
      name: '空闲率(%)',
      axisLabel: { color: '#718096', formatter: '{value}%' },
      splitLine: { lineStyle: { type: 'dashed' } },
      max: 100
    },
    series: [{
      type: 'bar',
      data: rates.map((rate, i) => ({
        value: rate,
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: rate < 20 ? '#F56C6C' : rate < 50 ? '#E6A23C' : '#67C23A'
        }
      })),
      barWidth: '50%',
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}%' }
    }]
  })
}

const renderFreeDurationPieChart = (data) => {
  if (!freeDurationPieChartRef.value) return
  if (!freeDurationPieChart) {
    freeDurationPieChart = echarts.init(freeDurationPieChartRef.value)
  }
  freeDurationPieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 间 ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#718096' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#4a5568' },
      data: data.map(d => ({ name: d.range, value: d.count })),
      color: ['#67C23A', '#85ce61', '#E6A23C', '#F56C6C', '#C0392B']
    }]
  })
}

const handleResize = () => {
  orientationChart?.resize()
  viewTypeChart?.resize()
  statusTrendChart?.resize()
  roomTypeHeatChart?.resize()
  freeDurationPieChart?.resize()
}

const openExportDialog = () => {
  exportDialogVisible.value = true
  loadExportOptions()
}

const handleConfirmExport = async () => {
  try {
    exporting.value = true
    const params = {
      scope: exportForm.value.scope,
      tabName: activeTab.value,
      dateRange: exportForm.value.dateRange,
      floorIds: exportForm.value.floorIds,
      roomTypeIds: exportForm.value.roomTypeIds,
      includeChart: exportForm.value.includeChart
    }
    const res = await api.hotel.exportDashboardStats(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `酒店统计报表_${tabLabels[activeTab.value]}_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    exportDialogVisible.value = false
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

const loadTabData = (tab) => {
  if (tab === 'roomType') {
    loadDetailedTypeStats()
  } else if (tab === 'floor') {
    loadDetailedFloorStats()
  } else if (tab === 'attribute') {
    loadAttributeDistribution()
  } else if (tab === 'statusTrend') {
    loadStatusTrend()
  } else if (tab === 'floorUsage') {
    loadFloorUsageStats()
  } else if (tab === 'roomTypeHeat') {
    loadRoomTypeHeatStats()
  } else if (tab === 'statusDuration') {
    loadStatusDurationStatsEnhanced()
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
  statusTrendChart?.dispose()
  roomTypeHeatChart?.dispose()
  freeDurationPieChart?.dispose()
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

.inner-chart-card {
  border-radius: 8px;
  border: 1px solid #ebeef5;
  box-shadow: none !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
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

.trend-filter {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.duration-stats-row {
  margin-bottom: 16px;
}

.duration-stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: #f7fafc;
  border-radius: 10px;
  transition: all 0.3s;
}

.duration-stat-item:hover {
  background: #edf2f7;
  transform: translateY(-2px);
}

.duration-stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.duration-stat-icon.blue {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.duration-stat-icon.red {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.duration-stat-info {
  flex: 1;
}

.duration-stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #2d3748;
}

.duration-stat-label {
  font-size: 13px;
  color: #718096;
  margin-top: 2px;
}

.heat-table {
  margin-bottom: 20px;
}

.chart-wrapper {
  margin-top: 10px;
}

.export-preview {
  width: 100%;
}

.export-preview :deep(.el-descriptions__label) {
  width: 100px;
  background: #fafafa;
}
</style>
