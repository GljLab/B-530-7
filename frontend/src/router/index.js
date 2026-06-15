import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 静态路由
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'User' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'system:user:list' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理', icon: 'Avatar', permission: 'system:role:list' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/Menu.vue'),
        meta: { title: '菜单管理', icon: 'Menu', permission: 'system:menu:list' }
      },
      {
        path: 'system/dataPerm',
        name: 'SystemDataPerm',
        component: () => import('@/views/system/DataPermission.vue'),
        meta: { title: '数据权限', icon: 'Lock', permission: 'system:dataPerm:list' }
      },
      {
        path: 'hotel/overview',
        name: 'HotelOverview',
        component: () => import('@/views/hotel/HotelOverview.vue'),
        meta: { title: '酒店概览', icon: 'OfficeBuilding', permission: 'hotel:info:list' }
      },
      {
        path: 'hotel/building',
        name: 'BuildingFloor',
        component: () => import('@/views/hotel/BuildingFloor.vue'),
        meta: { title: '楼栋楼层', icon: 'School', permission: 'hotel:building:list' }
      },
      {
        path: 'hotel/roomType',
        name: 'RoomType',
        component: () => import('@/views/hotel/RoomType.vue'),
        meta: { title: '房型管理', icon: 'Tickets', permission: 'hotel:roomType:list' }
      },
      {
        path: 'hotel/room',
        name: 'RoomManage',
        component: () => import('@/views/hotel/RoomManage.vue'),
        meta: { title: '房间管理', icon: 'Key', permission: 'hotel:room:list' }
      },
      {
        path: 'hotel/room/:id',
        name: 'RoomDetail',
        component: () => import('@/views/hotel/RoomDetail.vue'),
        meta: { title: '房间详情', icon: 'Key', permission: 'hotel:room:query' }
      },
      {
        path: 'hotel/dashboard',
        name: 'HotelDashboard',
        component: () => import('@/views/hotel/HotelDashboard.vue'),
        meta: { title: '统计看板', icon: 'DataAnalysis', permission: 'hotel:dashboard:list' }
      },
      {
        path: 'maintenance/order',
        name: 'MaintenanceOrderList',
        component: () => import('@/views/maintenance/MaintenanceOrderList.vue'),
        meta: { title: '维护单管理', icon: 'Document', permission: 'maintenance:order:list' }
      },
      {
        path: 'maintenance/order/create',
        name: 'MaintenanceOrderCreate',
        component: () => import('@/views/maintenance/MaintenanceOrderCreate.vue'),
        meta: { title: '创建维护单', icon: 'Edit', permission: 'maintenance:order:add' }
      },
      {
        path: 'maintenance/order/:id',
        name: 'MaintenanceOrderDetail',
        component: () => import('@/views/maintenance/MaintenanceOrderDetail.vue'),
        meta: { title: '维护单详情', icon: 'Document', permission: 'maintenance:order:query' }
      },
      {
        path: 'maintenance/changeLog',
        name: 'RoomChangeLog',
        component: () => import('@/views/maintenance/RoomChangeLog.vue'),
        meta: { title: '房间变更日志', icon: 'Clock', permission: 'maintenance:changeLog:list' }
      },
      {
        path: 'maintenance/statistics',
        name: 'MaintenanceStatistics',
        component: () => import('@/views/maintenance/MaintenanceStatistics.vue'),
        meta: { title: '维护统计报表', icon: 'DataLine', permission: 'maintenance:statistics:list' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/404.vue'),
    meta: { title: '404', requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智慧酒店管理平台` : '智慧酒店管理平台'
  
  const userStore = useUserStore()
  const token = userStore.token
  
  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    if (token && to.path === '/login') {
      next('/')
    } else {
      next()
    }
    return
  }
  
  // 需要认证的页面
  if (!token) {
    next('/login')
    return
  }
  
  // 检查用户信息是否已加载
  if (!userStore.user) {
    try {
      await userStore.getUserInfo()
    } catch (error) {
      userStore.logout()
      next('/login')
      return
    }
  }
  
  // 检查权限
  if (to.meta.permission) {
    const hasPermission = userStore.hasPermission(to.meta.permission)
    if (!hasPermission) {
      next('/dashboard')
      return
    }
  }
  
  next()
})

export default router
