package com.vm.service.policysubmission.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;

@Service
@Slf4j
public class PublicS3 {
    private final TimestampFileName timer;
    private final AmazonS3 s3Client;
    private final String bucketName = "vm-ctwo-public";
    private final String region = "us-east-1";

    public PublicS3(TimestampFileName timer) {
        this.timer = timer;
        // Create a client with Anonymous Credentials
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
                .withRegion("us-east-1") // Use your bucket's region
                .build();
    }

    public PutObjectResult putObject(String keyName, String localFilePath) {
        try {
            PutObjectResult result = s3Client.putObject(bucketName, keyName, new File(localFilePath));
            System.out.println("Upload successful!");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String putObjectBody(String key, byte[] content) {
        InputStream stream = new ByteArrayInputStream(content);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);
        // Ensure content type is always non-null
        metadata.setContentType("application/octet-stream");
        // Do NOT set contentEncoding to null; AWS SDK v1 may NPE on null metadata entries
        // metadata.setContentEncoding(null);
        String fileName = timer.addTimestamp(key);
        log.info("Uploading to public S3: bucket={}, key={}, contentLength={}, contentType={}", bucketName, fileName, content.length, metadata.getContentType());
        s3Client.putObject(bucketName, fileName, stream, metadata);
        return String.format(
                "https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                region,
                fileName);
    }
}