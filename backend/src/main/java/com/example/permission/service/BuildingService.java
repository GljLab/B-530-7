package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Building;
import com.example.permission.entity.Floor;
import com.example.permission.mapper.BuildingMapper;
import com.example.permission.mapper.FloorMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.BuildingTableDef.BUILDING;
import static com.example.permission.entity.table.FloorTableDef.FLOOR;

@Service
public class BuildingService {

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private FloorMapper floorMapper;

    public List<Building> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(Building.class)
                .where(BUILDING.DELETED.eq(0))
                .orderBy(BUILDING.ID.asc());
        List<Building> buildings = buildingMapper.selectListByQuery(query);
        for (Building building : buildings) {
            QueryWrapper floorQuery = QueryWrapper.create()
                    .from(Floor.class)
                    .where(FLOOR.BUILDING_ID.eq(building.getId()))
                    .and(FLOOR.DELETED.eq(0))
                    .orderBy(FLOOR.FLOOR_NUMBER.asc());
            building.setFloors(floorMapper.selectListByQuery(floorQuery));
        }
        return buildings;
    }

    public Building getById(Long id) {
        Building building = buildingMapper.selectOneById(id);
        if (building != null) {
            QueryWrapper floorQuery = QueryWrapper.create()
                    .from(Floor.class)
                    .where(FLOOR.BUILDING_ID.eq(building.getId()))
                    .and(FLOOR.DELETED.eq(0))
                    .orderBy(FLOOR.FLOOR_NUMBER.asc());
            building.setFloors(floorMapper.selectListByQuery(floorQuery));
        }
        return building;
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(Building building) {
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(Building.class)
                .where(BUILDING.BUILDING_CODE.eq(building.getBuildingCode()))
                .and(BUILDING.DELETED.eq(0));
        long count = buildingMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            throw new BusinessException("楼栋编码已存在");
        }
        building.setDeleted(0);
        building.setCreateTime(LocalDateTime.now());
        building.setUpdateTime(LocalDateTime.now());
        buildingMapper.insert(building);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Building building) {
        Building existing = buildingMapper.selectOneById(building.getId());
        if (existing == null) {
            throw new BusinessException("楼栋不存在");
        }
        if (building.getBuildingCode() != null && !building.getBuildingCode().equals(existing.getBuildingCode())) {
            QueryWrapper checkQuery = QueryWrapper.create()
                    .from(Building.class)
                    .where(BUILDING.BUILDING_CODE.eq(building.getBuildingCode()))
                    .and(BUILDING.DELETED.eq(0))
                    .and(BUILDING.ID.ne(building.getId()));
            long count = buildingMapper.selectCountByQuery(checkQuery);
            if (count > 0) {
                throw new BusinessException("楼栋编码已存在");
            }
        }
        building.setUpdateTime(LocalDateTime.now());
        buildingMapper.update(building);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        QueryWrapper floorCheckQuery = QueryWrapper.create()
                .from(Floor.class)
                .where(FLOOR.BUILDING_ID.eq(id))
                .and(FLOOR.DELETED.eq(0));
        long floorCount = floorMapper.selectCountByQuery(floorCheckQuery);
        if (floorCount > 0) {
            throw new BusinessException("请先删除该楼栋下的楼层");
        }
        Building building = buildingMapper.selectOneById(id);
        if (building == null) {
            throw new BusinessException("楼栋不存在");
        }
        building.setDeleted(1);
        building.setUpdateTime(LocalDateTime.now());
        buildingMapper.update(building);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Building building = buildingMapper.selectOneById(id);
        if (building == null) {
            throw new BusinessException("楼栋不存在");
        }
        building.setStatus(status);
        building.setUpdateTime(LocalDateTime.now());
        buildingMapper.update(building);
    }
}
