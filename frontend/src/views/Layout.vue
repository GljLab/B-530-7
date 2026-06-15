<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <span v-if="!isCollapse">智慧酒店管理平台</span>
        <span v-else>酒</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <el-sub-menu index="/system" v-if="hasSystemPermission">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          
          <el-menu-item index="/system/user" v-if="hasPermission('system:user:list')">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          
          <el-menu-item index="/system/role" v-if="hasPermission('system:role:list')">
            <el-icon><Avatar /></el-icon>
            <span>角色管理</span>
          </el-menu-item>
          
          <el-menu-item index="/system/menu" v-if="hasPermission('system:menu:list')">
            <el-icon><Menu /></el-icon>
            <span>菜单管理</span>
          </el-menu-item>
          
          <el-menu-item index="/system/dataPerm" v-if="hasPermission('system:dataPerm:list')">
            <el-icon><Lock /></el-icon>
            <span>数据权限</span>
          </el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="/hotel" v-if="hasHotelPermission">
          <template #title>
            <el-icon><House /></el-icon>
            <span>酒店管理</span>
          </template>

          <el-menu-item index="/hotel/overview" v-if="hasPermission('hotel:info:list')">
            <el-icon><OfficeBuilding /></el-icon>
            <span>酒店概览</span>
          </el-menu-item>

          <el-menu-item index="/hotel/building" v-if="hasPermission('hotel:building:list')">
            <el-icon><School /></el-icon>
            <span>楼栋楼层</span>
          </el-menu-item>

          <el-menu-item index="/hotel/roomType" v-if="hasPermission('hotel:roomType:list')">
            <el-icon><Tickets /></el-icon>
            <span>房型管理</span>
          </el-menu-item>

          <el-menu-item index="/hotel/room" v-if="hasPermission('hotel:room:list')">
            <el-icon><Key /></el-icon>
            <span>房间管理</span>
          </el-menu-item>

          <el-menu-item index="/hotel/dashboard" v-if="hasPermission('hotel:dashboard:list')">
            <el-icon><DataAnalysis /></el-icon>
            <span>统计看板</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/maintenance" v-if="hasMaintenancePermission">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>维护管理</span>
          </template>

          <el-menu-item index="/maintenance/order" v-if="hasPermission('maintenance:order:list')">
            <el-icon><Document /></el-icon>
            <span>维护单管理</span>
          </el-menu-item>

          <el-menu-item index="/maintenance/order/create" v-if="hasPermission('maintenance:order:add')">
            <el-icon><Edit /></el-icon>
            <span>创建维护单</span>
          </el-menu-item>

          <el-menu-item index="/maintenance/changeLog" v-if="hasPermission('maintenance:changeLog:list')">
            <el-icon><Clock /></el-icon>
            <span>房间变更日志</span>
          </el-menu-item>

          <el-menu-item index="/maintenance/statistics" v-if="hasPermission('maintenance:statistics:list')">
            <el-icon><DataLine /></el-icon>
            <span>维护统计报表</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区 -->
    <el-container class="layout-main">
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title && $route.path !== '/dashboard'">
              {{ $route.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.user?.avatar">
                {{ userStore.user?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="nickname">{{ userStore.user?.nickname || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区 -->
      <el-main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  HomeFilled, Setting, User, Avatar, Menu, Lock,
  Expand, Fold, ArrowDown, SwitchButton,
  House, OfficeBuilding, School, Tickets, Key, DataAnalysis,
  Tools, Document, Edit, Clock, DataLine
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const hasPermission = (permission) => {
  return userStore.hasPermission(permission)
}

const hasSystemPermission = computed(() => {
  return hasPermission('system:user:list') ||
         hasPermission('system:role:list') ||
         hasPermission('system:menu:list') ||
         hasPermission('system:dataPerm:list')
})

const hasHotelPermission = computed(() => {
  return hasPermission('hotel:info:list') ||
         hasPermission('hotel:building:list') ||
         hasPermission('hotel:roomType:list') ||
         hasPermission('hotel:room:list') ||
         hasPermission('hotel:dashboard:list')
})

const hasMaintenancePermission = computed(() => {
  return hasPermission('maintenance:order:list') ||
         hasPermission('maintenance:order:add') ||
         hasPermission('maintenance:changeLog:list') ||
         hasPermission('maintenance:statistics:list')
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      })
      userStore.logout()
      router.push('/login')
      ElMessage.success('已退出登录')
    } catch {
      // 用户取消
    }
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  min-height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  background-color: #263445;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  height: 60px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.collapse-btn:hover {
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.nickname {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.layout-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

:deep(.el-menu) {
  border-right: none;
}
</style>
