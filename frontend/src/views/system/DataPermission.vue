<template>
  <div class="data-permission-container">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>数据权限配置</span>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleKey" label="角色标识" width="150" />
        <el-table-column label="数据权限范围" min-width="200">
          <template #default="{ row }">
            <el-tag :type="getScopeTypeTag(row.scopeType)">
              {{ getScopeTypeName(row.scopeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">配置</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadData"
        @current-change="loadData"
        class="pagination"
      />
    </el-card>

    <!-- 配置对话框 -->
    <el-dialog v-model="dialogVisible" title="数据权限配置" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="角色名称">
          <el-input :value="currentRole?.roleName" disabled />
        </el-form-item>
        <el-form-item label="权限范围" prop="scopeType">
          <el-select v-model="form.scopeType" style="width: 100%">
            <el-option label="全部数据" :value="1" />
            <el-option label="自定义数据" :value="2" />
            <el-option label="本部门数据" :value="3" />
            <el-option label="本部门及以下数据" :value="4" />
            <el-option label="仅本人数据" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="自定义部门" v-if="form.scopeType === 2">
          <el-input v-model="form.customDeptIds" placeholder="请输入部门ID，多个用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const total = ref(0)
const currentRole = ref(null)
const formRef = ref(null)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  roleId: null,
  scopeType: 1,
  customDeptIds: ''
})

const rules = {
  scopeType: [{ required: true, message: '请选择权限范围', trigger: 'change' }]
}

const getScopeTypeName = (type) => {
  const types = {
    1: '全部数据',
    2: '自定义数据',
    3: '本部门数据',
    4: '本部门及以下数据',
    5: '仅本人数据'
  }
  return types[type] || '未设置'
}

const getScopeTypeTag = (type) => {
  const tags = {
    1: 'success',
    2: 'warning',
    3: '',
    4: '',
    5: 'info'
  }
  return tags[type] || 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/system/role/list', { params: queryForm })
    const roles = res.data?.list || []
    
    // 加载每个角色的数据权限配置
    for (const role of roles) {
      try {
        const permRes = await request.get(`/api/system/dataPermission/role/${role.id}`)
        role.scopeType = permRes.data?.scopeType
        role.customDeptIds = permRes.data?.customDeptIds
      } catch {
        role.scopeType = null
      }
    }
    
    tableData.value = roles
    total.value = res.data?.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  currentRole.value = row
  form.roleId = row.id
  form.scopeType = row.scopeType || 1
  form.customDeptIds = row.customDeptIds || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await request.post('/api/system/dataPermission', form)
      ElMessage.success('配置成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error(error.message || '配置失败')
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.table-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
