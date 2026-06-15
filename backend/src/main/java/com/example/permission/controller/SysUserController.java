package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.SysUser;
import com.example.permission.service.SysUserService;
import com.example.permission.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/system/user")
public class SysUserController {
    
    @Autowired
    private SysUserService userService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    /**
     * 分页查询用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status) {
        PageResult<SysUser> result = userService.pageList(pageNum, pageSize, username, status);
        return Result.success(result);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:query')")
    public Result<SysUser> getInfo(@PathVariable Long id) {
        SysUser user = userService.getUserDetail(id);
        return Result.success(user);
    }
    
    /**
     * 新增用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Void> add(@RequestBody @Valid SysUser user) {
        userService.addUser(user);
        return Result.success("新增成功", null);
    }
    
    /**
     * 修改用户
     */
    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> update(@RequestBody @Valid SysUser user) {
        userService.updateUser(user);
        return Result.success("修改成功", null);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功", null);
    }
    
    /**
     * 修改用户状态
     */
    @PutMapping("/status")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> updateStatus(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        userService.updateStatus(userId, status);
        return Result.success("状态修改成功", null);
    }
    
    /**
     * 重置密码
     */
    @PutMapping("/resetPwd")
    @PreAuthorize("hasAuthority('system:user:resetPwd')")
    public Result<Void> resetPassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("id").toString());
        String password = params.get("password").toString();
        userService.resetPassword(userId, password);
        return Result.success("密码重置成功", null);
    }
    
    /**
     * 修改密码（当前用户）
     */
    @PutMapping("/profile/password")
    public Result<Void> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> params) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }
    
    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody SysUser user) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        user.setId(userId);
        user.setPassword(null); // 不允许通过此接口修改密码
        user.setRoleIds(null); // 不允许通过此接口修改角色
        userService.updateProfile(user);
        return Result.success("个人信息更新成功", null);
    }
}
