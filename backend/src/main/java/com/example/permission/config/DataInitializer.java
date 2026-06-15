package com.example.permission.config;

import com.example.permission.entity.SysUser;
import com.example.permission.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 确保 admin 用户密码正确（解决 BCrypt 哈希不匹配问题）
 */
@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("开始检查并初始化用户数据...");
        
        // 检查 admin 用户密码是否正确
        SysUser admin = userMapper.selectByUsername("admin");
        if (admin != null) {
            // 验证密码是否匹配
            if (!passwordEncoder.matches("123456", admin.getPassword())) {
                log.info("admin 用户密码不匹配，正在重新加密...");
                admin.setPassword(passwordEncoder.encode("123456"));
                userMapper.update(admin);
                log.info("admin 用户密码已更新");
            } else {
                log.info("admin 用户密码验证通过");
            }
        } else {
            log.warn("admin 用户不存在，请检查数据库初始化脚本");
        }
        
        // 检查 test 用户密码
        SysUser test = userMapper.selectByUsername("test");
        if (test != null) {
            if (!passwordEncoder.matches("123456", test.getPassword())) {
                log.info("test 用户密码不匹配，正在重新加密...");
                test.setPassword(passwordEncoder.encode("123456"));
                userMapper.update(test);
                log.info("test 用户密码已更新");
            }
        }

        String[] testUsers = {"hotel_admin", "frontdesk_manager", "housekeeping_manager", "receptionist", "finance_staff", "housekeeper", "maintenance_manager", "maintenance_staff_a", "maintenance_staff_b"};
        for (String username : testUsers) {
            SysUser user = userMapper.selectByUsername(username);
            if (user != null) {
                if (!passwordEncoder.matches("123456", user.getPassword())) {
                    log.info("{} 用户密码不匹配，正在重新加密...", username);
                    user.setPassword(passwordEncoder.encode("123456"));
                    userMapper.update(user);
                    log.info("{} 用户密码已更新", username);
                } else {
                    log.info("{} 用户密码验证通过", username);
                }
            } else {
                log.warn("{} 用户不存在，请检查数据库初始化脚本", username);
            }
        }

        log.info("用户数据初始化完成");
    }
}
