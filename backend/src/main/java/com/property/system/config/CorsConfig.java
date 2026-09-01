package com.property.system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8081,http://127.0.0.1:3000,http://127.0.0.1:5173}")
    private String allowedOrigins;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Bean
    public CorsFilter corsFilter() {
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());

        // 生产环境若沿用默认 localhost 列表，浏览器跨域请求会全部被拒且报错信息不直观，
        // 属于上线高频踩坑点，故在启动时显式告警
        if ("prod".equals(activeProfile)) {
            List<String> localOrigins = origins.stream()
                    .filter(origin -> origin.contains("localhost") || origin.contains("127.0.0.1"))
                    .collect(Collectors.toList());
            if (!localOrigins.isEmpty()) {
                log.warn("生产环境 CORS 来源仍包含本地地址 {}，前端请求会被浏览器拦截，请配置 app.cors.allowed-origins",
                        localOrigins);
            }
        }

        CorsConfiguration config = new CorsConfiguration();
        for (String origin : origins) {
            config.addAllowedOriginPattern(origin);
        }
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        log.info("CORS 已配置允许的来源: {}", origins);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
