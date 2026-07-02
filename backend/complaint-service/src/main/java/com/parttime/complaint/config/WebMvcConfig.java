package com.parttime.complaint.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${complaint.upload.path:uploads/complaint}")
    private String uploadPath;

    private Path getAbsoluteUploadPath() {
        Path path = Paths.get(uploadPath);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath();
        }
        return path;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absolutePath = getAbsoluteUploadPath();
        registry.addResourceHandler("/uploads/complaint/**")
                .addResourceLocations("file:" + absolutePath.toString().replace("\\", "/") + "/");
    }
}
