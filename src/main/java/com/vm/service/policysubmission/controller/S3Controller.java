package com.vm.service.policysubmission.controller;

import com.vm.service.policysubmission.service.S3Service;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
@Slf4j
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> resp = new HashMap<>();
        String status = s3Service.health();
        resp.put("status", status.startsWith("OK") ? "UP" : "DOWN");
        resp.put("details", status);
        log.info("Controller S3 health check: {}", status);
        return resp;
    }

    @PostMapping(value = "/put", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> putMultipart(
            @RequestParam(value = "bucket", required = false) String bucket,
            @RequestParam("key") String key,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        log.info("Received file upload request: bucket={}, key={}, originalFilename={}, size={}", bucket, key, file.getOriginalFilename(), file.getSize());

        PutObjectResponse resp = s3Service.putObject(bucket, key, file);
        Map<String, Object> out = new HashMap<>();
        out.put("bucket", bucket);
        out.put("key", key);
        out.put("eTag", resp.eTag());
        out.put("versionId", resp.versionId());
        out.put("result", "UPLOADED");
        return out;
    }

    @PostMapping(value = "/put-file-public", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> putBytes(@RequestParam String fileName, String contentType, @RequestParam byte[] contentBytes) {
        PutObjectResponse resp = s3Service.putObject("vm-ctwo-public", fileName, contentBytes, "application/octet-stream");
        Map<String, Object> out = new HashMap<>();
        out.put("eTag", resp.eTag());
        out.put("versionId", resp.versionId());
        out.put("result", "UPLOADED");
        return out;
    }

    @PostMapping(value = "/put-bytes-public", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> putBytes(@RequestParam String key, @RequestParam byte[] content) {
        PutObjectResponse resp = s3Service.putObject("vm-ctwo-public", key, content, "application/bytes");
        Map<String, Object> out = new HashMap<>();
        out.put("eTag", resp.eTag());
        out.put("versionId", resp.versionId());
        out.put("result", "UPLOADED");
        return out;
    }

    @PostMapping(value = "/put-bytes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> putBytes(@RequestBody PutBytesRequest request) {
        PutObjectResponse resp = s3Service.putObject(request.getBucket(), request.getKey(), request.getContent(), request.getContentType());
        Map<String, Object> out = new HashMap<>();
        out.put("bucket", request.getBucket());
        out.put("key", request.getKey());
        out.put("eTag", resp.eTag());
        out.put("versionId", resp.versionId());
        out.put("result", "UPLOADED");
        return out;
    }

    @GetMapping(value = "/get")
    public ResponseEntity<byte[]> get(
            @RequestParam(value = "bucket", required = false) String bucket,
            @RequestParam("key") String key
    ) {
        ResponseBytes<GetObjectResponse> data = s3Service.getObject(bucket, key);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + key)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data.asByteArray());
    }

    @GetMapping(value = "/get-url")
    public Map<String, Object> getUrl(
            @RequestParam(value = "bucket", required = false) String bucket,
            @RequestParam("key") String key,
            @RequestParam(value = "expiresSeconds", required = false) Integer expiresSeconds
    ) {
        String url = s3Service.getObjectPresignedUrl(bucket, key, expiresSeconds);
        Map<String, Object> out = new HashMap<>();
        out.put("url", url);
        out.put("expiresSeconds", expiresSeconds);
        return out;
    }

    @GetMapping(value = "/list")
    public Map<String, Object> list(
            @RequestParam(value = "bucket", required = false) String bucket,
            @RequestParam(value = "prefix", required = false) String prefix
    ) {
        List<String> keys = s3Service.listObjects(bucket, prefix);
        Map<String, Object> out = new HashMap<>();
        out.put("count", keys.size());
        out.put("keys", keys);
        return out;
    }

    @Data
    public static class PutBytesRequest {
        private String bucket;
        private String key;
        private String contentType;
        private byte[] content;
    }
}
