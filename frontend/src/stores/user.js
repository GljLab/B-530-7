import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(null)
  const menus = ref([])
  const permissions = ref([])
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  
  const isAdmin = computed(() => {
    if (!user.value || !user.value.roles) return false
    return user.value.roles.some(role => role.roleKey === 'admin')
  })
  
  // 方法
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const login = async (username, password) => {
    const res = await api.auth.login({ username, password })
    if (res.code === 200) {
      setToken(res.data.token)
      user.value = res.data.user
      menus.value = res.data.menus || []
      permissions.value = res.data.permissions || []
      return res
    }
    throw new Error(res.message)
  }
  
  const getUserInfo = async () => {
    const res = await api.auth.getUserInfo()
    if (res.code === 200) {
      user.value = res.data.user
      menus.value = res.data.menus || []
      permissions.value = res.data.permissions || []
      return res
    }
    throw new Error(res.message)
  }
  
  const logout = () => {
    token.value = ''
    user.value = null
    menus.value = []
    permissions.value = []
    localStorage.removeItem('token')
  }
  
  const hasPermission = (perm) => {
    // 超级管理员拥有所有权限
    if (isAdmin.value) return true
    return permissions.value.includes(perm)
  }
  
  const hasAnyPermission = (perms) => {
    if (isAdmin.value) return true
    return perms.some(perm => permissions.value.includes(perm))
  }
  
  return {
    token,
    user,
    menus,
    permissions,
    isLoggedIn,
    isAdmin,
    setToken,
    login,
    getUserInfo,
    logout,
    hasPermission,
    hasAnyPermission
  }
})
