package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.SysMenu;
import com.example.permission.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 菜单管理控制器
 */
@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {
    
    @Autowired
    private SysMenuService menuService;
    
    /**
     * 获取菜单树形结构
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<SysMenu>> getMenuTree() {
        List<SysMenu> menuTree = menuService.getMenuTree();
        return Result.success(menuTree);
    }
    
    /**
     * 获取所有菜单列表（不构建树形）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<SysMenu>> list() {
        List<SysMenu> menus = menuService.listAll();
        return Result.success(menus);
    }
    
    /**
     * 获取菜单选择树（用于角色分配权限）
     */
    @GetMapping("/selectTree")
    public Result<List<SysMenu>> getMenuSelectTree() {
        List<SysMenu> menuTree = menuService.getMenuSelectTree();
        return Result.success(menuTree);
    }
    
    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:query')")
    public Result<SysMenu> getInfo(@PathVariable Long id) {
        SysMenu menu = menuService.getMenuDetail(id);
        return Result.success(menu);
    }
    
    /**
     * 新增菜单
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<Void> add(@RequestBody @Valid SysMenu menu) {
        menuService.addMenu(menu);
        return Result.success("新增成功", null);
    }
    
    /**
     * 修改菜单
     */
    @PutMapping
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> update(@RequestBody @Valid SysMenu menu) {
        menuService.updateMenu(menu);
        return Result.success("修改成功", null);
    }
    
    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success("删除成功", null);
    }
}
