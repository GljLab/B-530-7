package com.example.permission.controller;

import com.example.permission.common.BusinessException;
import com.example.permission.common.Result;
import com.example.permission.entity.MaintenanceAssignRule;
import com.example.permission.entity.MaintenanceStaffSkill;
import com.example.permission.security.LoginUser;
import com.example.permission.service.MaintenanceAssignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance/assign")
public class MaintenanceAssignController {

    @Autowired
    private MaintenanceAssignService maintenanceAssignService;

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        throw new BusinessException("用户未登录");
    }

    @PostMapping("/auto/{orderId}")
    @PreAuthorize("hasAuthority('maintenance:order:assign')")
    public Result<Map<String, Object>> autoAssign(@PathVariable Long orderId) {
        LoginUser user = getCurrentUser();
        String operatorName = user.getUser().getNickname() != null ?
                user.getUser().getNickname() : user.getUsername();
        return Result.success(maintenanceAssignService.autoAssign(orderId, user.getUserId(), operatorName));
    }

    @PostMapping("/batchAuto")
    @PreAuthorize("hasAuthority('maintenance:order:batch:assign')")
    public Result<Map<String, Object>> batchAutoAssign(@RequestBody Map<String, Object> params) {
        LoginUser user = getCurrentUser();
        String operatorName = user.getUser().getNickname() != null ?
                user.getUser().getNickname() : user.getUsername();
        List<Long> orderIds = (List<Long>) params.get("orderIds");
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException("请选择要分配的维护单");
        }
        return Result.success(maintenanceAssignService.batchAutoAssign(orderIds, user.getUserId(), operatorName));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('maintenance:order:batch:assign')")
    public Result<Map<String, Object>> batchAssign(@RequestBody Map<String, Object> params) {
        LoginUser user = getCurrentUser();
        String operatorName = user.getUser().getNickname() != null ?
                user.getUser().getNickname() : user.getUsername();
        List<Long> orderIds = (List<Long>) params.get("orderIds");
        Long targetUserId = Long.valueOf(params.get("targetUserId").toString());
        if (orderIds == null || orderIds.isEmpty()) {
            throw new BusinessException("请选择要分配的维护单");
        }
        if (targetUserId == null) {
            throw new BusinessException("请选择目标维修人员");
        }
        return Result.success(maintenanceAssignService.batchAssign(orderIds, targetUserId, user.getUserId(), operatorName));
    }

    @GetMapping("/staffWorkload")
    @PreAuthorize("hasAuthority('maintenance:order:list')")
    public Result<List<Map<String, Object>>> getStaffWorkload() {
        return Result.success(maintenanceAssignService.getStaffWorkload());
    }

    @GetMapping("/staffList")
    @PreAuthorize("hasAuthority('maintenance:order:list')")
    public Result<List<Map<String, Object>>> getStaffWithSkills() {
        return Result.success(maintenanceAssignService.getStaffWithSkills());
    }

    @GetMapping("/rules")
    @PreAuthorize("hasAuthority('maintenance:order:list')")
    public Result<List<MaintenanceAssignRule>> listRules() {
        return Result.success(maintenanceAssignService.listRules());
    }

    @PostMapping("/rule")
    @PreAuthorize("hasAuthority('maintenance:order:assign')")
    public Result<MaintenanceAssignRule> saveRule(@RequestBody MaintenanceAssignRule rule) {
        return Result.success(maintenanceAssignService.saveRule(rule));
    }

    @DeleteMapping("/rule/{id}")
    @PreAuthorize("hasAuthority('maintenance:order:assign')")
    public Result<Void> deleteRule(@PathVariable Long id) {
        maintenanceAssignService.deleteRule(id);
        return Result.success();
    }

    @PutMapping("/rule/{id}/status")
    @PreAuthorize("hasAuthority('maintenance:order:assign')")
    public Result<Void> updateRuleStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = Integer.valueOf(params.get("status").toString());
        maintenanceAssignService.updateRuleStatus(id, status);
        return Result.success();
    }

    @GetMapping("/staff/{userId}/skills")
    @PreAuthorize("hasAuthority('maintenance:order:list')")
    public Result<List<MaintenanceStaffSkill>> listStaffSkills(@PathVariable Long userId) {
        return Result.success(maintenanceAssignService.listStaffSkills(userId));
    }

    @PostMapping("/staff/skill")
    @PreAuthorize("hasAuthority('maintenance:order:assign')")
    public Result<MaintenanceStaffSkill> saveStaffSkill(@RequestBody MaintenanceStaffSkill skill) {
        return Result.success(maintenanceAssignService.saveStaffSkill(skill));
    }

    @DeleteMapping("/staff/skill/{id}")
    @PreAuthorize("hasAuthority('maintenance:order:assign')")
    public Result<Void> deleteStaffSkill(@PathVariable Long id) {
        maintenanceAssignService.deleteStaffSkill(id);
        return Result.success();
    }
}
