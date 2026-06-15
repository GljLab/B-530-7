<template>
  <div class="maintenance-statistics-container">
    <div class="page-header">
      <h2 class="page-title">维护统计报表</h2>
      <el-button
        v-if="hasPermission('maintenance:statistics:export')"
        type="primary"
        @click="openExportDialog"
        :loading="exporting"
      >
        <el-icon><Download /></el-icon>导出报表
      </el-button>
    </div>

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <el-icon :size="28"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.monthlyOrders || 0 }}</div>
              <div class="stat-label">本月维护单数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <el-icon :size="28"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.pendingCount || 0 }}</div>
              <div class="stat-label">待处理数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
              <el-icon :size="28"><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ overview.avgDuration || 0 }}</div>
              <div class="stat-label">平均处理时长（小时）</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="12" :sm="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
              <el-icon :size="28"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ formatMoney(overview.monthlyCost) }}</div>
              <div class="stat-label">本月费用（元）</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>维护类型分布</span>
          </template>
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>费用趋势（近6个月）</span>
          </template>
          <div ref="costChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>平均处理时长趋势（近6个月）</span>
          </template>
          <div ref="avgDurationTrendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>验收通过率统计</span>
          </template>
          <div class="inspection-summary">
            <div class="inspection-total">
              <div class="inspection-total-label">总通过率</div>
              <div class="inspection-total-value">
                <el-progress
                  type="dashboard"
                  :percentage="inspectionPassRate.totalPassRate || 0"
                  :color="getPassRateColor(inspectionPassRate.totalPassRate)"
                  :width="120"
                  :stroke-width="14"
                />
              </div>
            </div>
            <div ref="inspectionTrendChartRef" class="inspection-trend-chart"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>维修人员工作量对比</span>
          </template>
          <div ref="staffWorkloadChartRef" class="chart-container-lg"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span>维护费用趋势（近6个月增强版）</span>
          </template>
          <div ref="costTrendEnhancedChartRef" class="chart-container-lg"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <span>维修频率 TOP10</span>
          </template>
          <el-table :data="topRooms" stripe style="width: 100%">
            <el-table-column label="排名" width="80" align="center">
              <template #default="{ $index }">
                <el-tag :type="getRankType($index)" effect="dark" size="small">
                  {{ $index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="roomNo" label="房间号" min-width="120" />
            <el-table-column prop="orderCount" label="维护次数" width="120" align="center" />
            <el-table-column label="累计费用（元）" width="150" align="center">
              <template #default="{ row }">
                {{ formatMoney(row.totalCost) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="duration-card" shadow="hover">
          <template #header>
            <span>维修时长统计</span>
          </template>
          <el-row :gutter="20">
            <el-col :xs="12" :sm="6">
              <div class="duration-item">
                <div class="duration-label">平均时长</div>
                <div class="duration-value blue">{{ durationStats.avgDuration || 0 }} 小时</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="duration-item">
                <div class="duration-label">最长时长</div>
                <div class="duration-value red">
                  {{ durationStats.maxDuration || 0 }} 小时
                  <span class="sub-text" v-if="durationStats.maxOrderNo">单号: {{ durationStats.maxOrderNo }}</span>
                </div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="duration-item">
                <div class="duration-label">超时单数</div>
                <div class="duration-value orange">{{ durationStats.timeoutCount || 0 }} 单</div>
              </div>
            </el-col>
            <el-col :xs="12" :sm="6">
              <div class="duration-item">
                <div class="duration-label">超时率</div>
                <div class="duration-value purple">{{ durationStats.timeoutRate || '0' }}%</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="section-row">
      <el-col :span="24">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <span>维修人员工作量统计</span>
          </template>
          <el-table :data="staffWorkload" stripe style="width: 100%">
            <el-table-column label="用户名/姓名" min-width="150">
              <template #default="{ row }">
                <div>{{ row.username }}</div>
                <div class="sub-text">{{ row.nickName || '-' }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="totalOrders" label="总工单数" width="120" align="center" />
            <el-table-column prop="completedOrders" label="已完成数" width="120" align="center">
              <template #default="{ row }">
                <span style="color: #67C23A">{{ row.completedOrders || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column label="平均工时（小时）" width="140" align="center">
              <template #default="{ row }">
                {{ row.avgWorkHours || 0 }}
              </template>
            </el-table-column>
            <el-table-column label="验收通过率" width="160" align="center">
              <template #default="{ row }">
                <el-progress
                  :percentage="Number(row.passRate || 0)"
                  :color="getPassRateColor(row.passRate)"
                  :stroke-width="10"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="exportDialogVisible"
      title="导出维护报表"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form :model="exportForm" label-width="100px">
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
        <el-form-item label="维修人员">
          <el-select v-model="exportForm.staffIds" multiple placeholder="全部人员" style="width: 100%">
            <el-option
              v-for="s in allStaff"
              :key="s.userId"
              :label="`${s.username}${s.nickName ? ' (' + s.nickName + ')' : ''}`"
              :value="s.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="维护类型">
          <el-select v-model="exportForm.typeIds" multiple placeholder="全部类型" style="width: 100%">
            <el-option
              v-for="t in allTypes"
              :key="t.typeId"
              :label="t.typeName || t.name"
              :value="t.typeId || t.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="包含图表">
          <el-switch v-model="exportForm.includeChart" />
        </el-form-item>
        <el-form-item label="导出预览">
          <el-descriptions :column="1" border size="small" class="export-preview">
            <el-descriptions-item label="日期范围">
              {{ formatPreviewDateRange }}
            </el-descriptions-item>
            <el-descriptions-item label="维修人员">
              {{ exportForm.staffIds.length === 0 ? '全部' : exportForm.staffIds.length + ' 人' }}
            </el-descriptions-item>
            <el-descriptions-item label="维护类型">
              {{ exportForm.typeIds.length === 0 ? '全部' : exportForm.typeIds.length + ' 种' }}
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
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Tickets, Clock, Timer, Money, Download } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import api from '@/api'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const overview = ref({})
const topRooms = ref([])
const typeDistribution = ref([])
const costTrend = ref([])
const durationStats = ref({})
const staffWorkload = ref([])
const exporting = ref(false)

const avgDurationTrend = ref([])
const staffWorkloadCompare = ref([])
const inspectionPassRate = ref({})
const costTrendEnhanced = ref([])

const exportDialogVisible = ref(false)
const allStaff = ref([])
const allTypes = ref([])
const exportForm = ref({
  dateRange: [],
  staffIds: [],
  typeIds: [],
  includeChart: true
})

const typeChartRef = ref(null)
const costChartRef = ref(null)
const avgDurationTrendChartRef = ref(null)
const staffWorkloadChartRef = ref(null)
const inspectionTrendChartRef = ref(null)
const costTrendEnhancedChartRef = ref(null)

let typeChart = null
let costChart = null
let avgDurationTrendChart = null
let staffWorkloadChart = null
let inspectionTrendChart = null
let costTrendEnhancedChart = null

const formatMoney = (value) => {
  if (!value && value !== 0) return '0.00'
  return Number(value).toFixed(2)
}

const getRankType = (index) => {
  if (index === 0) return 'danger'
  if (index === 1) return 'warning'
  if (index === 2) return 'success'
  return 'info'
}

const getPassRateColor = (rate) => {
  const r = Number(rate || 0)
  if (r >= 90) return '#67C23A'
  if (r >= 70) return '#E6A23C'
  return '#F56C6C'
}

const formatPreviewDateRange = computed(() => {
  if (!exportForm.value.dateRange || exportForm.value.dateRange.length === 0) return '全部'
  const [start, end] = exportForm.value.dateRange
  return `${start} 至 ${end}`
})

const loadOverview = async () => {
  try {
    const res = await api.hotel.getStatsOverview()
    if (res.code === 200 && res.data) {
      overview.value = res.data
    }
  } catch {}
}

const loadTopRooms = async () => {
  try {
    const res = await api.hotel.getStatsTopRooms(10)
    if (res.code === 200) {
      topRooms.value = res.data || []
    }
  } catch {}
}

const loadTypeDistribution = async () => {
  try {
    const res = await api.hotel.getStatsTypeDistribution()
    if (res.code === 200) {
      typeDistribution.value = res.data || []
      allTypes.value = res.data || []
      await nextTick()
      renderTypeChart(typeDistribution.value)
    }
  } catch {}
}

const loadCostTrend = async () => {
  try {
    const res = await api.hotel.getStatsCostTrend(6)
    if (res.code === 200) {
      costTrend.value = res.data || []
      await nextTick()
      renderCostChart(costTrend.value)
    }
  } catch {}
}

const loadDurationStats = async () => {
  try {
    const res = await api.hotel.getStatsDuration()
    if (res.code === 200 && res.data) {
      durationStats.value = res.data
    }
  } catch {}
}

const loadStaffWorkload = async () => {
  try {
    const res = await api.hotel.getStatsStaffWorkload()
    if (res.code === 200) {
      staffWorkload.value = res.data || []
      allStaff.value = res.data || []
    }
  } catch {}
}

const loadAvgDurationTrend = async () => {
  try {
    const res = await api.maintenance.getAvgDurationTrend(6)
    if (res.code === 200 && res.data) {
      avgDurationTrend.value = res.data
      await nextTick()
      renderAvgDurationTrendChart()
    }
  } catch {}
}

const loadStaffWorkloadCompare = async () => {
  try {
    const res = await api.maintenance.getStaffWorkloadCompare()
    if (res.code === 200 && res.data) {
      staffWorkloadCompare.value = res.data
      await nextTick()
      renderStaffWorkloadChart()
    }
  } catch {}
}

const loadInspectionPassRate = async () => {
  try {
    const res = await api.maintenance.getInspectionPassRate()
    if (res.code === 200 && res.data) {
      inspectionPassRate.value = res.data
      await nextTick()
      renderInspectionTrendChart(res.data.monthlyTrend || [])
    }
  } catch {}
}

const loadCostTrendEnhanced = async () => {
  try {
    const res = await api.maintenance.getCostTrendEnhanced(6)
    if (res.code === 200 && res.data) {
      costTrendEnhanced.value = res.data
      await nextTick()
      renderCostTrendEnhancedChart()
    }
  } catch {}
}

const renderTypeChart = (data) => {
  if (!typeChartRef.value) return
  if (!typeChart) {
    typeChart = echarts.init(typeChartRef.value)
  }
  typeChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#718096' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', color: '#4a5568' },
      data: data.map(d => ({ name: d.typeName || d.name, value: d.count || d.value })),
      color: ['#667eea', '#43e97b', '#f093fb', '#4facfe', '#f5576c', '#ffa726', '#ab47bc', '#26c6da']
    }]
  })
}

const renderCostChart = (data) => {
  if (!costChartRef.value) return
  if (!costChart) {
    costChart = echarts.init(costChartRef.value)
  }
  costChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>费用: {c} 元' },
    grid: { left: 60, right: 30, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: data.map(d => d.month || d.name),
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '元',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.cost || d.value),
      barWidth: '50%',
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#43e97b' },
          { offset: 1, color: '#38f9d7' }
        ])
      },
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}' }
    }]
  })
}

