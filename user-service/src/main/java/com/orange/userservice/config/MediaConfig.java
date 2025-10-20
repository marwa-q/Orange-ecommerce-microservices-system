package com.orange.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MediaConfig implements WebMvcConfigurer {

    @Value("${app.media.avatar-dir}")
    private String avatarDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path avatarPath = Paths.get(avatarDir).toAbsolutePath().normalize();
        // Example: /media/avatars/abc.png -> file:/.../storage/avatars/abc.png
        registry.addResourceHandler("/media/avatars/**")
                .addResourceLocations(avatarPath.toUri().toString());
    }
}
