package com.takkqnavi.service.impl;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.takkqnavi.service.StorageService;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.net.URI;

@Service
@Profile("prod")
@Slf4j
public class R2StorageServiceImpl implements StorageService {

    @Value("${storage.r2.endpoint}")
    private String endpoint;

    @Value("${storage.r2.access-key}")
    private String accessKey;

    @Value("${storage.r2.secret-key}")
    private String secretKey;

    @Value("${storage.r2.bucket}")
    private String bucket;

    @Value("${storage.r2.public-url}")
    private String publicUrl;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .build();
        log.info("R2 storage initialized with bucket: {}", bucket);
    }

    @Override
    public String upload(String path, InputStream inputStream, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));
            log.info("File uploaded to R2: {}", path);
            return getUrl(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to R2", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            s3Client.deleteObject(request);
            log.info("File deleted from R2: {}", path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from R2", e);
        }
    }

    @Override
    public String getUrl(String path) {
        return publicUrl + "/" + path;
    }

    @Override
    public String extractPath(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        String prefix = publicUrl + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return null;
    }
}