const renderAvgDurationTrendChart = () => {
  if (!avgDurationTrendChartRef.value) return
  if (!avgDurationTrendChart) {
    avgDurationTrendChart = echarts.init(avgDurationTrendChartRef.value)
  }
  const data = avgDurationTrend.value || []
  avgDurationTrendChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>平均时长: {c} 小时' },
    grid: { left: 60, right: 30, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(d => d.month || d.name),
      axisLabel: { color: '#718096' }
    },
    yAxis: {
      type: 'value',
      name: '小时',
      axisLabel: { color: '#718096' },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'line',
      smooth: true,
      data: data.map(d => d.avgDuration || d.value || 0),
      itemStyle: { color: '#667eea' },
      lineStyle: { color: '#667eea', width: 3 },
      symbol: 'circle',
      symbolSize: 8,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102, 126, 234, 0.3)' },
          { offset: 1, color: 'rgba(102, 126, 234, 0.02)' }
        ])
      },
      label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}h' }
    }]
  })
}

const renderStaffWorkloadChart = () => {
  if (!staffWorkloadChartRef.value) return
  if (!staffWorkloadChart) {
    staffWorkloadChart = echarts.init(staffWorkloadChartRef.value)
  }
  const data = staffWorkloadCompare.value || []
  const names = data.map(d => d.nickName || d.username || d.name)
  const totalOrders = data.map(d => d.totalOrders || d.total || 0)
  const completedOrders = data.map(d => d.completedOrders || d.completed || 0)
  const totalWorkHours = data.map(d => d.totalWorkHours || d.workHours || 0)

  staffWorkloadChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 0, textStyle: { color: '#718096' }, data: ['总工单数', '已完成数', '总工时(小时)'] },
    grid: { left: 60, right: 60, top: 50, bottom: 60 },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#718096', rotate: 20 }
    },
    yAxis: [
      {
        type: 'value',
        name: '工单数',
        axisLabel: { color: '#718096' },
        splitLine: { lineStyle: { type: 'dashed' } }
      },
      {
        type: 'value',
        name: '工时(小时)',
        axisLabel: { color: '#718096' },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '总工单数',
        type: 'bar',
        data: totalOrders,
        barWidth: '25%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ])
        }
      },
      {
        name: '已完成数',
        type: 'bar',
        data: completedOrders,
        barWidth: '25%',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#43e97b' },
            { offset: 1, color: '#38f9d7' }
          ])
        }
      },
      {
        name: '总工时(小时)',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        data: totalWorkHours,
        itemStyle: { color: '#f5576c' },
        lineStyle: { color: '#f5576c', width: 2 },
        symbol: 'circle',
        symbolSize: 6
      }
    ]
  })
}

