package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.RoomType;
import com.example.permission.service.RoomTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hotel/roomType")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('hotel:roomType:list')")
    public Result<List<RoomType>> listAll() {
        List<RoomType> roomTypes = roomTypeService.listAll();
        return Result.success(roomTypes);
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('hotel:roomType:list')")
    public Result<PageResult<RoomType>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String bedType,
            @RequestParam(required = false) Integer saleStatus,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        PageResult<RoomType> result = roomTypeService.pageList(pageNum, pageSize, bedType, saleStatus, minPrice, maxPrice);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('hotel:roomType:query')")
    public Result<RoomType> getById(@PathVariable Long id) {
        RoomType roomType = roomTypeService.getById(id);
        return Result.success(roomType);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('hotel:roomType:add')")
    public Result<Void> add(@RequestBody RoomType roomType) {
        roomTypeService.add(roomType);
        return Result.success();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('hotel:roomType:edit')")
    public Result<Void> update(@RequestBody RoomType roomType) {
        roomTypeService.update(roomType);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hotel:roomType:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roomTypeService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/saleStatus")
    @PreAuthorize("hasAuthority('hotel:roomType:edit')")
    public Result<Void> updateSaleStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer saleStatus = Integer.valueOf(params.get("saleStatus").toString());
        roomTypeService.updateSaleStatus(id, saleStatus);
        return Result.success();
    }
}
