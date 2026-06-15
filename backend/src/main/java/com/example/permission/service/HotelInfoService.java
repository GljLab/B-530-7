package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.HotelFacility;
import com.example.permission.entity.HotelImage;
import com.example.permission.entity.HotelInfo;
import com.example.permission.mapper.HotelFacilityMapper;
import com.example.permission.mapper.HotelImageMapper;
import com.example.permission.mapper.HotelInfoMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.HotelFacilityTableDef.HOTEL_FACILITY;
import static com.example.permission.entity.table.HotelImageTableDef.HOTEL_IMAGE;
import static com.example.permission.entity.table.HotelInfoTableDef.HOTEL_INFO;

@Service
public class HotelInfoService {

    @Autowired
    private HotelInfoMapper hotelInfoMapper;

    @Autowired
    private HotelFacilityMapper hotelFacilityMapper;

    @Autowired
    private HotelImageMapper hotelImageMapper;

    public HotelInfo getHotelInfo() {
        QueryWrapper query = QueryWrapper.create()
                .from(HotelInfo.class)
                .where(HOTEL_INFO.DELETED.eq(0));
        HotelInfo hotel = hotelInfoMapper.selectOneByQuery(query);
        if (hotel != null) {
            hotel.setFacilities(getFacilities(hotel.getId()));
            hotel.setImages(getImages("hotel", hotel.getId()));
        }
        return hotel;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveHotelInfo(HotelInfo hotelInfo) {
        hotelInfo.setUpdateTime(LocalDateTime.now());
        QueryWrapper existQuery = QueryWrapper.create()
                .from(HotelInfo.class)
                .where(HOTEL_INFO.DELETED.eq(0));
        HotelInfo existing = hotelInfoMapper.selectOneByQuery(existQuery);
        if (existing != null) {
            hotelInfo.setId(existing.getId());
            hotelInfo.setCreateTime(existing.getCreateTime());
            hotelInfo.setDeleted(0);
            hotelInfoMapper.update(hotelInfo);
        } else {
            hotelInfo.setCreateTime(LocalDateTime.now());
            hotelInfo.setDeleted(0);
            hotelInfoMapper.insert(hotelInfo);
        }
    }

    public List<HotelFacility> getFacilities(Long hotelId) {
        QueryWrapper query = QueryWrapper.create()
                .from(HotelFacility.class)
                .where(HOTEL_FACILITY.HOTEL_ID.eq(hotelId))
                .orderBy(HOTEL_FACILITY.SORT_ORDER.asc());
        return hotelFacilityMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveFacilities(Long hotelId, List<HotelFacility> facilities) {
        QueryWrapper deleteQuery = QueryWrapper.create()
                .from(HotelFacility.class)
                .where(HOTEL_FACILITY.HOTEL_ID.eq(hotelId));
        hotelFacilityMapper.deleteByQuery(deleteQuery);
        for (HotelFacility facility : facilities) {
            facility.setHotelId(hotelId);
            facility.setCreateTime(LocalDateTime.now());
            facility.setUpdateTime(LocalDateTime.now());
            hotelFacilityMapper.insert(facility);
        }
    }

    public List<HotelImage> getImages(String refType, Long refId) {
        QueryWrapper query = QueryWrapper.create()
                .from(HotelImage.class)
                .where(HOTEL_IMAGE.REF_TYPE.eq(refType))
                .and(HOTEL_IMAGE.REF_ID.eq(refId))
                .orderBy(HOTEL_IMAGE.SORT_ORDER.asc());
        return hotelImageMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveImages(String refType, Long refId, List<HotelImage> images) {
        QueryWrapper deleteQuery = QueryWrapper.create()
                .from(HotelImage.class)
                .where(HOTEL_IMAGE.REF_TYPE.eq(refType))
                .and(HOTEL_IMAGE.REF_ID.eq(refId));
        hotelImageMapper.deleteByQuery(deleteQuery);
        for (HotelImage image : images) {
            image.setRefType(refType);
            image.setRefId(refId);
            image.setCreateTime(LocalDateTime.now());
            hotelImageMapper.insert(image);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void setMainImage(Long imageId, String refType, Long refId) {
        QueryWrapper resetQuery = QueryWrapper.create()
                .from(HotelImage.class)
                .where(HOTEL_IMAGE.REF_TYPE.eq(refType))
                .and(HOTEL_IMAGE.REF_ID.eq(refId));
        List<HotelImage> images = hotelImageMapper.selectListByQuery(resetQuery);
        for (HotelImage img : images) {
            img.setIsMain(0);
            hotelImageMapper.update(img);
        }
        HotelImage mainImage = hotelImageMapper.selectOneById(imageId);
        if (mainImage == null) {
            throw new BusinessException("图片不存在");
        }
        mainImage.setIsMain(1);
        hotelImageMapper.update(mainImage);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteImage(Long imageId) {
        hotelImageMapper.deleteById(imageId);
    }
}
