<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 用户信息卡片 -->
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-header">
            <el-avatar :size="100" :src="userStore.user?.avatar">
              {{ userStore.user?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <h3>{{ userStore.user?.nickname }}</h3>
            <p>{{ userStore.user?.username }}</p>
          </div>
          <el-divider />
          <div class="user-info-list">
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span>{{ userStore.user?.email || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Phone /></el-icon>
              <span>{{ userStore.user?.phone || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Avatar /></el-icon>
              <span>{{ userStore.user?.roles?.map(r => r.roleName).join(', ') || '普通用户' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 表单区域 -->
      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="info">
              <el-form ref="infoFormRef" :model="infoForm" :rules="infoRules" label-width="100px">
                <el-form-item label="用户名">
                  <el-input v-model="userStore.user.username" disabled />
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="infoForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="infoForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="infoForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateInfo" :loading="infoLoading">
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <el-tab-pane label="修改密码" name="password">
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdatePwd" :loading="pwdLoading">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Message, Phone, Avatar } from '@element-plus/icons-vue'
import request from '@/utils/request'

const userStore = useUserStore()

const activeTab = ref('info')

const infoFormRef = ref(null)
const pwdFormRef = ref(null)
const infoLoading = ref(false)
const pwdLoading = ref(false)

const infoForm = reactive({
  nickname: '',
  email: '',
  phone: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPwd = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const infoRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPwd, trigger: 'blur' }
  ]
}

const handleUpdateInfo = async () => {
  if (!infoFormRef.value) return
  await infoFormRef.value.validate(async (valid) => {
    if (!valid) return
    infoLoading.value = true
    try {
      await request.put('/api/system/user/profile', infoForm)
      ElMessage.success('修改成功')
      await userStore.getUserInfo()
    } catch (error) {
      ElMessage.error(error.message || '修改失败')
    } finally {
      infoLoading.value = false
    }
  })
}

const handleUpdatePwd = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    pwdLoading.value = true
    try {
      await request.put('/api/system/user/profile/password', {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('密码修改成功')
      pwdFormRef.value.resetFields()
    } catch (error) {
      ElMessage.error(error.message || '修改失败')
    } finally {
      pwdLoading.value = false
    }
  })
}

onMounted(() => {
  if (userStore.user) {
    infoForm.nickname = userStore.user.nickname || ''
    infoForm.email = userStore.user.email || ''
    infoForm.phone = userStore.user.phone || ''
  }
})
</script>

<style scoped>
.profile-container {
  padding: 10px;
}

.user-card {
  text-align: center;
}

.user-header {
  padding: 20px 0;
}

.user-header h3 {
  margin: 15px 0 5px;
  font-size: 20px;
  color: #2d3748;
}

.user-header p {
  margin: 0;
  color: #718096;
}

.user-info-list {
  text-align: left;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  color: #4a5568;
}

.info-item .el-icon {
  color: #667eea;
}
</style>
