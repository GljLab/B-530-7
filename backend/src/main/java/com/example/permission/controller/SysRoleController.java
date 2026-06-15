package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.SysRole;
import com.example.permission.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/system/role")
public class SysRoleController {
    
    @Autowired
    private SysRoleService roleService;
    
    /**
     * 分页查询角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<PageResult<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        PageResult<SysRole> result = roleService.pageList(pageNum, pageSize, roleName, status);
        return Result.success(result);
    }
    
    /**
     * 获取所有角色（不分页）
     */
    @GetMapping("/listAll")
    public Result<List<SysRole>> listAll() {
        List<SysRole> roles = roleService.listAll();
        return Result.success(roles);
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:query')")
    public Result<SysRole> getInfo(@PathVariable Long id) {
        SysRole role = roleService.getRoleDetail(id);
        return Result.success(role);
    }
    
    /**
     * 新增角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<Void> add(@RequestBody @Valid SysRole role) {
        roleService.addRole(role);
        return Result.success("新增成功", null);
    }
    
    /**
     * 修改角色
     */
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> update(@RequestBody @Valid SysRole role) {
        roleService.updateRole(role);
        return Result.success("修改成功", null);
    }
    
    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("删除成功", null);
    }
    
    /**
     * 修改角色状态
     */
    @PutMapping("/status")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long roleId = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        roleService.updateStatus(roleId, status);
        return Result.success("状态修改成功", null);
    }
}
