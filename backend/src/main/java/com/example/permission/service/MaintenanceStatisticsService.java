package com.example.permission.service;

import com.example.permission.entity.MaintenanceOrder;
import com.example.permission.entity.Room;
import com.example.permission.entity.SysUser;
import com.example.permission.mapper.MaintenanceOrderMapper;
import com.example.permission.mapper.MaintenanceStatusLogMapper;
import com.example.permission.mapper.RoomMapper;
import com.example.permission.mapper.SysUserMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.MaintenanceOrderTableDef.MAINTENANCE_ORDER;
import static com.example.permission.entity.table.MaintenanceStatusLogTableDef.MAINTENANCE_STATUS_LOG;

@Service
public class MaintenanceStatisticsService {

    @Autowired
    private MaintenanceOrderMapper maintenanceOrderMapper;

    @Autowired
    private MaintenanceStatusLogMapper maintenanceStatusLogMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    public Map<String, Object> getOverview() {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        QueryWrapper allQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.DELETED.eq(0));
        result.put("totalOrders", maintenanceOrderMapper.selectCountByQuery(allQuery));

        QueryWrapper pendingQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.STATUS.eq(1))
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        result.put("pendingCount", maintenanceOrderMapper.selectCountByQuery(pendingQuery));

        QueryWrapper processingQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.STATUS.eq(2))
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        result.put("processingCount", maintenanceOrderMapper.selectCountByQuery(processingQuery));

        QueryWrapper monthQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.CREATE_TIME.ge(monthStart))
                .and(MAINTENANCE_ORDER.CREATE_TIME.lt(monthEnd))
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        List<MaintenanceOrder> monthOrders = maintenanceOrderMapper.selectListByQuery(monthQuery);
        result.put("monthOrderCount", monthOrders.size());

        BigDecimal monthCost = BigDecimal.ZERO;
        double totalHours = 0;
        int finishedWithHours = 0;
        for (MaintenanceOrder o : monthOrders) {
            if (o.getMaintenanceCost() != null) {
                monthCost = monthCost.add(o.getMaintenanceCost());
            }
            if (o.getActualHours() != null) {
                totalHours += o.getActualHours().doubleValue();
                finishedWithHours++;
            }
        }
        result.put("monthCost", monthCost);
        result.put("avgHours", finishedWithHours > 0 ? totalHours / finishedWithHours : 0);

        return result;
    }

    public List<Map<String, Object>> getTopMaintenanceRooms(int limit) {
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.DELETED.eq(0));
        List<MaintenanceOrder> allOrders = maintenanceOrderMapper.selectListByQuery(query);

        Map<Long, List<MaintenanceOrder>> roomGroup = allOrders.stream()
                .collect(Collectors.groupingBy(MaintenanceOrder::getRoomId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<MaintenanceOrder>> entry : roomGroup.entrySet()) {
            Room room = roomMapper.selectOneById(entry.getKey());
            if (room == null) continue;
            List<MaintenanceOrder> orders = entry.getValue();
            BigDecimal totalCost = BigDecimal.ZERO;
            LocalDateTime lastTime = null;
            for (MaintenanceOrder o : orders) {
                if (o.getMaintenanceCost() != null) {
                    totalCost = totalCost.add(o.getMaintenanceCost());
                }
                if (o.getFinishTime() != null && (lastTime == null || o.getFinishTime().isAfter(lastTime))) {
                    lastTime = o.getFinishTime();
                }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("roomId", room.getId());
            item.put("roomNumber", room.getRoomNumber());
            item.put("roomTypeId", room.getRoomTypeId());
            item.put("maintenanceCount", orders.size());
            item.put("totalCost", totalCost);
            item.put("lastMaintenanceTime", lastTime);
            result.add(item);
        }

        result.sort((a, b) -> Integer.compare(
                (Integer) b.get("maintenanceCount"),
                (Integer) a.get("maintenanceCount")
        ));
        return result.stream().limit(limit).collect(Collectors.toList());
    }

    public Map<String, Object> getMaintenanceTypeDistribution() {
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.DELETED.eq(0));
        List<MaintenanceOrder> allOrders = maintenanceOrderMapper.selectListByQuery(query);

        Map<Integer, Long> typeCount = allOrders.stream()
                .collect(Collectors.groupingBy(
                        MaintenanceOrder::getMaintenanceType,
                        Collectors.counting()
                ));

        Map<Integer, String> typeNames = MaintenanceOrderService.getTypeMap();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : typeCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", entry.getKey());
            item.put("typeName", typeNames.getOrDefault(entry.getKey(), "未知"));
            item.put("count", entry.getValue());
            result.add(item);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", result);
        data.put("total", allOrders.size());
        return data;
    }

    public List<Map<String, Object>> getCostTrend(int months) {
        List<Map<String, Object>> result = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

            QueryWrapper query = QueryWrapper.create()
                    .from(MaintenanceOrder.class)
                    .where(MAINTENANCE_ORDER.FINISH_TIME.ge(start))
                    .and(MAINTENANCE_ORDER.FINISH_TIME.lt(end))
                    .and(MAINTENANCE_ORDER.DELETED.eq(0))
                    .and(MAINTENANCE_ORDER.STATUS.in(Arrays.asList(4, 5)));
            List<MaintenanceOrder> orders = maintenanceOrderMapper.selectListByQuery(query);

            BigDecimal cost = BigDecimal.ZERO;
            for (MaintenanceOrder o : orders) {
                if (o.getMaintenanceCost() != null) {
                    cost = cost.add(o.getMaintenanceCost());
                }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("month", ym.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            item.put("cost", cost);
            item.put("count", orders.size());
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> getDurationStats() {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.ACCEPT_TIME.isNotNull())
                .and(MAINTENANCE_ORDER.FINISH_TIME.isNotNull())
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        List<MaintenanceOrder> finishedOrders = maintenanceOrderMapper.selectListByQuery(query);

        if (finishedOrders.isEmpty()) {
            result.put("avgDurationMinutes", 0);
            result.put("maxDurationMinutes", 0);
            result.put("maxOrder", null);
            result.put("timeoutCount", 0);
            result.put("timeoutRate", 0);
            return result;
        }

        long totalMinutes = 0;
        long maxMinutes = 0;
        MaintenanceOrder maxOrder = null;
        int timeoutCount = 0;

        for (MaintenanceOrder o : finishedOrders) {
            long minutes = Duration.between(o.getAcceptTime(), o.getFinishTime()).toMinutes();
            totalMinutes += minutes;
            if (minutes > maxMinutes) {
                maxMinutes = minutes;
                maxOrder = o;
            }
            if (o.getExpectedFinishTime() != null && o.getFinishTime().isAfter(o.getExpectedFinishTime())) {
                timeoutCount++;
            }
        }

        result.put("avgDurationMinutes", totalMinutes / finishedOrders.size());
        result.put("maxDurationMinutes", maxMinutes);
        if (maxOrder != null) {
            Map<String, Object> maxOrderMap = new HashMap<>();
            maxOrderMap.put("orderNo", maxOrder.getOrderNo());
            maxOrderMap.put("roomNumber", maxOrder.getRoomNumber());
            maxOrderMap.put("durationMinutes", maxMinutes);
            result.put("maxOrder", maxOrderMap);
        }
        result.put("timeoutCount", timeoutCount);
        result.put("timeoutRate", finishedOrders.size() > 0 ? (double) timeoutCount / finishedOrders.size() : 0);
        return result;
    }

    public List<Map<String, Object>> getStaffWorkload() {
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.ASSIGNED_USER_ID.isNotNull())
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        List<MaintenanceOrder> assignedOrders = maintenanceOrderMapper.selectListByQuery(query);

        Map<Long, List<MaintenanceOrder>> userGroup = assignedOrders.stream()
                .collect(Collectors.groupingBy(MaintenanceOrder::getAssignedUserId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<MaintenanceOrder>> entry : userGroup.entrySet()) {
            SysUser user = sysUserMapper.selectOneById(entry.getKey());
            if (user == null) continue;
            List<MaintenanceOrder> orders = entry.getValue();
            List<MaintenanceOrder> finished = orders.stream()
                    .filter(o -> o.getFinishTime() != null)
                    .collect(Collectors.toList());

            double totalHours = 0;
            int withHours = 0;
            int passCount = 0;
            for (MaintenanceOrder o : finished) {
                if (o.getActualHours() != null) {
                    totalHours += o.getActualHours().doubleValue();
                    withHours++;
                }
                if (o.getInspectResult() != null && o.getInspectResult() == 1) {
                    passCount++;
                }
            }
            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getId());
            item.put("username", user.getUsername());
            item.put("nickname", user.getNickname());
            item.put("totalCount", orders.size());
            item.put("finishedCount", finished.size());
            item.put("avgHours", withHours > 0 ? totalHours / withHours : 0);
            item.put("passRate", finished.size() > 0 ? (double) passCount / finished.size() : 0);
            result.add(item);
        }
        result.sort((a, b) -> Integer.compare(
                (Integer) b.get("totalCount"),
                (Integer) a.get("totalCount")
        ));
        return result;
    }
}
