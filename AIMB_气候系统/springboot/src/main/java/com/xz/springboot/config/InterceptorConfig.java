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
        registry.addInterceptor(jwtInterceptor) // Use the injected interceptor
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/**/export",
                        "/**/import",
                        "/file/**",
                        "/griddata/**", // This rule will now be respected
                        "/ensodata/**",
                        "/enso/**",
                        "/api/enso/**",
                        "/"
                );
    }

}