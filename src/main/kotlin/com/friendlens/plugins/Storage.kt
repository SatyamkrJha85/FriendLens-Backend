package com.friendlens.plugins

import com.friendlens.EnvConfig
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ListBucketsRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import io.ktor.server.application.*
import java.net.URI

object StorageService {
    lateinit var s3: S3Client
    lateinit var bucketName: String
    
    fun init(log: io.ktor.util.logging.Logger) {
        try {
            val accessKey = EnvConfig.getOrThrow("AWS_ACCESS_KEY_ID")
            val secretKey = EnvConfig.getOrThrow("AWS_SECRET_ACCESS_KEY")
            val endpoint = EnvConfig.getOrThrow("AWS_S3_ENDPOINT")
            val regionStr = EnvConfig.getOrThrow("AWS_S3_REGION")
            bucketName = EnvConfig.getOrThrow("AWS_S3_BUCKET_NAME")

            val credentials = AwsBasicCredentials.create(accessKey, secretKey)
            
            s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(regionStr))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                // Supabase / R2 works best with PathStyleAccess enabled
                .forcePathStyle(true)
                .build()

            // Test connection by listing buckets or catching error implicitly
            val buckets = s3.listBuckets(ListBucketsRequest.builder().build()).buckets()
            val hasBucket = buckets.any { it.name() == bucketName }
            if (hasBucket) {
                log.info(" Supabase S3 connected successfully! Target bucket: $bucketName")
            } else {
                log.warn(" Supabase S3 connected, but bucket $bucketName was NOT found. Please make sure it's created.")
            }
        } catch (e: Exception) {
           log.error(" Supabase S3 connection failed! ${e.message}")
        }
    }
}

fun Application.configureStorage() {
    StorageService.init(log)
}
