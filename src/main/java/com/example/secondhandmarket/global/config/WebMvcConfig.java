package com.example.secondhandmarket.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.dir}")
    private String fileDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // '/images/**'로 들어오는 요청을 'file.dir' 경로의 파일로 매핑
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + fileDir);
    }
}