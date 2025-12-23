package org.example.expert.domain.log.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.log.entity.Log;
import org.example.expert.domain.log.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그 서비스
 * REQUIRES_NEW를 사용하여 매니저 등록 트랜잭션과 독립적으로 실행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    /**
     * 매니저 등록 성공 로그 저장
     * REQUIRES_NEW: 항상 새로운 트랜잭션을 시작하여 부모 트랜잭션과 독립적으로 실행
     * 부모 트랜잭션이 롤백되어도 이 로그는 커밋됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSuccessLog(Long todoId, Long requestUserId, Long managerUserId) {
        Log managerLog = Log.success(todoId, requestUserId, managerUserId);
        logRepository.save(managerLog);
        log.info("매니저 등록 성공 로그 저장 - todoId: {}, requestUserId: {}, managerUserId: {}",
                todoId, requestUserId, managerUserId);
    }

    /**
     * 매니저 등록 실패 로그 저장
     * REQUIRES_NEW: 항상 새로운 트랜잭션을 시작하여 부모 트랜잭션과 독립적으로 실행
     * 부모 트랜잭션이 롤백되어도 이 로그는 커밋됨
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailureLog(Long todoId, Long requestUserId, Long managerUserId, String errorMessage) {
        Log managerLog = Log.failure(todoId, requestUserId, managerUserId, errorMessage);
        logRepository.save(managerLog);
        log.info("매니저 등록 실패 로그 저장 - todoId: {}, requestUserId: {}, managerUserId: {}, error: {}",
                todoId, requestUserId, managerUserId, errorMessage);
    }
}
