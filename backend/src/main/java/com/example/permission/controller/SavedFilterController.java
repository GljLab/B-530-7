package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.SavedFilter;
import com.example.permission.service.SavedFilterService;
import com.example.permission.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel/filter")
public class SavedFilterController {

    @Autowired
    private SavedFilterService savedFilterService;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/list")
    public Result<List<SavedFilter>> list(@RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        List<SavedFilter> list = savedFilterService.listByUserId(userId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<SavedFilter> getById(@PathVariable Long id,
                                        @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        SavedFilter filter = savedFilterService.getById(id);
        if (filter != null && !filter.getUserId().equals(userId)) {
            return Result.error("无权访问该筛选方案");
        }
        return Result.success(filter);
    }

    @PostMapping
    public Result<Void> add(@RequestBody SavedFilter filter,
                             @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        filter.setUserId(userId);
        savedFilterService.add(filter);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SavedFilter filter,
                                @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        SavedFilter existing = savedFilterService.getById(filter.getId());
        if (existing == null) {
            return Result.error("筛选方案不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.error("无权修改该筛选方案");
        }
        filter.setUserId(userId);
        savedFilterService.update(filter);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                                @RequestHeader("Authorization") String token) {
        String actualToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(actualToken);
        SavedFilter existing = savedFilterService.getById(id);
        if (existing == null) {
            return Result.error("筛选方案不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            return Result.error("无权删除该筛选方案");
        }
        savedFilterService.delete(id);
        return Result.success();
    }
}
