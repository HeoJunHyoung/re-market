package com.example.remarket.global.config;

import com.example.remarket.global.monitoring.QueryCountInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.dir}")
    private String fileDir;

    private final QueryCountInterceptor queryCountInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // '/images/**'로 들어오는 요청을 'file.dir' 경로의 파일로 매핑
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + fileDir);
    }

    // 인터셉터 등록
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(queryCountInterceptor)
                .addPathPatterns("/**") // 모든 경로에 대해 모니터링
                .excludePathPatterns("/images/**", "/css/**", "/js/**", "/favicon.ico"); // 정적 리소스 제외
    }
}