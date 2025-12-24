package org.example.expert.domain.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 대용량 데이터 생성 테스트
 * JDBC Batch Insert를 사용하여 500만 건의 유저 데이터 생성
 */
@Slf4j
@SpringBootTest
class UserBulkInsertTest {

    @Autowired
    private DataSource dataSource;

    private static final int TOTAL_SIZE = 5_000_000;  // 500만 건
    private static final int BATCH_SIZE = 10_000;      // 1만 건씩 배치 처리

    /**
     * 500만 건의 유저 데이터 생성
     * 매번 실행되지 않도록 @Disabled 처리
     * 실행 시: @Disabled 주석 처리 후 실행
     */
    @Test
    @Disabled("대용량 데이터 생성 테스트 - 필요 시 주석 해제 후 실행")
    void bulkInsertUsers() throws SQLException {
        long startTime = System.currentTimeMillis();
        log.info("===== Bulk Insert 시작 =====");
        log.info("총 삽입할 데이터: {} 건", TOTAL_SIZE);
        log.info("배치 크기: {} 건", BATCH_SIZE);

        String sql = "INSERT INTO users (email, password, nickname, user_role, created_at, modified_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            // Auto commit 비활성화 (성능 향상)
            connection.setAutoCommit(false);

            for (int i = 1; i <= TOTAL_SIZE; i++) {
                // 랜덤 닉네임 생성 (UUID 사용으로 중복 최소화)
                String uniqueId = UUID.randomUUID().toString().substring(0, 8);
                String nickname = "user_" + uniqueId + "_" + i;
                String email = "user_" + i + "@test.com";
                String password = "password123";  // 실제로는 암호화 필요
                String userRole = UserRole.USER.name();
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                pstmt.setString(1, email);
                pstmt.setString(2, password);
                pstmt.setString(3, nickname);
                pstmt.setString(4, userRole);
                pstmt.setTimestamp(5, now);
                pstmt.setTimestamp(6, now);

                // Batch에 추가
                pstmt.addBatch();

                // BATCH_SIZE 단위로 실행
                if (i % BATCH_SIZE == 0) {
                    pstmt.executeBatch();  // 배치 실행
                    pstmt.clearBatch();     // 배치 초기화
                    connection.commit();    // 커밋

                    // 진행 상황 로그
                    log.info("진행: {}/{} ({} %)", i, TOTAL_SIZE, (i * 100 / TOTAL_SIZE));
                }
            }

            // 남은 데이터 처리
            pstmt.executeBatch();
            connection.commit();

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;  // 초 단위

            log.info("===== Bulk Insert 완료 =====");
            log.info("총 소요 시간: {} 초 ({} 분)", duration, duration / 60);
            log.info("초당 삽입 속도: {} 건/초", TOTAL_SIZE / duration);

        } catch (SQLException e) {
            log.error("Bulk Insert 실패", e);
            throw e;
        }
    }

    /**
     * 실제 DB에 있는 닉네임 샘플 조회
     * 성능 테스트에 사용할 닉네임을 찾기 위한 테스트
     */
    @Test
    @Disabled("닉네임 샘플 조회 - 필요 시 주석 해제 후 실행")
    void getSampleNicknames() throws SQLException {
        String sql = "SELECT nickname FROM users WHERE nickname LIKE 'user_%' LIMIT 10";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql)) {

            var rs = pstmt.executeQuery();

            log.info("===== 실제 DB에 있는 닉네임 샘플 =====");
            int count = 0;
            while (rs.next()) {
                String nickname = rs.getString("nickname");
                count++;
                log.info("{}. {}", count, nickname);
            }
            log.info("========================================");

        } catch (SQLException e) {
            log.error("샘플 조회 실패", e);
        }
    }

    /**
     * 총 데이터 개수 확인
     */
    @Test
    @Disabled("데이터 개수 확인 테스트 - 필요 시 주석 해제 후 실행")
    void countUsers() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM users")) {

            var rs = pstmt.executeQuery();
            if (rs.next()) {
                long count = rs.getLong(1);
                log.info("현재 users 테이블의 데이터 개수: {} 건", count);
            }

        } catch (SQLException e) {
            log.error("Count 실패", e);
        }
    }
}