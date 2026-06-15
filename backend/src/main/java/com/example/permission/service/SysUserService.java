package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.SysUser;
import com.example.permission.entity.SysUserRole;
import com.example.permission.mapper.SysRoleMapper;
import com.example.permission.mapper.SysUserMapper;
import com.example.permission.mapper.SysUserRoleMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.SysUserTableDef.SYS_USER;

/**
 * 用户服务
 */
@Service
public class SysUserService {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 分页查询用户
     */
    public PageResult<SysUser> pageList(Integer pageNum, Integer pageSize, String username, Integer status) {
        QueryWrapper query = QueryWrapper.create()
                .from(SysUser.class);
        
        if (StringUtils.hasText(username)) {
            query.and(SYS_USER.USERNAME.like(username).or(SYS_USER.NICKNAME.like(username)));
        }
        if (status != null) {
            query.and(SYS_USER.STATUS.eq(status));
        }
        query.orderBy(SYS_USER.CREATE_TIME.desc());
        
        Page<SysUser> page = userMapper.paginate(Page.of(pageNum, pageSize), query);
        
        // 查询每个用户的角色
        for (SysUser user : page.getRecords()) {
            user.setPassword(null); // 不返回密码
            List<Long> roleIds = roleMapper.selectRoleIdsByUserId(user.getId());
            user.setRoleIds(roleIds);
        }
        
        return new PageResult<>(page.getTotalRow(), page.getRecords(), 
                (long) page.getPageNumber(), (long) page.getPageSize());
    }
    
    /**
     * 根据用户名查询用户
     */
    public SysUser getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }
    
    /**
     * 新增用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUser user) {
        // 检查用户名是否已存在
        SysUser existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(user.getStatus() != null ? user.getStatus() : 1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // 添加用户角色关联（默认普通用户角色）
        if (user.getRoleIds() == null || user.getRoleIds().isEmpty()) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(2L); // 默认普通用户角色
            userRoleMapper.insert(userRole);
        } else {
            for (Long roleId : user.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }
    
    /**
     * 更新用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user) {
        SysUser existUser = userMapper.selectOneById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查用户名是否重复
        if (StringUtils.hasText(user.getUsername()) && 
            !user.getUsername().equals(existUser.getUsername())) {
            SysUser sameNameUser = userMapper.selectByUsername(user.getUsername());
            if (sameNameUser != null) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        // 如果密码不为空，则加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(existUser.getPassword()); // 保留原密码
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        
        // 更新用户角色关联
        if (user.getRoleIds() != null) {
            userRoleMapper.deleteByUserId(user.getId());
            for (Long roleId : user.getRoleIds()) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }
    
    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        SysUser user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能删除管理员账号
        if (userId == 1L) {
            throw new BusinessException("不能删除超级管理员账号");
        }
        
        // 删除用户角色关联
        userRoleMapper.deleteByUserId(userId);
        
        // 逻辑删除用户
        user.setDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }
    
    /**
     * 修改用户状态
     */
    public void updateStatus(Long userId, Integer status) {
        SysUser user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (userId == 1L) {
            throw new BusinessException("不能修改超级管理员状态");
        }
        
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }
    
    /**
     * 重置密码
     */
    public void resetPassword(Long userId, String newPassword) {
        SysUser user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }
    
    /**
     * 修改密码
     */
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }
    
    /**
     * 获取用户详情（包含角色）
     */
    public SysUser getUserDetail(Long userId) {
        SysUser user = userMapper.selectOneById(userId);
        if (user != null) {
            user.setPassword(null);
            user.setRoleIds(roleMapper.selectRoleIdsByUserId(userId));
            user.setRoles(roleMapper.selectRolesByUserId(userId));
        }
        return user;
    }
    
    /**
     * 更新用户信息（个人中心使用）
     */
    public void updateProfile(SysUser user) {
        SysUser existUser = userMapper.selectOneById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 只更新允许修改的字段
        existUser.setNickname(user.getNickname());
        existUser.setEmail(user.getEmail());
        existUser.setPhone(user.getPhone());
        existUser.setAvatar(user.getAvatar());
        existUser.setUpdateTime(LocalDateTime.now());
        
        userMapper.update(existUser);
    }
}
