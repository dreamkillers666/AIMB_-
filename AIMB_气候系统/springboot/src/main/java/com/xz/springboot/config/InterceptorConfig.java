package com.xz.springboot.config;

import com.xz.springboot.config.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired; // <-- Add this import
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    // Inject the Spring-managed instance of JwtInterceptor
    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/**/export",
                        "/**/import",
                        "/file/**",
                        "/griddata/**",
                        "/ensodata/**",
                        "/enso/**",      // 你的预测接口路径
                        "/api/enso/**",
                        "/",
                        // --- 以下是必须新增的 Swagger 排除路径 ---
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v2/api-docs/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/doc.html",      //
                        "/favicon.ico",
                        "/error"
                );
    }

}