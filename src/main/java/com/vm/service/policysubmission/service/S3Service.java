package com.vm.service.policysubmission.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.defaultBucket:demo-bucket}")
    private String defaultBucket;

    public String health() {
        try {
            String bucket = defaultBucket;
            if (bucket == null || bucket.isBlank()) {
                // If no bucket is configured, fall back to a simple call that still requires permissions
                s3Client.listBuckets(ListBucketsRequest.builder().build());
                return "OK";
            }
            // Prefer a minimal-permission check
            HeadBucketRequest headReq = HeadBucketRequest.builder().bucket(bucket).build();
            s3Client.headBucket(headReq);
            return "OK";
        } catch (S3Exception ex) {
            return "ERROR: " + ex.awsErrorDetails().errorMessage();
        } catch (Exception ex) {
            return "ERROR: " + ex.getMessage();
        }
    }

    public PutObjectResponse putObject(String bucket, String key, byte[] content, String contentType) {
        String targetBucket = bucket != null && !bucket.isBlank() ? bucket : defaultBucket;
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .contentType(contentType)
                .build();
        return s3Client.putObject(req, RequestBody.fromBytes(content));
    }

    public PutObjectResponse putObject(String bucket, String key, MultipartFile file) throws IOException {
        return putObject(bucket, key, file.getBytes(), file.getContentType());
    }

    public ResponseBytes<GetObjectResponse> getObject(String bucket, String key) {
        String targetBucket = bucket != null && !bucket.isBlank() ? bucket : defaultBucket;
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(req);
    }

    public String getObjectPresignedUrl(String bucket, String key, Integer expiresSeconds) {
        String targetBucket = bucket != null && !bucket.isBlank() ? bucket : defaultBucket;
        int expiry = expiresSeconds != null && expiresSeconds > 0 ? expiresSeconds : 900;
        // Using virtual-hosted-style URL format. For production, consider SDK Presigner.
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8);
        return String.format("https://%s.s3.amazonaws.com/%s?temp=true&expires=%d", targetBucket, encodedKey, expiry);
    }

    public List<String> listObjects(String bucket, String prefix) {
        String targetBucket = bucket != null && !bucket.isBlank() ? bucket : defaultBucket;
        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(targetBucket)
                .prefix(prefix)
                .build();
        ListObjectsV2Response resp = s3Client.listObjectsV2(req);
        return resp.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }
}
