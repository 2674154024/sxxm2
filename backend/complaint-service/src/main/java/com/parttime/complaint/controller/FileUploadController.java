package com.parttime.complaint.controller;

import com.parttime.common.annotation.AuditLog;
import com.parttime.common.response.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/v1/complaint")
public class FileUploadController {

    @Value("${complaint.upload.path:uploads/complaint}")
    private String uploadPath;

    @Value("${complaint.upload.max-size:5242880}")
    private long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/jpg", "image/webp"
    );

    private Path getAbsoluteUploadPath() {
        Path path = Paths.get(uploadPath);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath();
        }
        return path;
    }

    @PostMapping("/upload")
    @AuditLog(module = "投诉管理", action = "上传证据图片")
    public R<Map<String, String>> uploadFile(@RequestParam(value = "file", required = false) MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.badRequest("文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            return R.badRequest("文件大小不能超过" + (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return R.badRequest("只支持上传图片文件（JPG、PNG、GIF、WEBP）");
        }

        try {
            String datePath = new SimpleDateFormat("yyyyMMdd").format(new Date());
            Path baseDir = getAbsoluteUploadPath();
            Path targetDir = baseDir.resolve(datePath);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

            Path targetPath = targetDir.resolve(newFilename);
            file.transferTo(targetPath.toFile());

            String fileUrl = "/uploads/complaint/" + datePath + "/" + newFilename;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFilename);
            result.put("originalName", originalFilename);

            return R.ok(result);
        } catch (IOException e) {
            return R.serverError("文件上传失败：" + e.getMessage());
        }
    }
}
