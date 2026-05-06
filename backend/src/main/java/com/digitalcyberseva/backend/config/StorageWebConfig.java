package com.digitalcyberseva.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageWebConfig implements WebMvcConfigurer {

    @Value("${storage.local.base-path:uploads}")
    private String localStorageBasePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsPath = Paths.get(localStorageBasePath).toAbsolutePath().normalize();
        String uploadLocation = uploadsPath.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}
