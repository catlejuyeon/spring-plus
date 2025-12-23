package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 매니저 등록 요청 로그 엔티티
 * 매니저 등록 성공/실패 여부와 관계없이 모든 요청을 기록
 */
@Getter
@Entity
@NoArgsConstructor
@Table(name = "log")
@EntityListeners(AuditingEntityListener.class)
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long todoId;

    @Column(nullable = false)
    private Long requestUserId;

    @Column(nullable = false)
    private Long managerUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LogStatus status;

    @Column(length = 500)
    private String errorMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    public Log(Long todoId, Long requestUserId, Long managerUserId, LogStatus status, String errorMessage) {
        this.todoId = todoId;
        this.requestUserId = requestUserId;
        this.managerUserId = managerUserId;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    /**
     * 성공 로그 생성
     */
    public static Log success(Long todoId, Long requestUserId, Long managerUserId) {
        return new Log(todoId, requestUserId, managerUserId, LogStatus.SUCCESS, null);
    }

    /**
     * 실패 로그 생성
     */
    public static Log failure(Long todoId, Long requestUserId, Long managerUserId, String errorMessage) {
        return new Log(todoId, requestUserId, managerUserId, LogStatus.FAILURE, errorMessage);
    }
}
