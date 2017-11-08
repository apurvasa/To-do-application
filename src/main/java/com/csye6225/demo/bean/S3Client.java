package com.csye6225.demo.bean;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.stereotype.Service;

@Service
public class S3Client {

    public AmazonS3 getS3Client(){

     AWSCredentials credentials = new BasicAWSCredentials("$ACCESS_ID", "mfnb/gFl/tSpmqGu1Hc03Wj3ncV1eVvCtw6XaTtI");

        AmazonS3 s3client = new AmazonS3Client(credentials);

        return s3client;
    }


}
