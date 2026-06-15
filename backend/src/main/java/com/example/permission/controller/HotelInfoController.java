package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.HotelFacility;
import com.example.permission.entity.HotelImage;
import com.example.permission.entity.HotelInfo;
import com.example.permission.service.HotelInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotel/info")
public class HotelInfoController {

    @Autowired
    private HotelInfoService hotelInfoService;

    @GetMapping
    @PreAuthorize("hasAuthority('hotel:info:list')")
    public Result<HotelInfo> getHotelInfo() {
        HotelInfo hotelInfo = hotelInfoService.getHotelInfo();
        return Result.success(hotelInfo);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('hotel:info:edit')")
    public Result<Void> saveHotelInfo(@RequestBody HotelInfo hotelInfo) {
        hotelInfoService.saveHotelInfo(hotelInfo);
        return Result.success();
    }

    @GetMapping("/facilities/{hotelId}")
    @PreAuthorize("hasAuthority('hotel:info:query')")
    public Result<List<HotelFacility>> getFacilities(@PathVariable Long hotelId) {
        List<HotelFacility> facilities = hotelInfoService.getFacilities(hotelId);
        return Result.success(facilities);
    }

    @PostMapping("/facilities/{hotelId}")
    @PreAuthorize("hasAuthority('hotel:facility:edit')")
    public Result<Void> saveFacilities(@PathVariable Long hotelId, @RequestBody List<HotelFacility> facilities) {
        hotelInfoService.saveFacilities(hotelId, facilities);
        return Result.success();
    }

    @GetMapping("/images/{refType}/{refId}")
    @PreAuthorize("hasAuthority('hotel:info:query')")
    public Result<List<HotelImage>> getImages(@PathVariable String refType, @PathVariable Long refId) {
        List<HotelImage> images = hotelInfoService.getImages(refType, refId);
        return Result.success(images);
    }

    @PostMapping("/images/{refType}/{refId}")
    @PreAuthorize("hasAuthority('hotel:image:edit')")
    public Result<Void> saveImages(@PathVariable String refType, @PathVariable Long refId, @RequestBody List<HotelImage> images) {
        hotelInfoService.saveImages(refType, refId, images);
        return Result.success();
    }

    @PutMapping("/images/main/{imageId}")
    @PreAuthorize("hasAuthority('hotel:image:edit')")
    public Result<Void> setMainImage(@PathVariable Long imageId, @RequestParam String refType, @RequestParam Long refId) {
        hotelInfoService.setMainImage(imageId, refType, refId);
        return Result.success();
    }

    @DeleteMapping("/images/{imageId}")
    @PreAuthorize("hasAuthority('hotel:image:edit')")
    public Result<Void> deleteImage(@PathVariable Long imageId) {
        hotelInfoService.deleteImage(imageId);
        return Result.success();
    }
}
