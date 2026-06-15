package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.SysDataPermission;
import com.example.permission.mapper.SysDataPermissionMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据权限服务
 */
@Service
public class SysDataPermissionService {
    
    @Autowired
    private SysDataPermissionMapper dataPermissionMapper;
    
    /**
     * 分页查询数据权限
     */
    public PageResult<SysDataPermission> pageList(Integer pageNum, Integer pageSize, Long roleId) {
        QueryWrapper query = QueryWrapper.create().from(SysDataPermission.class);
        
        if (roleId != null) {
            query.where("role_id = {0}", roleId);
        }
        query.orderBy("create_time", false);
        
        Page<SysDataPermission> page = dataPermissionMapper.paginate(Page.of(pageNum, pageSize), query);
        
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }
    
    /**
     * 获取所有数据权限
     */
    public List<SysDataPermission> listAll() {
        return dataPermissionMapper.selectAll();
    }
    
    /**
     * 根据角色ID获取数据权限
     */
    public SysDataPermission getByRoleId(Long roleId) {
        return dataPermissionMapper.selectByRoleId(roleId);
    }
    
    /**
     * 新增或更新数据权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(SysDataPermission dataPermission) {
        if (dataPermission.getRoleId() == null) {
            throw new BusinessException("角色ID不能为空");
        }
        
        SysDataPermission existing = dataPermissionMapper.selectByRoleId(dataPermission.getRoleId());
        
        if (existing != null) {
            dataPermission.setId(existing.getId());
            dataPermission.setUpdateTime(LocalDateTime.now());
            dataPermissionMapper.update(dataPermission);
        } else {
            dataPermission.setCreateTime(LocalDateTime.now());
            dataPermission.setUpdateTime(LocalDateTime.now());
            dataPermissionMapper.insert(dataPermission);
        }
    }
    
    /**
     * 删除数据权限
     */
    public void deleteByRoleId(Long roleId) {
        dataPermissionMapper.deleteByRoleId(roleId);
    }
    
    /**
     * 获取权限范围类型名称
     */
    public String getScopeTypeName(Integer scopeType) {
        if (scopeType == null) return "未设置";
        switch (scopeType) {
            case 1: return "全部数据";
            case 2: return "自定义数据";
            case 3: return "本部门数据";
            case 4: return "本部门及以下数据";
            case 5: return "仅本人数据";
            default: return "未知";
        }
    }
}
