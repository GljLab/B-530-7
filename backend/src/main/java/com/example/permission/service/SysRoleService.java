package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.SysRole;
import com.example.permission.entity.SysRoleMenu;
import com.example.permission.mapper.SysMenuMapper;
import com.example.permission.mapper.SysRoleMapper;
import com.example.permission.mapper.SysRoleMenuMapper;
import com.example.permission.mapper.SysUserRoleMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.SysRoleTableDef.SYS_ROLE;

/**
 * 角色服务
 */
@Service
public class SysRoleService {
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private SysRoleMenuMapper roleMenuMapper;
    
    @Autowired
    private SysMenuMapper menuMapper;
    
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    /**
     * 分页查询角色
     */
    public PageResult<SysRole> pageList(Integer pageNum, Integer pageSize, String roleName, Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .from(SysRole.class);
        
        if (StringUtils.hasText(roleName)) {
            query.and(SYS_ROLE.ROLE_NAME.like(roleName));
        }
        if (status != null) {
            query.and(SYS_ROLE.STATUS.eq(status));
        }
        query.orderBy(SYS_ROLE.ORDER_NUM.asc());
        
        Page<SysRole> page = roleMapper.paginate(Page.of(pageNum, pageSize), query);
        
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }
    
    /**
     * 获取所有角色
     */
    public List<SysRole> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(SysRole.class)
                .where(SYS_ROLE.STATUS.eq(1))
                .orderBy(SYS_ROLE.ORDER_NUM.asc());
        return roleMapper.selectListByQuery(query);
    }
    
    /**
     * 新增角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void addRole(SysRole role) {
        // 检查角色名称是否已存在
        QueryWrapper query = QueryWrapper.create()
                .from(SysRole.class)
                .where(SYS_ROLE.ROLE_NAME.eq(role.getRoleName()));
        if (roleMapper.selectCountByQuery(query) > 0) {
            throw new BusinessException("角色名称已存在");
        }
        
        // 检查角色标识是否已存在
        query = QueryWrapper.create()
                .from(SysRole.class)
                .where(SYS_ROLE.ROLE_KEY.eq(role.getRoleKey()));
        if (roleMapper.selectCountByQuery(query) > 0) {
            throw new BusinessException("角色标识已存在");
        }
        
        role.setStatus(role.getStatus() != null ? role.getStatus() : 1);
        role.setDeleted(0);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
        
        // 添加角色菜单关联
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            for (Long menuId : role.getMenuIds()) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(role.getId());
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }
    }
    
    /**
     * 更新角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(SysRole role) {
        SysRole existRole = roleMapper.selectOneById(role.getId());
        if (existRole == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 检查角色名称是否重复
        if (StringUtils.hasText(role.getRoleName()) && 
            !role.getRoleName().equals(existRole.getRoleName())) {
            QueryWrapper query = QueryWrapper.create()
                    .from(SysRole.class)
                    .where(SYS_ROLE.ROLE_NAME.eq(role.getRoleName()));
            if (roleMapper.selectCountByQuery(query) > 0) {
                throw new BusinessException("角色名称已存在");
            }
        }
        
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.update(role);
        
        // 更新角色菜单关联
        if (role.getMenuIds() != null) {
            roleMenuMapper.deleteByRoleId(role.getId());
            for (Long menuId : role.getMenuIds()) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setRoleId(role.getId());
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }
    }
    
    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long roleId) {
        SysRole role = roleMapper.selectOneById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 检查是否有用户分配了该角色
        Long userCount = userRoleMapper.countByRoleId(roleId);
        if (userCount != null && userCount > 0) {
            throw new BusinessException("该角色已分配给 " + userCount + " 个用户，无法删除");
        }
        
        // 删除角色菜单关联
        roleMenuMapper.deleteByRoleId(roleId);
        
        // 逻辑删除角色
        role.setDeleted(1);
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.update(role);
    }
    
    /**
     * 修改角色状态
     */
    public void updateStatus(Long roleId, Integer status) {
        SysRole role = roleMapper.selectOneById(roleId);
        if (role != null) {
            role.setStatus(status);
            role.setUpdateTime(LocalDateTime.now());
            roleMapper.update(role);
        }
    }
    
    /**
     * 获取角色详情（包含菜单）
     */
    public SysRole getRoleDetail(Long roleId) {
        SysRole role = roleMapper.selectOneById(roleId);
        if (role != null) {
            role.setMenuIds(menuMapper.selectMenuIdsByRoleId(roleId));
        }
        return role;
    }
}