const renderInspectionTrendChart = (data) => {
  if (!inspectionTrendChartRef.value) return
  if (!inspectionTrendChart) {
    inspectionTrendChart = echarts.init(inspectionTrendChartRef.value)
  }
  inspectionTrendChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>通过率: {c}%' },
    grid: { left: 45, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.map(d => d.month || d.name),
      axisLabel: { color: '#718096', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { color: '#718096', formatter: '{value}%', fontSize: 11 },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      type: 'line',
      smooth: true,
      data: data.map(d => d.passRate || d.value || 0),
      itemStyle: { color: '#67C23A' },
      lineStyle: { color: '#67C23A', width: 2 },
      symbol: 'circle',
      symbolSize: 5,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(103, 194, 58, 0.25)' },
          { offset: 1, color: 'rgba(103, 194, 58, 0.02)' }
        ])
      }
    }]
  })
}

const renderCostTrendEnhancedChart = () => {
  if (!costTrendEnhancedChartRef.value) return
  if (!costTrendEnhancedChart) {
    costTrendEnhancedChart = echarts.init(costTrendEnhancedChartRef.value)
  }
  const data = costTrendEnhanced.value || []
  const months = data.map(d => d.month || d.name)
  const materialCost = data.map(d => d.materialCost || 0)
  const laborCost = data.map(d => d.laborCost || 0)
  const otherCost = data.map(d => d.otherCost || 0)
  const totalCost = data.map(d => (d.materialCost || 0) + (d.laborCost || 0) + (d.otherCost || 0))

  costTrendEnhancedChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        let result = params[0].axisValue + '<br/>'
        let total = 0
        params.forEach(p => {
          if (p.seriesName !== '总计') {
            result += `${p.marker} ${p.seriesName}: ${p.value} 元<br/>`
            total += p.value
          }
        })
        result += `<strong>总计: ${total} 元</strong>`
        return result
      }
    },
    legend: { top: 0, textStyle: { color: '#718096' }, data: ['材料费', '人工费', '其他费用', '总计'] },
    grid: { left: 60, right: 60, top: 50, bottom: 40 },
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: { color: '#718096' }
    },
    yAxis: [
      {
        type: 'value',
        name: '费用(元)',
        axisLabel: { color: '#718096' },
        splitLine: { lineStyle: { type: 'dashed' } }
      }
    ],
    series: [
      {
        name: '材料费',
        type: 'bar',
        stack: 'cost',
        data: materialCost,
        barWidth: '45%',
        itemStyle: { color: '#667eea' }
      },
      {
        name: '人工费',
        type: 'bar',
        stack: 'cost',
        data: laborCost,
        itemStyle: { color: '#f093fb' }
      },
      {
        name: '其他费用',
        type: 'bar',
        stack: 'cost',
        data: otherCost,
        itemStyle: { color: '#ffa726', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '总计',
        type: 'line',
        smooth: true,
        data: totalCost,
        itemStyle: { color: '#F56C6C' },
        lineStyle: { color: '#F56C6C', width: 3 },
        symbol: 'circle',
        symbolSize: 8,
        label: { show: true, position: 'top', color: '#4a5568', formatter: '{c}' }
      }
    ]
  })
}

