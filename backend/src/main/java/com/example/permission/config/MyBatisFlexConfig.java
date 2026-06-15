package com.example.permission.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Flex 配置
 */
@Configuration
public class MyBatisFlexConfig implements MyBatisFlexCustomizer {
    
    private static final Logger log = LoggerFactory.getLogger(MyBatisFlexConfig.class);
    
    @Override
    public void customize(FlexGlobalConfig globalConfig) {
        // 开启审计功能（SQL打印）
        AuditManager.setAuditEnable(true);
        
        // 设置SQL审计收集器（控制台打印）
        AuditManager.setMessageCollector(auditMessage -> 
            log.debug("SQL: {} | 耗时: {}ms", auditMessage.getFullSql(), auditMessage.getElapsedTime())
        );
        
        // 配置逻辑删除
        globalConfig.setLogicDeleteColumn("deleted");
        
        // 配置正常值和删除值
        globalConfig.setNormalValueOfLogicDelete(0);
        globalConfig.setDeletedValueOfLogicDelete(1);
    }
}
