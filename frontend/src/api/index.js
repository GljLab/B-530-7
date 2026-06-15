import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    let message = '请求失败'
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          message = '登录已过期，请重新登录'
          localStorage.removeItem('token')
          ElMessageBox.confirm(message, '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            window.location.href = '/login'
          }).catch(() => {})
          break
        case 403:
          message = '没有权限访问该资源'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = data?.message || '服务器内部错误'
          break
        default:
          message = data?.message || `请求失败: ${status}`
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// API模块
const api = {
  // 认证相关
  auth: {
    login: (data) => request.post('/auth/login', data),
    register: (data) => request.post('/auth/register', data),
    getUserInfo: () => request.get('/auth/info'),
    logout: () => request.post('/auth/logout')
  },
  
  // 用户管理
  user: {
    list: (params) => request.get('/system/user/list', { params }),
    getById: (id) => request.get(`/system/user/${id}`),
    add: (data) => request.post('/system/user', data),
    update: (data) => request.put('/system/user', data),
    delete: (id) => request.delete(`/system/user/${id}`),
    updateStatus: (data) => request.put('/system/user/status', data),
    resetPassword: (data) => request.put('/system/user/resetPwd', data),
    changePassword: (data) => request.put('/system/user/profile/password', data),
    updateProfile: (data) => request.put('/system/user/profile', data)
  },
  
  // 角色管理
  role: {
    list: (params) => request.get('/system/role/list', { params }),
    listAll: () => request.get('/system/role/listAll'),
    getById: (id) => request.get(`/system/role/${id}`),
    add: (data) => request.post('/system/role', data),
    update: (data) => request.put('/system/role', data),
    delete: (id) => request.delete(`/system/role/${id}`),
    updateStatus: (data) => request.put('/system/role/status', data)
  },
  
  // 菜单管理
  menu: {
    tree: () => request.get('/system/menu/tree'),
    list: () => request.get('/system/menu/list'),
    selectTree: () => request.get('/system/menu/selectTree'),
    getById: (id) => request.get(`/system/menu/${id}`),
    add: (data) => request.post('/system/menu', data),
    update: (data) => request.put('/system/menu', data),
    delete: (id) => request.delete(`/system/menu/${id}`)
  },
  
  // 数据权限
  dataPerm: {
    list: (params) => request.get('/system/dataPermission/list', { params }),
    listAll: () => request.get('/system/dataPermission/all'),
    getByRoleId: (roleId) => request.get(`/system/dataPermission/role/${roleId}`),
    save: (data) => request.post('/system/dataPermission', data),
    deleteByRoleId: (roleId) => request.delete(`/system/dataPermission/role/${roleId}`),
    getScopeTypes: () => request.get('/system/dataPermission/scopeTypes')
  },

  hotel: {
    getInfo: () => request.get('/hotel/info'),
    saveInfo: (data) => request.post('/hotel/info', data),
    getFacilities: (hotelId) => request.get(`/hotel/info/facilities/${hotelId}`),
    saveFacilities: (hotelId, data) => request.post(`/hotel/info/facilities/${hotelId}`, data),
    getImages: (refType, refId) => request.get(`/hotel/info/images/${refType}/${refId}`),
    saveImages: (refType, refId, data) => request.post(`/hotel/info/images/${refType}/${refId}`, data),
    setMainImage: (imageId, refType, refId) => request.put(`/hotel/info/images/main/${imageId}`, null, { params: { refType, refId } }),
    deleteImage: (imageId) => request.delete(`/hotel/info/images/${imageId}`),

    getBuildings: () => request.get('/hotel/building/list'),
    getBuilding: (id) => request.get(`/hotel/building/${id}`),
    addBuilding: (data) => request.post('/hotel/building', data),
    updateBuilding: (data) => request.put('/hotel/building', data),
    deleteBuilding: (id) => request.delete(`/hotel/building/${id}`),
    updateBuildingStatus: (data) => request.put(`/hotel/building/${data.id}/status`, data),

    getFloors: (buildingId) => request.get(`/hotel/floor/list/${buildingId}`),
    getFloor: (id) => request.get(`/hotel/floor/${id}`),
    addFloor: (data) => request.post('/hotel/floor', data),
    updateFloor: (data) => request.put('/hotel/floor', data),
    deleteFloor: (id) => request.delete(`/hotel/floor/${id}`),

    getRoomTypes: () => request.get('/hotel/roomType/list'),
    getRoomTypePage: (params) => request.get('/hotel/roomType/page', { params }),
    getRoomType: (id) => request.get(`/hotel/roomType/${id}`),
    addRoomType: (data) => request.post('/hotel/roomType', data),
    updateRoomType: (data) => request.put('/hotel/roomType', data),
    deleteRoomType: (id) => request.delete(`/hotel/roomType/${id}`),
    updateRoomTypeSaleStatus: (data) => request.put(`/hotel/roomType/${data.id}/saleStatus`, data),

    getRoomPage: (params) => request.get('/hotel/room/page', { params }),
    getRoom: (id) => request.get(`/hotel/room/${id}`),
    addRoom: (data) => request.post('/hotel/room', data),
    updateRoom: (data) => request.put('/hotel/room', data),
    deleteRoom: (id) => request.delete(`/hotel/room/${id}`),
    batchCreateRooms: (data) => request.post('/hotel/room/batch', data),
    copyRoom: (sourceRoomId, data) => request.post(`/hotel/room/copy/${sourceRoomId}`, data),
    applyTemplate: (data) => request.post('/hotel/room/applyTemplate', data),
    updateRoomStatus: (id, data) => request.put(`/hotel/room/${id}/status`, data),
    getRoomStatusLogs: (roomId) => request.get(`/hotel/room/${roomId}/logs`),
    getRoomList: (params) => request.get('/hotel/room/list', { params }),
    getRoomsByFloor: (floorId) => request.get(`/hotel/room/floor/${floorId}`),
    exportRooms: (data) => request.post('/hotel/room/export', data, { responseType: 'blob' }),

    getSavedFilters: () => request.get('/hotel/filter/list'),
    getSavedFilter: (id) => request.get(`/hotel/filter/${id}`),
    addSavedFilter: (data) => request.post('/hotel/filter', data),
    updateSavedFilter: (data) => request.put('/hotel/filter', data),
    deleteSavedFilter: (id) => request.delete(`/hotel/filter/${id}`),

    getDashboardOverview: () => request.get('/hotel/dashboard/overview'),
    getRoomStatusStats: () => request.get('/hotel/dashboard/roomStatus'),
    getRoomTypeStats: () => request.get('/hotel/dashboard/roomType'),
    getFloorStats: () => request.get('/hotel/dashboard/floor'),
    getDetailedRoomTypeStats: () => request.get('/hotel/dashboard/roomType/detail'),
    getDetailedFloorStats: () => request.get('/hotel/dashboard/floor/detail'),
    getAttributeDistribution: () => request.get('/hotel/dashboard/attributeDist'),
    getStatusDurationStats: () => request.get('/hotel/dashboard/statusDuration'),

    uploadFile: (formData) => request.post('/file/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),

    getMaintenanceDashboard: () => request.get('/maintenance/order/dashboard'),
    getMaintenanceOrderPage: (params) => request.get('/maintenance/order/page', { params }),
    getMaintenanceOrder: (id) => request.get(`/maintenance/order/${id}`),
    getMaintenanceOrdersByRoom: (roomId) => request.get(`/maintenance/order/room/${roomId}`),
    getRoomMaintenanceStats: (roomId) => request.get(`/maintenance/order/room/${roomId}/stats`),
    createMaintenanceOrder: (data) => request.post('/maintenance/order', data),
    assignMaintenanceOrder: (id, data) => request.put(`/maintenance/order/${id}/assign`, data),
    acceptMaintenanceOrder: (id) => request.put(`/maintenance/order/${id}/accept`),
    addMaintenanceProgress: (id, data) => request.put(`/maintenance/order/${id}/progress`, data),
    finishMaintenanceOrder: (id, data) => request.put(`/maintenance/order/${id}/finish`, data),
    inspectMaintenanceOrder: (id, data) => request.put(`/maintenance/order/${id}/inspect`, data),
    deleteMaintenanceOrder: (id) => request.delete(`/maintenance/order/${id}`),
    exportMaintenanceOrders: (data) => request.post('/maintenance/order/export', data, { responseType: 'blob' }),

    getChangeLogPage: (params) => request.get('/maintenance/changeLog/page', { params }),
    getChangeLogsByRoom: (roomId) => request.get(`/maintenance/changeLog/room/${roomId}`),

    getStatsOverview: () => request.get('/maintenance/statistics/overview'),
    getStatsTopRooms: (limit) => request.get('/maintenance/statistics/topRooms', { params: { limit } }),
    getStatsTypeDistribution: () => request.get('/maintenance/statistics/typeDistribution'),
    getStatsCostTrend: (months) => request.get('/maintenance/statistics/costTrend', { params: { months } }),
    getStatsDuration: () => request.get('/maintenance/statistics/durationStats'),
    getStatsStaffWorkload: () => request.get('/maintenance/statistics/staffWorkload'),
    exportMaintenanceStats: () => request.post('/maintenance/statistics/export', {}, { responseType: 'blob' }),

    getMaintenanceStaffList: () => request.get('/system/user/list', { params: { pageNum: 1, pageSize: 100, status: 1 } })
  }
}

export default api
