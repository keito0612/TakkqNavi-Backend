package com.takkqnavi.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.takkqnavi.service.StorageService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Profile({"dev", "test"})
@Slf4j
public class LocalStorageServiceImpl implements StorageService {

    @Value("${storage.local.base-path:./uploads}")
    private String basePath;

    @Value("${storage.local.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(basePath));
            log.info("Local storage initialized at: {}", basePath);
        } catch (IOException e) {
            log.error("Failed to create local storage directory", e);
        }
    }

    @Override
    public String upload(String path, InputStream inputStream, String contentType) {
        try {
            Path filePath = Paths.get(basePath, path);
            Files.createDirectories(filePath.getParent());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File uploaded to local storage: {}", path);
            return getUrl(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to local storage", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path filePath = Paths.get(basePath, path);
            Files.deleteIfExists(filePath);
            log.info("File deleted from local storage: {}", path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from local storage", e);
        }
    }

    @Override
    public String getUrl(String path) {
        return baseUrl + "/" + path;
    }

    @Override
    public String extractPath(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        String prefix = baseUrl + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return null;
    }
}
