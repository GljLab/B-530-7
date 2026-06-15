package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Building;
import com.example.permission.entity.Floor;
import com.example.permission.entity.Room;
import com.example.permission.mapper.BuildingMapper;
import com.example.permission.mapper.FloorMapper;
import com.example.permission.mapper.RoomMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.FloorTableDef.FLOOR;
import static com.example.permission.entity.table.RoomTableDef.ROOM;

@Service
public class FloorService {

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private RoomMapper roomMapper;

    public List<Floor> listByBuildingId(Long buildingId) {
        QueryWrapper query = QueryWrapper.create()
                .from(Floor.class)
                .where(FLOOR.BUILDING_ID.eq(buildingId))
                .and(FLOOR.DELETED.eq(0))
                .orderBy(FLOOR.FLOOR_NUMBER.asc());
        return floorMapper.selectListByQuery(query);
    }

    public Floor getById(Long id) {
        Floor floor = floorMapper.selectOneById(id);
        if (floor != null) {
            Building building = buildingMapper.selectOneById(floor.getBuildingId());
            if (building != null) {
                floor.setBuildingName(building.getBuildingName());
            }
        }
        return floor;
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(Floor floor) {
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(Floor.class)
                .where(FLOOR.BUILDING_ID.eq(floor.getBuildingId()))
                .and(FLOOR.FLOOR_NUMBER.eq(floor.getFloorNumber()))
                .and(FLOOR.DELETED.eq(0));
        long count = floorMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            throw new BusinessException("该楼栋下楼层号已存在");
        }
        floor.setDeleted(0);
        floor.setRoomCount(0);
        floor.setCreateTime(LocalDateTime.now());
        floor.setUpdateTime(LocalDateTime.now());
        floorMapper.insert(floor);
        updateBuildingTotalFloors(floor.getBuildingId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Floor floor) {
        Floor existing = floorMapper.selectOneById(floor.getId());
        if (existing == null) {
            throw new BusinessException("楼层不存在");
        }
        if (floor.getFloorNumber() != null && !floor.getFloorNumber().equals(existing.getFloorNumber())) {
            QueryWrapper checkQuery = QueryWrapper.create()
                    .from(Floor.class)
                    .where(FLOOR.BUILDING_ID.eq(existing.getBuildingId()))
                    .and(FLOOR.FLOOR_NUMBER.eq(floor.getFloorNumber()))
                    .and(FLOOR.DELETED.eq(0))
                    .and(FLOOR.ID.ne(floor.getId()));
            long count = floorMapper.selectCountByQuery(checkQuery);
            if (count > 0) {
                throw new BusinessException("该楼栋下楼层号已存在");
            }
        }
        floor.setUpdateTime(LocalDateTime.now());
        floorMapper.update(floor);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        QueryWrapper roomCheckQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.FLOOR_ID.eq(id))
                .and(ROOM.DELETED.eq(0));
        long roomCount = roomMapper.selectCountByQuery(roomCheckQuery);
        if (roomCount > 0) {
            throw new BusinessException("请先删除该楼层下的房间");
        }
        Floor floor = floorMapper.selectOneById(id);
        if (floor == null) {
            throw new BusinessException("楼层不存在");
        }
        floor.setDeleted(1);
        floor.setUpdateTime(LocalDateTime.now());
        floorMapper.update(floor);
        updateBuildingTotalFloors(floor.getBuildingId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRoomCount(Long floorId) {
        QueryWrapper countQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.FLOOR_ID.eq(floorId))
                .and(ROOM.DELETED.eq(0));
        long count = roomMapper.selectCountByQuery(countQuery);
        Floor floor = floorMapper.selectOneById(floorId);
        if (floor != null) {
            floor.setRoomCount((int) count);
            floor.setUpdateTime(LocalDateTime.now());
            floorMapper.update(floor);
        }
    }

    private void updateBuildingTotalFloors(Long buildingId) {
        QueryWrapper countQuery = QueryWrapper.create()
                .from(Floor.class)
                .where(FLOOR.BUILDING_ID.eq(buildingId))
                .and(FLOOR.DELETED.eq(0));
        long count = floorMapper.selectCountByQuery(countQuery);
        Building building = buildingMapper.selectOneById(buildingId);
        if (building != null) {
            building.setTotalFloors((int) count);
            building.setUpdateTime(LocalDateTime.now());
            buildingMapper.update(building);
        }
    }
}
