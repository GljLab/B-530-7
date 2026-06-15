package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.SysMenu;
import com.example.permission.mapper.SysMenuMapper;
import com.example.permission.mapper.SysRoleMenuMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.permission.entity.table.SysMenuTableDef.SYS_MENU;

/**
 * 菜单服务
 */
@Service
public class SysMenuService {
    
    @Autowired
    private SysMenuMapper menuMapper;
    
    @Autowired
    private SysRoleMenuMapper roleMenuMapper;
    
    /**
     * 获取菜单树形结构
     */
    public List<SysMenu> getMenuTree() {
        QueryWrapper query = QueryWrapper.create()
                .from(SysMenu.class)
                .where(SYS_MENU.STATUS.eq(1))
                .orderBy(SYS_MENU.PARENT_ID.asc())
                .orderBy(SYS_MENU.ORDER_NUM.asc());
        
        List<SysMenu> allMenus = menuMapper.selectListByQuery(query);
        return buildTree(allMenus, 0L);
    }
    
    /**
     * 获取所有菜单列表
     */
    public List<SysMenu> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(SysMenu.class)
                .orderBy(SYS_MENU.PARENT_ID.asc())
                .orderBy(SYS_MENU.ORDER_NUM.asc());
        return menuMapper.selectListByQuery(query);
    }
    
    /**
     * 根据用户ID获取菜单树
     */
    public List<SysMenu> getMenuTreeByUserId(Long userId) {
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        return buildTree(menus, 0L);
    }
    
    /**
     * 新增菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(SysMenu menu) {
        // 检查菜单名称是否在同一父级下重复
        Long parentId = menu.getParentId() != null ? menu.getParentId() : 0L;
        QueryWrapper query = QueryWrapper.create()
                .from(SysMenu.class)
                .where(SYS_MENU.MENU_NAME.eq(menu.getMenuName()))
                .and(SYS_MENU.PARENT_ID.eq(parentId));
        if (menuMapper.selectCountByQuery(query) > 0) {
            throw new BusinessException("同级菜单名称已存在");
        }
        
        menu.setParentId(parentId);
        menu.setStatus(menu.getStatus() != null ? menu.getStatus() : 1);
        menu.setVisible(menu.getVisible() != null ? menu.getVisible() : 1);
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        
        menuMapper.insert(menu);
    }
    
    /**
     * 更新菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(SysMenu menu) {
        SysMenu existMenu = menuMapper.selectOneById(menu.getId());
        if (existMenu == null) {
            throw new BusinessException("菜单不存在");
        }
        
        // 不能将自己设为父菜单
        if (menu.getParentId() != null && menu.getParentId().equals(menu.getId())) {
            throw new BusinessException("不能将自己设为父菜单");
        }
        
        menu.setUpdateTime(LocalDateTime.now());
        menuMapper.update(menu);
    }
    
    /**
     * 删除菜单
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        // 检查是否有子菜单
        QueryWrapper query = QueryWrapper.create()
                .from(SysMenu.class)
                .where(SYS_MENU.PARENT_ID.eq(menuId));
        Long childCount = menuMapper.selectCountByQuery(query);
        if (childCount != null && childCount > 0) {
            throw new BusinessException("该菜单下有 " + childCount + " 个子菜单，无法删除");
        }
        
        // 检查是否有角色使用该菜单
        Long roleCount = roleMenuMapper.countByMenuId(menuId);
        if (roleCount != null && roleCount > 0) {
            throw new BusinessException("该菜单已分配给 " + roleCount + " 个角色，无法删除");
        }
        
        // 删除菜单
        menuMapper.deleteById(menuId);
    }
    
    /**
     * 获取菜单详情
     */
    public SysMenu getMenuDetail(Long menuId) {
        return menuMapper.selectOneById(menuId);
    }
    
    /**
     * 构建菜单树
     */
    private List<SysMenu> buildTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> result = new ArrayList<>();
        
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                menu.setChildren(buildTree(menus, menu.getId()));
                result.add(menu);
            }
        }
        
        return result;
    }
    
    /**
     * 获取菜单选择树（用于角色分配权限）
     */
    public List<SysMenu> getMenuSelectTree() {
        QueryWrapper query = QueryWrapper.create()
                .from(SysMenu.class)
                .orderBy(SYS_MENU.PARENT_ID.asc())
                .orderBy(SYS_MENU.ORDER_NUM.asc());
        
        List<SysMenu> allMenus = menuMapper.selectListByQuery(query);
        return buildTree(allMenus, 0L);
    }
}
