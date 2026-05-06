package com.digitalcyberseva.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Configuration
public class CorsConfig {

    private static final List<String> ALLOWED_ORIGIN_PATTERNS = List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://0.0.0.0:*",
            "http://[::1]:*",
            "http://192.168.*:*",
            "http://10.*:*",
            "http://172.*:*",
            "https://localhost:*",
            "https://127.0.0.1:*",
            "capacitor://localhost",
            "ionic://localhost"
    );

    private static final List<String> ALLOWED_METHODS = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private static final List<String> EXPOSED_HEADERS = List.of("Authorization");

    @Value("${app.cors.additional-origin-patterns:}")
    private String additionalOriginPatternsRaw;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(resolveAllowedOriginPatterns());
        configuration.setAllowedMethods(ALLOWED_METHODS);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(EXPOSED_HEADERS);
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> resolveAllowedOriginPatterns() {
        List<String> patterns = new ArrayList<>(ALLOWED_ORIGIN_PATTERNS);

        if (StringUtils.hasText(additionalOriginPatternsRaw)) {
            Stream.of(additionalOriginPatternsRaw.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .forEach(patterns::add);
        }

        return patterns;
    }
}
