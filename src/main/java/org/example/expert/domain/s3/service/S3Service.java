package org.example.expert.domain.s3.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    /**
     * 파일을 S3에 업로드하고 URL 반환
     */
    public String uploadFile(MultipartFile file) {
        // 원본 파일명
        String originalFilename = file.getOriginalFilename();

        // 고유 파일명 생성 (UUID + 원본 파일명)
        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

        // S3 업로드 경로 (profile-images 폴더에 저장)
        String s3Key = "profile-images/" + uniqueFilename;

        try {
            // S3 업로드 요청
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            // 파일 업로드
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // 업로드된 파일의 URL 반환
            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, s3Key);

            log.info("파일 업로드 성공: {}", fileUrl);
            return fileUrl;

        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * Presigned URL 생성 (클라이언트가 직접 S3에 업로드할 수 있도록)
     */
    public String generatePresignedUrl(String filename) {
        // 고유 파일명 생성
        String uniqueFilename = UUID.randomUUID() + "_" + filename;

        // S3 업로드 경로
        String s3Key = "profile-images/" + uniqueFilename;

        try {
            // PutObject 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            // Presigned URL 요청 생성 (10분 유효)
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(putObjectRequest)
                    .build();

            // Presigned URL 생성
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("Presigned URL 생성 성공: {}", presignedUrl);
            return presignedUrl;

        } catch (Exception e) {
            log.error("Presigned URL 생성 실패", e);
            throw new RuntimeException("Presigned URL 생성에 실패했습니다.", e);
        }
    }

    /**
     * S3 파일 URL 생성
     */
    public String getFileUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName, region, s3Key);
    }
}
