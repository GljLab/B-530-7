package com.example.permission.service;

import com.example.permission.entity.Building;
import com.example.permission.entity.Floor;
import com.example.permission.entity.Room;
import com.example.permission.entity.RoomStatusLog;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.BuildingMapper;
import com.example.permission.mapper.FloorMapper;
import com.example.permission.mapper.RoomMapper;
import com.example.permission.mapper.RoomStatusLogMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.BuildingTableDef.BUILDING;
import static com.example.permission.entity.table.FloorTableDef.FLOOR;
import static com.example.permission.entity.table.RoomStatusLogTableDef.ROOM_STATUS_LOG;
import static com.example.permission.entity.table.RoomTableDef.ROOM;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class HotelDashboardService {

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private RoomStatusLogMapper roomStatusLogMapper;

    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalBuildings = buildingMapper.selectCountByQuery(
                QueryWrapper.create().from(Building.class).where(BUILDING.DELETED.eq(0)));
        long totalFloors = floorMapper.selectCountByQuery(
                QueryWrapper.create().from(Floor.class).where(FLOOR.DELETED.eq(0)));
        long totalRooms = roomMapper.selectCountByQuery(
                QueryWrapper.create().from(Room.class).where(ROOM.DELETED.eq(0)));
        long activeRoomTypes = roomTypeMapper.selectCountByQuery(
                QueryWrapper.create().from(RoomType.class)
                        .where(ROOM_TYPE.DELETED.eq(0)).and(ROOM_TYPE.SALE_STATUS.eq(1)));
        long availableRooms = roomMapper.selectCountByQuery(
                QueryWrapper.create().from(Room.class)
                        .where(ROOM.DELETED.eq(0)).and(ROOM.STATUS.eq(1)));
        stats.put("totalBuildings", totalBuildings);
        stats.put("totalFloors", totalFloors);
        stats.put("totalRooms", totalRooms);
        stats.put("activeRoomTypes", activeRoomTypes);
        stats.put("availableRooms", availableRooms);
        return stats;
    }

    public List<Map<String, Object>> getRoomStatusStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Room> allRooms = roomMapper.selectListByQuery(
                QueryWrapper.create().from(Room.class).where(ROOM.DELETED.eq(0)));
        Map<Integer, Long> statusCount = new HashMap<>();
        for (Room room : allRooms) {
            statusCount.merge(room.getStatus(), 1L, Long::sum);
        }
        for (Map.Entry<Integer, Long> entry : statusCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getRoomTypeStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<RoomType> types = roomTypeMapper.selectListByQuery(
                QueryWrapper.create().from(RoomType.class).where(ROOM_TYPE.DELETED.eq(0)));
        for (RoomType type : types) {
            long total = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.ROOM_TYPE_ID.eq(type.getId()))
                            .and(ROOM.DELETED.eq(0)));
            long available = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.ROOM_TYPE_ID.eq(type.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(1)));
            Map<String, Object> item = new HashMap<>();
            item.put("typeId", type.getId());
            item.put("typeName", type.getTypeName());
            item.put("total", total);
            item.put("available", available);
            result.add(item);
        }
        return result;
    }

    public List<Map<String, Object>> getFloorStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Floor> floors = floorMapper.selectListByQuery(
                QueryWrapper.create().from(Floor.class).where(FLOOR.DELETED.eq(0)));
        for (Floor floor : floors) {
            Building building = buildingMapper.selectOneById(floor.getBuildingId());
            Map<String, Object> item = new HashMap<>();
            item.put("floorId", floor.getId());
            item.put("buildingName", building != null ? building.getBuildingName() : "");
            item.put("floorNumber", floor.getFloorNumber());
            item.put("floorName", floor.getFloorName());
            item.put("roomCount", floor.getRoomCount());
            result.add(item);
        }
        return result;
    }

    public Map<String, Object> getDetailedRoomTypeStats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> typeList = new ArrayList<>();
        List<RoomType> types = roomTypeMapper.selectListByQuery(
                QueryWrapper.create().from(RoomType.class).where(ROOM_TYPE.DELETED.eq(0)));
        for (RoomType type : types) {
            long total = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.ROOM_TYPE_ID.eq(type.getId()))
                            .and(ROOM.DELETED.eq(0)));
            long free = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.ROOM_TYPE_ID.eq(type.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(1)));
            long occupied = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.ROOM_TYPE_ID.eq(type.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(3)));
            long maintenance = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.ROOM_TYPE_ID.eq(type.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(6)));
            Map<String, Object> item = new HashMap<>();
            item.put("typeId", type.getId());
            item.put("typeName", type.getTypeName());
            item.put("total", total);
            item.put("free", free);
            item.put("occupied", occupied);
            item.put("maintenance", maintenance);
            item.put("freeRate", total > 0 ? Math.round((free * 100.0) / total) : 0);
            typeList.add(item);
        }
        typeList.sort((a, b) -> Long.compare((int) a.get("freeRate"), (int) b.get("freeRate")));
        result.put("typeStats", typeList);
        return result;
    }

    public Map<String, Object> getDetailedFloorStats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> floorList = new ArrayList<>();
        List<Floor> floors = floorMapper.selectListByQuery(
                QueryWrapper.create().from(Floor.class).where(FLOOR.DELETED.eq(0)));
        for (Floor floor : floors) {
            Building building = buildingMapper.selectOneById(floor.getBuildingId());
            long total = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0)));
            long free = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(1)));
            long occupied = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(3)));
            long maintenance = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(6)));
            Map<String, Object> item = new HashMap<>();
            item.put("floorId", floor.getId());
            item.put("buildingId", floor.getBuildingId());
            item.put("buildingName", building != null ? building.getBuildingName() : "");
            item.put("floorNumber", floor.getFloorNumber());
            item.put("floorName", floor.getFloorName());
            item.put("total", total);
            item.put("free", free);
            item.put("occupied", occupied);
            item.put("maintenance", maintenance);
            item.put("utilizationRate", total > 0 ? Math.round((occupied * 100.0) / total) : 0);
            floorList.add(item);
        }
        floorList.sort((a, b) -> Integer.compare((int) a.get("floorNumber"), (int) b.get("floorNumber")));
        result.put("floorStats", floorList);
        return result;
    }

    public Map<String, Object> getAttributeDistribution() {
        Map<String, Object> result = new HashMap<>();
        List<Room> allRooms = roomMapper.selectListByQuery(
                QueryWrapper.create().from(Room.class).where(ROOM.DELETED.eq(0)));
        Map<String, Long> orientationDist = new HashMap<>();
        Map<String, Long> viewTypeDist = new HashMap<>();
        Map<String, Long> specialTagDist = new HashMap<>();
        for (Room room : allRooms) {
            String ori = room.getOrientation();
            if (ori != null && !ori.isEmpty()) {
                orientationDist.merge(ori, 1L, Long::sum);
            }
            String vt = room.getViewType();
            if (vt != null && !vt.isEmpty()) {
                viewTypeDist.merge(vt, 1L, Long::sum);
            }
            String tags = room.getSpecialTags();
            if (tags != null && !tags.isEmpty()) {
                try {
                    java.util.List<String> tagList = com.fasterxml.jackson.databind.ObjectMapper.class.getDeclaredConstructor().newInstance()
                            .readValue(tags, java.util.List.class);
                    for (String tag : tagList) {
                        specialTagDist.merge(tag, 1L, Long::sum);
                    }
                } catch (Exception e) {
                    for (String tag : tags.split(",")) {
                        String trimmed = tag.trim();
                        if (!trimmed.isEmpty()) {
                            specialTagDist.merge(trimmed, 1L, Long::sum);
                        }
                    }
                }
            }
        }
        List<Map<String, Object>> orientationList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : orientationDist.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("count", entry.getValue());
            orientationList.add(item);
        }
        List<Map<String, Object>> viewTypeList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : viewTypeDist.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("count", entry.getValue());
            viewTypeList.add(item);
        }
        List<Map<String, Object>> specialTagList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : specialTagDist.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("count", entry.getValue());
            specialTagList.add(item);
        }
        result.put("orientationDist", orientationList);
        result.put("viewTypeDist", viewTypeList);
        result.put("specialTagDist", specialTagList);
        return result;
    }

    public Map<String, Object> getStatusDurationStats() {
        Map<String, Object> result = new HashMap<>();
        List<Room> freeRooms = roomMapper.selectListByQuery(
                QueryWrapper.create().from(Room.class)
                        .where(ROOM.DELETED.eq(0))
                        .and(ROOM.STATUS.eq(1)));
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long within1Day = 0;
        long within1to3Days = 0;
        long within3to7Days = 0;
        long over7Days = 0;
        for (Room room : freeRooms) {
            java.time.Duration duration = java.time.Duration.between(room.getUpdateTime(), now);
            long days = duration.toDays();
            if (days < 1) within1Day++;
            else if (days < 3) within1to3Days++;
            else if (days < 7) within3to7Days++;
            else over7Days++;
        }
        List<Map<String, Object>> durationList = new ArrayList<>();
        Map<String, Object> d1 = new HashMap<>();
        d1.put("range", "1天内");
        d1.put("count", within1Day);
        durationList.add(d1);
        Map<String, Object> d2 = new HashMap<>();
        d2.put("range", "1-3天");
        d2.put("count", within1to3Days);
        durationList.add(d2);
        Map<String, Object> d3 = new HashMap<>();
        d3.put("range", "3-7天");
        d3.put("count", within3to7Days);
        durationList.add(d3);
        Map<String, Object> d4 = new HashMap<>();
        d4.put("range", "7天以上");
        d4.put("count", over7Days);
        durationList.add(d4);
        result.put("durationStats", durationList);
        result.put("totalFree", freeRooms.size());
        return result;
    }

    public List<Map<String, Object>> getStatusTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 30;
        }
        List<Map<String, Object>> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDateTime startDateTime = today.minusDays(days - 1).atStartOfDay();
        LocalDateTime endDateTime = today.plusDays(1).atStartOfDay();

        QueryWrapper logQuery = QueryWrapper.create()
                .from(RoomStatusLog.class)
                .where(ROOM_STATUS_LOG.CREATE_TIME.ge(startDateTime))
                .and(ROOM_STATUS_LOG.CREATE_TIME.lt(endDateTime))
                .orderBy(ROOM_STATUS_LOG.CREATE_TIME.asc());
        List<RoomStatusLog> allLogs = roomStatusLogMapper.selectListByQuery(logQuery);

        Map<Long, List<RoomStatusLog>> logsByRoom = allLogs.stream()
                .collect(Collectors.groupingBy(RoomStatusLog::getRoomId));

        List<Room> allRooms = roomMapper.selectListByQuery(
                QueryWrapper.create().from(Room.class).where(ROOM.DELETED.eq(0)));

        Map<Long, Integer> currentStatusMap = new HashMap<>();
        for (Room room : allRooms) {
            currentStatusMap.put(room.getId(), room.getStatus());
        }

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            Map<String, Object> dayItem = new LinkedHashMap<>();
            dayItem.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            Map<Integer, Long> statusCount = new HashMap<>();
            boolean hasLogsForDay = false;

            for (Room room : allRooms) {
                Long roomId = room.getId();
                List<RoomStatusLog> roomLogs = logsByRoom.getOrDefault(roomId, new ArrayList<>());

                Integer statusAtEndOfDay = null;
                LocalDateTime latestChangeBeforeDay = null;

                for (RoomStatusLog log : roomLogs) {
                    if (log.getCreateTime().isBefore(dayEnd)) {
                        if (latestChangeBeforeDay == null || log.getCreateTime().isAfter(latestChangeBeforeDay)) {
                            latestChangeBeforeDay = log.getCreateTime();
                            statusAtEndOfDay = log.getNewStatus();
                        }
                    }
                    if (log.getCreateTime().isAfter(dayStart) && log.getCreateTime().isBefore(dayEnd)) {
                        hasLogsForDay = true;
                    }
                }

                if (statusAtEndOfDay == null) {
                    statusAtEndOfDay = currentStatusMap.get(roomId);
                }

                if (statusAtEndOfDay != null) {
                    statusCount.merge(statusAtEndOfDay, 1L, Long::sum);
                }
            }

            for (Map.Entry<Integer, Long> entry : statusCount.entrySet()) {
                dayItem.put("status" + entry.getKey(), entry.getValue());
            }
            dayItem.put("_hasLogs", hasLogsForDay);
            result.add(dayItem);
        }

        return result;
    }

    public List<Map<String, Object>> getFloorUsageStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Floor> floors = floorMapper.selectListByQuery(
                QueryWrapper.create().from(Floor.class).where(FLOOR.DELETED.eq(0)));

        for (Floor floor : floors) {
            Building building = buildingMapper.selectOneById(floor.getBuildingId());
            long total = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0)));
            long free = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(1)));
            long occupied = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(3)));
            long maintenance = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.FLOOR_ID.eq(floor.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(6)));

            long available = total - maintenance;
            double usageRate = available > 0 ? (occupied * 100.0) / available : 0;

            Map<String, Object> item = new HashMap<>();
            item.put("floorId", floor.getId());
            item.put("buildingName", building != null ? building.getBuildingName() : "");
            item.put("floorNumber", floor.getFloorNumber());
            item.put("floorName", floor.getFloorName());
            item.put("total", total);
            item.put("free", free);
            item.put("occupied", occupied);
            item.put("maintenance", maintenance);
            item.put("usageRate", BigDecimal.valueOf(usageRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
            result.add(item);
        }

        result.sort((a, b) -> Double.compare(
                (Double) b.get("usageRate"),
                (Double) a.get("usageRate")
        ));

        return result;
    }

    public Map<String, Object> getRoomTypeHeatStats() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> typeList = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        List<Long> totalData = new ArrayList<>();
        List<Long> freeData = new ArrayList<>();
        List<Double> freeRateData = new ArrayList<>();

        List<RoomType> types = roomTypeMapper.selectListByQuery(
                QueryWrapper.create().from(RoomType.class).where(ROOM_TYPE.DELETED.eq(0)));

        for (RoomType type : types) {
            long total = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.ROOM_TYPE_ID.eq(type.getId()))
                            .and(ROOM.DELETED.eq(0)));
            long free = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class)
                            .where(ROOM.ROOM_TYPE_ID.eq(type.getId()))
                            .and(ROOM.DELETED.eq(0))
                            .and(ROOM.STATUS.eq(1)));
            double freeRate = total > 0 ? (free * 100.0) / total : 0;

            Map<String, Object> item = new HashMap<>();
            item.put("typeId", type.getId());
            item.put("typeName", type.getTypeName());
            item.put("total", total);
            item.put("free", free);
            item.put("freeRate", BigDecimal.valueOf(freeRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
            typeList.add(item);

            categories.add(type.getTypeName());
            totalData.add(total);
            freeData.add(free);
            freeRateData.add(BigDecimal.valueOf(freeRate).setScale(2, RoundingMode.HALF_UP).doubleValue());
        }

        typeList.sort(Comparator.comparingDouble(a -> (Double) a.get("freeRate")));

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("categories", categories);
        chartData.put("totalData", totalData);
        chartData.put("freeData", freeData);
        chartData.put("freeRateData", freeRateData);

        result.put("list", typeList);
        result.put("chart", chartData);
        return result;
    }

    public Map<String, Object> getStatusDurationStatsEnhanced() {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        double avgCleanHours = calculateAvgCleanHours();
        result.put("avgCleanHours", BigDecimal.valueOf(avgCleanHours).setScale(2, RoundingMode.HALF_UP).doubleValue());

        double avgRepairDays = calculateAvgRepairDays();
        result.put("avgRepairDays", BigDecimal.valueOf(avgRepairDays).setScale(2, RoundingMode.HALF_UP).doubleValue());

        List<Room> freeRooms = roomMapper.selectListByQuery(
                QueryWrapper.create().from(Room.class)
                        .where(ROOM.DELETED.eq(0))
                        .and(ROOM.STATUS.eq(1)));

        long within1Day = 0;
        long within1to3Days = 0;
        long within3to7Days = 0;
        long within7to15Days = 0;
        long over15Days = 0;
        List<Map<String, Object>> longIdleRooms = new ArrayList<>();

        for (Room room : freeRooms) {
            LocalDateTime idleSince = room.getUpdateTime();
            QueryWrapper lastStatusChangeQuery = QueryWrapper.create()
                    .from(RoomStatusLog.class)
                    .where(ROOM_STATUS_LOG.ROOM_ID.eq(room.getId()))
                    .and(ROOM_STATUS_LOG.NEW_STATUS.eq(1))
                    .orderBy(ROOM_STATUS_LOG.CREATE_TIME.desc())
                    .limit(1);
            List<RoomStatusLog> lastLogs = roomStatusLogMapper.selectListByQuery(lastStatusChangeQuery);
            if (!lastLogs.isEmpty()) {
                idleSince = lastLogs.get(0).getCreateTime();
            }

            long days = Duration.between(idleSince, now).toDays();

            if (days < 1) within1Day++;
            else if (days < 3) within1to3Days++;
            else if (days < 7) within3to7Days++;
            else if (days < 15) within7to15Days++;
            else {
                over15Days++;
                Map<String, Object> roomItem = new HashMap<>();
                roomItem.put("roomId", room.getId());
                roomItem.put("roomNumber", room.getRoomNumber());
                roomItem.put("idleDays", days);
                roomItem.put("idleSince", idleSince);
                longIdleRooms.add(roomItem);
            }
        }

        List<Map<String, Object>> idleDurationDist = new ArrayList<>();
        Map<String, Object> d1 = new HashMap<>();
        d1.put("range", "1天内");
        d1.put("count", within1Day);
        idleDurationDist.add(d1);
        Map<String, Object> d2 = new HashMap<>();
        d2.put("range", "1-3天");
        d2.put("count", within1to3Days);
        idleDurationDist.add(d2);
        Map<String, Object> d3 = new HashMap<>();
        d3.put("range", "3-7天");
        d3.put("count", within3to7Days);
        idleDurationDist.add(d3);
        Map<String, Object> d4 = new HashMap<>();
        d4.put("range", "7-15天");
        d4.put("count", within7to15Days);
        idleDurationDist.add(d4);
        Map<String, Object> d5 = new HashMap<>();
        d5.put("range", "15天以上");
        d5.put("count", over15Days);
        idleDurationDist.add(d5);

        result.put("idleDurationDist", idleDurationDist);
        result.put("longIdleRooms", longIdleRooms);
        result.put("totalFree", freeRooms.size());
        return result;
    }

    private double calculateAvgCleanHours() {
        QueryWrapper query = QueryWrapper.create()
                .from(RoomStatusLog.class)
                .where(ROOM_STATUS_LOG.NEW_STATUS.eq(1))
                .orderBy(ROOM_STATUS_LOG.ROOM_ID.asc(), ROOM_STATUS_LOG.CREATE_TIME.asc());
        List<RoomStatusLog> toFreeLogs = roomStatusLogMapper.selectListByQuery(query);

        if (toFreeLogs.isEmpty()) {
            return 0.0;
        }

        double totalHours = 0;
        int count = 0;

        for (RoomStatusLog freeLog : toFreeLogs) {
            QueryWrapper prevQuery = QueryWrapper.create()
                    .from(RoomStatusLog.class)
                    .where(ROOM_STATUS_LOG.ROOM_ID.eq(freeLog.getRoomId()))
                    .and(ROOM_STATUS_LOG.NEW_STATUS.eq(4))
                    .and(ROOM_STATUS_LOG.CREATE_TIME.lt(freeLog.getCreateTime()))
                    .orderBy(ROOM_STATUS_LOG.CREATE_TIME.desc())
                    .limit(1);
            List<RoomStatusLog> prevCleanLogs = roomStatusLogMapper.selectListByQuery(prevQuery);

            if (!prevCleanLogs.isEmpty()) {
                RoomStatusLog cleanLog = prevCleanLogs.get(0);
                long minutes = Duration.between(cleanLog.getCreateTime(), freeLog.getCreateTime()).toMinutes();
                totalHours += minutes / 60.0;
                count++;
            }
        }

        return count > 0 ? totalHours / count : 0.0;
    }

    private double calculateAvgRepairDays() {
        QueryWrapper query = QueryWrapper.create()
                .from(RoomStatusLog.class)
                .where(ROOM_STATUS_LOG.NEW_STATUS.eq(4))
                .orderBy(ROOM_STATUS_LOG.ROOM_ID.asc(), ROOM_STATUS_LOG.CREATE_TIME.asc());
        List<RoomStatusLog> toCleanLogs = roomStatusLogMapper.selectListByQuery(query);

        if (toCleanLogs.isEmpty()) {
            return 0.0;
        }

        double totalDays = 0;
        int count = 0;

        for (RoomStatusLog cleanLog : toCleanLogs) {
            QueryWrapper prevQuery = QueryWrapper.create()
                    .from(RoomStatusLog.class)
                    .where(ROOM_STATUS_LOG.ROOM_ID.eq(cleanLog.getRoomId()))
                    .and(ROOM_STATUS_LOG.NEW_STATUS.eq(6))
                    .and(ROOM_STATUS_LOG.CREATE_TIME.lt(cleanLog.getCreateTime()))
                    .orderBy(ROOM_STATUS_LOG.CREATE_TIME.desc())
                    .limit(1);
            List<RoomStatusLog> prevRepairLogs = roomStatusLogMapper.selectListByQuery(prevQuery);

            if (!prevRepairLogs.isEmpty()) {
                RoomStatusLog repairLog = prevRepairLogs.get(0);
                long hours = Duration.between(repairLog.getCreateTime(), cleanLog.getCreateTime()).toHours();
                totalDays += hours / 24.0;
                count++;
            }
        }

        return count > 0 ? totalDays / count : 0.0;
    }
}
