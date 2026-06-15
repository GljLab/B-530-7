package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.UserFloorPermission;
import com.example.permission.service.UserFloorPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel/floorPermission")
public class UserFloorPermissionController {

    @Autowired
    private UserFloorPermissionService userFloorPermissionService;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('hotel:floorPerm:list')")
    public Result<List<Map<String, Object>>> getUsersWithFloorPermission() {
        List<Map<String, Object>> users = userFloorPermissionService.getUsersWithFloorPermission();
        return Result.success(users);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('hotel:floorPerm:query')")
    public Result<List<UserFloorPermission>> getUserPermissions(@PathVariable Long userId) {
        List<UserFloorPermission> permissions = userFloorPermissionService.getPermissionsByUserId(userId);
        return Result.success(permissions);
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('hotel:floorPerm:edit')")
    public Result<Void> saveUserPermissions(@PathVariable Long userId,
                                             @RequestBody Map<String, List<Long>> request) {
        List<Long> floorIds = request.get("floorIds");
        userFloorPermissionService.savePermissions(userId, floorIds);
        return Result.success("保存成功", null);
    }

    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('hotel:floorPerm:delete')")
    public Result<Void> clearUserPermissions(@PathVariable Long userId) {
        userFloorPermissionService.clearPermissions(userId);
        return Result.success("清空成功", null);
    }

    @GetMapping("/user/{userId}/summary")
    @PreAuthorize("hasAuthority('hotel:floorPerm:query')")
    public Result<Map<String, Object>> getUserPermissionSummary(@PathVariable Long userId) {
        Map<String, Object> summary = userFloorPermissionService.getUserPermissionSummary(userId);
        return Result.success(summary);
    }
}