const handleResize = () => {
  typeChart?.resize()
  costChart?.resize()
  avgDurationTrendChart?.resize()
  staffWorkloadChart?.resize()
  inspectionTrendChart?.resize()
  costTrendEnhancedChart?.resize()
}

const openExportDialog = () => {
  exportDialogVisible.value = true
}

const handleConfirmExport = async () => {
  try {
    exporting.value = true
    const params = {
      dateRange: exportForm.value.dateRange,
      staffIds: exportForm.value.staffIds,
      typeIds: exportForm.value.typeIds,
      includeChart: exportForm.value.includeChart
    }
    const res = await api.hotel.exportMaintenanceStats(params)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `维护统计报表_${new Date().getTime()}.xlsx`)
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

onMounted(() => {
  Promise.all([
    loadOverview(),
    loadTopRooms(),
    loadTypeDistribution(),
    loadCostTrend(),
    loadDurationStats(),
    loadStaffWorkload(),
    loadAvgDurationTrend(),
    loadStaffWorkloadCompare(),
    loadInspectionPassRate(),
    loadCostTrendEnhanced()
  ])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  typeChart?.dispose()
  costChart?.dispose()
  avgDurationTrendChart?.dispose()
  staffWorkloadChart?.dispose()
  inspectionTrendChart?.dispose()
  costTrendEnhancedChart?.dispose()
})
</script>

<style scoped>
.maintenance-statistics-container {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
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

.chart-card,
.table-card,
.duration-card {
  border-radius: 12px;
  border: none;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.chart-container-lg {
  width: 100%;
  height: 400px;
}

.duration-card {
  padding: 10px 0;
}

.duration-item {
  text-align: center;
  padding: 15px 10px;
  background: #f7fafc;
  border-radius: 10px;
  transition: all 0.3s;
}

.duration-item:hover {
  background: #edf2f7;
  transform: translateY(-2px);
}

.duration-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 8px;
}

.duration-value {
  font-size: 24px;
  font-weight: 700;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.duration-value.blue {
  color: #409EFF;
}

.duration-value.red {
  color: #F56C6C;
}

.duration-value.orange {
  color: #E6A23C;
}

.duration-value.purple {
  color: #9b59b6;
}

.sub-text {
  font-size: 12px;
  font-weight: 400;
  color: #718096;
}

.inspection-summary {
  display: flex;
  align-items: center;
  gap: 20px;
  height: 350px;
}

.inspection-total {
  width: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px;
  background: #f7fafc;
  border-radius: 10px;
}

.inspection-total-label {
  font-size: 14px;
  color: #718096;
  margin-bottom: 8px;
}

.inspection-total-value {
  display: flex;
  align-items: center;
  justify-content: center;
}

.inspection-trend-chart {
  flex: 1;
  height: 100%;
}

.export-preview {
  width: 100%;
}

.export-preview :deep(.el-descriptions__label) {
  width: 100px;
  background: #fafafa;
}
</style>
