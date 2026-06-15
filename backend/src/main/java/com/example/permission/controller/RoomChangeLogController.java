package com.example.permission.controller;

import com.example.permission.common.PageResult;
import com.example.permission.common.Result;
import com.example.permission.entity.RoomChangeLog;
import com.example.permission.service.RoomChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/maintenance/changeLog")
public class RoomChangeLogController {

    @Autowired
    private RoomChangeLogService roomChangeLogService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('maintenance:changeLog:list')")
    public Result<PageResult<RoomChangeLog>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) List<Integer> operationTypes) {
        PageResult<RoomChangeLog> result = roomChangeLogService.pageList(
                pageNum, pageSize, roomNumber, operatorId, startTime, endTime, operationTypes);
        return Result.success(result);
    }

    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAuthority('maintenance:changeLog:query')")
    public Result<List<RoomChangeLog>> listByRoomId(@PathVariable Long roomId) {
        return Result.success(roomChangeLogService.listByRoomId(roomId));
    }
}
