package com.example.permission.controller;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.MaintenanceOrder;
import com.example.permission.entity.SysUser;
import com.example.permission.security.LoginUser;
import com.example.permission.service.ExportService;
import com.example.permission.service.MaintenanceOrderService;
import com.example.permission.service.RoomService;
import com.example.permission.service.SysUserService;
import com.example.permission.service.UserFloorPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance/order")
public class MaintenanceOrderController {

    @Autowired
    private MaintenanceOrderService maintenanceOrderService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ExportService exportService;

    @Autowired
    private UserFloorPermissionService userFloorPermissionService;

    @Autowired
    private RoomService roomService;

    private LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        throw new BusinessException("用户未登录");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('maintenance:order:list')")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.success(maintenanceOrderService.getDashboardStats());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('maintenance:order:list')")
    public Result<PageResult<MaintenanceOrder>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) List<Integer> statusList,
            @RequestParam(required = false) List<Integer> typeList,
            @RequestParam(required = false) List<Integer> priorityList,
            @RequestParam(required = false) Long assignedUserId) {
        LoginUser user = getCurrentUser();
        boolean canSeeAll = user.getPermissions().contains("maintenance:order:list")
                && !user.getPermissions().contains("maintenance:order:selfOnly");
        if (user.getPermissions().contains("maintenance:order:selfOnly") ||
                (user.getUser() != null && user.getUser().getRoles() != null &&
                        user.getUser().getRoles().stream().anyMatch(r -> "maintenance_staff".equals(r.getRoleKey())))) {
            canSeeAll = false;
        }
        PageResult<MaintenanceOrder> result = maintenanceOrderService.pageListWithPermission(
                pageNum, pageSize, orderNo, roomNumber, statusList, typeList,
                priorityList, assignedUserId, user.getUserId(), canSeeAll, user.getUserId());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('maintenance:order:query')")
    public Result<MaintenanceOrder> getById(@PathVariable Long id) {
        return Result.success(maintenanceOrderService.getById(id));
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAuthority('maintenance:order:query')")
    public Result<List<MaintenanceOrder>> listByRoomId(@PathVariable Long roomId) {
        return Result.success(maintenanceOrderService.listByRoomId(roomId));
    }

    @GetMapping("/room/{roomId}/stats")
    @PreAuthorize("hasAuthority('maintenance:order:query')")
    public Result<Map<String, Object>> getRoomStats(@PathVariable Long roomId) {
        return Result.success(maintenanceOrderService.getRoomMaintenanceStats(roomId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('maintenance:order:add')")
    public Result<MaintenanceOrder> create(@RequestBody Map<String, Object> params,
                                            HttpServletRequest request) {
        LoginUser user = getCurrentUser();
        SysUser sysUser = sysUserService.getUserDetail(user.getUserId());

        MaintenanceOrder order = new MaintenanceOrder();
        order.setRoomId(Long.valueOf(params.get("roomId").toString()));
        order.setMaintenanceType(Integer.valueOf(params.get("maintenanceType").toString()));
        order.setPriority(Integer.valueOf(params.get("priority").toString()));
        order.setProblemDescription(params.get("problemDescription") != null ? params.get("problemDescription").toString() : "");
        if (params.get("expectedFinishTime") != null && !params.get("expectedFinishTime").toString().isEmpty()) {
            String dateTimeStr = params.get("expectedFinishTime").toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            order.setExpectedFinishTime(LocalDateTime.parse(dateTimeStr, formatter));
        }
        order.setSpecialRemark(params.get("specialRemark") != null ? params.get("specialRemark").toString() : null);

        List<String> problemPhotos = params.get("problemPhotos") != null
                ? (List<String>) params.get("problemPhotos") : null;

        String roleKey = sysUser.getRoles() != null && !sysUser.getRoles().isEmpty()
                ? sysUser.getRoles().get(0).getRoleKey() : "";

        MaintenanceOrder created = maintenanceOrderService.create(order, problemPhotos,
                user.getUserId(), user.getUser().getNickname() != null ? user.getUser().getNickname() : user.getUsername(),
                user.getUsername(), roleKey, getClientIp(request));
        return Result.success(created);
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAuthority('maintenance:order:assign')")
    public Result<Void> assign(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        LoginUser user = getCurrentUser();
        Long assignedUserId = Long.valueOf(params.get("assignedUserId").toString());
        maintenanceOrderService.assign(id, assignedUserId, user.getUserId(),
                user.getUser().getNickname() != null ? user.getUser().getNickname() : user.getUsername());
        return Result.success();
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('maintenance:order:accept')")
    public Result<Void> accept(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        maintenanceOrderService.accept(id, user.getUserId(),
                user.getUser().getNickname() != null ? user.getUser().getNickname() : user.getUsername());
        return Result.success();
    }

    @PutMapping("/{id}/progress")
    @PreAuthorize("hasAuthority('maintenance:order:accept')")
    public Result<Void> addProgress(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        LoginUser user = getCurrentUser();
        String note = params.get("note") != null ? params.get("note").toString() : "";
        List<String> photos = params.get("photos") != null ? (List<String>) params.get("photos") : null;
        maintenanceOrderService.addProgressNote(id, note, photos, user.getUserId(),
                user.getUser().getNickname() != null ? user.getUser().getNickname() : user.getUsername());
        return Result.success();
    }

    @PutMapping("/{id}/finish")
    @PreAuthorize("hasAuthority('maintenance:order:finish')")
    public Result<Void> finish(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        LoginUser user = getCurrentUser();
        BigDecimal actualHours = params.get("actualHours") != null
                ? new BigDecimal(params.get("actualHours").toString()) : null;
        String usedParts = params.get("usedParts") != null ? params.get("usedParts").toString() : null;
        BigDecimal maintenanceCost = params.get("maintenanceCost") != null
                ? new BigDecimal(params.get("maintenanceCost").toString()) : BigDecimal.ZERO;
        String maintenanceDescription = params.get("maintenanceDescription") != null
                ? params.get("maintenanceDescription").toString() : "";
        List<String> afterPhotos = params.get("afterPhotos") != null
                ? (List<String>) params.get("afterPhotos") : null;
        maintenanceOrderService.finish(id, actualHours, usedParts, maintenanceCost,
                maintenanceDescription, afterPhotos, user.getUserId(),
                user.getUser().getNickname() != null ? user.getUser().getNickname() : user.getUsername());
        return Result.success();
    }

    @PutMapping("/{id}/inspect")
    @PreAuthorize("hasAuthority('maintenance:order:inspect')")
    public Result<Void> inspect(@PathVariable Long id, @RequestBody Map<String, Object> params,
                                 HttpServletRequest request) {
        LoginUser user = getCurrentUser();
        SysUser sysUser = sysUserService.getUserDetail(user.getUserId());
        Integer inspectResult = Integer.valueOf(params.get("inspectResult").toString());
        String inspectOpinion = params.get("inspectOpinion") != null ? params.get("inspectOpinion").toString() : "";
        String rectificationRequirement = params.get("rectificationRequirement") != null
                ? params.get("rectificationRequirement").toString() : null;
        String roleKey = sysUser.getRoles() != null && !sysUser.getRoles().isEmpty()
                ? sysUser.getRoles().get(0).getRoleKey() : "";
        maintenanceOrderService.inspect(id, inspectResult, inspectOpinion, rectificationRequirement,
                user.getUserId(), user.getUser().getNickname() != null ? user.getUser().getNickname() : user.getUsername(),
                user.getUsername(), roleKey, getClientIp(request));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('maintenance:order:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        LoginUser user = getCurrentUser();
        maintenanceOrderService.delete(id, user.getUserId(),
                user.getUser().getNickname() != null ? user.getUser().getNickname() : user.getUsername());
        return Result.success();
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('maintenance:order:export')")
    public ResponseEntity<byte[]> exportOrders(@RequestBody Map<String, Object> params) throws Exception {
        LoginUser user = getCurrentUser();
        String operatorName = user.getUser().getNickname() != null ? user.getUser().getNickname() : user.getUsername();

        String orderNo = params.get("orderNo") != null ? params.get("orderNo").toString() : null;
        String roomNumber = params.get("roomNumber") != null ? params.get("roomNumber").toString() : null;
        List<Integer> statusList = (List<Integer>) params.get("statusList");
        List<Integer> typeList = (List<Integer>) params.get("typeList");
        List<Integer> priorityList = (List<Integer>) params.get("priorityList");
        Long assignedUserId = params.get("assignedUserId") != null
                ? Long.valueOf(params.get("assignedUserId").toString()) : null;

        List<MaintenanceOrder> orders = maintenanceOrderService.listAllForExport(
                orderNo, roomNumber, statusList, typeList, priorityList, assignedUserId);

        byte[] excelData = exportService.exportMaintenanceOrders(orders, operatorName);
        String fileName = "维护单_" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok().headers(headers).body(excelData);
    }
}
