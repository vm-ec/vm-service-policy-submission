package com.vm.service.policysubmission.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

@Service
public class PublicS3 {
    private final AmazonS3 s3Client;
    private final String bucketName = "vm-ctwo-public";

    public PublicS3() {
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
    public PutObjectResult putObject(String bucket, String key, byte[] content, String contentType) {
        String targetBucket = bucket != null && !bucket.isBlank() ? bucket : bucketName;
        InputStream stream = new ByteArrayInputStream(content);
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(targetBucket)
                .key(key)
                .contentType(contentType)
                .build();
        return s3Client.putObject(key, bucket, Arrays.toString(content));
    }
}