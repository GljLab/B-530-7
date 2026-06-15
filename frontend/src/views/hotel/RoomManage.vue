<template>
  <div class="room-manage-container">
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-total">
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalRooms }}</div>
            <div class="stat-label">总房间数</div>
          </div>
          <el-icon :size="40" class="stat-icon"><House /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-free">
          <div class="stat-content">
            <div class="stat-value">{{ stats.freeRooms }}</div>
            <div class="stat-label">空闲数</div>
          </div>
          <el-icon :size="40" class="stat-icon"><CircleCheck /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-occupied">
          <div class="stat-content">
            <div class="stat-value">{{ stats.occupiedRooms }}</div>
            <div class="stat-label">已入住数</div>
          </div>
          <el-icon :size="40" class="stat-icon"><User /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-maintenance">
          <div class="stat-content">
            <div class="stat-value">{{ stats.maintenanceRooms }}</div>
            <div class="stat-label">维修中</div>
          </div>
          <el-icon :size="40" class="stat-icon"><WarnTriangleFilled /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="control-card">
      <div class="control-row">
        <el-radio-group v-model="viewMode" size="default">
          <el-radio-button value="table">表格视图</el-radio-button>
          <el-radio-button value="floorplan">楼层平面</el-radio-button>
        </el-radio-group>
        <div class="quick-tabs">
          <el-tag
            v-for="tab in quickTabs"
            :key="tab.value"
            :type="activeTab === tab.value ? '' : 'info'"
            :effect="activeTab === tab.value ? 'dark' : 'plain'"
            class="quick-tab"
            @click="handleQuickTab(tab.value)"
          >{{ tab.label }}</el-tag>
        </div>
        <el-select
          v-model="selectedSchemeId"
          placeholder="已存方案"
          clearable
          style="width: 160px"
          @change="handleLoadScheme"
        >
          <el-option v-for="s in savedSchemes" :key="s.id" :label="s.name" :value="s.id" />
        </el-select>
      </div>
    </el-card>

    <el-card shadow="never" class="filter-card">
      <div class="filter-row">
        <el-input v-model="queryParams.roomNumber" placeholder="房间号" clearable style="width: 130px" @keyup.enter="handleSearch" />
        <el-select v-model="queryParams.buildingId" placeholder="楼栋" clearable style="width: 140px" @change="handleBuildingChange">
          <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
        </el-select>
        <el-select v-model="queryParams.floorId" placeholder="楼层" clearable style="width: 120px">
          <el-option v-for="f in filteredFloors" :key="f.id" :label="f.floorName" :value="f.id" />
        </el-select>
        <el-select v-model="queryParams.roomTypeId" placeholder="房型" clearable style="width: 140px">
          <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
        </el-select>
        <el-select v-model="queryParams.statusList" placeholder="状态" multiple clearable collapse-tags style="width: 200px">
          <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-select v-model="queryParams.orientations" placeholder="朝向" multiple clearable collapse-tags style="width: 180px">
          <el-option v-for="o in orientationOptions" :key="o" :label="o" :value="o" />
        </el-select>
        <el-select v-model="queryParams.viewTypes" placeholder="景观" multiple clearable collapse-tags style="width: 180px">
          <el-option v-for="v in viewTypeOptions" :key="v" :label="v" :value="v" />
        </el-select>
        <el-select v-model="queryParams.specialTags" placeholder="特殊标签" multiple clearable collapse-tags style="width: 200px">
          <el-option v-for="t in specialTagOptions" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="openSaveSchemeDialog">
          <el-icon><FolderAdd /></el-icon>存方案
        </el-button>
      </div>
    </el-card>

    <el-card shadow="never" class="table-card">
      <div class="table-toolbar">
        <div class="table-toolbar-left">
          <el-button v-if="hasPermission('hotel:room:add')" type="primary" @click="openRoomDialog(null)">
            <el-icon><Plus /></el-icon>新增房间
          </el-button>
          <el-button v-if="hasPermission('hotel:room:batch:add')" @click="openBatchCreateDialog">
            <el-icon><CopyDocument /></el-icon>批量创建
          </el-button>
          <el-button
            v-if="hasPermission('hotel:room:template')"
            :disabled="selectedRows.length === 0"
            @click="openTemplateDialog"
          >
            <el-icon><Stamp /></el-icon>应用模板
          </el-button>
          <el-button v-if="hasPermission('hotel:room:export')" @click="openExportDialog">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
        <div v-if="selectedRows.length > 0" class="batch-toolbar">
          <el-tag type="info" class="batch-count-tag">
            已选择 <strong>{{ selectedRows.length }}</strong> 个房间
          </el-tag>
          <el-button 
            v-if="hasPermission('hotel:room:batch:status')" 
            type="primary" 
            size="small" 
            @click="openBatchDialog('status')"
          >
            <el-icon><Refresh /></el-icon>批量修改状态
          </el-button>
          <el-button 
            v-if="hasPermission('hotel:room:batch:attr')" 
            type="warning" 
            size="small" 
            @click="openBatchDialog('attr')"
          >
            <el-icon><Edit /></el-icon>批量修改属性
          </el-button>
          <el-button 
            v-if="hasPermission('hotel:room:batch:delete')" 
            type="danger" 
            size="small" 
            @click="openBatchDialog('delete')"
          >
            <el-icon><Delete /></el-icon>批量删除
          </el-button>
          <el-button size="small" @click="clearSelection">
            取消选择
          </el-button>
        </div>
      </div>

      <template v-if="viewMode === 'table'">
        <el-table
          :data="tableData"
          stripe
          border
          v-loading="tableLoading"
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column prop="roomNumber" label="房号" width="110" align="center" />
          <el-table-column prop="buildingName" label="楼栋" width="120" align="center" />
          <el-table-column prop="floorName" label="楼层" width="100" align="center" />
          <el-table-column prop="roomTypeName" label="房型" min-width="140" />
          <el-table-column prop="orientation" label="朝向" width="90" align="center">
            <template #default="{ row }">{{ row.orientation || '-' }}</template>
          </el-table-column>
          <el-table-column prop="viewType" label="景观" width="90" align="center">
            <template #default="{ row }">{{ row.viewType || '-' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="280" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleViewDetail(row)">查看详情</el-button>
              <el-button v-if="hasPermission('hotel:room:edit')" type="primary" link size="small" @click="openRoomDialog(row)">编辑</el-button>
              <el-button v-if="hasPermission('hotel:room:status:edit')" type="warning" link size="small" @click="openStatusDialog(row)">修改状态</el-button>
              <el-button v-if="hasPermission('hotel:room:copy')" type="success" link size="small" @click="openCopyDialog(row)">复制</el-button>
              <el-button v-if="hasPermission('hotel:room:delete')" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadRooms"
            @current-change="loadRooms"
          />
        </div>
      </template>

      <template v-if="viewMode === 'floorplan'">
        <div class="floorplan-controls">
          <el-select v-model="floorplanBuildingId" placeholder="选择楼栋" style="width: 180px" @change="handleFloorplanBuildingChange">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
          <el-select v-model="floorplanFloorId" placeholder="选择楼层" style="width: 160px" @change="loadFloorplanRooms">
            <el-option v-for="f in floorplanFloors" :key="f.id" :label="f.floorName" :value="f.id" />
          </el-select>
        </div>
        <div v-if="floorplanRooms.length > 0" class="floorplan-grid">
          <div
            v-for="room in floorplanRooms"
            :key="room.id"
            class="floorplan-room status-bg"
            :class="'status-' + room.status"
            @mouseenter="showTooltip($event, room)"
            @mouseleave="hideTooltip"
            @click="openFloorplanAction(room)"
          >
            <div class="room-number">{{ room.roomNumber }}</div>
            <div class="room-status-text">{{ statusLabel(room.status) }}</div>
          </div>
        </div>
        <el-empty v-else description="请选择楼栋和楼层查看房间分布" />
        <div class="floorplan-legend">
          <div v-for="s in statusOptions" :key="s.value" class="legend-item">
            <span class="legend-color" :class="'status-' + s.value"></span>
            <span class="legend-label">{{ s.label }}</span>
          </div>
        </div>
      </template>
    </el-card>

    <div
      v-if="tooltipVisible"
      class="room-tooltip"
      :style="{ top: tooltipY + 'px', left: tooltipX + 'px' }"
    >
      <div class="tooltip-title">{{ tooltipRoom.roomNumber }}</div>
      <div class="tooltip-row"><span>状态：</span>{{ statusLabel(tooltipRoom.status) }}</div>
      <div class="tooltip-row"><span>房型：</span>{{ tooltipRoom.roomTypeName || '-' }}</div>
      <div class="tooltip-row"><span>朝向：</span>{{ tooltipRoom.orientation || '-' }}</div>
      <div class="tooltip-row"><span>景观：</span>{{ tooltipRoom.viewType || '-' }}</div>
    </div>

    <div
      v-if="floorplanContextMenu"
      class="floorplan-context-menu"
      :style="{ top: floorplanMenuPos.y + 'px', left: floorplanMenuPos.x + 'px' }"
    >
      <div class="context-menu-item" @click="handleFloorplanAction('view')">查看详情</div>
      <div v-if="hasPermission('hotel:room:status:edit')" class="context-menu-item" @click="handleFloorplanAction('status')">修改状态</div>
      <div v-if="hasPermission('hotel:room:edit')" class="context-menu-item" @click="handleFloorplanAction('edit')">编辑</div>
      <div v-if="hasPermission('hotel:room:copy')" class="context-menu-item" @click="handleFloorplanAction('copy')">复制</div>
    </div>

    <el-dialog v-model="copyDialogVisible" title="复制房间" width="800px" destroy-on-close>
      <el-row :gutter="24">
        <el-col :span="12">
          <h4 class="dialog-section-title">源房间信息</h4>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="房间号">{{ copySourceRoom.roomNumber }}</el-descriptions-item>
            <el-descriptions-item label="楼栋">{{ copySourceRoom.buildingName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="楼层">{{ copySourceRoom.floorName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="房型">{{ copySourceRoom.roomTypeName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="朝向">{{ copySourceRoom.orientation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="景观">{{ copySourceRoom.viewType || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
        <el-col :span="12">
          <h4 class="dialog-section-title">新房间信息</h4>
          <el-form ref="copyFormRef" :model="copyForm" :rules="copyRules" label-width="100px" size="small">
            <el-form-item label="房间号" prop="roomNumber">
              <el-input v-model="copyForm.roomNumber" placeholder="请输入新房间号" />
            </el-form-item>
            <el-form-item label="楼栋" prop="buildingId">
              <el-select v-model="copyForm.buildingId" placeholder="请选择楼栋" style="width: 100%" @change="handleCopyBuildingChange">
                <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="楼层" prop="floorId">
              <el-select v-model="copyForm.floorId" placeholder="请选择楼层" style="width: 100%">
                <el-option v-for="f in copyFloors" :key="f.id" :label="f.floorName" :value="f.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="朝向">
              <el-select v-model="copyForm.orientation" placeholder="请选择朝向" clearable style="width: 100%">
                <el-option v-for="o in orientationOptions" :key="o" :label="o" :value="o" />
              </el-select>
            </el-form-item>
            <el-form-item label="景观">
              <el-select v-model="copyForm.viewType" placeholder="请选择景观" clearable style="width: 100%">
                <el-option v-for="v in viewTypeOptions" :key="v" :label="v" :value="v" />
              </el-select>
            </el-form-item>
            <el-form-item label="位置特征">
              <el-select v-model="copyForm.locationFeatures" multiple placeholder="请选择位置特征" style="width: 100%">
                <el-option v-for="f in locationFeatureOptions" :key="f" :label="f" :value="f" />
              </el-select>
            </el-form-item>
            <el-form-item label="特殊标签">
              <el-select v-model="copyForm.specialTags" multiple placeholder="请选择特殊标签" style="width: 100%">
                <el-option v-for="t in specialTagOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      <template #footer>
        <el-button @click="copyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="copySaving" @click="handleCopyRoom">确认复制</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="templateDialogVisible" title="应用模板" width="700px" destroy-on-close>
      <el-steps :active="templateStep" finish-status="success" class="batch-steps">
        <el-step title="选择模板房间" />
        <el-step title="选择应用属性" />
        <el-step title="确认应用" />
      </el-steps>

      <div v-if="templateStep === 0" class="batch-section">
        <el-input v-model="templateSearchKey" placeholder="搜索房间号" clearable style="margin-bottom: 12px" />
        <el-table
          :data="filteredTemplateRooms"
          border
          size="small"
          highlight-current-row
          @current-change="handleTemplateRoomSelect"
          style="max-height: 300px; overflow-y: auto"
        >
          <el-table-column prop="roomNumber" label="房号" width="120" />
          <el-table-column prop="buildingName" label="楼栋" width="120" />
          <el-table-column prop="roomTypeName" label="房型" />
          <el-table-column prop="orientation" label="朝向" width="80" />
        </el-table>
      </div>

      <div v-if="templateStep === 1" class="batch-section">
        <el-form label-width="120px">
          <el-form-item label="朝向">
            <el-checkbox v-model="templateForm.applyOrientation" />
          </el-form-item>
          <el-form-item label="景观">
            <el-checkbox v-model="templateForm.applyViewType" />
          </el-form-item>
          <el-form-item label="位置特征">
            <el-checkbox v-model="templateForm.applyLocationFeatures" />
          </el-form-item>
          <el-form-item label="特殊标签">
            <el-checkbox v-model="templateForm.applySpecialTags" />
          </el-form-item>
        </el-form>
      </div>

      <div v-if="templateStep === 2" class="batch-section">
        <el-alert
          :title="`将对 ${selectedRows.length} 间房间应用模板属性`"
          type="info"
          :closable="false"
          style="margin-bottom: 16px"
        />
        <el-form label-width="100px">
          <el-form-item label="原因">
            <el-input v-model="templateForm.reason" type="textarea" :rows="3" placeholder="请输入应用原因" />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="templateDialogVisible = false">关闭</el-button>
        <el-button v-if="templateStep > 0" @click="templateStep--">上一步</el-button>
        <el-button v-if="templateStep < 2" type="primary" :disabled="!templateSelectedRoom" @click="templateStep++">下一步</el-button>
        <el-button v-if="templateStep === 2" type="primary" :loading="templateSaving" @click="handleApplyTemplate">确认应用</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchCreateDialogVisible" title="批量创建房间" width="700px" destroy-on-close>
      <el-steps :active="batchCreateStep" finish-status="success" class="batch-steps">
        <el-step title="选择位置与房型" />
        <el-step title="设置房号规则" />
        <el-step title="默认属性" />
      </el-steps>

      <div v-if="batchCreateStep === 0" class="batch-section">
        <el-form label-width="100px">
          <el-form-item label="楼栋" required>
            <el-select v-model="batchCreateForm.buildingId" placeholder="请选择楼栋" style="width: 100%" @change="handleBatchCreateBuildingChange">
              <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="楼层" required>
            <el-select v-model="batchCreateForm.floorId" placeholder="请选择楼层" style="width: 100%">
              <el-option v-for="f in batchCreateFloors" :key="f.id" :label="f.floorName" :value="f.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="房型" required>
            <el-select v-model="batchCreateForm.roomTypeId" placeholder="请选择房型" style="width: 100%">
              <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="batchCreateStep === 1" class="batch-section">
        <el-form label-width="100px">
          <el-form-item label="号码前缀">
            <el-input v-model="batchCreateForm.numberPrefix" placeholder="例如: 2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="起始号" required>
            <el-input v-model="batchCreateForm.startNum" placeholder="例如: 01" style="width: 100%" />
          </el-form-item>
          <el-form-item label="结束号" required>
            <el-input v-model="batchCreateForm.endNum" placeholder="例如: 30" style="width: 100%" />
          </el-form-item>
        </el-form>
        <div v-if="batchCreatePreviewNumbers.length > 0" class="batch-preview">
          <div class="batch-preview-title">预览房号 ({{ batchCreatePreviewNumbers.length }}间)</div>
          <div class="batch-preview-list">
            <el-tag v-for="num in batchCreatePreviewNumbers" :key="num" size="small" class="preview-tag">{{ num }}</el-tag>
          </div>
        </div>
      </div>

      <div v-if="batchCreateStep === 2" class="batch-section">
        <el-form label-width="100px">
          <el-form-item label="朝向">
            <el-select v-model="batchCreateForm.orientation" placeholder="请选择朝向" clearable style="width: 100%">
              <el-option v-for="o in orientationOptions" :key="o" :label="o" :value="o" />
            </el-select>
          </el-form-item>
          <el-form-item label="景观">
            <el-select v-model="batchCreateForm.viewType" placeholder="请选择景观" clearable style="width: 100%">
              <el-option v-for="v in viewTypeOptions" :key="v" :label="v" :value="v" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div v-if="batchCreateResult" class="batch-result">
        <el-alert :title="`成功创建 ${batchCreateResult.successCount} 间房间`" type="success" show-icon :closable="false" />
        <div v-if="batchCreateResult.failures && batchCreateResult.failures.length > 0" class="batch-failures">
          <div class="batch-failures-title">失败列表：</div>
          <el-table :data="batchCreateResult.failures" size="small" border>
            <el-table-column prop="roomNumber" label="房号" width="140" />
            <el-table-column prop="reason" label="原因" />
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="batchCreateDialogVisible = false">关闭</el-button>
        <el-button v-if="batchCreateStep > 0 && !batchCreateResult" @click="batchCreateStep--">上一步</el-button>
        <el-button v-if="batchCreateStep < 2" type="primary" @click="batchCreateStep++">下一步</el-button>
        <el-button v-if="batchCreateStep === 2 && !batchCreateResult" type="primary" :loading="batchCreateSaving" @click="handleBatchCreate">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusDialogVisible" title="修改房间状态" width="480px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="当前状态">
          <el-tag :type="statusTagType(statusForm.currentStatus)">{{ statusLabel(statusForm.currentStatus) }}</el-tag>
        </el-form-item>
        <el-form-item label="新状态" required>
          <el-select v-model="statusForm.newStatus" placeholder="请选择新状态" style="width: 100%">
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="statusForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusSaving" @click="handleUpdateStatus">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="exportDialogVisible" title="导出房间" width="560px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportForm.scope">
            <el-radio value="current">当前页 ({{ tableData.length }}条)</el-radio>
            <el-radio value="all">全部 ({{ total }}条)</el-radio>
            <el-radio value="custom">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="exportForm.scope === 'custom'" label="自定义范围">
          <el-input v-model="exportForm.customIds" placeholder="输入房间ID，逗号分隔" />
        </el-form-item>
        <el-form-item label="导出字段">
          <el-checkbox-group v-model="exportForm.exportFields">
            <el-checkbox v-for="f in exportFieldOptions" :key="f.value" :value="f.value">{{ f.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="导出数量">
          <span>{{ exportCount }} 条</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="exportSaving" @click="handleExport">确认导出</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="saveSchemeDialogVisible" title="保存筛选方案" width="420px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="方案名称">
          <el-input v-model="saveSchemeName" placeholder="请输入方案名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveSchemeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveSchemeSaving" @click="handleSaveScheme">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="roomDialogVisible"
      :title="roomForm.id ? '编辑房间' : '新增房间'"
      width="640px"
      destroy-on-close
    >
      <el-form ref="roomFormRef" :model="roomForm" :rules="roomRules" label-width="100px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="roomForm.roomNumber" placeholder="请输入房间号" :disabled="!!roomForm.id" />
        </el-form-item>
        <el-form-item label="楼栋" prop="buildingId">
          <el-select v-model="roomForm.buildingId" placeholder="请选择楼栋" style="width: 100%" :disabled="!!roomForm.id" @change="handleFormBuildingChange">
            <el-option v-for="b in buildings" :key="b.id" :label="b.buildingName" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层" prop="floorId">
          <el-select v-model="roomForm.floorId" placeholder="请选择楼层" style="width: 100%" :disabled="!!roomForm.id">
            <el-option v-for="f in formFloors" :key="f.id" :label="f.floorName" :value="f.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房型" prop="roomTypeId">
          <el-select v-model="roomForm.roomTypeId" placeholder="请选择房型" style="width: 100%" :disabled="!!roomForm.id" @change="handleFormRoomTypeChange">
            <el-option v-for="rt in roomTypes" :key="rt.id" :label="rt.typeName" :value="rt.id" />
          </el-select>
        </el-form-item>
        <div v-if="selectedRoomTypeInfo" class="room-type-info-box">
          <el-descriptions :column="3" size="small" border>
            <el-descriptions-item label="面积">{{ selectedRoomTypeInfo.area }}m²</el-descriptions-item>
            <el-descriptions-item label="床型">{{ bedTypeLabel(selectedRoomTypeInfo.bedType) }}</el-descriptions-item>
            <el-descriptions-item label="最大入住">{{ selectedRoomTypeInfo.maxOccupancy }}人</el-descriptions-item>
          </el-descriptions>
        </div>

        <el-divider content-position="left">房间属性</el-divider>
        <el-form-item label="朝向" prop="orientation">
          <el-select v-model="roomForm.orientation" placeholder="请选择朝向" clearable style="width: 100%">
            <el-option v-for="o in orientationOptions" :key="o" :label="o" :value="o" />
          </el-select>
        </el-form-item>
        <el-form-item label="景观" prop="viewType">
          <el-select v-model="roomForm.viewType" placeholder="请选择景观" clearable style="width: 100%">
            <el-option v-for="v in viewTypeOptions" :key="v" :label="v" :value="v" />
          </el-select>
        </el-form-item>
        <el-form-item label="位置特征" prop="locationFeatures">
          <el-select v-model="roomForm.locationFeatures" multiple placeholder="请选择位置特征" style="width: 100%">
            <el-option v-for="f in locationFeatureOptions" :key="f" :label="f" :value="f" />
          </el-select>
        </el-form-item>
        <el-form-item label="特殊标签" prop="specialTags">
          <el-select v-model="roomForm.specialTags" multiple placeholder="请选择特殊标签" style="width: 100%">
            <el-option v-for="t in specialTagOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>

        <template v-if="hasPermission('hotel:room:remark:view') || hasPermission('hotel:room:remark:edit')">
          <el-divider content-position="left">备注</el-divider>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="roomForm.remark" type="textarea" :rows="3" placeholder="请输入备注" :disabled="!hasPermission('hotel:room:remark:edit')" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="roomDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roomSaving" @click="handleSaveRoom">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer
      v-model="detailVisible"
      :title="detailData ? `房间详情 - ${detailData.roomNumber}` : '房间详情'"
      size="520px"
      destroy-on-close
    >
      <div v-if="detailData" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="房间号">{{ detailData.roomNumber }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detailData.status)" size="small">{{ statusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="楼栋">{{ detailData.buildingName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="楼层">{{ detailData.floorName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ detailData.roomTypeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="朝向">{{ detailData.orientation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="景观">{{ detailData.viewType || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="parsedLocationFeatures.length > 0" class="detail-section">
          <h4 class="section-title">位置特征</h4>
          <div class="tag-list">
            <el-tag v-for="f in parsedLocationFeatures" :key="f" size="small" type="info" class="info-tag">{{ f }}</el-tag>
          </div>
        </div>

        <div v-if="parsedSpecialTags.length > 0" class="detail-section">
          <h4 class="section-title">特殊标签</h4>
          <div class="tag-list">
            <el-tag v-for="t in parsedSpecialTags" :key="t" size="small" type="warning" class="info-tag">{{ t }}</el-tag>
          </div>
        </div>

        <div v-if="detailData.remark && (hasPermission('hotel:room:remark:view') || hasPermission('hotel:room:remark:edit'))" class="detail-section">
          <h4 class="section-title">备注</h4>
          <p class="detail-remark">{{ detailData.remark }}</p>
        </div>
      </div>
    </el-drawer>

    <BatchOperationDialog
      v-model="batchDialogVisible"
      :title="batchDialogTitle"
      :operation-type="batchOperationType"
      :selected-rooms="selectedRows"
      @confirm="handleBatchConfirm"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Search, House, User, CircleCheck, WarnTriangleFilled,
  CopyDocument, Download, Stamp, FolderAdd, Refresh, Edit, Delete
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'
import BatchOperationDialog from '@/components/BatchOperationDialog.vue'

const userStore = useUserStore()
const hasPermission = (p) => userStore.hasPermission(p)

const statusOptions = [
  { value: 1, label: '空闲' },
  { value: 2, label: '已预订' },
  { value: 3, label: '已入住' },
  { value: 4, label: '待清洁' },
  { value: 5, label: '清洁中' },
  { value: 6, label: '维修中' },
  { value: 7, label: '停用' }
]

const quickTabs = [
  { value: '', label: '全部' },
  { value: '1', label: '空闲' },
  { value: '3', label: '入住' },
  { value: '4', label: '待清洁' },
  { value: '6', label: '维修中' }
]

const orientationOptions = ['东', '南', '西', '北', '东南', '东北', '西南', '西北']
const viewTypeOptions = ['江景', '海景', '山景', '园景', '城景']
const locationFeatureOptions = ['靠近电梯', '转角房', '安静区域', '靠近楼梯', '高楼层', '低楼层']
const specialTagOptions = ['残疾人房', '连通房', '禁烟房', '允许宠物', '行政房', 'VIP房']

const exportFieldOptions = [
  { value: 'roomNumber', label: '房间号' },
  { value: 'building', label: '楼栋' },
  { value: 'floor', label: '楼层' },
  { value: 'roomType', label: '房型' },
  { value: 'orientation', label: '朝向' },
  { value: 'viewType', label: '景观' },
  { value: 'locationFeatures', label: '位置特征' },
  { value: 'specialTags', label: '特殊标签' },
  { value: 'status', label: '状态' },
  { value: 'basePrice', label: '基础价格' }
]

const stats = reactive({ totalRooms: 0, freeRooms: 0, occupiedRooms: 0, maintenanceRooms: 0 })
const buildings = ref([])
const allFloors = ref([])
const roomTypes = ref([])
const tableData = ref([])
const total = ref(0)
const tableLoading = ref(false)
const selectedRows = ref([])

const viewMode = ref('table')
const activeTab = ref('')

const savedSchemes = ref([])
const selectedSchemeId = ref(null)

const queryParams = reactive({
  roomNumber: '',
  buildingId: null,
  floorId: null,
  roomTypeId: null,
  statusList: [],
  orientations: [],
  viewTypes: [],
  specialTags: [],
  pageNum: 1,
  pageSize: 10
})

const filteredFloors = computed(() => {
  if (!queryParams.buildingId) return allFloors.value
  return allFloors.value.filter(f => f.buildingId === queryParams.buildingId)
})

const statusTagType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: '', 4: 'info', 5: 'info', 6: 'danger', 7: 'info' }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = { 1: '空闲', 2: '已预订', 3: '已入住', 4: '待清洁', 5: '清洁中', 6: '维修中', 7: '停用' }
  return map[status] || '未知'
}

const bedTypeLabel = (type) => {
  const map = { single: '单人床', king: '大床', twin: '双床' }
  return map[type] || type || '-'
}

const parseJsonArray = (val) => {
  if (!val) return []
  if (Array.isArray(val)) return val
  try {
    const parsed = JSON.parse(val)
    return Array.isArray(parsed) ? parsed : []
  } catch { return [] }
}

const detailVisible = ref(false)
const detailData = ref(null)
const parsedLocationFeatures = computed(() => parseJsonArray(detailData.value?.locationFeatures))
const parsedSpecialTags = computed(() => parseJsonArray(detailData.value?.specialTags))

const loadStats = async () => {
  try {
    const res = await api.hotel.getDashboardOverview()
    if (res.code === 200 && res.data) {
      stats.totalRooms = res.data.totalRooms || 0
      stats.freeRooms = res.data.freeRooms || 0
      stats.occupiedRooms = res.data.occupiedRooms || 0
      stats.maintenanceRooms = res.data.maintenanceRooms || 0
    }
  } catch {}
}

const loadBuildings = async () => {
  try {
    const res = await api.hotel.getBuildings()
    if (res.code === 200) {
      buildings.value = res.data || []
      const floors = []
      for (const b of buildings.value) {
        if (b.floors && b.floors.length) {
          for (const f of b.floors) { floors.push({ ...f, buildingId: b.id }) }
        }
      }
      allFloors.value = floors
    }
  } catch { buildings.value = []; allFloors.value = [] }
}

const loadRoomTypes = async () => {
  try {
    const res = await api.hotel.getRoomTypes()
    if (res.code === 200) roomTypes.value = res.data || []
  } catch { roomTypes.value = [] }
}

const loadSavedSchemes = async () => {
  try {
    const res = await api.hotel.getSavedFilters()
    if (res.code === 200) savedSchemes.value = res.data || []
  } catch { savedSchemes.value = [] }
}

const loadRooms = async () => {
  tableLoading.value = true
  try {
    const params = { ...queryParams }
    if (params.statusList && params.statusList.length > 0) {
      params.status = params.statusList.join(',')
    }
    delete params.statusList
    if (params.orientations && params.orientations.length > 0) {
      params.orientations = params.orientations.join(',')
    } else {
      delete params.orientations
    }
    if (params.viewTypes && params.viewTypes.length > 0) {
      params.viewTypes = params.viewTypes.join(',')
    } else {
      delete params.viewTypes
    }
    if (params.specialTags && params.specialTags.length > 0) {
      params.specialTags = params.specialTags.join(',')
    } else {
      delete params.specialTags
    }
    const res = await api.hotel.getRoomPage(params)
    if (res.code === 200) {
      tableData.value = res.data?.records || res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch { tableData.value = []; total.value = 0 } finally { tableLoading.value = false }
}

const handleBuildingChange = () => { queryParams.floorId = null }

const handleQuickTab = (val) => {
  activeTab.value = val
  if (val === '') {
    queryParams.statusList = []
  } else {
    queryParams.statusList = [parseInt(val)]
  }
  queryParams.pageNum = 1
  loadRooms()
}

const handleLoadScheme = (id) => {
  if (!id) return
  const scheme = savedSchemes.value.find(s => s.id === id)
  if (!scheme) return
  try {
    const saved = typeof scheme.params === 'string' ? JSON.parse(scheme.params) : scheme.params
    queryParams.roomNumber = saved.roomNumber || ''
    queryParams.buildingId = saved.buildingId || null
    queryParams.floorId = saved.floorId || null
    queryParams.roomTypeId = saved.roomTypeId || null
    queryParams.statusList = saved.statusList || []
    queryParams.orientations = saved.orientations || []
    queryParams.viewTypes = saved.viewTypes || []
    queryParams.specialTags = saved.specialTags || []
    queryParams.pageNum = 1
    activeTab.value = ''
    loadRooms()
  } catch { ElMessage.error('加载方案失败') }
}

const handleSearch = () => { queryParams.pageNum = 1; loadRooms() }

const handleReset = () => {
  queryParams.roomNumber = ''
  queryParams.buildingId = null
  queryParams.floorId = null
  queryParams.roomTypeId = null
  queryParams.statusList = []
  queryParams.orientations = []
  queryParams.viewTypes = []
  queryParams.specialTags = []
  queryParams.pageNum = 1
  activeTab.value = ''
  selectedSchemeId.value = null
  loadRooms()
}

const handleSelectionChange = (rows) => { selectedRows.value = rows }

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除房间「${row.roomNumber}」？`, '提示', { type: 'warning' })
    const res = await api.hotel.deleteRoom(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadRooms(); await loadStats()
    } else { ElMessage.error(res.message || '删除失败') }
  } catch {}
}

const handleViewDetail = async (row) => {
  try {
    const res = await api.hotel.getRoom(row.id)
    if (res.code === 200 && res.data) { detailData.value = res.data; detailVisible.value = true }
  } catch { ElMessage.error('获取房间详情失败') }
}

// Room add/edit dialog
const roomDialogVisible = ref(false)
const roomSaving = ref(false)
const roomFormRef = ref(null)
const formFloors = ref([])

const roomForm = reactive({
  id: null, roomNumber: '', buildingId: null, floorId: null, roomTypeId: null,
  orientation: '', viewType: '', locationFeatures: [], specialTags: [], remark: ''
})

const roomRules = {
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }]
}

const selectedRoomTypeInfo = computed(() => {
  if (!roomForm.roomTypeId) return null
  return roomTypes.value.find(rt => rt.id === roomForm.roomTypeId) || null
})

const loadFormFloors = async (buildingId) => {
  if (!buildingId) { formFloors.value = []; return }
  try {
    const res = await api.hotel.getFloors(buildingId)
    if (res.code === 200) formFloors.value = res.data || []
  } catch { formFloors.value = [] }
}

const handleFormBuildingChange = () => { roomForm.floorId = null; loadFormFloors(roomForm.buildingId) }
const handleFormRoomTypeChange = () => {}

const openRoomDialog = async (row) => {
  if (row) {
    Object.assign(roomForm, {
      id: row.id, roomNumber: row.roomNumber || '', buildingId: row.buildingId || null,
      floorId: row.floorId || null, roomTypeId: row.roomTypeId || null,
      orientation: row.orientation || '', viewType: row.viewType || '',
      locationFeatures: parseJsonArray(row.locationFeatures), specialTags: parseJsonArray(row.specialTags),
      remark: row.remark || ''
    })
    if (row.buildingId) await loadFormFloors(row.buildingId)
  } else {
    Object.assign(roomForm, {
      id: null, roomNumber: '', buildingId: null, floorId: null, roomTypeId: null,
      orientation: '', viewType: '', locationFeatures: [], specialTags: [], remark: ''
    })
    formFloors.value = []
  }
  roomDialogVisible.value = true
}

const handleSaveRoom = async () => {
  const valid = await roomFormRef.value.validate().catch(() => false)
  if (!valid) return
  roomSaving.value = true
  try {
    const payload = {
      roomNumber: roomForm.roomNumber, buildingId: roomForm.buildingId, floorId: roomForm.floorId,
      roomTypeId: roomForm.roomTypeId, orientation: roomForm.orientation || undefined,
      viewType: roomForm.viewType || undefined,
      locationFeatures: roomForm.locationFeatures.length ? JSON.stringify(roomForm.locationFeatures) : undefined,
      specialTags: roomForm.specialTags.length ? JSON.stringify(roomForm.specialTags) : undefined,
      remark: roomForm.remark || undefined
    }
    const res = roomForm.id
      ? await api.hotel.updateRoom({ id: roomForm.id, ...payload })
      : await api.hotel.addRoom(payload)
    if (res.code === 200) {
      ElMessage.success(roomForm.id ? '更新成功' : '新增成功')
      roomDialogVisible.value = false; detailVisible.value = false
      await loadRooms(); await loadStats()
    } else { ElMessage.error(res.message || '操作失败') }
  } catch { ElMessage.error('操作失败') } finally { roomSaving.value = false }
}

// Batch create dialog
const batchCreateDialogVisible = ref(false)
const batchCreateStep = ref(0)
const batchCreateSaving = ref(false)
const batchCreateResult = ref(null)
const batchCreateFloors = ref([])

const batchCreateForm = reactive({
  buildingId: null, floorId: null, roomTypeId: null,
  numberPrefix: '', startNum: '', endNum: '', orientation: '', viewType: ''
})

const batchCreatePreviewNumbers = computed(() => {
  const start = parseInt(batchCreateForm.startNum, 10)
  const end = parseInt(batchCreateForm.endNum, 10)
  if (isNaN(start) || isNaN(end) || start > end) return []
  const prefix = batchCreateForm.numberPrefix || ''
  const numbers = []
  for (let i = start; i <= end; i++) numbers.push(prefix + String(i).padStart(batchCreateForm.startNum.length, '0'))
  return numbers
})

const openBatchCreateDialog = () => {
  batchCreateStep.value = 0; batchCreateResult.value = null
  Object.assign(batchCreateForm, {
    buildingId: null, floorId: null, roomTypeId: null,
    numberPrefix: '', startNum: '', endNum: '', orientation: '', viewType: ''
  })
  batchCreateFloors.value = []; batchCreateDialogVisible.value = true
}

const handleBatchCreateBuildingChange = async () => {
  batchCreateForm.floorId = null
  if (!batchCreateForm.buildingId) { batchCreateFloors.value = []; return }
  try {
    const res = await api.hotel.getFloors(batchCreateForm.buildingId)
    if (res.code === 200) batchCreateFloors.value = res.data || []
  } catch { batchCreateFloors.value = [] }
}

const handleBatchCreate = async () => {
  if (!batchCreateForm.buildingId || !batchCreateForm.floorId || !batchCreateForm.roomTypeId) { ElMessage.warning('请先选择楼栋、楼层和房型'); return }
  if (!batchCreateForm.startNum || !batchCreateForm.endNum) { ElMessage.warning('请输入起始号和结束号'); return }
  batchCreateSaving.value = true
  try {
    const payload = {
      buildingId: batchCreateForm.buildingId, floorId: batchCreateForm.floorId, roomTypeId: batchCreateForm.roomTypeId,
      numberPrefix: batchCreateForm.numberPrefix || '', startNum: parseInt(batchCreateForm.startNum, 10),
      endNum: parseInt(batchCreateForm.endNum, 10), orientation: batchCreateForm.orientation || undefined,
      viewType: batchCreateForm.viewType || undefined
    }
    const res = await api.hotel.batchCreateRooms(payload)
    if (res.code === 200) {
      batchCreateResult.value = res.data || { successCount: batchCreatePreviewNumbers.value.length, failures: [] }
      ElMessage.success('批量创建完成'); await loadRooms(); await loadStats()
    } else { ElMessage.error(res.message || '批量创建失败') }
  } catch { ElMessage.error('批量创建失败') } finally { batchCreateSaving.value = false }
}

// Batch operation dialog
const batchDialogVisible = ref(false)
const batchOperationType = ref('status')
const batchDialogTitle = computed(() => {
  const map = { status: '批量修改房间状态', attr: '批量修改房间属性', delete: '批量删除房间' }
  return map[batchOperationType.value] || '批量操作'
})
const batchLoading = ref(false)

const openBatchDialog = (type) => {
  if (selectedRows.value.length > 50) {
    ElMessage.warning('单次批量操作最多50个房间')
    return
  }
  batchOperationType.value = type
  batchDialogVisible.value = true
}

const clearSelection = () => {
  selectedRows.value = []
  ElMessage.info('请点击表格左上角的复选框取消选择')
}

const handleBatchConfirm = async (params) => {
  batchLoading.value = true
  try {
    const roomIds = selectedRows.value.map(r => r.id)
    let res
    if (batchOperationType.value === 'status') {
      res = await api.hotel.batchUpdateRoomStatus({
        roomIds,
        targetStatus: params.targetStatus,
        reason: params.reason,
        skipInvalid: params.skipInvalid
      })
    } else if (batchOperationType.value === 'attr') {
      res = await api.hotel.batchUpdateRoomAttr({
        roomIds,
        attrType: params.attrType,
        attrMode: params.attrMode,
        attrValue: params.attrValue,
        reason: params.reason,
        skipInvalid: params.skipInvalid
      })
    } else if (batchOperationType.value === 'delete') {
      res = await api.hotel.batchDeleteRooms({
        roomIds,
        reason: params.reason,
        skipInvalid: params.skipInvalid
      })
    }
    
    if (res.code === 200) {
      const data = res.data || {}
      const msg = `批量操作完成：成功${data.successCount || 0}个，失败${data.failCount || 0}个，跳过${data.skipCount || 0}个`
      if ((data.failCount || 0) > 0) {
        ElMessage.warning(msg + `，批次号：${data.batchNo || ''}`)
      } else {
        ElMessage.success(msg + `，批次号：${data.batchNo || ''}`)
      }
      batchDialogVisible.value = false
      selectedRows.value = []
      await loadRooms()
      await loadStats()
    } else {
      ElMessage.error(res.message || '批量操作失败')
    }
  } catch (e) {
    ElMessage.error('批量操作失败')
  } finally {
    batchLoading.value = false
  }
}

// Status dialog
const statusDialogVisible = ref(false)
const statusSaving = ref(false)
const statusForm = reactive({ roomId: null, currentStatus: null, newStatus: null, remark: '' })

const openStatusDialog = (row) => {
  statusForm.roomId = row.id; statusForm.currentStatus = row.status
  statusForm.newStatus = null; statusForm.remark = ''
  statusDialogVisible.value = true
}

const handleUpdateStatus = async () => {
  if (!statusForm.newStatus) { ElMessage.warning('请选择新状态'); return }
  statusSaving.value = true
  try {
    const res = await api.hotel.updateRoomStatus(statusForm.roomId, { status: statusForm.newStatus, remark: statusForm.remark })
    if (res.code === 200) {
      ElMessage.success('状态更新成功'); statusDialogVisible.value = false
      await loadRooms(); await loadStats()
    } else { ElMessage.error(res.message || '状态更新失败') }
  } catch { ElMessage.error('状态更新失败') } finally { statusSaving.value = false }
}

// Floorplan view
const floorplanBuildingId = ref(null)
const floorplanFloorId = ref(null)
const floorplanFloors = ref([])
const floorplanRooms = ref([])

const tooltipVisible = ref(false)
const tooltipX = ref(0)
const tooltipY = ref(0)
const tooltipRoom = ref({})

const handleFloorplanBuildingChange = async () => {
  floorplanFloorId.value = null; floorplanRooms.value = []
  if (!floorplanBuildingId.value) { floorplanFloors.value = []; return }
  try {
    const res = await api.hotel.getFloors(floorplanBuildingId.value)
    if (res.code === 200) floorplanFloors.value = res.data || []
  } catch { floorplanFloors.value = [] }
}

const loadFloorplanRooms = async () => {
  if (!floorplanFloorId.value) { floorplanRooms.value = []; return }
  try {
    const res = await api.hotel.getRoomsByFloor(floorplanFloorId.value)
    if (res.code === 200) floorplanRooms.value = res.data || []
  } catch { floorplanRooms.value = [] }
}

const showTooltip = (event, room) => {
  tooltipRoom.value = room
  tooltipX.value = event.clientX + 12
  tooltipY.value = event.clientY + 12
  tooltipVisible.value = true
}

const hideTooltip = () => { tooltipVisible.value = false }

const floorplanContextMenu = ref(false)
const floorplanMenuPos = reactive({ x: 0, y: 0 })
const floorplanMenuRoom = ref(null)

const openFloorplanAction = (room) => {
  floorplanMenuRoom.value = room
  floorplanMenuPos.x = event.clientX
  floorplanMenuPos.y = event.clientY
  floorplanContextMenu.value = true
}

const handleFloorplanAction = (action) => {
  floorplanContextMenu.value = false
  const room = floorplanMenuRoom.value
  if (!room) return
  switch (action) {
    case 'view': handleViewDetail(room); break
    case 'status': openStatusDialog(room); break
    case 'edit': openRoomDialog(room); break
    case 'copy': openCopyDialog(room); break
  }
}

// Copy dialog
const copyDialogVisible = ref(false)
const copySaving = ref(false)
const copyFormRef = ref(null)
const copySourceRoom = ref({})
const copyFloors = ref([])

const copyForm = reactive({
  roomNumber: '', buildingId: null, floorId: null,
  orientation: '', viewType: '', locationFeatures: [], specialTags: []
})

const copyRules = {
  roomNumber: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
  buildingId: [{ required: true, message: '请选择楼栋', trigger: 'change' }],
  floorId: [{ required: true, message: '请选择楼层', trigger: 'change' }]
}

const openCopyDialog = async (row) => {
  try {
    const res = await api.hotel.getRoom(row.id)
    if (res.code === 200 && res.data) {
      copySourceRoom.value = res.data
    } else {
      copySourceRoom.value = row
    }
  } catch { copySourceRoom.value = row }
  Object.assign(copyForm, {
    roomNumber: '', buildingId: null, floorId: null,
    orientation: copySourceRoom.value.orientation || '',
    viewType: copySourceRoom.value.viewType || '',
    locationFeatures: parseJsonArray(copySourceRoom.value.locationFeatures),
    specialTags: parseJsonArray(copySourceRoom.value.specialTags)
  })
  copyFloors.value = []
  copyDialogVisible.value = true
}

const handleCopyBuildingChange = async () => {
  copyForm.floorId = null
  if (!copyForm.buildingId) { copyFloors.value = []; return }
  try {
    const res = await api.hotel.getFloors(copyForm.buildingId)
    if (res.code === 200) copyFloors.value = res.data || []
  } catch { copyFloors.value = [] }
}

const handleCopyRoom = async () => {
  const valid = await copyFormRef.value.validate().catch(() => false)
  if (!valid) return
  copySaving.value = true
  try {
    const payload = {
      roomNumber: copyForm.roomNumber, buildingId: copyForm.buildingId, floorId: copyForm.floorId,
      orientation: copyForm.orientation || undefined, viewType: copyForm.viewType || undefined,
      locationFeatures: copyForm.locationFeatures.length ? JSON.stringify(copyForm.locationFeatures) : undefined,
      specialTags: copyForm.specialTags.length ? JSON.stringify(copyForm.specialTags) : undefined
    }
    const res = await api.hotel.copyRoom(copySourceRoom.value.id, payload)
    if (res.code === 200) {
      ElMessage.success('复制成功'); copyDialogVisible.value = false
      await loadRooms(); await loadStats()
    } else { ElMessage.error(res.message || '复制失败') }
  } catch { ElMessage.error('复制失败') } finally { copySaving.value = false }
}

// Template dialog
const templateDialogVisible = ref(false)
const templateStep = ref(0)
const templateSaving = ref(false)
const templateSearchKey = ref('')
const templateSelectedRoom = ref(null)
const allRoomList = ref([])

const templateForm = reactive({
  applyOrientation: true, applyViewType: true,
  applyLocationFeatures: true, applySpecialTags: true, reason: ''
})

const filteredTemplateRooms = computed(() => {
  if (!templateSearchKey.value) return allRoomList.value
  const key = templateSearchKey.value.toLowerCase()
  return allRoomList.value.filter(r =>
    (r.roomNumber && r.roomNumber.toLowerCase().includes(key)) ||
    (r.buildingName && r.buildingName.toLowerCase().includes(key))
  )
})

const openTemplateDialog = async () => {
  templateStep.value = 0; templateSelectedRoom.value = null; templateSearchKey.value = ''
  templateForm.applyOrientation = true; templateForm.applyViewType = true
  templateForm.applyLocationFeatures = true; templateForm.applySpecialTags = true
  templateForm.reason = ''
  try {
    const res = await api.hotel.getRoomList({})
    if (res.code === 200) allRoomList.value = res.data || []
  } catch { allRoomList.value = [] }
  templateDialogVisible.value = true
}

const handleTemplateRoomSelect = (row) => { templateSelectedRoom.value = row }

const handleApplyTemplate = async () => {
  templateSaving.value = true
  try {
    const payload = {
      templateRoomId: templateSelectedRoom.value.id,
      targetRoomIds: selectedRows.value.map(r => r.id),
      applyOrientation: templateForm.applyOrientation,
      applyViewType: templateForm.applyViewType,
      applyLocationFeatures: templateForm.applyLocationFeatures,
      applySpecialTags: templateForm.applySpecialTags,
      reason: templateForm.reason || undefined
    }
    const res = await api.hotel.applyTemplate(payload)
    if (res.code === 200) {
      ElMessage.success('模板应用成功'); templateDialogVisible.value = false
      await loadRooms(); await loadStats()
    } else { ElMessage.error(res.message || '应用失败') }
  } catch { ElMessage.error('应用失败') } finally { templateSaving.value = false }
}

// Export dialog
const exportDialogVisible = ref(false)
const exportSaving = ref(false)

const exportForm = reactive({
  scope: 'current',
  customIds: '',
  exportFields: ['roomNumber', 'building', 'floor', 'roomType', 'orientation', 'viewType', 'status']
})

const exportCount = computed(() => {
  if (exportForm.scope === 'current') return tableData.value.length
  if (exportForm.scope === 'all') return total.value
  return exportForm.customIds ? exportForm.customIds.split(',').filter(Boolean).length : 0
})

const openExportDialog = () => {
  exportForm.scope = 'current'; exportForm.customIds = ''
  exportForm.exportFields = ['roomNumber', 'building', 'floor', 'roomType', 'orientation', 'viewType', 'status']
  exportDialogVisible.value = true
}

const handleExport = async () => {
  if (exportForm.exportFields.length === 0) { ElMessage.warning('请至少选择一个导出字段'); return }
  exportSaving.value = true
  try {
    const payload = {
      scope: exportForm.scope,
      exportFields: exportForm.exportFields,
      customIds: exportForm.scope === 'custom' ? exportForm.customIds : undefined,
      roomNumber: queryParams.roomNumber || undefined,
      buildingId: queryParams.buildingId || undefined,
      floorId: queryParams.floorId || undefined,
      roomTypeId: queryParams.roomTypeId || undefined,
      statusList: queryParams.statusList.length > 0 ? queryParams.statusList : undefined,
      orientations: queryParams.orientations.length > 0 ? queryParams.orientations : undefined,
      viewTypes: queryParams.viewTypes.length > 0 ? queryParams.viewTypes : undefined,
      specialTags: queryParams.specialTags.length > 0 ? queryParams.specialTags : undefined
    }
    const res = await api.hotel.exportRooms(payload)
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url; link.download = '房间列表.xlsx'
    document.body.appendChild(link); link.click()
    document.body.removeChild(link); window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功'); exportDialogVisible.value = false
  } catch { ElMessage.error('导出失败') } finally { exportSaving.value = false }
}

// Save scheme dialog
const saveSchemeDialogVisible = ref(false)
const saveSchemeSaving = ref(false)
const saveSchemeName = ref('')

const openSaveSchemeDialog = () => {
  saveSchemeName.value = ''; saveSchemeDialogVisible.value = true
}

const handleSaveScheme = async () => {
  if (!saveSchemeName.value.trim()) { ElMessage.warning('请输入方案名称'); return }
  saveSchemeSaving.value = true
  try {
    const params = {
      roomNumber: queryParams.roomNumber,
      buildingId: queryParams.buildingId,
      floorId: queryParams.floorId,
      roomTypeId: queryParams.roomTypeId,
      statusList: queryParams.statusList,
      orientations: queryParams.orientations,
      viewTypes: queryParams.viewTypes,
      specialTags: queryParams.specialTags
    }
    const res = await api.hotel.addSavedFilter({ filterName: saveSchemeName.value, filterConfig: JSON.stringify(params) })
    if (res.code === 200) {
      ElMessage.success('方案保存成功'); saveSchemeDialogVisible.value = false
      await loadSavedSchemes()
    } else { ElMessage.error(res.message || '保存失败') }
  } catch { ElMessage.error('保存失败') } finally { saveSchemeSaving.value = false }
}

const closeFloorplanMenu = () => { floorplanContextMenu.value = false }

onMounted(() => {
  loadBuildings(); loadRoomTypes(); loadRooms(); loadStats(); loadSavedSchemes()
  document.addEventListener('click', closeFloorplanMenu)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', closeFloorplanMenu)
})
</script>

<style scoped>
.room-manage-container {
  padding: 10px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 12px;
  border: none;
  cursor: default;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #718096;
  margin-top: 4px;
}

.stat-icon {
  opacity: 0.15;
}

.stat-total .stat-value { color: #667eea; }
.stat-total .stat-icon { color: #667eea; }
.stat-free .stat-value { color: #67c23a; }
.stat-free .stat-icon { color: #67c23a; }
.stat-occupied .stat-value { color: #409eff; }
.stat-occupied .stat-icon { color: #409eff; }
.stat-maintenance .stat-value { color: #f56c6c; }
.stat-maintenance .stat-icon { color: #f56c6c; }

.control-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 12px;
}

.control-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.quick-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.quick-tab {
  cursor: pointer;
}

.filter-card {
  border-radius: 12px;
  border: none;
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.table-toolbar-left {
  display: flex;
  gap: 10px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.floorplan-controls {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.floorplan-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 10px;
  margin-bottom: 20px;
}

.floorplan-room {
  border-radius: 8px;
  padding: 12px 8px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  color: #fff;
  font-size: 13px;
}

.floorplan-room:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.room-number {
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 4px;
}

.room-status-text {
  font-size: 12px;
  opacity: 0.9;
}

.status-bg.status-1 { background: #67c23a; }
.status-bg.status-2 { background: #e6a23c; }
.status-bg.status-3 { background: #409eff; }
.status-bg.status-4 { background: #909399; }
.status-bg.status-5 { background: #b1b3b8; }
.status-bg.status-6 { background: #f56c6c; }
.status-bg.status-7 { background: #c0c4cc; }

.floorplan-legend {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  padding: 12px 0;
  border-top: 1px solid #ebeef5;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.legend-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  display: inline-block;
}

.legend-color.status-1 { background: #67c23a; }
.legend-color.status-2 { background: #e6a23c; }
.legend-color.status-3 { background: #409eff; }
.legend-color.status-4 { background: #909399; }
.legend-color.status-5 { background: #b1b3b8; }
.legend-color.status-6 { background: #f56c6c; }
.legend-color.status-7 { background: #c0c4cc; }

.room-tooltip {
  position: fixed;
  z-index: 9999;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  pointer-events: none;
  font-size: 13px;
  min-width: 160px;
}

.tooltip-title {
  font-weight: 700;
  font-size: 15px;
  margin-bottom: 6px;
  color: #303133;
}

.tooltip-row {
  color: #606266;
  line-height: 1.8;
}

.tooltip-row span {
  color: #909399;
}

.floorplan-context-menu {
  position: fixed;
  z-index: 10000;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 4px 0;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  min-width: 120px;
}

.context-menu-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  color: #303133;
  transition: background 0.15s;
}

.context-menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.dialog-section-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
}

.room-type-info-box {
  margin: -10px 0 10px 100px;
  padding: 10px;
  background: #f4f6fb;
  border-radius: 8px;
}

.batch-steps {
  margin-bottom: 24px;
}

.batch-section {
  min-height: 200px;
}

.batch-preview {
  margin-top: 16px;
}

.batch-preview-title {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
}

.batch-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  max-height: 160px;
  overflow-y: auto;
}

.preview-tag {
  border-radius: 4px;
}

.batch-result {
  margin-top: 20px;
}

.batch-failures {
  margin-top: 12px;
}

.batch-failures-title {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
}

.detail-content {
  padding: 0 4px;
}

.detail-section {
  margin-top: 20px;
}

.section-title {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.info-tag {
  border-radius: 14px;
}

.detail-remark {
  margin: 0;
  font-size: 14px;
  color: #4a5568;
  line-height: 1.6;
  white-space: pre-wrap;
}

.batch-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f0f9ff 100%);
  border: 1px solid #d9ecff;
  border-radius: 8px;
  margin-bottom: 12px;
}

.batch-count-tag {
  font-size: 14px;
}

.batch-count-tag strong {
  color: #409eff;
  font-size: 16px;
  margin: 0 4px;
}
</style>
