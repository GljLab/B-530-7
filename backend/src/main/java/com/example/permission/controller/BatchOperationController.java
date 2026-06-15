package com.example.permission.controller;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.BatchOperation;
import com.example.permission.entity.BatchOperationDetail;
import com.example.permission.service.BatchOperationService;
import com.example.permission.service.SysUserService;
import com.example.permission.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
public class BatchOperationController {

    @Autowired
    private BatchOperationService batchOperationService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/room/status")
    @PreAuthorize("hasAuthority('hotel:room:batch:status')")
    public Result<Map<String, Object>> batchUpdateStatus(@RequestBody Map<String, Object> params,
                                                          @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        String username = sysUserService.getUserDetail(userId).getUsername();

        List<?> rawIds = (List<?>) params.get("roomIds");
        List<Long> roomIds = new ArrayList<>();
        if (rawIds != null) {
            for (Object id : rawIds) {
                roomIds.add(Long.valueOf(id.toString()));
            }
        }
        Integer targetStatus = params.get("targetStatus") != null ? Integer.valueOf(params.get("targetStatus").toString()) : null;
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";

        if (roomIds.isEmpty()) {
            throw new BusinessException("请选择要操作的房间");
        }
        if (targetStatus == null) {
            throw new BusinessException("请选择目标状态");
        }
        if (reason.trim().isEmpty()) {
            throw new BusinessException("请填写操作原因");
        }

        Map<String, Object> result = batchOperationService.batchUpdateStatus(roomIds, targetStatus, reason, userId, username);
        return Result.success(result);
    }

    @PostMapping("/room/attr")
    @PreAuthorize("hasAuthority('hotel:room:batch:attr')")
    public Result<Map<String, Object>> batchUpdateAttr(@RequestBody Map<String, Object> params,
                                                        @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        String username = sysUserService.getUserDetail(userId).getUsername();

        List<?> rawIds = (List<?>) params.get("roomIds");
        List<Long> roomIds = new ArrayList<>();
        if (rawIds != null) {
            for (Object id : rawIds) {
                roomIds.add(Long.valueOf(id.toString()));
            }
        }
        String attrType = params.get("attrType") != null ? params.get("attrType").toString() : null;
        Integer attrMode = params.get("attrMode") != null ? Integer.valueOf(params.get("attrMode").toString()) : null;
        String attrValue = params.get("attrValue") != null ? params.get("attrValue").toString() : null;
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";

        if (roomIds.isEmpty()) {
            throw new BusinessException("请选择要操作的房间");
        }
        if (attrType == null || attrType.trim().isEmpty()) {
            throw new BusinessException("请选择属性类型");
        }
        if (attrMode == null) {
            throw new BusinessException("请选择操作模式");
        }
        if (reason.trim().isEmpty()) {
            throw new BusinessException("请填写操作原因");
        }

        Map<String, Object> result = batchOperationService.batchUpdateAttr(roomIds, attrType, attrMode, attrValue, reason, userId, username);
        return Result.success(result);
    }

    @PostMapping("/room/delete")
    @PreAuthorize("hasAuthority('hotel:room:batch:delete')")
    public Result<Map<String, Object>> batchDeleteRooms(@RequestBody Map<String, Object> params,
                                                         @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        String username = sysUserService.getUserDetail(userId).getUsername();

        List<?> rawIds = (List<?>) params.get("roomIds");
        List<Long> roomIds = new ArrayList<>();
        if (rawIds != null) {
            for (Object id : rawIds) {
                roomIds.add(Long.valueOf(id.toString()));
            }
        }
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";

        if (roomIds.isEmpty()) {
            throw new BusinessException("请选择要操作的房间");
        }
        if (reason.trim().isEmpty()) {
            throw new BusinessException("请填写操作原因");
        }

        Map<String, Object> result = batchOperationService.batchDeleteRooms(roomIds, reason, userId, username);
        return Result.success(result);
    }

    @GetMapping("/{batchNo}")
    @PreAuthorize("hasAuthority('hotel:room:batch:query')")
    public Result<BatchOperation> getBatchByNo(@PathVariable String batchNo) {
        BatchOperation batch = batchOperationService.getBatchByNo(batchNo);
        return Result.success(batch);
    }

    @GetMapping("/{batchNo}/details")
    @PreAuthorize("hasAuthority('hotel:room:batch:query')")
    public Result<List<BatchOperationDetail>> getBatchDetails(@PathVariable String batchNo) {
        List<BatchOperationDetail> details = batchOperationService.getBatchDetails(batchNo);
        return Result.success(details);
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('hotel:room:batch:query')")
    public Result<PageResult<BatchOperation>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String batchNo,
            @RequestParam(required = false) Integer operationType,
            @RequestParam(required = false) Long operatorId) {
        PageResult<BatchOperation> result = batchOperationService.pageList(pageNum, pageSize, batchNo, operationType, operatorId);
        return Result.success(result);
    }
}
