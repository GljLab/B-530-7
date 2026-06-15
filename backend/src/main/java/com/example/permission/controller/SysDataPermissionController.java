package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.SysDataPermission;
import com.example.permission.service.SysDataPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据权限管理控制器
 */
@RestController
@RequestMapping("/api/system/dataPermission")
public class SysDataPermissionController {
    
    @Autowired
    private SysDataPermissionService dataPermissionService;
    
    /**
     * 分页查询数据权限列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:dataPerm:list')")
    public Result<PageResult<SysDataPermission>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long roleId) {
        PageResult<SysDataPermission> result = dataPermissionService.pageList(pageNum, pageSize, roleId);
        return Result.success(result);
    }
    
    /**
     * 获取所有数据权限
     */
    @GetMapping("/all")
    public Result<List<SysDataPermission>> listAll() {
        List<SysDataPermission> permissions = dataPermissionService.listAll();
        return Result.success(permissions);
    }
    
    /**
     * 根据角色ID获取数据权限
     */
    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('system:dataPerm:query')")
    public Result<SysDataPermission> getByRoleId(@PathVariable Long roleId) {
        SysDataPermission permission = dataPermissionService.getByRoleId(roleId);
        return Result.success(permission);
    }
    
    /**
     * 保存或更新数据权限
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:dataPerm:edit')")
    public Result<Void> saveOrUpdate(@RequestBody @Valid SysDataPermission dataPermission) {
        dataPermissionService.saveOrUpdate(dataPermission);
        return Result.success("保存成功", null);
    }
    
    /**
     * 删除数据权限
     */
    @DeleteMapping("/role/{roleId}")
    @PreAuthorize("hasAuthority('system:dataPerm:delete')")
    public Result<Void> deleteByRoleId(@PathVariable Long roleId) {
        dataPermissionService.deleteByRoleId(roleId);
        return Result.success("删除成功", null);
    }
    
    /**
     * 获取数据权限范围类型选项
     */
    @GetMapping("/scopeTypes")
    public Result<List<Map<String, Object>>> getScopeTypes() {
        List<Map<String, Object>> types = new java.util.ArrayList<>();
        
        Map<String, Object> type1 = new HashMap<>();
        type1.put("value", 1);
        type1.put("label", "全部数据");
        types.add(type1);
        
        Map<String, Object> type2 = new HashMap<>();
        type2.put("value", 2);
        type2.put("label", "自定义数据");
        types.add(type2);
        
        Map<String, Object> type3 = new HashMap<>();
        type3.put("value", 3);
        type3.put("label", "本部门数据");
        types.add(type3);
        
        Map<String, Object> type4 = new HashMap<>();
        type4.put("value", 4);
        type4.put("label", "本部门及以下数据");
        types.add(type4);
        
        Map<String, Object> type5 = new HashMap<>();
        type5.put("value", 5);
        type5.put("label", "仅本人数据");
        types.add(type5);
        
        return Result.success(types);
    }
}
