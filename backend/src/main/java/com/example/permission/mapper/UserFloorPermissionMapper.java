package com.example.permission.mapper;

import com.mybatisflex.core.BaseMapper;
import com.example.permission.entity.UserFloorPermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFloorPermissionMapper extends BaseMapper<UserFloorPermission> {

    @Select("SELECT * FROM user_floor_permission WHERE user_id = #{userId}")
    List<UserFloorPermission> selectByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM user_floor_permission WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT user_id FROM user_floor_permission")
    List<Long> selectDistinctUserIds();
}
