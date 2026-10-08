package com.xz.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.visualization.output-dir:generated-plots/}")
    private String visualizationOutputDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射生成的图像文件
        String location = Paths.get(visualizationOutputDir).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) location += "/";
        registry.addResourceHandler("/generated-plots/**")
                .addResourceLocations(location);

        // 如果需要，还可以添加其他静态资源映射
        // registry.addResourceHandler("/static/**")
        //         .addResourceLocations("classpath:/static/");
    }
}
