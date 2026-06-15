package com.example.permission.mapper;

import com.mybatisflex.core.BaseMapper;
import com.example.permission.entity.SysDataPermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 数据权限Mapper接口
 */
@Mapper
public interface SysDataPermissionMapper extends BaseMapper<SysDataPermission> {
    
    /**
     * 根据角色ID查询数据权限
     */
    @Select("SELECT * FROM sys_data_permission WHERE role_id = #{roleId}")
    SysDataPermission selectByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 删除角色的数据权限
     */
    @Delete("DELETE FROM sys_data_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
