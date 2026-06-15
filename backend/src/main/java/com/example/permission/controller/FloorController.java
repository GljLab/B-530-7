package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.Floor;
import com.example.permission.service.FloorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel/floor")
public class FloorController {

    @Autowired
    private FloorService floorService;

    @GetMapping("/list/{buildingId}")
    @PreAuthorize("hasAuthority('hotel:floor:query')")
    public Result<List<Floor>> listByBuildingId(@PathVariable Long buildingId) {
        List<Floor> floors = floorService.listByBuildingId(buildingId);
        return Result.success(floors);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('hotel:floor:query')")
    public Result<Floor> getById(@PathVariable Long id) {
        Floor floor = floorService.getById(id);
        return Result.success(floor);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('hotel:floor:add')")
    public Result<Void> add(@RequestBody Floor floor) {
        floorService.add(floor);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('hotel:floor:edit')")
    public Result<Void> update(@RequestBody Floor floor) {
        floorService.update(floor);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hotel:floor:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        floorService.delete(id);
        return Result.success();
    }
}
