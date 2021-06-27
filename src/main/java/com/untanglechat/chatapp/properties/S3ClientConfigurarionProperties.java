package com.untanglechat.chatapp.properties;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import software.amazon.awssdk.regions.Region;

@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3ClientConfigurarionProperties {

    private Region region = Region.AP_SOUTH_1;
    private URI endpoint = null;

    @Value("${amazon.s3.access-key}")
    private String accessKeyId;
    @Value("${amazon.s3.secret-key}")
    private String secretAccessKey;
    
    // Bucket name we'll be using as our backend storage
    @Value("${amazon.s3.bucket-name}")
    private String bucket;

    // AWS S3 requires that file parts must have at least 5MB, except
    // for the last part. This may change for other S3-compatible services, so let't
    // define a configuration property for that
    private int multipartMinPartSize = 5*1024*1024;

}