package com.example.permission.controller;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.Room;
import com.example.permission.entity.RoomStatusLog;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ExportService;
import com.example.permission.service.RoomService;
import com.example.permission.service.RoomStatusLogService;
import com.example.permission.service.SysUserService;
import com.example.permission.service.UserFloorPermissionService;
import com.example.permission.utils.JwtUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomStatusLogService roomStatusLogService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ExportService exportService;

    @Autowired
    private UserFloorPermissionService userFloorPermissionService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('hotel:room:list')")
    public Result<PageResult<Room>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) List<Integer> status,
            @RequestParam(required = false) List<String> orientations,
            @RequestParam(required = false) List<String> viewTypes,
            @RequestParam(required = false) List<String> specialTags) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUserId();
        PageResult<Room> result = roomService.pageListWithPermission(pageNum, pageSize, roomNumber, buildingId, floorId,
                roomTypeId, status, orientations, viewTypes, specialTags, userId);
        return Result.success(result);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('hotel:room:list')")
    public Result<List<Room>> list(
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long floorId,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) List<Integer> status,
            @RequestParam(required = false) List<String> orientations,
            @RequestParam(required = false) List<String> viewTypes,
            @RequestParam(required = false) List<String> specialTags) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = loginUser.getUserId();
        PageResult<Room> page = roomService.pageListWithPermission(1, Integer.MAX_VALUE, roomNumber, buildingId, floorId,
                roomTypeId, status, orientations, viewTypes, specialTags, userId);
        return Result.success(page.getList());
    }

    @GetMapping("/floor/{floorId}")
    @PreAuthorize("hasAuthority('hotel:room:list')")
    public Result<List<Room>> listByFloorId(@PathVariable Long floorId) {
        List<Room> rooms = roomService.listByFloorId(floorId);
        return Result.success(rooms);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('hotel:room:query')")
    public Result<Room> getById(@PathVariable Long id) {
        Room room = roomService.getById(id);
        return Result.success(room);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('hotel:room:add')")
    public Result<Void> add(@RequestBody Room room) {
        roomService.add(room);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('hotel:room:edit')")
    public Result<Void> update(@RequestBody Room room) {
        roomService.update(room);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hotel:room:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return Result.success();
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAuthority('hotel:room:batch:add')")
    public Result<Map<String, Object>> batchCreate(@RequestBody Map<String, Object> params) {
        Long buildingId = Long.valueOf(params.get("buildingId").toString());
        Long floorId = Long.valueOf(params.get("floorId").toString());
        Long roomTypeId = Long.valueOf(params.get("roomTypeId").toString());
        String numberPrefix = params.get("numberPrefix").toString();
        Integer startNum = Integer.valueOf(params.get("startNum").toString());
        Integer endNum = Integer.valueOf(params.get("endNum").toString());
        String orientation = params.get("orientation") != null ? params.get("orientation").toString() : null;
        String viewType = params.get("viewType") != null ? params.get("viewType").toString() : null;
        Map<String, Object> result = roomService.batchCreate(buildingId, floorId, roomTypeId, numberPrefix, startNum, endNum, orientation, viewType);
        return Result.success(result);
    }

    @PostMapping("/copy/{sourceRoomId}")
    @PreAuthorize("hasAuthority('hotel:room:copy')")
    public Result<Room> copyRoom(@PathVariable Long sourceRoomId,
                                  @RequestBody Room newRoom,
                                  @RequestHeader("Authorization") String token) {
        if (newRoom.getRoomNumber() == null || newRoom.getRoomNumber().trim().isEmpty()) {
            throw new BusinessException("房间号不能为空");
        }
        Room copied = roomService.copyRoom(sourceRoomId, newRoom);
        return Result.success(copied);
    }

    @PostMapping("/applyTemplate")
    @PreAuthorize("hasAuthority('hotel:room:template')")
    public Result<Map<String, Object>> applyTemplate(@RequestBody Map<String, Object> params,
                                                      @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        String username = sysUserService.getUserDetail(userId).getUsername();

        Long templateRoomId = Long.valueOf(params.get("templateRoomId").toString());
        List<?> rawIds = (List<?>) params.get("targetRoomIds");
        List<Long> targetRoomIds = new ArrayList<>();
        if (rawIds != null) {
            for (Object id : rawIds) {
                targetRoomIds.add(Long.valueOf(id.toString()));
            }
        }
        Boolean applyOrientation = params.get("applyOrientation") != null ? (Boolean) params.get("applyOrientation") : false;
        Boolean applyViewType = params.get("applyViewType") != null ? (Boolean) params.get("applyViewType") : false;
        Boolean applyLocationFeatures = params.get("applyLocationFeatures") != null ? (Boolean) params.get("applyLocationFeatures") : false;
        Boolean applySpecialTags = params.get("applySpecialTags") != null ? (Boolean) params.get("applySpecialTags") : false;
        String reason = params.get("reason") != null ? params.get("reason").toString() : "";

        if (targetRoomIds == null || targetRoomIds.isEmpty()) {
            throw new BusinessException("请选择目标房间");
        }
        if (reason.trim().isEmpty()) {
            throw new BusinessException("请填写操作原因");
        }
        if (!applyOrientation && !applyViewType && !applyLocationFeatures && !applySpecialTags) {
            throw new BusinessException("请至少选择一项要应用的属性");
        }

        Map<String, Object> result = roomService.applyTemplate(templateRoomId, targetRoomIds,
                applyOrientation, applyViewType, applyLocationFeatures, applySpecialTags,
                userId, username, reason);
        return Result.success(result);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('hotel:room:status:edit')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestHeader("Authorization") String token,
                                     @RequestBody Map<String, Object> params) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        String username = sysUserService.getUserDetail(userId).getUsername();
        Integer status = Integer.valueOf(params.get("status").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        roomService.updateStatus(id, status, userId, username, remark);
        return Result.success();
    }

    @GetMapping("/{roomId}/logs")
    @PreAuthorize("hasAuthority('hotel:room:query')")
    public Result<List<RoomStatusLog>> getStatusLogs(@PathVariable Long roomId) {
        List<RoomStatusLog> logs = roomStatusLogService.listByRoomId(roomId);
        return Result.success(logs);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('hotel:room:export')")
    public ResponseEntity<byte[]> exportRooms(@RequestBody Map<String, Object> params,
                                               @RequestHeader("Authorization") String token) throws Exception {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        String username = sysUserService.getUserDetail(userId).getUsername();

        String scope = params.get("scope") != null ? params.get("scope").toString() : "current";

        String roomNumber = params.get("roomNumber") != null ? params.get("roomNumber").toString() : null;
        Long buildingId = params.get("buildingId") != null ? Long.valueOf(params.get("buildingId").toString()) : null;
        Long floorId = params.get("floorId") != null ? Long.valueOf(params.get("floorId").toString()) : null;
        Long roomTypeId = params.get("roomTypeId") != null ? Long.valueOf(params.get("roomTypeId").toString()) : null;
        List<Integer> status = (List<Integer>) params.get("statusList");
        List<String> orientations = (List<String>) params.get("orientations");
        List<String> viewTypes = (List<String>) params.get("viewTypes");
        List<String> specialTags = (List<String>) params.get("specialTags");

        List<String> exportFields = (List<String>) params.get("exportFields");
        if (exportFields == null || exportFields.isEmpty()) {
            throw new BusinessException("请选择要导出的字段");
        }

        List<Room> rooms;
        String filterDesc;
        if ("all".equals(scope)) {
            rooms = roomService.listAllForExport(null, null, null, null, null, null, null, null);
            filterDesc = "全部房间";
        } else if ("custom".equals(scope)) {
            rooms = roomService.listAllForExport(roomNumber, buildingId, floorId, roomTypeId, status, orientations, viewTypes, specialTags);
            filterDesc = buildFilterDesc(roomNumber, buildingId, floorId, roomTypeId, status, orientations, viewTypes, specialTags);
        } else {
            rooms = roomService.listAllForExport(roomNumber, buildingId, floorId, roomTypeId, status, orientations, viewTypes, specialTags);
            filterDesc = buildFilterDesc(roomNumber, buildingId, floorId, roomTypeId, status, orientations, viewTypes, specialTags);
        }

        boolean canSeePrice = exportFields.contains("basePrice");
        if (canSeePrice) {
            // 价格权限由前端控制，后端这里保持导出逻辑
        }

        byte[] excelData = exportService.exportRooms(rooms, exportFields, username, filterDesc);
        String fileName = exportService.generateFileName();
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    private String buildFilterDesc(String roomNumber, Long buildingId, Long floorId, Long roomTypeId,
                                    List<Integer> status, List<String> orientations,
                                    List<String> viewTypes, List<String> specialTags) {
        StringBuilder sb = new StringBuilder();
        if (roomNumber != null && !roomNumber.isEmpty()) sb.append("房号:").append(roomNumber).append("; ");
        if (buildingId != null) sb.append("楼栋ID:").append(buildingId).append("; ");
        if (floorId != null) sb.append("楼层ID:").append(floorId).append("; ");
        if (roomTypeId != null) sb.append("房型ID:").append(roomTypeId).append("; ");
        if (status != null && !status.isEmpty()) sb.append("状态:").append(status).append("; ");
        if (orientations != null && !orientations.isEmpty()) sb.append("朝向:").append(orientations).append("; ");
        if (viewTypes != null && !viewTypes.isEmpty()) sb.append("景观:").append(viewTypes).append("; ");
        if (specialTags != null && !specialTags.isEmpty()) sb.append("特殊标识:").append(specialTags).append("; ");
        return sb.length() == 0 ? "全部房间" : sb.toString().trim();
    }
}
