package com.vm.service.policysubmission.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
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
}