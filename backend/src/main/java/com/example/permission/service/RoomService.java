package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.Building;
import com.example.permission.entity.Floor;
import com.example.permission.entity.HotelImage;
import com.example.permission.entity.Room;
import com.example.permission.entity.RoomStatusLog;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.BuildingMapper;
import com.example.permission.mapper.FloorMapper;
import com.example.permission.mapper.HotelImageMapper;
import com.example.permission.mapper.RoomMapper;
import com.example.permission.mapper.RoomStatusLogMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.HotelImageTableDef.HOTEL_IMAGE;
import static com.example.permission.entity.table.RoomTableDef.ROOM;

@Service
public class RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private HotelImageMapper hotelImageMapper;

    @Autowired
    private RoomStatusLogMapper roomStatusLogMapper;

    @Autowired
    private FloorService floorService;

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private UserFloorPermissionService userFloorPermissionService;

    public PageResult<Room> pageList(Integer pageNum, Integer pageSize, String roomNumber,
                                      Long buildingId, Long floorId, Long roomTypeId,
                                      List<Integer> status, List<String> orientations,
                                      List<String> viewTypes, List<String> specialTags) {
        QueryWrapper query = buildQueryWrapper(roomNumber, buildingId, floorId, roomTypeId,
                status, orientations, viewTypes, specialTags, null);
        query.orderBy(ROOM.ROOM_NUMBER.asc());
        Page<Room> page = roomMapper.paginate(Page.of(pageNum, pageSize), query);
        for (Room room : page.getRecords()) {
            fillRoomAssociations(room);
        }
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public PageResult<Room> pageListWithPermission(Integer pageNum, Integer pageSize, String roomNumber,
                                                    Long buildingId, Long floorId, Long roomTypeId,
                                                    List<Integer> status, List<String> orientations,
                                                    List<String> viewTypes, List<String> specialTags,
                                                    Long userId) {
        List<Long> floorIds = userFloorPermissionService.getFloorIdsByUserId(userId);
        QueryWrapper query = buildQueryWrapper(roomNumber, buildingId, floorId, roomTypeId,
                status, orientations, viewTypes, specialTags, floorIds);
        query.orderBy(ROOM.ROOM_NUMBER.asc());
        Page<Room> page = roomMapper.paginate(Page.of(pageNum, pageSize), query);
        for (Room room : page.getRecords()) {
            fillRoomAssociations(room);
        }
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    private QueryWrapper buildQueryWrapper(String roomNumber, Long buildingId, Long floorId,
                                           Long roomTypeId, List<Integer> status,
                                           List<String> orientations, List<String> viewTypes,
                                           List<String> specialTags) {
        QueryWrapper query = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.DELETED.eq(0));
        if (StringUtils.hasText(roomNumber)) {
            query.and(ROOM.ROOM_NUMBER.like(roomNumber));
        }
        if (buildingId != null) {
            query.and(ROOM.BUILDING_ID.eq(buildingId));
        }
        if (floorId != null) {
            query.and(ROOM.FLOOR_ID.eq(floorId));
        }
        if (roomTypeId != null) {
            query.and(ROOM.ROOM_TYPE_ID.eq(roomTypeId));
        }
        if (status != null && !status.isEmpty()) {
            query.and(ROOM.STATUS.in(status));
        }
        if (orientations != null && !orientations.isEmpty()) {
            query.and(ROOM.ORIENTATION.in(orientations));
        }
        if (viewTypes != null && !viewTypes.isEmpty()) {
            query.and(ROOM.VIEW_TYPE.in(viewTypes));
        }
        if (specialTags != null && !specialTags.isEmpty()) {
            for (int i = 0; i < specialTags.size(); i++) {
                if (i == 0) {
                    query.and(ROOM.SPECIAL_TAGS.like(specialTags.get(i)));
                } else {
                    query.or(ROOM.SPECIAL_TAGS.like(specialTags.get(i)));
                }
            }
        }
        return query;
    }

    private QueryWrapper buildQueryWrapper(String roomNumber, Long buildingId, Long floorId,
                                           Long roomTypeId, List<Integer> status,
                                           List<String> orientations, List<String> viewTypes,
                                           List<String> specialTags, List<Long> floorIds) {
        QueryWrapper query = buildQueryWrapper(roomNumber, buildingId, floorId, roomTypeId,
                status, orientations, viewTypes, specialTags);
        if (floorIds != null && !floorIds.isEmpty()) {
            query.and(ROOM.FLOOR_ID.in(floorIds));
        }
        return query;
    }

    public Room getById(Long id) {
        Room room = roomMapper.selectOneById(id);
        if (room != null) {
            fillRoomAssociations(room);
            RoomType roomType = roomTypeMapper.selectOneById(room.getRoomTypeId());
            if (roomType != null) {
                QueryWrapper imageQuery = QueryWrapper.create()
                        .from(HotelImage.class)
                        .where(HOTEL_IMAGE.REF_TYPE.eq("roomType"))
                        .and(HOTEL_IMAGE.REF_ID.eq(roomType.getId()))
                        .orderBy(HOTEL_IMAGE.SORT_ORDER.asc());
                roomType.setImages(hotelImageMapper.selectListByQuery(imageQuery));
                room.setRoomType(roomType);
            }
        }
        return room;
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(Room room) {
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.ROOM_NUMBER.eq(room.getRoomNumber()))
                .and(ROOM.DELETED.eq(0));
        long count = roomMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            throw new BusinessException("房间号已存在");
        }
        Floor floor = floorMapper.selectOneById(room.getFloorId());
        if (floor == null || !floor.getBuildingId().equals(room.getBuildingId())) {
            throw new BusinessException("楼层不属于该楼栋");
        }
        room.setStatus(1);
        room.setDeleted(0);
        room.setCreateTime(LocalDateTime.now());
        room.setUpdateTime(LocalDateTime.now());
        roomMapper.insert(room);
        floorService.updateRoomCount(room.getFloorId());
        roomTypeService.updateRoomCount(room.getRoomTypeId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Room room) {
        Room existing = roomMapper.selectOneById(room.getId());
        if (existing == null) {
            throw new BusinessException("房间不存在");
        }
        existing.setOrientation(room.getOrientation());
        existing.setViewType(room.getViewType());
        existing.setLocationFeatures(room.getLocationFeatures());
        existing.setSpecialTags(room.getSpecialTags());
        existing.setRemark(room.getRemark());
        existing.setUpdateTime(LocalDateTime.now());
        roomMapper.update(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Room room = roomMapper.selectOneById(id);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        room.setDeleted(1);
        room.setUpdateTime(LocalDateTime.now());
        roomMapper.update(room);
        floorService.updateRoomCount(room.getFloorId());
        roomTypeService.updateRoomCount(room.getRoomTypeId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchCreate(Long buildingId, Long floorId, Long roomTypeId,
                                            String numberPrefix, Integer startNum, Integer endNum,
                                            String orientation, String viewType) {
        int success = 0;
        List<Map<String, String>> failures = new ArrayList<>();
        for (int i = startNum; i <= endNum; i++) {
            String roomNumber = numberPrefix + i;
            QueryWrapper checkQuery = QueryWrapper.create()
                    .from(Room.class)
                    .where(ROOM.ROOM_NUMBER.eq(roomNumber))
                    .and(ROOM.DELETED.eq(0));
            long count = roomMapper.selectCountByQuery(checkQuery);
            if (count > 0) {
                Map<String, String> fail = new HashMap<>();
                fail.put("roomNumber", roomNumber);
                fail.put("reason", "房间号已存在");
                failures.add(fail);
                continue;
            }
            Room room = new Room();
            room.setRoomNumber(roomNumber);
            room.setBuildingId(buildingId);
            room.setFloorId(floorId);
            room.setRoomTypeId(roomTypeId);
            room.setOrientation(orientation);
            room.setViewType(viewType);
            room.setStatus(1);
            room.setDeleted(0);
            room.setCreateTime(LocalDateTime.now());
            room.setUpdateTime(LocalDateTime.now());
            roomMapper.insert(room);
            success++;
        }
        floorService.updateRoomCount(floorId);
        roomTypeService.updateRoomCount(roomTypeId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failures", failures);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long roomId, Integer newStatus, Long operatorId,
                              String operatorName, String remark) {
        Room room = roomMapper.selectOneById(roomId);
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        Integer oldStatus = room.getStatus();
        room.setStatus(newStatus);
        room.setUpdateTime(LocalDateTime.now());
        roomMapper.update(room);
        RoomStatusLog log = new RoomStatusLog();
        log.setRoomId(roomId);
        log.setRoomNumber(room.getRoomNumber());
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setOperatorId(operatorId);
        log.setOperator(operatorName);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        roomStatusLogMapper.insert(log);
    }

    @Transactional(rollbackFor = Exception.class)
    public Room copyRoom(Long sourceRoomId, Room newRoom) {
        Room source = roomMapper.selectOneById(sourceRoomId);
        if (source == null) {
            throw new BusinessException("源房间不存在");
        }
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.ROOM_NUMBER.eq(newRoom.getRoomNumber()))
                .and(ROOM.DELETED.eq(0));
        long count = roomMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            throw new BusinessException("房间号已存在");
        }
        if (newRoom.getFloorId() != null) {
            Floor floor = floorMapper.selectOneById(newRoom.getFloorId());
            if (floor == null || !floor.getBuildingId().equals(newRoom.getBuildingId() != null ? newRoom.getBuildingId() : source.getBuildingId())) {
                throw new BusinessException("楼层不属于该楼栋");
            }
        }
        Room copied = new Room();
        copied.setRoomNumber(newRoom.getRoomNumber());
        copied.setBuildingId(newRoom.getBuildingId() != null ? newRoom.getBuildingId() : source.getBuildingId());
        copied.setFloorId(newRoom.getFloorId() != null ? newRoom.getFloorId() : source.getFloorId());
        copied.setRoomTypeId(newRoom.getRoomTypeId() != null ? newRoom.getRoomTypeId() : source.getRoomTypeId());
        copied.setOrientation(newRoom.getOrientation() != null ? newRoom.getOrientation() : source.getOrientation());
        copied.setViewType(newRoom.getViewType() != null ? newRoom.getViewType() : source.getViewType());
        copied.setLocationFeatures(newRoom.getLocationFeatures() != null ? newRoom.getLocationFeatures() : source.getLocationFeatures());
        copied.setSpecialTags(newRoom.getSpecialTags() != null ? newRoom.getSpecialTags() : source.getSpecialTags());
        copied.setRemark(newRoom.getRemark() != null ? newRoom.getRemark() : source.getRemark());
        copied.setStatus(1);
        copied.setDeleted(0);
        copied.setCreateTime(LocalDateTime.now());
        copied.setUpdateTime(LocalDateTime.now());
        roomMapper.insert(copied);
        floorService.updateRoomCount(copied.getFloorId());
        roomTypeService.updateRoomCount(copied.getRoomTypeId());
        return copied;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> applyTemplate(Long templateRoomId, List<Long> targetRoomIds,
                                              boolean applyOrientation, boolean applyViewType,
                                              boolean applyLocationFeatures, boolean applySpecialTags,
                                              Long operatorId, String operatorName, String reason) {
        Room template = roomMapper.selectOneById(templateRoomId);
        if (template == null) {
            throw new BusinessException("模板房间不存在");
        }
        int success = 0;
        List<Map<String, String>> failures = new ArrayList<>();
        for (Long targetId : targetRoomIds) {
            if (targetId.equals(templateRoomId)) continue;
            Room target = roomMapper.selectOneById(targetId);
            if (target == null) {
                Map<String, String> fail = new HashMap<>();
                fail.put("roomNumber", String.valueOf(targetId));
                fail.put("reason", "房间不存在");
                failures.add(fail);
                continue;
            }
            if (applyOrientation) target.setOrientation(template.getOrientation());
            if (applyViewType) target.setViewType(template.getViewType());
            if (applyLocationFeatures) target.setLocationFeatures(template.getLocationFeatures());
            if (applySpecialTags) target.setSpecialTags(template.getSpecialTags());
            target.setUpdateTime(LocalDateTime.now());
            roomMapper.update(target);
            RoomStatusLog log = new RoomStatusLog();
            log.setRoomId(targetId);
            log.setRoomNumber(target.getRoomNumber());
            log.setOldStatus(target.getStatus());
            log.setNewStatus(target.getStatus());
            log.setOperatorId(operatorId);
            log.setOperator(operatorName);
            log.setRemark("模板应用（来源：" + template.getRoomNumber() + "）" + (reason != null ? " - " + reason : ""));
            log.setCreateTime(LocalDateTime.now());
            roomStatusLogMapper.insert(log);
            success++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failures", failures);
        return result;
    }

    public List<Room> listByFloorId(Long floorId) {
        QueryWrapper query = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.FLOOR_ID.eq(floorId))
                .and(ROOM.DELETED.eq(0))
                .orderBy(ROOM.ROOM_NUMBER.asc());
        List<Room> rooms = roomMapper.selectListByQuery(query);
        for (Room room : rooms) {
            fillRoomAssociations(room);
        }
        return rooms;
    }

    public List<Room> listAllForExport(String roomNumber, Long buildingId, Long floorId,
                                        Long roomTypeId, List<Integer> status,
                                        List<String> orientations, List<String> viewTypes,
                                        List<String> specialTags) {
        QueryWrapper query = buildQueryWrapper(roomNumber, buildingId, floorId, roomTypeId,
                status, orientations, viewTypes, specialTags);
        query.orderBy(ROOM.ROOM_NUMBER.asc());
        List<Room> rooms = roomMapper.selectListByQuery(query);
        for (Room room : rooms) {
            fillRoomAssociations(room);
        }
        return rooms;
    }

    private void fillRoomAssociations(Room room) {
        Building building = buildingMapper.selectOneById(room.getBuildingId());
        if (building != null) {
            room.setBuildingName(building.getBuildingName());
        }
        Floor floor = floorMapper.selectOneById(room.getFloorId());
        if (floor != null) {
            room.setFloorName(floor.getFloorName());
            room.setFloorNumber(floor.getFloorNumber());
        }
        RoomType roomType = roomTypeMapper.selectOneById(room.getRoomTypeId());
        if (roomType != null) {
            room.setRoomTypeName(roomType.getTypeName());
            QueryWrapper imageQuery = QueryWrapper.create()
                    .from(HotelImage.class)
                    .where(HOTEL_IMAGE.REF_TYPE.eq("roomType"))
                    .and(HOTEL_IMAGE.REF_ID.eq(roomType.getId()))
                    .orderBy(HOTEL_IMAGE.SORT_ORDER.asc());
            roomType.setImages(hotelImageMapper.selectListByQuery(imageQuery));
            room.setRoomType(roomType);
        }
    }
}
