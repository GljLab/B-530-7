package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.HotelImage;
import com.example.permission.entity.Room;
import com.example.permission.entity.RoomType;
import com.example.permission.mapper.HotelImageMapper;
import com.example.permission.mapper.RoomMapper;
import com.example.permission.mapper.RoomTypeMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.HotelImageTableDef.HOTEL_IMAGE;
import static com.example.permission.entity.table.RoomTableDef.ROOM;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class RoomTypeService {

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private HotelImageMapper hotelImageMapper;

    public List<RoomType> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.DELETED.eq(0))
                .orderBy(ROOM_TYPE.ID.asc());
        List<RoomType> types = roomTypeMapper.selectListByQuery(query);
        for (RoomType type : types) {
            type.setImages(loadImages(type.getId()));
        }
        return types;
    }

    public PageResult<RoomType> pageList(Integer pageNum, Integer pageSize, String bedType,
                                          Integer saleStatus, java.math.BigDecimal minPrice,
                                          java.math.BigDecimal maxPrice) {
        QueryWrapper query = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.DELETED.eq(0));
        if (StringUtils.hasText(bedType)) {
            query.and(ROOM_TYPE.BED_TYPE.eq(bedType));
        }
        if (saleStatus != null) {
            query.and(ROOM_TYPE.SALE_STATUS.eq(saleStatus));
        }
        if (minPrice != null) {
            query.and(ROOM_TYPE.BASE_PRICE.ge(minPrice));
        }
        if (maxPrice != null) {
            query.and(ROOM_TYPE.BASE_PRICE.le(maxPrice));
        }
        query.orderBy(ROOM_TYPE.ID.desc());
        Page<RoomType> page = roomTypeMapper.paginate(Page.of(pageNum, pageSize), query);
        for (RoomType type : page.getRecords()) {
            type.setImages(loadImages(type.getId()));
        }
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public RoomType getById(Long id) {
        RoomType roomType = roomTypeMapper.selectOneById(id);
        if (roomType != null) {
            roomType.setImages(loadImages(id));
        }
        return roomType;
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(RoomType roomType) {
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.TYPE_CODE.eq(roomType.getTypeCode()))
                .and(ROOM_TYPE.DELETED.eq(0));
        long count = roomTypeMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            throw new BusinessException("房型编码已存在");
        }
        roomType.setDeleted(0);
        roomType.setRoomCount(0);
        roomType.setCreateTime(LocalDateTime.now());
        roomType.setUpdateTime(LocalDateTime.now());
        roomTypeMapper.insert(roomType);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(RoomType roomType) {
        RoomType existing = roomTypeMapper.selectOneById(roomType.getId());
        if (existing == null) {
            throw new BusinessException("房型不存在");
        }
        if (roomType.getTypeCode() != null && !roomType.getTypeCode().equals(existing.getTypeCode())) {
            QueryWrapper checkQuery = QueryWrapper.create()
                    .from(RoomType.class)
                    .where(ROOM_TYPE.TYPE_CODE.eq(roomType.getTypeCode()))
                    .and(ROOM_TYPE.DELETED.eq(0))
                    .and(ROOM_TYPE.ID.ne(roomType.getId()));
            long count = roomTypeMapper.selectCountByQuery(checkQuery);
            if (count > 0) {
                throw new BusinessException("房型编码已存在");
            }
        }
        roomType.setUpdateTime(LocalDateTime.now());
        roomTypeMapper.update(roomType);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        QueryWrapper roomCheckQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.ROOM_TYPE_ID.eq(id))
                .and(ROOM.DELETED.eq(0));
        long roomCount = roomMapper.selectCountByQuery(roomCheckQuery);
        if (roomCount > 0) {
            throw new BusinessException("该房型下存在房间，无法删除");
        }
        RoomType roomType = roomTypeMapper.selectOneById(id);
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }
        roomType.setDeleted(1);
        roomType.setUpdateTime(LocalDateTime.now());
        roomTypeMapper.update(roomType);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRoomCount(Long roomTypeId) {
        QueryWrapper countQuery = QueryWrapper.create()
                .from(Room.class)
                .where(ROOM.ROOM_TYPE_ID.eq(roomTypeId))
                .and(ROOM.DELETED.eq(0));
        long count = roomMapper.selectCountByQuery(countQuery);
        RoomType roomType = roomTypeMapper.selectOneById(roomTypeId);
        if (roomType != null) {
            roomType.setRoomCount((int) count);
            roomType.setUpdateTime(LocalDateTime.now());
            roomTypeMapper.update(roomType);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSaleStatus(Long id, Integer saleStatus) {
        RoomType roomType = roomTypeMapper.selectOneById(id);
        if (roomType == null) {
            throw new BusinessException("房型不存在");
        }
        roomType.setSaleStatus(saleStatus);
        roomType.setUpdateTime(LocalDateTime.now());
        roomTypeMapper.update(roomType);
    }

    private List<HotelImage> loadImages(Long roomTypeId) {
        QueryWrapper query = QueryWrapper.create()
                .from(HotelImage.class)
                .where(HOTEL_IMAGE.REF_TYPE.eq("roomType"))
                .and(HOTEL_IMAGE.REF_ID.eq(roomTypeId))
                .orderBy(HOTEL_IMAGE.SORT_ORDER.asc());
        return hotelImageMapper.selectListByQuery(query);
    }
}
