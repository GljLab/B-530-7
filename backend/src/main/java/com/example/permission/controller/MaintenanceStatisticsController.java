package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.service.ExportService;
import com.example.permission.service.MaintenanceStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance/statistics")
public class MaintenanceStatisticsController {

    @Autowired
    private MaintenanceStatisticsService statisticsService;

    @Autowired
    private ExportService exportService;

    private String getCurrentUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.example.permission.security.LoginUser) {
            com.example.permission.security.LoginUser user = (com.example.permission.security.LoginUser) auth.getPrincipal();
            return user.getUser().getNickname() != null ? user.getUser().getNickname() : user.getUsername();
        }
        return "系统";
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<Map<String, Object>> getOverview() {
        return Result.success(statisticsService.getOverview());
    }

    @GetMapping("/topRooms")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<List<Map<String, Object>>> getTopRooms(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statisticsService.getTopMaintenanceRooms(limit));
    }

    @GetMapping("/typeDistribution")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<Map<String, Object>> getTypeDistribution() {
        return Result.success(statisticsService.getMaintenanceTypeDistribution());
    }

    @GetMapping("/costTrend")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<List<Map<String, Object>>> getCostTrend(@RequestParam(defaultValue = "6") Integer months) {
        return Result.success(statisticsService.getCostTrend(months));
    }

    @GetMapping("/durationStats")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<Map<String, Object>> getDurationStats() {
        return Result.success(statisticsService.getDurationStats());
    }

    @GetMapping("/staffWorkload")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<List<Map<String, Object>>> getStaffWorkload() {
        return Result.success(statisticsService.getStaffWorkload());
    }

    @GetMapping("/avgDurationTrend")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<List<Map<String, Object>>> getAvgDurationTrend(@RequestParam(defaultValue = "6") Integer months) {
        return Result.success(statisticsService.getAvgDurationTrend(months));
    }

    @GetMapping("/staffWorkloadCompare")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<List<Map<String, Object>>> getStaffWorkloadCompare() {
        return Result.success(statisticsService.getStaffWorkloadCompare());
    }

    @GetMapping("/inspectionPassRate")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<Map<String, Object>> getInspectionPassRate() {
        return Result.success(statisticsService.getInspectionPassRate());
    }

    @GetMapping("/costTrendEnhanced")
    @PreAuthorize("hasAuthority('maintenance:statistics:list')")
    public Result<List<Map<String, Object>>> getCostTrendEnhanced(@RequestParam(defaultValue = "6") Integer months) {
        return Result.success(statisticsService.getCostTrendEnhanced(months));
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('maintenance:statistics:export')")
    public ResponseEntity<byte[]> exportStatistics(@RequestBody(required = false) Map<String, Object> params) throws Exception {
        String operatorName = getCurrentUserName();
        byte[] excelData = exportService.exportMaintenanceStatistics(
                statisticsService.getOverview(),
                statisticsService.getTopMaintenanceRooms(10),
                statisticsService.getMaintenanceTypeDistribution(),
                statisticsService.getCostTrend(6),
                statisticsService.getDurationStats(),
                statisticsService.getStaffWorkload(),
                operatorName
        );
        String fileName = "维护统计报表_" + java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", encodedFileName);
        headers.add("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        return ResponseEntity.ok().headers(headers).body(excelData);
    }
}
