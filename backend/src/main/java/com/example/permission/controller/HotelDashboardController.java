package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.service.HotelDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel/dashboard")
public class HotelDashboardController {

    @Autowired
    private HotelDashboardService hotelDashboardService;

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<Map<String, Object>> getOverviewStats() {
        Map<String, Object> stats = hotelDashboardService.getOverviewStats();
        return Result.success(stats);
    }

    @GetMapping("/roomStatus")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<List<Map<String, Object>>> getRoomStatusStats() {
        List<Map<String, Object>> stats = hotelDashboardService.getRoomStatusStats();
        return Result.success(stats);
    }

    @GetMapping("/roomType")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<List<Map<String, Object>>> getRoomTypeStats() {
        List<Map<String, Object>> stats = hotelDashboardService.getRoomTypeStats();
        return Result.success(stats);
    }

    @GetMapping("/floor")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<List<Map<String, Object>>> getFloorStats() {
        List<Map<String, Object>> stats = hotelDashboardService.getFloorStats();
        return Result.success(stats);
    }

    @GetMapping("/roomType/detail")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<Map<String, Object>> getDetailedRoomTypeStats() {
        Map<String, Object> stats = hotelDashboardService.getDetailedRoomTypeStats();
        return Result.success(stats);
    }

    @GetMapping("/floor/detail")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<Map<String, Object>> getDetailedFloorStats() {
        Map<String, Object> stats = hotelDashboardService.getDetailedFloorStats();
        return Result.success(stats);
    }

    @GetMapping("/attributeDist")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<Map<String, Object>> getAttributeDistribution() {
        Map<String, Object> stats = hotelDashboardService.getAttributeDistribution();
        return Result.success(stats);
    }

    @GetMapping("/statusDuration")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<Map<String, Object>> getStatusDurationStats() {
        Map<String, Object> stats = hotelDashboardService.getStatusDurationStats();
        return Result.success(stats);
    }

    @GetMapping("/statusTrend")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<List<Map<String, Object>>> getStatusTrend(@RequestParam(defaultValue = "30") Integer days) {
        List<Map<String, Object>> stats = hotelDashboardService.getStatusTrend(days);
        return Result.success(stats);
    }

    @GetMapping("/floorUsage")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<List<Map<String, Object>>> getFloorUsageStats() {
        List<Map<String, Object>> stats = hotelDashboardService.getFloorUsageStats();
        return Result.success(stats);
    }

    @GetMapping("/roomTypeHeat")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<Map<String, Object>> getRoomTypeHeatStats() {
        Map<String, Object> stats = hotelDashboardService.getRoomTypeHeatStats();
        return Result.success(stats);
    }

    @GetMapping("/statusDurationEnhanced")
    @PreAuthorize("hasAuthority('hotel:dashboard:query')")
    public Result<Map<String, Object>> getStatusDurationStatsEnhanced() {
        Map<String, Object> stats = hotelDashboardService.getStatusDurationStatsEnhanced();
        return Result.success(stats);
    }
}
