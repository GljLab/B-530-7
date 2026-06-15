package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.SysMenu;
import com.example.permission.entity.SysRole;
import com.example.permission.entity.SysUser;
import com.example.permission.entity.SysUserRole;
import com.example.permission.mapper.SysMenuMapper;
import com.example.permission.mapper.SysRoleMapper;
import com.example.permission.mapper.SysUserMapper;
import com.example.permission.mapper.SysUserRoleMapper;
import com.example.permission.security.LoginUser;
import com.example.permission.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证服务
 */
@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private SysMenuMapper menuMapper;
    
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 用户登录
     */
    public Map<String, Object> login(String username, String password) {
        try {
            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            
            // 获取登录用户信息
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            SysUser user = loginUser.getUser();
            
            // 生成Token
            String token = jwtUtils.generateToken(user.getId(), user.getUsername());
            
            // 查询用户角色
            List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
            
            // 查询用户菜单
            List<SysMenu> menus = menuMapper.selectMenusByUserId(user.getId());
            
            // 构建用户树形菜单
            List<SysMenu> menuTree = buildMenuTree(menus, 0L);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("user", buildUserInfo(user, roles));
            result.put("menus", menuTree);
            result.put("permissions", loginUser.getPermissions());
            
            return result;
        } catch (BadCredentialsException e) {
            throw new BusinessException("用户名或密码错误");
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            throw new BusinessException("登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password, String nickname) {
        // 检查用户名是否已存在
        SysUser existUser = userMapper.selectByUsername(username);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建新用户
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // 默认分配普通用户角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(2L); // 普通用户角色ID
        userRoleMapper.insert(userRole);
    }
    
    /**
     * 获取当前用户信息
     */
    public Map<String, Object> getCurrentUserInfo(Long userId) {
        SysUser user = userMapper.selectOneById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 查询用户角色
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        
        // 查询用户菜单
        List<SysMenu> menus = menuMapper.selectMenusByUserId(userId);
        
        // 构建用户树形菜单
        List<SysMenu> menuTree = buildMenuTree(menus, 0L);
        
        // 查询用户权限
        List<String> permissions = userMapper.selectPermissionsByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", buildUserInfo(user, roles));
        result.put("menus", menuTree);
        result.put("permissions", permissions);
        
        return result;
    }
    
    /**
     * 构建用户信息（去除敏感信息）
     */
    private Map<String, Object> buildUserInfo(SysUser user, List<SysRole> roles) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("status", user.getStatus());
        userInfo.put("roles", roles);
        return userInfo;
    }
    
    /**
     * 构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getId())))
                .collect(java.util.stream.Collectors.toList());
    }
}
