<template>
  <div class="maintenance-statistics-container">
    <div class="page-header">
      <h2 class="page-title">维护统计报表</h2>
      <el-button
        v-if="hasPermission('maintenance:statistics:export')"
        type="primary"
        @click="handleExport"
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
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
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

const typeChartRef = ref(null)
const costChartRef = ref(null)

let typeChart = null
let costChart = null

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

const handleResize = () => {
  typeChart?.resize()
  costChart?.resize()
}

const handleExport = async () => {
  try {
    exporting.value = true
    const res = await api.hotel.exportMaintenanceStats()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `维护统计报表_${new Date().getTime()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
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
    loadStaffWorkload()
  ])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  typeChart?.dispose()
  costChart?.dispose()
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
</style>
