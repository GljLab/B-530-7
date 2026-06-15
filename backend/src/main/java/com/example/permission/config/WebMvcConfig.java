package com.example.permission.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String appHome = System.getenv("APP_HOME");
        String uploadPath;
        if (appHome != null && !appHome.isEmpty()) {
            uploadPath = "file:" + appHome + "/uploads/";
        } else {
            uploadPath = "file:" + System.getProperty("user.dir") + "/uploads/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
