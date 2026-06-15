package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.Building;
import com.example.permission.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel/building")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('hotel:building:list')")
    public Result<List<Building>> listAll() {
        List<Building> buildings = buildingService.listAll();
        return Result.success(buildings);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('hotel:building:query')")
    public Result<Building> getById(@PathVariable Long id) {
        Building building = buildingService.getById(id);
        return Result.success(building);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('hotel:building:add')")
    public Result<Void> add(@RequestBody Building building) {
        buildingService.add(building);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('hotel:building:edit')")
    public Result<Void> update(@RequestBody Building building) {
        buildingService.update(building);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hotel:building:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('hotel:building:edit')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = Integer.valueOf(params.get("status").toString());
        buildingService.updateStatus(id, status);
        return Result.success();
    }
}
