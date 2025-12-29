package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.s3.service.S3Service;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileImageController {

    private final S3Service s3Service;
    private final UserRepository userRepository;

    /**
     * 프로필 이미지 업로드
     * POST /users/profile-image
     */
    @PostMapping("/profile-image")
    public ResponseEntity<Map<String, String>> uploadProfileImage(
            @Auth AuthUser authUser,
            @RequestParam("image") MultipartFile image
    ) {
        // 파일 검증
        if (image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        // 이미지 파일 형식 검증
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        // S3에 업로드
        String imageUrl = s3Service.uploadFile(image);

        // User 엔티티 업데이트
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateProfileImage(imageUrl);
        userRepository.save(user);

        // 응답
        Map<String, String> response = new HashMap<>();
        response.put("profileImageUrl", imageUrl);
        response.put("message", "프로필 이미지가 성공적으로 업로드되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 사용자의 프로필 이미지 URL 조회
     * GET /users/profile-image
     */
    @GetMapping("/profile-image")
    public ResponseEntity<Map<String, String>> getProfileImage(
            @Auth AuthUser authUser
    ) {
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Map<String, String> response = new HashMap<>();
        response.put("profileImageUrl", user.getProfileImageUrl());

        return ResponseEntity.ok(response);
    }

    /**
     * Presigned URL 발급 (클라이언트가 직접 S3에 업로드할 수 있도록)
     * POST /users/profile-image/presigned-url
     */
    @PostMapping("/profile-image/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(
            @Auth AuthUser authUser,
            @RequestParam("filename") String filename
    ) {
        // 파일명 검증
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("파일명이 비어있습니다.");
        }

        // Presigned URL 생성
        String presignedUrl = s3Service.generatePresignedUrl(filename);

        // 응답
        Map<String, String> response = new HashMap<>();
        response.put("presignedUrl", presignedUrl);
        response.put("message", "Presigned URL이 생성되었습니다. 이 URL로 직접 파일을 업로드하세요.");
        response.put("expiresIn", "10분");

        return ResponseEntity.ok(response);
    }
}
