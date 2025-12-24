package org.example.expert.domain.user.service;

import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 닉네임 검색 성능 테스트
 * 500만 건 데이터에서 검색 성능 측정
 */
@Slf4j
@SpringBootTest
@Transactional(readOnly = true)
class UserSearchPerformanceTest {

    @Autowired
    private UserService userService;

    /**
     * 닉네임 검색 성능 테스트
     *
     * 테스트 전 준비:
     * 1. UserBulkInsertTest.bulkInsertUsers() 실행하여 500만 건 데이터 생성
     * 2. 검색할 닉네임 확인 (예: user_12345678_1000000)
     *
     * 성능 측정 시나리오:
     * 1. 인덱스 없이 검색 (Full Table Scan)
     * 2. 인덱스 추가 후 검색 (Index Scan)
     */
    @Test
    //@Disabled("성능 테스트 - 필요 시 주석 해제 후 실행")
    void testSearchPerformance() {
        // 테스트할 닉네임 - 50030006번째
        String nickname = "normal2_88b5cc7a_9504";

        log.info("===== 닉네임 검색 성능 테스트 =====");
        searchAndMeasure(nickname);
        log.info("===== 테스트 종료 =====");
    }

    /**
     * 존재하지 않는 닉네임 검색 테스트
     * (최악의 경우 - Full Table Scan)
     */
    @Test
    @Disabled("성능 테스트 - 필요 시 주석 해제 후 실행")
    void testSearchNonExistentNickname() {
        log.info("===== 존재하지 않는 닉네임 검색 테스트 =====");

        String nonExistentNickname = "this_nickname_does_not_exist_12345";

        log.info("===== 닉네임 검색 성능 테스트 =====");
        searchAndMeasure(nonExistentNickname);
        log.info("===== 테스트 종료 =====");
    }

    /**
     * 검색 실행 및 시간 측정
     */
    private void searchAndMeasure(String nickname) {
        long startTime = System.currentTimeMillis();

        List<UserResponse> results = userService.searchUsersByNickname(nickname);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("검색 결과 - 닉네임: '{}', 결과: {} 건, 소요 시간: {} ms",
                nickname, results.size(), duration);
    }
}